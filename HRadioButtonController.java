/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HRadioButton;
import hcomponents.models.HRadioButtonModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author FIDELE
 */
public class HRadioButtonController {
    
    private HRadioButton button;
    private HRadioButtonModel model;
    
    public HRadioButtonController(HRadioButton button, HRadioButtonModel model){
        this.button = button;
        this.model = model;
        intiMouseHover();
    }
    
    private void intiMouseHover(){
        button.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e){
//                model.setClicked(false);
            }
            
            @Override
            public void mouseEntered(MouseEvent e){
                model.setHover(true);
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e){
                model.setHover(false);
                button.repaint();
            }
            
        });
    }
    
    
}
