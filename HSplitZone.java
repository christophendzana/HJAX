package hsplitpane;

import hcomponents.HScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class HSplitZone extends JPanel {

    /**
     * État exclusif d'une zone. Une zone est toujours dans exactement un de ces
     * états.
     */
    public enum ZoneState {
        NORMAL, COLLAPSED, FULLSCREEN, FLOATING
    }

    private final ZonePosition position;

    private String titre;

    private Integer initialSize;

    private Dimension sizeBeforeCollapse;

    private Dimension currentAnimationSize;

    private ZoneState state = ZoneState.NORMAL;

    /**
     * Mémorise l'état d'avant l'entrée en fullscreen, pour le restaurer
     * fidèlement à la sortie (une zone COLLAPSED qui passe en FULLSCREEN doit
     * redevenir COLLAPSED, pas NORMAL).
     */
    private ZoneState stateBeforeFullScreen;

    private boolean inAnimation;

    private HSplitPane splitPaneRef;

    private HSplitZoneHeader header;

    private Color headerBackgroundColor;

    private JPanel headerContainer;

    private JPanel contentPanel;

    private HScrollPane scrollPane;

    private HSplitFloatDialog floatDialog;

    private static final int ANIMATION_DURATION = 160;

    private static final int STEP_COUNT = 30;

    private javax.swing.Timer animationTimer;

    private HSplitZoneHeader.HeaderPosition expandedHeaderPosition;

    private HSplitZoneHeader.HeaderPosition defaultCollapsedPosition;

    private Color headerTitleColor;

    private Font headerTitleFont;

    private static final int DEFAULT_SIZE_NORTH_SOUTH = 150;

    private static final int DEFAULT_SIZE_WEST_EAST = 200;

    public HSplitZone(ZonePosition position) {
        this(position, null, null);
    }

    public HSplitZone(ZonePosition position, String titre) {
        this(position, titre, null);
    }

    public HSplitZone(ZonePosition position, String titre, Integer initialSize) {
        this.position = position;
        this.titre = titre;
        this.initialSize = resolveInitialSize(position, initialSize);

        this.currentAnimationSize = null;

        this.defaultCollapsedPosition = getDefaultCollapsedPosition(position);
        this.expandedHeaderPosition = this.defaultCollapsedPosition;

        this.headerBackgroundColor = new Color(60, 63, 65);
        this.headerTitleColor = new Color(187, 187, 187);
        this.headerTitleFont = new Font("Dialog", Font.PLAIN, 11);

        initializeComponents();
        assembleZone();

        connectListenersOnce();
    }

    private static Integer resolveInitialSize(ZonePosition position, Integer provided) {
        if (provided != null) {
            return provided;
        }
        return switch (position) {
            case NORTH, SOUTH ->
                DEFAULT_SIZE_NORTH_SOUTH;
            case WEST, EAST ->
                DEFAULT_SIZE_WEST_EAST;
            case CENTER ->
                null;
        };
    }

    private HSplitZoneHeader.HeaderPosition getDefaultCollapsedPosition(ZonePosition pos) {
        return switch (pos) {

            case NORTH ->
                HSplitZoneHeader.HeaderPosition.BOTTOM;

            case SOUTH ->
                HSplitZoneHeader.HeaderPosition.TOP;

            case WEST ->
                HSplitZoneHeader.HeaderPosition.RIGHT;

            case EAST ->
                HSplitZoneHeader.HeaderPosition.LEFT;

            case CENTER ->
                null;
            default ->
                null;
        };
    }

    private void initializeComponents() {

        if (position != ZonePosition.CENTER) {

            HSplitZoneHeader.HeaderPosition initialPosition = (state == ZoneState.COLLAPSED)
                    ? defaultCollapsedPosition
                    : expandedHeaderPosition;

            header = new HSplitZoneHeader(
                    position,
                    titre,
                    headerBackgroundColor,
                    headerTitleColor,
                    22,
                    initialPosition
            );

            if (headerTitleFont != null) {
                header.setTitleFont(headerTitleFont);
            }
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

        headerContainer = new JPanel(new BorderLayout());
        headerContainer.setOpaque(false);

        if (header != null) {

            HSplitZoneHeader.HeaderPosition initialPosition = (state == ZoneState.COLLAPSED)
                    ? defaultCollapsedPosition
                    : expandedHeaderPosition;
            String constraint = headerPositionToBorderLayoutConstraint(initialPosition);
            headerContainer.add(header, constraint);
        }

        String headerConstraint = headerPositionToBorderLayoutConstraint(
                state == ZoneState.COLLAPSED
                        ? defaultCollapsedPosition
                        : expandedHeaderPosition
        );

        add(headerContainer, headerConstraint);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void connectListenersOnce() {
        if (header == null) {
            return;
        }

        header.addToggleListener(e -> toggleCollapse());
        header.addFullScreenListener(e -> toggleFullScreen());
        header.addFloatListener(e -> toggleFloat());
    }

    private String headerPositionToBorderLayoutConstraint(HSplitZoneHeader.HeaderPosition pos) {
        if (pos == null) {
            return BorderLayout.CENTER;
        }

        return switch (pos) {
            case TOP ->
                BorderLayout.NORTH;
            case BOTTOM ->
                BorderLayout.SOUTH;
            case LEFT ->
                BorderLayout.WEST;
            case RIGHT ->
                BorderLayout.EAST;
            default ->
                BorderLayout.CENTER;
        };
    }

    private void toggleCollapse() {
        if (state == ZoneState.COLLAPSED) {
            expand();
        } else {
            collapse();
        }
    }

    public void collapse() {
        if (state == ZoneState.COLLAPSED) {
            return;
        }

        sizeBeforeCollapse = getSize();
        state = ZoneState.COLLAPSED;

        if (header != null) {
            header.updateCollapseState(true);
        }

        animateSize(getCollapsedSize());
    }

    public void expand() {
        if (state != ZoneState.COLLAPSED) {
            return;
        }

        state = ZoneState.NORMAL;

        if (header != null) {
            header.updateCollapseState(false);
        }

        animateSize(resolveExpandedTargetSize());
    }

    private Dimension resolveExpandedTargetSize() {
        Dimension targetSize = sizeBeforeCollapse;
        if (targetSize == null || (targetSize.width == 0 && targetSize.height == 0)) {
            targetSize = new Dimension(200, 150);
        }
        return targetSize;
    }

    private void refreshHeaderPosition() {
        if (header == null) {
            return;
        }

        HSplitZoneHeader.HeaderPosition targetPosition =
                (state == ZoneState.COLLAPSED)
                        ? defaultCollapsedPosition
                        : expandedHeaderPosition;

        // Mise à jour du header lui-même
        header.updateEffectivePosition(targetPosition);
        header.setCouleurFond(headerBackgroundColor);
        header.setCouleurTitre(headerTitleColor);

        if (headerTitleFont != null) {
            header.setTitleFont(headerTitleFont);
        }

        // Repositionnement du header dans son container
        headerContainer.removeAll();
        headerContainer.add(header,
                headerPositionToBorderLayoutConstraint(targetPosition));

        // IMPORTANT :
        // le headerContainer doit lui aussi être placé sur le bon bord
        remove(headerContainer);

        add(headerContainer,
                headerPositionToBorderLayoutConstraint(targetPosition));

        revalidate();
        repaint();
    }

    private void toggleFullScreen() {
        if (splitPaneRef == null) {
            return;
        }

        if (state == ZoneState.FULLSCREEN) {
            splitPaneRef.exitFullScreen();
        } else {
            splitPaneRef.enterFullScreen(position);
        }
    }

    /**
     * Bascule l'état plein écran de la zone. Appelée uniquement par HSplitPane,
     * qui est seul à savoir coordonner les autres zones — voir le commentaire
     * de classe sur ZoneState pour le principe de source de vérité unique.
     */
    void setFullScreenState(boolean fullScreen) {
        if (fullScreen) {
            stateBeforeFullScreen = state;
            state = ZoneState.FULLSCREEN;
        } else {
            state = (stateBeforeFullScreen != null) ? stateBeforeFullScreen : ZoneState.NORMAL;
            stateBeforeFullScreen = null;
        }
        if (header != null) {
            header.updateFullScreenState(fullScreen);
        }
    }

    private void toggleFloat() {
        if (state == ZoneState.FLOATING) {
            reintegrateZone();
        } else {
            floatZone();
        }
    }

    /**
     * Détache la zone dans une fenêtre flottante.
     *
     * CHANGEMENT — auparavant, floatZone() dupliquait collapse() dans une
     * méthode privée collapserPourFloat() presque identique (même animation,
     * mêmes mises à jour de header), simplement parce que le modèle à 4
     * booléens indépendants "avait besoin" d'être à la fois collapsed=true et
     * floating=true. Avec un état exclusif, on n'a plus ce besoin : le mode
     * FLOATING représente à lui seul "une zone réduite à un emplacement réservé
     * pendant que son contenu vit dans une fenêtre externe" (voir
     * HSplitPaneRootLayout, qui traite COLLAPSED et FLOATING de la même façon
     * pour le calcul de taille). Résultat : une seule animation de réduction,
     * appelée ici directement, sans méthode dupliquée.
     */
    public void floatZone() {

        if (state == ZoneState.FLOATING) {
            return;
        }

        boolean etaitCollapsed = (state == ZoneState.COLLAPSED);
        state = ZoneState.FLOATING;

        if (header != null) {
            header.updateFloatingState(true);
        }

        remove(scrollPane);
        revalidate();
        repaint();

        String dialogTitle = titre != null ? titre : "Zone flottante";
        floatDialog = new HSplitFloatDialog(dialogTitle);
        floatDialog.setContenu(scrollPane);
        floatDialog.setOnFermetureCallback(this::reintegrateZone);

        if (!etaitCollapsed) {
            sizeBeforeCollapse = getSize();
            animateSize(getCollapsedSize());
        }

        floatDialog.afficher();
    }

    public void reintegrateZone() {

        if (state != ZoneState.FLOATING) {
            return;
        }

        state = ZoneState.NORMAL;

        if (header != null) {
            header.updateFloatingState(false);
        }

        add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();

        animateSize(resolveExpandedTargetSize());

        if (floatDialog != null) {
            floatDialog.setOnFermetureCallback(null);
            if (floatDialog.isVisible()) {
                floatDialog.fermer();
            }
            floatDialog = null;
        }
    }

    // =========================================================================
    // Moteur d'animation — unifié
    //
    // CHANGEMENT — animate(Dimension,Dimension) et animerVersBounds(x,y,w,h)
    // réimplémentaient chacune leur propre javax.swing.Timer avec la même
    // interpolation linéaire. animateSize() n'est plus qu'un raccourci de
    // animateTo() qui garde la position (x,y) courante. Une seule mécanique
    // d'animation à maintenir : si demain tu veux changer la courbe
    // d'interpolation (easing au lieu de linéaire), un seul endroit à modifier.
    // =========================================================================
    private void animateTo(int targetX, int targetY, int targetWidth, int targetHeight) {
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
            int cw = (int) (startW + t * (targetWidth - startW));
            int ch = (int) (startH + t * (targetHeight - startH));

            currentAnimationSize = new Dimension(cw, ch);
            setBounds(cx, cy, cw, ch);

            if (getParent() != null) {
                getParent().revalidate();
                getParent().repaint();
            }

            if (step[0] >= STEP_COUNT) {
                animationTimer.stop();
                inAnimation = false;
                currentAnimationSize = null;
                setBounds(targetX, targetY, targetWidth, targetHeight);

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
     * Anime uniquement la taille, en conservant la position actuelle
     * (collapse/expand/float).
     */
    private void animateSize(Dimension targetSize) {
        animateTo(getX(), getY(), targetSize.width, targetSize.height);
    }

    /**
     * Anime position ET taille (utilisé pour le passage en plein écran).
     */
    public void animerVersBounds(int targetX, int targetY, int targetWidth, int targetHeight) {
        animateTo(targetX, targetY, targetWidth, targetHeight);
    }

    @Override
    public Dimension getPreferredSize() {

        if (inAnimation && currentAnimationSize != null) {
            return new Dimension(currentAnimationSize);
        }

        if (initialSize != null) {
            return switch (position) {
                case NORTH, SOUTH ->
                    new Dimension(0, initialSize);
                case WEST, EAST ->
                    new Dimension(initialSize, 0);
                case CENTER ->
                    super.getPreferredSize();
            };
        }

        return super.getPreferredSize();
    }

    public Dimension getCollapsedSize() {
        if (header == null) {
            return new Dimension(0, 0);
        }

        int thickness = header.getEpaisseur();

        if (defaultCollapsedPosition == HSplitZoneHeader.HeaderPosition.TOP
                || defaultCollapsedPosition == HSplitZoneHeader.HeaderPosition.BOTTOM) {

            return new Dimension(0, thickness);
        } else {

            return new Dimension(thickness, 0);
        }
    }

    public void setExpandedHeaderPosition(HSplitZoneHeader.HeaderPosition position) {
        if (position == null) {
            throw new IllegalArgumentException("La position du header ne peut pas être null");
        }

        if (!isValidHeaderPosition(position)) {
            throw new IllegalArgumentException(
                    "La position " + position + " n'est pas valide pour la zone " + this.position
            );
        }

        this.expandedHeaderPosition = position;

        if (!inAnimation) {
            refreshHeaderPosition();
            revalidate();
            repaint();
        }
    }

    public HSplitZoneHeader.HeaderPosition getExpandedHeaderPosition() {
        return expandedHeaderPosition;
    }

    public HSplitZoneHeader.HeaderPosition getDefaultCollapsedPosition() {
        return defaultCollapsedPosition;
    }

    private boolean isValidHeaderPosition(HSplitZoneHeader.HeaderPosition pos) {
        if (pos == null) {
            return false;
        }

        return switch (position) {
            case NORTH ->
                pos == HSplitZoneHeader.HeaderPosition.TOP
                || pos == HSplitZoneHeader.HeaderPosition.BOTTOM;
            case SOUTH ->
                pos == HSplitZoneHeader.HeaderPosition.TOP
                || pos == HSplitZoneHeader.HeaderPosition.BOTTOM;
            case WEST ->
                pos == HSplitZoneHeader.HeaderPosition.LEFT
                || pos == HSplitZoneHeader.HeaderPosition.RIGHT
                || pos == HSplitZoneHeader.HeaderPosition.TOP;
            case EAST ->
                pos == HSplitZoneHeader.HeaderPosition.LEFT
                || pos == HSplitZoneHeader.HeaderPosition.RIGHT
                || pos == HSplitZoneHeader.HeaderPosition.TOP;
            case CENTER ->
                false;
            default ->
                false;
        };
    }

    public void setHeaderBackgroundColor(Color color) {
        this.headerBackgroundColor = color;
        if (header != null) {
            header.setCouleurFond(color);
        }
    }

    public Color getHeaderBackgroundColor() {
        return headerBackgroundColor;
    }

    public void setHeaderTitleColor(Color color) {
        if (color == null) {
            return;
        }
        this.headerTitleColor = color;
        if (header != null) {
            header.setCouleurTitre(color);
        }
    }

    public Color getHeaderTitleColor() {
        return headerTitleColor;
    }

    public void setHeaderTitleFont(Font font) {
        if (font == null) {
            return;
        }
        this.headerTitleFont = font;
        if (header != null) {
            header.setTitleFont(font);
        }
    }

    public Font getHeaderTitleFont() {
        return headerTitleFont;
    }

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

    public ZonePosition getPosition() {
        return position;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
        if (header != null) {
            header.setTitre(titre);
        }
    }

    public ZoneState getState() {
        return state;
    }

    public boolean isCollapsed() {
        return state == ZoneState.COLLAPSED;
    }

    public boolean isFullScreen() {
        return state == ZoneState.FULLSCREEN;
    }

    public boolean isFloating() {
        return state == ZoneState.FLOATING;
    }

    /**
     * Vrai si la zone doit être rendue à sa taille réduite (collapsed OU
     * floating se comportent visuellement de la même façon dans le layout du
     * parent).
     */
    public boolean isCollapsedOrFloating() {
        return state == ZoneState.COLLAPSED || state == ZoneState.FLOATING;
    }

    public boolean isEnAnimation() {
        return inAnimation;
    }

    public boolean animationInProgress() {
        return inAnimation;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        if (position == ZonePosition.CENTER) {
            throw new UnsupportedOperationException(
                    "La zone CENTER occupe toujours l'espace restant, sa taille ne peut pas être définie.");
        }
        this.initialSize = initialSize;
    }

    public Dimension getSizeBeforeCollapse() {
        return sizeBeforeCollapse;
    }

    public HSplitZoneHeader getHeader() {
        return header;
    }

    public void setSplitPaneRef(HSplitPane splitPane) {
        this.splitPaneRef = splitPane;
    }
}
