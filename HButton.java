/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HMouseAdapter;
import hcomponents.models.HDefaultButtonModel;
import hcomponents.vues.HBasicButtonUI;
import java.awt.Color;
import javax.swing.JButton;

/**
 *
 * @author FIDELE
 */
public class HButton extends JButton {

    private final HDefaultButtonModel model;
    private final HMouseAdapter controller;

    public HButton(String text) {
        super(text);
        this.model = new HDefaultButtonModel();
        this.controller = new HMouseAdapter(this, model);

        setModel(model);
        setUI(new HBasicButtonUI());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        addMouseListener(controller);
    }

    

}
