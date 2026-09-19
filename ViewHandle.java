/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IllustrationShape;

import IllustrationShape.model.ViewEvent;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 *
 * @author FIDELE
 */
public interface ViewHandle {

    /**
     * Retourne la position locale de la poignée.
     */
    Point2D localPosition(int x, int y, int width, int height);

    /**
     * Indique si la poignée est située sous les coordonnées souris.
     */
    boolean contains(HShape shape, int worldMx, int worldMy);

    /**
     * Applique le déplacement de la poignée.
     */
    void drag(HShape shape, int worldMx, int worldMy);
    
    void paint(Graphics2D g, HShape shape, int x, int y, int width, int height);

    ViewEvent.Type getActionEventType();
    
}
