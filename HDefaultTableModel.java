package hsupertable.model;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * HDefaultTableModel — Modèle de données étendu pour HSuperTable.
 *
 * @author FIDELE
 * @version 3.0
 */
public class HDefaultTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L; // Obligatoire quand on implémente serializable

    /**
     * Unité de base de la grille. Toujours synchronisée avec les données de
     * DefaultTableModel.
     */
    private Cell[][] grid;
    private MergeModel mergeModel;

    private final Map<Integer, Class<?>> declaredColumnClasses = new HashMap<>();

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    public HDefaultTableModel() {
        super();
        initGrid(0, 0);
    }

    public HDefaultTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
        initGrid(rowCount, columnCount);
    }

    public HDefaultTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        initGrid(data.length, columnNames.length);
    }

    public HDefaultTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
        super(data, columnNames);
        initGrid(data.size(), columnNames.size());
    }

    public HDefaultTableModel(Object[] columnNames) {
        super(columnNames, 0);
        initGrid(0, columnNames.length);
    }

    public HDefaultTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        initGrid(rowCount, columnNames.length);
    }

    public HDefaultTableModel(Vector<String> columnNames, int rowCount) {
        super(columnNames, rowCount);
        initGrid(rowCount, columnNames.size());
    }

    // =========================================================================
    // INITIALISATION ET REDIMENSIONNEMENT
    // =========================================================================
    /**
     * Crée une grille neuve, toutes les cellules sont normales par défaut.
     */
    private void initGrid(int rows, int cols) {
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
        mergeModel = new MergeModel(rows, cols);
    }

    /**
     * Déclare explicitement le type d'une colonne, prioritaire sur l'inférence
     * automatique. Nécessaire pour qu'un renderer/editor personnalisé
     * enregistré via setDefaultRenderer/setDefaultEditor reste résolu même si
     * la colonne est vide (aucune valeur non nulle à inférer) au moment du
     * rendu.
     */
    public void setColumnClass(int columnIndex, Class<?> columnClass) {
        if (columnClass == null) {
            declaredColumnClasses.remove(columnIndex);
        } else {
            declaredColumnClasses.put(columnIndex, columnClass);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class<?> declared = declaredColumnClasses.get(columnIndex);
        if (declared != null) {
            return declared;
        }
        for (int row = 0; row < getRowCount(); row++) {
            Object value = getValueAt(row, columnIndex);
            if (value != null) {
                return value.getClass();
            }
        }
        return Object.class;
    }

    /**
     * Accès en lecture pour les prochaines étapes (contrôleur, UI, tests).
     */
    public MergeModel getMergeModel() {
        return mergeModel;
    }

    /**
     * Redimensionne la grille en conservant les cellules existantes. Les
     * nouvelles cellules sont normales, les cellules supprimées sont perdues.
     */
    private void resizeGrid(int newRows, int newCols) {
        int oldRows = (grid != null) ? grid.length : 0;
        int oldCols = (grid != null && oldRows > 0) ? grid[0].length : 0;

        Cell[][] newGrid = new Cell[newRows][newCols];
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if (r < oldRows && c < oldCols && grid[r][c] != null) {
                    newGrid[r][c] = grid[r][c];
                } else {
                    newGrid[r][c] = new Cell();
                }
            }
        }
        grid = newGrid;
        mergeModel.resizeKeepingRegions(newRows, newCols);
        syncAllSpanFields();
    }

    /**
     * Synchronise spanRow/spanCol/mergeOrigin de chaque Cell avec l'état réel
     * de MergeModel. Maintenu uniquement pour compatibilité avec le code qui
     * accède encore directement à ces champs (HSuperTableController,
     * HBasicTableUI). MergeModel reste la seule source de vérité — ces champs
     * ne sont plus jamais écrits ailleurs que dans cette méthode.
     *
     * À supprimer quand le contrôleur et l'UI interrogeront getMergeModel()
     * directement au lieu des champs de Cell.
     */
    private void syncAllSpanFields() {
        int rows = getRowCount();
        int cols = getColumnCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!isValidCell(r, c)) {
                    continue;
                }
                Cell cell = grid[r][c];
                MergeModel.MergeRegion region = mergeModel.getMergeAt(r, c);
                if (region == null) {
                    cell.spanRow = 1;
                    cell.spanCol = 1;
                    cell.mergeOrigin = null;
                } else if (region.originRow == r && region.originCol == c) {
                    cell.spanRow = region.rowSpan;
                    cell.spanCol = region.colSpan;
                    cell.mergeOrigin = null;
                } else {
                    cell.spanRow = 0;
                    cell.spanCol = 0;
                    cell.mergeOrigin = new Point(region.originRow, region.originCol);
                }
            }
        }
    }

    // =========================================================================
    // SURCHARGE DES MÉTHODES STRUCTURELLES
    // =========================================================================
    @Override
    public void setRowCount(int rowCount) {
        super.setRowCount(rowCount);
        resizeGrid(rowCount, getColumnCount());
    }

    @Override
    public void setColumnCount(int columnCount) {
        super.setColumnCount(columnCount);
        resizeGrid(getRowCount(), columnCount);
    }

    @Override
    public void addRow(Vector rowData) {
        super.addRow(rowData);
        resizeGrid(getRowCount(), getColumnCount());
    }

    @Override
    public void addRow(Object[] rowData) {
        super.addRow(rowData);
        resizeGrid(getRowCount(), getColumnCount());
    }

    @Override
    public void insertRow(int row, Vector rowData) {
        super.insertRow(row, rowData);
        shiftGridDown(row);
    }

    @Override
    public void insertRow(int row, Object[] rowData) {
        super.insertRow(row, rowData);
        shiftGridDown(row);
    }

    @Override
    public void removeRow(int row) {
        defuseRow(row);
        super.removeRow(row);
        shiftGridUp(row);
        mergeModel.onRowRemoved(row, getRowCount(), getColumnCount());
        syncAllSpanFields();
    }

    /**
     * Décale la grille Cell[][] d'une ligne vers le haut pour refléter la
     * suppression de removedRow dans DefaultTableModel. Corrige un bug
     * préexistant : resizeGrid() seul ne faisait que tronquer la fin de la
     * grille sans jamais combler le trou laissé par la ligne supprimée.
     */
    private void shiftGridUp(int removedRow) {
        int newRows = getRowCount(); // déjà décrémenté par super.removeRow
        int cols = getColumnCount();
        Cell[][] oldGrid = grid;
        Cell[][] newGrid = new Cell[newRows][cols];

        for (int r = 0; r < newRows; r++) {
            int sourceRow = (r < removedRow) ? r : r + 1;
            for (int c = 0; c < cols; c++) {
                newGrid[r][c] = (sourceRow < oldGrid.length && c < oldGrid[sourceRow].length)
                        ? oldGrid[sourceRow][c] : new Cell();
            }
        }
        grid = newGrid;
    }

    private void shiftGridDown(int insertedRow) {
        int rows = getRowCount(); // déjà incrémenté par super.insertRow
        int cols = getColumnCount();
        Cell[][] oldGrid = grid;
        Cell[][] newGrid = new Cell[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r < insertedRow) {
                    newGrid[r][c] = (r < oldGrid.length && c < oldGrid[r].length)
                            ? oldGrid[r][c] : new Cell();
                } else if (r == insertedRow) {
                    newGrid[r][c] = new Cell();
                } else {
                    int sourceRow = r - 1;
                    newGrid[r][c] = (sourceRow < oldGrid.length && c < oldGrid[sourceRow].length)
                            ? oldGrid[sourceRow][c] : new Cell();
                }
            }
        }
        grid = newGrid;

        mergeModel.onRowInserted(insertedRow, rows, cols);
        syncAllSpanFields();
    }

    /**
     * Défusionne proprement toutes les fusions qui impliquent la ligne row.
     * Appelé avant removeRow() pour ne pas laisser d'orphelines.
     */
    private void defuseRow(int row) {
        if (!isValidRow(row)) {
            return;
        }
        for (int c = 0; c < getColumnCount(); c++) {
            MergeModel.MergeRegion region = mergeModel.getMergeAt(row, c);
            if (region != null) {
                unmergeCell(region.originRow, region.originCol);
            }
        }
    }

    /**
     * Défusionne proprement toutes les fusions qui impliquent la colonne col.
     * Appelé avant removeColumn().
     */
    private void defuseColumn(int col) {
        if (col < 0 || col >= getColumnCount()) {
            return;
        }
        for (int r = 0; r < getRowCount(); r++) {
            MergeModel.MergeRegion region = mergeModel.getMergeAt(r, col);
            if (region != null) {
                unmergeCell(region.originRow, region.originCol);
            }
        }
    }

    // =========================================================================
    // GESTION DES COLONNES
    // =========================================================================
    /**
     * Ajoute une colonne vide à droite du tableau.
     *
     * @param columnName nom de la nouvelle colonne
     */
    public void addColumn(String columnName) {
        super.addColumn(columnName);
        resizeGrid(getRowCount(), getColumnCount());
    }

    /**
     * Insère une colonne vide à la position colIndex.
     *
     * CORRECTION v3 : on n'utilise plus setDataVector() qui réinitialisait les
     * listeners et la sélection Swing. On décale les données directement dans
     * le Vector interne de DefaultTableModel.
     *
     * @param colIndex position d'insertion (0 = tout à gauche)
     * @param columnName nom de la nouvelle colonne
     */
    public void insertColumn(int colIndex, String columnName) {
        int rows = getRowCount();
        int oldCols = getColumnCount();

        if (colIndex < 0 || colIndex > oldCols) {
            throw new IndexOutOfBoundsException("Index de colonne invalide : " + colIndex);
        }

        // ── 1. Sauvegarder toutes les données actuelles ───────────────────────
        Object[][] oldData = getAllData();

        // ── 2. Construire les nouveaux noms de colonnes avec le nom inséré ────
        Vector<String> newColNames = new Vector<>(oldCols + 1);
        for (int c = 0; c < oldCols + 1; c++) {
            if (c < colIndex) {
                newColNames.add(getColumnName(c));
            } else if (c == colIndex) {
                newColNames.add(columnName);
            } else {
                newColNames.add(getColumnName(c - 1));
            }
        }

        // ── 3. Construire les nouvelles données avec la colonne vide insérée ──
        Vector<Vector<Object>> newData = new Vector<>(rows);
        for (int r = 0; r < rows; r++) {
            Vector<Object> newRow = new Vector<>(oldCols + 1);
            for (int c = 0; c < oldCols + 1; c++) {
                if (c < colIndex) {
                    newRow.add(oldData[r][c]);
                } else if (c == colIndex) {
                    newRow.add(null);           // colonne vide
                } else {
                    newRow.add(oldData[r][c - 1]);
                }
            }
            newData.add(newRow);
        }

        // ── 4. Appliquer — setDataVector notifie les listeners correctement ───
        setDataVector(newData, newColNames);

        // ── 5. Mettre à jour la grille Cell[][] ───────────────────────────────
        resizeGrid(rows, getColumnCount());
        shiftGridRight(colIndex);
    }

    /**
     * Décale les cellules de la grille d'une colonne vers la droite à partir de
     * insertedCol. La colonne insérée reçoit des cellules vierges.
     */
    private void shiftGridRight(int insertedCol) {
        int rows = getRowCount();
        int cols = getColumnCount();

        for (int r = 0; r < rows; r++) {
            for (int c = cols - 1; c > insertedCol; c--) {
                grid[r][c] = grid[r][c - 1];
            }
            grid[r][insertedCol] = new Cell();
        }

        mergeModel.onColumnInserted(insertedCol, rows, cols);
        syncAllSpanFields();
    }

    /**
     * Supprime la colonne à l'index colIndex.
     *
     * @param colIndex index de la colonne à supprimer
     */
    public void removeColumn(int colIndex) {
        int rows = getRowCount();
        int oldCols = getColumnCount();

        if (colIndex < 0 || colIndex >= oldCols) {
            throw new IndexOutOfBoundsException("Index de colonne invalide : " + colIndex);
        }

        //Défusionner toute les fusion qui implique cette colonne avant de supprimer
        defuseColumn(colIndex);

        // 1. Sauvegarder les données 
        Object[][] oldData = getAllData();

        // 2. Nouveaux noms sans la colonne supprimée 
        Vector<String> newColNames = new Vector<>(oldCols - 1);
        for (int c = 0; c < oldCols; c++) {
            if (c != colIndex) {
                newColNames.add(getColumnName(c));
            }
        }

        //  3. Nouvelles données sans la colonne supprimée 
        Vector<Vector<Object>> newData = new Vector<>(rows);
        for (int r = 0; r < rows; r++) {
            Vector<Object> newRow = new Vector<>(oldCols - 1);
            for (int c = 0; c < oldCols; c++) {
                if (c != colIndex) {
                    newRow.add(oldData[r][c]);
                }
            }
            newData.add(newRow);
        }

        // 4. Appliquer 
        setDataVector(newData, newColNames);

        // ── 5. Mettre à jour la grille 
        shiftGridLeft(colIndex);
        resizeGrid(rows, getColumnCount());
    }

    /**
     * Décale les cellules de la grille vers la gauche à partir de removedCol.
     */
    private void shiftGridLeft(int removedCol) {
        int rows = getRowCount();
        int newCols = getColumnCount(); // déjà réduit par setDataVector

        for (int r = 0; r < rows; r++) {
            for (int c = removedCol; c < newCols; c++) {
                grid[r][c] = grid[r][c + 1];
            }
        }

        mergeModel.onColumnRemoved(removedCol, rows, newCols);
        syncAllSpanFields();
    }

    // =========================================================================
    // ACCÈS À LA GRILLE
    // =========================================================================
    /**
     * Retourne la Cell à la position (row, col).Ne retourne jamais null.
     *
     * @param row
     * @param col
     * @return
     */
    public Cell getCell(int row, int col) {
        if (!isValidCell(row, col)) {
            return new Cell();
        }
        if (grid[row][col] == null) {
            grid[row][col] = new Cell();
        }
        return grid[row][col];
    }

    /**
     * Raccourci pour accéder au HSuperTableCellModel d'une cellule. Utilisé
     * intensivement dans HBasicTableUI.
     */
    public HCellModel getCellModel(int row, int col) {
        return getCell(row, col).style;
    }

    // =========================================================================
    // ACCÈS AUX MÉTADONNÉES — raccourcis pour HSuperTable
    // =========================================================================
    public void setCellBackground(int row, int col, Color color) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.setBackground(color);
        }
    }

    public Color getCellBackground(int row, int col) {
        return isValidCell(row, col) ? getCell(row, col).style.getBackground() : null;
    }

    public void setCellForeground(int row, int col, Color color) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.setForeground(color);
        }
    }

    public Color getCellForeground(int row, int col) {
        return isValidCell(row, col) ? getCell(row, col).style.getForeground() : null;
    }

    public void setCellAlignment(int row, int col, int hAlign, int vAlign) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.setAlignment(hAlign, vAlign);
        }
    }

    public void setCellMargins(int row, int col, Insets margins) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.setMargins(margins);
        }
    }

    public void setCellTextDirection(int row, int col, int direction) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.setTextDirection(direction);
        }
    }

    public void setCellBorderSide(int row, int col, int side,
            Color color, float thickness, int style) {
        if (!isValidCell(row, col)) {
            return;
        }
        HCellModel m = getCell(row, col).style;
        if ((side & 0b0001) != 0) {
            m.setBorderTopColor(color);
            m.setBorderTopThickness(thickness);
            m.setBorderTopStyle(style);
        }
        if ((side & 0b0010) != 0) {
            m.setBorderBottomColor(color);
            m.setBorderBottomThickness(thickness);
            m.setBorderBottomStyle(style);
        }
        if ((side & 0b0100) != 0) {
            m.setBorderLeftColor(color);
            m.setBorderLeftThickness(thickness);
            m.setBorderLeftStyle(style);
        }
        if ((side & 0b1000) != 0) {
            m.setBorderRightColor(color);
            m.setBorderRightThickness(thickness);
            m.setBorderRightStyle(style);
        }
    }

    public void clearCellBorders(int row, int col) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.clearAllBorders();
        }
    }

    public void resetCellFormatting(int row, int col) {
        if (isValidCell(row, col)) {
            getCell(row, col).style.reset();
        }
    }

    // =========================================================================
    // GESTION DES SPANS (FUSION) — VERSION CORRIGÉE
    // =========================================================================
    /**
     * Retourne [spanRow, spanCol] de la cellule (row, col).
     *
     * @param row
     * @param col
     * @return
     */
    public int[] getSpan(int row, int col) {
        if (!isValidCell(row, col)) {
            return new int[]{1, 1};
        }
        MergeModel.MergeRegion region = mergeModel.getMergeAt(row, col);
        return region == null ? new int[]{1, 1} : new int[]{region.rowSpan, region.colSpan};
    }

    /**
     * Vrai si absorbée — le renderer doit sauter cette cellule.
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isAbsorbed(int row, int col) {
        return isValidCell(row, col) && mergeModel.isAbsorbed(row, col);
    }

    /**
     * Vrai si principale d'une fusion.
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isMergedCell(int row, int col) {
        return isValidCell(row, col) && mergeModel.isMergeOrigin(row, col);
    }

    /**
     * Retourne l'origine de la fusion en O(1).Lecture directe de
     * Cell.mergeOrigin — plus de scan O(n×m).
     *
     * @param row
     * @param col
     * @return Point(row, col) de la principale, ou null si non absorbée.
     */
    public Point findMergeOrigin(int row, int col) {
        if (!isValidCell(row, col)) {
            return null;
        }
        MergeModel.MergeRegion region = mergeModel.getMergeAt(row, col);
        if (region == null || (region.originRow == row && region.originCol == col)) {
            return null;
        }
        return new Point(region.originRow, region.originCol);
    }

    /**
     * Fusionne les cellules dans la zone (r1,c1) → (r2,c2). avant de poser la
     * nouvelle fusion, on détecte TOUTES les fusions qui intersectent la zone
     * et on les défusionne proprement.
     *
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     */
    public void mergeCells(int r1, int c1, int r2, int c2) {
        int rowStart = Math.min(r1, r2), rowEnd = Math.max(r1, r2);
        int colStart = Math.min(c1, c2), colEnd = Math.max(c1, c2);

        if (!isValidCell(rowStart, colStart) || !isValidCell(rowEnd, colEnd)) {
            return;
        }
        if (rowStart == rowEnd && colStart == colEnd) {
            return;
        }

        // MergeModel détecte et retire lui-même les fusions existantes qui
        // chevauchent la zone cible — collectIntersectingMerges() n'est plus
        // nécessaire.
        List<MergeModel.MergeRegion> overridden = mergeModel.merge(rowStart, colStart, rowEnd, colEnd);
        for (MergeModel.MergeRegion region : overridden) {
            restoreValuesFromRegion(region);
        }

        Cell principal = grid[rowStart][colStart];
        int rSpan = rowEnd - rowStart + 1;
        int cSpan = colEnd - colStart + 1;
        principal.mergedValues = new Object[rSpan][cSpan];

        StringBuilder merged = new StringBuilder();
        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                Object val = getValueAt(r, c);
                principal.mergedValues[r - rowStart][c - colStart] = val;
                if (val != null && !val.toString().trim().isEmpty()) {
                    if (merged.length() > 0) {
                        merged.append(" ");
                    }
                    merged.append(val.toString().trim());
                }
            }
        }

        setValueAt(merged.length() > 0 ? merged.toString() : null, rowStart, colStart);

        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                if (r == rowStart && c == colStart) {
                    continue;
                }
                setValueAt(null, r, c);
            }
        }

        syncAllSpanFields();
    }

    /**
     * Redistribue les valeurs individuelles sauvegardées d'une région fusionnée
     * vers ses cellules. Utilisé par unmergeCell() et par mergeCells() quand
     * une fusion existante est écrasée par une nouvelle zone.
     */
    private void restoreValuesFromRegion(MergeModel.MergeRegion region) {
        if (!isValidCell(region.originRow, region.originCol)) {
            return;
        }
        Cell origin = grid[region.originRow][region.originCol];
        Object[][] savedValues = origin.mergedValues;

        for (int dr = 0; dr < region.rowSpan; dr++) {
            for (int dc = 0; dc < region.colSpan; dc++) {
                int r = region.originRow + dr;
                int c = region.originCol + dc;
                if (!isValidCell(r, c)) {
                    continue;
                }
                if (savedValues != null && dr < savedValues.length && dc < savedValues[dr].length) {
                    setValueAt(savedValues[dr][dc], r, c);
                } else if (!(dr == 0 && dc == 0)) {
                    setValueAt(null, r, c);
                }
            }
        }
        origin.mergedValues = null;
    }

    public void unmergeCell(int row, int col) {
        if (!isValidCell(row, col)) {
            return;
        }
        MergeModel.MergeRegion region = mergeModel.getMergeAt(row, col);
        if (region == null) {
            return;
        }
        mergeModel.unmerge(region.originRow, region.originCol);
        restoreValuesFromRegion(region);
        syncAllSpanFields();
    }

    public boolean splitCell(int row, int col, int targetRows, int targetCols) {
        if (!isValidCell(row, col)) {
            return false;
        }

        MergeModel.MergeRegion region = mergeModel.getMergeAt(row, col);
        if (region == null) {
            return false;
        }

        if (region.rowSpan % targetRows != 0 || region.colSpan % targetCols != 0) {
            return false;
        }

        int r = region.originRow, c = region.originCol;
        int blockR = region.rowSpan / targetRows;
        int blockC = region.colSpan / targetCols;

        unmergeCell(r, c);

        if (blockR > 1 || blockC > 1) {
            for (int dr = 0; dr < targetRows; dr++) {
                for (int dc = 0; dc < targetCols; dc++) {
                    int startR = r + dr * blockR;
                    int startC = c + dc * blockC;
                    mergeCells(startR, startC, startR + blockR - 1, startC + blockC - 1);
                }
            }
        }
        return true;
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES
    // =========================================================================
    public void addEmptyRow() {
        Vector<Object> row = new Vector<>();
        for (int i = 0; i < getColumnCount(); i++) {
            row.add(null);
        }
        addRow(row);
    }

    public void insertEmptyRow(int row) {
        Vector<Object> emptyRow = new Vector<>();
        for (int i = 0; i < getColumnCount(); i++) {
            emptyRow.add(null);
        }
        insertRow(row, emptyRow);
    }

    /**
     * Déplace une ligne — données ET cellules bougent ensemble.
     *
     * @param fromIndex
     * @param toIndex
     */
    public void moveRow(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            return;
        }
        if (!isValidRow(fromIndex) || !isValidRow(toIndex)) {
            return;
        }

        // Sauvegarder la ligne source
        int cols = getColumnCount();
        Cell[] saved = new Cell[cols];
        for (int c = 0; c < cols; c++) {
            saved[c] = grid[fromIndex][c].copy();
        }

        // Déplacer les données dans DefaultTableModel
        Vector<?> rowData = (Vector<?>) getDataVector().get(fromIndex);
        getDataVector().remove(fromIndex);
        getDataVector().add(toIndex, rowData);

        // Décaler les cellules dans le même sens
        int step = (fromIndex < toIndex) ? 1 : -1;
        for (int row = fromIndex; row != toIndex; row += step) {
            grid[row] = grid[row + step];
        }
        grid[toIndex] = saved;

        fireTableRowsUpdated(Math.min(fromIndex, toIndex),
                Math.max(fromIndex, toIndex));
    }

    /**
     * Échange deux lignes — données ET cellules.
     *
     * @param row1
     * @param row2
     */
    public void swapRows(int row1, int row2) {
        if (row1 == row2) {
            return;
        }
        if (!isValidRow(row1) || !isValidRow(row2)) {
            return;
        }

        Vector<?> temp = (Vector<?>) getDataVector().get(row1);
        getDataVector().set(row1, getDataVector().get(row2));
        getDataVector().set(row2, temp);

        Cell[] tempCells = grid[row1];
        grid[row1] = grid[row2];
        grid[row2] = tempCells;

        fireTableRowsUpdated(Math.min(row1, row2), Math.max(row1, row2));
    }

    public void clear() {
        setRowCount(0);
    }

    public Object[][] getAllData() {
        int rows = getRowCount(), cols = getColumnCount();
        Object[][] data = new Object[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                data[r][c] = getValueAt(r, c);
            }
        }
        return data;
    }

    public Object[] getRowData(int row) {
        int cols = getColumnCount();
        Object[] d = new Object[cols];
        for (int i = 0; i < cols; i++) {
            d[i] = getValueAt(row, i);
        }
        return d;
    }

    public Object[] getColumnData(int col) {
        int rows = getRowCount();
        Object[] d = new Object[rows];
        for (int i = 0; i < rows; i++) {
            d[i] = getValueAt(i, col);
        }
        return d;
    }

    public boolean isCellEmpty(int row, int col) {
        Object value = getValueAt(row, col);
        if (value == null) {
            return true;
        }
        if (value instanceof String string) {
            return string.trim().isEmpty();
        }
        return false;
    }

    public int countNonEmptyCells(int col) {
        int count = 0;
        for (int i = 0; i < getRowCount(); i++) {
            if (!isCellEmpty(i, col)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Subdivise une cellule en une grille de nbRows × nbCols sous-cellules.
     *
     * Construction par arborescence d'InternalGrid imbriquées : - On découpe
     * d'abord horizontalement en nbRows lignes - Chaque ligne est ensuite
     * découpée verticalement en nbCols colonnes
     *
     * @param row ligne de la cellule cible
     * @param col colonne de la cellule cible
     * @param nbRows nombre de lignes dans la grille
     * @param nbCols nombre de colonnes dans la grille
     */
    public void splitCellGrid(int row, int col, int nbRows, int nbCols) {
        if (row < 0 || row >= getRowCount() || col < 0 || col >= getColumnCount()) {
            throw new IndexOutOfBoundsException("Coordonnées invalides : (" + row + ", " + col + ")");
        }
        if (nbRows < 1 || nbCols < 1 || (nbRows == 1 && nbCols == 1)) {
            return;
        }
        Cell cell = getCell(row, col);
        if (cell.isAbsorbed()) {
            return;
        }
        Object currentValue = getValueAt(row, col);
        cell.internalGrid = InternalGrid.buildGrid(nbRows, nbCols, currentValue, cell.style);
        cell.value = null;
        fireTableRowsUpdated(row, row);
    }

    /**
     * Subdivise une Cell directement en une grille nbRows × nbCols. Utilisé
     * quand une sous-cellule interne est focusée.
     *
     * @param targetCell cellule cible
     * @param nbRows nombre de lignes
     * @param nbCols nombre de colonnes
     */
    public void splitCellGridDirectly(Cell targetCell, int nbRows, int nbCols) {
        if (targetCell == null || targetCell.isAbsorbed()) {
            return;
        }
        if (nbRows < 1 || nbCols < 1 || (nbRows == 1 && nbCols == 1)) {
            return;
        }
        targetCell.internalGrid = InternalGrid.buildGrid(nbRows, nbCols, targetCell.value, targetCell.style);
        targetCell.value = null;
        fireTableDataChanged();
    }

    // =========================================================================
    // VALIDATIONS INTERNES
    // =========================================================================
    private boolean isValidRow(int row) {
        return row >= 0 && row < getRowCount();
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < getRowCount()
                && col >= 0 && col < getColumnCount()
                && grid != null
                && row < grid.length
                && grid[row] != null
                && col < grid[row].length;
    }

    /**
     * Subdivise une cellule en deux sous-cellules.
     *
     * @param row ligne cible
     * @param col colonne cible
     * @param splitType type de subdivision
     * @param dividerRatio position du séparateur (0f → 1f)
     */
    public void splitCellLocally(int row,
            int col,
            int splitType,
            float dividerRatio) {
        System.out.println("SPL called");

        // Vérification des bornes
        if (row < 0 || row >= getRowCount()
                || col < 0 || col >= getColumnCount()) {

            throw new IndexOutOfBoundsException("Coordonnées invalides : (" + row + ", " + col + ")");
        }

        Cell cell = getCell(row, col);

        // Une cellule absorbée ne peut pas être subdivisée
        if (cell.isAbsorbed()) {
            return;
        }

        // Pour l'instant : empêcher les subdivisions multiples
        if (cell.hasInternalGrid()) {
            return;
        }

        // Empêche les subdivisions trop petites
        dividerRatio = Math.max(0.15f, dividerRatio);
        dividerRatio = Math.min(0.85f, dividerRatio);

        // Création des deux sous-cellules
        Cell first = new Cell();
        Cell second = new Cell();

        // Conservation du contenu principal — on lit depuis DefaultTableModel        
        Object currentValue = getValueAt(row, col);
        first.value = (currentValue != null) ? currentValue : "";
        second.value = null;

        // Copie du style de la cellule mère
        if (cell.style != null) {
            first.style = cell.style.copy();
            second.style = cell.style.copy();
            InternalGrid.clearSharedEdge(splitType, first.style, second.style);
        }

        // Création de la subdivision locale
        first.internalGrid = cell.internalGrid;
        cell.internalGrid = new InternalGrid(splitType, dividerRatio, first, second);

        // Rafraîchissement
        fireTableRowsUpdated(row, row);
    }

    /**
     * Subdivise une Cell spécifique — peut être une sous-cellule interne.
     * Contrairement à splitCellLocally qui travaille par coordonnées, cette
     * méthode prend directement l'objet Cell cible.
     *
     * @param targetCell la cellule à subdiviser
     * @param splitType InternalGrid.SPLIT_VERTICAL ou SPLIT_HORIZONTAL
     * @param dividerRatio position du séparateur entre 0.15f et 0.85f
     */
    public void splitCellDirectly(Cell targetCell, int splitType, float dividerRatio) {
        if (targetCell == null || targetCell.isAbsorbed()) {
            return;
        }
        System.out.println("SPD called");
        // Toute cellule peut être subdivisée — on supprime la restriction
        // Si elle est déjà subdivisée, sa subdivision existante est conservée
        // dans first, et second est nouveau
        dividerRatio = Math.max(0.15f, Math.min(0.85f, dividerRatio));

        Cell first = new Cell();
        Cell second = new Cell();

        // Reporter le contenu existant dans first uniquement
        first.value = targetCell.value;
        second.value = null;

        // Copier le style
        if (targetCell.style != null) {
            first.style = targetCell.style.copy();
            first.style = targetCell.style.copy();
            second.style = targetCell.style.copy();
            InternalGrid.clearSharedEdge(splitType, first.style, second.style);
        }

        // Si la cellule avait déjà une subdivision, elle passe dans first
        first.internalGrid = targetCell.internalGrid;

        targetCell.internalGrid = new InternalGrid(splitType, dividerRatio, first, second);
        targetCell.value = null;

        // Remplacer fireTableDataChanged() par un fire ciblé
// On recherche la ligne de la cellule cible pour éviter
// de réinitialiser toute la structure
        int targetRow = -1;
        outer:
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                if (grid[r][c] == targetCell) {
                    targetRow = r;
                    break outer;
                }
            }
        }
        if (targetRow >= 0) {
            fireTableRowsUpdated(targetRow, targetRow);
        } else {
            fireTableRowsUpdated(0, getRowCount() - 1);
        }

    }

    /**
     * Supprime la subdivision interne d'une cellule.
     *
     * La cellule redevient une cellule normale.
     *
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     */
    public void removeInternalGrid(int row, int col) {
        if (row < 0 || row >= getRowCount() || col < 0 || col >= getColumnCount()) {
            throw new IndexOutOfBoundsException("Coordonnées invalides");
        }
        Cell cell = getCell(row, col);
        if (!cell.hasInternalGrid()) {
            return;
        }
        String finalValue = cell.removeSubdivision();
        if (cell.isMerged() && cell.mergedValues != null) {
            cell.mergedValues[0][0] = finalValue;
        }
        setValueAt(finalValue, row, col);
        fireTableDataChanged();
    }

    /**
     * Supprime la subdivision interne d'une Cell directement. Utilisé par la
     * gomme sur les sous-cellules.
     */
    public void removeInternalGridFromCell(Cell targetCell, int row, int col) {
        if (targetCell == null || !targetCell.hasInternalGrid()) {
            return;
        }
        String finalValue = targetCell.removeSubdivision();
        if (targetCell.isMerged() && targetCell.mergedValues != null) {
            targetCell.mergedValues[0][0] = finalValue;
        }
        if (isValidCell(row, col)) {
            setValueAt(finalValue, row, col);
        }
        fireTableDataChanged();
    }

    /**
     * Supprime la subdivision d'une sous-cellule interne
     */
    public void removeInternalGridFromCell(Cell subCell) {
        if (subCell == null || !subCell.hasInternalGrid()) {
            return;
        }
        String finalValue;
        if (subCell.isMerged() && subCell.mergedValues != null) {
            Object original = subCell.mergedValues[0][0];
            finalValue = (original != null) ? original.toString() : null;
            subCell.value = finalValue;
            subCell.internalGrid = null;
        } else {
            finalValue = subCell.removeSubdivision();
        }
        outer:
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                if (grid[r][c] == subCell) {
                    setValueAt(finalValue, r, c);
                    break outer;
                }
            }
        }
        fireTableDataChanged();
    }

}
