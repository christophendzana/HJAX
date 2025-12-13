/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HFormattedTextField.
 * Chaque style définit un ensemble cohérent de couleurs pour :
 * - Le fond normal et au focus
 * - Les bordures (normale, focus, erreur, succès)
 * - Les couleurs de texte et placeholder
 * - Les fonds d'état (erreur, succès)
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HFormattedTextFieldStyle {
    
    PRIMARY(
        new Color(255, 255, 255),      // background - fond normal
        new Color(240, 245, 255),      // focusBackground - fond au focus
        new Color(206, 212, 218),      // borderColor - bordure normale
        new Color(13, 110, 253),       // focusBorderColor - bordure au focus
        new Color(220, 53, 69),        // errorBorderColor - bordure en erreur
        new Color(25, 135, 84),        // successBorderColor - bordure valide
        new Color(33, 37, 41),         // textColor - couleur du texte
        new Color(108, 117, 125),      // placeholderColor - couleur du placeholder
        new Color(255, 240, 242),      // errorBackground - fond en erreur
        new Color(240, 250, 245)       // successBackground - fond valide
    ),
    
    SECONDARY(
        new Color(255, 255, 255),      // background
        new Color(240, 242, 245),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(108, 117, 125),      // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    SUCCESS(
        new Color(255, 255, 255),      // background
        new Color(240, 250, 245),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(25, 135, 84),        // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    DANGER(
        new Color(255, 255, 255),      // background
        new Color(255, 240, 242),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(220, 53, 69),        // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    WARNING(
        new Color(255, 255, 255),      // background
        new Color(255, 250, 235),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(255, 193, 7),        // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    INFO(
        new Color(255, 255, 255),      // background
        new Color(235, 250, 255),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(13, 202, 240),       // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    LIGHT(
        new Color(248, 249, 250),      // background
        new Color(255, 255, 255),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(173, 181, 189),      // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    DARK(
        new Color(52, 58, 64),         // background
        new Color(73, 80, 87),         // focusBackground
        new Color(108, 117, 125),      // borderColor
        new Color(173, 181, 189),      // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        Color.WHITE,                   // textColor
        new Color(173, 181, 189),      // placeholderColor
        new Color(120, 40, 50),        // errorBackground
        new Color(40, 80, 60)          // successBackground
    ),
    
    OCEAN(
        new Color(255, 255, 255),      // background
        new Color(224, 242, 241),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(0, 150, 136),        // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    ),
    
    PURPLE(
        new Color(255, 255, 255),      // background
        new Color(243, 229, 245),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(156, 39, 176),       // focusBorderColor
        new Color(220, 53, 69),        // errorBorderColor
        new Color(25, 135, 84),        // successBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125),      // placeholderColor
        new Color(255, 240, 242),      // errorBackground
        new Color(240, 250, 245)       // successBackground
    );
    
    private final Color background;
    private final Color focusBackground;
    private final Color borderColor;
    private final Color focusBorderColor;
    private final Color errorBorderColor;
    private final Color successBorderColor;
    private final Color textColor;
    private final Color placeholderColor;
    private final Color errorBackground;
    private final Color successBackground;
    
    private HFormattedTextFieldStyle(Color bg, Color focusBg, Color border, Color focusBorder,
                                    Color errorBorder, Color successBorder, Color text, 
                                    Color placeholder, Color errorBg, Color successBg) {
        this.background = bg;
        this.focusBackground = focusBg;
        this.borderColor = border;
        this.focusBorderColor = focusBorder;
        this.errorBorderColor = errorBorder;
        this.successBorderColor = successBorder;
        this.textColor = text;
        this.placeholderColor = placeholder;
        this.errorBackground = errorBg;
        this.successBackground = successBg;
    }
    
    public Color getBackground() { return background; }
    public Color getFocusBackground() { return focusBackground; }
    public Color getBorderColor() { return borderColor; }
    public Color getFocusBorderColor() { return focusBorderColor; }
    public Color getErrorBorderColor() { return errorBorderColor; }
    public Color getSuccessBorderColor() { return successBorderColor; }
    public Color getTextColor() { return textColor; }
    public Color getPlaceholderColor() { return placeholderColor; }
    public Color getErrorBackground() { return errorBackground; }
    public Color getSuccessBackground() { return successBackground; }
}