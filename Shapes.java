package illustrations.shapes;

import illustrations.model.Adjustable;
import illustrations.model.GraphicObject;
import illustrations.model.GraphicTransform;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Instance concrète d'une forme graphique éditable : position, taille,
 * rotation, couleurs et paramètres d'ajustement. Délègue toute la géométrie à
 * son ShapeBuilder (catalogue ShapeType ou forme personnalisée) et son
 * redimensionnement/rotation à HandleType.
 */
public class Shapes implements GraphicObject, Adjustable {

    // Tolérance de clic en pixels, pour sélectionner une forme même sans cliquer exactement sur le trait
    private static final float HIT_TEST_TOLERANCE = 6f;

    private final ShapeBuilder type;
    private final GraphicTransform transform;

    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private float strokeWidth = 1f;
    private boolean selected = false;
    private double[] adjustments;

    // Crée une forme d'un type donné (catalogue ou personnalisé), positionnée et dimensionnée
    public Shapes(ShapeBuilder type, int x, int y, int width, int height) {
        this.type = type;
        this.transform = new GraphicTransform(x, y, width, height);
        this.adjustments = type.defaultAdjustments();
    }

    // Donne accès à l'état géométrique, utilisé par HandleType et Canvas
    @Override
    public GraphicTransform getTransform() {
        return transform;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // Dessine la forme : applique la rotation, construit la géométrie via le ShapeBuilder, remplit puis trace
    @Override
    public void paint(Graphics2D g) {
        AffineTransform saved = g.getTransform();
        g.rotate(Math.toRadians(transform.getRotationDegrees()), transform.getCenterX(), transform.getCenterY());

        ShapeGeometry geometry = type.build(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), adjustments);
        if (geometry.fill() != null) {
            g.setColor(fillColor);
            g.fill(geometry.fill());
        }
        g.setColor(strokeColor);
        g.setStroke(new BasicStroke(strokeWidth));
        g.draw(geometry.stroke());

        g.setTransform(saved);
    }

    

    // Teste si un point écran touche la forme : zone remplie directement, ou tracé épaissi pour les formes ouvertes
    @Override
    public boolean containsPoint(int worldMx, int worldMy) {
        Point2D local = transform.toLocal(worldMx, worldMy);
        ShapeGeometry geometry = type.build(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), adjustments);

        if (geometry.fill() != null && geometry.fill().contains(local)) {
            return true;
        }

        Shape thickenedStroke = new BasicStroke(HIT_TEST_TOLERANCE).createStrokedShape(geometry.stroke());
        return thickenedStroke.contains(local);
    }   

    @Override
    public int adjustmentCount() {
        return type.adjustmentCount();
    }

    @Override
    public Point2D adjustmentHandlePosition(int index) {
        return type.adjustmentHandlePosition(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), adjustments, index);
    }

    // Recalcule le paramètre d'ajustement d'index donné, à partir d'un point de glissement écran
    @Override
    public void applyAdjustmentDrag(int index, int worldMx, int worldMy) {
        Point2D local = transform.toLocal(worldMx, worldMy);
        type.applyAdjustmentDrag(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(),
                adjustments, index, (int) local.getX(), (int) local.getY());
    }
}
