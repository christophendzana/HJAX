package hsplitpane;

/**
 * Définit la direction de disposition des composants dans une zone.
 *
 * Cette direction est utilisée par HSplitWrapLayout pour déterminer
 * l'axe principal sur lequel les composants sont alignés avant
 * de passer à la ligne ou colonne suivante.
 */
public enum WrapDirection {

    /**
     * Les composants s'alignent de gauche à droite.
     * Quand la largeur disponible est épuisée, on passe à la ligne suivante.
     * C'est la direction par défaut.
     */
    HORIZONTAL,

    /**
     * Les composants s'alignent de haut en bas.
     * Quand la hauteur disponible est épuisée, on passe à la colonne suivante.
     */
    VERTICAL
}
