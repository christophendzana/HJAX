/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicScrollBarUI;
import hcomponents.vues.HScrollBarStyle;
import javax.swing.*;
import java.awt.*;

/**
 * ScrollBar personnalisée avec design moderne.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HScrollBar extends JScrollBar {
    
    private HScrollBarStyle scrollStyle = HScrollBarStyle.PRIMARY;
    private int thumbRadius = 8;
    private boolean animationsEnabled = true;
    private boolean hoverEnabled = true;
    private int thumbWidth = 10;
    
    public HScrollBar() {
        super();
        updateUI();
    }
    
    public HScrollBar(int orientation) {
        super(orientation);
        updateUI();
    }
    
    public HScrollBar(int orientation, int value, int extent, int min, int max) {
        super(orientation, value, extent, min, max);
        updateUI();
    }
    
    @Override
    public void updateUI() {
        setUI(new HBasicScrollBarUI());
        setOpaque(false);
        setUnitIncrement(16);
        setBlockIncrement(50);
    }
    
    public HScrollBarStyle getScrollStyle() {
        return scrollStyle;
    }
    
    public void setScrollStyle(HScrollBarStyle style) {
        this.scrollStyle = style;
        repaint();
    }
    
    public int getThumbRadius() {
        return thumbRadius;
    }
    
    public void setThumbRadius(int radius) {
        this.thumbRadius = radius;
        repaint();
    }
    
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }
    
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
    
    public boolean isHoverEnabled() {
        return hoverEnabled;
    }
    
    public void setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
    }
    
    public int getThumbWidth() {
        return thumbWidth;
    }
    
    public void setThumbWidth(int width) {
        this.thumbWidth = width;
        revalidate();
        repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getOrientation() == VERTICAL) {
            d.width = thumbWidth + 4;
        } else {
            d.height = thumbWidth + 4;
        }
        return d;
    }
    
    public static HScrollBar withStyle(HScrollBarStyle style) {
        HScrollBar scrollBar = new HScrollBar();
        scrollBar.setScrollStyle(style);
        return scrollBar;
    }
    
    public static HScrollBar withStyle(int orientation, HScrollBarStyle style) {
        HScrollBar scrollBar = new HScrollBar(orientation);
        scrollBar.setScrollStyle(style);
        return scrollBar;
    }
}