package hsplitpane;

/**
 * Définit les positions disponibles pour les zones dans un HSplitPane.
 *
 * Chaque constante représente une région précise du composant,
 * en suivant le modèle classique des interfaces à panneaux ancrés
 * (comme celles que l'on retrouve dans les environnements de développement).
 */
public enum ZonePosition {

    /** Zone ancrée en haut du composant. */
    NORTH,

    /** Zone ancrée en bas du composant. */
    SOUTH,

    /** Zone ancrée à gauche du composant. */
    WEST,

    /** Zone ancrée à droite du composant. */
    EAST,

    /**
     * Zone centrale du composant.
     * Son affichage est optionnel et se contrôle via HSplitPaneConfig.
     */
    CENTER
}
