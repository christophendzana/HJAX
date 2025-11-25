/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.HBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public class DoubleDashBorder extends HAbstractBorder{
    
    public DoubleDashBorder() {
        super();
    }

    public DoubleDashBorder(Color color) {
        super(color);
    }

    public DoubleDashBorder(Color color, int thickness) {
        super(color, thickness);
    }

    public DoubleDashBorder(Color color, int thickness, int cornerRadius) {
        super(color, thickness, cornerRadius);
    }

    @Override
    protected void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
        
    float dash[] = {6.0f, 6.0f}; // longueur du "tiret" puis de l'espace
    BasicStroke dashedStroke = new BasicStroke(
        thickness,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER,
        10.0f, dash, 0.0f
    );

    // PREMIÈRE BORDURE (EXTERNE)
    
    g2.setStroke(dashedStroke);
    g2.drawRoundRect(
        thickness / 2, thickness / 2,
        width - thickness, height - thickness,
        radius, radius
    );

    // SECONDE COULEUR (optionnel, sinon retire ça)
    // g2.setColor(prevColor.darker());

    // DEUXIÈME BORDURE (INTERNE)
    int gap = thickness + 2;
    g2.drawRoundRect(
        gap + thickness / 2, gap + thickness / 2,
        width - 2 * gap - thickness, height - 2 * gap - thickness,
        radius > gap * 2 ? radius - gap * 2 : 0,
        radius > gap * 2 ? radius - gap * 2 : 0
    );

    // Remet l'ancienne couleur/stroke
    
    g2.setStroke(new BasicStroke());
    }
    
}
