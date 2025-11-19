/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HPasswordField;
import hcomponents.models.HPasswordFieldModel;
import hcomponents.vues.HPasswordFieldUI;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 *
 * @author FIDELE
 */
public class HPasswordFieldController {

    private HPasswordFieldModel model;
    private HPasswordField field;

    public HPasswordFieldController(HPasswordField field, HPasswordFieldModel model){
        this.field = field;
        this.model = model;
        this.installListeners();
    }
    
    private void installListeners(){
        field.addMouseListener( new MouseAdapter () {
            @Override
            public void mouseClicked(MouseEvent e){
                Rectangle r =  ( (HPasswordFieldUI) field.getUI()).getEyeButton();
                if (r.contains(e.getPoint())) {
                    model.setVisible(!model.isVisible());
                }
            }                       
        });
        
        field.addMouseMotionListener(new MouseMotionAdapter() {
            
            @Override
            public void mouseMoved(MouseEvent e){
                Rectangle r = ( (HPasswordFieldUI) field.getUI()).getEyeButton();
                boolean inside = r.contains(e.getPoint());
                if (inside != model.isHovered() ) {
                    model.setHovered(inside);
                }
            }            
        });
        
    }
    
}
