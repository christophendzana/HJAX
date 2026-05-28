package hsupertable;

import hsupertable.HSuperDefaultTableModel.Cell;
import hsupertable.HSuperDefaultTableModel.InternalGrid;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * HBasicTableUI — Moteur de rendu visuel de HSuperTable.
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

            if (!(jTable instanceof HSuperTable)) {
                return this;
            }

            HSuperTable t = (HSuperTable) jTable;
            HSuperTableStyle style = t.getTableStyle();

            // ── Récupération du HeaderStyle de cette colonne ──────────────────
            // Si aucun style custom n'existe, on utilise les valeurs du style global
            HSuperTable.HeaderStyle hs = t.headerStyles.get(column);

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

            if (!(jTable instanceof HSuperTable)) {
                setBackground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }

            HSuperTable t = (HSuperTable) jTable;
            HSuperTableStyle style = t.getTableStyle();

            // Police et couleur de texte par défaut depuis le style
            if (style != null) {
                setFont(style.getCellFont());
                setForeground(style.getCellForeground());
            }

            setOpaque(true);

            // ── Couleur de fond ──────────────────────────────────────────────
            // Priorité : couleur cellule custom > highlight > hover > sélection
            //            > bande de couleur (colonnes) > alternance de lignes
            setBackground(resolveCellBackground(t, style, row, column));

            // ── Couleur de texte custom ──────────────────────────────────────
            HSuperTableCellModel cellModel = t.getHModel().getCellModel(row, column);
            if (cellModel.hasForeground()) {
                setForeground(cellModel.getForeground());
            }

            // ── Focus ────────────────────────────────────────────────────────
            boolean isFocused = (row == t.getFocusedRow() && column == t.getFocusedColumn());
            if (isFocused) {
                setFont(getFont().deriveFont(Font.BOLD));
            }

            // Les marges et l'alignement sont appliqués dans paintCell()
            // via les infos du HSuperTableCellModel — pas ici, pour que la zone de
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
        if (!(c instanceof HSuperTable)) {
            return;
        }

        HSuperTable t = (HSuperTable) c;
        HSuperTableStyle style = t.getTableStyle();

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
            header.setReorderingAllowed(false);
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
        if (!(c instanceof HSuperTable)) {
            super.paint(g, c);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        applyRenderingHints(g2);

        HSuperTable t = (HSuperTable) c;

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
                paintCustomBorders(g2, t, row, col);
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
    private void paintCell(Graphics2D g2, HSuperTable t, int row, int col) {
        HSuperDefaultTableModel model = t.getHModel();
        HSuperDefaultTableModel.Cell cell = model.getCell(row, col);
        if (cell.isAbsorbed()) {
            return;
        }

        Rectangle cellRect = t.getCellRect(row, col, false);
        if (cell.spanRow > 1 || cell.spanCol > 1) {
            cellRect = computeMergedRect(t, row, col, cell.spanRow, cell.spanCol);
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
            Graphics2D g2, HSuperTable t,
            HSuperDefaultTableModel.Cell cell,
            Rectangle rect, int row, int col,
            boolean isSubCell // ← nouveau paramètre
    ) {
        // CAS 1
        if (cell.internalGrid == null) {
            HSuperTableStyle style = t.getTableStyle();
            HSuperTableCellModel cModel = t.getHModel().getCellModel(row, col);

            // Fond
            Color bg;
            if (cell.style != null && cell.style.hasBackground()) {
                bg = cell.style.getBackground();
            } else {
                bg = resolveCellBackground(t, style, row, col);
            }
            if (bg == null) {
                bg = style != null ? style.getCellBackground() : Color.WHITE;
            }
            g2.setColor(bg);
            g2.fillRect(rect.x, rect.y, rect.width, rect.height);

            // Valeur — sous-cellule : cell.value uniquement
            // cellule racine : cell.value puis DefaultTableModel
            Object value = cell.value;
            if (!isSubCell && value == null) {
                value = t.getValueAt(row, col);
            }

            if (value != null) {
                HSuperTableCellModel effectiveModel
                        = (cell.style != null) ? cell.style : cModel;
                paintCellText(g2, t, effectiveModel, style,
                        rect, value.toString(), row, col);
            }
            return;
        }

        // CAS 2
        InternalGrid grid = cell.internalGrid;
        Rectangle[] parts = computeInternalRects(rect, grid);
        Cell first = grid.getFirstCell();
        Cell second = grid.getSecondCell();

        // Récursif — les sous-cellules sont toujours isSubCell=true
        paintCellStructure(g2, t, first, parts[0], row, col, true);
        paintCellStructure(g2, t, second, parts[1], row, col, true);

        // Ligne de séparation
        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1f));
        if (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL) {
            int x = parts[0].x + parts[0].width;
            g2.drawLine(x, rect.y, x, rect.y + rect.height);
        } else {
            int y = parts[0].y + parts[0].height;
            g2.drawLine(rect.x, y, rect.x + rect.width, y);
        }

        paintInternalBorders(g2, first, parts[0], second, parts[1]);
    }

// =========================================================================
// RENDU D'UNE SOUS-CELLULE INTERNE
// =========================================================================
    /**
     * Rend une sous-cellule issue d'une subdivision interne.
     */
    private void paintInternalCell(
            Graphics2D g2,
            HSuperTable t,
            HSuperDefaultTableModel.Cell subCell,
            Rectangle rect,
            int row,
            int col
    ) {
        if (subCell == null) {
            return;
        }

        HSuperTableStyle style = t.getTableStyle();
        HSuperTableCellModel cModel = subCell.style;

        // ── Fond ─────────────────────────────────────────────────────────────
        Color bg;
        if (cModel != null && cModel.hasBackground()) {
            bg = cModel.getBackground();
        } else {
            bg = resolveCellBackground(t, style, row, col);
        }
        if (bg == null) {
            bg = style != null ? style.getCellBackground() : Color.WHITE;
        }

        g2.setColor(bg);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);

        // ── Texte — avec alignement, marges, direction depuis subCell.style ──
        Object value = subCell.value;
        if (value != null && !value.toString().isEmpty()) {
            // On passe cModel directement : il contient alignement, marges, direction
            paintCellText(g2, t, cModel, style, rect, value.toString(), row, col);
        }
    }

    /**
     * Dessine les bordures custom des deux sous-cellules d'une InternalGrid.
     * Appelé après le dessin du fond et du texte, par-dessus tout.
     */
    private void paintInternalBorders(Graphics2D g2, HSuperDefaultTableModel.Cell first,
            Rectangle firstRect, HSuperDefaultTableModel.Cell second,
            Rectangle secondRect) {

        paintSubCellBorders(g2, first, firstRect);
        paintSubCellBorders(g2, second, secondRect);
    }

    private void paintSubCellBorders(Graphics2D g2,
            HSuperDefaultTableModel.Cell subCell, Rectangle r) {

        if (subCell == null || subCell.style == null) {
            return;
        }
        HSuperTableCellModel m = subCell.style;

        if (m.hasBorderTop()) {
            g2.setColor(m.getBorderTopColor());
            g2.setStroke(createStroke(m.getBorderTopThickness(), m.getBorderTopStyle()));
            g2.drawLine(r.x, r.y, r.x + r.width, r.y);
        }
        if (m.hasBorderBottom()) {
            g2.setColor(m.getBorderBottomColor());
            g2.setStroke(createStroke(m.getBorderBottomThickness(), m.getBorderBottomStyle()));
            g2.drawLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
        }
        if (m.hasBorderLeft()) {
            g2.setColor(m.getBorderLeftColor());
            g2.setStroke(createStroke(m.getBorderLeftThickness(), m.getBorderLeftStyle()));
            g2.drawLine(r.x, r.y, r.x, r.y + r.height);
        }
        if (m.hasBorderRight()) {
            g2.setColor(m.getBorderRightColor());
            g2.setStroke(createStroke(m.getBorderRightThickness(), m.getBorderRightStyle()));
            g2.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void paintInternalHover(Graphics2D g2, HSuperTable t) {

        InternalCellHit hit = t.getHoveredInternalCell();

        if (hit == null) {
            return;
        }

        Rectangle r = hit.bounds;

        g2.setColor(new Color(37, 99, 235, 40));

        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private void paintInternalSelection(Graphics2D g2, HSuperTable t) {

        InternalCellHit hit = t.getSelectedInternalCell();

        if (hit == null) {
            return;
        }

        Rectangle r = hit.bounds;

        g2.setColor(new Color(37, 99, 235, 70));

        g2.fillRect(r.x, r.y, r.width, r.height);
    }

    private void paintInternalFocus(Graphics2D g2, HSuperTable t) {

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
// CALCUL DES RECTANGLES INTERNES
// =========================================================================
    /**
     * Découpe un rectangle parent en deux selon InternalGrid.
     */
    public Rectangle[] computeInternalRects(Rectangle rect, InternalGrid grid) {

        Rectangle first = new Rectangle(rect);
        Rectangle second = new Rectangle(rect);

        float ratio = grid.getDividerRatio();

        if (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL) {

            int splitX = (int) (rect.width * ratio);

            first.width = splitX;

            second.x = rect.x + splitX;
            second.width = rect.width - splitX;
        } else {

            int splitY = (int) (rect.height * ratio);

            first.height = splitY;

            second.y = rect.y + splitY;
            second.height = rect.height - splitY;
        }

        return new Rectangle[]{first, second};
    }

    /**
     * Dessine le texte d'une cellule en tenant compte de : - l'alignement
     * horizontal et vertical - les marges internes (custom ou globales) - la
     * direction du texte (horizontal, vertical haut/bas)
     *
     * Pour la rotation, on fait pivoter le contexte graphique autour du centre
     * de la cellule, on dessine le texte, puis on restitue le contexte.
     */
    private void paintCellText(Graphics2D g2, HSuperTable t, HSuperTableCellModel cModel,
            HSuperTableStyle style, Rectangle rect, String text,
            int row, int col) {

        // Police et couleur
        Font font = (style != null) ? style.getCellFont()
                : new Font("Segoe UI", Font.PLAIN, 13);

        // Cellule en focus → gras
        if (row == t.getFocusedRow() && col == t.getFocusedColumn()) {
            font = font.deriveFont(Font.BOLD);
        }

        Color fg = Color.BLACK;
        if (cModel.hasForeground()) {
            fg = cModel.getForeground();
        } else if (style != null) {
            fg = style.getCellForeground();
        }

        // Marges : custom > globales du tableau > constantes par défaut
        Insets margins;
        if (cModel.hasCustomMargins()) {
            margins = cModel.getMargins();
        } else {
            margins = t.getDefaultCellMargins();
            if (margins == null) {
                margins = new Insets(CELL_PADDING_V, CELL_PADDING_H, CELL_PADDING_V, CELL_PADDING_H);
            }
        }

        // Zone utile après application des marges
        int contentX = rect.x + margins.left;
        int contentY = rect.y + margins.top;
        int contentW = rect.width - margins.left - margins.right;
        int contentH = rect.height - margins.top - margins.bottom;
        if (contentW <= 0 || contentH <= 0) {
            return;
        }

        // Alignement horizontal (SwingConstants)
        int hAlign = cModel.getHorizontalAlignment();
        int vAlign = cModel.getVerticalAlignment();

        // Direction du texte
        int direction = cModel.getTextDirection();

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        if (direction == HSuperTable.TEXT_VERTICAL_UP || direction == HSuperTable.TEXT_VERTICAL_DOWN) {
            paintRotatedText(g2, text, font, fg, rect, margins, hAlign, vAlign, direction);
        } else {
            // Texte horizontal standard
            paintHorizontalText(g2, text, font, fg, fm, contentX, contentY, contentW, contentH, hAlign, vAlign);
        }
    }

    /**
     * Dessine du texte horizontal avec alignement H et V dans la zone donnée.
     * On tronque avec "..." si le texte est trop large.
     */
    private void paintHorizontalText(Graphics2D g2, String text, Font font, Color fg,
            FontMetrics fm, int x, int y, int w, int h,
            int hAlign, int vAlign) {
        g2.setColor(fg);

        // Tronquage si nécessaire
        String displayText = truncateText(fm, text, w);

        int textW = fm.stringWidth(displayText);
        int textH = fm.getAscent();

        // Position X selon l'alignement horizontal
        int drawX;
        if (hAlign == SwingConstants.CENTER) {
            drawX = x + (w - textW) / 2;
        } else if (hAlign == SwingConstants.RIGHT) {
            drawX = x + w - textW;
        } else {
            // LEFT par défaut
            drawX = x;
        }

        // Position Y selon l'alignement vertical
        int drawY;
        if (vAlign == SwingConstants.TOP) {
            drawY = y + textH;
        } else if (vAlign == SwingConstants.BOTTOM) {
            drawY = y + h;
        } else {
            // CENTER par défaut
            drawY = y + (h + textH) / 2 - fm.getDescent();
        }

        g2.drawString(displayText, drawX, drawY);
    }

    /**
     * Dessine du texte pivoté (90° vers le haut ou vers le bas).
     *
     * Principe : on translate l'origine au centre de la cellule, on fait
     * pivoter le contexte, on dessine comme si c'était du texte horizontal,
     * puis on restitue. Les alignements H et V s'inversent naturellement avec
     * la rotation.
     */
    private void paintRotatedText(Graphics2D g2, String text, Font font, Color fg,
            Rectangle rect, Insets margins,
            int hAlign, int vAlign, int direction) {

        AffineTransform originalTransform = g2.getTransform();

        // Centre de la cellule = pivot de rotation
        int cx = rect.x + rect.width / 2;
        int cy = rect.y + rect.height / 2;

        double angle = (direction == HSuperTable.TEXT_VERTICAL_UP)
                ? -Math.PI / 2 // rotation 90° sens antihoraire
                : Math.PI / 2;  // rotation 90° sens horaire

        g2.translate(cx, cy);
        g2.rotate(angle);

        // Après rotation, les dimensions s'inversent pour la zone de texte
        // (la hauteur de la cellule devient la "largeur" disponible pour le texte)
        int availW = rect.height - margins.top - margins.bottom;
        int availH = rect.width - margins.left - margins.right;

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        String displayText = truncateText(fm, text, availW);

        int textW = fm.stringWidth(displayText);
        int textH = fm.getAscent();

        // Alignement dans le repère pivoté
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

    // =========================================================================
    // GRILLE GLOBALE
    // =========================================================================
    /**
     * Dessine le quadrillage global du tableau — des lignes légères entre
     * toutes les cellules. On saute les bordures qui passent à l'intérieur
     * d'une zone fusionnée pour ne pas "couper" visuellement la fusion.
     */
    private void paintGlobalGrid(Graphics2D g2, HSuperTable t,
            int firstRow, int lastRow,
            int firstCol, int lastCol) {

        HSuperDefaultTableModel model = t.getHModel();
        Color gridColor = t.getGridColor();
        if (gridColor == null) {
            gridColor = new Color(220, 220, 220);
        }

        g2.setColor(gridColor);
        g2.setStroke(new BasicStroke(1f));

        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {

                // On ne dessine rien pour les cellules absorbées
                if (model.isAbsorbed(row, col)) {
                    continue;
                }

                HSuperDefaultTableModel.Cell cell = model.getCell(row, col);
                Rectangle r = (cell.spanRow > 1 || cell.spanCol > 1)
                        ? computeMergedRect(t, row, col,
                                cell.spanRow, cell.spanCol)
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
    // BORDURES CUSTOM PAR CELLULE
    // =========================================================================
    /**
     * Dessine les bordures définies dans le HTableCellModel de la cellule. Ces
     * bordures sont dessinées par-dessus la grille globale, donc elles la
     * remplacent visuellement là où elles sont définies.
     *
     * Les cellules absorbées n'ont pas de bordures propres — c'est la cellule
     * principale qui porte les bordures de toute la zone fusionnée.
     */
    private void paintCustomBorders(Graphics2D g2, HSuperTable t, int row, int col) {
        HSuperDefaultTableModel model = t.getHModel();

        // Les absorbées n'ont pas de bordures propres
        if (model.isAbsorbed(row, col)) {
            return;
        }

        HSuperTableCellModel cModel = model.getCellModel(row, col);
        if (!cModel.hasAnyBorder()) {
            return;
        }

        // ── POINT CLÉ : on utilise le rectangle FUSIONNÉ, pas le rectangle de base ──
        HSuperDefaultTableModel.Cell cell = model.getCell(row, col);
        Rectangle rect = (cell.spanRow > 1 || cell.spanCol > 1)
                ? computeMergedRect(t, row, col, cell.spanRow, cell.spanCol)
                : t.getCellRect(row, col, false);

        // Dessiner uniquement sur le périmètre du rectangle fusionné
        if (cModel.hasBorderTop()) {
            g2.setColor(cModel.getBorderTopColor());
            g2.setStroke(createStroke(cModel.getBorderTopThickness(),
                    cModel.getBorderTopStyle()));
            g2.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
        }
        if (cModel.hasBorderBottom()) {
            g2.setColor(cModel.getBorderBottomColor());
            g2.setStroke(createStroke(cModel.getBorderBottomThickness(),
                    cModel.getBorderBottomStyle()));
            g2.drawLine(rect.x, rect.y + rect.height,
                    rect.x + rect.width, rect.y + rect.height);
        }
        if (cModel.hasBorderLeft()) {
            g2.setColor(cModel.getBorderLeftColor());
            g2.setStroke(createStroke(cModel.getBorderLeftThickness(),
                    cModel.getBorderLeftStyle()));
            g2.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
        }
        if (cModel.hasBorderRight()) {
            g2.setColor(cModel.getBorderRightColor());
            g2.setStroke(createStroke(cModel.getBorderRightThickness(),
                    cModel.getBorderRightStyle()));
            g2.drawLine(rect.x + rect.width, rect.y,
                    rect.x + rect.width, rect.y + rect.height);
        }

        g2.setStroke(new BasicStroke(1f));
    }

    /**
     * Crée un BasicStroke selon l'épaisseur et le style. Les styles
     * correspondent aux constantes HSuperTable.BORDER_SOLID, BORDER_DASHED,
     * BORDER_DOTTED.
     */
    private BasicStroke createStroke(float thickness, int style) {
        if (style == HSuperTable.BORDER_DASHED) {
            return new BasicStroke(thickness, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f);
        }
        if (style == HSuperTable.BORDER_DOTTED) {
            return new BasicStroke(thickness, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 10f, new float[]{1f, 4f}, 0f);
        }
        if (style == HSuperTable.BORDER_DOUBLE) {
            // Pour le double, on dessine deux traits fins — le stroke seul ne suffit pas,
            // mais on retourne le stroke principal ; le second trait est dessiné dans
            // paintCustomBorders() juste avant l'appel (décalé de 2px).
            // Ici on rend juste le trait épais, l'effet double est une approximation.
            return new BasicStroke(thickness);
        }
        // BORDER_SOLID et cas par défaut
        return new BasicStroke(thickness);
    }

    // =========================================================================
    // SUPERPOSITIONS ET FOCUS
    // =========================================================================
    /**
     * Dessine une superposition semi-transparente sur les lignes sélectionnées.
     */
    private void paintSelectionOverlays(Graphics2D g2, HSuperTable t) {
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
    private void paintFocusIndicator(Graphics2D g2, HSuperTable t) {
        int fr = t.getFocusedRow();
        int fc = t.getFocusedColumn();
        if (fr < 0 || fc < 0) {
            return;
        }
        if (fr >= t.getRowCount() || fc >= t.getColumnCount()) {
            return;
        }

        HSuperDefaultTableModel model = t.getHModel();
        HSuperDefaultTableModel.Cell cell = model.getCell(fr, fc);
        Rectangle rect = (cell.spanRow > 1 || cell.spanCol > 1)
                ? computeMergedRect(t, fr, fc, cell.spanRow, cell.spanCol)
                : t.getCellRect(fr, fc, false);
        
        HSuperTableStyle style = t.getTableStyle();
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
    /**
     * Détermine la couleur de fond d'une cellule selon les priorités suivantes:
     * 1. Couleur custom de la cellule individuelle (HTableCellModel) 2. Couleur
     * custom de la ligne (setRowBackground) 3. Ligne en surbrillance
     * (highlightedRow) 4. Ligne survolée (hoveredRow) 5. Ligne sélectionnée 6.
     * Options de style : ligne totale, première/dernière colonne 7. Bandes de
     * colonnes (si activé) 8. Alternance de lignes (zebra) 9. Couleur de fond
     * par défaut du style
     *
     * Cette méthode centralise toute la logique de couleur pour éviter qu'elle
     * soit éparpillée entre le renderer et le paint().
     */
    private Color resolveCellBackground(HSuperTable t, HSuperTableStyle style,
            int row, int col) {
        HSuperDefaultTableModel model = t.getHModel();
        HSuperTableCellModel cModel = model.getCellModel(row, col);

        // 1. Couleur custom individuelle — priorité absolue
        if (cModel.hasBackground()) {
            return cModel.getBackground();
        }

        // 2. Couleur custom de ligne (ancienne API)
        Color rowBg = t.getRowBackground(row);
        if (rowBg != null) {
            return rowBg;
        }

        // 3. Ligne en surbrillance
        if (row == t.getHighlightedRow()) {
            return style != null ? style.getHighlightBackground()
                    : new Color(13, 110, 253, 40);
        }

        // 4. Ligne survolée
        if (row == t.getHoveredRow()) {
            return style != null ? style.getHoverBackground()
                    : new Color(13, 110, 253, 20);
        }

        // 5. Cellule dans la sélection de zone (CellRange) — NOUVEAU
        //    Légèrement plus marqué que le hover pour bien délimiter la zone
        if (t.hasSelection() && t.getSelection().contains(row, col)) {
            return style != null
                    ? new Color(
                            style.getSelectionBackground().getRed(),
                            style.getSelectionBackground().getGreen(),
                            style.getSelectionBackground().getBlue(),
                            70) // alpha plus marqué que le hover
                    : new Color(13, 110, 253, 70);
        }

        // 6. Ligne sélectionnée (ancienne sélection par lignes)
        if (t.getRowsSelected().contains(row)) {
            return style != null ? style.getSelectionBackground()
                    : new Color(13, 110, 253, 30);
        }

        if (style == null) {
            return Color.WHITE;
        }

        // 7. Ligne totale
        if (t.isTotalRowEnabled() && row == t.getRowCount() - 1) {
            return style.getTotalRowBackground();
        }

        // 8. Première / dernière colonne
        if (t.isFirstColumnHighlighted() && col == 0) {
            return style.getFirstColumnBackground();
        }
        if (t.isLastColumnHighlighted() && col == t.getColumnCount() - 1) {
            return style.getLastColumnBackground();
        }

        // 9. Bandes de colonnes
        if (t.isBandedColumns()) {
            return (col % 2 == 0) ? style.getCellBackground()
                    : style.getCellAlternateBackground();
        }

        // 10. Alternance de lignes
        if (t.isBandedRows()) {
            return (row % 2 == 0) ? style.getCellBackground()
                    : style.getCellAlternateBackground();
        }

        return style.getCellBackground();
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================
    
    /**
 * Version publique de computeMergedRect — accessible depuis le contrôleur.
 *
 * @param t     tableau
 * @param row   ligne de la cellule principale
 * @param col   colonne de la cellule principale
 * @param rSpan nombre de lignes fusionnées
 * @param cSpan nombre de colonnes fusionnées
 * @return rectangle total de la fusion
 */
public Rectangle computeMergedRectPublic(HSuperTable t,
        int row, int col, int rSpan, int cSpan) {
    return computeMergedRect(t, row, col, rSpan, cSpan);
}
    
    
    /**
     * Calcule le rectangle total couvert par une cellule fusionnée. On
     * additionne les largeurs des colonnes et les hauteurs des lignes
     * impliquées dans la fusion.
     */
    public Rectangle computeMergedRect(HSuperTable t, int row, int col, int rSpan, int cSpan) {
        Rectangle cellBaseRect = t.getCellRect(row, col, false);
        int totalW = cellBaseRect.width;
        int totalH = cellBaseRect.height;

        for (int dc = 1; dc < cSpan; dc++) {
            int nextCol = col + dc;
            if (nextCol < t.getColumnCount()) {
                totalW += t.getColumnModel().getColumn(nextCol).getWidth();
            }
        }
        for (int dr = 1; dr < rSpan; dr++) {
            int nextRow = row + dr;
            if (nextRow < t.getRowCount()) {
                totalH += t.getRowHeight(nextRow);
            }
        }

        return new Rectangle(cellBaseRect.x, cellBaseRect.y, totalW, totalH);
    }

    /**
     * Dessine le trait de prévisualisation pendant le glisser en mode crayon.
     */
    private void paintDrawPreview(Graphics2D g2, HSuperTable t) {
    HSuperTableController ctrl = t.getController();
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
    HSuperDefaultTableModel.Cell cell = t.getHModel().getCell(row, col);
    Rectangle cellRect = (cell.spanRow > 1 || cell.spanCol > 1)
            ? computeMergedRect(t, row, col, cell.spanRow, cell.spanCol)
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
    private void paintResizePreview(Graphics2D g2, HSuperTable t) {

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
     * Résultat d'une détection de sous-cellule interne.
     *
     * Contient : - la cellule réellement ciblée, - son rectangle réel, - sa
     * cellule parente.
     */
    public static class InternalCellHit {

        /**
         * Cellule réellement ciblée.
         */
        public Cell cell;

        /**
         * Cellule parente.
         */
        public Cell parent;

        /**
         * Rectangle réel occupé par cette cellule.
         */
        public Rectangle bounds;

        /**
         * Crée un résultat de hit-testing.
         *
         * @param cell
         * @param bounds
         * @param parent
         */
        public InternalCellHit(
                Cell cell,
                Rectangle bounds,
                Cell parent
        ) {

            this.cell = cell;
            this.bounds = bounds;
            this.parent = parent;
        }
    }

    // RECHERCHE D'UNE CELLULE INTERNE DEPUIS LA TABLE
    /**
     * Retourne la sous-cellule située sous un point souris.
     *
     * @param t tableau
     * @param point point souris
     * @return résultat du hit-testing
     */
    public InternalCellHit getInternalCellAt(HSuperTable t, Point point) {

    // ── Résolution via resolvePoint ───────────────────────────────────────
    int[] resolved = t.resolvePoint(point);
    int row = resolved[0];
    int col = resolved[1];

    if (row < 0 || col < 0) {
        return null;
    }

    HSuperDefaultTableModel model = t.getHModel();
    Cell cell = model.getCell(row, col);

    // La cellule est déjà résolue vers la principale — pas besoin
    // de vérifier isAbsorbed ici
    Rectangle rect = t.getCellRect(row, col, false);

    if (cell.isMerged()) {
        rect = computeMergedRect(t, row, col, cell.spanRow, cell.spanCol);
    }

    return findInternalCellAt(cell, rect, point);
}

    /**
     * Recherche récursivement la sous-cellule interne située sous un point.
     *
     * @param cell cellule de départ
     * @param rect rectangle occupé par cette cellule
     * @param point point souris
     * @return résultat du hit-testing
     */
    private InternalCellHit findInternalCellAt(
            Cell cell,
            Rectangle rect,
            Point point
    ) {

        // Sécurité
        if (cell == null || rect == null || point == null) {
            return null;
        }

        // Le point n'est pas dans cette cellule
        if (!rect.contains(point)) {
            return null;
        }

        // Cellule simple : cible trouvée
        if (cell.internalGrid == null) {

            return new InternalCellHit(
                    cell,
                    rect,
                    null
            );
        }

        // Cellule subdivisée
        InternalGrid grid = cell.internalGrid;

        Rectangle[] parts = computeInternalRects(rect, grid);

        Rectangle firstRect = parts[0];
        Rectangle secondRect = parts[1];

        Cell firstCell = grid.getFirstCell();
        Cell secondCell = grid.getSecondCell();

        // Recherche dans la première sous-cellule
        if (firstRect.contains(point)) {

            InternalCellHit hit = findInternalCellAt(
                    firstCell,
                    firstRect,
                    point
            );

            if (hit != null) {
                if (hit.parent == null) {
                    hit.parent = cell;
                }
                return hit;
            }
        }

        // Recherche dans la deuxième sous-cellule
        if (secondRect.contains(point)) {

            InternalCellHit hit = findInternalCellAt(
                    secondCell,
                    secondRect,
                    point
            );

            if (hit != null) {
                if (hit.parent == null) {
                    hit.parent = cell;
                }
                return hit;
            }
        }

        // Fallback
        return new InternalCellHit(
                cell,
                rect,
                null
        );
    }

    /**
     * Tronque le texte avec "…" s'il dépasse la largeur disponible.
     */
    private String truncateText(FontMetrics fm, String text, int availableWidth) {
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

    /**
     * Active l'anticrénelage pour un rendu net sur tous les écrans.
     */
    private void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    
}
