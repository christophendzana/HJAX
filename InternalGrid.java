/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsupertable.model;

/**
 * Représente une subdivision interne d'une cellule. Une InternalGrid permet de
 * partitionner localement l'espace d'une cellule en deux sous-cellules sans
 * modifier la structure globale du tableau.
 *
 * @author FIDELE
 */
public class InternalGrid {

    /**
     * Division verticale :
     */
    public static final int SPLIT_VERTICAL = 0;

    /**
     * Division horizontale :
     */
    public static final int SPLIT_HORIZONTAL = 1;
    
    /**
     * Division diagonale
     */
    
    public static final int SPLit_DIAGONAL = 2;

    /**
     * Type de subdivision.
     */
    private int splitType;

    /**
     * Ratio de séparation entre les deux sous-cellules.
     *
     * Exemple : 0.5f = 50% / 50%
     */
    private float dividerRatio;

    /**
     * Première sous-cellule.
     */
    private Cell firstCell;

    /**
     * Deuxième sous-cellule.
     */
    private Cell secondCell;

    /**
     * Crée une subdivision avec un ratio personnalisé.
     *
     * @param splitType type de subdivision
     * @param dividerRatio ratio du séparateur
     * @param firstCell première sous-cellule
     * @param secondCell deuxième sous-cellule
     */
    public InternalGrid(int splitType,
            float dividerRatio,
            Cell firstCell,
            Cell secondCell) {

        setSplitType(splitType);
        setDividerRatio(dividerRatio);

        this.firstCell = firstCell;
        this.secondCell = secondCell;
    }

    /**
     * Crée une subdivision 50 / 50.
     *
     * @param splitType type de subdivision
     * @param firstCell première sous-cellule
     * @param secondCell deuxième sous-cellule
     */
    public InternalGrid(int splitType,
            Cell firstCell,
            Cell secondCell) {

        this(splitType, 0.5f, firstCell, secondCell);
    }

    /**
     * Retourne le type de subdivision.
     *
     * @return SPLIT_VERTICAL ou SPLIT_HORIZONTAL
     */
    public int getSplitType() {
        return splitType;
    }

    /**
     * Définit le type de subdivision.
     *
     * @param splitType type de subdivision
     */
    public void setSplitType(int splitType) {

        if (splitType != SPLIT_VERTICAL
                && splitType != SPLIT_HORIZONTAL) {

            throw new IllegalArgumentException("Type de subdivision invalide : " + splitType);
        }

        this.splitType = splitType;
    }

    /**
     * Retourne le ratio du séparateur.
     *
     * @return ratio entre 0f et 1f
     */
    public float getDividerRatio() {
        return dividerRatio;
    }

    /**
     * Définit le ratio du séparateur.
     *
     * @param dividerRatio ratio entre 0f et 1f
     */
    public void setDividerRatio(float dividerRatio) {

        if (dividerRatio <= 0f || dividerRatio >= 1f) {
            throw new IllegalArgumentException("Le dividerRatio doit être compris entre 0 et 1.");
        }

        this.dividerRatio = dividerRatio;
    }

    /**
     * Retourne la première sous-cellule.
     *
     * @return première sous-cellule
     */
    public Cell getFirstCell() {
        return firstCell;
    }

    /**
     * Définit la première sous-cellule.
     *
     * @param firstCell première sous-cellule
     */
    public void setFirstCell(Cell firstCell) {
        this.firstCell = firstCell;
    }

    /**
     * Retourne la deuxième sous-cellule.
     *
     * @return deuxième sous-cellule
     */
    public Cell getSecondCell() {
        return secondCell;
    }

    /**
     * Définit la deuxième sous-cellule.
     *
     * @param secondCell deuxième sous-cellule
     */
    public void setSecondCell(Cell secondCell) {
        this.secondCell = secondCell;
    }

    /**
     * Vrai si la subdivision est verticale.
     *
     * @return true si verticale
     */
    public boolean isVerticalSplit() {
        return splitType == SPLIT_VERTICAL;
    }

    /**
     * Vrai si la subdivision est horizontale.
     *
     * @return true si horizontale
     */
    public boolean isHorizontalSplit() {
        return splitType == SPLIT_HORIZONTAL;
    }

    /**
     * Construit une nouvelle subdivision nbRows × nbCols. Pure — ne dépend
     * d'aucun état du tableau.
     */
    public static InternalGrid buildGrid(int nbRows, int nbCols, Object value, HCellModel style) {
        if (nbRows == 1 && nbCols == 1) {
            return null;
        }
        if (nbRows > 1) {
            float ratio = clampRatio(1.0f / nbRows);
            Cell first = new Cell();
            Cell second = new Cell();
            if (style != null) {
                first.style = style.copy();
                second.style = style.copy();
                clearSharedEdge(SPLIT_HORIZONTAL, first.style, second.style);
            }
            if (nbCols == 1) {
                first.value = value;
            } else {
                first.internalGrid = buildGrid(1, nbCols, value, style);
            }
            if (nbRows - 1 == 1 && nbCols == 1) {
                second.value = null;
            } else if (nbRows - 1 == 1) {
                second.internalGrid = buildGrid(1, nbCols, null, style);
            } else {
                second.internalGrid = buildGrid(nbRows - 1, nbCols, null, style);
            }
            return new InternalGrid(SPLIT_HORIZONTAL, ratio, first, second);
        } else {
            float ratio = clampRatio(1.0f / nbCols);
            Cell first = new Cell();
            Cell second = new Cell();
            if (style != null) {
                first.style = style.copy();
                second.style = style.copy();
                clearSharedEdge(SPLIT_HORIZONTAL, first.style, second.style);
            }
            first.value = value;
            if (nbCols - 1 > 1) {
                second.internalGrid = buildGrid(1, nbCols - 1, null, style);
            } else {
                second.value = null;
            }
            return new InternalGrid(SPLIT_VERTICAL, ratio, first, second);
        }
    }

    /**
     * Supprime, sur chacune des deux sous-cellules issues d'un split, la
     * bordure du côté qui devient la ligne de partage interne — ce côté
     * n'appartient plus au contour extérieur de la cellule mère, il ne doit
     * donc plus être dessiné. Les trois autres côtés (contour extérieur)
     * restent inchangés sur chaque sous-cellule.
     */
    static void clearSharedEdge(int splitType, HCellModel firstStyle, HCellModel secondStyle) {
        if (firstStyle == null || secondStyle == null) {
            return;
        }
        if (splitType == SPLIT_HORIZONTAL) {
            firstStyle.setBorderBottomThickness(0f);
            secondStyle.setBorderTopThickness(0f);
        } else {
            firstStyle.setBorderRightThickness(0f);
            secondStyle.setBorderLeftThickness(0f);
        }
    }

    /**
     * Lecture pure — reconstitue le texte d'une Cell, subdivision comprise,
     * sans rien modifier. Remplace HSuperDefaultTableModel.collectValue(Cell).
     */
    public static String valueOf(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.internalGrid != null) {
            return cell.internalGrid.collectValue();
        }
        return cell.value != null ? cell.value.toString().trim() : null;
    }

    /**
     * Concatène les deux branches de cette subdivision.
     */
    public String collectValue() {
        String v1 = valueOf(firstCell);
        String v2 = valueOf(secondCell);
        StringBuilder sb = new StringBuilder();
        if (v1 != null && !v1.isEmpty()) {
            sb.append(v1);
        }
        if (v2 != null && !v2.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(v2);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static float clampRatio(float ratio) {
        return Math.max(0.15f, Math.min(0.85f, ratio));
    }

}
