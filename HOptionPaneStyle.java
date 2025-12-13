/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HOptionPane.
 * Chaque style définit un ensemble cohérent de couleurs pour :
 * - Le fond du dialog
 * - La couleur de l'icône
 * - La couleur de l'en-tête
 * - Les boutons
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HOptionPaneStyle {
    
    PRIMARY(
        new Color(255, 255, 255),      // backgroundColor - fond du dialog
        new Color(240, 245, 255),      // headerBackground - fond de l'en-tête
        new Color(13, 110, 253),       // iconColor - couleur de l'icône
        new Color(13, 110, 253),       // accentColor - couleur d'accentuation
        new Color(33, 37, 41),         // textColor - couleur du texte
        new Color(108, 117, 125)       // secondaryTextColor - texte secondaire
    ),
    
    SUCCESS(
        new Color(255, 255, 255),      // backgroundColor
        new Color(240, 250, 245),      // headerBackground
        new Color(25, 135, 84),        // iconColor
        new Color(25, 135, 84),        // accentColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125)       // secondaryTextColor
    ),
    
    DANGER(
        new Color(255, 255, 255),      // backgroundColor
        new Color(255, 240, 242),      // headerBackground
        new Color(220, 53, 69),        // iconColor
        new Color(220, 53, 69),        // accentColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125)       // secondaryTextColor
    ),
    
    WARNING(
        new Color(255, 255, 255),      // backgroundColor
        new Color(255, 250, 235),      // headerBackground
        new Color(255, 193, 7),        // iconColor
        new Color(255, 193, 7),        // accentColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125)       // secondaryTextColor
    ),
    
    INFO(
        new Color(255, 255, 255),      // backgroundColor
        new Color(235, 250, 255),      // headerBackground
        new Color(13, 202, 240),       // iconColor
        new Color(13, 202, 240),       // accentColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125)       // secondaryTextColor
    ),
    
    QUESTION(
        new Color(255, 255, 255),      // backgroundColor
        new Color(243, 229, 245),      // headerBackground
        new Color(156, 39, 176),       // iconColor
        new Color(156, 39, 176),       // accentColor
        new Color(33, 37, 41),         // textColor
        new Color(108, 117, 125)       // secondaryTextColor
    ),
    
    DARK(
        new Color(52, 58, 64),         // backgroundColor
        new Color(73, 80, 87),         // headerBackground
        new Color(173, 181, 189),      // iconColor
        new Color(173, 181, 189),      // accentColor
        Color.WHITE,                   // textColor
        new Color(206, 212, 218)       // secondaryTextColor
    );
    
    private final Color backgroundColor;
    private final Color headerBackground;
    private final Color iconColor;
    private final Color accentColor;
    private final Color textColor;
    private final Color secondaryTextColor;
    
    private HOptionPaneStyle(Color bg, Color headerBg, Color icon, Color accent, 
                            Color text, Color secondaryText) {
        this.backgroundColor = bg;
        this.headerBackground = headerBg;
        this.iconColor = icon;
        this.accentColor = accent;
        this.textColor = text;
        this.secondaryTextColor = secondaryText;
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getHeaderBackground() { return headerBackground; }
    public Color getIconColor() { return iconColor; }
    public Color getAccentColor() { return accentColor; }
    public Color getTextColor() { return textColor; }
    public Color getSecondaryTextColor() { return secondaryTextColor; }
}