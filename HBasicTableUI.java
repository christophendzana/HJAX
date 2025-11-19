/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HTable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 *
 * @author FIDELE
 */
public class HBasicTableUI extends BasicTableUI {

    // ---- CONSTANTES ----
    private static final Color HEADER_BACKGROUND = new Color(143, 1, 1);
    private static final Color HEADER_FOREGROUND = Color.WHITE;

    private static final Color GRID_COLOR = new Color(226, 232, 240);
    private static final Color HOVER_COLOR = new Color(236, 172, 172);
    private static final Color HIGHLIGHT_COLOR = new Color(236, 172, 172);
    private static final Color SELECTION_COLOR = new Color(236, 172, 172);
    private static final Color FOCUS_BORDER_COLOR = new Color(235, 103, 103);

    private Font headerFont;
    private Font cellFont;

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        initializeStyles();
        installCustomRenderers();
        configureTableSize();
    }

    /**
     * Définit une taille initiale basée sur 85% de l'écran
     */
    private void configureTableSize() {
        table.setPreferredScrollableViewportSize(calculate85PercentSize());
        table.setOpaque(true);
        table.setBackground(Color.WHITE);
    }

    private Dimension calculate85PercentSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
                (int) (screenSize.width * 0.85),
                (int) (screenSize.height * 0.85)
        );
    }

    /**
     * Initialise polices et style visuel général
     */
    private void initializeStyles() {
        headerFont = new Font("Segoe UI", Font.BOLD, 16);
        cellFont = new Font("Segoe UI", Font.PLAIN, 16);

        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(GRID_COLOR);
        table.setFillsViewportHeight(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BACKGROUND);
        header.setForeground(HEADER_FOREGROUND);
        header.setFont(headerFont);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new ModernHeaderRenderer());
    }

    /**
     * Applique nos renderers personnalisés
     */
    private void installCustomRenderers() {
        for (int i = 0; i < table.getColumnCount(); i++) {
            Class<?> columnType = table.getColumnClass(i);
            table.setDefaultRenderer(columnType, new ModernCellRenderer());
        }
    }

    // Rendu de l'entête
    private class ModernHeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBackground(HEADER_BACKGROUND);
            setForeground(HEADER_FOREGROUND);
            setFont(headerFont);
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            setHorizontalAlignment(JLabel.LEFT);

            return this;
        }
    }

    // Rendu des cellules
    private class ModernCellRenderer extends DefaultTableCellRenderer {

        private final Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FOCUS_BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        );

        private final Border defaultBorder =
                BorderFactory.createEmptyBorder(0, 12, 0, 12);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!(table instanceof HTable ht)) {
                // Sécurité : on ne casse pas le rendu même si table != HTable
                setBackground(Color.WHITE);
                return this;
            }

            configureAppearance(ht, row, column, hasFocus);
            configureBorders(hasFocus);

            return this;
        }

        private void configureAppearance(HTable table, int row, int column, boolean hasFocus) {
            setFont(cellFont);
            setOpaque(true);

            if (row == table.getHighlightedRow()) {
                setBackground(HIGHLIGHT_COLOR);
            } else if (row == table.getHoveredRow()) {
                setBackground(HOVER_COLOR);
            } else if (table.getRowsSelected().contains(row)) {
                setBackground(SELECTION_COLOR);
            } else if (row % 2 == 0) {
                setBackground(Color.WHITE);
            } else {
                setBackground(new Color(250, 250, 250));
            }

            if (hasFocus) {
                setFont(cellFont.deriveFont(Font.BOLD));
            }
        }

        private void configureBorders(boolean hasFocus) {
            setBorder(hasFocus ? focusBorder : defaultBorder);
        }
    }
    
    // Superposer nos effets
    @Override
    public void paint(Graphics g, JComponent c) {

        Graphics2D g2d = (Graphics2D) g;

        // activer antialiasing AVANT les dessins
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        super.paint(g2d, c);

        paintCustomOverlays(g2d);
        paintRoundedTop(g2d, c);
    }

    /**
     * Dessine un arrondi sous le header
     */
    private void paintRoundedTop(Graphics2D g2d, JComponent c) {
        g2d.setPaint(Color.BLACK);        
        g2d.fillRoundRect(0, 0, c.getWidth(), 8, 40, 40);
    }

    private void paintCustomOverlays(Graphics2D g2d) {
        if (!(table instanceof HTable ht)) return;
        paintRowStates(g2d, ht);
    }

    private void paintRowStates(Graphics2D g2d, HTable hTable) {
        for (Integer selectedRow : hTable.getRowsSelected()) {
            paintSelectionEffect(g2d, selectedRow);
        }
    }

    private void paintSelectionEffect(Graphics2D g2d, int row) {

        Rectangle rect = table.getCellRect(row, 0, true);

        int width = 0;
        for (int col = 0; col < table.getColumnCount(); col++) {
            width += table.getColumnModel().getColumn(col).getWidth();
        }

        g2d.setColor(new Color(59, 130, 246, 15));
        g2d.fillRect(rect.x, rect.y, width, rect.height);
    }

}
