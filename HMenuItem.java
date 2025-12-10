/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicMenuItemUI;
import hcomponents.vues.HMenuStyle;
import javax.swing.*;

/**
 * Item de menu personnalisé avec design moderne.
 * Étend JMenuItem pour offrir des animations fluides et styles prédéfinis.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JMenuItem
 * @see HMenuStyle
 */
public class HMenuItem extends JMenuItem {
    
    private HMenuStyle menuStyle = HMenuStyle.PRIMARY;
    private int FontMenuItemSise = 14;
    private int cornerRadius = 8;
    
    public HMenuItem() {
        super();
        updateUI();
    }
    
    public HMenuItem(String text) {
        super(text);
        updateUI();
    }
    
    public HMenuItem(Icon icon) {
        super(icon);
        updateUI();
    }
    
    public HMenuItem(String text, Icon icon) {
        super(text, icon);
        updateUI();
    }
    
    public HMenuItem(String text, int mnemonic) {
        super(text, mnemonic);
        updateUI();
    }
    
    public HMenuItem(Action a) {
        super(a);
        updateUI();
    }
    
    @Override
    public void updateUI() {
        setUI(new HBasicMenuItemUI());
        setOpaque(false);
    }
    
    public HMenuStyle getMenuStyle() {
        return menuStyle;
    }
    
    public void setMenuStyle(HMenuStyle style) {
        this.menuStyle = style;
        setForeground(style.getItemTextColor());
        repaint();
    }
    
    public int getCornerRadius() {
        return cornerRadius;
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
    
    public int getFontMenuItemSize(){
        return FontMenuItemSise;
    }
    
    public void setFontMenuItemSize(int size){
        this.FontMenuItemSise = size;
    }
    
    public static HMenuItem withStyle(String text, HMenuStyle style) {
        HMenuItem item = new HMenuItem(text);
        item.setMenuStyle(style);
        return item;
    }
    
    public static HMenuItem withStyle(String text, Icon icon, HMenuStyle style) {
        HMenuItem item = new HMenuItem(text, icon);
        item.setMenuStyle(style);
        return item;
    }
}