package IllustrationShape;

import IllustrationShape.vues.border.DashedLineBorder;
import IllustrationShape.vues.border.LineBorder;
import IllustrationShape.vues.border.CompoundBorder;
import IllustrationShape.vues.border.EmptyBorder;
import IllustrationShape.vues.border.ViewBorder;
import java.awt.Color;

/**
 * Point d'entrée pratique pour créer les bordures prédéfinies, à la manière de javax.swing.BorderFactory.
 */
public final class ViewBorderFactory {

    private ViewBorderFactory() {}

    public static ViewBorder createLineBorder(Color color, int thickness) {
        return new LineBorder(color, thickness);
    }

    public static ViewBorder createDashedLineBorder(Color color, float thickness, float[] dashPattern) {
        return new DashedLineBorder(color, thickness, dashPattern);
    }

    public static ViewBorder createEmptyBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static ViewBorder createCompoundBorder(ViewBorder outside, ViewBorder inside) {
        return new CompoundBorder(outside, inside);
    }
    
}