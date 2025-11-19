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
public class HDefaultButtonModel extends DefaultButtonModel {
    
    private int elevationLevel = 1;
    private boolean rippleEnabled = true;
    private boolean hovered = false;
    private boolean pressed = false;

    public int getElevationLevel() {
        return elevationLevel;
    }

    public void setElevationLevel(int elevationLevel) {
        this.elevationLevel = elevationLevel;
    }

    public boolean isRippleEnabled() {
        return rippleEnabled;
    }

    public void setRippleEnabled(boolean rippleEnabled) {
        this.rippleEnabled = rippleEnabled;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }
    
}
