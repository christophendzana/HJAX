/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HSlider;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Interface utilisateur moderne pour HSlider avec animations fluides,
 * curseur arrondi et track progressif coloré.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicSliderUI extends BasicSliderUI {
    
    private HSlider hSlider;
    private boolean isThumbHovered = false;
    private boolean isThumbPressed = false;
    private float hoverProgress = 0f;
    private Timer hoverTimer;
    
    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    public HBasicSliderUI(JSlider slider) {
        super(slider);
        if (slider instanceof HSlider) {
            hSlider = (HSlider) slider;
        }
    }
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        c.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e, c);
            }
        });
        
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isThumbPressed = true;
                c.repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isThumbPressed = false;
                c.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (isThumbHovered) {
                    isThumbHovered = false;
                    animateHover(false, c);
                }
            }
        });
    }
    
    private void handleMouseMoved(MouseEvent e, JComponent c) {
        if (hSlider == null || !hSlider.isHoverEnabled()) return;
        
        if (thumbRect == null) {
        if (isThumbHovered) {
            isThumbHovered = false;
            animateHover(false, c);
        }
        return;
    }
        
        Rectangle thumbBounds = thumbRect;
        boolean wasHovered = isThumbHovered;
        isThumbHovered = thumbBounds.contains(e.getPoint());
        
        if (wasHovered != isThumbHovered) {
            animateHover(isThumbHovered, c);
        }
    }
    
    private void animateHover(boolean in, JComponent c) {
        if (hSlider == null || !hSlider.isAnimationsEnabled()) {
            hoverProgress = in ? 1f : 0f;
            c.repaint();
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
                
                c.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        hoverTimer.start();
    }
    
    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hSlider == null || hSlider.getSliderStyle() == null || thumbRect == null) {
            super.paintTrack(g);
            g2.dispose();
            return;
        }
        
        HSliderStyle style = hSlider.getSliderStyle();
        int trackHeight = hSlider.getTrackHeight();
        
        Rectangle trackBounds = trackRect;
        int x = trackBounds.x;
        int y = trackBounds.y + (trackBounds.height - trackHeight) / 2;
        int width = trackBounds.width;
        int height = trackHeight;
        
        // Dessiner le fond du track
        g2.setColor(style.getTrackBackground());
        RoundRectangle2D trackBg = new RoundRectangle2D.Float(
            x, y, width, height, height, height
        );
        g2.fill(trackBg);
        
        // Dessiner la partie remplie du track
        int fillWidth;
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            fillWidth = thumbRect.x + thumbRect.width / 2 - x;
        } else {
            fillWidth = width;
        }
        
        if (fillWidth > 0 && fillWidth <= width) {
        g2.setColor(style.getTrackFillColor());
        RoundRectangle2D trackFill = new RoundRectangle2D.Float(
            x, y, fillWidth, height, height, height
        );
        g2.fill(trackFill);
    }
        
        g2.dispose();
    }
    
    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hSlider == null || hSlider.getSliderStyle() == null || thumbRect == null) {
            super.paintThumb(g);
            g2.dispose();
            return;
        }
        
        HSliderStyle style = hSlider.getSliderStyle();
        int thumbSize = hSlider.getThumbSize();
        
        Rectangle r = thumbRect;
        int x = r.x + (r.width - thumbSize) / 2;
        int y = r.y + (r.height - thumbSize) / 2;
        
        // Déterminer la couleur du thumb
        Color thumbColor;
        if (isThumbPressed) {
            thumbColor = style.getThumbPressedColor();
        } else {
            Color normalColor = style.getThumbColor();
            Color hoverColor = style.getThumbHoverColor();
            thumbColor = interpolateColor(normalColor, hoverColor, hoverProgress);
        }
        
        // Ombre douce
        for (int i = 0; i < 3; i++) {
            float alpha = (3 - i) / 10f;
            g2.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
            g2.fillOval(x - i, y - i + 2, thumbSize + i * 2, thumbSize + i * 2);
        }
        
        // Dessiner le thumb
        g2.setColor(thumbColor);
        g2.fillOval(x, y, thumbSize, thumbSize);
        
        // Bordure du thumb
        g2.setColor(style.getThumbBorderColor());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y, thumbSize, thumbSize);
        
        // Afficher la valeur si activé
        if (hSlider.isShowValue()) {
            paintValue(g2, x + thumbSize / 2, y - 5);
        }
        
        g2.dispose();
    }
    
    private void paintValue(Graphics2D g2, int x, int y) {
        String valueText = hSlider.getValuePrefix() + 
                          slider.getValue() + 
                          hSlider.getValueSuffix();
        
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(valueText);
        
        // Fond arrondi pour la valeur
        int padding = 6;
        int bgWidth = textWidth + padding * 2;
        int bgHeight = fm.getHeight() + 2;
        int bgX = x - bgWidth / 2;
        int bgY = y - bgHeight;
        
        g2.setColor(hSlider.getSliderStyle().getThumbColor());
        g2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 8, 8);
        
        // Texte
        g2.setColor(Color.WHITE);
        g2.drawString(valueText, bgX + padding, bgY + fm.getAscent() + 1);
    }
    
    @Override
    public void paintTicks(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hSlider == null || hSlider.getSliderStyle() == null) {
            super.paintTicks(g);
            g2.dispose();
            return;
        }
        
        g2.setColor(hSlider.getSliderStyle().getTickColor());
        super.paintTicks(g2);
        
        g2.dispose();
    }
    
    @Override
    public void paintLabels(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (hSlider != null && hSlider.getSliderStyle() != null) {
            g2.setColor(hSlider.getSliderStyle().getLabelColor());
        }
        
        super.paintLabels(g2);
        
        g2.dispose();
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
    protected Dimension getThumbSize() {
        int size = (hSlider != null) ? hSlider.getThumbSize() + 4 : 24;
        return new Dimension(size, size);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }
}