package hsplitpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import hcomponents.vues.HLabelOrientation;
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
    private HSplitDivider northDivider;
    private HSplitDivider southDivider;
    private HSplitDivider westDivider;
    private HSplitDivider eastDivider;

    // -------------------------------------------------------------------------
    // Layout racine
    // -------------------------------------------------------------------------
    private HSplitPaneRootLayout rootLayout ;

    
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
        initializeComponents(config);
        assembleComponents(config);
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    /**
     * Instancie toutes les zones et séparateurs selon la configuration.
     *
     * @param config la configuration fournie par l'utilisateur
     */
    private void initializeComponents(HSplitPaneConfig config) {

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
        northDivider = new HSplitDivider(WrapDirection.HORIZONTAL);
        southDivider = new HSplitDivider(WrapDirection.HORIZONTAL);

        // Séparateurs verticaux (drag horizontal)
        westDivider = new HSplitDivider(WrapDirection.VERTICAL);
        eastDivider = new HSplitDivider(WrapDirection.VERTICAL);
    }

    /**
     * Assemble les zones et séparateurs dans le HSplitPane et configure le
     * layout.
     *
     * @param config la configuration fournie par l'utilisateur
     */
    private void assembleComponents(HSplitPaneConfig config) {

        // Création et configuration du layout racine
        rootLayout  = new HSplitPaneRootLayout();
        setLayout(rootLayout );

        setOpaque(true);
        setBackground(new Color(30, 30, 30));

        // On enregistre les zones et dividers dans le layout
        rootLayout .saveZones(zoneNorth, zoneSouth, zoneWest, zoneCenter, zoneEast);
        rootLayout .saveDividers(northDivider, southDivider, westDivider, eastDivider);

        // Ajout des composants dans le panel
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

    // =========================================================================
    // API publique — ajout et retrait de composants
    // =========================================================================

    /**
     * Ajoute un composant dans la zone spécifiée.
     *
     * @param composant le composant à ajouter
     * @param position  la zone de destination
     */
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

    /**
     * Retire un composant d'une zone spécifique.
     *
     * @param composant le composant à retirer
     * @param position  la zone dont retirer le composant
     */
    public void removeContainer(Component composant, ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.removeContainer(composant);
            revalidate();
            repaint();
        }
    }

    // =========================================================================
    // API publique — collapse / expand des zones
    // =========================================================================

    /**
     * Réduit la zone spécifiée programmatiquement.
     *
     * @param position la zone à réduire
     */
    public void collapseZone(ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) zone.collapse();
    }

    /**
     * Développe la zone spécifiée programmatiquement.
     *
     * @param position la zone à développer
     */
    public void expandZone(ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) zone.expand();
    }

    /**
     * Indique si la zone spécifiée est actuellement réduite.
     *
     * @param position la zone à vérifier
     * @return true si la zone est réduite
     */
    public boolean isZoneCollapsed(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null && zone.isCollapsed();
    }

    // =========================================================================
    // API publique — disposition des composants dans les zones
    // =========================================================================

    /**
     * Modifie la direction de disposition des composants dans la zone spécifiée.
     *
     * @param position  la zone concernée
     * @param direction la nouvelle direction de wrapping
     */
    public void setZoneDirection(ZonePosition position, WrapDirection direction) {
        HSplitZone zone = getZone(position);
        if (zone != null) zone.setWrapDirection(direction);
    }

    /**
     * Active ou désactive l'étirement des composants dans la zone spécifiée.
     *
     * @param position la zone concernée
     * @param etirer   true pour étirer, false pour respecter la preferredSize
     */
    public void setZoneStretch(ZonePosition position, boolean etirer) {
        HSplitZone zone = getZone(position);
        if (zone != null) zone.setEtirer(etirer);
    }

    // =========================================================================
    // API publique — personnalisation du fond des zones
    // =========================================================================

    /**
     * Modifie la couleur de fond d'une zone.
     *
     * @param position la zone concernée
     * @param couleur  la nouvelle couleur de fond
     */
    public void setZoneBackground(ZonePosition position, Color couleur) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.setBackground(couleur);
            zone.revalidate();
            zone.repaint();
        }
    }

    /**
     * Retourne la couleur de fond d'une zone.
     *
     * @param position la zone concernée
     * @return la couleur de fond, ou null si la zone n'existe pas
     */
    public Color getZoneBackground(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null ? zone.getBackground() : null;
    }

    // =========================================================================
    // API publique — personnalisation du header
    // =========================================================================

    /**
     * Modifie le titre affiché dans la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @param titre    le nouveau titre, ou null pour masquer le titre
     */
    public void setZoneTitle(ZonePosition position, String titre) {
        HSplitZone zone = getZone(position);
        if (zone != null) zone.setTitre(titre);
    }

    /**
     * Retourne le titre affiché dans la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @return le titre courant, ou null
     */
    public String getZoneTitle(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null ? zone.getTitre() : null;
    }

    /**
     * Modifie la couleur de fond de la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @param couleur  la nouvelle couleur de fond du header
     */
    public void setHeaderBackground(ZonePosition position, Color couleur) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) header.setCouleurFond(couleur);
    }

    /**
     * Retourne la couleur de fond de la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @return la couleur de fond du header, ou null
     */
    public Color getHeaderBackground(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getCouleurFond() : null;
    }

    /**
     * Modifie la couleur du texte du titre dans la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @param couleur  la nouvelle couleur du titre
     */
    public void setHeaderTitleColor(ZonePosition position, Color couleur) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) header.setCouleurTitre(couleur);
    }

    /**
     * Retourne la couleur du texte du titre d'une barre de contrôle.
     *
     * @param position la zone concernée
     * @return la couleur du titre, ou null
     */
    public Color getHeaderTitleColor(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getCouleurTitre() : null;
    }

    /**
     * Modifie la police du titre dans la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @param font     la nouvelle police
     */
    public void setHeaderTitleFont(ZonePosition position, Font font) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) header.setTitleFont(font);
    }

    /**
     * Retourne la police courante du titre d'une barre de contrôle.
     *
     * @param position la zone concernée
     * @return la police du titre, ou null
     */
    public Font getHeaderTitleFont(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getTitleFont() : null;
    }

    /**
     * Modifie l'orientation du titre dans la barre de contrôle d'une zone.
     * Par défaut l'orientation est automatique, mais ce setter permet de la forcer.
     *
     * @param position    la zone concernée
     * @param orientation la nouvelle orientation du titre
     */
    public void setHeaderTitleOrientation(ZonePosition position, HLabelOrientation orientation) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) header.setTitleOrientation(orientation);
    }

    /**
     * Retourne l'orientation courante du titre d'une barre de contrôle.
     *
     * @param position la zone concernée
     * @return l'orientation du titre, ou null
     */
    public HLabelOrientation getHeaderTitleOrientation(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getTitleOrientation() : null;
    }

    /**
     * Modifie l'épaisseur de la barre de contrôle d'une zone.
     *
     * @param position  la zone concernée
     * @param epaisseur la nouvelle épaisseur en pixels
     */
    public void setHeaderThickness(ZonePosition position, int epaisseur) {
        HSplitZoneHeader header = getHeader(position);
        if (header != null) header.setEpaisseur(epaisseur);
    }

    /**
     * Retourne l'épaisseur de la barre de contrôle d'une zone.
     *
     * @param position la zone concernée
     * @return l'épaisseur en pixels, ou -1 si la zone n'a pas de header
     */
    public int getHeaderThickness(ZonePosition position) {
        HSplitZoneHeader header = getHeader(position);
        return header != null ? header.getEpaisseur() : -1;
    }

    // =========================================================================
    // API publique — personnalisation des séparateurs
    // =========================================================================

    /**
     * Verrouille ou déverrouille le séparateur adjacent à la zone spécifiée.
     *
     * @param position la zone concernée
     * @param locked   true pour verrouiller, false pour déverrouiller
     */
    public void setDividerLocked(ZonePosition position, boolean locked) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) divider.setLocked(locked);
    }

    /**
     * Indique si le séparateur adjacent à la zone spécifiée est verrouillé.
     *
     * @param position la zone concernée
     * @return true si le séparateur est verrouillé
     */
    public boolean isDividerLocked(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null && divider.isLocked();
    }

    /**
     * Modifie la couleur normale du séparateur adjacent à la zone spécifiée.
     *
     * @param position la zone concernée
     * @param couleur  la nouvelle couleur normale
     */
    public void setDividerColor(ZonePosition position, Color couleur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) divider.setCouleur(couleur);
    }

    /**
     * Retourne la couleur normale du séparateur d'une zone.
     *
     * @param position la zone concernée
     * @return la couleur normale, ou null
     */
    public Color getDividerColor(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getCouleur() : null;
    }

    /**
     * Modifie la couleur de survol du séparateur adjacent à la zone spécifiée.
     *
     * @param position la zone concernée
     * @param couleur  la nouvelle couleur au survol
     */
    public void setDividerHoverColor(ZonePosition position, Color couleur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) divider.setCouleurHover(couleur);
    }

    /**
     * Retourne la couleur de survol du séparateur d'une zone.
     *
     * @param position la zone concernée
     * @return la couleur au survol, ou null
     */
    public Color getDividerHoverColor(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getCouleurHover() : null;
    }

    /**
     * Modifie la couleur de drag du séparateur adjacent à la zone spécifiée.
     *
     * @param position la zone concernée
     * @param couleur  la nouvelle couleur pendant le drag
     */
    public void setDividerDragColor(ZonePosition position, Color couleur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) divider.setCouleurDrag(couleur);
    }

    /**
     * Retourne la couleur de drag du séparateur d'une zone.
     *
     * @param position la zone concernée
     * @return la couleur de drag, ou null
     */
    public Color getDividerDragColor(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getCouleurDrag() : null;
    }

    /**
     * Modifie l'épaisseur du séparateur adjacent à la zone spécifiée.
     *
     * @param position  la zone concernée
     * @param epaisseur la nouvelle épaisseur en pixels
     */
    public void setDividerThickness(ZonePosition position, int epaisseur) {
        HSplitDivider divider = getDivider(position);
        if (divider != null) divider.setEpaisseur(epaisseur);
    }

    /**
     * Retourne l'épaisseur du séparateur d'une zone.
     *
     * @param position la zone concernée
     * @return l'épaisseur en pixels, ou -1 si pas de séparateur
     */
    public int getDividerThickness(ZonePosition position) {
        HSplitDivider divider = getDivider(position);
        return divider != null ? divider.getEpaisseur() : -1;
    }    

    // =========================================================================
    // Méthodes utilitaires internes
    // =========================================================================

    private HSplitZone getZone(ZonePosition position) {
        switch (position) {
            case NORTH:  return zoneNorth;
            case SOUTH:  return zoneSouth;
            case WEST:   return zoneWest;
            case CENTER: return zoneCenter;
            case EAST:   return zoneEast;
            default:     return null;
        }
    }

    private HSplitDivider getDivider(ZonePosition position) {
        switch (position) {
            case NORTH:  return northDivider;
            case SOUTH:  return southDivider;
            case WEST:   return westDivider;
            case EAST:   return eastDivider;
            default:     return null;
        }
    }

    /**
     * Retourne la barre de contrôle de la zone spécifiée.
     * La zone CENTER n'a pas de header — retourne null dans ce cas.
     *
     * @param position la zone concernée
     * @return le header ou null
     */
    private HSplitZoneHeader getHeader(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null ? zone.getHeader() : null;
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
