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
public class SolidBorder extends HAbstractBorder {

    public SolidBorder() {
        super();
    }

    public SolidBorder(Color color) {
        super(color);
    }

    public SolidBorder(Color color, int thickness) {
        super(color, thickness);
    }

    public SolidBorder(Color color, int thickness, int cornerRadius) {
        super(color, thickness, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
        
        if (radius <= 0) {
            g2.drawRect(0, 0, width - 1, height - 1);
        } else {
            g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
        }
    }

}
