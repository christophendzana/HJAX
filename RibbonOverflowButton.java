/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban;

import hcomponents.HButton;
import hcomponents.HPopupMenu;
import hcomponents.HScrollPane;
import hcomponents.HSeparator;
import hcomponents.vues.HMenuStyle;
import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HScrollBarStyle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bouton de débordement pour ruban (Ribbon Overflow Button).
 * 
 * Quand le ruban manque
 * d'espace, les composants sont regroupés dans ce bouton et accessibles via un menu.
 * 
 * Ce n'est pas un simple bouton avec un JPopupMenu basique.
 * C'est l'intégration réfléchie de trois composants de la bibliothèque HComponents :
 * 
 * - HButton : le bouton lui-même, avec ses styles, bordures et ombres
 * - HPopupMenu : le conteneur popup, avec ses coins arrondis et animations
 * - HScrollPane : le défilement optionnel, avec ses scrollbars stylisées
 * 
 * Le défi : faire dialoguer trois systèmes de style indépendants
 * (HButtonStyle, HMenuStyle, HScrollBarStyle) sans imposer de contraintes au développeur.
 * 
 * La solution : 
 * - Par défaut, on synchronise le style du popup sur celui du bouton
 * - Mais on peut tout à fait les découpler via les méthodes dédiées
 * - Chaque composant interne garde ses propres réglages si on les modifie explicitement
 * 
 * @author FIDELE
 * @version 1.0
 * @see HButton
 * @see HPopupMenu
 * @see HScrollPane
 */
public class RibbonOverflowButton extends HButton {
    
    // ============================================================
    // CONSTANTES DE CONFIGURATION PAR DÉFAUT
    // ============================================================
    
    /** Largeur minimale du popup. Évite les popups trop étroits. */
    private static final int MIN_POPUP_WIDTH = 200;
    
    /** Hauteur maximale du popup avant apparition du scroll. */
    private static final int MAX_POPUP_HEIGHT = 400;
    
    /** Marge interne entre les composants dans le popup. */
    private static final int DEFAULT_COMPONENT_MARGIN = 4;
    
    /** Texte par défaut du bouton : un chevron vers le bas (▼). */
    private static final String DEFAULT_TEXT = "▼";
    
    /** Délai avant fermeture automatique (0 = désactivé). */
    private static final int DEFAULT_AUTO_CLOSE_DELAY = 0;
    
    // ============================================================
    // PROPRIÉTÉS FONDAMENTALES
    // ============================================================
    
    /** Menu popup qui contient les composants masqués.
     *  C'est le cœur du composant : tout ce qui est caché est ici. */
    private final HPopupMenu popupMenu;
    
    /** Panneau interne qui accueille les composants avant mise en scroll.
     *  On utilise un GridBagLayout pour un contrôle fin du placement. */
    private JPanel contentPanel;
    
    /** ScrollPane optionnel. Null si pas besoin de défilement. */
    private HScrollPane scrollPane;
    
    /** Liste des composants actuellement hébergés dans le popup.
     *  On garde cette liste en parallèle pour pouvoir itérer facilement. */
    private final List<JComponent> hiddenComponents;
    
    // ============================================================
    // PROPRIÉTÉS DE PERSONNALISATION
    // ============================================================
    
    /** Largeur personnalisée du popup. 0 = calcul automatique. */
    private int customPopupWidth = 0;
    
    /** Hauteur personnalisée du popup. 0 = calcul automatique. */
    private int customPopupHeight = 0;
    
    /** Marge entre les composants. */
    private int componentMargin = DEFAULT_COMPONENT_MARGIN;
    
    /** Activer/désactiver le défilement automatique. */
    private boolean scrollEnabled = true;
    
    /** Délai avant fermeture automatique du popup. */
    private int autoCloseDelay = DEFAULT_AUTO_CLOSE_DELAY;
    
    /** Timer pour la fermeture automatique. */
    private Timer autoCloseTimer;
    
    /** État de synchronisation des styles.
     *  Si true, le style du popup suit celui du bouton. */
    private boolean syncStyleWithButton = true;
    
    /** Style de scrollbar personnalisé. */
    private HScrollBarStyle scrollBarStyle = HScrollBarStyle.PRIMARY;
    
    /** Mapping optionnel : on peut forcer un HMenuStyle spécifique
     *  indépendant de celui du bouton. */
    private HMenuStyle forcedMenuStyle = null;
    
    /**
 * Index du groupe que ce bouton représente.
 * Nécessaire pour reconstruire les proxies à partir du modèle.
 */
private int groupIndex = -1;

/**
 * Version du modèle au moment du dernier rebuild des proxies.
 * Permet de détecter les changements et de recréer si nécessaire.
 */
private long modelVersion = -1;
    
    // ============================================================
    // CONSTRUCTEURS
    // ============================================================
    
    /**
     * Constructeur par défaut.
     * Crée un bouton avec le texte "▼" et un popup vide.
     */
    public RibbonOverflowButton() {
        this(DEFAULT_TEXT);
    }
    
    /**
     * Constructeur avec texte personnalisé.
     * 
     * @param text le texte à afficher sur le bouton
     */
    public RibbonOverflowButton(String text) {
        super(text);
        
        // Initialisation des structures de données
        this.hiddenComponents = new ArrayList<>();
        
        // Création du popup menu HComponents (pas un JPopupMenu basique)
        this.popupMenu = new HPopupMenu();
        
        // Configuration initiale
        initContentPanel();
        initPopupLayout();
        initButtonBehavior();
        initStyleSynchronization();
    }
    
    /**
     * Constructeur avec style HButtonStyle.
     * Utilise la méthode factory de HButton.
     * 
     * @param text le texte du bouton
     * @param style le style HButtonStyle à appliquer
     */
    public RibbonOverflowButton(String text, HButtonStyle style) {
        this(text);
        setButtonStyle(style);
    }
    
    // ============================================================
    // INITIALISATIONS
    // ============================================================
    
    /**
     * Initialise le panneau de contenu avec un GridBagLayout.
     * Le GridBagLayout est idéal ici car il permet :
     * - D'empiler verticalement les composants
     * - De les étirer horizontalement
     * - De gérer finement les marges
     */
    private void initContentPanel() {
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(0, 0, 0, 0)); // Transparent
    }
    
    /**
     * Configure la structure du popup.
     * Par défaut : pas de scroll, contenu directement dans le popup.
     * Le scroll sera ajouté dynamiquement si besoin.
     */
    private void initPopupLayout() {
        // On commence sans scroll
        popupMenu.add(contentPanel);
    }
    
    /**
     * Configure le comportement du bouton.
     * - Action : afficher/masquer le popup
     * - MouseListener : fermeture sur clic extérieur (géré par HPopupMenu)
     */
    private void initButtonBehavior() {
        this.addActionListener(e -> togglePopup());
        
        // On laisse HPopupMenu gérer la fermeture automatique
        // Pas besoin de réinventer la roue
    }
    
    /**
     * Initialise la synchronisation des styles.
     * Par défaut, le popup et le bouton partagent le même style.
     * On utilise HButtonStyle comme référence et on le traduit en HMenuStyle.
     */
    private void initStyleSynchronization() {
        // Écouter les changements de style du bouton
        this.addPropertyChangeListener("buttonStyle", evt -> {
            if (syncStyleWithButton && forcedMenuStyle == null) {
                syncPopupStyleWithButtonStyle();
            }
        });
        
        // Synchronisation initiale
        syncPopupStyleWithButtonStyle();
    }
    
    /**
 * Définit l'index du groupe associé à ce bouton.
 * 
 * @param groupIndex l'index du groupe dans le Ribbon
 */
public void setGroupIndex(int groupIndex) {
    this.groupIndex = groupIndex;
}

/**
 * Retourne l'index du groupe associé à ce bouton.
 * 
 * @return l'index du groupe, ou -1 si non défini
 */
public int getGroupIndex() {
    return groupIndex;
}
    
    // ============================================================
    // GESTION DES COMPOSANTS
    // ============================================================
    
    /**
     * Ajoute un composant Swing au menu de débordement.
     * Le composant sera masqué du ruban et accessible via le popup.
     * 
     * @param component le composant à ajouter (JButton, JComboBox, JPanel, etc.)
     * @return this pour le chaînage (fluent API)
     */
    public RibbonOverflowButton addComponent(JComponent component) {
        if (component == null) {
            return this;
        }
        
        // Ajouter à la liste de suivi
        hiddenComponents.add(component);
        
        // Créer le conteneur avec les marges
        JPanel container = createComponentContainer(component);
        
        // Ajouter au panneau de contenu
        GridBagConstraints gbc = createDefaultConstraints();
        gbc.gridy = contentPanel.getComponentCount(); // Position verticale
        
        contentPanel.add(container, gbc);
        
        // Mettre à jour le scroll si nécessaire
        updateScrollPane();
        
        // Revalidation
        contentPanel.revalidate();
        contentPanel.repaint();
        
        return this;
    }
    
    /**
     * Ajoute un composant avec des contraintes GridBag personnalisées.
     * Pour les cas où on veut un placement spécifique (colspan, alignement, etc.)
     * 
     * @param component le composant à ajouter
     * @param constraints les contraintes GridBagConstraints
     * @return this pour le chaînage
     */
    public RibbonOverflowButton addComponent(JComponent component, GridBagConstraints constraints) {
        if (component == null) {
            return this;
        }
        
        hiddenComponents.add(component);
        
        JPanel container = createComponentContainer(component);
        
        // Cloner les contraintes pour ne pas modifier l'original
        GridBagConstraints gbc = (GridBagConstraints) constraints.clone();
        if (gbc.gridy < 0) {
            gbc.gridy = contentPanel.getComponentCount();
        }
        
        contentPanel.add(container, gbc);
        updateScrollPane();
        contentPanel.revalidate();
        contentPanel.repaint();
        
        return this;
    }
    
    /**
     * Ajoute un séparateur horizontal dans le popup.
     * Utilise HSeparator de la bibliothèque HComponents.
     * 
     * @return this pour le chaînage
     */
    public RibbonOverflowButton addSeparator() {
        HSeparator separator = new HSeparator();
        
        // Récupérer le style du menu pour colorer le séparateur
        if (popupMenu.getMenuStyle() != null) {
            separator.setMenuStyle(popupMenu.getMenuStyle());
        }
        
        GridBagConstraints gbc = createDefaultConstraints();
        gbc.gridy = contentPanel.getComponentCount();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0); // Plus d'espace autour du séparateur
        
        contentPanel.add(separator, gbc);
        contentPanel.revalidate();
        
        return this;
    }
    
    /**
     * Supprime tous les composants du menu de débordement.
     * 
     * @return this pour le chaînage
     */
    public RibbonOverflowButton clearComponents() {
        hiddenComponents.clear();
        contentPanel.removeAll();
        
        // Réinitialiser le GridBagLayout
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // Désactiver le scroll si plus rien à afficher
        if (scrollPane != null) {
            popupMenu.remove(scrollPane);
            popupMenu.add(contentPanel);
            scrollPane = null;
        }
        
        return this;
    }
    
    /**
     * Crée un conteneur pour un composant avec les marges appropriées.
     * 
     * @param component le composant à encapsuler
     * @return un JPanel contenant le composant avec marges
     */
    private JPanel createComponentContainer(JComponent component) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(
            componentMargin, componentMargin, componentMargin, componentMargin
        ));
        container.add(component, BorderLayout.CENTER);
        
        return container;
    }
    
    /**
     * Crée des contraintes GridBag par défaut pour l'empilement vertical.
     * 
     * @return des GridBagConstraints configurées
     */
    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Prend toute la ligne
        gbc.fill = GridBagConstraints.HORIZONTAL;     // S'étire horizontalement
        gbc.weightx = 1.0;                           // Prend tout l'espace horizontal
        gbc.insets = new Insets(
            componentMargin / 2, 0, 
            componentMargin / 2, 0
        );
        return gbc;
    }
    
    // ============================================================
    // GESTION DU DÉFILEMENT
    // ============================================================
    
    /**
     * Active ou désactive le défilement automatique.
     * Quand c'est activé, si le contenu dépasse MAX_POPUP_HEIGHT,
     * on encapsule le contentPanel dans un HScrollPane.
     * 
     * @param enabled true pour activer le défilement
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setScrollEnabled(boolean enabled) {
        this.scrollEnabled = enabled;
        updateScrollPane();
        return this;
    }
    
    /**
     * Met à jour la présence du scroll pane en fonction du contenu.
     * Si le contenu est trop grand et que scrollEnabled = true,
     * on remplace le contentPanel direct par un HScrollPane.
     */
    private void updateScrollPane() {
        if (!scrollEnabled || contentPanel.getComponentCount() == 0) {
            // Pas besoin de scroll
            if (scrollPane != null) {
                popupMenu.remove(scrollPane);
                popupMenu.add(contentPanel);
                scrollPane = null;
            }
            return;
        }
        
        // Calculer la hauteur totale approximative du contenu
        int totalHeight = calculateContentHeight();
        
        if (totalHeight > MAX_POPUP_HEIGHT && scrollEnabled) {
            // Besoin de scroll
            if (scrollPane == null) {
                // Créer le scroll pane avec HScrollBar
                scrollPane = new HScrollPane(contentPanel);
                scrollPane.setScrollStyle(scrollBarStyle);
                scrollPane.setShowBorder(false); // Plus propre sans bordure
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                
                // Remplacer dans le popup
                popupMenu.remove(contentPanel);
                popupMenu.add(scrollPane);
            }
        } else {
            // Plus besoin de scroll
            if (scrollPane != null) {
                popupMenu.remove(scrollPane);
                popupMenu.add(contentPanel);
                scrollPane = null;
            }
        }
    }
    
    /**
     * Calcule approximativement la hauteur totale du contenu.
     * Utile pour décider si on active le scroll.
     * 
     * @return la hauteur estimée en pixels
     */
    private int calculateContentHeight() {
        int height = 0;
        for (Component comp : contentPanel.getComponents()) {
            Dimension pref = comp.getPreferredSize();
            height += pref.height + componentMargin * 2;
        }
        return height;
    }
    
    // ============================================================
    // GESTION DE L'AFFICHAGE
    // ============================================================
    
    /**
     * Affiche ou masque le popup.
     */
    private void togglePopup() {
        if (popupMenu.isVisible()) {
            hidePopup();
        } else {
            showPopup();
        }
    }
    
    /**
     * Affiche le popup.
     */
    public void showPopup() {
        if (hiddenComponents.isEmpty()) {
            return; // Rien à afficher
        }
        
         // ============================================================
    // VÉRIFICATION DE COHÉRENCE AVEC LE MODÈLE
    // ============================================================
    Ribbon ribbon = (Ribbon) SwingUtilities.getAncestorOfClass(Ribbon.class, this);
    
    if (ribbon != null && groupIndex != -1) {
        HRibbonModel Rmodel = ribbon.getModel();
        long currentVersion = Rmodel.getVersion();
        
        // Si le modèle a changé depuis notre dernier rebuild → on recrée tout
        if (currentVersion != this.modelVersion) {
            rebuildFromModel(ribbon, Rmodel);
        }
    }
        
        // Afficher le popup
        popupMenu.show(this, 0, this.getHeight());
        
        // Démarrer le timer de fermeture automatique si configuré
        startAutoCloseTimer();
    }
    
    /**
 * Reconstruit TOUS les proxies du popup à partir du modèle.
 * 
 * Cette méthode est appelée automatiquement si le modèle a changé
 * depuis la dernière ouverture du popup.
 * 
 * @param ribbon le Ribbon parent
 * @param model le modèle de données actuel
 */
private void rebuildFromModel(Ribbon ribbon, HRibbonModel model) {
    // 1. Vider complètement le popup
    clearComponents();
    
    // 2. Récupérer la factory de proxies
    OverflowProxyFactory proxyFactory = ribbon.getOverflowProxyFactory();
    GroupRenderer groupRenderer = ribbon.getGroupRenderer();
    
    if (proxyFactory == null || groupRenderer == null || groupIndex == -1) {
        return;
    }
    
    // 3. Recréer tous les proxies à partir du modèle
    int valueCount = model.getValueCount(groupIndex);
    
    for (int i = 0; i < valueCount; i++) {
        Object value = model.getValueAt(i, groupIndex);
        JComponent proxy = null;
        
        if (value instanceof JComponent) {
            proxy = proxyFactory.createProxy((JComponent) value);
        } else if (value != null) {
            try {
                Component original = groupRenderer.getGroupComponent(
                    ribbon, value, groupIndex, i, false, false
                );
                if (original instanceof JComponent) {
                    proxy = proxyFactory.createProxy((JComponent) original);
                }
            } catch (Exception e) {
                // Ignorer silencieusement
            }
        }
        
        if (proxy != null) {
            addComponent(proxy);
        }
    }
    
    // 4. Mémoriser la version du modèle pour la prochaine fois
    this.modelVersion = model.getVersion();
    
    // 5. Mettre à jour la taille du popup
    updatePopupSize();
}
    
    /**
     * Masque le popup.
     */
    public void hidePopup() {
        popupMenu.setVisible(false);
        stopAutoCloseTimer();
    }
    
    /**
     * Met à jour la taille du popup en fonction du contenu et des réglages.
     */
    private void updatePopupSize() {
        Dimension contentSize;
        
        if (scrollPane != null) {
            // Si on utilise le scroll, on prend la taille du contentPanel
            contentSize = contentPanel.getPreferredSize();
        } else {
            contentSize = contentPanel.getPreferredSize();
        }
        
        int width = customPopupWidth > 0 ? 
                    customPopupWidth : 
                    Math.max(MIN_POPUP_WIDTH, contentSize.width + 20);
        
        int height = customPopupHeight > 0 ? 
                     customPopupHeight : 
                     Math.min(MAX_POPUP_HEIGHT, contentSize.height + 20);
        
        popupMenu.setPreferredSize(new Dimension(width, height));
    }
    
    // ============================================================
    // GESTION DES STYLES
    // ============================================================
    
    /**
     * Synchronise le style du popup avec celui du bouton.
     * Traduit un HButtonStyle en HMenuStyle équivalent.
     */
    private void syncPopupStyleWithButtonStyle() {
        if (forcedMenuStyle != null) {
            // Un style a été forcé, on l'utilise
            popupMenu.setMenuStyle(forcedMenuStyle);
            return;
        }
        
        HButtonStyle btnStyle = getButtonStyle();
        if (btnStyle == null) {
            return;
        }
        
        // Traduction HButtonStyle -> HMenuStyle
        // On fait correspondre les styles par nom
        HMenuStyle menuStyle = convertButtonStyleToMenuStyle(btnStyle);
        popupMenu.setMenuStyle(menuStyle);
    }
    
    /**
     * Convertit un HButtonStyle en HMenuStyle équivalent.
     * 
     * @param btnStyle le style du bouton
     * @return le style de menu correspondant
     */
    private HMenuStyle convertButtonStyleToMenuStyle(HButtonStyle btnStyle) {
        if (btnStyle == null) return HMenuStyle.PRIMARY;
        
        switch (btnStyle) {
            case PRIMARY:   return HMenuStyle.PRIMARY;
            case SECONDARY: return HMenuStyle.SECONDARY;
            case SUCCESS:   return HMenuStyle.SUCCESS;
            case DANGER:    return HMenuStyle.DANGER;
            case WARNING:   return HMenuStyle.WARNING;
            case INFO:      return HMenuStyle.INFO;
            case LIGHT:     return HMenuStyle.LIGHT;
            case DARK:      return HMenuStyle.DARK;
            // FIELD et LINK n'ont pas d'équivalent direct
            // On utilise PRIMARY par défaut
            case FIELD:     
            case LINK:      
            default:        return HMenuStyle.PRIMARY;
        }
    }
    
    /**
     * Force un style de menu spécifique, indépendant du style du bouton.
     * 
     * @param menuStyle le style HMenuStyle à appliquer
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setPopupMenuStyle(HMenuStyle menuStyle) {
        this.forcedMenuStyle = menuStyle;
        this.syncStyleWithButton = false;
        popupMenu.setMenuStyle(menuStyle);
        return this;
    }
    
    /**
     * Réactive la synchronisation automatique des styles.
     * Le popup suivra désormais le style du bouton.
     * 
     * @return this pour le chaînage
     */
    public RibbonOverflowButton syncStyleWithButton() {
        this.syncStyleWithButton = true;
        this.forcedMenuStyle = null;
        syncPopupStyleWithButtonStyle();
        return this;
    }
    
    /**
     * Désactive la synchronisation des styles.
     * Le popup garde son style actuel.
     * 
     * @return this pour le chaînage
     */
    public RibbonOverflowButton unsyncStyleFromButton() {
        this.syncStyleWithButton = false;
        return this;
    }
    
    /**
     * Définit le style des scrollbars du popup.
     * 
     * @param style le style HScrollBarStyle
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setScrollBarStyle(HScrollBarStyle style) {
        this.scrollBarStyle = style;
        if (scrollPane != null) {
            scrollPane.setScrollStyle(style);
        }
        return this;
    }
    
    // ============================================================
    // PERSONNALISATION DU POPUP
    // ============================================================
    
    /**
     * Définit le rayon des coins arrondis du popup.
     * Délégation directe à HPopupMenu.
     * 
     * @param radius le rayon en pixels
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setPopupCornerRadius(int radius) {
        popupMenu.setCornerRadius(radius);
        return this;
    }
    
    /**
     * Active/désactive les animations d'apparition du popup.
     * 
     * @param enabled true pour activer les animations
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setPopupAnimationsEnabled(boolean enabled) {
        popupMenu.setAnimationsEnabled(enabled);
        return this;
    }
    
    /**
     * Définit la largeur personnalisée du popup.
     * 0 = calcul automatique.
     * 
     * @param width la largeur en pixels
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setPopupWidth(int width) {
        this.customPopupWidth = Math.max(0, width);
        return this;
    }
    
    /**
     * Définit la hauteur personnalisée du popup.
     * 0 = calcul automatique.
     * 
     * @param height la hauteur en pixels
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setPopupHeight(int height) {
        this.customPopupHeight = Math.max(0, height);
        return this;
    }
    
    /**
     * Définit la marge entre les composants dans le popup.
     * 
     * @param margin la marge en pixels
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setComponentMargin(int margin) {
        this.componentMargin = Math.max(0, margin);
        return this;
    }
    
    // ============================================================
    // FERMETURE AUTOMATIQUE
    // ============================================================
    
    /**
     * Configure la fermeture automatique du popup après un délai.
     * 
     * @param delayMilliseconds délai en millisecondes (0 = désactivé)
     * @return this pour le chaînage
     */
    public RibbonOverflowButton setAutoCloseDelay(int delayMilliseconds) {
        this.autoCloseDelay = Math.max(0, delayMilliseconds);
        return this;
    }
    
    /**
     * Démarre le timer de fermeture automatique.
     */
    private void startAutoCloseTimer() {
        stopAutoCloseTimer();
        
        if (autoCloseDelay > 0) {
            autoCloseTimer = new Timer(autoCloseDelay, e -> hidePopup());
            autoCloseTimer.setRepeats(false);
            autoCloseTimer.start();
        }
    }
    
    /**
     * Arrête le timer de fermeture automatique.
     */
    private void stopAutoCloseTimer() {
        if (autoCloseTimer != null && autoCloseTimer.isRunning()) {
            autoCloseTimer.stop();
        }
    }
    
    // ============================================================
    // GETTERS / SETTERS
    // ============================================================
    
    /**
     * Retourne le popup menu interne.
     * Permet un accès direct pour des personnalisations avancées.
     * 
     * @return le HPopupMenu utilisé
     */
    public HPopupMenu getPopupMenu() {
        return popupMenu;
    }
    
    /**
     * Retourne la liste des composants cachés.
     * 
     * @return copie de la liste des composants
     */
    public List<JComponent> getHiddenComponents() {
        return new ArrayList<>(hiddenComponents);
    }
    
    /**
     * Vérifie si le popup est actuellement visible.
     * 
     * @return true si le popup est affiché
     */
    public boolean isPopupVisible() {
        return popupMenu.isVisible();
    }
    
    /**
     * Retourne le nombre de composants cachés.
     * 
     * @return le nombre de composants
     */
    public int getHiddenComponentCount() {
        return hiddenComponents.size();
    }
    
    /**
     * Vérifie si le popup a des composants.
     * 
     * @return true si au moins un composant est caché
     */
    public boolean hasHiddenComponents() {
        return !hiddenComponents.isEmpty();
    }
    
    // ============================================================
    // MÉTHODES FACTORY
    // ============================================================
    
    /**
     * Méthode factory pour créer un bouton de débordement avec style.
     * 
     * @param text le texte du bouton
     * @param buttonStyle le style HButtonStyle
     * @return une instance configurée
     */
    public static RibbonOverflowButton withStyle(String text, HButtonStyle buttonStyle) {
        return new RibbonOverflowButton(text, buttonStyle);
    }
    
    /**
     * Méthode factory pour créer un bouton de débordement avec styles séparés.
     * 
     * @param text le texte du bouton
     * @param buttonStyle le style du bouton
     * @param menuStyle le style du popup
     * @return une instance configurée
     */
    public static RibbonOverflowButton withStyles(String text, 
                                                  HButtonStyle buttonStyle, 
                                                  HMenuStyle menuStyle) {
        RibbonOverflowButton button = new RibbonOverflowButton(text, buttonStyle);
        button.setPopupMenuStyle(menuStyle);
        return button;
    }
    
    /**
     * Méthode factory pour créer un bouton de débordement avec composants initiaux.
     * 
     * @param text le texte du bouton
     * @param components les composants à ajouter
     * @return une instance configurée
     */
    public static RibbonOverflowButton withComponents(String text, JComponent... components) {
        RibbonOverflowButton button = new RibbonOverflowButton(text);
        for (JComponent comp : components) {
            button.addComponent(comp);
        }
        return button;
    }
}