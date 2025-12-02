/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HToggleButtonUI;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 *
 * @author FIDELE
 */
public class HToggleButton extends JToggleButton {
    private HAbstractBorder hBorder;
    private HShadow hShadow;
    private int cornerRadius = 12;
    private HButtonStyle buttonStyle = HButtonStyle.PRIMARY;
    private int gap = 0;
    private int checkmarkMargin = 30;
    
     // Constructeur sans paramètre
    public HToggleButton() {
        this("", null, false);
    }
    
    // Constructeur avec texte
    public HToggleButton(String text) {
        this(text, null, false);
    }
    
    // Constructeur avec icône
    public HToggleButton(Icon icon) {
        this("", icon, false);
    }
    
    // Constructeur avec Action
    public HToggleButton(Action a) {
        super(a);
        updateUI();
    }
    
    // Constructeur avec texte et état sélectionné
    public HToggleButton(String text, boolean selected) {
        this(text, null, selected);
    }
    
    // Constructeur avec icône et état sélectionné
    public HToggleButton(Icon icon, boolean selected) {
        this("", icon, selected);
    }
    
    // Constructeur principal avec texte, icône et état sélectionné
    public HToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        updateUI();
    }
    
    // Constructeur avec texte et icône (non sélectionné par défaut)
    public HToggleButton(String text, Icon icon) {
        this(text, icon, false);
    }
    
    
    @Override
    public void updateUI() {
        setUI(new HToggleButtonUI());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
    }
    
    // Getters/Setters similaires à HButton
    public HAbstractBorder getHBorder() { return hBorder; }
    public void setHBorder(HAbstractBorder border) { this.hBorder = border; repaint(); }
    
    public HShadow getShadow() { return hShadow; }
    public void setShadow(HShadow shadow) { this.hShadow = shadow; repaint(); }
    
    public int getCornerRadius() { return cornerRadius; }
    public void setCornerRadius(int radius) { this.cornerRadius = radius; repaint(); }
    
    public int getGap() { return gap; }
    public void setGap(int gap) { this.gap = gap; repaint(); }
    
    public int getCheckMarkMargin(){ return checkmarkMargin; }
    public void setCheckMarkMargin(int check){ this.checkmarkMargin = check; }
    
    public HButtonStyle getButtonStyle() { return buttonStyle; }
    public void setButtonStyle(HButtonStyle style) { 
        this.buttonStyle = style; 
        setForeground(style.getTextColor());
        repaint();
    }
    
    // Factory methods comme HButton
    public static HToggleButton withStyle(String text, HButtonStyle style) {
        HToggleButton button = new HToggleButton(text);
        button.setButtonStyle(style);
        return button;
    }
    
    public static HToggleButton withStyle(String text, Icon icon, HButtonStyle style) {
        HToggleButton button = new HToggleButton(text, icon);
        button.setButtonStyle(style);
        return button;
    }
    
}
