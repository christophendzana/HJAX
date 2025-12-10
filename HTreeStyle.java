/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HTree.
 * Chaque style définit un ensemble cohérent de couleurs pour :
 * - Les nœuds normaux (base)
 * - Les nœuds au survol (hover)
 * - Les nœuds sélectionnés (selected)
 * - Le texte
 * - Les lignes de connexion
 * - Les nœuds parents et enfants
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum HTreeStyle {
    
    PRIMARY(
        new Color(13, 110, 253),      // baseColor - nœud normal
        new Color(10, 88, 202),        // hoverColor - nœud au survol
        new Color(4, 57, 128),         // selectedColor - nœud sélectionné
        Color.WHITE,                   // textColor - couleur du texte
        new Color(100, 149, 237, 80),  // connectionLineColor - lignes de connexion
        new Color(13, 110, 253),       // parentNodeColor - nœuds parents
        new Color(66, 165, 245)        // childNodeColor - nœuds enfants
    ),
    
    SECONDARY(
        new Color(108, 117, 125),      // baseColor
        new Color(86, 94, 100),        // hoverColor
        new Color(92, 99, 106),        // selectedColor
        Color.WHITE,                   // textColor
        new Color(108, 117, 125, 80),  // connectionLineColor
        new Color(108, 117, 125),      // parentNodeColor
        new Color(134, 142, 150)       // childNodeColor
    ),
    
    SUCCESS(
        new Color(25, 135, 84),        // baseColor
        new Color(20, 108, 67),        // hoverColor
        new Color(15, 90, 55),         // selectedColor
        Color.WHITE,                   // textColor
        new Color(40, 167, 69, 80),    // connectionLineColor
        new Color(25, 135, 84),        // parentNodeColor
        new Color(40, 167, 69)         // childNodeColor
    ),
    
    DANGER(
        new Color(220, 53, 69),        // baseColor
        new Color(176, 42, 55),        // hoverColor
        new Color(148, 30, 41),        // selectedColor
        Color.WHITE,                   // textColor
        new Color(220, 53, 69, 80),    // connectionLineColor
        new Color(220, 53, 69),        // parentNodeColor
        new Color(245, 101, 101)       // childNodeColor
    ),
    
    WARNING(
        new Color(193, 142, 0),        // baseColor
        new Color(255, 213, 85),       // hoverColor
        new Color(255, 205, 57),       // selectedColor
        Color.BLACK,                   // textColor
        new Color(255, 193, 7, 80),    // connectionLineColor
        new Color(193, 142, 0),        // parentNodeColor
        new Color(255, 205, 57)        // childNodeColor
    ),
    
    INFO(
        new Color(13, 202, 240),       // baseColor
        new Color(10, 162, 192),       // hoverColor
        new Color(7, 114, 135),        // selectedColor
        Color.BLACK,                   // textColor
        new Color(13, 202, 240, 80),   // connectionLineColor
        new Color(13, 202, 240),       // parentNodeColor
        new Color(92, 219, 242)        // childNodeColor
    ),
    
    LIGHT(
        new Color(248, 249, 250),      // baseColor
        new Color(221, 224, 227),      // hoverColor
        new Color(233, 236, 239),      // selectedColor
        Color.BLACK,                   // textColor
        new Color(206, 212, 218, 80),  // connectionLineColor
        new Color(233, 236, 239),      // parentNodeColor
        new Color(248, 249, 250)       // childNodeColor
    ),
    
    DARK(
        new Color(33, 37, 41),         // baseColor
        new Color(52, 58, 64),         // hoverColor
        new Color(41, 46, 51),         // selectedColor
        Color.WHITE,                   // textColor
        new Color(73, 80, 87, 80),     // connectionLineColor
        new Color(33, 37, 41),         // parentNodeColor
        new Color(52, 58, 64)          // childNodeColor
    ),
    
    NATURE(
        new Color(76, 175, 80),        // baseColor - vert naturel
        new Color(56, 142, 60),        // hoverColor
        new Color(46, 125, 50),        // selectedColor
        Color.WHITE,                   // textColor
        new Color(129, 199, 132, 80),  // connectionLineColor
        new Color(56, 142, 60),        // parentNodeColor
        new Color(129, 199, 132)       // childNodeColor
    ),
    
    OCEAN(
        new Color(0, 150, 136),        // baseColor - bleu océan
        new Color(0, 121, 107),        // hoverColor
        new Color(0, 96, 88),          // selectedColor
        Color.WHITE,                   // textColor
        new Color(77, 182, 172, 80),   // connectionLineColor
        new Color(0, 121, 107),        // parentNodeColor
        new Color(77, 182, 172)        // childNodeColor
    ),
    
    SUNSET(
        new Color(255, 112, 67),       // baseColor - orange sunset
        new Color(244, 81, 30),        // hoverColor
        new Color(230, 74, 25),        // selectedColor
        Color.WHITE,                   // textColor
        new Color(255, 167, 38, 80),   // connectionLineColor
        new Color(244, 81, 30),        // parentNodeColor
        new Color(255, 167, 38)        // childNodeColor
    ),
    
    PURPLE(
        new Color(156, 39, 176),       // baseColor - violet
        new Color(123, 31, 162),       // hoverColor
        new Color(106, 27, 154),       // selectedColor
        Color.WHITE,                   // textColor
        new Color(186, 104, 200, 80),  // connectionLineColor
        new Color(123, 31, 162),       // parentNodeColor
        new Color(186, 104, 200)       // childNodeColor
    );
    
    private final Color baseColor;
    private final Color hoverColor;
    private final Color selectedColor;
    private final Color textColor;
    private final Color connectionLineColor;
    private final Color parentNodeColor;
    private final Color childNodeColor;
    
    private HTreeStyle(Color base, Color hover, Color selected, Color text, 
                       Color connectionLine, Color parentNode, Color childNode) {
        this.baseColor = base;
        this.hoverColor = hover;
        this.selectedColor = selected;
        this.textColor = text;
        this.connectionLineColor = connectionLine;
        this.parentNodeColor = parentNode;
        this.childNodeColor = childNode;
    }
    
    /**
     * Retourne la couleur de base des nœuds.
     * @return la couleur de base
     */
    public Color getBaseColor() { 
        return baseColor; 
    }
    
    /**
     * Retourne la couleur des nœuds au survol.
     * @return la couleur de survol
     */
    public Color getHoverColor() { 
        return hoverColor; 
    }
    
    /**
     * Retourne la couleur des nœuds sélectionnés.
     * @return la couleur de sélection
     */
    public Color getSelectedColor() { 
        return selectedColor; 
    }
    
    /**
     * Retourne la couleur du texte.
     * @return la couleur du texte
     */
    public Color getTextColor() { 
        return textColor; 
    }
    
    /**
     * Retourne la couleur des lignes de connexion.
     * @return la couleur des lignes
     */
    public Color getConnectionLineColor() { 
        return connectionLineColor; 
    }
    
    /**
     * Retourne la couleur des nœuds parents.
     * @return la couleur des parents
     */
    public Color getParentNodeColor() { 
        return parentNodeColor; 
    }
    
    /**
     * Retourne la couleur des nœuds enfants.
     * @return la couleur des enfants
     */
    public Color getChildNodeColor() { 
        return childNodeColor; 
    }
}