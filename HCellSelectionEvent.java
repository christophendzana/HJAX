package hsupertable.model;

import java.util.EventObject;

/** Miroir 2D de ListSelectionEvent — s'ajoute aux événements natifs, ne les remplace pas. */
public class HCellSelectionEvent extends EventObject {

    private final boolean valueIsAdjusting;

    public HCellSelectionEvent(HCellSelectionModel source, boolean valueIsAdjusting) {
        super(source);
        this.valueIsAdjusting = valueIsAdjusting;
    }

    @Override
    public HCellSelectionModel getSource() {
        return (HCellSelectionModel) super.getSource();
    }

    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }
}