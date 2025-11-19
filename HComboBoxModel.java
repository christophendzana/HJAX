/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author FIDELE
 */
public class HComboBoxModel<E> extends DefaultComboBoxModel<E> {
    
    // Etats graphiques
    private boolean hovered = false;
    private boolean focused = false;
    private boolean pressed = false;

    // Pour la notification au UI (observers):
    private final List<Runnable> stateListeners = new ArrayList<>();

    public HComboBoxModel(E[] items) { super(items); }
    public HComboBoxModel() { super(); }

    // GETTERS
    public boolean isHovered() { return hovered; }
    public boolean isFocused() { return focused; }
    public boolean isPressed() { return pressed; }

    // SETTERS
    public void setHovered(boolean hovered) {
        this.hovered = hovered;
        notifyStateListeners();
    }
    public void setFocused(boolean focused) {
        this.focused = focused;
        notifyStateListeners();
    }
    public void setPressed(boolean pressed) {
        this.pressed = pressed;
        notifyStateListeners();
    }

    // Système d’écouteurs pour la vue:
    public void addStateListener(Runnable r) { stateListeners.add(r); }
    private void notifyStateListeners() {
        for (Runnable r : stateListeners) r.run();
    }
    
    public void clear() {
        int size = getSize();
        for (int i = size - 1; i >= 0; i--) {
            removeElementAt(i);
        }
    }
    
}
