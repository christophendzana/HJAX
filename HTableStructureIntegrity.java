package hsupertable.geometry;

import hsupertable.HTable;
import hsupertable.model.Cell;
import hsupertable.model.HDefaultTableModel;
import hsupertable.model.MergeModel;
import hsupertable.model.MergeModel.MergeRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Garantit que les fusions restent cohérentes avec l'ordre VISUEL des lignes
 * (tri) et des colonnes (drag & drop d'en-tête). Une fusion exige des cellules
 * adjacentes : si un tri ou un déplacement rompt cette adjacence, la fusion est
 * défaite, et la subdivision de sa cellule principale avec elle.
 */
public class HTableStructureIntegrity {

    private HTableStructureIntegrity() {
    }

    /**
     * Défait TOUTES les fusions du tableau, sans exception, après un tri.
     *
     */
    public static void invalidateAllMergesOnSort(HTable t) {
        HDefaultTableModel model = t.getHModel();
        for (MergeRegion region : snapshotRegions(model.getMergeModel())) {
            breakMerge(model, region);
        }
    }

    /**
     * Défait les fusions dont les colonnes ne sont plus adjacentes à l'écran
     * (appelé après un drag & drop).
     */
    public static void enforceColumnAdjacency(HTable t) {
        HDefaultTableModel model = t.getHModel();
        for (MergeRegion region : snapshotRegions(model.getMergeModel())) {
            if (region.colSpan <= 1) {
                continue; // fusion purement verticale : sa colonne bouge comme un bloc
            }
            int[] viewCols = new int[region.colSpan];
            for (int i = 0; i < region.colSpan; i++) {
                viewCols[i] = t.convertColumnIndexToView(region.originCol + i);
            }
            if (!isContiguous(viewCols)) {
                breakMerge(model, region);
            }
        }
    }

    /**
     * Vrai si la sélection (indices VUE) forme un rectangle contigu dans les
     * données (indices MODÈLE).
     */
    public static boolean isMergeableViewRange(HTable t,
            int viewRowStart, int viewRowEnd, int viewColStart, int viewColEnd) {
        int[] modelRows = new int[viewRowEnd - viewRowStart + 1];
        for (int i = 0; i < modelRows.length; i++) {
            modelRows[i] = t.toModelRow(viewRowStart + i);
        }
        int[] modelCols = new int[viewColEnd - viewColStart + 1];
        for (int i = 0; i < modelCols.length; i++) {
            modelCols[i] = t.toModelColumn(viewColStart + i);
        }
        return isContiguous(modelRows) && isContiguous(modelCols);
    }

    /**
     * Défusionne, puis supprime la subdivision de la cellule principale si elle
     * en avait une.
     */
    private static void breakMerge(HDefaultTableModel model, MergeRegion region) {
        model.unmergeCell(region.originRow, region.originCol);
        Cell origin = model.getCell(region.originRow, region.originCol);
        if (origin.hasInternalGrid()) {
            model.removeInternalGrid(region.originRow, region.originCol);
        }
    }

    /**
     * Copie défensive : unmergeCell() modifie la liste vivante pendant le
     * parcours.
     */
    private static List<MergeRegion> snapshotRegions(MergeModel mergeModel) {
        return new ArrayList<>(mergeModel.getAllRegions());
    }

    /**
     * Vrai si les positions triées forment une suite consécutive sans trou.
     */
    private static boolean isContiguous(int[] positions) {
        int[] sorted = positions.clone();
        Arrays.sort(sorted);
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] != sorted[i - 1] + 1) {
                return false;
            }
        }
        return true;
    }
}
