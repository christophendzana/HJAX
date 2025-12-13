/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HTabbedPane.
 * Chaque style définit un ensemble cohérent de couleurs pour :
 * - Le fond des onglets
 * - L'onglet sélectionné
 * - L'onglet au survol
 * - Le fond du contenu
 * - Les bordures et indicateurs
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HTabbedPaneStyle {
    
    PRIMARY(
        new Color(255, 255, 255),      // backgroundColor - fond général
        new Color(245, 245, 245),      // tabBackground - fond des onglets
        new Color(13, 110, 253),       // selectedTabBackground - onglet sélectionné
        new Color(240, 245, 255),      // hoverTabBackground - onglet au survol
        new Color(13, 110, 253),       // indicatorColor - indicateur sous l'onglet
        new Color(33, 37, 41),         // textColor - texte normal
        Color.WHITE,                   // selectedTextColor - texte sélectionné
        new Color(220, 220, 220),      // borderColor - bordure
        new Color(255, 255, 255)       // contentBackground - fond du contenu
    ),
    
    SECONDARY(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(108, 117, 125),      // selectedTabBackground
        new Color(240, 242, 245),      // hoverTabBackground
        new Color(108, 117, 125),      // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.WHITE,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    SUCCESS(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(25, 135, 84),        // selectedTabBackground
        new Color(240, 250, 245),      // hoverTabBackground
        new Color(25, 135, 84),        // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.WHITE,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    DANGER(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(220, 53, 69),        // selectedTabBackground
        new Color(255, 240, 242),      // hoverTabBackground
        new Color(220, 53, 69),        // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.WHITE,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    WARNING(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(255, 193, 7),        // selectedTabBackground
        new Color(255, 250, 235),      // hoverTabBackground
        new Color(255, 193, 7),        // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.BLACK,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    INFO(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(13, 202, 240),       // selectedTabBackground
        new Color(235, 250, 255),      // hoverTabBackground
        new Color(13, 202, 240),       // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.BLACK,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    LIGHT(
        new Color(248, 249, 250),      // backgroundColor
        new Color(255, 255, 255),      // tabBackground
        new Color(233, 236, 239),      // selectedTabBackground
        new Color(248, 249, 250),      // hoverTabBackground
        new Color(173, 181, 189),      // indicatorColor
        new Color(33, 37, 41),         // textColor
        new Color(33, 37, 41),         // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    DARK(
        new Color(33, 37, 41),         // backgroundColor
        new Color(52, 58, 64),         // tabBackground
        new Color(73, 80, 87),         // selectedTabBackground
        new Color(62, 68, 74),         // hoverTabBackground
        new Color(173, 181, 189),      // indicatorColor
        Color.WHITE,                   // textColor
        Color.WHITE,                   // selectedTextColor
        new Color(73, 80, 87),         // borderColor
        new Color(52, 58, 64)          // contentBackground
    ),
    
    OCEAN(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(0, 150, 136),        // selectedTabBackground
        new Color(224, 242, 241),      // hoverTabBackground
        new Color(0, 150, 136),        // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.WHITE,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    ),
    
    PURPLE(
        new Color(255, 255, 255),      // backgroundColor
        new Color(245, 245, 245),      // tabBackground
        new Color(156, 39, 176),       // selectedTabBackground
        new Color(243, 229, 245),      // hoverTabBackground
        new Color(156, 39, 176),       // indicatorColor
        new Color(33, 37, 41),         // textColor
        Color.WHITE,                   // selectedTextColor
        new Color(220, 220, 220),      // borderColor
        new Color(255, 255, 255)       // contentBackground
    );
    
    private final Color backgroundColor;
    private final Color tabBackground;
    private final Color selectedTabBackground;
    private final Color hoverTabBackground;
    private final Color indicatorColor;
    private final Color textColor;
    private final Color selectedTextColor;
    private final Color borderColor;
    private final Color contentBackground;
    
    private HTabbedPaneStyle(Color bg, Color tabBg, Color selectedBg, Color hoverBg,
                            Color indicator, Color text, Color selectedText, 
                            Color border, Color contentBg) {
        this.backgroundColor = bg;
        this.tabBackground = tabBg;
        this.selectedTabBackground = selectedBg;
        this.hoverTabBackground = hoverBg;
        this.indicatorColor = indicator;
        this.textColor = text;
        this.selectedTextColor = selectedText;
        this.borderColor = border;
        this.contentBackground = contentBg;
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getTabBackground() { return tabBackground; }
    public Color getSelectedTabBackground() { return selectedTabBackground; }
    public Color getHoverTabBackground() { return hoverTabBackground; }
    public Color getIndicatorColor() { return indicatorColor; }
    public Color getTextColor() { return textColor; }
    public Color getSelectedTextColor() { return selectedTextColor; }
    public Color getBorderColor() { return borderColor; }
    public Color getContentBackground() { return contentBackground; }
}