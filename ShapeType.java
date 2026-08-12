/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package illustrations.shapes;

import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import illustrations.shapes.geometry.*;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

/**
 *
 * @author FIDELE
 */
public enum ShapeType implements ShapeBuilder {

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
    },
    RECTANGLE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE));
        }
    },
    ROUNDED_RECTANGLE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND));
        }

        @Override
        public int adjustmentCount() {
            return 1;
        }

        @Override
        public double[] defaultAdjustments() {
            return new double[]{DEFAULT_CORNER_RATIO};
        }

        @Override
        public ShapeGeometry build(int x, int y, int w, int h, double[] adjustments) {
            return closedShape(rectanglePath(x, y, w, h,
                    ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, adjustments[0]));
        }

        @Override
        public Point2D adjustmentHandlePosition(int x, int y, int w, int h, double[] adjustments, int index) {
            double inset = Math.min(w, h) * adjustments[0];
            return new Point2D.Double(x + inset, y);
        }

        @Override
        public void applyAdjustmentDrag(int x, int y, int w, int h, double[] adjustments, int index, int localMx, int localMy) {
            double raw = (localMx - x) / (double) Math.min(w, h);
            adjustments[0] = clamp(raw, MIN_CORNER_RATIO, MAX_CORNER_RATIO);
        }
    },
    RECTANGLE_ONE_CORNER_CUT {
        // Construction de base, sans ajustement (comportement existant, inchangé)
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE));
        }
        // Une seule poignée jaune : la taille du coin coupé

        @Override
        public int adjustmentCount() {
            return 1;
        }

        @Override
        public double[] defaultAdjustments() {
            return new double[]{DEFAULT_CORNER_RATIO};
        }
        // Construction tenant compte du ratio ajustable

        @Override
        public ShapeGeometry build(int x, int y, int w, int h, double[] adjustments) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, adjustments[0]));
        }
        // Position de la poignée : sur le trait diagonal du coin coupé, à mi-chemin

        @Override
        public Point2D adjustmentHandlePosition(int x, int y, int w, int h, double[] adjustments, int index) {
            double inset = Math.min(w, h) * adjustments[0];
            return new Point2D.Double(x + inset / 2.0, y + inset / 2.0);
        }
        // Glissement : recalcule le ratio à partir de la distance au coin d'origine

        @Override
        public void applyAdjustmentDrag(int x, int y, int w, int h, double[] adjustments, int index, int localMx, int localMy) {
            double raw = ((localMx - x) + (localMy - y)) / (double) Math.min(w, h);
            adjustments[0] = clamp(raw, MIN_CORNER_RATIO, MAX_CORNER_RATIO);
        }
    },
    RECTANGLE_OPPOSITE_CORNERS_CUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.NONE));
        }
    },
    RECTANGLE_ADJACENT_CORNERS_CUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE));
        }
    },
    RECTANGLE_ONE_CORNER_ROUND {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE));
        }
    },
    RECTANGLE_OPPOSITE_CORNERS_ROUND {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.NONE));
        }
    },
    RECTANGLE_ADJACENT_CORNERS_ROUND {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE));
        }
    },
    TRIANGLE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 3, -90));
        }
    },
    ROOF {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return TRIANGLE.build(x, y, w, h);
        }
    },
    PENTAGON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 5, -90));
        }
    },
    HEXAGON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 6, -90));
        }
    },
    HEPTAGON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 7, -90));
        }
    },
    OCTOGON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 8, -90));
        }
    },
    DECAGON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 10, -90));
        }
    },
    DODECAGON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 12, -90));
        }
    },
    DIAMOND {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 4, -90));
        }
    },
    RIGHT_TRIANGLE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y);
            path.lineTo(x, y + h);
            path.lineTo(x + w, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    PARALLELOGRAM {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double slant = w * 0.2;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x + slant, y);
            path.lineTo(x + w, y);
            path.lineTo(x + w - slant, y + h);
            path.lineTo(x, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    TRAPEZOID {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double inset = w * 0.2;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x + inset, y);
            path.lineTo(x + w - inset, y);
            path.lineTo(x + w, y + h);
            path.lineTo(x, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    PLATE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rectanglePath(x, y, w, h,
                    ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.CUT, ShapeType.CornerStyle.CUT));
        }
    },
    L_SHAPE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double vw = w * 0.4, hh = h * 0.4;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y);
            path.lineTo(x + vw, y);
            path.lineTo(x + vw, y + h - hh);
            path.lineTo(x + w, y + h - hh);
            path.lineTo(x + w, y + h);
            path.lineTo(x, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    T_SHAPE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double barH = h * 0.4, stemW = w * 0.2;
            double stemLeft = x + w * 0.4, stemRight = x + w * 0.6;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y);
            path.lineTo(x + w, y);
            path.lineTo(x + w, y + barH);
            path.lineTo(stemRight, y + barH);
            path.lineTo(stemRight, y + h);
            path.lineTo(stemLeft, y + h);
            path.lineTo(stemLeft, y + barH);
            path.lineTo(x, y + barH);
            path.closePath();
            return closedShape(path);
        }
    },
    OVAL {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(new Ellipse2D.Double(x, y, w, h));
        }
    },
    TEXT_BOX {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return RECTANGLE.build(x, y, w, h);
        }
    },
    RING {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(ringPath(x, y, w, h, RING_THICKNESS_RATIO));
        }
    },
    HALF_RING {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(halfRingPath(x, y, w, h, RING_THICKNESS_RATIO));
        }
    },
    ARC {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Arc2D arc = new Arc2D.Double(x, y, w, h, 200, 140, Arc2D.OPEN);
            return new ShapeGeometry(arc, null);
        }
    },
    NO_SYMBOL {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D ring = ringPath(x, y, w, h, RING_THICKNESS_RATIO);
            double barWidth = Math.min(w, h) * RING_THICKNESS_RATIO;
            Path2D.Double bar = diagonalBar(x, y, w, h, barWidth);
            Path2D full = new Path2D.Double(Path2D.WIND_NON_ZERO);
            full.append(ring, false);
            full.append(bar, false);
            return closedShape(full);
        }
    },
    CUBE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double depth = Math.min(w, h) * DEPTH_RATIO;
            Path2D.Double outline = new Path2D.Double();
            // face avant
            outline.moveTo(x, y + depth);
            outline.lineTo(x + w - depth, y + depth);
            outline.lineTo(x + w - depth, y + h);
            outline.lineTo(x, y + h);
            outline.closePath();
            // face du dessus
            outline.moveTo(x, y + depth);
            outline.lineTo(x + depth, y);
            outline.lineTo(x + w, y);
            outline.lineTo(x + w - depth, y + depth);
            outline.closePath();
            // face de côté
            outline.moveTo(x + w - depth, y + depth);
            outline.lineTo(x + w, y);
            outline.lineTo(x + w, y + h - depth);
            outline.lineTo(x + w - depth, y + h);
            outline.closePath();
            return closedShape(outline);
        }
    },
    CYLINDER {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double capHeight = h * DEPTH_RATIO;
            Path2D.Double outline = new Path2D.Double();
            outline.append(new Ellipse2D.Double(x, y, w, capHeight), false);
            outline.moveTo(x, y + capHeight / 2);
            outline.lineTo(x, y + h - capHeight / 2);
            outline.append(new Arc2D.Double(x, y + h - capHeight, w, capHeight, 180, 180, Arc2D.OPEN), true);
            outline.moveTo(x + w, y + capHeight / 2);
            outline.lineTo(x + w, y + h - capHeight / 2);
            return closedShape(outline);
        }
    },
    CLOUD {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Area area = unionOf(
                    new Ellipse2D.Double(x + w * 0.05, y + h * 0.35, w * 0.35, h * 0.55),
                    new Ellipse2D.Double(x + w * 0.25, y + h * 0.10, w * 0.45, h * 0.65),
                    new Ellipse2D.Double(x + w * 0.50, y + h * 0.30, w * 0.40, h * 0.55),
                    new Ellipse2D.Double(x + w * 0.15, y + h * 0.45, w * 0.70, h * 0.45)
            );
            return closedShape(area);
        }
    },
    SUN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double cx = x + w / 2.0, cy = y + h / 2.0;
            double coreRadius = Math.min(w, h) * SUN_CORE_RATIO;
            double rayLength = Math.min(w, h) / 2.0 - coreRadius;
            double rayBaseWidth = coreRadius * SUN_RAY_WIDTH_RATIO;

            Path2D.Double shape = new Path2D.Double();
            shape.append(new Ellipse2D.Double(cx - coreRadius, cy - coreRadius, coreRadius * 2, coreRadius * 2), false);
            for (int i = 0; i < SUN_RAY_COUNT; i++) {
                double angle = i * 2 * Math.PI / SUN_RAY_COUNT;
                shape.append(rayTriangle(cx, cy, angle, coreRadius, rayLength, rayBaseWidth), false);
            }
            return closedShape(shape);
        }
    },
    MOON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Area area = new Area(new Ellipse2D.Double(x, y, w, h));
            double offsetX = w * MOON_OFFSET_RATIO;
            area.subtract(new Area(new Ellipse2D.Double(x + offsetX, y, w, h)));
            return closedShape(area);
        }
    },
    SMILEY {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double eyeR = Math.min(w, h) * SMILEY_EYE_RATIO;
            double eyeY = y + h * 0.35;

            Area area = new Area(new Ellipse2D.Double(x, y, w, h));
            area.subtract(new Area(new Ellipse2D.Double(x + w * 0.30 - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2)));
            area.subtract(new Area(new Ellipse2D.Double(x + w * 0.70 - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2)));
            area.subtract(new Area(new Arc2D.Double(x + w * 0.25, y + h * 0.30, w * 0.50, h * 0.50, 200, 140, Arc2D.PIE)));
            return closedShape(area);
        }
    },
    HEART {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x + w / 2.0, y + h);
            path.curveTo(x - w * 0.1, y + h * 0.55, x + w * 0.05, y - h * 0.05, x + w / 2.0, y + h * 0.30);
            path.curveTo(x + w * 0.95, y - h * 0.05, x + w * 1.1, y + h * 0.55, x + w / 2.0, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    TEARDROP {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double r = Math.min(w, h) * 0.5;
            double cx = x + w / 2.0, cy = y + h - r;

            Path2D.Double path = new Path2D.Double();
            path.moveTo(cx, y);
            path.curveTo(cx + w * 0.45, y + h * 0.35, x + w, cy - r * 0.2, x + w, cy);
            path.append(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 0, -270, Arc2D.OPEN), true);
            path.curveTo(x, cy - r * 0.2, cx - w * 0.45, y + h * 0.35, cx, y);
            path.closePath();
            return closedShape(path);
        }
    },
    LIGHTNING {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x + w * 0.55, y);
            path.lineTo(x + w * 0.15, y + h * 0.55);
            path.lineTo(x + w * 0.40, y + h * 0.55);
            path.lineTo(x + w * 0.20, y + h);
            path.lineTo(x + w * 0.85, y + h * 0.40);
            path.lineTo(x + w * 0.55, y + h * 0.40);
            path.closePath();
            return closedShape(path);
        }
    },
    SUPPORT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return new ShapeGeometry(bracketPath(x, y, w, h, false), null);
        }
    },
    LEFT_BRACE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return new ShapeGeometry(bracePath(x, y, w, h, false), null);
        }
    },
    RIGHT_BRACE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return new ShapeGeometry(bracePath(x, y, w, h, true), null);
        }
    },
    DOUBLE_BRACE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double left = bracePath(x, y, w / 2, h, false);
            Path2D.Double right = bracePath(x + w / 2, y, w / 2, h, true);
            Path2D.Double combined = new Path2D.Double();
            combined.append(left, false);
            combined.append(right, false);
            return new ShapeGeometry(combined, null);
        }
    },
    RIGHT_ARROW {
        // Construction de base, sans ajustement (inchangé)
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(horizontalArrow(x, y, w, h, true, ARROW_HEAD_LENGTH_RATIO, ARROW_SHAFT_THICKNESS_RATIO));
        }

        @Override
        public int adjustmentCount() {
            return 2;
        } // 0 = longueur de la pointe, 1 = épaisseur de la hampe

        @Override
        public double[] defaultAdjustments() {
            return new double[]{ARROW_HEAD_LENGTH_RATIO, ARROW_SHAFT_THICKNESS_RATIO};
        }

        @Override
        public ShapeGeometry build(int x, int y, int w, int h, double[] adjustments) {
            return closedShape(horizontalArrow(x, y, w, h, true, adjustments[0], adjustments[1]));
        }
        // Deux poignées distinctes selon l'index demandé

        @Override
        public Point2D adjustmentHandlePosition(int x, int y, int w, int h, double[] adjustments, int index) {
            double headLength = w * adjustments[0];
            double shaftThickness = h * adjustments[1];
            double neckX = x + w - headLength;
            if (index == 0) {
                // Poignée de longueur de pointe : sur le bord supérieur de la pointe, au niveau du "cou"
                return new Point2D.Double(neckX, y);
            } else {
                // Poignée d'épaisseur de hampe : sur le bord supérieur de la hampe, à mi-longueur
                double shaftTop = y + (h - shaftThickness) / 2.0;
                return new Point2D.Double(x + (neckX - x) / 2.0, shaftTop);
            }
        }
        // Chaque index recalcule son propre ratio, indépendamment de l'autre

        @Override
        public void applyAdjustmentDrag(int x, int y, int w, int h, double[] adjustments, int index, int localMx, int localMy) {
            if (index == 0) {
                double raw = (x + w - localMx) / (double) w;
                adjustments[0] = clamp(raw, ARROW_HEAD_LENGTH_MIN, ARROW_HEAD_LENGTH_MAX);
            } else {
                double raw = 1 - 2 * (localMy - y) / (double) h;
                adjustments[1] = clamp(raw, ARROW_SHAFT_THICKNESS_MIN, ARROW_SHAFT_THICKNESS_MAX);
            }
        }
    },
    LEFT_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(horizontalArrow(x, y, w, h, false, ARROW_HEAD_LENGTH_RATIO, ARROW_SHAFT_THICKNESS_RATIO));
        }
    },
    UP_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(verticalArrow(x, y, w, h, false));
        }
    },
    DOWN_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(verticalArrow(x, y, w, h, true));
        }
    },
    LEFT_RIGHT_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(horizontalDoubleArrow(x, y, w, h));
        }
    },
    UP_DOWN_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(verticalDoubleArrow(x, y, w, h));
        }
    },
    QUAD_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Area area = new Area(horizontalDoubleArrow(x, y, w, h));
            area.add(new Area(verticalDoubleArrow(x, y, w, h)));
            return closedShape(area);
        }
    },
    U_TURN_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(curvedArrowPath(x, y, w, h, 180, 180));
        }
    },
    CURVED_UP_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(curvedArrowPath(x, y, w, h, 180, -90));
        }
    },
    CURVED_DOWN_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(curvedArrowPath(x, y, w, h, 180, 90));
        }
    },
    CHEVRON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(chevronPath(x, y, w, h, CHEVRON_TIP_RATIO, CHEVRON_NOTCH_RATIO));
        }
    },
    STACKED_CHEVRON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(stackedChevronPath(x, y, w, h));
        }
    },
    RIGHT_ANGLE_CHEVRON {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(rightAngleChevronPath(x, y, w, h));
        }
    },
    NOTCHED_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(notchedArrowPath(x, y, w, h));
        }
    },
    STRIPED_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(stripedArrowPath(x, y, w, h));
        }
    },
    BULGED_ARROW {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(bulgedArrowPath(x, y, w, h));
        }
    },
    PLUS_SIGN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double thickness = Math.min(w, h) * EQUATION_THICKNESS_RATIO;
            double cx = x + w / 2.0, cy = y + h / 2.0;

            Path2D.Double path = new Path2D.Double();
            path.append(thickLine(x, cy, x + w, cy, thickness), false);
            path.append(thickLine(cx, y, cx, y + h, thickness), false);
            return closedShape(path);
        }
    },
    MINUS_SIGN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double thickness = Math.min(w, h) * EQUATION_THICKNESS_RATIO;
            double cy = y + h / 2.0;
            return closedShape(thickLine(x, cy, x + w, cy, thickness));
        }
    },
    MULTIPLY_SIGN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double thickness = Math.min(w, h) * EQUATION_THICKNESS_RATIO;

            Path2D.Double path = new Path2D.Double();
            path.append(thickLine(x, y, x + w, y + h, thickness), false);
            path.append(thickLine(x + w, y, x, y + h, thickness), false);
            return closedShape(path);
        }
    },
    DIVIDE_SIGN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double thickness = Math.min(w, h) * EQUATION_THICKNESS_RATIO;
            double cy = y + h / 2.0;
            double dotRadius = Math.min(w, h) * EQUATION_DOT_RADIUS_RATIO;
            double dotGap = h * EQUATION_BAR_GAP_RATIO;

            Path2D.Double path = new Path2D.Double();
            path.append(thickLine(x, cy, x + w, cy, thickness), false);
            path.append(new Ellipse2D.Double(x + w / 2.0 - dotRadius, cy - dotGap - dotRadius, dotRadius * 2, dotRadius * 2), false);
            path.append(new Ellipse2D.Double(x + w / 2.0 - dotRadius, cy + dotGap - dotRadius, dotRadius * 2, dotRadius * 2), false);
            return closedShape(path);
        }
    },
    EQUAL_SIGN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(equalBars(x, y, w, h));
        }
    },
    NOT_EQUAL_SIGN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double thickness = Math.min(w, h) * EQUATION_THICKNESS_RATIO;

            Path2D.Double path = new Path2D.Double();
            path.append(equalBars(x, y, w, h), false);
            path.append(thickLine(x + w * 0.15, y + h, x + w * 0.85, y, thickness), false);
            return closedShape(path);
        }
    },
    PROCESS {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return RECTANGLE.build(x, y, w, h);
        }
    },
    DECISION {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return DIAMOND.build(x, y, w, h);
        }
    },
    DATA {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return PARALLELOGRAM.build(x, y, w, h);
        }
    },
    EXTRACT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return TRIANGLE.build(x, y, w, h);
        }
    },
    ON_PAGE_CONNECTOR {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return OVAL.build(x, y, w, h);
        }
    },
    PREPARATION {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(stretchedHexagonPath(x, y, w, h));
        }
    },
    MANUAL_INPUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y + h * MANUAL_INPUT_SLANT_RATIO);
            path.lineTo(x + w, y);
            path.lineTo(x + w, y + h);
            path.lineTo(x, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    MANUAL_OPERATION {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double inset = w * MANUAL_OPERATION_INSET_RATIO;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y);
            path.lineTo(x + w, y);
            path.lineTo(x + w - inset, y + h);
            path.lineTo(x + inset, y + h);
            path.closePath();
            return closedShape(path);
        }
    },
    MERGE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(regularPolygon(x, y, w, h, 3, 90));
        }
    },
    OFF_PAGE_CONNECTOR {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double flatBottom = h * OFF_PAGE_POINT_RATIO;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y);
            path.lineTo(x + w, y);
            path.lineTo(x + w, y + h - flatBottom);
            path.lineTo(x + w / 2.0, y + h);
            path.lineTo(x, y + h - flatBottom);
            path.closePath();
            return closedShape(path);
        }
    },
    INTERNAL_STORAGE {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double offsetX = w * INTERNAL_STORAGE_OFFSET_RATIO;
            double offsetY = h * INTERNAL_STORAGE_OFFSET_RATIO;
            Rectangle2D.Double body = new Rectangle2D.Double(x, y, w, h);

            Path2D.Double stroke = new Path2D.Double();
            stroke.append(body, false);
            stroke.moveTo(x + offsetX, y);
            stroke.lineTo(x + offsetX, y + h);
            stroke.moveTo(x, y + offsetY);
            stroke.lineTo(x + w, y + offsetY);

            return new ShapeGeometry(stroke, body);
        }
    },
    DIRECT_ACCESS {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double notch = w * DIRECT_ACCESS_NOTCH_RATIO;
            Rectangle2D.Double body = new Rectangle2D.Double(x, y, w, h);

            Path2D.Double stroke = new Path2D.Double();
            stroke.append(body, false);
            stroke.moveTo(x + notch, y);
            stroke.lineTo(x + notch, y + h);

            return new ShapeGeometry(stroke, body);
        }
    },
    TERMINATOR {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(stadiumPath(x, y, w, h));
        }
    },
    SINGLE_DOCUMENT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(documentPath(x, y, w, h));
        }
    },
    MULTIPLE_DOCUMENTS {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double offset = Math.min(w, h) * DOCUMENT_STACK_OFFSET_RATIO;
            Path2D combined = new Path2D.Double();
            combined.append(documentPath((int) (x + 2 * offset), (int) (y - 2 * offset), (int) (w - 2 * offset), (int) (h - 2 * offset)), false);
            combined.append(documentPath((int) (x + offset), (int) (y - offset), (int) (w - offset), (int) (h - offset)), false);
            combined.append(documentPath(x, y, w, h), false);
            return closedShape(combined);
        }
    },
    STAR_4 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 4, STAR_INNER_RADIUS_RATIO));
        }
    },
    STAR_5 {
        // Construction de base, sans ajustement (inchangé)
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 5, STAR_INNER_RADIUS_RATIO));
        }

        @Override
        public int adjustmentCount() {
            return 1;
        } // le creux entre les pointes

        @Override
        public double[] defaultAdjustments() {
            return new double[]{STAR_INNER_RADIUS_RATIO};
        }

        @Override
        public ShapeGeometry build(int x, int y, int w, int h, double[] adjustments) {
            return closedShape(starPolygon(x, y, w, h, 5, adjustments[0]));
        }
        // Poignée placée sur le premier sommet creux (entre les deux premières pointes)

        @Override
        public Point2D adjustmentHandlePosition(int x, int y, int w, int h, double[] adjustments, int index) {
            double cx = x + w / 2.0, cy = y + h / 2.0;
            double angle = -Math.PI / 2 + Math.PI / 5; // angle du 1er sommet creux, même formule que starPolygon
            double ratio = adjustments[0];
            return new Point2D.Double(cx + Math.cos(angle) * (w / 2.0) * ratio, cy + Math.sin(angle) * (h / 2.0) * ratio);
        }
        // Glissement : distance au centre normalisée par le rayon extérieur, peu importe la direction exacte du glissement

        @Override
        public void applyAdjustmentDrag(int x, int y, int w, int h, double[] adjustments, int index, int localMx, int localMy) {
            double cx = x + w / 2.0, cy = y + h / 2.0;
            double dx = (localMx - cx) / (w / 2.0);
            double dy = (localMy - cy) / (h / 2.0);
            double raw = Math.sqrt(dx * dx + dy * dy);
            adjustments[0] = clamp(raw, STAR_INNER_RADIUS_MIN, STAR_INNER_RADIUS_MAX);
        }
    },
    STAR_8 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 8, STAR_INNER_RADIUS_RATIO));
        }
    },
    STAR_10 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 10, STAR_INNER_RADIUS_RATIO));
        }
    },
    STAR_12 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 12, STAR_INNER_RADIUS_RATIO));
        }
    },
    STAR_16 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 16, STAR_INNER_RADIUS_RATIO));
        }
    },
    STAR_24 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 24, STAR_INNER_RADIUS_RATIO));
        }
    },
    STAR_32 {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(starPolygon(x, y, w, h, 32, STAR_INNER_RADIUS_RATIO));
        }
    },
    EXPLOSION {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(explosionPath(x, y, w, h));
        }
    },
    HORIZONTAL_SCROLL {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(scrollPath(x, y, w, h, true));
        }
    },
    VERTICAL_SCROLL {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(scrollPath(x, y, w, h, false));
        }
    },
    WAVY_BANNER_UP {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(wavyBannerPath(x, y, w, h, true));
        }
    },
    WAVY_BANNER_DOWN {
        public ShapeGeometry build(int x, int y, int w, int h) {
            return closedShape(wavyBannerPath(x, y, w, h, false));
        }
    },
    RECTANGULAR_CALLOUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D body = rectanglePath(x, y, w, h, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE, ShapeType.CornerStyle.NONE);
            Path2D combined = new Path2D.Double();
            combined.append(body, false);
            combined.append(calloutTail(x, y, w, h), false);
            return closedShape(combined);
        }
    },
    ROUNDED_RECTANGULAR_CALLOUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D body = rectanglePath(x, y, w, h, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND, ShapeType.CornerStyle.ROUND);
            Path2D combined = new Path2D.Double();
            combined.append(body, false);
            combined.append(calloutTail(x, y, w, h), false);
            return closedShape(combined);
        }
    },
    OVAL_CALLOUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double combined = new Path2D.Double();
            combined.append(new Ellipse2D.Double(x, y, w, h), false);
            combined.append(calloutTail(x, y, w, h), false);
            return closedShape(combined);
        }
    },
    CLOUD_CALLOUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            Path2D.Double combined = new Path2D.Double();
            combined.append(cloudPath(x, y, w, h), false);
            combined.append(thoughtTrail(x, y, w, h), false);
            return closedShape(combined);
        }
    },
    LINE_CALLOUT {
        public ShapeGeometry build(int x, int y, int w, int h) {
            double textLineY = y;
            double textLineLength = w * LINE_CALLOUT_TEXT_LENGTH_RATIO;

            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, textLineY);
            path.lineTo(x + textLineLength, textLineY);
            path.moveTo(x, textLineY);
            path.lineTo(x - w * LINE_CALLOUT_LEADER_RATIO, y + h);
            return new ShapeGeometry(path, null);
        }
    };

    public abstract ShapeGeometry build(int x, int y, int w, int h);

    //Paramètre du Lot 2 rectanglePath2D
    private static final double DEFAULT_CORNER_RATIO = 0.25;
    private static final double MIN_CORNER_RATIO = 0.02;
    private static final double MAX_CORNER_RATIO = 0.5;

    // Taille par défaut des pointes de flèche
    private static double ARROW_LENGTH = 12.0;
    private static double ARROW_WIDTH = 8.0;

    // --- Paramètres  du Lot 3b ( futures poignées jaunes) ---
    private static double RING_THICKNESS_RATIO = 0.3;
    private static double DEPTH_RATIO = 0.25;

    // --- Paramètres du Lot 3c ( futures poignées jaunes) ---
    private static double SUN_CORE_RATIO = 0.35;
    private static int SUN_RAY_COUNT = 8;
    private static double SUN_RAY_WIDTH_RATIO = 0.5;
    private static double MOON_OFFSET_RATIO = 0.35;
    private static double SMILEY_EYE_RATIO = 0.08;
    private static double BRACE_POINT_RATIO = 0.35;

    // --- Paramètre du Lot 4b ( futures poignées jaunes) ---
    private static double CURVED_ARROW_THICKNESS_RATIO = 0.35;

    // --- Paramètres nommés du Lot 4a (futures poignées jaunes) ---
    private static double ARROW_HEAD_LENGTH_RATIO = 0.35;     // proportion de la longueur occupée par la pointe
    private static double ARROW_SHAFT_THICKNESS_RATIO = 0.5;  // épaisseur de la hampe / épaisseur totale    

    // --- Paramètres du Lot 4c (candidats aux futures poignées jaunes) ---
    private static double CHEVRON_TIP_RATIO = 0.3;
    private static double CHEVRON_NOTCH_RATIO = 0.3;
    private static int STACKED_CHEVRON_COUNT = 3;
    private static double STACKED_CHEVRON_GAP_RATIO = 0.08;
    private static double RIGHT_ANGLE_BAND_RATIO = 0.3;
    private static double ARROW_NOTCH_DEPTH_RATIO = 0.25;
    private static int STRIPE_COUNT = 3;
    private static double STRIPE_THICKNESS_RATIO = 0.2;
    private static double BULGE_RATIO = 0.25;

    // --- Paramètres du Lot 5 ( futures poignées jaunes) ---
    private static double EQUATION_THICKNESS_RATIO = 0.18;
    private static double EQUATION_BAR_GAP_RATIO = 0.15;
    private static double EQUATION_DOT_RADIUS_RATIO = 0.06;

    // --- Paramètres du Lot 6 ( futures poignées jaunes) ---
    private static double PREPARATION_SLANT_RATIO = 0.15;
    private static double MANUAL_INPUT_SLANT_RATIO = 0.25;
    private static double MANUAL_OPERATION_INSET_RATIO = 0.15;
    private static double OFF_PAGE_POINT_RATIO = 0.3;
    private static double INTERNAL_STORAGE_OFFSET_RATIO = 0.15;
    private static double DIRECT_ACCESS_NOTCH_RATIO = 0.15;
    private static double DOCUMENT_WAVE_RATIO = 0.08;
    private static double DOCUMENT_STACK_OFFSET_RATIO = 0.08;

    // --- Paramètres du Lot 7 (futures poignées jaunes) ---
    private static double STAR_INNER_RADIUS_RATIO = 0.45;
    private static int EXPLOSION_POINT_COUNT = 14;
    private static double EXPLOSION_JITTER_RATIO = 0.35;
    private static long EXPLOSION_SEED = 42L;
    private static double SCROLL_ROLL_RATIO = 0.18;
    private static double BANNER_WAVE_COUNT = 4;
    private static double BANNER_WAVE_DEPTH_RATIO = 0.12;
    private static double BANNER_TAIL_NOTCH_RATIO = 0.12;

    // --- Paramètres du Lot 8 (futures poignées jaunes) ---
    private static double TAIL_POSITION_RATIO = 0.25;   // position horizontale de la base de la queue
    private static double TAIL_LENGTH_RATIO = 0.35;     // longueur de la queue, en proportion de h
    private static double TAIL_BASE_RATIO = 0.15;       // largeur de la base de la queue, en proportion de w
    private static int THOUGHT_TRAIL_COUNT = 3;
    private static double THOUGHT_TRAIL_SPACING_RATIO = 0.12;
    private static double LINE_CALLOUT_TEXT_LENGTH_RATIO = 0.6;
    private static double LINE_CALLOUT_LEADER_RATIO = 0.3;

    private static final double ARROW_HEAD_LENGTH_MIN = 0.1;
    private static final double ARROW_HEAD_LENGTH_MAX = 0.8;
    private static final double ARROW_SHAFT_THICKNESS_MIN = 0.1;
    private static final double ARROW_SHAFT_THICKNESS_MAX = 0.9;

    private static final double STAR_INNER_RADIUS_MIN = 0.1;
    private static final double STAR_INNER_RADIUS_MAX = 0.9;

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
        if (length == 0) {
            length = 1; // évite une division par zéro sur une courbe de longueur nulle
        }
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

    // --- Helpers du Lot 2 : gestion générique des coins de rectangle ---
    private enum CornerStyle {
        NONE, CUT, ROUND
    }

// Forme pleine : le contour et le remplissage sont la même géométrie
    private static ShapeGeometry closedShape(Shape shape) {
        return new ShapeGeometry(shape, shape);
    }

// Construit un rectangle dont chaque coin est droit, coupé (chanfrein) ou arrondi
    private static Path2D rectanglePath(int x, int y, int w, int h,
            CornerStyle topLeft, CornerStyle topRight, CornerStyle bottomRight, CornerStyle bottomLeft) {
        double inset = Math.min(w, h) * 0.25;
        double tl = cornerInset(topLeft, inset);
        double tr = cornerInset(topRight, inset);
        double br = cornerInset(bottomRight, inset);
        double bl = cornerInset(bottomLeft, inset);

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x + tl, y);
        path.lineTo(x + w - tr, y);
        addCorner(path, topRight, x + w, y, x + w, y + tr);
        path.lineTo(x + w, y + h - br);
        addCorner(path, bottomRight, x + w, y + h, x + w - br, y + h);
        path.lineTo(x + bl, y + h);
        addCorner(path, bottomLeft, x, y + h, x, y + h - bl);
        path.lineTo(x, y + tl);
        addCorner(path, topLeft, x, y, x + tl, y);
        path.closePath();
        return path;
    }

    private static Path2D rectanglePath(int x, int y, int w, int h,
            CornerStyle topLeft, CornerStyle topRight, CornerStyle bottomRight, CornerStyle bottomLeft,
            double insetRatio) {
        double inset = Math.min(w, h) * insetRatio;
        double tl = topLeft == CornerStyle.NONE ? 0 : inset;
        double tr = topRight == CornerStyle.NONE ? 0 : inset;
        double br = bottomRight == CornerStyle.NONE ? 0 : inset;
        double bl = bottomLeft == CornerStyle.NONE ? 0 : inset;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x + tl, y);
        path.lineTo(x + w - tr, y);
        addCorner(path, topRight, x + w, y, x + w, y + tr);
        path.lineTo(x + w, y + h - br);
        addCorner(path, bottomRight, x + w, y + h, x + w - br, y + h);
        path.lineTo(x + bl, y + h);
        addCorner(path, bottomLeft, x, y + h, x, y + h - bl);
        path.lineTo(x, y + tl);
        addCorner(path, topLeft, x, y, x + tl, y);
        path.closePath();
        return path;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double cornerInset(CornerStyle style, double inset) {
        return style == CornerStyle.NONE ? 0 : inset;
    }

// Ligne droite pour un coin droit ou coupé, courbe pour un coin arrondi
    private static void addCorner(Path2D path, CornerStyle style, double pivotX, double pivotY, double toX, double toY) {
        if (style == CornerStyle.ROUND) {
            path.quadTo(pivotX, pivotY, toX, toY);
        } else {
            path.lineTo(toX, toY);
        }
    }

    // --- Helper du Lot 3a : polygone régulier redimensionné pour remplir exactement (x,y,w,h) ---
    private static Path2D regularPolygon(int x, int y, int w, int h, int sides, double rotationDegrees) {
        double rotation = Math.toRadians(rotationDegrees);
        double[] xs = new double[sides];
        double[] ys = new double[sides];
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (int i = 0; i < sides; i++) {
            double angle = rotation + i * 2 * Math.PI / sides;
            xs[i] = Math.cos(angle);
            ys[i] = Math.sin(angle);
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }

        double scaleX = w / (maxX - minX);
        double scaleY = h / (maxY - minY);

        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < sides; i++) {
            double px = x + (xs[i] - minX) * scaleX;
            double py = y + (ys[i] - minY) * scaleY;
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        return path;
    }

    // --- Helpers du Lot 3b ---
    private static Path2D ringPath(int x, int y, int w, int h, double thicknessRatio) {
        double insetX = w * thicknessRatio / 2;
        double insetY = h * thicknessRatio / 2;
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.append(new Ellipse2D.Double(x, y, w, h), false);
        path.append(new Ellipse2D.Double(x + insetX, y + insetY, w - 2 * insetX, h - 2 * insetY), false);
        return path;
    }

    private static Path2D halfRingPath(int x, int y, int w, int h, double thicknessRatio) {
        double insetX = w * thicknessRatio / 2;
        double insetY = h * thicknessRatio / 2;
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.append(new Arc2D.Double(x, y, w, h * 2, 0, 180, Arc2D.PIE), false);
        path.append(new Arc2D.Double(x + insetX, y + insetY, w - 2 * insetX, (h - insetY) * 2, 0, 180, Arc2D.PIE), false);
        return path;
    }

    private static Path2D.Double diagonalBar(int x, int y, int w, int h, double barWidth) {
        double dx = barWidth / 2 * Math.sqrt(2);
        Path2D.Double bar = new Path2D.Double();
        bar.moveTo(x - dx, y + dx);
        bar.lineTo(x + dx, y - dx);
        bar.lineTo(x + w + dx, y + h - dx);
        bar.lineTo(x + w - dx, y + h + dx);
        bar.closePath();
        return bar;
    }

    // --- Helpers du Lot 3c ---
    private static Area unionOf(Ellipse2D... ellipses) {
        Area area = new Area();
        for (Ellipse2D e : ellipses) {
            area.add(new Area(e));
        }
        return area;
    }

    private static Path2D.Double rayTriangle(double cx, double cy, double angle,
            double innerRadius, double length, double baseWidth) {
        double tipX = cx + Math.cos(angle) * (innerRadius + length);
        double tipY = cy + Math.sin(angle) * (innerRadius + length);
        double baseX = cx + Math.cos(angle) * innerRadius;
        double baseY = cy + Math.sin(angle) * innerRadius;
        double perp = angle + Math.PI / 2;
        double dx = Math.cos(perp) * baseWidth / 2;
        double dy = Math.sin(perp) * baseWidth / 2;

        Path2D.Double triangle = new Path2D.Double();
        triangle.moveTo(tipX, tipY);
        triangle.lineTo(baseX + dx, baseY + dy);
        triangle.lineTo(baseX - dx, baseY - dy);
        triangle.closePath();
        return triangle;
    }

// Courbe lisse sans pointe (support / crochet)
    private static Path2D.Double bracketPath(int x, int y, int w, int h, boolean mirrored) {
        double left = mirrored ? x + w : x;
        double reach = mirrored ? -w : w;
        double midY = y + h / 2.0;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(left, y);
        path.curveTo(left + reach, y, left + reach, midY, left + reach, midY);
        path.curveTo(left + reach, midY, left + reach, y + h, left, y + h);
        return path;
    }

// Courbe avec une pointe médiane (accolade)
    private static Path2D.Double bracePath(int x, int y, int w, int h, boolean mirrored) {
        double left = mirrored ? x + w : x;
        double reach = mirrored ? -w : w;
        double midY = y + h / 2.0;
        double pointReach = reach * BRACE_POINT_RATIO;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(left, y);
        path.curveTo(left + reach * 0.6, y, left + reach * 0.6, midY - h * 0.05, left + pointReach, midY);
        path.curveTo(left + reach * 0.6, midY + h * 0.05, left + reach * 0.6, y + h, left, y + h);
        return path;
    }

    // --- Helpers du Lot 4a ---
// Flèche simple horizontale : pointRight = true → pointe à droite, false → pointe à gauche
    private static Path2D.Double horizontalArrow(int x, int y, int w, int h, boolean pointRight,
            double headLengthRatio, double shaftThicknessRatio) {
        double headLength = w * headLengthRatio;
        double shaftThickness = h * shaftThicknessRatio;
        double shaftTop = y + (h - shaftThickness) / 2.0;
        double shaftBottom = shaftTop + shaftThickness;
        double neckX = pointRight ? x + w - headLength : x + headLength;
        double tipX = pointRight ? x + w : x;
        double backX = pointRight ? x : x + w;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(backX, shaftTop);
        path.lineTo(neckX, shaftTop);
        path.lineTo(neckX, y);
        path.lineTo(tipX, y + h / 2.0);
        path.lineTo(neckX, y + h);
        path.lineTo(neckX, shaftBottom);
        path.lineTo(backX, shaftBottom);
        path.closePath();
        return path;
    }

// Flèche simple verticale : pointDown = true → pointe en bas, false → pointe en haut
    private static Path2D.Double verticalArrow(int x, int y, int w, int h, boolean pointDown) {
        double headLength = h * ARROW_HEAD_LENGTH_RATIO;
        double shaftThickness = w * ARROW_SHAFT_THICKNESS_RATIO;
        double shaftLeft = x + (w - shaftThickness) / 2.0;
        double shaftRight = shaftLeft + shaftThickness;
        double neckY = pointDown ? y + h - headLength : y + headLength;
        double tipY = pointDown ? y + h : y;
        double backY = pointDown ? y : y + h;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(shaftLeft, backY);
        path.lineTo(shaftLeft, neckY);
        path.lineTo(x, neckY);
        path.lineTo(x + w / 2.0, tipY);
        path.lineTo(x + w, neckY);
        path.lineTo(shaftRight, neckY);
        path.lineTo(shaftRight, backY);
        path.closePath();
        return path;
    }

// Flèche double horizontale : pointes aux deux extrémités
    private static Path2D.Double horizontalDoubleArrow(int x, int y, int w, int h) {
        double headLength = w * ARROW_HEAD_LENGTH_RATIO;
        double shaftThickness = h * ARROW_SHAFT_THICKNESS_RATIO;
        double shaftTop = y + (h - shaftThickness) / 2.0;
        double shaftBottom = shaftTop + shaftThickness;
        double leftNeckX = x + headLength;
        double rightNeckX = x + w - headLength;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y + h / 2.0);
        path.lineTo(leftNeckX, y);
        path.lineTo(leftNeckX, shaftTop);
        path.lineTo(rightNeckX, shaftTop);
        path.lineTo(rightNeckX, y);
        path.lineTo(x + w, y + h / 2.0);
        path.lineTo(rightNeckX, y + h);
        path.lineTo(rightNeckX, shaftBottom);
        path.lineTo(leftNeckX, shaftBottom);
        path.lineTo(leftNeckX, y + h);
        path.closePath();
        return path;
    }

// Flèche double verticale : pointes en haut et en bas
    private static Path2D.Double verticalDoubleArrow(int x, int y, int w, int h) {
        double headLength = h * ARROW_HEAD_LENGTH_RATIO;
        double shaftThickness = w * ARROW_SHAFT_THICKNESS_RATIO;
        double shaftLeft = x + (w - shaftThickness) / 2.0;
        double shaftRight = shaftLeft + shaftThickness;
        double topNeckY = y + headLength;
        double bottomNeckY = y + h - headLength;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x + w / 2.0, y);
        path.lineTo(shaftRight, topNeckY);
        path.lineTo(shaftRight, bottomNeckY);
        path.lineTo(x + w, bottomNeckY);
        path.lineTo(x + w / 2.0, y + h);
        path.lineTo(x, bottomNeckY);
        path.lineTo(shaftLeft, bottomNeckY);
        path.lineTo(shaftLeft, topNeckY);
        path.closePath();
        return path;
    }

    // --- Helpers du Lot 4b ---
// Bande arquée + pointe tangente à l'extrémité (startAngleDeg → départ, extentDeg → étendue et sens)
    private static Path2D.Double curvedArrowPath(int x, int y, int w, int h, double startAngleDeg, double extentDeg) {
        double radius = Math.min(w, h) / 2.0;
        double innerRadius = radius * (1 - CURVED_ARROW_THICKNESS_RATIO);
        double cx = x + w / 2.0, cy = y + h / 2.0;

        double startRad = Math.toRadians(startAngleDeg);
        double endRad = Math.toRadians(startAngleDeg + extentDeg);

        Point2D outerStart = circlePoint(cx, cy, radius, startRad);
        Point2D innerEnd = circlePoint(cx, cy, innerRadius, endRad);
        Point2D midEnd = circlePoint(cx, cy, (radius + innerRadius) / 2.0, endRad);

        Path2D.Double band = new Path2D.Double();
        band.moveTo(outerStart.getX(), outerStart.getY());
        band.append(new Arc2D.Double(cx - radius, cy - radius, radius * 2, radius * 2,
                -startAngleDeg, -extentDeg, Arc2D.OPEN), true);
        band.lineTo(innerEnd.getX(), innerEnd.getY());
        band.append(new Arc2D.Double(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2,
                -(startAngleDeg + extentDeg), extentDeg, Arc2D.OPEN), true);
        band.closePath();

        // Tangente à l'arc : perpendiculaire au rayon, orientée selon le sens de parcours
        double tangentSign = extentDeg >= 0 ? 1 : -1;
        double tangentAngle = endRad + tangentSign * Math.PI / 2;
        Path2D arrowHead = buildArrowHead(midEnd, tangentAngle, ARROW_LENGTH * 1.4, ARROW_WIDTH * 1.6);

        Path2D.Double combined = new Path2D.Double();
        combined.append(band, false);
        combined.append(arrowHead, false);
        return combined;
    }

    private static Point2D circlePoint(double cx, double cy, double radius, double angleRad) {
        return new Point2D.Double(cx + radius * Math.cos(angleRad), cy + radius * Math.sin(angleRad));
    }

    // --- Helpers du Lot 4c ---
// Bande à pointe + encoche : la forme de base du chevron (6 sommets)
    private static Path2D.Double chevronPath(int x, int y, int w, int h, double tipRatio, double notchRatio) {
        double tip = w * tipRatio;
        double notch = w * notchRatio;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x + w - tip, y);
        path.lineTo(x + w, y + h / 2.0);
        path.lineTo(x + w - tip, y + h);
        path.lineTo(x, y + h);
        path.lineTo(x + notch, y + h / 2.0);
        path.closePath();
        return path;
    }

// Plusieurs chevrons de même taille, espacés régulièrement
    private static Path2D stackedChevronPath(int x, int y, int w, int h) {
        double gap = w * STACKED_CHEVRON_GAP_RATIO;
        double chevronWidth = (w - gap * (STACKED_CHEVRON_COUNT - 1)) / STACKED_CHEVRON_COUNT;

        Path2D.Double combined = new Path2D.Double();
        for (int i = 0; i < STACKED_CHEVRON_COUNT; i++) {
            double cx = x + i * (chevronWidth + gap);
            combined.append(chevronPath((int) cx, y, (int) chevronWidth, h, CHEVRON_TIP_RATIO, CHEVRON_NOTCH_RATIO), false);
        }
        return combined;
    }

// Bande horizontale + bande verticale unies en L, pointe en bas, encoche à gauche
    private static Area rightAngleChevronPath(int x, int y, int w, int h) {
        double band = Math.min(w, h) * RIGHT_ANGLE_BAND_RATIO;

        Area area = new Area(new Rectangle2D.Double(x, y, w, band));
        area.add(new Area(new Rectangle2D.Double(x + w - band, y, band, h)));

        Point2D tip = new Point2D.Double(x + w - band / 2.0, y + h);
        area.add(new Area(buildArrowHead(tip, Math.PI / 2, band * 1.2, band * 1.6)));

        double notchDepth = band * CHEVRON_NOTCH_RATIO;
        Path2D.Double notch = new Path2D.Double();
        notch.moveTo(x, y);
        notch.lineTo(x + notchDepth, y + band / 2.0);
        notch.lineTo(x, y + band);
        notch.closePath();
        area.subtract(new Area(notch));

        return area;
    }

// Flèche pleine (Lot 4a) avec un triangle retranché à l'arrière
    private static Area notchedArrowPath(int x, int y, int w, int h) {
        Area area = new Area(horizontalArrow(x, y, w, h, true, ARROW_HEAD_LENGTH_RATIO, ARROW_SHAFT_THICKNESS_RATIO));
        double notchDepth = h * ARROW_NOTCH_DEPTH_RATIO;

        Path2D.Double notch = new Path2D.Double();
        notch.moveTo(x, y);
        notch.lineTo(x + notchDepth, y + h / 2.0);
        notch.lineTo(x, y + h);
        notch.closePath();
        area.subtract(new Area(notch));

        return area;
    }

// Flèche pleine (Lot 4a) avec des fentes verticales retranchées dans la hampe
    private static Area stripedArrowPath(int x, int y, int w, int h) {
        Area area = new Area(horizontalArrow(x, y, w, h, true, ARROW_HEAD_LENGTH_RATIO, ARROW_SHAFT_THICKNESS_RATIO));
        double headLength = w * ARROW_HEAD_LENGTH_RATIO;
        double shaftLength = w - headLength;
        double spacing = shaftLength / (STRIPE_COUNT + 1);
        double stripeThickness = spacing * STRIPE_THICKNESS_RATIO;

        for (int i = 1; i <= STRIPE_COUNT; i++) {
            double sx = x + spacing * i - stripeThickness / 2.0;
            area.subtract(new Area(new Rectangle2D.Double(sx, y, stripeThickness, h)));
        }
        return area;
    }

// Variante de flèche pleine dont les bords de la hampe sont des courbes plutôt que des droites
    private static Path2D.Double bulgedArrowPath(int x, int y, int w, int h) {
        double headLength = w * ARROW_HEAD_LENGTH_RATIO;
        double shaftThickness = h * ARROW_SHAFT_THICKNESS_RATIO;
        double shaftTop = y + (h - shaftThickness) / 2.0;
        double shaftBottom = shaftTop + shaftThickness;
        double neckX = x + w - headLength;
        double bulge = shaftThickness * BULGE_RATIO;
        double midX = x + (neckX - x) / 2.0;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, shaftTop);
        path.quadTo(midX, shaftTop - bulge, neckX, shaftTop);
        path.lineTo(neckX, y);
        path.lineTo(x + w, y + h / 2.0);
        path.lineTo(neckX, y + h);
        path.lineTo(neckX, shaftBottom);
        path.quadTo(midX, shaftBottom + bulge, x, shaftBottom);
        path.closePath();
        return path;
    }

    // Ligne épaissie en rectangle, entre deux points quelconques (généralise diagonalBar du Lot 3b)
    private static Path2D.Double thickLine(double x1, double y1, double x2, double y2, double thickness) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double perp = angle + Math.PI / 2;
        double dx = Math.cos(perp) * thickness / 2;
        double dy = Math.sin(perp) * thickness / 2;

        Path2D.Double bar = new Path2D.Double();
        bar.moveTo(x1 + dx, y1 + dy);
        bar.lineTo(x2 + dx, y2 + dy);
        bar.lineTo(x2 - dx, y2 - dy);
        bar.lineTo(x1 - dx, y1 - dy);
        bar.closePath();
        return bar;
    }

// Deux barres horizontales symétriques par rapport au centre (utilisé par EQUAL et NOT_EQUAL)
    private static Path2D.Double equalBars(int x, int y, int w, int h) {
        double thickness = Math.min(w, h) * EQUATION_THICKNESS_RATIO;
        double gap = h * EQUATION_BAR_GAP_RATIO;
        double cy = y + h / 2.0;

        Path2D.Double path = new Path2D.Double();
        path.append(thickLine(x, cy - gap, x + w, cy - gap, thickness), false);
        path.append(thickLine(x, cy + gap, x + w, cy + gap, thickness), false);
        return path;
    }

    // --- Helpers du Lot 6 ---
    private static Path2D.Double stretchedHexagonPath(int x, int y, int w, int h) {
        double slant = w * PREPARATION_SLANT_RATIO;
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x + slant, y);
        path.lineTo(x + w - slant, y);
        path.lineTo(x + w, y + h / 2.0);
        path.lineTo(x + w - slant, y + h);
        path.lineTo(x + slant, y + h);
        path.lineTo(x, y + h / 2.0);
        path.closePath();
        return path;
    }

// Pilule complète : deux demi-cercles reliés par des côtés droits
    private static Path2D stadiumPath(int x, int y, int w, int h) {
        double radius = h / 2.0;
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x + radius, y);
        path.lineTo(x + w - radius, y);
        path.append(new Arc2D.Double(x + w - h, y, h, h, 90, -180, Arc2D.OPEN), true);
        path.lineTo(x + radius, y + h);
        path.append(new Arc2D.Double(x, y, h, h, 270, -180, Arc2D.OPEN), true);
        path.closePath();
        return path;
    }

// Rectangle avec base ondulée (feuille de document)
    private static Path2D.Double documentPath(int x, int y, int w, int h) {
        double waveDepth = h * DOCUMENT_WAVE_RATIO;
        double baseline = y + h * 0.85;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, y);
        path.lineTo(x + w, y);
        path.lineTo(x + w, baseline);
        path.quadTo(x + w * 0.75, baseline + waveDepth, x + w * 0.5, baseline);
        path.quadTo(x + w * 0.25, baseline - waveDepth, x, baseline);
        path.closePath();
        return path;
    }

    // --- Helpers du Lot 7 ---
// Polygone en étoile : alterne rayon extérieur (pointe) et rayon intérieur (creux)
    private static Path2D.Double starPolygon(int x, int y, int w, int h, int points, double innerRatio) {
        double cx = x + w / 2.0, cy = y + h / 2.0;
        double outerRx = w / 2.0, outerRy = h / 2.0;
        int vertexCount = points * 2;

        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < vertexCount; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / points;
            double ratio = (i % 2 == 0) ? 1.0 : innerRatio;
            double px = cx + Math.cos(angle) * outerRx * ratio;
            double py = cy + Math.sin(angle) * outerRy * ratio;
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        return path;
    }

// Étoile aux pointes irrégulières, déterministe grâce à une graine fixe (même résultat à chaque appel)
    private static Path2D.Double explosionPath(int x, int y, int w, int h) {
        double cx = x + w / 2.0, cy = y + h / 2.0;
        double outerRx = w / 2.0, outerRy = h / 2.0;
        int vertexCount = EXPLOSION_POINT_COUNT * 2;
        java.util.Random random = new java.util.Random(EXPLOSION_SEED);

        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < vertexCount; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / EXPLOSION_POINT_COUNT;
            boolean isTip = i % 2 == 0;
            double baseRatio = isTip ? 1.0 : STAR_INNER_RADIUS_RATIO;
            double jitter = 1.0 + (random.nextDouble() - 0.5) * EXPLOSION_JITTER_RATIO;
            double ratio = baseRatio * jitter;
            double px = cx + Math.cos(angle) * outerRx * ratio;
            double py = cy + Math.sin(angle) * outerRy * ratio;
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        return path;
    }

// Bande rectangulaire dont les deux extrémités s'enroulent (horizontal ou vertical)
    private static Path2D.Double scrollPath(int x, int y, int w, int h, boolean horizontal) {
        double roll = (horizontal ? h : w) * SCROLL_ROLL_RATIO;

        Path2D.Double path = new Path2D.Double();
        if (horizontal) {
            path.moveTo(x + roll, y);
            path.lineTo(x + w - roll, y);
            path.curveTo(x + w, y, x + w, y + roll, x + w - roll, y + roll);
            path.lineTo(x + roll, y + roll);
            path.curveTo(x, y + roll, x, y + h - roll, x + roll, y + h - roll);
            path.lineTo(x + w - roll, y + h - roll);
            path.curveTo(x + w, y + h - roll, x + w, y + h, x + w - roll, y + h);
            path.lineTo(x + roll, y + h);
            path.curveTo(x, y + h, x, y + h - roll, x + roll, y + h - roll);
            path.lineTo(x + roll, y + roll);
            path.curveTo(x, y + roll, x, y, x + roll, y);
        } else {
            path.moveTo(x, y + roll);
            path.lineTo(x, y + h - roll);
            path.curveTo(x, y + h, x + roll, y + h, x + roll, y + h - roll);
            path.lineTo(x + roll, y + roll);
            path.curveTo(x + roll, y, x + w - roll, y, x + w - roll, y + roll);
            path.lineTo(x + w - roll, y + h - roll);
            path.curveTo(x + w - roll, y + h, x + w, y + h, x + w, y + h - roll);
            path.lineTo(x + w, y + roll);
            path.curveTo(x + w, y, x + w - roll, y, x + w - roll, y + roll);
            path.lineTo(x + w - roll, y + h - roll);
            path.curveTo(x + w - roll, y + h, x + roll, y + h, x + roll, y + h - roll);
        }
        path.closePath();
        return path;
    }

// Ruban à bord ondulé + pointes échancrées aux extrémités
    private static Path2D.Double wavyBannerPath(int x, int y, int w, int h, boolean waveOnTop) {
        double waveDepth = h * BANNER_WAVE_DEPTH_RATIO;
        double notch = w * BANNER_TAIL_NOTCH_RATIO / 2;
        double straightY = waveOnTop ? y + h : y;
        double waveY = waveOnTop ? y : y + h;
        double waveSign = waveOnTop ? 1 : -1;
        double segment = w / BANNER_WAVE_COUNT;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(x, straightY);
        path.lineTo(x + w, straightY);
        path.lineTo(x + w - notch, (straightY + waveY) / 2.0);
        path.lineTo(x + w, waveY);
        for (int i = (int) BANNER_WAVE_COUNT - 1; i >= 0; i--) {
            double segStart = x + segment * (i + 1);
            double segEnd = x + segment * i;
            double segMid = (segStart + segEnd) / 2.0;
            path.quadTo(segMid, waveY - waveSign * waveDepth, segEnd, waveY);
        }
        path.lineTo(x, waveY);
        path.lineTo(x + notch, (straightY + waveY) / 2.0);
        path.closePath();
        return path;
    }

    // --- Helpers du Lot 8 ---
// Triangle de queue, ancré sur le bord bas du rectangle englobant
    private static Path2D.Double calloutTail(int x, int y, int w, int h) {
        double baseCenterX = x + w * TAIL_POSITION_RATIO;
        double baseWidth = w * TAIL_BASE_RATIO;
        double baseY = y + h;
        double tipX = baseCenterX - w * TAIL_LENGTH_RATIO * 0.4;
        double tipY = baseY + h * TAIL_LENGTH_RATIO;

        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(baseCenterX - baseWidth / 2.0, baseY);
        tail.lineTo(tipX, tipY);
        tail.lineTo(baseCenterX + baseWidth / 2.0, baseY);
        tail.closePath();
        return tail;
    }

// Silhouette de nuage (même construction que CLOUD au Lot 3c)
    private static Path2D.Double cloudPath(int x, int y, int w, int h) {
        Area area = unionOf(
                new Ellipse2D.Double(x + w * 0.05, y + h * 0.35, w * 0.35, h * 0.55),
                new Ellipse2D.Double(x + w * 0.25, y + h * 0.10, w * 0.45, h * 0.65),
                new Ellipse2D.Double(x + w * 0.50, y + h * 0.30, w * 0.40, h * 0.55),
                new Ellipse2D.Double(x + w * 0.15, y + h * 0.45, w * 0.70, h * 0.45)
        );
        Path2D.Double path = new Path2D.Double();
        path.append(area, false);
        return path;
    }

// Traînée de petits cercles décroissants (bulle de pensée)
    private static Path2D.Double thoughtTrail(int x, int y, int w, int h) {
        double baseX = x + w * TAIL_POSITION_RATIO;
        double baseY = y + h;
        Path2D.Double trail = new Path2D.Double();

        for (int i = 1; i <= THOUGHT_TRAIL_COUNT; i++) {
            double radius = Math.min(w, h) * THOUGHT_TRAIL_SPACING_RATIO / i;
            double cx = baseX - w * TAIL_LENGTH_RATIO * 0.4 * i / THOUGHT_TRAIL_COUNT;
            double cy = baseY + h * TAIL_LENGTH_RATIO * i / THOUGHT_TRAIL_COUNT;
            trail.append(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2), false);
        }
        return trail;
    }

}
