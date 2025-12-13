/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HScrollBar.
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HScrollBarStyle {
    
    PRIMARY(
        new Color(240, 245, 255),      // trackColor - fond de la piste
        new Color(13, 110, 253),       // thumbColor - couleur du curseur
        new Color(10, 88, 202),        // thumbHoverColor - curseur au survol
        new Color(4, 57, 128)          // thumbPressedColor - curseur pressé
    ),
    
    SECONDARY(
        new Color(240, 242, 245),      // trackColor
        new Color(108, 117, 125),      // thumbColor
        new Color(92, 99, 106),        // thumbHoverColor
        new Color(73, 80, 87)          // thumbPressedColor
    ),
    
    SUCCESS(
        new Color(240, 250, 245),      // trackColor
        new Color(25, 135, 84),        // thumbColor
        new Color(20, 108, 67),        // thumbHoverColor
        new Color(15, 90, 55)          // thumbPressedColor
    ),
    
    DANGER(
        new Color(255, 240, 242),      // trackColor
        new Color(220, 53, 69),        // thumbColor
        new Color(176, 42, 55),        // thumbHoverColor
        new Color(148, 30, 41)         // thumbPressedColor
    ),
    
    WARNING(
        new Color(255, 250, 235),      // trackColor
        new Color(255, 193, 7),        // thumbColor
        new Color(255, 213, 85),       // thumbHoverColor
        new Color(193, 142, 0)         // thumbPressedColor
    ),
    
    INFO(
        new Color(235, 250, 255),      // trackColor
        new Color(13, 202, 240),       // thumbColor
        new Color(10, 162, 192),       // thumbHoverColor
        new Color(7, 114, 135)         // thumbPressedColor
    ),
    
    LIGHT(
        new Color(248, 249, 250),      // trackColor
        new Color(173, 181, 189),      // thumbColor
        new Color(148, 156, 164),      // thumbHoverColor
        new Color(134, 142, 150)       // thumbPressedColor
    ),
    
    DARK(
        new Color(52, 58, 64),         // trackColor
        new Color(173, 181, 189),      // thumbColor
        new Color(206, 212, 218),      // thumbHoverColor
        new Color(233, 236, 239)       // thumbPressedColor
    ),
    
    OCEAN(
        new Color(224, 242, 241),      // trackColor
        new Color(0, 150, 136),        // thumbColor
        new Color(0, 121, 107),        // thumbHoverColor
        new Color(0, 96, 88)           // thumbPressedColor
    ),
    
    PURPLE(
        new Color(243, 229, 245),      // trackColor
        new Color(156, 39, 176),       // thumbColor
        new Color(123, 31, 162),       // thumbHoverColor
        new Color(106, 27, 154)        // thumbPressedColor
    );
    
    private final Color trackColor;
    private final Color thumbColor;
    private final Color thumbHoverColor;
    private final Color thumbPressedColor;
    
    private HScrollBarStyle(Color track, Color thumb, Color thumbHover, Color thumbPressed) {
        this.trackColor = track;
        this.thumbColor = thumb;
        this.thumbHoverColor = thumbHover;
        this.thumbPressedColor = thumbPressed;
    }
    
    public Color getTrackColor() { return trackColor; }
    public Color getThumbColor() { return thumbColor; }
    public Color getThumbHoverColor() { return thumbHoverColor; }
    public Color getThumbPressedColor() { return thumbPressedColor; }
}