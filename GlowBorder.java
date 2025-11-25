/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.HBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public class GlowBorder extends HAbstractBorder {
    
    public GlowBorder() {
        super();
    }

    public GlowBorder(Color color) {
        super(color);
    }

    public GlowBorder(Color color, int thickness) {
        super(color, thickness);
    }

    public GlowBorder(Color color, int thickness, int cornerRadius) {
        super(color, thickness, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
         // Dessine le glow en plusieurs couches (dégradé radial)
        for (int i = 2; i > 0; i--) {
            g2.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)(this.color.getAlpha()*0.18*i)));
            g2.setStroke(new BasicStroke(thickness + i*2));
            g2.drawRoundRect(
                thickness/2 + i, thickness/2 + i,
                width - thickness - 2*i, height - thickness - 2*i,
                radius, radius
            );
        }
        // Border de base
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRoundRect(
            thickness/2, thickness/2,
            width - thickness, height - thickness,
            radius, radius
        );
    }
    
}
