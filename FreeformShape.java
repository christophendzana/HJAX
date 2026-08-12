package illustrations.shapes;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Forme définie par une liste de points plutôt qu'un rectangle englobant :
 * Courbe (lissée), Forme libre, Dessin à main levée.
 */
public class FreeformShape {

    private final List<Point2D> points;
    private final boolean smooth; // true = Courbe (lissée), false = Forme libre / main levée

    public FreeformShape(List<Point2D> points, boolean smooth) {
        this.points = points;
        this.smooth = smooth;
    }

    // Construit la géométrie : jamais de remplissage pour ces formes, uniquement un tracé
    public ShapeGeometry buildGeometry() {
        Path2D path = new Path2D.Double();
        if (points.isEmpty()) {
            return new ShapeGeometry(path, null);
        }
        Point2D first = points.get(0);
        path.moveTo(first.getX(), first.getY());

        if (smooth) {
            // Relie chaque point via une courbe quadratique passant par le milieu des segments
            for (int i = 1; i < points.size(); i++) {
                Point2D prev = points.get(i - 1);
                Point2D curr = points.get(i);
                double midX = (prev.getX() + curr.getX()) / 2.0;
                double midY = (prev.getY() + curr.getY()) / 2.0;
                path.quadTo(prev.getX(), prev.getY(), midX, midY);
            }
            Point2D last = points.get(points.size() - 1);
            path.lineTo(last.getX(), last.getY());
        } else {
            for (int i = 1; i < points.size(); i++) {
                Point2D p = points.get(i);
                path.lineTo(p.getX(), p.getY());
            }
        }
        return new ShapeGeometry(path, null);
    }
}
