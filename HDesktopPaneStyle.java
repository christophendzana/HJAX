/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles visuels prédéfinis pour le composant HDesktopPane.
 * Chaque style définit une couleur de fond cohérente pour le conteneur
 * de fenêtres internes.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HDesktopPane
 */
public enum HDesktopPaneStyle {
    
    /**
     * Style LIGHT - Fond clair.
     * Design minimaliste et épuré, idéal pour un environnement de travail.
     */
    LIGHT(new Color(248, 249, 250)),
    
    /**
     * Style DARK - Fond sombre.
     * Mode sombre élégant pour réduire la fatigue oculaire.
     */
    DARK(new Color(33, 37, 41)),
    
    /**
     * Style BLUE - Fond bleu doux.
     * Ambiance professionnelle et apaisante.
     */
    BLUE(new Color(230, 240, 255)),
    
    /**
     * Style GRAY - Fond gris neutre.
     * Équilibre entre clair et sombre.
     */
    GRAY(new Color(220, 226, 230)),
    
    /**
     * Style GRADIENT_BLUE - Dégradé bleu moderne.
     * Pour un effet visuel sophistiqué.
     */
    GRADIENT_BLUE(new Color(13, 110, 253)),
    
    /**
     * Style GRADIENT_PURPLE - Dégradé violet créatif.
     * Pour une interface distinctive et moderne.
     */
    GRADIENT_PURPLE(new Color(130, 80, 180)),
    
    /**
     * Style GRADIENT_GREEN - Dégradé vert naturel.
     * Ambiance reposante et productive.
     */
    GRADIENT_GREEN(new Color(25, 135, 84)),
    
    /**
     * Style WARM - Tons chauds.
     * Ambiance chaleureuse et accueillante.
     */
    WARM(new Color(255, 248, 240)),
    
    /**
     * Style COOL - Tons froids.
     * Ambiance calme et concentrée.
     */
    COOL(new Color(240, 248, 255));
    
    /** Couleur de fond du desktop pane */
    private final Color backgroundColor;
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param background la couleur de fond
     */
    private HDesktopPaneStyle(Color background) {
        this.backgroundColor = background;
    }
    
    /**
     * Retourne la couleur de fond du style.
     * 
     * @return la couleur de fond
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }
}