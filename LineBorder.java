package IllustrationShape.vues.border;

import java.awt.*;

/**
 * Bordure pleine, unie, d'épaisseur constante.
 */
public class LineBorder extends AbstractViewBorder {

    private Color color;
    private int thickness;

    public LineBorder(Color color, int thickness) {
        this.setColor(color);
        this.setThickness(thickness);
    }

    // Trace un rectangle plein autour de la zone, sans altérer le style du contexte graphique appelant
    @Override
    public void paintBorder(Graphics2D g, int x, int y, int width, int height) {
        Color savedColor = g.getColor();
        Stroke savedStroke = g.getStroke();
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness));
        g.drawRect(x, y, width, height);
        g.setColor(savedColor);
        g.setStroke(savedStroke);
    }
    
    public void setThickness( int thickness ){
        if (thickness < 0) {
            throw new IllegalArgumentException("The thickness cannot be a negative value.");
        }
        this.thickness = thickness;
    }

    public int getThickness() {
        return thickness;
    }       

    public void setColor( Color color ){
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
    
    @Override
    public Insets getBorderInsets(int width, int height) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}