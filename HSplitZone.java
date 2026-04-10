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
 * Chaque zone occupe une position fixe (NORTH, SOUTH, EAST, WEST ou CENTER) et
 * peut accueillir plusieurs composants enfants positionnés par
 * HSplitWrapLayout. La zone dispose d'une barre de contrôle (HSplitZoneHeader)
 * permettant de la réduire ou de la développer via un bouton toggle.
 *
 * Structure interne :
 * <pre>
 *   HSplitZone (BorderLayout)
 *   ├── HSplitZoneHeader  [bord selon la position]
 *   └── HScrollPane       [CENTER]
 *         └── contentPanel (JPanel avec HSplitWrapLayout)
 * </pre>
 *
 * Quand la zone est réduite, sa taille est mise à zéro et la taille d'avant
 * réduction est mémorisée pour être restaurée au moment de l'expansion.
 */
public class HSplitZone extends JPanel {

    // -------------------------------------------------------------------------
    // Identité et position
    // -------------------------------------------------------------------------
    /**
     * Position de cette zone dans le HSplitPane parent.
     */
    private final ZonePosition position;

    /**
     * Titre affiché dans la barre de contrôle. Peut être null.
     */
    private String titre;

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------
    /**
     * Taille initiale fournie par la configuration. Null signifie que la zone
     * est flexible et prend l'espace restant.
     */
    private Dimension initialSize;

    /**
     * Taille sauvegardée juste avant un collapse. Permet de restaurer
     * exactement la même taille lors de l'expansion.
     */
    private Dimension sizeBeforeCollapse;

    // -------------------------------------------------------------------------
    // État
    // -------------------------------------------------------------------------
    /**
     * Indique si la zone est actuellement réduite.
     */
    private boolean collapsed;

    // -------------------------------------------------------------------------
    // Composants internes
    // -------------------------------------------------------------------------
    /**
     * Barre de contrôle avec titre et bouton toggle.
     */
    private HSplitZoneHeader header;

    /**
     * Panneau interne qui reçoit les composants de l'utilisateur.
     */
    private JPanel contentPanel;

    /**
     * Conteneur de défilement qui enveloppe le panneau de contenu.
     */
    private HScrollPane scrollPane;

    /**
     * Durée totale de l'animation en millisecondes.
     */
    private static final int ANIMATION_DURATION = 200;

    /**
     * Nombre de pas de l'animation — plus il est élevé, plus c'est fluide.
     */
    private static final int STEP_COUNT = 30;

    /**
     * Timer qui pilote l'animation de collapse/expand.
     */
    private javax.swing.Timer animationTimer;

    /**
     * Indique qu'une animation est en cours — le layout respecte la taille
     * courante de la zone pendant cette période.
     */
    private boolean inAnimation = false;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée une zone sans titre ni taille initiale définie.
     *
     * @param position la position de cette zone dans le HSplitPane
     */
    public HSplitZone(ZonePosition position) {
        this(position, null, null);
    }

    /**
     * Crée une zone avec un titre mais sans taille initiale définie.
     *
     * @param position la position de cette zone dans le HSplitPane
     * @param titre le titre affiché dans la barre de contrôle, ou null
     */
    public HSplitZone(ZonePosition position, String titre) {
        this(position, titre, null);
    }

    /**
     * Crée une zone entièrement paramétrée.
     *
     * @param position la position de cette zone dans le HSplitPane
     * @param titre le titre affiché dans la barre de contrôle, ou null
     * @param initialSize la taille initiale souhaitée, ou null pour flexible
     */
    public HSplitZone(ZonePosition position, String titre, Dimension initialSize) {
        this.position = position;
        this.titre = titre;
        this.initialSize = initialSize;
        this.collapsed = false;

        initializeComponents();
        assembleZone();
        connectListeners();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    /**
     * Instancie les composants internes de la zone.
     */
    private void initializeComponents() {

        // La barre de contrôle n'est pas créée pour la zone CENTER
        if (position != ZonePosition.CENTER) {
            header = new HSplitZoneHeader(position, titre);
        }

        // Le panneau de contenu utilise le layout wrap
        contentPanel = new JPanel(new HSplitWrapLayout());
        contentPanel.setOpaque(false);

        // Le scroll pane enveloppe le panneau de contenu
        scrollPane = new HScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(HScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(HScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Assemble les composants dans la zone selon la position. La barre de
     * contrôle est placée du côté intérieur de la zone pour que le bouton
     * toggle soit visible près du séparateur adjacent.
     */
    private void assembleZone() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(43, 43, 43));

        // Placement de la barre selon la position de la zone
        if (header != null) {
            switch (position) {
                case NORTH:
                    // La barre est en bas de la zone NORTH (côté intérieur)
                    add(header, BorderLayout.SOUTH);
                    break;
                case SOUTH:
                    // La barre est en haut de la zone SOUTH (côté intérieur)
                    add(header, BorderLayout.NORTH);
                    break;
                case WEST:
                    // La barre est à droite de la zone WEST (côté intérieur)
                    add(header, BorderLayout.EAST);
                    break;
                case EAST:
                    // La barre est à gauche de la zone EAST (côté intérieur)
                    add(header, BorderLayout.WEST);
                    break;
                default:
                    break;
            }
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Connecte le bouton toggle de la barre au mécanisme de collapse/expand. À
     * chaque clic, on bascule l'état de la zone et on notifie le parent.
     */
    private void connectListeners() {
        if (header != null) {
            header.addToggleListener(e -> toggleCollapse());
        }
    }

    // =========================================================================
    // Logique collapse / expand
    // =========================================================================
    /**
     * Bascule l'état de la zone entre réduit et développé. Appelé
     * automatiquement par le clic sur le bouton toggle.
     */
    private void toggleCollapse() {
        if (collapsed) {
            expand();
        } else {
            collapse();
        }
    }

    /**
     * Réduit la zone à une taille nulle.
     *
     * La taille courante est mémorisée dans sizeBeforeCollapse avant que la
     * zone soit masquée. Le HSplitPaneRootLayout détectera le changement lors
     * du prochain revalidate et redistribuera l'espace.
     */
    public void collapse() {
    if (collapsed) return;

    if (animationTimer != null && animationTimer.isRunning()) {
        animationTimer.stop();
    }

    sizeBeforeCollapse = getSize();
    collapsed = true;

    if (header != null) {
        header.updateCollapseState(true);
    }

    animate(sizeBeforeCollapse, getCollapsedSize());
}

    public boolean animationInProgress() {
        return inAnimation;
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

    if (header != null) {
        header.updateCollapseState(false);
    }

    Dimension targetSize = sizeBeforeCollapse;
    if (targetSize == null || (targetSize.width == 0 && targetSize.height == 0)) {
        targetSize = getPreferredSize();
        if (targetSize.width  <= 0) targetSize.width  = 200;
        if (targetSize.height <= 0) targetSize.height = 150;
    }

    animate(getCollapsedSize(), targetSize);
}

    /**
     * Lance une animation progressive entre deux tailles.
     *
     * À chaque tick du timer, on calcule la taille intermédiaire en interpolant
     * linéairement entre la taille de départ et la taille cible. Quand on
     * atteint le dernier pas, on force la taille finale exacte et on arrête le
     * timer.
     *
     * @param startSize taille au début de l'animation
     * @param targetSize taille à atteindre en fin d'animation
     */
    private void animate(Dimension startSize, Dimension targetSize) {
    final int[] step = {0};
    int interval = ANIMATION_DURATION / STEP_COUNT;

    inAnimation = true;

    animationTimer = new javax.swing.Timer(interval, e -> {
        step[0]++;

        float t = (float) step[0] / STEP_COUNT;

        int currentWidth  = (int) (startSize.width  + t * (targetSize.width  - startSize.width));
        int currentHeight = (int) (startSize.height + t * (targetSize.height - startSize.height));

        setSize(currentWidth, currentHeight);

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
     * Retourne la taille minimale visible quand la zone est réduite. Correspond
     * à la taille du header seul, pour que le bouton toggle reste accessible
     * même après un collapse.
     *
     * @return la taille du header, ou une taille nulle si pas de header
     */
    public Dimension getCollapsedSize() {
        if (header == null) {
            return new Dimension(0, 0);
        }

        Dimension headerSize = header.getPreferredSize();

        switch (position) {
            case NORTH:
            case SOUTH:
                // Header horizontal : on garde sa hauteur
                return new Dimension(0, headerSize.height);

            case WEST:
            case EAST:
                // Header vertical : on garde sa largeur
                return new Dimension(headerSize.width, 0);

            default:
                return new Dimension(0, 0);
        }
    }

    // =========================================================================
    // API publique
    // =========================================================================
    /**
     * Ajoute un composant dans la zone de contenu. Le HSplitWrapLayout se
     * chargera de le positionner avec les autres.
     *
     * @param composant le composant à ajouter dans cette zone
     */
    public void addContainer(Component composant) {
        contentPanel.add(composant);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Retire un composant de la zone de contenu.
     *
     * @param composant le composant à retirer
     */
    public void removeContainer(Component composant) {
        contentPanel.remove(composant);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Indique si la zone est vide, c'est-à-dire qu'elle ne contient aucun
     * composant. Une zone vide ne devrait pas prendre d'espace dans le layout
     * racine.
     *
     * @return true si aucun composant n'a été ajouté à cette zone
     */
    public boolean isEmpty() {
        return contentPanel.getComponentCount() == 0;
    }

    /**
     * Modifie la direction de disposition des composants dans cette zone. Les
     * composants seront réorganisés immédiatement après l'appel.
     *
     * @param direction la nouvelle direction de wrapping
     */
    public void setWrapDirection(WrapDirection direction) {
        HSplitWrapLayout layout = (HSplitWrapLayout) contentPanel.getLayout();
        layout.setDirection(direction);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Active ou désactive l'étirement des composants dans cette zone. Quand
     * true, les composants occupent tout l'espace disponible sur leur ligne.
     * Quand false, ils conservent leur preferredSize.
     *
     * @param etirer true pour étirer, false pour respecter la preferredSize
     */
    public void setEtirer(boolean etirer) {
        HSplitWrapLayout layout = (HSplitWrapLayout) contentPanel.getLayout();
        layout.setEtirer(etirer);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================================
    // Getters et Setters
    // =========================================================================
    public ZonePosition getPosition() {
        return position;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public Dimension getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Dimension initialSize) {
        this.initialSize = initialSize;
    }

    public Dimension getSizeBeforeCollapse() {
        return sizeBeforeCollapse;
    }

    public String getTitre() {
        return titre;
    }

    /**
     * Modifie le titre de la zone et met à jour la barre de contrôle.
     *
     * @param titre le nouveau titre, ou null pour masquer le titre
     */
    public void setTitre(String titre) {
        this.titre = titre;
        if (header != null) {
            header.setTitre(titre);
        }
    }

    /**
     * Retourne la barre de contrôle de cette zone. Null si la zone est CENTER
     * (pas de barre pour le centre).
     *
     * @return le header ou null
     */
    public HSplitZoneHeader getHeader() {
        return header;
    }
}
