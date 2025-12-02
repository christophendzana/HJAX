/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.shadow;

import java.awt.Graphics2D;

/**
 *
 * @author FIDELE
 */
public class ShadowFactory {
    private ShadowFactory() {
        // Classe utilitaire, pas d'instanciation
    }
    
    public static HShadow createSmallShadow() {
        return new HShadowSM();
    }
    
    public static HShadow createMediumShadow() {
        return new HShadowMD();
    }
    
    public static HShadow createLargeShadow() {
        return new HShadowLG();
    }
    
    public static HShadow createFloatingShadow() {
        return new HShadowFloating();
    }
    
    public static HShadow createInnerShadow() {
        return new HShadowInner();
    }
    
    public static HShadow createNoShadow() {
        return new HShadow() {
            @Override
            public void paint(Graphics2D g2, javax.swing.JComponent c, int width, int height, int radius) {
                // Ne rien faire - pas d'ombre
            }
        };
    }
}
