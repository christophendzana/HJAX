/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues.shadow;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public abstract class HAbstractShadow implements HShadow{
    
    protected Color shadowColor;
    protected float opacity;
    protected int blurRadius;
    protected int offsetX;
    protected int offsetY;
    
    public HAbstractShadow(Color color, float opacity, int blurRadius, int offsetX, int offsetY) {
        this.shadowColor = color;
        this.opacity = opacity;
        this.blurRadius = blurRadius;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    @Override
    public void paint(Graphics2D g2, JComponent c, int width, int height, int radius) {
        Graphics2D g2d = (Graphics2D) g2.create();
        
        // Activer l'antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Créer l'ombre floue
        paintBlurredShadow(g2d, width, height, radius);
        
        g2d.dispose();
    }
    
    protected abstract void paintBlurredShadow(Graphics2D g2d, int width, int height, int radius);    
    
     protected BufferedImage createBlurredImage(int width, int height, int radius, Color color) {
        if (blurRadius <= 0) {
            return createSimpleShadow(width, height, radius, color);
        }
        return createGaussianBlurShadow(width, height, radius, color);
    }
    
    private BufferedImage createSimpleShadow(int width, int height, int radius, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(opacity * 255)));
        g2d.fillRoundRect(0, 0, width, height, radius, radius);
        
        g2d.dispose();
        return image;
    }
    
    private BufferedImage createGaussianBlurShadow(int width, int height, int radius, Color color) {
        // Taille de l'image avec marge pour le flou
        int margin = blurRadius * 2;
        BufferedImage source = new BufferedImage(
            width + margin, height + margin, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = source.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillRoundRect(blurRadius, blurRadius, width, height, radius, radius);
        g2d.dispose();
        
        // Appliquer un flou gaussien simple (approximation)
        return applyFastBlur(source, blurRadius);
    }
    
    private BufferedImage applyFastBlur(BufferedImage image, int radius) {
        // Implémentation simplifiée du flou pour la performance
        // Vous pouvez améliorer cette méthode plus tard
        BufferedImage blurred = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                
                if (alpha > 0) {
                    // Simuler un flou en réduisant l'opacité progressivement
                    int newAlpha = (int)(alpha * opacity);
                    int rgb = (newAlpha << 24) | (shadowColor.getRGB() & 0x00FFFFFF);
                    blurred.setRGB(x, y, rgb);
                }
            }
        }
        
        return blurred;
    }
    
}
