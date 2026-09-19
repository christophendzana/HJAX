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

    private int northHeight;

    private int southHeight;

    private int westWidth;

    private int eastWidth;

    private int dividerThickness;

    private Container parent;

    public HSplitPaneRootLayout() {
        this(4);
    }

    public HSplitPaneRootLayout(int dividerThickness) {
        this.dividerThickness = dividerThickness;
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
    private int applyDrag(HSplitZone zone, int currentSize, int delta) {
        if (zone == null || zone.isCollapsed()) {
            return currentSize;
        }
        return Math.max(0, currentSize + delta);
    }

    private void onDragNorth(int delta) {
        northHeight = applyDrag(zoneNorth, northHeight, delta);
        notifyParent();
    }

    private void onDragSouth(int delta) {
        southHeight = applyDrag(zoneSouth, southHeight, -delta);
        notifyParent();
    }

    private void onDragWest(int delta) {
        westWidth = applyDrag(zoneWest, westWidth, delta);
        notifyParent();
    }

    private void onDragEast(int delta) {
        eastWidth = applyDrag(zoneEast, eastWidth, -delta);
        notifyParent();
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

            if (northHeight == 0 && southHeight == 0
                    && westWidth == 0 && eastWidth == 0) {
                initializeSizes(totalW, totalH);
            }

            int hauteurNorthEffective = getEffectiveSize(zoneNorth, northHeight, WrapDirection.VERTICAL);
            int hauteurSouthEffective = getEffectiveSize(zoneSouth, southHeight, WrapDirection.VERTICAL);

            int divNorthH = (dividerNorth != null && zoneNorth != null
                    && !zoneNorth.isEmpty()) ? dividerThickness : 0;
            int divSouthH = (dividerSouth != null && zoneSouth != null
                    && !zoneSouth.isEmpty()) ? dividerThickness : 0;

            int hauteurRangeeCentrale = totalH
                    - hauteurNorthEffective - divNorthH
                    - hauteurSouthEffective - divSouthH;

            int largeurWestEffective = getEffectiveSize(zoneWest, westWidth, WrapDirection.HORIZONTAL);
            int largeurEastEffective = getEffectiveSize(zoneEast, eastWidth, WrapDirection.HORIZONTAL);

            int divWestW = (dividerWest != null && zoneWest != null
                    && !zoneWest.isEmpty()) ? dividerThickness : 0;
            int divEastW = (dividerEast != null && zoneEast != null
                    && !zoneEast.isEmpty()) ? dividerThickness : 0;

            int largeurCenterEffective = totalW
                    - largeurWestEffective - divWestW
                    - largeurEastEffective - divEastW;

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
     * Calcule les tailles initiales des 4 zones bordières (répartition entre
     * taille fixe déclarée et espace flexible restant).
     *
     * CHANGEMENT — remplace les deux blocs quasi identiques (vertical puis
     * horizontal) par un seul calcul générique paramétré par l'axe.
     */
    private void initializeSizes(int totalW, int totalH) {
        int[] vertical = computeAxisSizes(zoneNorth, zoneSouth, totalH);
        northHeight = vertical[0];
        southHeight = vertical[1];

        int[] horizontal = computeAxisSizes(zoneWest, zoneEast, totalW);
        westWidth = horizontal[0];
        eastWidth = horizontal[1];
    }

    /**
     * Répartit l'espace disponible entre deux zones opposées sur un axe : une
     * zone avec une taille initiale déclarée la conserve, les zones sans taille
     * déclarée se partagent équitablement l'espace restant.
     */
    private int[] computeAxisSizes(HSplitZone zoneA, HSplitZone zoneB, int totalSpace) {
        int espaceFixe = 0;
        int nbFlexible = 0;
        Integer sizeA = null;
        Integer sizeB = null;

        if (zoneA != null && !zoneA.isEmpty()) {
            Integer taille = zoneA.getInitialSize();
            if (taille != null) {
                sizeA = taille;
                espaceFixe += sizeA;
            } else {
                nbFlexible++;
            }
        }

        if (zoneB != null && !zoneB.isEmpty()) {
            Integer taille = zoneB.getInitialSize();
            if (taille != null) {
                sizeB = taille;
                espaceFixe += sizeB;
            } else {
                nbFlexible++;
            }
        }

        int espaceRestant = totalSpace - espaceFixe;
        int partFlexible = nbFlexible > 0 ? espaceRestant / nbFlexible : 0;

        int resultA = (sizeA != null) ? sizeA : partFlexible;
        int resultB = (sizeB != null) ? sizeB : partFlexible;
        return new int[]{resultA, resultB};
    }

    /**
     * Taille effective d'une zone selon son état : taille en cours d'animation,
     * taille réduite si collapsed/floating, ou taille normale.
     *
     * CHANGEMENT — remplace getEffectiveHeight()/getEffectiveWidth()
     * (identiques à l'inversion largeur/hauteur près) par une seule méthode
     * paramétrée par l'axe. Traite aussi FLOATING comme COLLAPSED (une zone
     * flottante occupe la taille réduite dans le split pane, son contenu réel
     * vivant dans la fenêtre détachée — voir
     * HSplitZone.isCollapsedOrFloating()).
     */
    private int getEffectiveSize(HSplitZone zone, int size, WrapDirection axis) {
        if (zone == null || zone.isEmpty()) {
            return 0;
        }

        if (zone.animationInProgress()) {
            return axis.mainSize(zone.getSize());
        }

        if (zone.isCollapsedOrFloating()) {
            return axis.mainSize(zone.getCollapsedSize());
        }

        return size;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return parent.getSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(100, 100);
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
