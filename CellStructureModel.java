package hsupertable.model.structure;

import hsupertable.model.CellStyle;
import hsupertable.model.MergeRegion;
import hsupertable.model.SubCellPath;
import java.util.List;
import hsupertable.model.structure.CellStructureListener;

/**
 * Structure d'une HTable, en coordonnées MODÈLE : fusions, subdivisions,
 * styles. Ignore TableModel, TableColumnModel et la vue. Les valeurs de cellule
 * restent dans TableModel ; seules les sous-valeurs d'une cellule subdivisée
 * (qui n'existent pas dans TableModel, lequel n'adresse que (row, col))
 * vivent ici.
 *
 * Règle d'admission d'une méthode dans cette interface : elle porte sur une
 * POSITION et doit suivre les insertions/suppressions, ou elle protège un
 * invariant. Le reste (conversion vue↔modèle, lecture/écriture du TableModel)
 * appartient à HTable.
 *
 * À utiliser depuis l'EDT, comme TableModel.
 */
public interface CellStructureModel {

    // ── FUSIONS ──────────────────────────────────────────────────────────────
    /**
     * Fusion couvrant (row, col), origine ou cellule absorbée ; null sinon.
     */
    MergeRegion getMergeAt(int row, int col);

    /**
     * Instantané (non modifiable) des fusions actuelles.
     */
    List<MergeRegion> getMergeRegions();

    /**
     * Vrai si la zone (bornes incluses, ordre indifférent) peut être fusionnée
     * : au moins deux cellules, et aucune fusion existante ne la chevauche
     * partiellement (comportement « Word » de l'ancien canMergeSelection).
     */
    boolean canMerge(int firstRow, int firstCol, int lastRow, int lastCol);

    /**
     * Fusionne la zone. originalValues[rowSpan][colSpan] : valeurs AVANT
     * fusion, que unmerge() restituera. Les subdivisions des cellules de la
     * zone sont repliées (I4).
     *
     * @return les fusions existantes dissoutes (entièrement contenues dans la
     * nouvelle zone)
     * @throws IllegalArgumentException si !canMerge(...)
     */
    List<MergeRegion> merge(int firstRow, int firstCol, int lastRow, int lastCol,
            Object[][] originalValues);

    /**
     * Défait la fusion couvrant (row, col).
     *
     * @return la région retirée (avec getOriginalValues()), ou null si la
     * cellule n'était pas fusionnée.
     */
    MergeRegion unmerge(int row, int col);

    default boolean isMergeOrigin(int row, int col) {
        MergeRegion r = getMergeAt(row, col);
        return r != null && r.originRow == row && r.originCol == col;
    }

    default boolean isAbsorbed(int row, int col) {
        MergeRegion r = getMergeAt(row, col);
        return r != null && !(r.originRow == row && r.originCol == col);
    }

    // ── SUBDIVISIONS ─────────────────────────────────────────────────────────
    /**
     * Racine de la case (row, col). Jamais null : une feuille si la cellule
     * n'est pas subdivisée.
     */
    CellNode getCellNode(int row, int col);

    /**
     * Coupe en deux le noeud désigné par path. Son contenu actuel passe dans
     * le premier fils : firstValue s'il était une feuille, sa subdivision
     * existante sinon. Le ratio est borné à [0.15, 0.85].
     *
     * @return false si la cellule est absorbée (I4) ou si path ne désigne
     * aucun noeud.
     */
    boolean subdivide(int row, int col, SubCellPath path,
            int splitType, float dividerRatio, Object firstValue);

    /**
     * Remplace le contenu du noeud désigné par une grille nbRows × nbCols
     * (équivalent d'imbrications de subdivisions) ; value va dans la première
     * sous-cellule.
     *
     * @return false si absorbée, chemin invalide, ou grille 1×1.
     */
    boolean subdivideGrid(int row, int col, SubCellPath path, int nbRows, int nbCols, Object value);

    /**
     * Replie la subdivision du noeud désigné en une feuille.
     *
     * @return le texte replié (concaténation des feuilles), null s'il n'y avait
     * pas de subdivision ou si les feuilles étaient vides. Pour une sous-cellule,
     * ce texte devient sa valeur ; pour la racine, il appartient à l'appelant de
     * le reporter dans le TableModel.
     */
    Object removeSubdivision(int row, int col, SubCellPath path);

    /**
     * Valeur d'une FEUILLE issue d'une subdivision.
     *
     * @throws IllegalArgumentException si path désigne la racine (sa valeur
     * vit dans le TableModel), un noeud interne, ou rien.
     */
    void setSubCellValue(int row, int col, SubCellPath path, Object value);

    // ── STYLE ────────────────────────────────────────────────────────────────
    // path est obligatoire : chaque noeud d'une subdivision a son propre style,
    // comme Cell.style avant la refonte.
    /**
     * Style du noeud, jamais null. Instance vivante, créée à la première
     * lecture (état neutre) : la modifier modifie le style stocké.
     *
     * @throws IllegalArgumentException si path ne désigne aucun noeud.
     */
    CellStyle getCellStyle(int row, int col, SubCellPath path);

    void setCellStyle(int row, int col, SubCellPath path, CellStyle style);

    /**
     * Remet le noeud à un style neutre.
     */
    void clearCellStyle(int row, int col, SubCellPath path);

    // ── SUIVI DES CHANGEMENTS STRUCTURELS DU TABLEMODEL ──────────────────────
    /**
     * Une fusion traversée par l'insertion s'étend ; celles qui suivent se
     * décalent.
     */
    void columnsInserted(int firstCol, int lastCol);

    void rowsInserted(int firstRow, int lastRow);

    /**
     * @return les fusions touchées par la suppression, dissoutes. Les valeurs
     * des cellules survivantes sont à restituer par l'appelant (via
     * getOriginalValues()).
     */
    List<MergeRegion> rowsRemoved(int firstRow, int lastRow);

    List<MergeRegion> columnsRemoved(int firstCol, int lastCol);

    /**
     * Efface fusions, subdivisions et styles.
     */
    void clear();

    // ── OBSERVATION ──────────────────────────────────────────────────────────
    void addCellStructureListener(CellStructureListener l);

    void removeCellStructureListener(CellStructureListener l);
}
