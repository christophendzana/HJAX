package IllustrationShape.border;

import illustrations.border.*;
import java.awt.*;

/**
 * Bordure pleine, unie, d'épaisseur constante.
 */
public class LineBorder extends AbstractViewBorder {

    private final Color color;
    private final int thickness;

    public LineBorder(Color color, int thickness) {
        this.color = color;
        this.thickness = thickness;
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

    @Override
    public Insets getBorderInsets(int width, int height) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}