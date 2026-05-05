package hsupertable;

import hcomponents.HButton;
import hcomponents.HFrame;
import hcomponents.HLabel;
import hcomponents.HOptionPane;
import hcomponents.HScrollPane;
import hcomponents.HSeparator;
import hcomponents.HTextArea;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * HSuperTableTestApp — Application de test complète pour HSuperTable.
 *
 * Chaque fonctionnalité des onglets "Création" et "Disposition" de Word
 * est représentée par un bouton. On clique, on voit le résultat sur le tableau.
 *
 * Organisation des panneaux latéraux (comme les rubans de Word) :
 *   - Panneau CRÉATION : styles, trame, bordures, options
 *   - Panneau DISPOSITION : structure, fusion, taille, alignement, données
 *
 * Pour lancer : exécuter main() directement.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSuperTableTestApp extends JFrame {

    // Le tableau sous test — tout tourne autour de lui
    private final HSuperTable table;

    // ScrollPane qui contient le tableau
    private final JScrollPane scrollPane;

    // Zone de log en bas — affiche ce qui vient d'être fait
    private final HTextArea logArea;

    // =========================================================================
    // DONNÉES DE TEST
    // =========================================================================

    private static final Object[] COLUMNS = {
        "Nom", "Prénom", "Département", "Salaire", "Note", "Statut"
    };

    private static final Object[][] DATA = {
        { "Mbarga",   "Jean",    "Informatique", 850000,  18.5, "Actif"    },
        { "Fouda",    "Marie",   "Comptabilité", 720000,  15.0, "Actif"    },
        { "Nkomo",    "Paul",    "RH",           680000,  12.5, "Congé"    },
        { "Essono",   "Claire",  "Informatique", 920000,  19.0, "Actif"    },
        { "Biyong",   "Henri",   "Marketing",    750000,  14.0, "Inactif"  },
        { "Ateba",    "Sylvie",  "Comptabilité", 700000,  16.5, "Actif"    },
        { "Owona",    "Thierry", "Informatique", 880000,  17.0, "Actif"    },
        { "Mbassi",   "Grace",   "RH",           660000,  13.0, "Congé"    },
        { "Ndongo",   "Alain",   "Marketing",    730000,  15.5, "Actif"    },
        { "Bikele",   "Lucie",   "Direction",    1200000, 20.0, "Actif"    },
    };

    // =========================================================================
    // CONSTRUCTION DE LA FENÊTRE
    // =========================================================================

    public HSuperTableTestApp() {
        super("HSuperTable — Banc de test complet");
        setDefaultCloseOperation(HFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setLocationRelativeTo(null);

        // Initialisation du tableau avec les données de test
        table = new HSuperTable(DATA, COLUMNS);
        scrollPane = new HScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("HSuperTable"));

        // Zone de log
        logArea = new HTextArea(4, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(80, 220, 80));
        HScrollPane logScroll = new HScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Journal des actions"));

        // Panneau central : tableau + log
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(logScroll, BorderLayout.SOUTH);
        center.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Panneau latéral gauche : onglet Création
        JPanel leftPanel = buildCreationPanel();

        // Panneau latéral droit : onglet Disposition
        JPanel rightPanel = buildDispositionPanel();

        // Mise en page globale
        setLayout(new BorderLayout(4, 4));
        add(leftPanel,  BorderLayout.WEST);
        add(center,     BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        log("Application démarrée — HSuperTable prête pour les tests.");
    }

    // =========================================================================
    // PANNEAU CRÉATION
    // =========================================================================

    private JPanel buildCreationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("── Onglet CRÉATION ──"));
        panel.setPreferredSize(new Dimension(220, 0));

        // ── Options de style ─────────────────────────────────────────────────
        panel.add(sectionLabel("Options de style"));

        panel.add(btn("✔ Ligne d'en-tête ON", e -> {
            table.setHeaderRowEnabled(true);
            log("setHeaderRowEnabled(true)");
        }));
        panel.add(btn("✘ Ligne d'en-tête OFF", e -> {
            table.setHeaderRowEnabled(false);
            log("setHeaderRowEnabled(false)");
        }));
        panel.add(btn("✔ Ligne totale ON", e -> {
            table.setTotalRowEnabled(true);
            log("setTotalRowEnabled(true) — dernière ligne mise en valeur");
        }));
        panel.add(btn("✘ Ligne totale OFF", e -> {
            table.setTotalRowEnabled(false);
            log("setTotalRowEnabled(false)");
        }));
        panel.add(btn("✔ Lignes à bandes", e -> {
            table.setBandedRows(true);
            table.setBandedColumns(false);
            log("setBandedRows(true) — alternance de couleurs sur les lignes");
        }));
        panel.add(btn("✔ Colonnes à bandes", e -> {
            table.setBandedColumns(true);
            table.setBandedRows(false);
            log("setBandedColumns(true) — alternance de couleurs sur les colonnes");
        }));
        panel.add(btn("✔ 1ère colonne", e -> {
            table.setFirstColumnHighlighted(true);
            log("setFirstColumnHighlighted(true)");
        }));
        panel.add(btn("✔ Dernière colonne", e -> {
            table.setLastColumnHighlighted(true);
            log("setLastColumnHighlighted(true)");
        }));
        panel.add(btn("↺ Reset options", e -> {
            table.setBandedRows(true);
            table.setBandedColumns(false);
            table.setFirstColumnHighlighted(false);
            table.setLastColumnHighlighted(false);
            table.setTotalRowEnabled(false);
            table.setHeaderRowEnabled(true);
            log("Toutes les options de style remises par défaut");
        }));

        panel.add(sep());

        // ── Galerie de styles ─────────────────────────────────────────────────
        panel.add(sectionLabel("Styles prédéfinis"));

        // Clairs
        panel.add(btn("Style PLAIN",        e -> applyStyle(HSuperTableStyle.PLAIN,        "PLAIN")));
        panel.add(btn("Style PLAIN_BLUE",   e -> applyStyle(HSuperTableStyle.PLAIN_BLUE,   "PLAIN_BLUE")));
        panel.add(btn("Style PLAIN_GREEN",  e -> applyStyle(HSuperTableStyle.PLAIN_GREEN,  "PLAIN_GREEN")));
        panel.add(btn("Style PLAIN_RED",    e -> applyStyle(HSuperTableStyle.PLAIN_RED,    "PLAIN_RED")));
        panel.add(btn("Style PLAIN_ORANGE", e -> applyStyle(HSuperTableStyle.PLAIN_ORANGE, "PLAIN_ORANGE")));
        panel.add(btn("Style PLAIN_PURPLE", e -> applyStyle(HSuperTableStyle.PLAIN_PURPLE, "PLAIN_PURPLE")));
        // Moyens
        panel.add(btn("Style PRIMARY",   e -> applyStyle(HSuperTableStyle.PRIMARY,   "PRIMARY")));
        panel.add(btn("Style SUCCESS",   e -> applyStyle(HSuperTableStyle.SUCCESS,   "SUCCESS")));
        panel.add(btn("Style DANGER",    e -> applyStyle(HSuperTableStyle.DANGER,    "DANGER")));
        panel.add(btn("Style WARNING",   e -> applyStyle(HSuperTableStyle.WARNING,   "WARNING")));
        panel.add(btn("Style SECONDARY", e -> applyStyle(HSuperTableStyle.SECONDARY, "SECONDARY")));
        panel.add(btn("Style INFO",      e -> applyStyle(HSuperTableStyle.INFO,      "INFO")));
        panel.add(btn("Style PURPLE",    e -> applyStyle(HSuperTableStyle.PURPLE,    "PURPLE")));
        panel.add(btn("Style PINK",      e -> applyStyle(HSuperTableStyle.PINK,      "PINK")));
        panel.add(btn("Style BROWN",     e -> applyStyle(HSuperTableStyle.BROWN,     "BROWN")));
        panel.add(btn("Style TEAL",      e -> applyStyle(HSuperTableStyle.TEAL,      "TEAL")));
        // Sombres
        panel.add(btn("Style DARK_BLUE",   e -> applyStyle(HSuperTableStyle.DARK_BLUE,   "DARK_BLUE")));
        panel.add(btn("Style DARK_GRAY",   e -> applyStyle(HSuperTableStyle.DARK_GRAY,   "DARK_GRAY")));
        panel.add(btn("Style DARK_GREEN",  e -> applyStyle(HSuperTableStyle.DARK_GREEN,  "DARK_GREEN")));
        panel.add(btn("Style DARK_RED",    e -> applyStyle(HSuperTableStyle.DARK_RED,    "DARK_RED")));
        panel.add(btn("Style DARK_PURPLE", e -> applyStyle(HSuperTableStyle.DARK_PURPLE, "DARK_PURPLE")));
        // Spéciaux
        panel.add(btn("Style ZEBRA",      e -> applyStyle(HSuperTableStyle.ZEBRA,      "ZEBRA")));
        panel.add(btn("Style BORDERLESS", e -> applyStyle(HSuperTableStyle.BORDERLESS, "BORDERLESS")));

        // Style custom via Builder
        panel.add(btn("Style CUSTOM (Builder)", e -> {
            HSuperTableStyle custom = new HSuperTableStyle.Builder()
                .headerBackground(new Color(80, 0, 120))
                .headerForeground(Color.WHITE)
                .cellBackground(Color.WHITE)
                .cellAlternateBackground(new Color(245, 235, 255))
                .gridColor(new Color(180, 140, 220))
                .focusBorderColor(new Color(80, 0, 120))
                .totalRowBackground(new Color(80, 0, 120))
                .totalRowForeground(Color.WHITE)
                .firstColumnBackground(new Color(235, 215, 255))
                .lastColumnBackground(new Color(235, 215, 255))
                .build();
            table.setTableStyle(custom);
            log("Style custom créé via Builder — violet foncé");
        }));

        panel.add(btn("↺ Reset style", e -> {
            table.resetStyle();
            log("resetStyle() — retour au style PRIMARY");
        }));

        panel.add(sep());

        // ── Trame de fond ─────────────────────────────────────────────────────
        panel.add(sectionLabel("Trame de fond"));

        panel.add(btn("Fond cellule (0,0) jaune", e -> {
            table.setCellBackground(0, 0, new Color(255, 250, 180));
            log("setCellBackground(0, 0, jaune)");
        }));
        panel.add(btn("Fond ligne 3 vert pâle", e -> {
            table.setRowBackground(3, new Color(200, 255, 210));
            log("setRowBackground(3, vert pâle)");
        }));
        panel.add(btn("Fond colonne 2 bleu pâle", e -> {
            table.setColumnBackground(2, new Color(210, 230, 255));
            log("setColumnBackground(2, bleu pâle)");
        }));
        panel.add(btn("Texte cellule (1,1) rouge", e -> {
            table.setCellForeground(1, 1, Color.RED);
            log("setCellForeground(1, 1, rouge)");
        }));
        panel.add(btn("Texte ligne 0 blanc", e -> {
            table.setRowForeground(0, Color.WHITE);
            log("setRowForeground(0, blanc)");
        }));

        panel.add(sep());

        // ── Bordures ──────────────────────────────────────────────────────────
        panel.add(sectionLabel("Bordures"));

        panel.add(btn("Bordure SOLID rouge (0,0)", e -> {
            table.setCellBorderAll(0, 0, Color.RED, 2f, HSuperTable.BORDER_SOLID);
            log("setCellBorderAll(0,0) — rouge solid 2px");
        }));
        panel.add(btn("Bordure DASHED bleue (1,1)", e -> {
            table.setCellBorderAll(1, 1, Color.BLUE, 1.5f, HSuperTable.BORDER_DASHED);
            log("setCellBorderAll(1,1) — bleu tirets 1.5px");
        }));
        panel.add(btn("Bordure DOTTED verte (2,2)", e -> {
            table.setCellBorderAll(2, 2, new Color(0, 160, 0), 1f, HSuperTable.BORDER_DOTTED);
            log("setCellBorderAll(2,2) — vert pointillés 1px");
        }));
        panel.add(btn("Bord bas uniquement (3,0)", e -> {
            table.setCellBorderSide(3, 0, HSuperTable.SIDE_BOTTOM,
                Color.ORANGE, 3f, HSuperTable.BORDER_SOLID);
            log("setCellBorderSide(3,0) SIDE_BOTTOM — orange 3px");
        }));
        panel.add(btn("Bords haut+gauche (4,1)", e -> {
            table.setCellBorderSide(4, 1,
                HSuperTable.SIDE_TOP | HSuperTable.SIDE_LEFT,
                new Color(128, 0, 128), 2f, HSuperTable.BORDER_SOLID);
            log("setCellBorderSide(4,1) TOP|LEFT — violet 2px");
        }));
        panel.add(btn("Bordure OUTER tableau", e -> {
            table.setBorderOuter(new Color(33, 37, 41), 2.5f, HSuperTable.BORDER_SOLID);
            log("setBorderOuter() — contour extérieur du tableau");
        }));
        panel.add(btn("Bordure INNER tableau", e -> {
            table.setBorderInner(new Color(150, 150, 150), 1f, HSuperTable.BORDER_SOLID);
            log("setBorderInner() — séparations internes du tableau");
        }));
        panel.add(btn("Bordures ALL tableau", e -> {
            table.setBorderAll(new Color(80, 80, 80), 1f, HSuperTable.BORDER_SOLID);
            log("setBorderAll() — toutes les bordures du tableau");
        }));
        panel.add(btn("Supprimer bordure (0,0)", e -> {
            table.removeCellBorder(0, 0);
            log("removeCellBorder(0, 0)");
        }));
        panel.add(btn("Supprimer TOUTES bordures", e -> {
            table.removeAllBorders();
            log("removeAllBorders()");
        }));

        // Rendre le panneau scrollable (beaucoup de boutons)
        HScrollPane scroll = new HScrollPane(panel);
        scroll.setPreferredSize(new Dimension(230, 0));
        scroll.setHorizontalScrollBarPolicy(HScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================================
    // PANNEAU DISPOSITION
    // =========================================================================

    private JPanel buildDispositionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("── Onglet DISPOSITION ──"));
        panel.setPreferredSize(new Dimension(220, 0));

        // ── Tableau / Sélection ───────────────────────────────────────────────
        panel.add(sectionLabel("Sélection"));

        panel.add(btn("Sélectionner cellule (2,3)", e -> {
            table.selectCell(2, 3);
            log("selectCell(2, 3)");
        }));
        panel.add(btn("Sélectionner ligne 4", e -> {
            table.selectRow(4);
            log("selectRow(4)");
        }));
        panel.add(btn("Sélectionner colonne 1", e -> {
            table.selectColumn(1);
            log("selectColumn(1)");
        }));
        panel.add(btn("Sélectionner tout", e -> {
            table.selectAll();
            log("selectAll()");
        }));
        panel.add(btn("Effacer sélection", e -> {
            table.clearSelection();
            log("clearSelection()");
        }));

        panel.add(sep());

        // ── Quadrillage ───────────────────────────────────────────────────────
        panel.add(sectionLabel("Quadrillage"));

        panel.add(btn("✔ Afficher quadrillage", e -> {
            table.setGridVisible(true);
            log("setGridVisible(true)");
        }));
        panel.add(btn("✘ Masquer quadrillage", e -> {
            table.setGridVisible(false);
            log("setGridVisible(false)");
        }));

        panel.add(sep());

        // ── Lignes & Colonnes ─────────────────────────────────────────────────
        panel.add(sectionLabel("Lignes & Colonnes"));

        panel.add(btn("Insérer ligne AU-DESSUS de 2", e -> {
            table.insertRowAbove(2);
            log("insertRowAbove(2)");
        }));
        panel.add(btn("Insérer ligne EN-DESSOUS de 2", e -> {
            table.insertRowBelow(2);
            log("insertRowBelow(2)");
        }));
        panel.add(btn("Insérer colonne À GAUCHE de 2", e -> {
            table.insertColumnLeft(2);
            log("insertColumnLeft(2)");
        }));
        panel.add(btn("Insérer colonne À DROITE de 2", e -> {
            table.insertColumnRight(2);
            log("insertColumnRight(2)");
        }));
        panel.add(btn("Supprimer ligne 5", e -> {
            if (table.getRowCount() > 5) {
                table.deleteRow(5);
                log("deleteRow(5)");
            } else {
                log("Pas assez de lignes pour supprimer la ligne 5");
            }
        }));
        panel.add(btn("Supprimer colonnes [1,2]", e -> {
            if (table.getColumnCount() > 2) {
                table.deleteColumns(new int[]{1, 2});
                log("deleteColumns([1, 2])");
            } else {
                log("Pas assez de colonnes");
            }
        }));
        panel.add(btn("Supprimer lignes sélectionnées", e -> {
            table.deleteSelectedRows();
            log("deleteSelectedRows()");
        }));
        panel.add(btn("Vider tout le tableau", e -> {
            int confirm = HOptionPane.showConfirmDialog(this,
                "Vider toutes les lignes ?", "Confirmation",
                HOptionPane.YES_NO_OPTION);
            if (confirm == HOptionPane.YES_OPTION) {
                table.clearTable();
                log("clearTable() — toutes les lignes supprimées");
            }
        }));
        panel.add(btn("↺ Recharger données test", e -> {
            reloadData();
            log("Données de test rechargées");
        }));

        panel.add(sep());

        // ── Fusion ────────────────────────────────────────────────────────────
        panel.add(sectionLabel("Fusion / Fractionnement"));

        panel.add(btn("Fusionner (0,0)→(1,1)", e -> {
            table.mergeCells(0, 0, 1, 1);
            log("mergeCells(0,0 → 1,1) — fusion 2×2");
        }));
        panel.add(btn("Fusionner (2,0)→(2,2)", e -> {
            table.mergeCells(2, 0, 2, 2);
            log("mergeCells(2,0 → 2,2) — fusion 1×3");
        }));
        panel.add(btn("Fusionner (4,3)→(6,5)", e -> {
            table.mergeCells(4, 3, 6, 5);
            log("mergeCells(4,3 → 6,5) — fusion 3×3");
        }));
        panel.add(btn("Défusionner cellule (0,0)", e -> {
            table.unmergeCell(0, 0);
            log("unmergeCell(0, 0)");
        }));
        panel.add(btn("Fractionner (4,3) en 3×3", e -> {
            table.splitCell(4, 3, 3, 3);
            log("splitCell(4,3) → 3 lignes × 3 colonnes");
        }));
        panel.add(btn("Fractionner tableau à ligne 5", e -> {
            if (table.getRowCount() > 5) {
                HSuperTable newTable = table.splitTable(5);
                if (newTable != null) {
                    showDetachedTable(newTable);
                    log("splitTable(5) — nouveau tableau dans une fenêtre séparée");
                }
            } else {
                log("Pas assez de lignes pour fractionner à la ligne 5");
            }
        }));

        panel.add(sep());

        // ── Taille de cellule ─────────────────────────────────────────────────
        panel.add(sectionLabel("Taille de cellule"));

        panel.add(btn("Hauteur ligne 0 = 60px", e -> {
            table.setRowHeight(0, 60);
            log("setRowHeight(0, 60)");
        }));
        panel.add(btn("Toutes lignes = 40px", e -> {
            table.setAllRowsHeight(40);
            log("setAllRowsHeight(40)");
        }));
        panel.add(btn("Largeur colonne 0 = 180px", e -> {
            table.setColumnWidth(0, 180);
            log("setColumnWidth(0, 180)");
        }));
        panel.add(btn("Toutes colonnes = 120px", e -> {
            table.setAllColumnsWidth(120);
            log("setAllColumnsWidth(120)");
        }));
        panel.add(btn("Répartir lignes uniformément", e -> {
            table.distributeRowsEvenly();
            log("distributeRowsEvenly()");
        }));
        panel.add(btn("Répartir colonnes uniformément", e -> {
            table.distributeColumnsEvenly();
            log("distributeColumnsEvenly()");
        }));
        panel.add(btn("Ajustement AUTO contenu", e -> {
            table.autoFit(HSuperTable.AUTOFIT_CONTENT);
            log("autoFit(AUTOFIT_CONTENT)");
        }));
        panel.add(btn("Ajustement AUTO fenêtre", e -> {
            table.autoFit(HSuperTable.AUTOFIT_WINDOW);
            log("autoFit(AUTOFIT_WINDOW)");
        }));

        panel.add(sep());

        // ── Alignement ────────────────────────────────────────────────────────
        panel.add(sectionLabel("Alignement (9 positions)"));

        // Ligne 0 — en haut
        panel.add(btn("↖ Haut-Gauche  (ligne 0)", e -> {
            table.setRowAlignment(0, SwingConstants.LEFT, SwingConstants.TOP);
            log("setRowAlignment(0, LEFT, TOP)");
        }));
        panel.add(btn("↑ Haut-Centre  (ligne 0)", e -> {
            table.setRowAlignment(0, SwingConstants.CENTER, SwingConstants.TOP);
            log("setRowAlignment(0, CENTER, TOP)");
        }));
        panel.add(btn("↗ Haut-Droite  (ligne 0)", e -> {
            table.setRowAlignment(0, SwingConstants.RIGHT, SwingConstants.TOP);
            log("setRowAlignment(0, RIGHT, TOP)");
        }));
        // Ligne 1 — milieu
        panel.add(btn("← Milieu-Gauche (ligne 1)", e -> {
            table.setRowAlignment(1, SwingConstants.LEFT, SwingConstants.CENTER);
            log("setRowAlignment(1, LEFT, CENTER)");
        }));
        panel.add(btn("✛ Milieu-Centre (ligne 1)", e -> {
            table.setRowAlignment(1, SwingConstants.CENTER, SwingConstants.CENTER);
            log("setRowAlignment(1, CENTER, CENTER)");
        }));
        panel.add(btn("→ Milieu-Droite (ligne 1)", e -> {
            table.setRowAlignment(1, SwingConstants.RIGHT, SwingConstants.CENTER);
            log("setRowAlignment(1, RIGHT, CENTER)");
        }));
        // Ligne 2 — en bas
        panel.add(btn("↙ Bas-Gauche   (ligne 2)", e -> {
            table.setRowAlignment(2, SwingConstants.LEFT, SwingConstants.BOTTOM);
            log("setRowAlignment(2, LEFT, BOTTOM)");
        }));
        panel.add(btn("↓ Bas-Centre   (ligne 2)", e -> {
            table.setRowAlignment(2, SwingConstants.CENTER, SwingConstants.BOTTOM);
            log("setRowAlignment(2, CENTER, BOTTOM)");
        }));
        panel.add(btn("↘ Bas-Droite   (ligne 2)", e -> {
            table.setRowAlignment(2, SwingConstants.RIGHT, SwingConstants.BOTTOM);
            log("setRowAlignment(2, RIGHT, BOTTOM)");
        }));
        panel.add(btn("Aligner colonne 3 = droite", e -> {
            table.setColumnAlignment(3, SwingConstants.RIGHT, SwingConstants.CENTER);
            log("setColumnAlignment(3, RIGHT, CENTER) — colonne Salaire");
        }));
        panel.add(btn("Aligner cellule (5,4) centre", e -> {
            table.setCellAlignment(5, 4, SwingConstants.CENTER, SwingConstants.CENTER);
            log("setCellAlignment(5, 4, CENTER, CENTER)");
        }));
        panel.add(btn("Aligner tout le tableau", e -> {
            table.setTableAlignment(SwingConstants.CENTER, SwingConstants.CENTER);
            log("setTableAlignment(CENTER, CENTER) — tout centré");
        }));

        panel.add(sep());

        // ── Direction du texte ────────────────────────────────────────────────
        panel.add(sectionLabel("Direction du texte"));

        panel.add(btn("Texte VERTICAL UP col 0", e -> {
            table.setColumnTextDirection(0, HSuperTable.TEXT_VERTICAL_UP);
            log("setColumnTextDirection(0, TEXT_VERTICAL_UP)");
        }));
        panel.add(btn("Texte VERTICAL DOWN col 1", e -> {
            table.setColumnTextDirection(1, HSuperTable.TEXT_VERTICAL_DOWN);
            log("setColumnTextDirection(1, TEXT_VERTICAL_DOWN)");
        }));
        panel.add(btn("Texte HORIZONTAL col 0+1", e -> {
            table.setColumnTextDirection(0, HSuperTable.TEXT_HORIZONTAL);
            table.setColumnTextDirection(1, HSuperTable.TEXT_HORIZONTAL);
            log("setColumnTextDirection(0+1, TEXT_HORIZONTAL) — retour normal");
        }));
        panel.add(btn("Texte vertical cellule (3,5)", e -> {
            table.setCellTextDirection(3, 5, HSuperTable.TEXT_VERTICAL_UP);
            log("setCellTextDirection(3, 5, TEXT_VERTICAL_UP)");
        }));

        panel.add(sep());

        // ── Marges ────────────────────────────────────────────────────────────
        panel.add(sectionLabel("Marges de cellule"));

        panel.add(btn("Marges globales larges (20px)", e -> {
            table.setDefaultCellMargins(new Insets(10, 20, 10, 20));
            table.setAllRowsHeight(50);
            log("setDefaultCellMargins(10, 20, 10, 20) + hauteur 50px");
        }));
        panel.add(btn("Marges globales serrées (4px)", e -> {
            table.setDefaultCellMargins(new Insets(2, 4, 2, 4));
            table.setAllRowsHeight(28);
            log("setDefaultCellMargins(2, 4, 2, 4) + hauteur 28px");
        }));
        panel.add(btn("Marge custom cellule (0,2)", e -> {
            table.setCellMargins(0, 2, new Insets(15, 30, 15, 30));
            log("setCellMargins(0, 2, 15, 30, 15, 30)");
        }));
        panel.add(btn("Retirer marge cellule (0,2)", e -> {
            table.setCellMargins(0, 2, null);
            log("setCellMargins(0, 2, null) — retour aux marges globales");
        }));

        panel.add(sep());

        // ── Données ───────────────────────────────────────────────────────────
        panel.add(sectionLabel("Données"));

        panel.add(btn("Trier col 0 croissant (Nom)", e -> {
            table.sortByColumn(0, SortOrder.ASCENDING);
            log("sortByColumn(0, ASCENDING) — tri par Nom A→Z");
        }));
        panel.add(btn("Trier col 3 décroissant (Salaire)", e -> {
            table.sortByColumn(3, SortOrder.DESCENDING);
            log("sortByColumn(3, DESCENDING) — tri par Salaire décroissant");
        }));
        panel.add(btn("Tri multi-colonnes (Dept + Salaire)", e -> {
            table.sortByColumns(
                new int[]{2, 3},
                new SortOrder[]{SortOrder.ASCENDING, SortOrder.DESCENDING}
            );
            log("sortByColumns([2,3], [ASC, DESC]) — Département puis Salaire");
        }));
        panel.add(btn("↺ Effacer tri", e -> {
            table.clearSort();
            log("clearSort() — retour à l'ordre naturel");
        }));
        panel.add(btn("Formule SUM col 3 (ligne 9)", e -> {
            // La colonne D (index 3) contient les salaires
            // On calcule la somme D1:D9 et on la met dans D10 (index 9)
            ensureRow(9);
            table.setCellFormula(9, 3, "=SUM(D1:D9)");
            log("setCellFormula(9, 3, '=SUM(D1:D9)') → " + table.getHModel().getValueAt(9, 3));
        }));
        panel.add(btn("Formule AVERAGE col 4 (ligne 9)", e -> {
            ensureRow(9);
            table.setCellFormula(9, 4, "=AVERAGE(E1:E9)");
            log("setCellFormula(9, 4, '=AVERAGE(E1:E9)') → " + table.getHModel().getValueAt(9, 4));
        }));
        panel.add(btn("Formule MAX salaire (ligne 8,col 3)", e -> {
            ensureRow(8);
            table.setCellFormula(8, 3, "=MAX(D1:D7)");
            log("setCellFormula(8, 3, '=MAX(D1:D7)') → " + table.getHModel().getValueAt(8, 3));
        }));
        panel.add(btn("Formule MIN salaire (ligne 8,col 4)", e -> {
            ensureRow(8);
            table.setCellFormula(8, 4, "=MIN(D1:D7)");
            log("setCellFormula(8, 4, '=MIN(D1:D7)') → " + table.getHModel().getValueAt(8, 4));
        }));
        panel.add(btn("Formule COUNT (ligne 9, col 5)", e -> {
            ensureRow(9);
            table.setCellFormula(9, 5, "=COUNT(F1:F9)");
            log("setCellFormula(9, 5, '=COUNT(F1:F9)') → " + table.getHModel().getValueAt(9, 5));
        }));
        panel.add(btn("Recalculer toutes formules", e -> {
            table.recalculateAllFormulas();
            log("recalculateAllFormulas()");
        }));
        panel.add(btn("Convertir en texte (TAB)", e -> {
            String text = table.convertToText("\t");
            showTextResult("Contenu converti en texte", text);
            log("convertToText(TAB) — " + text.split("\n").length + " lignes exportées");
        }));
        panel.add(btn("Convertir en texte ( ; )", e -> {
            String text = table.convertToText(";");
            showTextResult("Contenu CSV (point-virgule)", text);
            log("convertToText(';') — format CSV");
        }));
        panel.add(btn("↺ Reset formatage cellule (0,0)", e -> {
            table.resetCellFormatting(0, 0);
            log("resetCellFormatting(0, 0)");
        }));
        panel.add(btn("↺ Effacer états visuels", e -> {
            table.clearAllVisualStates();
            log("clearAllVisualStates()");
        }));

        // Rendre le panneau scrollable
        HScrollPane scroll = new HScrollPane(panel);
        scroll.setPreferredSize(new Dimension(230, 0));
        scroll.setHorizontalScrollBarPolicy(HScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================================
    // UTILITAIRES INTERNES
    // =========================================================================

    /** Crée un bouton avec le libellé et l'action donnés. Style sobre et uniforme. */
    private HButton btn(String label, ActionListener action) {
        HButton b = new HButton(label);
        b.addActionListener(action);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setMargin(new Insets(2, 6, 2, 6));
        b.setFocusPainted(false);
        return b;
    }

    /** Crée un label de section (titre de groupe de boutons). */
    private HLabel sectionLabel(String title) {
        HLabel lbl = new HLabel("  " + title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(13, 110, 253));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 240)));
        return lbl;
    }

    /** Crée un séparateur visuel entre les sections. */
    private HSeparator sep() {
        HSeparator s = new HSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        return s;
    }

    /** Applique un style et log le nom. */
    private void applyStyle(HSuperTableStyle style, String name) {
        table.setTableStyle(style);
        log("setTableStyle(" + name + ")");
    }

    /** Ajoute un message dans la zone de log. */
    private void log(String message) {
        logArea.append("▶ " + message + "\n");
        // Auto-scroll vers le bas
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /**
     * S'assure que le tableau a au moins (rowIndex+1) lignes.
     * Ajoute des lignes vides si nécessaire — utile pour les formules.
     */
    private void ensureRow(int rowIndex) {
        while (table.getRowCount() <= rowIndex) {
            table.getHModel().addEmptyRow();
        }
    }

    /** Recharge les données de test initiales dans le tableau. */
    private void reloadData() {
        table.clearAllVisualStates();
        table.clearTable();
        // On recrée le modèle avec les données de base
        for (Object[] row : DATA) {
            table.getHModel().addRow(row);
        }
        table.setTableStyle(HSuperTableStyle.PRIMARY);
        table.setBandedRows(true);
        table.setDefaultCellMargins(new Insets(6, 12, 6, 12));
        table.setAllRowsHeight(36);
    }

    /**
     * Ouvre une petite fenêtre pour afficher le tableau résultat de splitTable().
     */
    private void showDetachedTable(HSuperTable newTable) {
        HFrame frame = new HFrame("Tableau fractionné");
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(this);
        frame.add(new HScrollPane(newTable));
        frame.setVisible(true);
    }

    /**
     * Ouvre une fenêtre pour afficher le résultat de convertToText().
     */
    private void showTextResult(String title, String content) {
        HFrame frame = new HFrame(title);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(this);
        HTextArea area = new HTextArea(content);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        frame.add(new HScrollPane(area));
        frame.setVisible(true);
    }

    // =========================================================================
    // MAIN
    // =========================================================================

    public static void main(String[] args) {
        // Applique le look & feel système pour un rendu natif propre
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Pas critique — on continue avec le L&F par défaut
        }

        // Swing doit toujours être lancé depuis l'EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            HSuperTableTestApp app = new HSuperTableTestApp();
            app.setVisible(true);
        });
    }
}
