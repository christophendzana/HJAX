/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HTextFieldController;
import hcomponents.models.HDefaultTextModel;
import hcomponents.vues.HBasicTextFieldUI;
import javax.swing.JTextField;

/**
 *
 * @author FIDELE
 */
public class HTextField extends JTextField {
    
     private final HDefaultTextModel model;
    private final HTextFieldController controller;

    public HTextField(String placeholder) {
        super();

        // 1️⃣ Modèle
        this.model = new HDefaultTextModel();
        model.setPlaceholder(placeholder);
        setDocument(model);

        // 2️⃣ Vue (UI delegate)
        setUI(new HBasicTextFieldUI());

        // 3️⃣ Apparence
        setBorder(null);
        setOpaque(false);
        setCaretColor(getForeground());

        // 4️⃣ Contrôleur
        this.controller = new HTextFieldController(this, model);
    }
    
}
