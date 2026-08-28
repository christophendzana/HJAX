package IllustrationShape;

import IllustrationShape.border.ViewBorder;
import java.awt.*;
import java.awt.geom.*;

public abstract class HViewShape extends HView {

    private static final float HIT_TEST_TOLERANCE = 6f;

    // --- géométrie ---
    protected int x, y, width, height;
    protected double rotationDegrees;

    // --- style ---
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private float strokeWidth = 1f;

    // --- état d'édition ---
    private boolean selected = false;
    protected double[] adjustments = new double[0];
    private ViewBorder border;

    // Plus de "builder" en paramètre : chaque forme SE construit elle-même via buildGeometry()
    protected HViewShape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- contrat que chaque forme (ShapeType.XXX) doit fournir ---
    protected abstract ShapeGeometry buildGeometry();

    // --- accesseurs géométrie ---
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getRotationDegrees() {
        return rotationDegrees;
    }

    public void setRotationDegrees(double rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    // --- style ---
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    // --- sélection ---
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // --- bordure ---
    public ViewBorder getBorder() {
        return border;
    }

    public void setBorder(ViewBorder border) {
        this.border = border;
    }

    // --- ajustements (poignées jaunes) : valeurs par défaut, surchargeables ---
    public int adjustmentCount() {
        return adjustments.length;
    }

    public Point2D adjustmentHandlePosition(int index) {
        return null;
    }

    public void applyAdjustmentDrag(int index, int worldMx, int worldMy) {
    }

    public ResizeBorder.HandleType handleAt(int worldMx, int worldMy) {
        return ResizeBorder.HandleType.at(this, worldMx, worldMy);
    }

    // --- contrat HView ---
    @Override
    public void Paint(Graphics g, int px, int py) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform saved = g2d.getTransform();
        g2d.rotate(Math.toRadians(rotationDegrees), getCenterX(), getCenterY());

        ShapeGeometry geometry = buildGeometry();

        if (geometry.fill() != null) {
            g2d.setColor(fillColor);
            g2d.fill(geometry.fill());
        }
        g2d.setColor(strokeColor);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(geometry.stroke());

        g2d.setTransform(saved);
    }

    @Override
    public void paintBackground(Graphics g, Rectangle rect) {
        // réservé aux futures vues (texte...), une forme n'a pas d'arrière-plan propre
    }

    @Override
    public void paintBorder(Graphics g) {
        if (border == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform saved = g2d.getTransform();
        g2d.rotate(Math.toRadians(rotationDegrees), getCenterX(), getCenterY());
        border.paintBorder(g2d, x, y, width, height);
        g2d.setTransform(saved);
    }

    public boolean containsPoint(int worldMx, int worldMy) {
        Point2D local = toLocal(worldMx, worldMy);
        ShapeGeometry geometry = buildGeometry(); // <-- corrigé, même chose ici

        if (geometry.fill() != null && geometry.fill().contains(local)) {
            return true;
        }

        java.awt.Shape thickenedStroke = new BasicStroke(HIT_TEST_TOLERANCE).createStrokedShape(geometry.stroke());
        return thickenedStroke.contains(local);
    }

    public Point2D toWorld(double localX, double localY) {
        double cx = getCenterX(), cy = getCenterY();
        double rad = Math.toRadians(rotationDegrees);
        double dx = localX - cx, dy = localY - cy;
        return new Point2D.Double(
                cx + dx * Math.cos(rad) - dy * Math.sin(rad),
                cy + dx * Math.sin(rad) + dy * Math.cos(rad));
    }

    public Point2D toLocal(double worldX, double worldY) {
        double cx = getCenterX(), cy = getCenterY();
        double rad = Math.toRadians(-rotationDegrees);
        double dx = worldX - cx, dy = worldY - cy;
        return new Point2D.Double(
                cx + dx * Math.cos(rad) - dy * Math.sin(rad),
                cy + dx * Math.sin(rad) + dy * Math.cos(rad));
    }

}
