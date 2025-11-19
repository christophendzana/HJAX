/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HComboBox;
import hcomponents.models.HComboBoxModel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author FIDELE
 */
public class HComboBoxController<E> {
    
     private final HComboBox<E> comboBox;
    private final HComboBoxModel<E> model;

    public HComboBoxController(HComboBox<E> comboBox, HComboBoxModel<E> model) {
        this.comboBox = comboBox;
        this.model = model;

        comboBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { model.setHovered(true); }
            @Override
            public void mouseExited(MouseEvent e) { model.setHovered(false); }
            @Override
            public void mousePressed(MouseEvent e) { model.setPressed(true); }
            @Override
            public void mouseReleased(MouseEvent e) { model.setPressed(false); }
        });

        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { model.setFocused(true); }
            @Override
            public void focusLost(FocusEvent e) { model.setFocused(false); }
        });
    }
    
}
