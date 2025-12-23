/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles visuels prédéfinis pour le composant HInternalFrame.
 * Chaque style définit une palette de couleurs cohérente pour la barre de titre,
 * le fond, le texte et les boutons de contrôle.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HInternalFrame
 */
public enum HInternalFrameStyle {
    
    /**
     * Style PRIMARY - Bleu moderne.
     * Pour les fenêtres principales.
     */
    PRIMARY(
        new Color(13, 110, 253),           // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style SECONDARY - Gris neutre.
     * Pour les fenêtres secondaires.
     */
    SECONDARY(
        new Color(108, 117, 125),          // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style LIGHT - Thème clair.
     * Design minimaliste.
     */
    LIGHT(
        new Color(248, 249, 250),          // Barre de titre
        Color.WHITE,                        // Fond
        new Color(33, 37, 41),             // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style DARK - Mode sombre.
     * Élégant et moderne.
     */
    DARK(
        new Color(33, 37, 41),             // Barre de titre
        new Color(52, 58, 64),             // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style SUCCESS - Vert.
     * Pour les fenêtres de succès.
     */
    SUCCESS(
        new Color(25, 135, 84),            // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.INFO,                 // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    ),
    
    /**
     * Style DANGER - Rouge.
     * Pour les fenêtres d'alerte.
     */
    DANGER(
        new Color(220, 53, 69),            // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.WHITE,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.SECONDARY             // Bouton fermer
    ),
    
    /**
     * Style INFO - Cyan.
     * Pour les fenêtres informatives.
     */
    INFO(
        new Color(13, 202, 240),           // Barre de titre
        new Color(255, 255, 255),          // Fond
        Color.BLACK,                        // Couleur du titre
        HButtonStyle.WARNING,              // Bouton iconifier
        HButtonStyle.SUCCESS,              // Bouton maximiser
        HButtonStyle.DANGER                // Bouton fermer
    );
    
    /** Couleur de la barre de titre */
    private final Color titleBarColor;
    
    /** Couleur de fond de la fenêtre */
    private final Color backgroundColor;
    
    /** Couleur du texte du titre */
    private final Color titleColor;
    
    /** Style du bouton iconifier */
    private final HButtonStyle iconifyButtonStyle;
    
    /** Style du bouton maximiser */
    private final HButtonStyle maximizeButtonStyle;
    
    /** Style du bouton fermer */
    private final HButtonStyle closeButtonStyle;
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param titleBar couleur de la barre de titre
     * @param background couleur de fond
     * @param title couleur du titre
     * @param iconify style du bouton iconifier
     * @param maximize style du bouton maximiser
     * @param close style du bouton fermer
     */
    private HInternalFrameStyle(Color titleBar, Color background, Color title,
                                HButtonStyle iconify, HButtonStyle maximize, HButtonStyle close) {
        this.titleBarColor = titleBar;
        this.backgroundColor = background;
        this.titleColor = title;
        this.iconifyButtonStyle = iconify;
        this.maximizeButtonStyle = maximize;
        this.closeButtonStyle = close;
    }
    
    /**
     * Retourne la couleur de la barre de titre.
     * 
     * @return la couleur de la barre de titre
     */
    public Color getTitleBarColor() { 
        return titleBarColor; 
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
     * Retourne la couleur du titre.
     * 
     * @return la couleur du titre
     */
    public Color getTitleColor() { 
        return titleColor; 
    }
    
    /**
     * Retourne le style du bouton iconifier.
     * 
     * @return le style HButtonStyle
     */
    public HButtonStyle getIconifyButtonStyle() { 
        return iconifyButtonStyle; 
    }
    
    /**
     * Retourne le style du bouton maximiser.
     * 
     * @return le style HButtonStyle
     */
    public HButtonStyle getMaximizeButtonStyle() { 
        return maximizeButtonStyle; 
    }
    
    /**
     * Retourne le style du bouton fermer.
     * 
     * @return le style HButtonStyle
     */
    public HButtonStyle getCloseButtonStyle() { 
        return closeButtonStyle; 
    }
}