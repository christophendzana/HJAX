package htextarea;

/**
 * Styles visuels disponibles pour {@link hcomponents.HTextArea}.
 *
 * <p>Chaque constante représente un thème visuel global du composant
 * (couleur de fond, bordure…). Les styles typographiques par caractère
 * (gras, couleur du texte…) sont gérés séparément via les méthodes
 * de {@code HTextArea} et le {@code StyledDocument} interne.</p>
 *
 * <p>Pour ajouter un nouveau style, il suffit :</p>
 * <ol>
 *   <li>D'ajouter une constante ici.</li>
 *   <li>D'ajouter le {@code case} correspondant dans
 *       {@link hcomponents.vues.HBasicTextAreaUI#resoudreCouleurFond}.</li>
 * </ol>
 *
 * @author FIDELE
 * @version 2.0
 */
public enum HTextAreaStyle {

    /**
     * Style principal — utilise la couleur de fond native du Look & Feel.
     * Convient pour la majorité des usages.
     */
    PRIMARY,

    /**
     * Style secondaire — fond légèrement assombri pour différencier le composant
     * de son conteneur parent.
     */
    SECONDARY
}
