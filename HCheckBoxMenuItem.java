/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HCheckBoxMenuItemUI;
import hcomponents.vues.HListTheme;
import javax.swing.JCheckBoxMenuItem;

/**
 *
 * @author FIDELE
 */
public class HCheckBoxMenuItem extends JCheckBoxMenuItem {
    
    private HListTheme theme;
    
    public HCheckBoxMenuItem() {
        super();
        this.theme = new HListTheme();
        initializeComponent();
    }
    
    public HCheckBoxMenuItem(String text) {
        super(text);
        this.theme = new HListTheme();
        initializeComponent();
    }
    
    private void initializeComponent() {
        setUI(new HCheckBoxMenuItemUI(theme));
        applyThemeSettings();
    }
    
    private void applyThemeSettings() {
        setFont(theme.getNormalFont());
        setOpaque(false);
        setBorderPainted(false);
    }
    
    public HCheckBoxMenuItem setHListTheme(HListTheme theme) {
        this.theme = theme;
        refreshUI();
        return this;
    }
    
    public void refreshUI() {
        setUI(new HCheckBoxMenuItemUI(theme));
        applyThemeSettings();
        repaint();
    }
    
    public HListTheme getHListTheme() { return theme; }
    
}
