package IllustrationShape;

import IllustrationShape.vues.catalogue.ShapeCircle;
import IllustrationShape.vues.catalogue.ShapeElbowConnector;
import IllustrationShape.vues.catalogue.ShapeDoubleArrow;
import IllustrationShape.vues.catalogue.ShapeLineArrow;
import IllustrationShape.vues.catalogue.ShapeCurvedConnectorDoubleArrow;
import IllustrationShape.vues.catalogue.ShapeCurvedConnectorArrow;
import IllustrationShape.vues.catalogue.ShapeStraightConnectorArrow;
import IllustrationShape.vues.catalogue.ShapeSimpleLine;
import IllustrationShape.vues.catalogue.ShapeCurvedConnector;
import IllustrationShape.vues.catalogue.ShapeStraightConnectorDoubleArrow;
import IllustrationShape.vues.catalogue.ShapeElbowConnectorDoubleArrow;
import IllustrationShape.vues.catalogue.ShapeElbowConnectorArrow;
import IllustrationShape.vues.catalogue.ShapeStraightConnector;
import IllustrationShape.vues.catalogue.ShapeRectangle;
import IllustrationShape.vues.catalogue.ShapeRoundedRectangle;

public final class ShapeFactory {

    private ShapeFactory() {
    }    

    public static HShape createShapeRectangle(int x, int y, int width, int height) {
        return new ShapeRectangle(x, y, width, height);
    }

    public static HShape createShapeSimpleLine(int x, int y, int w, int h) {
        return new ShapeSimpleLine(x, y, w, h);
    }

    public static HShape createShapeLineArrow(int x, int y, int w, int h) {
        return new ShapeLineArrow(x, y, w, h);
    }

    public static HShape createShapeDoubleArrow(int x, int y, int w, int h) {
        return new ShapeDoubleArrow(x, y, w, h);
    }

    public static HShape createShapeStraightConnector(int x, int y, int w, int h) {
        return new ShapeStraightConnector(x, y, w, h);
    }

    public static HShape createShapeStraightConnectorArrow(int x, int y, int w, int h) {
        return new ShapeStraightConnectorArrow(x, y, w, h);
    }

    public static HShape createShapeStraightConnectorDoubleArrow(int x, int y, int w, int h) {
        return new ShapeStraightConnectorDoubleArrow(x, y, w, h);
    }

    public static HShape createShapeCurvedConnector(int x, int y, int w, int h) {
        return new ShapeCurvedConnector(x, y, w, h);
    }

    public static HShape createShapeCurvedConnectorArrow(int x, int y, int w, int h) {
        return new ShapeCurvedConnectorArrow(x, y, w, h);
    }

    public static HShape createShapeCurvedConnectorDoubleArrow(int x, int y, int w, int h) {
        return new ShapeCurvedConnectorDoubleArrow(x, y, w, h);
    }

    public static HShape createShapeElbowConnector(int x, int y, int w, int h) {
        return new ShapeElbowConnector(x, y, w, h);
    }

    public static HShape createShapeElbowConnectorArrow(int x, int y, int w, int h) {
        return new ShapeElbowConnectorArrow(x, y, w, h);
    }

    public static HShape createShapeElbowConnectorDoubleArrow(int x, int y, int w, int h) {
        return new ShapeElbowConnectorDoubleArrow(x, y, w, h);
    }
    
    public static HShape createShapeRoundedRectangle(int x, int y, int w, int h) {
        return new ShapeRoundedRectangle(x, y, w, h);
    }
    
    public static HShape createShapeCircle(int x, int y, int w, int h) {
        return new ShapeCircle(x, y, w, h);
    }

}
