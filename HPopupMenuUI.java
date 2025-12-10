/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.vues.HListTheme;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicPopupMenuUI;

/**
 *
 * @author FIDELE
 */
public class HPopupMenuUI extends BasicPopupMenuUI {
    
    private final HListTheme theme;
    
    public HPopupMenuUI(HListTheme theme) {
        this.theme = theme;
    }
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setBackground(theme.getNormalBackground());
        c.setForeground(theme.getNormalForeground());
        c.setBorder(new RoundedPopupBorder());
        c.setOpaque(false);
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        RoundRectangle2D backgroundRect = new RoundRectangle2D.Float(1, 1, c.getWidth() - 2, c.getHeight() - 2, theme.getBorderRadius(), theme.getBorderRadius());
        g2.setColor(theme.getNormalBackground());
        g2.fill(backgroundRect);
        
        g2.dispose();
        super.paint(g, c);
    }
    
    private class RoundedPopupBorder implements Border {
        private final Insets insets;
        public RoundedPopupBorder() {
            this.insets = new Insets(theme.getItemMargin(), theme.getItemMargin(), theme.getItemMargin(), theme.getItemMargin());
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(theme.getBorderColor());
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, theme.getBorderRadius(), theme.getBorderRadius());
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return insets; }
        @Override public boolean isBorderOpaque() { return false; }
    }
    
}
