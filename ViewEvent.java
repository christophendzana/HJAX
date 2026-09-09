package IllustrationShape.model;

import IllustrationShape.HView;
import IllustrationShape.model.ViewModel;

/**
 * Événement produit par un ViewModel lorsqu'une vue change d'état ou lorsqu'une
 * opération concernant une vue est effectuée.
 *
 * La sélection n'est volontairement pas représentée ici : elle est portée par
 * son propre canal (ViewModelSelection / ListSelectionViewListener /
 * ListViewSelectionEvent), à la manière dont JTable sépare TableModel de
 * ListSelectionModel.
 */
public class ViewEvent {

    // Gestion des vues
    public static final int VIEW_ADDED = 0;
    public static final int VIEW_REMOVED = 1;

// Survol
    public static final int VIEW_HOVER_ENTER = 2;
    public static final int VIEW_HOVER_EXIT = 3;

// Transformations
    public static final int VIEW_MOVED = 4;
    public static final int VIEW_RESIZED = 5;
    public static final int VIEW_ROTATED = 6;

// Ajustement (poignées jaunes)
    public static final int VIEW_ADJUSTED = 7;

    private final ViewModel source;
    private final HView view;
    private final int type;
    private final boolean isAdjusting;

    /**
     * Construit un événement ponctuel, qui ne fait pas partie d'une série de
     * changements continue (ex. VIEW_ADDED, VIEW_REMOVED, survol).
     */
    public ViewEvent(ViewModel source, HView view, int type) {
        this(source, view, type, false);
    }

    /**
     * Construit un événement en précisant s'il fait partie d'une série de
     * changements continue (ex. glissement de souris en cours pendant un
     * déplacement ou un redimensionnement).
     *
     * @param isAdjusting true si d'autres événements du même type vont suivre
     * immédiatement pour la même opération ; false si c'est l'état final de
     * l'opération.
     */
    public ViewEvent(ViewModel source, HView view, int type, boolean isAdjusting) {
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

    public int getType() {
        return type;
    }

    /**
     * @return true si cet événement fait partie d'une série de changements
     * continue (ex. drag en cours) et que d'autres événements du même type vont
     * suivre ; false si c'est l'état final.
     */
    public boolean isAdjusting() {
        return isAdjusting;
    }
}
