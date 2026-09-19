package hsplitpane;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Direction de disposition des composants dans une zone, ET axe géométrique
 * principal utilisé par HSplitPaneRootLayout / HSplitWrapLayout pour
 * factoriser leur logique horizontale/verticale (voir mainSize/crossSize/
 * mainPos — évite un second enum "Axis" séparé qui dirait la même chose).
 */
public enum WrapDirection {

    HORIZONTAL {
        @Override
        public int mainSize(Dimension d) {
            return d.width;
        }

        @Override
        public int crossSize(Dimension d) {
            return d.height;
        }

        @Override
        public int mainPos(Rectangle r) {
            return r.x;
        }
    },
    VERTICAL {
        @Override
        public int mainSize(Dimension d) {
            return d.height;
        }

        @Override
        public int crossSize(Dimension d) {
            return d.width;
        }

        @Override
        public int mainPos(Rectangle r) {
            return r.y;
        }
    };

    public abstract int mainSize(Dimension d);

    public abstract int crossSize(Dimension d);

    public abstract int mainPos(Rectangle r);
}