/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HListTheme;
import hcomponents.vues.HRadioButtonMenuItemUI;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author FIDELE
 */
public class HRadioButtonMenuItem extends JRadioButtonMenuItem {
    
    private HListTheme theme;
    
    public HRadioButtonMenuItem() {
        super();
        this.theme = new HListTheme();
        initializeComponent();
    }
    
    public HRadioButtonMenuItem(String text) {
        super(text);
        this.theme = new HListTheme();
        initializeComponent();
    }
    
    private void initializeComponent() {
        setUI(new HRadioButtonMenuItemUI(theme));
        applyThemeSettings();
    }
    
    private void applyThemeSettings() {
        setFont(theme.getNormalFont());
        setOpaque(false);
        setBorderPainted(false);
    }
    
    public HRadioButtonMenuItem setHListTheme(HListTheme theme) {
        this.theme = theme;
        refreshUI();
        return this;
    }
    
    public void refreshUI() {
        setUI(new HRadioButtonMenuItemUI(theme));
        applyThemeSettings();
        repaint();
    }
    
    public HListTheme getHListTheme() { return theme; }
    
}
