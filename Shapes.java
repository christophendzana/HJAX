package illustrations;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public class Shapes {

    // Taille par défaut des pointes de flèche
    private static final double ARROW_LENGTH = 12.0;
    private static final double ARROW_WIDTH = 8.0;

    // Résultat de construction d'une forme : ce qui est tracé (contour)
    // et ce qui est rempli (peut être null si rien à remplir)
    public record ShapeGeometry(Shape stroke, Shape fill) {}

    public enum ShapeType {

        SIMPLE_LINE {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return lineWithArrows(x, y, w, h, false, false);
            }
        },
        LINE_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return lineWithArrows(x, y, w, h, false, true);
            }
        },
        DOUBLE_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return lineWithArrows(x, y, w, h, true, true);
            }
        },
        STRAIGHT_CONNECTOR {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return lineWithArrows(x, y, w, h, false, false);
            }
        },
        STRAIGHT_CONNECTOR_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return lineWithArrows(x, y, w, h, false, true);
            }
        },
        STRAIGHT_CONNECTOR_DOUBLE_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return lineWithArrows(x, y, w, h, true, true);
            }
        },
        CURVED_CONNECTOR {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return curveWithArrows(x, y, w, h, false, false);
            }
        },
        CURVED_CONNECTOR_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return curveWithArrows(x, y, w, h, false, true);
            }
        },
        CURVED_CONNECTOR_DOUBLE_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return curveWithArrows(x, y, w, h, true, true);
            }
        },
        ELBOW_CONNECTOR {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return elbowWithArrows(x, y, w, h, false, false);
            }
        },
        ELBOW_CONNECTOR_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return elbowWithArrows(x, y, w, h, false, true);
            }
        },
        ELBOW_CONNECTOR_DOUBLE_ARROW {
            public ShapeGeometry build(int x, int y, int w, int h) {
                return elbowWithArrows(x, y, w, h, true, true);
            }
        };

        public abstract ShapeGeometry build(int x, int y, int w, int h);

        // --- Helpers partagés, utilisés par plusieurs constantes ---

        // Segment droit, avec pointe(s) de flèche optionnelle(s) aux extrémités
        private static ShapeGeometry lineWithArrows(int x, int y, int w, int h,
                                                      boolean arrowAtStart, boolean arrowAtEnd) {
            Point2D start = new Point2D.Double(x, y);
            Point2D end = new Point2D.Double(x + w, y + h);
            double angle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());

            Path2D stroke = new Path2D.Double();
            stroke.append(new Line2D.Double(start, end), false);

            Path2D fill = new Path2D.Double();
            if (arrowAtEnd) {
                Path2D arrow = buildArrowHead(end, angle, ARROW_LENGTH, ARROW_WIDTH);
                stroke.append(arrow, false);
                fill.append(arrow, false);
            }
            if (arrowAtStart) {
                Path2D arrow = buildArrowHead(start, angle + Math.PI, ARROW_LENGTH, ARROW_WIDTH);
                stroke.append(arrow, false);
                fill.append(arrow, false);
            }
            return new ShapeGeometry(stroke, fill.getCurrentPoint() == null ? null : fill);
        }

        // Courbe quadratique par défaut : point de contrôle décalé perpendiculairement au milieu
        private static QuadCurve2D defaultCurve(int x, int y, int w, int h) {
            double x1 = x, y1 = y, x2 = x + w, y2 = y + h;
            double length = Math.hypot(x2 - x1, y2 - y1);
            if (length == 0) length = 1; // évite une division par zéro sur une courbe de longueur nulle
            double offset = length * 0.25;
            double perpX = -(y2 - y1) / length;
            double perpY = (x2 - x1) / length;
            double ctrlX = (x1 + x2) / 2.0 + perpX * offset;
            double ctrlY = (y1 + y2) / 2.0 + perpY * offset;
            return new QuadCurve2D.Double(x1, y1, ctrlX, ctrlY, x2, y2);
        }

        private static ShapeGeometry curveWithArrows(int x, int y, int w, int h,
                                                       boolean arrowAtStart, boolean arrowAtEnd) {
            QuadCurve2D curve = defaultCurve(x, y, w, h);
            Point2D start = curve.getP1();
            Point2D ctrl = curve.getCtrlPt();
            Point2D end = curve.getP2();

            double angleEnd = Math.atan2(end.getY() - ctrl.getY(), end.getX() - ctrl.getX());
            double angleStart = Math.atan2(ctrl.getY() - start.getY(), ctrl.getX() - start.getX());

            Path2D stroke = new Path2D.Double();
            stroke.append(curve, false);

            Path2D fill = new Path2D.Double();
            if (arrowAtEnd) {
                Path2D arrow = buildArrowHead(end, angleEnd, ARROW_LENGTH, ARROW_WIDTH);
                stroke.append(arrow, false);
                fill.append(arrow, false);
            }
            if (arrowAtStart) {
                Path2D arrow = buildArrowHead(start, angleStart + Math.PI, ARROW_LENGTH, ARROW_WIDTH);
                stroke.append(arrow, false);
                fill.append(arrow, false);
            }
            return new ShapeGeometry(stroke, fill.getCurrentPoint() == null ? null : fill);
        }

        // Connecteur en L : coude par défaut à mi-largeur
        private static ShapeGeometry elbowWithArrows(int x, int y, int w, int h,
                                                       boolean arrowAtStart, boolean arrowAtEnd) {
            double midX = x + w / 2.0;
            Point2D start = new Point2D.Double(x, y);
            Point2D afterStart = new Point2D.Double(midX, y);
            Point2D beforeEnd = new Point2D.Double(midX, y + h);
            Point2D end = new Point2D.Double(x + w, y + h);

            Path2D path = new Path2D.Double();
            path.moveTo(start.getX(), start.getY());
            path.lineTo(afterStart.getX(), afterStart.getY());
            path.lineTo(beforeEnd.getX(), beforeEnd.getY());
            path.lineTo(end.getX(), end.getY());

            double angleStart = Math.atan2(afterStart.getY() - start.getY(), afterStart.getX() - start.getX());
            double angleEnd = Math.atan2(end.getY() - beforeEnd.getY(), end.getX() - beforeEnd.getX());

            Path2D stroke = new Path2D.Double();
            stroke.append(path, false);

            Path2D fill = new Path2D.Double();
            if (arrowAtEnd) {
                Path2D arrow = buildArrowHead(end, angleEnd, ARROW_LENGTH, ARROW_WIDTH);
                stroke.append(arrow, false);
                fill.append(arrow, false);
            }
            if (arrowAtStart) {
                Path2D arrow = buildArrowHead(start, angleStart + Math.PI, ARROW_LENGTH, ARROW_WIDTH);
                stroke.append(arrow, false);
                fill.append(arrow, false);
            }
            return new ShapeGeometry(stroke, fill.getCurrentPoint() == null ? null : fill);
        }

        // Construit le triangle d'une pointe de flèche, orienté selon "angle", à la position "tip"
        private static Path2D buildArrowHead(Point2D tip, double angle, double length, double width) {
            double baseX = tip.getX() - length * Math.cos(angle);
            double baseY = tip.getY() - length * Math.sin(angle);
            double perpAngle = angle + Math.PI / 2;
            double dx = (width / 2.0) * Math.cos(perpAngle);
            double dy = (width / 2.0) * Math.sin(perpAngle);

            Path2D.Double arrow = new Path2D.Double();
            arrow.moveTo(tip.getX(), tip.getY());
            arrow.lineTo(baseX + dx, baseY + dy);
            arrow.lineTo(baseX - dx, baseY - dy);
            arrow.closePath();
            return arrow;
        }
    }

    // Formes définies par une liste de points : Courbe, Forme libre, Dessin à main levée
    public static class FreeformShape {
        private final List<Point2D> points;
        private final boolean smooth; // true = Courbe (lissée), false = Forme libre / main levée

        public FreeformShape(List<Point2D> points, boolean smooth) {
            this.points = points;
            this.smooth = smooth;
        }

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
            return new ShapeGeometry(path, null); // jamais de remplissage pour ces formes
        }
    }

    // --- Instance : une forme concrète à dessiner ---

    private final ShapeType type;
    private final int x, y, width, height;
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private float strokeWidth = 1f;

    public Shapes(ShapeType type, int x, int y, int width, int height) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setFillColor(Color fillColor) { this.fillColor = fillColor; }
    public void setStrokeColor(Color strokeColor) { this.strokeColor = strokeColor; }
    public void setStrokeWidth(float strokeWidth) { this.strokeWidth = strokeWidth; }

    public void paint(Graphics2D g) {
        ShapeGeometry geometry = type.build(x, y, width, height);
        if (geometry.fill() != null) {
            g.setColor(fillColor);
            g.fill(geometry.fill());
        }
        g.setColor(strokeColor);
        g.setStroke(new BasicStroke(strokeWidth));
        g.draw(geometry.stroke());
    }
}