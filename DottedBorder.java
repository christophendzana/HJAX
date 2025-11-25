/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.HBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public class DottedBorder extends HAbstractBorder {
    
    public DottedBorder() {
        super();
    }

    public DottedBorder(Color color) {
        super(color);
    }

    public DottedBorder(Color color, int thickness) {
        super(color, thickness);
    }

    public DottedBorder(Color color, int thickness, int cornerRadius) {
        super(color, thickness, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
            float[] pattern = {2f, 4f}; // dot, space
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(
            thickness,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            1f, pattern, 0f
        ));
        g2.drawRoundRect(
            thickness / 2, thickness / 2,
            width - thickness, height - thickness,
            radius, radius
        );
        g2.setStroke(oldStroke);
    }
    
}
