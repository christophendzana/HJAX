package hsplitpane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * Aucun changement structurel ici — cette classe était déjà bien conçue
 * (responsabilité unique, découplage propre via Consumer&lt;Integer&gt;, voir
 * l'analyse initiale). Seul correctif : setCouleurHover()/setCouleurDrag() ne
 * déclenchaient pas de repaint(), contrairement à setCouleur(), ce qui pouvait
 * laisser une couleur de survol/drag obsolète affichée si elle était changée
 * pendant que la souris était déjà dessus.
 */
public class HSplitDivider extends JPanel {

    private static final int DEFAULT_THICKNESS = 4;

    private static final Color DEFAULT_COLOR = new Color(50, 50, 50);

    private static final Color DEFAULT_HOVER_COLOR = new Color(80, 130, 200);

    private static final Color DEFAULT_DRAG_COLOR = new Color(100, 160, 230);

    private final WrapDirection orientation;

    private int epaisseur;

    private Color color;

    private Color hoverColor;

    private Color dragColor;

    private boolean isDragging;

    private boolean isHovered;

    private boolean locked;

    private int dragStartX;

    private int dragStartY;

    private Consumer<Integer> onDragCallback;

    public HSplitDivider(WrapDirection orientation) {
        this(orientation, DEFAULT_THICKNESS, DEFAULT_COLOR,
                DEFAULT_HOVER_COLOR, DEFAULT_DRAG_COLOR);
    }

    public HSplitDivider(WrapDirection orientation, int epaisseur,
            Color color, Color hoverColor, Color dragColor) {
        this.orientation = orientation;
        this.epaisseur = epaisseur;
        this.color = color;
        this.hoverColor = hoverColor;
        this.dragColor = dragColor;
        this.locked = false;

        configureCursor();
        installMouseListeners();
    }

    private void configureCursor() {
        if (!locked) {
            if (orientation == WrapDirection.HORIZONTAL) {
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            }
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void installMouseListeners() {
        MouseAdapter adaptateur = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!locked) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!locked) {
                    isDragging = true;
                    dragStartX = e.getXOnScreen();
                    dragStartY = e.getYOnScreen();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!locked && isDragging && onDragCallback != null) {

                    int delta;
                    if (orientation == WrapDirection.HORIZONTAL) {
                        delta = e.getYOnScreen() - dragStartY;
                        dragStartY = e.getYOnScreen();
                    } else {
                        delta = e.getXOnScreen() - dragStartX;
                        dragStartX = e.getXOnScreen();
                    }

                    if (delta != 0) {
                        onDragCallback.accept(delta);
                    }
                }
            }
        };

        addMouseListener(adaptateur);
        addMouseMotionListener(adaptateur);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isDragging) {
            g.setColor(dragColor);
        } else if (isHovered) {
            g.setColor(hoverColor);
        } else {
            g.setColor(color);
        }

        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setOnDragCallback(Consumer<Integer> callback) {
        this.onDragCallback = callback;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        configureCursor();
        repaint();
    }

    public WrapDirection getOrientation() {
        return orientation;
    }

    public int getEpaisseur() {
        return epaisseur;
    }

    public void setEpaisseur(int epaisseur) {
        this.epaisseur = epaisseur;
        revalidate();
        repaint();
    }

    public Color getCouleur() {
        return color;
    }

    public void setCouleur(Color color) {
        this.color = color;
        repaint();
    }

    public Color getCouleurHover() {
        return hoverColor;
    }

    public void setCouleurHover(Color hoverColor) {
        this.hoverColor = hoverColor;
        repaint();
    }

    public Color getCouleurDrag() {
        return dragColor;
    }

    public void setCouleurDrag(Color dragColor) {
        this.dragColor = dragColor;
        repaint();
    }
}
