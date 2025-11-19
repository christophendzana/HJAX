/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

/**
 *
 * @author FIDELE
 */
public class HDefaultTextModel extends javax.swing.text.PlainDocument {
    
    private boolean hovered = false;
    private boolean focused = false;

    private String placeholder = "";
    private boolean showPlaceholder = true;

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean isShowPlaceholder() {
        return showPlaceholder;
    }

    public void setShowPlaceholder(boolean showPlaceholder) {
        this.showPlaceholder = showPlaceholder;
    }
    
}
