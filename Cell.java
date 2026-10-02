package hsupertable.model;

import java.awt.Point;

/**
 * Unité de base de la grille.
 *
 * @author FIDELE
 */
public class Cell {

    public Object value;

    /**
     * Métadonnées visuelles : couleurs, bordures, alignement, marges,
     * direction, formule.
     */
    public HCellModel style;

    /**
     * Nombre de lignes occupées. 1 = normale ou principale sur 1 ligne N > 1 =
     * principale d'une fusion sur N lignes; 0 = absorbée
     */
    public int spanRow;

    /**
     * Nombre de colonnes occupées. Même logique que spanRow.
     */
    public int spanCol;

    // HSuperTableStyle style = t.getTableStyle();
    // HCellModel cModel = model.getCellModel(row, col);
    /**
     * Adresse de la cellule principale de la fusion. null -> cette cellule est
     * normale ou est elle-même la principale Point(r,c) -> la principale est en
     * (r,c).
     */
    public Point mergeOrigin;

    /**
     * Subdivision interne de la cellule
     */
    public InternalGrid internalGrid;

    /**
     * Valeurs individuelles des cellules avant fusion. Stockées dans la cellule
     * principale au moment de mergeCells(). Utilisées par unmergeCell() pour
     * redistribuer les contenus. null si la cellule n'est pas fusionnée ou si
     * les valeurs étaient vides. NB: mergedValues[dr][dc] = valeur de la
     * cellule (row+dr, col+dc)
     */
    public Object[][] mergedValues;

    /**
     * Crée une cellule normale avec toutes les valeurs par défaut.
     */
    public Cell() {
        this.style = new HCellModel();
        this.spanRow = 1;
        this.spanCol = 1;
        this.mergeOrigin = null;
    }

    /**
     * Vrai si absorbée par une fusion -> le renderer doit la sauter.
     *
     * @return
     */
    public boolean isAbsorbed() {
        return spanRow == 0 && spanCol == 0;
    }

    public boolean hasInternalGrid() {
        return internalGrid != null;
    }

    /**
     * Vrai si principale d'une fusion de taille > 1×1.
     *
     * @return
     */
    public boolean isMerged() {
        return spanRow > 1 || spanCol > 1;
    }

    /**
     * Vrai si dans l'état normal (ni fusionnée, ni absorbée).
     *
     * @return
     */
    public boolean isNormal() {
        return spanRow == 1 && spanCol == 1 && mergeOrigin == null;
    }

    /**
     * Remet la cellule dans l'état normal — utilisé lors de la défusion.
     */
    public void resetSpan() {
        this.spanRow = 1;
        this.spanCol = 1;
        this.mergeOrigin = null;
    }

    /**
     * Copie profonde.Utilisée lors du déplacement de lignes/colonnes pour
     * éviter que deux cases de la grille partagent le même objet.
     *
     * @return
     */
    public Cell copy() {
        Cell copy = new Cell();
        copy.style = this.style.copy();
        copy.spanRow = this.spanRow;
        copy.spanCol = this.spanCol;
        copy.value = this.value;
        copy.mergeOrigin = (this.mergeOrigin != null)
                ? new Point(this.mergeOrigin.x, this.mergeOrigin.y)
                : null;

        // Copie de mergedValues si présent
        if (this.mergedValues != null) {
            int rows = this.mergedValues.length;
            int cols = this.mergedValues[0].length;
            copy.mergedValues = new Object[rows][cols];
            for (int r = 0; r < rows; r++) {
                System.arraycopy(this.mergedValues[r], 0,
                        copy.mergedValues[r], 0, cols);
            }
        }

        return copy;
    }

    /**
     * Replie la subdivision de cette cellule en une seule valeur, et la
     * supprime.
     * @return the value of subdivision
     */
    public String removeSubdivision() {
        String finalValue = InternalGrid.valueOf(this);
        this.value = finalValue;
        this.internalGrid = null;
        return finalValue;
    }

    @Override
    public String toString() {
        if (isAbsorbed()) {
            return "Cell[ABSORBED origin=" + mergeOrigin + "]";
        }
        if (isMerged()) {
            return "Cell[MERGED " + spanRow + "×" + spanCol + "]";
        }
        return "Cell[normal]";
    }
}
