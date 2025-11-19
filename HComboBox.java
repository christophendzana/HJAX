/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HComboBoxController;
import hcomponents.models.HComboBoxModel;
import hcomponents.vues.HComboBoxRenderer;
import hcomponents.vues.HComboBoxUI;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;

/**
 *
 * @author FIDELE
 */
public class HComboBox<E> extends JComboBox {
    
    private final HComboBoxModel<E> model;
    private final HComboBoxController<E> controller;
    
    public HComboBox(E[] items) {
        super(new HComboBoxModel<>(items));
        this.model = (HComboBoxModel<E>) getModel();
        this.controller = new HComboBoxController<>(this, model);

        setUI(new HComboBoxUI(model));
        setRenderer(new HComboBoxRenderer());
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setFont(getFont().deriveFont(Font.BOLD, 16f));
        setOpaque(false);
    }

    public HComboBox() {
        super(new HComboBoxModel<E>());
        this.model = (HComboBoxModel<E>) getModel();
        this.controller = new HComboBoxController<>(this, model);

        setUI(new HComboBoxUI(model));
        setRenderer(new HComboBoxRenderer());
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setFont(getFont().deriveFont(Font.BOLD, 16f));
        setOpaque(false);
    }
    
}
