/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HCheckBox;
import hcomponents.models.HCheckBoxModel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author FIDELE
 */
public class HCheckBoxController extends MouseAdapter implements FocusListener {
    
     private final HCheckBox checkBox;
    private final HCheckBoxModel model;
    public HCheckBoxController(HCheckBox checkBox, HCheckBoxModel model) {
        this.checkBox = checkBox;
        this.model = model;
        checkBox.addMouseListener(this);
        checkBox.addFocusListener(this);
        checkBox.addItemListener(e -> checkBox.repaint());
    }
    @Override public void mouseEntered(MouseEvent e) { model.setHovered(true); checkBox.repaint(); }
    @Override public void mouseExited(MouseEvent e)  { model.setHovered(false); checkBox.repaint(); }
    @Override public void mousePressed(MouseEvent e) { model.setPressed(true); checkBox.repaint(); }
    @Override public void mouseReleased(MouseEvent e){ model.setPressed(false); checkBox.repaint(); }
    @Override public void focusGained(FocusEvent e)  { model.setFocused(true); checkBox.repaint(); }
    @Override public void focusLost(FocusEvent e)    { model.setFocused(false); checkBox.repaint(); }
}
