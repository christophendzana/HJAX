/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.models.HDefaultTextModel;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author FIDELE
 */
public class HTextFieldController {
    
    private final JTextField field;
    private final HDefaultTextModel model;

    public HTextFieldController(JTextField field, HDefaultTextModel model) {
        this.field = field;
        this.model = model;

        // 🖱️ Gère le survol
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                model.setHovered(true);
                field.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                model.setHovered(false);
                field.repaint();
            }
        });

        // 🎯 Gère le focus (clic à l’intérieur ou à l’extérieur)
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                model.setFocused(true);
                model.setShowPlaceholder(false); // on cache le placeholder
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                model.setFocused(false);

                // si le champ est vide, on réaffiche le placeholder
                if (field.getText().isEmpty()) {
                    model.setShowPlaceholder(true);
                }

                field.repaint();
            }
        });
    }
    
}
