/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicSpinnerUI;
import hcomponents.vues.HSpinnerStyle;
import javax.swing.JSpinner.DefaultEditor;

import javax.swing.*;

/**
 * Spinner personnalisé avec design moderne.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSpinner extends JSpinner {

    private HSpinnerStyle spinnerStyle = HSpinnerStyle.PRIMARY;
    private int cornerRadius = 25;
    private boolean animationsEnabled = true;

    public HSpinner() {
        super();
        updateUI();
    }

    public HSpinner(SpinnerModel model) {
        super(model);
        updateUI();
    }

    @Override
    public void updateUI() {
        setUI(new HBasicSpinnerUI());
        setOpaque(false);
    }

    public HSpinnerStyle getSpinnerStyle() {
        return spinnerStyle;
    }

    public void setSpinnerStyle(HSpinnerStyle style) {
        this.spinnerStyle = style;
        JComponent editor = getEditor();
        if (editor instanceof DefaultEditor) {
            ((DefaultEditor) editor).getTextField().setForeground(style.getTextColor());
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

    public static HSpinner withStyle(HSpinnerStyle style) {
        HSpinner spinner = new HSpinner();
        spinner.setSpinnerStyle(style);
        return spinner;
    }

    public static HSpinner withStyle(SpinnerModel model, HSpinnerStyle style) {
        HSpinner spinner = new HSpinner(model);
        spinner.setSpinnerStyle(style);
        return spinner;
    }
}
