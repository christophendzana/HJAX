/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package IllustrationShape.border;

import java.awt.Graphics2D;
import java.awt.Insets;

/**
 *
 * @author FIDELE
 */
public interface ViewBorder {
    
   // Dessine la bordure dans le rectangle (x, y, width, height) donné
    void paintBorder(Graphics2D g, int x, int y, int width, int height);

    // Espace réservé par la bordure autour du contenu (haut, gauche, bas, droite)
    Insets getBorderInsets(int width, int height);

    // Indique si la bordure couvre entièrement la zone qu'elle occupe, sans transparence
    boolean isBorderOpaque();
    
    
}
