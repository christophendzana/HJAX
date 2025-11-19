/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

import javax.swing.DefaultButtonModel;

/**
 *
 * @author FIDELE
 */
public class HRadioButtonModel extends DefaultButtonModel {
    
    private boolean hover = false;
    private boolean clicked = false;
    
    public HRadioButtonModel(){        
    }
    
    public boolean isHover(){
        return hover;
    }    
       
    public void setHover(Boolean hover){
        this.hover = hover;  
        fireStateChanged();
    }
    
    public boolean isClicked(){
        return clicked;
    }
    
    public void setClicked(Boolean clicked){
        this.clicked = clicked;
        fireStateChanged();
    }
    
}
