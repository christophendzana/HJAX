package hsupertable.view;

import hsupertable.HTable;
import hsupertable.controller.HTableController;
import hsupertable.model.HCellModel;
import hsupertable.model.HDefaultTableModel;
import hsupertable.model.Cell;
import hsupertable.style.HTableStyle;
import hsupertable.style.HTableStyle.HeaderStyle;
import hsupertable.geometry.HTableGeometry;
import hsupertable.geometry.HTableGeometry.InternalCellHit;
import hsupertable.model.InternalGrid;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * HBasicTableUI — Moteur de rendu visuel de HTable.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HBasicTableUI extends BasicTableUI {

    // CONSTANTES DE RENDU
    private static final int CELL_PADDING_H = 12;  // padding horizontal par défaut
    private static final int CELL_PADDING_V = 6;   // padding vertical par défaut
    private static final int OVERLAY_ALPHA = 18;  // transparence des superpositions

    // RENDERER DE L'EN-TÊTE
    /**
     * Renderer de l'en-tête — fond coloré, texte en gras, bordure basse.
     */
    private class ModernHeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(jTable, value, isSelected,
                    hasFocus, row, column);

            if (!(jTable instanceof HTable)) {
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
                    BorderFactory.createEmptyBorder(10, CELL_PADDING_H, 10, CELL_PADDING_H)
            ));

            setOpaque(true);
            return this;
        }
    }

    @Override
    protected void installListeners() {
        // Ne pas installer le MouseInputHandler natif de BasicTableUI : toute
        // la gestion souris de HTable passe par HTableController
        // (installé par HTable), qui pilote directement
        // JTable.changeSelection(...). Installer les deux en parallèle
        // provoquait des changements de sélection concurrents.
    }

    @Override
    protected void uninstallListeners() {
        // Symétrique — rien n'a été installé ici, HTableController gère
        // son propre cycle de vie via dispose().
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
        super.installUI(c);
        if (!(c instanceof HTable)) {
            return;
        }

        HTable t = (HTable) c;
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
        HTableController ctrl = t.getController();
        if (!ctrl.isDrawing()) {
            return;
        }

        int x1 = ctrl.getDrawStartX();
        int y1 = ctrl.getDrawStartY();
        int x2 = ctrl.getDrawEndX();
        int y2 = ctrl.getDrawEndY();

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
         * Dessine les bordures définies dans un HCellModel sur le
 périmètre du rectangle donné.
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
 son HCellModel.
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
            if (hAlign == SwingConstants.CENTER) {
                drawX = x + (w - textW) / 2;
            } else if (hAlign == SwingConstants.RIGHT) {
                drawX = x + w - textW;
            } else {
                drawX = x;
            }

            int drawY;
            if (vAlign == SwingConstants.TOP) {
                drawY = y + textH;
            } else if (vAlign == SwingConstants.BOTTOM) {
                drawY = y + h;
            } else {
                drawY = y + (h + textH) / 2 - fm.getDescent();
            }

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
            if (hAlign == SwingConstants.CENTER) {
                drawX = -textW / 2;
            } else if (hAlign == SwingConstants.RIGHT) {
                drawX = availW / 2 - textW;
            } else {
                drawX = -availW / 2;
            }

            int drawY;
            if (vAlign == SwingConstants.TOP) {
                drawY = -availH / 2 + textH;
            } else if (vAlign == SwingConstants.BOTTOM) {
                drawY = availH / 2;
            } else {
                drawY = textH / 2 - fm.getDescent();
            }

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

}
