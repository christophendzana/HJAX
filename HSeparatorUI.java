/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.plaf.basic.BasicSeparatorUI;

/**
 *
 * @author FIDELE
 */
public class HSeparatorUI extends BasicSeparatorUI {
    
     private final HListTheme theme;
    private JSeparator separator;

    public HSeparatorUI(HListTheme theme) {
        this.theme = theme;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        if (c instanceof JSeparator) {
            this.separator = (JSeparator) c;
        }

        c.setForeground(theme.getBorderColor());
        c.setOpaque(false);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (separator == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(theme.getBorderColor());

        if (separator.getOrientation() == javax.swing.SwingConstants.VERTICAL) {
            g2.fillRect(separator.getWidth() / 2, 0, 20, separator.getHeight());
        } else {
            g2.fillRect(0, separator.getHeight() / 2, separator.getWidth(), 20);
        }

        g2.dispose();
    }
    
}
