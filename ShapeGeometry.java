package illustrations.shapes;

import java.awt.Shape;

/**
 * Résultat géométrique produit par un ShapeBuilder : le tracé à dessiner (stroke)
 * et, optionnellement, la zone à remplir (fill, null pour une forme ouverte comme une ligne).
 */
public record ShapeGeometry(Shape stroke, Shape fill) {}