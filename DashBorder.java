/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public class DashBorder extends HAbstractBorder {
    
   public DashBorder() {
        super();
    }

    public DashBorder(Color color) {
        super(color);
    }

    public DashBorder(Color color, int thickness) {
        super(color, thickness);
    }

    public DashBorder(Color color, int thickness, int cornerRadius) {
        super(color, thickness, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
        
      float dash[] = {7.0f, 7.0f};
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(
            thickness,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, dash, 0.0f
        ));
        g2.drawRoundRect(
            thickness / 2, thickness / 2,
            width - thickness, height - thickness,
            radius, radius
        );
        g2.setStroke(oldStroke);
    }
        
    
}
