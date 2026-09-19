/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package IllustrationShape.vues.catalogue;

import IllustrationShape.HShape;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *Ligne droite avec une pointe de flèche à l'extrémité finale.
 * 
 * @author FIDELE
 */

public class ShapeLineArrow extends HShape {
    private Shape stroke, fill;

    public ShapeLineArrow(int x, int y, int width, int height) { super(x, y, width, height); }

    private void computeGeometry() {
        Shape[] result = GeometryHelpers.lineWithArrows(x, y, width, height, false, true);
        stroke = result[0]; fill = result[1];
    }

    @Override
    public void Paint(Graphics g, int px, int py) {
        computeGeometry();
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform saved = g2d.getTransform();
        g2d.rotate(Math.toRadians(rotationDegrees), getCenterX(), getCenterY());
        if (fill != null) { g2d.setColor(fillColor); g2d.fill(fill); }
        g2d.setColor(strokeColor);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(stroke);
        g2d.setTransform(saved);
    }

    @Override
    public boolean containsPoint(int worldMx, int worldMy) {
        computeGeometry();
        Point2D local = toLocal(worldMx, worldMy);
        if (fill != null && fill.contains(local)) return true;
        Shape thickened = new BasicStroke(GeometryHelpers.HIT_TEST_TOLERANCE).createStrokedShape(stroke);
        return thickened.contains(local);
    }
}
