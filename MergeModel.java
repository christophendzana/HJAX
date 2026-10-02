package hsupertable.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * MergeModel — source de vérité des fusions de cellules d'un HSuperTable.
 *
 * Remplace l'ancien schéma où chaque Cell portait spanRow/spanCol/mergeOrigin.
 * Une fusion est ici une MergeRegion (cellule d'origine + étendue), stocké une
 * seule fois dans une liste (source de vérité), et retrouvable en O(1) depuis
 * n'importe quelle cellule via un index dérivé.
 *
 * Ne connaît ni HSuperTable ni Cell — uniquement des coordonnées et des
 * dimensions, fournies explicitement par l'appelant (HSuperDefaultTableModel).
 * Les subdivisions internes (InternalGrid) restent hors périmètre : c'est un
 * mécanisme différent (partition visuelle locale d'une cellule, pas fusion de
 * cellules du tableau).
 *
 * @author FIDELE
 * @version 1.0
 */
public class MergeModel {

    // =========================================================================
    // ÉTAT
    // =========================================================================
    /**
     * Source de vérité — une fusion par entrée, jamais de doublon ni de
     * chevauchement entre deux régions.
     */
    private final List<MergeRegion> regions = new ArrayList<>();

    /**
     * Index dérivé — regionIndex[row][col] pointe vers la fusion qui couvre
     * cette cellule, ou null si la cellule n'est pas fusionnée.
     */
    private MergeRegion[][] regionIndex;

    public MergeModel(int rowCount, int columnCount) {
        this.regionIndex = newIndex(rowCount, columnCount);
    }

    // =========================================================================
    // LECTURE
    // =========================================================================
    /**
     * Retourne la fusion couvrant (row, col), ou null si non fusionnée. Accès
     * O(1).
     */
    public MergeRegion getMergeAt(int row, int col) {
        if (!isValid(row, col)) {
            return null;
        }
        return regionIndex[row][col];
    }

    /**
     * Vrai si (row, col) est la cellule principale d'une fusion.
     */
    public boolean isMergeOrigin(int row, int col) {
        MergeRegion r = getMergeAt(row, col);
        return r != null && r.originRow == row && r.originCol == col;
    }

    /**
     * Vrai si (row, col) est absorbée par une fusion — le renderer doit la
     * sauter, ce n'est pas la cellule principale.
     */
    public boolean isAbsorbed(int row, int col) {
        MergeRegion r = getMergeAt(row, col);
        return r != null && !(r.originRow == row && r.originCol == col);
    }

    // =========================================================================
    // ÉCRITURE
    // =========================================================================
    /**
     * Crée une fusion sur la zone (rowStart,colStart) → (rowEnd,colEnd), bornes
     * incluses. Toute fusion existante qui chevauche la zone est d'abord
     * retirée.
     *
     * @return les fusions retirées — l'appelant en a besoin pour
     * redistribuer/concaténer les valeurs des cellules concernées avant
     * qu'elles soient écrasées par la nouvelle fusion.
     */
    public List<MergeRegion> merge(int rowStart, int colStart, int rowEnd, int colEnd) {
        List<MergeRegion> removed = new ArrayList<>();
        for (MergeRegion existing : new ArrayList<>(regions)) {
            if (existing.intersects(rowStart, colStart, rowEnd, colEnd)) {
                removeRegion(existing);
                removed.add(existing);
            }
        }

        MergeRegion region = new MergeRegion(
                rowStart, colStart,
                rowEnd - rowStart + 1,
                colEnd - colStart + 1);
        regions.add(region);
        applyToIndex(region);
        return removed;
    }

    /**
     * Retire la fusion couvrant (row, col). Ne fait rien si la cellule n'est
     * pas fusionnée.
     *
     * @return la fusion retirée, ou null si (row, col) n'était pas fusionnée.
     */
    public MergeRegion unmerge(int row, int col) {
        MergeRegion region = getMergeAt(row, col);
        if (region == null) {
            return null;
        }
        removeRegion(region);
        return region;
    }

    private void removeRegion(MergeRegion region) {
        regions.remove(region);
        for (int r = region.originRow; r <= region.lastRow(); r++) {
            for (int c = region.originCol; c <= region.lastCol(); c++) {
                if (isValid(r, c)) {
                    regionIndex[r][c] = null;
                }
            }
        }
    }

    private void applyToIndex(MergeRegion region) {
        for (int r = region.originRow; r <= region.lastRow(); r++) {
            for (int c = region.originCol; c <= region.lastCol(); c++) {
                if (isValid(r, c)) {
                    regionIndex[r][c] = region;
                }
            }
        }
    }

    // =========================================================================
    // SUIVI DES CHANGEMENTS STRUCTURELS
    // Une fusion est ancrée à des coordonnées — insérer/supprimer une ligne
    // ou colonne doit décaler ces coordonnées, pas juste reconstruire
    // l'index sur les nouvelles dimensions.
    // =========================================================================
    /**
     * À appeler après l'insertion d'une ligne vide à l'index insertedRow. Une
     * fusion qui commençait à ou après insertedRow voit son origine décalée
     * d'une ligne ; une fusion qui s'étendait déjà par-dessus insertedRow voit
     * son rowSpan augmenter d'une ligne (absorbe la nouvelle ligne plutôt que
     * de la laisser orpheline sous la fusion).
     */
    public void onRowInserted(int insertedRow, int newRowCount, int columnCount) {
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originRow >= insertedRow) {
                updated.add(new MergeRegion(r.originRow + 1, r.originCol, r.rowSpan, r.colSpan));
            } else if (r.lastRow() >= insertedRow) {
                updated.add(new MergeRegion(r.originRow, r.originCol, r.rowSpan + 1, r.colSpan));
            } else {
                updated.add(r);
            }
        }
        replaceAll(updated, newRowCount, columnCount);
    }

    /**
     * À appeler après la suppression de la ligne removedRow. Toute fusion qui
     * touchait cette ligne disparaît entièrement (comportement identique à
     * l'ancien defuseRow) — à l'appelant de redistribuer les valeurs avant
     * d'appeler cette méthode si nécessaire.
     */
    public void onRowRemoved(int removedRow, int newRowCount, int columnCount) {
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originRow <= removedRow && r.lastRow() >= removedRow) {
                continue; // touchait la ligne supprimée : disparaît
            }
            if (r.originRow > removedRow) {
                updated.add(new MergeRegion(r.originRow - 1, r.originCol, r.rowSpan, r.colSpan));
            } else {
                updated.add(r);
            }
        }
        replaceAll(updated, newRowCount, columnCount);
    }

    /**
     * Symétrique de onRowInserted, pour une colonne.
     */
    public void onColumnInserted(int insertedCol, int rowCount, int newColumnCount) {
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originCol >= insertedCol) {
                updated.add(new MergeRegion(r.originRow, r.originCol + 1, r.rowSpan, r.colSpan));
            } else if (r.lastCol() >= insertedCol) {
                updated.add(new MergeRegion(r.originRow, r.originCol, r.rowSpan, r.colSpan + 1));
            } else {
                updated.add(r);
            }
        }
        replaceAll(updated, rowCount, newColumnCount);
    }

    /**
     * Symétrique de onRowRemoved, pour une colonne.
     */
    public void onColumnRemoved(int removedCol, int rowCount, int newColumnCount) {
        List<MergeRegion> updated = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.originCol <= removedCol && r.lastCol() >= removedCol) {
                continue; // touchait la colonne supprimée : disparaît
            }
            if (r.originCol > removedCol) {
                updated.add(new MergeRegion(r.originRow, r.originCol - 1, r.rowSpan, r.colSpan));
            } else {
                updated.add(r);
            }
        }
        replaceAll(updated, rowCount, newColumnCount);
    }

    private void replaceAll(List<MergeRegion> newRegions, int rowCount, int columnCount) {
        regions.clear();
        regions.addAll(newRegions);
        regionIndex = newIndex(rowCount, columnCount);
        for (MergeRegion r : regions) {
            applyToIndex(r);
        }
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================
    private MergeRegion[][] newIndex(int rowCount, int columnCount) {
        return new MergeRegion[Math.max(rowCount, 0)][Math.max(columnCount, 0)];
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < regionIndex.length
                && regionIndex.length > 0
                && col >= 0 && col < regionIndex[0].length;
    }

    /**
     * Redimensionne les structures internes sans décaler aucune région — pour
     * un ajout en fin de tableau (addRow/addColumn) ou un redimensionnement
     * direct (setRowCount/setColumnCount) qui ne déplace aucune ligne/colonne
     * existante. Les régions qui déborderaient des nouvelles dimensions sont
     * retirées (pas de redistribution de valeurs ici — à la charge de
     * l'appelant s'il en a besoin).
     */
    public void resizeKeepingRegions(int newRowCount, int newColumnCount) {
        List<MergeRegion> kept = new ArrayList<>(regions.size());
        for (MergeRegion r : regions) {
            if (r.lastRow() < newRowCount && r.lastCol() < newColumnCount) {
                kept.add(r);
            }
        }
        replaceAll(kept, newRowCount, newColumnCount);
    }

    // =========================================================================
    // MergeRegion — value object immuable représentant une fusion
    // =========================================================================
    /**
     * Une zone rectangulaire fusionnée : cellule d'origine + étendue. Bornes
     * incluses, comme HSuperTable.CellRange.
     */
    public static final class MergeRegion {

        public final int originRow;
        public final int originCol;
        public final int rowSpan;
        public final int colSpan;

        public MergeRegion(int originRow, int originCol, int rowSpan, int colSpan) {
            this.originRow = originRow;
            this.originCol = originCol;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }

        public int lastRow() {
            return originRow + rowSpan - 1;
        }

        public int lastCol() {
            return originCol + colSpan - 1;
        }

        public boolean contains(int row, int col) {
            return row >= originRow && row <= lastRow()
                    && col >= originCol && col <= lastCol();
        }

        /**
         * Vrai si cette région chevauche le rectangle donné (bornes incluses).
         */
        public boolean intersects(int rowStart, int colStart, int rowEnd, int colEnd) {
            return originRow <= rowEnd && lastRow() >= rowStart
                    && originCol <= colEnd && lastCol() >= colStart;
        }

        

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MergeRegion)) {
                return false;
            }
            MergeRegion other = (MergeRegion) o;
            return originRow == other.originRow && originCol == other.originCol
                    && rowSpan == other.rowSpan && colSpan == other.colSpan;
        }

        @Override
        public int hashCode() {
            return Objects.hash(originRow, originCol, rowSpan, colSpan);
        }

        @Override
        public String toString() {
            return "MergeRegion[origin=(" + originRow + "," + originCol
                    + "), span=" + rowSpan + "x" + colSpan + "]";
        }
    }
    
    /**
         * Vue non modifiable de toutes les fusions actuelles.
     * @return 
         */
        public List<MergeRegion> getAllRegions() {
            return Collections.unmodifiableList(regions);
        }
        
        public  boolean canMergeSelection(int rowStart, int colStart,
            int rowEnd, int colEnd, MergeModel mergeModel) {
        if (rowStart == rowEnd && colStart == colEnd) {
            return false;
        }
        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                MergeRegion region = this.getMergeAt(r, c);
                if (region == null) continue;
                boolean fullyContained =
                        region.originRow >= rowStart && region.lastRow() <= rowEnd
                        && region.originCol >= colStart && region.lastCol() <= colEnd;
                if (!fullyContained) return false;
            }
        }
        return true;
    }
        
}
