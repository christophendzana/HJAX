/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HMenuStyle;
import javax.swing.*;
import java.awt.*;

/**
 * Barre de menu personnalisée avec design moderne.
 * Étend JMenuBar pour offrir des fonctionnalités avancées avec styles prédéfinis.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JMenuBar
 * @see HMenuStyle
 */
public class HMenuBar extends JMenuBar {
    
    private HMenuStyle menuStyle = HMenuStyle.PRIMARY;
    private int cornerRadius = 0; // Généralement 0 pour une barre de menu
    
    public HMenuBar() {
        super();
        updateUI();
    }
    
    public HMenuBar(HMenuStyle style) {
        super();
        this.menuStyle = style;
        updateUI();
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(true);
        setBorderPainted(false);
        applyStyle();
    }
    
    private void applyStyle() {
        if (menuStyle != null) {
            setBackground(menuStyle.getMenuBarColor());
            setForeground(menuStyle.getMenuBarTextColor());
        }
    }
    
    public HMenuStyle getMenuStyle() {
        return menuStyle;
    }
    
    public void setMenuStyle(HMenuStyle style) {
        this.menuStyle = style;
        applyStyle();
        // Mettre à jour tous les menus enfants
        for (int i = 0; i < getMenuCount(); i++) {
            JMenu menu = getMenu(i);
            if (menu instanceof HMenu) {
                ((HMenu) menu).setMenuStyle(style);
            }
        }
        repaint();
    }
    
    public int getCornerRadius() {
        return cornerRadius;
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (menuStyle != null) {
            g2.setColor(menuStyle.getMenuBarColor());
            if (cornerRadius > 0) {
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            } else {
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }
        
        g2.dispose();
    }
    
    public static HMenuBar withStyle(HMenuStyle style) {
        return new HMenuBar(style);
    }
}