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
public class HShadowMD extends HAbstractShadow{

     public HShadowMD() {
        super(new Color(0, 0, 0), 0.2f, 5, 2, 4);
    }
    
    @Override
    protected void paintBlurredShadow(Graphics2D g2d, int width, int height, int radius) {
        // Première couche (flou)
        g2d.setColor(new Color(0, 0, 0, 15));
        g2d.fillRoundRect(1, 1, width - 2, height - 2, radius, radius);
        
        // Deuxième couche (principale)
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(2, 2, width - 4, height - 4, radius, radius);
    }
    
}
