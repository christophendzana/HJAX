/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.shadow;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author FIDELE
 */
public class HShadowSM extends HAbstractShadow {
     public HShadowSM() {
        super(new Color(0, 0, 0), 0.1f, 2, 0, 1);
    }
    
    @Override
    protected void paintBlurredShadow(Graphics2D g2d, int width, int height, int radius) {
        // Ombre très légère
        g2d.setColor(new Color(0, 0, 0, 25));
        g2d.fillRoundRect(1, 1, width - 2, height - 2, radius, radius);
    }
}
