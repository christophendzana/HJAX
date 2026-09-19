package IllustrationShape.model;

import IllustrationShape.HShapeResizer;
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
    
    private final Set<HView> selectedViews = new LinkedHashSet<>();
    private final List<ListViewSelectionListener> listeners = new ArrayList<>();

    private int selectionMode = MULTIPLE_SELECTION;
    
    //Est-ce qu'une opération de sélection est actuellement en cours ?
    private boolean valueIsAdjusting = false;
    
    //Est-ce que la sélection a effectivement changé pendant cette opération ?
    private boolean changedDuringAdjustment = false;

    private List<HShapeResizer> HShapeResizers;
    
    public AbstractViewModelSelection(){
        HShapeResizers = new ArrayList<>();
    }
    
    @Override
    public boolean addViewSelected(HView view) {
        if (view == null) {
            return false;
        }
        if (selectionMode == SINGLE_SELECTION && !selectedViews.contains(view)) {
            List<HView> previouslySelected = new ArrayList<>(selectedViews);
            selectedViews.clear();
            for (HView removedView : previouslySelected) {
                notifySelectionChanged(removedView);
            }
        }
        boolean added = selectedViews.add(view);
        if (added) {
            notifySelectionChanged(view);
        }
        return added;
    }

    @Override
    public boolean removeViewSelected(HView view) {
        boolean removed = selectedViews.remove(view);
        if (removed) {
            notifySelectionChanged(view);
        }
        return removed;
    }

    @Override
    public HView getViewSelected() {
        HView last = null;
        for (HView view : selectedViews) {
            last = view;
        }
        return last;
    }

    @Override
    public boolean isSelectedView(HView view) {
        return selectedViews.contains(view);
    }

    @Override
    public List<HView> getListViewSelected() {
        return new ArrayList<>(selectedViews);
    }

    @Override
    public void clearSelection() {
        if (selectedViews.isEmpty()) {
            return;
        }
        List<HView> previouslySelected = new ArrayList<>(selectedViews);
        selectedViews.clear();
        for (HView removedView : previouslySelected) {
            notifySelectionChanged(removedView);
        }
    }

    @Override
    public boolean isSelectionEmpty() {
        return selectedViews.isEmpty();
    }

    @Override
    public boolean removeSelectedViews(List<HView> listViews) {
        if (listViews == null || listViews.isEmpty()) {
            return false;
        }
        List<HView> actuallyRemoved = new ArrayList<>();
        for (HView view : listViews) {
            if (selectedViews.remove(view)) {
                actuallyRemoved.add(view);
            }
        }
        for (HView view : actuallyRemoved) {
            notifySelectionChanged(view);
        }
        return !actuallyRemoved.isEmpty();
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
        return selectedViews.size();
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
            fireEvent(new ListViewSelectionEvent(this, null, false));
            changedDuringAdjustment = false;
        }        
    }

    @Override
    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }

    @Override
    public boolean addListSelectionListener(ListViewSelectionListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return false;
        }
        listeners.add(listener);
        return true;
    }

    @Override
    public boolean removeListSelectionListener(ListViewSelectionListener listener) {
        return listeners.remove(listener);
    }

    private void notifySelectionChanged(HView view) {
        if (valueIsAdjusting) {
            changedDuringAdjustment = true;
        }
        fireEvent(new ListViewSelectionEvent(this, view, valueIsAdjusting));
    }

    protected void fireEvent(ListViewSelectionEvent event) {
        if (event == null) {
            return;
        }
        for (ListViewSelectionListener listener : new ArrayList<>(listeners)) {
            listener.valueChanged(event);
        }
    }
}