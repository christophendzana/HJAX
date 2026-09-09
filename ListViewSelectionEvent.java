package IllustrationShape.model;

import IllustrationShape.HView;
import java.util.EventObject;

/**
 * Événement produit par un ViewModelSelection lorsque la sélection change.
 *
 */
public class ListViewSelectionEvent extends EventObject {

    private final HView view;
    private final boolean isAdjusting;

    /**
     * @param source le ViewModelSelection à l'origine de l'événement
     * @param view la HView concernée par ce changement, ou null lorsque
     * l'événement caractérise un changement global (ex. clearSelection(), ou
     * l'événement final envoyé à la fin d'une série d'ajustements)
     * @param isAdjusting true si ce changement fait partie d'une série de
     * changements continue (ex. drag en cours)
     */
    public ListViewSelectionEvent(ViewModelSelection source, HView view, boolean isAdjusting) {
        super(source);
        this.view = view;
        this.isAdjusting = isAdjusting;
    }

    @Override
    public ViewModelSelection getSource() {
        return (ViewModelSelection) super.getSource();
    }

    public HView getView() {
        return view;
    }

    public boolean isAdjusting() {
        return isAdjusting;
    }
}
