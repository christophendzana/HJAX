package hsupertable.model.selection;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * HCellSelectionModel — source de vérité unique de la sélection de cellules de
 * HSuperTable. N'est pas un troisième stockage : orchestre les deux
 * ListSelectionModel natifs (lignes, colonnes), dont il dérive toute
 * information 2D. Les mutations passent par JTable.changeSelection(...), jamais
 * réimplémenté ici — ce modèle est un point de lecture et d'événement, pas un
 * second point de mutation.
 */
public class CellSelectionModel {

    private ListSelectionModel rowModel;
    private ListSelectionModel columnModel;
    private final ListSelectionListener axisListener = e -> fireSelectionChanged(e.getValueIsAdjusting());
    private final List<CellSelectionListener> listeners = new ArrayList<>();

    public CellSelectionModel(ListSelectionModel rowModel, ListSelectionModel columnModel) {
        setRowModel(rowModel);
        setColumnModel(columnModel);
    }

    /**
     * Reconnexion explicite — appelée par HSuperTable.setSelectionModel(...).
     */
    public void setRowModel(ListSelectionModel newModel) {
        if (rowModel != null) {
            rowModel.removeListSelectionListener(axisListener);
        }
        rowModel = newModel;
        rowModel.addListSelectionListener(axisListener);
    }

    /**
     * Reconnexion explicite — appelée par
     * HSuperTable.setColumnSelectionModel(...).
     */
    public void setColumnModel(ListSelectionModel newModel) {
        if (columnModel != null) {
            columnModel.removeListSelectionListener(axisListener);
        }
        columnModel = newModel;
        columnModel.addListSelectionListener(axisListener);
    }

    public boolean isCellSelected(int row, int col) {
        return rowModel.isSelectedIndex(row) && columnModel.isSelectedIndex(col);
    }

    public boolean hasSelection() {
        return !rowModel.isSelectionEmpty() && !columnModel.isSelectionEmpty();
    }

    public Point getAnchorCell() {
        return new Point(rowModel.getAnchorSelectionIndex(), columnModel.getAnchorSelectionIndex());
    }

    public Point getLeadCell() {
        return new Point(rowModel.getLeadSelectionIndex(), columnModel.getLeadSelectionIndex());
    }

    /**
     * {rowStart, colStart, rowEnd, colEnd}, ou null si un des deux axes n'a
     * rien sélectionné.
     */
    public int[] getSelectedCellRange() {
        if (!hasSelection()) {
            return null;
        }
        return new int[]{
            rowModel.getMinSelectionIndex(), columnModel.getMinSelectionIndex(),
            rowModel.getMaxSelectionIndex(), columnModel.getMaxSelectionIndex()
        };
    }

    public int[] getSelectedRows() {
        return toArray(rowModel);
    }

    public int[] getSelectedColumns() {
        return toArray(columnModel);
    }

    private int[] toArray(ListSelectionModel model) {
        if (model.isSelectionEmpty()) {
            return new int[0];
        }
        List<Integer> result = new ArrayList<>();
        for (int i = model.getMinSelectionIndex(); i <= model.getMaxSelectionIndex(); i++) {
            if (model.isSelectedIndex(i)) {
                result.add(i);
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Non couvert par changeSelection(...) — sélection totale des deux axes.
     */
    public void selectAll(int rowCount, int colCount) {
        if (rowCount <= 0 || colCount <= 0) {
            return;
        }
        rowModel.setSelectionInterval(0, rowCount - 1);
        columnModel.setSelectionInterval(0, colCount - 1);
    }

    public void clearSelection() {
        rowModel.clearSelection();
        columnModel.clearSelection();
    }

    public void addCellSelectionListener(CellSelectionListener l) {
        listeners.add(l);
    }

    public void removeCellSelectionListener(CellSelectionListener l) {
        listeners.remove(l);
    }

    private void fireSelectionChanged(boolean adjusting) {
        CellSelectionEvent event = new CellSelectionEvent(this, adjusting);
        for (CellSelectionListener l : listeners) {
            l.cellSelectionChanged(event);
        }
    }
        
}
