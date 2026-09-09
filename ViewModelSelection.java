package IllustrationShape.model;

import IllustrationShape.HView;
import java.util.List;

/**
 * Modèle de sélection des HView, indépendant du contenu.
 *
 */
public interface ViewModelSelection {

    /** Un seul élément peut être sélectionné à la fois. */
    int SINGLE_SELECTION = 0;

    /** Plusieurs éléments peuvent être sélectionnés simultanément. */
    int MULTIPLE_SELECTION = 1;

    boolean addViewSelected(HView view);

    boolean removeViewSelected(HView view);

    HView getViewSelected();

    boolean isSelectedView(HView view);

    List<HView> getListViewSelected();

    void clearSelection();

    boolean isSelectionEmpty();

    boolean removeSelectedViews(List<HView> listViews);

    void setSelectionMode(int mode);

    int getSelectionMode();

    int getSelectedViewCount();

    /**
     * Indique si les changements de sélection à venir doivent être
     * considérés comme une seule et même opération (ex. glissement de
     * souris pendant une sélection rectangle). Permet aux listeners de
     * n'agir que sur le changement final plutôt que sur chaque état
     * intermédiaire.
     *
     * @param adjusting true pour démarrer une série de changements,
     *                  false pour la clôturer
     */
    void setValueIsAdjusting(boolean adjusting);

    boolean getValueIsAdjusting();

    boolean addListSelectionListener(ListSelectionViewListener listener);

    boolean removeListSelectionListener(ListSelectionViewListener listener);
}