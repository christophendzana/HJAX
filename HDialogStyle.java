/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles visuels prédéfinis pour le composant HDialog.
 * Chaque style définit une palette de couleurs cohérente pour le fond,
 * les effets hover, l'état pressé et le texte.
 * 
 * <p>Les styles suivent les conventions Bootstrap pour une cohérence visuelle.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see HDialog
 */
public enum HDialogStyle {
    
    /**
     * Style PRIMARY - Bleu vif pour les actions principales.
     * Utilisé pour les dialogs d'information ou de confirmation importantes.
     */
    PRIMARY(new Color(13, 110, 253),   
            new Color(4, 57, 128),   
            new Color(10, 88, 202),    
            Color.WHITE),
    
    /**
     * Style SECONDARY - Gris neutre pour les actions secondaires.
     * Utilisé pour les dialogs d'information générale.
     */
    SECONDARY(new Color(108, 117, 125), 
              new Color(92, 99, 106),   
              new Color(86, 94, 100),   
              Color.WHITE),
    
    /**
     * Style SUCCESS - Vert pour les messages de succès.
     * Utilisé pour confirmer une action réussie.
     */
    SUCCESS(new Color(25, 135, 84),    
            new Color(15, 90, 55),   
            new Color(20, 108, 67),    
            Color.WHITE),
    
    /**
     * Style DANGER - Rouge pour les avertissements critiques.
     * Utilisé pour les dialogs de suppression ou d'actions destructives.
     */
    DANGER(new Color(220, 53, 69),     
           new Color(148, 30, 41),     
           new Color(176, 42, 55),    
           Color.WHITE),
    
    /**
     * Style WARNING - Jaune/Orange pour les avertissements.
     * Utilisé pour alerter l'utilisateur sans bloquer l'action.
     */
    WARNING(new Color(255, 193, 7),    
            new Color(255, 205, 57),   
            new Color(255, 213, 85),   
            Color.BLACK),
    
    /**
     * Style INFO - Cyan pour les informations neutres.
     * Utilisé pour les notifications et conseils.
     */
    INFO(new Color(13, 202, 240),      
         new Color(7, 114, 135),      
         new Color(10, 162, 192),      
         Color.BLACK),
    
    /**
     * Style LIGHT - Fond clair avec texte sombre.
     * Utilisé pour un design minimaliste et épuré.
     */
    LIGHT(new Color(248, 249, 250),    
          new Color(233, 236, 239),    
          new Color(221, 224, 227),    
          Color.BLACK),
    
    /**
     * Style DARK - Fond sombre avec texte clair.
     * Utilisé pour un mode sombre élégant.
     */
    DARK(new Color(33, 37, 41),        
         new Color(41, 46, 51),        
         new Color(52, 58, 64),       
         Color.WHITE),
    
    /**
     * Style CUSTOM - Fond blanc avec accent bleu.
     * Utilisé pour un design personnalisé et neutre.
     */
    CUSTOM(Color.WHITE,                  
           new Color(248, 249, 250),     
           new Color(233, 236, 239),     
           new Color(13, 110, 253));
    
    /** Couleur de base du dialog */
    private final Color baseColor;
    
    /** Couleur lors du survol (hover) */
    private final Color hoverColor;
    
    /** Couleur lors de la pression (pressed) */
    private final Color pressColor;
    
    /** Couleur du texte */
    private final Color textColor;
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param base couleur de base
     * @param hover couleur de survol
     * @param press couleur de pression
     * @param text couleur du texte
     */
    private HDialogStyle(Color base, Color hover, Color press, Color text) {
        this.baseColor = base;
        this.hoverColor = hover;
        this.pressColor = press;
        this.textColor = text;
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
     * Retourne la couleur de survol du style.
     * 
     * @return la couleur de survol
     */
    public Color getHoverColor() { 
        return hoverColor; 
    }
    
    /**
     * Retourne la couleur de pression du style.
     * 
     * @return la couleur de pression
     */
    public Color getPressColor() { 
        return pressColor; 
    }
    
    /**
     * Retourne la couleur du texte du style.
     * 
     * @return la couleur du texte
     */
    public Color getTextColor() {
        return textColor;
    }
}