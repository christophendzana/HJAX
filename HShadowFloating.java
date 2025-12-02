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
public class HShadowFloating extends HAbstractShadow {
    
    public HShadowFloating() {
        super(new Color(0, 0, 0), 0.25f, 15, 0, 6);
    }
    
    @Override
    protected void paintBlurredShadow(Graphics2D g2d, int width, int height, int radius) {
        // Effet d'élévation
        for (int i = 0; i < 3; i++) {
            int alpha = 20 - (i * 5);
            int offset = i * 2;
            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fillRoundRect(offset, offset, width - offset * 2, height - offset * 2, radius, radius);
        }
        
        // Ombre principale
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(3, 6, width - 6, height - 6, radius, radius);
    }
    
}
