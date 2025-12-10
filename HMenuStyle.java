/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour les composants HMenu et HMenuItem.
 * Chaque style définit un ensemble cohérent de couleurs pour :
 * - La barre de menu (menuBarColor)
 * - Le fond du menu déroulant (popupBackground)
 * - Les items normaux (baseColor)
 * - Les items au survol (hoverColor)
 * - Les items sélectionnés (selectedColor)
 * - Le texte
 * - Les séparateurs
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HMenuStyle {
    
    PRIMARY(
        new Color(13, 110, 253),       // menuBarColor - fond de la barre de menu
        new Color(229, 239, 255),      // popupBackground - fond du menu popup
        new Color(240, 245, 255),      // baseColor - item normal
        new Color(13, 110, 253),       // hoverColor - item au survol
        new Color(10, 88, 202),        // selectedColor - item sélectionné
        Color.WHITE,                   // menuBarTextColor - texte dans la barre
        new Color(33, 37, 41),         // itemTextColor - texte des items
        new Color(200, 200, 200)       // separatorColor - couleur des séparateurs
    ),
    
    SECONDARY(
        new Color(108, 117, 125),      // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(240, 242, 245),      // baseColor
        new Color(108, 117, 125),      // hoverColor
        new Color(92, 99, 106),        // selectedColor
        Color.WHITE,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    SUCCESS(
        new Color(25, 135, 84),        // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(240, 250, 245),      // baseColor
        new Color(25, 135, 84),        // hoverColor
        new Color(20, 108, 67),        // selectedColor
        Color.WHITE,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    DANGER(
        new Color(220, 53, 69),        // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(255, 240, 242),      // baseColor
        new Color(220, 53, 69),        // hoverColor
        new Color(176, 42, 55),        // selectedColor
        Color.WHITE,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    WARNING(
        new Color(193, 142, 0),        // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(255, 250, 235),      // baseColor
        new Color(255, 193, 7),        // hoverColor
        new Color(193, 142, 0),        // selectedColor
        Color.WHITE,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    INFO(
        new Color(13, 202, 240),       // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(235, 250, 255),      // baseColor
        new Color(13, 202, 240),       // hoverColor
        new Color(10, 162, 192),       // selectedColor
        Color.BLACK,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    LIGHT(
        new Color(248, 249, 250),      // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(240, 242, 245),      // baseColor
        new Color(233, 236, 239),      // hoverColor
        new Color(221, 224, 227),      // selectedColor
        new Color(33, 37, 41),         // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    DARK(
        new Color(33, 37, 41),         // menuBarColor
        new Color(52, 58, 64),         // popupBackground
        new Color(73, 80, 87),         // baseColor
        new Color(108, 117, 125),      // hoverColor
        new Color(134, 142, 150),      // selectedColor
        Color.WHITE,                   // menuBarTextColor
        Color.WHITE,                   // itemTextColor
        new Color(108, 117, 125)       // separatorColor
    ),
    
    OCEAN(
        new Color(0, 150, 136),        // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(224, 242, 241),      // baseColor
        new Color(0, 150, 136),        // hoverColor
        new Color(0, 121, 107),        // selectedColor
        Color.WHITE,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    ),
    
    PURPLE(
        new Color(156, 39, 176),       // menuBarColor
        new Color(255, 255, 255),      // popupBackground
        new Color(243, 229, 245),      // baseColor
        new Color(156, 39, 176),       // hoverColor
        new Color(123, 31, 162),       // selectedColor
        Color.WHITE,                   // menuBarTextColor
        new Color(33, 37, 41),         // itemTextColor
        new Color(200, 200, 200)       // separatorColor
    );
    
    private final Color menuBarColor;
    private final Color popupBackground;
    private final Color baseColor;
    private final Color hoverColor;
    private final Color selectedColor;
    private final Color menuBarTextColor;
    private final Color itemTextColor;
    private final Color separatorColor;
    
    private HMenuStyle(Color menuBar, Color popup, Color base, Color hover, 
                       Color selected, Color menuBarText, Color itemText, Color separator) {
        this.menuBarColor = menuBar;
        this.popupBackground = popup;
        this.baseColor = base;
        this.hoverColor = hover;
        this.selectedColor = selected;
        this.menuBarTextColor = menuBarText;
        this.itemTextColor = itemText;
        this.separatorColor = separator;
    }
    
    public Color getMenuBarColor() { return menuBarColor; }
    public Color getPopupBackground() { return popupBackground; }
    public Color getBaseColor() { return baseColor; }
    public Color getHoverColor() { return hoverColor; }
    public Color getSelectedColor() { return selectedColor; }
    public Color getMenuBarTextColor() { return menuBarTextColor; }
    public Color getItemTextColor() { return itemTextColor; }
    public Color getSeparatorColor() { return separatorColor; }
}