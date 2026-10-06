package hsupertable.geometry;

import hsupertable.HTable;
import hsupertable.model.structure.CellNode;
import hsupertable.model.MergeRegion;
import hsupertable.model.SubCellPath;

import java.awt.Point;
import java.awt.Rectangle;
import hsupertable.model.structure.CellStructureModel;

/**
 * HTableGeometry — calcul des rectangles occupés par les cellules (fusion
 * comprise) et les sous-cellules internes (subdivision comprise), et
 * hit-testing associé.
 *
 * Ne dépend d'aucune classe de rendu (HBasicTableUI) — uniquement de HTable et
 * de son HCellStructureModel. Utilisable par n'importe quel consommateur ayant
 * besoin de connaître la géométrie d'une cellule, indépendamment de la façon
 * dont elle est peinte.
 *
 * Les rectangles sont toujours DÉRIVÉS à la demande (jamais stockés) : après
 * un tri, une insertion ou une fusion, un rectangle conservé serait faux.
 */
public final class TableGeometry {

    private TableGeometry() {
    }

    /**
     * Rectangle d'une fusion.
     */
    public static Rectangle computeMergedRect(HTable t, MergeRegion region) {
        return computeMergedRect(t, region.originRow, region.originCol, region.rowSpan, region.colSpan);
    }

    /**
     * Rectangle d'une fusion, à partir des coordonnées MODÈLE de sa cellule
     * principale.
     */
    public static Rectangle computeMergedRect(HTable t, int modelRow, int modelCol, int rSpan, int cSpan) {
        int minViewRow = Integer.MAX_VALUE, maxViewRow = Integer.MIN_VALUE;
        for (int dr = 0; dr < rSpan; dr++) {
            int vr = t.convertRowIndexToView(modelRow + dr);
            minViewRow = Math.min(minViewRow, vr);
            maxViewRow = Math.max(maxViewRow, vr);
        }
        int minViewCol = Integer.MAX_VALUE, maxViewCol = Integer.MIN_VALUE;
        for (int dc = 0; dc < cSpan; dc++) {
            int vc = t.convertColumnIndexToView(modelCol + dc);
            minViewCol = Math.min(minViewCol, vc);
            maxViewCol = Math.max(maxViewCol, vc);
        }
        Rectangle topLeft = t.getCellRect(minViewRow, minViewCol, false);
        Rectangle bottomRight = t.getCellRect(maxViewRow, maxViewCol, false);
        return topLeft.union(bottomRight);
    }

    /**
     * Rectangle réel occupé par (row, col), fusion prise en compte. Les indices
     * sont des indices VUE ; la structure est lue en indices modèle. Une cellule
     * absorbée garde son propre rectangle : c'est à l'appelant de remonter à
     * l'origine (voir HTable.resolvePoint).
     */
    public static Rectangle getCellBounds(HTable t, int row, int col) {
        int modelRow = t.toModelRow(row);
        int modelCol = t.toModelColumn(col);
        if (modelRow >= 0 && modelCol >= 0) {
            CellStructureModel structure = t.getStructureModel();
            if (structure.isMergeOrigin(modelRow, modelCol)) {
                return computeMergedRect(t, structure.getMergeAt(modelRow, modelCol));
            }
        }
        return t.getCellRect(row, col, false);
    }

    /**
     * Découpe rect selon la subdivision du noeud (non feuille).
     */
    public static Rectangle[] computeInternalRects(Rectangle rect, CellNode node) {
        Rectangle first = new Rectangle(rect);
        Rectangle second = new Rectangle(rect);
        float ratio = node.getDividerRatio();

        if (node.getSplitType() == CellNode.SPLIT_VERTICAL) {
            int splitX = (int) (rect.width * ratio);
            first.width = splitX;
            second.x = rect.x + splitX;
            second.width = rect.width - splitX;
        } else {
            int splitY = (int) (rect.height * ratio);
            first.height = splitY;
            second.y = rect.y + splitY;
            second.height = rect.height - splitY;
        }
        return new Rectangle[]{first, second};
    }

    /**
     * Sous-cellule sous un point souris : la feuille la plus profonde contenant
     * le point (ou la cellule entière si elle n'est pas subdivisée). null si le
     * point n'est dans aucune cellule.
     */
    public static InternalCellHit getInternalCellAt(HTable t, Point point) {
        int[] resolved = t.resolvePoint(point);
        int row = resolved[0];
        int col = resolved[1];
        if (row < 0 || col < 0) {
            return null;
        }
        int modelRow = t.toModelRow(row);
        int modelCol = t.toModelColumn(col);
        if (modelRow < 0 || modelCol < 0) {
            return null;
        }

        Rectangle rect = getCellBounds(t, row, col);
        if (!rect.contains(point)) {
            return null;
        }

        CellNode node = t.getStructureModel().getCellNode(modelRow, modelCol);
        SubCellPath path = SubCellPath.ROOT;
        while (!node.isLeaf()) {
            Rectangle[] parts = computeInternalRects(rect, node);
            if (parts[0].contains(point)) {
                node = node.getFirst();
                rect = parts[0];
                path = path.child(false);
            } else if (parts[1].contains(point)) {
                node = node.getSecond();
                rect = parts[1];
                path = path.child(true);
            } else {
                break;
            }
        }
        return new InternalCellHit(modelRow, modelCol, path);
    }

    /**
     * Rectangle (en coordonnées de la table) du noeud désigné par hit, calculé
     * à la demande contre la structure courante.
     *
     * @return null si hit ne correspond plus à la structure (cellule masquée,
     * chemin périmé).
     */
    public static Rectangle boundsOf(HTable t, InternalCellHit hit) {
        if (hit == null) {
            return null;
        }
        int viewRow = t.convertRowIndexToView(hit.row);
        int viewCol = t.convertColumnIndexToView(hit.col);
        if (viewRow < 0 || viewCol < 0) {
            return null;
        }
        Rectangle rect = getCellBounds(t, viewRow, viewCol);
        CellNode node = t.getStructureModel().getCellNode(hit.row, hit.col);
        for (int level = 0; level < hit.path.depth(); level++) {
            if (node.isLeaf()) {
                return null;
            }
            Rectangle[] parts = computeInternalRects(rect, node);
            boolean second = hit.path.stepAt(level);
            rect = second ? parts[1] : parts[0];
            node = second ? node.getSecond() : node.getFirst();
        }
        return rect;
    }

    /**
     * Résultat d'une détection de sous-cellule : une ADRESSE (cellule en
     * coordonnées modèle + chemin dans l'arbre de subdivision), jamais un
     * rectangle ni une référence d'objet. Immuable.
     */
    public static final class InternalCellHit {

        public final int row;
        public final int col;
        public final SubCellPath path;

        public InternalCellHit(int row, int col, SubCellPath path) {
            this.row = row;
            this.col = col;
            this.path = path;
        }

        /**
         * Vrai si le hit désigne une sous-cellule (et non la cellule entière).
         */
        public boolean isSubCell() {
            return !path.isRoot();
        }

        /**
         * Hit du fils FIRST (false) ou SECOND (true).
         */
        public InternalCellHit child(boolean second) {
            return new InternalCellHit(row, col, path.child(second));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof InternalCellHit h && row == h.row && col == h.col && path.equals(h.path);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(row, col, path);
        }

        @Override
        public String toString() {
            return "InternalCellHit[(" + row + "," + col + ") " + path + "]";
        }
    }
}
