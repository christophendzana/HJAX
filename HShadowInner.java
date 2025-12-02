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
public class HShadowInner extends HAbstractShadow {
    
    public HShadowInner() {
        super(new Color(0, 0, 0), 0.15f, 3, 0, 0);
    }
    
    @Override
    protected void paintBlurredShadow(Graphics2D g2d, int width, int height, int radius) {
        // Sauvegarder le clip
        java.awt.Shape originalClip = g2d.getClip();
        
        // Créer une forme arrondie pour le clip
        java.awt.geom.RoundRectangle2D innerShape = 
            new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, radius, radius);
        g2d.clip(innerShape);
        
        // Dessiner l'ombre interne
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawRoundRect(1, 1, width - 2, height - 2, radius, radius);
        
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.drawRoundRect(2, 2, width - 4, height - 4, radius, radius);
        
        // Restaurer le clip
        g2d.setClip(originalClip);
    }
    
}
