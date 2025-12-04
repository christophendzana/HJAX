package hcomponents.vues;

import hcomponents.HLabel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicLabelUI;

/**
 * UI personnalisé pour HLabel avec support d'orientation.
 * 
 * @author FIDELE
 */
public class HBasicLabelUI extends BasicLabelUI {
    
    private HLabel hLabel;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.hLabel = (HLabel) c;
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        HLabelStyle style = hLabel.getLabelStyle();
        int width = c.getWidth();
        int height = c.getHeight();
        int padding = hLabel.getPadding();
        
        // Dessiner le fond arrondi si activé
        if (hLabel.hasRoundedBackground()) {
            int radius = hLabel.getCornerRadius();
            
            // Fond arrondi
            g2.setColor(style.getBackgroundColor());
            g2.fillRoundRect(0, 0, width, height, radius, radius);
            
            // Bordure subtile
            g2.setColor(style.getTextColor().darker());
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
            
            // Effet de profondeur
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(1, 1, width - 2, height / 2, radius, radius);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        // Sauvegarder la transformation originale
        AffineTransform originalTransform = g2.getTransform();
        
        // Appliquer l'orientation
        switch (hLabel.getOrientation()) {
            case HORIZONTAL:
                // Pas de transformation
                paintHorizontalText(g2, c, padding);
                break;
                
            case VERTICAL_UP:
            case VERTICAL:
                // Rotation de 90 degrés vers le haut
                g2.rotate(Math.toRadians(-90));
                g2.translate(-height, 0);
                paintVerticalText(g2, c, padding, false);
                break;
                
            case VERTICAL_DOWN:
                // Rotation de 90 degrés vers le bas
                g2.rotate(Math.toRadians(90));
                g2.translate(0, -width);
                paintVerticalText(g2, c, padding, true);
                break;
        }
        
        // Restaurer la transformation
        g2.setTransform(originalTransform);
        g2.dispose();
    }
    
    /**
     * Dessine le texte horizontalement.
     */
    private void paintHorizontalText(Graphics2D g2, JComponent c, int padding) {
        String text = hLabel.getText();
        if (text == null || text.isEmpty()) return;
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // Calculer la position centrée avec padding
        int x = (c.getWidth() - textWidth) / 2;
        int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        
        // Ajuster avec padding
        if (hLabel.hasRoundedBackground()) {
            x = Math.max(padding, x);
            y = Math.max(padding + fm.getAscent(), y);
        }
        
        // Dessiner l'ombre du texte (optionnel)
        if (hLabel.hasRoundedBackground()) {
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawString(text, x + 1, y + 1);
        }
        
        // Dessiner le texte principal
        g2.setColor(hLabel.getForeground());
        g2.drawString(text, x, y);
    }
    
    /**
     * Dessine le texte verticalement.
     */
    private void paintVerticalText(Graphics2D g2, JComponent c, int padding, boolean reversed) {
        String text = hLabel.getText();
        if (text == null || text.isEmpty()) return;
        
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // Pour le texte vertical, on inverse width/height
        int componentWidth = c.getHeight();  // Inversé
        int componentHeight = c.getWidth();  // Inversé
        
        // Position centrée verticale
        int x = (componentWidth - textWidth) / 2;
        int y = (componentHeight + fm.getAscent() - fm.getDescent()) / 2;
        
        // Ajuster avec padding
        if (hLabel.hasRoundedBackground()) {
            x = Math.max(padding, x);
            y = Math.max(padding + fm.getAscent(), y);
            
            // Pour le texte inversé, ajuster différemment
            if (reversed) {
                y = componentHeight - padding - fm.getDescent();
            }
        }
        
        // Dessiner l'ombre du texte (optionnel)
        if (hLabel.hasRoundedBackground()) {
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawString(text, x + 1, y + 1);
        }
        
        // Dessiner le texte principal
        g2.setColor(hLabel.getForeground());
        g2.drawString(text, x, y);
    }
    
    @Override
    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        // Texte désactivé avec transparence
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        super.paintDisabledText(l, g2, s, textX, textY);
        g2.dispose();
    }
}