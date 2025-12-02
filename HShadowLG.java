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
public class HShadowLG extends HAbstractShadow{

    public HShadowLG() {
        super(new Color(0, 0, 0), 0.3f, 10, 0, 4);
    }
    
    @Override
    protected void paintBlurredShadow(Graphics2D g2d, int width, int height, int radius) {
        // Trois couches pour un effet de profondeur
        g2d.setColor(new Color(0, 0, 0, 10));
        g2d.fillRoundRect(1, 1, width - 2, height - 2, radius, radius);
        
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(2, 2, width - 4, height - 4, radius, radius);
        
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(4, 4, width - 8, height - 8, radius, radius);
    }
    
}
