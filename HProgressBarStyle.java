/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HProgressBar.
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HProgressBarStyle {
    
    PRIMARY(
        new Color(240, 245, 255),      // backgroundColor - fond de la barre
        new Color(13, 110, 253),       // progressColor - couleur de progression
        new Color(10, 88, 202),        // progressEndColor - couleur fin (gradient)
        Color.WHITE,                   // textColor - couleur du texte
        new Color(206, 212, 218)       // borderColor - bordure
    ),
    
    SECONDARY(
        new Color(240, 242, 245),      // backgroundColor
        new Color(108, 117, 125),      // progressColor
        new Color(92, 99, 106),        // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    SUCCESS(
        new Color(240, 250, 245),      // backgroundColor
        new Color(25, 135, 84),        // progressColor
        new Color(20, 108, 67),        // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    DANGER(
        new Color(255, 240, 242),      // backgroundColor
        new Color(220, 53, 69),        // progressColor
        new Color(176, 42, 55),        // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    WARNING(
        new Color(255, 250, 235),      // backgroundColor
        new Color(255, 193, 7),        // progressColor
        new Color(193, 142, 0),        // progressEndColor
        Color.BLACK,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    INFO(
        new Color(235, 250, 255),      // backgroundColor
        new Color(13, 202, 240),       // progressColor
        new Color(10, 162, 192),       // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    LIGHT(
        new Color(248, 249, 250),      // backgroundColor
        new Color(173, 181, 189),      // progressColor
        new Color(148, 156, 164),      // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    DARK(
        new Color(73, 80, 87),         // backgroundColor
        new Color(173, 181, 189),      // progressColor
        new Color(206, 212, 218),      // progressEndColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125)       // borderColor
    ),
    
    OCEAN(
        new Color(224, 242, 241),      // backgroundColor
        new Color(0, 150, 136),        // progressColor
        new Color(0, 121, 107),        // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    ),
    
    PURPLE(
        new Color(243, 229, 245),      // backgroundColor
        new Color(156, 39, 176),       // progressColor
        new Color(123, 31, 162),       // progressEndColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // borderColor
    );
    
    private final Color backgroundColor;
    private final Color progressColor;
    private final Color progressEndColor;
    private final Color textColor;
    private final Color borderColor;
    
    private HProgressBarStyle(Color bg, Color progress, Color progressEnd, Color text, Color border) {
        this.backgroundColor = bg;
        this.progressColor = progress;
        this.progressEndColor = progressEnd;
        this.textColor = text;
        this.borderColor = border;
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getProgressColor() { return progressColor; }
    public Color getProgressEndColor() { return progressEndColor; }
    public Color getTextColor() { return textColor; }
    public Color getBorderColor() { return borderColor; }
}