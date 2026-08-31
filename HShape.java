package IllustrationShape;

import IllustrationShape.border.ViewBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public abstract class HShape extends HView {

    protected int x, y, width, height;
    protected double rotationDegrees;
    protected double[] adjustments = new double[0];

    protected Color fillColor = Color.WHITE;
    protected Color strokeColor = Color.BLACK;
    protected float strokeWidth = 1f;

    private boolean selected = false;
    private ViewBorder border;

    public HShape(){        
    }
    
    protected HShape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Volontairement vide : chaque forme concrète décide entièrement comment se dessiner
    @Override
    public void Paint(Graphics g, int px, int py) {
    }

    @Override
    public void paintBackground(Graphics g, Rectangle rect) {
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

    // Détection par défaut : simple rectangle englobant — ne nécessite aucune connaissance de Path2D.
    // Une forme qui a besoin d'une détection précise (suivant son contour réel) surcharge cette méthode.
    public boolean containsPoint(int worldMx, int worldMy) {
        Point2D local = toLocal(worldMx, worldMy);
        return local.getX() >= x && local.getX() <= x + width
                && local.getY() >= y && local.getY() <= y + height;
    }

    // --- accesseurs géométrie, rotation, style, sélection, bordure, ajustements : inchangés ---
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

    public void setRotationDegrees(double r) {
        this.rotationDegrees = r;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    public Point2D toWorld(double localX, double localY) {
        double cx = getCenterX(), cy = getCenterY();
        double rad = Math.toRadians(rotationDegrees);
        double dx = localX - cx, dy = localY - cy;
        return new Point2D.Double(cx + dx * Math.cos(rad) - dy * Math.sin(rad), cy + dx * Math.sin(rad) + dy * Math.cos(rad));
    }

    public Point2D toLocal(double worldX, double worldY) {
        double cx = getCenterX(), cy = getCenterY();
        double rad = Math.toRadians(-rotationDegrees);
        double dx = worldX - cx, dy = worldY - cy;
        return new Point2D.Double(cx + dx * Math.cos(rad) - dy * Math.sin(rad), cy + dx * Math.sin(rad) + dy * Math.cos(rad));
    }

    public void setFillColor(Color c) {
        this.fillColor = c;
    }

    public void setStrokeColor(Color c) {
        this.strokeColor = c;
    }

    public void setStrokeWidth(float w) {
        this.strokeWidth = w;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean s) {
        this.selected = s;
    }

    public ViewBorder getBorder() {
        return border;
    }

    public void setBorder(ViewBorder b) {
        this.border = b;
    }

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
}
