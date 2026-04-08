package hsplitpane;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;

/**
 * Conteneur multi-zones inspiré des interfaces à panneaux ancrés (dockable
 * panels).
 *
 * HSplitPane divise son espace en cinq zones positionnelles : NORTH, SOUTH,
 * WEST, CENTER et EAST. Chaque zone peut accueillir plusieurs composants
 * positionnés automatiquement en mode wrapping par HSplitWrapLayout. Les zones
 * sont séparées par des diviseurs draggables (HSplitDivider) et peuvent être
 * réduites individuellement via leur barre de contrôle.
 *
 * Utilisation typique :
 * <pre>
 *   HSplitPane pane = new HSplitPane();
 *   pane.addContainer(monComposant, ZonePosition.WEST);
 *   pane.addContainer(autreComposant, ZonePosition.CENTER);
 * </pre>
 *
 * Avec configuration personnalisée :
 * <pre>
 *   HSplitPaneConfig config = new HSplitPaneConfig();
 *   config.setWestSize(new Dimension(200, 0));
 *   config.setNorthSize(new Dimension(0, 120));
 *   HSplitPane pane = new HSplitPane(config);
 * </pre>
 */
public class HSplitPane extends JPanel {

    // -------------------------------------------------------------------------
    // Zones
    // -------------------------------------------------------------------------
    private HSplitZone zoneNorth;
    private HSplitZone zoneSouth;
    private HSplitZone zoneWest;
    private HSplitZone zoneCenter;
    private HSplitZone zoneEast;

    // -------------------------------------------------------------------------
    // Séparateurs
    // -------------------------------------------------------------------------
    private HSplitDivider dividerNorth;
    private HSplitDivider dividerSouth;
    private HSplitDivider dividerWest;
    private HSplitDivider dividerEast;

    // -------------------------------------------------------------------------
    // Layout racine
    // -------------------------------------------------------------------------
    private HSplitPaneRootLayout rootLayout;

    
    /** Positions disponibles pour les zones dans le HSplitPane. */
    public enum ZonePosition {
        NORTH, SOUTH, WEST, EAST, CENTER
    }

    /** Directions de disposition des composants dans une zone. */
    public enum WrapDirection {
        HORIZONTAL, VERTICAL
    }
    
    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée un HSplitPane avec les paramètres par défaut. Toutes les zones sont
     * créées et la zone CENTER est visible. Les tailles sont calculées
     * automatiquement au premier rendu.
     */
    public HSplitPane() {
        this(new HSplitPaneConfig());
    }

    /**
     * Crée un HSplitPane à partir d'un objet de configuration. Seuls les champs
     * renseignés dans la config seront appliqués, les autres zones prendront
     * des tailles calculées automatiquement.
     *
     * @param config la configuration initiale du composant, non null
     */
    public HSplitPane(HSplitPaneConfig config) {
        if (config == null) {
            config = new HSplitPaneConfig();
        }
        initialiserComposants(config);
        assemblerComposants(config);
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    /**
     * Instancie toutes les zones et séparateurs selon la configuration.
     *
     * @param config la configuration fournie par l'utilisateur
     */
    private void initialiserComposants(HSplitPaneConfig config) {

        // Création des zones avec leur taille initiale respective
        zoneNorth = new HSplitZone(ZonePosition.NORTH, null, config.getNorthSize());
        zoneSouth = new HSplitZone(ZonePosition.SOUTH, null, config.getSouthSize());
        zoneWest = new HSplitZone(ZonePosition.WEST, null, config.getWestSize());
        zoneEast = new HSplitZone(ZonePosition.EAST, null, config.getEastSize());

        // La zone CENTER n'est créée que si l'utilisateur la demande
        if (config.isShowCenter()) {
            zoneCenter = new HSplitZone(ZonePosition.CENTER, null, config.getCenterSize());
        }

        // Séparateurs horizontaux (drag vertical)
        dividerNorth = new HSplitDivider(WrapDirection.HORIZONTAL);
        dividerSouth = new HSplitDivider(WrapDirection.HORIZONTAL);

        // Séparateurs verticaux (drag horizontal)
        dividerWest = new HSplitDivider(WrapDirection.VERTICAL);
        dividerEast = new HSplitDivider(WrapDirection.VERTICAL);
    }

    /**
     * Assemble les zones et séparateurs dans le HSplitPane et configure le
     * layout.
     *
     * @param config la configuration fournie par l'utilisateur
     */
    private void assemblerComposants(HSplitPaneConfig config) {

        // Création et configuration du layout racine
        rootLayout = new HSplitPaneRootLayout();
        setLayout(rootLayout);

        setOpaque(true);
        setBackground(new Color(30, 30, 30));

        // On enregistre les zones et dividers dans le layout
        rootLayout.saveZones(zoneNorth, zoneSouth, zoneWest, zoneCenter, zoneEast);
        rootLayout.saveDividers(dividerNorth, dividerSouth, dividerWest, dividerEast);

        // Ajout des composants dans le panel
        add(zoneNorth);
        add(dividerNorth);

        add(zoneWest);
        add(dividerWest);

        if (zoneCenter != null) {
            add(zoneCenter);
        }

        add(dividerEast);
        add(zoneEast);

        add(dividerSouth);
        add(zoneSouth);
    }

    // =========================================================================
    // API publique — ajout de composants
    // =========================================================================
    /**
     * Ajoute un composant dans la zone spécifiée.
     *
     * Si la zone est vide au moment de l'ajout, elle devient visible et le
     * layout est recalculé pour lui allouer de l'espace.
     *
     * @param composant le composant à ajouter
     * @param position la zone de destination
     * @throws IllegalArgumentException si la position est null
     */
    public void addContainer(Component composant, ZonePosition position) {
        if (position == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }

        HSplitZone zone = obtenirZone(position);

        if (zone == null) {
            // Si l'utilisateur essaie d'ajouter dans CENTER alors qu'elle est désactivée
            System.err.println("HSplitPane : la zone " + position
                    + " n'est pas disponible dans cette instance.");
            return;
        }

        zone.addContainer(composant);
        revalidate();
        repaint();
    }

    /**
     * Retire un composant d'une zone spécifique.
     *
     * @param composant le composant à retirer
     * @param position la zone dont retirer le composant
     */
    public void removeContainer(Component composant, ZonePosition position) {
        HSplitZone zone = obtenirZone(position);
        if (zone != null) {
            zone.removeContainer(composant);
            revalidate();
            repaint();
        }
    }

    // =========================================================================
    // API publique — contrôle des zones
    // =========================================================================
    /**
     * Réduit la zone spécifiée programmatiquement. Équivalent à un clic sur le
     * bouton toggle de cette zone.
     *
     * @param position la zone à réduire
     */
    public void collapseZone(ZonePosition position) {
        HSplitZone zone = obtenirZone(position);
        if (zone != null) {
            zone.collapse();
        }
    }

    /**
     * Développe la zone spécifiée programmatiquement.
     *
     * @param position la zone à développer
     */
    public void expandZone(ZonePosition position) {
        HSplitZone zone = obtenirZone(position);
        if (zone != null) {
            zone.expand();
        }
    }

    /**
     * Modifie le titre affiché dans la barre de contrôle d'une zone.
     *
     * @param position la zone dont on modifie le titre
     * @param titre le nouveau titre, ou null pour masquer le titre
     */
    public void setZoneTitle(ZonePosition position, String titre) {
        HSplitZone zone = obtenirZone(position);
        if (zone != null) {
            zone.setTitre(titre);
        }
    }

    // =========================================================================
    // API publique — contrôle des séparateurs
    // =========================================================================
    /**
     * Verrouille ou déverrouille le séparateur adjacent à la zone spécifiée.
     *
     * Chaque zone a un séparateur qui la sépare de la zone voisine. Verrouiller
     * ce séparateur empêche tout redimensionnement par drag.
     *
     * @param position la zone dont on verrouille le séparateur
     * @param locked true pour verrouiller, false pour déverrouiller
     */
    public void setDividerLocked(ZonePosition position, boolean locked) {
        HSplitDivider divider = obtenirDivider(position);
        if (divider != null) {
            divider.setLocked(locked);
        }
    }

    /**
     * Indique si le séparateur adjacent à la zone spécifiée est verrouillé.
     *
     * @param position la zone dont on vérifie le séparateur
     * @return true si le séparateur est verrouillé
     */
    public boolean isDividerLocked(ZonePosition position) {
        HSplitDivider divider = obtenirDivider(position);
        return divider != null && divider.isLocked();
    }

    /**
     * Modifie la direction de disposition des composants dans la zone
     * spécifiée.
     *
     * @param position la zone dont on modifie la direction
     * @param direction la nouvelle direction de wrapping
     */
    public void setZoneDirection(ZonePosition position, WrapDirection direction) {
        HSplitZone zone = obtenirZone(position);
        if (zone != null) {
            zone.setWrapDirection(direction);
        }
    }
    
    /**
 * Active ou désactive l'étirement des composants dans la zone spécifiée.
 *
 * @param position la zone concernée
 * @param etirer   true pour étirer les composants, false pour leur preferredSize
 */
public void setZoneEtirer(ZonePosition position, boolean etirer) {
    HSplitZone zone = obtenirZone(position);
    if (zone != null) {
        zone.setEtirer(etirer);
    }
}    

    // =========================================================================
    // Méthodes utilitaires internes
    // =========================================================================
    /**
     * Retourne la zone correspondant à la position donnée.
     *
     * @param position la position recherchée
     * @return la zone ou null si elle n'existe pas
     */
    private HSplitZone obtenirZone(ZonePosition position) {
        switch (position) {
            case NORTH:
                return zoneNorth;
            case SOUTH:
                return zoneSouth;
            case WEST:
                return zoneWest;
            case CENTER:
                return zoneCenter;
            case EAST:
                return zoneEast;
            default:
                return null;
        }
    }

    /**
     * Retourne le séparateur associé à la position donnée. Convention : le
     * séparateur d'une zone est celui qui la borde du côté intérieur (vers le
     * center).
     *
     * @param position la position de la zone
     * @return le séparateur ou null
     */
    private HSplitDivider obtenirDivider(ZonePosition position) {
        switch (position) {
            case NORTH:
                return dividerNorth;
            case SOUTH:
                return dividerSouth;
            case WEST:
                return dividerWest;
            case EAST:
                return dividerEast;
            default:
                return null;
        }
    }

    // =========================================================================
    // Getters — accès aux zones pour personnalisation avancée
    // =========================================================================
    public HSplitZone getZoneNorth() {
        return zoneNorth;
    }

    public HSplitZone getZoneSouth() {
        return zoneSouth;
    }

    public HSplitZone getZoneWest() {
        return zoneWest;
    }

    public HSplitZone getZoneCenter() {
        return zoneCenter;
    }

    public HSplitZone getZoneEast() {
        return zoneEast;
    }

    public HSplitDivider getDividerNorth() {
        return dividerNorth;
    }

    public HSplitDivider getDividerSouth() {
        return dividerSouth;
    }

    public HSplitDivider getDividerWest() {
        return dividerWest;
    }

    public HSplitDivider getDividerEast() {
        return dividerEast;
    }
}
