/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicMenuBarUI;

/**
 *
 * @author FIDELE
 */
public class HMenuBarUI extends BasicMenuBarUI {
    
    private final HListTheme theme;
    
    public HMenuBarUI(HListTheme theme) {
        this.theme = theme;
    }
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setBackground(theme.getNormalBackground());
        c.setForeground(theme.getNormalForeground());
        c.setOpaque(false);
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(theme.getNormalBackground());
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        g2.dispose();
        super.paint(g, c);
    }
    
}
