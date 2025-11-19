/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HPasswordField;
import hcomponents.models.HPasswordFieldModel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.text.JTextComponent;

/**
 *
 * @author FIDELE
 */
public class HPasswordFieldUI extends BasicPasswordFieldUI {
    
    private HPasswordFieldModel model;
    
    public HPasswordFieldUI(){
        super();        
    }
    
    @Override
    protected void paintSafely(Graphics g){
        super.paintSafely(g);
        
        JTextComponent c = getComponent();
        boolean visible = ( (HPasswordField)c).getHModel().isVisible();
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                
        int h = c.getHeight();
        int w = c.getWidth();
        
        int size = h -8; //taille du button = hauteur - marge
        int x = w - size - 4; // marge à droite
        int y = 4; // marge en haut
        
        eyeButton.setBounds(x, y, size, size);
        
        HPasswordField field = (HPasswordField)getComponent();
        HPasswordFieldModel model = field.getHModel();
        
        Color color = model.isHovered() ? Color.BLACK : Color.BLACK;
        
        
        Rectangle r = getEyeButton();
        
        int cx = r.x + r.width/2;
        int cy = r.y + r.height/2;
        
        g2.setColor(color);
        g2.drawOval(r.x +4, r.y+r.height/2-6, r.width-8, 12); // Oeil (Simple ovale
        
        g2.fillOval(cx-3, cy-3, 6, 6); //pupille
        
        if (!visible) {
            g2.drawLine(r.x+4, r.y+r.height-6, r.x+r.width-4, r.y+6);
        }
        
        g2.dispose();
    }
    
    private Rectangle eyeButton = new Rectangle();
    public Rectangle getEyeButton(){
        return eyeButton;
    }
    
}
