package hsupertable.model.selection;

import java.util.EventObject;

/** Miroir 2D de ListSelectionEvent — s'ajoute aux événements natifs, ne les remplace pas. */
public class CellSelectionEvent extends EventObject {

    private final boolean valueIsAdjusting;

    public CellSelectionEvent(CellSelectionModel source, boolean valueIsAdjusting) {
        super(source);
        this.valueIsAdjusting = valueIsAdjusting;
    }

    @Override
    public CellSelectionModel getSource() {
        return (CellSelectionModel) super.getSource();
    }

    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }
}