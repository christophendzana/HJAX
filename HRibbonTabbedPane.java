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
 * HRibbonTabs > HRibbonTabbedPane > HBasicRibbonTabbedPaneUI >
 *                                         
 *lit HRibbonTabsTheme > depuis HRibbonTabs parent
 */
package HRIbbonTabs;

import HRIbbonTabs.model.DefaultRibbonThemeModel;
import HRIbbonTabs.model.RibbonTabEvent;
import HRIbbonTabs.model.RibbonTabListener;
import HRIbbonTabs.model.RibbonThemeModel;
import HRIbbonTabs.view.HBasicRibbonTabbedPaneUI;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.EventListenerList;

/**
 * HRibbonTabbedPane — TabbedPane visuel pour HRibbonTabs.
 *
 * Étend directement JTabbedPane sans dépendance à HTabbedPane. Les couleurs
 * sont entièrement gérées par HBasicRibbonTabbedPaneUI qui remonte jusqu'au
 * HRibbonTabs parent pour lire le thème actif.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HRibbonTabbedPane extends JTabbedPane {

    // =========================================================================
    // CYCLE DE COULEURS AUTOMATIQUES
    // =========================================================================
    /**
     * Palette de couleurs attribuées automatiquement aux onglets. Chaque nouvel
     * onglet sans couleur explicite reçoit la couleur suivante dans ce tableau,
     * en bouclant quand on arrive à la fin.
     */
    private static final Color[] DEFAULT_TAB_COLORS = {
        new Color(13, 110, 253), // Bleu primaire
        new Color(25, 135, 84), // Vert succès
        new Color(220, 53, 69), // Rouge danger
        new Color(255, 193, 7), // Jaune avertissement
        new Color(13, 202, 240), // Cyan info
        new Color(156, 39, 176), // Violet
        new Color(0, 150, 136), // Vert océan
        new Color(108, 117, 125), // Gris secondaire
    };

    /**
     * Index courant dans le cycle de couleurs. Incrémenté à chaque fois qu'une
     * couleur automatique est attribuée.
     */
    private int colorCycleIndex = 0;

    // =========================================================================
    // PROPRIÉTÉS VISUELLES
    // =========================================================================
    /**
     * Rayon des coins arrondis des onglets en pixels. Lu par
     * HBasicRibbonTabbedPaneUI lors du rendu. Valeur par défaut : 6px.
     */
    private int cornerRadius = 6;

    /**
     * Active ou désactive les animations de survol. Valeur par défaut : true.
     */
    private boolean animationsEnabled = true;

    private final EventListenerList tabListeners = new EventListenerList();

    private RibbonThemeModel themeModel = new DefaultRibbonThemeModel();

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    /**
     * Constructeur par défaut. Crée un HRibbonTabbedPane avec les onglets
     * positionnés en haut.
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
     * Ajoute un onglet avec un titre et un composant. Une icône colorée est
     * générée automatiquement depuis le cycle de couleurs.
     *
     * @param title titre affiché sur l'onglet
     * @param component composant affiché quand l'onglet est sélectionné
     */
    @Override
    public void addTab(String title, Component component) {
        Color couleurAutomatique = nextColor();
        Icon iconeAutomatique = createColorIcon(couleurAutomatique);
        appendTab(title, iconeAutomatique, component, null);
    }

    /**
     * Ajoute un onglet avec un titre, une couleur explicite et un composant.
     * L'icône ronde est générée depuis la couleur fournie.
     *
     * @param title titre affiché sur l'onglet
     * @param color couleur de l'icône ronde de l'onglet
     * @param component composant affiché quand l'onglet est sélectionné
     */
    public void addTab(String title, Color color, Component component) {
        Icon icone = createColorIcon(color);
        appendTab(title, icone, component, null);
    }

    /**
     * Ajoute un onglet avec une icône fournie explicitement. Le cycle de
     * couleurs automatique n'est PAS avancé dans ce cas.
     *
     * @param title titre de l'onglet
     * @param icon icône fournie explicitement
     * @param component composant du contenu
     */
    @Override
    public void addTab(String title, Icon icon, Component component) {
        appendTab(title, icon, component, null);
    }

    /**
     * Ajoute un onglet avec icône et tooltip.
     *
     * @param title titre de l'onglet
     * @param icon icône fournie explicitement
     * @param component composant du contenu
     * @param tip texte du tooltip
     */
    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        appendTab(title, icon, component, tip);
    }

    private void appendTab(String title, Icon icon, java.awt.Component component, String tip) {
        if (tip != null) {
            super.addTab(title, icon, component, tip);
        } else {
            super.addTab(title, icon, component);
        }
        fireTabChanged(RibbonTabEvent.Type.ADDED, getTabCount() - 1, title);
    }

    @Override
    public void removeTabAt(int index) {
        String title = (index >= 0 && index < getTabCount()) ? getTitleAt(index) : null;
        super.removeTabAt(index);
        fireTabChanged(RibbonTabEvent.Type.REMOVED, index, title);
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

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    // HRibbonTabbedPane
    public int getTabAreaHeight() {
    return (getUI() instanceof HBasicRibbonTabbedPaneUI)
            ? ((HBasicRibbonTabbedPaneUI) getUI()).getTabAreaHeight()
            : 40;
}
    // =========================================================================
    // CYCLE DE COULEURS INTERNE
    // =========================================================================
    /**
     * Retourne la prochaine couleur du cycle et avance l'index. Quand toutes
     * les couleurs ont été utilisées, on repart du début.
     *
     * @return la prochaine couleur disponible dans DEFAULT_TAB_COLORS
     */
    private Color nextColor() {
        Color couleur = DEFAULT_TAB_COLORS[colorCycleIndex];
        colorCycleIndex = (colorCycleIndex + 1) % DEFAULT_TAB_COLORS.length;
        return couleur;
    }

    // =========================================================================
    // MISE À JOUR UI
    // =========================================================================
    /**
     * Appelé par Swing lors d'un changement de Look and Feel. On force notre UI
     * delegate pour conserver notre rendu personnalisé.
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

    public RibbonThemeModel getThemeModel() {
        return themeModel;
    }

    public void setThemeModel(RibbonThemeModel themeModel) {
        this.themeModel = (themeModel != null) ? themeModel : new DefaultRibbonThemeModel();
        repaint();
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
     * Définit le rayon des coins arrondis des onglets. Propagé automatiquement
     * à HBasicRibbonTabbedPaneUI au prochain repaint.
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

    public void addRibbonTabListener(RibbonTabListener listener) {
        tabListeners.add(RibbonTabListener.class, listener);
    }

    public void removeRibbonTabListener(RibbonTabListener listener) {
        tabListeners.remove(RibbonTabListener.class, listener);
    }

// Notifie tous les RibbonTabListener enregistrés
    private void fireTabChanged(RibbonTabEvent.Type type, int index, String title) {
        RibbonTabEvent event = new RibbonTabEvent(this, type, index, title);
        for (RibbonTabListener l : tabListeners.getListeners(RibbonTabListener.class)) {
            l.tabChanged(event);
        }
    }

}
