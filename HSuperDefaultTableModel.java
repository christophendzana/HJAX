package stable;

import hsupertable.*;
import java.awt.Color;
import java.awt.Insets;
import java.util.Vector;

/**
 * HSuperDefaultTableModel — Modèle de données étendu pour HSuperTable.
 * @author FIDELE
 * @version 2.0
 */
public class HSuperDefaultTableModel extends javax.swing.table.DefaultTableModel {

     public HSuperDefaultTableModel() {
        super();
    }

    public HSuperDefaultTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    public HSuperDefaultTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public HSuperDefaultTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
        super(data, columnNames);
    }

    public HSuperDefaultTableModel(Object[] columnNames) {
        super(columnNames, 0);
    }

    public HSuperDefaultTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public HSuperDefaultTableModel(Vector<String> columnNames, int rowCount) {
        super(columnNames, rowCount);
    }
    
}
