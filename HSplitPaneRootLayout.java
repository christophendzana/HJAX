package hsplitpane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 *
 */
public class HSplitPaneRootLayout implements LayoutManager2 {

    private HSplitZone zoneNorth;

    private HSplitZone zoneSouth;

    private HSplitZone zoneWest;

    private HSplitZone zoneCenter;

    private HSplitZone zoneEast;

    private HSplitDivider dividerNorth;

    private HSplitDivider dividerSouth;

    private HSplitDivider dividerWest;

    private HSplitDivider dividerEast;

    // Espace minimal laissé à la zone centrale sur chaque axe
    private static final int MIN_CENTER_SIZE = 50;

    private Container parent;

    public HSplitPaneRootLayout() {

    }

    public void saveZones(HSplitZone north, HSplitZone south,
                          HSplitZone west, HSplitZone center,
                          HSplitZone east) {
        this.zoneNorth = north;
        this.zoneSouth = south;
        this.zoneWest = west;
        this.zoneCenter = center;
        this.zoneEast = east;
    }

    public void saveDividers(HSplitDivider north, HSplitDivider south,
                             HSplitDivider west, HSplitDivider east) {
        this.dividerNorth = north;
        this.dividerSouth = south;
        this.dividerWest = west;
        this.dividerEast = east;

        if (dividerNorth != null) {
            dividerNorth.setOnDragCallback(delta -> onDragNorth(delta));
        }
        if (dividerSouth != null) {
            dividerSouth.setOnDragCallback(delta -> onDragSouth(delta));
        }
        if (dividerWest != null) {
            dividerWest.setOnDragCallback(delta -> onDragWest(delta));
        }
        if (dividerEast != null) {
            dividerEast.setOnDragCallback(delta -> onDragEast(delta));
        }
    }

    // -------------------------------------------------------------------------
    // Drag des séparateurs
    //
    // Ce n'est pas une duplication d'AXE ici (contrairement à getEffectiveSize
    // ou initializeSizes) mais une duplication de SIGNE : north/west grandissent
    // avec un delta positif, south/east grandissent avec un delta négatif. Une
    // seule méthode paramétrée par le signe suffit, pas besoin de WrapDirection.
    // -------------------------------------------------------------------------

    /**
     * Une zone réduite ou détachée n'a pas de taille à ajuster : son divider ne bouge pas.
     */
    private boolean isDraggable(HSplitZone zone) {
        return zone != null && !zone.isCollapsedOrFloating() && !zone.animationInProgress();
    }

    /**
     * Applique un déplacement à partir de la taille réellement affichée, borné à [0, max].
     */
    private int clampDrag(int base, int delta, int max) {
        return Math.max(0, Math.min(max, base + delta));
    }

    private int onDragNorth(int delta) {
        if (!isDraggable(zoneNorth)) {
            return 0;
        }
        int available = availableVertical();
        int before = Math.min(wantedSize(zoneNorth), available);
        zoneNorth.applyExpandedSize(clampDrag(before, delta, available));
        notifyParent();
        return wantedSize(zoneNorth) - before;
    }

    private int onDragSouth(int delta) {
        if (!isDraggable(zoneSouth)) {
            return 0;
        }
        int available = availableVertical();
        int northShown = Math.min(getEffectiveSize(zoneNorth, WrapDirection.VERTICAL), available);
        int max = available - northShown;
        int before = Math.min(wantedSize(zoneSouth), max);
        zoneSouth.applyExpandedSize(clampDrag(before, -delta, max));
        notifyParent();
        return before - wantedSize(zoneSouth);
    }

    private int onDragWest(int delta) {
        if (!isDraggable(zoneWest)) {
            return 0;
        }
        int available = availableHorizontal();
        int before = Math.min(wantedSize(zoneWest), available);
        zoneWest.applyExpandedSize(clampDrag(before, delta, available));
        notifyParent();
        return wantedSize(zoneWest) - before;
    }

    private int onDragEast(int delta) {
        if (!isDraggable(zoneEast)) {
            return 0;
        }
        int available = availableHorizontal();
        int westShown = Math.min(getEffectiveSize(zoneWest, WrapDirection.HORIZONTAL), available);
        int max = available - westShown;
        int before = Math.min(wantedSize(zoneEast), max);
        zoneEast.applyExpandedSize(clampDrag(before, -delta, max));
        notifyParent();
        return before - wantedSize(zoneEast);
    }

    /** Taille voulue d'une zone (0 tant que le layout ne l'a pas déterminée). */
    private int wantedSize(HSplitZone zone) {
        Integer size = zone.getExpandedSize();
        return size != null ? size : 0;
    }

    /**
     * Épaisseur occupée par le divider d'une zone (0 si la zone ou son divider est absent / vide).
     */
    private int dividerSpace(HSplitZone zone, HSplitDivider divider) {
        return (divider != null && zone != null && !zone.isEmpty()) ? divider.getEpaisseur() : 0;
    }

    /**
     * Espace que peuvent se partager north et south : tout moins les dividers et le centre minimal.
     */
    private int availableSpace(int totalSpace, int dividers) {
        return Math.max(0, totalSpace - dividers - MIN_CENTER_SIZE);
    }

    private int availableVertical() {
        if (parent == null) {
            return 0;
        }
        Insets insets = parent.getInsets();
        int totalH = parent.getHeight() - insets.top - insets.bottom;
        return availableSpace(totalH, dividerSpace(zoneNorth, dividerNorth)
                + dividerSpace(zoneSouth, dividerSouth));
    }

    private int availableHorizontal() {
        if (parent == null) {
            return 0;
        }
        Insets insets = parent.getInsets();
        int totalW = parent.getWidth() - insets.left - insets.right;
        return availableSpace(totalW, dividerSpace(zoneWest, dividerWest)
                + dividerSpace(zoneEast, dividerEast));
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {

            this.parent = parent;

            Insets insets = parent.getInsets();
            int totalW = parent.getWidth() - insets.left - insets.right;
            int totalH = parent.getHeight() - insets.top - insets.bottom;
            int x0 = insets.left;
            int y0 = insets.top;

            HSplitZone zoneFullScreen = findFullScreenZone();
            if (zoneFullScreen != null) {
                layoutFullScreen(x0, y0, totalW, totalH, zoneFullScreen);
                return;
            }

            resolveFlexibleSizes(totalW, totalH);

            int hauteurNorthEffective = getEffectiveSize(zoneNorth, WrapDirection.VERTICAL);
            int hauteurSouthEffective = getEffectiveSize(zoneSouth, WrapDirection.VERTICAL);

            int divNorthH = dividerSpace(zoneNorth, dividerNorth);
            int divSouthH = dividerSpace(zoneSouth, dividerSouth);

            // Fenêtre trop petite : north garde sa taille, south prend le reste.
            // Les tailles mémorisées ne sont pas touchées (elles reviennent si la fenêtre regrandit).
            int availableH = availableSpace(totalH, divNorthH + divSouthH);
            hauteurNorthEffective = Math.min(hauteurNorthEffective, availableH);
            hauteurSouthEffective = Math.min(hauteurSouthEffective, availableH - hauteurNorthEffective);

            int hauteurRangeeCentrale = Math.max(0, totalH
                    - hauteurNorthEffective - divNorthH
                    - hauteurSouthEffective - divSouthH);

            int largeurWestEffective = getEffectiveSize(zoneWest, WrapDirection.HORIZONTAL);
            int largeurEastEffective = getEffectiveSize(zoneEast, WrapDirection.HORIZONTAL);

            int divWestW = dividerSpace(zoneWest, dividerWest);
            int divEastW = dividerSpace(zoneEast, dividerEast);

            int availableW = availableSpace(totalW, divWestW + divEastW);
            largeurWestEffective = Math.min(largeurWestEffective, availableW);
            largeurEastEffective = Math.min(largeurEastEffective, availableW - largeurWestEffective);

            int largeurCenterEffective = Math.max(0, totalW
                    - largeurWestEffective - divWestW
                    - largeurEastEffective - divEastW);

            int y = y0;

            if (zoneNorth != null && !zoneNorth.isEmpty()) {
                zoneNorth.setBounds(x0, y, totalW, hauteurNorthEffective);
                y += hauteurNorthEffective;

                if (dividerNorth != null) {
                    dividerNorth.setBounds(x0, y, totalW, divNorthH);
                    y += divNorthH;
                }
            }

            int yRangee = y;

            if (zoneWest != null && !zoneWest.isEmpty()) {
                zoneWest.setBounds(x0, yRangee, largeurWestEffective, hauteurRangeeCentrale);

                if (dividerWest != null) {
                    dividerWest.setBounds(x0 + largeurWestEffective, yRangee,
                            divWestW, hauteurRangeeCentrale);
                }
            }

            if (zoneCenter != null) {
                int xCenter = x0 + largeurWestEffective + divWestW;
                zoneCenter.setBounds(xCenter, yRangee,
                        largeurCenterEffective, hauteurRangeeCentrale);
            }

            if (zoneEast != null && !zoneEast.isEmpty()) {
                int xEast = x0 + totalW - largeurEastEffective;
                zoneEast.setBounds(xEast, yRangee, largeurEastEffective, hauteurRangeeCentrale);

                if (dividerEast != null) {
                    dividerEast.setBounds(xEast - divEastW, yRangee,
                            divEastW, hauteurRangeeCentrale);
                }
            }

            y = yRangee + hauteurRangeeCentrale;

            if (zoneSouth != null && !zoneSouth.isEmpty()) {
                if (dividerSouth != null) {
                    dividerSouth.setBounds(x0, y, totalW, divSouthH);
                    y += divSouthH;
                }
                zoneSouth.setBounds(x0, y, totalW, hauteurSouthEffective);
            }
        }
    }

    /**
     * La zone en plein écran occupe tout l'espace disponible. Toutes les autres
     * zones sont déjà masquées par HSplitPane (setVisible(false)) — voir le
     * commentaire de classe pour l'explication du bug corrigé ici.
     */
    private void layoutFullScreen(int x0, int y0, int totalW, int totalH, HSplitZone zoneFullScreen) {
        masquerDividers();

        if (!zoneFullScreen.animationInProgress()) {
            zoneFullScreen.setBounds(x0, y0, totalW, totalH);
        }

        masquerZonesHorsFullScreen(zoneFullScreen);
    }

    private HSplitZone findFullScreenZone() {
        for (HSplitZone z : new HSplitZone[]{zoneNorth, zoneSouth, zoneWest, zoneCenter, zoneEast}) {
            if (z != null && z.isFullScreen()) {
                return z;
            }
        }
        return null;
    }

    private void masquerZonesHorsFullScreen(HSplitZone zoneFullScreen) {
        for (HSplitZone z : new HSplitZone[]{zoneNorth, zoneSouth, zoneWest, zoneCenter, zoneEast}) {
            if (z != null && z != zoneFullScreen) {
                z.setBounds(0, 0, 0, 0);
            }
        }
    }

    /**
     * Une zone vide n'est pas positionnée par layoutContainer() : sans ce
     * nettoyage elle garderait ses anciens bounds (et resterait affichée
     * par-dessus le centre). On la ramène à 0x0, comme le fait déjà le mode
     * plein écran.
     */
    private void hideEmptyZones() {
        hideIfEmpty(zoneNorth, dividerNorth);
        hideIfEmpty(zoneSouth, dividerSouth);
        hideIfEmpty(zoneWest, dividerWest);
        hideIfEmpty(zoneEast, dividerEast);
    }

    /**
     * Ramène une zone vide et son divider à 0x0.
     */
    private void hideIfEmpty(HSplitZone zone, HSplitDivider divider) {
        if (zone != null && zone.isEmpty()) {
            zone.setBounds(0, 0, 0, 0);
            if (divider != null) {
                divider.setBounds(0, 0, 0, 0);
            }
        }
    }

    private void masquerDividers() {
        if (dividerNorth != null) {
            dividerNorth.setBounds(0, 0, 0, 0);
        }
        if (dividerSouth != null) {
            dividerSouth.setBounds(0, 0, 0, 0);
        }
        if (dividerWest != null) {
            dividerWest.setBounds(0, 0, 0, 0);
        }
        if (dividerEast != null) {
            dividerEast.setBounds(0, 0, 0, 0);
        }
    }

    /**
     * Donne une taille aux zones bordières qui n'en ont pas encore
     * (expandedSize == null) : elles se partagent équitablement l'espace laissé
     * par les zones à taille déclarée. Un seul calcul générique pour les deux axes.
     */
    private void resolveFlexibleSizes(int totalW, int totalH) {
        resolveAxis(zoneNorth, zoneSouth, totalH);
        resolveAxis(zoneWest, zoneEast, totalW);
    }

    /** Répartit l'espace d'un axe entre deux zones opposées (voir resolveFlexibleSizes). */
    private void resolveAxis(HSplitZone zoneA, HSplitZone zoneB, int totalSpace) {
        if (totalSpace <= 0) {
            return; // pas encore affiché : on réessaiera au prochain layout
        }

        HSplitZone[] pair = {zoneA, zoneB};
        int fixedSpace = 0;
        int flexibleCount = 0;
        for (HSplitZone zone : pair) {
            if (zone == null || zone.isEmpty()) {
                continue;
            }
            if (zone.getExpandedSize() != null) {
                fixedSpace += zone.getExpandedSize();
            } else {
                flexibleCount++;
            }
        }
        if (flexibleCount == 0) {
            return;
        }

        int share = Math.max(0, (totalSpace - fixedSpace) / flexibleCount);
        for (HSplitZone zone : pair) {
            if (zone != null && !zone.isEmpty() && zone.getExpandedSize() == null) {
                zone.applyExpandedSize(share);
            }
        }
    }

    /**
     * Taille effective d'une zone selon son état : taille en cours d'animation,
     * taille réduite si collapsed/floating, ou taille normale.
     * <p>
     * CHANGEMENT — remplace getEffectiveHeight()/getEffectiveWidth()
     * (identiques à l'inversion largeur/hauteur près) par une seule méthode
     * paramétrée par l'axe. Traite aussi FLOATING comme COLLAPSED (une zone
     * flottante occupe la taille réduite dans le split pane, son contenu réel
     * vivant dans la fenêtre détachée — voir
     * HSplitZone.isCollapsedOrFloating()).
     */
    private int getEffectiveSize(HSplitZone zone, WrapDirection axis) {
        if (zone == null || zone.isEmpty()) {
            return 0;
        }

        if (zone.animationInProgress()) {
            return axis.mainSize(zone.getSize());
        }

        if (zone.isCollapsedOrFloating()) {
            return axis.mainSize(zone.getCollapsedSize());
        }

        return wantedSize(zone);
    }

    /**
     * Taille préférée = tailles voulues des zones bordières + dividers + taille
     * préférée du centre (au moins MIN_CENTER_SIZE). Indépendante de la taille
     * actuelle du conteneur, donc utilisable par pack() ou un parent qui s'adapte.
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();

            int north = getEffectiveSize(zoneNorth, WrapDirection.VERTICAL);
            int south = getEffectiveSize(zoneSouth, WrapDirection.VERTICAL);
            int west = getEffectiveSize(zoneWest, WrapDirection.HORIZONTAL);
            int east = getEffectiveSize(zoneEast, WrapDirection.HORIZONTAL);

            Dimension center = zoneCenter != null ? zoneCenter.getPreferredSize() : new Dimension();

            int width = west + east + dividerSpace(zoneWest, dividerWest)
                    + dividerSpace(zoneEast, dividerEast) + Math.max(center.width, MIN_CENTER_SIZE);
            int height = north + south + dividerSpace(zoneNorth, dividerNorth)
                    + dividerSpace(zoneSouth, dividerSouth) + Math.max(center.height, MIN_CENTER_SIZE);

            return new Dimension(width + insets.left + insets.right,
                    height + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        return new Dimension(MIN_CENTER_SIZE + insets.left + insets.right,
                MIN_CENTER_SIZE + insets.top + insets.bottom);
    }
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    private void notifyParent() {
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
}
