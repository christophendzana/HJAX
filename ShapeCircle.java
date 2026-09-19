/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package IllustrationShape.vues.catalogue;

import IllustrationShape.HShape;
import IllustrationShape.model.HTextContent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author FIDELE
 */
public class ShapeCircle extends HShape implements HTextContent {
    
    private String text;
    
    public ShapeCircle() {
    }

    public ShapeCircle(int x, int y, int width, int height){
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
    }
    
    @Override
    public void Paint(Graphics g, int px, int py){  
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform saved = g2d.getTransform();
        g2d.rotate(Math.toRadians(rotationDegrees), getCenterX(), getCenterY());
        g2d.setColor(Color.black);
        g2d.drawOval(getX(), getY(), getWidth(), getHeight());  
        g2d.setTransform(saved);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        if (text == null) {
            return;
        }        
        this.text = text;
    }
    
}
