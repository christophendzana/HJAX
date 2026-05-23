package hsupertable;

import hcomponents.HMenu;
import hcomponents.HMenuItem;
import hcomponents.HPopupMenu;
import hcomponents.HTextField;
import hsupertable.HBasicTableUI.InternalCellHit;
import hsupertable.HSuperDefaultTableModel.Cell;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;
import javax.swing.table.JTableHeader;

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
public class HSuperTable extends JTable {

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
    private final HSuperDefaultTableModel hModel;

    /**
     * Contrôleur des événements souris/clavier.
     */
    private final HSuperTableController controller;

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
     * Lignes sélectionnées (sélection multiple custom).
     */
    private ArrayList<Integer> selectedRows = new ArrayList<>();

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
     * Sélection de zone courante — null si aucune sélection active.
     */
    private CellRange currentSelection = null;

    private final HTextField internalEditor = new HTextField();

    /**
     * Éditeur flottant pour le renommage des colonnes. Positionné sur la
     * cellule d'en-tête au double-clic.
     */
    private final HTextField headerEditor = new HTextField();
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
    private HSuperTableStyle tableStyle = HSuperTableStyle.PRIMARY;

    /**
     * Liste des actions du menu contextuel. Contient les actions prédéfinies et
     * les actions personnalisées du développeur
     */
    private final List<ContextAction> contextActions = new ArrayList<>();

    /**
     * Liste des actions du menu contextuel des en-têtes. Contient les actions
     * conçue et les actions personnalisées du développeur
     */
    private final List<HeaderAction> headerActions = new ArrayList<>();

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

    // Index de la colonne voisine droite lors du resize de colonne
    private int resizeColNeighborIndex = -1;

    // Largeur originale de la colonne voisine droite au moment du press
    private int resizeNeighborOriginalSize = -1;

    // CONSTRUCTEURS
    public HSuperTable() {
        this(new HSuperDefaultTableModel());
    }

    public HSuperTable(HSuperDefaultTableModel model) {
        super(model);
        this.hModel = model;
        this.controller = new hsupertable.HSuperTableController(this);
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
        initializeDefaultContextActions();
        initializeDefaultHeaderActions();
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
     * Constructeur de compatibilité : accepte n'importe quel TableModel.Si ce
     * n'est pas un HDefaultTableModel, les données sont converties.
     *
     * @param model
     */
    public HSuperTable(TableModel model) {
        this(model instanceof HSuperDefaultTableModel
                ? (HSuperDefaultTableModel) model
                : convertToHDefaultTableModel(model));
    }

    /**
     * Conversion d'un TableModel standard vers HDefaultTableModel.
     */
    private static HSuperDefaultTableModel convertToHDefaultTableModel(TableModel src) {
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
        return new HSuperDefaultTableModel(data, colNames);
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
    public HSuperTableStyle getTableStyle() {
        return tableStyle;
    }

    /**
     * Applique un style prédéfini au tableau. Met à jour les couleurs de
     * grille, sélection et en-tête immédiatement.
     */
    public void setTableStyle(HSuperTableStyle style) {
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
        setTableStyle(HSuperTableStyle.PRIMARY);
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
        for (int row : selectedRows) {
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
        this.currentSelection = range;
        refreshUI();
    }

    /**
     * Retourne la sélection courante, ou null si aucune.
     *
     * @return
     */
    public CellRange getSelection() {
        return currentSelection;
    }

    /**
     * Vrai si une sélection de zone est active.
     *
     * @return
     */
    public boolean hasSelection() {
        return currentSelection != null;
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
            HSuperTableCellModel m = focusedInternalCell.cell.style;
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
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
        if (!hasSelection() || currentSelection.isSingleCell()) {
            return;
        }
        mergeCells(currentSelection.rowStart, currentSelection.colStart,
                currentSelection.rowEnd, currentSelection.colEnd);
    }

    /**
     * Applique un style prédéfini uniquement sur la sélection.
     */
    public void applyStyleToSelection(HSuperTableStyle style) {
        if (!hasSelection() || style == null) {
            return;
        }
        for (int r = currentSelection.rowStart; r <= currentSelection.rowEnd; r++) {
            for (int c = currentSelection.colStart; c <= currentSelection.colEnd; c++) {
                hModel.setCellBackground(r, c, (r % 2 == 0)
                        ? style.getCellBackground()
                        : style.getCellAlternateBackground());
                hModel.setCellForeground(r, c, style.getCellForeground());
            }
        }
        refreshUI();
    }

    // =========================================================================
    // ── ONGLET DISPOSITION ───────────────────────────────────────────────────
    // =========================================================================
    // ── Sélection ────────────────────────────────────────────────────────────
    /**
     * Sélectionne une cellule précise et met le focus dessus.
     */
    public void selectCell(int row, int col) {
        clearSelection();
        addSelectedRow(row);
        setFocusedCell(row, col);
    }

    /**
     * Sélectionne toute une ligne.
     */
    public void selectRow(int row) {
        clearSelection();
        addSelectedRow(row);
        setFocusedCell(row, 0);
    }

    /**
     * Sélectionne toute une colonne. JTable n'a pas de notion de "colonne
     * sélectionnée" dans notre modèle custom — on met le focus sur la colonne
     * et on sélectionne toutes les lignes pour signaler visuellement la
     * sélection.
     */
    public void selectColumn(int col) {
        clearSelection();
        for (int r = 0; r < getRowCount(); r++) {
            addSelectedRow(r);
        }
        setFocusedCell(0, col);
    }

    /**
     * Sélectionne tout le tableau.
     */
    public void selectAll() {
        clearSelection();
        for (int r = 0; r < getRowCount(); r++) {
            addSelectedRow(r);
        }
    }

    public int[] getSelectedRowsArray() {
        return selectedRows.stream().mapToInt(Integer::intValue).sorted().toArray();
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
        hModel.splitCellLocally(row, col, splitType, dividerRatio);
        refreshUI();
    }

    public void removeInternalGrid(int row, int col) {
        hModel.removeInternalGrid(row, col);
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
            hModel.removeInternalGrid(row, col);
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

    public HBasicTableUI.InternalCellHit getHoveredInternalCell() {
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

        // ── Sauvegarder les largeurs actuelles avant insertion ────────────────
        int oldColCount = getColumnCount();
        int[] savedWidths = new int[oldColCount];
        for (int c = 0; c < oldColCount; c++) {
            savedWidths[c] = getColumnModel().getColumn(c).getWidth();
        }

        // ── Sauvegarder les hauteurs de lignes ────────────────────────────────
        int rowCount = getRowCount();
        int[] savedHeights = new int[rowCount];
        for (int r = 0; r < rowCount; r++) {
            savedHeights[r] = getRowHeight(r);
        }

        // ── Insertion ─────────────────────────────────────────────────────────
        String name = (nameColumn == null)
                ? "Colonne " + (col + 1)
                : nameColumn + " " + (col + 1);
        hModel.insertColumn(col, name);

        // ── Restaurer les largeurs après insertion ────────────────────────────
        // Les colonnes avant col conservent leur largeur.
        // La nouvelle colonne reçoit la largeur moyenne des colonnes existantes.
        // Les colonnes après col récupèrent leur largeur d'origine.
        int newColCount = getColumnCount();
        int defaultNewWidth = oldColCount > 0
                ? savedWidths[(col < oldColCount) ? col : oldColCount - 1]
                : 100;

        for (int c = 0; c < newColCount; c++) {
            int width;
            if (c < col) {
                width = savedWidths[c];
            } else if (c == col) {
                width = defaultNewWidth;
            } else {
                width = savedWidths[c - 1];
            }
            getColumnModel().getColumn(c).setPreferredWidth(width);
            getColumnModel().getColumn(c).setWidth(width);
        }

        // ── Restaurer les hauteurs de lignes ──────────────────────────────────
        for (int r = 0; r < Math.min(rowCount, getRowCount()); r++) {
            super.setRowHeight(r, savedHeights[r]);
        }

        refreshUI();
    }

    /**
     * Insère une colonne vide à droite de la colonne donnée.
     */
    public void insertColumnRight(int col) {
        if (col < 0 || col >= getColumnCount()) {
            return;
        }

        // ── Sauvegarder les largeurs actuelles avant insertion ────────────────
        int oldColCount = getColumnCount();
        int[] savedWidths = new int[oldColCount];
        for (int c = 0; c < oldColCount; c++) {
            savedWidths[c] = getColumnModel().getColumn(c).getWidth();
        }

        // ── Sauvegarder les hauteurs de lignes ────────────────────────────────
        int rowCount = getRowCount();
        int[] savedHeights = new int[rowCount];
        for (int r = 0; r < rowCount; r++) {
            savedHeights[r] = getRowHeight(r);
        }

        // ── Insertion à droite = insertion à gauche de col + 1 ────────────────
        int insertAt = col + 1;
        hModel.insertColumn(insertAt, "Colonne " + (insertAt + 1));

        // ── Restaurer les largeurs après insertion ────────────────────────────
        // La nouvelle colonne reçoit la largeur de la colonne de gauche (col).
        int newColCount = getColumnCount();
        for (int c = 0; c < newColCount; c++) {
            int width;
            if (c < insertAt) {
                width = savedWidths[c];
            } else if (c == insertAt) {
                // Largeur de la colonne source (celle de gauche)
                width = savedWidths[col];
            } else {
                width = savedWidths[c - 1];
            }
            getColumnModel().getColumn(c).setPreferredWidth(width);
            getColumnModel().getColumn(c).setWidth(width);
        }

        // ── Restaurer les hauteurs de lignes ──────────────────────────────────
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
        hModel.mergeCells(r1, c1, r2, c2);
        refreshUI();
    }

    /**
     * Fusionne les cellules de la sélection courante. Calcule automatiquement
     * le rectangle englobant de la sélection. Ne fait rien si moins de deux
     * cellules sont sélectionnées.
     */
    public void mergeSelectedCells() {
        if (selectedRows.size() < 2 && focusedColumn < 0) {
            return;
        }

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

        if (minRow < 0 || minCol < 0) {
            return;
        }
        mergeCells(minRow, minCol, maxRow, maxCol);
    }

    /**
     * Défusionne la cellule à la position donnée. Si la cellule est absorbée,
     * remonte à la cellule principale et la libère.
     */
    public void unmergeCell(int row, int col) {
        hModel.unmergeCell(row, col);
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
     * Coupe le tableau en deux à partir de la ligne donnée. Les lignes [0,
     * atRow-1] restent dans ce tableau. Les lignes [atRow, fin] sont retournées
     * dans un nouveau HSuperTable indépendant.
     *
     * @param atRow index de la première ligne du second tableau
     * @return un nouveau HSuperTable contenant les lignes détachées
     */
    public HSuperTable splitTable(int atRow) {
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

        HSuperTable newTable = new HSuperTable(newData, colNames);
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
     * Ajustement automatique selon le mode choisi.
     *
     * AUTOFIT_CONTENT : chaque colonne s'adapte au contenu le plus large
     * (en-tête inclus) + une marge de 20px. AUTOFIT_WINDOW : toutes les
     * colonnes se partagent équitablement la largeur du composant parent
     * visible.
     *
     * @param mode HSuperTable.AUTOFIT_CONTENT ou HSuperTable.AUTOFIT_WINDOW
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
        for (int row : selectedRows) {
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
     * @param direction HSuperTable.TEXT_HORIZONTAL, TEXT_VERTICAL_UP ou
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
        TableRowSorter<HSuperDefaultTableModel> sorter = new TableRowSorter<>(hModel);
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
        TableRowSorter<HSuperDefaultTableModel> sorter = new TableRowSorter<>(hModel);
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
        // Stocker la formule brute dans les métadonnées de la cellule
        hModel.getCellModel(row, col).setFormula(formula);
        // Évaluer et afficher le résultat immédiatement
        Object result = evaluateFormula(row, col);
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
        return HTableFormula.evaluate(formula, hModel);
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

    /**
     * Efface tous les états visuels (hover, highlight, focus, sélection).
     */
    public void clearAllVisualStates() {
        highlightedRow = -1;
        hoveredRow = -1;
        focusedRow = -1;
        focusedColumn = -1;
        selectedRows.clear();
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
        if (selectedRows == null) {
            selectedRows = new ArrayList<>();
        }
        selectedRows.clear();
        super.clearSelection();
        refreshUI();
    }

    public Set<Integer> getRowsSelected() {
        return new HashSet<>(selectedRows);
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
    public HSuperDefaultTableModel getHModel() {
        return hModel;
    }

    public hsupertable.HSuperTableController getController() {
        return controller;
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
        if (action != null) {
            contextActions.add(action);
        }
    }

    /**
     * Supprime une action de la liste par référence.
     *
     * @param action l'action à supprimer
     */
    public void removeContextAction(ContextAction action) {
        contextActions.remove(action);
    }

    /**
     * Supprime toutes les actions personnalisées ET système. À utiliser si le
     * développeur veut repartir d'un menu vide.
     */
    public void clearContextActions() {
        contextActions.clear();
    }

    /**
     * Permet juste de voir les actions enregistrées
     *
     * @return liste non modifiable des actions
     */
    public List<ContextAction> getContextActions() {
        return Collections.unmodifiableList(contextActions);
    }

    // MENU HEADER — GESTION DES ACTIONS
    /**
     * Ajoute une action personnalisée à la fin de la liste des actions header.
     *
     * @param action l'action à ajouter
     */
    public void addHeaderAction(HeaderAction action) {
        if (action != null) {
            headerActions.add(action);
        }
    }

    /**
     * Supprime une action header de la liste par référence.
     *
     * @param action l'action à supprimer
     */
    public void removeHeaderAction(HeaderAction action) {
        headerActions.remove(action);
    }

    /**
     * Supprime toutes les actions header personnalisées ET système. Prévue si
     * le développeur veut repartir d'un menu vide.
     */
    public void clearHeaderActions() {
        headerActions.clear();
    }

    /**
     * Retourne une vue non modifiable de la liste des actions header. Utile
     * pour inspecter les actions enregistrées.
     */
    public List<HeaderAction> getHeaderActions() {
        return Collections.unmodifiableList(headerActions);
    }

    // =========================================================================
// MENU CONTEXTUEL — ACTIONS PAR DÉFAUT
// =========================================================================
    /**
     * Initialise toutes les actions système du menu contextuel. Appelée une
     * seule fois dans le constructeur principal. Le développeur peut ajouter
     * ses propres actions via addContextAction().
     */
    private void initializeDefaultContextActions() {

        // ── FUSIONNER ─────────────────────────────────────────────────────────
        contextActions.add(new ContextAction("Fusionner les cellules") {
            @Override
            public boolean isVisible(TableContext ctx) {
                // Visible uniquement si plusieurs cellules sont sélectionnées
                // et qu'on n'est pas sur une sous-cellule
                return ctx.hasMultipleSelection && !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.hasMultipleSelection;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.mergeSelection();
            }
        });

        // ── DÉFUSIONNER ───────────────────────────────────────────────────────
        contextActions.add(new ContextAction("Défusionner la cellule") {
            @Override
            public boolean isVisible(TableContext ctx) {
                // Visible uniquement si la cellule est fusionnée ou absorbée
                return (ctx.isMerged || ctx.isAbsorbed) && !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.isMerged || ctx.isAbsorbed;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.unmergeCell(ctx.row, ctx.column);
            }
        });

        // ── SÉPARATEUR 1 ──────────────────────────────────────────────────────
        contextActions.add(new ContextAction("---") {
            @Override
            public boolean isVisible(TableContext ctx) {
                // Visible si au moins une action de fusion est visible
                return ctx.hasMultipleSelection || ctx.isMerged || ctx.isAbsorbed;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return false; // un séparateur n'est jamais cliquable
            }

            @Override
            public void perform(TableContext ctx) {
            }
        });

        // ── SUBDIVISER VERTICALEMENT ──────────────────────────────────────────
        contextActions.add(new ContextAction("Subdiviser verticalement") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isAbsorbed;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return !ctx.isAbsorbed;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.splitCellLocally(
                        ctx.row, ctx.column,
                        HSuperDefaultTableModel.InternalGrid.SPLIT_VERTICAL,
                        0.5f
                );
            }
        });

        // ── SUBDIVISER HORIZONTALEMENT ────────────────────────────────────────
        contextActions.add(new ContextAction("Subdiviser horizontalement") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isAbsorbed;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return !ctx.isAbsorbed;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.splitCellLocally(
                        ctx.row, ctx.column,
                        HSuperDefaultTableModel.InternalGrid.SPLIT_HORIZONTAL,
                        0.5f
                );
            }
        });

        // ── SUPPRIMER SUBDIVISION ─────────────────────────────────────────────
        contextActions.add(new ContextAction("Supprimer la subdivision") {
            @Override
            public boolean isVisible(TableContext ctx) {
                // Visible si la cellule a une subdivision OU si on est sur une sous-cellule
                return ctx.hasInternalGrid || ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.hasInternalGrid || ctx.isInternalCell;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.removeInternalGridFromFocused();
            }
        });

        // ── SÉPARATEUR 2 ──────────────────────────────────────────────────────
        contextActions.add(new ContextAction("---") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return false;
            }

            @Override
            public void perform(TableContext ctx) {
            }
        });

        // ── INSÉRER LIGNE AU-DESSUS ───────────────────────────────────────────
        contextActions.add(new ContextAction("Insérer une ligne au-dessus") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.row >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.insertRowAbove(ctx.row);
            }
        });

        // ── INSÉRER LIGNE EN-DESSOUS ──────────────────────────────────────────
        contextActions.add(new ContextAction("Insérer une ligne en-dessous") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.row >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.insertRowBelow(ctx.row);
            }
        });

        // ── SUPPRIMER LIGNE ───────────────────────────────────────────────────
        contextActions.add(new ContextAction("Supprimer la ligne") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.row >= 0 && ctx.table.getRowCount() > 1;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.deleteRow(ctx.row);
            }
        });

        // ── SÉPARATEUR 3 ──────────────────────────────────────────────────────
        contextActions.add(new ContextAction("---") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return false;
            }

            @Override
            public void perform(TableContext ctx) {
            }
        });

        // ── INSÉRER COLONNE À GAUCHE ──────────────────────────────────────────
        contextActions.add(new ContextAction("Insérer une colonne à gauche") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.insertColumnLeft(ctx.column);
            }
        });

        // ── INSÉRER COLONNE À DROITE ──────────────────────────────────────────
        contextActions.add(new ContextAction("Insérer une colonne à droite") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.insertColumnRight(ctx.column);
            }
        });

        // ── SUPPRIMER COLONNE ─────────────────────────────────────────────────
        contextActions.add(new ContextAction("Supprimer la colonne") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.column >= 0 && ctx.table.getColumnCount() > 1;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.deleteColumn(ctx.column);
            }
        });

        // ── SÉPARATEUR 5 ──────────────────────────────────────────────────────
        contextActions.add(new ContextAction("---") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return false;
            }

            @Override
            public void perform(TableContext ctx) {
            }
        });

// ── ALIGNER EN-TÊTE À GAUCHE ──────────────────────────────────────────
        contextActions.add(new ContextAction("En-tête : aligner à gauche") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.setColumnHeaderAlignment(ctx.column, SwingConstants.LEFT);
            }
        });

// ── ALIGNER EN-TÊTE AU CENTRE ─────────────────────────────────────────
        contextActions.add(new ContextAction("En-tête : centrer") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.setColumnHeaderAlignment(ctx.column, SwingConstants.CENTER);
            }
        });

// ── ALIGNER EN-TÊTE À DROITE ──────────────────────────────────────────
        contextActions.add(new ContextAction("En-tête : aligner à droite") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.setColumnHeaderAlignment(ctx.column, SwingConstants.RIGHT);
            }
        });

        // ── SÉPARATEUR 4 ──────────────────────────────────────────────────────
        contextActions.add(new ContextAction("---") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return false;
            }

            @Override
            public void perform(TableContext ctx) {
            }
        });

        // ── RÉINITIALISER LE FORMATAGE ────────────────────────────────────────
        contextActions.add(new ContextAction("Réinitialiser le formatage") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.row >= 0 && ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.resetCellFormatting(ctx.row, ctx.column);
            }
        });
    }

    // =========================================================================
// MENU HEADER — ACTIONS PAR DÉFAUT
// =========================================================================
    /**
     * Initialise toutes les actions système du menu contextuel des en-têtes.
     * Appelée une seule fois dans le constructeur principal. Le développeur
     * peut ajouter ses propres actions via addHeaderAction().
     */
    private void initializeDefaultHeaderActions() {

        // ── RENOMMER ──────────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Renommer la colonne") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
                ctx.table.startHeaderEdit(ctx.columnIndex);
            }
        });

        // ── SÉPARATEUR 1 ──────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("---") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return false;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }
        });

        // ── COULEUR DE FOND ───────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Couleur de fond") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
                Color chosen = JColorChooser.showDialog(
                        ctx.table,
                        "Couleur de fond de l'en-tête",
                        ctx.headerStyle.hasBackground()
                        ? ctx.headerStyle.getBackground()
                        : ctx.table.getTableStyle().getHeaderBackground()
                );
                if (chosen != null) {
                    ctx.table.setHeaderBackground(ctx.columnIndex, chosen);
                }
            }
        });

        // ── COULEUR DU TEXTE ──────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Couleur du texte") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
                Color chosen = JColorChooser.showDialog(
                        ctx.table,
                        "Couleur du texte de l'en-tête",
                        ctx.headerStyle.hasForeground()
                        ? ctx.headerStyle.getForeground()
                        : ctx.table.getTableStyle().getHeaderForeground()
                );
                if (chosen != null) {
                    ctx.table.setHeaderForeground(ctx.columnIndex, chosen);
                }
            }
        });

        // ── SÉPARATEUR 2 ──────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("---") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return false;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }
        });

        // ── ALIGNEMENT ────────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Alignement") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }

            @Override
            public JComponent buildMenuItem(HeaderContext ctx) {
                HMenu menu = new HMenu(getName());
                menu.setEnabled(isEnabled(ctx));

                HMenuItem left = new HMenuItem("Gauche");
                left.addActionListener(e
                        -> ctx.table.setHeaderAlignment(ctx.columnIndex,
                                SwingConstants.LEFT));
                menu.add(left);

                HMenuItem center = new HMenuItem("Centre");
                center.addActionListener(e
                        -> ctx.table.setHeaderAlignment(ctx.columnIndex,
                                SwingConstants.CENTER));
                menu.add(center);

                HMenuItem right = new HMenuItem("Droite");
                right.addActionListener(e
                        -> ctx.table.setHeaderAlignment(ctx.columnIndex,
                                SwingConstants.RIGHT));
                menu.add(right);

                return menu;
            }
        });

        // ── SÉPARATEUR 3 ──────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("---") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return false;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }
        });

        // ── STYLE DE POLICE ───────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Style de police") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }

            @Override
            public JComponent buildMenuItem(HeaderContext ctx) {
                HMenu menu = new HMenu(getName());
                menu.setEnabled(isEnabled(ctx));

                HMenuItem bold = new HMenuItem(
                        ctx.headerStyle.isBold() ? "Supprimer le gras" : "Gras");
                bold.addActionListener(e -> {
                    ctx.table.setHeaderBold(ctx.columnIndex,
                            !ctx.headerStyle.isBold());
                });
                menu.add(bold);

                HMenuItem italic = new HMenuItem(
                        ctx.headerStyle.isItalic() ? "Supprimer l'italique" : "Italique");
                italic.addActionListener(e -> {
                    ctx.table.setHeaderItalic(ctx.columnIndex,
                            !ctx.headerStyle.isItalic());
                });
                menu.add(italic);

                return menu;
            }
        });

        // ── TAILLE DU TEXTE ───────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Taille du texte") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }

            @Override
            public JComponent buildMenuItem(HeaderContext ctx) {
                HMenu menu = new HMenu(getName());
                menu.setEnabled(isEnabled(ctx));

                int[] sizes = {10, 11, 12, 13, 14, 16, 18, 20, 24};
                for (int size : sizes) {
                    HMenuItem item = new HMenuItem(size + " pt");
                    final int s = size;
                    item.addActionListener(e
                            -> ctx.table.setHeaderFontSize(ctx.columnIndex, s));
                    menu.add(item);
                }

                return menu;
            }
        });

        // ── SÉPARATEUR 4 ──────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("---") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return false;
            }

            @Override
            public void perform(HeaderContext ctx) {
            }
        });

        // ── RÉINITIALISER ─────────────────────────────────────────────────────
        headerActions.add(new HeaderAction("Réinitialiser le style") {
            @Override
            public boolean isVisible(HeaderContext ctx) {
                return true;
            }

            @Override
            public boolean isEnabled(HeaderContext ctx) {
                return ctx.columnIndex >= 0;
            }

            @Override
            public void perform(HeaderContext ctx) {
                ctx.table.resetHeaderStyle(ctx.columnIndex);
            }
        });
    }

// =========================================================================
// MENU CONTEXTUEL — CONSTRUCTION ET AFFICHAGE
// =========================================================================
    /**
     * Construit et affiche le menu contextuel pour le contexte donné.
     *
     * Pipeline : 1. Créer le JPopupMenu 2. Parcourir toutes les actions
     * enregistrées 3. Pour chaque action : vérifier isVisible() 4. Si visible :
     * créer le HMenuItem, appliquer isEnabled() 5. Connecter perform() au clic
     * 6. Afficher le popup à la position souris
     *
     * Les séparateurs ("---") sont traités à part — on évite d'afficher un
     * séparateur en début ou en fin de menu, et deux séparateurs consécutifs.
     *
     * @param ctx contexte du clic droit
     * @param x position X souris dans le tableau
     * @param y position Y souris dans le tableau
     */
    public void showContextMenu(TableContext ctx, int x, int y) {
        HPopupMenu popup = new HPopupMenu();

        boolean lastWasSeparator = true; // évite un séparateur en début de menu
        int visibleItemCount = 0;

        for (ContextAction action : contextActions) {

            // Vérification de la visibilité
            if (!action.isVisible(ctx)) {
                continue;
            }

            // Traitement des séparateurs
            if ("---".equals(action.getName())) {
                // On n'ajoute pas deux séparateurs consécutifs
                // et on n'ajoute pas un séparateur si rien n'a encore été ajouté
                if (!lastWasSeparator && visibleItemCount > 0) {
                    popup.addSeparator();
                    lastWasSeparator = true;
                }
                continue;
            }

            // Création du HMenuItem
            HMenuItem item = new HMenuItem(action.getName());
            item.setEnabled(action.isEnabled(ctx));

            // Connexion de l'action au clic
            item.addActionListener(e -> action.perform(ctx));

            popup.add(item);
            lastWasSeparator = false;
            visibleItemCount++;
        }

        // Supprimer le dernier séparateur s'il est en fin de menu
        int count = popup.getComponentCount();
        if (count > 0 && popup.getComponent(count - 1) instanceof JSeparator) {
            popup.remove(count - 1);
        }

        // N'afficher le popup que s'il contient au moins une action
        if (visibleItemCount > 0) {
            popup.show(this, x, y);
        }
    }

    // =========================================================================
// MENU HEADER — CONSTRUCTION ET AFFICHAGE
// =========================================================================
    /**
     * Construit et affiche le menu contextuel des en-têtes pour le contexte
     * donné.
     *
     * Notes: 1. Créer le JPopupMenu 2. Parcourir toutes les actions header
     * enregistrées 3. Pour chaque action : vérifier isVisible() 4. Si visible :
     * créer le JMenuItem via buildMenuItem(), appliquer isEnabled() 5.
     * Connecter perform() au clic 6. Afficher le popup à la position souris
     *
     * @param ctx contexte du clic droit sur l'en-tête
     * @param x position X souris dans l'en-tête
     * @param y position Y souris dans l'en-tête
     */
    public void showHeaderMenu(HeaderContext ctx, int x, int y) {
        JPopupMenu popup = new JPopupMenu();

        boolean lastWasSeparator = true;
        int visibleItemCount = 0;

        for (HeaderAction action : headerActions) {

            // Vérification de la visibilité
            if (!action.isVisible(ctx)) {
                continue;
            }

            // Traitement des séparateurs
            if ("---".equals(action.getName())) {
                if (!lastWasSeparator && visibleItemCount > 0) {
                    popup.addSeparator();
                    lastWasSeparator = true;
                }
                continue;
            }

            // Déléguer la construction du composant à l'action
            JComponent menuItem = action.buildMenuItem(ctx);
            popup.add(menuItem);
            lastWasSeparator = false;
            visibleItemCount++;
        }

        // Supprimer le dernier séparateur s'il est en fin de menu
        int count = popup.getComponentCount();
        if (count > 0 && popup.getComponent(count - 1) instanceof JSeparator) {
            popup.remove(count - 1);
        }

        // N'afficher le popup que s'il contient au moins une action
        if (visibleItemCount > 0) {
            // Le popup s'affiche sur le JTableHeader, pas sur le tableau
            JTableHeader header = getTableHeader();
            if (header != null) {
                popup.show(header, x, y);
            }
        }
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
     * Déclenche un repaint + revalidate du tableau. Toutes les méthodes
     * publiques qui modifient l'état visuel appellent cette méthode à la fin —
     * jamais repaint() directement.
     */
    public void refreshUI() {
        revalidate();
        repaint();
    }

    @Override
    public void repaint() {
        super.repaint(0, 0, getWidth(), getHeight());
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

    // MENU CONTEXTUEL — CONTEXTE
    /**
     * Centralise toutes les informations nécessaires pour décider quelles
     * actions afficher et comment les exécuter.
     */
    public static class TableContext {

        /**
         * Ligne de la cellule ciblée (-1 si hors tableau).
         */
        public final int row;

        /**
         * Colonne de la cellule ciblée (-1 si hors tableau).
         */
        public final int column;

        /**
         * Cellule du modèle ciblée (jamais null).
         */
        public final Cell cell;

        /**
         * Sous-cellule interne ciblée (null si cellule normale).
         */
        public final Cell internalCell;

        /**
         * Résultat complet du hit-testing interne (null si on a une cellule
         * normale).
         */
        public final InternalCellHit internalHit;

        /**
         * Position souris au moment du clic droit.
         */
        public final Point mousePosition;

        /**
         * Vrai si la cellule ciblée est fusionnée (principale).
         */
        public final boolean isMerged;

        /**
         * Vrai si la cellule ciblée est absorbée par une fusion.
         */
        public final boolean isAbsorbed;

        /**
         * Vrai si la cellule ciblée possède une subdivision interne.
         */
        public final boolean hasInternalGrid;

        /**
         * Vrai si le clic a atteint une sous-cellule interne.
         */
        public final boolean isInternalCell;

        /**
         * Vrai si plusieurs cellules sont sélectionnées.
         */
        public final boolean hasMultipleSelection;

        /**
         * Référence au tableau — permet aux actions d'appeler l'API.
         */
        public final HSuperTable table;

        /**
         * Construit un contexte complet.
         *
         * @param table le tableau source
         * @param row ligne ciblée
         * @param column colonne ciblée
         * @param cell cellule du modèle
         * @param internalHit résultat du hit-testing interne
         * @param mousePosition position souris
         */
        public TableContext(
                HSuperTable table,
                int row,
                int column,
                HSuperDefaultTableModel.Cell cell,
                HBasicTableUI.InternalCellHit internalHit,
                Point mousePosition
        ) {
            this.table = table;
            this.row = row;
            this.column = column;
            this.cell = cell;
            this.internalHit = internalHit;
            this.mousePosition = mousePosition;

            // Résolution des états depuis la cellule
            this.isMerged = (cell != null) && cell.isMerged();
            this.isAbsorbed = (cell != null) && cell.isAbsorbed();
            this.hasInternalGrid = (cell != null) && cell.hasInternalGrid();

            // Sous-cellule : hit valide avec un parent
            this.isInternalCell = (internalHit != null && internalHit.parent != null);
            this.internalCell = isInternalCell ? internalHit.cell : null;

            // Sélection multiple : plus d'une ligne sélectionnée
            this.hasMultipleSelection = table.getRowsSelected().size() > 1;
        }

        @Override
        public String toString() {
            return "TableContext[row=" + row + ", col=" + column
                    + ", merged=" + isMerged
                    + ", internal=" + isInternalCell
                    + ", multiSel=" + hasMultipleSelection + "]";
        }
    }

    // =========================================================================
    // MENU HEADER — STYLE
    // =========================================================================
    /**
     * HeaderStyle — Métadonnées visuelles d'un en-tête de colonne.
     *
     * Stocke toutes les personnalisations applicables à un en-tête : couleur de
     * fond, couleur du texte, alignement, style de police, taille du texte.
     *
     * null sur une propriété = utiliser la valeur par défaut du
     * HSuperTableStyle.
     */
    public static class HeaderStyle {

        /**
         * Couleur de fond. null = valeur du style global.
         */
        private Color background;

        /**
         * Couleur du texte. null = valeur du style global.
         */
        private Color foreground;

        /**
         * Alignement horizontal. SwingConstants.LEFT / CENTER / RIGHT.
         */
        private int horizontalAlignment = SwingConstants.LEFT;

        /**
         * Texte en gras.
         */
        private boolean bold = true;

        /**
         * Texte en italique.
         */
        private boolean italic = false;

        /**
         * Taille du texte. -1 = taille du style global.
         */
        private float fontSize = -1f;

        // ── Couleur de fond ───────────────────────────────────────────────
        public Color getBackground() {
            return background;
        }

        public void setBackground(Color c) {
            this.background = c;
        }

        public boolean hasBackground() {
            return background != null;
        }

        // ── Couleur du texte ──────────────────────────────────────────────
        public Color getForeground() {
            return foreground;
        }

        public void setForeground(Color c) {
            this.foreground = c;
        }

        public boolean hasForeground() {
            return foreground != null;
        }

        // ── Alignement ────────────────────────────────────────────────────
        public int getHorizontalAlignment() {
            return horizontalAlignment;
        }

        public void setHorizontalAlignment(int a) {
            this.horizontalAlignment = a;
        }

        // ── Gras ──────────────────────────────────────────────────────────
        public boolean isBold() {
            return bold;
        }

        public void setBold(boolean b) {
            this.bold = b;
        }

        // ── Italique ──────────────────────────────────────────────────────
        public boolean isItalic() {
            return italic;
        }

        public void setItalic(boolean i) {
            this.italic = i;
        }

        // ── Taille du texte ───────────────────────────────────────────────
        public float getFontSize() {
            return fontSize;
        }

        public void setFontSize(float s) {
            this.fontSize = s;
        }

        public boolean hasCustomFontSize() {
            return fontSize > 0f;
        }

        /**
         * Remet toutes les propriétés à leurs valeurs par défaut.
         */
        public void reset() {
            background = null;
            foreground = null;
            horizontalAlignment = SwingConstants.LEFT;
            bold = true;
            italic = false;
            fontSize = -1f;
        }

        /**
         * Construit la police finale en combinant gras, italique et taille. Si
         * fontSize <= 0, on utilise la taille de la police de référence.
         *
         * @param baseFont police de référence (depuis HSuperTableStyle)
         * @return police calculée
         */
        public Font buildFont(Font baseFont) {
            if (baseFont == null) {
                baseFont = new Font("Segoe UI", Font.BOLD, 13);
            }
            int style = Font.PLAIN;
            if (bold) {
                style |= Font.BOLD;
            }
            if (italic) {
                style |= Font.ITALIC;
            }
            float size = hasCustomFontSize() ? fontSize : baseFont.getSize2D();
            return baseFont.deriveFont(style, size);
        }

        @Override
        public String toString() {
            return "HeaderStyle{bg=" + background
                    + ", fg=" + foreground
                    + ", align=" + horizontalAlignment
                    + ", bold=" + bold
                    + ", italic=" + italic
                    + ", fontSize=" + fontSize + "}";
        }
    }

    // =========================================================================
// MENU HEADER — CONTEXTE
// =========================================================================
    /**
     * HeaderContext Construit par le contrôleur. Centralise toutes les
     * informations nécessaires pour décider quelles actions afficher et comment
     * les exécuter.
     */
    public static class HeaderContext {

        /**
         * Index de la colonne ciblée.
         */
        public final int columnIndex;

        /**
         * Nom actuel de la colonne.
         */
        public final String columnName;

        /**
         * Style actuel de l'en-tête — jamais null.
         */
        public final HeaderStyle headerStyle;

        /**
         * Position souris au moment du clic droit.
         */
        public final Point mousePosition;

        /**
         * Référence au tableau — permet aux actions d'appeler l'API.
         */
        public final HSuperTable table;

        /**
         * Construit un contexte complet.
         *
         * @param table le tableau source
         * @param columnIndex index de la colonne ciblée
         * @param mousePosition position souris
         */
        public HeaderContext(
                HSuperTable table,
                int columnIndex,
                Point mousePosition
        ) {
            this.table = table;
            this.columnIndex = columnIndex;
            this.mousePosition = mousePosition;
            this.columnName = (columnIndex >= 0 && columnIndex < table.getColumnCount())
                    ? table.getColumnName(columnIndex)
                    : "";

            // Récupérer ou créer le style de cet en-tête
            this.headerStyle = table.getHeaderStyle(columnIndex);
        }

        @Override
        public String toString() {
            return "HeaderContext[col=" + columnIndex
                    + ", name=" + columnName + "]";
        }
    }

    // =========================================================================
// MENU HEADER — ACTION
// =========================================================================
    /**
     * HeaderAction — Commande indépendante du menu contextuel des en-têtes.
     *
     * Identique à ContextAction mais dédiée aux en-têtes de colonnes. Chaque
     * entrée du menu header est une action autonome qui : - décide elle-même si
     * elle est visible selon le contexte, - décide elle-même si elle est
     * activée selon le contexte, - exécute sa propre logique métier.
     *
     * Le développeur crée ses actions personnalisées en héritant de cette
     * classe et en implémentant les trois méthodes abstraites.
     *
     * Exemple d'utilisation :
     * <pre>
     *   table.addHeaderAction(new HSuperTable.HeaderAction("Trier") {
     *       {@literal @}Override
     *       public boolean isVisible(HeaderContext ctx) { return true; }
     *
     *       {@literal @}Override
     *       public boolean isEnabled(HeaderContext ctx) { return true; }
     *
     *       {@literal @}Override
     *       public void perform(HeaderContext ctx) {
     *           ctx.table.sortByColumn(ctx.columnIndex, SortOrder.ASCENDING);
     *       }
     *   });
     * </pre>
     */
    public abstract static class HeaderAction {

        /**
         * Nom affiché dans le menu contextuel.
         */
        private final String name;

        /**
         * Crée une action avec le nom donné.
         *
         * @param name libellé affiché dans le menu
         */
        public HeaderAction(String name) {
            this.name = name;
        }

        /**
         * Retourne le libellé de l'action.
         *
         * @return nom de l'action
         */
        public String getName() {
            return name;
        }

        /**
         * Détermine si cette action doit apparaître dans le menu pour ce
         * contexte. Si false, l'entrée n'est pas créée du tout.
         *
         * @param ctx contexte du clic droit sur l'en-tête
         * @return true si l'action doit être visible
         */
        public abstract boolean isVisible(HeaderContext ctx);

        /**
         * Détermine si cette action est cliquable dans le menu. Si false,
         * l'entrée est grisée mais reste visible.
         *
         * @param ctx contexte du clic droit sur l'en-tête
         * @return true si l'action est activée
         */
        public abstract boolean isEnabled(HeaderContext ctx);

        /**
         * Exécute la logique de l'action. Appelée uniquement si l'action est
         * visible et activée.
         *
         * @param ctx contexte du clic droit sur l'en-tête
         */
        public abstract void perform(HeaderContext ctx);

        /**
         * Construit le HMenuItem correspondant à cette action. Les actions
         * simples retournent un HMenuItem standard. Les actions avec sous-menu
         * peuvent surcharger cette méthode pour retourner un HMenu contenant
         * des HMenuItem enfants.
         *
         * @param ctx contexte du clic droit sur l'en-tête
         * @return le composant menu à ajouter au popup
         */
        public JComponent buildMenuItem(HeaderContext ctx) {
            HMenuItem item = new HMenuItem(getName());
            item.setEnabled(isEnabled(ctx));
            item.addActionListener(e -> perform(ctx));
            return item;
        }
    }

    // MENU CONTEXTUEL — ACTION
    /**
     * Permet de créer ses actions personnalisées en héritant de cette classe et
     * en implémentant les trois méthodes abstraites.
     *
     * en Exemple on peut faire :
     * <pre>
     *   table.addContextAction(new HSuperTable.ContextAction("Copier") {
     *       public boolean isVisible(TableContext ctx) { return true; }
     *
     *       public boolean isEnabled(TableContext ctx) { return ctx.row >= 0; }
     *
     *       public void perform(TableContext ctx) {
     *           // logique de copie
     *       }
     *   });
     * </pre>
     */
    public abstract static class ContextAction {

        /**
         * Nom affiché dans le menu contextuel.
         */
        private final String name;

        /**
         * Crée une action avec le nom donné.
         *
         * @param name libellé affiché dans le menu
         */
        public ContextAction(String name) {
            this.name = name;
        }

        /**
         * Retourne le libellé de l'action.
         *
         * @return nom de l'action
         */
        public String getName() {
            return name;
        }

        /**
         * Détermine si cette action doit apparaître dans le menu pour ce
         * contexte. Si false, l'entrée n'est pas créée du tout.
         *
         * @param ctx contexte du clic droit
         * @return true si l'action doit être visible
         */
        public abstract boolean isVisible(TableContext ctx);

        /**
         * Détermine si cette action est cliquable dans le menu. Si false,
         * l'entrée est grisée mais reste visible.
         *
         * @param ctx contexte du clic droit
         * @return true si l'action est activée
         */
        public abstract boolean isEnabled(TableContext ctx);

        /**
         * Exécute la logique de l'action. Appelée uniquement si l'action est
         * visible et activée.
         *
         * @param ctx contexte du clic droit
         */
        public abstract void perform(TableContext ctx);
    }

    /**
     * Vrai si une sous-cellule interne est actuellement focusée. Utilisé par
     * toutes les méthodes de formatage pour router l'action vers la bonne
     * cible.
     */
    public boolean hasInternalFocus() {
        return focusedInternalCell != null && focusedInternalCell.parent != null;
    }

}
