package illustrations.shapes;

import illustrations.model.GraphicTransform;
import java.awt.geom.Point2D;

/**
 * Poignées génériques de redimensionnement et de rotation, agissant uniquement
 * sur un GraphicTransform — aucune dépendance vers Shapes ni vers une forme
 * concrète.
 */
public enum HandleType {

    TOP_LEFT {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX(), t.getY());
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            int newWidth = t.getX() + t.getWidth() - mx;
            int newHeight = t.getY() + t.getHeight() - my;
            t.setX(mx);
            t.setY(my);
            t.setWidth(newWidth);
            t.setHeight(newHeight);
        }
    },
    TOP_CENTER {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX() + t.getWidth() / 2.0, t.getY());
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            int newHeight = t.getY() + t.getHeight() - my;
            t.setY(my);
            t.setHeight(newHeight);
        }
    },
    TOP_RIGHT {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX() + t.getWidth(), t.getY());
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            int newHeight = t.getY() + t.getHeight() - my;
            t.setWidth(mx - t.getX());
            t.setY(my);
            t.setHeight(newHeight);
        }
    },
    MIDDLE_LEFT {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX(), t.getY() + t.getHeight() / 2.0);
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            int newWidth = t.getX() + t.getWidth() - mx;
            t.setX(mx);
            t.setWidth(newWidth);
        }
    },
    MIDDLE_RIGHT {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX() + t.getWidth(), t.getY() + t.getHeight() / 2.0);
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            t.setWidth(mx - t.getX());
        }
    },
    BOTTOM_LEFT {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX(), t.getY() + t.getHeight());
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            int newWidth = t.getX() + t.getWidth() - mx;
            t.setX(mx);
            t.setWidth(newWidth);
            t.setHeight(my - t.getY());
        }
    },
    BOTTOM_CENTER {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX() + t.getWidth() / 2.0, t.getY() + t.getHeight());
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            t.setHeight(my - t.getY());
        }
    },
    BOTTOM_RIGHT {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX() + t.getWidth(), t.getY() + t.getHeight());
        }

        void applyDrag(GraphicTransform t, int mx, int my) {
            t.setWidth(mx - t.getX());
            t.setHeight(my - t.getY());
        }
    },
    ROTATE {
        Point2D localPosition(GraphicTransform t) {
            return new Point2D.Double(t.getX() + t.getWidth() / 2.0, t.getY() - ROTATE_HANDLE_DISTANCE);
        }

        void applyDrag(GraphicTransform t, int worldMx, int worldMy) {
            double cx = t.getCenterX(), cy = t.getCenterY();
            double angle = Math.toDegrees(Math.atan2(worldMy - cy, worldMx - cx));
            t.setRotationDegrees(angle + 90); // +90 : la poignée part du haut (-90°), pas de la droite (0°)
        }
    };

    // Position locale (non pivotée) de la poignée, selon l'état actuel du transform
    abstract Point2D localPosition(GraphicTransform t);

    // Recalcule le transform à partir d'une position de glissement écran (world)
    abstract void applyDrag(GraphicTransform t, int worldMx, int worldMy);

    private static final int HANDLE_SIZE = 8;
    private static final double ROTATE_HANDLE_DISTANCE = 24;

    // Détecte si un point écran (world) touche cette poignée, en tenant compte de la rotation
    boolean contains(GraphicTransform t, int worldMx, int worldMy) {
        Point2D local = localPosition(t);
        Point2D world = t.toWorld(local.getX(), local.getY());
        double half = HANDLE_SIZE / 2.0 + 2; // +2 : marge de tolérance au clic
        return Math.abs(world.getX() - worldMx) <= half && Math.abs(world.getY() - worldMy) <= half;
    }

    // Convertit la souris (world) en local avant d'appliquer le glissement à ce type de poignée
    void drag(GraphicTransform t, int worldMx, int worldMy) {
        if (this == ROTATE) {
            applyDrag(t, worldMx, worldMy); // la rotation raisonne directement en coordonnées écran
        } else {
            Point2D local = t.toLocal(worldMx, worldMy);
            applyDrag(t, (int) local.getX(), (int) local.getY());
        }
    }

    // Trouve la poignée touchée par un clic écran (world), ou null si aucune
    public static HandleType at(GraphicTransform t, int worldMx, int worldMy) {
        for (HandleType handle : values()) {
            if (handle.contains(t, worldMx, worldMy)) {
                return handle;
            }
        }
        return null;
    }
}
