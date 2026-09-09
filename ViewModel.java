package IllustrationShape.model;

import IllustrationShape.HView;
import java.util.List;

/**
 * Modèle de contenu : gère la collection des HView présentes dans le
 * document (ajout, suppression, recherche), et notifie ses listeners
 * lorsqu'une vue change d'état.
 *
 */
public interface ViewModel {

    void addView(HView view);

    void addView(List<HView> viewList);

    boolean removeView(HView view);

    boolean removeView(int x, int y);

    boolean removeViews(List<HView> listView);

    boolean removeViews(int x, int y);

    HView getView(int x, int y);

    List<HView> getHViews(int x, int y);

    /**
     * Ajoute un listener au ViewModel.
     *
     * @param listener listener à ajouter
     * @return true si le listener a été ajouté, false s'il était déjà présent
     */
    boolean addListener(ViewModelListener listener);

    /**
     * Retire un listener du ViewModel.
     *
     * @param listener listener à retirer
     * @return true si le listener a été retiré, false s'il n'était pas présent
     */
    boolean removeListener(ViewModelListener listener);
}