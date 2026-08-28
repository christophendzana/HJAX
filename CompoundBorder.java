package IllustrationShape.border;

import java.awt.Insets;
import java.awt.Graphics2D;

/**
 * Empile deux bordures : l'extérieure sur la zone complète, l'intérieure
 * décalée vers l'intérieur d'exactement les insets de l'extérieure.
 */
public class CompoundBorder implements ViewBorder {

    private final ViewBorder outside;
    private final ViewBorder inside;

    public CompoundBorder(ViewBorder outside, ViewBorder inside) {
        this.outside = outside;
        this.inside = inside;
    }

    // Dessine l'extérieure d'abord, puis l'intérieure dans la zone restante
    @Override
    public void paintBorder(Graphics2D g, int x, int y, int width, int height) {
        outside.paintBorder(g, x, y, width, height);
        Insets o = outside.getBorderInsets(width, height);
        inside.paintBorder(g, x + o.left, y + o.top, width - o.left - o.right, height - o.top - o.bottom);
    }

    @Override
    public Insets getBorderInsets(int width, int height) {
        Insets o = outside.getBorderInsets(width, height);
        Insets i = inside.getBorderInsets(width, height);
        return new Insets(o.top + i.top, o.left + i.left, o.bottom + i.bottom, o.right + i.right);
    }

    @Override
    public boolean isBorderOpaque() {
        return outside.isBorderOpaque() && inside.isBorderOpaque();
    }
}