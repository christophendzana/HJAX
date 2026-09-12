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

    public HShapeResizer() {
    }

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

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(mx, my, ox + ow - mx, oy + oh - my);
            }
        },
        TOP_CENTER {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(ox, my, ow, oy + oh - my);
            }
        },
        TOP_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w, y);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(ox, my, mx - ox, oy + oh - my);
            }
        },
        MIDDLE_LEFT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x, y + h / 2.0);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(mx, oy, ox + ow - mx, oh);
            }
        },
        MIDDLE_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w, y + h / 2.0);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(ox, oy, mx - ox, oh);
            }
        },
        BOTTOM_LEFT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x, y + h);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(mx, oy, ox + ow - mx, my - oy);
            }
        },
        BOTTOM_CENTER {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y + h);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(ox, oy, ow, my - oy);
            }
        },
        BOTTOM_RIGHT {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w, y + h);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                return new Rectangle2D.Double(ox, oy, mx - ox, my - oy);
            }
        },
        ROTATE {
            Point2D localPosition(int x, int y, int w, int h) {
                return new Point2D.Double(x + w / 2.0, y - ROTATE_HANDLE_DISTANCE);
            }

            Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
                throw new UnsupportedOperationException("ROTATE ne redimensionne pas");
            }

            void applyDrag(HShape s, int worldMx, int worldMy) {
                double cx = s.getCenterX();
                double cy = s.getCenterY();
                double angle = Math.toDegrees(Math.atan2(worldMy - cy, worldMx - cx));
                s.setRotationDegrees(angle + 90);
            }
        };

        abstract Point2D localPosition(int x, int y, int w, int h);

        /**
         * Calcule la géométrie brute (largeur/hauteur possiblement négatives) à
         * partir de l'origine du drag.
         */
        Rectangle2D.Double computeRawBounds(int ox, int oy, int ow, int oh, double mx, double my) {
            throw new UnsupportedOperationException();
        }

        void applyDrag(HShape shape, int worldMx, int worldMy) {
            throw new UnsupportedOperationException();
        }

        private static final int HANDLE_SIZE = 8;

        boolean contains(HShape shape, int worldMx, int worldMy) {
            Point2D local = localPosition(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
            Point2D world = shape.toWorld(local.getX(), local.getY());
            double half = HANDLE_SIZE / 2.0 + 2;
            return Math.abs(world.getX() - worldMx) <= half && Math.abs(world.getY() - worldMy) <= half;
        }

        /**
         * Redimensionne à partir de la géométrie D'ORIGINE (capturée une seule
         * fois au mousePressed) — jamais à partir de l'état courant, qui peut
         * déjà être corrompu par un débordement précédent.Bascule (flip)
         * proprement quand la souris dépasse le côté opposé.
         *
         * @param shape la forme à redimmensionnée
         * @param origX coordonée x au début du drag
         * @param origY
         * @param origWidth
         * @param origHeight
         * @param anchorWorldOriginal point qui doit rester fixe pendant le
         * redimensionnement.
         * @param worldMx coordonnée x actuelle de la souris dans le système
         * world.
         * @param worldMy
         */
        public void drag(HShape shape,
                int origX,
                int origY,
                int origWidth,
                int origHeight,
                Point2D anchorWorldOriginal,
                int worldMx,
                int worldMy) {

            // Si rotation on applique la logique de rotation
            if (this == ROTATE) {
                applyDrag(shape, worldMx, worldMy);
                return;
            }

            // TRES IMPORTANT: Position de la souris dans le système local de la shape.
            // Parce que si la forme est tournée de 45°
            // les coordonnées écran de la souris ne correspondent plus
            //directement aux coordonnées x/y de la forme.
            //toLocal() nous permet donc de raisonner comme si la forme n'était pas tournée.
            Point2D local = shape.toLocal(worldMx, worldMy);

            //À partir de la géométrie originale et de la position actuelle de la 
            // souris, quelle serait la nouvelle géométrie ?  
            //Géométrie brute ie peut contenir des valeurs négative par ex: si
            // poignée midde-top drag au delà de son côté opposé -> bug constaté: la shape disparaît
            Rectangle2D.Double raw = computeRawBounds(
                    origX,
                    origY,
                    origWidth,
                    origHeight,
                    local.getX(),
                    local.getY()
            );

            double x = raw.x;
            double y = raw.y;
            double w = raw.width;
            double h = raw.height;

            /*
        * On mémorise si la souris a dépassé le côté opposé.     
        * Donc si la nouvelle géométrie ressort du déppasement du côté opposé
        * L'ancre géométrique change donc de côté.
             */
            boolean flippedX = w < 0; //true la souris a dépassé côté opposée de la forme
            boolean flippedY = h < 0; // inversement

            // Normalisation horizontale.
            if (flippedX) {
                x += w;
                w = -w;
            }

            // Normalisation verticale.
            if (flippedY) {
                y += h;
                h = -h;
            }

            // Taille minimale, mais aucune limite sur la position de la souris.
            //Eviter que la forme disparaisse avec un w ou h = 0
            w = Math.max(1, w);
            h = Math.max(1, h);

            shape.setX((int) Math.round(x));
            shape.setY((int) Math.round(y));
            shape.setWidth((int) Math.round(w));
            shape.setHeight((int) Math.round(h));

            /*
        * Détermination de l'ancre APRÈS le flip.
        *
        * Si aucun flip n'a eu lieu :
        * l'ancre = côté opposé à la poignée active.
        *
        * Si un flip horizontal a eu lieu :
        * l'ancre horizontale se retrouve du côté de la poignée active.
        *
        * Même principe verticalement.
             */
            Point2D anchorLocalNow = getAnchorAfterFlip(
                    shape,
                    flippedX,
                    flippedY
            );

            Point2D anchorWorldNow = shape.toWorld(
                    anchorLocalNow.getX(),
                    anchorLocalNow.getY()
            );

            // Déplace la shape horizontalement de la différence entre la position 
            //où l'ancre devrait être et celle où elle est actuellement.
            shape.setX(shape.getX() + (int) Math.round(anchorWorldOriginal.getX() - anchorWorldNow.getX()));

            shape.setY(shape.getY() + (int) Math.round(anchorWorldOriginal.getY() - anchorWorldNow.getY()));
        }

        

        private Point2D getAnchorAfterFlip(HShape shape, boolean flippedX, boolean flippedY) {
            double left = shape.getX();
            double right = shape.getX() + shape.getWidth();
            double top = shape.getY();
            double bottom = shape.getY() + shape.getHeight();
            double centerX = shape.getX() + shape.getWidth() / 2.0;
            double centerY = shape.getY() + shape.getHeight() / 2.0;

            double anchorX;
            double anchorY;

            switch (this) {
                case TOP_LEFT -> {
                    anchorX = flippedX ? left : right;
                    anchorY = flippedY ? top : bottom;
                }
                case TOP_CENTER -> {
                    anchorX = centerX;
                    anchorY = flippedY ? top : bottom;
                }
                case TOP_RIGHT -> {
                    anchorX = flippedX ? right : left;
                    anchorY = flippedY ? top : bottom;
                }
                case MIDDLE_LEFT -> {
                    anchorX = flippedX ? left : right;
                    anchorY = centerY;
                }
                case MIDDLE_RIGHT -> {
                    anchorX = flippedX ? right : left;
                    anchorY = centerY;
                }
                case BOTTOM_LEFT -> {
                    anchorX = flippedX ? left : right;
                    anchorY = flippedY ? bottom : top;
                }
                case BOTTOM_CENTER -> {
                    anchorX = centerX;
                    anchorY = flippedY ? bottom : top;
                }
                case BOTTOM_RIGHT -> {
                    anchorX = flippedX ? right : left;
                    anchorY = flippedY ? bottom : top;
                }
                default ->
                    throw new IllegalStateException("ROTATE ne redimensionne pas.");
            }

            return new Point2D.Double(anchorX, anchorY);
        }

        public static HandleType at(HShape shape, int worldMx, int worldMy) {
            for (HandleType handle : values()) {
                if (handle.contains(shape, worldMx, worldMy)) {
                    return handle;
                }
            }
            return null;
        }

        public HandleType opposite() {
            return switch (this) {
                case TOP_LEFT ->
                    BOTTOM_RIGHT;
                case TOP_CENTER ->
                    BOTTOM_CENTER;
                case TOP_RIGHT ->
                    BOTTOM_LEFT;
                case MIDDLE_LEFT ->
                    MIDDLE_RIGHT;
                case MIDDLE_RIGHT ->
                    MIDDLE_LEFT;
                case BOTTOM_LEFT ->
                    TOP_RIGHT;
                case BOTTOM_CENTER ->
                    TOP_CENTER;
                case BOTTOM_RIGHT ->
                    TOP_LEFT;
                case ROTATE ->
                    ROTATE;
            };
        }
        
        public ViewEvent.Type getActionEventType() {
            return this == ROTATE ? ViewEvent.Type.VIEW_ROTATED : ViewEvent.Type.VIEW_RESIZED;
        }
    }
}
