package hsupertable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * HBasicTableUI — Moteur de rendu visuel de HSuperTable.
 *
 * C'est ici que tout ce qui est "dessin" se passe. Cette classe remplace le
 * moteur de rendu par défaut de JTable pour prendre en charge :
 *
 * - La fusion de cellules : les cellules absorbées sont sautées, la cellule
 * principale est peinte sur toute la zone fusionnée.
 *
 * - Les bordures personnalisées par cellule, indépendantes de la grille globale
 * du tableau.
 *
 * - L'alignement horizontal et vertical du contenu dans chaque cellule.
 *
 * - La direction du texte : horizontal, vertical vers le haut, vertical vers le
 * bas.
 *
 * - Les marges internes par cellule.
 *
 * - Tous les effets visuels existants : survol, surbrillance, sélection, bandes
 * de couleur, en-tête, ligne totale.
 *
 * Point d'architecture important : cette classe ne connaît que HSuperTable et
 * HDefaultTableModel. Elle ne touche jamais aux données métier — elle lit, elle
 * dessine, c'est tout.
 *
 * @author FIDELE
 * @version 2.0
 */
public class HBasicTableUI extends BasicTableUI {

    // =========================================================================
    // CONSTANTES DE RENDU
    // =========================================================================
    private static final int CELL_PADDING_H = 12;  // padding horizontal par défaut
    private static final int CELL_PADDING_V = 6;   // padding vertical par défaut
    private static final int OVERLAY_ALPHA = 18;  // transparence des superpositions

    // =========================================================================
    // RENDERER DE L'EN-TÊTE
    // =========================================================================
    /**
     * Renderer de l'en-tête — fond coloré, texte en gras, bordure basse. Pas de
     * logique de fusion ici : l'en-tête reste toujours une ligne simple.
     */
    private class ModernHeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, column);

            if (!(jTable instanceof HSuperTable)) {
                return this;
            }
            HSuperTable t = (HSuperTable) jTable;
            HSuperTableStyle style = t.getTableStyle();

            if (style != null) {
                setBackground(style.getHeaderBackground());
                setForeground(style.getHeaderForeground());
                setFont(style.getHeaderFont());
            }

            // Ligne de séparation en bas de l'en-tête
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0,
                            style != null ? style.getHeaderForeground().darker()
                                    : Color.DARK_GRAY),
                    BorderFactory.createEmptyBorder(10, CELL_PADDING_H, 10, CELL_PADDING_H)
            ));

            setHorizontalAlignment(JLabel.LEFT);
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
     * Point d'entrée du rendu. On peint dans cet ordre : 1. Le fond du tableau
     * (couleur unie derrière tout) 2. Les cellules (en sautant les absorbées,
     * en élargissant les fusionnées) 3. La grille globale (si activée) 4. Les
     * bordures custom par cellule (par-dessus la grille) 5. Les superpositions
     * de sélection 6. L'indicateur de focus
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

        // Indicateur de focus sur la cellule active
        paintFocusIndicator(g2, t);

        g2.dispose();
    }

    // =========================================================================
    // PEINTURE D'UNE CELLULE
    // =========================================================================
    /**
     * Peint une cellule individuelle.
     *
     * Si la cellule est absorbée (spanRows==0), on ne fait rien. Si la cellule
     * est fusionnée (spanRows>1 ou spanCols>1), on calcule le rectangle élargi
     * qui couvre toute la zone fusionnée.
     *
     * @param g2 contexte graphique
     * @param t le tableau
     * @param row ligne
     * @param col colonne
     */
    private void paintCell(Graphics2D g2, HSuperTable t, int row, int col) {
        HSuperDefaultTableModel model = t.getHModel();

        HSuperDefaultTableModel.Cell cell = model.getCell(row, col);
        if (cell.isAbsorbed()) {
            return;
        }
        int rSpan = cell.spanRow;
        int cSpan = cell.spanCol;

        // Rectangle de base de la cellule
        Rectangle cellRect = t.getCellRect(row, col, false);

        if (rSpan > 1 || cSpan > 1) {
            cellRect = computeMergedRect(t, row, col, rSpan, cSpan);
        }

        // ── Fond ────────────────────────────────────────────────────────────
        HSuperTableStyle style = t.getTableStyle();
        HSuperTableCellModel cModel = model.getCellModel(row, col);

        Color bg = resolveCellBackground(t, style, row, col);
        g2.setColor(bg);
        g2.fillRect(cellRect.x, cellRect.y, cellRect.width, cellRect.height);

        // ── Contenu (texte) ──────────────────────────────────────────────────
        Object value = t.getValueAt(row, col);
        if (value != null) {
            paintCellText(g2, t, cModel, style, cellRect, value.toString(), row, col);
        }
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
    if (gridColor == null) gridColor = new Color(220, 220, 220);

    g2.setColor(gridColor);
    g2.setStroke(new BasicStroke(1f));

    for (int row = firstRow; row <= lastRow; row++) {
        for (int col = firstCol; col <= lastCol; col++) {

            // On ne dessine rien pour les cellules absorbées
            if (model.isAbsorbed(row, col)) continue;

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
    if (model.isAbsorbed(row, col)) return;

    HSuperTableCellModel cModel = model.getCellModel(row, col);
    if (!cModel.hasAnyBorder()) return;

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
     * L'effet est léger pour rester lisible.
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
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
        g2.setStroke(new BasicStroke(1f));
    }

    // =========================================================================
    // RÉSOLUTION DE LA COULEUR DE FOND
    // =========================================================================
    /**
     * Détermine la couleur de fond d'une cellule selon les priorités suivantes
     * :
     *
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
    private Color resolveCellBackground(HSuperTable t, HSuperTableStyle style, int row, int col) {
        HSuperDefaultTableModel model = t.getHModel();
        HSuperTableCellModel cModel = model.getCellModel(row, col);

        // 1. Couleur custom individuelle sur la cellule
        if (cModel.hasBackground()) {
            return cModel.getBackground();
        }

        // 2. Couleur custom de toute la ligne (ancienne API, conservée)
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

        // 5. Ligne sélectionnée
        if (t.getRowsSelected().contains(row)) {
            return style != null ? style.getSelectionBackground()
                    : new Color(13, 110, 253, 30);
        }

        if (style == null) {
            return Color.WHITE;
        }

        // 6. Ligne totale (dernière ligne si option activée)
        if (t.isTotalRowEnabled() && row == t.getRowCount() - 1) {
            return style.getTotalRowBackground();
        }

        // 7. Première ou dernière colonne mise en valeur
        if (t.isFirstColumnHighlighted() && col == 0) {
            return style.getFirstColumnBackground();
        }
        if (t.isLastColumnHighlighted() && col == t.getColumnCount() - 1) {
            return style.getLastColumnBackground();
        }

        // 8. Bandes de colonnes (priorité sur bandes de lignes si les deux sont actives)
        if (t.isBandedColumns()) {
            return (col % 2 == 0) ? style.getCellBackground()
                    : style.getCellAlternateBackground();
        }

        // 9. Alternance de lignes (zebra)
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
     * Calcule le rectangle total couvert par une cellule fusionnée. On
     * additionne les largeurs des colonnes et les hauteurs des lignes
     * impliquées dans la fusion.
     */
    private Rectangle computeMergedRect(HSuperTable t, int row, int col, int rSpan, int cSpan) {
        Rectangle base = t.getCellRect(row, col, false);
        int totalW = base.width;
        int totalH = base.height;

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

        return new Rectangle(base.x, base.y, totalW, totalH);
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
