package hsupertable.model.structure;

import hsupertable.model.structure.CellStructureEvent;
import java.util.EventListener;

/**
 * Observateur des changements de structure (fusions, subdivisions,
 * décalages). Miroir de HCellSelectionListener : s'ajoute aux événements
 * natifs de Swing, ne les remplace pas.
 */
public interface CellStructureListener extends EventListener {

    void structureChanged(CellStructureEvent e);
}
