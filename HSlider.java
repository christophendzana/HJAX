/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicSliderUI;
import hcomponents.vues.HSliderStyle;
import javax.swing.*;

/**
 * Slider personnalisé avec design moderne.
 * Étend JSlider pour offrir des animations fluides, curseur arrondi,
 * track coloré progressivement et styles prédéfinis.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JSlider
 * @see HSliderStyle
 */
public class HSlider extends JSlider {
    
    private HSliderStyle sliderStyle = HSliderStyle.PRIMARY;
    private boolean animationsEnabled = true;
    private boolean hoverEnabled = true;
    private int thumbSize = 20;
    private int trackHeight = 6;
    private boolean showValue = false;
    private String valuePrefix = "";
    private String valueSuffix = "";
    
    /**
     * Constructeur par défaut.
     */
    public HSlider() {
        super();
        updateUI();
    }
    
    /**
     * Constructeur avec orientation.
     */
    public HSlider(int orientation) {
        super(orientation);
        updateUI();
    }
    
    /**
     * Constructeur avec min et max.
     */
    public HSlider(int min, int max) {
        super(min, max);
        updateUI();
    }
    
    /**
     * Constructeur avec min, max et valeur initiale.
     */
    public HSlider(int min, int max, int value) {
        super(min, max, value);
        updateUI();
    }
    
    /**
     * Constructeur avec orientation, min, max et valeur.
     */
    public HSlider(int orientation, int min, int max, int value) {
        super(orientation, min, max, value);
        updateUI();
    }
    
    /**
     * Constructeur avec BoundedRangeModel.
     */
    public HSlider(BoundedRangeModel brm) {
        super(brm);
        updateUI();
    }
    
    @Override
    public void updateUI() {
        setUI(new HBasicSliderUI(this));
        setOpaque(false);
    }
    
    public HSliderStyle getSliderStyle() {
        return sliderStyle;
    }
    
    public void setSliderStyle(HSliderStyle style) {
        this.sliderStyle = style;
        setForeground(style.getLabelColor());
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
    
    public int getThumbSize() {
        return thumbSize;
    }
    
    public void setThumbSize(int size) {
        this.thumbSize = size;
        repaint();
    }
    
    public int getTrackHeight() {
        return trackHeight;
    }
    
    public void setTrackHeight(int height) {
        this.trackHeight = height;
        repaint();
    }
    
    public boolean isShowValue() {
        return showValue;
    }
    
    public void setShowValue(boolean show) {
        this.showValue = show;
        repaint();
    }
    
    public String getValuePrefix() {
        return valuePrefix;
    }
    
    public void setValuePrefix(String prefix) {
        this.valuePrefix = prefix;
        repaint();
    }
    
    public String getValueSuffix() {
        return valueSuffix;
    }
    
    public void setValueSuffix(String suffix) {
        this.valueSuffix = suffix;
        repaint();
    }
    
    /**
     * Méthode factory pour créer un HSlider avec style.
     */
    public static HSlider withStyle(HSliderStyle style) {
        HSlider slider = new HSlider();
        slider.setSliderStyle(style);
        return slider;
    }
    
    /**
     * Méthode factory avec style et plage.
     */
    public static HSlider withStyle(int min, int max, int value, HSliderStyle style) {
        HSlider slider = new HSlider(min, max, value);
        slider.setSliderStyle(style);
        return slider;
    }
    
    /**
     * Méthode factory avec style, plage et graduations.
     */
    public static HSlider withStyleAndTicks(int min, int max, int value, 
                                           int majorTick, int minorTick, HSliderStyle style) {
        HSlider slider = new HSlider(min, max, value);
        slider.setSliderStyle(style);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(majorTick);
        slider.setMinorTickSpacing(minorTick);
        return slider;
    }
}