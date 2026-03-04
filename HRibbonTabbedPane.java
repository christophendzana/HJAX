/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban;


import hcomponents.vues.HBasicTabbedPaneUI;
import hcomponents.vues.HTabbedPaneStyle;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author FIDELE
 */
public class HRibbonTabbedPane extends JTabbedPane {
    
    
    // =========================================================================
    // CYCLE DE COULEURS PAR DÉFAUT
    // =========================================================================

    /**
     * Palette de couleurs attribuées automatiquement aux onglets.
     * Chaque nouvel onglet sans couleur explicite reçoit la couleur suivante
     * dans ce tableau, en bouclant quand on arrive à la fin.
     */
    private static final Color[] DEFAULT_TAB_COLORS = {
        new Color(13,  110, 253),   // Bleu primaire
        new Color(25,  135,  84),   // Vert succès
        new Color(220,  53,  69),   // Rouge danger
        new Color(255, 193,   7),   // Jaune avertissement
        new Color( 13, 202, 240),   // Cyan info
        new Color(156,  39, 176),   // Violet
        new Color(  0, 150, 136),   // Vert ocean
        new Color(108, 117, 125),   // Gris secondaire
    };

    /**
     * Index courant dans le cycle de couleurs.
     * Incrémenté à chaque fois qu'une couleur automatique est attribuée.
     */
    private int colorCycleIndex = 0;

    // =========================================================================
    // PROPRIÉTÉS VISUELLES
    // =========================================================================

    /** Style visuel appliqué au composant (couleurs, bordures, indicateur). */
    private HTabbedPaneStyle tabbedStyle = HTabbedPaneStyle.PRIMARY;

    /** Rayon des coins arrondis des onglets en pixels. */
    private int cornerRadius = 8;

    /** Active ou désactive les animations (survol, indicateur). */
    private boolean animationsEnabled = true;

    /** Active ou désactive l'effet de survol sur les onglets. */
    private boolean hoverEnabled = true;

    /** Hauteur en pixels de la barre indicatrice sous l'onglet sélectionné. */
    private int indicatorHeight = 3;

    /** Affiche ou cache la barre indicatrice de sélection. */
    private boolean showIndicator = true;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Constructeur par défaut.
     * Crée un HRibbonTabbedPane avec les onglets positionnés en haut (TOP).
     */
    public HRibbonTabbedPane() {
        super();
        setUI(new HBasicTabbedPaneUI());
        setOpaque(false);
    }

    /**
     * Constructeur avec placement des onglets.
     *
     * @param tabPlacement position des onglets : TOP, BOTTOM, LEFT ou RIGHT
     */
    public HRibbonTabbedPane(int tabPlacement) {
        super(tabPlacement);
        setUI(new HBasicTabbedPaneUI());
        setOpaque(false);
    }

    /**
     * Constructeur avec placement et politique de layout.
     *
     * @param tabPlacement    position des onglets
     * @param tabLayoutPolicy politique : WRAP_TAB_LAYOUT ou SCROLL_TAB_LAYOUT
     */
    public HRibbonTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        setUI(new HBasicTabbedPaneUI());
        setOpaque(false);
    }

    // =========================================================================
    // SURCHARGE DE addTab — GESTION DES ICÔNES COLORÉES
    // =========================================================================

    /**
     * Ajoute un onglet avec un titre et un composant.
     * Une icône colorée est générée automatiquement depuis le cycle de couleurs.
     *
     * C'est la méthode de base de JTabbedPane que l'on surcharge pour injecter
     * automatiquement une icône colorée même quand l'appelant n'en fournit pas.
     *
     * @param title     titre affiché sur l'onglet
     * @param component composant affiché quand l'onglet est sélectionné
     */
    @Override
    public void addTab(String title, Component component) {
        // Récupérer la prochaine couleur du cycle et générer l'icône
        Color couleurAutomatique = nextColor();
        Icon iconeAutomatique = createIcon(couleurAutomatique);

        // Déléguer à la méthode parente avec l'icône générée
        super.addTab(title, iconeAutomatique, component);
    }

    /**
     * Ajoute un onglet avec un titre, une couleur explicite et un composant.
     * L'icône ronde est générée depuis la couleur fournie.
     *
     * Permet à l'utilisateur de choisir précisément la couleur de l'indicateur
     * sans avoir à créer lui-même l'icône.
     *
     * @param title     titre affiché sur l'onglet
     * @param color     couleur de l'icône ronde de l'onglet
     * @param component composant affiché quand l'onglet est sélectionné
     */
    public void addTab(String title, Color color, Component component) {
        // Générer l'icône depuis la couleur fournie par l'utilisateur
        Icon icone = createIcon(color);

        // Déléguer à la méthode parente standard
        super.addTab(title, icone, component);
    }

    /**
     * Ajoute un onglet avec une icône fournie explicitement.
     * Le cycle de couleurs automatique n'est PAS avancé dans ce cas.
     *
     * @param title     titre de l'onglet
     * @param icon      icône fournie explicitement (peut être null)
     * @param component composant du contenu
     */
    @Override
    public void addTab(String title, Icon icon, Component component) {
        super.addTab(title, icon, component);
    }

    /**
     * Ajoute un onglet avec icône et tooltip.
     * Le cycle de couleurs automatique n'est PAS avancé dans ce cas.
     *
     * @param title     titre de l'onglet
     * @param icon      icône fournie explicitement
     * @param component composant du contenu
     * @param tip       texte du tooltip affiché au survol
     */
    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
    }

    // =========================================================================
    // MÉTHODE UTILITAIRE — GÉNÉRATION D'ICÔNE COLORÉE
    // =========================================================================

    /**
     * Crée une icône ronde de 16x16 pixels remplie avec la couleur donnée.
     *
     * L'icône est créée de manière anonyme (classe interne) et utilise
     * Graphics2D avec anti-crénelage pour un rendu lisse.
     *
     * @param color couleur de remplissage du cercle
     * @return une icône Swing de 16x16 pixels
     */
    public static Icon createIcon(Color color) {
        return new Icon() {

            /**
             * Dessine le cercle coloré avec anti-crénelage.
             */
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                // Créer un contexte graphique local pour ne pas polluer celui du parent
                Graphics2D g2 = (Graphics2D) g.create();

                // Activer l'anti-crénelage pour un cercle aux bords lisses
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Appliquer la couleur et dessiner le cercle plein
                g2.setColor(color);
                g2.fillOval(x, y, 16, 16);

                // Libérer le contexte graphique local
                g2.dispose();
            }

            /** Largeur fixe de l'icône en pixels. */
            @Override
            public int getIconWidth() {
                return 16;
            }

            /** Hauteur fixe de l'icône en pixels. */
            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    // =========================================================================
    // MÉTHODE INTERNE — CYCLE DE COULEURS
    // =========================================================================

    /**
     * Retourne la prochaine couleur du cycle et avance l'index.
     * Quand toutes les couleurs ont été utilisées, on repart du début.
     *
     * @return la prochaine couleur disponible dans DEFAULT_TAB_COLORS
     */
    private Color nextColor() {
        // Lire la couleur à l'index courant
        Color couleur = DEFAULT_TAB_COLORS[colorCycleIndex];

        // Avancer l'index en bouclant sur la taille du tableau
        colorCycleIndex = (colorCycleIndex + 1) % DEFAULT_TAB_COLORS.length;

        return couleur;
    }

    // =========================================================================
    // MISE À JOUR UI
    // =========================================================================

    /**
     * Appelé par Swing lors d'un changement de Look and Feel.
     * On force setOpaque(false) pour conserver notre rendu personnalisé.
     */
    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false);
    }

    // =========================================================================
    // GETTERS / SETTERS DES PROPRIÉTÉS VISUELLES
    // =========================================================================

    /**
     * Retourne le style visuel actuel du composant.
     *
     * @return le HRibbonTabbedPaneStyle appliqué
     */
    public HTabbedPaneStyle getTabbedStyle() {
        return tabbedStyle;
    }

    /**
     * Applique un style visuel au composant.
     * Met à jour les couleurs de fond et de texte, puis redessine.
     *
     * @param style le nouveau style à appliquer
     */
    public void setTabbedStyle(HTabbedPaneStyle style) {
        this.tabbedStyle = style;
        setBackground(style.getBackgroundColor());
        setForeground(style.getTextColor());
        repaint();
    }

    /**
     * Retourne le rayon des coins arrondis des onglets.
     *
     * @return rayon en pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis des onglets.
     *
     * @param radius nouveau rayon en pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Indique si les animations sont activées.
     *
     * @return true si les animations sont actives
     */
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    /**
     * Active ou désactive les animations de survol et d'indicateur.
     *
     * @param enabled true pour activer, false pour désactiver
     */
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    /**
     * Indique si l'effet de survol est activé.
     *
     * @return true si le hover est actif
     */
    public boolean isHoverEnabled() {
        return hoverEnabled;
    }

    /**
     * Active ou désactive l'effet visuel de survol sur les onglets.
     *
     * @param enabled true pour activer, false pour désactiver
     */
    public void setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
    }

    /**
     * Retourne la hauteur de la barre indicatrice de sélection.
     *
     * @return hauteur en pixels
     */
    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    /**
     * Définit la hauteur de la barre indicatrice de sélection.
     *
     * @param height nouvelle hauteur en pixels
     */
    public void setIndicatorHeight(int height) {
        this.indicatorHeight = height;
        repaint();
    }

    /**
     * Indique si la barre indicatrice est visible.
     *
     * @return true si l'indicateur est affiché
     */
    public boolean isShowIndicator() {
        return showIndicator;
    }

    /**
     * Affiche ou cache la barre indicatrice de sélection.
     *
     * @param show true pour afficher, false pour cacher
     */
    public void setShowIndicator(boolean show) {
        this.showIndicator = show;
        repaint();
    }

    // =========================================================================
    // MÉTHODES FACTORY
    // =========================================================================

    /**
     * Crée un HRibbonTabbedPane avec un style prédéfini (onglets en haut par défaut).
     *
     * @param style le style à appliquer
     * @return un nouveau HRibbonTabbedPane configuré avec ce style
     */
    public static HRibbonTabbedPane withStyle(HTabbedPaneStyle style) {
        HRibbonTabbedPane tabbedPane = new HRibbonTabbedPane();
        tabbedPane.setTabbedStyle(style);
        return tabbedPane;
    }

    /**
     * Crée un HRibbonTabbedPane avec un style et un placement d'onglets spécifiques.
     *
     * @param style        le style à appliquer
     * @param tabPlacement position des onglets : TOP, BOTTOM, LEFT ou RIGHT
     * @return un nouveau HRibbonTabbedPane configuré
     */
    public static HRibbonTabbedPane withStyle(HTabbedPaneStyle style, int tabPlacement) {
        HRibbonTabbedPane tabbedPane = new HRibbonTabbedPane(tabPlacement);
        tabbedPane.setTabbedStyle(style);
        return tabbedPane;
    }
    
    
}
