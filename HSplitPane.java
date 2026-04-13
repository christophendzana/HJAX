package hsplitpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import hcomponents.vues.HLabelOrientation;
import javax.swing.JPanel;

/**
 * Conteneur multi-zones inspiré des interfaces à panneaux ancrés (dockable
 * panels).
 *
 * HSplitPane divise son espace en cinq zones positionnelles : NORTH, SOUTH,
 * WEST, CENTER et EAST. Chaque zone peut accueillir plusieurs composants
 * positionnés automatiquement en mode wrapping par HSplitWrapLayout.
 *
 * Fonctionnalités principales : - Zones redimensionnables via des séparateurs
 * draggables - Collapse / expand animé par zone - Mode fullscreen : une zone
 * occupe tout l'espace, les autres se collapsent - Mode flottant : une zone se
 * détache dans une fenêtre HDialog indépendante - API publique complète pour
 * personnaliser zones, headers et séparateurs
 */
public class HSplitPane extends JPanel {

    // -------------------------------------------------------------------------
    // Enums imbriquées
    // -------------------------------------------------------------------------
    /**
     * Positions disponibles pour les zones dans le HSplitPane.
     */
    public enum ZonePosition {
        NORTH, SOUTH, WEST, EAST, CENTER
    }

    /**
     * Directions de disposition des composants dans une zone.
     */
    public enum WrapDirection {
        HORIZONTAL, VERTICAL
    }

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
    private HSplitDivider northDivider;
    private HSplitDivider southDivider;
    private HSplitDivider westDivider;
    private HSplitDivider eastDivider;

    // -------------------------------------------------------------------------
    // Layout racine
    // -------------------------------------------------------------------------
    private HSplitPaneRootLayout rootLayout;

    // -------------------------------------------------------------------------
    // Fullscreen
    // Quand une zone est en fullscreen, on mémorise les tailles de toutes
    // les zones pour les restaurer à la sortie du mode fullscreen.
    // -------------------------------------------------------------------------
    /**
     * Position de la zone actuellement en fullscreen, ou null si aucune. Une
     * seule zone fullscreen à la fois.
     */
    private ZonePosition zoneFullScreen;

    /**
     * Tailles sauvegardées de toutes les zones avant l'entrée en fullscreen.
     * Permet de restaurer exactement l'état précédent.
     */
    private Map<ZonePosition, Dimension> taillesAvantFullScreen;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    public HSplitPane() {
        this(new HSplitPaneConfig());
    }

    public HSplitPane(HSplitPaneConfig config) {
        if (config == null) {
            config = new HSplitPaneConfig();
        }
        initializeComponents(config);
        assembleComponents(config);
        enregistrerReferences();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    private void initializeComponents(HSplitPaneConfig config) {
        zoneNorth = new HSplitZone(ZonePosition.NORTH, null, config.getNorthSize());
        zoneSouth = new HSplitZone(ZonePosition.SOUTH, null, config.getSouthSize());
        zoneWest = new HSplitZone(ZonePosition.WEST, null, config.getWestSize());
        zoneEast = new HSplitZone(ZonePosition.EAST, null, config.getEastSize());

        if (config.isShowCenter()) {
            zoneCenter = new HSplitZone(ZonePosition.CENTER, null, config.getCenterSize());
        }

        northDivider = new HSplitDivider(WrapDirection.HORIZONTAL);
        southDivider = new HSplitDivider(WrapDirection.HORIZONTAL);
        westDivider = new HSplitDivider(WrapDirection.VERTICAL);
        eastDivider = new HSplitDivider(WrapDirection.VERTICAL);
    }

    private void assembleComponents(HSplitPaneConfig config) {
        rootLayout = new HSplitPaneRootLayout();
//        rootLayout.setSplitPane(this);
        setLayout(rootLayout);
        setOpaque(true);
        setBackground(new Color(30, 30, 30));

        rootLayout.saveZones(zoneNorth, zoneSouth, zoneWest, zoneCenter, zoneEast);
        rootLayout.saveDividers(northDivider, southDivider, westDivider, eastDivider);

        add(zoneNorth);
        add(northDivider);
        add(zoneWest);
        add(westDivider);
        if (zoneCenter != null) {
            add(zoneCenter);
        }
        add(eastDivider);
        add(zoneEast);
        add(southDivider);
        add(zoneSouth);
    }

    /**
     * Enregistre la référence à ce HSplitPane dans chaque zone. Nécessaire pour
     * que les boutons fullscreen et float puissent coordonner leurs actions
     * avec le parent.
     */
    private void enregistrerReferences() {
        zoneNorth.setSplitPaneRef(this);
        zoneSouth.setSplitPaneRef(this);
        zoneWest.setSplitPaneRef(this);
        zoneEast.setSplitPaneRef(this);
        if (zoneCenter != null) {
            zoneCenter.setSplitPaneRef(this);
        }
    }

    // =========================================================================
    // API publique — ajout et retrait de composants
    // =========================================================================
    public void addContainer(Component composant, ZonePosition position) {
        if (position == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }
        HSplitZone zone = getZone(position);
        if (zone == null) {
            System.err.println("HSplitPane : la zone " + position
                    + " n'est pas disponible dans cette instance.");
            return;
        }
        zone.addContainer(composant);
        revalidate();
        repaint();
    }

    public void removeContainer(Component composant, ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.removeContainer(composant);
            revalidate();
            repaint();
        }
    }

    // =========================================================================
    // API publique — collapse / expand
    // =========================================================================
    public void collapseZone(ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.collapse();
        }
    }

    public void expandZone(ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.expand();
        }
    }

    public boolean isZoneCollapsed(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null && zone.isCollapsed();
    }

    // =========================================================================
    // API publique — fullscreen
    // =========================================================================
    /**
     * Fait passer la zone spécifiée en mode fullscreen.
     *
     * La zone fullscreen occupe tout l'espace disponible du HSplitPane. Toutes
     * les autres zones sont masquées — celles qui ont un header le conservent
     * visible (via collapse), la zone CENTER qui n'a pas de header est
     * simplement rendue invisible via setVisible(false).
     *
     * Si une autre zone est déjà en fullscreen, elle est d'abord restaurée.
     *
     * @param position la zone à passer en fullscreen
     */
    public void enterFullScreen(ZonePosition position) {
        if (zoneFullScreen != null && zoneFullScreen != position) {
            exitFullScreen();
        }

        HSplitZone zone = getZone(position);
        if (zone == null || zone.isEmpty()) {
            return;
        }

        zoneFullScreen = position;

        // Sauvegarde des tailles
        taillesAvantFullScreen = new EnumMap<>(ZonePosition.class);
        for (ZonePosition pos : ZonePosition.values()) {
            HSplitZone z = getZone(pos);
            if (z != null) {
                taillesAvantFullScreen.put(pos, z.getSize());
            }
        }

        // Masquage de toutes les autres zones
        // Masquage immédiat de toutes les zones sauf la fullscreen
        for (ZonePosition pos : ZonePosition.values()) {
            if (pos == position) {
                continue;
            }
            HSplitZone z = getZone(pos);
            if (z != null) {
                z.setVisible(false);
            }
        }

        zone.setFullScreenState(true);

// On notifie le layout pour qu'il passe en mode fullscreen
        rootLayout.setZoneFullScreen(position);

// Calcul des bounds cibles pour l'animation
        int totalW = getWidth();
        int totalH = getHeight();

// Lancement de l'animation vers les bounds fullscreen
        zone.animerVersBounds(0, 0, totalW, totalH);

        repaint();
    }

    /**
     * Quitte le mode fullscreen et restaure toutes les zones à leur état
     * d'avant l'entrée en fullscreen.
     */
    public void exitFullScreen() {
        if (zoneFullScreen == null) {
            return;
        }

        HSplitZone zonePleinEcran = getZone(zoneFullScreen);
        if (zonePleinEcran != null) {
            zonePleinEcran.setFullScreenState(false);
        }

        // On notifie le layout AVANT de restaurer les zones
        // pour qu'il revienne au calcul normal dès le prochain revalidate
        rootLayout.setZoneFullScreen(null);

        for (ZonePosition pos : ZonePosition.values()) {
    if (pos == zoneFullScreen) continue;
    HSplitZone z = getZone(pos);
    if (z != null) z.setVisible(true);
}

        zoneFullScreen = null;
        taillesAvantFullScreen = null;
        revalidate();
        repaint();
    }

  

    public boolean isZoneFullScreen(ZonePosition position) {
        return zoneFullScreen != null && zoneFullScreen == position;
    }

    // =========================================================================
    // API publique — float
    // =========================================================================
    /**
     * Détache la zone spécifiée dans une fenêtre flottante.
     *
     * @param position la zone à détacher
     */
    public void floatZone(ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.floatZone();
        }
    }

    /**
     * Réintègre la zone flottante spécifiée dans le HSplitPane.
     *
     * @param position la zone à réintégrer
     */
    public void reintegrateZone(ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.reintegrateZone();
        }
    }

    public boolean isZoneFloating(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null && zone.isFloating();
    }

    // =========================================================================
    // API publique — disposition des composants dans les zones
    // =========================================================================
    public void setZoneDirection(ZonePosition position, WrapDirection direction) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.setWrapDirection(direction);
        }
    }

    public void setZoneStretch(ZonePosition position, boolean etirer) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.setEtirer(etirer);
        }
    }

    // =========================================================================
    // API publique — personnalisation du fond des zones
    // =========================================================================
    public void setZoneBackground(ZonePosition position, Color couleur) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.setBackground(couleur);
            zone.repaint();
        }
    }

    public Color getZoneBackground(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null ? zone.getBackground() : null;
    }

    // =========================================================================
    // API publique — personnalisation des headers
    // =========================================================================
    public void setZoneTitle(ZonePosition position, String titre) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.setTitre(titre);
        }
    }

    public String getZoneTitle(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null ? zone.getTitre() : null;
    }

    public void setHeaderBackground(ZonePosition position, Color couleur) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) {
            header.setCouleurFond(couleur);
        }
    }

    public Color getHeaderBackground(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getCouleurFond() : null;
    }

    public void setHeaderTitleColor(ZonePosition position, Color couleur) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) {
            header.setCouleurTitre(couleur);
        }
    }

    public Color getHeaderTitleColor(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getCouleurTitre() : null;
    }

    public void setHeaderTitleFont(ZonePosition position, Font font) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) {
            header.setTitleFont(font);
        }
    }

    public Font getHeaderTitleFont(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getTitleFont() : null;
    }

    public void setHeaderTitleOrientation(ZonePosition position, HLabelOrientation orientation) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) {
            header.setTitleOrientation(orientation);
        }
    }

    public HLabelOrientation getHeaderTitleOrientation(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getTitleOrientation() : null;
    }

    public void setHeaderThickness(ZonePosition position, int epaisseur) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) {
            header.setEpaisseur(epaisseur);
        }
    }

    public int getHeaderThickness(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getEpaisseur() : -1;
    }

    // =========================================================================
    // API publique — personnalisation des séparateurs
    // =========================================================================
    public void setDividerLocked(ZonePosition position, boolean locked) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) {
            divider.setLocked(locked);
        }
    }

    public boolean isDividerLocked(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null && divider.isLocked();
    }

    public void setDividerColor(ZonePosition position, Color couleur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) {
            divider.setCouleur(couleur);
        }
    }

    public Color getDividerColor(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getCouleur() : null;
    }

    public void setDividerHoverColor(ZonePosition position, Color couleur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) {
            divider.setCouleurHover(couleur);
        }
    }

    public Color getDividerHoverColor(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getCouleurHover() : null;
    }

    public void setDividerDragColor(ZonePosition position, Color couleur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) {
            divider.setCouleurDrag(couleur);
        }
    }

    public Color getDividerDragColor(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getCouleurDrag() : null;
    }

    public void setDividerThickness(ZonePosition position, int epaisseur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) {
            divider.setEpaisseur(epaisseur);
        }
    }

    public int getDividerThickness(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getEpaisseur() : -1;
    }

    // =========================================================================
    // Méthodes utilitaires internes
    // =========================================================================
    private HSplitZone getZone(ZonePosition position) {
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

    private HSplitDivider getDivider(ZonePosition position) {
        switch (position) {
            case NORTH:
                return northDivider;
            case SOUTH:
                return southDivider;
            case WEST:
                return westDivider;
            case EAST:
                return eastDivider;
            default:
                return null;
        }
    }

    private HSplitZoneHeader getHeader(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null ? zone.getHeader() : null;
    }

    // =========================================================================
    // Getters — accès direct aux zones et séparateurs
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
        return northDivider;
    }

    public HSplitDivider getDividerSouth() {
        return southDivider;
    }

    public HSplitDivider getDividerWest() {
        return westDivider;
    }

    public HSplitDivider getDividerEast() {
        return eastDivider;
    }
}
