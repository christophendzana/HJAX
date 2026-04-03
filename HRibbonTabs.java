/*
 * HRibbonTabs.java

 * THÈME EN CASCADE :
 *   Le thème global s'applique à tous les onglets qui n'ont pas de thème propre.
 *   Un thème défini sur un onglet précis prend priorité sur le thème global.
 *
 * ÉTATS :
 *   EXPANDED   — hauteur normale, Ribbon visible
 *   COLLAPSED  — hauteur réduite à la barre d'onglets seule
 */
package rubban;

import hcomponents.ArrowIcon;
import hcomponents.HButton;
import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HTabbedPaneStyle;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

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

    // État courant — commence toujours en EXPANDED
    private RibbonTabsState currentState = RibbonTabsState.EXPANDED;

    // Hauteur souhaitée en mode EXPANDED — fixée via le constructeur ou setHeight()
    private int totalHeight = DEFAULT_HEIGHT;

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

        // On force un minimum raisonnable pour éviter un ruban trop écrasé
        this.totalHeight = Math.max(TAB_BAR_HEIGHT + 40, height);

        // Pas de LayoutManager — on positionne tout manuellement dans doLayout()
        setLayout(null);

        this.tabbedPane = new HRibbonTabPanel();
        this.tabbedPane.setRibbonTabs(this);
        add(this.tabbedPane);
        this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.tabbedPane.setTabBarHeight(TAB_BAR_HEIGHT);

        // Quand le tabbedPane change de taille, on recalcule la hauteur des Ribbon
        tabbedPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                propagateHeightToRibbons();
            }
        });

        // --- Bouton collapse ---
        this.collapseButton = new HButton(
                new ArrowIcon(iconColor, ArrowIcon.Direction.UP, 0.4f, 5)
        );
        this.collapseButton.setButtonStyle(HButtonStyle.PRIMARY);
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
                    if (!autoCollapseEnabled || isAdjustingState) {
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
                    int threshold = totalHeight + minContentHeight;

                    isAdjustingState = true;
                    try {
                        if (windowHeight < threshold && currentState == RibbonTabsState.EXPANDED) {
                            setState(RibbonTabsState.COLLAPSED);
                        } else if (windowHeight >= threshold && currentState == RibbonTabsState.COLLAPSED) {
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
        if (currentState == RibbonTabsState.COLLAPSED) {
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
        if (currentState == RibbonTabsState.EXPANDED) {
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
        if (newState == null || newState == currentState) {
            return;
        }

        RibbonTabsState oldState = currentState;
        currentState = newState;

        if (newState == RibbonTabsState.COLLAPSED) {
            collapseButton.setIcon(new ArrowIcon(iconColor, ArrowIcon.Direction.DOWN, 0.4f, 5));
            collapseButton.setToolTipText("Étendre le ruban");
            animateTo(collapsedHeight);
        } else {
            collapseButton.setIcon(new ArrowIcon(iconColor, ArrowIcon.Direction.UP, 0.4f, 5));
            collapseButton.setToolTipText("Réduire le ruban");
            animateTo(totalHeight);
        }

        // Les listeners enregistrés via addPropertyChangeListener("ribbonTabsState", ...)
        // seront automatiquement notifiés par ce firePropertyChange
        firePropertyChange("ribbonTabsState", oldState, newState);
    }

    /**
     * Retourne l'état actuel.
     *
     * @return EXPANDED ou COLLAPSED
     */
    public RibbonTabsState getState() {
        return currentState;
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
        this.totalHeight = Math.max(TAB_BAR_HEIGHT + 40, height);
        applyPreferredSize();
        propagateHeightToRibbons();
        revalidate();
        repaint();
    }

    /**
     * Retourne la hauteur configurée pour le mode EXPANDED.
     *
     * @return hauteur en pixels
     */
    public int getConfiguredHeight() {
        return totalHeight;
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
        return Math.max(40, totalHeight - TAB_BAR_HEIGHT - 10);
    }

    /**
     * Fixe la taille préférée du composant. Largeur 0 = le LayoutManager du
     * parent décide de la largeur (BorderLayout.NORTH).
     */
    private void applyPreferredSize() {
        setPreferredSize(new Dimension(0, totalHeight));
        setMinimumSize(new Dimension(0, totalHeight));
        setMaximumSize(new Dimension(Short.MAX_VALUE, totalHeight));
    }

    /**
     * Applique la hauteur correcte à tous les Ribbon déjà enregistrés. Appelée
     * quand le tabbedPane change de taille ou quand totalHeight change.
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
        this.globalTheme = theme;
        propagateThemes();
    }

    /**
     * Retourne le thème global actuel.
     *
     * @return le thème global, ou null si aucun
     */
    public HRibbonTabsTheme getTheme() {
        return globalTheme;
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
        if (tabThemes.containsKey(tabIndex)) {
            return tabThemes.get(tabIndex);
        }
        return globalTheme;
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
        if (theme == null) {
            tabThemes.remove(tabIndex);
        } else {
            tabThemes.put(tabIndex, theme);
        }
        propagateThemes();
    }

    /**
     * Retourne le thème propre d'un onglet, ou null s'il hérite du thème
     * global.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return le thème de l'onglet, ou null
     */
    public HRibbonTabsTheme getTabTheme(int tabIndex) {
        return tabThemes.get(tabIndex);
    }

    /**
     * Retire le thème propre d'un onglet. L'onglet reviendra au thème global
     * après cet appel.
     *
     * @param tabIndex index de l'onglet (0-based)
     */
    public void removeTabTheme(int tabIndex) {
        tabThemes.remove(tabIndex);
        propagateThemes();
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
        this.autoCollapseEnabled = enabled;
    }

    /**
     * Indique si le collapse automatique est activé.
     *
     * @return true si activé
     */
    public boolean isAutoCollapseEnabled() {
        return autoCollapseEnabled;
    }

    /**
     * Définit l'espace minimal à conserver sous le ruban pour le contenu. Cet
     * espace sert de seuil pour le collapse automatique : si windowHeight <
     * totalHeight + minContentHeight → collapse. Valeur par défaut : 150px.
     *
     * @param height espace minimal en pixels
     */
    public void setMinContentHeight(int height) {
        this.minContentHeight = Math.max(0, height);
    }

    /**
     * Retourne l'espace minimal configuré pour le contenu sous le ruban.
     *
     * @return espace minimal en pixels
     */
    public int getMinContentHeight() {
        return minContentHeight;
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
    public void setComponentsPanelWidth(int width) {
        tabbedPane.setComponentsPanelWidth(width);
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
        if (currentState == RibbonTabsState.EXPANDED) {
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
        this.collapsedHeight = Math.max(TAB_BAR_HEIGHT, height);
        // Si on est déjà en COLLAPSED, appliquer immédiatement
        if (currentState == RibbonTabsState.COLLAPSED) {
            animateTo(this.collapsedHeight);
        }
    }

    /**
     * Retourne la hauteur configurée pour le mode COLLAPSED.
     *
     * @return hauteur en pixels
     */
    public int getCollapsedHeight() {
        return collapsedHeight;
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

        tabbedPane.removeTabAt(index);
        tabThemes.remove(index);

        // Réindexer les thèmes — tous les index > index sont décrémentés de 1
        Map<Integer, HRibbonTabsTheme> reindexed = new HashMap<>();
        for (Map.Entry<Integer, HRibbonTabsTheme> entry : tabThemes.entrySet()) {
            int oldIndex = entry.getKey();
            reindexed.put(oldIndex > index ? oldIndex - 1 : oldIndex, entry.getValue());
        }
        tabThemes.clear();
        tabThemes.putAll(reindexed);

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
            System.err.println("HRibbonTabs.addRibbon : onglet introuvable \"" + title + "\"");
            return;
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
        HRibbonTabsTheme effectiveTheme = tabThemes.containsKey(tabIndex)
                ? tabThemes.get(tabIndex)
                : globalTheme;
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

    public void setTabbedPane(HRibbonTabPanel tabPanel) {
        this.tabbedPane = tabPanel;
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
        return new Dimension(width, totalHeight);
    }

}
