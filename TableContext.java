package hsupertable.menu;

import hsupertable.HTable;
import hsupertable.geometry.TableGeometry.InternalCellHit;
import java.awt.Point;
import hsupertable.model.structure.CellStructureModel;

/**
 * TableContext — centralise les informations nécessaires pour décider
 * quelles actions du menu contextuel afficher et comment les exécuter.
 * Déplacé depuis HSuperTable.TableContext sans changement de logique.
 *
 * row / column sont des indices VUE (ceux de la souris) ; les questions sur la
 * structure (fusion, subdivision) sont posées au HCellStructureModel en
 * indices MODÈLE.
 */
public class TableContext {

    public final int row;
    public final int column;
    public final InternalCellHit internalHit;
    public final Point mousePosition;
    public final boolean isMerged;
    public final boolean isAbsorbed;
    public final boolean hasInternalGrid;
    public final boolean isInternalCell;
    public final boolean hasMultipleSelection;
    public final HTable table;

    public TableContext(
            HTable table,
            int row,
            int column,
            InternalCellHit internalHit,
            Point mousePosition
    ) {
        this.table = table;
        this.row = row;
        this.column = column;
        this.internalHit = internalHit;
        this.mousePosition = mousePosition;

        int modelRow = table.toModelRow(row);
        int modelCol = table.toModelColumn(column);
        boolean inBounds = modelRow >= 0 && modelCol >= 0;
        CellStructureModel structure = table.getStructureModel();

        this.isMerged = inBounds && structure.isMergeOrigin(modelRow, modelCol);
        this.isAbsorbed = inBounds && structure.isAbsorbed(modelRow, modelCol);
        this.hasInternalGrid = inBounds && !structure.getCellNode(modelRow, modelCol).isLeaf();

        this.isInternalCell = (internalHit != null && internalHit.isSubCell());

        this.hasMultipleSelection = table.hasSelection()
                && !table.getSelection().isSingleCell();
    }

    @Override
    public String toString() {
        return "TableContext[row=" + row + ", col=" + column
                + ", merged=" + isMerged
                + ", internal=" + isInternalCell
                + ", multiSel=" + hasMultipleSelection + "]";
    }
}
