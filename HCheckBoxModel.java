/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

import javax.swing.JToggleButton;

/**
 *
 * @author FIDELE
 */
public class HCheckBoxModel extends JToggleButton.ToggleButtonModel {
    
    private boolean hovered = false, focused = false, pressed = false;
    public boolean isHovered() { return hovered; }
    public boolean isFocused() { return focused; }
    public boolean isPressed() { return pressed; }
    public void setHovered(boolean b) { hovered = b; }
    public void setFocused(boolean b) { focused = b; }
    public void setPressed(boolean b) { pressed = b; }
    
}
