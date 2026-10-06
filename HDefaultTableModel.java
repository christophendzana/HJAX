package hsupertable.model;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * HDefaultTableModel  
 *
 * @author FIDELE
 * @version 1.0
 */
public class HDefaultTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    public HDefaultTableModel() {
        super();
    }

    public HDefaultTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    public HDefaultTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public HDefaultTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
        super(data, columnNames);
    }

    public HDefaultTableModel(Object[] columnNames) {
        super(columnNames, 0);
    }

    public HDefaultTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public HDefaultTableModel(Vector<String> columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    // =========================================================================
    // AJOUT / SUPPRESSION DE LIGNES (événements Swing standard : HTable les suit)
    // =========================================================================
    /**
     * Ajoute une ligne vide à la fin.
     */
    public void addEmptyRow() {
        addRow(new Object[getColumnCount()]);
    }

    /**
     * Insère une ligne vide à l'indice donné.
     */
    public void insertEmptyRow(int row) {
        insertRow(row, new Object[getColumnCount()]);
    }

    /**
     * Supprime toutes les lignes (les colonnes sont conservées).
     */
    public void clear() {
        setRowCount(0);
    }

    // =========================================================================
    // LECTURE DES DONNÉES
    // =========================================================================
    public Object[][] getAllData() {
        int rows = getRowCount();
        Object[][] data = new Object[rows][];
        for (int r = 0; r < rows; r++) {
            data[r] = getRowData(r);
        }
        return data;
    }

    public Object[] getRowData(int row) {
        int cols = getColumnCount();
        Object[] data = new Object[cols];
        for (int c = 0; c < cols; c++) {
            data[c] = getValueAt(row, c);
        }
        return data;
    }

    public Object[] getColumnData(int col) {
        int rows = getRowCount();
        Object[] data = new Object[rows];
        for (int r = 0; r < rows; r++) {
            data[r] = getValueAt(r, col);
        }
        return data;
    }

    /**
     * Vrai si la cellule est null ou ne contient qu'un texte vide.
     */
    public boolean isCellEmpty(int row, int col) {
        Object value = getValueAt(row, col);
        if (value == null) {
            return true;
        }
        return value instanceof String s && s.trim().isEmpty();
    }

    /**
     * Nombre de cellules non vides dans la colonne.
     */
    public int countNonEmptyCells(int col) {
        int count = 0;
        for (int r = 0; r < getRowCount(); r++) {
            if (!isCellEmpty(r, col)) {
                count++;
            }
        }
        return count;
    }
}