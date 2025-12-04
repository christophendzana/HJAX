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
public enum HLabelStyle {
    
     PRIMARY(new Color(13, 110, 253),      // Texte
            new Color(237, 244, 255)),    // Fond
    
    SECONDARY(new Color(108, 117, 125),
              new Color(233, 236, 239)),
    
    SUCCESS(new Color(25, 135, 84),
            new Color(212, 237, 218)),
    
    DANGER(new Color(220, 53, 69),
           new Color(248, 215, 218)),
    
    WARNING(new Color(255, 193, 7),
            new Color(255, 243, 205)),
    
    INFO(new Color(13, 202, 240),
         new Color(209, 236, 241)),
    
    LIGHT(new Color(248, 249, 250),
          new Color(252, 252, 253)),
    
    DARK(new Color(33, 37, 41),
         new Color(52, 58, 64)),
    
    WHITE(Color.BLACK,                    // Texte noir
          Color.WHITE),                   // Fond blanc
    
    BLACK(Color.WHITE,                    // Texte blanc
          Color.BLACK),   
    ;                   // Fond noir
    
    private final Color textColor;
    private final Color backgroundColor;
    
    private HLabelStyle(Color textColor, Color backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }
    
    public Color getTextColor() { return textColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    
}
