/*
 * HRibbonTabbedPane.java
 *
 * TabbedPane spécialisé pour HRibbonTabs.
 * Toute la logique visuelle est déléguée à HBasicRibbonTabbedPaneUI.
 *
 * RESPONSABILITÉS :
 * - Installer HBasicRibbonTabbedPaneUI comme UI delegate
 * - Gérer le cycle de couleurs automatiques des onglets
 * - Exposer le cornerRadius configurable
 *
 * ARCHITECTURE :
 * HRibbonTabs → HRibbonTabbedPane → HBasicRibbonTabbedPaneUI
 *                                         ↓
 *                                   lit HRibbonTabsTheme
 *                                   depuis HRibbonTabs parent
 */
package rubban;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

/**
 * HRibbonTabbedPane — TabbedPane visuel pour HRibbonTabs.
 *
 * Étend directement JTabbedPane sans dépendance à HTabbedPane.
 * Les couleurs sont entièrement gérées par HBasicRibbonTabbedPaneUI
 * qui remonte jusqu'au HRibbonTabs parent pour lire le thème actif.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HRibbonTabbedPane extends JTabbedPane {

    // =========================================================================
    // CYCLE DE COULEURS AUTOMATIQUES
    // =========================================================================

    /**
     * Palette de couleurs attribuées automatiquement aux onglets.
     * Chaque nouvel onglet sans couleur explicite reçoit la couleur suivante
     * dans ce tableau, en bouclant quand on arrive à la fin.
     */
    private static final Color[] DEFAULT_TAB_COLORS = {
        new Color( 13, 110, 253),   // Bleu primaire
        new Color( 25, 135,  84),   // Vert succès
        new Color(220,  53,  69),   // Rouge danger
        new Color(255, 193,   7),   // Jaune avertissement
        new Color( 13, 202, 240),   // Cyan info
        new Color(156,  39, 176),   // Violet
        new Color(  0, 150, 136),   // Vert océan
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

    /**
     * Rayon des coins arrondis des onglets en pixels.
     * Lu par HBasicRibbonTabbedPaneUI lors du rendu.
     * Valeur par défaut : 6px.
     */
    private int cornerRadius = 6;

    /**
     * Active ou désactive les animations de survol.
     * Valeur par défaut : true.
     */
    private boolean animationsEnabled = true;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Constructeur par défaut.
     * Crée un HRibbonTabbedPane avec les onglets positionnés en haut.
     */
    public HRibbonTabbedPane() {
        super(); // JTabbedPane appelle updateUI() ici — on laisse passer
    // Installer notre UI APRÈS que super() a terminé son initialisation
    setUI(new HBasicRibbonTabbedPaneUI());
    setOpaque(false);
    }

    // =========================================================================
    // SURCHARGE DE addTab — GESTION DES ICÔNES COLORÉES
    // =========================================================================

    /**
     * Ajoute un onglet avec un titre et un composant.
     * Une icône colorée est générée automatiquement depuis le cycle de couleurs.
     *
     * @param title     titre affiché sur l'onglet
     * @param component composant affiché quand l'onglet est sélectionné
     */
    @Override
    public void addTab(String title, java.awt.Component component) {
        // Récupérer la prochaine couleur du cycle et générer l'icône
        Color couleurAutomatique = nextColor();
        Icon iconeAutomatique    = createColorIcon(couleurAutomatique);
        super.addTab(title, iconeAutomatique, component);
    }

    /**
     * Ajoute un onglet avec un titre, une couleur explicite et un composant.
     * L'icône ronde est générée depuis la couleur fournie.
     *
     * @param title     titre affiché sur l'onglet
     * @param color     couleur de l'icône ronde de l'onglet
     * @param component composant affiché quand l'onglet est sélectionné
     */
    public void addTab(String title, Color color, java.awt.Component component) {
        Icon icone = createColorIcon(color);
        super.addTab(title, icone, component);
    }

    /**
     * Ajoute un onglet avec une icône fournie explicitement.
     * Le cycle de couleurs automatique n'est PAS avancé dans ce cas.
     *
     * @param title     titre de l'onglet
     * @param icon      icône fournie explicitement
     * @param component composant du contenu
     */
    @Override
    public void addTab(String title, Icon icon, java.awt.Component component) {
        super.addTab(title, icon, component);
    }

    /**
     * Ajoute un onglet avec icône et tooltip.
     *
     * @param title     titre de l'onglet
     * @param icon      icône fournie explicitement
     * @param component composant du contenu
     * @param tip       texte du tooltip
     */
    @Override
    public void addTab(String title, Icon icon, java.awt.Component component, String tip) {
        super.addTab(title, icon, component, tip);
    }

    // =========================================================================
    // MÉTHODE UTILITAIRE — GÉNÉRATION D'ICÔNE COLORÉE
    // =========================================================================

    /**
     * Crée une icône ronde de 10x10 pixels remplie avec la couleur donnée.
     *
     * @param color couleur de remplissage du cercle
     * @return une icône Swing de 10x10 pixels
     */
    public static Icon createColorIcon(Color color) {
        return new Icon() {

            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(color);
                g2.fillOval(x, y, 10, 10);
                g2.dispose();
            }

            @Override public int getIconWidth()  { return 10; }
            @Override public int getIconHeight() { return 10; }
        };
    }

    // =========================================================================
    // CYCLE DE COULEURS INTERNE
    // =========================================================================

    /**
     * Retourne la prochaine couleur du cycle et avance l'index.
     * Quand toutes les couleurs ont été utilisées, on repart du début.
     *
     * @return la prochaine couleur disponible dans DEFAULT_TAB_COLORS
     */
    private Color nextColor() {
        Color couleur    = DEFAULT_TAB_COLORS[colorCycleIndex];
        colorCycleIndex  = (colorCycleIndex + 1) % DEFAULT_TAB_COLORS.length;
        return couleur;
    }

    // =========================================================================
    // MISE À JOUR UI
    // =========================================================================

    /**
     * Appelé par Swing lors d'un changement de Look and Feel.
     * On force notre UI delegate pour conserver notre rendu personnalisé.
     */
    @Override
public void updateUI() {
    // Éviter d'installer notre UI avant que le composant soit
    // complètement initialisé par le constructeur de JTabbedPane
    if (getUI() instanceof HBasicRibbonTabbedPaneUI) {
        // Notre UI est déjà installée — laisser Swing faire son updateUI normal
        // puis réinstaller la nôtre pour conserver notre rendu
        super.updateUI();
        setUI(new HBasicRibbonTabbedPaneUI());
    } else {
        // Premier appel depuis le constructeur de JTabbedPane — laisser passer
        super.updateUI();
    }
    setOpaque(false);
}

    // =========================================================================
    // GETTERS / SETTERS
    // =========================================================================

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
     * Propagé automatiquement à HBasicRibbonTabbedPaneUI au prochain repaint.
     *
     * @param radius rayon en pixels (0 = coins carrés)
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = Math.max(0, radius);
        repaint();
    }

    /**
     * Indique si les animations de survol sont activées.
     *
     * @return true si les animations sont actives
     */
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    /**
     * Active ou désactive les animations de survol sur les onglets.
     *
     * @param enabled true pour activer, false pour désactiver
     */
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
}