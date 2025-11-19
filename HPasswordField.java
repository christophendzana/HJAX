/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.controllers.HPasswordFieldController;
import hcomponents.models.HPasswordFieldModel;
import hcomponents.vues.HPasswordFieldUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JPasswordField;

/**
 *
 * @author FIDELE
 */
public class HPasswordField extends JPasswordField {

    private HPasswordFieldModel model;
    private HPasswordFieldController controller;

    public HPasswordField() {
        this.model = new HPasswordFieldModel();
        this.setUI(new HPasswordFieldUI());
        this.controller = new HPasswordFieldController(this, model);
         setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
         setFont(getFont().deriveFont(Font.BOLD, 16f));
        this.setOpaque(false);
        model.setOnChange(() -> updateVisibility());
        repaint();
    }

    public HPasswordFieldModel getHModel() {
        return model;
    }

    //Ajout de la marge droite pour l'espace de l'icone
    @Override
    public Insets getInsets(Insets insets) {
        super.getInsets(insets);
        insets.right += 60; // reserve la place de l'icone
        return insets;
    }

    public HPasswordFieldController getHController() {
        return controller;
    }

    public void updateVisibility() {
        if (model.isVisible()) {
            this.setEchoChar((char) 0);
        } else {
            this.setEchoChar(model.getMaskChar());
        }
    }
     
    @Override
protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getWidth();
    int h = getHeight();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Couleurs
    Color basicColor = new Color(14, 146, 161);  // par défaut
    Color hoverRed = new Color(180, 0, 0);     // hover/focus
    Color borderColor = basicColor;

    if (hasFocus() || model.isHovered()) {
        borderColor = hoverRed;
    }

    // Ombre portée (optionnelle)
    g2.setColor(new Color(102, 87, 213, 45));
    g2.fill(new RoundRectangle2D.Float(2, 2, w-4, h-4, 18, 18));

    // Fond arrondi
    g2.setColor(new Color (225, 241, 242)); // blanc ou transparent
    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));

    // Bordure
    g2.setColor(borderColor);
    g2.setStroke(new BasicStroke(2));
    g2.draw(new RoundRectangle2D.Float(0, 0, w-1, h-1, 20, 20));

    g2.dispose();

    super.paintComponent(g); // texte + caret
}

}
