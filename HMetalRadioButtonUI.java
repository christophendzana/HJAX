/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HRadioButton;
import hcomponents.models.HRadioButtonModel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.plaf.metal.MetalRadioButtonUI;

/**
 *
 * @author FIDELE
 */
public class HMetalRadioButtonUI extends MetalRadioButtonUI {

    public static HMetalRadioButtonUI createUI(JComponent c) {
        return new HMetalRadioButtonUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        if (!(c instanceof HRadioButton)) {
            throw new IllegalArgumentException("bad Compononet");
        }

        HRadioButton button = (HRadioButton) c;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        try {

            
                        

        } finally {
            g2d.dispose();
        }

    }

}
