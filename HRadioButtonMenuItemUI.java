/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;

/**
 *
 * @author FIDELE
 */
public class HRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI{
 
      private final HListTheme theme;
    private JRadioButtonMenuItem item;

    public HRadioButtonMenuItemUI(HListTheme theme) {
        this.theme = theme;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        if (c instanceof JRadioButtonMenuItem) {
            this.item = (JRadioButtonMenuItem) c;
        }

        c.setBackground(theme.getNormalBackground());
        c.setForeground(theme.getSelectionForeground());
        c.setOpaque(false);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ButtonModel model = (item != null) ? item.getModel() : null;

        boolean highlighted = model != null && (model.isArmed() || model.isSelected());

        if (highlighted) {
            RoundRectangle2D rect = new RoundRectangle2D.Float(
                2, 2,
                c.getWidth() - 4, c.getHeight() - 4,
                theme.getItemRadius(), theme.getItemRadius()
            );
            g2.setColor(theme.getSelectionForeground());
            g2.fill(rect);
        } else {
            g2.setColor(theme.getNormalBackground());
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        g2.dispose();
        super.paint(g, c);
    }
    
}
