package IllustrationShape;

import IllustrationShape.HShapeResizer.HandleType;
import IllustrationShape.model.DefaultViewModel;
import IllustrationShape.model.HTextContent;
import IllustrationShape.model.ViewEvent;
import IllustrationShape.model.ViewModelSelection;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestre les interactions souris sur les HShape d'un DefaultViewModel :
 * hover, sélection (simple/Ctrl+clic), déplacement, redimensionnement,
 * rotation, édition de texte par double-clic.
 *
 * Ne définit jamais elle-même comment une forme réagit à une poignée
 * précise — cette logique reste entièrement dans HShapeResizer/HandleType.
 * Cette classe se contente de détecter l'intention (quelle poignée, quelle
 * forme), déléguer le calcul, puis diffuser le résultat aux autres formes
 * sélectionnées en cas de sélection multiple.
 *
 * Réutilisable par n'importe quel JComponent hôte — ne connaît que
 * DefaultViewModel et ViewModelSelection, rien de spécifique à HTextArea.
 */
public class HShapeInteractionController implements MouseListener, MouseMotionListener {

    private static final int HOVER_DELAY_MS = 400;

    private final JComponent host;
    private final DefaultViewModel viewModel;
    private final ViewModelSelection selectionModel;
    private final HShapeEditor shapeEditor = new HShapeEditor();

    private final Timer hoverTimer;
    private HShape hoveredShape;

    private HandleType activeHandle;
    private Integer activeAdjustmentIndex;
    private boolean movingShape;
    private HShape draggedShape;
    private ViewEvent.Type draggedEventType;
    private int lastMouseX, lastMouseY;

    public HShapeInteractionController(JComponent host, DefaultViewModel viewModel, ViewModelSelection selectionModel) {
        this.host = host;
        this.viewModel = viewModel;
        this.selectionModel = selectionModel;
        this.hoverTimer = new Timer(HOVER_DELAY_MS, e -> onHoverTimeout());
        this.hoverTimer.setRepeats(false);
    }

    public HShapeEditor getShapeEditor() {
        return shapeEditor;
    }

    /** Reconstruite à la demande depuis la sélection — rien n'est mis en cache. */
    public List<HShapeResizer> getShapeResizers() {
        List<HShapeResizer> resizers = new ArrayList<>();
        for (HView view : selectionModel.getListViewSelected()) {
            if (view instanceof HShape shape) {
                HShapeResizer resizer = new HShapeResizer();
                resizer.setTargetShape(shape);
                resizers.add(resizer);
            }
        }
        return resizers;
    }

    private HShape getSelectedShape() {
        return (selectionModel.getViewSelected() instanceof HShape s) ? s : null;
    }

    private void updateHoveredShape(HShape shape) {
        if (shape == hoveredShape) {
            return;
        }
        if (hoveredShape != null) {
            viewModel.viewHoverExited(hoveredShape);
        }
        hoveredShape = shape;
        hoverTimer.stop();
        if (hoveredShape != null) {
            viewModel.viewHoverEntered(hoveredShape);
            if (selectionModel.isSelectionEmpty()) {
                hoverTimer.start();
            }
        }
    }

    private void onHoverTimeout() {
        if (selectionModel.isSelectionEmpty() && hoveredShape != null) {
            selectionModel.addViewSelected(hoveredShape);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (HShapeResizer resizer : getShapeResizers()) {
            HandleType handle = resizer.handleAt(e.getX(), e.getY());
            if (handle != null) {
                activeHandle = handle;
                draggedShape = resizer.getTargetShape();
                return;
            }
        }

        HShape primary = getSelectedShape();
        if (primary != null) {
            Integer index = viewModel.adjustmentHandleAt(primary, e.getX(), e.getY());
            if (index != null) {
                activeAdjustmentIndex = index;
                draggedShape = primary;
                return;
            }
        }

        HShape clicked = viewModel.findShapeAt(e.getX(), e.getY());

        if (clicked == null) {
            selectionModel.setSelectionMode(ViewModelSelection.SINGLE_SELECTION);
            selectionModel.clearSelection();
        } else if (e.isControlDown()) {
            if (selectionModel.getSelectionMode() == ViewModelSelection.SINGLE_SELECTION) {
                selectionModel.setSelectionMode(ViewModelSelection.MULTIPLE_SELECTION);
            }
            if (selectionModel.isSelectedView(clicked)) {
                selectionModel.removeViewSelected(clicked);
            } else {
                selectionModel.addViewSelected(clicked);
            }
        } else if (!selectionModel.isSelectedView(clicked)) {
            selectionModel.setSelectionMode(ViewModelSelection.SINGLE_SELECTION);
            selectionModel.clearSelection();
            selectionModel.addViewSelected(clicked);
        }

        if (!selectionModel.isSelectionEmpty()) {
            movingShape = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedShape != null && activeHandle != null) {
            int beforeX = draggedShape.getX();
            int beforeY = draggedShape.getY();
            int beforeWidth = draggedShape.getWidth();
            int beforeHeight = draggedShape.getHeight();
            double beforeRotation = draggedShape.getRotationDegrees();

            activeHandle.drag(draggedShape, e.getX(), e.getY());

            int dx = draggedShape.getX() - beforeX;
            int dy = draggedShape.getY() - beforeY;
            int dWidth = draggedShape.getWidth() - beforeWidth;
            int dHeight = draggedShape.getHeight() - beforeHeight;
            double dRotation = draggedShape.getRotationDegrees() - beforeRotation;

            for (HView view : selectionModel.getListViewSelected()) {
                if (view instanceof HShape shape && shape != draggedShape) {
                    shape.setX(shape.getX() + dx);
                    shape.setY(shape.getY() + dy);
                    shape.setWidth(shape.getWidth() + dWidth);
                    shape.setHeight(shape.getHeight() + dHeight);
                    shape.setRotationDegrees(shape.getRotationDegrees() + dRotation);
                }
            }

            draggedEventType = activeHandle.getActionEventType();

        } else if (draggedShape != null && activeAdjustmentIndex != null) {
            draggedShape.applyAdjustmentDrag(activeAdjustmentIndex, e.getX(), e.getY());
            draggedEventType = ViewEvent.Type.VIEW_ADJUSTED;

        } else if (movingShape) {
            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            for (HView view : selectionModel.getListViewSelected()) {
                if (view instanceof HShape shape) {
                    shape.setX(shape.getX() + dx);
                    shape.setY(shape.getY() + dy);
                }
            }

            draggedShape = getSelectedShape();
            draggedEventType = ViewEvent.Type.VIEW_MOVED;
        }

        if (draggedShape != null && draggedEventType != null) {
            viewModel.notifyViewEvent(draggedShape, draggedEventType, true);
        }
        host.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggedShape != null && draggedEventType != null) {
            viewModel.notifyViewEvent(draggedShape, draggedEventType, false);
        }
        activeHandle = null;
        activeAdjustmentIndex = null;
        movingShape = false;
        draggedShape = null;
        draggedEventType = null;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateHoveredShape(viewModel.findShapeAt(e.getX(), e.getY()));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        updateHoveredShape(null);
        hoverTimer.stop();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
            return;
        }
        HShape shape = viewModel.findShapeAt(e.getX(), e.getY());
        if (shape instanceof HTextContent) {
            shapeEditor.startEditing(shape, host);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }
}