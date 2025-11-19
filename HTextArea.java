/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;

/**
 *
 * @author FIDELE
 */
public class HTextArea extends JTextArea {
    
      public HTextArea(int rows, int cols) {
        super(rows, cols);
        setOpaque(false);
        setFont(getFont().deriveFont(Font.PLAIN, 16f));
        setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Ombre portée plus foncée et décalée
        g2.setColor(new Color(120, 20, 10, 90)); // Rouge sombre, ombre bien visible
        g2.fill(new RoundRectangle2D.Double(6, 12, w - 12, h - 12, 26, 26));
               
        // Fond blanc arrondi
        g2.setColor(new Color (225, 241, 242));
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 26, 26));

        super.paintComponent(g2);

        // Bordure rouge claire arrondie
        g2.setColor(new Color(14, 146, 161));
        g2.setStroke(new BasicStroke(3));
        g2.draw(new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, 24, 24));

        g2.dispose();
    }
    
}
