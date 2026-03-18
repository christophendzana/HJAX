/*
 * HRibbonTabsTheme.java
 *
 * Classe abstraite définissant le contrat de thème pour HRibbonTabs.
 * Un thème couvre l'intégralité de la surface visuelle du composant :
 * barre d'onglets, zone de contenu, Ribbon, groupes, headers et bouton collapse.
 *
 * UTILISATION — thème prédéfini :
 *   ribbonTabs.setTheme(HRibbonTabsTheme.PRIMARY);
 *
 * UTILISATION — thème personnalisé :
 *   HRibbonTabsTheme monTheme = new HRibbonTabsTheme() {
 *       public Color getTabBarBackground()      { return new Color(30, 30, 30); }
 *       public Color getTabBackground()         { return new Color(45, 45, 45); }
 *       // ... implémenter toutes les méthodes abstraites
 *   };
 *   ribbonTabs.setTheme(monTheme);
 *
 * THÈMES PRÉDÉFINIS :
 *   HRibbonTabsTheme.PRIMARY   — bleu Bootstrap
 *   HRibbonTabsTheme.SECONDARY — gris neutre
 *   HRibbonTabsTheme.SUCCESS   — vert
 *   HRibbonTabsTheme.DANGER    — rouge
 *   HRibbonTabsTheme.WARNING   — jaune
 *   HRibbonTabsTheme.INFO      — cyan
 *   HRibbonTabsTheme.DARK      — sombre
 *   HRibbonTabsTheme.OCEAN     — vert océan
 *   HRibbonTabsTheme.PURPLE    — violet
 */
package rubban;

import java.awt.Color;

/**
 * HRibbonTabsTheme — contrat de thème visuel pour HRibbonTabs.
 *
 * Étendre cette classe pour créer un thème personnalisé. Les thèmes prédéfinis
 * sont accessibles via les constantes statiques.
 *
 * @author FIDELE
 * @version 1.0
 */
public abstract class HRibbonTabsTheme {

    // =========================================================================
    // THÈMES PRÉDÉFINIS — CONSTANTES STATIQUES
    // Les implémentations concrètes sont des classes internes privées
    // inaccessibles depuis l'extérieur — l'utilisateur passe par ces constantes
    // =========================================================================
    /**
     * Thème bleu primaire — nuances de bleu Bootstrap (#0D6EFD)
     */
    public static final HRibbonTabsTheme PRIMARY = new PrimaryTheme();

    /**
     * Thème gris neutre — nuances de gris secondaire (#6C757D)
     */
    public static final HRibbonTabsTheme SECONDARY = new SecondaryTheme();

    /**
     * Thème vert succès — nuances de vert (#198754)
     */
    public static final HRibbonTabsTheme SUCCESS = new SuccessTheme();

    /**
     * Thème rouge danger — nuances de rouge (#DC3545)
     */
    public static final HRibbonTabsTheme DANGER = new DangerTheme();

    /**
     * Thème jaune avertissement — nuances de jaune (#FFC107)
     */
    public static final HRibbonTabsTheme WARNING = new WarningTheme();

    /**
     * Thème cyan info — nuances de cyan (#0DCAF0)
     */
    public static final HRibbonTabsTheme INFO = new InfoTheme();

    /**
     * Thème sombre — nuances de gris foncé
     */
    public static final HRibbonTabsTheme DARK = new DarkTheme();

    /**
     * Thème vert océan — nuances de vert teal (#009688)
     */
    public static final HRibbonTabsTheme OCEAN = new OceanTheme();

    /**
     * Thème violet — nuances de violet (#9C27B0)
     */
    public static final HRibbonTabsTheme PURPLE = new PurpleTheme();

    /**
     * Thème clair neutre — gris très clairs et blanc, sans couleur dominante
     */
    public static final HRibbonTabsTheme LIGHT = new LightTheme();

    // =========================================================================
    // CONTRAT — BARRE D'ONGLETS
    // =========================================================================
    /**
     * Couleur de fond générale de la barre d'onglets. C'est la couleur du
     * bandeau qui contient tous les onglets.
     *
     * @return couleur de fond de la barre d'onglets
     */
    public abstract Color getTabBarBackground();

    /**
     * Couleur de fond d'un onglet dans son état normal (non sélectionné).
     *
     * @return couleur de fond d'un onglet normal
     */
    public abstract Color getTabBackground();

    /**
     * Couleur de fond de l'onglet actuellement sélectionné. Doit créer une
     * continuité visuelle avec getContentBackground() pour que l'onglet et la
     * zone de contenu semblent former un seul bloc.
     *
     * @return couleur de fond de l'onglet sélectionné
     */
    public abstract Color getTabSelectedBackground();

    /**
     * Couleur de fond d'un onglet au survol de la souris.
     *
     * @return couleur de fond au survol
     */
    public abstract Color getTabHoverBackground();

    /**
     * Couleur du texte des onglets non sélectionnés.
     *
     * @return couleur du texte normal
     */
    public abstract Color getTabTextColor();

    /**
     * Couleur du texte de l'onglet sélectionné.
     *
     * @return couleur du texte sélectionné
     */
    public abstract Color getTabSelectedTextColor();

    /**
     * Couleur de l'indicateur de sélection (barre sous l'onglet actif).
     *
     * @return couleur de l'indicateur
     */
    public abstract Color getTabIndicatorColor();

    // =========================================================================
    // CONTRAT — ZONE DE CONTENU DU TABBEDPANE
    // =========================================================================
    /**
     * Couleur de fond de la zone de contenu du TabbedPane. Doit être cohérente
     * avec getTabSelectedBackground() pour éviter l'impression de deux blocs
     * séparés.
     *
     * @return couleur de fond du contenu
     */
    public abstract Color getContentBackground();

    /**
     * Couleur de la bordure de la zone de contenu.
     *
     * @return couleur de la bordure du contenu
     */
    public abstract Color getContentBorderColor();

    // =========================================================================
    // CONTRAT — RIBBON
    // =========================================================================
    /**
     * Couleur de fond général du Ribbon. Sert de fond entre les groupes et
     * autour d'eux.
     *
     * @return couleur de fond du Ribbon
     */
    public abstract Color getRibbonBackground();

    /**
     * Couleur de la bordure inférieure du Ribbon.
     *
     * @return couleur de la bordure du Ribbon
     */
    public abstract Color getRibbonBorderColor();

    // =========================================================================
    // CONTRAT — GROUPES
    // =========================================================================
    /**
     * Couleur de départ du dégradé de fond des groupes (coin supérieur gauche).
     *
     * @return couleur de départ du dégradé
     */
    public abstract Color getGroupGradientStart();

    /**
     * Couleur de fin du dégradé de fond des groupes (coin inférieur droit).
     *
     * @return couleur de fin du dégradé
     */
    public abstract Color getGroupGradientEnd();

    /**
     * Couleur de la bordure normale d'un groupe.
     *
     * @return couleur de bordure normale
     */
    public abstract Color getGroupBorderColor();

    /**
     * Couleur de la bordure d'un groupe au survol de la souris.
     *
     * @return couleur de bordure au survol
     */
    public abstract Color getGroupHoverBorderColor();

    /**
     * Teinte semi-transparente appliquée sur le groupe au survol. Doit avoir
     * une composante alpha < 255 pour l'effet de transparence.
     *
     * @return couleur de teinte au survol (avec alpha)
     */
    public abstract Color getGroupHoverTint();

    // =========================================================================
    // CONTRAT — HEADERS DES GROUPES
    // =========================================================================
    /**
     * Couleur de fond des headers (titres) des groupes.
     *
     * @return couleur de fond des headers
     */
    public abstract Color getHeaderBackground();

    /**
     * Couleur du texte des headers des groupes.
     *
     * @return couleur du texte des headers
     */
    public abstract Color getHeaderTextColor();

    // =========================================================================
    // CONTRAT — BOUTON COLLAPSE
    // =========================================================================
    /**
     * Couleur de fond du bouton collapse/expand.
     *
     * @return couleur de fond du bouton collapse
     */
    public abstract Color getCollapseButtonBackground();

    /**
     * Couleur de l'icône flèche du bouton collapse/expand.
     *
     * @return couleur de l'icône du bouton collapse
     */
    public abstract Color getCollapseButtonIconColor();

    // =========================================================================
    // MÉTHODE UTILITAIRE — DISPONIBLE POUR TOUTES LES IMPLÉMENTATIONS
    // =========================================================================
    /**
     * Éclaircit une couleur d'un facteur donné. Utile pour générer des nuances
     * dans les thèmes personnalisés. Un facteur de 0.1 éclaircit légèrement,
     * 0.5 éclaircit fortement.
     *
     * @param color la couleur de base
     * @param factor facteur d'éclaircissement entre 0.0 et 1.0
     * @return la couleur éclaircie
     */
    protected static Color lighten(Color color, float factor) {
        // Interpolation linéaire entre la couleur et le blanc
        int r = color.getRed() + (int) ((255 - color.getRed()) * factor);
        int g = color.getGreen() + (int) ((255 - color.getGreen()) * factor);
        int b = color.getBlue() + (int) ((255 - color.getBlue()) * factor);
        return new Color(
                Math.min(255, r),
                Math.min(255, g),
                Math.min(255, b)
        );
    }

    /**
     * Assombrit une couleur d'un facteur donné. Un facteur de 0.1 assombrit
     * légèrement, 0.5 assombrit fortement.
     *
     * @param color la couleur de base
     * @param factor facteur d'assombrissement entre 0.0 et 1.0
     * @return la couleur assombrie
     */
    protected static Color darken(Color color, float factor) {
        // Interpolation linéaire entre la couleur et le noir
        int r = (int) (color.getRed() * (1f - factor));
        int g = (int) (color.getGreen() * (1f - factor));
        int b = (int) (color.getBlue() * (1f - factor));
        return new Color(
                Math.max(0, r),
                Math.max(0, g),
                Math.max(0, b)
        );
    }

    /**
     * Crée une version semi-transparente d'une couleur.
     *
     * @param color la couleur de base
     * @param alpha valeur d'opacité entre 0 (transparent) et 255 (opaque)
     * @return la couleur avec l'alpha appliqué
     */
    protected static Color withAlpha(Color color, int alpha) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, Math.min(255, alpha))
        );
    }

    // =========================================================================
    // CLASSES INTERNES PRIVÉES — THÈMES PRÉDÉFINIS
    // Inaccessibles depuis l'extérieur — l'utilisateur passe par les constantes
    // statiques PUBLIC définies en haut de cette classe
    // =========================================================================
    // -------------------------------------------------------------------------
    // PRIMARY — Bleu Bootstrap (#0D6EFD)
    // -------------------------------------------------------------------------
    private static final class PrimaryTheme extends HRibbonTabsTheme {

        // Couleur de base du thème
        private static final Color BASE = new Color(13, 110, 253);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.6f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.6f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.60f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }

    // -------------------------------------------------------------------------
    // SECONDARY — Gris neutre (#6C757D)
    // -------------------------------------------------------------------------
    private static final class SecondaryTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(108, 117, 125);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.5f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.5f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.4f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.3f);
        }
    }

    // -------------------------------------------------------------------------
    // SUCCESS — Vert (#198754)
    // -------------------------------------------------------------------------
    private static final class SuccessTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(25, 135, 84);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }

    // -------------------------------------------------------------------------
    // DANGER — Rouge (#DC3545)
    // -------------------------------------------------------------------------
    private static final class DangerTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(220, 53, 69);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.82f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.68f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.58f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.72f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }

    // -------------------------------------------------------------------------
    // WARNING — Jaune (#FFC107)
    // -------------------------------------------------------------------------
    private static final class WarningTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(255, 193, 7);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.5f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.40f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.40f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.72f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(darken(BASE, 0.2f), 50);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.45f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.55f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.60f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.45f);
        }
    }

    // -------------------------------------------------------------------------
    // INFO — Cyan (#0DCAF0)
    // -------------------------------------------------------------------------
    private static final class InfoTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(13, 202, 240);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.78f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.90f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.45f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return darken(BASE, 0.15f);
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.78f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.45f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.82f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.45f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.58f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(darken(BASE, 0.1f), 45);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return darken(BASE, 0.15f);
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 35);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.48f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.50f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.62f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.40f);
        }
    }

    // -------------------------------------------------------------------------
    // DARK — Gris foncé
    // -------------------------------------------------------------------------
    private static final class DarkTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(52, 58, 64);

        @Override
        public Color getTabBarBackground() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabBackground() {
            return new Color(52, 58, 64);
        }

        @Override
        public Color getTabSelectedBackground() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getTabHoverBackground() {
            return new Color(62, 68, 75);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(200, 200, 200);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return Color.WHITE;
        }

        @Override
        public Color getTabIndicatorColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getContentBackground() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getContentBorderColor() {
            return new Color(90, 98, 106);
        }

        @Override
        public Color getRibbonBackground() {
            return new Color(68, 74, 80);
        }

        @Override
        public Color getRibbonBorderColor() {
            return new Color(90, 98, 106);
        }

        @Override
        public Color getGroupGradientStart() {
            return new Color(80, 87, 94);
        }

        @Override
        public Color getGroupGradientEnd() {
            return new Color(65, 72, 78);
        }

        @Override
        public Color getGroupBorderColor() {
            return new Color(100, 108, 115);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getGroupHoverTint() {
            return new Color(255, 255, 255, 20);
        }

        @Override
        public Color getHeaderBackground() {
            return new Color(45, 50, 55);
        }

        @Override
        public Color getHeaderTextColor() {
            return new Color(190, 195, 200);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return new Color(80, 87, 94);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return new Color(190, 195, 200);
        }
    }

    // -------------------------------------------------------------------------
    // OCEAN — Vert teal (#009688)
    // -------------------------------------------------------------------------
    private static final class OceanTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(0, 150, 136);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }

    // -------------------------------------------------------------------------
    // PURPLE — Violet (#9C27B0)
    // -------------------------------------------------------------------------
    private static final class PurpleTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(156, 39, 176);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.15f);
        }
    }

    // -------------------------------------------------------------------------
// LIGHT — Clair neutre (gris très clairs et blanc)
// -------------------------------------------------------------------------
    private static final class LightTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(173, 181, 189); // Gris neutre

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return Color.WHITE;
        }

        @Override
        public Color getTabHoverBackground() {
            return new Color(241, 243, 245);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabIndicatorColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getContentBackground() {
            return Color.WHITE;
        }

        @Override
        public Color getContentBorderColor() {
            return new Color(222, 226, 230);
        }

        @Override
        public Color getRibbonBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getRibbonBorderColor() {
            return new Color(222, 226, 230);
        }

        @Override
        public Color getGroupGradientStart() {
            return new Color(255, 255, 255);
        }

        @Override
        public Color getGroupGradientEnd() {
            return new Color(241, 243, 245);
        }

        @Override
        public Color getGroupBorderColor() {
            return new Color(222, 226, 230);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 20);
        }

        @Override
        public Color getHeaderBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getHeaderTextColor() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return new Color(241, 243, 245);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return new Color(108, 117, 125);
        }
    }

}
