/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HCheckBoxController;
import hcomponents.models.HCheckBoxModel;
import hcomponents.vues.HCheckBoxUI;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

/**
 *
 * @author FIDELE
 */
public class HCheckBox extends JCheckBox {
    
   private final HCheckBoxModel model;
    private final HCheckBoxController controller;
    public HCheckBox(String text) {
        super(text);
        model = new HCheckBoxModel();
        setModel(model);
        setUI(new HCheckBoxUI(model));
        controller = new HCheckBoxController(this, model);
        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        setFont(getFont().deriveFont(Font.BOLD, 15f));
        setOpaque(false);
    }
    public HCheckBox() { this(""); }
}
