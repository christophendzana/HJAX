/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package hcomponents.vues;

import java.awt.Dimension;

/**
 *
 * @author FIDELE
 */
public enum HButtonSize {
    
    SMALL(80, 30, 12),    // Petits boutons
    MEDIUM(120, 40, 14),  // Taille par défaut
    LARGE(160, 50, 16),   // Grands boutons
    BLOCK(-1, 40, 14);    // S'étire sur toute la largeur
    
    private final int preferredWidth;
    private final int preferredHeight;
    private final int fontSize;
    
    private HButtonSize(int width, int height, int fontSize) {
        this.preferredWidth = width;
        this.preferredHeight = height;
        this.fontSize = fontSize;
    }
    
    public Dimension getPreferredSize() {
        if (preferredWidth == -1) {
            return new Dimension(100, preferredHeight); // Largeur flexible
        }
        return new Dimension(preferredWidth, preferredHeight);
    }
    
    public int getFontSize() { return fontSize; }
    
}
