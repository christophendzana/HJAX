/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HScrollBarStyle;
import javax.swing.*;
import java.awt.*;

/**
 * ScrollPane personnalisé avec scrollbars modernes intégrées.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HScrollPane extends JScrollPane {
    
    private HScrollBarStyle scrollStyle = HScrollBarStyle.PRIMARY;
    private int cornerRadius = 8;
    private boolean showBorder = true;
    private Color borderColor = new Color(220, 220, 220);
    
    public HScrollPane() {
        super();
        init();
    }
    
    public HScrollPane(Component view) {
        super(view);
        init();
    }
    
    public HScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
        init();
    }
    
    public HScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        init();
    }
    
    private void init() {
        setOpaque(false);
        getViewport().setOpaque(false);
        
        // Remplacer les scrollbars par des HScrollBar
        setVerticalScrollBar(HScrollBar.withStyle(JScrollBar.VERTICAL, scrollStyle));
        setHorizontalScrollBar(HScrollBar.withStyle(JScrollBar.HORIZONTAL, scrollStyle));
        
        // Configuration moderne
        setBorder(createModernBorder());
        setViewportBorder(null);
        
        // Coins arrondis
        getViewport().setBackground(Color.WHITE);
    }
    
    private javax.swing.border.Border createModernBorder() {
        if (!showBorder) {
            return BorderFactory.createEmptyBorder();
        }
        
        return new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(x, y, width - 1, height - 1, cornerRadius, cornerRadius);
                
                g2.dispose();
            }
            
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }
        };
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fond arrondi
        g2.setColor(getViewport().getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    public HScrollBarStyle getScrollStyle() {
        return scrollStyle;
    }
    
    public void setScrollStyle(HScrollBarStyle style) {
        this.scrollStyle = style;
        
        // Mettre à jour les scrollbars
        if (getVerticalScrollBar() instanceof HScrollBar) {
            ((HScrollBar) getVerticalScrollBar()).setScrollStyle(style);
        }
        if (getHorizontalScrollBar() instanceof HScrollBar) {
            ((HScrollBar) getHorizontalScrollBar()).setScrollStyle(style);
        }
        
        repaint();
    }
    
    public int getCornerRadius() {
        return cornerRadius;
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        setBorder(createModernBorder());
        repaint();
    }
    
    public boolean isShowBorder() {
        return showBorder;
    }
    
    public void setShowBorder(boolean show) {
        this.showBorder = show;
        setBorder(createModernBorder());
        repaint();
    }
    
    public Color getBorderColor() {
        return borderColor;
    }
    
    public void setBorderColor(Color color) {
        this.borderColor = color;
        setBorder(createModernBorder());
        repaint();
    }
    
    /**
     * Configure le viewport pour avoir un fond blanc.
     */
    public void setViewportBackground(Color color) {
        getViewport().setBackground(color);
        repaint();
    }
    
    /**
     * Active/désactive les scrollbars.
     */
    public void setScrollBarsVisible(boolean vertical, boolean horizontal) {
        setVerticalScrollBarPolicy(vertical ? VERTICAL_SCROLLBAR_AS_NEEDED : VERTICAL_SCROLLBAR_NEVER);
        setHorizontalScrollBarPolicy(horizontal ? HORIZONTAL_SCROLLBAR_AS_NEEDED : HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    /**
     * Méthode factory avec style.
     */
    public static HScrollPane withStyle(Component view, HScrollBarStyle style) {
        HScrollPane scrollPane = new HScrollPane(view);
        scrollPane.setScrollStyle(style);
        return scrollPane;
    }
    
    /**
     * Méthode factory avec style et policies.
     */
    public static HScrollPane withStyle(Component view, int vsbPolicy, int hsbPolicy, HScrollBarStyle style) {
        HScrollPane scrollPane = new HScrollPane(view, vsbPolicy, hsbPolicy);
        scrollPane.setScrollStyle(style);
        return scrollPane;
    }
}