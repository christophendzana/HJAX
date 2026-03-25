/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 *
 * @author FIDELE
 */
public enum HTextAreaStyle {
    
    PRIMARY(new Color(248, 249, 250),     // Fond
            new Color(13, 110, 253),      // Bordure
            Color.BLACK,                  // Texte
            new Color(13, 110, 253, 50)), // Ombre
    
    SECONDARY(new Color(248, 249, 250),
             new Color(108, 117, 125),
             Color.BLACK,
             new Color(108, 117, 125, 50)),
    
    SUCCESS(new Color(232, 247, 238),
            new Color(25, 135, 84),
            Color.BLACK,
            new Color(25, 135, 84, 50)),
    
    DANGER(new Color(248, 215, 218),
           new Color(220, 53, 69),
           Color.BLACK,
           new Color(220, 53, 69, 50)),
    
    LIGHT(new Color(255, 255, 255),
          new Color(222, 226, 230),
          Color.BLACK,
          new Color(0, 0, 0, 20)),
    
    DARK(new Color(52, 58, 64),
         new Color(33, 37, 41),
         Color.WHITE,
         new Color(0, 0, 0, 50));
    
    private final Color backgroundColor;
    private final Color borderColor;
    private final Color textColor;
    private final Color shadowColor;
    
    private HTextAreaStyle(Color bg, Color border, Color text, Color shadow) {
        this.backgroundColor = bg;
        this.borderColor = border;
        this.textColor = text;
        this.shadowColor = shadow;
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getBorderColor() { return borderColor; }
    public Color getTextColor() { return textColor; }
    public Color getShadowColor() { return shadowColor; }
    
}
