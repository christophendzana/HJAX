package IllustrationShape;

import IllustrationShape.HShapeResizer.HandleType;
import IllustrationShape.model.*;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestre les interactions souris sur les HShape d'un DefaultViewModel.
 *
 * @author FIDELE
 */
public final class HShapeInteractionController implements ViewModelListener, ListViewSelectionListener {

    private static final int HOVER_DELAY_MS = 400;

    private JComponent host;
    private ViewModel viewModel;
    private ViewModelSelection selectionModel;
    private final HShapeEditor shapeEditor = new HShapeEditor(); 
    private final HShapeRenderer shapeRenderer = new HShapeRenderer();
    private MouseAdapter adapter;

    private Timer hoverTimer;
    private HShape hoveredShape;

    private HandleType activeHandle;
    private Integer activeAdjustmentIndex;
    private boolean movingShape;
    private HShape draggedShape;
    private ViewEvent.Type draggedEventType;
    private int lastMouseX, lastMouseY;

    private int dragOriginalX, dragOriginalY, dragOriginalWidth, dragOriginalHeight;
    private Point2D dragAnchorWorld;

    public HShapeInteractionController(JComponent host) {
        this(host, null, null);
    }

    public HShapeInteractionController(JComponent host, ViewModel model) {
        this(host, model, null);
    }

    public HShapeInteractionController(JComponent host, ViewModel model, ViewModelSelection selectionModel) {
        
        if (host == null) {
            throw new IllegalArgumentException("host cannot be null");
        }
        this.host = host;
       ;
        this.viewModel = (model != null) ? model : new DefaultViewModel();
        this.selectionModel = (selectionModel != null) ? selectionModel : new DefaultViewModelSelection();

        this.hoverTimer = new Timer(HOVER_DELAY_MS, e -> onHoverTimeout());
        this.hoverTimer.setRepeats(false);

        this.adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onPress(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onDrag(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onRelease(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                onMove(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onExit(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick(e);
            }
        };
        host.addMouseListener(adapter);
        host.addMouseMotionListener(adapter);
        this.viewModel.addListener(this);
        this.selectionModel.addListSelectionListener(this);
    }

    /**
     * Remplace le ViewModel en cours d'utilisation.
     *     
     */
    public void setViewModel(ViewModel model) {
        if (model == null) {
            throw new IllegalArgumentException("viewModel cannot be null");
        }
        if (model == this.viewModel) {
            return;
        }
        this.viewModel.removeListener(this);
        this.viewModel = model;
        this.viewModel.addListener(this);
        host.repaint();
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Remplace le ViewModelSelection en cours d'utilisation. 
     */
    public void setSelectionModel(ViewModelSelection model) {
        if (model == null) {
            throw new IllegalArgumentException("viewModelSelection cannot be null");
        }
        if (model == this.selectionModel) {
            return;
        }
        this.selectionModel.removeListSelectionListener(this);
        this.selectionModel = model;
        this.selectionModel.addListSelectionListener(this);
        host.repaint();
    }

    public ViewModelSelection getSelectionModel() {
        return selectionModel;
    }

    public JComponent getHost() {
        return host;
    }

    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();

        for (HShape shape : getViewModel().getShapes()) {
            shape.Paint(g2, 0, 0);

            if (shape instanceof HTextContent content && shape != getShapeEditor().getEditingShape()) {
                Graphics2D textG = (Graphics2D) g2.create();
                textG.rotate(Math.toRadians(shape.getRotationDegrees()), shape.getCenterX(), shape.getCenterY());
                textG.translate(shape.getX(), shape.getY());
                getShapeRenderer().getRendererComponent(shape, content).paint(textG);
                textG.dispose();
            }
        }
        for (HShapeResizer resizer : getShapeResizers()) {
            resizer.Paint(g2, 0, 0);
        }

        g2.dispose();
    }

    public void addView(HView view) {
        if (view == null) {
            throw new IllegalArgumentException("view cannot be null");
        }
        viewModel.addView(view);
    }

    @Override
    public void viewChanged(ViewEvent e) {
        host.repaint();
    }

    @Override
    public void valueChanged(ListViewSelectionEvent e) {
        for (HShape shape : viewModel.getShapes()) {
            shape.setSelected(selectionModel.isSelectedView(shape));
        }
        host.repaint();
    }

    /**
     * À appeler par le UI delegate quand le contrôleur n'est plus utilisé.
     *     
     */
    public void dispose() {
        host.removeMouseListener(adapter);
        host.removeMouseMotionListener(adapter);
        hoverTimer.stop();
        viewModel.removeListener(this);
        selectionModel.removeListSelectionListener(this);
    }

    public HShapeEditor getShapeEditor() {
        return shapeEditor;
    }

    public HShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    /**
     * Reconstruite à la demande depuis la sélection — rien n'est mis en cache.
     */
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

    private void onPress(MouseEvent e) {
        for (HShapeResizer resizer : getShapeResizers()) {
            HandleType handle = resizer.handleAt(e.getX(), e.getY());
            if (handle != null) {
                activeHandle = handle;
                draggedShape = resizer.getTargetShape();
                captureResizeStart(draggedShape, handle);
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

    private void onDrag(MouseEvent e) {
        if (draggedShape != null && activeHandle != null) {
            int beforeX = draggedShape.getX();
            int beforeY = draggedShape.getY();
            int beforeWidth = draggedShape.getWidth();
            int beforeHeight = draggedShape.getHeight();
            double beforeRotation = draggedShape.getRotationDegrees();

            activeHandle.drag(draggedShape, dragOriginalX, dragOriginalY, dragOriginalWidth, dragOriginalHeight,
                    dragAnchorWorld, e.getX(), e.getY());

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

    private void onRelease(MouseEvent e) {
        if (draggedShape != null && draggedEventType != null) {
            viewModel.notifyViewEvent(draggedShape, draggedEventType, false);
        }
        activeHandle = null;
        activeAdjustmentIndex = null;
        movingShape = false;
        draggedShape = null;
        draggedEventType = null;
    }

    private void onMove(MouseEvent e) {
        updateHoveredShape(viewModel.findShapeAt(e.getX(), e.getY()));
    }

    private void onExit(MouseEvent e) {
        updateHoveredShape(null);
        hoverTimer.stop();
    }

    private void onClick(MouseEvent e) {
        if (e.getClickCount() != 2) {
            return;
        }
        HShape shape = viewModel.findShapeAt(e.getX(), e.getY());
        if (shape instanceof HTextContent) {
            shapeEditor.startEditing(shape, host);
        }
    }

    private void captureResizeStart(HShape shape, HandleType handle) {
        dragOriginalX = shape.getX();
        dragOriginalY = shape.getY();
        dragOriginalWidth = shape.getWidth();
        dragOriginalHeight = shape.getHeight();
        if (handle != HandleType.ROTATE) {
            HandleType anchor = handle.opposite();
            Point2D anchorLocal = anchor.localPosition(dragOriginalX, dragOriginalY, dragOriginalWidth, dragOriginalHeight);
            dragAnchorWorld = shape.toWorld(anchorLocal.getX(), anchorLocal.getY());
        }
    }
}
