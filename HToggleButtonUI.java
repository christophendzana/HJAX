/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HToggleButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author FIDELE
 */
public class HToggleButtonUI extends BasicButtonUI {
 
   private Timer hoverTimer;
    private float hoverProgress = 0f;
    private static final int ANIMATION_DURATION = 200;
    private boolean animatingToHover = false;
    private long animationStartTime = 0;
    private boolean selected = false;
    private boolean isHovering = false;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        HToggleButton button = (HToggleButton) c;
        
        // Timer d'animation (identique à HBasicButtonUI)
        hoverTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - animationStartTime;
                float progress = Math.min(1f, elapsed / (float)ANIMATION_DURATION);
                
                if (animatingToHover) hoverProgress = progress;
                else hoverProgress = 1f - progress;
                
                if (elapsed >= ANIMATION_DURATION) {
                    hoverTimer.stop();
                    hoverProgress = animatingToHover ? 1f : 0f;
                }
                c.repaint();
            }
        });
        
        // Listeners pour hover
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovering = true;
                startHoverAnimation(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                startHoverAnimation(false);
            }
        });
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        HToggleButton button = (HToggleButton) c;
        ButtonModel model = button.getModel();
        Graphics2D g2 = (Graphics2D) g.create();
        
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = button.getCornerRadius();
        
        // Lissage
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Couleurs selon le style et l'état
        HButtonStyle style = button.getButtonStyle();
        Color baseColor = style.getBaseColor();
        Color hoverColor = style.getHoverColor();
        Color pressColor = style.getPressColor();
        Color selectedColor = style.getPressColor().darker(); // Couleur spéciale pour selected
        
        // Déterminer la couleur actuelle
        Color currentColor;
        if (model.isSelected()) {
            currentColor = selectedColor;
        } else if (model.isPressed()) {
            currentColor = pressColor;
        } else {
            currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
        }
        
        // Ombre
        if (button.getShadow() != null) {
            button.getShadow().paint(g2, c, width, height, radius);
        }
        
        // Fond avec dégradé
        GradientPaint gradient = new GradientPaint(
            0, 0, currentColor.brighter(),
            0, height, currentColor.darker()
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width - 5, height - 5, radius, radius);
            
        // Bordure
        if (button.getHBorder() != null) {
            button.getHBorder().paint(g2, button, width, height, radius);
        } else if (model.isSelected()) {
            // Bordure de sélection par défaut
            g2.setColor(style.getTextColor());
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, width - 3, height - 3, radius, radius);
        }
        
        // Dessiner l'icône de "checked" si sélectionné
        if (model.isSelected()) {
            int checkMargin = button.getCheckMarkMargin();           
            drawCheckmark(g2, width, height, style.getTextColor(), checkMargin);
        }
        
        // Appeler le paint parent pour le texte et l'icône
        super.paint(g2, c);
        
        g2.dispose();
    }
    
    private void drawCheckmark(Graphics2D g2, int width, int height, Color color, int checkMargin) {
        
       g2.setColor(color);
    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    
    // Taille proportionnelle mais limitée
    int size = Math.min(16, Math.min(width, height) / 4);
    
    // Position : centré verticalement, avec marge à droite
    int x = width - size - checkMargin;
    int y = height / 2;
    
    // Checkmark 
    g2.drawLine(x, y, x + size/3, y + size/2);
    g2.drawLine(x + size/3, y + size/2, x + size, y - size/3);
    }  
    
    private void startHoverAnimation(boolean toHover) {
        animatingToHover = toHover;
        animationStartTime = System.currentTimeMillis();
        if (!hoverTimer.isRunning()) hoverTimer.start();
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }
    
}
