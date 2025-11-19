/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.models.HDefaultTextModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicTextFieldUI;

/**
 *
 * @author FIDELE
 */
public class HBasicTextFieldUI extends BasicTextFieldUI {

    private int cornerRadius = 15;
    private Color baseBorderColor = new Color(150, 150, 150);
    private Color hoverBorderColor = new Color(100, 100, 250);
    private Color focusBorderColor = new Color(33, 150, 243);
    private Color textColor = Color.BLACK;
    private String placeholder = "";

        protected void paintSafely(Graphics g) {
        super.paintSafely(g);

            JTextField field = (JTextField) getComponent();
            HDefaultTextModel model = (HDefaultTextModel) field.getDocument();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = field.getWidth();
            int height = field.getHeight();

            // Couleur de bordure selon l’état
            Color borderColor;
            if (model.isFocused()) {
                borderColor = new Color(33, 150, 243);     // bleu focus
            } else if (model.isHovered()) {
                borderColor = new Color(120, 144, 156); // gris clair hover
            } else {
                borderColor = new Color(189, 189, 189);                     // gris normal
            }
            // Dessiner la bordure arrondie
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(borderColor);
            g2.drawRoundRect(1, 1, width - 3, height - 3, 10, 10);

            // ✏️ Dessiner le placeholder
            if (model.isShowPlaceholder() && model.getPlaceholder() != null) {
                g2.setColor(new Color(150, 150, 150)); // gris clair
                g2.setFont(field.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textY = (height + fm.getAscent()) / 2 - 3;
                g2.drawString(model.getPlaceholder(), 8, textY);
            }

            g2.dispose();
        }
   

    // Setters pour personnalisation
    public void setBaseBorderColor(Color baseBorderColor) {
        this.baseBorderColor = baseBorderColor;
    }

    public void setHoverBorderColor(Color hoverBorderColor) {
        this.hoverBorderColor = hoverBorderColor;
    }

    public void setFocusBorderColor(Color focusBorderColor) {
        this.focusBorderColor = focusBorderColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

}
