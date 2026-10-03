package hsupertable.view;

import hsupertable.HTable;
import hsupertable.model.HCellModel;
import hsupertable.model.HDefaultTableModel;
import hsupertable.model.Cell;
import hsupertable.style.HTableStyle;
import hsupertable.style.HTableStyle.HeaderStyle;
import hsupertable.geometry.HTableGeometry;
import hsupertable.geometry.HTableGeometry.InternalCellHit;
import hsupertable.menu.HeaderContext;
import hsupertable.menu.TableContext;
import hsupertable.model.InternalGrid;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import javax.swing.event.MouseInputListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * HBasicTableUI — Moteur de rendu visuel de HTable.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HBasicTableUI extends BasicTableUI {

    // =========================================================================
// HANDLERS D'INTERACTION — créés par installListeners(), retirés par
// uninstallListeners(). Chacun est produit par une factory protected,
// comme createMouseInputListener() dans le vrai BasicTableUI : une
// sous-classe peut remplacer un seul handler sans toucher aux autres.
// =========================================================================
    protected HTable table;
    protected SelectionHandler selectionHandler;
    protected ResizeHandler resizeHandler;
    protected InternalCellHandler internalCellHandler;
    protected HeaderHandler headerHandler;
    protected MouseInputListener mouseInputListener;
    protected FocusListener focusListener;

    // CONSTANTES DE RENDU
    private static final int CELL_PADDING_H = 12;  // padding horizontal par défaut
    private static final int CELL_PADDING_V = 6;   // padding vertical par défaut
    private static final int OVERLAY_ALPHA = 18;  // transparence des superpositions

    private static final int SORT_ICON_ZONE_WIDTH = 20;

    private static final Icon NEUTRAL_SORT_ICON = new NeutralSortIcon();

    // RENDERER DE L'EN-TÊTE
    /**
     * Renderer de l'en-tête — fond coloré, texte en gras, bordure basse.
     */
    private class ModernHeaderRenderer extends DefaultTableCellRenderer {

        private Icon sortIcon;

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(jTable, value, isSelected,
                    hasFocus, row, column);

            if (!(jTable instanceof HTable)) {
                sortIcon = null;
                return this;
            }

            HTable t = (HTable) jTable;
            HTableStyle style = t.getTableStyle();

            // ── Récupération du HeaderStyle de cette colonne ──────────────────
            // Si aucun style custom n'existe, on utilise les valeurs du style global
            HeaderStyle hs = t.headerStyles.get(column);

            // ── Couleur de fond ───────────────────────────────────────────────
            // Priorité : HeaderStyle custom > style global
            Color bg = (hs != null && hs.hasBackground())
                    ? hs.getBackground()
                    : (style != null ? style.getHeaderBackground() : Color.DARK_GRAY);
            setBackground(bg);

            // ── Couleur du texte ──────────────────────────────────────────────
            Color fg = (hs != null && hs.hasForeground())
                    ? hs.getForeground()
                    : (style != null ? style.getHeaderForeground() : Color.WHITE);
            setForeground(fg);

            // ── Police ────────────────────────────────────────────────────────
            // Si HeaderStyle custom existe, on construit la police depuis lui
            // Sinon on utilise la police globale du style
            Font baseFont = (style != null)
                    ? style.getHeaderFont()
                    : new Font("Segoe UI", Font.BOLD, 13);
            Font font = (hs != null)
                    ? hs.buildFont(baseFont)
                    : baseFont;
            setFont(font);

            // ── Alignement ────────────────────────────────────────────────────
            // Priorité : HeaderStyle custom > columnHeaderAlignments > LEFT
            int align = (hs != null)
                    ? hs.getHorizontalAlignment()
                    : t.getColumnHeaderAlignment(column);
            setHorizontalAlignment(align);

            // ── Bordure basse ─────────────────────────────────────────────────
            Color borderColor = (style != null)
                    ? style.getHeaderForeground().darker()
                    : Color.DARK_GRAY;
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, borderColor),
                    BorderFactory.createEmptyBorder(10, CELL_PADDING_H, 10,
                            CELL_PADDING_H + HTable.SORT_ICON_ZONE_WIDTH)
            ));

            SortOrder order = t.getViewColumnSortOrder(column);
            sortIcon = (order == SortOrder.UNSORTED)
                    ? NEUTRAL_SORT_ICON
                    : new SortArrowIcon(order == SortOrder.ASCENDING, fg);

            setOpaque(true);
            return this;

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (sortIcon == null) {
                return;
            }
            int zoneLeft = getWidth() - HTable.SORT_ICON_ZONE_WIDTH;
            int x = zoneLeft + (HTable.SORT_ICON_ZONE_WIDTH - sortIcon.getIconWidth()) / 2;
            int y = (getHeight() - sortIcon.getIconHeight()) / 2;
            sortIcon.paintIcon(this, g, x, y);
        }

    }

    // =========================================================================
    // RENDERER DES CELLULES
    // =========================================================================
    /**
     * Renderer principal des cellules.
     *
     * Il est appelé par paintCell() avec les dimensions réelles de la zone à
     * peindre (qui peut couvrir plusieurs cellules si fusion). Il lit le
     * HTableCellModel pour appliquer couleur, alignement et marges, puis
     * délègue le dessin du texte à paintCellText() pour gérer la rotation.
     */
    private class ModernCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);

            if (!(jTable instanceof HTable)) {
                setBackground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }

            HTable t = (HTable) jTable;
            HTableStyle style = t.getTableStyle();

            // Police et couleur de texte par défaut depuis le style
            if (style != null) {
                setFont(style.getCellFont());
                setForeground(style.getCellForeground());
            }

            setOpaque(true);

            // ── Couleur de fond ──────────────────────────────────────────────
            // Priorité : couleur cellule custom > highlight > hover > sélection
            //            > bande de couleur (colonnes) > alternance de lignes
            setBackground(HTableColorResolver.resolveCellBackground(t, style, row, column));

            // ── Couleur de texte custom ──────────────────────────────────────
            HCellModel cellModel = t.getHModel().getCellModel(
                    t.toModelRow(row), t.toModelColumn(column));
            if (cellModel.hasForeground()) {
                setForeground(cellModel.getForeground());
            }

            // ── Focus ────────────────────────────────────────────────────────
            boolean isFocused = (row == t.getFocusedRow() && column == t.getFocusedColumn());
            if (isFocused) {
                setFont(getFont().deriveFont(Font.BOLD));
            }

            // Les marges et l'alignement sont appliqués dans paintCell()
            // via les infos du HCellModel — pas ici, pour que la zone de
            // peinture soit correcte même en cas de fusion.
            setBorder(BorderFactory.createEmptyBorder());

            return this;
        }
    }

    // =========================================================================
    // INSTALLATION DU UI
    // =========================================================================
    @Override
    public void installUI(JComponent c) {
        // BasicTableUI.installUI() appelle lui-même installDefaults(),
        // installListeners() et installKeyboardActions() — par envoi virtuel,
        // donc vers NOS versions à nous puisqu'elles les surchargent. Le champ
        // doit être prêt AVANT cet appel, sinon nos méthodes le trouvent null.
        this.table = (c instanceof HTable) ? (HTable) c : null;
        super.installUI(c);
    }

    @Override
    public void uninstallUI(JComponent c) {
        // Même mécanisme en sens inverse : super.uninstallUI() appelle déjà
        // nos uninstallDefaults()/uninstallListeners()/uninstallKeyboardActions().
        super.uninstallUI(c);
        this.table = null;
    }

    @Override
    protected void installDefaults() {
        HTable t = table;
        HTableStyle style = t.getTableStyle();

        t.setRowHeight(36);
        t.setShowHorizontalLines(false);  // on dessine nous-mêmes les bordures
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(false);
        t.setOpaque(true);
        t.setBackground(Color.WHITE);

        if (style != null) {
            t.setGridColor(style.getGridColor());
            t.setSelectionBackground(style.getSelectionBackground());
            t.setSelectionForeground(style.getCellForeground());
        }

        // En-tête
        JTableHeader header = t.getTableHeader();
        if (header != null) {
            if (style != null) {
                header.setBackground(style.getHeaderBackground());
                header.setForeground(style.getHeaderForeground());
                header.setFont(style.getHeaderFont());
            }
            header.setReorderingAllowed(true);
            header.setDefaultRenderer(new ModernHeaderRenderer());
        }

        // Renderer de cellules — on l'installe pour tous les types
        ModernCellRenderer renderer = new ModernCellRenderer();
        t.setDefaultRenderer(Object.class, renderer);
        t.setDefaultRenderer(String.class, renderer);
        t.setDefaultRenderer(Integer.class, renderer);
        t.setDefaultRenderer(Double.class, renderer);
        t.setDefaultRenderer(Boolean.class, renderer);

        // Taille confortable par défaut
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        t.setPreferredScrollableViewportSize(
                new Dimension((int) (screen.width * 0.85), (int) (screen.height * 0.85)));
    }

    @Override
    protected void uninstallDefaults() {
        // Rien à restaurer explicitement 
    }

    @Override
    protected void installListeners() {
        selectionHandler = createSelectionHandler(table);
        resizeHandler = createResizeHandler(table);
        internalCellHandler = createInternalCellHandler(table);
        headerHandler = createHeaderHandler(table);

        mouseInputListener = createMouseInputListener();
        table.addMouseListener(mouseInputListener);
        table.addMouseMotionListener(mouseInputListener);

        focusListener = createFocusListener();
        table.addFocusListener(focusListener);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            headerHandler.install(header);
        }
    }

    @Override
    protected void uninstallListeners() {
        if (mouseInputListener != null) {
            table.removeMouseListener(mouseInputListener);
            table.removeMouseMotionListener(mouseInputListener);
            mouseInputListener = null;
        }
        if (focusListener != null) {
            table.removeFocusListener(focusListener);
            focusListener = null;
        }
        JTableHeader header = table.getTableHeader();
        if (header != null && headerHandler != null) {
            headerHandler.dispose(header);           
        }

        selectionHandler = null;
        resizeHandler = null;
        internalCellHandler = null;
        headerHandler = null;
    }

    protected SelectionHandler createSelectionHandler(HTable t) {
        return new SelectionHandler(t);
    }

    protected ResizeHandler createResizeHandler(HTable t) {
        return new ResizeHandler(t);
    }

    protected InternalCellHandler createInternalCellHandler(HTable t) {
        return new InternalCellHandler(t);
    }

    protected HeaderHandler createHeaderHandler(HTable t) {
        return new HeaderHandler(t);
    }

    @Override
    protected MouseInputListener createMouseInputListener() {
        return new TableMouseHandler(table, selectionHandler, resizeHandler, internalCellHandler);
    }

    @Override
    protected FocusListener createFocusListener() {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                table.setVisualState("focused", true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                table.setVisualState("focused", false);
            }
        };
    }

    //GETTERS POUR LES HANDLERS 
    public SelectionHandler getSelectionHandler() {
        return selectionHandler;
    }

    public ResizeHandler getResizeHandler() {
        return resizeHandler;
    }

    public InternalCellHandler getInternalCellHandler() {
        return internalCellHandler;
    }

    public HeaderHandler getHeaderHandler() {
        return headerHandler;
    }

    @Override
    protected void installKeyboardActions() {
        InputMap inputMap = new InputMap();
        ActionMap actionMap = new ActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "selectAll");
        actionMap.put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionHandler.selectAll();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hsuperEscape");
        actionMap.put("hsuperEscape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.clearAllVisualStates();
                selectionHandler.clear();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelectedRows");
        actionMap.put("deleteSelectedRows", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionHandler.deleteSelectedRows();
            }
        });

        bindNavigation(inputMap, actionMap, KeyEvent.VK_RIGHT, 0, 1, "selectNextColumn");
        bindNavigation(inputMap, actionMap, KeyEvent.VK_LEFT, 0, -1, "selectPreviousColumn");
        bindNavigation(inputMap, actionMap, KeyEvent.VK_DOWN, 1, 0, "selectNextRow");
        bindNavigation(inputMap, actionMap, KeyEvent.VK_UP, -1, 0, "selectPreviousRow");

        table.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
        table.setActionMap(actionMap);
    }

    @Override
    protected void uninstallKeyboardActions() {
        table.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
        table.setActionMap(null);
    }

    /**
     * Enregistre une flèche et sa variante Maj+flèche sous les mêmes noms que
     * ceux de JTable.
     */
    private void bindNavigation(InputMap inputMap, ActionMap actionMap, int keyCode, int dr, int dc, String baseName) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), baseName);
        actionMap.put(baseName, new NavigateAction(dr, dc, false));

        String extendName = baseName + "ExtendSelection";
        inputMap.put(KeyStroke.getKeyStroke(keyCode, InputEvent.SHIFT_DOWN_MASK), extendName);
        actionMap.put(extendName, new NavigateAction(dr, dc, true));
    }

    // =========================================================================
    // MÉTHODE PRINCIPALE DE PEINTURE
    // =========================================================================
    /**
     * Point d'entrée du rendu.On peint dans cet ordre : 1.Le fond du tableau
     * (couleur unie derrière tout) 2. Les cellules (en sautant les absorbées,
     * en élargissant les fusionnées) 3. La grille globale (si activée) 4. Les
     * bordures custom par cellule (par-dessus la grille) 5. Les superpositions
     * de sélection 6. L'indicateur de focus
     *
     * @param g
     * @param c
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        if (!(c instanceof HTable)) {
            super.paint(g, c);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        applyRenderingHints(g2);

        HTable t = (HTable) c;

        // Fond
        g2.setColor(t.getBackground());
        g2.fillRect(0, 0, t.getWidth(), t.getHeight());

        // Calcul de la zone visible (pour ne peindre que ce qui est à l'écran)
        Rectangle clip = g2.getClipBounds();

        int firstRow = t.rowAtPoint(new Point(clip.x, clip.y));
        int lastRow = t.rowAtPoint(new Point(clip.x, clip.y + clip.height - 1));
        if (firstRow < 0) {
            firstRow = 0;
        }
        if (lastRow < 0) {
            lastRow = t.getRowCount() - 1;
        }

        int firstCol = t.columnAtPoint(new Point(clip.x, clip.y));
        int lastCol = t.columnAtPoint(new Point(clip.x + clip.width - 1, clip.y));
        if (firstCol < 0) {
            firstCol = 0;
        }
        if (lastCol < 0) {
            lastCol = t.getColumnCount() - 1;
        }

        // Peinture des cellules
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                paintCell(g2, t, row, col);
            }
        }

        // Grille globale (quadrillage léger, si visible)
        if (t.isGridVisible()) {
            paintGlobalGrid(g2, t, firstRow, lastRow, firstCol, lastCol);
        }

        // Bordures custom par cellule (par-dessus la grille)
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                HTableBorderPainter.paintCellBorders(g2, t, row, col);
            }
        }

        // Superpositions de sélection
        paintSelectionOverlays(g2, t);

        // Indicateur de focus sur la ou la sous cellule active        
        if (t.getFocusedInternalCell() == null) {
            paintFocusIndicator(g2, t);
        } else {
            paintInternalHover(g2, t);
            paintInternalSelection(g2, t);
            paintInternalFocus(g2, t);
        }

        // Prévisualisation du trait crayon (mode dessiner)
        paintDrawPreview(g2, t);
        paintResizePreview(g2, t);
        g2.dispose();
    }

    // =========================================================================
    // RENDU PRINCIPAL D'UNE CELLULE
    // =========================================================================
    /**
     * Point d'entrée du rendu d'une cellule. Délègue au moteur de rendu
     * structurel (gestion des subdivisions internes).
     */
    private void paintCell(Graphics2D g2, HTable t, int row, int col) {

        HDefaultTableModel model = t.getHModel();
        int modelRow = t.toModelRow(row);
        int modelCol = t.toModelColumn(col);
        Cell cell = model.getCell(modelRow, modelCol);
        if (cell.isAbsorbed()) {
            return;
        }

        Rectangle cellRect = t.getCellRect(row, col, false);
        if (cell.spanRow > 1 || cell.spanCol > 1) {
            cellRect = HTableGeometry.computeMergedRect(t, modelRow, modelCol, cell.spanRow, cell.spanCol);
        }

        // false = cellule racine, pas une sous-cellule
        paintCellStructure(g2, t, cell, cellRect, row, col, false);
    }

    // =========================================================================
    // MOTEUR DE RENDU STRUCTUREL (NORMAL + SUBDIVISIONS)
    // =========================================================================
    /**
     * Rend une cellule en tenant compte de ses éventuelles subdivisions
     * internes.
     */
    private void paintCellStructure(
            Graphics2D g2, HTable t,
            Cell cell,
            Rectangle rect, int row, int col,
            boolean isSubCell
    ) {
        if (cell.internalGrid == null) {
            Object value = cell.value;
            if (!isSubCell && value == null) {
                value = t.getValueAt(row, col);
            }

            if (value != null && hasCustomRendererFor(t, t.getColumnClass(col))) {
                paintViaTypedRenderer(g2, t, value, rect, row, col);
                return;
            }

            // Chemin actuel — texte riche, inchangé
            HTableStyle style = t.getTableStyle();
            HCellModel cModel = t.getHModel().getCellModel(
                    t.toModelRow(row), t.toModelColumn(col));

            Color bg;
            if (cell.style != null && cell.style.hasBackground()) {
                bg = cell.style.getBackground();
            } else {
                bg = HTableColorResolver.resolveCellBackground(t, style, row, col);
            }
            if (bg == null) {
                bg = style != null ? style.getCellBackground() : Color.WHITE;
            }
            g2.setColor(bg);
            g2.fillRect(rect.x, rect.y, rect.width, rect.height);

            Color overlay = HTableColorResolver.resolveSelectionOverlay(t, style, row, col);
            if (overlay != null) {
                g2.setColor(overlay);
                g2.fillRect(rect.x, rect.y, rect.width, rect.height);
            }

            if (value != null) {
                HCellModel effectiveModel = (cell.style != null) ? cell.style : cModel;
                HTableTextPainter.paintCellText(g2, t, effectiveModel, style, rect, value.toString(), row, col);
            }
            return;
        }

        InternalGrid grid = cell.internalGrid;
        Rectangle[] parts = HTableGeometry.computeInternalRects(rect, grid);
        Cell first = grid.getFirstCell();
        Cell second = grid.getSecondCell();

        paintCellStructure(g2, t, first, parts[0], row, col, true);
        paintCellStructure(g2, t, second, parts[1], row, col, true);

        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1f));
        if (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL) {
            int x = parts[0].x + parts[0].width;
            g2.drawLine(x, rect.y, x, rect.y + rect.height);
        } else {
            int y = parts[0].y + parts[0].height;
            g2.drawLine(rect.x, y, rect.x + rect.width, y);
        }

        HTableBorderPainter.paintInternalBorders(g2, first, parts[0], second, parts[1]);
    }

    /**
     * Peint une feuille (racine ou sous-cellule) via le pipeline
     * TableCellRenderer standard — bascule active uniquement quand un renderer
     * non-défaut existe pour le type réel de la colonne d'origine (row, col),
     * quelle que soit la profondeur de la feuille dans l'arbre de subdivision.
     */
    private void paintViaTypedRenderer(Graphics2D g2, HTable t, Object value, Rectangle rect, int row, int col) {
        TableCellRenderer renderer = t.getCellRenderer(row, col);
        Component comp = renderer.getTableCellRendererComponent(
                t, value, t.isCellSelected(row, col), false, row, col);
        comp.setBounds(0, 0, rect.width, rect.height);

        Graphics2D cellG2 = (Graphics2D) g2.create(rect.x, rect.y, rect.width, rect.height);
        comp.paint(cellG2);
        cellG2.dispose();
    }

// =========================================================================
// RENDU D'UNE SOUS-CELLULE INTERNE
// =========================================================================
    private void paintInternalHover(Graphics2D g2, HTable t) {

        InternalCellHit hit = t.getHoveredInternalCell();

        if (hit == null) {
            return;
        }

        Rectangle r = hit.bounds;

        g2.setColor(new Color(37, 99, 235, 40));

        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private void paintInternalSelection(Graphics2D g2, HTable t) {

        InternalCellHit hit = t.getSelectedInternalCell();

        if (hit == null) {
            return;
        }

        Rectangle r = hit.bounds;

        g2.setColor(new Color(37, 99, 235, 70));

        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private void paintInternalFocus(Graphics2D g2, HTable t) {

        InternalCellHit hit = t.getFocusedInternalCell();

        if (hit == null) {
            return;
        }

        Rectangle r = hit.bounds;

        g2.setColor(new Color(37, 99, 235));

        g2.setStroke(new BasicStroke(1.5f));

        g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);

        g2.setStroke(new BasicStroke(1f));
    }

    // =========================================================================
    // GRILLE GLOBALE
    // =========================================================================
    /**
     * Dessine le quadrillage global du tableau — des lignes légères entre
     * toutes les cellules. On saute les bordures qui passent à l'intérieur
     * d'une zone fusionnée pour ne pas "couper" visuellement la fusion.
     */
    private void paintGlobalGrid(Graphics2D g2, HTable t,
            int firstRow, int lastRow,
            int firstCol, int lastCol) {

        HDefaultTableModel model = t.getHModel();
        Color gridColor = t.getGridColor();
        if (gridColor == null) {
            gridColor = new Color(220, 220, 220);
        }

        g2.setColor(gridColor);
        g2.setStroke(new BasicStroke(1f));

        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                // On ne dessine rien pour les cellules absorbées
                int modelRow = t.toModelRow(row);
                int modelCol = t.toModelColumn(col);
                if (model.isAbsorbed(modelRow, modelCol)) {
                    continue;
                }
                // APRÈS
                Cell cell = model.getCell(modelRow, modelCol);
                Rectangle r = (cell.spanRow > 1 || cell.spanCol > 1)
                        ? HTableGeometry.computeMergedRect(t, modelRow, modelCol, cell.spanRow, cell.spanCol)
                        : t.getCellRect(row, col, false);

                // Ligne du bas — seulement si on n'est pas dans une fusion
                // qui continue vers le bas
                g2.drawLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height);

                // Ligne de droite
                g2.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
            }
        }
    }

    // =========================================================================
    // SUPERPOSITIONS ET FOCUS
    // =========================================================================
    /**
     * Dessine une superposition semi-transparente sur les lignes sélectionnées.
     */
    private void paintSelectionOverlays(Graphics2D g2, HTable t) {
        for (Integer selectedRow : t.getRowsSelected()) {
            if (selectedRow < 0 || selectedRow >= t.getRowCount()) {
                continue;
            }
            Rectangle first = t.getCellRect(selectedRow, 0, true);
            int totalW = 0;
            for (int c = 0; c < t.getColumnCount(); c++) {
                totalW += t.getColumnModel().getColumn(c).getWidth();
            }
            g2.setColor(new Color(59, 130, 246, OVERLAY_ALPHA));
            g2.fillRect(first.x, first.y, totalW, first.height);
        }
    }

    /**
     * Dessine un liseré coloré autour de la cellule en focus. On le dessine en
     * dernier pour qu'il soit toujours visible par-dessus tout.
     */
    private void paintFocusIndicator(Graphics2D g2, HTable t) {
        int fr = t.getFocusedRow();
        int fc = t.getFocusedColumn();
        if (fr < 0 || fc < 0) {
            return;
        }
        if (fr >= t.getRowCount() || fc >= t.getColumnCount()) {
            return;
        }

        // APRÈS
        HDefaultTableModel model = t.getHModel();
        int modelFr = t.toModelRow(fr);
        int modelFc = t.toModelColumn(fc);
        Cell cell = model.getCell(modelFr, modelFc);
        Rectangle rect = (cell.spanRow > 1 || cell.spanCol > 1)
                ? HTableGeometry.computeMergedRect(t, modelFr, modelFc, cell.spanRow, cell.spanCol)
                : t.getCellRect(fr, fc, false);

        HTableStyle style = t.getTableStyle();
        Color focusColor = (style != null) ? style.getFocusBorderColor()
                : new Color(13, 110, 253);

        g2.setColor(focusColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
        g2.setStroke(new BasicStroke(1f));
    }

    // =========================================================================
    // RÉSOLUTION DE LA COULEUR DE FOND
    // =========================================================================
    // =========================================================================
    // UTILITAIRES
    // =========================================================================
    /**
     * Dessine le trait de prévisualisation pendant le glisser en mode crayon.
     */
    private void paintDrawPreview(Graphics2D g2, HTable t) {
        if (internalCellHandler == null || !internalCellHandler.isDrawing()) {
            return;
        }
        int x1 = internalCellHandler.getDrawStartX();
        int y1 = internalCellHandler.getDrawStartY();
        int x2 = internalCellHandler.getDrawEndX();
        int y2 = internalCellHandler.getDrawEndY();

        if (x1 < 0 || y1 < 0) {
            return;
        }

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        // ── Résolution via resolvePoint ───────────────────────────────────────
        int[] resolved = t.resolvePoint(new Point(x1, y1));
        int row = resolved[0];
        int col = resolved[1];
        if (row < 0 || col < 0) {
            return;
        }

        // ── Rectangle de référence — fusionné si nécessaire ───────────────────
        // APRÈS
        int modelRow = t.toModelRow(row);
        int modelCol = t.toModelColumn(col);
        Cell cell = t.getHModel().getCell(modelRow, modelCol);
        Rectangle cellRect = (cell.spanRow > 1 || cell.spanCol > 1)
                ? HTableGeometry.computeMergedRect(t, modelRow, modelCol, cell.spanRow, cell.spanCol)
                : t.getCellRect(row, col, false);

        g2.setColor(new Color(37, 99, 235, 180));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{6f, 3f}, 0f));

        if (dx >= dy) {
            int splitX = x2;
            splitX = Math.max(cellRect.x + 10, splitX);
            splitX = Math.min(cellRect.x + cellRect.width - 10, splitX);
            g2.drawLine(splitX, cellRect.y, splitX, cellRect.y + cellRect.height);
        } else {
            int splitY = y2;
            splitY = Math.max(cellRect.y + 10, splitY);
            splitY = Math.min(cellRect.y + cellRect.height - 10, splitY);
            g2.drawLine(cellRect.x, splitY, cellRect.x + cellRect.width, splitY);
        }

        g2.setStroke(new BasicStroke(1f));
    }

    /**
     * Dessine la ligne de prévisualisation pendant le redimensionnement manuel
     * d'une ligne ou d'une colonne.
     */
    private void paintResizePreview(Graphics2D g2, HTable t) {

        // Couleur et style communs
        g2.setColor(new Color(37, 99, 235, 180));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{6f, 3f}, 0f));

        if (t.isResizingRow()) {
            int y = t.getResizePreviewY();
            if (y < 0) {
                return;
            }
            // Ligne horizontale sur toute la largeur du tableau
            g2.drawLine(0, y, t.getWidth(), y);
        }

        if (t.isResizingCol()) {
            int x = t.getResizePreviewX();
            if (x < 0) {
                return;
            }
            // Ligne verticale sur toute la hauteur du tableau
            g2.drawLine(x, 0, x, t.getHeight());
        }

        g2.setStroke(new BasicStroke(1f));
    }

    /**
     * Vrai si un renderer autre que celui installé par défaut (Object.class,
     * texte riche) a été enregistré pour cette classe — condition de bascule
     * vers le pipeline TableCellRenderer standard.
     */
    private boolean hasCustomRendererFor(HTable t, Class<?> valueClass) {
        TableCellRenderer renderer = t.getDefaultRenderer(valueClass);
        return renderer != null && !(renderer instanceof ModernCellRenderer);
    }

    private boolean hasCustomEditorFor(HTable t, Class<?> valueClass) {
        TableCellEditor editor = t.getDefaultEditor(valueClass);
        return editor != null;
    }

    /**
     * Active l'anticrénelage pour un rendu net sur tous les écrans.
     */
    private void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    //===========================================================================================================
    // CLASSES INTERNES INTERNE ENCAPSULANT LES ALGORITHMES DE PEINTURES
    //================================================================================================================
    /**
     * HTableBorderPainter — dessin des bordures personnalisées par côté, pour
     * une cellule normale/fusionnée (via son HSuperTableCellModel) ou pour une
     * sous-cellule interne. Un seul mécanisme de dessin, appliqué aux deux cas.
     */
    public final class HTableBorderPainter {

        private HTableBorderPainter() {
        }

        public static BasicStroke createStroke(float thickness, int style) {
            if (style == HTable.BORDER_DASHED) {
                return new BasicStroke(thickness, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f);
            }
            if (style == HTable.BORDER_DOTTED) {
                return new BasicStroke(thickness, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 10f, new float[]{1f, 4f}, 0f);
            }
            return new BasicStroke(thickness);
        }

        /**
         * Dessine les bordures définies dans un HCellModel sur le périmètre du
         * rectangle donné.
         *
         * @param g2
         * @param m
         * @param rect
         */
        public static void paintBorders(Graphics2D g2, HCellModel m, Rectangle rect) {
            if (m == null || !m.hasAnyBorder()) {
                return;
            }
            if (m.hasBorderTop()) {
                g2.setColor(m.getBorderTopColor());
                g2.setStroke(createStroke(m.getBorderTopThickness(), m.getBorderTopStyle()));
                g2.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
            }
            if (m.hasBorderBottom()) {
                g2.setColor(m.getBorderBottomColor());
                g2.setStroke(createStroke(m.getBorderBottomThickness(), m.getBorderBottomStyle()));
                g2.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
            }
            if (m.hasBorderLeft()) {
                g2.setColor(m.getBorderLeftColor());
                g2.setStroke(createStroke(m.getBorderLeftThickness(), m.getBorderLeftStyle()));
                g2.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
            }
            if (m.hasBorderRight()) {
                g2.setColor(m.getBorderRightColor());
                g2.setStroke(createStroke(m.getBorderRightThickness(), m.getBorderRightStyle()));
                g2.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
            }
            g2.setStroke(new BasicStroke(1f));
        }

        /**
         * Bordures custom d'une cellule normale ou fusionnée (row, col), via
         * son HCellModel.
         *
         * @param g2
         * @param t
         * @param row
         * @param col
         */
        public static void paintCellBorders(Graphics2D g2, HTable t, int row, int col) {
            HDefaultTableModel model = t.getHModel();
            if (model.isAbsorbed(row, col)) {
                return;
            }
            int modelRow = t.toModelRow(row);
            int modelCol = t.toModelColumn(col);
            if (model.isAbsorbed(modelRow, modelCol)) {
                return;
            }
            HCellModel cModel = model.getCellModel(modelRow, modelCol);
            if (!cModel.hasAnyBorder()) {
                return;
            }
            Rectangle rect = HTableGeometry.getCellBounds(t, row, col);
            paintBorders(g2, cModel, rect);
        }

        /**
         * Bordures custom des deux sous-cellules d'une InternalGrid déjà
         * décomposée en rectangles.
         *
         * @param g2
         * @param first
         * @param firstRect
         * @param second
         * @param secondRect
         */
        public static void paintInternalBorders(Graphics2D g2, Cell first, Rectangle firstRect,
                Cell second, Rectangle secondRect) {
            if (first != null) {
                paintBorders(g2, first.style, firstRect);
            }
            if (second != null) {
                paintBorders(g2, second.style, secondRect);
            }
        }
    }

    /**
     * HTableColorResolver — détermine la couleur de fond de base d'une cellule
     * (priorités : custom > ligne/colonne mise en valeur > bandes > alternance)
     * et la superposition d'état interactif à peindre par-dessus (surbrillance,
     * survol, sélection). Logique auparavant dupliquée entre ModernCellRenderer
     * et HBasicTableUI.paintCellStructure.
     */
    public final class HTableColorResolver {

        private HTableColorResolver() {
        }

        public static Color resolveCellBackground(HTable t, HTableStyle style,
                int row, int col) {
            HDefaultTableModel model = t.getHModel();
            HCellModel cModel = model.getCellModel(t.toModelRow(row), t.toModelColumn(col));

            if (cModel.hasBackground()) {
                return cModel.getBackground();
            }

            Color rowBg = t.getRowBackground(row);
            if (rowBg != null) {
                return rowBg;
            }

            if (style == null) {
                return Color.WHITE;
            }

            if (t.isTotalRowEnabled() && row == t.getRowCount() - 1) {
                return style.getTotalRowBackground();
            }
            if (t.isFirstColumnHighlighted() && col == 0) {
                return style.getFirstColumnBackground();
            }
            if (t.isLastColumnHighlighted() && col == t.getColumnCount() - 1) {
                return style.getLastColumnBackground();
            }
            if (t.isBandedColumns()) {
                return (col % 2 == 0) ? style.getCellBackground()
                        : style.getCellAlternateBackground();
            }
            if (t.isBandedRows()) {
                return (row % 2 == 0) ? style.getCellBackground()
                        : style.getCellAlternateBackground();
            }
            return style.getCellBackground();
        }

        public static Color resolveSelectionOverlay(HTable t, HTableStyle style,
                int row, int col) {

            if (row == t.getHighlightedRow()) {
                return style != null ? style.getHighlightBackground()
                        : new Color(13, 110, 253, 40);
            }
            if (row == t.getHoveredRow()) {
                return style != null ? style.getHoverBackground()
                        : new Color(13, 110, 253, 20);
            }
            if (t.hasSelection() && t.getSelection().contains(row, col)) {
                return style != null
                        ? new Color(
                                style.getSelectionBackground().getRed(),
                                style.getSelectionBackground().getGreen(),
                                style.getSelectionBackground().getBlue(),
                                70)
                        : new Color(13, 110, 253, 70);
            }
            if (t.getRowsSelected().contains(row)) {
                return style != null ? style.getSelectionBackground()
                        : new Color(13, 110, 253, 30);
            }
            return null;
        }
    }

    /**
     * HTableTextPainter — rendu du texte d'une cellule : alignement, marges,
     * direction (horizontal ou pivoté), troncature. Reçoit un rectangle et un
     * modèle déjà résolus — ignore tout de la fusion ou de la subdivision.
     */
    public final class HTableTextPainter {

        private static final int CELL_PADDING_H = 12;
        private static final int CELL_PADDING_V = 6;

        private HTableTextPainter() {
        }

        public static void paintCellText(Graphics2D g2, HTable t, HCellModel cModel,
                HTableStyle style, Rectangle rect, String text,
                int row, int col) {

            Font font = (style != null) ? style.getCellFont()
                    : new Font("Segoe UI", Font.PLAIN, 13);

            if (row == t.getFocusedRow() && col == t.getFocusedColumn()) {
                font = font.deriveFont(Font.BOLD);
            }

            Color fg = Color.BLACK;
            if (cModel.hasForeground()) {
                fg = cModel.getForeground();
            } else if (style != null) {
                fg = style.getCellForeground();
            }

            Insets margins;
            if (cModel.hasCustomMargins()) {
                margins = cModel.getMargins();
            } else {
                margins = t.getDefaultCellMargins();
                if (margins == null) {
                    margins = new Insets(CELL_PADDING_V, CELL_PADDING_H, CELL_PADDING_V, CELL_PADDING_H);
                }
            }

            int contentX = rect.x + margins.left;
            int contentY = rect.y + margins.top;
            int contentW = rect.width - margins.left - margins.right;
            int contentH = rect.height - margins.top - margins.bottom;
            if (contentW <= 0 || contentH <= 0) {
                return;
            }

            int hAlign = cModel.getHorizontalAlignment();
            int vAlign = cModel.getVerticalAlignment();
            int direction = cModel.getTextDirection();

            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            if (direction == HTable.TEXT_VERTICAL_UP || direction == HTable.TEXT_VERTICAL_DOWN) {
                paintRotatedText(g2, text, font, fg, rect, margins, hAlign, vAlign, direction);
            } else {
                paintHorizontalText(g2, text, font, fg, fm, contentX, contentY, contentW, contentH, hAlign, vAlign);
            }
        }

        private static void paintHorizontalText(Graphics2D g2, String text, Font font, Color fg,
                FontMetrics fm, int x, int y, int w, int h,
                int hAlign, int vAlign) {
            g2.setColor(fg);

            String displayText = truncateText(fm, text, w);
            int textW = fm.stringWidth(displayText);
            int textH = fm.getAscent();

            int drawX;
            drawX = switch (hAlign) {
                case SwingConstants.CENTER ->
                    x + (w - textW) / 2;
                case SwingConstants.RIGHT ->
                    x + w - textW;
                default ->
                    x;
            };

            int drawY;
            drawY = switch (vAlign) {
                case SwingConstants.TOP ->
                    y + textH;
                case SwingConstants.BOTTOM ->
                    y + h;
                default ->
                    y + (h + textH) / 2 - fm.getDescent();
            };

            g2.drawString(displayText, drawX, drawY);
        }

        private static void paintRotatedText(Graphics2D g2, String text, Font font, Color fg,
                Rectangle rect, Insets margins,
                int hAlign, int vAlign, int direction) {

            AffineTransform originalTransform = g2.getTransform();

            int cx = rect.x + rect.width / 2;
            int cy = rect.y + rect.height / 2;

            double angle = (direction == HTable.TEXT_VERTICAL_UP)
                    ? -Math.PI / 2
                    : Math.PI / 2;

            g2.translate(cx, cy);
            g2.rotate(angle);

            int availW = rect.height - margins.top - margins.bottom;
            int availH = rect.width - margins.left - margins.right;

            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            String displayText = truncateText(fm, text, availW);

            int textW = fm.stringWidth(displayText);
            int textH = fm.getAscent();

            int drawX;
            drawX = switch (hAlign) {
                case SwingConstants.CENTER ->
                    -textW / 2;
                case SwingConstants.RIGHT ->
                    availW / 2 - textW;
                default ->
                    -availW / 2;
            };

            int drawY;
            drawY = switch (vAlign) {
                case SwingConstants.TOP ->
                    -availH / 2 + textH;
                case SwingConstants.BOTTOM ->
                    availH / 2;
                default ->
                    textH / 2 - fm.getDescent();
            };

            g2.setColor(fg);
            g2.drawString(displayText, drawX, drawY);
            g2.setTransform(originalTransform);
        }

        public static String truncateText(FontMetrics fm, String text, int availableWidth) {
            if (fm.stringWidth(text) <= availableWidth) {
                return text;
            }
            String ellipsis = "…";
            int ellipsisW = fm.stringWidth(ellipsis);
            StringBuilder sb = new StringBuilder(text);

            while (sb.length() > 0 && fm.stringWidth(sb.toString()) + ellipsisW > availableWidth) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString() + ellipsis;
        }
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

            if (e.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(e)
                    && isInSortIconZone(e.getPoint(), col)) {
                cycleSort(col);
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

            if (isInSortIconZone(e.getPoint(), col)) {
                return; // le clic sur l'icône se traite dans mouseClicked
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

    private static class TableMouseHandler implements MouseInputListener {

        private final HTable table;
        private final SelectionHandler selectionHandler;
        private final ResizeHandler resizeHandler;
        private final InternalCellHandler internalCellHandler;

        TableMouseHandler(HTable table, SelectionHandler selectionHandler,
                ResizeHandler resizeHandler, InternalCellHandler internalCellHandler) {
            this.table = table;
            this.selectionHandler = selectionHandler;
            this.resizeHandler = resizeHandler;
            this.internalCellHandler = internalCellHandler;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
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

            selectionHandler.ensureAnchorFor(row, col);
            table.setFocusedCell(row, col);

            HDefaultTableModel model = table.getHModel();
            Cell cell = model.getCell(table.toModelRow(row), table.toModelColumn(col));
            if (cell.isAbsorbed() && cell.mergeOrigin != null) {
                cell = model.getCell(cell.mergeOrigin.x, cell.mergeOrigin.y);
            }

            HTableGeometry.InternalCellHit internalHit = HTableGeometry.getInternalCellAt(table, mousePos);
            if (internalHit != null && internalHit.parent != null) {
                table.setFocusedInternalCell(internalHit);
                table.setSelectedInternalCell(internalHit);
            }

            TableContext ctx = new TableContext(table, row, col, cell, internalHit, mousePos);
            table.showContextMenu(ctx, mousePos.x, mousePos.y);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
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
                return;
            }

            if (e.getClickCount() == 2 && table.isCellEditable(row, col)) {
                table.editCellAt(row, col, e);
                return;
            }

            internalCellHandler.updateInternalFocusOnPress(e.getPoint());
            selectionHandler.handleSingleClick(row, col, e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
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

        @Override
        public void mouseReleased(MouseEvent e) {
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

        @Override
        public void mouseMoved(MouseEvent e) {
            if (table.getInteractionMode() == HTable.MODE_NORMAL) {
                resizeHandler.detectResize(e.getPoint());
            }

            if (table.getResizeRowIndex() < 0 && table.getResizeColIndex() < 0) {
                if (table.hasSelection()) {
                    if (table.getHoveredRow() >= 0) {
                        table.setHoveredRow(-1);
                    }
                    if (table.getHoveredInternalCell() != null) {
                        table.setHoveredInternalCell(null);
                    }
                } else {
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

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        private boolean isValidCell(int row, int col) {
            return row >= 0 && row < table.getRowCount()
                    && col >= 0 && col < table.getColumnCount();
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
         * @param e
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
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelSubCellEdit();
                }
            });

            inputMap.put(KeyStroke.getKeyStroke("ENTER"), "commit-subcell-edit");
            actionMap.put("commit-subcell-edit", new AbstractAction() {
                @Override
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
         *
         * @param point
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
            java.util.List<Integer> rows = new ArrayList<>();
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

    private class NavigateAction extends AbstractAction {

        private final int dr;
        private final int dc;
        private final boolean extend;

        NavigateAction(int dr, int dc, boolean extend) {
            this.dr = dr;
            this.dc = dc;
            this.extend = extend;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!extend && internalCellHandler.tryNavigateInternal(dr, dc)) {
                return;
            }
            selectionHandler.navigate(dr, dc, extend);
        }
    }

    private static class SortArrowIcon implements Icon {

        private final boolean ascending;
        private final Color color;

        SortArrowIcon(boolean ascending, Color color) {
            this.ascending = ascending;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int[] xs = {x, x + 8, x + 4};
            int[] ys = ascending ? new int[]{y + 5, y + 5, y} : new int[]{y, y, y + 5};
            g2.fillPolygon(xs, ys, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 5;
        }
    }

    private boolean isInSortIconZone(Point point, int col) {
        Rectangle r = table.getTableHeader().getHeaderRect(col);
        return point.x >= r.x + r.width - HTable.SORT_ICON_ZONE_WIDTH && point.x <= r.x + r.width;
    }

    private void cycleSort(int viewCol) {
        SortOrder current = table.getViewColumnSortOrder(viewCol);
        int modelCol = table.toModelColumn(viewCol);
        switch (current) {
            case UNSORTED ->
                table.sortByColumn(modelCol, SortOrder.ASCENDING);
            case ASCENDING ->
                table.sortByColumn(modelCol, SortOrder.DESCENDING);
            case DESCENDING ->
                table.clearSort();
        }
    }

    

    /**
     * Icône neutre affichée sur toute colonne triable mais non triée — deux
     * petits triangles gris clair (haut + bas), visibles dès l'affichage du
     * tableau. Sans état propre : une seule instance partagée suffit.
     */
    private static class NeutralSortIcon implements Icon {

        private static final Color COLOR = new Color(190, 190, 190);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR);
            g2.fillPolygon(new int[]{x, x + 8, x + 4}, new int[]{y + 3, y + 3, y}, 3);
            g2.fillPolygon(new int[]{x, x + 8, x + 4}, new int[]{y + 7, y + 7, y + 10}, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }

}
