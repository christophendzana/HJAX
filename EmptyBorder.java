package IllustrationShape.border;

import java.awt.*;

/**
 * Bordure invisible : ne dessine rien, sert uniquement à réserver un espace (insets).
 */
public class EmptyBorder implements ViewBorder {

    private final int top, left, bottom, right;

    public EmptyBorder(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    @Override
    public void paintBorder(Graphics2D g, int x, int y, int width, int height) {
        // volontairement vide
    }

    @Override
    public Insets getBorderInsets(int width, int height) {
        return new Insets(top, left, bottom, right);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}