package hsupertable;

import hsupertable.formula.HTableFormula;
import hsupertable.style.HTableStyle;
import hsupertable.view.HBasicTableUI;
import hsupertable.model.HCellModel;
import hsupertable.model.HDefaultTableModel;
import hsupertable.menu.*;
import hsupertable.menu.MenuHandler;
import hsupertable.geometry.HTableGeometry.InternalCellHit;
import hsupertable.model.HCellSelectionModel;
import hsupertable.style.HTableStyle.HeaderStyle;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;
import javax.swing.table.JTableHeader;

import hsupertable.geometry.HTableStructureIntegrity;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

/**
 * HSuperTable — Tableau Swing avancé inspiré des outils tableau de Microsoft
 * Word.
 *
 * Point d'entrée unique de toutes les fonctionnalités. L'utilisateur n'a besoin
 * que de cette classe pour tout faire.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HTable extends JTable {

    // =========================================================================
    // CONSTANTES PUBLIQUES 
    // =========================================================================
    // -- Direction du texte --
    public static final int TEXT_HORIZONTAL = 0;
    public static final int TEXT_VERTICAL_UP = 1;
    public static final int TEXT_VERTICAL_DOWN = 2;

    // -- Styles de bordure --
    public static final int BORDER_SOLID = 0;
    public static final int BORDER_DASHED = 1;
    public static final int BORDER_DOTTED = 2;
    public static final int BORDER_DOUBLE = 3;

    // -- Côtés de bordure (utilisables en combinaison avec l'opérateur | ) --
    public static final int SIDE_TOP = 0b0001;
    public static final int SIDE_BOTTOM = 0b0010;
    public static final int SIDE_LEFT = 0b0100;
    public static final int SIDE_RIGHT = 0b1000;
    public static final int SIDE_ALL = 0b1111;
    public static final int SIDE_OUTER = 0b1111;   // alias sémantique de ALL
    public static final int SIDE_INNER = 0b10000;  // bit réservé, traité dans les méthodes

    // -- Modes d'ajustement automatique --
    public static final int AUTOFIT_CONTENT = 0;  // ajuste au contenu des cellules
    public static final int AUTOFIT_WINDOW = 1;  // ajuste à la largeur du parent

    // -- Modes d'interaction --
    public static final int MODE_NORMAL = 0;
    public static final int MODE_DRAW = 1;
    public static final int MODE_ERASE = 2;

    private int interactionMode = MODE_NORMAL;

    // =========================================================================
    // COMPOSANTS INTERNES
    // =========================================================================
    /**
     * Modèle de données — contient aussi toutes les métadonnées (spans,
     * cellModels).
     */
    private HDefaultTableModel hModel;

    private HCellSelectionModel cellSelectionModel;
    /**
     * Contrôleur des événements souris/clavier.
     */

    private MenuHandler menuController;

    // =========================================================================
    // ÉTATS VISUELS 
    // =========================================================================
    private int highlightedRow = -1;
    private int hoveredRow = -1;
    private int focusedRow = -1;
    private int focusedColumn = -1;
    private boolean editingCell = false;
    private boolean gridVisible = true;

    /**
     * Alignement horizontal de l'en-tête par colonne. Clé = index de colonne,
     * valeur = SwingConstants.LEFT / CENTER / RIGHT. Si absent, LEFT est
     * utilisé par défaut.
     */
    private final Map<Integer, Integer> columnHeaderAlignments = new HashMap<>();

    /**
     * Map des Styles visuels des en-têtes par colonne. Clé = index de colonne,
     * valeur = HeaderStyle personnalisé.
     */
    public final Map<Integer, HeaderStyle> headerStyles = new HashMap<>();

    /**
     * Couleurs de fond et de texte custom par ligne
     */
    private final Map<Integer, Color> rowBackgroundColors = new HashMap<>();
    private final Map<Integer, Color> rowForegroundColors = new HashMap<>();

    /**
     * États visuels génériques: focus, survol... (possibles extensions
     * futures). Pas utilisé pour l'instant.
     */
    private final Map<String, Object> visualStates = new HashMap<>();

    /**
     * Edieur de sous cellule.
     */
    private final JTextField internalEditor = new JTextField();

    /**
     * Éditeur flottant pour le renommage des colonnes. Positionné sur la
     * cellule d'en-tête au double-clic.
     */
    private final JTextField headerEditor = new JTextField();
    private int editingColumnIndex = -1;

    // =========================================================================
    // OPTIONS DE STYLE 
    // =========================================================================
    private boolean headerRowEnabled = true;
    private boolean totalRowEnabled = false;
    private boolean bandedRows = true;
    private boolean bandedColumns = false;
    private boolean firstColumnHighlighted = false;
    private boolean lastColumnHighlighted = false;

    // MARGES PAR DÉFAUT DES CELLULES
    /**
     * Marges internes appliquées à toutes les cellules sans marge custom.
     */
    private Insets defaultCellMargins = new Insets(6, 12, 6, 12);

    // STYLE VISUEL
    private HTableStyle tableStyle = HTableStyle.PRIMARY;

    //on stock la cellule qui subit l'opération
    private InternalCellHit focusedInternalCell, hoveredInternalCell,
            selectedInternalCell, editingInternalCell;

    // ÉTATS DE REDIMENSIONNEMENT
    private int resizeRowIndex = -1;   // index de la ligne en cours de resize (-1 = aucun)
    private int resizeColIndex = -1;   // index de la colonne en cours de resize (-1 = aucun)
    private int resizePreviewY = -1;   // position Y de la ligne de prévisualisation horizontale
    private int resizePreviewX = -1;   // position X de la ligne de prévisualisation verticale
    private boolean isResizingRow = false;
    private boolean isResizingCol = false;

    /**
     * Largeur de la zone cliquable de l'icône de tri, Constante utilisée par le
     * rendu (HBasicTableUI dessine l'icône dans cette zone) et la détection de
     * clic (HeaderHandler teste si le clic tombe dedans)
     */
    public static final int SORT_ICON_ZONE_WIDTH = 20;

    // Index de la colonne voisine droite lors du resize de colonne
    private int resizeColNeighborIndex = -1;

    // Largeur originale de la colonne voisine droite au moment du press
    private int resizeNeighborOriginalSize = -1;

    // CONSTRUCTEURS
    public HTable() {
        this(new HDefaultTableModel());
    }

    public HTable(HDefaultTableModel model) {
        super(model);
        this.hModel = model;
        this.cellSelectionModel = new HCellSelectionModel(getSelectionModel(), getColumnModel().getSelectionModel());
        this.menuController = new MenuHandler(this);
        setLayout(null);
        internalEditor.setVisible(false);
        add(internalEditor);
        // Éditeur d'en-tête — invisible par défaut
        headerEditor.setVisible(false);
        headerEditor.setBorder(BorderFactory.createLineBorder(new Color(13, 110, 253), 2));
        headerEditor.setFont(new Font("Segoe UI", Font.BOLD, 13));

// Validation à Enter
        headerEditor.addActionListener(e -> stopHeaderEdit());

// Validation à la perte de focus
        headerEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stopHeaderEdit();
            }
        });

// L'éditeur est ajouté sur le JTableHeader, pas sur le tableau lui-même
// On le fera dans installUI via le header — on l'ajoute après setUI()
        internalEditor.addActionListener(e -> stopInternalEdit());
        internalEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stopInternalEdit();
            }
        });
        setUI(new HBasicTableUI());
        initDefaults();
    }

    public HTable(Object[][] data, Object[] columnNames) {
        this(new HDefaultTableModel(data, columnNames));
    }

    public HTable(Vector<Vector<Object>> data, Vector<String> columnNames) {
        this(new HDefaultTableModel(data, columnNames));
    }

    public HTable(int rowCount, int columnCount) {
        this(new HDefaultTableModel(rowCount, columnCount));
    }

    /**
     * Constructeur de compatibilité : accepte n'importe quel TableModel.Si ce
     * n'est pas un HDefaultTableModel, les données sont converties.
     *
     * @param model
     */
    public HTable(TableModel model) {
        this(model instanceof HDefaultTableModel
                ? (HDefaultTableModel) model
                : convertToHDefaultTableModel(model));
    }

    /**
     * Conversion d'un TableModel standard vers HDefaultTableModel.
     */
    private static HDefaultTableModel convertToHDefaultTableModel(TableModel src) {
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        Object[] colNames = new Object[cols];
        for (int c = 0; c < cols; c++) {
            colNames[c] = src.getColumnName(c);
        }
        Object[][] data = new Object[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                data[r][c] = src.getValueAt(r, c);
            }
        }
        return new HDefaultTableModel(data, colNames);
    }

    /**
     * Configuration initiale commune à tous les constructeurs.
     */
    private void initDefaults() {
        setRowHeight(36);
        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        setGridColor(new Color(222, 226, 230));
        setSelectionBackground(new Color(13, 110, 253, 30));
        setSelectionForeground(Color.BLACK);
        setFillsViewportHeight(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setSelectionModel(new DefaultListSelectionModel());
        setColumnSelectionModel(new DefaultListSelectionModel());

        //Déclencheur du Drag and Drop
        getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() != e.getToIndex()) {
                    HTableStructureIntegrity.enforceColumnAdjacency(HTable.this);
                    refreshUI();
                }
            }

            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

    }

    public void setColumnSelectionModel(ListSelectionModel model) {
        getColumnModel().setSelectionModel(model);
        if (cellSelectionModel != null) {
            cellSelectionModel.setColumnModel(model);
        }
    }

    @Override
    public void setSelectionModel(ListSelectionModel newModel) {
        super.setSelectionModel(newModel);
        if (cellSelectionModel != null) {
            cellSelectionModel.setRowModel(newModel);
        }
    }

    public HCellSelectionModel getCellSelectionModel() {
        return cellSelectionModel;
    }

    // ── ONGLET CRÉATION ──────────────────────────────────────────────────────
    //  Options de style 
    /**
     * Active ou désactive la mise en forme spéciale de la ligne d'en-tête.Quand
     * elle est active, la première ligne reçoit le fond headerBackground du
     * style courant.
     *
     * @param enabled
     */
    public void setHeaderRowEnabled(boolean enabled) {
        this.headerRowEnabled = enabled;
        refreshUI();
    }

    public boolean isHeaderRowEnabled() {
        return headerRowEnabled;
    }

    /**
     * Active la ligne totale : la dernière ligne reçoit une mise en forme
     * distincte (fond totalRowBackground du style).
     *
     * @param enabled
     */
    public void setTotalRowEnabled(boolean enabled) {
        this.totalRowEnabled = enabled;
        refreshUI();
    }

    public boolean isTotalRowEnabled() {
        return totalRowEnabled;
    }

    /**
     * Active ou désactive l'alternance de couleurs sur les lignes
     *
     * @param enabled
     */
    public void setBandedRows(boolean enabled) {
        this.bandedRows = enabled;
        refreshUI();
    }

    public boolean isBandedRows() {
        return bandedRows;
    }

    /**
     * Active ou désactive l'alternance de couleurs sur les colonnes.
     *
     * @param enabled
     */
    public void setBandedColumns(boolean enabled) {
        this.bandedColumns = enabled;
        refreshUI();
    }

    public boolean isBandedColumns() {
        return bandedColumns;
    }

    /**
     * Met en valeur la première colonne (fond firstColumnBackground du style).
     */
    public void setFirstColumnHighlighted(boolean enabled) {
        this.firstColumnHighlighted = enabled;
        refreshUI();
    }

    public boolean isFirstColumnHighlighted() {
        return firstColumnHighlighted;
    }

    /**
     * Met en valeur la dernière colonne (fond lastColumnBackground du style).
     */
    public void setLastColumnHighlighted(boolean enabled) {
        this.lastColumnHighlighted = enabled;
        refreshUI();
    }

    public boolean isLastColumnHighlighted() {
        return lastColumnHighlighted;
    }

    // ── Styles prédéfinis ────────────────────────────────────────────────────
    public HTableStyle getTableStyle() {
        return tableStyle;
    }

    /**
     * Applique un style prédéfini au tableau. Met à jour les couleurs de
     * grille, sélection et en-tête immédiatement.
     */
    public void setTableStyle(HTableStyle style) {
        this.tableStyle = style;
        if (style == null) {
            return;
        }
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

    /**
     * Remet le style par défaut (PRIMARY) sans aucune personnalisation.
     */
    public void resetStyle() {
        setTableStyle(HTableStyle.PRIMARY);
        clearAllVisualStates();
    }

    // ── Trame de fond ────────────────────────────────────────────────────────
    /**
     * Définit la couleur de fond d'une cellule précise. Priorité maximale —
     * écrase tout le reste (style, bandes, hover, etc.).
     *
     * @param row ligne (0-indexée)
     * @param col colonne (0-indexée)
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
     * Applique une couleur de fond à toute une ligne. Utilise l'ancienne Map
     * rowBackgroundColors pour rester compatible avec le code existant.
     */
    public void setRowBackground(int row, Color color) {
        if (color == null) {
            rowBackgroundColors.remove(row);
        } else {
            rowBackgroundColors.put(row, color);
        }
        refreshUI();
    }

    public Color getRowBackground(int row) {
        return rowBackgroundColors.get(row);
    }

    /**
     * Applique une couleur de fond à toute une colonne.
     */
    public void setColumnBackground(int col, Color color) {
        for (int r = 0; r < getRowCount(); r++) {
            hModel.setCellBackground(r, col, color);
        }
        refreshUI();
    }

    /**
     * Applique une couleur de fond à la sélection courante.
     */
    public void setSelectionCellBackground(Color color) {
        for (int row : getRowsSelected()) {
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
        if (color == null) {
            rowForegroundColors.remove(row);
        } else {
            rowForegroundColors.put(row, color);
        }
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
     * Définit une bordure sur un ou plusieurs côtés d'une cellule.Exemple :
     * <pre>
     * // Bordure rouge épaisse en bas et à droite de la cellule (1,2)
     * table.setCellBorderSide(1, 2,
     * HTable.SIDE_BOTTOM | HTable.SIDE_RIGHT,
     * Color.RED, 2f, HTable.BORDER_SOLID);
     * </pre>
     *
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     * @param sides combinaison de SIDE_TOP, SIDE_BOTTOM, SIDE_LEFT, SIDE_RIGHT
     * @param color couleur de la bordure
     * @param thickness épaisseur en pixels
     * @param style BORDER_SOLID, BORDER_DASHED, BORDER_DOTTED ou BORDER_DOUBLE
     */
    public void setCellBorderSide(int row, int col, int sides,
            Color color, float thickness, int style) {
        hModel.setCellBorderSide(row, col, sides, color, thickness, style);
        refreshUI();
    }

    /**
     * Bordure identique sur les quatre côtés d'une cellule.
     *
     * @param row
     * @param col
     * @param color
     * @param thickness
     * @param style
     */
    public void setCellBorderAll(int row, int col,
            Color color, float thickness, int style) {
        hModel.setCellBorderSide(row, col, SIDE_ALL, color, thickness, style);
        refreshUI();
    }

    /**
     * Applique la même bordure sur toutes les cellules du tableau.
     *
     * @param color
     * @param thickness
     * @param style
     */
    public void setBorderAll(Color color, float thickness, int style) {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                hModel.setCellBorderSide(r, c, SIDE_ALL, color, thickness, style);
            }
        }
        refreshUI();
    }

    /**
     * Applique une bordure uniquement sur les bords extérieurs du tableau (haut
     * de la première ligne, bas de la dernière, gauche de la première colonne,
     * droite de la dernière colonne).
     *
     * @param color
     * @param thickness
     * @param style
     */
    public void setBorderOuter(Color color, float thickness, int style) {
        int lastRow = getRowCount() - 1;
        int lastCol = getColumnCount() - 1;
        for (int c = 0; c <= lastCol; c++) {
            hModel.setCellBorderSide(0, c, SIDE_TOP, color, thickness, style);
            hModel.setCellBorderSide(lastRow, c, SIDE_BOTTOM, color, thickness, style);
        }
        for (int r = 0; r <= lastRow; r++) {
            hModel.setCellBorderSide(r, 0, SIDE_LEFT, color, thickness, style);
            hModel.setCellBorderSide(r, lastCol, SIDE_RIGHT, color, thickness, style);
        }
        refreshUI();
    }

    /**
     * Applique une bordure sur toutes les séparations internes du tableau
     * (entre les lignes et entre les colonnes, mais pas sur les bords
     * externes).
     */
    public void setBorderInner(Color color, float thickness, int style) {
        int rows = getRowCount();
        int cols = getColumnCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c < cols - 1) {
                    hModel.setCellBorderSide(r, c, SIDE_RIGHT, color, thickness, style);
                }
                if (r < rows - 1) {
                    hModel.setCellBorderSide(r, c, SIDE_BOTTOM, color, thickness, style);
                }
            }
        }
        refreshUI();
    }

    /**
     * Applique une bordure sur toute la sélection courante.
     */
    public void setSelectionBorder(int sides, Color color, float thickness, int style) {
        for (int row : getRowsSelected()) {
            hModel.setCellBorderSide(row, getFocusedColumn(), sides, color, thickness, style);
        }
        refreshUI();
    }

    /**
     * Supprime toutes les bordures custom d'une cellule.
     */
    public void removeCellBorder(int row, int col) {
        hModel.clearCellBorders(row, col);
        refreshUI();
    }

    /**
     * Supprime un côté spécifique de la bordure d'une cellule.
     */
    public void removeCellBorderSide(int row, int col, int sides) {
        // On supprime en passant thickness=0 et color=null
        hModel.setCellBorderSide(row, col, sides, null, 0f, BORDER_SOLID);
        refreshUI();
    }

    /**
     * Supprime toutes les bordures custom du tableau entier.
     */
    public void removeAllBorders() {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                hModel.clearCellBorders(r, c);
            }
        }
        refreshUI();
    }

    // ── Sélection de zone ────────────────────────────────────────────────────────
    /**
     * Définit la sélection courante. Appelé par HTableController. Passer null
     * pour effacer la sélection.
     */
    public void setSelection(CellRange range) {
        if (range == null) {
            cellSelectionModel.clearSelection();
            refreshUI();
            return;
        }
        getSelectionModel().setSelectionInterval(range.rowStart, range.rowEnd);
        getColumnModel().getSelectionModel().setSelectionInterval(range.colStart, range.colEnd);
        refreshUI();
    }

    /**
     * Retourne la sélection courante, ou null si aucune.
     *
     * @return
     */
    public CellRange getSelection() {
        int[] r = cellSelectionModel.getSelectedCellRange();
        return (r == null) ? null : new CellRange(r[0], r[1], r[2], r[3]);
    }

    /**
     * Vrai si une sélection de zone est active.
     *
     * @return
     */
    public boolean hasSelection() {
        return cellSelectionModel.hasSelection();
    }

    /**
     * Applique une couleur de fond à toute la zone sélectionnée.Ne fait rien si
     * aucune sélection n'est active.
     *
     * @param color
     */
    public void applyBackgroundToSelection(Color color) {
        if (hasInternalFocus()) {
            focusedInternalCell.cell.style.setBackground(color);
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellBackground(r, c, color);
            }
        }
        refreshUI();
    }

    /**
     * Applique une couleur de texte à la sélection.
     */
    public void applyForegroundToSelection(Color color) {
        if (hasInternalFocus()) {
            focusedInternalCell.cell.style.setForeground(color);
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellForeground(r, c, color);
            }
        }
        refreshUI();
    }

    /**
     * Applique des bordures à la sélection.
     *
     * @param sides combinaison de SIDE_TOP, SIDE_BOTTOM, SIDE_LEFT, SIDE_RIGHT
     * @param color couleur de la bordure
     * @param thickness épaisseur en pixels
     * @param style BORDER_SOLID, BORDER_DASHED, BORDER_DOTTED ou BORDER_DOUBLE
     */
    public void applyBorderToSelection(int sides, Color color,
            float thickness, int style) {
        if (hasInternalFocus()) {
            HCellModel m = focusedInternalCell.cell.style;
            if ((sides & SIDE_TOP) != 0) {
                m.setBorderTopColor(color);
                m.setBorderTopThickness(thickness);
                m.setBorderTopStyle(style);
            }
            if ((sides & SIDE_BOTTOM) != 0) {
                m.setBorderBottomColor(color);
                m.setBorderBottomThickness(thickness);
                m.setBorderBottomStyle(style);
            }
            if ((sides & SIDE_LEFT) != 0) {
                m.setBorderLeftColor(color);
                m.setBorderLeftThickness(thickness);
                m.setBorderLeftStyle(style);
            }
            if ((sides & SIDE_RIGHT) != 0) {
                m.setBorderRightColor(color);
                m.setBorderRightThickness(thickness);
                m.setBorderRightStyle(style);
            }
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellBorderSide(r, c, sides, color, thickness, style);
            }
        }
        refreshUI();
    }

    /**
     * Applique un alignement à la sélection.
     */
    public void applyAlignmentToSelection(int hAlign, int vAlign) {
        if (hasInternalFocus()) {
            focusedInternalCell.cell.style.setAlignment(hAlign, vAlign);
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellAlignment(r, c, hAlign, vAlign);
            }
        }
        refreshUI();
    }

    /**
     * Applique une direction de texte à la sélection.
     */
    public void applyTextDirectionToSelection(int direction) {
        if (hasInternalFocus()) {
            focusedInternalCell.cell.style.setTextDirection(direction);
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellTextDirection(r, c, direction);
            }
        }
        refreshUI();
    }

    /**
     * Applique des marges internes à la sélection.
     */
    public void applyMarginsToSelection(Insets margins) {
        if (hasInternalFocus()) {
            focusedInternalCell.cell.style.setMargins(margins);
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellMargins(r, c, margins);
            }
        }
        refreshUI();
    }

    /**
     * Remet le formatage par défaut sur toute la sélection.
     */
    public void resetFormattingOnSelection() {
        if (hasInternalFocus()) {
            focusedInternalCell.cell.style.reset();
            repaint();
            return;
        }
        if (!hasSelection()) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.resetCellFormatting(r, c);
            }
        }
        refreshUI();
    }

    /**
     * Fusionne la zone sélectionnée. Remplace mergeSelectedCells() qui
     * dépendait de l'ancienne sélection par lignes.
     */
    public void mergeSelection() {
        CellRange sel = getSelection();
        if (sel == null || sel.isSingleCell()) {
            return;
        }
        if (!HTableStructureIntegrity.isMergeableViewRange(
                this, sel.rowStart, sel.rowEnd, sel.colStart, sel.colEnd)) {
            return;
        }
        int mr1 = toModelRow(sel.rowStart);
        int mr2 = toModelRow(sel.rowEnd);
        int mc1 = toModelColumn(sel.colStart);
        int mc2 = toModelColumn(sel.colEnd);
        if (!hModel.getMergeModel().canMergeSelection(
                Math.min(mr1, mr2), Math.min(mc1, mc2),
                Math.max(mr1, mr2), Math.max(mc1, mc2), hModel.getMergeModel())) {
            return;
        }
        mergeCells(sel.rowStart, sel.colStart, sel.rowEnd, sel.colEnd);
    }

    /**
     * Applique un style prédéfini uniquement sur la sélection.
     */
    public void applyStyleToSelection(HTableStyle style) {
        if (!hasSelection() || style == null) {
            return;
        }
        CellRange sel = getSelection();
        for (int r = sel.rowStart; r <= sel.rowEnd; r++) {
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                hModel.setCellBackground(r, c, (r % 2 == 0)
                        ? style.getCellBackground()
                        : style.getCellAlternateBackground());
                hModel.setCellForeground(r, c, style.getCellForeground());
            }
        }
        refreshUI();
    }

    public void setColumnClass(int columnIndex, Class<?> columnClass) {
        hModel.setColumnClass(columnIndex, columnClass);
    }

    // =========================================================================
    // ── ONGLET DISPOSITION ───────────────────────────────────────────────────
    // =========================================================================
    // ── Sélection ────────────────────────────────────────────────────────────
    /**
     * Sélectionne une cellule précise et met le focus dessus.
     */
    public void selectCell(int row, int col) {
        changeSelection(row, col, false, false);
        setFocusedCell(row, col);
    }

    /**
     * Sélectionne toute une ligne.
     */
    public void selectRow(int row) {
        if (row < 0 || row >= getRowCount() || getColumnCount() == 0) {
            return;
        }
        getSelectionModel().setSelectionInterval(row, row);
        getColumnModel().getSelectionModel().setSelectionInterval(0, getColumnCount() - 1);
        setFocusedCell(row, 0);
    }

    /**
     * Sélectionne toute une colonne — délègue à selectColumns() pour une
     * colonne unique.
     */
    public void selectColumn(int col) {
        selectColumns(col, col);
    }

    /**
     * Sélectionne une plage de colonnes, de colStart à colEnd (ordre
     * indifférent — CellRange normalise). Base de la sélection façon Word
     * depuis le header : clic = une colonne, clic-glisser = plusieurs.
     */
    public void selectColumns(int colStart, int colEnd) {
        if (getRowCount() == 0
                || colStart < 0 || colEnd < 0
                || colStart >= getColumnCount() || colEnd >= getColumnCount()) {
            return;
        }
        clearSelection(); // efface toute sélection de lignes précédente
        int lastRow = getRowCount() - 1;
        setSelection(new CellRange(0, colStart, lastRow, colEnd));
        setFocusedCell(0, Math.min(colStart, colEnd));
    }

    /**
     * Sélectionne tout le tableau.
     */
    public void selectAll() {
        ((HBasicTableUI) getUI()).getSelectionHandler().selectAll();
    }

    public int[] getSelectedRowsArray() {
        int[] rows = cellSelectionModel.getSelectedRows();
        int[] sorted = rows.clone();
        Arrays.sort(sorted);
        return sorted;
    }

    /**
     * Retourne les index des colonnes visibles (toutes, dans l'ordre).
     */
    public int[] getSelectedColumns() {
        int[] cols = new int[getColumnCount()];
        for (int c = 0; c < cols.length; c++) {
            cols[c] = c;
        }
        return cols;
    }

    // ── Quadrillage ──────────────────────────────────────────────────────────
    /**
     * Affiche ou masque le quadrillage léger entre les cellules. Ce quadrillage
     * est purement visuel — il ne crée pas de bordures réelles.
     */
    public void setGridVisible(boolean visible) {
        this.gridVisible = visible;
        refreshUI();
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    public void setInteractionMode(int mode) {
        this.interactionMode = mode;
        // Changer le curseur selon le mode
        switch (mode) {
            case MODE_DRAW ->
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            case MODE_ERASE ->
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            default ->
                setCursor(Cursor.getDefaultCursor());
        }
        refreshUI();
    }

    public int getInteractionMode() {
        return interactionMode;
    }

    public void splitCellLocally(int row, int col, int splitType, float dividerRatio) {
        if (hasInternalFocus()) {
            hModel.splitCellDirectly(focusedInternalCell.cell, splitType, dividerRatio);
            refreshUI();
            return;
        }
        hModel.splitCellLocally(toModelRow(row), toModelColumn(col), splitType, dividerRatio);;
        refreshUI();
    }

    /**
     * Subdivise la cellule focusée ou la cellule (row, col) en une grille de
     * nbRows × nbCols sous-cellules.
     *
     * Si une sous-cellule interne est focusée, la subdivision s'applique sur
     * elle. Sinon elle s'applique sur la cellule aux coordonnées données.
     *
     * @param row ligne de la cellule cible
     * @param col colonne de la cellule cible
     * @param nbRows nombre de lignes dans la grille
     * @param nbCols nombre de colonnes dans la grille
     */
    public void splitCellGrid(int row, int col, int nbRows, int nbCols) {
        if (nbRows < 1 || nbCols < 1) {
            return;
        }
        if (hasInternalFocus()) {
            hModel.splitCellGridDirectly(
                    focusedInternalCell.cell, nbRows, nbCols);
            refreshUI();
            return;
        }
        hModel.splitCellGrid(toModelRow(row), toModelColumn(col), nbRows, nbCols);
        refreshUI();
    }

    public void removeInternalGrid(int row, int col) {
        hModel.removeInternalGrid(toModelRow(row), toModelColumn(col));;
        refreshUI();
    }

    public void removeInternalGridFromFocused() {
        if (hasInternalFocus()) {

            hModel.removeInternalGridFromCell(
                    focusedInternalCell.cell,
                    getFocusedRow(),
                    getFocusedColumn()
            );
            setFocusedInternalCell(null);
            setSelectedInternalCell(null);
            refreshUI();
            return;
        }

        // Pas de sous-cellule focusée — gomme sur la cellule globale
        int row = getFocusedRow();
        int col = getFocusedColumn();
        if (row >= 0 && col >= 0) {
            hModel.removeInternalGrid(toModelRow(row), toModelColumn(col));
            refreshUI();
        }
    }

    public void setFocusedInternalCell(InternalCellHit hit) {
        this.focusedInternalCell = hit;
        repaint();
    }

    public InternalCellHit getFocusedInternalCell() {
        return focusedInternalCell;
    }

    public InternalCellHit getHoveredInternalCell() {
        return hoveredInternalCell;
    }

    public void setHoveredInternalCell(InternalCellHit hoveredInternalCell) {
        this.hoveredInternalCell = hoveredInternalCell;
        repaint();
    }

    public InternalCellHit getSelectedInternalCell() {
        return selectedInternalCell;
    }

    public void setSelectedInternalCell(
            InternalCellHit selectedInternalCell
    ) {

        this.selectedInternalCell = selectedInternalCell;
        repaint();
    }

    public InternalCellHit getEditingInternalCell() {
        return editingInternalCell;
    }

    public void startInternalEdit(InternalCellHit hit) {
        System.out.println("start internal");
        if (hit == null || hit.cell == null) {
            return;
        }

        editingInternalCell = hit;

        Rectangle r = hit.bounds;

        internalEditor.setBounds(r.x + 1, r.y + 1, r.width - 2, r.height - 2);

        Object value = hit.cell.value;

        internalEditor.setText(value != null ? value.toString() : "");

        internalEditor.setVisible(true);

        internalEditor.requestFocus();

        internalEditor.selectAll();
    }

    //on passe la valeur du textFiled à la cellule 
    public void stopInternalEdit() {

        if (editingInternalCell != null) {

            editingInternalCell.cell.value
                    = internalEditor.getText();
            System.out.println("Texte " + internalEditor.getText());
        }

        internalEditor.setVisible(false);

        editingInternalCell = null;

        repaint();

    }

    // ── Lignes et colonnes ───────────────────────────────────────────────────
    /**
     * Insère une ligne vide au-dessus de la ligne donnée.
     */
    public void insertRowAbove(int row) {
        if (row < 0 || row > getRowCount()) {
            return;
        }
        hModel.insertEmptyRow(row);
        refreshUI();
    }

    /**
     * Insère une ligne vide en-dessous de la ligne donnée.
     */
    public void insertRowBelow(int row) {
        insertRowAbove(row + 1);
    }

    /**
     * Insère une colonne vide à gauche de la colonne donnée.
     */
    public void insertColumnLeft(int col) {
        insertColumnLeft(col, null);
    }

    public void insertColumnLeft(int col, String nameColumn) {
        if (col < 0 || col > getColumnCount()) {
            return;
        }
        String name = (nameColumn == null) ? "Colonne " + (col + 1) : nameColumn + " " + (col + 1);
        int oldColCount = getColumnCount();
        int widthSource = (col < oldColCount) ? col : oldColCount - 1;
        insertColumnWithLayout(col, name, widthSource);
    }

    public void insertColumnRight(int col) {
        insertColumnRight(col, null);
    }

    /**
     * Insère une colonne vide à droite de la colonne donnée.
     */
    public void insertColumnRight(int col, String nameColumn) {
        if (col < 0 || col >= getColumnCount()) {
            return;
        }
        int insertAt = col + 1;
        String name = (nameColumn == null)
                ? "Colonne " + (insertAt + 1)
                : nameColumn + " " + (insertAt + 1);
        insertColumnWithLayout(insertAt, name, col);
    }

    /**
     * Insère une colonne à insertAt, en préservant les largeurs des colonnes
     * existantes et les hauteurs de lignes. widthSourceCol désigne, dans
     * l'ANCIENNE numérotation (avant insertion), la colonne dont la largeur
     * doit être reprise pour la nouvelle colonne. -1 ou hors bornes → largeur
     * de la dernière colonne existante, ou 100 si le tableau n'a aucune
     * colonne.
     */
    private void insertColumnWithLayout(int insertAt, String columnName, int widthSourceCol) {
        int oldColCount = getColumnCount();
        int[] savedWidths = new int[oldColCount];
        for (int c = 0; c < oldColCount; c++) {
            savedWidths[c] = getColumnModel().getColumn(c).getWidth();
        }

        int rowCount = getRowCount();
        int[] savedHeights = new int[rowCount];
        for (int r = 0; r < rowCount; r++) {
            savedHeights[r] = getRowHeight(r);
        }

        hModel.insertColumn(insertAt, columnName);

        int newColCount = getColumnCount();
        int defaultNewWidth;
        if (widthSourceCol >= 0 && widthSourceCol < oldColCount) {
            defaultNewWidth = savedWidths[widthSourceCol];
        } else {
            defaultNewWidth = oldColCount > 0 ? savedWidths[oldColCount - 1] : 100;
        }

        for (int c = 0; c < newColCount; c++) {
            int width;
            if (c < insertAt) {
                width = savedWidths[c];
            } else if (c == insertAt) {
                width = defaultNewWidth;
            } else {
                width = savedWidths[c - 1];
            }
            getColumnModel().getColumn(c).setPreferredWidth(width);
            getColumnModel().getColumn(c).setWidth(width);
        }

        for (int r = 0; r < Math.min(rowCount, getRowCount()); r++) {
            super.setRowHeight(r, savedHeights[r]);
        }

        refreshUI();
    }

    /**
     * Supprime la ligne à l'index donné.
     */
    public void deleteRow(int row) {
        if (row < 0 || row >= getRowCount()) {
            return;
        }
        hModel.removeRow(row);
        refreshUI();
    }

    /**
     * Supprime plusieurs lignes en une fois. On supprime de la fin vers le
     * début pour éviter le décalage d'index.
     *
     * @param rows tableau des index à supprimer (non trié, c'est géré ici)
     */
    public void deleteRows(int[] rows) {
        List<Integer> sorted = new ArrayList<>();
        for (int r : rows) {
            sorted.add(r);
        }
        sorted.sort(Collections.reverseOrder());  // suppression de bas en haut
        for (int r : sorted) {
            if (r >= 0 && r < getRowCount()) {
                hModel.removeRow(r);
            }
        }
        refreshUI();
    }

    /**
     * Supprime toutes les lignes de la sélection courante.
     */
    public void deleteSelectedRows() {
        deleteRows(getSelectedRowsArray());
        clearSelection();
    }

    /**
     * Supprime la colonne à l'index donné.
     */
    public void deleteColumn(int col) {
        if (col < 0 || col >= getColumnCount()) {
            return;
        }
        hModel.removeColumn(col);
        refreshUI();
    }

    /**
     * Supprime plusieurs colonnes en une fois. Même principe que deleteRows() :
     * de droite à gauche.
     */
    public void deleteColumns(int[] cols) {
        List<Integer> sorted = new ArrayList<>();
        for (int c : cols) {
            sorted.add(c);
        }
        sorted.sort(Collections.reverseOrder());
        for (int c : sorted) {
            if (c >= 0 && c < getColumnCount()) {
                hModel.removeColumn(c);
            }
        }
        refreshUI();
    }

    /**
     * Vide le tableau (supprime toutes les lignes, conserve les colonnes).
     */
    public void clearTable() {
        hModel.clear();
        refreshUI();
    }

    // ── Fusion ───────────────────────────────────────────────────────────────
    /**
     * Fusionne les cellules dans la zone (r1,c1) → (r2,c2). La cellule (r1,c1)
     * devient la cellule principale et concatenne son contenu à celui des
     * autres cellules. Les autres cellules de la zone sont vidées et marquées
     * comme absorbées.
     *
     * @param r1 ligne du coin supérieur gauche
     * @param c1 colonne du coin supérieur gauche
     * @param r2 ligne du coin inférieur droit
     * @param c2 colonne du coin inférieur droit
     */
    public void mergeCells(int r1, int c1, int r2, int c2) {
        hModel.mergeCells(toModelRow(r1), toModelColumn(c1), toModelRow(r2), toModelColumn(c2));
        refreshUI();
    }

    /**
     * Défusionne la cellule à la position donnée. Si la cellule est absorbée,
     * remonte à la cellule principale et la libère.
     */
    public void unmergeCell(int row, int col) {
        hModel.unmergeCell(toModelRow(row), toModelColumn(col));
        refreshUI();
    }

    /**
     * Fractionne une cellule fusionnée en targetRows × targetCols
     * sous-cellules.Exemple : une fusion 4×4 fractionnée en (2, 2) donne quatre
     * blocs de 2×2.
     *
     *
     * @param row ligne de la cellule à fractionner
     * @param col colonne de la cellule à fractionner
     * @param targetRows nombre de lignes dans le fractionnement
     * @param targetCols nombre de colonnes dans le fractionnement
     * @return
     */
    public boolean splitCell(int row, int col, int targetRows, int targetCols) {
        boolean success = hModel.splitCell(toModelRow(row), toModelColumn(col), targetRows, targetCols);
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
     * Coupe le tableau en deux à partir de la ligne donnée.Les lignes [0,
     * atRow-1] restent dans ce tableau. Les lignes [atRow, fin] sont retournées
     * dans un nouveau HSuperTable indépendant.
     *
     * @param atRow index de la première ligne du second tableau
     * @return un nouveau HTable contenant les lignes détachées
     */
    public HTable splitTable(int atRow) {
        if (atRow <= 0 || atRow >= getRowCount()) {
            return null;
        }

        int cols = getColumnCount();
        Object[] colNames = new Object[cols];
        for (int c = 0; c < cols; c++) {
            colNames[c] = getColumnName(c);
        }

        // Copier les lignes qui vont dans le nouveau tableau
        int newRowCount = getRowCount() - atRow;
        Object[][] newData = new Object[newRowCount][cols];
        for (int r = 0; r < newRowCount; r++) {
            for (int c = 0; c < cols; c++) {
                newData[r][c] = hModel.getValueAt(atRow + r, c);
            }
        }

        // Supprimer ces lignes du tableau courant (de bas en haut)
        for (int r = getRowCount() - 1; r >= atRow; r--) {
            hModel.removeRow(r);
        }

        HTable newTable = new HTable(newData, colNames);
        newTable.setTableStyle(this.tableStyle);
        refreshUI();
        return newTable;
    }

    // ── Taille de cellule ────────────────────────────────────────────────────
    /**
     * Définit la hauteur d'une ligne précise. JTable gère déjà
     * setRowHeight(int) globalement — on ajoute la version par ligne.
     */
    public void setRowHeight(int row, int height) {
        if (row >= 0 && row < getRowCount() && height > 0) {
            super.setRowHeight(row, height);
        }
    }

    /**
     * Applique la même hauteur à toutes les lignes.
     */
    public void setAllRowsHeight(int height) {
        if (height > 0) {
            super.setRowHeight(height);
        }
    }

    /**
     * Définit la largeur d'une colonne précise.
     */
    public void setColumnWidth(int col, int width) {
        if (col >= 0 && col < getColumnCount() && width > 0) {
            getColumnModel().getColumn(col).setPreferredWidth(width);
        }
    }

    /**
     * Applique la même largeur à toutes les colonnes.
     */
    public void setAllColumnsWidth(int width) {
        for (int c = 0; c < getColumnCount(); c++) {
            getColumnModel().getColumn(c).setPreferredWidth(width);
        }
    }

    /**
     * Répartit la hauteur de toutes les lignes de façon uniforme. La hauteur
     * cible est la moyenne des hauteurs actuelles.
     */
    public void distributeRowsEvenly() {
        if (getRowCount() == 0) {
            return;
        }
        int total = 0;
        for (int r = 0; r < getRowCount(); r++) {
            total += getRowHeight(r);
        }
        int avg = total / getRowCount();
        setAllRowsHeight(Math.max(avg, 20));
    }

    /**
     * Répartit la largeur de toutes les colonnes de façon uniforme. Utilise la
     * largeur totale actuelle du tableau divisée par le nombre de colonnes.
     */
    public void distributeColumnsEvenly() {
        if (getColumnCount() == 0) {
            return;
        }
        int totalW = 0;
        for (int c = 0; c < getColumnCount(); c++) {
            totalW += getColumnModel().getColumn(c).getWidth();
        }
        int avg = totalW / getColumnCount();
        setAllColumnsWidth(Math.max(avg, 20));
    }

    /**
     * Ajustement automatique selon le mode choisi.AUTOFIT_CONTENT : chaque
     * colonne s'adapte au contenu le plus large (en-tête inclus) + une marge de
     * 20px.
     *
     * AUTOFIT_WINDOW : toutes les colonnes se partagent équitablement la
     * largeur du composant parent visible.
     *
     * @param mode HTable.AUTOFIT_CONTENT ou HTable.AUTOFIT_WINDOW
     */
    public void autoFit(int mode) {
        if (mode == AUTOFIT_WINDOW) {
            // Largeur totale disponible = largeur du parent ou du viewport
            int availableWidth = getParent() != null ? getParent().getWidth() : getWidth();
            if (availableWidth <= 0 || getColumnCount() == 0) {
                return;
            }
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
     * @param row ligne
     * @param col colonne
     * @param hAlign SwingConstants.LEFT / CENTER / RIGHT
     * @param vAlign SwingConstants.TOP / CENTER / BOTTOM
     */
    public void setCellAlignment(int row, int col, int hAlign, int vAlign) {
        hModel.setCellAlignment(row, col, hAlign, vAlign);
        refreshUI();
    }

    /**
     * Aligne toutes les cellules d'une ligne.
     */
    public void setRowAlignment(int row, int hAlign, int vAlign) {
        for (int c = 0; c < getColumnCount(); c++) {
            hModel.setCellAlignment(row, c, hAlign, vAlign);
        }
        refreshUI();
    }

    /**
     * Aligne toutes les cellules d'une colonne.
     */
    public void setColumnAlignment(int col, int hAlign, int vAlign) {
        for (int r = 0; r < getRowCount(); r++) {
            hModel.setCellAlignment(r, col, hAlign, vAlign);
        }
        refreshUI();
    }

    /**
     * Aligne toutes les cellules de la sélection courante.
     */
    public void setSelectionAlignment(int hAlign, int vAlign) {
        for (int row : getRowsSelected()) {
            for (int c = 0; c < getColumnCount(); c++) {
                hModel.setCellAlignment(row, c, hAlign, vAlign);
            }
        }
        refreshUI();
    }

    /**
     * Aligne tout le tableau d'un coup.
     */
    public void setTableAlignment(int hAlign, int vAlign) {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                hModel.setCellAlignment(r, c, hAlign, vAlign);
            }
        }
        refreshUI();
    }

    // ── Direction du texte ───────────────────────────────────────────────────
    /**
     * Définit la direction du texte dans une cellule.
     *
     * @param row ligne
     * @param col colonne
     * @param direction HTable.TEXT_HORIZONTAL, TEXT_VERTICAL_UP ou
     * TEXT_VERTICAL_DOWN
     */
    public void setCellTextDirection(int row, int col, int direction) {
        hModel.setCellTextDirection(row, col, direction);
        refreshUI();
    }

    /**
     * Applique la direction à toute une colonne.
     */
    public void setColumnTextDirection(int col, int direction) {
        for (int r = 0; r < getRowCount(); r++) {
            hModel.setCellTextDirection(r, col, direction);
        }
        refreshUI();
    }

    /**
     * Applique la direction à toute une ligne.
     */
    public void setRowTextDirection(int row, int direction) {
        for (int c = 0; c < getColumnCount(); c++) {
            hModel.setCellTextDirection(row, c, direction);
        }
        refreshUI();
    }

    // ── Marges de cellule ────────────────────────────────────────────────────
    /**
     * Définit les marges internes d'une cellule précise. Passer null retire les
     * marges custom et revient aux marges globales.
     *
     * @param row ligne
     * @param col colonne
     * @param margins Insets(top, left, bottom, right) en pixels
     */
    public void setCellMargins(int row, int col, Insets margins) {
        hModel.setCellMargins(row, col, margins);
        refreshUI();
    }

    /**
     * Marges globales appliquées à toutes les cellules sans marge custom.
     */
    public void setDefaultCellMargins(Insets margins) {
        this.defaultCellMargins = margins;
        refreshUI();
    }

    public Insets getDefaultCellMargins() {
        return defaultCellMargins;
    }

    // ── Données ──────────────────────────────────────────────────────────────
    /**
     * Trie le tableau selon une colonne, dans l'ordre croissant ou décroissant.
     *
     * On utilise TableRowSorter pour ne pas perturber les données du modèle. Le
     * tri est visuel — les données sous-jacentes restent dans leur ordre
     * d'insertion.
     *
     * @param col colonne de tri (0-indexée)
     * @param order SortOrder.ASCENDING ou SortOrder.DESCENDING
     */
    public void sortByColumn(int col, SortOrder order) {
        if (col < 0 || col >= getColumnCount()) {
            return;
        }
        TableRowSorter<HDefaultTableModel> sorter = newNonClickableSorter();
        setRowSorter(sorter);
        List<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(col, order));
        sorter.setSortKeys(keys);
        sorter.sort();
    }

    /**
     * Trie selon plusieurs colonnes en cascade. La première colonne est le
     * critère principal, la suivante sert de départage, etc.
     *
     * @param cols colonnes de tri, dans l'ordre de priorité
     * @param orders ordre de tri pour chaque colonne
     */
    public void sortByColumns(int[] cols, SortOrder[] orders) {
        if (cols == null || orders == null || cols.length != orders.length) {
            return;
        }
        TableRowSorter<HDefaultTableModel> sorter = newNonClickableSorter();
        setRowSorter(sorter);
        List<RowSorter.SortKey> keys = new ArrayList<>();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] >= 0 && cols[i] < getColumnCount()) {
                keys.add(new RowSorter.SortKey(cols[i], orders[i]));
            }
        }
        sorter.setSortKeys(keys);
        sorter.sort();
    }

    /**
     * Retire tout tri actif et revient à l'ordre naturel des données.
     */
    public void clearSort() {
        setRowSorter(null);
    }

    /**
     * Active la répétition de la ligne d'en-tête à l'impression (multi-pages).
     * Utilise l'API d'impression de JTable via le PrintMode. Cette option n'a
     * d'effet qu'au moment de l'impression.
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
     * Convertit le contenu du tableau en texte brut. Chaque ligne devient une
     * ligne de texte, les cellules sont séparées par le délimiteur donné.
     *
     * @param delimiter séparateur entre les cellules (ex: "\t", ";", " | ")
     * @return le contenu du tableau sous forme de String
     */
    public String convertToText(String delimiter) {
        if (delimiter == null) {
            delimiter = "\t";
        }
        StringBuilder sb = new StringBuilder();
        // En-tête
        for (int c = 0; c < getColumnCount(); c++) {
            if (c > 0) {
                sb.append(delimiter);
            }
            sb.append(getColumnName(c));
        }
        sb.append("\n");
        // Données
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                if (c > 0) {
                    sb.append(delimiter);
                }
                Object val = hModel.getValueAt(r, c);
                sb.append(val != null ? val.toString() : "");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ── Formules ─────────────────────────────────────────────────────────────
    /**
     * Insère une formule dans une cellule. La formule est évaluée immédiatement
     * et le résultat est affiché. La formule brute est stockée dans le
     * HTableCellModel pour permettre la recalculation ultérieure.
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
     * La notation de colonne est alphabétique (A=0, B=1, ...) et les lignes
     * sont 1-indexées (comme dans Excel/Word).
     *
     * @param row ligne de la cellule cible (0-indexée)
     * @param col colonne de la cellule cible (0-indexée)
     * @param formula la formule, doit commencer par "="
     */
    public void setCellFormula(int row, int col, String formula) {
        if (formula == null || !formula.startsWith("=")) {
            return;
        }
        hModel.getCellModel(row, col).setFormula(formula);
        // Nouvelle signature avec coordonnées
        Object result = HTableFormula.evaluate(formula, hModel, row, col);
        hModel.setValueAt(result, row, col);
        refreshUI();
    }

    /**
     * Évalue la formule stockée dans une cellule et retourne le résultat. Ne
     * modifie pas le tableau — utile pour prévisualiser un calcul.
     *
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     * @return le résultat numérique, ou un message d'erreur si la formule est
     * invalide ou si les données ne sont pas numériques
     */
    public Object evaluateFormula(int row, int col) {
        String formula = hModel.getCellModel(row, col).getFormula();
        if (formula == null || formula.isEmpty()) {
            return hModel.getValueAt(row, col);
        }
        // Nouvelle signature avec coordonnées
        return HTableFormula.evaluate(formula, hModel, row, col);
    }

    /**
     * Recalcule toutes les formules du tableau. À appeler après une
     * modification des données pour mettre à jour les cellules qui contiennent
     * des formules dépendantes.
     */
    public void recalculateAllFormulas() {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                String formula = hModel.getCellModel(r, c).getFormula();
                if (formula != null && !formula.isEmpty()) {
                    // Nouvelle signature avec coordonnées
                    Object result = HTableFormula.evaluate(formula, hModel, r, c);
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

    /**
     * Efface tous les états visuels (hover, highlight, focus, sélection).
     */
    public void clearAllVisualStates() {
        highlightedRow = -1;
        hoveredRow = -1;
        focusedRow = -1;
        focusedColumn = -1;
        rowBackgroundColors.clear();
        rowForegroundColors.clear();
        visualStates.clear();
        refreshUI();
    }

    // ETATS VISUELS — API interne utilisée par HTableController et HBasicTableUI
    public void setHighlightedRow(int row) {
        this.highlightedRow = row;
        refreshUI();
    }

    public int getHighlightedRow() {
        return highlightedRow;
    }

    public void setHoveredRow(int row) {
        this.hoveredRow = row;
        refreshUI();
    }

    public int getHoveredRow() {
        return hoveredRow;
    }

    public void setFocusedCell(int row, int col) {
        this.focusedRow = row;
        this.focusedColumn = col;
        refreshUI();
    }

    public int getFocusedRow() {
        return focusedRow;
    }

    public int getFocusedColumn() {
        return focusedColumn;
    }

    public void setEditing(boolean editing) {
        this.editingCell = editing;
        refreshUI();
    }

    public boolean isEditingCell() {
        return editingCell;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // Les cellules absorbées ne sont pas éditables directement par JTable        
        if (hModel.isAbsorbed(toModelRow(row), toModelColumn(col))) {
            return false;
        }
        return super.isCellEditable(row, col);
    }

    @Override
    public void clearSelection() {
        getSelectionModel().clearSelection();
        getColumnModel().getSelectionModel().clearSelection();
        refreshUI();
    }

    /**
     * Après chaque tri, défait les fusions dont les lignes ne sont plus
     * adjacentes.
     */
    @Override
    public void setRowSorter(RowSorter<? extends TableModel> sorter) {
        super.setRowSorter(sorter);
        if (sorter != null) {
            sorter.addRowSorterListener(e -> {
                if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                    HTableStructureIntegrity.invalidateAllMergesOnSort(this);
                    refreshUI();
                }
            });
        }
    }

    public Set<Integer> getRowsSelected() {
        int[] rows = cellSelectionModel.getSelectedRows();
        Set<Integer> result = new HashSet<>();
        for (int r : rows) {
            result.add(r);
        }
        return result;
    }

    public void setVisualState(String key, Object value) {
        visualStates.put(key, value);
        refreshUI();
    }

    public Object getVisualState(String key) {
        return visualStates.get(key);
    }

    // ── Resize ───────────────────────────────────────────────────────────────
    public void setResizeRowIndex(int row) {
        this.resizeRowIndex = row;
    }

    public int getResizeRowIndex() {
        return resizeRowIndex;
    }

    public void setResizeColIndex(int col) {
        this.resizeColIndex = col;
    }

    public int getResizeColIndex() {
        return resizeColIndex;
    }

    public void setResizePreviewY(int y) {
        this.resizePreviewY = y;
    }

    public int getResizePreviewY() {
        return resizePreviewY;
    }

    public void setResizePreviewX(int x) {
        this.resizePreviewX = x;
    }

    public int getResizePreviewX() {
        return resizePreviewX;
    }

    public void setResizingRow(boolean b) {
        this.isResizingRow = b;
    }

    public boolean isResizingRow() {
        return isResizingRow;
    }

    public void setResizingCol(boolean b) {
        this.isResizingCol = b;
    }

    public boolean isResizingCol() {
        return isResizingCol;
    }

    public void setResizeColNeighborIndex(int col) {
        this.resizeColNeighborIndex = col;
    }

    public int getResizeColNeighborIndex() {
        return resizeColNeighborIndex;
    }

    public void setResizeNeighborOriginalSize(int size) {
        this.resizeNeighborOriginalSize = size;
    }

    public int getResizeNeighborOriginalSize() {
        return resizeNeighborOriginalSize;
    }

    // ── Alignement des en-têtes de colonnes ───────────────────────────────
    /**
     * Définit l'alignement horizontal du texte d'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param align SwingConstants.LEFT / CENTER / RIGHT
     */
    public void setColumnHeaderAlignment(int col, int align) {
        columnHeaderAlignments.put(col, align);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Retourne l'alignement horizontal de l'en-tête d'une colonne. LEFT par
     * défaut si non défini.
     *
     * @param col index de la colonne
     * @return SwingConstants.LEFT / CENTER / RIGHT
     */
    public int getColumnHeaderAlignment(int col) {
        return columnHeaderAlignments.getOrDefault(col, SwingConstants.LEFT);
    }

    // =========================================================================
// STYLES DES EN-TÊTES DE COLONNES
// =========================================================================
    /**
     * Retourne le HeaderStyle de la colonne donnée. Crée un HeaderStyle vide si
     * aucun n'existe encore pour cette colonne.
     *
     * @param col index de la colonne
     * @return HeaderStyle de la colonne
     */
    public HeaderStyle getHeaderStyle(int col) {
        return headerStyles.computeIfAbsent(col, k -> new HeaderStyle());
    }

    /**
     * Définit la couleur de fond de l'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param color couleur souhaitée, null pour revenir au style global
     */
    public void setHeaderBackground(int col, Color color) {
        getHeaderStyle(col).setBackground(color);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Définit la couleur du texte de l'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param color couleur souhaitée, null pour revenir au style global
     */
    public void setHeaderForeground(int col, Color color) {
        getHeaderStyle(col).setForeground(color);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Définit l'alignement horizontal du texte d'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param align SwingConstants.LEFT / CENTER / RIGHT
     */
    public void setHeaderAlignment(int col, int align) {
        getHeaderStyle(col).setHorizontalAlignment(align);
        // Mise à jour de l'ancienne Map pour rétrocompatibilité
        columnHeaderAlignments.put(col, align);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Active ou désactive le gras sur l'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param bold true = gras
     */
    public void setHeaderBold(int col, boolean bold) {
        getHeaderStyle(col).setBold(bold);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Active ou désactive l'italique sur l'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param italic true = italique
     */
    public void setHeaderItalic(int col, boolean italic) {
        getHeaderStyle(col).setItalic(italic);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Définit la taille du texte de l'en-tête d'une colonne.
     *
     * @param col index de la colonne
     * @param fontSize taille en points, -1 pour revenir à la taille globale
     */
    public void setHeaderFontSize(int col, float fontSize) {
        getHeaderStyle(col).setFontSize(fontSize);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Remet le style de l'en-tête d'une colonne aux valeurs par défaut.
     *
     * @param col index de la colonne
     */
    public void resetHeaderStyle(int col) {
        headerStyles.remove(col);
        columnHeaderAlignments.remove(col);
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    /**
     * Remet tous les en-têtes aux valeurs par défaut du style global.
     */
    public void resetAllHeaderStyles() {
        headerStyles.clear();
        columnHeaderAlignments.clear();
        if (getTableHeader() != null) {
            getTableHeader().repaint();
        }
    }

    // =========================================================================
    // ACCESSEURS INTERNES
    // =========================================================================
    public HDefaultTableModel getHModel() {
        return hModel;
    }

    // =========================================================================
    // DIMENSIONS
    // =========================================================================
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getRowCount() > 0) {
            int headerH = getTableHeader() != null ? getTableHeader().getHeight() : 0;
            int rowsH = 0;
            for (int r = 0; r < getRowCount(); r++) {
                rowsH += getRowHeight(r);
            }
            d.height = headerH + rowsH + 2;
        }
        d.width = Math.max(d.width, 200);
        return d;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 80);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // =========================================================================
    // UTILITAIRE INTERNE
    // =========================================================================
// MENU CONTEXTUEL — GESTION DES ACTIONS
    /**
     * Ajoute une action personnalisée à la fin de la liste. Elle sera évaluée
     * comme toutes les autres actions système via isVisible() et isEnabled() au
     * moment du clic droit.
     *
     * @param action l'action à ajouter
     */
    public void addContextAction(ContextAction action) {
        menuController.addContextAction(action);
    }

    /**
     * Supprime une action de la liste par référence.
     *
     * @param action l'action à supprimer
     */
    public void removeContextAction(ContextAction action) {
        menuController.removeContextAction(action);
    }

    /**
     * Supprime toutes les actions personnalisées ET système. À utiliser si le
     * développeur veut repartir d'un menu vide.
     */
    public void clearContextActions() {
        menuController.clearContextActions();
    }

    /**
     * Permet juste de voir les actions enregistrées
     *
     * @return liste non modifiable des actions
     */
    public List<ContextAction> getContextActions() {
        return menuController.getContextActions();
    }

    // MENU HEADER — GESTION DES ACTIONS
    /**
     * Ajoute une action personnalisée à la fin de la liste des actions header.
     *
     * @param action l'action à ajouter
     */
    public void addHeaderAction(HeaderAction action) {
        menuController.addHeaderAction(action);
    }

    /**
     * Supprime une action header de la liste par référence.
     *
     * @param action l'action à supprimer
     */
    public void removeHeaderAction(HeaderAction action) {
        menuController.removeHeaderAction(action);
    }

    /**
     * Supprime toutes les actions header personnalisées ET système. Prévue si
     * le développeur veut repartir d'un menu vide.
     */
    public void clearHeaderActions() {
        menuController.clearHeaderActions();
    }

    /**
     * Retourne une vue non modifiable de la liste des actions header. Utile
     * pour inspecter les actions enregistrées.
     */
    public List<HeaderAction> getHeaderActions() {
        return menuController.getHeaderActions();
    }

    // =========================================================================
// MENU CONTEXTUEL — ACTIONS PAR DÉFAUT
// =========================================================================
// =========================================================================
// MENU CONTEXTUEL — CONSTRUCTION ET AFFICHAGE
// =========================================================================
    /**
     * Construit et affiche le menu contextuel pour le contexte donné.
     *
     * @param ctx contexte du clic droit
     * @param x position X souris dans le tableau
     * @param y position Y souris dans le tableau
     */
    public void showContextMenu(TableContext ctx, int x, int y) {
        menuController.showContextMenu(ctx, x, y);
    }

    // =========================================================================
// MENU HEADER — CONSTRUCTION ET AFFICHAGE
// =========================================================================
    /**
     * Construit et affiche le menu contextuel des en-têtes pour le contexte
     * donné.
     *
     * @param ctx contexte du clic droit sur l'en-tête
     * @param x position X souris dans l'en-tête
     * @param y position Y souris dans l'en-tête
     */
    public void showHeaderMenu(HeaderContext ctx, int x, int y) {
        menuController.showHeaderMenu(ctx, x, y);
    }

    // =========================================================================
// ÉDITION DU NOM DE COLONNE
// =========================================================================
    /**
     * Démarre l'édition du nom de la colonne à l'index donné. Positionne le
     * JTextField sur la cellule d'en-tête et lui donne le focus.
     *
     * @param colIndex index de la colonne à renommer
     */
    public void startHeaderEdit(int colIndex) {
        if (colIndex < 0 || colIndex >= getColumnCount()) {
            return;
        }

        JTableHeader header = getTableHeader();
        if (header == null) {
            return;
        }

        // S'assurer que l'éditeur est bien sur le header
        if (headerEditor.getParent() != header) {
            header.setLayout(null);
            header.add(headerEditor);
        }

        editingColumnIndex = colIndex;

        // Rectangle de la cellule d'en-tête ciblée
        Rectangle rect = header.getHeaderRect(colIndex);

        // Positionner le JTextField exactement sur la cellule
        headerEditor.setBounds(
                rect.x + 2,
                rect.y + 2,
                rect.width - 4,
                rect.height - 4
        );

        // Pré-remplir avec le nom actuel
        headerEditor.setText(getColumnName(colIndex));
        headerEditor.setVisible(true);
        headerEditor.requestFocus();
        headerEditor.selectAll();

        header.repaint();
    }

    /**
     * Valide et applique le nouveau nom de colonne. Cache l'éditeur et met à
     * jour le TableColumnModel.
     */
    public void stopHeaderEdit() {
        if (editingColumnIndex < 0) {
            return;
        }

        String newName = headerEditor.getText().trim();

        // Appliquer le nouveau nom si non vide
        if (!newName.isEmpty() && editingColumnIndex < getColumnCount()) {
            getColumnModel()
                    .getColumn(editingColumnIndex)
                    .setHeaderValue(newName);
        }

        headerEditor.setVisible(false);
        editingColumnIndex = -1;

        // Rafraîchir l'en-tête
        JTableHeader header = getTableHeader();
        if (header != null) {
            header.repaint();
        }
    }

    /**
     * Ouvre la boîte de dialogue de subdivision en grille. Si l'utilisateur
     * confirme, applique la subdivision sur la cellule focusée ou sur la
     * cellule aux coordonnées données.
     *
     * @param row ligne de la cellule cible
     * @param col colonne de la cellule cible
     */
    public void showSplitCellDialog(int row, int col) {
        menuController.showSplitCellDialog(row, col);
    }

    /**
     * Ouvre la boîte de dialogue de formule (style Microsoft Word).
     *
     * Pré-remplit la formule selon le contexte : - Si des valeurs existent
     * au-dessus → propose =SUM(ABOVE) - Si des valeurs existent à gauche →
     * propose =SUM(LEFT) - Sinon → propose =
     *
     * @param row ligne de la cellule cible
     * @param col colonne de la cellule cible
     */
    public void showFormulaDialog(int row, int col) {
        menuController.showFormulaDialog(row, col);
    }

    /**
     * Résout les coordonnées réelles sous un point souris. Si la cellule sous
     * le point est absorbée, retourne les coordonnées de la cellule principale
     * de la fusion.
     *
     * @param point point souris
     * @return int[]{row, col} de la cellule principale
     */
    public int[] resolvePoint(Point point) {
        int row = rowAtPoint(point);
        int col = columnAtPoint(point);
        if (row < 0 || col < 0) {
            return new int[]{row, col};
        }
        int modelRow = toModelRow(row);
        int modelCol = toModelColumn(col);
        if (hModel.isAbsorbed(modelRow, modelCol)) {
            Point origin = hModel.findMergeOrigin(modelRow, modelCol);
            if (origin != null) {
                // Position VUE de la cellule principale
                return new int[]{convertRowIndexToView(origin.x), convertColumnIndexToView(origin.y)};
            }
        }
        return new int[]{row, col};
    }

    /**
     * Convertit un index de ligne VUE en index MODÈLE (-1 si hors bornes).
     */
    public int toModelRow(int viewRow) {
        return (viewRow >= 0 && viewRow < getRowCount()) ? convertRowIndexToModel(viewRow) : -1;
    }

    /**
     * Convertit un index de colonne VUE en index MODÈLE (-1 si hors bornes).
     */
    public int toModelColumn(int viewCol) {
        return (viewCol >= 0 && viewCol < getColumnCount()) ? convertColumnIndexToModel(viewCol) : -1;
    }

    /**
     * Déclenche un repaint + revalidate du tableau. Toutes les méthodes
     * publiques qui modifient l'état visuel appellent cette méthode à la fin —
     * jamais repaint() directement.
     */
    public void refreshUI() {
        revalidate();
        repaint();
    }

    /**
     * Représente une zone rectangulaire sélectionnée. Toujours normalisée :
     * rowStart <= rowEnd, colStart <= colEnd.
     */
    public static class CellRange {

        public final int rowStart, colStart, rowEnd, colEnd;

        public CellRange(int r1, int c1, int r2, int c2) {
            this.rowStart = Math.min(r1, r2);
            this.colStart = Math.min(c1, c2);
            this.rowEnd = Math.max(r1, r2);
            this.colEnd = Math.max(c1, c2);
        }

        public boolean isSingleCell() {
            return rowStart == rowEnd && colStart == colEnd;
        }

        public boolean contains(int r, int c) {
            return r >= rowStart && r <= rowEnd
                    && c >= colStart && c <= colEnd;
        }

        public int rowCount() {
            return rowEnd - rowStart + 1;
        }

        public int colCount() {
            return colEnd - colStart + 1;
        }

        @Override
        public String toString() {
            return "CellRange[(" + rowStart + "," + colStart + ")→("
                    + rowEnd + "," + colEnd + ")]";
        }
    }

    /**
     * Vrai si une sous-cellule interne est actuellement focusée. Utilisé par
     * toutes les méthodes de formatage pour router l'action vers la bonne
     * cible.
     */
    public boolean hasInternalFocus() {
        return focusedInternalCell != null && focusedInternalCell.parent != null;
    }

    /**
     * Toujours ignorée : HTable ne doit jamais avoir de TableRowSorter
     * auto-créé par JTable lui-même. Un sorter par défaut n'aurait pas notre
     * isSortable() = false, et réintroduirait le tri au clic n'importe où sur
     * l'en-tête — exactement ce que sortByColumn()/sortByColumns() neutralisent
     * en construisant toujours leur propre sorter. Le tri ne doit passer que
     * par ces deux méthodes, jamais par l'auto-création de JTable
     * (getAutoCreateRowSorter() reste donc toujours false, sans override
     * nécessaire : le champ interne de JTable n'est jamais touché).
     */
    @Override
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
        // volontairement vide
    }

    /**
     * TableRowSorter dont aucune colonne n'est "sortable" au sens de
     * toggleSortOrder() — c'est ce que vérifie en premier le clic natif du
     * JTableHeader avant d'agir. Ça neutralise le tri-au-clic-n'importe-où de
     * Swing sans toucher à setSortKeys(), notre propre chemin de tri, qui ne
     * passe pas par ce garde-fou.
     */
    private TableRowSorter<HDefaultTableModel> newNonClickableSorter() {
        return new TableRowSorter<>(hModel) {
            @Override
            public boolean isSortable(int column) {
                return false;
            }
        };
    }

    //Etat de tri d'une colonne
    public SortOrder getViewColumnSortOrder(int viewCol) {
        RowSorter<? extends TableModel> sorter = getRowSorter();
        if (sorter == null) {
            return SortOrder.UNSORTED;
        }
        int modelCol = toModelColumn(viewCol);
        for (RowSorter.SortKey key : sorter.getSortKeys()) {
            if (key.getColumn() == modelCol) {
                return key.getSortOrder();
            }
        }
        return SortOrder.UNSORTED;
    }

}
