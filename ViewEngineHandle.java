package IllustrationShape;

import IllustrationShape.border.ViewBorder;
import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Moteur d'interaction pur : gère la liste des Shape, la sélection (clic ou
 * survol prolongé), le déplacement, le redimensionnement, la rotation et les
 * ajustements. Ne dessine jamais rien lui-même — c'est le composant hôte (ex.
 * HTextArea) qui appelle Shape.Paint()/paintBorder() où bon lui semble.
 */
public class ViewEngineHandle implements MouseListener, MouseMotionListener {

    private static final int HOVER_DELAY_MS = 400;

    private final JComponent host;
    private final List<HViewShape> shapes = new ArrayList<>();

    private HViewShape selectedShape;
    private ViewBorder previousBorder;

    private ResizeBorder.HandleType activeHandle;
    private Integer activeAdjustmentIndex;

    private boolean movingShape = false;
    private int moveStartMouseX, moveStartMouseY, moveStartShapeX, moveStartShapeY;

    private HViewShape hoveredShape;
    private final Timer hoverTimer;

    public ViewEngineHandle(JComponent host) {
        this.host = host;
        host.addMouseListener(this);
        host.addMouseMotionListener(this);
        hoverTimer = new Timer(HOVER_DELAY_MS, e -> onHoverTimeout());
        hoverTimer.setRepeats(false);
    }

    public void addShape(HViewShape shape) {
        shapes.add(shape);
        host.repaint();
    }

    public List<HViewShape> getShapes() {
        return shapes;
    }

    // Change la forme sélectionnée, en attachant/détachant ResizeBorder au passage
    private void select(HViewShape shape) {
        if (selectedShape != null) {
            selectedShape.setSelected(false);
            selectedShape.setBorder(previousBorder);
        }
        selectedShape = shape;
        if (selectedShape != null) {
            selectedShape.setSelected(true);
            previousBorder = selectedShape.getBorder();
            selectedShape.setBorder(ViewBorderFactory.createResizeBorder(selectedShape));
        }
        host.repaint();
    }

    // Ne sélectionne au survol que si rien n'est déjà sélectionné par clic
    private void onHoverTimeout() {
        if (selectedShape == null && hoveredShape != null) {
            select(hoveredShape);
        }
    }

    private HViewShape findShapeAt(int mx, int my) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (shapes.get(i).containsPoint(mx, my)) {
                return shapes.get(i);
            }
        }
        return null;
    }

    private Integer adjustmentHandleAt(HViewShape shape, int worldMx, int worldMy) {
        for (int i = 0; i < shape.adjustmentCount(); i++) {
            Point2D local = shape.adjustmentHandlePosition(i);
            if (local == null) {
                continue;
            }
            Point2D world = shape.toWorld(local.getX(), local.getY());
            if (Math.abs(world.getX() - worldMx) <= 6 && Math.abs(world.getY() - worldMy) <= 6) {
                return i;
            }
        }
        return null;
    }

    // --- MouseListener ---
    @Override
    public void mousePressed(MouseEvent e) {
        if (selectedShape != null) {
            activeHandle = selectedShape.handleAt(e.getX(), e.getY());
            if (activeHandle != null) {
                return;
            }
            activeAdjustmentIndex = adjustmentHandleAt(selectedShape, e.getX(), e.getY());
            if (activeAdjustmentIndex != null) {
                return;
            }
        }
        HViewShape clicked = findShapeAt(e.getX(), e.getY());
        select(clicked);
        if (selectedShape != null) {
            movingShape = true;
            moveStartMouseX = e.getX();
            moveStartMouseY = e.getY();
            moveStartShapeX = selectedShape.getX();
            moveStartShapeY = selectedShape.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        activeHandle = null;
        activeAdjustmentIndex = null;
        movingShape = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hoveredShape = null;
        hoverTimer.stop();
    }

    // --- MouseMotionListener ---
    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedShape != null && activeHandle != null) {
            activeHandle.drag(selectedShape, e.getX(), e.getY());
        } else if (selectedShape != null && activeAdjustmentIndex != null) {
            selectedShape.applyAdjustmentDrag(activeAdjustmentIndex, e.getX(), e.getY());
        } else if (movingShape && selectedShape != null) {
            int dx = e.getX() - moveStartMouseX;
            int dy = e.getY() - moveStartMouseY;
            selectedShape.setX(moveStartShapeX + dx);
            selectedShape.setY(moveStartShapeY + dy);
        }
        host.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        HViewShape shape = findShapeAt(e.getX(), e.getY());
        if (shape != hoveredShape) {
            hoveredShape = shape;
            hoverTimer.stop();
            if (hoveredShape != null && selectedShape == null) {
                hoverTimer.start();
            }
        }
    }
}
