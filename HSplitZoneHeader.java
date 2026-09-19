package hsplitpane;

import hcomponents.HButton;
import hcomponents.HLabel;
import hcomponents.vues.HLabelOrientation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 *@author FIDELE
 */
public class HSplitZoneHeader extends JPanel {

    private static final int DEFAULT_HEIGHT = 22;

    private static final int BUTTON_SIZE = 16;

    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(60, 63, 65);

    private static final Color DEFAULT_TITLE_COLOR = new Color(187, 187, 187);

    public enum HeaderPosition {

        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

    private final ZonePosition position;

    private String title;

    private Color backgroundColor;

    private Color titleColor;

    private int thickness;

    private HeaderPosition effectivePosition;

    private boolean isCollapsed;

    private boolean isFullScreen;

    private boolean isFloating;

    private HLabel titleLabel;

    // Les 3 actions standard du header. Pour ajouter une 4ème action (ex: pin),
    // voir getActions() plus bas et le commentaire de limite assumée dans
    // HeaderAction.java.
    private final List<HeaderAction> actions = new ArrayList<>();
    private HeaderAction toggleAction;
    private HeaderAction fullScreenAction;
    private HeaderAction floatAction;

    public HSplitZoneHeader(ZonePosition position) {
        this(position, null, DEFAULT_BACKGROUND_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_HEIGHT);
    }

    public HSplitZoneHeader(ZonePosition position, String title) {
        this(position, title, DEFAULT_BACKGROUND_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_HEIGHT);
    }

    public HSplitZoneHeader(ZonePosition position, String title,
            Color backgroundColor, Color titleColor,
            int thickness) {
        this(position, title, backgroundColor, titleColor, thickness,
                getDefaultHeaderPositionForZone(position));
    }

    public HSplitZoneHeader(ZonePosition position, String title,
            Color backgroundColor, Color titleColor,
            int thickness, HeaderPosition effectivePosition) {
        this.position = position;
        this.title = title;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.thickness = thickness;
        this.effectivePosition = effectivePosition;

        this.isCollapsed = false;
        this.isFullScreen = false;
        this.isFloating = false;

        initialiserActions();
        initialiserComposants();
        applyDimensions();
    }

    private static HeaderPosition getDefaultHeaderPositionForZone(ZonePosition pos) {
        if (pos == null) {
            return HeaderPosition.TOP;
        }

        return switch (pos) {
            case NORTH ->
                HeaderPosition.BOTTOM;
            case SOUTH ->
                HeaderPosition.TOP;
            case WEST ->
                HeaderPosition.RIGHT;
            case EAST ->
                HeaderPosition.LEFT;
            default ->
                HeaderPosition.TOP;
        };
    }

    /**
     * Crée les 3 actions standard une seule fois (à la construction). Leurs
     * écouteurs survivent à toute reconstruction ultérieure des boutons —
     * voir HeaderAction.bindButton().
     */
    private void initialiserActions() {
        toggleAction = new HeaderAction("toggle", (g2, w, h, color) -> {
            g2.setColor(color);
            dessinerFleche(g2, w / 2, h / 2, 4);
        });

        fullScreenAction = new HeaderAction("fullscreen", (g2, w, h, color) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            dessinerIconeFullScreen(g2, w, h);
        });

        floatAction = new HeaderAction("float", (g2, w, h, color) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            dessinerIconeFloat(g2, w, h);
        });

        actions.add(toggleAction);
        actions.add(fullScreenAction);
        actions.add(floatAction);
    }

    private void initialiserComposants() {
        setOpaque(true);

        titleLabel = new HLabel(title != null ? title : "");
        titleLabel.setForeground(titleColor);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));
        titleLabel.setVisible(title != null && !title.isEmpty());

        if (effectivePosition == HeaderPosition.LEFT) {
            titleLabel.setOrientation(HLabelOrientation.VERTICAL_UP);
        } else if (effectivePosition == HeaderPosition.RIGHT) {
            titleLabel.setOrientation(HLabelOrientation.VERTICAL_DOWN);
        } else {
            titleLabel.setOrientation(HLabelOrientation.HORIZONTAL);
        }

        HButton toggleButton = createActionButton(toggleAction);
        HButton fullScreenButton = createActionButton(fullScreenAction);
        HButton floatButton = createActionButton(floatAction);

        if (effectivePosition == HeaderPosition.TOP || effectivePosition == HeaderPosition.BOTTOM) {

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            add(Box.createHorizontalStrut(4));
            add(titleLabel);
            add(Box.createHorizontalGlue());
            add(floatButton);
            add(Box.createHorizontalStrut(2));
            add(fullScreenButton);
            add(Box.createHorizontalStrut(2));
            add(toggleButton);
            add(Box.createHorizontalStrut(4));

            titleLabel.setAlignmentY(CENTER_ALIGNMENT);
            floatButton.setAlignmentY(CENTER_ALIGNMENT);
            fullScreenButton.setAlignmentY(CENTER_ALIGNMENT);
            toggleButton.setAlignmentY(CENTER_ALIGNMENT);

        } else {

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(4));
            add(toggleButton);
            add(Box.createVerticalStrut(2));
            add(fullScreenButton);
            add(Box.createVerticalStrut(2));
            add(floatButton);
            add(Box.createVerticalGlue());
            add(titleLabel);
            add(Box.createVerticalStrut(4));

            titleLabel.setAlignmentX(CENTER_ALIGNMENT);
            toggleButton.setAlignmentX(CENTER_ALIGNMENT);
            fullScreenButton.setAlignmentX(CENTER_ALIGNMENT);
            floatButton.setAlignmentX(CENTER_ALIGNMENT);
        }
    }

    private void applyDimensions() {
        if (effectivePosition == HeaderPosition.TOP || effectivePosition == HeaderPosition.BOTTOM) {
            setPreferredSize(new Dimension(Integer.MAX_VALUE, thickness));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, thickness));
            setMinimumSize(new Dimension(0, thickness));
        } else {
            setPreferredSize(new Dimension(thickness, Integer.MAX_VALUE));
            setMaximumSize(new Dimension(thickness, Integer.MAX_VALUE));
            setMinimumSize(new Dimension(thickness, 0));
        }
    }

    /**
     * CHANGEMENT — ne recrée plus les HeaderAction (ce qui aurait perdu leurs
     * listeners), seulement les boutons/layout. Voir le commentaire de classe.
     */
    public void updateEffectivePosition(HeaderPosition newPosition) {

        if (this.effectivePosition == newPosition) {
            return;
        }

        this.effectivePosition = newPosition;

        removeAll();
        initialiserComposants();
        applyDimensions();

        revalidate();
        repaint();
    }

    private HButton createActionButton(HeaderAction action) {
        HButton bouton = new HButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                action.getIconPainter().paint(g2, getWidth(), getHeight(), titleColor);
                g2.dispose();
            }
        };
        styleButton(bouton);
        action.bindButton(bouton);
        return bouton;
    }

    private void dessinerIconeFullScreen(Graphics2D g2, int w, int h) {
        int m = 3;
        int sz = Math.min(w, h) - m * 2;
        int x0 = (w - sz) / 2;
        int y0 = (h - sz) / 2;

        if (!isFullScreen) {
            g2.drawRect(x0, y0, sz, sz);

            int cornerLen = 3;
            g2.drawLine(x0, y0, x0 + cornerLen, y0);
            g2.drawLine(x0, y0, x0, y0 + cornerLen);
            g2.drawLine(x0 + sz, y0, x0 + sz - cornerLen, y0);
            g2.drawLine(x0 + sz, y0, x0 + sz, y0 + cornerLen);
        } else {
            int off = 3;
            int sz2 = sz - off;
            g2.drawRect(x0 + off, y0, sz2, sz2);
            g2.drawRect(x0, y0 + off, sz2, sz2);
        }
    }

    private void dessinerIconeFloat(Graphics2D g2, int w, int h) {
        int m = 3;
        int sz = Math.min(w, h) - m * 2;
        int x0 = (w - sz) / 2;
        int y0 = (h - sz) / 2;

        if (!isFloating) {
            int ws = (int) (sz * 0.65);

            g2.drawRect(x0, y0 + sz - ws, ws, ws);

            int ax = x0 + sz;
            int ay = y0;
            int as = 3;
            g2.drawLine(x0 + ws - 1, y0 + sz - ws + 1, ax, ay);

            g2.drawLine(ax, ay, ax - as, ay);
            g2.drawLine(ax, ay, ax, ay + as);
        } else {
            int as = 4;
            g2.drawLine(x0 + sz, y0, x0, y0 + sz);

            g2.drawLine(x0, y0 + sz, x0 + as * 2, y0 + sz);
            g2.drawLine(x0, y0 + sz, x0, y0 + sz - as * 2);
        }
    }

    private void styleButton(HButton bouton) {
        bouton.setOpaque(false);
        bouton.setContentAreaFilled(false);
        bouton.setBorderPainted(false);
        bouton.setFocusPainted(false);
        bouton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        bouton.setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        bouton.setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        bouton.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    // =========================================================================
    // NOTE — dessinerFleche() n'a pas été retouchée dans cette refonte : sa
    // logique combine (position de zone × position du header) avec des cas
    // spéciaux codés en dur, sans garantie que isValidHeaderPosition()
    // (dans HSplitZone) et cette méthode restent synchronisées si une nouvelle
    // combinaison devient valide un jour. Ce n'était pas dans le périmètre des
    // 4 décisions validées — je te le signale comme recommandation de suite,
    // pas comme un correctif appliqué ici.
    // =========================================================================
    private void dessinerFleche(Graphics2D g2, int cx, int cy, int t) {
        int[] xPoints;
        int[] yPoints;

        if (position == ZonePosition.WEST && effectivePosition == HeaderPosition.TOP) {
            if (isCollapsed) {
                xPoints = new int[]{cx + t, cx - t, cx - t};
                yPoints = new int[]{cy, cy - t, cy + t};
            } else {
                xPoints = new int[]{cx - t, cx + t, cx + t};
                yPoints = new int[]{cy, cy - t, cy + t};
            }
        } else if (position == ZonePosition.EAST && effectivePosition == HeaderPosition.TOP) {
            if (isCollapsed) {
                xPoints = new int[]{cx - t, cx + t, cx + t};
                yPoints = new int[]{cy, cy - t, cy + t};
            } else {
                xPoints = new int[]{cx + t, cx - t, cx - t};
                yPoints = new int[]{cy, cy - t, cy + t};
            }
        } else if (position == ZonePosition.NORTH && effectivePosition == HeaderPosition.TOP) {
            if (isCollapsed) {
                xPoints = new int[]{cx - t, cx + t, cx};
                yPoints = new int[]{cy - t, cy - t, cy + t};
            } else {
                xPoints = new int[]{cx - t, cx + t, cx};
                yPoints = new int[]{cy + t, cy + t, cy - t};
            }
        } else if (position == ZonePosition.SOUTH && effectivePosition == HeaderPosition.BOTTOM) {
            if (isCollapsed) {
                xPoints = new int[]{cx - t, cx + t, cx};
                yPoints = new int[]{cy + t, cy + t, cy - t};
            } else {
                xPoints = new int[]{cx - t, cx + t, cx};
                yPoints = new int[]{cy - t, cy - t, cy + t};
            }
        } else {
            switch (effectivePosition) {
                case TOP:
                    xPoints = new int[]{cx - t, cx + t, cx};
                    yPoints = isCollapsed
                            ? new int[]{cy + t, cy + t, cy - t}
                            : new int[]{cy - t, cy - t, cy + t};
                    break;

                case BOTTOM:
                    xPoints = new int[]{cx - t, cx + t, cx};
                    yPoints = isCollapsed
                            ? new int[]{cy - t, cy - t, cy + t}
                            : new int[]{cy + t, cy + t, cy - t};
                    break;

                case LEFT:
                    xPoints = isCollapsed
                            ? new int[]{cx - t, cx - t, cx + t}
                            : new int[]{cx + t, cx + t, cx - t};
                    yPoints = new int[]{cy - t, cy + t, cy};
                    break;

                case RIGHT:
                    xPoints = isCollapsed
                            ? new int[]{cx + t, cx + t, cx - t}
                            : new int[]{cx - t, cx - t, cx + t};
                    yPoints = new int[]{cy - t, cy + t, cy};
                    break;

                default:
                    return;
            }
        }

        g2.fillPolygon(xPoints, yPoints, 3);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // =========================================================================
    // Méthodes de compatibilité — routent vers les HeaderAction correspondantes.
    // HSplitZone continue d'appeler ces 3 méthodes sans aucun changement de son
    // côté ; c'est l'implémentation interne qui n'est plus dupliquée.
    // =========================================================================
    public void addToggleListener(ActionListener listener) {
        toggleAction.addListener(listener);
    }

    public void addFullScreenListener(ActionListener listener) {
        fullScreenAction.addListener(listener);
    }

    public void addFloatListener(ActionListener listener) {
        floatAction.addListener(listener);
    }

    public void updateCollapseState(boolean collapsed) {
        this.isCollapsed = collapsed;
        toggleAction.repaintButton();
    }

    public void updateFullScreenState(boolean fullScreen) {
        this.isFullScreen = fullScreen;
        fullScreenAction.repaintButton();
    }

    public void updateFloatingState(boolean floating) {
        this.isFloating = floating;
        floatAction.repaintButton();
    }

    public String getTitre() {
        return title;
    }

    public void setTitre(String title) {
        this.title = title;
        titleLabel.setText(title != null ? title : "");
        titleLabel.setVisible(title != null && !title.isEmpty());
        revalidate();
        repaint();
    }

    public void setTitleFont(Font font) {
        if (font != null && titleLabel != null) {
            titleLabel.setFont(font);
            revalidate();
            repaint();
        }
    }

    public Font getTitleFont() {
        return titleLabel != null ? titleLabel.getFont() : null;
    }

    public void setTitleOrientation(HLabelOrientation orientation) {
        if (titleLabel != null) {
            titleLabel.setOrientation(orientation);
            revalidate();
            repaint();
        }
    }

    public HLabelOrientation getTitleOrientation() {
        return titleLabel != null ? titleLabel.getOrientation() : HLabelOrientation.HORIZONTAL;
    }

    public Color getCouleurFond() {
        return backgroundColor;
    }

    public void setCouleurFond(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getCouleurTitre() {
        return titleColor;
    }

    public void setCouleurTitre(Color titleColor) {
        this.titleColor = titleColor;
        if (titleLabel != null) {
            titleLabel.setForeground(titleColor);
        }
        repaint();
    }

    public int getEpaisseur() {
        return thickness;
    }

    public void setEpaisseur(int thickness) {
        this.thickness = thickness;
        applyDimensions();
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (effectivePosition == HeaderPosition.TOP || effectivePosition == HeaderPosition.BOTTOM) {
            return new Dimension(Short.MAX_VALUE, thickness);
        } else {
            return new Dimension(thickness, Short.MAX_VALUE);
        }
    }

    private static class HeaderAction {

        @FunctionalInterface
        private interface IconPainter {
            void paint(Graphics2D g2, int width, int height, Color color);
        }

        private final String id;
        private final IconPainter iconPainter;
        private final List<ActionListener> listeners = new ArrayList<>();
        private AbstractButton button;

        private HeaderAction(String id, IconPainter iconPainter) {
            this.id = id;
            this.iconPainter = iconPainter;
        }

        private IconPainter getIconPainter() {
            return iconPainter;
        }

        private void addListener(ActionListener listener) {
            listeners.add(listener);
            if (button != null) {
                button.addActionListener(listener);
            }
        }

        private void bindButton(AbstractButton newButton) {
            this.button = newButton;
            for (ActionListener l : listeners) {
                newButton.addActionListener(l);
            }
        }

        private void repaintButton() {
            if (button != null) {
                button.repaint();
            }
        }
    }

}
