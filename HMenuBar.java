/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;
import javax.swing.JMenuBar;

/**
 *
 * @author FIDELE
 */
public class HMenuBar extends JMenuBar {
    
    private HListTheme theme;
    
    public HMenuBar() {
        super();
        this.theme = new HListTheme();
        initializeComponent();
    }
    
    private void initializeComponent() {
        setUI(new HMenuBarUI(theme));
        applyThemeSettings();
    }
    
    private void applyThemeSettings() {
        setFont(theme.getNormalFont());
        setOpaque(false);
        setBorderPainted(false);
    }
    
    public HMenuBar setHListTheme(HListTheme theme) {
        this.theme = theme;
        refreshUI();
        return this;
    }
    
    public void refreshUI() {
        setUI(new HMenuBarUI(theme));
        applyThemeSettings();
        repaint();
    }
    
    public HListTheme getHListTheme() { return theme; }
    
}
