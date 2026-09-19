package IllustrationShape.model;

import IllustrationShape.HView;
import IllustrationShape.model.ViewModel;
import javax.swing.ListSelectionModel;

/**
 * Événement produit par un ViewModel lorsqu'une vue change d'état
 * ou lorsqu'une opération concernant une vue est effectuée.
 *
 * La sélection n'est volontairement pas représentée ici : elle est portée
 * par son propre canal (ViewModelSelection / ListSelectionViewListener /
 * ListViewSelectionEvent), à la manière dont JTable sépare TableModel
 * de ListSelectionModel.
 */
public class ViewEvent {

    public enum Type {
        // Gestion des view
        VIEW_ADDED,
        VIEW_REMOVED,

        // Survol
        VIEW_HOVER_ENTER,
        VIEW_HOVER_EXIT,

        // Transformations
        VIEW_MOVED,
        VIEW_RESIZED,
        VIEW_ROTATED,

        // Ajustement (poignées jaunes)
        VIEW_ADJUSTED
    }

    private final ViewModel source;
    private final HView view;
    private final Type type;
    private final boolean isAdjusting;

    /**
     * Construit un événement ponctuel, qui ne fait pas partie d'une série
     * de changements continue (ex. VIEW_ADDED, VIEW_REMOVED, survol).
     */
    public ViewEvent(ViewModel source, HView view, Type type) {
        this(source, view, type, false);
    }

    /**
     * Construit un événement en précisant s'il fait partie d'une série de
     * changements continue (ex. glissement de souris en cours pendant un
     * déplacement ou un redimensionnement).
     *
     * @param isAdjusting true si d'autres événements du même type vont
     *                    suivre immédiatement pour la même opération ;
     *                    false si c'est l'état final de l'opération.
     */
    public ViewEvent(ViewModel source, HView view, Type type, boolean isAdjusting) {
        this.source = source;
        this.view = view;
        this.type = type;
        this.isAdjusting = isAdjusting;        
    }

    public ViewModel getSource() {
        return source;
    }

    public HView getView() {
        return view;
    }

    public Type getType() {
        return type;
    }

    /**
     * @return true si cet événement fait partie d'une série de changements
     * continue (ex. drag en cours) et que d'autres événements du même type
     * vont suivre ; false si c'est l'état final.
     */
    public boolean isAdjusting() {
        return isAdjusting;
    }
}