package hsupertable.geometry;

import hsupertable.HTable;
import hsupertable.model.MergeRegion;
import hsupertable.model.SubCellPath;

import java.util.Arrays;

/**
 * Garantit que les fusions restent cohérentes avec l'ordre VISUEL des lignes
 * (tri) et des colonnes (drag & drop d'en-tête). Une fusion exige des cellules
 * adjacentes : si un tri ou un déplacement rompt cette adjacence, la fusion est
 * défaite, et la subdivision de sa cellule principale avec elle.
 *
 * Ne connaît que HTable et sa structure : jamais le TableModel directement.
 * Toute restauration de valeurs passe par HTable.unmergeRegion(), l'unique
 * pont entre le modèle de structure et le modèle de données.
 */
public class TableStructureIntegrity {

    private TableStructureIntegrity() {
    }

    /**
     * Défait TOUTES les fusions du tableau, sans exception, après un tri.
     */
    public static void invalidateAllMergesOnSort(HTable t) {
        // getMergeRegions() est un instantané : on peut défusionner pendant le parcours
        for (MergeRegion region : t.getStructureModel().getMergeRegions()) {
            breakMerge(t, region);
        }
    }

    /**
     * Défait les fusions dont les colonnes ne sont plus adjacentes à l'écran
     * (appelé après un drag & drop).
     */
    public static void enforceColumnAdjacency(HTable t) {
        for (MergeRegion region : t.getStructureModel().getMergeRegions()) {
            if (region.colSpan <= 1) {
                continue; // fusion purement verticale : sa colonne bouge comme un bloc
            }
            int[] viewCols = new int[region.colSpan];
            for (int i = 0; i < region.colSpan; i++) {
                viewCols[i] = t.convertColumnIndexToView(region.originCol + i);
            }
            if (!isContiguous(viewCols)) {
                breakMerge(t, region);
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
    private static void breakMerge(HTable t, MergeRegion region) {
        t.unmergeRegion(region);
        if (!t.getStructureModel().getCellNode(region.originRow, region.originCol).isLeaf()) {
            t.removeSubdivision(region.originRow, region.originCol, SubCellPath.ROOT);
        }
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
