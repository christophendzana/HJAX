package illustrations.model;

import java.awt.geom.Point2D;

/**
 * Contrat optionnel pour un GraphicObject exposant des poignées jaunes (paramètres
 * spécifiques à sa géométrie : arrondi, épaisseur, etc.). Un objet sans paramètre
 * réglable (texte, image) n'implémente pas cette interface plutôt que de renvoyer 0 partout.
 */
public interface Adjustable {

    int adjustmentCount();

    // Position locale (non pivotée) de la poignée jaune d'index donné
    Point2D adjustmentHandlePosition(int index);

    // Recalcule le paramètre d'index donné à partir d'un point de glissement local
    void applyAdjustmentDrag(int index, int worldMx, int worldMy);
}