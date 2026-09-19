package IllustrationShape.model;

import IllustrationShape.HShape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Prend en charge l'enregistrement des listeners et la notification des
 * événements pour un ViewModel. 
 */
public abstract class AbstractViewModel implements ViewModel {

    private final List<ViewModelListener> listeners = new ArrayList<>();

    @Override
    public boolean addListener(ViewModelListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return false;
        }
        listeners.add(listener);
        return true;
    }

    @Override
    public boolean removeListener(ViewModelListener listener) {
        return listeners.remove(listener);
    }   

    /**
     * Notifie tous les listeners d'un événement.
     *
     * @param event événement à transmettre
     */
    protected void fireEvent(ViewEvent event) {
        if (event == null) {
            return;
        }
        for (ViewModelListener listener : new ArrayList<>(listeners)) {
            listener.viewChanged(event);
        }
    }    
    
    // ---------------------------------------------------------
    // Notification des transformations.    
    // ---------------------------------------------------------
    @Override
    public void viewMoved(HShape shape, boolean isAdjusting) {
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_MOVED, isAdjusting));
    }

    @Override
    public void viewResized(HShape shape, boolean isAdjusting) {
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_RESIZED, isAdjusting));
    }

    @Override
    public void viewRotated(HShape shape, boolean isAdjusting) {
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_ROTATED, isAdjusting));
    }

    @Override
    public void viewAdjusted(HShape shape, boolean isAdjusting) {
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_ADJUSTED, isAdjusting));
    }

    @Override
    public void viewHoverEntered(HShape shape) {
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_HOVER_ENTER));
    }

    @Override
    public void viewHoverExited(HShape shape) {
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_HOVER_EXIT));
    }

    /**
     * Cas générique, utile quand le type vient de
     * ViewHandle.getActionEventType().
     */
    @Override
    public void notifyViewEvent(HShape shape, ViewEvent.Type type, boolean isAdjusting) {
        fireEvent(new ViewEvent(this, shape, type, isAdjusting));
    }

    // ---------------------------------------------------------
    // Hit-test réutilisable — évite que chaque hôte réimplémente sa
    // propre recherche dans la collection de formes.
    // ---------------------------------------------------------
    
    
    public Integer adjustmentHandleAt(HShape shape, int worldMx, int worldMy) {
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
    
}