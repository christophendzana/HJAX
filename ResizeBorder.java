package IllustrationShape;

import IllustrationShape.border.AbstractViewBorder;
import java.awt.*;
import java.awt.geom.*;

/**
 * Bordure spéciale : dessine et fait réagir les poignées d'édition (redimensionnement,
 * rotation, ajustements) d'une Shape sélectionnée. Contient tout ce que portait HandleType.
 */
public class ResizeBorder extends AbstractViewBorder {

    private static final double ROTATE_HANDLE_DISTANCE = 24;

    private final HViewShape shape;

    public ResizeBorder(HViewShape shape) {
        this.shape = shape;
    }

    @Override
    public void paintBorder(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(Color.GRAY);
        double centerX = x + width / 2.0;
        g.draw(new Line2D.Double(centerX, y, centerX, y - ROTATE_HANDLE_DISTANCE));

        for (HandleType handle : HandleType.values()) {
            Point2D p = handle.localPosition(x, y, width, height);
            if (handle == HandleType.ROTATE) {
                g.setColor(Color.WHITE);
                Ellipse2D circle = new Ellipse2D.Double(p.getX() - 5, p.getY() - 5, 10, 10);
                g.fill(circle); g.setColor(Color.BLACK); g.draw(circle);
            } else {
                g.setColor(Color.WHITE);
                Rectangle2D square = new Rectangle2D.Double(p.getX() - 4, p.getY() - 4, 8, 8);
                g.fill(square); g.setColor(Color.BLACK); g.draw(square);
            }
        }

        if (shape != null) {
            for (int i = 0; i < shape.adjustmentCount(); i++) {
                Point2D p = shape.adjustmentHandlePosition(i);
                if (p == null) continue;
                g.setColor(Color.YELLOW);
                Ellipse2D dot = new Ellipse2D.Double(p.getX() - 4, p.getY() - 4, 8, 8);
                g.fill(dot); g.setColor(Color.BLACK); g.draw(dot);
            }
        }
    }

    /**
     * Les 9 poignées génériques : 8 de redimensionnement + 1 de rotation.
     * Opère directement sur Shape (plus de GraphicTransform intermédiaire).
     */
    public enum HandleType {
        TOP_LEFT {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x, y); }
            void applyDrag(HViewShape s, int mx, int my) {
                int newWidth = s.getX() + s.getWidth() - mx;
                int newHeight = s.getY() + s.getHeight() - my;
                s.setX(mx); s.setY(my); s.setWidth(newWidth); s.setHeight(newHeight);
            }
        },
        TOP_CENTER {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x + w / 2.0, y); }
            void applyDrag(HViewShape s, int mx, int my) {
                int newHeight = s.getY() + s.getHeight() - my;
                s.setY(my); s.setHeight(newHeight);
            }
        },
        TOP_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x + w, y); }
            void applyDrag(HViewShape s, int mx, int my) {
                int newHeight = s.getY() + s.getHeight() - my;
                s.setWidth(mx - s.getX()); s.setY(my); s.setHeight(newHeight);
            }
        },
        MIDDLE_LEFT {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x, y + h / 2.0); }
            void applyDrag(HViewShape s, int mx, int my) {
                int newWidth = s.getX() + s.getWidth() - mx;
                s.setX(mx); s.setWidth(newWidth);
            }
        },
        MIDDLE_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x + w, y + h / 2.0); }
            void applyDrag(HViewShape s, int mx, int my) {
                s.setWidth(mx - s.getX());
            }
        },
        BOTTOM_LEFT {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x, y + h); }
            void applyDrag(HViewShape s, int mx, int my) {
                int newWidth = s.getX() + s.getWidth() - mx;
                s.setX(mx); s.setWidth(newWidth); s.setHeight(my - s.getY());
            }
        },
        BOTTOM_CENTER {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x + w / 2.0, y + h); }
            void applyDrag(HViewShape s, int mx, int my) {
                s.setHeight(my - s.getY());
            }
        },
        BOTTOM_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) { return new Point2D.Double(x + w, y + h); }
            void applyDrag(HViewShape s, int mx, int my) {
                s.setWidth(mx - s.getX()); s.setHeight(my - s.getY());
            }
        },
        ROTATE {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y - ROTATE_HANDLE_DISTANCE);
            }
            void applyDrag(HViewShape s, int worldMx, int worldMy) {
                double cx = s.getCenterX(), cy = s.getCenterY();
                double angle = Math.toDegrees(Math.atan2(worldMy - cy, worldMx - cx));
                s.setRotationDegrees(angle + 90);
            }
        };

        abstract Point2D localPosition(int x, int y, int w, int h);
        abstract void applyDrag(HViewShape shape, int mx, int my);

        private static final int HANDLE_SIZE = 8;

        boolean contains(HViewShape shape, int worldMx, int worldMy) {
            Point2D local = localPosition(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
            Point2D world = shape.toWorld(local.getX(), local.getY());
            double half = HANDLE_SIZE / 2.0 + 2;
            return Math.abs(world.getX() - worldMx) <= half && Math.abs(world.getY() - worldMy) <= half;
        }

        public void drag(HViewShape shape, int worldMx, int worldMy) {
            if (this == ROTATE) {
                applyDrag(shape, worldMx, worldMy);
            } else {
                Point2D local = shape.toLocal(worldMx, worldMy);
                applyDrag(shape, (int) local.getX(), (int) local.getY());
            }
        }

        public static HandleType at(HViewShape shape, int worldMx, int worldMy) {
            for (HandleType handle : values()) {
                if (handle.contains(shape, worldMx, worldMy)) return handle;
            }
            return null;
        }
    }
}