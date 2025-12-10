/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HMenuStyle;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Menu contextuel (popup) personnalisé avec design moderne.
 * Étend JPopupMenu pour offrir des bordures arrondies, ombres,
 * animations d'apparition et styles prédéfinis.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JPopupMenu
 * @see HMenuStyle
 */
public class HPopupMenu extends JPopupMenu {
    
    private HMenuStyle menuStyle = HMenuStyle.PRIMARY;
    private int cornerRadius = 12;
    private boolean animationsEnabled = true;
    private float opacity = 0f;
    private Timer fadeTimer;
    
    private static final int FADE_DURATION = 150;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    public HPopupMenu() {
        super();
        init();
    }
    
    public HPopupMenu(String label) {
        super(label);
        init();
    }
    
    private void init() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setLightWeightPopupEnabled(true);
        
        // Listener pour animation d'ouverture
        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (animationsEnabled) {
                    animateFadeIn();
                } else {
                    opacity = 1f;
                }
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                opacity = 0f;
            }
            
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                opacity = 0f;
            }
        });
    }
    
    private void animateFadeIn() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        
        opacity = 0f;
        long startTime = System.currentTimeMillis();
        
        fadeTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) FADE_DURATION);
                
                opacity = progress;
                repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    opacity = 1f;
                }
            }
        });
        
        fadeTimer.start();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Appliquer l'opacité pour l'animation
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        
        // Dessiner l'ombre
        paintShadow(g2, width, height);
        
        // Dessiner le fond arrondi
        if (menuStyle != null) {
            g2.setColor(menuStyle.getPopupBackground());
        } else {
            g2.setColor(Color.WHITE);
        }
        
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            3, 3, width - 6, height - 6, cornerRadius, cornerRadius
        );
        g2.fill(roundRect);
        
        // Bordure subtile
        g2.setColor(new Color(0, 0, 0, 20));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(roundRect);
        
        g2.dispose();
    }
    
    private void paintShadow(Graphics2D g2, int width, int height) {
        // Ombre douce en plusieurs couches
        int shadowSize = 10;
        for (int i = 0; i < shadowSize; i++) {
            float alpha = (shadowSize - i) / (float) shadowSize * 0.1f;
            g2.setColor(new Color(0, 0, 0, (int) (alpha * 255 * opacity)));
            
            RoundRectangle2D shadow = new RoundRectangle2D.Float(
                3 - i, 3 - i, 
                width - 6 + i * 2, height - 6 + i * 2,
                cornerRadius + i, cornerRadius + i
            );
            g2.draw(shadow);
        }
    }
    
    /**
     * Ajoute un HMenuItem au popup.
     * Configure automatiquement le style du item.
     */
    public void addHMenuItem(HMenuItem item) {
        item.setMenuStyle(this.menuStyle);
        add(item);
    }
    
    /**
     * Ajoute un sous-menu HMenu au popup.
     * Configure automatiquement le style du menu.
     */
    public void addHMenu(HMenu menu) {
        menu.setMenuStyle(this.menuStyle);
        add(menu);
    }
    
    /**
     * Ajoute un séparateur stylisé.
     */
    public void addHSeparator() {
        add(new HSeparator(menuStyle));
    }
    
    public HMenuStyle getMenuStyle() {
        return menuStyle;
    }
    
    public void setMenuStyle(HMenuStyle style) {
        this.menuStyle = style;
        if (style != null) {
            setBackground(style.getPopupBackground());
        }
        
        // Mettre à jour tous les items enfants
        for (Component comp : getComponents()) {
            if (comp instanceof HMenuItem) {
                ((HMenuItem) comp).setMenuStyle(style);
            } else if (comp instanceof HMenu) {
                ((HMenu) comp).setMenuStyle(style);
            } else if (comp instanceof HSeparator) {
                ((HSeparator) comp).setMenuStyle(style);
            }
        }
        
        repaint();
    }
    
    public int getCornerRadius() {
        return cornerRadius;
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
    
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }
    
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
    
    /**
     * Affiche le popup à la position de la souris.
     */
    public void showAtMouse(Component invoker, MouseEvent e) {
        show(invoker, e.getX(), e.getY());
    }
    
    /**
     * Affiche le popup centré sur le composant.
     */
    public void showCentered(Component invoker) {
        Dimension popupSize = getPreferredSize();
        int x = (invoker.getWidth() - popupSize.width) / 2;
        int y = (invoker.getHeight() - popupSize.height) / 2;
        show(invoker, x, y);
    }
    
    /**
     * Méthode factory pour créer un HPopupMenu avec style.
     */
    public static HPopupMenu withStyle(HMenuStyle style) {
        HPopupMenu popup = new HPopupMenu();
        popup.setMenuStyle(style);
        return popup;
    }
    
    /**
     * Méthode factory pour créer un HPopupMenu avec label et style.
     */
    public static HPopupMenu withStyle(String label, HMenuStyle style) {
        HPopupMenu popup = new HPopupMenu(label);
        popup.setMenuStyle(style);
        return popup;
    }
}