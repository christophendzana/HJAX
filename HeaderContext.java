package hsupertable.menu;

import hsupertable.HTable;
import hsupertable.style.HTableStyle.HeaderStyle;
import java.awt.Point;

public class HeaderContext {

    public final int columnIndex;
    public final String columnName;
    public final HeaderStyle headerStyle;
    public final Point mousePosition;
    public final HTable table;

    public HeaderContext(HTable table, int columnIndex, Point mousePosition) {
        this.table = table;
        this.columnIndex = columnIndex;
        this.mousePosition = mousePosition;
        this.columnName = (columnIndex >= 0 && columnIndex < table.getColumnCount())
                ? table.getColumnName(columnIndex)
                : "";
        this.headerStyle = table.getHeaderStyle(columnIndex);
    }

    @Override
    public String toString() {
        return "HeaderContext[col=" + columnIndex + ", name=" + columnName + "]";
    }
}