package illustrations.model;

import java.awt.geom.Point2D;

/**
 * État géométrique mutable d'un objet graphique : position, dimensions et rotation.
 * Indépendant de tout objet dessinable — réutilisable par n'importe quel GraphicObject futur.
 */
public class GraphicTransform {

    private int x, y, width, height;
    private double rotationDegrees;

    // Construit un transform à une position et une taille données, sans rotation initiale
    public GraphicTransform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotationDegrees = 0;
    }

    // --- Accesseurs simples ---
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public double getRotationDegrees() { return rotationDegrees; }
    public void setRotationDegrees(double rotationDegrees) { this.rotationDegrees = rotationDegrees; }

    // Coordonnée du centre, utilisée comme pivot pour la rotation
    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }

    // Convertit un point du référentiel local (non pivoté) vers l'écran (pivoté)
    public Point2D toWorld(double localX, double localY) {
        double cx = getCenterX(), cy = getCenterY();
        double rad = Math.toRadians(rotationDegrees);
        double dx = localX - cx, dy = localY - cy;
        return new Point2D.Double(
            cx + dx * Math.cos(rad) - dy * Math.sin(rad),
            cy + dx * Math.sin(rad) + dy * Math.cos(rad));
    }

    // Convertit un point de l'écran (pivoté) vers le référentiel local (non pivoté)
    public Point2D toLocal(double worldX, double worldY) {
        double cx = getCenterX(), cy = getCenterY();
        double rad = Math.toRadians(-rotationDegrees);
        double dx = worldX - cx, dy = worldY - cy;
        return new Point2D.Double(
            cx + dx * Math.cos(rad) - dy * Math.sin(rad),
            cy + dx * Math.sin(rad) + dy * Math.cos(rad));
    }
}