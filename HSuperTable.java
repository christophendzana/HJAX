package hsupertable;


import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * HSuperTable — Tableau Swing avancé inspiré des outils tableau de Microsoft Word.
 *
 * Point d'entrée unique de toutes les fonctionnalités. L'utilisateur n'a besoin
 * que de cette classe pour tout faire : modifier la structure, personnaliser
 * l'apparence, fusionner des cellules, trier, calculer des formules, etc.
 *
 * Utilisation typique avec un bouton :
 * <pre>
 *   HSuperTable table = new HSuperTable(data, colonnes);
 *   btnFusionner.addActionListener(e -> table.mergeSelectedCells());
 *   btnTrier.addActionListener(e -> table.sortByColumn(0, SortOrder.ASCENDING));
 * </pre>
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSuperTable extends JTable {

    // =========================================================================
    // CONSTANTES PUBLIQUES — Enums intégrées comme classes statiques internes
    // L'utilisateur les utilise via HSuperTable.BORDER_SOLID, etc.
    // =========================================================================

    // -- Direction du texte --
    public static final int TEXT_HORIZONTAL     = 0;
    public static final int TEXT_VERTICAL_UP    = 1;
    public static final int TEXT_VERTICAL_DOWN  = 2;

    // -- Styles de bordure --
    public static final int BORDER_SOLID  = 0;
    public static final int BORDER_DASHED = 1;
    public static final int BORDER_DOTTED = 2;
    public static final int BORDER_DOUBLE = 3;

    // -- Côtés de bordure (utilisables en combinaison avec l'opérateur | ) --
    public static final int SIDE_TOP    = 0b0001;
    public static final int SIDE_BOTTOM = 0b0010;
    public static final int SIDE_LEFT   = 0b0100;
    public static final int SIDE_RIGHT  = 0b1000;
    public static final int SIDE_ALL    = 0b1111;
    public static final int SIDE_OUTER  = 0b1111;   // alias sémantique de ALL
    public static final int SIDE_INNER  = 0b10000;  // bit réservé, traité dans les méthodes

    // -- Modes d'ajustement automatique --
    public static final int AUTOFIT_CONTENT = 0;  // ajuste au contenu des cellules
    public static final int AUTOFIT_WINDOW  = 1;  // ajuste à la largeur du parent

    // =========================================================================
    // COMPOSANTS INTERNES
    // =========================================================================

    /** Modèle de données — contient aussi toutes les métadonnées (spans, cellModels). */
    private final HSuperDefaultTableModel hModel;

    /** Contrôleur des événements souris/clavier. */
    private final hsupertable.HSuperTableController controller;

    // =========================================================================
    // ÉTATS VISUELS (repris de HTable, nettoyés et étendus)
    // =========================================================================

    private int highlightedRow = -1;
    private int hoveredRow     = -1;
    private int focusedRow     = -1;
    private int focusedColumn  = -1;
    private boolean editingCell = false;
    private boolean gridVisible = true;

    /** Lignes sélectionnées (sélection multiple custom). */
    private Set<Integer> selectedRows = new HashSet<>();

    /** Couleurs de fond custom par ligne (API de haut niveau, conservée). */
    private final Map<Integer, Color> rowBackgroundColors  = new HashMap<>();
    private final Map<Integer, Color> rowForegroundColors  = new HashMap<>();

    /** États visuels génériques (pour extensions futures). */
    private final Map<String, Object> visualStates = new HashMap<>();

    // =========================================================================
    // OPTIONS DE STYLE (onglet Création de Word)
    // =========================================================================

    private boolean headerRowEnabled       = true;
    private boolean totalRowEnabled        = false;
    private boolean bandedRows             = true;
    private boolean bandedColumns          = false;
    private boolean firstColumnHighlighted = false;
    private boolean lastColumnHighlighted  = false;

    // =========================================================================
    // MARGES PAR DÉFAUT DES CELLULES
    // =========================================================================

    /** Marges internes appliquées à toutes les cellules sans marge custom. */
    private Insets defaultCellMargins = new Insets(6, 12, 6, 12);

    // =========================================================================
    // STYLE VISUEL
    // =========================================================================

    private HSuperTableStyle tableStyle = HSuperTableStyle.PRIMARY;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    public HSuperTable() {
        this(new HSuperDefaultTableModel());
    }

    public HSuperTable(HSuperDefaultTableModel model) {
        super(model);
        this.hModel     = model;
        this.controller = new hsupertable.HSuperTableController(this);
        setUI(new HBasicTableUI());
        initDefaults();
    }

    public HSuperTable(Object[][] data, Object[] columnNames) {
        this(new HSuperDefaultTableModel(data, columnNames));
    }

    public HSuperTable(Vector<Vector<Object>> data, Vector<String> columnNames) {
        this(new HSuperDefaultTableModel(data, columnNames));
    }

    public HSuperTable(int rowCount, int columnCount) {
        this(new HSuperDefaultTableModel(rowCount, columnCount));
    }

    /**
     * Constructeur de compatibilité : accepte n'importe quel TableModel.
     * Si ce n'est pas un HDefaultTableModel, les données sont converties.
     */
    public HSuperTable(TableModel model) {
        this(model instanceof HSuperDefaultTableModel
             ? (HSuperDefaultTableModel) model
             : convertToHDefaultTableModel(model));
    }

    /** Conversion d'un TableModel standard vers HDefaultTableModel. */
    private static HSuperDefaultTableModel convertToHDefaultTableModel(TableModel src) {
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        Object[] colNames = new Object[cols];
        for (int c = 0; c < cols; c++) colNames[c] = src.getColumnName(c);
        Object[][] data = new Object[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                data[r][c] = src.getValueAt(r, c);
        return new HSuperDefaultTableModel(data, colNames);
    }

    /** Configuration initiale commune à tous les constructeurs. */
    private void initDefaults() {
        setRowHeight(36);
        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        setGridColor(new Color(222, 226, 230));
        setSelectionBackground(new Color(13, 110, 253, 30));
        setSelectionForeground(Color.BLACK);
        setFillsViewportHeight(false);
    }

    // =========================================================================
    // ── ONGLET CRÉATION ──────────────────────────────────────────────────────
    // =========================================================================

    // ── Options de style ─────────────────────────────────────────────────────

    /**
     * Active ou désactive la mise en forme spéciale de la ligne d'en-tête.Quand elle est active, la première ligne reçoit le fond headerBackground
 du style courant.
     * @param enabled
     */
    public void setHeaderRowEnabled(boolean enabled) {
        this.headerRowEnabled = enabled;
        refreshUI();
    }

    public boolean isHeaderRowEnabled() { return headerRowEnabled; }

    /**
     * Active la ligne totale : la dernière ligne reçoit une mise en forme
     * distincte (fond totalRowBackground du style).
     * @param enabled
     */
    public void setTotalRowEnabled(boolean enabled) {
        this.totalRowEnabled = enabled;
        refreshUI();
    }

    public boolean isTotalRowEnabled() { return totalRowEnabled; }

    /** Active ou désactive l'alternance de couleurs sur les lignes (zebra).
     * @param enabled */
    public void setBandedRows(boolean enabled) {
        this.bandedRows = enabled;
        refreshUI();
    }

    public boolean isBandedRows() { return bandedRows; }

    /** Active ou désactive l'alternance de couleurs sur les colonnes.
     * @param enabled */
    public void setBandedColumns(boolean enabled) {
        this.bandedColumns = enabled;
        refreshUI();
    }

    public boolean isBandedColumns() { return bandedColumns; }

    /** Met en valeur la première colonne (fond firstColumnBackground du style). */
    public void setFirstColumnHighlighted(boolean enabled) {
        this.firstColumnHighlighted = enabled;
        refreshUI();
    }

    public boolean isFirstColumnHighlighted() { return firstColumnHighlighted; }

    /** Met en valeur la dernière colonne (fond lastColumnBackground du style). */
    public void setLastColumnHighlighted(boolean enabled) {
        this.lastColumnHighlighted = enabled;
        refreshUI();
    }

    public boolean isLastColumnHighlighted() { return lastColumnHighlighted; }

    // ── Styles prédéfinis ────────────────────────────────────────────────────

    public HSuperTableStyle getTableStyle() { return tableStyle; }

    /**
     * Applique un style prédéfini au tableau.
     * Met à jour les couleurs de grille, sélection et en-tête immédiatement.
     */
    public void setTableStyle(HSuperTableStyle style) {
        this.tableStyle = style;
        if (style == null) return;
        setGridColor(style.getGridColor());
        setSelectionBackground(style.getSelectionBackground());
        setSelectionForeground(style.getCellForeground());
        if (getTableHeader() != null) {
            getTableHeader().setBackground(style.getHeaderBackground());
            getTableHeader().setForeground(style.getHeaderForeground());
            getTableHeader().setFont(style.getHeaderFont());
        }
        refreshUI();
    }

    /** Remet le style par défaut (PRIMARY) sans aucune personnalisation. */
    public void resetStyle() {
        setTableStyle(HSuperTableStyle.PRIMARY);
        clearAllVisualStates();
    }

    // ── Trame de fond ────────────────────────────────────────────────────────

    /**
     * Définit la couleur de fond d'une cellule précise.
     * Priorité maximale — écrase tout le reste (style, bandes, hover, etc.).
     *
     * @param row   ligne (0-indexée)
     * @param col   colonne (0-indexée)
     * @param color couleur souhaitée, ou null pour retirer la couleur custom
     */
    public void setCellBackground(int row, int col, Color color) {
        hModel.setCellBackground(row, col, color);
        refreshUI();
    }

    public Color getCellBackground(int row, int col) {
        return hModel.getCellBackground(row, col);
    }

    /**
     * Applique une couleur de fond à toute une ligne.
     * Utilise l'ancienne Map rowBackgroundColors pour rester compatible
     * avec le code existant.
     */
    public void setRowBackground(int row, Color color) {
        if (color == null) rowBackgroundColors.remove(row);
        else rowBackgroundColors.put(row, color);
        refreshUI();
    }

    public Color getRowBackground(int row) {
        return rowBackgroundColors.get(row);
    }

    /** Applique une couleur de fond à toute une colonne. */
    public void setColumnBackground(int col, Color color) {
        for (int r = 0; r < getRowCount(); r++) {
            hModel.setCellBackground(r, col, color);
        }
        refreshUI();
    }

    /** Applique une couleur de fond à la sélection courante. */
    public void setSelectionCellBackground(Color color) {
        for (int row : selectedRows) {
            hModel.setCellBackground(row, getFocusedColumn(), color);
        }
        refreshUI();
    }

    // ── Couleur du texte ─────────────────────────────────────────────────────

    public void setCellForeground(int row, int col, Color color) {
        hModel.setCellForeground(row, col, color);
        refreshUI();
    }

    public void setRowForeground(int row, Color color) {
        if (color == null) rowForegroundColors.remove(row);
        else rowForegroundColors.put(row, color);
        refreshUI();
    }

    public Color getRowForeground(int row) {
        return rowForegroundColors.get(row);
    }

    public void setColumnForeground(int col, Color color) {
        for (int r = 0; r < getRowCount(); r++) {
            hModel.setCellForeground(r, col, color);
        }
        refreshUI();
    }

    // ── Bordures ─────────────────────────────────────────────────────────────

    /**
     * Définit une bordure sur un ou plusieurs côtés d'une cellule.
     *
     * Exemple :
     * <pre>
     *   // Bordure rouge épaisse en bas et à droite de la cellule (1,2)
     *   table.setCellBorderSide(1, 2,
     *       HSuperTable.SIDE_BOTTOM | HSuperTable.SIDE_RIGHT,
     *       Color.RED, 2f, HSuperTable.BORDER_SOLID);
     * </pre>
     *
     * @param row       ligne de la cellule
     * @param col       colonne de la cellule
     * @param sides     combinaison de SIDE_TOP, SIDE_BOTTOM, SIDE_LEFT, SIDE_RIGHT
     * @param color     couleur de la bordure
     * @param thickness épaisseur en pixels
     * @param style     BORDER_SOLID, BORDER_DASHED, BORDER_DOTTED ou BORDER_DOUBLE
     */
    public void setCellBorderSide(int row, int col, int sides,
                                   Color color, float thickness, int style) {
        hModel.setCellBorderSide(row, col, sides, color, thickness, style);
        refreshUI();
    }

    /** Bordure identique sur les quatre côtés d'une cellule. */
    public void setCellBorderAll(int row, int col,
                                  Color color, float thickness, int style) {
        hModel.setCellBorderSide(row, col, SIDE_ALL, color, thickness, style);
        refreshUI();
    }

    /** Applique la même bordure sur toutes les cellules du tableau. */
    public void setBorderAll(Color color, float thickness, int style) {
        for (int r = 0; r < getRowCount(); r++)
            for (int c = 0; c < getColumnCount(); c++)
                hModel.setCellBorderSide(r, c, SIDE_ALL, color, thickness, style);
        refreshUI();
    }

    /**
     * Applique une bordure uniquement sur les bords extérieurs du tableau
     * (haut de la première ligne, bas de la dernière, gauche de la première
     * colonne, droite de la dernière colonne).
     */
    public void setBorderOuter(Color color, float thickness, int style) {
        int lastRow = getRowCount() - 1;
        int lastCol = getColumnCount() - 1;
        for (int c = 0; c <= lastCol; c++) {
            hModel.setCellBorderSide(0,       c, SIDE_TOP,    color, thickness, style);
            hModel.setCellBorderSide(lastRow, c, SIDE_BOTTOM, color, thickness, style);
        }
        for (int r = 0; r <= lastRow; r++) {
            hModel.setCellBorderSide(r, 0,       SIDE_LEFT,  color, thickness, style);
            hModel.setCellBorderSide(r, lastCol, SIDE_RIGHT, color, thickness, style);
        }
        refreshUI();
    }

    /**
     * Applique une bordure sur toutes les séparations internes du tableau
     * (entre les lignes et entre les colonnes, mais pas sur les bords externes).
     */
    public void setBorderInner(Color color, float thickness, int style) {
        int rows = getRowCount();
        int cols = getColumnCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c < cols - 1) hModel.setCellBorderSide(r, c, SIDE_RIGHT,  color, thickness, style);
                if (r < rows - 1) hModel.setCellBorderSide(r, c, SIDE_BOTTOM, color, thickness, style);
            }
        }
        refreshUI();
    }

    /** Applique une bordure sur toute la sélection courante. */
    public void setSelectionBorder(int sides, Color color, float thickness, int style) {
        for (int row : selectedRows) {
            hModel.setCellBorderSide(row, getFocusedColumn(), sides, color, thickness, style);
        }
        refreshUI();
    }

    /** Supprime toutes les bordures custom d'une cellule. */
    public void removeCellBorder(int row, int col) {
        hModel.clearCellBorders(row, col);
        refreshUI();
    }

    /** Supprime un côté spécifique de la bordure d'une cellule. */
    public void removeCellBorderSide(int row, int col, int sides) {
        // On supprime en passant thickness=0 et color=null
        hModel.setCellBorderSide(row, col, sides, null, 0f, BORDER_SOLID);
        refreshUI();
    }

    /** Supprime toutes les bordures custom du tableau entier. */
    public void removeAllBorders() {
        for (int r = 0; r < getRowCount(); r++)
            for (int c = 0; c < getColumnCount(); c++)
                hModel.clearCellBorders(r, c);
        refreshUI();
    }

    // =========================================================================
    // ── ONGLET DISPOSITION ───────────────────────────────────────────────────
    // =========================================================================

    // ── Sélection ────────────────────────────────────────────────────────────

    /** Sélectionne une cellule précise et met le focus dessus. */
    public void selectCell(int row, int col) {
        clearSelection();
        addSelectedRow(row);
        setFocusedCell(row, col);
    }

    /** Sélectionne toute une ligne. */
    public void selectRow(int row) {
        clearSelection();
        addSelectedRow(row);
        setFocusedCell(row, 0);
    }

    /**
     * Sélectionne toute une colonne.
     * JTable n'a pas de notion de "colonne sélectionnée" dans notre modèle
     * custom — on met le focus sur la colonne et on sélectionne toutes les
     * lignes pour signaler visuellement la sélection.
     */
    public void selectColumn(int col) {
        clearSelection();
        for (int r = 0; r < getRowCount(); r++) addSelectedRow(r);
        setFocusedCell(0, col);
    }

    /** Sélectionne tout le tableau. */
    public void selectAll() {
        clearSelection();
        for (int r = 0; r < getRowCount(); r++) addSelectedRow(r);
    }

    public int[] getSelectedRowsArray() {
        return selectedRows.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    /** Retourne les index des colonnes visibles (toutes, dans l'ordre). */
    public int[] getSelectedColumns() {
        int[] cols = new int[getColumnCount()];
        for (int c = 0; c < cols.length; c++) cols[c] = c;
        return cols;
    }

    // ── Quadrillage ──────────────────────────────────────────────────────────

    /**
     * Affiche ou masque le quadrillage léger entre les cellules.
     * Ce quadrillage est purement visuel — il ne crée pas de bordures réelles.
     */
    public void setGridVisible(boolean visible) {
        this.gridVisible = visible;
        refreshUI();
    }

    public boolean isGridVisible() { return gridVisible; }

    // ── Lignes et colonnes ───────────────────────────────────────────────────

    /** Insère une ligne vide au-dessus de la ligne donnée. */
    public void insertRowAbove(int row) {
        if (row < 0 || row > getRowCount()) return;
        hModel.insertEmptyRow(row);
        refreshUI();
    }

    /** Insère une ligne vide en-dessous de la ligne donnée. */
    public void insertRowBelow(int row) {
        insertRowAbove(row + 1);
    }

    /** Insère une colonne vide à gauche de la colonne donnée. */
    public void insertColumnLeft(int col) {
        if (col < 0 || col > getColumnCount()) return;
        hModel.insertColumn(col, "Colonne " + (col + 1));
        refreshUI();
    }

    /** Insère une colonne vide à droite de la colonne donnée. */
    public void insertColumnRight(int col) {
        insertColumnLeft(col + 1);
    }

    /** Supprime la ligne à l'index donné. */
    public void deleteRow(int row) {
        if (row < 0 || row >= getRowCount()) return;
        hModel.removeRow(row);
        refreshUI();
    }

    /**
     * Supprime plusieurs lignes en une fois.
     * On supprime de la fin vers le début pour éviter le décalage d'index.
     *
     * @param rows tableau des index à supprimer (non trié, c'est géré ici)
     */
    public void deleteRows(int[] rows) {
        List<Integer> sorted = new ArrayList<>();
        for (int r : rows) sorted.add(r);
        sorted.sort(Collections.reverseOrder());  // suppression de bas en haut
        for (int r : sorted) {
            if (r >= 0 && r < getRowCount()) hModel.removeRow(r);
        }
        refreshUI();
    }

    /** Supprime toutes les lignes de la sélection courante. */
    public void deleteSelectedRows() {
        deleteRows(getSelectedRowsArray());
        clearSelection();
    }

    /** Supprime la colonne à l'index donné. */
    public void deleteColumn(int col) {
        if (col < 0 || col >= getColumnCount()) return;
        hModel.removeColumn(col);
        refreshUI();
    }

    /**
     * Supprime plusieurs colonnes en une fois.
     * Même principe que deleteRows() : de droite à gauche.
     */
    public void deleteColumns(int[] cols) {
        List<Integer> sorted = new ArrayList<>();
        for (int c : cols) sorted.add(c);
        sorted.sort(Collections.reverseOrder());
        for (int c : sorted) {
            if (c >= 0 && c < getColumnCount()) hModel.removeColumn(c);
        }
        refreshUI();
    }

    /** Vide le tableau (supprime toutes les lignes, conserve les colonnes). */
    public void clearTable() {
        hModel.clear();
        refreshUI();
    }

    // ── Fusion ───────────────────────────────────────────────────────────────

    /**
     * Fusionne les cellules dans la zone (r1,c1) → (r2,c2).
     * La cellule (r1,c1) devient la cellule principale et conserve son contenu.
     * Les autres cellules de la zone sont vidées et marquées comme absorbées.
     *
     * @param r1 ligne du coin supérieur gauche
     * @param c1 colonne du coin supérieur gauche
     * @param r2 ligne du coin inférieur droit
     * @param c2 colonne du coin inférieur droit
     */
    public void mergeCells(int r1, int c1, int r2, int c2) {
        hModel.mergeCells(r1, c1, r2, c2);
        refreshUI();
    }

    /**
     * Fusionne les cellules de la sélection courante.
     * Calcule automatiquement le rectangle englobant de la sélection.
     * Ne fait rien si moins de deux cellules sont sélectionnées.
     */
    public void mergeSelectedCells() {
        if (selectedRows.size() < 2 && focusedColumn < 0) return;

        // Rectangle englobant : ligne min → max, colonne du focus uniquement
        // (pour une sélection multi-colonnes il faudrait une sélection étendue,
        // mais notre modèle de sélection actuel est par lignes entières)
        int minRow = selectedRows.stream().min(Integer::compareTo).orElse(focusedRow);
        int maxRow = selectedRows.stream().max(Integer::compareTo).orElse(focusedRow);

        // On utilise les colonnes sélectionnées via le columnSelectionModel de JTable
        int minCol = getSelectedColumn();
        int maxCol = minCol;
        for (int c : getSelectedColumns()) {
            minCol = Math.min(minCol, c);
            maxCol = Math.max(maxCol, c);
        }

        if (minRow < 0 || minCol < 0) return;
        mergeCells(minRow, minCol, maxRow, maxCol);
    }

    /**
     * Défusionne la cellule à la position donnée.
     * Si la cellule est absorbée, remonte à la cellule principale et la libère.
     */
    public void unmergeCell(int row, int col) {
        hModel.unmergeCell(row, col);
        refreshUI();
    }

    /**
     * Fractionne une cellule fusionnée en targetRows × targetCols sous-cellules.Exemple : une fusion 4×4 fractionnée en (2, 2) donne quatre blocs de 2×2.
     *
     *
     * @param row        ligne de la cellule à fractionner
     * @param col        colonne de la cellule à fractionner
     * @param targetRows nombre de lignes dans le fractionnement
     * @param targetCols nombre de colonnes dans le fractionnement
     * @return 
     */
    public boolean splitCell(int row, int col, int targetRows, int targetCols) {
    boolean success = hModel.splitCell(row, col, targetRows, targetCols);
    if (!success) {
        // Le span n'est pas divisible exactement — on informe l'appelant
        // L'utilisateur peut brancher un HOptionPane sur ce retour
        System.out.println("splitCell : division impossible sans perte "
            + "— vérifiez que le span est divisible par ("
            + targetRows + ", " + targetCols + ")");
    }
    refreshUI();
    return success;
}

    /**
     * Coupe le tableau en deux à partir de la ligne donnée.
     * Les lignes [0, atRow-1] restent dans ce tableau.
     * Les lignes [atRow, fin] sont retournées dans un nouveau HSuperTable
     * indépendant.
     *
     * @param  atRow index de la première ligne du second tableau
     * @return un nouveau HSuperTable contenant les lignes détachées
     */
    public HSuperTable splitTable(int atRow) {
        if (atRow <= 0 || atRow >= getRowCount()) return null;

        int cols = getColumnCount();
        Object[] colNames = new Object[cols];
        for (int c = 0; c < cols; c++) colNames[c] = getColumnName(c);

        // Copier les lignes qui vont dans le nouveau tableau
        int newRowCount = getRowCount() - atRow;
        Object[][] newData = new Object[newRowCount][cols];
        for (int r = 0; r < newRowCount; r++)
            for (int c = 0; c < cols; c++)
                newData[r][c] = hModel.getValueAt(atRow + r, c);

        // Supprimer ces lignes du tableau courant (de bas en haut)
        for (int r = getRowCount() - 1; r >= atRow; r--) {
            hModel.removeRow(r);
        }

        HSuperTable newTable = new HSuperTable(newData, colNames);
        newTable.setTableStyle(this.tableStyle);
        refreshUI();
        return newTable;
    }

    // ── Taille de cellule ────────────────────────────────────────────────────

    /**
     * Définit la hauteur d'une ligne précise.
     * JTable gère déjà setRowHeight(int) globalement — on ajoute la version
     * par ligne.
     */
    public void setRowHeight(int row, int height) {
        if (row >= 0 && row < getRowCount() && height > 0) {
            super.setRowHeight(row, height);
        }
    }

    /** Applique la même hauteur à toutes les lignes. */
    public void setAllRowsHeight(int height) {
        if (height > 0) super.setRowHeight(height);
    }

    /** Définit la largeur d'une colonne précise. */
    public void setColumnWidth(int col, int width) {
        if (col >= 0 && col < getColumnCount() && width > 0) {
            getColumnModel().getColumn(col).setPreferredWidth(width);
        }
    }

    /** Applique la même largeur à toutes les colonnes. */
    public void setAllColumnsWidth(int width) {
        for (int c = 0; c < getColumnCount(); c++) {
            getColumnModel().getColumn(c).setPreferredWidth(width);
        }
    }

    /**
     * Répartit la hauteur de toutes les lignes de façon uniforme.
     * La hauteur cible est la moyenne des hauteurs actuelles.
     */
    public void distributeRowsEvenly() {
        if (getRowCount() == 0) return;
        int total = 0;
        for (int r = 0; r < getRowCount(); r++) total += getRowHeight(r);
        int avg = total / getRowCount();
        setAllRowsHeight(Math.max(avg, 20));
    }

    /**
     * Répartit la largeur de toutes les colonnes de façon uniforme.
     * Utilise la largeur totale actuelle du tableau divisée par le nombre
     * de colonnes.
     */
    public void distributeColumnsEvenly() {
        if (getColumnCount() == 0) return;
        int totalW = 0;
        for (int c = 0; c < getColumnCount(); c++)
            totalW += getColumnModel().getColumn(c).getWidth();
        int avg = totalW / getColumnCount();
        setAllColumnsWidth(Math.max(avg, 20));
    }

    /**
     * Ajustement automatique selon le mode choisi.
     *
     * AUTOFIT_CONTENT : chaque colonne s'adapte au contenu le plus large
     *                   (en-tête inclus) + une marge de 20px.
     * AUTOFIT_WINDOW  : toutes les colonnes se partagent équitablement
     *                   la largeur du composant parent visible.
     *
     * @param mode HSuperTable.AUTOFIT_CONTENT ou HSuperTable.AUTOFIT_WINDOW
     */
    public void autoFit(int mode) {
        if (mode == AUTOFIT_WINDOW) {
            // Largeur totale disponible = largeur du parent ou du viewport
            int availableWidth = getParent() != null ? getParent().getWidth() : getWidth();
            if (availableWidth <= 0 || getColumnCount() == 0) return;
            int colW = availableWidth / getColumnCount();
            setAllColumnsWidth(Math.max(colW, 20));
        } else {
            // AUTOFIT_CONTENT : on mesure le contenu de chaque colonne
            for (int col = 0; col < getColumnCount(); col++) {
                int maxW = 0;
                // En-tête
                var headerRenderer = getTableHeader().getDefaultRenderer();
                var headerComp = headerRenderer.getTableCellRendererComponent(
                    this, getColumnName(col), false, false, 0, col);
                maxW = Math.max(maxW, headerComp.getPreferredSize().width);
                // Cellules
                for (int row = 0; row < getRowCount(); row++) {
                    var renderer = getCellRenderer(row, col);
                    var comp = prepareRenderer(renderer, row, col);
                    maxW = Math.max(maxW, comp.getPreferredSize().width);
                }
                getColumnModel().getColumn(col).setPreferredWidth(maxW + 20);
            }
        }
        revalidate();
        repaint();
    }

    // ── Alignement ───────────────────────────────────────────────────────────

    /**
     * Définit l'alignement d'une cellule précise.
     *
     * @param row    ligne
     * @param col    colonne
     * @param hAlign SwingConstants.LEFT / CENTER / RIGHT
     * @param vAlign SwingConstants.TOP  / CENTER / BOTTOM
     */
    public void setCellAlignment(int row, int col, int hAlign, int vAlign) {
        hModel.setCellAlignment(row, col, hAlign, vAlign);
        refreshUI();
    }

    /** Aligne toutes les cellules d'une ligne. */
    public void setRowAlignment(int row, int hAlign, int vAlign) {
        for (int c = 0; c < getColumnCount(); c++)
            hModel.setCellAlignment(row, c, hAlign, vAlign);
        refreshUI();
    }

    /** Aligne toutes les cellules d'une colonne. */
    public void setColumnAlignment(int col, int hAlign, int vAlign) {
        for (int r = 0; r < getRowCount(); r++)
            hModel.setCellAlignment(r, col, hAlign, vAlign);
        refreshUI();
    }

    /** Aligne toutes les cellules de la sélection courante. */
    public void setSelectionAlignment(int hAlign, int vAlign) {
        for (int row : selectedRows)
            for (int c = 0; c < getColumnCount(); c++)
                hModel.setCellAlignment(row, c, hAlign, vAlign);
        refreshUI();
    }

    /** Aligne tout le tableau d'un coup. */
    public void setTableAlignment(int hAlign, int vAlign) {
        for (int r = 0; r < getRowCount(); r++)
            for (int c = 0; c < getColumnCount(); c++)
                hModel.setCellAlignment(r, c, hAlign, vAlign);
        refreshUI();
    }

    // ── Direction du texte ───────────────────────────────────────────────────

    /**
     * Définit la direction du texte dans une cellule.
     *
     * @param row       ligne
     * @param col       colonne
     * @param direction HSuperTable.TEXT_HORIZONTAL, TEXT_VERTICAL_UP ou TEXT_VERTICAL_DOWN
     */
    public void setCellTextDirection(int row, int col, int direction) {
        hModel.setCellTextDirection(row, col, direction);
        refreshUI();
    }

    /** Applique la direction à toute une colonne. */
    public void setColumnTextDirection(int col, int direction) {
        for (int r = 0; r < getRowCount(); r++)
            hModel.setCellTextDirection(r, col, direction);
        refreshUI();
    }

    /** Applique la direction à toute une ligne. */
    public void setRowTextDirection(int row, int direction) {
        for (int c = 0; c < getColumnCount(); c++)
            hModel.setCellTextDirection(row, c, direction);
        refreshUI();
    }

    // ── Marges de cellule ────────────────────────────────────────────────────

    /**
     * Définit les marges internes d'une cellule précise.
     * Passer null retire les marges custom et revient aux marges globales.
     *
     * @param row     ligne
     * @param col     colonne
     * @param margins Insets(top, left, bottom, right) en pixels
     */
    public void setCellMargins(int row, int col, Insets margins) {
        hModel.setCellMargins(row, col, margins);
        refreshUI();
    }

    /** Marges globales appliquées à toutes les cellules sans marge custom. */
    public void setDefaultCellMargins(Insets margins) {
        this.defaultCellMargins = margins;
        refreshUI();
    }

    public Insets getDefaultCellMargins() { return defaultCellMargins; }

    // ── Données ──────────────────────────────────────────────────────────────

    /**
     * Trie le tableau selon une colonne, dans l'ordre croissant ou décroissant.
     *
     * On utilise TableRowSorter pour ne pas perturber les données du modèle.
     * Le tri est visuel — les données sous-jacentes restent dans leur ordre
     * d'insertion.
     *
     * @param col   colonne de tri (0-indexée)
     * @param order SortOrder.ASCENDING ou SortOrder.DESCENDING
     */
    public void sortByColumn(int col, SortOrder order) {
        if (col < 0 || col >= getColumnCount()) return;
        TableRowSorter<HSuperDefaultTableModel> sorter = new TableRowSorter<>(hModel);
        setRowSorter(sorter);
        List<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(col, order));
        sorter.setSortKeys(keys);
        sorter.sort();
    }

    /**
     * Trie selon plusieurs colonnes en cascade.
     * La première colonne est le critère principal, la suivante sert de
     * départage, etc.
     *
     * @param cols   colonnes de tri, dans l'ordre de priorité
     * @param orders ordre de tri pour chaque colonne
     */
    public void sortByColumns(int[] cols, SortOrder[] orders) {
        if (cols == null || orders == null || cols.length != orders.length) return;
        TableRowSorter<HSuperDefaultTableModel> sorter = new TableRowSorter<>(hModel);
        setRowSorter(sorter);
        List<RowSorter.SortKey> keys = new ArrayList<>();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] >= 0 && cols[i] < getColumnCount())
                keys.add(new RowSorter.SortKey(cols[i], orders[i]));
        }
        sorter.setSortKeys(keys);
        sorter.sort();
    }

    /** Retire tout tri actif et revient à l'ordre naturel des données. */
    public void clearSort() {
        setRowSorter(null);
    }

    /**
     * Active la répétition de la ligne d'en-tête à l'impression (multi-pages).
     * Utilise l'API d'impression de JTable via le PrintMode.
     * Cette option n'a d'effet qu'au moment de l'impression.
     *
     * @param repeat true pour répéter l'en-tête sur chaque page imprimée
     */
    public void setHeaderRowRepeated(boolean repeat) {
        // JTable gère cela via getPrintable() — on stocke le flag pour l'utiliser
        // au moment de l'impression dans une méthode print() dédiée.
        putClientProperty("HSuperTable.repeatHeader", repeat);
    }

    public boolean isHeaderRowRepeated() {
        Object val = getClientProperty("HSuperTable.repeatHeader");
        return Boolean.TRUE.equals(val);
    }

    /**
     * Convertit le contenu du tableau en texte brut.
     * Chaque ligne devient une ligne de texte, les cellules sont séparées
     * par le délimiteur donné.
     *
     * @param delimiter séparateur entre les cellules (ex: "\t", ";", " | ")
     * @return le contenu du tableau sous forme de String
     */
    public String convertToText(String delimiter) {
        if (delimiter == null) delimiter = "\t";
        StringBuilder sb = new StringBuilder();
        // En-tête
        for (int c = 0; c < getColumnCount(); c++) {
            if (c > 0) sb.append(delimiter);
            sb.append(getColumnName(c));
        }
        sb.append("\n");
        // Données
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                if (c > 0) sb.append(delimiter);
                Object val = hModel.getValueAt(r, c);
                sb.append(val != null ? val.toString() : "");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ── Formules ─────────────────────────────────────────────────────────────

    /**
     * Insère une formule dans une cellule.
     * La formule est évaluée immédiatement et le résultat est affiché.
     * La formule brute est stockée dans le HTableCellModel pour permettre
     * la recalculation ultérieure.
     *
     * Formules supportées :
     * <pre>
     *   =SUM(A1:A5)      — somme des cellules A1 à A5
     *   =AVERAGE(B1:B3)  — moyenne de B1 à B3
     *   =COUNT(C1:C10)   — nombre de cellules non vides
     *   =MAX(A1:A5)      — valeur maximale
     *   =MIN(A1:A5)      — valeur minimale
     *   =A1+B2           — addition simple entre deux cellules
     * </pre>
     *
     * La notation de colonne est alphabétique (A=0, B=1, ...) et les
     * lignes sont 1-indexées (comme dans Excel/Word).
     *
     * @param row     ligne de la cellule cible (0-indexée)
     * @param col     colonne de la cellule cible (0-indexée)
     * @param formula la formule, doit commencer par "="
     */
    public void setCellFormula(int row, int col, String formula) {
        if (formula == null || !formula.startsWith("=")) return;
        // Stocker la formule brute dans les métadonnées de la cellule
        hModel.getCellModel(row, col).setFormula(formula);
        // Évaluer et afficher le résultat immédiatement
        Object result = evaluateFormula(row, col);
        hModel.setValueAt(result, row, col);
        refreshUI();
    }

    /**
     * Évalue la formule stockée dans une cellule et retourne le résultat.
     * Ne modifie pas le tableau — utile pour prévisualiser un calcul.
     *
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     * @return le résultat numérique, ou un message d'erreur si la formule
     *         est invalide ou si les données ne sont pas numériques
     */
    public Object evaluateFormula(int row, int col) {
        String formula = hModel.getCellModel(row, col).getFormula();
        if (formula == null || formula.isEmpty()) return hModel.getValueAt(row, col);
        return HTableFormula.evaluate(formula, hModel);
    }

    /**
     * Recalcule toutes les formules du tableau.
     * À appeler après une modification des données pour mettre à jour les
     * cellules qui contiennent des formules dépendantes.
     */
    public void recalculateAllFormulas() {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                String formula = hModel.getCellModel(r, c).getFormula();
                if (formula != null && !formula.isEmpty()) {
                    Object result = HTableFormula.evaluate(formula, hModel);
                    hModel.setValueAt(result, r, c);
                }
            }
        }
        refreshUI();
    }

    // ── Propriétés générales ─────────────────────────────────────────────────

    /**
     * Réinitialise le formatage d'une cellule (couleurs, bordures, alignement,
     * marges, direction) sans toucher à son contenu.
     */
    public void resetCellFormatting(int row, int col) {
        hModel.resetCellFormatting(row, col);
        refreshUI();
    }

    /** Efface tous les états visuels (hover, highlight, focus, sélection). */
    public void clearAllVisualStates() {
        highlightedRow = -1;
        hoveredRow     = -1;
        focusedRow     = -1;
        focusedColumn  = -1;
        selectedRows.clear();
        rowBackgroundColors.clear();
        rowForegroundColors.clear();
        visualStates.clear();
        refreshUI();
    }

    // =========================================================================
    // ÉTATS VISUELS — API interne utilisée par HTableController et HBasicTableUI
    // =========================================================================

    public void setHighlightedRow(int row)  { this.highlightedRow = row; refreshUI(); }
    public int  getHighlightedRow()         { return highlightedRow; }

    public void setHoveredRow(int row)      { this.hoveredRow = row; refreshUI(); }
    public int  getHoveredRow()             { return hoveredRow; }

    public void setFocusedCell(int row, int col) {
        this.focusedRow = row;
        this.focusedColumn = col;
        refreshUI();
    }
    public int getFocusedRow()    { return focusedRow; }
    public int getFocusedColumn() { return focusedColumn; }

    public void setEditing(boolean editing) { this.editingCell = editing; refreshUI(); }
    public boolean isEditingCell()          { return editingCell; }

    public void addSelectedRow(int row) {
        selectedRows.add(row);
        refreshUI();
    }

    public void removeSelectedRow(int row) {
        selectedRows.remove(row);
        refreshUI();
    }

    @Override
    public void clearSelection() {
        if (selectedRows == null) selectedRows = new HashSet<>();
        selectedRows.clear();
        super.clearSelection();
        refreshUI();
    }

    public Set<Integer> getRowsSelected() { return new HashSet<>(selectedRows); }

    public void setVisualState(String key, Object value) {
        visualStates.put(key, value);
        refreshUI();
    }

    public Object getVisualState(String key) { return visualStates.get(key); }

    // =========================================================================
    // ACCESSEURS INTERNES
    // =========================================================================

    public HSuperDefaultTableModel getHModel()  { return hModel; }
    public hsupertable.HSuperTableController getController() { return controller; }

    // =========================================================================
    // DIMENSIONS
    // =========================================================================

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getRowCount() > 0) {
            int headerH = getTableHeader() != null ? getTableHeader().getHeight() : 0;
            int rowsH   = 0;
            for (int r = 0; r < getRowCount(); r++) rowsH += getRowHeight(r);
            d.height = headerH + rowsH + 2;
        }
        d.width = Math.max(d.width, 200);
        return d;
    }

    @Override
    public Dimension getMinimumSize() { return new Dimension(100, 80); }

    @Override
    public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }

    // =========================================================================
    // UTILITAIRE INTERNE
    // =========================================================================

    /**
     * Déclenche un repaint + revalidate du tableau.
     * Toutes les méthodes publiques qui modifient l'état visuel appellent
     * cette méthode à la fin — jamais repaint() directement.
     */
    private void refreshUI() {
        revalidate();
        repaint();
    }
}