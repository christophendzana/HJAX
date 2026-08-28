package IllustrationShape;

import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Racine commune de toute vue dessinable : une forme, un arrière-plan, une bordure —
 * ou une combinaison des trois. Chaque sous-classe décide de ce qu'elle implémente réellement.
 */
public abstract class HView {

    // Dessine le contenu principal de la vue (la forme elle-même) à la position donnée
    public abstract void Paint(Graphics g, int x, int y);

    // Dessine l'arrière-plan de la vue dans le rectangle donné
    public abstract void paintBackground(Graphics g, Rectangle rect);

    // Dessine la bordure de la vue, en s'appuyant sur ses propres coordonnées internes
    public abstract void paintBorder(Graphics g);
}