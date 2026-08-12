package illustrations.shapes;

import java.awt.geom.Point2D;

/**
 * Contrat de construction géométrique d'une forme (Strategy Pattern).
 * Implémenté par le catalogue interne (ShapeType) ou par une forme
 * personnalisée fournie par l'utilisateur de la bibliothèque.
 */
public interface ShapeBuilder {

    // Construit la géométrie de base de la forme, sans ajustement particulier
    ShapeGeometry build(int x, int y, int w, int h);

    // Nombre de poignées jaunes que cette forme expose (0 par défaut : aucune)
    default int adjustmentCount() {
        return 0;
    }

    // Valeurs par défaut des ajustements, dans l'ordre de leurs index (vide par défaut)
    default double[] defaultAdjustments() {
        return new double[0];
    }

    // Construit la géométrie en tenant compte des ajustements ; ignore adjustments par défaut
    default ShapeGeometry build(int x, int y, int w, int h, double[] adjustments) {
        return build(x, y, w, h);
    }

    // Position locale (non pivotée) de la poignée jaune d'index donné ; null si aucune poignée
    default Point2D adjustmentHandlePosition(int x, int y, int w, int h, double[] adjustments, int index) {
        return null;
    }

    // Recalcule adjustments[index] en fonction d'un point de glissement local ; ne fait rien par défaut
    default void applyAdjustmentDrag(int x, int y, int w, int h, double[] adjustments, int index, int localMx, int localMy) {
    }
}
