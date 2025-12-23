/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles visuels prédéfinis pour le composant HFrame.
 * Chaque style définit une palette de couleurs cohérente pour la barre de titre,
 * le fond, le texte et les boutons de contrôle.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HFrame
 */
public enum HFrameStyle {
    
    /**
     * Style PRIMARY - Bleu moderne.
     */
    PRIMARY(
        new Color(13, 110, 253),           // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser (jaune)
        HButtonStyle.SUCCESS,              // Bouton maximiser (vert)
        HButtonStyle.DANGER                // Bouton fermer (rouge)
    ),
    
    /**
     * Style DARK - Mode sombre élégant.
     */
    DARK(
        new Color(33, 37, 41),             // Barre de titre
        new Color(52, 58, 64),             // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style LIGHT - Thème clair minimaliste.
     */
    LIGHT(
        new Color(248, 249, 250),          // Barre de titre
        Color.WHITE,                        // Fond
        new Color(33, 37, 41),             // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style SUCCESS - Vert moderne.
     */
    SUCCESS(
        new Color(25, 135, 84),            // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser
        HButtonStyle.INFO,                 // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style DANGER - Rouge pour les alertes.
     */
    DANGER(
        new Color(220, 53, 69),            // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.SECONDARY             // Bouton fermer (gris)
    ),
    
    /**
     * Style INFO - Cyan moderne.
     */
    INFO(
        new Color(13, 202, 240),           // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.BLACK,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style CUSTOM - Personnalisable.
     */
    CUSTOM(
        new Color(100, 100, 120),          // Barre de titre
        new Color(240, 242, 245),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton minimiser
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    );
    
    /** Couleur de la barre de titre */
    private final Color titleBarColor;
    
    /** Couleur de fond de la fenêtre */
    private final Color backgroundColor;
    
    /** Couleur du texte du titre */
    private final Color titleColor;
    
    /** Style du bouton minimiser */
    private final HButtonStyle minimizeButtonStyle;
    
    /** Style du bouton maximiser */
    private final HButtonStyle maximizeButtonStyle;
    
    /** Style du bouton fermer */
    private final HButtonStyle closeButtonStyle;
    
    /**
     * Constructeur de l'énumération.
     */
    private HFrameStyle(Color titleBar, Color background, Color title,
                        HButtonStyle minimize, HButtonStyle maximize, HButtonStyle close) {
        this.titleBarColor = titleBar;
        this.backgroundColor = background;
        this.titleColor = title;
        this.minimizeButtonStyle = minimize;
        this.maximizeButtonStyle = maximize;
        this.closeButtonStyle = close;
    }
    
    public Color getTitleBarColor() { return titleBarColor; }
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getTitleColor() { return titleColor; }
    public HButtonStyle getMinimizeButtonStyle() { return minimizeButtonStyle; }
    public HButtonStyle getMaximizeButtonStyle() { return maximizeButtonStyle; }
    public HButtonStyle getCloseButtonStyle() { return closeButtonStyle; }
}