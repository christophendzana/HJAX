/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HScrollBar;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Interface utilisateur moderne pour HScrollBar.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicScrollBarUI extends BasicScrollBarUI {
    
    private HScrollBar hScrollBar;
    private boolean isThumbHovered = false;
    private float hoverProgress = 0f;
    private Timer hoverTimer;
    
    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    @Override
    protected void installComponents() {
        
         super.installComponents();
        
        if (scrollbar instanceof HScrollBar) {
            hScrollBar = (HScrollBar) scrollbar;
        }
    }
    
    @Override
    protected void installListeners() {
        super.installListeners();
        
        scrollbar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e);
            }
        });
        
        scrollbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (isThumbHovered) {
                    isThumbHovered = false;
                    animateHover(false);
                }
            }
        });
    }
    
    private void handleMouseMoved(MouseEvent e) {
        if (hScrollBar == null || !hScrollBar.isHoverEnabled()) return;
        
        if (getThumbBounds() == null) {
        if (isThumbHovered) {
            isThumbHovered = false;
            animateHover(false);
        }
        return;
    }         
         
        boolean wasHovered = isThumbHovered;
        isThumbHovered = getThumbBounds().contains(e.getPoint());
        
        if (wasHovered != isThumbHovered) {
            animateHover(isThumbHovered);
        }
    }
    
    private void animateHover(boolean in) {
        if (hScrollBar == null || !hScrollBar.isAnimationsEnabled()) {
            hoverProgress = in ? 1f : 0f;
            scrollbar.repaint();
            return;
        }
        
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        
        float startProgress = hoverProgress;
        long startTime = System.currentTimeMillis();
        
        hoverTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                hoverProgress = in ? (startProgress + (1f - startProgress) * progress)
                                   : (startProgress - startProgress * progress);
                
                scrollbar.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        hoverTimer.start();
    }
    
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hScrollBar == null || hScrollBar.getScrollStyle() == null) {
            super.paintTrack(g, c, trackBounds);
            g2.dispose();
            return;
        }
        
        HScrollBarStyle style = hScrollBar.getScrollStyle();
        
        g2.setColor(style.getTrackColor());
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        
        g2.dispose();
    }
    
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hScrollBar == null || hScrollBar.getScrollStyle() == null) {
            super.paintThumb(g, c, thumbBounds);
            g2.dispose();
            return;
        }
        
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            g2.dispose();
            return;
        }
        
        HScrollBarStyle style = hScrollBar.getScrollStyle();
        int radius = hScrollBar.getThumbRadius();
        
        // Déterminer la couleur du thumb
        Color thumbColor;
        if (isDragging) {
            thumbColor = style.getThumbPressedColor();
        } else {
            Color normalColor = style.getThumbColor();
            Color hoverColor = style.getThumbHoverColor();
            thumbColor = interpolateColor(normalColor, hoverColor, hoverProgress);
        }
        
        // Ajouter des marges pour un thumb plus fin
        int margin = 2;
        int x = thumbBounds.x + margin;
        int y = thumbBounds.y + margin;
        int width = thumbBounds.width - margin * 2;
        int height = thumbBounds.height - margin * 2;
        
        // Dessiner le thumb
        g2.setColor(thumbColor);
        RoundRectangle2D thumb = new RoundRectangle2D.Float(
            x, y, width, height, radius, radius
        );
        g2.fill(thumb);
        
        g2.dispose();
    }
    
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createInvisibleButton();
    }
    
    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createInvisibleButton();
    }
    
    private JButton createInvisibleButton() {
         JButton button = new JButton() {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(0, 0);
        }
    };
    button.setFocusable(false);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    return button;
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }
    
    @Override
    protected void uninstallListeners() {
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        super.uninstallListeners();
    }
}