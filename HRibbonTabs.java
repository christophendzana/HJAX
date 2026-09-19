package HRIbbonTabs;

import HRIbbonTabs.model.DefaultRibbonTabsModel;
import HRIbbonTabs.model.RibbonTabEvent;
import HRIbbonTabs.model.RibbonTabListener;
import HRIbbonTabs.model.RibbonTabsModel;
import HRIbbonTabs.view.HRibbonTabsTheme;
import hcomponents.ArrowIcon;
import hcomponents.HButton;
import hcomponents.vues.HTabbedPaneStyle;
import rubban.Ribbon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

/**
 * HRibbonTabs — Ruban à onglets style Word.
 *
 * Gère la réduction/expansion globale du ruban, le système de thèmes en cascade
 * et toute la navigation entre les onglets.
 *
 * @author FIDELE
 * @version 5.0
 */
public class HRibbonTabs extends JComponent {

    // =========================================================================
    // CONSTANTES
    // =========================================================================
    // Hauteur de la barre d'onglets — utilisée comme hauteur cible en COLLAPSED
    // et pour calculer la hauteur disponible pour les Ribbon en EXPANDED
    private static final int TAB_BAR_HEIGHT = 35;

    // Hauteur par défaut en mode EXPANDED si l'utilisateur ne précise rien
    private static final int DEFAULT_HEIGHT = 150;

    // Dimensions fixes du bouton collapse — identiques à celles de HRibbonLayoutManager
    private static final int BUTTON_WIDTH = 30;
    private static final int BUTTON_HEIGHT = 30;

    // Durée de l'animation collapse/expand en millisecondes
    private static final int ANIMATION_DURATION = 200;

    // Espace minimal sous le ruban pour déclencher le collapse automatique
    // L'utilisateur peut modifier cette valeur via setMinContentHeight()
    private int minContentHeight = 150;

    private HRibbonTabPanel tabbedPane;

    private final RibbonTabsModel tabsModel;

    // Le bouton qui déclenche le collapse/expand
    private HButton collapseButton;

    /**
     * Les deux états du composant. EXPANDED — Ribbon visible, hauteur normale.
     * COLLAPSED — Ribbon masqué, seule la barre d'onglets reste visible.
     */
    public enum RibbonTabsState {
        EXPANDED,
        COLLAPSED
    }

    // Hauteur du ruban en mode COLLAPSED — par défaut égale à la barre d'onglets
    // L'utilisateur peut la modifier via setCollapsedHeight()
    private int collapsedHeight = TAB_BAR_HEIGHT;

    // =========================================================================
    // AUTO-COLLAPSE
    // =========================================================================
    // Quand true, le ruban se réduit automatiquement si la fenêtre devient
    // trop petite pour afficher le ruban + du contenu en dessous
    private boolean autoCollapseEnabled = false;

    // Verrou anti-boucle infinie : setState() → revalidate() → componentResized()
    // → setState() → ... ce flag coupe la chaîne
    private boolean isAdjustingState = false;

    // Listener installé sur la fenêtre racine (JFrame) pour détecter les resizes
    // Stocké pour pouvoir le retirer proprement dans removeNotify()
    private ComponentListener windowResizeListener;

    // =========================================================================
    // ANIMATION
    // =========================================================================
    // Timer qui cadence l'animation collapse/expand — créé une fois, réutilisé
    private Timer animator;

    // Hauteur de départ et hauteur cible de l'animation courante
    private int startHeight;
    private int targetHeight;

    // Timestamp de démarrage de l'animation courante
    private long animationStartTime;

    // =========================================================================
    // THÈMES
    // =========================================================================
    // Thème appliqué à tous les onglets qui n'ont pas de thème propre
    // null = aucun thème global, chaque composant garde ses couleurs par défaut
    private HRibbonTabsTheme globalTheme = null;

    // Thèmes par index d'onglet — prioritaires sur le thème global
    // Si un onglet n'est pas dans cette map, il utilise globalTheme
    private final Map<Integer, HRibbonTabsTheme> tabThemes = new HashMap<>();

    // =========================================================================
    // APPARENCE DU BOUTON COLLAPSE
    // =========================================================================
    // Couleur de la flèche du bouton collapse — modifiable via setCollapseButtonIconColor()
    private Color iconColor = Color.DARK_GRAY;

    // Visibilité du bouton collapse — true par défaut
    private boolean collapseButtonVisible = true;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    /**
     * Constructeur par défaut — hauteur 150px.
     */
    public HRibbonTabs() {
        this(DEFAULT_HEIGHT, HTabbedPaneStyle.PRIMARY);
    }

    /**
     * Constructeur avec hauteur personnalisée.
     *
     * @param height hauteur totale en mode EXPANDED, en pixels
     */
    public HRibbonTabs(int height) {
        this(height, HTabbedPaneStyle.PRIMARY);
    }

    /**
     * Constructeur principal — tous les autres lui délèguent.
     *
     * @param height hauteur totale en mode EXPANDED, en pixels
     * @param style style visuel initial du TabbedPane
     */
    public HRibbonTabs(int height, HTabbedPaneStyle style) {
        super();
        setLayout(null);

        this.tabsModel = new DefaultRibbonTabsModel(height);
        this.tabsModel.addPropertyChangeListener(this::onTabsModelChanged);

        this.tabbedPane = new HRibbonTabPanel();
        add(this.tabbedPane);
        this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.tabbedPane.setTabBarHeight(TAB_BAR_HEIGHT);

        // Rediffuse les événements de structure d'onglets vers les
        // RibbonTabListener externes — même schéma que componentResized ci-dessous
        this.tabbedPane.addRibbonTabListener(this::fireRibbonTabEvent);

        // Rediffuse les changements de thème (icône du bouton, fond de la barre)
        this.tabbedPane.getThemeModel().addChangeListener(e -> syncChromeWithTheme());

        tabbedPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                propagateHeightToRibbons();
            }
        });

        this.collapseButton = new HButton(
                new ArrowIcon(iconColor, ArrowIcon.Direction.UP, 0.4f, 5)
        );
        collapseButton.setBackgroundPainted(false);
        this.collapseButton.setToolTipText("Réduire le ruban");
        this.collapseButton.addActionListener(e -> toggleState());
        add(this.collapseButton);

        applyPreferredSize();
    }

    // =========================================================================
    // CYCLE DE VIE — LISTENER SUR LA FENÊTRE RACINE
    // =========================================================================
    /**
     * Appelée par Swing quand HRibbonTabs est ajouté à un conteneur. On remonte
     * jusqu'au JFrame pour y installer le listener de resize. On cible le
     * JFrame et non le parent immédiat parce que c'est lui qui reçoit les
     * événements de redimensionnement de l'OS.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        if (windowResizeListener == null) {
            windowResizeListener = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (!tabsModel.isAutoCollapseEnabled() || isAdjustingState) {
                        return;
                    }

                    // Remonter jusqu'à la fenêtre pour lire sa hauteur
                    Container window = getParent();
                    while (window != null && !(window instanceof Window)) {
                        window = window.getParent();
                    }
                    if (window == null) {
                        return;
                    }

                    int windowHeight = window.getHeight();
                    // Le seuil = hauteur du ruban + espace minimum pour le contenu
                    int threshold = tabsModel.getExpandedHeight() + tabsModel.getMinContentHeight();

                    isAdjustingState = true;
                    try {
                        if (windowHeight < threshold && tabsModel.getState() == RibbonTabsState.EXPANDED) {
                            setState(RibbonTabsState.COLLAPSED);
                        } else if (windowHeight >= threshold && tabsModel.getState() == RibbonTabsState.COLLAPSED) {
                            setState(RibbonTabsState.EXPANDED);
                        }
                    } finally {
                        isAdjustingState = false;
                    }
                }
            };
        }

        Container window = getParent();
        while (window != null && !(window instanceof Window)) {
            window = window.getParent();
        }
        if (window != null) {
            window.addComponentListener(windowResizeListener);
        }
    }

    /**
     * Appelée par Swing quand HRibbonTabs est retiré de son conteneur. On
     * retire le listener pour éviter les fuites mémoire.
     */
    @Override
    public void removeNotify() {
        Container window = getParent();
        while (window != null && !(window instanceof Window)) {
            window = window.getParent();
        }
        if (window != null && windowResizeListener != null) {
            window.removeComponentListener(windowResizeListener);
        }
        super.removeNotify();
    }

    // =========================================================================
    // LAYOUT PERSONNALISÉ
    // =========================================================================
    /**
     * Positionne les composants internes manuellement.
     *
     * Le tabbedPane prend toute la surface sauf la zone du bouton. Le bouton
     * est positionné en absolu — en bas à droite en EXPANDED, en haut à droite
     * en COLLAPSED, aligné avec la barre d'onglets.
     */
    @Override
    public void doLayout() {
        int width = getWidth();
        int height = getHeight();
        Insets insets = getInsets();

        int top = insets != null ? insets.top : 0;
        int bottom = insets != null ? insets.bottom : 0;
        int left = insets != null ? insets.left : 0;
        int right = insets != null ? insets.right : 0;

        int posX = width - BUTTON_WIDTH - right;

        int posY;
        if (tabsModel.getState() == RibbonTabsState.COLLAPSED) {
            // En COLLAPSED le bouton est aligné avec le haut de la barre d'onglets
            posY = top;
        } else {
            // En EXPANDED le bouton est en bas du composant, aligné avec
            // le bas de la barre d'onglets (d'où la soustraction de TAB_BAR_HEIGHT)
            posY = height - BUTTON_HEIGHT - bottom;
        }

        // Bouton positionné en absolu
        collapseButton.setVisible(collapseButtonVisible);
        collapseButton.setBounds(posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Le tabbedPane prend tout l'espace à gauche du bouton
        // 3px de marge entre le bord droit du tabbedPane et le bouton
        int tabbedWidth = posX - left - 3;
        tabbedPane.setBounds(left, top, tabbedWidth, height - top - bottom);
    }

    // =========================================================================
    // GESTION DE L'ÉTAT COLLAPSE / EXPAND
    // =========================================================================
    /**
     * Bascule entre EXPANDED et COLLAPSED. Appelée par le bouton collapse.
     */
    private void toggleState() {
        if (tabsModel.getState() == RibbonTabsState.EXPANDED) {
            setState(RibbonTabsState.COLLAPSED);
        } else {
            setState(RibbonTabsState.EXPANDED);
        }
    }

    /**
     * Applique un nouvel état au composant. Met à jour l'icône du bouton et
     * lance l'animation de hauteur.
     *
     * @param newState EXPANDED ou COLLAPSED
     */
    public void setState(RibbonTabsState newState) {
        tabsModel.setState(newState);
    }

    public RibbonTabsState getState() {
        return tabsModel.getState();
    }

    // =========================================================================
    // ANIMATION
    // =========================================================================
    /**
     * Lance l'animation de transition vers une nouvelle hauteur cible. Même
     * mécanique que animateHeight() dans Ribbon — on modifie setPreferredSize()
     * progressivement et on appelle revalidate() à chaque tick pour que le
     * parent se réorganise.
     *
     * @param target hauteur finale en pixels
     */
    private void animateTo(int target) {
        this.startHeight = getHeight();
        this.targetHeight = target;
        this.animationStartTime = System.currentTimeMillis();

        if (animator == null) {
            animator = new Timer(10, e -> {
                long elapsed = System.currentTimeMillis() - animationStartTime;
                float progress = Math.min(1f, (float) elapsed / ANIMATION_DURATION);
                int current = startHeight + (int) ((targetHeight - startHeight) * progress);

                setPreferredSize(new Dimension(getWidth(), current));
                revalidate();

                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    setPreferredSize(new Dimension(getWidth(), targetHeight));
                    revalidate();
                }
            });
        }

        animator.restart();
    }

    // =========================================================================
    // GESTION DE LA TAILLE
    // =========================================================================
    /**
     * Modifie la hauteur totale en mode EXPANDED et propage aux Ribbon.
     *
     * @param height nouvelle hauteur en pixels
     */
    public void setHeight(int height) {
        tabsModel.setExpandedHeight(height);
    }

    /**
     * Retourne la hauteur configurée pour le mode EXPANDED.
     *
     * @return hauteur en pixels
     */
    public int getConfiguredHeight() {
        return tabsModel.getExpandedHeight();
    }

    /**
     * Calcule la hauteur disponible pour les Ribbon. On lit la position Y
     * réelle du premier contenu dans le tabbedPane pour obtenir la hauteur
     * exacte de la barre d'onglets (qui peut varier selon le Look and Feel).
     */
    private int calculateRibbonHeight() {
        if (tabbedPane.getTabCount() > 0) {
            Component content = tabbedPane.getComponentAt(0);
            if (content != null && content.getY() > 0) {
                return Math.max(40, tabbedPane.getHeight());
            }
        }
        return Math.max(40, tabsModel.getExpandedHeight() - TAB_BAR_HEIGHT - 10);
    }

    /**
     * Fixe la taille préférée du composant. Largeur 0 = le LayoutManager du
     * parent décide de la largeur (BorderLayout.NORTH).
     */
    private void applyPreferredSize() {
        setPreferredSize(new Dimension(0, tabsModel.getExpandedHeight()));
        setMinimumSize(new Dimension(0, tabsModel.getExpandedHeight()));
        setMaximumSize(new Dimension(Short.MAX_VALUE, tabsModel.getExpandedHeight()));
    }

    /**
     * Applique la hauteur correcte à tous les Ribbon déjà enregistrés. Appelée
     * quand le tabbedPane change de taille ou quand
     * tabsModel.getExpandedHeight() change.
     */
    private void propagateHeightToRibbons() {
        int h = calculateRibbonHeight();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component content = tabbedPane.getComponentAt(i);
            if (content instanceof Ribbon) {
                applyHeightToRibbon((Ribbon) content, h);
            }
        }
    }

    /**
     * Applique une hauteur donnée à un Ribbon spécifique. Short.MAX_VALUE =
     * convention Swing pour "aussi large que possible".
     *
     * @param ribbon le Ribbon à configurer
     * @param h la hauteur à appliquer
     */
    private void applyHeightToRibbon(Ribbon ribbon, int h) {
        ribbon.setRibbonHeight(h);
        ribbon.setPreferredSize(new Dimension(Short.MAX_VALUE, h));
        ribbon.setMinimumSize(new Dimension(0, h));
        ribbon.setMaximumSize(new Dimension(Short.MAX_VALUE, h));
    }

    // =========================================================================
    // API PUBLIQUE — THÈMES
    // =========================================================================
    /**
     * Applique un thème global à tous les onglets. Les onglets qui ont un thème
     * propre ne sont pas affectés.
     *
     * @param theme le thème à appliquer, ou null pour tout réinitialiser
     */
    public void setTheme(HRibbonTabsTheme theme) {
        tabbedPane.getThemeModel().setGlobalTheme(theme);
    }

    /**
     * Retourne le thème global actuel.
     *
     * @return le thème global, ou null si aucun
     */
    public HRibbonTabsTheme getTheme() {
        return tabbedPane.getThemeModel().getGlobalTheme();
    }

    /**
     * Retourne le thème effectif d'un onglet donné. Si l'onglet a un thème
     * propre on le retourne, sinon on retourne le global. Utilisée par
     * HBasicRibbonTabbedPaneUI pour peindre chaque onglet avec les bonnes
     * couleurs.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return le thème effectif, ou null si aucun thème n'est défini
     */
    public HRibbonTabsTheme getEffectiveTabTheme(int tabIndex) {
        return tabbedPane.getThemeModel().getEffectiveTheme(tabIndex);
    }

    /**
     * Applique un thème spécifique à un onglet donné. Ce thème prend priorité
     * sur le thème global pour cet onglet uniquement.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @param theme le thème à appliquer, ou null pour revenir au thème global
     */
    public void setTabTheme(int tabIndex, HRibbonTabsTheme theme) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.getThemeModel().setTabTheme(tabIndex, theme);
    }

    /**
     * Retourne le thème propre d'un onglet, ou null s'il hérite du thème
     * global.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return le thème de l'onglet, ou null
     */
    public HRibbonTabsTheme getTabTheme(int tabIndex) {
        return tabbedPane.getThemeModel().getTabTheme(tabIndex);
    }

    /**
     * Retire le thème propre d'un onglet. L'onglet reviendra au thème global
     * après cet appel.
     *
     * @param tabIndex index de l'onglet (0-based)
     */
    public void removeTabTheme(int tabIndex) {
        tabbedPane.getThemeModel().removeTabTheme(tabIndex);
    }

    /**
     * Propage le thème effectif à chaque onglet. Logique en cascade : thème
     * propre → thème global → aucun thème.
     */
    private void propagateThemes() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component content = tabbedPane.getComponentAt(i);
            if (!(content instanceof Ribbon)) {
                continue;
            }
            // Thème effectif : celui de l'onglet s'il existe, sinon le global
            HRibbonTabsTheme effectiveTheme = tabThemes.containsKey(i)
                    ? tabThemes.get(i)
                    : globalTheme;
            if (effectiveTheme != null) {
                ((Ribbon) content).setTheme(effectiveTheme);
            }
        }

        // Mettre à jour la couleur de l'icône du bouton depuis le thème global
        if (globalTheme != null) {
            setCollapseButtonIconColor(globalTheme.getCollapseButtonIconColor());
            applyThemeToTabbedPane(globalTheme);
        }

        repaint();
    }

    /**
     * Applique les couleurs du thème au TabbedPane.
     *
     * @param theme le thème source
     */
    private void applyThemeToTabbedPane(HRibbonTabsTheme theme) {
        if (theme == null) {
            return;
        }
        tabbedPane.setBackground(theme.getTabBarBackground());
        tabbedPane.setForeground(theme.getTabTextColor());
        tabbedPane.repaint();
    }

    // =========================================================================
    // API PUBLIQUE — AUTO-COLLAPSE
    // =========================================================================
    /**
     * Active ou désactive le collapse automatique basé sur la hauteur de la
     * fenêtre. Quand activé, le ruban se réduit automatiquement si la fenêtre
     * n'a plus assez de place pour afficher le ruban et du contenu en dessous.
     *
     * @param enabled true pour activer
     */
    public void setAutoCollapseEnabled(boolean enabled) {
        tabsModel.setAutoCollapseEnabled(enabled);
    }

    /**
     * Indique si le collapse automatique est activé.
     *
     * @return true si activé
     */
    public boolean isAutoCollapseEnabled() {
        return tabsModel.isAutoCollapseEnabled();
    }

    /**
     * Définit l'espace minimal à conserver sous le ruban pour le contenu. Cet
     * espace sert de seuil pour le collapse automatique : si windowHeight <
     * tabsModel.getExpandedHeight() + minContentHeight → collapse. Valeur par
     * défaut : 150px.
     *
     * @param height espace minimal en pixels
     */
    public void setMinContentHeight(int height) {
        tabsModel.setMinContentHeight(height);
    }

    /**
     * Retourne l'espace minimal configuré pour le contenu sous le ruban.
     *
     * @return espace minimal en pixels
     */
    public int getMinContentHeight() {
        return tabsModel.getMinContentHeight();
    }

    // =========================================================================
    // API PUBLIQUE — APPARENCE
    // =========================================================================
    /**
     * Ajoute un composant Swing dans la zone de droite de la barre d'onglets.
     * La largeur de l'ActionsPanel est recalculée automatiquement.
     *
     * @param comp le composant à ajouter (bouton, champ texte, etc.)
     */
    public void addComponent(Component comp) {
        tabbedPane.addComponent(comp);
    }

    /**
     * Retire un composant de la zone de droite de la barre d'onglets.
     *
     * @param comp le composant à retirer
     */
    public void removeComponent(Component comp) {
        tabbedPane.removeComponent(comp);
    }

    /**
     * Fixe manuellement la largeur de la zone de composants custom. Passe en
     * mode manuel — la largeur ne sera plus calculée automatiquement.
     *
     * @param width largeur en pixels
     */
    public void setActionPanelWidth(int width) {
        tabbedPane.setActionPanelWidth(width);
    }

    /**
     * Repasse en mode auto pour la largeur de la zone de composants custom. La
     * largeur sera recalculée depuis les preferredSize des composants.
     */
    public void resetComponentsPanelWidth() {
        tabbedPane.resetComponentsPanelWidth();
    }

    /**
     * Modifie le rayon des coins arrondis des onglets. Délègue directement à
     * HRibbonTabbedPane.
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
     * Modifie la couleur de l'icône flèche du bouton collapse.
     *
     * @param color la nouvelle couleur
     */
    public void setCollapseButtonIconColor(Color color) {
        this.iconColor = color;
        if (tabsModel.getState() == RibbonTabsState.EXPANDED) {
            collapseButton.setIcon(new ArrowIcon(color, ArrowIcon.Direction.UP, 0.4f, 5));
        } else {
            collapseButton.setIcon(new ArrowIcon(color, ArrowIcon.Direction.DOWN, 0.4f, 5));
        }
    }

    /**
     * Affiche ou masque le bouton collapse. Utile quand on veut un ruban fixe
     * sans possibilité de le réduire.
     *
     * @param visible true pour afficher, false pour masquer
     */
    public void setCollapseButtonVisible(boolean visible) {
        this.collapseButtonVisible = visible;
        collapseButton.setVisible(visible);
        doLayout();
        repaint();
    }

    /**
     * Définit la hauteur du ruban en mode COLLAPSED. Par défaut égale à
     * TAB_BAR_HEIGHT (35px). Permet de laisser plus ou moins d'espace visible
     * quand le ruban est réduit.
     *
     * @param height hauteur en pixels
     */
    public void setCollapsedHeight(int height) {
        tabsModel.setCollapsedHeight(height);
    }

    /**
     * Retourne la hauteur configurée pour le mode COLLAPSED.
     *
     * @return hauteur en pixels
     */
    public int getCollapsedHeight() {
        return tabsModel.getCollapsedHeight();
    }

    // =========================================================================
    // API PUBLIQUE — GESTION DES ONGLETS
    // =========================================================================
    /**
     * Ajoute un onglet vide avec un titre. Une icône colorée est attribuée
     * automatiquement par HRibbonTabbedPane.
     *
     * @param title titre affiché sur l'onglet
     */
    public void addTab(String title) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.addTab(title, placeholder);
    }

    /**
     * Ajoute un onglet vide avec un titre et une couleur d'icône explicite.
     *
     * @param title titre affiché sur l'onglet
     * @param color couleur de l'icône ronde
     */
    public void addTab(String title, Color color) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.addTab(title, color, placeholder);
    }

    /**
     * Ajoute un onglet vide avec un titre et une icône fournie.
     *
     * @param title titre affiché sur l'onglet
     * @param icon icône à afficher
     */
    public void addTab(String title, Icon icon) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.addTab(title, icon, placeholder);
    }

    /**
     * Supprime l'onglet à l'index donné, ainsi que son Ribbon et son thème. Les
     * index des onglets suivants sont décrémentés et les thèmes réindexés.
     *
     * @param index index de l'onglet à supprimer (0-based)
     */
    public void removeTab(int index) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.removeTabAt(index); // déclenche RibbonTabEvent.REMOVED en interne
        tabbedPane.getThemeModel().reindexAfterTabRemoval(index);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    /**
     * Supprime l'onglet identifié par son titre. Si plusieurs onglets ont le
     * même titre, seul le premier est supprimé.
     *
     * @param title titre de l'onglet à supprimer
     */
    public void removeTab(String title) {
        int index = tabbedPane.indexOfTab(title);
        if (index == -1) {
            System.err.println("HRibbonTabs.removeTab : onglet introuvable \"" + title + "\"");
            return;
        }
        removeTab(index);
    }

    /**
     * Renomme l'onglet à l'index donné.
     *
     * @param index index de l'onglet (0-based)
     * @param newTitle nouveau titre
     */
    public void renameTab(int index, String newTitle) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.setTitleAt(index, newTitle != null ? newTitle : "");
        tabbedPane.repaint();
    }

    /**
     * Active ou désactive un onglet. Un onglet désactivé reste visible mais
     * n'est pas cliquable.
     *
     * @param index index de l'onglet (0-based)
     * @param enabled true pour activer, false pour désactiver
     */
    public void setTabEnabled(int index, boolean enabled) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.setEnabledAt(index, enabled);
        tabbedPane.repaint();
    }

    /**
     * Indique si l'onglet à l'index donné est activé.
     *
     * @param index index de l'onglet (0-based)
     * @return true si activé, false si désactivé ou index invalide
     */
    public boolean isTabEnabled(int index) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return false;
        }
        return tabbedPane.isEnabledAt(index);
    }

    /**
     * Retourne le titre de l'onglet à l'index donné.
     *
     * @param index index de l'onglet (0-based)
     * @return le titre, ou null si l'index est invalide
     */
    public String getTabTitle(int index) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return null;
        }
        return tabbedPane.getTitleAt(index);
    }

    /**
     * Retourne l'index du premier onglet dont le titre correspond. Retourne -1
     * si aucun onglet ne correspond.
     *
     * @param title le titre recherché
     * @return index de l'onglet, ou -1
     */
    public int indexOfTab(String title) {
        return tabbedPane.indexOfTab(title);
    }

    /**
     * Remplace l'icône de l'onglet à l'index donné.
     *
     * @param index index de l'onglet (0-based)
     * @param icon nouvelle icône
     */
    public void setTabIcon(int index, Icon icon) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.setIconAt(index, icon);
        tabbedPane.repaint();
    }

    /**
     * Remplace l'icône de l'onglet par une icône colorée générée depuis la
     * couleur donnée.
     *
     * @param index index de l'onglet (0-based)
     * @param color couleur de la nouvelle icône ronde
     */
    public void setTabIcon(int index, Color color) {
        if (index < 0 || index >= tabbedPane.getTabCount()) {
            return;
        }
        tabbedPane.setIconAt(index, HRibbonTabbedPane.createColorIcon(color));
        tabbedPane.repaint();
    }

    // =========================================================================
    // API PUBLIQUE — GESTION DES RIBBON
    // =========================================================================
    /**
     * Associe un Ribbon à l'onglet identifié par son titre.
     *
     * @param title titre de l'onglet cible
     * @param ribbon le Ribbon à injecter
     */
    public void addRibbon(String title, Ribbon ribbon) {
        int index = tabbedPane.indexOfTab(title);
        if (index == -1) {
            throw new IllegalArgumentException("HRibbonTabs.addRibbon : onglet introuvable \"" + title + "\"");
        }
        addRibbon(index, ribbon);
    }

    /**
     * Associe un Ribbon à l'onglet à l'index donné. Applique automatiquement le
     * thème effectif de cet onglet au Ribbon.
     *
     * @param tabIndex index de l'onglet cible (0-based)
     * @param ribbon le Ribbon à injecter
     */
    public void addRibbon(int tabIndex, Ribbon ribbon) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            throw new IndexOutOfBoundsException(
                    "HRibbonTabs.addRibbon : index invalide " + tabIndex
                    + " / " + tabbedPane.getTabCount() + " onglets"
            );
        }
        if (ribbon == null) {
            throw new IllegalArgumentException("HRibbonTabs.addRibbon : ribbon null");
        }

        applyHeightToRibbon(ribbon, calculateRibbonHeight());

        // Appliquer le thème effectif au Ribbon injecté
        HRibbonTabsTheme effectiveTheme = tabbedPane.getThemeModel().getEffectiveTheme(tabIndex);
        if (effectiveTheme != null) {
            ribbon.setTheme(effectiveTheme);
        }

        tabbedPane.setComponentAt(tabIndex, ribbon);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    /**
     * Remplace le Ribbon d'un onglet par un nouveau. L'ancien Ribbon est retiré
     * et le thème effectif est appliqué au nouveau.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @param ribbon le nouveau Ribbon
     */
    public void replaceRibbon(int tabIndex, Ribbon ribbon) {
        // addRibbon gère déjà le remplacement via setComponentAt()
        addRibbon(tabIndex, ribbon);
    }

    /**
     * Supprime le Ribbon d'un onglet et remet un placeholder vide à la place.
     * L'onglet reste présent mais sans contenu.
     *
     * @param tabIndex index de l'onglet (0-based)
     */
    public void removeRibbon(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            return;
        }
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.setComponentAt(tabIndex, placeholder);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    /**
     * Supprime le Ribbon de l'onglet identifié par son titre.
     *
     * @param title titre de l'onglet cible
     */
    public void removeRibbon(String title) {
        int index = tabbedPane.indexOfTab(title);
        if (index == -1) {
            System.err.println("HRibbonTabs.removeRibbon : onglet introuvable \"" + title + "\"");
            return;
        }
        removeRibbon(index);
    }

    /**
     * Indique si l'onglet à l'index donné possède un Ribbon (et non un
     * placeholder).
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return true si un Ribbon est associé à cet onglet
     */
    public boolean hasRibbon(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            return false;
        }
        return tabbedPane.getComponentAt(tabIndex) instanceof Ribbon;
    }

    // Point d'extension : notifie l'ajout/suppression d'un onglet
    public void addRibbonTabListener(RibbonTabListener listener) {
        listenerList.add(RibbonTabListener.class, listener);
    }

    public void removeRibbonTabListener(RibbonTabListener listener) {
        listenerList.remove(RibbonTabListener.class, listener);
    }

    private void fireRibbonTabEvent(RibbonTabEvent e) {
        for (RibbonTabListener l : listenerList.getListeners(RibbonTabListener.class)) {
            l.tabChanged(e);
        }
    }

    // =========================================================================
    // API PUBLIQUE — ACCÈS AUX COMPOSANTS
    // =========================================================================
    /**
     * Retourne le Ribbon de l'onglet identifié par son titre, ou null.
     *
     * @param title titre de l'onglet
     * @return le Ribbon, ou null si l'onglet n'existe pas ou n'a pas de Ribbon
     */
    public Ribbon getRibbon(String title) {
        int index = tabbedPane.indexOfTab(title);
        return index == -1 ? null : getRibbon(index);
    }

    /**
     * Retourne le Ribbon de l'onglet à l'index donné, ou null.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return le Ribbon, ou null
     */
    public Ribbon getRibbon(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            return null;
        }
        Component content = tabbedPane.getComponentAt(tabIndex);
        return (content instanceof Ribbon) ? (Ribbon) content : null;
    }

    /**
     * Retourne le nombre d'onglets présents.
     *
     * @return nombre d'onglets
     */
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    /**
     * Retourne le HRibbonTabPanel interne.
     *
     * @return le HRibbonTabPanel
     */
    public HRibbonTabPanel getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Retourne l'index de l'onglet actuellement sélectionné.
     *
     * @return index sélectionné, ou -1 si aucun
     */
    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Sélectionne l'onglet à l'index donné.
     *
     * @param index index de l'onglet à sélectionner
     */
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * Passe à l'onglet suivant. Si on est sur le dernier onglet, on revient au
     * premier.
     */
    public void selectNext() {
        int count = tabbedPane.getTabCount();
        if (count == 0) {
            return;
        }
        int next = (tabbedPane.getSelectedIndex() + 1) % count;
        tabbedPane.setSelectedIndex(next);
    }

    /**
     * Passe à l'onglet précédent. Si on est sur le premier onglet, on va au
     * dernier.
     */
    public void selectPrevious() {
        int count = tabbedPane.getTabCount();
        if (count == 0) {
            return;
        }
        int prev = (tabbedPane.getSelectedIndex() - 1 + count) % count;
        tabbedPane.setSelectedIndex(prev);
    }

    // =========================================================================
    // SURCHARGE DE getPreferredSize
    // =========================================================================
    /**
     * La largeur est celle du parent (on prend toute la largeur disponible). La
     * hauteur est gérée entièrement par ce composant.
     *
     * @return la dimension préférée
     */
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Container parent = getParent();
        int width = (parent != null) ? parent.getWidth() : 800;
        return new Dimension(width, tabsModel.getExpandedHeight());
    }

    // Réagit aux changements du modèle : déclenche animation/layout selon
// la propriété modifiée
    private void onTabsModelChanged(java.beans.PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (prop.equals("state")) {
            onStateChanged((RibbonTabsState) evt.getOldValue(), (RibbonTabsState) evt.getNewValue());
        } else if (prop.equals("expandedHeight")) {
            applyPreferredSize();
            propagateHeightToRibbons();
            revalidate();
            repaint();
        } else if (prop.equals("collapsedHeight") && tabsModel.getState() == RibbonTabsState.COLLAPSED) {
            animateTo(tabsModel.getCollapsedHeight());
        }
        // minContentHeight / autoCollapseEnabled : lus à la volée par le listener
        // de resize de fenêtre, rien à propager ici
    }

    @Override
    protected void paintComponent(Graphics g) {
        HRibbonTabsTheme theme = tabbedPane.getThemeModel().getGlobalTheme();
        if (theme != null) {
            int tabAreaHeight = tabbedPane.getTabbedPane().getTabAreaHeight();

            g.setColor(theme.getTabBarBackground());
            g.fillRect(0, 0, getWidth(), Math.min(tabAreaHeight, getHeight()));

            g.setColor(theme.getContentBackground());
            g.fillRect(0, tabAreaHeight, getWidth(), getHeight() - tabAreaHeight);
        }
        super.paintComponent(g);
    }

// Applique l'icône, le tooltip et l'animation correspondant au nouvel état
    private void onStateChanged(RibbonTabsState oldState, RibbonTabsState newState) {
        if (newState == RibbonTabsState.COLLAPSED) {
            collapseButton.setIcon(new ArrowIcon(iconColor, ArrowIcon.Direction.DOWN, 0.4f, 5));
            collapseButton.setToolTipText("Étendre le ruban");
            animateTo(tabsModel.getCollapsedHeight());
        } else {
            collapseButton.setIcon(new ArrowIcon(iconColor, ArrowIcon.Direction.UP, 0.4f, 5));
            collapseButton.setToolTipText("Réduire le ruban");
            animateTo(tabsModel.getExpandedHeight());
        }
        firePropertyChange("ribbonTabsState", oldState, newState);
    }

    // Remplace propagateThemes() — pousse le thème effectif vers chaque Ribbon
// (Ribbon ne s'abonne pas lui-même au modèle) et synchronise le chrome
    private void syncChromeWithTheme() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component content = tabbedPane.getComponentAt(i);
            if (content instanceof Ribbon) {
                HRibbonTabsTheme effective = tabbedPane.getThemeModel().getEffectiveTheme(i);
                if (effective != null) {
                    ((Ribbon) content).setTheme(effective);
                }
            }
        }

        HRibbonTabsTheme globalTheme = tabbedPane.getThemeModel().getGlobalTheme();
        if (globalTheme != null) {            
            collapseButton.setBackground(globalTheme.getCollapseButtonBackground());
            collapseButton.setOpaque(false);
            applyThemeToTabbedPane(globalTheme);
        }

        repaint();
    }

}
