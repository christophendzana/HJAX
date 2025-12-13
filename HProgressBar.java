/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicProgressBarUI;
import hcomponents.vues.HProgressBarStyle;
import javax.swing.*;

/**
 * ProgressBar personnalisée avec design moderne.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HProgressBar extends JProgressBar {
    
    private HProgressBarStyle progressStyle = HProgressBarStyle.PRIMARY;
    private int cornerRadius = 10;
    private boolean animationsEnabled = true;
    private boolean showPercentage = true;
    private boolean striped = false;
    private boolean animated = false;
    
    public HProgressBar() {
        super();
        updateUI();
    }
    
    public HProgressBar(int orient) {
        super(orient);
        updateUI();
    }
    
    public HProgressBar(int min, int max) {
        super(min, max);
        updateUI();
    }
    
    public HProgressBar(int orient, int min, int max) {
        super(orient, min, max);
        updateUI();
    }
    
    public HProgressBar(BoundedRangeModel newModel) {
        super(newModel);
        updateUI();
    }
    
    @Override
    public void updateUI() {
        setUI(new HBasicProgressBarUI());
        setOpaque(false);
        setBorderPainted(false);
    }
    
    public HProgressBarStyle getProgressStyle() {
        return progressStyle;
    }
    
    public void setProgressStyle(HProgressBarStyle style) {
        this.progressStyle = style;
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
    
    public boolean isShowPercentage() {
        return showPercentage;
    }
    
    public void setShowPercentage(boolean show) {
        this.showPercentage = show;
        repaint();
    }
    
    public boolean isStriped() {
        return striped;
    }
    
    public void setStriped(boolean striped) {
        this.striped = striped;
        repaint();
    }
    
    public boolean isAnimated() {
        return animated;
    }
    
    public void setAnimated(boolean animated) {
        this.animated = animated;
        repaint();
    }
    
    public static HProgressBar withStyle(HProgressBarStyle style) {
        HProgressBar bar = new HProgressBar();
        bar.setProgressStyle(style);
        return bar;
    }
    
    public static HProgressBar withStyle(int min, int max, HProgressBarStyle style) {
        HProgressBar bar = new HProgressBar(min, max);
        bar.setProgressStyle(style);
        return bar;
    }
}