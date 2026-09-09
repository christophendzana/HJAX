package IllustrationShape.model;

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
}