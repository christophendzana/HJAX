package IllustrationShape.border;

import java.awt.*;

/**
 * Bordure pointillée, motif de tirets configurable (ex. {4, 4} pour tiret-espace égaux).
 */
public class DashedLineBorder extends AbstractViewBorder {

    private final Color color;
    private final float thickness;
    private final float[] dashPattern;

    public DashedLineBorder(Color color, float thickness, float[] dashPattern) {
        this.color = color;
        this.thickness = thickness;
        this.dashPattern = dashPattern;
    }

    // Trace le rectangle avec un BasicStroke à motif de tirets plutôt qu'un trait plein
    @Override
    public void paintBorder(Graphics2D g, int x, int y, int width, int height) {
        Color savedColor = g.getColor();
        Stroke savedStroke = g.getStroke();
        g.setColor(color);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dashPattern, 0f));
        g.drawRect(x, y, width, height);
        g.setColor(savedColor);
        g.setStroke(savedStroke);
    }

    @Override
    public Insets getBorderInsets(int width, int height) {
        int t = (int) Math.ceil(thickness);
        return new Insets(t, t, t, t);
    }

    // Les tirets laissent apparaître le fond entre eux : jamais opaque
    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}