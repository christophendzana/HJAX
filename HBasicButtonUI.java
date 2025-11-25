/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HButton;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author FIDELE
 */
public class HBasicButtonUI extends BasicButtonUI {

    private int cornerRadius = 48;
    private Color baseColor = new Color(66, 165, 245);
    private Color hoverColor = new Color(33, 150, 243);
    private Color pressColor = new Color(30, 136, 229);
    private Color textColor = Color.BLACK;

    private Color currentColor;
    private Timer hoverTimer;

    public HBasicButtonUI() {
        currentColor = baseColor;
    }

    public static ComponentUI createUI(JComponent c) {
        return new HBasicButtonUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        int vAlign = b.getVerticalAlignment();
        ButtonModel model = b.getModel();

        Graphics2D g2 = (Graphics2D) g.create();

        int width = c.getWidth();
        int height = c.getHeight();

        // Lissage  
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Couleur selon l'état  
        Color currentColor = baseColor;
        if (model.isPressed()) {
            currentColor = pressColor;
        }

        // Fond  
        GradientPaint gradient = new GradientPaint(0, 0, currentColor.brighter(), 0, height, currentColor.darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        if (vAlign != SwingConstants.NORTH && vAlign != SwingConstants.EAST) {
            super.paint(g, c);
            return;
        }

        // Texte  
        FontMetrics fm = g2.getFontMetrics(); //Fournit les infos sur le texte  
        String text = b.getText();
        Icon icon = b.getIcon();

        //Calcul de la zonz de dessin: taille du composant - les marges;
        Rectangle viewRect = new Rectangle();
        viewRect.x = b.getInsets().left;
        viewRect.y = b.getInsets().top;
        viewRect.width = b.getWidth() - b.getInsets().left - b.getInsets().right;
        viewRect.height = b.getHeight() - b.getInsets().top - b.getInsets().bottom;
        
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        if (vAlign == SwingConstants.NORTH) {
            // Mesure  
            int iconH = (icon != null) ? icon.getIconHeight() : 0;
            int textH = fm.getHeight();

            // Icône centrée horizontalement  
            if (icon != null) {
                iconRect.x = viewRect.x + (viewRect.width - icon.getIconWidth()) / 2;
                iconRect.y = viewRect.y;
                iconRect.width = icon.getIconWidth();
                iconRect.height = icon.getIconHeight();
            }

            // Texte centré horizontalement et en dessous  
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = viewRect.y + iconH;
            textRect.width = fm.stringWidth(text);
            textRect.height = textH;
        } else if (vAlign == SwingConstants.EAST) {
            int iconH = (icon != null) ? icon.getIconHeight() : 0;
            int textH = fm.getHeight();

            // Texte centré au-dessus  
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = viewRect.y;
            textRect.width = fm.stringWidth(text);
            textRect.height = textH;

            // Icône centrée en dessous  
            if (icon != null) {
                iconRect.x = viewRect.x + (viewRect.width - icon.getIconWidth()) / 2;
                iconRect.y = viewRect.y + textH;
                iconRect.width = icon.getIconWidth();
                iconRect.height = icon.getIconHeight();
            }
        }

        // Dessiner l'icône  
        if (icon != null) {
            icon.paintIcon(c, g, iconRect.x, iconRect.y);
        }

        g.setFont(b.getFont());
        g.setColor(b.getForeground());
        g.drawString(text, textRect.x, textRect.y + textRect.height - fm.getDescent());

        g2.dispose(); //Libérer la mémoire et les flux de données  
    }

  

}
