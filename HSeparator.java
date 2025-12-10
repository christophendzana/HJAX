/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HMenuStyle;
import javax.swing.*;
import java.awt.*;

/**
 * Séparateur personnalisé pour les menus avec design moderne.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HSeparator extends JPopupMenu.Separator {
    
    private HMenuStyle menuStyle = HMenuStyle.PRIMARY;
    private int margin = 10;
    
    public HSeparator() {
        super();
        setPreferredSize(new Dimension(0, 10));
    }
    
    public HSeparator(HMenuStyle style) {
        this();
        this.menuStyle = style;
    }
    
    public HMenuStyle getMenuStyle() {
        return menuStyle;
    }
    
    public void setMenuStyle(HMenuStyle style) {
        this.menuStyle = style;
        repaint();
    }
    
    public int getMargin() {
        return margin;
    }
    
    public void setMargin(int margin) {
        this.margin = margin;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int y = height / 2;
        
        if (menuStyle != null) {
            g2.setColor(menuStyle.getSeparatorColor());
        } else {
            g2.setColor(new Color(200, 200, 200));
        }
        
        // Ligne avec marges
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(margin, y, width - margin, y);
        
        g2.dispose();
    }
}