package IllustrationShape;

import IllustrationShape.model.ViewEvent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Forme spéciale dessinée au-dessus de la HShape sélectionnée, portant les
 * poignées de redimensionnement/rotation/ajustement. Remplace ResizeBorder : le
 * rendu passe par HShape.Paint() plutôt que par javax.swing.border.Border.
 *
 * Ne fait pas partie de la collection de shapes de DefaultViewModel — peinte à
 * part par l'hôte, pour ne pas être elle-même trouvée par findShapeAt().
 */
public class HShapeResizer extends HShape {

    private static final double ROTATE_HANDLE_DISTANCE = 24;

    private HShape targetShape;

    public void setTargetShape(HShape targetShape) {
        this.targetShape = targetShape;
    }

    public HShape getTargetShape() {
        return targetShape;
    }

    /**
     * Poignée sous le point donné (coordonnées monde), ou null. Appelée par
     * l'hôte lors de mousePressed pour décider du type de drag.
     */
    public HandleType handleAt(int worldMx, int worldMy) {
        return targetShape == null ? null : HandleType.at(targetShape, worldMx, worldMy);
    }

    @Override
    public void Paint(Graphics g, int px, int py) {
        if (targetShape == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform saved = g2d.getTransform();

        g2d.translate(px, py);
        g2d.rotate(Math.toRadians(targetShape.getRotationDegrees()),
                targetShape.getCenterX(), targetShape.getCenterY());

        int x = targetShape.getX();
        int y = targetShape.getY();
        int width = targetShape.getWidth();
        int height = targetShape.getHeight();

        g2d.setColor(Color.GRAY);
        g2d.drawRect(x, y, width, height);

        double centerX = x + width / 2.0;
        g2d.draw(new Line2D.Double(centerX, y, centerX, y - ROTATE_HANDLE_DISTANCE));

        for (HandleType handle : HandleType.values()) {
            Point2D p = handle.localPosition(x, y, width, height);
            if (handle == HandleType.ROTATE) {
                g2d.setColor(Color.WHITE);
                Ellipse2D circle = new Ellipse2D.Double(p.getX() - 5, p.getY() - 5, 10, 10);
                g2d.fill(circle);
                g2d.setColor(Color.BLACK);
                g2d.draw(circle);
            } else {
                g2d.setColor(Color.WHITE);
                Rectangle2D square = new Rectangle2D.Double(p.getX() - 4, p.getY() - 4, 8, 8);
                g2d.fill(square);
                g2d.setColor(Color.BLACK);
                g2d.draw(square);
            }
        }

        for (int i = 0; i < targetShape.adjustmentCount(); i++) {
            Point2D p = targetShape.adjustmentHandlePosition(i);
            if (p == null) {
                continue;
            }
            g2d.setColor(Color.YELLOW);
            Ellipse2D dot = new Ellipse2D.Double(p.getX() - 4, p.getY() - 4, 8, 8);
            g2d.fill(dot);
            g2d.setColor(Color.BLACK);
            g2d.draw(dot);
        }

        g2d.setTransform(saved);
    }

    /**
     * Les 9 poignées génériques : 8 de redimensionnement + 1 de rotation.
     */
    public enum HandleType {
        TOP_LEFT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x, y);
            }

            void applyDrag(HShape s, int mx, int my) {
                int newWidth = s.getX() + s.getWidth() - mx;
                int newHeight = s.getY() + s.getHeight() - my;
                s.setX(mx);
                s.setY(my);
                s.setWidth(newWidth);
                s.setHeight(newHeight);
            }
        },
        TOP_CENTER {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y);
            }

            void applyDrag(HShape s, int mx, int my) {
                int newHeight = s.getY() + s.getHeight() - my;
                s.setY(my);
                s.setHeight(newHeight);
            }
        },
        TOP_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w, y);
            }

            void applyDrag(HShape s, int mx, int my) {
                int newHeight = s.getY() + s.getHeight() - my;
                s.setWidth(mx - s.getX());
                s.setY(my);
                s.setHeight(newHeight);
            }
        },
        MIDDLE_LEFT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x, y + h / 2.0);
            }

            void applyDrag(HShape s, int mx, int my) {
                int newWidth = s.getX() + s.getWidth() - mx;
                s.setX(mx);
                s.setWidth(newWidth);
            }
        },
        MIDDLE_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w, y + h / 2.0);
            }

            void applyDrag(HShape s, int mx, int my) {
                s.setWidth(mx - s.getX());
            }
        },
        BOTTOM_LEFT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x, y + h);
            }

            void applyDrag(HShape s, int mx, int my) {
                int newWidth = s.getX() + s.getWidth() - mx;
                s.setX(mx);
                s.setWidth(newWidth);
                s.setHeight(my - s.getY());
            }
        },
        BOTTOM_CENTER {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y + h);
            }

            void applyDrag(HShape s, int mx, int my) {
                s.setHeight(my - s.getY());
            }
        },
        BOTTOM_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w, y + h);
            }

            void applyDrag(HShape s, int mx, int my) {
                s.setWidth(mx - s.getX());
                s.setHeight(my - s.getY());
            }
        },
        ROTATE {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y - ROTATE_HANDLE_DISTANCE);
            }

            void applyDrag(HShape s, int worldMx, int worldMy) {
                double cx = s.getCenterX();
                double cy = s.getCenterY();
                double angle = Math.toDegrees(Math.atan2(worldMy - cy, worldMx - cx));
                s.setRotationDegrees(angle + 90);
            }
        };

        abstract Point2D localPosition(int x, int y, int w, int h);

        abstract void applyDrag(HShape shape, int mx, int my);

        private static final int HANDLE_SIZE = 8;

        boolean contains(HShape shape, int worldMx, int worldMy) {
            Point2D local = localPosition(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
            Point2D world = shape.toWorld(local.getX(), local.getY());
            double half = HANDLE_SIZE / 2.0 + 2;
            return Math.abs(world.getX() - worldMx) <= half && Math.abs(world.getY() - worldMy) <= half;
        }

        /**
         * Calcule et applique le déplacement à la forme cible, à partir de
         * coordonnées souris en repère monde. L'hôte appelle cette méthode
         * après avoir identifié la poignée active (hit-test), puis notifie
         * lui-même le DefaultViewModel du type d'événement correspondant.
         */
        public void drag(HShape shape, int worldMx, int worldMy) {
            if (this == ROTATE) {
                applyDrag(shape, worldMx, worldMy);
            } else {
                Point2D local = shape.toLocal(worldMx, worldMy);
                applyDrag(shape, (int) local.getX(), (int) local.getY());
            }
        }

        public static HandleType at(HShape shape, int worldMx, int worldMy) {
            for (HandleType handle : values()) {
                if (handle.contains(shape, worldMx, worldMy)) {
                    return handle;
                }
            }
            return null;
        }

        /**
         * Indique à l'hôte quelle méthode appeler sur DefaultViewModel après un
         * drag.
         */
        public ViewEvent.Type getActionEventType() {
            return this == ROTATE ? ViewEvent.Type.VIEW_ROTATED : ViewEvent.Type.VIEW_RESIZED;
        }
    }
}
