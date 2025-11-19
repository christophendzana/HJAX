/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HToggleButton;
import hcomponents.models.HToggleButtonModel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author FIDELE
 */
public class HToggleButtonController {
    
    private final HToggleButton button;
    private final HToggleButtonModel model;

    public HToggleButtonController(HToggleButton button, HToggleButtonModel model) {
        this.button = button;
        this.model = model;

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                model.setHovered(true);
            }
            public void mouseExited(MouseEvent e) {
                model.setHovered(false);
            }
            public void mousePressed(MouseEvent e) { 
                model.setPressed(true);
            }
            public void mouseReleased(MouseEvent e) { 
                model.setPressed(false); 
            }
        });
        button.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                model.setFocused(true); 
            }
            public void focusLost(FocusEvent e) {
                model.setFocused(false); 
            }
        });
        button.addItemListener(e -> model.setSelected(button.isSelected()));
    }
    
}
