/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicButtonUI;
import hcomponents.vues.HBorder.HAbstractBorder;
import hcomponents.vues.HBorder.HBorder;
import hcomponents.vues.HBorder.HShadow;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *
 * @author FIDELE
 */
public class HButton extends JButton {

    private HBorder hBorder;
    private int cornerRadius =12 ; 
    private HShadow shadow;
    
    public HButton(String text) {
        super(text);
        updateUI();
    }

    /**
     * Creates a button with no set text or icon.
     */
    public HButton() {
        this(null, null);
    }

    /**
     * Creates a button with an icon.
     *
     * @param icon the Icon image to display on the button
     */
    public HButton(Icon icon) {
        this(null, icon);

    }

    /**
     * Creates a button where properties are taken from the <code>Action</code>
     * supplied.
     *
     * @param a the <code>Action</code> used to specify the new button
     *
     * @since 1.3
     */
    public HButton(Action a) {
        this();
        setAction(a);
    }

    /**
     * Creates a button with initial text and an icon.
     *
     * @param text the text of the button
     * @param icon the Icon image to display on the button
     */
    public HButton(String text, Icon icon) {
        // Create the model
        setModel(new DefaultButtonModel());

        // initialize
        init(text, icon);
        updateUI();
    }

    @Override
    public void updateUI() {
        setUI(new HBasicButtonUI());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
    }
    
    public void setVerticalAlignment(int alignment) {
        super.setVerticalAlignment(alignment);

        if (alignment == this.getVerticalAlignment()) {
            return;
        }
        int oldValue = this.getVerticalAlignment();
        super.setVerticalAlignment(checkVerticalKey(alignment, "verticalAlignment Error"));
    }
    
    @Override
    protected int checkVerticalKey(int key, String exception) {
        if ((key == TOP) || (key == CENTER) || (key == BOTTOM) || (key == EAST) || (key == NORTH )) {
            return key;
        } else {
            throw new IllegalArgumentException(exception);
        }
    }
    
    public HBorder getHBorder(){
        return hBorder;
    }    
    
    public int getCornerRadius(){
        return cornerRadius;        
    }  
    
    public void setCornerRadius(int radius){
        this.cornerRadius = radius;
        repaint();
    }
    
    public void setHBorder(HAbstractBorder border){
        this.hBorder = border;
    }
    
    public HShadow getShadow(){
        return shadow;
    }
    
    public void setShadow(HShadow shadow){
        this.shadow = shadow;
    }
    
}
