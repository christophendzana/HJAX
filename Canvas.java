package illustrations.shapes;

import illustrations.model.Adjustable;
import illustrations.model.GraphicObject;
import illustrations.model.GraphicTransform;
import illustrations.shapes.HandleType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant Swing affichant et manipulant des GraphicObject : sélection, déplacement,
 * redimensionnement, rotation, ajustement de paramètres — sans jamais connaître Shapes ni ShapeType.
 */
public class Canvas extends JPanel implements MouseListener, MouseMotionListener {

    private static final double ROTATE_HANDLE_DISTANCE = 24;

    private final List<GraphicObject> objects = new ArrayList<>();
    private GraphicObject selectedObject;
    private HandleType activeHandle;
    private Integer activeAdjustmentIndex;

    private boolean movingObject = false;
    private int moveStartMouseX, moveStartMouseY, moveStartObjectX, moveStartObjectY;

    public Canvas() {
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void addObject(GraphicObject object) {
        objects.add(object);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (GraphicObject object : objects) {
            object.paint(g2d);
            if (object.isSelected()) paintHandles(g2d, object);
        }
    }

    // Dessine les poignées blanches (redim./rotation) et jaunes (ajustements) d'un objet sélectionné, génériquement
    private void paintHandles(Graphics2D g, GraphicObject object) {
        GraphicTransform t = object.getTransform();
        AffineTransform saved = g.getTransform();
        g.rotate(Math.toRadians(t.getRotationDegrees()), t.getCenterX(), t.getCenterY());

        g.setColor(Color.GRAY);
        g.draw(new Line2D.Double(t.getCenterX(), t.getY(), t.getCenterX(), t.getY() - ROTATE_HANDLE_DISTANCE));

        for (HandleType handle : HandleType.values()) {
            Point2D p = handle.localPosition(t);
            if (handle == HandleType.ROTATE) {
                g.setColor(Color.WHITE);
                Ellipse2D circle = new Ellipse2D.Double(p.getX() - 5, p.getY() - 5, 10, 10);
                g.fill(circle); g.setColor(Color.BLACK); g.draw(circle);
            } else {
                g.setColor(Color.WHITE);
                Rectangle2D square = new Rectangle2D.Double(p.getX() - 4, p.getY() - 4, 8, 8);
                g.fill(square); g.setColor(Color.BLACK); g.draw(square);
            }
        }

        if (object instanceof Adjustable adjustable) {
            for (int i = 0; i < adjustable.adjustmentCount(); i++) {
                Point2D p = adjustable.adjustmentHandlePosition(i);
                if (p == null) continue;
                g.setColor(Color.YELLOW);
                Ellipse2D dot = new Ellipse2D.Double(p.getX() - 4, p.getY() - 4, 8, 8);
                g.fill(dot); g.setColor(Color.BLACK); g.draw(dot);
            }
        }

        g.setTransform(saved);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (selectedObject != null) {
            activeHandle = HandleType.at(selectedObject.getTransform(), e.getX(), e.getY());
            if (activeHandle != null) return;

            if (selectedObject instanceof Adjustable adjustable) {
                activeAdjustmentIndex = adjustmentHandleAt(adjustable, selectedObject.getTransform(), e.getX(), e.getY());
                if (activeAdjustmentIndex != null) return;
            }
        }
        GraphicObject clicked = findObjectAt(e.getX(), e.getY());
        if (selectedObject != null) selectedObject.setSelected(false);
        selectedObject = clicked;
        if (selectedObject != null) {
            selectedObject.setSelected(true);
            movingObject = true;
            moveStartMouseX = e.getX();
            moveStartMouseY = e.getY();
            moveStartObjectX = selectedObject.getTransform().getX();
            moveStartObjectY = selectedObject.getTransform().getY();
        }
        repaint();
    }

    // Détecte un clic sur une poignée jaune d'un objet ajustable, indépendamment de sa nature concrète
    private Integer adjustmentHandleAt(Adjustable adjustable, GraphicTransform t, int worldMx, int worldMy) {
        for (int i = 0; i < adjustable.adjustmentCount(); i++) {
            Point2D local = adjustable.adjustmentHandlePosition(i);
            if (local == null) continue;
            Point2D world = t.toWorld(local.getX(), local.getY());
            if (Math.abs(world.getX() - worldMx) <= 6 && Math.abs(world.getY() - worldMy) <= 6) return i;
        }
        return null;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        activeHandle = null;
        activeAdjustmentIndex = null;
        movingObject = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedObject != null && activeHandle != null) {
            activeHandle.drag(selectedObject.getTransform(), e.getX(), e.getY());
        } else if (selectedObject != null && activeAdjustmentIndex != null && selectedObject instanceof Adjustable adjustable) {
            adjustable.applyAdjustmentDrag(activeAdjustmentIndex, e.getX(), e.getY());
        } else if (movingObject && selectedObject != null) {
            int dx = e.getX() - moveStartMouseX;
            int dy = e.getY() - moveStartMouseY;
            selectedObject.getTransform().setX(moveStartObjectX + dx);
            selectedObject.getTransform().setY(moveStartObjectY + dy);
        }
        repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    private GraphicObject findObjectAt(int mx, int my) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (objects.get(i).containsPoint(mx, my)) return objects.get(i);
        }
        return null;
    }
}