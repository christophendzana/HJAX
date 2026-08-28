package IllustrationShape.border;

import java.awt.Insets;

/**
 * Base pratique pour une bordure personnalisée : fournit des valeurs par défaut
 * neutres, il ne reste plus qu'à surcharger paintBorder().
 */
public abstract class AbstractViewBorder implements ViewBorder {

    @Override
    public Insets getBorderInsets(int width, int height) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}