package hsplitpane;

import hcomponents.HScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import hsplitpane.HSplitPane.ZonePosition;
import hsplitpane.HSplitPane.WrapDirection;

/**
 * Représente une zone d'ancrage dans un HSplitPane.
 *
 * Chaque zone occupe une position fixe (NORTH, SOUTH, EAST, WEST ou CENTER)
 * et peut accueillir plusieurs composants enfants positionnés par
 * HSplitWrapLayout.
 *
 * La zone supporte trois modes spéciaux :
 * - Collapsed  : réduite à la taille du header uniquement
 * - FullScreen : agrandie pour occuper tout l'espace du HSplitPane
 * - Floating   : détachée dans une fenêtre HDialog indépendante
 *
 * Structure interne :
 * <pre>
 *   HSplitZone (BorderLayout)
 *   ├── HSplitZoneHeader  [bord selon la position]
 *   └── HScrollPane       [CENTER]
 *         └── contentPanel (JPanel avec HSplitWrapLayout)
 * </pre>
 */
public class HSplitZone extends JPanel {

    // -------------------------------------------------------------------------
    // Identité et position
    // -------------------------------------------------------------------------

    /** Position de cette zone dans le HSplitPane parent. */
    private final ZonePosition position;

    /** Titre affiché dans la barre de contrôle. */
    private String titre;

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------

    /** Taille initiale fournie par la configuration. Null = flexible. */
    private Dimension initialSize;

    /**
     * Taille mémorisée avant un collapse, pour restauration.
     * Utilisée aussi pour la réintégration depuis le mode flottant.
     */
    private Dimension sizeBeforeCollapse;

    // -------------------------------------------------------------------------
    // États
    // -------------------------------------------------------------------------

    /** true si la zone est actuellement réduite. */
    private boolean collapsed;

    /** true si la zone est en mode fullscreen. */
    private boolean fullScreen;

    /** true si la zone est détachée dans une fenêtre flottante. */
    private boolean floating;

    // -------------------------------------------------------------------------
    // Référence au HSplitPane parent
    // Nécessaire pour déléguer les opérations fullscreen qui affectent
    // toutes les zones simultanément.
    // -------------------------------------------------------------------------

    /** Référence au HSplitPane qui contient cette zone. */
    private HSplitPane splitPaneRef;

    // -------------------------------------------------------------------------
    // Composants internes
    // -------------------------------------------------------------------------

    /** Barre de contrôle avec titre et boutons d'action. */
    private HSplitZoneHeader header;

    /** Panneau interne qui reçoit les composants de l'utilisateur. */
    private JPanel contentPanel;

    /** Conteneur de défilement enveloppant le panneau de contenu. */
    private HScrollPane scrollPane;

    /** Fenêtre flottante active, ou null si la zone n'est pas flottante. */
    private HSplitFloatDialog floatDialog;

    // -------------------------------------------------------------------------
    // Animation
    // -------------------------------------------------------------------------

    private static final int ANIMATION_DURATION = 160;
    private static final int STEP_COUNT         = 30;

    private javax.swing.Timer animationTimer;

    /**
     * true pendant une animation — le layout respecte la taille courante
     * de la zone plutôt que de la recalculer depuis ses valeurs stockées.
     */
    private boolean inAnimation = false;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    public HSplitZone(ZonePosition position) {
        this(position, null, null);
    }

    public HSplitZone(ZonePosition position, String titre) {
        this(position, titre, null);
    }

    public HSplitZone(ZonePosition position, String titre, Dimension initialSize) {
        this.position    = position;
        this.titre       = titre;
        this.initialSize = initialSize;
        this.collapsed   = false;
        this.fullScreen  = false;
        this.floating    = false;

        initializeComponents();
        assembleZone();
        connectListeners();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    private void initializeComponents() {
        if (position != ZonePosition.CENTER) {
            header = new HSplitZoneHeader(position, titre);
        }

        contentPanel = new JPanel(new HSplitWrapLayout());
        contentPanel.setOpaque(false);

        scrollPane = new HScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(HScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(HScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private void assembleZone() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(43, 43, 43));

        if (header != null) {
            switch (position) {
                case NORTH: add(header, BorderLayout.SOUTH); break;
                case SOUTH: add(header, BorderLayout.NORTH); break;
                case WEST:  add(header, BorderLayout.EAST);  break;
                case EAST:  add(header, BorderLayout.WEST);  break;
                default:    break;
            }
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Branche les trois boutons du header sur leurs méthodes respectives.
     */
    private void connectListeners() {
        if (header != null) {
            header.addToggleListener(e     -> toggleCollapse());
            header.addFullScreenListener(e -> toggleFullScreen());
            header.addFloatListener(e      -> toggleFloat());
        }
    }

    // =========================================================================
    // Logique collapse / expand
    // =========================================================================

    private void toggleCollapse() {
        if (collapsed) expand(); else collapse();
    }

    /**
     * Réduit la zone à la taille du header.
     * La taille courante est mémorisée pour la restauration ultérieure.
     */
    public void collapse() {
        if (collapsed) return;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        sizeBeforeCollapse = getSize();
        collapsed = true;

        if (header != null) header.updateCollapseState(true);

        animate(sizeBeforeCollapse, getCollapsedSize());
    }

    /**
     * Développe la zone en restaurant sa taille d'avant le collapse.
     */
    public void expand() {
        if (!collapsed) return;

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        collapsed = false;

        if (header != null) header.updateCollapseState(false);

        Dimension targetSize = sizeBeforeCollapse;
        if (targetSize == null || (targetSize.width == 0 && targetSize.height == 0)) {
            targetSize = getPreferredSize();
            if (targetSize.width  <= 0) targetSize.width  = 200;
            if (targetSize.height <= 0) targetSize.height = 150;
        }

        animate(getCollapsedSize(), targetSize);
    }
    
    /**
 * Anime la zone depuis ses bounds actuels vers les bounds cibles.
 * Utilisé par HSplitPane pour la transition d'entrée en fullscreen.
 *
 * @param targetX      position X cible
 * @param targetY      position Y cible
 * @param targetWidth  largeur cible
 * @param targetHeight hauteur cible
 */
public void animerVersBounds(int targetX, int targetY, int targetWidth, int targetHeight) {
    if (animationTimer != null && animationTimer.isRunning()) {
        animationTimer.stop();
    }

    final int startX = getX();
    final int startY = getY();
    final int startW = getWidth();
    final int startH = getHeight();
    final int[] step = {0};

    inAnimation = true;

    animationTimer = new javax.swing.Timer(ANIMATION_DURATION / STEP_COUNT, e -> {
        step[0]++;
        float t = (float) step[0] / STEP_COUNT;

        int cx = (int) (startX + t * (targetX - startX));
        int cy = (int) (startY + t * (targetY - startY));
        int cw = (int) (startW + t * (targetWidth  - startW));
        int ch = (int) (startH + t * (targetHeight - startH));

        setBounds(cx, cy, cw, ch);

        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }

        if (step[0] >= STEP_COUNT) {
            animationTimer.stop();
            inAnimation = false;
            setBounds(targetX, targetY, targetWidth, targetHeight);
            if (getParent() != null) {
                getParent().repaint();
            }
        }
    });

    animationTimer.setRepeats(true);
    animationTimer.start();
}

    // =========================================================================
    // Logique fullscreen
    // =========================================================================

    /**
     * Bascule entre le mode fullscreen et le mode normal.
     * Délègue entièrement au HSplitPane qui coordonne toutes les zones.
     */
    private void toggleFullScreen() {
        if (splitPaneRef == null) return;
        if (fullScreen) splitPaneRef.exitFullScreen();
        else            splitPaneRef.enterFullScreen(position);
    }

    /**
     * Active ou désactive le mode fullscreen sur cette zone.
     * Appelé exclusivement par HSplitPane — ne pas appeler directement.
     *
     * @param fullScreen true pour activer le fullscreen
     */
    void setFullScreenState(boolean fullScreen) {
        this.fullScreen = fullScreen;
        if (header != null) header.updateFullScreenState(fullScreen);
    }

    // =========================================================================
    // Logique float
    // =========================================================================

    /**
     * Bascule entre le mode flottant et le mode intégré.
     */
    private void toggleFloat() {
        if (floating) reintegrateZone(); else floatZone();
    }

    /**
     * Détache la zone dans une fenêtre flottante HDialog.
     *
     * Séquence d'opérations :
     * 1. Retire le scrollPane de la zone pour le transférer dans le dialog
     * 2. Crée le HSplitFloatDialog et y place le scrollPane
     * 3. Collapse la zone dans le HSplitPane (seul le header reste visible)
     * 4. Affiche le dialog
     *
     * Note : en Swing un composant ne peut avoir qu'un seul parent.
     * Le retrait du scrollPane de la zone et son ajout dans le dialog
     * est donc la seule façon de le "déplacer".
     */
    public void floatZone() {
        if (floating) return;

        floating = true;

        if (header != null) header.updateFloatingState(true);

        // Transfert du scrollPane vers le dialog
        remove(scrollPane);
        revalidate();
        repaint();

        // Création du dialog flottant
        floatDialog = new HSplitFloatDialog(titre != null ? titre : "Zone flottante");
        floatDialog.setContenu(scrollPane);

        // Le callback de fermeture déclenche la réintégration
        floatDialog.setOnFermetureCallback(this::reintegrateZone);

        // La zone se réduit dans le HSplitPane — seul le header reste visible
        collapserPourFloat();

        floatDialog.afficher();
    }

    /**
     * Réintègre la zone dans le HSplitPane après fermeture du dialog flottant.
     *
     * Séquence d'opérations :
     * 1. Récupère le scrollPane depuis le dialog et le réintègre dans la zone
     * 2. Expand la zone dans le HSplitPane
     * 3. Ferme le dialog si toujours ouvert
     */
    public void reintegrateZone() {
        if (!floating) return;

        floating = false;

        if (header != null) header.updateFloatingState(false);

        // Récupération du scrollPane et réintégration dans la zone
        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();

        // Expand pour restaurer la zone dans le HSplitPane
        expandDepuisFloat();

        // Fermeture du dialog si toujours visible.
        // On neutralise le callback AVANT d'appeler fermer() pour éviter
        // que la fermeture ne déclenche à nouveau reintegrateZone().
        if (floatDialog != null) {
            floatDialog.setOnFermetureCallback(null);
            if (floatDialog.isVisible()) {
                floatDialog.fermer();
            }
            floatDialog = null;
        }
    }

    /**
     * Réduit la zone dans le HSplitPane au moment du passage en mode flottant.
     * Mémorise la taille courante pour la restauration future.
     */
    private void collapserPourFloat() {
        if (!collapsed) {
            sizeBeforeCollapse = getSize();
            collapsed = true;
            if (header != null) header.updateCollapseState(true);
            animate(sizeBeforeCollapse, getCollapsedSize());
        }
    }

    /**
     * Développe la zone lors de la réintégration depuis le mode flottant.
     */
    private void expandDepuisFloat() {
        if (collapsed) {
            collapsed = false;
            if (header != null) header.updateCollapseState(false);

            Dimension targetSize = sizeBeforeCollapse;
            if (targetSize == null || (targetSize.width == 0 && targetSize.height == 0)) {
                targetSize = new Dimension(200, 150);
            }

            animate(getCollapsedSize(), targetSize);
        }
    }

    // =========================================================================
    // Animation
    // =========================================================================

    /**
     * Lance une animation progressive entre deux tailles.
     *
     * À chaque tick du timer on interpole linéairement entre startSize et
     * targetSize. Le flag inAnimation signale au HSplitPaneRootLayout de
     * respecter la taille courante de la zone pendant toute la durée.
     */
    private void animate(Dimension startSize, Dimension targetSize) {
        final int[] step = {0};
        int interval = ANIMATION_DURATION / STEP_COUNT;

        inAnimation = true;

        animationTimer = new javax.swing.Timer(interval, e -> {
            step[0]++;
            float t = (float) step[0] / STEP_COUNT;

            int w = (int) (startSize.width  + t * (targetSize.width  - startSize.width));
            int h = (int) (startSize.height + t * (targetSize.height - startSize.height));

            setSize(w, h);

            if (getParent() != null) {
                getParent().revalidate();
                getParent().repaint();
            }

            if (step[0] >= STEP_COUNT) {
                animationTimer.stop();
                inAnimation = false;
                setSize(targetSize.width, targetSize.height);

                if (getParent() != null) {
                    getParent().revalidate();
                    getParent().repaint();
                }
            }
        });

        animationTimer.setRepeats(true);
        animationTimer.start();
    }

    /**
     * Retourne la taille minimale visible quand la zone est réduite.
     * Correspond à la taille du header pour que les boutons restent accessibles.
     */
    public Dimension getCollapsedSize() {
        if (header == null) return new Dimension(0, 0);

        Dimension hs = header.getPreferredSize();

        switch (position) {
            case NORTH:
            case SOUTH: return new Dimension(0, hs.height);
            case WEST:
            case EAST:  return new Dimension(hs.width, 0);
            default:    return new Dimension(0, 0);
        }
    }

    // =========================================================================
    // API publique
    // =========================================================================

    public void addContainer(Component composant) {
        contentPanel.add(composant);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void removeContainer(Component composant) {
        contentPanel.remove(composant);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public boolean isEmpty() {
        return contentPanel.getComponentCount() == 0;
    }

    public void setWrapDirection(WrapDirection direction) {
        HSplitWrapLayout layout = (HSplitWrapLayout) contentPanel.getLayout();
        layout.setDirection(direction);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setEtirer(boolean etirer) {
        HSplitWrapLayout layout = (HSplitWrapLayout) contentPanel.getLayout();
        layout.setEtirer(etirer);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================================
    // Getters et Setters
    // =========================================================================

    public ZonePosition getPosition()         { return position;    }
    public boolean      isCollapsed()         { return collapsed;   }
    public boolean      isFullScreen()        { return fullScreen;  }
    public boolean      isFloating()          { return floating;    }
    public boolean      isEnAnimation()       { return inAnimation; }
    public boolean      animationInProgress() { return inAnimation; }
    public Dimension    getInitialSize()      { return initialSize; }

    public void setInitialSize(Dimension initialSize) {
        this.initialSize = initialSize;
    }

    public Dimension getSizeBeforeCollapse() { return sizeBeforeCollapse; }
    public String    getTitre()              { return titre;              }

    public void setTitre(String titre) {
        this.titre = titre;
        if (header != null) header.setTitre(titre);
    }

    public HSplitZoneHeader getHeader() { return header; }

    /**
     * Enregistre la référence au HSplitPane parent.
     * Appelé par HSplitPane lors de la construction — nécessaire pour que
     * les boutons fullscreen et float puissent coordonner avec le parent.
     *
     * @param splitPane le HSplitPane qui contient cette zone
     */
    public void setSplitPaneRef(HSplitPane splitPane) {
        this.splitPaneRef = splitPane;
    }
}