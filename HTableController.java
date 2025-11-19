/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HTable;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Set;
import javax.swing.SwingUtilities;

/**
 *
 * @author FIDELE
 */
public class HTableController{
    
private final HTable table;
    private boolean isMultipleSelection = false;

    // Références aux listeners pour pouvoir les retirer dans dispose()
    private final MouseAdapter mouseListener;
    private final MouseMotionAdapter motionListener;
    private final KeyAdapter keyListener;
    private final FocusAdapter focusListener;

    public HTableController(HTable table) {
        this.table = table;

        // Création des listeners (champs) — facilite la suppression ultérieure
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease(e);
            }
        };

        motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        };

        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        };

        focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                handleFocusGained();
            }

            @Override
            public void focusLost(FocusEvent e) {
                handleFocusLost();
            }
        };

        initializeEventHandlers();
    }

    private void initializeEventHandlers() {
        setupMouseListeners();
        setupKeyboardListeners();
        setupFocusListeners();
    }

    private void setupMouseListeners() {
        table.addMouseListener(mouseListener);
        table.addMouseMotionListener(motionListener);
    }

    private void setupKeyboardListeners() {
        table.addKeyListener(keyListener);
    }

    private void setupFocusListeners() {
        table.addFocusListener(focusListener);
    }

    // ---------------------------
    // GESTION SOURIS
    // ---------------------------

    private void handleMouseClick(MouseEvent e) {
        Point point = e.getPoint();
        int row = table.rowAtPoint(point);
        int column = table.columnAtPoint(point);

        if (row == -1 || column == -1) {
            // Click en dehors des cellules : on nettoie et on sort.
            table.clearAllVisualStates();
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() == 1) {
                handleSingleClick(row, column, e);
            } else if (e.getClickCount() == 2) {
                handleDoubleClick(row, column, e);
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            handleRightClick(row, column, e);
        }
    }

    private void handleSingleClick(int row, int column, MouseEvent e) {
        if (!isValidCell(row, column)) return;

        table.setHighlightedRow(row);
        table.setFocusedCell(row, column);

        if (e.isControlDown()) {
            toggleRowSelection(row);
        } else if (e.isShiftDown()) {
            selectRowRange(row);
        } else {
            selectSingleRow(row);
        }
    }

    private void handleDoubleClick(int row, int column, MouseEvent e) {
        if (!isValidCell(row, column)) return;

        // Tenter d'entrer en édition si la cellule est éditable
        // On vérifie via JTable API ; si ton modèle expose isCellEditable, JTable prendra le relais.
        if (table.isCellEditable(row, column)) {
            table.editCellAt(row, column);
        } else {
            // comportement alternatif : marquer en édition visuelle
            table.setEditing(true);
        }
    }

    private void handleRightClick(int row, int column, MouseEvent e) {
        if (!isValidCell(row, column)) return;

        table.setHighlightedRow(row);
        table.setFocusedCell(row, column);
        selectSingleRow(row);
        showContextMenu();
    }

    private void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            isMultipleSelection = e.isControlDown() || e.isShiftDown();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        isMultipleSelection = false;
    }

    private void handleMouseMove(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        // Mettre à jour uniquement si la valeur change (réduire repaint)
        if (row != table.getHoveredRow()) {
            table.setHoveredRow(row);
        }
    }

    // ---------------------------
    // GESTION SÉLECTION
    // ---------------------------

    private void selectSingleRow(int row) {
        if (!isValidRow(row)) return;
        table.clearSelection();
        table.addSelectedRow(row);
    }

    private void toggleRowSelection(int row) {
        if (!isValidRow(row)) return;

        // getRowsSelected() retourne une copie — on vérifie et on utilise les méthodes d'HTable
        if (table.getRowsSelected().contains(row)) {
            table.removeSelectedRow(row);
        } else {
            table.addSelectedRow(row);
        }
    }

    private void selectRowRange(int targetRow) {
        if (!isValidRow(targetRow)) return;

        Set<Integer> currentSelection = table.getRowsSelected();

        int firstSelected;
        if (currentSelection.isEmpty()) {
            // Si aucune ligne sélectionnée, utiliser la ligne focusée ou la target
            firstSelected = Math.max(0, Math.min(table.getRowCount() - 1, table.getFocusedRow() >= 0 ? table.getFocusedRow() : targetRow));
        } else {
            firstSelected = currentSelection.stream().min(Integer::compareTo).orElse(targetRow);
        }

        int start = Math.max(0, Math.min(firstSelected, targetRow));
        int end = Math.min(table.getRowCount() - 1, Math.max(firstSelected, targetRow));

        table.clearSelection();
        for (int i = start; i <= end; i++) {
            table.addSelectedRow(i);
        }
    }

    // ---------------------------
    // GESTION CLAVIER
    // ---------------------------

    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                if (e.isControlDown()) {
                    selectAllRows();
                    e.consume();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                table.clearAllVisualStates();
                break;
            case KeyEvent.VK_DELETE:
                deleteSelectedRows();
                break;
            default:
                // autres touches : ne rien faire
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        // Pas d'action par défaut pour l'instant
    }

    // ---------------------------
    // FOCUS
    // ---------------------------

    private void handleFocusGained() {
        table.setVisualState("focused", true);
    }

    private void handleFocusLost() {
        table.setVisualState("focused", false);
    }

    // ---------------------------
    // UTILITAIRES / ACTIONS
    // ---------------------------

    private void showContextMenu() {
        // TODO: Implémenter ton menu contextuel ici (popup)
    }

    private void selectAllRows() {
        table.clearSelection();
        for (int i = 0; i < table.getRowCount(); i++) {
            table.addSelectedRow(i);
        }
    }

    private void deleteSelectedRows() {
        // TODO: Implémenter la suppression en appelant ton modèle HTableModel.
        // Exemple (si tu ajoutes removeRow dans HTableModel) :
        // List<Integer> rows = new ArrayList<>(table.getRowsSelected());
        // Collections.sort(rows, Collections.reverseOrder()); // supprimer de la fin vers le début
        // for (int r : rows) { table.getHModel().removeRow(r); }
        System.out.println("Suppression des lignes sélectionnées (non implémentée)");
    }

    /**
     * Nettoyage des listeners pour éviter les fuites mémoire.
     * Appeler dispose() quand le composant n'est plus utilisé.
     */
    public void dispose() {
        table.removeMouseListener(mouseListener);
        table.removeMouseMotionListener(motionListener);
        table.removeKeyListener(keyListener);
        table.removeFocusListener(focusListener);
    }

    // ---------------------------
    // VALIDATIONS
    // ---------------------------

    private boolean isValidRow(int row) {
        return row >= 0 && row < table.getRowCount();
    }

    private boolean isValidCell(int row, int column) {
        return isValidRow(row) && column >= 0 && column < table.getColumnCount();
    }
     
}
