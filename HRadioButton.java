/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HRadioButtonController;
import hcomponents.models.HRadioButtonModel;
import hcomponents.vues.HMetalRadioButtonUI;
import javax.swing.JRadioButton;

/**
 *
 * @author FIDELE
 */
public class HRadioButton extends JRadioButton {
    
   private HRadioButtonModel model;
   private HRadioButtonController controller;
    
    public HRadioButton(){
        super();
    }
    
    public HRadioButton(String text){
        super(text);
        this.model = new HRadioButtonModel();
        setModel(model);
        this.controller = new HRadioButtonController(this, model);
        updateUI();
    }
    
    public HRadioButton(String text, Boolean selected){
        super(text, selected);
    }
    
    @Override
    public void updateUI(){
        setUI(new HMetalRadioButtonUI());
    }
    
}
