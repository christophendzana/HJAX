package IllustrationShape.model;

import IllustrationShape.HShape;
import IllustrationShape.HView;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation de référence de ViewModel : gère uniquement la collection de
 * HShape et notifie les changements de contenu/géométrie.
 *
 * Ne connaît ni composant hôte, ni sélection, ni souris — exactement comme
 * DefaultTableModel ne connaît ni JTable ni ListSelectionModel. Le composant
 * hôte composera ce modèle et un ViewModelSelection en tant que frères.
 */
public class DefaultViewModel extends AbstractViewModel {

    private final List<HShape> shapes = new ArrayList<>();

    @Override
    public void addView(HView view) {
        if (!(view instanceof HShape shape)) {
            return;
        }
        shapes.add(shape);
        fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_ADDED));
    }

    @Override
    public void addView(List<HView> viewList) {
        if (viewList == null) {
            return;
        }
        for (HView view : viewList) {
            addView(view);
        }
    }

    @Override
    public boolean removeView(HView view) {
        if (!(view instanceof HShape shape)) {
            return false;
        }
        boolean removed = shapes.remove(shape);
        if (removed) {
            fireEvent(new ViewEvent(this, shape, ViewEvent.Type.VIEW_REMOVED));
        }
        return removed;
    }

    @Override
    public boolean removeView(int x, int y) {
        HView view = getView(x, y);
        return view != null && removeView(view);
    }

    @Override
    public boolean removeViews(List<HView> listView) {
        if (listView == null) {
            return false;
        }
        boolean removed = false;
        for (HView view : new ArrayList<>(listView)) {
            removed |= removeView(view);
        }
        return removed;
    }

    @Override
    public boolean removeViews(int x, int y) {
        return removeViews(getHViews(x, y));
    }

    @Override
    public HView getView(int x, int y) {
        return findShapeAt(x, y);
    }

    @Override
    public List<HView> getHViews(int x, int y) {
        List<HView> result = new ArrayList<>();
        for (int i = shapes.size() - 1; i >= 0; i--) {
            HShape shape = shapes.get(i);
            if (shape.containsPoint(x, y)) {
                result.add(shape);
            }
        }
        return result;
    }

    public List<HShape> getShapes() {
        return shapes;
    }

    @Override
    public HShape findShapeAt(int mx, int my) {
        for (int i = shapes.size() - 1; i >= 0; i--) {
            HShape shape = shapes.get(i);
            if (shape.containsPoint(mx, my)) {
                return shape;
            }
        }
        return null;
    }

}
