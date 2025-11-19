/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.models.HDefaultButtonModel;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author FIDELE
 */
public class HBasicButtonUI extends BasicButtonUI {
    
    private final int cornerRadius = 48;
    private final Color baseColor = new Color(66, 165, 245);
    private final Color hoverColor = new Color(33, 150, 243);
    private final Color pressColor = new Color(30, 136, 229);
    private final Color textColor = Color.BLACK;
    private final Color shadowColor = new Color(0, 0, 0, 40);

    @Override
    public void paint(Graphics g, JComponent c) { //paint: methode de dessin d'un JComponent
        AbstractButton b = (AbstractButton) c;
        HDefaultButtonModel model = (HDefaultButtonModel) b.getModel();
        Graphics2D g2 = (Graphics2D) g.create();

        int width = c.getWidth();
        int height = c.getHeight();

        // Lissage
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Couleur selon l'état
        Color currentColor = baseColor;
        if (model.isPressed()) currentColor = pressColor;
        else if (model.isHovered()) currentColor = hoverColor;

        // Ombre
        g2.setColor(shadowColor);
        g2.fillRoundRect(3, 3, width - 6, height - 6, cornerRadius, cornerRadius);

        // Fond
        GradientPaint gradient = new GradientPaint(0, 0, currentColor.brighter(), 0, height, currentColor.darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width - 6, height - 6, cornerRadius, cornerRadius);

        // Texte
        FontMetrics fm = g2.getFontMetrics(); //Fournit les infos sur le texte
        String text = b.getText();
        if (text != null) {
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int x = (width - textWidth) / 2;
            int y = (height + textHeight) / 2 - 4;
            g2.setColor(textColor);
            g2.drawString(text, x, y);
        }

        g2.dispose(); //Libérer la mémoire et les flux de données
    }
    
//    public void setBaseColor(Color color){
//        this.baseColor = color;
//    }
//    
//    public void setHoverColor(Color color){
//        this.hoverColor = color;
//    }
//    
//    public void setPressColor(Color color){
//        this.pressColor = color;
//    }
//    
//    public void setShadowColor(Color color){
//        this.shadowColor = color;
//    }
//    
//    public void setTextColor(Color color){
//        this.textColor = color;
//    }
    
}
