/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.models.HCheckBoxModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicCheckBoxUI;

/**
 *
 * @author FIDELE
 */
public class HCheckBoxUI extends BasicCheckBoxUI {
    
     private final HCheckBoxModel model;
    private final Color mainRed = new Color(220, 49, 29);
    public HCheckBoxUI(HCheckBoxModel model) { this.model = model; }
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setOpaque(false);
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        JCheckBox cb = (JCheckBox) c;
        Graphics2D g2 = (Graphics2D) g.create();
        int w = cb.getWidth(), h = cb.getHeight();
        int boxSize = 18, boxX = 5, boxY = (h - boxSize) / 2;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(boxX, boxY, boxSize, boxSize, 6, 6);
        g2.setColor(mainRed);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(boxX, boxY, boxSize, boxSize, 6, 6);
        if (cb.isSelected()) {
            g2.setColor(mainRed);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(boxX+4, boxY+9, boxX+9, boxY+14);
            g2.drawLine(boxX+9, boxY+14, boxX+14, boxY+4);
        }
        g2.setFont(cb.getFont());
        g2.setColor(cb.isEnabled() ? Color.BLACK : Color.GRAY);
        FontMetrics m = g2.getFontMetrics();
        String txt = cb.getText();
        int textX = boxX + boxSize + 8;
        int textY = (h + m.getAscent() - m.getDescent()) / 2;
        g2.drawString(txt, textX, textY);
        g2.dispose();
    }
    
}
