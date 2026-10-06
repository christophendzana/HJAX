package hsupertable.model;

import java.util.Objects;

/**
 * Une zone rectangulaire fusionnée, en coordonnées MODÈLE : cellule d'origine
 * (coin supérieur gauche) + étendue, bornes incluses.
 *
 * Immuable. Elle ne dit rien de l'adjacence VISUELLE des cellules (un tri ou
 * un déplacement de colonne peut la rompre) : c'est le rôle de
 * HTableStructureIntegrity, côté vue.
 *
 * Elle porte les valeurs d'origine de chaque cellule de la zone, indexées
 * [ligne relative][colonne relative], pour que unmerge() puisse les restituer.
 * Egalité = même géométrie 
 */
public final class MergeRegion {

    public final int originRow;
    public final int originCol;
    public final int rowSpan;
    public final int colSpan;

    private final Object[][] originalValues;

    public MergeRegion(int originRow, int originCol, int rowSpan, int colSpan,
            Object[][] originalValues) {
        if (rowSpan < 1 || colSpan < 1 || (rowSpan == 1 && colSpan == 1)) {
            throw new IllegalArgumentException(
                    "Une fusion couvre au moins deux cellules : " + rowSpan + "x" + colSpan);
        }
        this.originRow = originRow;
        this.originCol = originCol;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
        this.originalValues = copyOf(originalValues, rowSpan, colSpan);
    }

    /**
     * Copie défensive : [rowSpan][colSpan], cases manquantes à null.
     */
    private static Object[][] copyOf(Object[][] src, int rows, int cols) {
        Object[][] copy = new Object[rows][cols];
        if (src != null) {
            for (int r = 0; r < Math.min(rows, src.length); r++) {
                if (src[r] != null) {
                    System.arraycopy(src[r], 0, copy[r], 0, Math.min(cols, src[r].length));
                }
            }
        }
        return copy;
    }

    /**
     * Valeurs des cellules avant fusion : [dr][dc] = cellule (originRow + dr,
     * originCol + dc). Copie défensive.
     */
    public Object[][] getOriginalValues() {
        return copyOf(originalValues, rowSpan, colSpan);
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
    public boolean intersects(int firstRow, int firstCol, int lastRow, int lastCol) {
        return originRow <= lastRow && lastRow() >= firstRow
                && originCol <= lastCol && lastCol() >= firstCol;
    }

    /**
     * Vrai si cette région est entièrement contenue dans le rectangle donné.
     */
    public boolean isInside(int firstRow, int firstCol, int lastRow, int lastCol) {
        return originRow >= firstRow && lastRow() <= lastRow
                && originCol >= firstCol && lastCol() <= lastCol;
    }

    // ── Transformations utilisées par l'implémentation du modèle ────────────
    // Package-private : elles servent à suivre les insertions/suppressions de
    // lignes et de colonnes, pas à l'API publique.
    MergeRegion translated(int dRow, int dCol) {
        return new MergeRegion(originRow + dRow, originCol + dCol, rowSpan, colSpan, originalValues);
    }

    /**
     * Région élargie de count lignes vides (valeurs d'origine null) à
     * l'index relatif atRelRow.
     */
    MergeRegion withInsertedRows(int atRelRow, int count) {
        Object[][] values = new Object[rowSpan + count][colSpan];
        for (int r = 0; r < rowSpan; r++) {
            int target = r < atRelRow ? r : r + count;
            values[target] = originalValues[r].clone();
        }
        return new MergeRegion(originRow, originCol, rowSpan + count, colSpan, values);
    }

    MergeRegion withInsertedColumns(int atRelCol, int count) {
        Object[][] values = new Object[rowSpan][colSpan + count];
        for (int r = 0; r < rowSpan; r++) {
            for (int c = 0; c < colSpan; c++) {
                int target = c < atRelCol ? c : c + count;
                values[r][target] = originalValues[r][c];
            }
        }
        return new MergeRegion(originRow, originCol, rowSpan, colSpan + count, values);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MergeRegion other
                && originRow == other.originRow && originCol == other.originCol
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
