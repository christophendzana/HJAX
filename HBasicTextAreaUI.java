package hcomponents.vues;

import hcomponents.HTextArea;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.JTextComponent;

/**
 * UI personnalisé pour HTextArea avec rendu moderne.
 * 
 * @author FIDELE
 */
public class HBasicTextAreaUI extends BasicTextAreaUI {
    
     private boolean hovered = false;
    private boolean focused = false;
    private HTextArea hTextArea;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.hTextArea = (HTextArea) c;
        
        // Écouteurs pour les effets visuels
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                c.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                c.repaint();
            }
        });
        
        c.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                c.repaint();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                c.repaint();
            }
        });
    }
    
    @Override
    protected void paintSafely(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        HTextAreaStyle style = hTextArea.getTextAreaStyle();
        int width = hTextArea.getWidth();
        int height = hTextArea.getHeight();
        int radius = hTextArea.getCornerRadius();
        
        // Ombre légère
        g2.setColor(style.getShadowColor());
        g2.fillRoundRect(2, 2, width - 4, height - 4, radius, radius);
        
        // Fond
        Color bgColor = style.getBackgroundColor();
        if (focused) {
            bgColor = bgColor.brighter();
        } else if (hovered) {
            bgColor = bgColor.brighter();
        }
        
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        
        // Bordure
        Color borderColor;
        if (focused) {
            borderColor = style.getBorderColor();
        } else if (hovered) {
            borderColor = style.getBorderColor().brighter();
        } else {
            borderColor = style.getBorderColor().darker();
        }
        
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(focused ? 2.0f : 1.5f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
        
        // Effet de surbrillance interne pour le focus
        if (focused) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2.setColor(style.getBorderColor());
            g2.fillRoundRect(2, 2, width - 4, height - 4, radius - 2, radius - 2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        g2.dispose();
        
        // Dessiner le texte (avec les marges automatiquement respectées)
        super.paintSafely(g);
    }
    
    @Override
    protected void paintBackground(Graphics g) {
        // Ne rien faire ici, le fond est déjà dessiné dans paintSafely
    }
    
  
    
}