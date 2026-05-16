package hsupertable;

import hsupertable.HBasicTableUI.InternalCellHit;
import hsupertable.HSuperDefaultTableModel.InternalGrid;
import hsupertable.HSuperTable.CellRange;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * HTableController — Gestion des événements souris, clavier et focus de
 * HSuperTable.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HSuperTableController {

    private final HSuperTable table;

    // =========================================================================
    // ÉTAT DE LA SÉLECTION DE ZONE
    // =========================================================================
    /**
     * Cellule d'ancrage — point de départ du glisser ou de la sélection Shift.
     * Reste fixe pendant tout le glisser, change seulement à mousePressed.
     */
    private int anchorRow = -1;
    private int anchorCol = -1;

    // Point de départ du glisser en mode crayon
    private int drawStartX = -1;
    private int drawStartY = -1;
    private int drawEndX = -1;
    private int drawEndY = -1;
    private boolean isDrawing = false;

    /**
     * Cellule courante pendant le glisser — mise à jour à chaque mouseDragged.
     * Avec anchorRow/Col, définit le rectangle de sélection.
     */
    private int dragRow = -1;
    private int dragCol = -1;

    /**
     * Vrai pendant un glisser actif (entre mousePressed et mouseReleased).
     */
    private boolean isDragging = false;

    // =========================================================================
    // RÉFÉRENCES AUX LISTENERS (pour pouvoir les retirer dans dispose())
    // =========================================================================
    private final MouseAdapter mouseListener;
    private final MouseMotionAdapter motionListener;
    private final KeyAdapter keyListener;
    private final FocusAdapter focusListener;

    private long lastClickTime = 0;

    // ====================================================
    // CONSTRUCTEUR
    // ==========================================
    public HSuperTableController(HSuperTable table) {
        this.table = table;

        // ── Listener souris ──────────────────────────────────────────
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

        // ── Listener de mouvement (hover + drag) ───────────────────
        motionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        };

        // ── Listener clavier ───────────────────────────────────────────────
        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        };

        // ── Listener focus ─────────────────────────────────────────────
        focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                table.setVisualState("focused", true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                table.setVisualState("focused", false);
            }
        };

        // Enregistrement des listeners
        table.addMouseListener(mouseListener);
        table.addMouseMotionListener(motionListener);
        table.addKeyListener(keyListener);
        table.addFocusListener(focusListener);

    }

    // =========================================================================
    // GESTION SOURIS — CLIC
    // =========================================================================
    private void handleMouseClick(MouseEvent e) {
        // ── Mode gomme ───────────────────────────────────────────────────────
        if (table.getInteractionMode() == HSuperTable.MODE_ERASE
                && SwingUtilities.isLeftMouseButton(e)) {

            // Mettre à jour le focus sur la cellule cliquée d'abord
            Point point = e.getPoint();
            int row = table.rowAtPoint(point);
            int col = table.columnAtPoint(point);
            if (row >= 0 && col >= 0) {
                HBasicTableUI ui = (HBasicTableUI) table.getUI();
                InternalCellHit hit = ui.getInternalCellAt(table, point);
                table.setFocusedInternalCell(hit);
                table.setFocusedCell(row, col);
                // Gomme sur la cible courante
                table.removeInternalGridFromFocused();
            }
            return;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (row >= 0 && col >= 0) {
                int[] resolved = resolveAbsorbed(row, col);
                handleRightClick(resolved[0], resolved[1]);
            }
        }
    }

    private void handleSingleClick(int row, int col, MouseEvent e) {
        if (!isValidCell(row, col)) {
            return;
        }

        table.setHighlightedRow(row);
        table.setFocusedCell(row, col);

        if (e.isShiftDown() && anchorRow >= 0 && anchorCol >= 0) {
            // Shift+clic : on étend la sélection depuis l'ancre jusqu'ici
            // L'ancre NE change PAS — c'est la définition du Shift+clic
            dragRow = row;
            dragCol = col;
            applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);

        } else if (e.isControlDown()) {
            // Ctrl+clic : bascule la sélection de la ligne (comportement existant)
            // On pose quand même l'ancre sur cette cellule
            anchorRow = row;
            anchorCol = col;
            toggleRowSelection(row);

        } else {
            // Clic simple : sélection de la cellule uniquement
            anchorRow = row;
            anchorCol = col;
            dragRow = row;
            dragCol = col;
            table.clearSelection();
            table.addSelectedRow(row);
            // Sélection de zone = cellule unique
            table.setSelection(new CellRange(row, col, row, col));
        }
    }

    private void handleDoubleClick(MouseEvent e, int row, int col) {
        System.out.println("double clic called");
        HBasicTableUI ui = (HBasicTableUI) table.getUI();

        InternalCellHit hit = ui.getInternalCellAt(table, e.getPoint());
        // Sous-cellule interne
        if (hit != null && hit.parent != null) {
            table.startInternalEdit(hit);
            return;
        }

        // Cellule normale
        if (!isValidCell(row, col)) {
            return;
        }

        if (table.isCellEditable(row, col)) {
            table.editCellAt(row, col);
        } else {
            table.setEditing(true);
        }
    }

    private void handleRightClick(int row, int col) {
        if (!isValidCell(row, col)) {
            return;
        }
        // Le clic droit sélectionne la cellule si elle n'est pas déjà dans
        // la sélection courante — comportement identique à Word
        CellRange sel = table.getSelection();
        if (sel == null || !sel.contains(row, col)) {
            anchorRow = row;
            anchorCol = col;
            dragRow = row;
            dragCol = col;
            table.clearSelection();
            table.addSelectedRow(row);
            table.setSelection(new CellRange(row, col, row, col));
        }
        table.setHighlightedRow(row);
        table.setFocusedCell(row, col);
        showContextMenu();
    }

    // =========================================================================
    // GESTION SOURIS — PRESS / DRAG / RELEASE
    // =========================================================================
    private void handleMousePress(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        // ── Mode crayon ──────────────────────────────────────────────────────
        if (table.getInteractionMode() == HSuperTable.MODE_DRAW) {
            drawStartX = e.getX();
            drawStartY = e.getY();
            drawEndX = e.getX();
            drawEndY = e.getY();
            isDrawing = true;

            // Mettre à jour le focus instantanément sur la cellule sous le curseur
            Point point = e.getPoint();
            int row = table.rowAtPoint(point);
            int col = table.columnAtPoint(point);
            if (row >= 0 && col >= 0) {
                table.setFocusedCell(row, col);
                HBasicTableUI ui = (HBasicTableUI) table.getUI();
                InternalCellHit hit = ui.getInternalCellAt(table, point);
                table.setFocusedInternalCell(hit);
                table.setSelectedInternalCell(hit);
            }
            return;
        }

        Point point = e.getPoint();
        int row = table.rowAtPoint(point);
        int col = table.columnAtPoint(point);
        if (row < 0 || col < 0) {
            return;
        }

        int[] resolved = resolveAbsorbed(row, col);
        row = resolved[0];
        col = resolved[1];

        // ── Double-press : on vérifie si on est sur une sous-cellule ─────────
        if (e.getClickCount() == 2) {
            HBasicTableUI ui = (HBasicTableUI) table.getUI();
            InternalCellHit hit = ui.getInternalCellAt(table, e.getPoint());

            if (hit != null && hit.parent != null) {
                // Sous-cellule détectée — on bloque JTable et on lance notre éditeur
                e.consume();
                table.startInternalEdit(hit);
                return;
            }
            // Pas de sous-cellule — on laisse JTable gérer son éditeur natif
            return;
        }

        // ── Clic simple : sélection normale ──────────────────────────────────
        HBasicTableUI ui = (HBasicTableUI) table.getUI();
        InternalCellHit hit = ui.getInternalCellAt(table, e.getPoint());
        table.setFocusedInternalCell(hit);
        table.setSelectedInternalCell(hit);

        if (!e.isShiftDown()) {
            anchorRow = row;
            anchorCol = col;
        }

        dragRow = row;
        dragCol = col;
        isDragging = false;

        handleSingleClick(row, col, e);
    }

    private void handleMouseDrag(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        // Mode crayon : mise à jour du point final pour la prévisualisation 
        if (table.getInteractionMode() == HSuperTable.MODE_DRAW && isDrawing) {
            drawEndX = e.getX();
            drawEndY = e.getY();
            table.repaint(); // déclenche le dessin du trait de prévisualisation
            return;
        }

        // Clamp les coordonnées pour rester dans le tableau même si la souris
        // sort des bords pendant le glisser
        int x = Math.max(0, Math.min(e.getX(), table.getWidth() - 1));
        int y = Math.max(0, Math.min(e.getY(), table.getHeight() - 1));

        int row = table.rowAtPoint(new Point(x, y));
        int col = table.columnAtPoint(new Point(x, y));
        if (row < 0 || col < 0) {
            return;
        }

        int[] resolved = resolveAbsorbed(row, col);
        row = resolved[0];
        col = resolved[1];

        // On ne met à jour que si la cellule a changé — évite des repaints inutiles
        if (row == dragRow && col == dragCol) {
            return;
        }

        isDragging = true;
        dragRow = row;
        dragCol = col;

        // Mettre à jour la sélection visuellement pendant le glisser
        applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
    }

    private void handleMouseRelease(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        // ── Mode crayon : appliquer la subdivision ───────────────────────────
        if (table.getInteractionMode() == HSuperTable.MODE_DRAW && isDrawing) {
            isDrawing = false;

            int row = table.rowAtPoint(new Point(drawStartX, drawStartY));
            int col = table.columnAtPoint(new Point(drawStartX, drawStartY));
            if (row < 0 || col < 0) {
                drawStartX = drawEndX = drawStartY = drawEndY = -1;
                return;
            }

            int dx = Math.abs(drawEndX - drawStartX);
            int dy = Math.abs(drawEndY - drawStartY);

            // Référence : bounds de la sous-cellule focusée si présente,
            // sinon rectangle de la cellule globale
            Rectangle refRect;
            if (table.hasInternalFocus()) {
                refRect = table.getFocusedInternalCell().bounds;
            } else {
                refRect = table.getCellRect(row, col, false);
            }

            int splitType;
            float ratio;

            if (dx >= dy) {
                splitType = InternalGrid.SPLIT_VERTICAL;
                int relativeX = drawEndX - refRect.x;
                ratio = (float) relativeX / refRect.width;
            } else {
                splitType = InternalGrid.SPLIT_HORIZONTAL;
                int relativeY = drawEndY - refRect.y;
                ratio = (float) relativeY / refRect.height;
            }

            ratio = Math.max(0.15f, Math.min(0.85f, ratio));
            table.splitCellLocally(row, col, splitType, ratio);

            drawStartX = drawEndX = -1;
            drawStartY = drawEndY = -1;
            table.repaint();
            return;
        }

        if (isDragging && anchorRow >= 0 && dragRow >= 0) {
            // Finaliser la sélection de zone
            applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
        }
        isDragging = false;
    }

    // =========================================================================
    // GESTION SOURIS — MOUVEMENT (hover)
    // =========================================================================
    private void handleMouseMove(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row != table.getHoveredRow()) {
            table.setHoveredRow(row);
        }

        HBasicTableUI ui = (HBasicTableUI) table.getUI();

        InternalCellHit hit
                = ui.getInternalCellAt(table, e.getPoint());
        table.setHoveredInternalCell(hit);

    }

    public int getDrawStartX() {
        return drawStartX;
    }

    public int getDrawStartY() {
        return drawStartY;
    }

    public int getDrawEndX() {
        return drawEndX;
    }

    public int getDrawEndY() {
        return drawEndY;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    // =========================================================================
    // SÉLECTION DE ZONE
    // =========================================================================
    /**
     * Applique une sélection rectangulaire de (r1,c1) à (r2,c2). Met à jour à
     * la fois la sélection de lignes (pour la surbrillance) et la CellRange
     * (pour les méthodes applyXxx).
     */
    private void applyRangeSelection(int r1, int c1, int r2, int c2) {
        if (r1 < 0 || c1 < 0 || r2 < 0 || c2 < 0) {
            return;
        }

        int rowStart = Math.min(r1, r2);
        int rowEnd = Math.max(r1, r2);
        int colStart = Math.min(c1, c2);
        int colEnd = Math.max(c1, c2);

        // Mettre à jour la CellRange dans HSuperTable
        table.setSelection(new CellRange(rowStart, colStart,
                rowEnd, colEnd));

        // Mettre à jour la sélection de lignes pour la surbrillance existante
        table.clearSelection();
        for (int r = rowStart; r <= rowEnd; r++) {
            table.addSelectedRow(r);
        }

        // Focus sur la cellule d'ancrage
        if (isValidCell(r1, c1)) {
            table.setFocusedCell(r1, c1);
        }
    }

    // =========================================================================
    // GESTION CLAVIER
    // =========================================================================
    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {

            case KeyEvent.VK_A -> {
                if (e.isControlDown()) {
                    // Ctrl+A : sélectionner tout le tableau
                    anchorRow = 0;
                    anchorCol = 0;
                    dragRow = table.getRowCount() - 1;
                    dragCol = table.getColumnCount() - 1;
                    applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
                    e.consume();
                }
            }

            case KeyEvent.VK_ESCAPE -> {
                // Échap : annuler la sélection
                table.clearAllVisualStates();
                table.setSelection(null);
                anchorRow = -1;
                anchorCol = -1;
                dragRow = -1;
                dragCol = -1;
            }

            case KeyEvent.VK_DELETE ->
                deleteSelectedRows();
            case KeyEvent.VK_RIGHT -> {
                navigateOrExtend(0, 1, e.isShiftDown());
                e.consume();
            }
            case KeyEvent.VK_LEFT -> {
                navigateOrExtend(0, -1, e.isShiftDown());
                e.consume();
            }
            case KeyEvent.VK_DOWN -> {
                navigateOrExtend(1, 0, e.isShiftDown());
                e.consume();
            }
            case KeyEvent.VK_UP -> {
                navigateOrExtend(-1, 0, e.isShiftDown());
                e.consume();
            }
        }
        // Navigation clavier — étend la sélection avec Shift+flèche
    }

    /**
     * Déplace le focus ou étend la sélection d'une cellule dans la direction
     * donnée (dr, dc). Si extend=true (Shift enfoncé), la sélection s'élargit.
     */
    private void navigateOrExtend(int dr, int dc, boolean extend) {
        // ── Navigation dans les sous-cellules ─────────────────────────────────
        InternalCellHit focused = table.getFocusedInternalCell();
        if (focused != null && focused.parent != null && !extend) {
            // On est dans une sous-cellule — naviguer vers l'autre sous-cellule
            // si la direction pointe vers elle, sinon sortir vers la cellule voisine
            InternalGrid grid = focused.parent.internalGrid;
            if (grid != null) {
                boolean goToSecond
                        = (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL && dc > 0)
                        || (grid.getSplitType() == InternalGrid.SPLIT_HORIZONTAL && dr > 0);
                boolean goToFirst
                        = (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL && dc < 0)
                        || (grid.getSplitType() == InternalGrid.SPLIT_HORIZONTAL && dr < 0);

                HSuperDefaultTableModel.Cell target = null;
                Rectangle targetRect = null;

                // Calculer les rectangles actuels
                int row = table.getFocusedRow();
                int col = table.getFocusedColumn();
                Rectangle cellRect = table.getCellRect(row, col, false);
                Rectangle[] parts = ((HBasicTableUI) table.getUI())
                        .computeInternalRects(cellRect, grid);

                if (goToSecond && focused.cell == grid.getFirstCell()) {
                    target = grid.getSecondCell();
                    targetRect = parts[1];
                } else if (goToFirst && focused.cell == grid.getSecondCell()) {
                    target = grid.getFirstCell();
                    targetRect = parts[0];
                }

                if (target != null) {
                    // Rester dans la même cellule parente, changer de sous-cellule
                    InternalCellHit newHit = new InternalCellHit(
                            target, targetRect, focused.parent);
                    table.setFocusedInternalCell(newHit);
                    table.setSelectedInternalCell(newHit);
                    return;
                }
            }
            // Direction vers l'extérieur — on sort de la sous-cellule
            table.setFocusedInternalCell(null);
            table.setSelectedInternalCell(null);
        }

        // ── Navigation normale entre cellules ─────────────────────────────────
        int focusRow = table.getFocusedRow();
        int focusCol = table.getFocusedColumn();
        if (focusRow < 0 || focusCol < 0) {
            return;
        }

        int newRow = Math.max(0, Math.min(table.getRowCount() - 1, focusRow + dr));
        int newCol = Math.max(0, Math.min(table.getColumnCount() - 1, focusCol + dc));

        if (extend) {
            dragRow = newRow;
            dragCol = newCol;
            applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
            table.setFocusedCell(newRow, newCol);
        } else {
            anchorRow = newRow;
            anchorCol = newCol;
            dragRow = newRow;
            dragCol = newCol;
            table.clearSelection();
            table.addSelectedRow(newRow);
            table.setSelection(new HSuperTable.CellRange(newRow, newCol, newRow, newCol));
            table.setFocusedCell(newRow, newCol);
        }
    }

    // =========================================================================
    // SÉLECTION DE LIGNES (comportement existant conservé)
    // =========================================================================
    private void toggleRowSelection(int row) {
        if (!isValidRow(row)) {
            return;
        }
        if (table.getRowsSelected().contains(row)) {
            table.removeSelectedRow(row);
        } else {
            table.addSelectedRow(row);
        }
    }

    private void selectAllRows() {
        anchorRow = 0;
        anchorCol = 0;
        dragRow = table.getRowCount() - 1;
        dragCol = table.getColumnCount() - 1;
        applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
    }

    private void deleteSelectedRows() {
        List<Integer> rows = new ArrayList<>(table.getRowsSelected());
        Collections.sort(rows, Collections.reverseOrder());
        for (int r : rows) {
            if (r >= 0 && r < table.getRowCount()) {
                table.getHModel().removeRow(r);
            }
        }
        table.setSelection(null);
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================
    /**
     * Si la cellule (row, col) est absorbée, retourne les coordonnées de la
     * cellule principale. Sinon retourne (row, col) inchangé. Utilise
     * mergeOrigin — O(1).
     *
     * @return int[]{row, col} résolu
     */
    private int[] resolveAbsorbed(int row, int col) {
        HSuperDefaultTableModel model = table.getHModel();
        if (model.isAbsorbed(row, col)) {
            Point origin = model.findMergeOrigin(row, col);
            if (origin != null) {
                return new int[]{origin.x, origin.y};
            }
        }
        return new int[]{row, col};
    }

    private void showContextMenu() {
        // TODO : implémenter le menu contextuel (popup)
    }

    private boolean isValidRow(int row) {
        return row >= 0 && row < table.getRowCount();
    }

    private boolean isValidCell(int row, int col) {
        return isValidRow(row) && col >= 0 && col < table.getColumnCount();
    }

    /**
     * Libère tous les listeners enregistrés. À appeler quand HSuperTable est
     * retiré de l'interface pour éviter les fuites mémoire.
     */
    public void dispose() {
        table.removeMouseListener(mouseListener);
        table.removeMouseMotionListener(motionListener);
        table.removeKeyListener(keyListener);
        table.removeFocusListener(focusListener);
    }
}
