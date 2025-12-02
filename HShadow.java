/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hcomponents.vues.shadow;

import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author FIDELE
 */
public interface HShadow {
    void paint (Graphics2D g2, JComponent c, int width, int height, int radius);   
}
