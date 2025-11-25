/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.HBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public abstract class HAbstractBorder implements HBorder {

    protected Color color;
    protected int thickness;
    protected int cornerRadius;

    // Constructeurs
    public HAbstractBorder() {
        this.color = Color.BLACK;
        this.thickness = 1;
        this.cornerRadius = 0;
    }

    public HAbstractBorder(Color color) {
        this.color = color;
        this.thickness = 1;
        this.cornerRadius = 0;
    }

    public HAbstractBorder(Color color, int thickness) {
        this.color = color;
        this.thickness = thickness;
        this.cornerRadius = 0;
    }

    public HAbstractBorder(Color color, int thickness, int cornerRadius) {
        this.color = color;
        this.thickness = thickness;
        this.cornerRadius = cornerRadius;
    }

    // Getters / setters
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

    // Méthode paint finale, applique stroke et couleur
    @Override
    public final void paint(Graphics2D g2, JComponent c, int width, int height) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintBorder(g2, c, width, height, cornerRadius);
    }

    @Override
    public final void paint(Graphics2D g2, JComponent c, int width, int height, int radius) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        paintBorder(g2, c, width, height, radius);
    }

    protected abstract void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius);

}
