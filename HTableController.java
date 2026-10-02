package hsupertable.controller;

import hsupertable.HTable;
import hsupertable.model.HDefaultTableModel;
import hsupertable.geometry.HTableGeometry;
import hsupertable.menu.HeaderContext;
import hsupertable.menu.TableContext;
import hsupertable.model.Cell;
import hsupertable.model.InternalGrid;
import java.awt.Component;
import java.awt.Cursor;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
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
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

/**
 * HTableController — Gestion des événements souris, clavier et focus de
 HTable.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HTableController {

    private final HTable table;

    // =========================================================================
    // ÉTAT DE LA SÉLECTION DE ZONE
    // =========================================================================    
    private ResizeHandler resizeHandler;
    private HeaderHandler headerHandler;
    private SelectionHandler selectionHandler;
    private InternalCellHandler internalCellHandler;

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
    public HTableController(HTable table) {
        this.table = table;
        this.resizeHandler = new ResizeHandler(table);
        this.headerHandler = new HeaderHandler(table);
        this.selectionHandler = new SelectionHandler(table);
        this.internalCellHandler = new InternalCellHandler(table);

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
        // ── Listener souris + survol sur l'en-tête ───────────────────────────
        // Renommage (double-clic), menu contextuel (clic droit), et
        // sélection de colonne(s) façon Word (clic / clic-glisser gauche).
        headerHandler.install(table.getTableHeader());

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
        if (internalCellHandler.tryHandleErase(e)) {
            return;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            int[] resolved = table.resolvePoint(e.getPoint());
            int row = resolved[0];
            int col = resolved[1];
            if (row >= 0 && col >= 0) {
                handleRightClick(row, col, e.getPoint());
            }
        }
    }

    private void handleRightClick(int row, int col, Point mousePos) {
        if (!isValidCell(row, col)) {
            return;
        }

        // ── Sélection de la cellule ciblée ────────────────────────────────────
        HTable.CellRange sel = table.getSelection();
        selectionHandler.ensureAnchorFor(row, col);

//        table.setHighlightedRow(row);
        table.setFocusedCell(row, col);

        // ── Construction du TableContext ──────────────────────────────────────
        HDefaultTableModel model = table.getHModel();
        Cell cell = model.getCell(table.toModelRow(row), table.toModelColumn(col));

        // Résolution de la cellule principale si absorbée
        if (cell.isAbsorbed() && cell.mergeOrigin != null) {
            cell = model.getCell(cell.mergeOrigin.x, cell.mergeOrigin.y);
        }

        // Détection de la sous-cellule interne sous le curseur
        HTableGeometry.InternalCellHit internalHit = HTableGeometry.getInternalCellAt(table, mousePos);

        // Mise à jour du focus interne si sous-cellule détectée
        if (internalHit != null && internalHit.parent != null) {
            table.setFocusedInternalCell(internalHit);
            table.setSelectedInternalCell(internalHit);
        }

        // ── Création du contexte et affichage du menu ─────────────────────────
        TableContext ctx = new TableContext(table, row, col, cell, internalHit, mousePos);

        table.showContextMenu(ctx, mousePos.x, mousePos.y);
    }

    // =========================================================================
    // GESTION SOURIS — PRESS / DRAG / RELEASE
    // =========================================================================
    private void handleMousePress(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        // Restaure ce que le MouseInputHandler natif de BasicTableUI faisait
        // silencieusement avant qu'on le désactive : donner le focus clavier
        // au tableau à chaque clic. Sans ça, aucun KeyListener ne reçoit plus
        // d'événement.
        table.requestFocusInWindow();

        if (internalCellHandler.handleDrawModePress(e)) {
            return;
        }

        int[] resolved = table.resolvePoint(e.getPoint());
        int row = resolved[0];
        int col = resolved[1];
        if (row < 0 || col < 0) {
            return;
        }

        if (table.getInteractionMode() == HTable.MODE_NORMAL
                && resizeHandler.handleMousePressed(e)) {
            return;
        }

        if (internalCellHandler.tryHandleDoublePress(e)) {
            return; // édition de sous-cellule déclenchée
        }

        if (e.getClickCount() == 2 && table.isCellEditable(row, col)) {
            table.editCellAt(row, col, e);
            resizeEditorToMergedBounds(row, col);
            return;
        }

        internalCellHandler.updateInternalFocusOnPress(e.getPoint());
        selectionHandler.handleSingleClick(row, col, e);
    }

    /**
     * JTable.editCellAt positionne l'éditeur sur le rectangle d'une seule
     * cellule physique — il ne connaît rien de MergeModel. Si la cellule éditée
     * est la principale d'une fusion, on élargit le composant d'édition au
     * rectangle fusionné réel juste après son apparition.
     */
    private void resizeEditorToMergedBounds(int row, int col) {
        Component editorComp = table.getEditorComponent();
        if (editorComp == null) {
            return;
        }

        Cell cell = table.getHModel().getCell(row, col);
        if (cell.spanRow <= 1 && cell.spanCol <= 1) {
            return; // pas de fusion, rien à faire
        }
        Rectangle mergedRect = HTableGeometry.getCellBounds(table, row, col);
        editorComp.setBounds(mergedRect);
    }

    private void handleMouseDrag(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        if (resizeHandler.handleMouseDragged(e)) {
            return;
        }
        if (internalCellHandler.handleDrawModeDrag(e)) {
            return;
        }

        int x = Math.max(0, Math.min(e.getX(), table.getWidth() - 1));
        int y = Math.max(0, Math.min(e.getY(), table.getHeight() - 1));
        int[] resolved = table.resolvePoint(new Point(x, y));
        int row = resolved[0];
        int col = resolved[1];
        if (row < 0 || col < 0) {
            return;
        }

        selectionHandler.handleDrag(row, col);
    }

    private void handleMouseRelease(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        if (resizeHandler.handleMouseReleased(e)) {
            return;
        }
        if (internalCellHandler.handleDrawModeRelease()) {
            return;
        }

        selectionHandler.handleRelease();
    }

    // =========================================================================
    // GESTION SOURIS — MOUVEMENT hover et resize
    // =========================================================================
    private void handleMouseMove(MouseEvent e) {

        // ── Détection resize — sur coordonnées brutes ─────────────────────────
        if (table.getInteractionMode() == HTable.MODE_NORMAL) {
            resizeHandler.detectResize(e.getPoint());
        }

        // ── Hover — résolution via resolvePoint ───────────────────────────────
        if (table.getResizeRowIndex() < 0 && table.getResizeColIndex() < 0) {

            // Dès qu'une cellule (ou une plage) est sélectionnée, le survol
            // n'affiche plus rien — évite la confusion avec la sélection
            // active, déjà mise en avant par ailleurs
            if (table.hasSelection()) {
                if (table.getHoveredRow() >= 0) {
                    table.setHoveredRow(-1);
                }
                if (table.getHoveredInternalCell() != null) {
                    table.setHoveredInternalCell(null);
                }
            } else {
                // Résoudre vers la cellule principale
                int[] resolved = table.resolvePoint(e.getPoint());
                int row = resolved[0];
                if (row != table.getHoveredRow()) {
                    table.setHoveredRow(row);
                }

                HTableGeometry.InternalCellHit hit = HTableGeometry.getInternalCellAt(table, e.getPoint());
                table.setHoveredInternalCell(hit);
            }
        }
    }

    public int getDrawStartX() {
        return internalCellHandler.getDrawStartX();
    }

    public int getDrawStartY() {
        return internalCellHandler.getDrawStartY();
    }

    public int getDrawEndX() {
        return internalCellHandler.getDrawEndX();
    }

    public int getDrawEndY() {
        return internalCellHandler.getDrawEndY();
    }

    public boolean isDrawing() {
        return internalCellHandler.isDrawing();
    }

    // =========================================================================
    // SÉLECTION DE ZONE
    // =========================================================================
    // =========================================================================
    // GESTION CLAVIER
    // =========================================================================
    private void handleKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> {
                if (e.isControlDown()) {
                    selectionHandler.selectAll();
                    e.consume();
                }
            }
            case KeyEvent.VK_ESCAPE -> {
                table.clearAllVisualStates();
                selectionHandler.clear();
            }
            case KeyEvent.VK_DELETE ->
                selectionHandler.deleteSelectedRows();
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
    }

    /**
     * Déplace le focus ou étend la sélection d'une cellule dans la direction
     * donnée (dr, dc). Si extend=true (Shift enfoncé), la sélection s'élargit.
     */
    private void navigateOrExtend(int dr, int dc, boolean extend) {
        if (!extend && internalCellHandler.tryNavigateInternal(dr, dc)) {
            return;
        }
        selectionHandler.navigate(dr, dc, extend);
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < table.getRowCount()
                && col >= 0 && col < table.getColumnCount();
    }

    public void selectAll() {
        selectionHandler.selectAll();
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
        headerHandler.dispose(table.getTableHeader());
    }

    //====================================================================================================
    // CLASSES INTERNES REGROUPANT LES LOGIQUES DES HEADERS, REZISE, DE SELECTION, ET DE CELLULES INTERNES
    // ON SAURA EXACTEMENT Où REGARDER EN CAS DE BUG CAR LES FONCTIONNALITES SONT REGROUPEES EN CLASSES
    //=============================================================================================
    /**
     * HTableHeaderController: interactions souris sur le JTableHeader :
     * renommage (double-clic), menu contextuel (clic droit), sélection de
     * colonne(s) façon Word (clic / clic-glisser en dehors des bordures de
     * redimensionnement natives).
     */
    public class HeaderHandler extends MouseAdapter {

        private static final int RESIZE_TOLERANCE = 4;

        private final HTable table;
        private int headerAnchorCol = -1;
        private int headerDragCol = -1;

        public HeaderHandler(HTable table) {
            this.table = table;
        }

        public void install(JTableHeader header) {
            header.addMouseListener(this);
            header.addMouseMotionListener(this);
        }

        public void dispose(JTableHeader header) {
            header.removeMouseListener(this);
            header.removeMouseMotionListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int col = table.getTableHeader().columnAtPoint(e.getPoint());
            if (col < 0) {
                return;
            }

            if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                table.startHeaderEdit(col);
            } else if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
                HeaderContext ctx = new HeaderContext(table, col, e.getPoint());
                table.showHeaderMenu(ctx, e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            if (isNearColumnHeaderBorder(e.getPoint())) {
                return;
            }
            int col = table.getTableHeader().columnAtPoint(e.getPoint());
            if (col < 0) {
                return;
            }
            headerAnchorCol = col;
            headerDragCol = col;
            table.selectColumn(col);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (headerAnchorCol < 0) {
                return;
            }
            int col = table.getTableHeader().columnAtPoint(e.getPoint());
            if (col < 0 || col == headerDragCol) {
                return;
            }
            headerDragCol = col;
            table.selectColumns(headerAnchorCol, headerDragCol);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            headerAnchorCol = -1;
            headerDragCol = -1;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            JTableHeader header = table.getTableHeader();
            header.setCursor(isNearColumnHeaderBorder(e.getPoint())
                    ? Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
                    : Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            table.getTableHeader().setCursor(Cursor.getDefaultCursor());
        }

        private boolean isNearColumnHeaderBorder(Point point) {
            JTableHeader header = table.getTableHeader();
            for (int col = 0; col < table.getColumnCount(); col++) {
                Rectangle r = header.getHeaderRect(col);
                if (Math.abs(point.x - (r.x + r.width)) <= RESIZE_TOLERANCE) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Tout ce qui concerne les sous-cellules internes (InternalGrid) : mode
     * crayon, mode gomme, suivi du hit-testing au clic, navigation clavier
     * entre sous-cellules. Extrait de HSuperTableController.
     */
    public class InternalCellHandler {

        private final HTable table;

        private int drawStartX = -1;
        private int drawStartY = -1;
        private int drawEndX = -1;
        private int drawEndY = -1;
        private boolean isDrawing = false;

        private Component activeSubCellEditorComponent;
        private HTableGeometry.InternalCellHit activeSubCellEditorHit;

        public InternalCellHandler(HTable table) {
            this.table = table;
        }

        // ── État exposé pour le rendu (HBasicTableUI.paintDrawPreview) ────────
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

        // ── Mode gomme ─────────────────────────────────────────────────────────
        public boolean tryHandleErase(MouseEvent e) {
            if (table.getInteractionMode() != HTable.MODE_ERASE
                    || !SwingUtilities.isLeftMouseButton(e)) {
                return false;
            }
            Point point = e.getPoint();
            HTableGeometry.InternalCellHit hit = HTableGeometry.getInternalCellAt(table, point);
            if (hit == null || hit.cell == null) {
                return false;
            }

            int[] resolved = table.resolvePoint(point);
            int row = resolved[0];
            int col = resolved[1];
            if (row < 0 || col < 0) {
                return false;
            }

            if (hit.cell.hasInternalGrid()) {
                if (hit.parent == null) {
                    table.getHModel().removeInternalGrid(table.toModelRow(row), table.toModelColumn(col));
                } else {
                    table.getHModel().removeInternalGridFromCell(hit.cell);
                }
            } else if (hit.parent != null && hit.parent.hasInternalGrid()) {
                table.getHModel().removeInternalGridFromCell(hit.parent);
            } else {
                return false;
            }

            table.setFocusedInternalCell(null);
            table.setSelectedInternalCell(null);
            table.repaint();
            return true;
        }

        // ── Mode crayon ────────────────────────────────────────────────────────
        public boolean handleDrawModePress(MouseEvent e) {
            if (table.getInteractionMode() != HTable.MODE_DRAW) {
                return false;
            }
            drawStartX = e.getX();
            drawStartY = e.getY();
            drawEndX = e.getX();
            drawEndY = e.getY();
            isDrawing = true;

            int[] resolved = table.resolvePoint(e.getPoint());
            int row = resolved[0];
            int col = resolved[1];
            if (row >= 0 && col >= 0) {
                table.setFocusedCell(row, col);

                HTableGeometry.InternalCellHit hit = HTableGeometry.getInternalCellAt(table, e.getPoint());
                table.setFocusedInternalCell(hit);
                table.setSelectedInternalCell(hit);
            }
            return true;
        }

        public boolean handleDrawModeDrag(MouseEvent e) {
            if (table.getInteractionMode() != HTable.MODE_DRAW || !isDrawing) {
                return false;
            }
            drawEndX = e.getX();
            drawEndY = e.getY();
            table.repaint();
            return true;
        }

        public boolean handleDrawModeRelease() {
            if (table.getInteractionMode() != HTable.MODE_DRAW || !isDrawing) {
                return false;
            }
            isDrawing = false;

            int[] resolved = table.resolvePoint(new Point(drawStartX, drawStartY));
            int row = resolved[0];
            int col = resolved[1];
            if (row < 0 || col < 0) {
                drawStartX = drawEndX = drawStartY = drawEndY = -1;
                return true;
            }

            int dx = Math.abs(drawEndX - drawStartX);
            int dy = Math.abs(drawEndY - drawStartY);

            Rectangle refRect;
            if (table.hasInternalFocus()) {
                refRect = table.getFocusedInternalCell().bounds;
            } else {
                refRect = HTableGeometry.getCellBounds(table, row, col);
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
            return true;
        }

        // ── Suivi du hit-testing interne (mode normal) ─────────────────────────
        public void updateInternalFocusOnPress(Point point) {
            HTableGeometry.InternalCellHit hit = HTableGeometry.getInternalCellAt(table, point);

            if (hit != null && hit.parent != null) {
                table.setFocusedInternalCell(hit);
                table.setSelectedInternalCell(hit);
            } else {
                table.setFocusedInternalCell(null);
                table.setSelectedInternalCell(null);
            }
        }

        /**
         * @return true dans tous les cas si clickCount == 2 (comportement
         * identique à l'original).
         */
        public boolean tryHandleDoublePress(MouseEvent e) {
            if (e.getClickCount() != 2) {
                return false;
            }
            HTableGeometry.InternalCellHit hit = HTableGeometry.getInternalCellAt(table, e.getPoint());
            if (hit != null && hit.parent != null) {
                e.consume();
                table.startInternalEdit(hit);
                return true;
            }
            return false; // pas de sous-cellule : laisser l'appelant gérer le double-clic normal
        }

        /**
         * Route vers l'éditeur typé si la colonne d'origine en a un enregistré,
         * sinon vers l'édition texte historique (internalEditor). Le type suit
         * toujours la colonne (row, col) du hit-testing, jamais la profondeur
         * de la sous-cellule.
         */
        private void startEditForHit(HTableGeometry.InternalCellHit hit) {
            int row = table.getFocusedRow();
            int col = table.getFocusedColumn();
            Class<?> valueClass = table.getColumnClass(col);
            TableCellEditor editor = table.getDefaultEditor(valueClass);

            boolean isDefaultTextEditor = editor == null
                    || (editor instanceof DefaultCellEditor
                    && table.getDefaultEditor(Object.class) == editor);

            if (isDefaultTextEditor) {
                table.startInternalEdit(hit); // chemin texte historique, inchangé
                return;
            }

            Component comp = editor.getTableCellEditorComponent(table, hit.cell.value, false, row, col);
            comp.setBounds(hit.bounds);

            // Câblage manuel pour les sous cellules qui n'est nativement pas gérer par editCellAt,
            // donc aucune correspondance Échap/Entrée n'existe pour lui nativement.
            InputMap inputMap = ((JComponent) comp).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap actionMap = ((JComponent) comp).getActionMap();

            inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel-subcell-edit");
            actionMap.put("cancel-subcell-edit", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    cancelSubCellEdit();
                }
            });

            inputMap.put(KeyStroke.getKeyStroke("ENTER"), "commit-subcell-edit");
            actionMap.put("commit-subcell-edit", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    commitSubCellEdit(editor);
                }
            });

            activeSubCellEditorComponent = comp;
            activeSubCellEditorHit = hit;
            table.add(comp);
            comp.setVisible(true);
            comp.requestFocusInWindow();
        }

        private void commitSubCellEdit(TableCellEditor editor) {
            if (activeSubCellEditorHit == null) {
                return;
            }
            activeSubCellEditorHit.cell.value = editor.getCellEditorValue();
            endSubCellEdit();
        }

        private void cancelSubCellEdit() {
            endSubCellEdit(); // pas d'écriture — valeur d'origine conservée
        }

        private void endSubCellEdit() {
            if (activeSubCellEditorComponent != null) {
                table.remove(activeSubCellEditorComponent);
            }
            activeSubCellEditorComponent = null;
            activeSubCellEditorHit = null;
            table.repaint();
        }

        // ── Navigation clavier entre sous-cellules ──────────────────────────────
        /**
         * @return true si géré entièrement en interne. false si aucune
         * sous-cellule n'était focusée, ou si la direction sort de la cellule
         * (le focus interne est alors déjà libéré — l'appelant doit poursuivre
         * avec la navigation normale entre cellules).
         */
        public boolean tryNavigateInternal(int dr, int dc) {
            HTableGeometry.InternalCellHit focused = table.getFocusedInternalCell();
            if (focused == null || focused.parent == null) {
                return false;
            }

            InternalGrid grid = focused.parent.internalGrid;
            if (grid != null) {
                boolean goToSecond = (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL && dc > 0)
                        || (grid.getSplitType() == InternalGrid.SPLIT_HORIZONTAL && dr > 0);
                boolean goToFirst = (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL && dc < 0)
                        || (grid.getSplitType() == InternalGrid.SPLIT_HORIZONTAL && dr < 0);

                Cell target = null;
                Rectangle targetRect = null;

                int row = table.getFocusedRow();
                int col = table.getFocusedColumn();
                Rectangle cellRect = table.getCellRect(row, col, false);
                Rectangle[] parts = HTableGeometry.computeInternalRects(cellRect, grid);

                if (goToSecond && focused.cell == grid.getFirstCell()) {
                    target = grid.getSecondCell();
                    targetRect = parts[1];
                } else if (goToFirst && focused.cell == grid.getSecondCell()) {
                    target = grid.getFirstCell();
                    targetRect = parts[0];
                }

                if (target != null) {
                    HTableGeometry.InternalCellHit newHit = new HTableGeometry.InternalCellHit(target, targetRect, focused.parent);
                    table.setFocusedInternalCell(newHit);
                    table.setSelectedInternalCell(newHit);
                    return true;
                }
            }
            table.setFocusedInternalCell(null);
            table.setSelectedInternalCell(null);
            return false;
        }
    }

    /**
     * HTableResizeController: détection et exécution du redimensionnement
     * manuel des lignes et colonnes (bordure survolée, drag, relâche). Extrait
     * de HSuperTableController : aucune dépendance vers la sélection, les
     * cellules internes ou le clavier.
     */
    public class ResizeHandler {

        private static final int RESIZE_TOLERANCE = 4;

        private final HTable table;

        private int resizeDragStartY = -1;
        private int resizeDragStartX = -1;
        private int resizeOriginalSize = -1;

        public ResizeHandler(HTable table) {
            this.table = table;
        }

        /**
         * À appeler depuis mouseMoved en MODE_NORMAL.
         */
        public void detectResize(Point point) {
            HDefaultTableModel model = table.getHModel();
            int hoveredCol = table.columnAtPoint(point);
            int hoveredRow = table.rowAtPoint(point);

            // ── Détection resize de ligne ────────────────────────────────────────
            // On avance par BLOC (une ligne normale, ou l'étendue complète d'une
            // fusion verticale) plutôt que ligne par ligne — une fusion doit être
            // vue comme une seule cellule, ses frontières internes ne sont jamais
            // des bordures redimensionnables.
            int row = 0;
            while (row < table.getRowCount()) {
                int blockEnd = resolveRowBlockEnd(model, row, hoveredCol);
                Rectangle bottomRect = table.getCellRect(blockEnd, 0, true);
                int bordureBasse = bottomRect.y + bottomRect.height;

                if (Math.abs(point.y - bordureBasse) <= RESIZE_TOLERANCE) {
                    table.setResizeRowIndex(blockEnd);
                    table.setResizeColIndex(-1);
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                    return;
                }
                row = blockEnd + 1;
            }

            // ── Détection resize de colonne ──────────────────────────────────────
            int col = 0;
            while (col < table.getColumnCount()) {
                int blockEnd = resolveColBlockEnd(model, hoveredRow, col);
                Rectangle rect = table.getCellRect(0, blockEnd, true);
                int bordureDroite = rect.x + rect.width;

                if (Math.abs(point.x - bordureDroite) <= RESIZE_TOLERANCE) {
                    table.setResizeColIndex(blockEnd);
                    table.setResizeRowIndex(-1);
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    return;
                }
                col = blockEnd + 1;
            }

            table.setResizeRowIndex(-1);
            table.setResizeColIndex(-1);
            switch (table.getInteractionMode()) {
                case HTable.MODE_DRAW ->
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                case HTable.MODE_ERASE ->
                    table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                default ->
                    table.setCursor(Cursor.getDefaultCursor());
            }
        }

        /**
         * Dernière ligne du bloc (fusion ou ligne simple) couvrant (row,
         * hoveredCol).
         */
        private int resolveRowBlockEnd(HDefaultTableModel model, int row, int hoveredCol) {
            if (hoveredCol < 0) {
                return row;
            }
            Point origin = model.isAbsorbed(row, hoveredCol)
                    ? model.findMergeOrigin(row, hoveredCol)
                    : new Point(row, hoveredCol);
            if (origin == null) {
                return row;
            }
            int[] span = model.getSpan(origin.x, origin.y);
            return Math.max(row, origin.x + Math.max(1, span[0]) - 1);
        }

        /**
         * Dernière colonne du bloc (fusion ou colonne simple) couvrant
         * (hoveredRow, col).
         */
        private int resolveColBlockEnd(HDefaultTableModel model, int hoveredRow, int col) {
            if (hoveredRow < 0) {
                return col;
            }
            Point origin = model.isAbsorbed(hoveredRow, col)
                    ? model.findMergeOrigin(hoveredRow, col)
                    : new Point(hoveredRow, col);
            if (origin == null) {
                return col;
            }
            int[] span = model.getSpan(origin.x, origin.y);
            return Math.max(col, origin.y + Math.max(1, span[1]) - 1);
        }

        /**
         * @return true si un resize a démarré (événement à considérer
         * consommé).
         */
        public boolean handleMousePressed(MouseEvent e) {
            if (table.getResizeRowIndex() >= 0) {
                table.setResizingRow(true);
                resizeDragStartY = e.getY();
                resizeOriginalSize = table.getRowHeight(table.getResizeRowIndex());
                table.setResizePreviewY(e.getY());
                return true;
            }
            if (table.getResizeColIndex() >= 0) {
                int col = table.getResizeColIndex();
                int neighbor = (col + 1 < table.getColumnCount()) ? col + 1 : -1;
                table.setResizeColNeighborIndex(neighbor);
                table.setResizingCol(true);
                resizeDragStartX = e.getX();
                resizeOriginalSize = table.getColumnModel().getColumn(col).getWidth();
                if (neighbor >= 0) {
                    table.setResizeNeighborOriginalSize(
                            table.getColumnModel().getColumn(neighbor).getWidth());
                }
                table.setResizePreviewX(e.getX());
                return true;
            }
            return false;
        }

        /**
         * @return true si un resize était actif (événement à considérer
         * consommé).
         */
        public boolean handleMouseDragged(MouseEvent e) {
            if (table.isResizingRow()) {
                int row = table.getResizeRowIndex();
                int delta = e.getY() - resizeDragStartY;
                int newHeight = Math.max(20, resizeOriginalSize + delta);
                Rectangle cellRect = table.getCellRect(row, 0, true);
                table.setResizePreviewY(cellRect.y + newHeight);
                table.repaint();
                return true;
            }
            if (table.isResizingCol()) {
                int col = table.getResizeColIndex();
                int delta = e.getX() - resizeDragStartX;
                int newWidth = Math.max(30, resizeOriginalSize + delta);
                Rectangle cellRect = table.getCellRect(0, col, true);
                table.setResizePreviewX(cellRect.x + newWidth);
                table.repaint();
                return true;
            }
            return false;
        }

        /**
         * @return true si un resize était actif (événement à considérer
         * consommé).
         */
        public boolean handleMouseReleased(MouseEvent e) {
            if (table.isResizingRow()) {
                int row = table.getResizeRowIndex();
                int delta = e.getY() - resizeDragStartY;
                table.setRowHeight(row, Math.max(20, resizeOriginalSize + delta));
                resetRowState();
                table.setCursor(Cursor.getDefaultCursor());
                table.refreshUI();
                return true;
            }
            if (table.isResizingCol()) {
                int col = table.getResizeColIndex();
                int neighbor = table.getResizeColNeighborIndex();
                int delta = e.getX() - resizeDragStartX;

                if (Math.abs(delta) > 2) {
                    int newWidthLeft = Math.max(30, resizeOriginalSize + delta);
                    table.getColumnModel().getColumn(col).setWidth(newWidthLeft);
                    table.getColumnModel().getColumn(col).setPreferredWidth(newWidthLeft);
                    if (neighbor >= 0) {
                        int newWidthRight = Math.max(30,
                                table.getResizeNeighborOriginalSize() - delta);
                        table.getColumnModel().getColumn(neighbor).setWidth(newWidthRight);
                        table.getColumnModel().getColumn(neighbor).setPreferredWidth(newWidthRight);
                    }
                }
                resetColState();
                table.setCursor(Cursor.getDefaultCursor());
                table.refreshUI();
                return true;
            }
            return false;
        }

        private void resetRowState() {
            table.setResizingRow(false);
            table.setResizeRowIndex(-1);
            table.setResizePreviewY(-1);
            resizeDragStartY = -1;
            resizeOriginalSize = -1;
        }

        private void resetColState() {
            table.setResizingCol(false);
            table.setResizeColIndex(-1);
            table.setResizeColNeighborIndex(-1);
            table.setResizeNeighborOriginalSize(-1);
            table.setResizePreviewX(-1);
            resizeDragStartX = -1;
            resizeOriginalSize = -1;
        }
    }
    
    
    /**
     * Orchestre la détection et sélection manuelle
     */
    public class SelectionHandler {

        private final HTable table;

        public SelectionHandler(HTable table) {
            this.table = table;
        }

        public void handleSingleClick(int row, int col, MouseEvent e) {
            if (!isValidCell(row, col)) {
                return;
            }
            table.changeSelection(row, col, e.isControlDown(), e.isShiftDown());
            table.setFocusedCell(row, col);
        }

        public void handleDrag(int row, int col) {
            if (!isValidCell(row, col)) {
                return;
            }
            table.changeSelection(row, col, false, true);
        }

        public void handleRelease() {
            // La sélection est déjà à jour en continu — rien à ré-appliquer.
        }

        public void selectAll() {
            table.getCellSelectionModel().selectAll(table.getRowCount(), table.getColumnCount());
            table.refreshUI();
        }

        public void clear() {
            table.clearSelection();
        }

        public void ensureAnchorFor(int row, int col) {
            HTable.CellRange sel = table.getSelection();
            if (sel == null || !sel.contains(row, col)) {
                table.changeSelection(row, col, false, false);
            }
        }

        public void deleteSelectedRows() {
            if (!table.hasSelection()) {
                return;
            }
            HTable.CellRange sel = table.getSelection();
            List<Integer> rows = new ArrayList<>();
            for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
                rows.add(r);
            }
            Collections.sort(rows, Collections.reverseOrder());
            for (int r : rows) {
                if (r >= 0 && r < table.getRowCount()) {
                    table.getHModel().removeRow(r);
                }
            }
            table.setSelection(null);
        }

        public void navigate(int dr, int dc, boolean extend) {
            int focusRow = table.getFocusedRow();
            int focusCol = table.getFocusedColumn();
            if (focusRow < 0 || focusCol < 0) {
                return;
            }

            int newRow = Math.max(0, Math.min(table.getRowCount() - 1, focusRow + dr));
            int newCol = Math.max(0, Math.min(table.getColumnCount() - 1, focusCol + dc));

            table.changeSelection(newRow, newCol, false, extend);
            table.setFocusedCell(newRow, newCol);
        }

        private boolean isValidCell(int row, int col) {
            return row >= 0 && row < table.getRowCount()
                    && col >= 0 && col < table.getColumnCount();
        }
    }

}
