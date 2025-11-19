/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

/**
 *
 * @author FIDELE
 */
public class HPasswordFieldModel  {
    
    private boolean visible;
    public boolean hovered = false ;
    private char maskChar = '.';
    private int minLength = 0;
    private Runnable onChange;
    
    public boolean isVisible(){ return visible; }    
    public void setVisible(boolean visible) { 
        this.visible = visible; 
        if (onChange != null) {
            onChange.run();
        }
    }
    
    public char getMaskChar(){ return maskChar; }
    public void setMaskChar(char maskChar) { this.maskChar = maskChar; }
    
    public int getMinLenght() { return minLength; }
    public void setMinLength(int minLenght){ this.minLength = minLenght; }
    
    public boolean isHovered(){
        return hovered;
    }
    
    public void setHovered(boolean hovered){
        this.hovered = hovered;
        if (onChange != null) onChange.run(); 
    }
    
    public void setOnChange(Runnable r){
        this.onChange = r;
    }
    
}
