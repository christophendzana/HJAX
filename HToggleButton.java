/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HToggleButtonController;
import hcomponents.models.HToggleButtonModel;
import hcomponents.vues.HToggleButtonUI;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

/**
 *
 * @author FIDELE
 */
public class HToggleButton extends JToggleButton {
    
    private final HToggleButtonModel model;
    private final HToggleButtonController controller;
    public static JComponent lastPainted; 

    public HToggleButton(String text) {
        super(text);
        model = new HToggleButtonModel();
        controller = new HToggleButtonController(this, model);

        setUI(new HToggleButtonUI(model));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setFont(getFont().deriveFont(Font.BOLD, 16f));
        setOpaque(false);
    }

    public HToggleButton() {
        this("");
    }
    
}
