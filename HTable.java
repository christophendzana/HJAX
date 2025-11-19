/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HTableController;
import hcomponents.models.HTableModel;
import hcomponents.vues.HBasicTableUI;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author FIDELE
 */
public class HTable extends JTable {
    
   private final HTableModel hModel;
    private final HTableController controller;

    // États visuels
    private int highlightedRow = -1;
    private int hoveredRow = -1;
    private Set<Integer> selectedRows = new HashSet<>();

    private int focusedRow = -1;
    private int focusedColumn = -1;
    private boolean isEditing = false;

    private final Map<Integer, Color> rowBackgroundColors = new HashMap<>();
    private final Map<Integer, Color> rowForegroundColors = new HashMap<>();

    // État générique
    private final Map<String, Object> visualStates = new HashMap<>();
    
    public HTable(HTableModel model) {
        super(model);
        this.hModel = model;
        this.controller = new HTableController(this);

        setUI(new HBasicTableUI());
        initializeTable();
    }
    
    private void initializeTable() {
        setRowHeight(36);
        setShowHorizontalLines(true);
        setShowVerticalLines(false);
        setGridColor(new Color(240, 240, 240));
        setSelectionBackground(new Color(220, 240, 255));
        setSelectionForeground(Color.BLACK);
    }
    
    // ─────────────────────────────────────────────
    //               ÉTATS VISUELS
    // ─────────────────────────────────────────────
    
    // Surbrillance
    public void setHighlightedRow(int row) {
        this.highlightedRow = row;
        repaint();
    }
    
    public int getHighlightedRow() {
        return highlightedRow;
    }
    
    // Hover
    public void setHoveredRow(int row) {
        this.hoveredRow = row;
        repaint();
    }
    
    public int getHoveredRow() {
        return hoveredRow;
    }
    
    // Sélection multiple personnalisée
    public void addSelectedRow(int row) {
        selectedRows.add(row);
        repaint();
    }
    
    public void removeSelectedRow(int row) {
        selectedRows.remove(row);
        repaint();
    }
    
    @Override
    public void clearSelection() {
        if (selectedRows == null ) {
            selectedRows = new HashSet<>();
        }
        selectedRows.clear();
        super.clearSelection();  // Appel Swing natif si tu veux garder la compatibilité
        repaint();
    }
    
    public Set<Integer> getRowsSelected() {
        return new HashSet<>(selectedRows);
    }
    
    // Focus
    public void setFocusedCell(int row, int column) {
        this.focusedRow = row;
        this.focusedColumn = column;
        repaint();
    }
    
    public int getFocusedRow() {
        return focusedRow;
    }
    
    public int getFocusedColumn() {
        return focusedColumn;
    }
    
    // Édition
    public void setEditing(boolean editing) {
        this.isEditing = editing;
        repaint();
    }
    
    public boolean isEditing() {
        return isEditing;
    }
    
    // Couleurs personnalisées par ligne
    public void setRowBackground(int row, Color color) {
        rowBackgroundColors.put(row, color);
        repaint();
    }
    
    public Color getRowBackground(int row) {
        return rowBackgroundColors.get(row);
    }
    
    public void setRowForeground(int row, Color color) {
        rowForegroundColors.put(row, color);
        repaint();
    }
    
    public Color getRowForeground(int row) {
        return rowForegroundColors.get(row);
    }
    
    // États visuels génériques
    public void setVisualState(String stateKey, Object value) {
        visualStates.put(stateKey, value);
        repaint();
    }
    
    public Object getVisualState(String stateKey) {
        return visualStates.get(stateKey);
    }
    
    // Nettoyage complet
    public void clearAllVisualStates() {

        highlightedRow = -1;
        hoveredRow = -1;

        selectedRows.clear();

        focusedRow = -1;
        focusedColumn = -1;

        rowBackgroundColors.clear();
        rowForegroundColors.clear();

        visualStates.clear();

        repaint();
    }
    
    public HTableController getController() {
        return controller;
    }
    
    public HTableModel getHModel() {
        return hModel;
    } 
    
        public void AjustColumnsToFitContent() {
    for (int col = 0; col < this.getColumnCount(); col++) {
        int maxWidth = 0;

        // Prendre en compte toutes les lignes
        for (int row = 0; row < this.getRowCount(); row++) {
            TableCellRenderer renderer = this.getCellRenderer(row, col);
            Component comp = this.prepareRenderer(renderer, row, col);
            maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
        }

        // Prendre en compte l'entête
        TableCellRenderer headerRenderer = this.getTableHeader().getDefaultRenderer();
        Component headerComp = headerRenderer.getTableCellRendererComponent(this,
                this.getColumnName(col), false, false, 0, col);
        maxWidth = Math.max(maxWidth, headerComp.getPreferredSize().width);

        // Ajouter un padding
        maxWidth += 10;

        this.getColumnModel().getColumn(col).setPreferredWidth(maxWidth);
    }
}    
    
}
