package hsupertable.model.structure;

import hsupertable.model.CellStyle;

/**
 * Un noeud de la structure d'une cellule, en lecture. La racine d'une case de
 * la grille et une sous-cellule issue d'une subdivision sont le même type :
 * un arbre binaire dont chaque noeud interne coupe son espace en deux.
 *
 * Interface de LECTURE : toute modification passe par
 * {@link HCellStructureModel}, qui garantit les invariants et notifie les
 * observateurs.
 */
public interface CellNode {

    /**
     * Division verticale : first à gauche, second à droite.
     */
    int SPLIT_VERTICAL = 0;

    /**
     * Division horizontale : first en haut, second en bas.
     */
    int SPLIT_HORIZONTAL = 1;

    boolean isLeaf();

    /**
     * Valeur propre du noeud. N'a de sens que pour une feuille issue d'une
     * subdivision. Pour la racine d'une cellule NON subdivisée, la vraie
     * valeur vient de TableModel.getValueAt(row, col), pas d'ici.
     */
    Object getValue();

    /**
     * Style du noeud, jamais null. Instance vivante : la modifier modifie le
     * style du noeud (comme Cell.style avant la refonte).
     */
    CellStyle getStyle();

    /**
     * SPLIT_VERTICAL ou SPLIT_HORIZONTAL. Sans objet si isLeaf().
     */
    int getSplitType();

    /**
     * Position du séparateur dans ]0,1[. Sans objet si isLeaf().
     */
    float getDividerRatio();

    /**
     * Sans objet si isLeaf().
     */
    CellNode getFirst();

    /**
     * Sans objet si isLeaf().
     */
    CellNode getSecond();

    /**
     * Texte replié de ce noeud : pour une feuille, sa valeur (trim) ; pour un
     * noeud interne, la concaténation (séparée par un espace) des textes
     * non vides de ses deux branches. null si tout est vide.
     *
     * C'est la valeur que HTable range dans le TableModel pour une cellule
     * subdivisée (le TableModel garde la concaténation des feuilles).
     */
    default String collectText() {
        if (isLeaf()) {
            Object v = getValue();
            return v != null ? v.toString().trim() : null;
        }
        String v1 = getFirst().collectText();
        String v2 = getSecond().collectText();
        StringBuilder sb = new StringBuilder();
        if (v1 != null && !v1.isEmpty()) {
            sb.append(v1);
        }
        if (v2 != null && !v2.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(v2);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
