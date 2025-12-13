/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import javax.swing.plaf.basic.BasicOptionPaneUI;

/**
 *
 * @author FIDELE
 */
public class HOptionPaneUI extends BasicOptionPaneUI {

    private HPaneTheme theme;
    
    public HOptionPaneUI(HPaneTheme theme) {
        this.theme = theme;
    }
    
    @Override
    protected void installDefaults() {
        super.installDefaults();
        // Applique notre thème au JOptionPane
        optionPane.setBackground(theme.getBackgroundColor());
        optionPane.setForeground(theme.getTextColor());
        optionPane.setFont(theme.getNormalFont());
    }
    
}
