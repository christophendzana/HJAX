/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.HBorder;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public class DoubleBorder extends HAbstractBorder{
    
     public DoubleBorder() {
        super();
    }

    public DoubleBorder(Color color) {
        super(color);
    }

    public DoubleBorder(Color color, int thickness) {
        super(color, thickness);
    }

    public DoubleBorder(Color color, int thickness, int cornerRadius) {
        super(color, thickness, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
        
     // PREMIÈRE BORDURE
    g2.drawRoundRect(
        thickness / 2, thickness / 2,
        width - thickness, height - thickness,
        radius, radius
    );

    int gap = thickness + 2;
    g2.drawRoundRect(
        gap + thickness / 2, gap + thickness / 2,
        width - 2*gap - thickness, height - 2*gap - thickness,
        radius > gap*2 ? radius - gap*2 : 0,
        radius > gap*2 ? radius - gap*2 : 0
    );
    }        
  
}
