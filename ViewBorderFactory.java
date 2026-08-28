package IllustrationShape;

import IllustrationShape.border.ViewBorder;
import IllustrationShape.border.*;
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

    // La bordure spéciale des poignées : liée à une HViewShape précise, jamais partagée entre plusieurs formes
    public static ViewBorder createResizeBorder(HViewShape shape) {
        return new ResizeBorder(shape);
    }
}