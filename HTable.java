package hsupertable;

import hsupertable.formula.HTableFormula;
import hsupertable.geometry.TableGeometry;
import hsupertable.geometry.TableGeometry.InternalCellHit;
import hsupertable.geometry.TableStructureIntegrity;
import hsupertable.menu.*;
import hsupertable.model.structure.CellNode;
import hsupertable.model.CellStyle;
import hsupertable.model.selection.CellSelectionModel;
import hsupertable.model.structure.CellStructureEvent;
import hsupertable.model.DefaultCellStructureModel;
import hsupertable.model.HDefaultTableModel;
import hsupertable.model.MergeRegion;
import hsupertable.model.SubCellPath;
import hsupertable.style.HTableStyle;
import hsupertable.style.HTableStyle.HeaderStyle;
import hsupertable.view.HBasicTableUI;
import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import hsupertable.model.structure.CellStructureListener;
import hsupertable.model.structure.CellStructureModel;
import java.util.function.BiConsumer;

/**
 * HTable — Tableau Swing avancé inspiré des outils tableau de Microsoft Word.
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
    // Source unique : CellStyle (c'est lui qui interprète ces bits).
    public static final int SIDE_TOP = CellStyle.SIDE_TOP;
    public static final int SIDE_BOTTOM = CellStyle.SIDE_BOTTOM;
    public static final int SIDE_LEFT = CellStyle.SIDE_LEFT;
    public static final int SIDE_RIGHT = CellStyle.SIDE_RIGHT;
    public static final int SIDE_ALL = SIDE_TOP | SIDE_BOTTOM | SIDE_LEFT | SIDE_RIGHT;
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
     * Structure de la table : fusions, subdivisions et styles par cellule, en
     * coordonnées MODÈLE. Les VALEURS des cellules restent dans le TableModel
     * (n'importe quelle implémentation) : HTable est le seul à connaître les
     * deux et à les garder cohérents.
     */
    private CellStructureModel structureModel;

    private CellSelectionModel cellSelectionModel;

    /**
     * Si vrai (défaut), un changement de structure signalé par le TableModel
     * (fireTableStructureChanged, nouveau modèle...) efface la structure : des
     * fusions et styles posés sur d'anciennes colonnes n'ont plus de sens.
     */
    private boolean autoCreateStructureFromModel = true;

    /**
     * Vrai pendant que HTable modifie elle-même le TableModel : les événements
     * produits alors ne doivent pas être interprétés comme des changements
     * externes.
     */
    private boolean adjustingStructure = false;

    private final CellStructureListener structureListener = this::structureChanged;

    /**
     * Types de colonne déclarés explicitement (indice MODÈLE), prioritaires sur
     * l'inférence. Voir setColumnClass().
     */
    private final Map<Integer, Class<?>> declaredColumnClasses = new HashMap<>();

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

    private boolean sortingEnabled = true;

    // CONSTRUCTEURS
    public HTable() {
        this(new HDefaultTableModel());
    }

    /**
     * Table au-dessus d'un TableModel quelconque, utilisé tel quel : HTable ne
     * lui demande rien de plus que l'interface standard (les fusions et
     * subdivisions vivent dans le modèle de structure, pas dans les données).
     */
    public HTable(TableModel model) {
        this(model, new DefaultCellStructureModel());
    }

    /**
     * Point d'injection du modèle de structure : permet de fournir une autre
     * implémentation (persistante, observée, de test...).
     */
    public HTable(TableModel model, CellStructureModel structureModel) {
        super(model);
        this.structureModel = Objects.requireNonNull(structureModel, "structureModel");
        this.structureModel.addCellStructureListener(structureListener);
        this.cellSelectionModel = new CellSelectionModel(getSelectionModel(), getColumnModel().getSelectionModel());
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
                    TableStructureIntegrity.enforceColumnAdjacency(HTable.this);
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

    public CellSelectionModel getCellSelectionModel() {
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
    // Les indices (row, col) de toute cette API sont des indices VUE, comme
    // partout dans JTable : ils sont convertis en indices MODÈLE avant d'écrire
    // dans la structure, pour que le style suive sa cellule lors d'un tri ou
    // d'un déplacement de colonne.
    /**
     * Définit la couleur de fond d'une cellule précise. Priorité maximale —
     * écrase tout le reste (style, bandes, hover, etc.).
     *
     * @param row ligne (0-indexée)
     * @param col colonne (0-indexée)
     * @param color couleur souhaitée, ou null pour retirer la couleur custom
     */
    public void setCellBackground(int row, int col, Color color) {
        withStyle(row, col, s -> s.setBackground(color));
        refreshUI();
    }

    public Color getCellBackground(int row, int col) {
        CellStyle s = getCellStyle(row, col);
        return s == null ? null : s.getBackground();
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
            withStyle(r, col, s -> s.setBackground(color));
        }
        refreshUI();
    }

    /**
     * Applique une couleur de fond à la sélection courante.
     */
    public void setSelectionCellBackground(Color color) {
        forEachSelectedCell((r, c) -> withStyle(r, c, s -> s.setBackground(color)));
        refreshUI();
    }

    // ── Couleur du texte ─────────────────────────────────────────────────────
    public void setCellForeground(int row, int col, Color color) {
        withStyle(row, col, s -> s.setForeground(color));
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
            withStyle(r, col, s -> s.setForeground(color));
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
        withStyle(row, col, s -> s.setBorderSides(sides, color, thickness, style));
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
        setCellBorderSide(row, col, SIDE_ALL, color, thickness, style);
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
                withStyle(r, c, s -> s.setBorderSides(SIDE_ALL, color, thickness, style));
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
        if (lastRow < 0 || lastCol < 0) {
            return;
        }
        for (int c = 0; c <= lastCol; c++) {
            withStyle(0, c, s -> s.setBorderSides(SIDE_TOP, color, thickness, style));
            withStyle(lastRow, c, s -> s.setBorderSides(SIDE_BOTTOM, color, thickness, style));
        }
        for (int r = 0; r <= lastRow; r++) {
            withStyle(r, 0, s -> s.setBorderSides(SIDE_LEFT, color, thickness, style));
            withStyle(r, lastCol, s -> s.setBorderSides(SIDE_RIGHT, color, thickness, style));
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
                    withStyle(r, c, s -> s.setBorderSides(SIDE_RIGHT, color, thickness, style));
                }
                if (r < rows - 1) {
                    withStyle(r, c, s -> s.setBorderSides(SIDE_BOTTOM, color, thickness, style));
                }
            }
        }
        refreshUI();
    }

    /**
     * Applique une bordure sur toute la sélection courante.
     */
    public void setSelectionBorder(int sides, Color color, float thickness, int style) {
        forEachSelectedCell((r, c) -> withStyle(r, c, s -> s.setBorderSides(sides, color, thickness, style)));
        refreshUI();
    }

    /**
     * Supprime toutes les bordures custom d'une cellule.
     */
    public void removeCellBorder(int row, int col) {
        withStyle(row, col, CellStyle::clearAllBorders);
        refreshUI();
    }

    /**
     * Supprime un côté spécifique de la bordure d'une cellule.
     */
    public void removeCellBorderSide(int row, int col, int sides) {
        // On supprime en passant thickness=0 et color=null
        withStyle(row, col, s -> s.setBorderSides(sides, null, 0f, BORDER_SOLID));
        refreshUI();
    }

    /**
     * Supprime toutes les bordures custom du tableau entier.
     */
    public void removeAllBorders() {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                withStyle(r, c, CellStyle::clearAllBorders);
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
     * Applique une modification de style à la cible courante : la sous-cellule
     * focusée s'il y en a une, sinon toutes les cellules de la zone
     * sélectionnée. Factorise ce que faisaient séparément chaque méthode
     * apply...ToSelection (même routage, seule la modification change).
     */
    private void applyToSelection(Consumer<CellStyle> change) {
        if (hasInternalFocus()) {
            CellStyle s = styleOf(focusedInternalCell);
            if (s != null) {
                change.accept(s);
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
                withStyle(r, c, change);
            }
        }
        refreshUI();
    }

    /**
     * Applique une couleur de fond à toute la zone sélectionnée.Ne fait rien si
     * aucune sélection n'est active.
     *
     * @param color
     */
    public void applyBackgroundToSelection(Color color) {
        applyToSelection(s -> s.setBackground(color));
    }

    /**
     * Applique une couleur de texte à la sélection.
     */
    public void applyForegroundToSelection(Color color) {
        applyToSelection(s -> s.setForeground(color));
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
        applyToSelection(s -> s.setBorderSides(sides, color, thickness, style));
    }

    /**
     * Applique un alignement à la sélection.
     */
    public void applyAlignmentToSelection(int hAlign, int vAlign) {
        applyToSelection(s -> s.setAlignment(hAlign, vAlign));
    }

    /**
     * Applique une direction de texte à la sélection.
     */
    public void applyTextDirectionToSelection(int direction) {
        applyToSelection(s -> s.setTextDirection(direction));
    }

    /**
     * Applique des marges internes à la sélection.
     */
    public void applyMarginsToSelection(Insets margins) {
        applyToSelection(s -> s.setMargins(margins));
    }

    /**
     * Remet le formatage par défaut sur toute la sélection.
     */
    public void resetFormattingOnSelection() {
        applyToSelection(CellStyle::reset);
    }

    /**
     * Vrai si la sélection courante peut être fusionnée : au moins deux
     * cellules, contiguës dans les données (tri, colonnes déplacées), et sans
     * chevauchement partiel d'une fusion existante.
     */
    public boolean canMergeSelection() {
        CellRange sel = getSelection();
        if (sel == null || sel.isSingleCell()) {
            return false;
        }
        if (!TableStructureIntegrity.isMergeableViewRange(
                this, sel.rowStart, sel.rowEnd, sel.colStart, sel.colEnd)) {
            return false;
        }
        return structureModel.canMerge(
                toModelRow(sel.rowStart), toModelColumn(sel.colStart),
                toModelRow(sel.rowEnd), toModelColumn(sel.colEnd));
    }

    /**
     * Fusionne la zone sélectionnée. Remplace mergeSelectedCells() qui
     * dépendait de l'ancienne sélection par lignes.
     */
    public void mergeSelection() {
        CellRange sel = getSelection();
        if (sel == null || !canMergeSelection()) {
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
            final Color background = (r % 2 == 0)
                    ? style.getCellBackground()
                    : style.getCellAlternateBackground();
            for (int c = sel.colStart; c <= sel.colEnd; c++) {
                withStyle(r, c, s -> {
                    s.setBackground(background);
                    s.setForeground(style.getCellForeground());
                });
            }
        }
        refreshUI();
    }

    /**
     * Déclare explicitement le type d'une colonne, prioritaire sur l'inférence
     * automatique. Nécessaire pour qu'un renderer/editor personnalisé
     * enregistré via setDefaultRenderer/setDefaultEditor reste résolu même si
     * la colonne est vide (aucune valeur non nulle à inférer).
     *
     * @param columnIndex indice de colonne MODÈLE
     * @param columnClass le type, ou null pour revenir à l'inférence
     */
    public void setColumnClass(int columnIndex, Class<?> columnClass) {
        if (columnClass == null) {
            declaredColumnClasses.remove(columnIndex);
        } else {
            declaredColumnClasses.put(columnIndex, columnClass);
        }
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
        return cellSelectionModel.getSelectedColumns();
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

    // ── Subdivision interne ──────────────────────────────────────────────────
    /**
     * Coupe en deux la sous-cellule focusée, ou la cellule (row, col) (indices
     * VUE) si aucune sous-cellule n'est focusée.
     */
    public void splitCellLocally(int row, int col, int splitType, float dividerRatio) {
        if (hasInternalFocus()) {
            InternalCellHit hit = focusedInternalCell;
            subdivideNode(hit.row, hit.col, hit.path, splitType, dividerRatio);
            refreshUI();
            return;
        }
        int modelRow = toModelRow(row);
        int modelCol = toModelColumn(col);
        requireModelCell(modelRow, modelCol);
        // Une cellule déjà subdivisée ne se re-subdivise pas depuis la cellule
        // entière : il faut viser l'une de ses sous-cellules.
        if (!structureModel.getCellNode(modelRow, modelCol).isLeaf()) {
            return;
        }
        subdivideNode(modelRow, modelCol, SubCellPath.ROOT, splitType, dividerRatio);
        refreshUI();
    }

    /**
     * Subdivise la cellule focusée ou la cellule (row, col) en une grille de
     * nbRows × nbCols sous-cellules.
     *
     * Si une sous-cellule interne est focusée, la subdivision s'applique sur
     * elle. Sinon elle s'applique sur la cellule aux coordonnées données.
     *
     * @param row ligne de la cellule cible (indice VUE)
     * @param col colonne de la cellule cible (indice VUE)
     * @param nbRows nombre de lignes dans la grille
     * @param nbCols nombre de colonnes dans la grille
     */
    public void splitCellGrid(int row, int col, int nbRows, int nbCols) {
        if (nbRows < 1 || nbCols < 1) {
            return;
        }
        if (hasInternalFocus()) {
            InternalCellHit hit = focusedInternalCell;
            subdivideNodeAsGrid(hit.row, hit.col, hit.path, nbRows, nbCols);
            refreshUI();
            return;
        }
        int modelRow = toModelRow(row);
        int modelCol = toModelColumn(col);
        requireModelCell(modelRow, modelCol);
        subdivideNodeAsGrid(modelRow, modelCol, SubCellPath.ROOT, nbRows, nbCols);
        refreshUI();
    }

    private void subdivideNode(int modelRow, int modelCol, SubCellPath path,
            int splitType, float dividerRatio) {
        CellNode target = nodeOf(modelRow, modelCol, path);
        if (target == null) {
            return;
        }
        // Le contenu actuel passe dans la première moitié. Pour la cellule
        // entière, il vit dans le TableModel ; pour une sous-cellule, dans le
        // noeud lui-même.
        Object firstValue = null;
        if (target.isLeaf()) {
            firstValue = path.isRoot() ? getModel().getValueAt(modelRow, modelCol) : target.getValue();
        }
        structureModel.subdivide(modelRow, modelCol, path, splitType, dividerRatio, firstValue);
        syncSubdividedValue(modelRow, modelCol);
    }

    private void subdivideNodeAsGrid(int modelRow, int modelCol, SubCellPath path,
            int nbRows, int nbCols) {
        CellNode target = nodeOf(modelRow, modelCol, path);
        if (target == null) {
            return;
        }
        Object value;
        if (!target.isLeaf()) {
            value = target.collectText();
        } else {
            value = path.isRoot() ? getModel().getValueAt(modelRow, modelCol) : target.getValue();
        }
        structureModel.subdivideGrid(modelRow, modelCol, path, nbRows, nbCols, value);
        syncSubdividedValue(modelRow, modelCol);
    }

    /**
     * Supprime la subdivision de la cellule (row, col) (indices VUE) : elle
     * redevient une cellule simple dont le texte est la concaténation des
     * sous-cellules.
     */
    public void removeInternalGrid(int row, int col) {
        int modelRow = toModelRow(row);
        int modelCol = toModelColumn(col);
        requireModelCell(modelRow, modelCol);
        removeSubdivision(modelRow, modelCol, SubCellPath.ROOT);
        refreshUI();
    }

    public void removeInternalGridFromFocused() {
        if (hasInternalFocus()) {
            InternalCellHit hit = focusedInternalCell;
            removeSubdivision(hit.row, hit.col, hit.path);
            setFocusedInternalCell(null);
            setSelectedInternalCell(null);
            refreshUI();
            return;
        }

        // Pas de sous-cellule focusée — gomme sur la cellule globale
        int row = getFocusedRow();
        int col = getFocusedColumn();
        if (row >= 0 && col >= 0) {
            removeInternalGrid(row, col);
        }
    }

    /**
     * Replie la subdivision du noeud désigné (coordonnées MODÈLE). Sans effet
     * si le noeud n'est pas subdivisé. Point d'entrée unique de ce repli pour
     * les gestionnaires (gomme, menu) comme pour HTableStructureIntegrity.
     */
    public void removeSubdivision(int modelRow, int modelCol, SubCellPath path) {
        CellNode node = nodeOf(modelRow, modelCol, path);
        if (node == null || node.isLeaf()) {
            return;
        }
        Object folded = structureModel.removeSubdivision(modelRow, modelCol, path);
        if (path.isRoot()) {
            // Le texte de la cellule entière appartient au TableModel
            writeTextIfDifferent(modelRow, modelCol, folded);
            refreshMergeOriginalText(modelRow, modelCol, folded);
        } else {
            syncSubdividedValue(modelRow, modelCol);
        }
    }

    /**
     * Si (row, col) est l'origine d'une fusion, la valeur « avant fusion » de
     * l'origine devient le texte replié : un défusionnement ultérieur doit
     * rendre le texte que l'utilisateur voit, pas celui d'avant la subdivision.
     */
    private void refreshMergeOriginalText(int modelRow, int modelCol, Object text) {
        MergeRegion region = structureModel.getMergeAt(modelRow, modelCol);
        if (region == null || region.originRow != modelRow || region.originCol != modelCol) {
            return;
        }
        Object[][] values = region.getOriginalValues();
        values[0][0] = text;
        structureModel.unmerge(region.originRow, region.originCol);
        structureModel.merge(region.originRow, region.originCol,
                region.lastRow(), region.lastCol(), values);
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

    /**
     * Ouvre l'éditeur flottant sur une feuille (sous-cellule, ou cellule non
     * subdivisée).
     */
    public void startInternalEdit(InternalCellHit hit) {
        if (hit == null) {
            return;
        }
        CellNode node = nodeOf(hit);
        Rectangle r = TableGeometry.boundsOf(this, hit);
        if (node == null || r == null || !node.isLeaf()) {
            return;
        }

        editingInternalCell = hit;

        internalEditor.setBounds(r.x + 1, r.y + 1, r.width - 2, r.height - 2);

        Object value = getInternalCellValue(hit);

        internalEditor.setText(value != null ? value.toString() : "");

        internalEditor.setVisible(true);

        internalEditor.requestFocus();

        internalEditor.selectAll();
    }

    /**
     * Valide l'éditeur flottant : le texte saisi passe à la sous-cellule (ou à
     * la cellule du TableModel si elle n'est pas subdivisée).
     */
    public void stopInternalEdit() {
        // On libère l'état AVANT de masquer l'éditeur : masquer déclenche un
        // focusLost, qui rappelle cette méthode.
        InternalCellHit hit = editingInternalCell;
        editingInternalCell = null;

        if (hit != null) {
            setInternalCellValue(hit, internalEditor.getText());
        }

        internalEditor.setVisible(false);

        repaint();

    }

    /**
     * Valeur de la feuille désignée par hit : celle du TableModel pour une
     * cellule non subdivisée, celle du noeud pour une sous-cellule. null si hit
     * ne désigne plus une feuille.
     */
    public Object getInternalCellValue(InternalCellHit hit) {
        CellNode node = nodeOf(hit);
        if (node == null || !node.isLeaf()) {
            return null;
        }
        return hit.isSubCell() ? node.getValue() : getModel().getValueAt(hit.row, hit.col);
    }

    /**
     * Écrit la valeur de la feuille désignée par hit (TableModel pour une
     * cellule non subdivisée, noeud pour une sous-cellule) et garde le
     * TableModel cohérent. Sans effet si hit ne désigne plus une feuille.
     */
    public void setInternalCellValue(InternalCellHit hit, Object value) {
        CellNode node = nodeOf(hit);
        if (node == null || !node.isLeaf()) {
            return;
        }
        if (hit.isSubCell()) {
            structureModel.setSubCellValue(hit.row, hit.col, hit.path, value);
            syncSubdividedValue(hit.row, hit.col);
        } else {
            getModel().setValueAt(value, hit.row, hit.col);
        }
        repaint();
    }

    /**
     * Noeud désigné par (modelRow, modelCol, path), ou null si le chemin ne
     * correspond plus à la structure.
     */
    private CellNode nodeOf(int modelRow, int modelCol, SubCellPath path) {
        return path.resolve(structureModel.getCellNode(modelRow, modelCol));
    }

    private CellNode nodeOf(InternalCellHit hit) {
        return hit == null ? null : nodeOf(hit.row, hit.col, hit.path);
    }

    /**
     * Garde le TableModel cohérent avec une cellule subdivisée : il contient la
     * concaténation des textes de ses sous-cellules (c'est ce que voient le
     * tri, la copie, les formules). N'écrit que si le texte diffère, pour ne
     * pas écraser inutilement une valeur typée (Integer, Date...).
     */
    private void syncSubdividedValue(int modelRow, int modelCol) {
        CellNode root = structureModel.getCellNode(modelRow, modelCol);
        if (root.isLeaf()) {
            return;
        }
        writeTextIfDifferent(modelRow, modelCol, root.collectText());
    }

    private void writeTextIfDifferent(int modelRow, int modelCol, Object text) {
        Object current = getModel().getValueAt(modelRow, modelCol);
        String currentText = current == null ? null : current.toString().trim();
        if (currentText != null && currentText.isEmpty()) {
            currentText = null;
        }
        String newText = text == null ? null : text.toString().trim();
        if (newText != null && newText.isEmpty()) {
            newText = null;
        }
        if (!Objects.equals(currentText, newText)) {
            getModel().setValueAt(text, modelRow, modelCol);
        }
    }

    // ── Lignes et colonnes ───────────────────────────────────────────────────
    // Les indices de cette API sont des indices VUE. HTable les convertit en
    // indices MODÈLE, appelle le point d'extension protégé correspondant
    // (insertRowInModel, removeColumnFromModel...) puis laisse la structure
    // suivre : les lignes via tableChanged (qui voit aussi les modifications
    // faites directement sur le TableModel), les colonnes ici.
    /**
     * Insère une ligne vide au-dessus de la ligne donnée.
     */
    public void insertRowAbove(int row) {
        if (row < 0 || row > getRowCount()) {
            return;
        }
        insertRowInModel(viewToModelRowInsertIndex(row));
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
        int widthSource = (col < oldColCount)
                ? toModelColumn(col)
                : (oldColCount > 0 ? toModelColumn(oldColCount - 1) : -1);
        insertColumnWithLayout(viewToModelColumnInsertIndex(col), name, widthSource);
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
        insertColumnWithLayout(viewToModelColumnInsertIndex(insertAt), name, toModelColumn(col));
    }

    /**
     * Insère une colonne à l'indice MODÈLE insertAt, en préservant les largeurs
     * des colonnes existantes et les hauteurs de lignes. widthSourceCol
     * désigne, dans l'ANCIENNE numérotation MODÈLE, la colonne dont la largeur
     * doit être reprise pour la nouvelle colonne. -1 ou hors bornes → largeur
     * de la dernière colonne existante, ou 100 si le tableau n'a aucune
     * colonne.
     */
    private void insertColumnWithLayout(int insertAt, String columnName, int widthSourceCol) {
        int oldColCount = getModel().getColumnCount();
        int[] savedWidths = new int[oldColCount];
        for (int m = 0; m < oldColCount; m++) {
            int v = convertColumnIndexToView(m);
            savedWidths[m] = v >= 0 ? getColumnModel().getColumn(v).getWidth() : 100;
        }

        int rowCount = getRowCount();
        int[] savedHeights = new int[rowCount];
        for (int r = 0; r < rowCount; r++) {
            savedHeights[r] = getRowHeight(r);
        }

        adjustingStructure = true;
        try {
            insertColumnInModel(insertAt, columnName);
            structureModel.columnsInserted(insertAt, insertAt);
            shiftDeclaredColumnClasses(insertAt, 1);
        } finally {
            adjustingStructure = false;
        }

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
        removeRowFromModel(toModelRow(row));
        refreshUI();
    }

    /**
     * Supprime plusieurs lignes en une fois. Les indices VUE sont convertis en
     * indices MODÈLE AVANT toute suppression, puis supprimés de la fin vers le
     * début pour éviter le décalage d'index.
     *
     * @param rows tableau des index à supprimer (non trié, c'est géré ici)
     */
    public void deleteRows(int[] rows) {
        Set<Integer> modelRows = new TreeSet<>(Collections.reverseOrder());
        for (int r : rows) {
            int modelRow = toModelRow(r);
            if (modelRow >= 0) {
                modelRows.add(modelRow);
            }
        }
        for (int modelRow : modelRows) {
            removeRowFromModel(modelRow);
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
        removeModelColumn(toModelColumn(col));
        refreshUI();
    }

    /**
     * Supprime plusieurs colonnes en une fois. Même principe que deleteRows() :
     * de droite à gauche.
     */
    public void deleteColumns(int[] cols) {
        Set<Integer> modelCols = new TreeSet<>(Collections.reverseOrder());
        for (int c : cols) {
            int modelCol = toModelColumn(c);
            if (modelCol >= 0) {
                modelCols.add(modelCol);
            }
        }
        for (int modelCol : modelCols) {
            removeModelColumn(modelCol);
        }
        refreshUI();
    }

    private void removeModelColumn(int modelCol) {
        adjustingStructure = true;
        try {
            removeColumnFromModel(modelCol);
            List<MergeRegion> dissolved = structureModel.columnsRemoved(modelCol, modelCol);
            restoreSurvivingValues(dissolved, false, modelCol, modelCol);
            declaredColumnClasses.remove(modelCol);
            shiftDeclaredColumnClasses(modelCol + 1, -1);
        } finally {
            adjustingStructure = false;
        }
    }

    /**
     * Vide le tableau (supprime toutes les lignes, conserve les colonnes) ainsi
     * que toute la structure (fusions, subdivisions, styles).
     */
    public void clearTable() {
        for (int r = getModel().getRowCount() - 1; r >= 0; r--) {
            removeRowFromModel(r);
        }
        structureModel.clear();
        refreshUI();
    }

    // ── Points d'extension : modification structurelle du TableModel ─────────
    // Le TableModel de Swing n'a pas d'API standard pour ajouter/supprimer une
    // ligne ou une colonne. Ces méthodes savent le faire pour DefaultTableModel
    // (et ses sous-classes). Pour un autre modèle, les surcharger.
    /**
     * Insère une ligne vide à l'indice MODÈLE donné.
     */
    protected void insertRowInModel(int modelIndex) {
        if (getModel() instanceof DefaultTableModel dm) {
            dm.insertRow(modelIndex, new Object[dm.getColumnCount()]);
            return;
        }
        throw new UnsupportedOperationException("Le TableModel " + getModel().getClass().getName()
                + " n'est pas un DefaultTableModel : surchargez insertRowInModel().");
    }

    /**
     * Supprime la ligne d'indice MODÈLE donné.
     */
    protected void removeRowFromModel(int modelIndex) {
        if (getModel() instanceof DefaultTableModel dm) {
            dm.removeRow(modelIndex);
            return;
        }
        throw new UnsupportedOperationException("Le TableModel " + getModel().getClass().getName()
                + " n'est pas un DefaultTableModel : surchargez removeRowFromModel().");
    }

    /**
     * Insère une colonne vide à l'indice MODÈLE donné. Doit signaler le
     * changement par un événement de structure (comme DefaultTableModel).
     */
    @SuppressWarnings("unchecked")
    protected void insertColumnInModel(int modelIndex, String name) {
        if (getModel() instanceof DefaultTableModel dm) {
            Vector<Object> identifiers = new Vector<>();
            for (int c = 0; c < dm.getColumnCount(); c++) {
                identifiers.add(dm.getColumnName(c));
            }
            identifiers.add(modelIndex, name);
            for (Object row : dm.getDataVector()) {
                ((Vector<Object>) row).add(modelIndex, null);
            }
            dm.setColumnIdentifiers(identifiers);
            return;
        }
        throw new UnsupportedOperationException("Le TableModel " + getModel().getClass().getName()
                + " n'est pas un DefaultTableModel : surchargez insertColumnInModel().");
    }

    /**
     * Supprime la colonne d'indice MODÈLE donné. Doit signaler le changement
     * par un événement de structure (comme DefaultTableModel).
     */
    @SuppressWarnings("unchecked")
    protected void removeColumnFromModel(int modelIndex) {
        if (getModel() instanceof DefaultTableModel dm) {
            Vector<Object> identifiers = new Vector<>();
            for (int c = 0; c < dm.getColumnCount(); c++) {
                if (c != modelIndex) {
                    identifiers.add(dm.getColumnName(c));
                }
            }
            for (Object row : dm.getDataVector()) {
                ((Vector<Object>) row).remove(modelIndex);
            }
            dm.setColumnIdentifiers(identifiers);
            return;
        }
        throw new UnsupportedOperationException("Le TableModel " + getModel().getClass().getName()
                + " n'est pas un DefaultTableModel : surchargez removeColumnFromModel().");
    }

    private int viewToModelRowInsertIndex(int viewRow) {
        return viewRow >= getRowCount() ? getModel().getRowCount() : toModelRow(viewRow);
    }

    private int viewToModelColumnInsertIndex(int viewCol) {
        return viewCol >= getColumnCount() ? getModel().getColumnCount() : toModelColumn(viewCol);
    }

    /**
     * Les types de colonne déclarés sont indexés par colonne MODÈLE : ils
     * suivent leur colonne quand on en insère ou supprime une avant eux.
     */
    private void shiftDeclaredColumnClasses(int fromModelColumn, int delta) {
        Map<Integer, Class<?>> shifted = new HashMap<>();
        for (Map.Entry<Integer, Class<?>> e : declaredColumnClasses.entrySet()) {
            int key = e.getKey();
            if (key >= fromModelColumn) {
                key += delta;
            }
            shifted.put(key, e.getValue());
        }
        declaredColumnClasses.clear();
        declaredColumnClasses.putAll(shifted);
    }

    // ── Fusion ───────────────────────────────────────────────────────────────
    /**
     * Fusionne les cellules dans la zone (r1,c1) → (r2,c2) (indices VUE). La
     * cellule en haut à gauche devient la cellule principale et concatène son
     * contenu à celui des autres cellules. Les autres cellules de la zone sont
     * vidées et marquées comme absorbées. Sans effet si la zone n'est pas
     * contiguë dans les données (tri, colonnes déplacées) ou chevauche
     * partiellement une fusion existante.
     *
     * @param r1 ligne du coin supérieur gauche
     * @param c1 colonne du coin supérieur gauche
     * @param r2 ligne du coin inférieur droit
     * @param c2 colonne du coin inférieur droit
     */
    public void mergeCells(int r1, int c1, int r2, int c2) {
        int rowStart = Math.min(r1, r2), rowEnd = Math.max(r1, r2);
        int colStart = Math.min(c1, c2), colEnd = Math.max(c1, c2);
        if (rowStart < 0 || colStart < 0 || rowEnd >= getRowCount() || colEnd >= getColumnCount()) {
            return;
        }
        if (!TableStructureIntegrity.isMergeableViewRange(this, rowStart, rowEnd, colStart, colEnd)) {
            return;
        }
        if (mergeModelRange(toModelRow(r1), toModelColumn(c1), toModelRow(r2), toModelColumn(c2))) {
            refreshUI();
        }
    }

    /**
     * Fusion en coordonnées MODÈLE. Les valeurs « avant fusion » sont confiées
     * à la structure (qui les rend au défusionnement) ; le TableModel reçoit le
     * texte concaténé dans la cellule principale, null ailleurs.
     */
    private boolean mergeModelRange(int mr1, int mc1, int mr2, int mc2) {
        int rowStart = Math.min(mr1, mr2), rowEnd = Math.max(mr1, mr2);
        int colStart = Math.min(mc1, mc2), colEnd = Math.max(mc1, mc2);
        TableModel m = getModel();
        if (rowStart < 0 || colStart < 0 || rowEnd >= m.getRowCount() || colEnd >= m.getColumnCount()) {
            return false;
        }
        if (rowStart == rowEnd && colStart == colEnd) {
            return false;
        }
        if (!structureModel.canMerge(rowStart, colStart, rowEnd, colEnd)) {
            return false;
        }

        adjustingStructure = true;
        try {
            // Les fusions entièrement contenues dans la zone vont être
            // dissoutes : on leur fait d'abord rendre leurs valeurs d'origine.
            for (MergeRegion inner : structureModel.getMergeRegions()) {
                if (inner.isInside(rowStart, colStart, rowEnd, colEnd)) {
                    restoreRegionValues(inner);
                }
            }

            int rSpan = rowEnd - rowStart + 1;
            int cSpan = colEnd - colStart + 1;
            Object[][] original = new Object[rSpan][cSpan];
            StringBuilder merged = new StringBuilder();
            for (int r = rowStart; r <= rowEnd; r++) {
                for (int c = colStart; c <= colEnd; c++) {
                    Object val = m.getValueAt(r, c);
                    original[r - rowStart][c - colStart] = val;
                    if (val != null && !val.toString().trim().isEmpty()) {
                        if (merged.length() > 0) {
                            merged.append(" ");
                        }
                        merged.append(val.toString().trim());
                    }
                }
            }

            structureModel.merge(rowStart, colStart, rowEnd, colEnd, original);

            m.setValueAt(merged.length() > 0 ? merged.toString() : null, rowStart, colStart);
            for (int r = rowStart; r <= rowEnd; r++) {
                for (int c = colStart; c <= colEnd; c++) {
                    if (r != rowStart || c != colStart) {
                        m.setValueAt(null, r, c);
                    }
                }
            }
        } finally {
            adjustingStructure = false;
        }
        return true;
    }

    /**
     * Défusionne la cellule à la position donnée (indices VUE). Si la cellule
     * est absorbée, remonte à la cellule principale et la libère.
     */
    public void unmergeCell(int row, int col) {
        int modelRow = toModelRow(row);
        int modelCol = toModelColumn(col);
        if (modelRow < 0 || modelCol < 0) {
            return;
        }
        MergeRegion region = structureModel.getMergeAt(modelRow, modelCol);
        if (region != null) {
            unmergeRegion(region);
        }
        refreshUI();
    }

    /**
     * Défait une fusion ET restitue au TableModel les valeurs qu'avaient les
     * cellules avant la fusion. Seul pont entre le modèle de structure et le
     * modèle de données pour cette opération : à utiliser plutôt que
     * getStructureModel().unmerge(...), qui laisserait les cellules libérées
     * vides.
     */
    public void unmergeRegion(MergeRegion region) {
        MergeRegion removed = structureModel.unmerge(region.originRow, region.originCol);
        if (removed == null) {
            return;
        }
        adjustingStructure = true;
        try {
            restoreRegionValues(removed);
        } finally {
            adjustingStructure = false;
        }
    }

    private void restoreRegionValues(MergeRegion region) {
        Object[][] values = region.getOriginalValues();
        TableModel m = getModel();
        for (int dr = 0; dr < region.rowSpan; dr++) {
            for (int dc = 0; dc < region.colSpan; dc++) {
                int r = region.originRow + dr;
                int c = region.originCol + dc;
                if (r < m.getRowCount() && c < m.getColumnCount()) {
                    m.setValueAt(values[dr][dc], r, c);
                }
            }
        }
    }

    /**
     * Après la suppression des lignes (ou colonnes) first..last, rend leurs
     * valeurs aux cellules survivantes des fusions dissoutes par cette
     * suppression, à leur nouvelle position.
     */
    private void restoreSurvivingValues(List<MergeRegion> dissolved, boolean rows, int first, int last) {
        int removed = last - first + 1;
        TableModel m = getModel();
        for (MergeRegion region : dissolved) {
            Object[][] values = region.getOriginalValues();
            for (int dr = 0; dr < region.rowSpan; dr++) {
                for (int dc = 0; dc < region.colSpan; dc++) {
                    int r = region.originRow + dr;
                    int c = region.originCol + dc;
                    if (rows) {
                        if (r >= first && r <= last) {
                            continue;
                        }
                        r = shiftIndex(r, last, removed);
                    } else {
                        if (c >= first && c <= last) {
                            continue;
                        }
                        c = shiftIndex(c, last, removed);
                    }
                    if (r < m.getRowCount() && c < m.getColumnCount()) {
                        m.setValueAt(values[dr][dc], r, c);
                    }
                }
            }
        }
    }

    private static int shiftIndex(int index, int lastRemoved, int removedCount) {
        return index > lastRemoved ? index - removedCount : index;
    }

    /**
     * Fractionne une cellule fusionnée en targetRows × targetCols
     * sous-cellules.Exemple : une fusion 4×4 fractionnée en (2, 2) donne quatre
     * blocs de 2×2.
     *
     *
     * @param row ligne de la cellule à fractionner (indice VUE)
     * @param col colonne de la cellule à fractionner (indice VUE)
     * @param targetRows nombre de lignes dans le fractionnement
     * @param targetCols nombre de colonnes dans le fractionnement
     * @return false si la cellule n'est pas fusionnée ou si la fusion n'est pas
     * divisible exactement
     */
    public boolean splitCell(int row, int col, int targetRows, int targetCols) {
        boolean success = splitModelCell(toModelRow(row), toModelColumn(col), targetRows, targetCols);
        refreshUI();
        return success;
    }

    private boolean splitModelCell(int modelRow, int modelCol, int targetRows, int targetCols) {
        if (modelRow < 0 || modelCol < 0 || targetRows < 1 || targetCols < 1) {
            return false;
        }
        MergeRegion region = structureModel.getMergeAt(modelRow, modelCol);
        if (region == null) {
            return false;
        }
        if (region.rowSpan % targetRows != 0 || region.colSpan % targetCols != 0) {
            return false;
        }

        int r = region.originRow, c = region.originCol;
        int blockR = region.rowSpan / targetRows;
        int blockC = region.colSpan / targetCols;

        unmergeRegion(region);

        if (blockR > 1 || blockC > 1) {
            for (int dr = 0; dr < targetRows; dr++) {
                for (int dc = 0; dc < targetCols; dc++) {
                    int startR = r + dr * blockR;
                    int startC = c + dc * blockC;
                    mergeModelRange(startR, startC, startR + blockR - 1, startC + blockC - 1);
                }
            }
        }
        return true;
    }

    /**
     * Coupe le tableau en deux à partir de la ligne donnée.Les lignes [0,
     * atRow-1] restent dans ce tableau. Les lignes [atRow, fin] sont retournées
     * dans un nouveau HTable indépendant.
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
                newData[r][c] = getModel().getValueAt(atRow + r, c);
            }
        }

        // Supprimer ces lignes du tableau courant (de bas en haut)
        for (int r = getModel().getRowCount() - 1; r >= atRow; r--) {
            removeRowFromModel(r);
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
        withStyle(row, col, s -> s.setAlignment(hAlign, vAlign));
        refreshUI();
    }

    /**
     * Aligne toutes les cellules d'une ligne.
     */
    public void setRowAlignment(int row, int hAlign, int vAlign) {
        for (int c = 0; c < getColumnCount(); c++) {
            withStyle(row, c, s -> s.setAlignment(hAlign, vAlign));
        }
        refreshUI();
    }

    /**
     * Aligne toutes les cellules d'une colonne.
     */
    public void setColumnAlignment(int col, int hAlign, int vAlign) {
        for (int r = 0; r < getRowCount(); r++) {
            withStyle(r, col, s -> s.setAlignment(hAlign, vAlign));
        }
        refreshUI();
    }

    /**
     * Aligne toutes les cellules de la sélection courante.
     */
    public void setSelectionAlignment(int hAlign, int vAlign) {
        forEachSelectedCell((r, c) -> withStyle(r, c, s -> s.setAlignment(hAlign, vAlign)));
        refreshUI();
    }

    /**
     * Aligne tout le tableau d'un coup.
     */
    public void setTableAlignment(int hAlign, int vAlign) {
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                withStyle(r, c, s -> s.setAlignment(hAlign, vAlign));
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
        withStyle(row, col, s -> s.setTextDirection(direction));
        refreshUI();
    }

    /**
     * Applique la direction à toute une colonne.
     */
    public void setColumnTextDirection(int col, int direction) {
        for (int r = 0; r < getRowCount(); r++) {
            withStyle(r, col, s -> s.setTextDirection(direction));
        }
        refreshUI();
    }

    /**
     * Applique la direction à toute une ligne.
     */
    public void setRowTextDirection(int row, int direction) {
        for (int c = 0; c < getColumnCount(); c++) {
            withStyle(row, c, s -> s.setTextDirection(direction));
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
        withStyle(row, col, s -> s.setMargins(margins));
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
        TableRowSorter<TableModel> sorter = newNonClickableSorter(getModel());
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
        TableRowSorter<TableModel> sorter = newNonClickableSorter(getModel());
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
                Object val = getValueAt(r, c);
                sb.append(val != null ? val.toString() : "");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ── Formules ─────────────────────────────────────────────────────────────
    // Les formules portent sur les DONNÉES : (row, col) y sont des indices
    // MODÈLE, comme la notation A1 qu'elles utilisent. Un tri de l'affichage ne
    // change donc ni leur cible ni leur résultat.
    /**
     * Insère une formule dans une cellule. La formule est évaluée immédiatement
     * et le résultat est affiché. La formule brute est stockée dans le
     * CellStyle de la cellule pour permettre la recalculation ultérieure.
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
     * @param row ligne de la cellule cible (0-indexée, MODÈLE)
     * @param col colonne de la cellule cible (0-indexée, MODÈLE)
     * @param formula la formule, doit commencer par "="
     */
    public void setCellFormula(int row, int col, String formula) {
        if (formula == null || !formula.startsWith("=") || !isModelCell(row, col)) {
            return;
        }
        structureModel.getCellStyle(row, col, SubCellPath.ROOT).setFormula(formula);
        Object result = HTableFormula.evaluate(formula, getModel(), row, col);
        getModel().setValueAt(result, row, col);
        refreshUI();
    }

    /**
     * Évalue la formule stockée dans une cellule et retourne le résultat. Ne
     * modifie pas le tableau — utile pour prévisualiser un calcul.
     *
     * @param row ligne de la cellule (MODÈLE)
     * @param col colonne de la cellule (MODÈLE)
     * @return le résultat numérique, ou un message d'erreur si la formule est
     * invalide ou si les données ne sont pas numériques
     */
    public Object evaluateFormula(int row, int col) {
        if (!isModelCell(row, col)) {
            return null;
        }
        String formula = structureModel.getCellStyle(row, col, SubCellPath.ROOT).getFormula();
        if (formula == null || formula.isEmpty()) {
            return getModel().getValueAt(row, col);
        }
        return HTableFormula.evaluate(formula, getModel(), row, col);
    }

    /**
     * Recalcule toutes les formules du tableau. À appeler après une
     * modification des données pour mettre à jour les cellules qui contiennent
     * des formules dépendantes.
     */
    public void recalculateAllFormulas() {
        TableModel m = getModel();
        for (int r = 0; r < m.getRowCount(); r++) {
            for (int c = 0; c < m.getColumnCount(); c++) {
                String formula = structureModel.getCellStyle(r, c, SubCellPath.ROOT).getFormula();
                if (formula != null && !formula.isEmpty()) {
                    Object result = HTableFormula.evaluate(formula, m, r, c);
                    m.setValueAt(result, r, c);
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
        withStyle(row, col, CellStyle::reset);
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
        if (structureModel != null) {
            int modelRow = toModelRow(row);
            int modelCol = toModelColumn(col);
            if (modelRow >= 0 && modelCol >= 0 && structureModel.isAbsorbed(modelRow, modelCol)) {
                return false;
            }
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
                    TableStructureIntegrity.invalidateAllMergesOnSort(this);
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
    // MODÈLE DE STRUCTURE
    // =========================================================================
    /**
     * Fusions, subdivisions et styles, en coordonnées MODÈLE. Lecture libre ;
     * pour défusionner, préférer unmergeRegion()/unmergeCell() qui restituent
     * aussi les valeurs au TableModel.
     */
    public CellStructureModel getStructureModel() {
        return structureModel;
    }

    /**
     * Remplace le modèle de structure (propriété liée « structureModel »). Les
     * sous-cellules focusées/éditées sont abandonnées.
     */
    public void setStructureModel(CellStructureModel newModel) {
        Objects.requireNonNull(newModel, "structureModel");
        CellStructureModel old = structureModel;
        if (old == newModel) {
            return;
        }
        if (old != null) {
            old.removeCellStructureListener(structureListener);
        }
        structureModel = newModel;
        structureModel.addCellStructureListener(structureListener);
        clearInternalCellStates();
        firePropertyChange("structureModel", old, newModel);
        refreshUI();
    }

    /**
     * Vrai (défaut) : un changement de STRUCTURE du TableModel (nouvelles
     * colonnes, setDataVector...) ou un « toutes les données ont changé »
     * efface fusions, subdivisions et styles. Mettre à faux si l'on gère
     * soi-même le modèle de structure.
     */
    public boolean isAutoCreateStructureFromModel() {
        return autoCreateStructureFromModel;
    }

    public void setAutoCreateStructureFromModel(boolean autoCreate) {
        boolean old = this.autoCreateStructureFromModel;
        this.autoCreateStructureFromModel = autoCreate;
        firePropertyChange("autoCreateStructureFromModel", old, autoCreate);
    }

    /**
     * Style de la cellule entière (indices VUE), ou null si hors bornes.
     * Instance vivante du modèle de structure : la modifier modifie le style
     * stocké (préférer les setters de HTable, qui rafraîchissent l'affichage).
     */
    public CellStyle getCellStyle(int viewRow, int viewCol) {
        int modelRow = toModelRow(viewRow);
        int modelCol = toModelColumn(viewCol);
        if (modelRow < 0 || modelCol < 0) {
            return null;
        }
        return structureModel.getCellStyle(modelRow, modelCol, SubCellPath.ROOT);
    }

    /**
     * Applique une modification au style de la cellule (indices VUE) ; sans
     * effet si hors bornes.
     */
    private void withStyle(int viewRow, int viewCol, Consumer<CellStyle> change) {
        CellStyle style = getCellStyle(viewRow, viewCol);
        if (style != null) {
            change.accept(style);
        }
    }

    /**
     * Parcourt les cellules réellement sélectionnées (indices VUE) : les lignes
     * sélectionnées × les colonnes sélectionnées, comme le modèle de sélection.
     * Ne fait rien s'il n'y a pas de sélection.
     */
    private void forEachSelectedCell(BiConsumer<Integer, Integer> action) {
        if (!hasSelection()) {
            return;
        }
        for (int row : cellSelectionModel.getSelectedRows()) {
            for (int col : cellSelectionModel.getSelectedColumns()) {
                action.accept(row, col);
            }
        }
    }

    /**
     * Style du noeud désigné par hit, ou null si hit ne correspond plus à la
     * structure.
     */
    private CellStyle styleOf(InternalCellHit hit) {
        try {
            return structureModel.getCellStyle(hit.row, hit.col, hit.path);
        } catch (IllegalArgumentException stale) {
            return null;
        }
    }

    private boolean isModelCell(int modelRow, int modelCol) {
        return modelRow >= 0 && modelRow < getModel().getRowCount()
                && modelCol >= 0 && modelCol < getModel().getColumnCount();
    }

    private void requireModelCell(int modelRow, int modelCol) {
        if (!isModelCell(modelRow, modelCol)) {
            throw new IndexOutOfBoundsException(
                    "Coordonnées invalides : (" + modelRow + ", " + modelCol + ")");
        }
    }

    /**
     * Abandonne les sous-cellules survolée/focusée/sélectionnée/en cours
     * d'édition : leurs adresses ne signifient plus rien après un décalage ou
     * une réinitialisation de la structure.
     */
    private void clearInternalCellStates() {
        focusedInternalCell = null;
        hoveredInternalCell = null;
        selectedInternalCell = null;
        editingInternalCell = null;
        if (internalEditor != null && internalEditor.isVisible()) {
            internalEditor.setVisible(false);
        }
    }

    /**
     * Abandonne uniquement celles qui ne désignent plus aucun noeud.
     */
    private void pruneStaleInternalCells() {
        if (isStale(focusedInternalCell)) {
            focusedInternalCell = null;
        }
        if (isStale(hoveredInternalCell)) {
            hoveredInternalCell = null;
        }
        if (isStale(selectedInternalCell)) {
            selectedInternalCell = null;
        }
        if (isStale(editingInternalCell)) {
            editingInternalCell = null;
            if (internalEditor != null) {
                internalEditor.setVisible(false);
            }
        }
    }

    private boolean isStale(InternalCellHit hit) {
        if (hit == null) {
            return false;
        }
        try {
            return TableGeometry.boundsOf(this, hit) == null;
        } catch (RuntimeException outOfRange) {
            return true;
        }
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
     * @return int[]{row, col} (indices VUE) de la cellule principale
     */
    public int[] resolvePoint(Point point) {
        int row = rowAtPoint(point);
        int col = columnAtPoint(point);
        if (row < 0 || col < 0) {
            return new int[]{row, col};
        }
        int modelRow = toModelRow(row);
        int modelCol = toModelColumn(col);
        MergeRegion region = structureModel.getMergeAt(modelRow, modelCol);
        if (region != null && !(region.originRow == modelRow && region.originCol == modelCol)) {
            // Position VUE de la cellule principale
            return new int[]{convertRowIndexToView(region.originRow), convertColumnIndexToView(region.originCol)};
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
        return focusedInternalCell != null && focusedInternalCell.isSubCell();
    }

    /**
     * JTable appelle updateUI() à chaque changement de Look and Feel : sans
     * cette surcharge, le delegate HBasicTableUI serait remplacé par celui du
     * L&F. (Pendant la construction de JTable, nos champs ne sont pas encore
     * initialisés : le constructeur de HTable installe lui-même HBasicTableUI.)
     */
    @Override
    public void updateUI() {
        super.updateUI();
        if (structureModel != null) {
            setUI(new HBasicTableUI());
        }
    }

    /**
     * Necessaire pour utiliser notre newNonClickableSorter(model) qui bloque le
     * tri natif
     *
     * @param autoCreateRowSorter
     */
    @Override
    public void setAutoCreateRowSorter(boolean autoCreateRowSorter) {
        setSortingEnabled(autoCreateRowSorter);
    }

    @Override
    public boolean getAutoCreateRowSorter() {
        return isSortingEnabled();
    }

    @Override
    public void setModel(TableModel dataModel) {
        TableModel previous = getModel();
        super.setModel(dataModel);
        if (structureModel == null) {
            return; // appel depuis le constructeur de JTable
        }
        if (dataModel != previous) {
            // Fusions, subdivisions et styles décrivaient l'ancien modèle
            declaredColumnClasses.clear();
            structureModel.clear();
        }
        if (getAutoCreateRowSorter()) {
            setRowSorter(newNonClickableSorter(dataModel));
        }
    }

    /**
     * Garde la structure alignée sur le TableModel, quelle que soit l'origine
     * de la modification : lignes insérées/supprimées → la structure se décale
     * ; structure du modèle changée → elle est réinitialisée (voir
     * setAutoCreateStructureFromModel). Les modifications que HTable fait
     * elle-même (adjustingStructure) sont déjà répercutées.
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (structureModel == null || adjustingStructure) {
            return;
        }
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
            if (autoCreateStructureFromModel) {
                structureModel.clear();
            }
            return;
        }
        switch (e.getType()) {
            case TableModelEvent.INSERT ->
                structureModel.rowsInserted(e.getFirstRow(), e.getLastRow());
            case TableModelEvent.DELETE -> {
                adjustingStructure = true;
                try {
                    List<MergeRegion> dissolved = structureModel.rowsRemoved(e.getFirstRow(), e.getLastRow());
                    restoreSurvivingValues(dissolved, true, e.getFirstRow(), e.getLastRow());
                } finally {
                    adjustingStructure = false;
                }
            }
            default -> {
                // fireTableDataChanged() : « tout a changé, y compris le nombre de lignes »
                if (e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE
                        && autoCreateStructureFromModel) {
                    structureModel.clear();
                }
            }
        }
    }

    /**
     * Réaction aux changements de la structure (quelle qu'en soit l'origine :
     * HTable, code externe, modèle de structure de test...).
     */
    private void structureChanged(CellStructureEvent e) {
        switch (e.getType()) {
            case RESET, SHIFTED, MERGED, UNMERGED ->
                clearInternalCellStates();
            default ->
                pruneStaleInternalCells();
        }
        refreshUI();
    }

    // ── Type des colonnes ────────────────────────────────────────────────────
    /**
     * Type de la colonne VUE : déclaré (setColumnClass), sinon celui du modèle,
     * sinon inféré de la première valeur non nulle — pour que renderers,
     * éditeurs et tri numérique fonctionnent même sur un modèle qui ne
     * renseigne pas getColumnClass (DefaultTableModel renvoie toujours Object).
     */
    @Override
    public Class<?> getColumnClass(int column) {
        if (structureModel == null) {
            return super.getColumnClass(column); // construction de JTable
        }
        return modelColumnClass(convertColumnIndexToModel(column));
    }

    private Class<?> modelColumnClass(int modelColumn) {
        Class<?> declared = declaredColumnClasses.get(modelColumn);
        if (declared != null) {
            return declared;
        }
        Class<?> fromModel = getModel().getColumnClass(modelColumn);
        if (fromModel != null && fromModel != Object.class) {
            return fromModel;
        }
        TableModel m = getModel();
        for (int row = 0; row < m.getRowCount(); row++) {
            Object value = m.getValueAt(row, modelColumn);
            if (value != null) {
                return value.getClass();
            }
        }
        return Object.class;
    }

    /**
     * TableRowSorter dont aucune colonne n'est "sortable" au sens de
     * toggleSortOrder() — c'est ce que vérifie en premier le clic natif du
     * JTableHeader avant d'agir. Ça neutralise le tri-au-clic-n'importe-où de
     * Swing sans toucher à setSortKeys(), notre propre chemin de tri, qui ne
     * passe pas par ce garde-fou.
     *
     * Le comparateur s'appuie sur modelColumnClass() (déclaré/inféré) plutôt
     * que sur getColumnClass() du modèle, pour que le tri numérique reste
     * numérique avec un DefaultTableModel.
     */
    private TableRowSorter<TableModel> newNonClickableSorter(TableModel model) {
        return new TableRowSorter<>(model) {
            @Override
            public boolean isSortable(int column) {
                return false;
            }

            @Override
            public Comparator<?> getComparator(int column) {
                Class<?> type = modelColumnClass(column);
                if (type == String.class || !Comparable.class.isAssignableFrom(type)) {
                    return Collator.getInstance();
                }
                return (Comparator<Object>) (a, b) -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Comparable<Object> comparable = (Comparable<Object>) a;
                        return comparable.compareTo(b);
                    } catch (ClassCastException mixedTypes) {
                        return a.toString().compareTo(b.toString());
                    }
                };
            }

            @Override
            protected boolean useToString(int column) {
                Class<?> type = modelColumnClass(column);
                return !(type == String.class || Comparable.class.isAssignableFrom(type));
            }
        };
    }

    /**
     * Active ou désactive NOTRE fonctionnalité de tri (icône d'en-tête +
     * cycleSort) — à ne pas confondre avec setAutoCreateRowSorter() ci-dessus,
     * qui concerne le raccourci natif de JTable, bloqué en permanence pour une
     * tout autre raison. Celle-ci, au contraire, est un vrai interrupteur : un
     * développeur qui ne veut aucun tri sur son tableau l'utilise. Désactiver
     * efface aussi un tri déjà en cours.
     */
    public void setSortingEnabled(boolean sortingEnabled) {
        this.sortingEnabled = sortingEnabled;
        if (!sortingEnabled) {
            clearSort();
        }
        repaint();
    }

    public boolean isSortingEnabled() {
        return sortingEnabled;
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
