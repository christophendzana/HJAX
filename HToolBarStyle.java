/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles visuels prédéfinis pour le composant HToolBar.
 * Chaque style définit une palette de couleurs cohérente pour le fond
 * et les séparateurs.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HToolBar
 */
public enum HToolBarStyle {
    
    /**
     * Style PRIMARY - Bleu moderne.
     * Idéal pour les barres d'outils principales.
     */
    PRIMARY(
        new Color(13, 110, 253),           // Couleur de base
        new Color(255, 255, 255, 80)       // Couleur des séparateurs
    ),
    
    /**
     * Style SECONDARY - Gris neutre.
     * Pour les barres d'outils secondaires.
     */
    SECONDARY(
        new Color(108, 117, 125),          // Couleur de base
        new Color(255, 255, 255, 80)       // Couleur des séparateurs
    ),
    
    /**
     * Style LIGHT - Fond clair.
     * Design minimaliste et épuré.
     */
    LIGHT(
        new Color(248, 249, 250),          // Couleur de base
        new Color(222, 226, 230)           // Couleur des séparateurs
    ),
    
    /**
     * Style DARK - Fond sombre.
     * Mode sombre élégant.
     */
    DARK(
        new Color(33, 37, 41),             // Couleur de base
        new Color(255, 255, 255, 50)       // Couleur des séparateurs
    ),
    
    /**
     * Style SUCCESS - Vert.
     * Pour les actions positives.
     */
    SUCCESS(
        new Color(25, 135, 84),            // Couleur de base
        new Color(255, 255, 255, 80)       // Couleur des séparateurs
    ),
    
    /**
     * Style DANGER - Rouge.
     * Pour les actions destructives.
     */
    DANGER(
        new Color(220, 53, 69),            // Couleur de base
        new Color(255, 255, 255, 80)       // Couleur des séparateurs
    ),
    
    /**
     * Style WARNING - Jaune/Orange.
     * Pour les avertissements.
     */
    WARNING(
        new Color(255, 193, 7),            // Couleur de base
        new Color(0, 0, 0, 50)             // Couleur des séparateurs
    ),
    
    /**
     * Style INFO - Cyan.
     * Pour les informations.
     */
    INFO(
        new Color(13, 202, 240),           // Couleur de base
        new Color(0, 0, 0, 50)             // Couleur des séparateurs
    ),
    
    /**
     * Style TRANSPARENT - Fond transparent.
     * Pour un look discret et moderne.
     */
    TRANSPARENT(
        new Color(255, 255, 255, 30),      // Couleur de base (très transparent)
        new Color(0, 0, 0, 30)             // Couleur des séparateurs
    );
    
    /** Couleur de base de la toolbar */
    private final Color baseColor;
    
    /** Couleur des séparateurs */
    private final Color separatorColor;
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param base la couleur de base
     * @param separator la couleur des séparateurs
     */
    private HToolBarStyle(Color base, Color separator) {
        this.baseColor = base;
        this.separatorColor = separator;
    }
    
    /**
     * Retourne la couleur de base du style.
     * 
     * @return la couleur de base
     */
    public Color getBaseColor() {
        return baseColor;
    }
    
    /**
     * Retourne la couleur des séparateurs.
     * 
     * @return la couleur des séparateurs
     */
    public Color getSeparatorColor() {
        return separatorColor;
    }
}