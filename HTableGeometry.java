package hsupertable.geometry;

import hsupertable.HTable;
import hsupertable.model.Cell;
import hsupertable.model.InternalGrid;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * HTableGeometry — calcul des rectangles occupés par les cellules (fusion
 * comprise) et les sous-cellules internes (subdivision comprise), et
 * hit-testing associé.
 *
 * Ne dépend d'aucune classe de rendu (HBasicTableUI) — uniquement de
 * HSuperTable et HSuperDefaultTableModel. Utilisable par n'importe quel
 * consommateur ayant besoin de connaître la géométrie d'une cellule,
 * indépendamment de la façon dont elle est peinte.
 */
public final class HTableGeometry {

    private HTableGeometry() {
    }

    /**
     * Rectangle d'une fusion, à partir des coordonnées MODÈLE de sa cellule
     * principale. dre.
     */
    public static Rectangle computeMergedRect(HTable t, int modelRow, int modelCol, int rSpan, int cSpan) {
        int minViewRow = Integer.MAX_VALUE, maxViewRow = Integer.MIN_VALUE;
        for (int dr = 0; dr < rSpan; dr++) {
            int vr = t.convertRowIndexToView(modelRow + dr);
            minViewRow = Math.min(minViewRow, vr);
            maxViewRow = Math.max(maxViewRow, vr);
        }
        int minViewCol = Integer.MAX_VALUE, maxViewCol = Integer.MIN_VALUE;
        for (int dc = 0; dc < cSpan; dc++) {
            int vc = t.convertColumnIndexToView(modelCol + dc);
            minViewCol = Math.min(minViewCol, vc);
            maxViewCol = Math.max(maxViewCol, vc);
        }
        Rectangle topLeft = t.getCellRect(minViewRow, minViewCol, false);
        Rectangle bottomRight = t.getCellRect(maxViewRow, maxViewCol, false);
        return topLeft.union(bottomRight);
    }

    /**
     * Rectangle réel occupé par (row, col), fusion prise en compte.
     *
     * NB: les rectangles et les positions restent en indices vue, mais la
     * lecture de la Cell passe par les indices modèle.
     */
    public static Rectangle getCellBounds(HTable t, int row, int col) {
        int modelRow = t.toModelRow(row);
        int modelCol = t.toModelColumn(col);
        Cell cell = t.getHModel().getCell(modelRow, modelCol);
        return (cell.spanRow > 1 || cell.spanCol > 1)
                ? computeMergedRect(t, modelRow, modelCol, cell.spanRow, cell.spanCol)
                : t.getCellRect(row, col, false);
    }

    public static Rectangle[] computeInternalRects(Rectangle rect, InternalGrid grid) {
        Rectangle first = new Rectangle(rect);
        Rectangle second = new Rectangle(rect);
        float ratio = grid.getDividerRatio();

        if (grid.getSplitType() == InternalGrid.SPLIT_VERTICAL) {
            int splitX = (int) (rect.width * ratio);
            first.width = splitX;
            second.x = rect.x + splitX;
            second.width = rect.width - splitX;
        } else {
            int splitY = (int) (rect.height * ratio);
            first.height = splitY;
            second.y = rect.y + splitY;
            second.height = rect.height - splitY;
        }
        return new Rectangle[]{first, second};
    }

    public static InternalCellHit getInternalCellAt(HTable t, Point point) {
        int[] resolved = t.resolvePoint(point);
        int row = resolved[0];
        int col = resolved[1];
        if (row < 0 || col < 0) {
            return null;
        }

        Cell cell = t.getHModel().getCell(t.toModelRow(row), t.toModelColumn(col));
        Rectangle rect = getCellBounds(t, row, col);
        return findInternalCellAt(cell, rect, point);
    }

    private static InternalCellHit findInternalCellAt(Cell cell, Rectangle rect, Point point) {
        if (cell == null || rect == null || point == null || !rect.contains(point)) {
            return null;
        }
        if (cell.internalGrid == null) {
            return new InternalCellHit(cell, rect, null);
        }

        InternalGrid grid = cell.internalGrid;
        Rectangle[] parts = computeInternalRects(rect, grid);
        Rectangle firstRect = parts[0];
        Rectangle secondRect = parts[1];

        if (firstRect.contains(point)) {
            InternalCellHit hit = findInternalCellAt(grid.getFirstCell(), firstRect, point);
            if (hit != null) {
                if (hit.parent == null) {
                    hit.parent = cell;
                }
                return hit;
            }
        }
        if (secondRect.contains(point)) {
            InternalCellHit hit = findInternalCellAt(grid.getSecondCell(), secondRect, point);
            if (hit != null) {
                if (hit.parent == null) {
                    hit.parent = cell;
                }
                return hit;
            }
        }
        return new InternalCellHit(cell, rect, null);
    }

    /**
     * Résultat d'une détection de sous-cellule interne.
     */
    public static class InternalCellHit {

        public Cell cell;
        public Cell parent;
        public Rectangle bounds;

        public InternalCellHit(Cell cell, Rectangle bounds, Cell parent) {
            this.cell = cell;
            this.bounds = bounds;
            this.parent = parent;
        }
    }
}
