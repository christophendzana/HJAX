package rubban;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * @author FIDELE
 * @version 1.0
 */
public class HRibbonTabPanel extends JComponent {

    // =========================================================================
    // CONSTANTES
    // =========================================================================
    /**
     * Espacement horizontal entre les composants dans l'ActionsPanel.
     */
    private static final int ACTIONS_GAP = 4;

    /**
     * Marge interne gauche/droite de l'ActionsPanel.
     */
    private static final int ACTIONS_PADDING = 6;

    /**
     * Le TabbedPane qui gère les onglets.
     */
    private HRibbonTabbedPane tabbedPane;

    /**
     * Panneau transparent qui accueille les composants custom. Positionné à
     * droite du tabbedPane sur la même ligne.
     */
    private final JPanel actionsPanel;

    /**
     * Liste ordonnée des composants ajoutés à l'ActionsPanel. Utilisée pour
     * calculer la largeur automatique.
     */
    private final List<Component> actionComponents = new ArrayList<>();

    /**
     * Référence vers HRibbonTabs parent.
     */
    private HRibbonTabs ribbonTabs;

    /**
     * Largeur manuelle de l'ActionsPanel en pixels. -1 = mode auto (calculé
     * depuis les preferredSize des composants).
     */
    private int manualActionsWidth = -1;

    private int tabBarHeight = 35;

    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    /**
     * Crée un HRibbonTabPanel avec un HRibbonTabbedPane interne. Le layout est
     * géré manuellement via doLayout().
     */
    public HRibbonTabPanel() {
        super();
        setLayout(null); // Layout manuel -> doLayout() gère tout

        // TabbedPane interne 
        this.tabbedPane = new HRibbonTabbedPane();
        add(this.tabbedPane);

        // --- ActionsPanel ---
        // Fond transparent pour se fondre visuellement avec le tabbedPane
        this.actionsPanel = new JPanel();
        this.actionsPanel.setLayout(null); // Layout manuel pour positionner chaque composant
        this.actionsPanel.setOpaque(true);
        this.actionsPanel.setBackground(Color.RED);
        add(this.actionsPanel);

    }

    // =========================================================================
    // LAYOUT PERSONNALISÉ
    // =========================================================================
    /**
     * Calcule et applique les bounds de HRibbonTabbedPane et ActionsPanel.
     *
     * Formule : largeur tabbedPane = totalWidth - actionsWidth largeur
     * actionsPanel = calculée automatiquement ou fixée manuellement
     */
    @Override
    public void doLayout() {
        int totalWidth = getWidth();
        int totalHeight = getHeight();

        // Calculer la largeur effective de l'ActionsPanel
        int actionsWidth = computeActionsWidth();

        // HRibbonTabbedPane prend tout l'espace à gauche de l'ActionsPanel
        int tabbedWidth = totalWidth - actionsWidth;

        System.out.println("HRibbonTabPanel.doLayout | totalWidth=" + totalWidth
                + " totalHeight=" + totalHeight
                + " actionsWidth=" + actionsWidth
                + " tabbedWidth=" + tabbedWidth
                + " actionComponents.size=" + actionComponents.size());

        tabbedPane.setBounds(0, 0, tabbedWidth, totalHeight);

        // ActionsPanel collé à droite, même hauteur
        actionsPanel.setBounds(tabbedWidth, 0, actionsWidth, totalHeight);

        // Positionner chaque composant dans l'ActionsPanel, centré verticalement
        layoutActionComponents();
        System.out.println("HRibbonTabPanel.actionsPanel.bounds=" + actionsPanel.getBounds());
        
        System.out.println("HRibbonTabPanel.bounds=" + getBounds() 
        + " parent=" + (getParent() != null ? getParent().getClass().getSimpleName() : "null")
        + " parentSize=" + (getParent() != null ? getParent().getSize() : "null"));

    }

    /**
     * Positionne les composants à l'intérieur de l'ActionsPanel. Chaque
     * composant est centré verticalement et espacé de ACTIONS_GAP.
     *
     * @param panelWidth largeur disponible dans l'ActionsPanel
     * @param panelHeight hauteur totale de HRibbonTabPanel
     */
    private void layoutActionComponents() {
        int x = ACTIONS_PADDING;

        for (Component comp : actionComponents) {
            Dimension pref = comp.getPreferredSize();
            int compWidth = pref.width;
            int compHeight = pref.height;

            // Centrage vertical par rapport à la hauteur totale du panneau
            int y = (tabBarHeight - compHeight) / 2;

            comp.setBounds(x, y, compWidth, compHeight);

            System.out.println("layoutActionComponents | comp=" + comp.getClass().getSimpleName()
                    + " bounds=" + comp.getBounds()
                    + " actionsPanel.size=" + actionsPanel.getSize());

            x += compWidth + ACTIONS_GAP;
        }
    }

    /**
     * Calcule la largeur effective de l'ActionsPanel.
     *
     * Mode manuel : retourne manualActionsWidth. Mode auto : somme des
     * preferredWidth + gaps + padding. Aucun composant : retourne 0 pour que le
     * tabbedPane prenne toute la place.
     *
     * @return largeur en pixels
     */
    private int computeActionsWidth() {
        // Mode manuel — largeur fixée par l'utilisateur
        if (manualActionsWidth >= 0) {
            return manualActionsWidth;
        }

        // Aucun composant — l'ActionsPanel n'occupe aucun espace
        if (actionComponents.isEmpty()) {
            return 0;
        }

        // Mode auto — on somme les largeurs préférées + gaps + padding x2
        int total = ACTIONS_PADDING * 2;
        for (Component comp : actionComponents) {
            total += comp.getPreferredSize().width + ACTIONS_GAP;
        }
        // Retirer le dernier gap superflu
        total -= ACTIONS_GAP;
        return total;
    }

    public void setTabBarHeight(int height) {
    this.tabBarHeight = height;
}

    // =========================================================================
    // API PUBLIQUE — GESTION DES COMPOSANTS CUSTOM
    // =========================================================================
    /**
     * Ajoute un composant Swing dans la zone de droite (ActionsPanel). La
     * largeur de l'ActionsPanel est recalculée automatiquement si on est en
     * mode auto.
     *
     * @param comp le composant à ajouter (bouton, champ texte, etc.)
     */
    public void addComponent(Component comp) {
        if (comp == null) {
            return;
        }
        actionComponents.add(comp);
        actionsPanel.add(comp);

        System.out.println("HRibbonTabPanel.addComponent | actionComponents.size="
                + actionComponents.size()
                + " comp=" + comp.getClass().getSimpleName()
                + " preferredSize=" + comp.getPreferredSize());

        revalidate();
        repaint();
    }

    /**
     * Retire un composant de l'ActionsPanel. La largeur est recalculée
     * automatiquement si on est en mode auto.
     *
     * @param comp le composant à retirer
     */
    public void removeComponent(Component comp) {
        if (comp == null) {
            return;
        }
        actionComponents.remove(comp);
        actionsPanel.remove(comp);
        revalidate();
        repaint();
    }

    /**
     * Fixe manuellement la largeur de l'ActionsPanel en pixels. Passe en mode
     * manuel — la largeur ne sera plus calculée automatiquement.
     *
     * @param width largeur en pixels (doit être positif)
     */
    public void setComponentsPanelWidth(int width) {
        this.manualActionsWidth = Math.max(0, width);
        revalidate();
        repaint();
    }

    /**
     * Repasse en mode auto pour la largeur de l'ActionsPanel. La largeur sera
     * recalculée depuis les preferredSize des composants.
     */
    public void resetComponentsPanelWidth() {
        this.manualActionsWidth = -1;
        revalidate();
        repaint();
    }

    // =========================================================================
    // DÉLÉGATION VERS HRibbonTabbedPane — paramètres visuels
    // =========================================================================
    /**
     * Transmet le rayon des coins arrondis à HRibbonTabbedPane.
     *
     * @param radius rayon en pixels (0 = coins carrés)
     */
    public void setCornerRadius(int radius) {
        tabbedPane.setCornerRadius(radius);
    }

    /**
     * Active ou désactive les animations de survol sur les onglets.
     *
     * @param enabled true pour activer
     */
    public void setAnimationsEnabled(boolean enabled) {
        tabbedPane.setAnimationsEnabled(enabled);
    }

    /**
     * Transmet la couleur de fond au tabbedPane et à l'ActionsPanel pour
     * garantir la continuité visuelle entre les deux zones.
     *
     * @param bg la couleur de fond
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        // tabbedPane et actionsPanel peuvent être null lors du premier appel
        // depuis le constructeur de JComponent — on vérifie avant d'appeler
        if (tabbedPane != null) {
            tabbedPane.setBackground(bg);
        }
        if (actionsPanel != null) {
            actionsPanel.setBackground(bg);
        }
    }

    /**
     * Transmet la couleur de texte à HRibbonTabbedPane.
     *
     * @param fg la couleur de texte
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (tabbedPane != null) {
            tabbedPane.setForeground(fg);
        }
    }

    /**
     * Transmet la politique de layout des onglets à HRibbonTabbedPane.
     * Typiquement JTabbedPane.SCROLL_TAB_LAYOUT.
     *
     * @param policy la politique de layout
     */
    public void setTabLayoutPolicy(int policy) {
        tabbedPane.setTabLayoutPolicy(policy);
    }

    /**
     * Ajoute un ComponentListener sur HRibbonTabbedPane. Utilisé par
     * HRibbonTabs pour détecter les changements de taille et propager la
     * hauteur aux Ribbon.
     *
     * @param listener le listener à enregistrer
     */
    public void addTabbedPaneComponentListener(java.awt.event.ComponentListener listener) {
        tabbedPane.addComponentListener(listener);
    }

    // =========================================================================
    // DÉLÉGATION VERS HRibbonTabbedPane — gestion des onglets
    // =========================================================================
    /**
     * Délègue l'ajout d'un onglet (titre + composant) à HRibbonTabbedPane. Une
     * icône colorée automatique est attribuée par HRibbonTabbedPane.
     */
    public void addTab(String title, Component component) {
        tabbedPane.addTab(title, component);
    }

    /**
     * Délègue l'ajout d'un onglet avec couleur d'icône explicite.
     */
    public void addTab(String title, Color color, Component component) {
        tabbedPane.addTab(title, color, component);
    }

    /**
     * Délègue l'ajout d'un onglet avec icône explicite.
     */
    public void addTab(String title, Icon icon, Component component) {
        tabbedPane.addTab(title, icon, component);
    }

    /**
     * Délègue la suppression d'un onglet par index.
     */
    public void removeTabAt(int index) {
        tabbedPane.removeTabAt(index);
    }

    /**
     * Délègue la recherche d'un onglet par titre.
     */
    public int indexOfTab(String title) {
        return tabbedPane.indexOfTab(title);
    }

    /**
     * Délègue le nombre d'onglets.
     */
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    /**
     * Délègue la récupération du composant d'un onglet.
     *
     * @param index
     * @return
     */
    public Component getComponentAt(int index) {
        return tabbedPane.getComponentAt(index);
    }

    /**
     * Délègue le remplacement du composant d'un onglet.
     *
     * @param index
     * @param component
     */
    public void setComponentAt(int index, Component component) {
        tabbedPane.setComponentAt(index, component);
    }

    /**
     * Délègue le titre d'un onglet.
     *
     * @param index
     * @return
     */
    public String getTitleAt(int index) {
        return tabbedPane.getTitleAt(index);
    }

    /**
     * Délègue le renommage d'un onglet.
     *
     * @param index
     * @param title
     */
    public void setTitleAt(int index, String title) {
        tabbedPane.setTitleAt(index, title);
    }

    /**
     * Délègue l'activation/désactivation d'un onglet.
     */
    public void setEnabledAt(int index, boolean enabled) {
        tabbedPane.setEnabledAt(index, enabled);
    }

    /**
     * Délègue la lecture de l'état d'activation d'un onglet.
     */
    public boolean isEnabledAt(int index) {
        return tabbedPane.isEnabledAt(index);
    }

    /**
     * Délègue le remplacement de l'icône d'un onglet.
     */
    public void setIconAt(int index, Icon icon) {
        tabbedPane.setIconAt(index, icon);
    }

    /**
     * Délègue l'index sélectionné.
     */
    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Délègue la sélection d'un onglet par index.
     */
    public void setSelectedIndex(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    // =========================================================================
    // THÈMES — interface pour HBasicRibbonTabbedPaneUI (Option A)
    // =========================================================================
    /**
     * Injecte la référence vers HRibbonTabs. Appelé par HRibbonTabs juste après
     * la création de HRibbonTabPanel. Permet à HBasicRibbonTabbedPaneUI de lire
     * les thèmes via ce composant sans avoir à remonter jusqu'à HRibbonTabs
     * lui-même.
     *
     * @param ribbonTabs l'instance parente HRibbonTabs
     */
    public void setRibbonTabs(HRibbonTabs ribbonTabs) {
        this.ribbonTabs = ribbonTabs;
    }

    /**
     * Retourne le thème global depuis HRibbonTabs. Appelé par
     * HBasicRibbonTabbedPaneUI pour peindre le fond de la barre d'onglets et la
     * zone de contenu.
     *
     * @return le thème global, ou null si aucun thème n'est défini
     */
    public HRibbonTabsTheme getTheme() {
        return (ribbonTabs != null) ? ribbonTabs.getTheme() : null;
    }

    /**
     * Retourne le thème effectif d'un onglet donné depuis HRibbonTabs. Appelé
     * par HBasicRibbonTabbedPaneUI pour peindre chaque onglet avec les bonnes
     * couleurs (thème propre → thème global → null).
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return le thème effectif, ou null
     */
    public HRibbonTabsTheme getEffectiveTabTheme(int tabIndex) {
        return (ribbonTabs != null) ? ribbonTabs.getEffectiveTabTheme(tabIndex) : null;
    }

    // =========================================================================
    // ACCÈS INTERNE
    // =========================================================================
    /**
     * Retourne le HRibbonTabbedPane interne. Utile pour HRibbonTabs qui expose
     * getTabbedPane() dans son API publique.
     *
     * @return le HRibbonTabbedPane
     */
    public HRibbonTabbedPane getTabbedPane() {
        return tabbedPane;
    }
}
