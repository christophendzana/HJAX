/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HSlider.
 * Chaque style définit un ensemble cohérent de couleurs pour :
 * - Le track (piste)
 * - Le thumb (curseur)
 * - Les états hover et pressed
 * - Les graduations et labels
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HSliderStyle {
    
    PRIMARY(
        new Color(206, 212, 218),      // trackBackground - fond de la piste
        new Color(13, 110, 253),       // trackFillColor - partie remplie
        new Color(13, 110, 253),       // thumbColor - couleur du curseur
        new Color(10, 88, 202),        // thumbHoverColor - curseur au survol
        new Color(4, 57, 128),         // thumbPressedColor - curseur pressé
        Color.WHITE,                   // thumbBorderColor - bordure du curseur
        new Color(108, 117, 125),      // tickColor - couleur des graduations
        new Color(33, 37, 41)          // labelColor - couleur des labels
    ),
    
    SECONDARY(
        new Color(206, 212, 218),      // trackBackground
        new Color(108, 117, 125),      // trackFillColor
        new Color(108, 117, 125),      // thumbColor
        new Color(92, 99, 106),        // thumbHoverColor
        new Color(73, 80, 87),         // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    SUCCESS(
        new Color(206, 212, 218),      // trackBackground
        new Color(25, 135, 84),        // trackFillColor
        new Color(25, 135, 84),        // thumbColor
        new Color(20, 108, 67),        // thumbHoverColor
        new Color(15, 90, 55),         // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    DANGER(
        new Color(206, 212, 218),      // trackBackground
        new Color(220, 53, 69),        // trackFillColor
        new Color(220, 53, 69),        // thumbColor
        new Color(176, 42, 55),        // thumbHoverColor
        new Color(148, 30, 41),        // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    WARNING(
        new Color(206, 212, 218),      // trackBackground
        new Color(255, 193, 7),        // trackFillColor
        new Color(255, 193, 7),        // thumbColor
        new Color(255, 213, 85),       // thumbHoverColor
        new Color(193, 142, 0),        // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    INFO(
        new Color(206, 212, 218),      // trackBackground
        new Color(13, 202, 240),       // trackFillColor
        new Color(13, 202, 240),       // thumbColor
        new Color(10, 162, 192),       // thumbHoverColor
        new Color(7, 114, 135),        // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    LIGHT(
        new Color(233, 236, 239),      // trackBackground
        new Color(173, 181, 189),      // trackFillColor
        new Color(173, 181, 189),      // thumbColor
        new Color(148, 156, 164),      // thumbHoverColor
        new Color(134, 142, 150),      // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    DARK(
        new Color(73, 80, 87),         // trackBackground
        new Color(173, 181, 189),      // trackFillColor
        new Color(173, 181, 189),      // thumbColor
        new Color(206, 212, 218),      // thumbHoverColor
        new Color(233, 236, 239),      // thumbPressedColor
        new Color(52, 58, 64),         // thumbBorderColor
        new Color(173, 181, 189),      // tickColor
        Color.WHITE                    // labelColor
    ),
    
    OCEAN(
        new Color(206, 212, 218),      // trackBackground
        new Color(0, 150, 136),        // trackFillColor
        new Color(0, 150, 136),        // thumbColor
        new Color(0, 121, 107),        // thumbHoverColor
        new Color(0, 96, 88),          // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    ),
    
    PURPLE(
        new Color(206, 212, 218),      // trackBackground
        new Color(156, 39, 176),       // trackFillColor
        new Color(156, 39, 176),       // thumbColor
        new Color(123, 31, 162),       // thumbHoverColor
        new Color(106, 27, 154),       // thumbPressedColor
        Color.WHITE,                   // thumbBorderColor
        new Color(108, 117, 125),      // tickColor
        new Color(33, 37, 41)          // labelColor
    );
    
    private final Color trackBackground;
    private final Color trackFillColor;
    private final Color thumbColor;
    private final Color thumbHoverColor;
    private final Color thumbPressedColor;
    private final Color thumbBorderColor;
    private final Color tickColor;
    private final Color labelColor;
    
    private HSliderStyle(Color trackBg, Color trackFill, Color thumb, Color thumbHover,
                        Color thumbPressed, Color thumbBorder, Color tick, Color label) {
        this.trackBackground = trackBg;
        this.trackFillColor = trackFill;
        this.thumbColor = thumb;
        this.thumbHoverColor = thumbHover;
        this.thumbPressedColor = thumbPressed;
        this.thumbBorderColor = thumbBorder;
        this.tickColor = tick;
        this.labelColor = label;
    }
    
    public Color getTrackBackground() { return trackBackground; }
    public Color getTrackFillColor() { return trackFillColor; }
    public Color getThumbColor() { return thumbColor; }
    public Color getThumbHoverColor() { return thumbHoverColor; }
    public Color getThumbPressedColor() { return thumbPressedColor; }
    public Color getThumbBorderColor() { return thumbBorderColor; }
    public Color getTickColor() { return tickColor; }
    public Color getLabelColor() { return labelColor; }
}