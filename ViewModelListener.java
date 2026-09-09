package IllustrationShape.model;

/**
 * Écoute les événements produits par un ViewModel.
 */
public interface ViewModelListener {

    /**
     * Appelée lorsqu'un événement concernant une vue est produit
     * par le ViewModel.
     *
     * @param e événement produit par le ViewModel
     */
    public void viewChanged(ViewEvent e);
}