package hsplitpane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * Gestionnaire de disposition racine du HSplitPane.
 *
 * Ce layout est responsable du positionnement et du dimensionnement de toutes
 * les zones (HSplitZone) et de tous les séparateurs (HSplitDivider) à
 * l'intérieur du HSplitPane.
 *
 * Logique de disposition : - Les zones NORTH et SOUTH occupent toute la largeur
 * du composant. - Les zones WEST, CENTER et EAST se partagent la largeur
 * restante. - Les séparateurs sont intercalés entre les zones adjacentes. -
 * Quand une zone est réduite ou vide, son espace est redistribué
 * proportionnellement aux autres zones visibles. - Au redimensionnement de la
 * fenêtre, toutes les zones s'adaptent proportionnellement à leur taille
 * courante.
 *
 * Ce layout maintient des "tailles courantes" pour chaque zone afin de gérer la
 * proportionnalité lors du resize et du drag des séparateurs.
 */
public class HSplitPaneRootLayout implements LayoutManager2 {

    // -------------------------------------------------------------------------
    // Références aux zones et séparateurs
    // -------------------------------------------------------------------------
    /**
     * Zone du haut, peut être null si non utilisée.
     */
    private HSplitZone zoneNorth;

    /**
     * Zone du bas, peut être null si non utilisée.
     */
    private HSplitZone zoneSouth;

    /**
     * Zone de gauche, peut être null si non utilisée.
     */
    private HSplitZone zoneWest;

    /**
     * Zone centrale, peut être null si désactivée.
     */
    private HSplitZone zoneCenter;

    /**
     * Zone de droite, peut être null si non utilisée.
     */
    private HSplitZone zoneEast;

    // -------------------------------------------------------------------------
    // Séparateurs
    // -------------------------------------------------------------------------
    /**
     * Séparateur entre NORTH et la rangée centrale (WEST/CENTER/EAST).
     */
    private HSplitDivider dividerNorth;

    /**
     * Séparateur entre la rangée centrale et SOUTH.
     */
    private HSplitDivider dividerSouth;

    /**
     * Séparateur entre WEST et CENTER (ou WEST et EAST si pas de CENTER).
     */
    private HSplitDivider dividerWest;

    /**
     * Séparateur entre CENTER et EAST.
     */
    private HSplitDivider dividerEast;

    // -------------------------------------------------------------------------
    // Tailles courantes des zones
    // Ces valeurs sont mises à jour lors de chaque drag et lors du resize.
    // -------------------------------------------------------------------------
    /**
     * Hauteur courante de la zone NORTH en pixels.
     */
    private int northHeight;

    /**
     * Hauteur courante de la zone SOUTH en pixels.
     */
    private int southHeight;

    /**
     * Largeur courante de la zone WEST en pixels.
     */
    private int westWidth;

    /**
     * Largeur courante de la zone CENTER en pixels.
     */
    private int centerWidth;

    /**
     * Largeur courante de la zone EAST en pixels.
     */
    private int eastWidth;

    // -------------------------------------------------------------------------
    // Épaisseur des séparateurs
    // -------------------------------------------------------------------------
    /**
     * Épaisseur utilisée pour tous les séparateurs lors du calcul des bounds.
     */
    private int dividerThickness;

    // -------------------------------------------------------------------------
    // Référence au conteneur parent
    // Nécessaire pour déclencher un revalidate depuis les callbacks de drag.
    // -------------------------------------------------------------------------
    /**
     * Référence au HSplitPane parent, enregistrée lors du premier
     * layoutContainer.
     */
    private Container parent;

    /**
     * Position de la zone actuellement en fullscreen, ou null si aucune. Quand
     * ce champ est non-null, layoutContainer délègue entièrement à
     * layoutFullScreen() au lieu du calcul normal.
     */
    private HSplitPane.ZonePosition zoneFullScreen;

    /** true pendant l'animation d'entrée en fullscreen — layoutContainer ne fait rien. */
private boolean animationFullScreenEnCours = false;
    
    // =========================================================================
    // Constructeur
    // =========================================================================
    /**
     * Crée le layout racine avec l'épaisseur de séparateur par défaut.
     */
    public HSplitPaneRootLayout() {
        this(4);
    }

    /**
     * Crée le layout racine avec une épaisseur de séparateur personnalisée.
     *
     * @param dividerThickness l'épaisseur en pixels des séparateurs
     */
    public HSplitPaneRootLayout(int dividerThickness) {
        this.dividerThickness = dividerThickness;
    }

    // =========================================================================
    // Enregistrement des zones et séparateurs
    // =========================================================================
    /**
     * Enregistre les zones gérées par ce layout. Appelé par HSplitPane lors de
     * sa construction.
     *
     * @param north zone NORTH ou null
     * @param south zone SOUTH ou null
     * @param west zone WEST ou null
     * @param center zone CENTER ou null
     * @param east zone EAST ou null
     */
    public void saveZones(HSplitZone north, HSplitZone south,
            HSplitZone west, HSplitZone center,
            HSplitZone east) {
        this.zoneNorth = north;
        this.zoneSouth = south;
        this.zoneWest = west;
        this.zoneCenter = center;
        this.zoneEast = east;
    }

    /**
     * Enregistre les séparateurs gérés par ce layout et branche leurs
     * callbacks. Appelé par HSplitPane lors de sa construction.
     *
     * @param north séparateur sous NORTH ou null
     * @param south séparateur au-dessus de SOUTH ou null
     * @param west séparateur à droite de WEST ou null
     * @param east séparateur à droite de CENTER ou null
     */
    public void saveDividers(HSplitDivider north, HSplitDivider south,
            HSplitDivider west, HSplitDivider east) {
        this.dividerNorth = north;
        this.dividerSouth = south;
        this.dividerWest = west;
        this.dividerEast = east;

        // On branche les callbacks de drag sur les méthodes de ce layout
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

    // =========================================================================
    // Logique de drag des séparateurs
    // =========================================================================
    /**
     * Réagit au drag du séparateur NORTH. Agrandit ou rétrécit NORTH et ajuste
     * la hauteur de la rangée centrale.
     *
     * @param delta le déplacement vertical en pixels (positif = vers le bas)
     */
    private void onDragNorth(int delta) {
        if (zoneNorth == null || zoneNorth.isCollapsed()) {
            return;
        }
        northHeight = Math.max(0, northHeight + delta);
        notifyParent();
    }

    /**
     * Réagit au drag du séparateur SOUTH. Un déplacement vers le haut (delta
     * négatif) agrandit SOUTH.
     *
     * @param delta le déplacement vertical en pixels
     */
    private void onDragSouth(int delta) {
        if (zoneSouth == null || zoneSouth.isCollapsed()) {
            return;
        }
        southHeight = Math.max(0, southHeight - delta);
        notifyParent();
    }

    /**
     * Réagit au drag du séparateur WEST.
     *
     * @param delta le déplacement horizontal en pixels
     */
    private void onDragWest(int delta) {
        if (zoneWest == null || zoneWest.isCollapsed()) {
            return;
        }
        westWidth = Math.max(0, westWidth + delta);
        notifyParent();
    }

    /**
     * Réagit au drag du séparateur EAST. Un déplacement vers la gauche (delta
     * négatif) agrandit EAST.
     *
     * @param delta le déplacement horizontal en pixels
     */
    private void onDragEast(int delta) {
        if (zoneEast == null || zoneEast.isCollapsed()) {
            return;
        }
        eastWidth = Math.max(0, eastWidth - delta);
        notifyParent();
    }

    // =========================================================================
    // Disposition principale
    // =========================================================================
    /**
     * Calcule et applique les bounds de toutes les zones et séparateurs.
     *
     * Le calcul se fait en plusieurs étapes : 1. Initialisation des tailles
     * courantes si c'est le premier appel. 2. Calcul des hauteurs de NORTH et
     * SOUTH. 3. Calcul des largeurs de WEST, CENTER et EAST. 4. Application des
     * bounds à chaque composant.
     *
     * @param parent le HSplitPane contenant tous les composants
     */
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {

            // On mémorise le parent pour pouvoir le notifier depuis les callbacks de drag
            this.parent = parent;

//            // Pendant l'animation fullscreen, on ne recalcule rien.
//// C'est animerVersBounds() dans HSplitZone qui gère directement les bounds.
//if (animationFullScreenEnCours) return;
            
            Insets insets = parent.getInsets();
            int totalW = parent.getWidth() - insets.left - insets.right;
            int totalH = parent.getHeight() - insets.top - insets.bottom;
            int x0 = insets.left;
            int y0 = insets.top;

            
            
            // Si une zone est en fullscreen, on délègue à layoutFullScreen()
// qui court-circuite tout le calcul normal.
            if (zoneFullScreen != null) {
                layoutFullScreen(parent, x0, y0, totalW, totalH);
                return;
            }

            // Initialisation des tailles si nécessaire
            if (northHeight == 0 && southHeight == 0
                    && westWidth == 0 && eastWidth == 0) {
                initializeSizes(totalW, totalH);
            }

            // --- Calcul de la hauteur effective de la rangée centrale ---
            int hauteurNorthEffective = getEffectiveHeight(zoneNorth, northHeight);
            int hauteurSouthEffective = getEffectiveHeight(zoneSouth, southHeight);

            // Espace consommé par les séparateurs horizontaux
            int divNorthH = (dividerNorth != null && zoneNorth != null
                    && !zoneNorth.isEmpty()) ? dividerThickness : 0;
            int divSouthH = (dividerSouth != null && zoneSouth != null
                    && !zoneSouth.isEmpty()) ? dividerThickness : 0;

            int hauteurRangeeCentrale = totalH
                    - hauteurNorthEffective - divNorthH
                    - hauteurSouthEffective - divSouthH;

            // --- Calcul des largeurs de la rangée centrale ---
            int largeurWestEffective = getEffectiveWidth(zoneWest, westWidth);
            int largeurEastEffective = getEffectiveWidth(zoneEast, eastWidth);

            int divWestW = (dividerWest != null && zoneWest != null
                    && !zoneWest.isEmpty()) ? dividerThickness : 0;
            int divEastW = (dividerEast != null && zoneEast != null
                    && !zoneEast.isEmpty()) ? dividerThickness : 0;

            int largeurCenterEffective = totalW
                    - largeurWestEffective - divWestW
                    - largeurEastEffective - divEastW;

            // --- Positionnement ---
            int y = y0;

            // Zone NORTH
            if (zoneNorth != null && !zoneNorth.isEmpty()) {
                zoneNorth.setBounds(x0, y, totalW, hauteurNorthEffective);
                y += hauteurNorthEffective;

                if (dividerNorth != null) {
                    dividerNorth.setBounds(x0, y, totalW, divNorthH);
                    y += divNorthH;
                }
            }

            // Rangée centrale : WEST | CENTER | EAST
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

            // Zone SOUTH
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
 * Dispose les zones quand une d'elles est en mode fullscreen.
 *
 * La zone fullscreen occupe tout l'espace disponible, déduction faite
 * des headers visibles des zones adjacentes.
 * Les dividers sont masqués (taille 0) en mode fullscreen.
 *
 * WEST  fullscreen : x=0,           y=0,            w=totalW-headerEAST, h=totalH
 * EAST  fullscreen : x=headerWEST,  y=0,            w=totalW-headerWEST, h=totalH
 * NORTH fullscreen : x=0,           y=0,            w=totalW,            h=totalH-headerWEST
 * SOUTH fullscreen : x=0,           y=headerWEST,   w=totalW,            h=totalH-headerWEST
 * CENTER fullscreen: x=0,           y=0,            w=totalW,            h=totalH
 */
private void layoutFullScreen(Container parent, int x0, int y0, int totalW, int totalH) {

    // Épaisseur des headers des zones adjacentes
    int headerWestW  = obtenirEpaisseurHeader(zoneWest);
    int headerEastW  = obtenirEpaisseurHeader(zoneEast);

    // On masque tous les dividers en fullscreen
    masquerDividers();

    // Positionnement de la zone fullscreen
    HSplitZone zoneFS = obtenirZone(zoneFullScreen);
    if (zoneFS == null) return;

    int x, y, w, h;

    switch (zoneFullScreen) {

        case WEST:
            x = x0;
            y = y0;
            w = totalW ;
            h = totalH;
            break;

        case EAST:
            int offW = (zoneWest != null && !zoneWest.isEmpty() ? headerWestW : 0);
            x = x0 + offW;
            y = y0;
            w = totalW;
            h = totalH;
            break;

        case NORTH:
            // On réserve en bas l'espace pour que les headers WEST/EAST
            // restent accessibles verticalement
            int resN = (zoneWest != null && !zoneWest.isEmpty()
                     || zoneEast != null && !zoneEast.isEmpty())
                     ? headerWestW : 0;
            x = x0;
            y = y0;
            w = totalW;
            h = totalH ;
            break;

        case SOUTH:
            int resS = (zoneWest != null && !zoneWest.isEmpty()
                     || zoneEast != null && !zoneEast.isEmpty())
                     ? headerWestW : 0;
            x = x0;
            y = y0 ;
            w = totalW;
            h = totalH;
            break;

        case CENTER:
        default:
            x = x0;
            y = y0;
            w = totalW;
            h = totalH;
            break;
    }

    // On ne force les bounds que si la zone n'est pas en cours d'animation.
// Pendant l'animation, c'est animerVersBounds() qui gère les bounds.
if (!zoneFS.animationInProgress()) {
    zoneFS.setBounds(x, y, w, h);
}

    masquerZonesHorsFullScreen();
}


public void setAnimationFullScreenEnCours(boolean enCours) {
    this.animationFullScreenEnCours = enCours;
}

/**
 * Met à taille nulle toutes les zones qui ne sont pas la zone fullscreen
 * et qui n'ont pas été repositionnées explicitement.
 */
/** Masque toutes les zones sauf la zone fullscreen. */
private void masquerZonesHorsFullScreen() {
    if (zoneFullScreen != HSplitPane.ZonePosition.NORTH && zoneNorth != null)
        zoneNorth.setBounds(0, 0, 0, 0);
    if (zoneFullScreen != HSplitPane.ZonePosition.SOUTH && zoneSouth != null)
        zoneSouth.setBounds(0, 0, 0, 0);
    if (zoneFullScreen != HSplitPane.ZonePosition.WEST  && zoneWest  != null)
        zoneWest.setBounds(0, 0, 0, 0);
    if (zoneFullScreen != HSplitPane.ZonePosition.EAST  && zoneEast  != null)
        zoneEast.setBounds(0, 0, 0, 0);
    if (zoneFullScreen != HSplitPane.ZonePosition.CENTER && zoneCenter != null)
        zoneCenter.setBounds(0, 0, 0, 0);
}

/** Masque tous les dividers en mode fullscreen (taille 0). */
private void masquerDividers() {
    if (dividerNorth != null) dividerNorth.setBounds(0, 0, 0, 0);
    if (dividerSouth != null) dividerSouth.setBounds(0, 0, 0, 0);
    if (dividerWest  != null) dividerWest .setBounds(0, 0, 0, 0);
    if (dividerEast  != null) dividerEast .setBounds(0, 0, 0, 0);
}

/** Retourne l'épaisseur du header d'une zone, ou 0 si absent. */
private int obtenirEpaisseurHeader(HSplitZone zone) {
    if (zone == null || zone.isEmpty() || zone.getHeader() == null) return 0;
    return zone.getHeader().getEpaisseur();
}

/** Retourne la zone correspondant à la position. */
private HSplitZone obtenirZone(HSplitPane.ZonePosition position) {
    if (position == null) return null;
    switch (position) {
        case NORTH:  return zoneNorth;
        case SOUTH:  return zoneSouth;
        case WEST:   return zoneWest;
        case CENTER: return zoneCenter;
        case EAST:   return zoneEast;
        default:     return null;
    }
}
    
    // =========================================================================
    // Initialisation des tailles
    // =========================================================================
    /**
     * Initialise les tailles courantes à partir des tailles initiales
     * configurées dans chaque zone, ou calcule des valeurs par défaut si elles
     * ne sont pas définies.
     *
     * Les zones sans taille initiale se partagent équitablement l'espace
     * restant.
     *
     * @param totalW largeur totale disponible
     * @param totalH hauteur totale disponible
     */
    private void initializeSizes(int totalW, int totalH) {

        // -- Hauteurs verticales (NORTH / SOUTH) --
        int espaceVerticalFixe = 0;
        int nbVerticalFlexible = 0;

        if (zoneNorth != null && !zoneNorth.isEmpty()) {
            Dimension d = zoneNorth.getInitialSize();
            if (d != null) {
                northHeight = d.height;
                espaceVerticalFixe += northHeight;
            } else {
                nbVerticalFlexible++;
            }
        }

        if (zoneSouth != null && !zoneSouth.isEmpty()) {
            Dimension d = zoneSouth.getInitialSize();
            if (d != null) {
                southHeight = d.height;
                espaceVerticalFixe += southHeight;
            } else {
                nbVerticalFlexible++;
            }
        }

        // Distribution de l'espace vertical restant aux zones flexibles
        int espaceVerticalRestant = totalH - espaceVerticalFixe;
        int partFlexibleV = nbVerticalFlexible > 0
                ? espaceVerticalRestant / (nbVerticalFlexible)
                : 0;

        if (zoneNorth != null && zoneNorth.getInitialSize() == null) {
            northHeight = partFlexibleV;
        }
        if (zoneSouth != null && zoneSouth.getInitialSize() == null) {
            southHeight = partFlexibleV;
        }

        // -- Largeurs horizontales (WEST / EAST) --
        int espaceHorizontalFixe = 0;
        int nbHorizontalFlexible = 0;

        if (zoneWest != null && !zoneWest.isEmpty()) {
            Dimension d = zoneWest.getInitialSize();
            if (d != null) {
                westWidth = d.width;
                espaceHorizontalFixe += westWidth;
            } else {
                nbHorizontalFlexible++;
            }
        }

        if (zoneEast != null && !zoneEast.isEmpty()) {
            Dimension d = zoneEast.getInitialSize();
            if (d != null) {
                eastWidth = d.width;
                espaceHorizontalFixe += eastWidth;
            } else {
                nbHorizontalFlexible++;
            }
        }

        int espaceHorizontalRestant = totalW - espaceHorizontalFixe;
        int partFlexibleH = nbHorizontalFlexible > 0
                ? espaceHorizontalRestant / (nbHorizontalFlexible)
                : 0;

        if (zoneWest != null && zoneWest.getInitialSize() == null) {
            westWidth = partFlexibleH;
        }
        if (zoneEast != null && zoneEast.getInitialSize() == null) {
            eastWidth = partFlexibleH;
        }
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================
    /**
     * Retourne la hauteur effective d'une zone. Si la zone est collapsed,
     * retourne la hauteur du header uniquement pour que le bouton toggle reste
     * visible. Si la zone est null ou vide, retourne 0.
     *
     * @param zone la zone dont on veut la hauteur
     * @param hauteur la hauteur courante stockée dans ce layout
     * @return la hauteur en pixels à utiliser pour le positionnement
     */
    private int getEffectiveHeight(HSplitZone zone, int hauteur) {
        if (zone == null || zone.isEmpty()) {
            return 0;
        }

        // Pendant une animation, on laisse la zone gérer sa propre taille
        if (zone.animationInProgress()) {
            return zone.getHeight();
        }

        if (zone.isCollapsed()) {
            return zone.getCollapsedSize().height;
        }
        return hauteur;
    }

    /**
     * Retourne la largeur effective d'une zone. Si la zone est collapsed,
     * retourne la largeur du header uniquement. Si la zone est null ou vide,
     * retourne 0.
     *
     * @param zone la zone dont on veut la largeur
     * @param largeur la largeur courante stockée dans ce layout
     * @return la largeur en pixels à utiliser pour le positionnement
     */
    private int getEffectiveWidth(HSplitZone zone, int largeur) {
        if (zone == null || zone.isEmpty()) {
            return 0;
        }

        // Pendant une animation, on laisse la zone gérer sa propre taille
        if (zone.animationInProgress()) {
            return zone.getWidth();
        }

        if (zone.isCollapsed()) {
            return zone.getCollapsedSize().width;
        }
        return largeur;
    }

    /**
     * Notifie le layout qu'une zone est en fullscreen ou qu'on en sort. Appelé
     * par HSplitPane.enterFullScreen() et exitFullScreen().
     *
     * @param position la zone en fullscreen, ou null pour sortir du mode
     */
    public void setZoneFullScreen(HSplitPane.ZonePosition position) {
        this.zoneFullScreen = position;
    }

    // =========================================================================
    // Tailles du conteneur parent
    // =========================================================================
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

    // =========================================================================
    // Méthodes non utilisées mais requises par l'interface LayoutManager2
    // =========================================================================
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
        // Cette méthode est appelée par Swing lui-même pendant son cycle interne.
        // Elle ne doit JAMAIS déclencher un revalidate() sous peine de boucle infinie.
        // On ne fait rien ici volontairement.
    }

    /**
     * Notifie le parent qu'un recalcul du layout est nécessaire. Appelée
     * uniquement depuis les callbacks de drag, jamais depuis invalidateLayout.
     */
    private void notifyParent() {
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
}
