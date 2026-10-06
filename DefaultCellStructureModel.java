package hsupertable.model;

import hsupertable.model.structure.CellNode;
import hsupertable.model.structure.CellStructureEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import hsupertable.model.structure.CellStructureListener;
import hsupertable.model.structure.CellStructureModel;

/**
 * Implémentation par défaut de {@link CellStructureModel}.Stockage creux : une cellule n'a une entrée que si on lui a demandé un noeud
 ou un style.
 *
 * Il n'y a donc plus de tableau Cell[][] à redimensionner ni à
 resynchroniser après une insertion ou une suppression — c'était la source
 d'une classe entière de bugs dans l'ancien HDefaultTableModel.

 Reprend la logique de l'ancien MergeModel (liste de régions + index,
 décalage lors des insertions/suppressions) et de l'ancien InternalGrid
 (construction de grilles, bordure partagée).
 */
public class DefaultCellStructureModel implements CellStructureModel {

    public static final float MIN_DIVIDER_RATIO = 0.15f;
    public static final float MAX_DIVIDER_RATIO = 0.85f;

    /**
     * Source de vérité des fusions.
     */
    private final List<MergeRegion> regions = new ArrayList<>();

    /**
     * Index dérivé : cellule → fusion qui la couvre (accès O(1) au rendu).
     */
    private final Map<Long, MergeRegion> regionIndex = new HashMap<>();

    /**
     * Racines des cellules (subdivisions + styles), créées à la demande.
     */
    private final Map<Long, Node> nodes = new HashMap<>();

    private final List<CellStructureListener> listeners = new ArrayList<>();

    // =========================================================================
    // FUSIONS
    // =========================================================================
    @Override
    public MergeRegion getMergeAt(int row, int col) {
        return regionIndex.get(key(row, col));
    }

    @Override
    public List<MergeRegion> getMergeRegions() {
        return List.copyOf(regions);
    }

    @Override
    public boolean canMerge(int firstRow, int firstCol, int lastRow, int lastCol) {
        int r1 = Math.min(firstRow, lastRow), r2 = Math.max(firstRow, lastRow);
        int c1 = Math.min(firstCol, lastCol), c2 = Math.max(firstCol, lastCol);
        if (r1 < 0 || c1 < 0 || (r1 == r2 && c1 == c2)) {
            return false;
        }
        for (MergeRegion region : regions) {
            if (region.intersects(r1, c1, r2, c2) && !region.isInside(r1, c1, r2, c2)) { //chevauchement partiel
                return false;  
            }
        }
        return true;
    }

    @Override
    public List<MergeRegion> merge(int firstRow, int firstCol, int lastRow, int lastCol,
            Object[][] originalValues) {
        int r1 = Math.min(firstRow, lastRow), r2 = Math.max(firstRow, lastRow);
        int c1 = Math.min(firstCol, lastCol), c2 = Math.max(firstCol, lastCol);
        if (!canMerge(r1, c1, r2, c2)) {
            throw new IllegalArgumentException("Fusion impossible sur la zone ("
                    + r1 + "," + c1 + ")→(" + r2 + "," + c2 + ")");
        }

        List<MergeRegion> dissolved = new ArrayList<>();
        for (MergeRegion existing : new ArrayList<>(regions)) {
            if (existing.isInside(r1, c1, r2, c2)) {
                removeRegion(existing);
                dissolved.add(existing);
            }
        }

        // (I4) une cellule absorbée n'est pas subdivisée : la zone entière est
        // repliée. Le texte des feuilles est déjà dans originalValues (HTable
        // lit le TableModel, qui en garde la concaténation).
        for (Map.Entry<Long, Node> e : nodes.entrySet()) {
            int row = rowOf(e.getKey()), col = colOf(e.getKey());
            if (row >= r1 && row <= r2 && col >= c1 && col <= c2) {
                e.getValue().collapse();
            }
        }

        MergeRegion region = new MergeRegion(r1, c1, r2 - r1 + 1, c2 - c1 + 1, originalValues);
        regions.add(region);
        indexRegion(region);
        fire(CellStructureEvent.Type.MERGED, r1, c1, r2, c2);
        return dissolved;
    }

    @Override
    public MergeRegion unmerge(int row, int col) {
        MergeRegion region = getMergeAt(row, col);
        if (region == null) {
            return null;
        }
        removeRegion(region);
        fire(CellStructureEvent.Type.UNMERGED,
                region.originRow, region.originCol, region.lastRow(), region.lastCol());
        return region;
    }

    private void removeRegion(MergeRegion region) {
        regions.remove(region);
        for (int r = region.originRow; r <= region.lastRow(); r++) {
            for (int c = region.originCol; c <= region.lastCol(); c++) {
                regionIndex.remove(key(r, c));
            }
        }
    }

    private void indexRegion(MergeRegion region) {
        for (int r = region.originRow; r <= region.lastRow(); r++) {
            for (int c = region.originCol; c <= region.lastCol(); c++) {
                regionIndex.put(key(r, c), region);
            }
        }
    }

    private void replaceRegions(List<MergeRegion> newRegions) {
        regions.clear();
        regionIndex.clear();
        for (MergeRegion region : newRegions) {
            regions.add(region);
            indexRegion(region);
        }
    }

    // =========================================================================
    // SUBDIVISIONS
    // =========================================================================
    @Override
    public CellNode getCellNode(int row, int col) {
        return rootAt(row, col);
    }

    @Override
    public boolean subdivide(int row, int col, SubCellPath path,
            int splitType, float dividerRatio, Object firstValue) {
        checkSplitType(splitType);
        if (isAbsorbed(row, col)) {
            return false; // (I4)
        }
        Node target = nodeAt(row, col, path);
        if (target == null) {
            return false;
        }

        Node first = new Node();
        Node second = new Node();
        first.style = target.getStyle().copy();
        second.style = target.getStyle().copy();
        clearSharedEdge(splitType, first.style, second.style);

        if (target.isLeaf()) {
            first.value = firstValue;
        } else {
            first.adopt(target); // la subdivision existante passe dans first
        }
        target.setSplit(splitType, clampRatio(dividerRatio), first, second);

        fireCell(CellStructureEvent.Type.SUBDIVIDED, row, col);
        return true;
    }

    @Override
    public boolean subdivideGrid(int row, int col, SubCellPath path,
            int nbRows, int nbCols, Object value) {
        if (nbRows < 1 || nbCols < 1 || (nbRows == 1 && nbCols == 1)) {
            return false;
        }
        if (isAbsorbed(row, col)) {
            return false; // (I4)
        }
        Node target = nodeAt(row, col, path);
        if (target == null) {
            return false;
        }
        target.adopt(buildGrid(nbRows, nbCols, value, target.getStyle()));
        fireCell(CellStructureEvent.Type.SUBDIVIDED, row, col);
        return true;
    }

    @Override
    public Object removeSubdivision(int row, int col, SubCellPath path) {
        Node target = nodeAt(row, col, path);
        if (target == null || target.isLeaf()) {
            return null;
        }
        String text = target.collectText();
        target.collapse();
        // La racine n'a pas de valeur propre (elle vit dans le TableModel).
        target.value = path.isRoot() ? null : text;
        fireCell(CellStructureEvent.Type.SUBDIVISION_REMOVED, row, col);
        return text;
    }

    @Override
    public void setSubCellValue(int row, int col, SubCellPath path, Object value) {
        Node target = nodeAt(row, col, path);
        if (target == null || path.isRoot() || !target.isLeaf()) {
            throw new IllegalArgumentException("Pas de valeur propre pour " + path
                    + " en (" + row + "," + col + ") : seule une feuille issue d'une subdivision en a une");
        }
        target.value = value;
    }

    /**
     * Construit la grille nbRows × nbCols sous forme de noeud interne (le
     * contenu est ensuite adopté par la cellule cible). Port de
     * InternalGrid.buildGrid : on coupe d'abord en lignes (horizontal), puis
     * chaque ligne en colonnes (vertical), par imbrication.
     */
    private static Node buildGrid(int nbRows, int nbCols, Object value, CellStyle style) {
        Node first = new Node();
        Node second = new Node();
        if (style != null) {
            first.style = style.copy();
            second.style = style.copy();
        }
        Node container = new Node();

        if (nbRows > 1) {
            if (style != null) {
                clearSharedEdge(CellNode.SPLIT_HORIZONTAL, first.style, second.style);
            }
            if (nbCols == 1) {
                first.value = value;
            } else {
                first.adopt(buildGrid(1, nbCols, value, style));
            }
            if (nbRows - 1 == 1 && nbCols == 1) {
                second.value = null;
            } else if (nbRows - 1 == 1) {
                second.adopt(buildGrid(1, nbCols, null, style));
            } else {
                second.adopt(buildGrid(nbRows - 1, nbCols, null, style));
            }
            container.setSplit(CellNode.SPLIT_HORIZONTAL, clampRatio(1.0f / nbRows), first, second);
        } else {
            // L'ancien code appelait ici clearSharedEdge(SPLIT_HORIZONTAL, ...)
            // par erreur : le séparateur d'une coupe en colonnes est vertical.
            if (style != null) {
                clearSharedEdge(CellNode.SPLIT_VERTICAL, first.style, second.style);
            }
            first.value = value;
            if (nbCols - 1 > 1) {
                second.adopt(buildGrid(1, nbCols - 1, null, style));
            } else {
                second.value = null;
            }
            container.setSplit(CellNode.SPLIT_VERTICAL, clampRatio(1.0f / nbCols), first, second);
        }
        return container;
    }

    /**
     * Supprime, sur chacune des deux sous-cellules issues d'une coupe, la
     * bordure du côté qui devient la ligne de partage interne : ce côté
     * n'appartient plus au contour extérieur de la cellule mère.
     */
    private static void clearSharedEdge(int splitType, CellStyle firstStyle, CellStyle secondStyle) {
        if (splitType == CellNode.SPLIT_HORIZONTAL) {
            firstStyle.setBorderBottomThickness(0f);
            secondStyle.setBorderTopThickness(0f);
        } else {
            firstStyle.setBorderRightThickness(0f);
            secondStyle.setBorderLeftThickness(0f);
        }
    }

    private static float clampRatio(float ratio) {
        return Math.max(MIN_DIVIDER_RATIO, Math.min(MAX_DIVIDER_RATIO, ratio));
    }

    private static void checkSplitType(int splitType) {
        if (splitType != CellNode.SPLIT_VERTICAL && splitType != CellNode.SPLIT_HORIZONTAL) {
            throw new IllegalArgumentException("Type de subdivision invalide : " + splitType);
        }
    }

    // =========================================================================
    // STYLE
    // =========================================================================
    @Override
    public CellStyle getCellStyle(int row, int col, SubCellPath path) {
        return requireNode(row, col, path).getStyle();
    }

    @Override
    public void setCellStyle(int row, int col, SubCellPath path, CellStyle style) {
        requireNode(row, col, path).style = Objects.requireNonNull(style, "style");
    }

    @Override
    public void clearCellStyle(int row, int col, SubCellPath path) {
        requireNode(row, col, path).style = null; // recréé neutre à la prochaine lecture
    }

    // =========================================================================
    // SUIVI DES CHANGEMENTS STRUCTURELS DU TABLEMODEL
    // =========================================================================
    @Override
    public void rowsInserted(int firstRow, int lastRow) {
        int n = checkRange(firstRow, lastRow);
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originRow >= firstRow) {
                updated.add(r.translated(n, 0));
            } else if (r.lastRow() >= firstRow) {
                // traversée : absorbe les nouvelles lignes plutôt que de les
                // laisser orphelines sous la fusion
                updated.add(r.withInsertedRows(firstRow - r.originRow, n));
            } else {
                updated.add(r);
            }
        }
        replaceRegions(updated);
        remapNodes(row -> row >= firstRow ? row + n : row, col -> col);
        fire(CellStructureEvent.Type.SHIFTED, firstRow, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void columnsInserted(int firstCol, int lastCol) {
        int n = checkRange(firstCol, lastCol);
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originCol >= firstCol) {
                updated.add(r.translated(0, n));
            } else if (r.lastCol() >= firstCol) {
                updated.add(r.withInsertedColumns(firstCol - r.originCol, n));
            } else {
                updated.add(r);
            }
        }
        replaceRegions(updated);
        remapNodes(row -> row, col -> col >= firstCol ? col + n : col);
        fire(CellStructureEvent.Type.SHIFTED, 0, firstCol, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public List<MergeRegion> rowsRemoved(int firstRow, int lastRow) {
        int n = checkRange(firstRow, lastRow);
        List<MergeRegion> dissolved = new ArrayList<>();
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originRow <= lastRow && r.lastRow() >= firstRow) {
                dissolved.add(r); // touchait une ligne supprimée : disparaît
            } else if (r.originRow > lastRow) {
                updated.add(r.translated(-n, 0));
            } else {
                updated.add(r);
            }
        }
        replaceRegions(updated);
        remapNodes(row -> row > lastRow ? row - n : (row >= firstRow ? -1 : row), col -> col);
        fire(CellStructureEvent.Type.SHIFTED, firstRow, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        return dissolved;
    }

    @Override
    public List<MergeRegion> columnsRemoved(int firstCol, int lastCol) {
        int n = checkRange(firstCol, lastCol);
        List<MergeRegion> dissolved = new ArrayList<>();
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originCol <= lastCol && r.lastCol() >= firstCol) {
                dissolved.add(r);
            } else if (r.originCol > lastCol) {
                updated.add(r.translated(0, -n));
            } else {
                updated.add(r);
            }
        }
        replaceRegions(updated);
        remapNodes(row -> row, col -> col > lastCol ? col - n : (col >= firstCol ? -1 : col));
        fire(CellStructureEvent.Type.SHIFTED, 0, firstCol, Integer.MAX_VALUE, Integer.MAX_VALUE);
        return dissolved;
    }

    @Override
    public void clear() {
        regions.clear();
        regionIndex.clear();
        nodes.clear();
        fire(CellStructureEvent.Type.RESET, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    private static int checkRange(int first, int last) {
        if (first < 0 || last < first) {
            throw new IllegalArgumentException("Intervalle invalide : " + first + ".." + last);
        }
        return last - first + 1;
    }

    /**
     * Re-clé les racines : un index transformé négatif supprime l'entrée.
     */
    private void remapNodes(java.util.function.IntUnaryOperator rowMap,
            java.util.function.IntUnaryOperator colMap) {
        Map<Long, Node> moved = new HashMap<>(nodes.size());
        for (Map.Entry<Long, Node> e : nodes.entrySet()) {
            int row = rowMap.applyAsInt(rowOf(e.getKey()));
            int col = colMap.applyAsInt(colOf(e.getKey()));
            if (row >= 0 && col >= 0) {
                moved.put(key(row, col), e.getValue());
            }
        }
        nodes.clear();
        nodes.putAll(moved);
    }

    // =========================================================================
    // OBSERVATION
    // =========================================================================
    @Override
    public void addCellStructureListener(CellStructureListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    @Override
    public void removeCellStructureListener(CellStructureListener l) {
        listeners.remove(l);
    }

    private void fire(CellStructureEvent.Type type, int r1, int c1, int r2, int c2) {
        if (listeners.isEmpty()) {
            return;
        }
        CellStructureEvent event = new CellStructureEvent(this, type, r1, c1, r2, c2);
        for (CellStructureListener l : new ArrayList<>(listeners)) {
            l.structureChanged(event);
        }
    }

    /**
     * Événement sur une cellule : la zone de sa fusion si elle en a une.
     */
    private void fireCell(CellStructureEvent.Type type, int row, int col) {
        MergeRegion region = getMergeAt(row, col);
        if (region != null) {
            fire(type, region.originRow, region.originCol, region.lastRow(), region.lastCol());
        } else {
            fire(type, row, col, row, col);
        }
    }

    // =========================================================================
    // INTERNE
    // =========================================================================
    private Node rootAt(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IndexOutOfBoundsException("Cellule invalide : (" + row + "," + col + ")");
        }
        return nodes.computeIfAbsent(key(row, col), k -> new Node());
    }

    /**
     * Noeud désigné, ou null si path ne correspond plus à la structure.
     */
    private Node nodeAt(int row, int col, SubCellPath path) {
        return (Node) Objects.requireNonNull(path, "path").resolve(rootAt(row, col));
    }

    private Node requireNode(int row, int col, SubCellPath path) {
        Node node = nodeAt(row, col, path);
        if (node == null) {
            throw new IllegalArgumentException("Aucun noeud " + path + " en (" + row + "," + col + ")");
        }
        return node;
    }

    private static long key(int row, int col) {
        return ((long) row << 32) | (col & 0xFFFFFFFFL);
    }

    private static int rowOf(long key) {
        return (int) (key >> 32);
    }

    private static int colOf(long key) {
        return (int) key;
    }

    /**
     * Noeud modifiable. Seul ce modèle en détient des références modifiables ;
     * le reste du monde ne voit que {@link CellNode}.
     */
    private static final class Node implements CellNode {

        Object value;
        CellStyle style;          // créé à la première lecture
        int splitType;
        float ratio;
        Node first;
        Node second;

        @Override
        public boolean isLeaf() {
            return first == null;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public CellStyle getStyle() {
            if (style == null) {
                style = new CellStyle();
            }
            return style;
        }

        @Override
        public int getSplitType() {
            return splitType;
        }

        @Override
        public float getDividerRatio() {
            return ratio;
        }

        @Override
        public CellNode getFirst() {
            return first;
        }

        @Override
        public CellNode getSecond() {
            return second;
        }

        void setSplit(int splitType, float ratio, Node first, Node second) {
            this.splitType = splitType;
            this.ratio = ratio;
            this.first = first;
            this.second = second;
            this.value = null;
        }

        /**
         * Prend la subdivision de other (le style et l'identité restent les
         * miens).
         */
        void adopt(Node other) {
            setSplit(other.splitType, other.ratio, other.first, other.second);
        }

        void collapse() {
            first = null;
            second = null;
        }
    }
}
