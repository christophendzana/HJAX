/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class HToggleButtonModel {
    
     private boolean hovered = false;
    private boolean focused = false;
    private boolean pressed = false;
    private boolean selected = false;
    private final List<Runnable> listeners = new ArrayList<>();

    // GETTERS/SETTERS
    public boolean isHovered() { return hovered; }
    public boolean isFocused() { return focused; }
    public boolean isPressed() { return pressed; }
    public boolean isSelected() { return selected; }

    public void setHovered(boolean b) { hovered = b; notifyListeners(); }
    public void setFocused(boolean b) { focused = b; notifyListeners(); }
    public void setPressed(boolean b) { pressed = b; notifyListeners(); }
    public void setSelected(boolean b) { selected = b; notifyListeners(); }

    // Listener management
    public void addStateListener(Runnable r) { 
        listeners.add(r); 
    }
    private void notifyListeners() { 
        for (Runnable r : listeners) r.run(); 
    }
    
}
