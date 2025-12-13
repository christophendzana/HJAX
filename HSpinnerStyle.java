/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HSpinner.
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HSpinnerStyle {
    
    PRIMARY(
        new Color(255, 255, 255),      // background
        new Color(240, 245, 255),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(13, 110, 253),       // focusBorderColor
        new Color(13, 110, 253),       // buttonColor
        new Color(10, 88, 202),        // buttonHoverColor
        new Color(33, 37, 41),         // textColor
        Color.WHITE                    // buttonIconColor
    ),
    
    SECONDARY(
        new Color(255, 255, 255),
        new Color(240, 242, 245),
        new Color(206, 212, 218),
        new Color(108, 117, 125),
        new Color(108, 117, 125),
        new Color(92, 99, 106),
        new Color(33, 37, 41),
        Color.WHITE
    ),
    
    SUCCESS(
        new Color(255, 255, 255),
        new Color(240, 250, 245),
        new Color(206, 212, 218),
        new Color(25, 135, 84),
        new Color(25, 135, 84),
        new Color(20, 108, 67),
        new Color(33, 37, 41),
        Color.WHITE
    ),
    
    DANGER(
        new Color(255, 255, 255),
        new Color(255, 240, 242),
        new Color(206, 212, 218),
        new Color(220, 53, 69),
        new Color(220, 53, 69),
        new Color(176, 42, 55),
        new Color(33, 37, 41),
        Color.WHITE
    ),
    
    WARNING(
        new Color(255, 255, 255),
        new Color(255, 250, 235),
        new Color(206, 212, 218),
        new Color(255, 193, 7),
        new Color(255, 193, 7),
        new Color(193, 142, 0),
        new Color(33, 37, 41),
        Color.BLACK
    ),
    
    INFO(
        new Color(255, 255, 255),
        new Color(235, 250, 255),
        new Color(206, 212, 218),
        new Color(13, 202, 240),
        new Color(13, 202, 240),
        new Color(10, 162, 192),
        new Color(33, 37, 41),
        Color.WHITE
    ),
    
    LIGHT(
        new Color(248, 249, 250),
        new Color(255, 255, 255),
        new Color(206, 212, 218),
        new Color(173, 181, 189),
        new Color(173, 181, 189),
        new Color(148, 156, 164),
        new Color(33, 37, 41),
        Color.WHITE
    ),
    
    DARK(
        new Color(52, 58, 64),
        new Color(73, 80, 87),
        new Color(108, 117, 125),
        new Color(173, 181, 189),
        new Color(173, 181, 189),
        new Color(206, 212, 218),
        Color.WHITE,
        new Color(33, 37, 41)
    ),
    
    OCEAN(
        new Color(255, 255, 255),
        new Color(224, 242, 241),
        new Color(206, 212, 218),
        new Color(0, 150, 136),
        new Color(0, 150, 136),
        new Color(0, 121, 107),
        new Color(33, 37, 41),
        Color.WHITE
    ),
    
    PURPLE(
        new Color(255, 255, 255),
        new Color(243, 229, 245),
        new Color(206, 212, 218),
        new Color(156, 39, 176),
        new Color(156, 39, 176),
        new Color(123, 31, 162),
        new Color(33, 37, 41),
        Color.WHITE
    );
    
    private final Color background;
    private final Color focusBackground;
    private final Color borderColor;
    private final Color focusBorderColor;
    private final Color buttonColor;
    private final Color buttonHoverColor;
    private final Color textColor;
    private final Color buttonIconColor;
    
    private HSpinnerStyle(Color bg, Color focusBg, Color border, Color focusBorder,
                         Color button, Color buttonHover, Color text, Color buttonIcon) {
        this.background = bg;
        this.focusBackground = focusBg;
        this.borderColor = border;
        this.focusBorderColor = focusBorder;
        this.buttonColor = button;
        this.buttonHoverColor = buttonHover;
        this.textColor = text;
        this.buttonIconColor = buttonIcon;
    }
    
    public Color getBackground() { return background; }
    public Color getFocusBackground() { return focusBackground; }
    public Color getBorderColor() { return borderColor; }
    public Color getFocusBorderColor() { return focusBorderColor; }
    public Color getButtonColor() { return buttonColor; }
    public Color getButtonHoverColor() { return buttonHoverColor; }
    public Color getTextColor() { return textColor; }
    public Color getButtonIconColor() { return buttonIconColor; }
}