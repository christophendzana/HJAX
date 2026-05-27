package hsupertable;

import hsupertable.HBasicTableUI.InternalCellHit;
import hsupertable.HSuperDefaultTableModel.InternalGrid;
import hsupertable.HSuperTable.CellRange;
import java.awt.Component;
import java.awt.Cursor;
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

    // Position Y du curseur au début du resize de ligne
    private int resizeDragStartY = -1;

    // Position X du curseur au début du resize de colonne  
    private int resizeDragStartX = -1;

    // Hauteur / largeur originale au début du drag
    private int resizeOriginalSize = -1;

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

    // Tolérance en pixels pour détecter une bordure de resize
    private static final int RESIZE_TOLERANCE = 4;

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

        // ── Listener double-clic et clic droit sur l'en-tête ─────────────────
        table.getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int col = table.getTableHeader().columnAtPoint(e.getPoint());
                if (col < 0) {
                    return;
                }

                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    // ── Double-clic gauche → renommer la colonne ──────────────
                    table.startHeaderEdit(col);

                } else if (SwingUtilities.isRightMouseButton(e)
                        && e.getClickCount() == 1) {
                    // ── Clic droit → menu contextuel des en-têtes ─────────────
                    HSuperTable.HeaderContext ctx = new HSuperTable.HeaderContext(
                            table,
                            col,
                            e.getPoint()
                    );
                    table.showHeaderMenu(ctx, e.getX(), e.getY());
                }
            }
        });

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
        // Mode gomme
        if (table.getInteractionMode() == HSuperTable.MODE_ERASE && SwingUtilities.isLeftMouseButton(e)) {
            Point point = e.getPoint();
            HBasicTableUI ui = (HBasicTableUI) table.getUI();
            InternalCellHit hit = ui.getInternalCellAt(table, point);
            if (hit == null || hit.cell == null) {
                return;
            }

            int row = table.rowAtPoint(point);
            int col = table.columnAtPoint(point);
            if (row < 0 || col < 0) {
                return;
            }

            // Déterminer quelle subdivision supprimer
            if (hit.cell.hasInternalGrid()) {
                if (hit.parent == null) {
                    // Cellule racine subdivisée -> utiliser les coordonnées
                    table.getHModel().removeInternalGrid(row, col);
                } else {
                    // Sous‑cellule subdivisée (second niveau ou plus)
                    table.getHModel().removeInternalGridFromCell(hit.cell);
                }
            } else if (hit.parent != null && hit.parent.hasInternalGrid()) {
                // Sous‑cellule simple -> supprimer la subdivision du parent
                table.getHModel().removeInternalGridFromCell(hit.parent);
            } else {
                return; // Rien à effacer
            }

            // Nettoyer l'état des sélections internes
            table.setFocusedInternalCell(null);
            table.setSelectedInternalCell(null);
            table.repaint();
            return;
        }

        // --- Clic droit ---
        if (SwingUtilities.isRightMouseButton(e)) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (row >= 0 && col >= 0) {
                int[] resolved = resolveAbsorbed(row, col);
                handleRightClick(resolved[0], resolved[1], e.getPoint());
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

    private void handleRightClick(int row, int col, Point mousePos) {
        if (!isValidCell(row, col)) {
            return;
        }

        // ── Sélection de la cellule ciblée ────────────────────────────────────
        HSuperTable.CellRange sel = table.getSelection();
        if (sel == null || !sel.contains(row, col)) {
            anchorRow = row;
            anchorCol = col;
            dragRow = row;
            dragCol = col;
            table.clearSelection();
            table.addSelectedRow(row);
            table.setSelection(new HSuperTable.CellRange(row, col, row, col));
        }

        table.setHighlightedRow(row);
        table.setFocusedCell(row, col);

        // ── Construction du TableContext ──────────────────────────────────────
        HSuperDefaultTableModel model = table.getHModel();
        HSuperDefaultTableModel.Cell cell = model.getCell(row, col);

        // Résolution de la cellule principale si absorbée
        if (cell.isAbsorbed() && cell.mergeOrigin != null) {
            cell = model.getCell(cell.mergeOrigin.x, cell.mergeOrigin.y);
        }

        // Détection de la sous-cellule interne sous le curseur
        HBasicTableUI ui = (HBasicTableUI) table.getUI();
        HBasicTableUI.InternalCellHit internalHit = ui.getInternalCellAt(table, mousePos);

        // Mise à jour du focus interne si sous-cellule détectée
        if (internalHit != null && internalHit.parent != null) {
            table.setFocusedInternalCell(internalHit);
            table.setSelectedInternalCell(internalHit);
        }

        // ── Création du contexte et affichage du menu ─────────────────────────
        HSuperTable.TableContext ctx = new HSuperTable.TableContext(
                table,
                row,
                col,
                cell,
                internalHit,
                mousePos
        );

        table.showContextMenu(ctx, mousePos.x, mousePos.y);
    }

    // =========================================================================
    // GESTION SOURIS — PRESS / DRAG / RELEASE
    // =========================================================================
    private void handleMousePress(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        //  Démarrage du resize 
        // Si un index de resize est actif (détecté dans handleMouseMove),
        // on démarre le resize et on bloque toute la logique de sélection normale.
        if (table.getInteractionMode() == HSuperTable.MODE_NORMAL) {

            if (table.getResizeRowIndex() >= 0) {
                // Démarrage resize de ligne
                table.setResizingRow(true);
                resizeDragStartY = e.getY();
                resizeOriginalSize = table.getRowHeight(table.getResizeRowIndex());
                table.setResizePreviewY(e.getY());
                return; // on bloque la sélection
            }

            if (table.getResizeColIndex() >= 0) {
                int col = table.getResizeColIndex();
                int neighbor = (col + 1 < table.getColumnCount()) ? col + 1 : -1;
                table.setResizeColNeighborIndex(neighbor);
                table.setResizingCol(true);
                resizeDragStartX = e.getX();
                // Largeur originale colonne gauche
                resizeOriginalSize = table.getColumnModel().getColumn(col).getWidth();
                // Largeur originale colonne voisine droite
                if (neighbor >= 0) {
                    table.setResizeNeighborOriginalSize(
                            table.getColumnModel().getColumn(neighbor).getWidth());
                }
                table.setResizePreviewX(e.getX());
                return;
            }
        }

        // Mode crayon 
        if (table.getInteractionMode() == HSuperTable.MODE_DRAW) {
            drawStartX = e.getX();
            drawStartY = e.getY();
            drawEndX = e.getX();
            drawEndY = e.getY();
            isDrawing = true;

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

        //  Double-press 
       if (e.getClickCount() == 2) {
    HBasicTableUI ui = (HBasicTableUI) table.getUI();
    InternalCellHit hit = ui.getInternalCellAt(table, e.getPoint());

    if (hit != null && hit.parent != null) {
        // Sous-cellule détectée — éditeur interne
        e.consume();
        table.startInternalEdit(hit);
        return;
    }

    // ── Redirection vers la cellule principale si absorbée ────────────
    int editRow = resolved[0];
    int editCol = resolved[1];

    // Mettre le focus sur la cellule principale
    table.setFocusedCell(editRow, editCol);
    table.setHighlightedRow(editRow);

    // Forcer isCellEditable à true temporairement via editCellAt
    // en contournant la vérification — on utilise changeSelection
    // puis editCellAt sur la cellule principale
    table.changeSelection(editRow, editCol, false, false);

    if (table.isCellEditable(editRow, editCol)
            || table.getHModel().isAbsorbed(row, col)) {
        // Lancer l'éditeur sur la cellule principale
        table.editCellAt(editRow, editCol, e);
        Component editor = table.getEditorComponent();
        if (editor != null) {
            editor.requestFocusInWindow();
        }
    }
    return;
}

        // Clic simple : sélection normale 
        HBasicTableUI ui = (HBasicTableUI) table.getUI();
        InternalCellHit hit = ui.getInternalCellAt(table, e.getPoint());

        // On ne setter le focus interne que si c'est une vraie sous-cellule
        if (hit != null && hit.parent != null) {
            table.setFocusedInternalCell(hit);
            table.setSelectedInternalCell(hit);
        } else {
            table.setFocusedInternalCell(null);
            table.setSelectedInternalCell(null);
        }

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

        //  Drag resize de ligne 
        if (table.isResizingRow()) {
            // On calcule la nouvelle hauteur en temps réel
            // et on met à jour uniquement la position de la ligne de prévisualisation.
            // La hauteur réelle est appliquée au release.
            int row = table.getResizeRowIndex();
            int delta = e.getY() - resizeDragStartY;
            int newHeight = Math.max(20, resizeOriginalSize + delta);

            // Position Y de la prévisualisation = haut de la ligne + nouvelle hauteur
            Rectangle cellRect = table.getCellRect(row, 0, true);
            table.setResizePreviewY(cellRect.y + newHeight);
            table.repaint();
            return;
        }

        //  Drag resize de colonne 
        if (table.isResizingCol()) {
            int delta = e.getX() - resizeDragStartX;
            int col = table.getResizeColIndex();

            // Nouvelle largeur de la colonne gauche — minimum 30px
            int newWidth = Math.max(30, resizeOriginalSize + delta);

            // Position X de la prévisualisation = bord gauche de la colonne + nouvelle largeur
            Rectangle cellRect = table.getCellRect(0, col, true);
            table.setResizePreviewX(cellRect.x + newWidth);
            table.repaint();
            return;
        }

        //  Mode crayon 
        if (table.getInteractionMode() == HSuperTable.MODE_DRAW && isDrawing) {
            drawEndX = e.getX();
            drawEndY = e.getY();
            table.repaint();
            return;
        }

        // ── Drag sélection normale ────────────────────────────────────────────
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

        if (row == dragRow && col == dragCol) {
            return;
        }

        isDragging = true;
        dragRow = row;
        dragCol = col;

        applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
    }

    private void handleMouseRelease(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        //  Fin resize de ligne 
        // On applique la hauteur finale et on remet tous les états à zéro.
        if (table.isResizingRow()) {
            int row = table.getResizeRowIndex();
            int delta = e.getY() - resizeDragStartY;
            int newHeight = Math.max(20, resizeOriginalSize + delta);
            table.setRowHeight(row, newHeight);

            // Remise à zéro de tous les états de resize
            table.setResizingRow(false);
            table.setResizeRowIndex(-1);
            table.setResizePreviewY(-1);
            resizeDragStartY = -1;
            resizeOriginalSize = -1;

            table.setCursor(Cursor.getDefaultCursor());
            table.refreshUI();
            // Forcer le redessin du focus après resize de colonne
            int fr = table.getFocusedRow();
            int fc = table.getFocusedColumn();
            if (fr >= 0 && fc >= 0) {
                table.setFocusedCell(fr, fc);
            }
            return;
        }

        // ── Fin resize de colonne ─────────────────────────────────────────────
        if (table.isResizingCol()) {
            int col = table.getResizeColIndex();
            int delta = e.getX() - resizeDragStartX;

            if (Math.abs(delta) > 2) {
                // Colonne gauche
                int newWidthLeft = Math.max(30, resizeOriginalSize + delta);
                table.getColumnModel().getColumn(col).setWidth(newWidthLeft);
                table.getColumnModel().getColumn(col).setPreferredWidth(newWidthLeft);

                // Colonne voisine droite
                int neighbor = table.getResizeColNeighborIndex();
                if (neighbor >= 0) {
                    int newWidthRight = Math.max(30,
                            table.getResizeNeighborOriginalSize() - delta);
                    table.getColumnModel().getColumn(neighbor).setWidth(newWidthRight);
                    table.getColumnModel().getColumn(neighbor).setPreferredWidth(newWidthRight);
                }
            }

            // Remise à zéro
            table.setResizingCol(false);
            table.setResizeColIndex(-1);
            table.setResizeColNeighborIndex(-1);
            table.setResizeNeighborOriginalSize(-1);
            table.setResizePreviewX(-1);
            resizeDragStartX = -1;
            resizeOriginalSize = -1;

            table.setCursor(Cursor.getDefaultCursor());
            table.refreshUI();

            // Forcer le redessin du focus après resize de ligne
            int fr = table.getFocusedRow();
            int fc = table.getFocusedColumn();
            if (fr >= 0 && fc >= 0) {
                table.setFocusedCell(fr, fc);
            }
            return;
        }

        // Fin mode crayon 
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

        // ── Fin drag sélection normale ────────────────────────────────────────
        if (isDragging && anchorRow >= 0 && dragRow >= 0) {
            applyRangeSelection(anchorRow, anchorCol, dragRow, dragCol);
        }
        isDragging = false;
    }

    // =========================================================================
    // GESTION SOURIS — MOUVEMENT hover et resize
    // =========================================================================
    private void handleMouseMove(MouseEvent e) {

        // ── Détection resize en priorité ──────────────────────────────────────
        if (table.getInteractionMode() == HSuperTable.MODE_NORMAL) {
            detectResize(e.getPoint());
        }

        // ── Hover — désactivé si une sélection de plage multi-cellules active ─
        // Le hover masquerait visuellement la sélection de plage
        if (table.getResizeRowIndex() < 0 && table.getResizeColIndex() < 0) {

            boolean hasMultiCellSelection = table.hasSelection()
                    && !table.getSelection().isSingleCell();

            if (hasMultiCellSelection) {
                // Effacer le hover pendant la sélection de plage
                if (table.getHoveredRow() >= 0) {
                    table.setHoveredRow(-1);
                }
                if (table.getHoveredInternalCell() != null) {
                    table.setHoveredInternalCell(null);
                }
            } else {
                // Comportement normal du hover
                int row = table.rowAtPoint(e.getPoint());
                if (row != table.getHoveredRow()) {
                    table.setHoveredRow(row);
                }
                HBasicTableUI ui = (HBasicTableUI) table.getUI();
                InternalCellHit hit = ui.getInternalCellAt(table, e.getPoint());
                table.setHoveredInternalCell(hit);
            }
        }
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

    /**
     * Détecte si le point souris est proche d'une bordure redimensionnable.
     *
     * Pour les lignes : on vérifie la bordure basse ET haute de chaque ligne.
     * La bordure haute de la ligne N = bordure basse de la ligne N-1. On
     * redimensionne toujours la ligne DU DESSUS.
     *
     * Pour les colonnes : on vérifie la bordure droite ET gauche de chaque
     * colonne. La bordure gauche de la colonne N = bordure droite de la colonne
     * N-1. On redimensionne toujours la colonne DE GAUCHE.
     *
     * Priorité : le resize de ligne est testé en premier. Si aucune bordure
     * n'est détectée, on remet les index à -1.
     */
    private void detectResize(Point point) {

        // ── Détection resize de ligne ─────────────────────────────────────────
        for (int row = 0; row < table.getRowCount(); row++) {

            Rectangle cellRect = table.getCellRect(row, 0, true);

            int bordureBasse = cellRect.y + cellRect.height;
            int bordureHaute = cellRect.y;

            // Proche de la bordure basse → on redimensionne cette ligne
            if (Math.abs(point.y - bordureBasse) <= RESIZE_TOLERANCE) {
                table.setResizeRowIndex(row);
                table.setResizeColIndex(-1);
                table.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                return;
            }

            // Proche de la bordure haute → on redimensionne la ligne du dessus
            if (Math.abs(point.y - bordureHaute) <= RESIZE_TOLERANCE && row > 0) {
                table.setResizeRowIndex(row - 1);
                table.setResizeColIndex(-1);
                table.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                return;
            }
        }

        // ── Détection resize de colonne ───────────────────────────────────────
        for (int col = 0; col < table.getColumnCount(); col++) {

            Rectangle cellRect = table.getCellRect(0, col, true);
            int bordureDroite = cellRect.x + cellRect.width;

            // Uniquement la bordure droite — pas de bordure gauche
            if (Math.abs(point.x - bordureDroite) <= RESIZE_TOLERANCE) {
                table.setResizeColIndex(col);
                table.setResizeRowIndex(-1);
                table.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                return;
            }
        }

        // ── Aucune bordure détectée → on remet à zéro ─────────────────────────
        table.setResizeRowIndex(-1);
        table.setResizeColIndex(-1);

        // Restaurer le curseur selon le mode actif
        switch (table.getInteractionMode()) {
            case HSuperTable.MODE_DRAW ->
                table.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            case HSuperTable.MODE_ERASE ->
                table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            default ->
                table.setCursor(Cursor.getDefaultCursor());
        }
    }

}
