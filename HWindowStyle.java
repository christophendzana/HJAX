/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles visuels prédéfinis pour le composant HWindow.
 * Chaque style définit une palette de couleurs cohérente pour le fond,
 * les bordures et l'apparence générale de la fenêtre.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HWindow
 */
public enum HWindowStyle {
    
    /**
     * Style PRIMARY - Blanc moderne avec bordure bleue.
     * Pour les fenêtres principales.
     */
    PRIMARY(
        new Color(255, 255, 255),          // Fond blanc
        new Color(13, 110, 253),           // Bordure bleue
        2                                   // Épaisseur bordure
    ),
    
    /**
     * Style SECONDARY - Gris clair neutre.
     * Pour les fenêtres secondaires.
     */
    SECONDARY(
        new Color(248, 249, 250),          // Fond gris très clair
        new Color(108, 117, 125),          // Bordure grise
        2
    ),
    
    /**
     * Style SUCCESS - Blanc avec bordure verte.
     * Pour les notifications de succès.
     */
    SUCCESS(
        new Color(255, 255, 255),          // Fond blanc
        new Color(25, 135, 84),            // Bordure verte
        2
    ),
    
    /**
     * Style DANGER - Blanc avec bordure rouge.
     * Pour les alertes et erreurs.
     */
    DANGER(
        new Color(255, 255, 255),          // Fond blanc
        new Color(220, 53, 69),            // Bordure rouge
        2
    ),
    
    /**
     * Style WARNING - Blanc avec bordure jaune/orange.
     * Pour les avertissements.
     */
    WARNING(
        new Color(255, 255, 255),          // Fond blanc
        new Color(255, 193, 7),            // Bordure jaune
        2
    ),
    
    /**
     * Style INFO - Blanc avec bordure cyan.
     * Pour les informations.
     */
    INFO(
        new Color(255, 255, 255),          // Fond blanc
        new Color(13, 202, 240),           // Bordure cyan
        2
    ),
    
    /**
     * Style DARK - Mode sombre élégant.
     * Pour les fenêtres en mode sombre.
     */
    DARK(
        new Color(33, 37, 41),             // Fond sombre
        new Color(52, 58, 64),             // Bordure gris foncé
        2
    ),
    
    /**
     * Style LIGHT - Ultra minimaliste.
     * Fond blanc avec bordure très subtile.
     */
    LIGHT(
        new Color(255, 255, 255),          // Fond blanc pur
        new Color(233, 236, 239),          // Bordure gris très clair
        1                                   // Bordure fine
    ),
    
    /**
     * Style TRANSPARENT - Fond semi-transparent.
     * Pour les overlays et fenêtres flottantes.
     */
    TRANSPARENT(
        new Color(255, 255, 255, 240),     // Fond blanc semi-transparent
        new Color(200, 200, 200, 200),     // Bordure grise semi-transparente
        1
    ),
    
    /**
     * Style GLASS - Effet verre dépoli.
     * Pour les fenêtres modernes avec effet blur.
     */
    GLASS(
        new Color(255, 255, 255, 200),     // Fond blanc très transparent
        new Color(150, 150, 150, 150),     // Bordure subtile
        1
    ),
    
    /**
     * Style SPLASH - Pour splash screens.
     * Blanc pur sans bordure visible.
     */
    SPLASH(
        new Color(255, 255, 255),          // Fond blanc
        new Color(240, 240, 240),          // Bordure presque invisible
        1
    ),
    
    /**
     * Style TOOLTIP - Pour tooltips personnalisés.
     * Fond jaune pâle avec bordure dorée.
     */
    TOOLTIP(
        new Color(255, 253, 231),          // Fond jaune très pâle
        new Color(255, 193, 7),            // Bordure dorée
        1
    );
    
    /** Couleur de fond de la fenêtre */
    private final Color backgroundColor;
    
    /** Couleur de la bordure */
    private final Color borderColor;
    
    /** Épaisseur de la bordure (en pixels) */
    private final int borderWidth;
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param background couleur de fond
     * @param border couleur de bordure
     * @param borderWidth épaisseur de la bordure
     */
    private HWindowStyle(Color background, Color border, int borderWidth) {
        this.backgroundColor = background;
        this.borderColor = border;
        this.borderWidth = borderWidth;
    }
    
    /**
     * Retourne la couleur de fond.
     * 
     * @return la couleur de fond
     */
    public Color getBackgroundColor() { 
        return backgroundColor; 
    }
    
    /**
     * Retourne la couleur de bordure.
     * 
     * @return la couleur de bordure
     */
    public Color getBorderColor() { 
        return borderColor; 
    }
    
    /**
     * Retourne l'épaisseur de la bordure.
     * 
     * @return l'épaisseur en pixels
     */
    public int getBorderWidth() { 
        return borderWidth; 
    }
}