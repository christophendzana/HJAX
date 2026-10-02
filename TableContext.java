package hsupertable.menu;

import hsupertable.HTable;
import hsupertable.model.Cell;
import hsupertable.geometry.HTableGeometry.InternalCellHit;
import java.awt.Point;

/**
 * TableContext — centralise les informations nécessaires pour décider
 * quelles actions du menu contextuel afficher et comment les exécuter.
 * Déplacé depuis HSuperTable.TableContext sans changement de logique.
 */
public class TableContext {

    public final int row;
    public final int column;
    public final Cell cell;
    public final Cell internalCell;
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
            Cell cell,
            InternalCellHit internalHit,
            Point mousePosition
    ) {
        this.table = table;
        this.row = row;
        this.column = column;
        this.cell = cell;
        this.internalHit = internalHit;
        this.mousePosition = mousePosition;

        this.isMerged = (cell != null) && cell.isMerged();
        this.isAbsorbed = (cell != null) && cell.isAbsorbed();
        this.hasInternalGrid = (cell != null) && cell.hasInternalGrid();

        this.isInternalCell = (internalHit != null && internalHit.parent != null);
        this.internalCell = isInternalCell ? internalHit.cell : null;

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