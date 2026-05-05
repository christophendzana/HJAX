package hsupertable;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * HSuperDefaultTableModel — Modèle de données étendu pour HSuperTable.
 *
 *
 * @author FIDELE
 * @version 3.0
 */
public class HSuperDefaultTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L; // Obligatoire quand on implémente serializable

    /**
    * Unité de base de la grille. 
    * Toujours synchronisée avec les données de DefaultTableModel.
    */
    private Cell[][] grid;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    public HSuperDefaultTableModel() {
        super();
        initGrid(0, 0);
    }

    public HSuperDefaultTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
        initGrid(rowCount, columnCount);
    }

    public HSuperDefaultTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        initGrid(data.length, columnNames.length);
    }

    public HSuperDefaultTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
        super(data, columnNames);
        initGrid(data.size(), columnNames.size());
    }

    public HSuperDefaultTableModel(Object[] columnNames) {
        super(columnNames, 0);
        initGrid(0, columnNames.length);
    }

    public HSuperDefaultTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
        initGrid(rowCount, columnNames.length);
    }

    public HSuperDefaultTableModel(Vector<String> columnNames, int rowCount) {
        super(columnNames, rowCount);
        initGrid(rowCount, columnNames.size());
    }

    // =========================================================================
    // INITIALISATION ET REDIMENSIONNEMENT
    // =========================================================================
    /**
     * Crée une grille neuve — toutes les cellules sont normales par défaut.
     */
    private void initGrid(int rows, int cols) {
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell();
            }
        }
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
                    newGrid[r][c] = grid[r][c]; // on garde la cellule existante
                } else {
                    newGrid[r][c] = new Cell();
                }
            }
        }
        grid = newGrid;
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
        // Défusionner proprement avant de supprimer pour éviter
        // des cellules absorbées orphelines dans la grille
        defuseRow(row);
        super.removeRow(row);
        resizeGrid(getRowCount(), getColumnCount());
    }

    /**
     * Décale la grille d'une ligne vers le bas à partir de insertedRow. On
     * travaille de bas en haut pour ne pas écraser avant d'avoir copié. On met
     * aussi à jour les mergeOrigin qui pointent vers des lignes qui viennent de
     * changer d'index.
     */
    private void shiftGridDown(int insertedRow) {
        int rows = getRowCount();
        int cols = getColumnCount();
        resizeGrid(rows, cols);

        for (int r = rows - 1; r > insertedRow; r--) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = grid[r - 1][c];
                // Mettre à jour les mergeOrigin décalés
                if (grid[r][c].mergeOrigin != null
                        && grid[r][c].mergeOrigin.x >= insertedRow) {
                    grid[r][c].mergeOrigin = new Point(
                            grid[r][c].mergeOrigin.x + 1,
                            grid[r][c].mergeOrigin.y
                    );
                }
            }
        }
        // La ligne insérée reçoit des cellules vierges
        for (int c = 0; c < cols; c++) {
            grid[insertedRow][c] = new Cell();
        }
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
            Cell cell = grid[row][c];
            if (cell.isAbsorbed() && cell.mergeOrigin != null) {
                unmergeCell(cell.mergeOrigin.x, cell.mergeOrigin.y);
            } else if (cell.isMerged()) {
                unmergeCell(row, c);
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
            Cell cell = grid[r][col];
            if (cell.isAbsorbed() && cell.mergeOrigin != null) {
                unmergeCell(cell.mergeOrigin.x, cell.mergeOrigin.y);
            } else if (cell.isMerged()) {
                unmergeCell(r, col);
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
                // Mettre à jour les mergeOrigin décalés
                if (grid[r][c].mergeOrigin != null
                        && grid[r][c].mergeOrigin.y >= insertedCol) {
                    grid[r][c].mergeOrigin = new Point(
                            grid[r][c].mergeOrigin.x,
                            grid[r][c].mergeOrigin.y + 1
                    );
                }
            }
            grid[r][insertedCol] = new Cell();
        }
    }

    /**
     * Supprime la colonne à l'index colIndex.
     *
     * CORRECTION v3 : même approche qu'insertColumn — on manipule le Vector
     * interne directement au lieu de setDataVector().
     *
     * @param colIndex index de la colonne à supprimer
     */
    public void removeColumn(int colIndex) {
        int rows = getRowCount();
        int oldCols = getColumnCount();

        if (colIndex < 0 || colIndex >= oldCols) {
            throw new IndexOutOfBoundsException("Index de colonne invalide : " + colIndex);
        }

        defuseColumn(colIndex);

        // ── 1. Sauvegarder les données ────────────────────────────────────────
        Object[][] oldData = getAllData();

        // ── 2. Nouveaux noms sans la colonne supprimée ────────────────────────
        Vector<String> newColNames = new Vector<>(oldCols - 1);
        for (int c = 0; c < oldCols; c++) {
            if (c != colIndex) {
                newColNames.add(getColumnName(c));
            }
        }

        // ── 3. Nouvelles données sans la colonne supprimée ────────────────────
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

        // ── 4. Appliquer ──────────────────────────────────────────────────────
        setDataVector(newData, newColNames);

        // ── 5. Mettre à jour la grille ────────────────────────────────────────
        shiftGridLeft(colIndex);
        resizeGrid(rows, getColumnCount());
    }

    /**
     * Décale les cellules de la grille vers la gauche à partir de removedCol.
     */
    private void shiftGridLeft(int removedCol) {
        int rows = getRowCount();
        int cols = getColumnCount(); // déjà réduit par setColumnCount

        for (int r = 0; r < rows; r++) {
            for (int c = removedCol; c < cols; c++) {
                grid[r][c] = grid[r][c + 1];
                // Mettre à jour les mergeOrigin qui pointaient après removedCol
                if (grid[r][c].mergeOrigin != null
                        && grid[r][c].mergeOrigin.y > removedCol) {
                    grid[r][c].mergeOrigin = new Point(
                            grid[r][c].mergeOrigin.x,
                            grid[r][c].mergeOrigin.y - 1
                    );
                }
            }
        }
    }

    // =========================================================================
    // ACCÈS À LA GRILLE
    // =========================================================================
    /**
     * Retourne la Cell à la position (row, col). Ne retourne jamais null.
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
    public HSuperTableCellModel getCellModel(int row, int col) {
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
        HSuperTableCellModel m = getCell(row, col).style;
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
     */
    public int[] getSpan(int row, int col) {
        if (!isValidCell(row, col)) {
            return new int[]{1, 1};
        }
        Cell c = grid[row][col];
        return new int[]{c.spanRow, c.spanCol};
    }

    /**
     * Vrai si absorbée — le renderer doit sauter cette cellule.
     */
    public boolean isAbsorbed(int row, int col) {
        return isValidCell(row, col) && grid[row][col].isAbsorbed();
    }

    /**
     * Vrai si principale d'une fusion.
     */
    public boolean isMergedCell(int row, int col) {
        return isValidCell(row, col) && grid[row][col].isMerged();
    }

    /**
     * Retourne l'origine de la fusion en O(1). Lecture directe de
     * Cell.mergeOrigin — plus de scan O(n×m).
     *
     * @return Point(row, col) de la principale, ou null si non absorbée.
     */
    public Point findMergeOrigin(int row, int col) {
        if (!isValidCell(row, col)) {
            return null;
        }
        return grid[row][col].mergeOrigin;
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

        // Étape 1 : collecter les fusions intersectées AVANT de modifier la grille
        List<Point> toUnmerge = collectIntersectingMerges(rowStart, colStart,
                rowEnd, colEnd);
        // Étape 2 : les défusionner proprement
        for (Point origin : toUnmerge) {
            unmergeCell(origin.x, origin.y);
        }

        // Étape 3 : poser la nouvelle fusion
        Cell principal = grid[rowStart][colStart];
        principal.spanRow = rowEnd - rowStart + 1;
        principal.spanCol = colEnd - colStart + 1;
        principal.mergeOrigin = null;

// collecter le contenu de toutes les cellules de la zone
        StringBuilder merged = new StringBuilder();
        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                Object val = getValueAt(r, c);
                if (val != null && !val.toString().trim().isEmpty()) {
                    if (merged.length() > 0) {
                        merged.append(" ");
                    }
                    merged.append(val.toString().trim());
                }
            }
        }

// Placer le contenu fusionné dans la cellule principale
        setValueAt(merged.length() > 0 ? merged.toString() : null,
                rowStart, colStart);

        // Absorber les autres cellules
        for (int r = rowStart; r <= rowEnd; r++) {
            for (int c = colStart; c <= colEnd; c++) {
                if (r == rowStart && c == colStart) {
                    continue;
                }
                Cell absorbed = grid[r][c];
                absorbed.spanRow = 0;
                absorbed.spanCol = 0;
                absorbed.mergeOrigin = new Point(rowStart, colStart);
                setValueAt(null, r, c); // vider APRÈS avoir collecté le contenu
            }
        }
    }

    /**
     * Collecte les Points des cellules principales dont la zone de fusion
     * intersecte le rectangle cible.
     *
     * Condition d'intersection entre deux rectangles : A intersecte B si A.top <= B.bottom ET A.bottom
     * >= B.top ET A.left <= B.right  ET A.right >= B.left
     */
    private List<Point> collectIntersectingMerges(int rowStart, int colStart,
            int rowEnd, int colEnd) {
        List<Point> result = new ArrayList<>();
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                Cell cell = grid[r][c];
                if (!cell.isMerged()) {
                    continue; // on ne s'intéresse qu'aux principales
                }
                int rEnd = r + cell.spanRow - 1;
                int cEnd = c + cell.spanCol - 1;

                boolean intersects = r <= rowEnd && rEnd >= rowStart
                        && c <= colEnd && cEnd >= colStart;
                if (intersects) {
                    result.add(new Point(r, c));
                }
            }
        }
        return result;
    }

    /**
     * Défusionne la cellule à (row, col).
     *
     * Si absorbée → O(1) via mergeOrigin pour trouver la principale. Si
     * principale → libère toutes les absorbées qu'elle couvre. Si normale → ne
     * fait rien.
     */
    public void unmergeCell(int row, int col) {
        if (!isValidCell(row, col)) {
            return;
        }
        Cell cell = grid[row][col];

        if (cell.isAbsorbed()) {
            // Remonte à la principale en O(1) et défusionne celle-là
            if (cell.mergeOrigin != null) {
                unmergeCell(cell.mergeOrigin.x, cell.mergeOrigin.y);
            } else {
                cell.resetSpan(); // incohérence — on remet à 1,1
            }
            return;
        }

        if (cell.isNormal()) {
            return; // rien à faire
        }
        // C'est la principale : libère toutes les absorbées
        int rSpan = cell.spanRow;
        int cSpan = cell.spanCol;
        for (int dr = 0; dr < rSpan; dr++) {
            for (int dc = 0; dc < cSpan; dc++) {
                if (isValidCell(row + dr, col + dc)) {
                    grid[row + dr][col + dc].resetSpan();
                }
            }
        }
    }

    /**
     * Fractionne une cellule fusionnée en targetRows × targetCols sous-blocs.
     *
     * CORRECTION v3 : refuse si le span n'est pas divisible exactement.
     * Retourne false dans ce cas (comme Word qui grise le bouton).
     *
     * @return true si le fractionnement a réussi, false si impossible
     */
    public boolean splitCell(int row, int col, int targetRows, int targetCols) {
        if (!isValidCell(row, col)) {
            return false;
        }

        // Si absorbée, remonter à la principale en O(1)
        int r = row, c = col;
        Cell cell = grid[row][col];
        if (cell.isAbsorbed()) {
            Point origin = findMergeOrigin(row, col);
            if (origin == null) {
                return false;
            }
            r = origin.x;
            c = origin.y;
            cell = grid[r][c];
        }

        if (!cell.isMerged()) {
            return false;
        }

        int currentRSpan = cell.spanRow;
        int currentCSpan = cell.spanCol;

        // Refus si division inexacte — plus de perte silencieuse
        if (currentRSpan % targetRows != 0 || currentCSpan % targetCols != 0) {
            return false;
        }

        int blockR = currentRSpan / targetRows;
        int blockC = currentCSpan / targetCols;

        unmergeCell(r, c); // libérer d'abord toute la zone

        // Refusionner en sous-blocs si nécessaire
        if (blockR > 1 || blockC > 1) {
            for (int dr = 0; dr < targetRows; dr++) {
                for (int dc = 0; dc < targetCols; dc++) {
                    int startR = r + dr * blockR;
                    int startC = c + dc * blockC;
                    mergeCells(startR, startC,
                            startR + blockR - 1,
                            startC + blockC - 1);
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
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
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

    // CLASSE INTERNE — Cell
    // Unité de base de la grille.     
    /**
     * Cell — fiche complète d'une cellule (métadonnées uniquement).
     */
    public static class Cell {

        /**
         * Métadonnées visuelles : couleurs, bordures, alignement, marges,
         * direction, formule.
         */
        public HSuperTableCellModel style;

        /**
         * Nombre de lignes occupées. 1 = normale ou principale sur 1 ligne N >
         * 1 = principale d'une fusion sur N lignes; 0 = absorbée
         */
        public int spanRow;

        /**
         * Nombre de colonnes occupées. Même logique que spanRow.
         */
        public int spanCol;

        /**
         * Adresse de la cellule principale de la fusion. null -> cette cellule
         * est normale ou est elle-même la principale Point(r,c) -> la principale
         * est en (r,c).         
         */
        public Point mergeOrigin;

        /**
         * Crée une cellule normale avec toutes les valeurs par défaut.
         */
        public Cell() {
            this.style = new HSuperTableCellModel();
            this.spanRow = 1;
            this.spanCol = 1;
            this.mergeOrigin = null;
        }

        /**
         * Vrai si absorbée par une fusion — le renderer doit la sauter.
         */
        public boolean isAbsorbed() {
            return spanRow == 0 && spanCol == 0;
        }

        /**
         * Vrai si principale d'une fusion de taille > 1×1.
         */
        public boolean isMerged() {
            return spanRow > 1 || spanCol > 1;
        }

        /**
         * Vrai si dans l'état normal (ni fusionnée, ni absorbée).
         */
        public boolean isNormal() {
            return spanRow == 1 && spanCol == 1 && mergeOrigin == null;
        }

        /**
         * Remet la cellule dans l'état normal — utilisé lors de la défusion.
         */
        public void resetSpan() {
            this.spanRow = 1;
            this.spanCol = 1;
            this.mergeOrigin = null;
        }

        /**
         * Copie profonde. Utilisée lors du déplacement de lignes/colonnes pour
         * éviter que deux cases de la grille partagent le même objet.
         */
        public Cell copy() {
            Cell copy = new Cell();
            copy.style = this.style.copy();
            copy.spanRow = this.spanRow;
            copy.spanCol = this.spanCol;
            copy.mergeOrigin = (this.mergeOrigin != null)
                    ? new Point(this.mergeOrigin.x, this.mergeOrigin.y)
                    : null;
            return copy;
        }

        @Override
        public String toString() {
            if (isAbsorbed()) {
                return "Cell[ABSORBED origin=" + mergeOrigin + "]";
            }
            if (isMerged()) {
                return "Cell[MERGED " + spanRow + "×" + spanCol + "]";
            }
            return "Cell[normal]";
        }
    }

}
