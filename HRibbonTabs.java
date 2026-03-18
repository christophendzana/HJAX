/*
 * HRibbonTabs.java
 *
 * Composant combinant un HTabbedPane et des Ribbon.
 * Chaque onglet contient exactement un Ribbon, style Microsoft Word.
 *
 * ARCHITECTURE :
 *
 *   HRibbonTabs (doLayout() custom)
 *       ├── HTabbedPane → occupe toute la surface moins la zone du bouton
 *       └── boutonCollapse → positionné en absolu via setBounds()
 *                            coin bas droite en EXPANDED  (flèche haut)
 *                            coin haut droite en COLLAPSED (flèche bas)
 *
 * Le positionnement du bouton est identique à la mécanique utilisée dans
 * HRibbonLayoutManager pour le bouton collapse du Ribbon.
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
import java.util.Map;
import javax.swing.*;

/**
 * HRibbonTabs — Composant de ruban à onglets style Microsoft Word.
 *
 * Gère à lui seul la réduction/expansion de l'ensemble du ruban. Le Ribbon
 * individuel ne possède plus de bouton de réduction.
 *
 * @author FIDELE
 * @version 4.0
 */
public class HRibbonTabs extends JComponent {

    // =========================================================================
    // CONSTANTES
    // =========================================================================
    /**
     * Hauteur de la barre d'onglets du HTabbedPane en pixels. Utilisée comme
     * hauteur cible en mode COLLAPSED et pour calculer la hauteur disponible
     * pour les Ribbon en mode EXPANDED.
     */
    private static final int TAB_BAR_HEIGHT = 35;

    /**
     * Hauteur totale par défaut du composant en mode EXPANDED.
     */
    private static final int DEFAULT_HEIGHT = 150;

    /**
     * Largeur fixe du bouton de réduction en pixels. Identique à
     * collapseBtnWidth dans HRibbonLayoutManager.
     */
    private static final int BUTTON_WIDTH = 30;

    /**
     * Hauteur fixe du bouton de réduction en pixels. Identique à
     * collapseBtnHeight dans HRibbonLayoutManager.
     */
    private static final int BUTTON_HEIGHT = 30;

    /**
     * Durée de l'animation de transition en millisecondes.
     */
    private static final int ANIMATION_DURATION = 200;

    // =========================================================================
    // COMPOSANTS INTERNES
    // =========================================================================
    /**
     * Le TabbedPane qui contient visuellement tous les onglets. Sa largeur
     * disponible = largeur totale - BOUTON_LARGEUR - marges.
     */
    private final HRibbonTabbedPane tabbedPane;

    /**
     * Le bouton qui déclenche le collapse ou l'expand de HRibbonTabs.
     * Positionné en absolu via setBounds() dans doLayout(). Coin bas droite en
     * EXPANDED, coin haut droite en COLLAPSED.
     */
    private final HButton collapseButton;

    // =========================================================================
    // ÉTAT INTERNE
    // =========================================================================
    /**
     * Les deux états possibles du composant. EXPANDED : hauteur normale,
     * Ribbons visibles. COLLAPSED : hauteur réduite à la barre d'onglets seule.
     */
    public enum RibbonTabsState {
        EXPANDED,
        COLLAPSED
    }

    /**
     * État courant du composant. Initialisé à EXPANDED à la création.
     */
    private RibbonTabsState currentState = RibbonTabsState.EXPANDED;

    /**
     * Hauteur totale souhaitée en mode EXPANDED. Définie par l'utilisateur via
     * le constructeur ou setHeight().
     */
    private int totalHeight = DEFAULT_HEIGHT;

    // =========================================================================
    // AUTO-COLLAPSE
    // =========================================================================
    /**
     * Active ou désactive le collapse automatique basé sur la hauteur de la
     * fenêtre racine. Désactivé par défaut — l'utilisateur doit l'activer
     * explicitement via setAutoCollapseEnabled(true).
     */
    private boolean autoCollapseEnabled = false;

    /**
     * Verrou pour éviter les boucles infinies lors du collapse automatique.
     * Quand setState() est appelé par le listener, il déclenche un revalidate()
     * qui peut re-déclencher componentResized() — ce verrou coupe la boucle.
     */
    private boolean isAdjustingState = false;

    /**
     * Listener installé sur la fenêtre racine (JFrame) pour détecter les
     * redimensionnements. Stocké pour pouvoir le désinstaller dans
     * removeNotify(). On remonte jusqu'au JFrame car c'est lui qui reçoit les
     * événements de redimensionnement — pas les conteneurs intermédiaires comme
     * JPanel.
     */
    private ComponentListener windowResizeListener;

    // =========================================================================
    // ANIMATION
    // =========================================================================
    /**
     * Timer Swing qui cadence l'animation de transition de hauteur. Créé une
     * seule fois et réutilisé pour chaque transition. Identique au
     * collapseAnimator de Ribbon.
     */
    private Timer animator;

    /**
     * Hauteur de départ de l'animation courante.
     */
    private int startHeight;

    /**
     * Hauteur cible de l'animation courante.
     */
    private int targetHeight;

    /**
     * Timestamp de démarrage de l'animation courante.
     */
    private long animationStartTime;

    // =========================================================================
    // COULEUR PAR DÉFAUT DU BOUTON
    // =========================================================================
    /**
     * Couleur utilisée pour l'icône flèche du bouton de réduction.
     */
    private Color iconColor = Color.DARK_GRAY;

    /**
     * Thème global appliqué à tous les onglets qui n'ont pas de thème propre.
     * Null par défaut — aucun thème appliqué, chaque composant garde ses
     * couleurs.
     */
    private HRibbonTabsTheme globalTheme = null;

    /**
     * Thèmes spécifiques par index d'onglet. Si un onglet a un thème ici, il
     * prend priorité sur le thème global. Clé : index de l'onglet. Valeur :
     * thème à appliquer sur cet onglet.
     */
    private final Map<Integer, HRibbonTabsTheme> tabThemes = new java.util.HashMap<>();

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    /**
     * Constructeur par défaut. Hauteur 150px, style PRIMARY.
     */
    public HRibbonTabs() {
        this(DEFAULT_HEIGHT, HTabbedPaneStyle.PRIMARY);
    }

    /**
     * Constructeur avec hauteur personnalisée. Style PRIMARY par défaut.
     *
     * @param height hauteur totale souhaitée en pixels
     */
    public HRibbonTabs(int height) {
        this(height, HTabbedPaneStyle.PRIMARY);
    }

    /**
     * Constructeur principal — tous les autres lui délèguent.
     *
     * @param height hauteur totale souhaitée en pixels
     * @param style style visuel du HTabbedPane
     */
    public HRibbonTabs(int height, HTabbedPaneStyle style) {
        super();

        this.totalHeight = Math.max(TAB_BAR_HEIGHT + 40, height);

        // Pas de LayoutManager — on gère tout dans doLayout()
        setLayout(null);

        // -----------------------------------------------------------------
        // CRÉATION DU TABBEDPANE
        // -----------------------------------------------------------------
        this.tabbedPane = new HRibbonTabbedPane();
        add(this.tabbedPane);

        tabbedPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                propagateHeightToRibbons();
            }
        });

        // -----------------------------------------------------------------
        // CRÉATION DU BOUTON DE RÉDUCTION
        // Flèche vers le haut en mode EXPANDED initial (comme Ribbon)
        // -----------------------------------------------------------------
        this.collapseButton = new HButton(
                new ArrowIcon(iconColor, ArrowIcon.Direction.UP, 0.4f, 5)
        );
        this.collapseButton.setButtonStyle(HButtonStyle.SUCCESS);
        this.collapseButton.setToolTipText("Réduire le ruban");
        this.collapseButton.addActionListener(e -> toggleState());
        add(this.collapseButton);

        // -----------------------------------------------------------------
        // TAILLE PRÉFÉRÉE INITIALE
        // -----------------------------------------------------------------
        applyPreferredSize();
    }

    // =========================================================================
    // CYCLE DE VIE — INSTALLATION ET DÉSINSTALLATION DU LISTENER
    // =========================================================================
    /**
     * Appelée automatiquement par Swing quand HRibbonTabs est ajouté à un
     * conteneur. On remonte jusqu'à la fenêtre racine (JFrame) pour y installer
     * le listener de redimensionnement. On cible le JFrame et non le parent
     * immédiat car c'est lui qui reçoit les événements de resize de l'OS.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        // Créer le listener une seule fois
        if (windowResizeListener == null) {
            windowResizeListener = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    // Ignorer si le collapse automatique est désactivé
                    if (!autoCollapseEnabled) {
                        return;
                    }

                    // Verrou pour éviter la boucle infinie
                    if (isAdjustingState) {
                        return;
                    }

                    // Remonter jusqu'à la fenêtre racine pour lire sa hauteur
                    Container window = getParent();
                    while (window != null && !(window instanceof Window)) {
                        window = window.getParent();
                    }
                    if (window == null) {
                        return;
                    }

                    int windowHeight = window.getHeight();

                    isAdjustingState = true;
                    try {
                        if (windowHeight < totalHeight
                                && currentState == RibbonTabsState.EXPANDED) {
                            // Fenêtre trop petite : réduire le ruban
                            setState(RibbonTabsState.COLLAPSED);

                        } else if (windowHeight >= totalHeight
                                && currentState == RibbonTabsState.COLLAPSED) {
                            // Fenêtre suffisamment grande : étendre le ruban
                            setState(RibbonTabsState.EXPANDED);
                        }
                    } finally {
                        // Déverrouiller dans tous les cas
                        isAdjustingState = false;
                    }
                }
            };
        }

        // Remonter jusqu'au JFrame et y installer le listener
        Container window = getParent();
        while (window != null && !(window instanceof Window)) {
            window = window.getParent();
        }
        if (window != null) {
            window.addComponentListener(windowResizeListener);
        }
    }

    /**
     * Appelée automatiquement par Swing quand HRibbonTabs est retiré de son
     * conteneur. On retire le listener pour éviter les fuites mémoire.
     */
    @Override
    public void removeNotify() {
        // Remonter jusqu'au JFrame pour retirer le listener
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
     * Reproduit exactement la mécanique de HRibbonLayoutManager : - Le bouton
     * est positionné en absolu via setBounds() - Le HTabbedPane reçoit la
     * largeur disponible après soustraction du bouton
     *
     * En mode EXPANDED : bouton au coin bas droite En mode COLLAPSED : bouton
     * au coin haut droite
     */
    @Override
    public void doLayout() {
        int width = getWidth();
        int height = getHeight();
        Insets insets = getInsets();

        int insetTop = insets != null ? insets.top : 0;
        int insetBottom = insets != null ? insets.bottom : 0;
        int insetLeft = insets != null ? insets.left : 0;
        int insetRight = insets != null ? insets.right : 0;

        int posX = width - BUTTON_WIDTH - insetRight;
        int posY;
        if (currentState == RibbonTabsState.COLLAPSED) {
            // Coin haut droite : aligné avec le haut de la barre d'onglets
            posY = insetTop;
        } else {
            // Coin bas droite : aligné avec le bas du composant
            posY = height - BUTTON_HEIGHT - insetBottom;
        }

        //  Positionner le bouton en absolu
        collapseButton.setBounds(posX, posY, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Le HTabbedPane prend tout l'espace restant à gauche du bouton
        int tabbedPaneWidth = posX - insetLeft - 3; // 3px de marge entre tab et bouton
        tabbedPane.setBounds(
                insetLeft,
                insetTop,
                tabbedPaneWidth,
                height - insetTop - insetBottom
        );
    }

    // =========================================================================
    // GESTION DE L'ÉTAT (COLLAPSE / EXPAND)
    // =========================================================================
    /**
     * Bascule l'état entre EXPANDED et COLLAPSED. Appelée par le bouton de
     * réduction.
     */
    private void toggleState() {
        if (currentState == RibbonTabsState.EXPANDED) {
            setState(RibbonTabsState.COLLAPSED);
        } else {
            setState(RibbonTabsState.EXPANDED);
        }
    }

    /**
     * Applique un nouvel état au composant.
     *
     * En mode COLLAPSED : icône flèche bas, animation vers TAB_BAR_HEIGHT En
     * mode EXPANDED : icône flèche haut, animation vers totalHeight
     *
     * @param newState le nouvel état à appliquer
     */
    public void setState(RibbonTabsState newState) {

        if (newState == null || newState == currentState) {
            return;
        }

        RibbonTabsState oldState = currentState;
        currentState = newState;

        if (newState == RibbonTabsState.COLLAPSED) {

            // Flèche vers le bas : indique qu'on peut ré-ouvrir
            collapseButton.setIcon(
                    new ArrowIcon(iconColor, ArrowIcon.Direction.DOWN, 0.4f, 5)
            );
            collapseButton.setToolTipText("Étendre le ruban");

            // Lancer l'animation vers la hauteur réduite
            animateTo(TAB_BAR_HEIGHT);

        } else {

            // Flèche vers le haut : indique qu'on peut réduire
            collapseButton.setIcon(
                    new ArrowIcon(iconColor, ArrowIcon.Direction.UP, 0.4f, 5)
            );
            collapseButton.setToolTipText("Réduire le ruban");

            // Lancer l'animation vers la hauteur totale
            animateTo(totalHeight);
        }

        // Notifier les observateurs du changement d'état
        firePropertyChange("ribbonTabsState", oldState, newState);
    }

    /**
     * Retourne l'état actuel du composant.
     *
     * @return EXPANDED ou COLLAPSED
     */
    public RibbonTabsState getState() {
        return currentState;
    }

    // =========================================================================
    // ANIMATION DE HAUTEUR
    // =========================================================================
    /**
     * Lance une animation de transition vers une nouvelle hauteur cible.
     *
     * Reprend exactement la mécanique de animateHeight() dans Ribbon : on
     * modifie setPreferredSize() progressivement et on appelle revalidate() à
     * chaque tick pour que le parent se réorganise.
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

                int currentHeight = startHeight
                        + (int) ((targetHeight - startHeight) * progress);

                setPreferredSize(new Dimension(getWidth(), currentHeight));
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
     * Définit la hauteur totale du composant en mode EXPANDED.
     *
     * @param height nouvelle hauteur totale en pixels
     */
    public void setHeight(int height) {
        this.totalHeight = Math.max(TAB_BAR_HEIGHT + 40, height);
        applyPreferredSize();
        propagateHeightToRibbons();
        revalidate();
        repaint();
    }

    /**
     * Retourne la hauteur totale configurée pour le mode EXPANDED.
     *
     * @return hauteur totale en pixels
     */
    public int getConfiguredHeight() {
        return totalHeight;
    }

    /**
     * Calcule la hauteur disponible pour les Ribbon. Hauteur Ribbon = hauteur
     * totale - barre onglets - insets contenu (10px)
     */
    private int calculateRibbonHeight() {
        if (tabbedPane.getTabCount() > 0) {
            Component content = tabbedPane.getComponentAt(0);
            if (content != null && content.getY() > 0) {
                // On ajoute 5px pour compenser l'inset bottom de HBasicTabbedPaneUI
                return Math.max(40, tabbedPane.getHeight());
            }
        }
        return Math.max(40, totalHeight - TAB_BAR_HEIGHT - 10);
    }

    /**
     * Applique la taille préférée initiale. Largeur 0 = le LayoutManager du
     * parent décide de la largeur.
     */
    private void applyPreferredSize() {
        setPreferredSize(new Dimension(0, totalHeight));
        setMinimumSize(new Dimension(0, totalHeight));
        setMaximumSize(new Dimension(Short.MAX_VALUE, totalHeight));
    }

    /**
     * Propage la hauteur correcte à tous les Ribbon enregistrés dans les
     * onglets.
     */
    private void propagateHeightToRibbons() {
        int ribbonHeight = calculateRibbonHeight();

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component content = tabbedPane.getComponentAt(i);

            if (content instanceof Ribbon) {
                applyHeightToRibbon((Ribbon) content, ribbonHeight);
            }
        }
    }

    /**
     * Applique la hauteur calculée à un Ribbon spécifique. Short.MAX_VALUE =
     * convention Swing pour "aussi large que possible".
     *
     * @param ribbon le Ribbon à configurer
     * @param ribbonHeight la hauteur à appliquer en pixels
     */
    private void applyHeightToRibbon(Ribbon ribbon, int ribbonHeight) {
        ribbon.setRibbonHeight(ribbonHeight);
        ribbon.setPreferredSize(new Dimension(Short.MAX_VALUE, ribbonHeight));
        ribbon.setMinimumSize(new Dimension(0, ribbonHeight));
        ribbon.setMaximumSize(new Dimension(Short.MAX_VALUE, ribbonHeight));
    }

    /**
     * Applique un thème à un Ribbon spécifique et met à jour les couleurs du
     * bouton collapse si le thème global est concerné.
     *
     * @param ribbon le Ribbon cible
     * @param theme le thème à appliquer
     */
    private void applyThemeToRibbon(Ribbon ribbon, HRibbonTabsTheme theme) {
        if (ribbon == null || theme == null) {
            return;
        }
        // Propager le thème au Ribbon — BasicHRibbonUI le lira au prochain repaint
        ribbon.setTheme(theme);
    }

    /**
     * Recalcule et applique le thème effectif à chaque onglet.
     *
     * LOGIQUE EN CASCADE : - Si l'onglet a un thème propre → appliquer ce thème
     * - Sinon → appliquer le thème global - Si aucun thème global → ne rien
     * faire
     *
     * Appelée après chaque changement de thème global ou de thème d'onglet.
     */
    private void propagateThemes() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component content = tabbedPane.getComponentAt(i);

            if (!(content instanceof Ribbon)) {
                continue;
            }

            Ribbon ribbon = (Ribbon) content;

            // Thème effectif : thème de l'onglet si défini, sinon thème global
            HRibbonTabsTheme effectiveTheme = tabThemes.containsKey(i)
                    ? tabThemes.get(i)
                    : globalTheme;

            // Appliquer uniquement si un thème est disponible
            applyThemeToRibbon(ribbon, effectiveTheme);
        }

        // Mettre à jour la couleur du bouton collapse depuis le thème global
        if (globalTheme != null) {
            setCollapseButtonIconColor(globalTheme.getCollapseButtonIconColor());
        }

        // Mettre à jour le style visuel du tabbedPane depuis le thème global
        if (globalTheme != null) {
            applyThemeToTabbedPane(globalTheme);
        }

        repaint();
    }

    /**
     * Applique les couleurs du thème au HTabbedPane via un HTabbedPaneStyle
     * construit dynamiquement depuis les valeurs du thème.
     *
     * @param theme le thème source
     */
    private void applyThemeToTabbedPane(HRibbonTabsTheme theme) {
        if (theme == null) {
            return;
        }
        // Construire un HTabbedPaneStyle dynamique depuis les couleurs du thème
        // On surcharge setTabbedStyle() avec les couleurs exactes du thème
        tabbedPane.setBackground(theme.getTabBarBackground());
        tabbedPane.setForeground(theme.getTabTextColor());
        tabbedPane.repaint();
    }

    // =========================================================================
    // API PUBLIQUE — AUTO-COLLAPSE
    // =========================================================================
    /**
     * Active ou désactive le collapse automatique basé sur la hauteur de la
     * fenêtre racine.
     *
     * @param enabled true pour activer, false pour désactiver
     */
    public void setAutoCollapseEnabled(boolean enabled) {
        this.autoCollapseEnabled = enabled;
    }

    /**
     * Retourne true si le collapse automatique est activé.
     *
     * @return true si activé
     */
    public boolean isAutoCollapseEnabled() {
        return autoCollapseEnabled;
    }

    // =========================================================================
    // API PUBLIQUE — GESTION DES ONGLETS
    // =========================================================================
    /**
     * Ajoute un onglet vide avec un titre. Icône colorée attribuée
     * automatiquement par HTabbedPane.
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
     * @param color couleur de l'icône ronde de l'onglet
     */
    public void addTab(String title, Color color) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.addTab(title, color, placeholder);
    }

    /**
     * Ajoute un onglet vide avec une icône fournie explicitement.
     *
     * @param title titre affiché sur l'onglet
     * @param icon icône à afficher à côté du titre
     */
    public void addTab(String title, Icon icon) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.addTab(title, icon, placeholder);
    }

    // =========================================================================
    // API PUBLIQUE — ASSOCIATION DES RIBBON
    // =========================================================================
    /**
     * Associe un Ribbon à un onglet identifié par son titre.
     *
     * @param title titre de l'onglet cible
     * @param ribbon le Ribbon à injecter dans cet onglet
     */
    public void addRibbon(String title, Ribbon ribbon) {
        int index = tabbedPane.indexOfTab(title);

        if (index == -1) {
            System.err.println(
                    "HRibbonTabs.addRibbon : aucun onglet avec le titre \"" + title + "\""
            );
            return;
        }

        addRibbon(index, ribbon);
    }

    /**
     * Associe un Ribbon à un onglet identifié par son index.
     *
     * @param tabIndex index de l'onglet cible (0-based)
     * @param ribbon le Ribbon à injecter
     */
    public void addRibbon(int tabIndex, Ribbon ribbon) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            throw new IndexOutOfBoundsException(
                    "HRibbonTabs.addRibbon : index invalide " + tabIndex
                    + " (nombre d'onglets : " + tabbedPane.getTabCount() + ")"
            );
        }

        if (ribbon == null) {
            throw new IllegalArgumentException(
                    "HRibbonTabs.addRibbon : le Ribbon ne peut pas être null"
            );
        }

        applyHeightToRibbon(ribbon, calculateRibbonHeight());
        tabbedPane.setComponentAt(tabIndex, ribbon);
        // Appliquer le thème effectif au Ribbon nouvellement ajouté
        HRibbonTabsTheme effectiveTheme = tabThemes.containsKey(tabIndex)
                ? tabThemes.get(tabIndex)
                : globalTheme;
        applyThemeToRibbon(ribbon, effectiveTheme);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    // =========================================================================
    // API PUBLIQUE — ACCÈS AUX COMPOSANTS
    // =========================================================================
    /**
     * Retourne le Ribbon de l'onglet identifié par son titre, ou null.
     *
     * @param title titre de l'onglet recherché
     * @return le Ribbon, ou null
     */
    public Ribbon getRibbon(String title) {
        int index = tabbedPane.indexOfTab(title);
        if (index == -1) {
            return null;
        }
        return getRibbon(index);
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

        if (content instanceof Ribbon) {
            return (Ribbon) content;
        }

        return null;
    }

    /**
     * Retourne le nombre d'onglets actuellement présents.
     *
     * @return nombre d'onglets
     */
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    /**
     * Retourne le HTabbedPane interne.
     *
     * @return le HTabbedPane interne
     */
    public HRibbonTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Retourne l'index de l'onglet actuellement sélectionné.
     *
     * @return index sélectionné, ou -1
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
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * Permet de personnaliser la couleur de l'icône flèche du bouton.
     *
     * @param color la nouvelle couleur de l'icône
     */
    public void setCollapseButtonIconColor(Color color) {
        this.iconColor = color;

        if (currentState == RibbonTabsState.EXPANDED) {
            collapseButton.setIcon(
                    new ArrowIcon(color, ArrowIcon.Direction.UP, 0.4f, 5)
            );
        } else {
            collapseButton.setIcon(
                    new ArrowIcon(color, ArrowIcon.Direction.DOWN, 0.4f, 5)
            );
        }
    }

    /**
     * Définit le thème global appliqué à tous les onglets. Les onglets ayant un
     * thème propre ne sont pas affectés.
     *
     * @param theme le thème global, ou null pour réinitialiser
     */
    public void setTheme(HRibbonTabsTheme theme) {
        this.globalTheme = theme;
        propagateThemes();
    }

    /**
     * Retourne le thème global actuel.
     *
     * @return le thème global, ou null
     */
    public HRibbonTabsTheme getTheme() {
        return globalTheme;
    }

    /**
     * Définit un thème spécifique pour un onglet donné. Ce thème prend priorité
     * sur le thème global pour cet onglet.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @param theme le thème à appliquer, ou null pour revenir au thème global
     */
    public void setTabTheme(int tabIndex, HRibbonTabsTheme theme) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) {
            return;
        }

        if (theme == null) {
            // Retirer le thème propre — l'onglet héritera du thème global
            tabThemes.remove(tabIndex);
        } else {
            tabThemes.put(tabIndex, theme);
        }

        propagateThemes();
    }

    /**
     * Retourne le thème spécifique d'un onglet, ou null s'il hérite du global.
     *
     * @param tabIndex index de l'onglet (0-based)
     * @return le thème propre de l'onglet, ou null
     */
    public HRibbonTabsTheme getTabTheme(int tabIndex) {
        return tabThemes.get(tabIndex);
    }

    // =========================================================================
    // SURCHARGE DE getPreferredSize
    // =========================================================================
    /**
     * Retourne la taille préférée du composant. La largeur = celle du parent.
     * La hauteur = celle gérée par ce composant.
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
