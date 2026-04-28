package stable;


import hsupertable.*;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * HSuperTable — Tableau Swing avancé  
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
    private final HSuperTableController controller;

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
     * Active ou désactive la mise en forme spéciale de la ligne d'en-tête.
     * Quand elle est active, la première ligne reçoit le fond headerBackground
     * du style courant.
     */
    public void setHeaderRowEnabled(boolean enabled) {
        this.headerRowEnabled = enabled;
        refreshUI();
    }

    public boolean isHeaderRowEnabled() { return headerRowEnabled; }

    /**
     * Active la ligne totale : la dernière ligne reçoit une mise en forme
     * distincte (fond totalRowBackground du style).
     */
    public void setTotalRowEnabled(boolean enabled) {
        this.totalRowEnabled = enabled;
        refreshUI();
    }

    public boolean isTotalRowEnabled() { return totalRowEnabled; }

    /** Active ou désactive l'alternance de couleurs sur les lignes (zebra). */
    public void setBandedRows(boolean enabled) {
        this.bandedRows = enabled;
        refreshUI();
    }

    public boolean isBandedRows() { return bandedRows; }

    /** Active ou désactive l'alternance de couleurs sur les colonnes. */
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
    public HSuperTableController getController() { return controller; }

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