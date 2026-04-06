package hsplitpane;

import java.awt.Dimension;

/**
 * Objet de configuration pour la construction d'un HSplitPane.
 *
 * Cet objet permet de définir les paramètres initiaux du composant
 * sans avoir à passer par un constructeur surchargé. L'utilisateur
 * instancie cette classe, renseigne uniquement les champs qui
 * l'intéressent, puis transmet l'objet au constructeur de HSplitPane.
 *
 * Tous les champs de dimensions sont à null par défaut, ce qui signifie
 * que la zone correspondante sera flexible et prendra l'espace restant
 * après placement des zones à taille définie.
 */
public class HSplitPaneConfig {

    // -------------------------------------------------------------------------
    // Dimensions initiales de chaque zone
    // Une valeur null indique que la zone est flexible (taille automatique)
    // -------------------------------------------------------------------------

    /** Taille initiale de la zone du haut. */
    private Dimension northSize;

    /** Taille initiale de la zone du bas. */
    private Dimension southSize;

    /** Taille initiale de la zone de gauche. */
    private Dimension westSize;

    /** Taille initiale de la zone de droite. */
    private Dimension eastSize;

    /** Taille initiale de la zone centrale. */
    private Dimension centerSize;

    // -------------------------------------------------------------------------
    // Paramètres d'affichage
    // -------------------------------------------------------------------------

    /**
     * Indique si la zone centrale doit être affichée.
     * Par défaut à true : la zone CENTER est présente.
     */
    private boolean showCenter = true;

    // =========================================================================
    // Getters et Setters
    // =========================================================================

    public Dimension getNorthSize() {
        return northSize;
    }

    public void setNorthSize(Dimension northSize) {
        this.northSize = northSize;
    }

    public Dimension getSouthSize() {
        return southSize;
    }

    public void setSouthSize(Dimension southSize) {
        this.southSize = southSize;
    }

    public Dimension getWestSize() {
        return westSize;
    }

    public void setWestSize(Dimension westSize) {
        this.westSize = westSize;
    }

    public Dimension getEastSize() {
        return eastSize;
    }

    public void setEastSize(Dimension eastSize) {
        this.eastSize = eastSize;
    }

    public Dimension getCenterSize() {
        return centerSize;
    }

    public void setCenterSize(Dimension centerSize) {
        this.centerSize = centerSize;
    }

    public boolean isShowCenter() {
        return showCenter;
    }

    public void setShowCenter(boolean showCenter) {
        this.showCenter = showCenter;
    }
}
