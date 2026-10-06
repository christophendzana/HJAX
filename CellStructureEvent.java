package hsupertable.model.structure;

import java.util.EventObject;

/**
 * Changement de structure : un type et la zone affectée (coordonnées MODÈLE,
 * bornes incluses). Pour SHIFTED, lastRow / lastCol valent Integer.MAX_VALUE
 * quand le décalage concerne « tout ce qui suit ».
 */
public class CellStructureEvent extends EventObject {

    public enum Type {
        MERGED, UNMERGED, SUBDIVIDED, SUBDIVISION_REMOVED, SHIFTED, RESET
    }

    private final Type type;
    private final int firstRow;
    private final int firstCol;
    private final int lastRow;
    private final int lastCol;

    public CellStructureEvent(CellStructureModel source, Type type,
            int firstRow, int firstCol, int lastRow, int lastCol) {
        super(source);
        this.type = type;
        this.firstRow = firstRow;
        this.firstCol = firstCol;
        this.lastRow = lastRow;
        this.lastCol = lastCol;
    }

    @Override
    public CellStructureModel getSource() {
        return (CellStructureModel) super.getSource();
    }

    public Type getType() {
        return type;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getFirstCol() {
        return firstCol;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getLastCol() {
        return lastCol;
    }

    @Override
    public String toString() {
        return "HCellStructureEvent[" + type + " (" + firstRow + "," + firstCol
                + ")→(" + lastRow + "," + lastCol + ")]";
    }
}
