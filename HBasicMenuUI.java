/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HMenu;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuUI;

/**
 * Interface utilisateur moderne pour HMenu avec animations fluides.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicMenuUI extends BasicMenuUI {
    
    private HMenu hMenu;
    private float hoverProgress = 0f;
    private Timer hoverTimer;
    private boolean isHovering = false;
    
    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (c instanceof HMenu) {
            hMenu = (HMenu) c;
        }
        
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (hMenu != null && hMenu.isHoverEnabled()) {
                    isHovering = true;
                    animateHover(true, c);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (hMenu != null && hMenu.isHoverEnabled()) {
                    isHovering = false;
                    animateHover(false, c);
                }
            }
        });
    }
    
    private void animateHover(boolean in, JComponent c) {
        if (hMenu == null || !hMenu.isAnimationsEnabled()) {
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
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = menuItem.getWidth();
        int height = menuItem.getHeight();
        
        if (hMenu != null && hMenu.getMenuStyle() != null) {
            HMenuStyle style = hMenu.getMenuStyle();
            Color baseColor = style.getMenuBarColor();
            Color hoverColor = brighten(baseColor, 0.2f);
            
            Color currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
            
            g2.setColor(currentColor);
            g2.fillRect(0, 0, width, height);
        }
        
        g2.dispose();
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }
    
    private Color brighten(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }
}