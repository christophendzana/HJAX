/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HTabbedPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Interface utilisateur moderne pour HTabbedPane avec animations fluides,
 * indicateurs colorés et transitions progressives.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicTabbedPaneUI extends BasicTabbedPaneUI {
    
    private HTabbedPane hTabbedPane;
    private Map<Integer, Float> hoverProgressMap = new HashMap<>();
    private Map<Integer, Timer> hoverTimerMap = new HashMap<>();
    private int hoveredTab = -1;
    private float indicatorPosition = 0f;
    private Timer indicatorTimer;
    
    private static final int ANIMATION_DURATION = 250;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (c instanceof HTabbedPane) {
            hTabbedPane = (HTabbedPane) c;
        }
        
        c.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e, c);
            }
        });
        
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredTab != -1) {
                    animateHover(hoveredTab, false, c);
                    hoveredTab = -1;
                }
            }
        });
        
        // Listener pour animation de l'indicateur
        c.addPropertyChangeListener("selectedIndex", evt -> {
            if (hTabbedPane != null && hTabbedPane.isAnimationsEnabled()) {
                animateIndicator((Integer) evt.getOldValue(), (Integer) evt.getNewValue(), c);
            }
        });
    }
    
    private void handleMouseMoved(MouseEvent e, JComponent c) {
        if (hTabbedPane == null || !hTabbedPane.isHoverEnabled()) return;
        
        int tabIndex = tabForCoordinate((JTabbedPane)c, e.getX(), e.getY());
        
        if (tabIndex != hoveredTab) {
            if (hoveredTab != -1) {
                animateHover(hoveredTab, false, c);
            }
            hoveredTab = tabIndex;
            if (hoveredTab != -1) {
                animateHover(hoveredTab, true, c);
            }
        }
    }
    
    private void animateHover(int tabIndex, boolean in, JComponent c) {
        if (tabIndex < 0 || tabIndex >= tabPane.getTabCount()) return;
        
        if (hTabbedPane == null || !hTabbedPane.isAnimationsEnabled()) {
            hoverProgressMap.put(tabIndex, in ? 1f : 0f);
            c.repaint();
            return;
        }
        
        Timer existing = hoverTimerMap.get(tabIndex);
        if (existing != null) existing.stop();
        
        float startProgress = hoverProgressMap.getOrDefault(tabIndex, in ? 0f : 1f);
        long startTime = System.currentTimeMillis();
        
        Timer timer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                float current = in ? (startProgress + (1f - startProgress) * progress)
                                   : (startProgress - startProgress * progress);
                
                hoverProgressMap.put(tabIndex, current);
                c.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    hoverTimerMap.remove(tabIndex);
                }
            }
        });
        
        hoverTimerMap.put(tabIndex, timer);
        timer.start();
    }
    
    private void animateIndicator(int oldIndex, int newIndex, JComponent c) {
        if (indicatorTimer != null) {
            indicatorTimer.stop();
        }
        
        float startPos = (oldIndex >= 0) ? oldIndex : newIndex;
        float endPos = newIndex;
        long startTime = System.currentTimeMillis();
        
        indicatorTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Easing out cubic
                float eased = 1f - (float) Math.pow(1f - progress, 3);
                indicatorPosition = startPos + (endPos - startPos) * eased;
                
                c.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    indicatorPosition = endPos;
                }
            }
        });
        
        indicatorTimer.start();
    }
    
    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                     int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hTabbedPane == null || hTabbedPane.getTabbedStyle() == null) {
            g2.dispose();
            return;
        }
        
        HTabbedPaneStyle style = hTabbedPane.getTabbedStyle();
        int radius = hTabbedPane.getCornerRadius();
        
        Color bgColor;
        if (isSelected) {
            bgColor = style.getSelectedTabBackground();
        } else {
            Float progress = hoverProgressMap.getOrDefault(tabIndex, 0f);
            Color normalBg = style.getTabBackground();
            Color hoverBg = style.getHoverTabBackground();
            bgColor = interpolateColor(normalBg, hoverBg, progress);
        }
        
        g2.setColor(bgColor);
        
        // Dessiner le fond avec coins arrondis en haut
        if (tabPlacement == TOP) {
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                x + 2, y + 2, w - 4, h + radius, radius, radius
            );
            g2.fill(roundRect);
        } else if (tabPlacement == BOTTOM) {
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                x + 2, y - radius, w - 4, h + radius, radius, radius
            );
            g2.fill(roundRect);
        } else {
            g2.fillRect(x, y, w, h);
        }
        
        g2.dispose();
    }
    
    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                  int x, int y, int w, int h, boolean isSelected) {
        // Pas de bordure visible pour un look moderne
    }
    
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hTabbedPane == null || hTabbedPane.getTabbedStyle() == null) {
            g2.dispose();
            return;
        }
        
        HTabbedPaneStyle style = hTabbedPane.getTabbedStyle();
        int width = tabPane.getWidth();
        int height = tabPane.getHeight();
        int radius = hTabbedPane.getCornerRadius();
        
        // Calculer la position du contenu
        Insets insets = tabPane.getInsets();
        Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
        
        int x = insets.left;
        int y = insets.top;
        int w = width - insets.left - insets.right;
        int h = height - insets.top - insets.bottom;
        
        // Ajuster selon le placement
        switch (tabPlacement) {
            case TOP:
                y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                h -= (y - insets.top);
                break;
            case BOTTOM:
                h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                break;
            case LEFT:
                x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                w -= (x - insets.left);
                break;
            case RIGHT:
                w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                break;
        }
        
        // Dessiner le fond du contenu avec bordure
        g2.setColor(style.getContentBackground());
        RoundRectangle2D contentRect = new RoundRectangle2D.Float(
            x, y, w, h, radius, radius
        );
        g2.fill(contentRect);
        
        // Bordure subtile
        g2.setColor(style.getBorderColor());
        g2.setStroke(new BasicStroke(1f));
        g2.draw(contentRect);
        
        g2.dispose();
    }
    
    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                            int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (hTabbedPane == null || hTabbedPane.getTabbedStyle() == null) {
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
            g2.dispose();
            return;
        }
        
        HTabbedPaneStyle style = hTabbedPane.getTabbedStyle();
        
        // Couleur du texte
        Color textColor = isSelected ? style.getSelectedTextColor() : style.getTextColor();
        g2.setColor(textColor);
        
        // Font en gras pour l'onglet sélectionné
        Font tabFont = isSelected ? font.deriveFont(Font.BOLD) : font;
        g2.setFont(tabFont);
        
        // Dessiner le texte
        FontMetrics fm = g2.getFontMetrics();
        int textX = textRect.x;
        int textY = textRect.y + fm.getAscent();
        
        g2.drawString(title, textX, textY);
        
        g2.dispose();
    }
    
    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                      int tabIndex, Rectangle iconRect, Rectangle textRect,
                                      boolean isSelected) {
        // Dessiner l'indicateur coloré sous l'onglet sélectionné
        if (hTabbedPane == null || !hTabbedPane.isShowIndicator()) return;
        if (hTabbedPane.getTabbedStyle() == null) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        HTabbedPaneStyle style = hTabbedPane.getTabbedStyle();
        int indicatorHeight = hTabbedPane.getIndicatorHeight();
        
        // Utiliser l'animation de position
        int selectedIndex = tabPane.getSelectedIndex();
        float animatedIndex = hTabbedPane.isAnimationsEnabled() ? indicatorPosition : selectedIndex;
        
        if (selectedIndex >= 0 && selectedIndex < rects.length) {
            Rectangle selectedRect = rects[selectedIndex];
            Rectangle rect;
            
            // Interpolation entre les positions
            if (animatedIndex != selectedIndex && Math.abs(animatedIndex - selectedIndex) < 0.01f) {
                rect = selectedRect;
            } else {
                int index1 = (int) Math.floor(animatedIndex);
                int index2 = (int) Math.ceil(animatedIndex);
                
                if (index1 >= 0 && index1 < rects.length && index2 >= 0 && index2 < rects.length) {
                    Rectangle rect1 = rects[index1];
                    Rectangle rect2 = rects[index2];
                    float progress = animatedIndex - index1;
                    
                    int x = (int) (rect1.x + (rect2.x - rect1.x) * progress);
                    int y = (int) (rect1.y + (rect2.y - rect1.y) * progress);
                    int w = (int) (rect1.width + (rect2.width - rect1.width) * progress);
                    int h = (int) (rect1.height + (rect2.height - rect1.height) * progress);
                    
                    rect = new Rectangle(x, y, w, h);
                } else {
                    rect = selectedRect;
                }
            }
            
            g2.setColor(style.getIndicatorColor());
            
            if (tabPlacement == TOP) {
                g2.fillRoundRect(rect.x + 5, rect.y + rect.height - indicatorHeight - 2,
                               rect.width - 10, indicatorHeight, indicatorHeight, indicatorHeight);
            } else if (tabPlacement == BOTTOM) {
                g2.fillRoundRect(rect.x + 5, rect.y + 2,
                               rect.width - 10, indicatorHeight, indicatorHeight, indicatorHeight);
            }
        }
        
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
    public void uninstallUI(JComponent c) {
        // Arrêter tous les timers
        for (Timer t : hoverTimerMap.values()) {
            if (t != null) t.stop();
        }
        if (indicatorTimer != null) {
            indicatorTimer.stop();
        }
        hoverTimerMap.clear();
        hoverProgressMap.clear();
        
        super.uninstallUI(c);
    }
    
    @Override
    protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(5, 5, 5, 5);
    }
    
    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10;
    }
    
    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
    }
}