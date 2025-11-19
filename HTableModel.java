/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author FIDELE
 */
public class HTableModel extends AbstractTableModel {

    private List<List<Object>> data; // different de Vector<Vector<Object>> dans DefaultTableModel : Thread-Safe / Rapidité
    private List<String> columnNames;
    private List<Class<?>> columnTypes;
    private List<Boolean> editableColumns;        
    
    public HTableModel(){
        this.data = new ArrayList<>();  
        this.columnNames = new ArrayList<>();
        this.columnTypes = new ArrayList<>();
        this.editableColumns = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
       return data.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.size();
    }   

    @Override
    public Object getValueAt(int row, int column) {
        
        if (row>=0  && row < data.size() && column >= 0 && column < columnNames.size()) {
            List<Object> rowData = data.get(row);            
            return rowData.get(column);          
        }
        throw new IllegalArgumentException("Valeur incorrecte");
    }
   
    
    public void addColumn(String columnName, Class<?> columnType, Boolean editable){
        columnNames.add(columnName);
        columnTypes.add(columnType);
        editableColumns.add(editable);
                
        for (List<Object> row : data) {
            row.add(null);
        }
        
        fireTableStructureChanged();        
    }
    
    public void addRow(List<Object> values){
        if (values.size() != columnNames.size() ) {            
            String diff = values.size() > columnNames.size() ? "o" : "";            
            throw new IllegalArgumentException("Nombre de valeur " + diff + " au nombre de valeur attendues" );
        }                          
        
        for (int i=0 ; i <values.size(); i++) {
                        
            Class<?> Type = columnTypes.get(i);
            
            if (values.get(i)!=null && !Type.isInstance(values.get(i)) ) {
                throw new IllegalArgumentException("Type incorrect à la colonne: " +columnNames.get(i) + "."
                + "Type attendu: " + Type.getSimpleName() + "."
                + "Type reçu " + values.get(i).getClass().getSimpleName() + "."
                );
            }            
        }
        
        data.add(new ArrayList<>(values));  // Defense programming       
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }
    
    public void setValueAt(Object value, int row, int column){
        
        if (row >= data.size() || row < 0 ) {
            throw new IllegalArgumentException("ligne invalide");
        } 
        
        if (column >= this.getColumnCount() || column < 0 ) {
            throw new IllegalArgumentException("colonne invalide invalide");
        }
        
        Class<?> Type = columnTypes.get(column);
        
        if (value != null && !Type.isInstance(value)) {
            throw new IllegalArgumentException("Type attendu: " + Type.getSimpleName() + "."
                + "Type reçu " + value.getClass().getSimpleName() + ".");
        }
        
        data.get(row).set(column, value);     
        fireTableCellUpdated(row, column);
        
    }
    
    public boolean isCellEditable(int row, int column){
        return editableColumns.get(column);
    }
    
    public Class<?> getColumnClass(int column){
        return columnTypes.get(column);
    }
    
    public String getColumnName(int column){
        return columnNames.get(column);
    }
    
    public int getIndexColumn(String nameColumn){
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(nameColumn)  ) {
                return i;
            }
        }        
        throw new IllegalArgumentException("Colonne inexistante");        
    }
    
}
