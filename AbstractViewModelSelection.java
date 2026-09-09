package IllustrationShape.model;

import IllustrationShape.HView;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implémentation de référence de ViewModelSelection 
 * Stockage de la sélection, respect du mode de sélection, et notification 
 * des listeners avec gestion de valueIsAdjusting.
 *
 */
public abstract class AbstractViewModelSelection implements ViewModelSelection {

    private final Set<HView> selected = new LinkedHashSet<>();
    private final List<ListSelectionViewListener> listeners = new ArrayList<>();

    private int selectionMode = MULTIPLE_SELECTION;
    private boolean valueIsAdjusting = false;
    private boolean changedDuringAdjustment = false;

    @Override
    public boolean addViewSelected(HView view) {
        if (view == null) {
            return false;
        }
        if (selectionMode == SINGLE_SELECTION && !selected.contains(view)) {
            selected.clear();
        }
        boolean added = selected.add(view);
        if (added) {
            notifySelectionChanged(view);
        }
        return added;
    }

    @Override
    public boolean removeViewSelected(HView view) {
        boolean removed = selected.remove(view);
        if (removed) {
            notifySelectionChanged(view);
        }
        return removed;
    }

    @Override
    public HView getViewSelected() {
        HView last = null;
        for (HView view : selected) {
            last = view;
        }
        return last;
    }

    @Override
    public boolean isSelectedView(HView view) {
        return selected.contains(view);
    }

    @Override
    public List<HView> getListViewSelected() {
        return new ArrayList<>(selected);
    }

    @Override
    public void clearSelection() {
        if (selected.isEmpty()) {
            return;
        }
        selected.clear();
        notifySelectionChanged(null);
    }

    @Override
    public boolean isSelectionEmpty() {
        return selected.isEmpty();
    }

    @Override
    public boolean removeSelectedViews(List<HView> listViews) {
        if (listViews == null || listViews.isEmpty()) {
            return false;
        }
        boolean changed = selected.removeAll(listViews);
        if (changed) {
            notifySelectionChanged(null);
        }
        return changed;
    }

    @Override
    public void setSelectionMode(int mode) {
        this.selectionMode = mode;
    }

    @Override
    public int getSelectionMode() {
        return selectionMode;
    }

    @Override
    public int getSelectedViewCount() {
        return selected.size();
    }

    @Override
    public void setValueIsAdjusting(boolean adjusting) {
        if (this.valueIsAdjusting == adjusting) {
            return;
        }
        this.valueIsAdjusting = adjusting;
        if (adjusting) {
            changedDuringAdjustment = false;
        } else if (changedDuringAdjustment) {
            // Événement final caractérisant l'ensemble du changement,
            // comme le fait DefaultListSelectionModel.
            fireEvent(new ListViewSelectionEvent(this, null, false));
            changedDuringAdjustment = false;
        }
    }

    @Override
    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }

    @Override
    public boolean addListSelectionListener(ListSelectionViewListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return false;
        }
        listeners.add(listener);
        return true;
    }

    @Override
    public boolean removeListSelectionListener(ListSelectionViewListener listener) {
        return listeners.remove(listener);
    }

    private void notifySelectionChanged(HView view) {
        if (valueIsAdjusting) {
            changedDuringAdjustment = true;
        }
        fireEvent(new ListViewSelectionEvent(this, view, valueIsAdjusting));
    }

    /**
     * Notifie tous les listeners d'un changement de sélection.
     *
     * @param event événement à transmettre
     */
    protected void fireEvent(ListViewSelectionEvent event) {
        if (event == null) {
            return;
        }
        for (ListSelectionViewListener listener : new ArrayList<>(listeners)) {
            listener.valueChanged(event);
        }
    }
}