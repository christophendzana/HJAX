package hsupertable.menu;

import hcomponents.HMenu;
import hsupertable.HTable;
import hsupertable.model.HDefaultTableModel;
import hcomponents.HMenuItem;
import hcomponents.HPopupMenu;
import hsupertable.model.InternalGrid;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.JTableHeader;

/**
 * HTableContextMenuHandler — porte les actions par défaut et
 * personnalisées des menus contextuels (cellules et en-têtes), leur
 * construction/affichage, et les boîtes de dialogue associées (subdivision
 * en grille, insertion de formule).
 *
 * Extrait de HSuperTable : aucune dépendance vers le rendu, le contrôleur
 * de sélection ou de resize.
 */
public class MenuHandler {

    private final HTable table;
    private final List<ContextAction> contextActions = new ArrayList<>();
    private final List<HeaderAction> headerActions = new ArrayList<>();

    public MenuHandler(HTable table) {
        this.table = table;
        initializeDefaultContextActions();
        initializeDefaultHeaderActions();
    }

    // ── GESTION DES ACTIONS — CELLULES ──────────────────────────────────────
    public void addContextAction(ContextAction action) {
        if (action != null) contextActions.add(action);
    }

    public void removeContextAction(ContextAction action) {
        contextActions.remove(action);
    }

    public void clearContextActions() {
        contextActions.clear();
    }

    public List<ContextAction> getContextActions() {
        return Collections.unmodifiableList(contextActions);
    }

    // ── GESTION DES ACTIONS — EN-TÊTES ──────────────────────────────────────
    public void addHeaderAction(HeaderAction action) {
        if (action != null) headerActions.add(action);
    }

    public void removeHeaderAction(HeaderAction action) {
        headerActions.remove(action);
    }

    public void clearHeaderActions() {
        headerActions.clear();
    }

    public List<HeaderAction> getHeaderActions() {
        return Collections.unmodifiableList(headerActions);
    }

    // ── CONSTRUCTION ET AFFICHAGE — MENU CELLULES ───────────────────────────
    public void showContextMenu(TableContext ctx, int x, int y) {
        HPopupMenu popup = new HPopupMenu();
        boolean lastWasSeparator = true;
        int visibleItemCount = 0;

        for (ContextAction action : contextActions) {
            if (!action.isVisible(ctx)) continue;

            if ("---".equals(action.getName())) {
                if (!lastWasSeparator && visibleItemCount > 0) {
                    popup.addSeparator();
                    lastWasSeparator = true;
                }
                continue;
            }

            HMenuItem item = new HMenuItem(action.getName());
            item.setEnabled(action.isEnabled(ctx));
            item.addActionListener(e -> action.perform(ctx));
            popup.add(item);
            lastWasSeparator = false;
            visibleItemCount++;
        }

        int count = popup.getComponentCount();
        if (count > 0 && popup.getComponent(count - 1) instanceof JSeparator) {
            popup.remove(count - 1);
        }
        if (visibleItemCount > 0) {
            popup.show(table, x, y);
        }
    }

    // ── CONSTRUCTION ET AFFICHAGE — MENU EN-TÊTES ───────────────────────────
    public void showHeaderMenu(HeaderContext ctx, int x, int y) {
        HPopupMenu popup = new HPopupMenu();
        boolean lastWasSeparator = true;
        int visibleItemCount = 0;

        for (HeaderAction action : headerActions) {
            if (!action.isVisible(ctx)) continue;

            if ("---".equals(action.getName())) {
                if (!lastWasSeparator && visibleItemCount > 0) {
                    popup.addSeparator();
                    lastWasSeparator = true;
                }
                continue;
            }

            JComponent menuItem = action.buildMenuItem(ctx);
            popup.add(menuItem);
            lastWasSeparator = false;
            visibleItemCount++;
        }

        int count = popup.getComponentCount();
        if (count > 0 && popup.getComponent(count - 1) instanceof JSeparator) {
            popup.remove(count - 1);
        }
        if (visibleItemCount > 0) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                popup.show(header, x, y);
            }
        }
    }

    // ── DIALOGUES ────────────────────────────────────────────────────────────
    public void showSplitCellDialog(int row, int col) {
        Window parent = SwingUtilities.getWindowAncestor(table);
        SplitCellDialog dialog = new SplitCellDialog(parent);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            table.splitCellGrid(row, col, dialog.getRows(), dialog.getCols());
        }
    }

    public void showFormulaDialog(int row, int col) {
        String suggestion = suggestFormula(row, col);
        String existing = table.getHModel().getCellModel(row, col).getFormula();
        String initial = (existing != null && !existing.isEmpty()) ? existing : suggestion;

        Window parent = SwingUtilities.getWindowAncestor(table);
        HFormulaDialog dialog = new HFormulaDialog(parent, initial);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            String formula = dialog.getFormula();
            if (formula != null && formula.startsWith("=")) {
                table.setCellFormula(row, col, formula);
            }
        }
    }

    private String suggestFormula(int row, int col) {
        HDefaultTableModel hModel = table.getHModel();

        boolean hasAbove = false;
        for (int r = 0; r < row; r++) {
            Object val = hModel.getValueAt(r, col);
            if (val != null) {
                try {
                    Double.parseDouble(val.toString());
                    hasAbove = true;
                    break;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (hasAbove) return "=SUM(ABOVE)";

        boolean hasLeft = false;
        for (int c = 0; c < col; c++) {
            Object val = hModel.getValueAt(row, c);
            if (val != null) {
                try {
                    Double.parseDouble(val.toString());
                    hasLeft = true;
                    break;
                } catch (NumberFormatException ignored) {}
            }
        }
        return hasLeft ? "=SUM(LEFT)" : "=";
    }

    public boolean isEnabled(TableContext ctx) {
    if (!ctx.hasMultipleSelection) return false;
    HTable.CellRange sel = ctx.table.getSelection();
    if (sel == null) return false;
    return table.getHModel().getMergeModel().canMergeSelection(
            sel.rowStart, sel.colStart, sel.rowEnd, sel.colEnd,
            ctx.table.getHModel().getMergeModel());
}
    
    // ── ACTIONS PAR DÉFAUT — CELLULES ───────────────────────────────────────
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
                        InternalGrid.SPLIT_VERTICAL,
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
                        InternalGrid.SPLIT_HORIZONTAL,
                        0.5f
                );
            }
        });

// ── SUBDIVISER EN GRILLE ──────────────────────────────────────────────
        contextActions.add(new ContextAction("Subdiviser en grille...") {
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
                ctx.table.showSplitCellDialog(ctx.row, ctx.column);
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

        // ── SÉPARATEUR FORMULE ────────────────────────────────────────────────
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

// ── INSÉRER UNE FORMULE ───────────────────────────────────────────────
        contextActions.add(new ContextAction("Insérer une formule...") {
            @Override
            public boolean isVisible(TableContext ctx) {
                return !ctx.isInternalCell && !ctx.isAbsorbed;
            }

            @Override
            public boolean isEnabled(TableContext ctx) {
                return ctx.row >= 0 && ctx.column >= 0;
            }

            @Override
            public void perform(TableContext ctx) {
                ctx.table.showFormulaDialog(ctx.row, ctx.column);
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

    

    // ── ACTIONS PAR DÉFAUT — EN-TÊTES ───────────────────────────────────────
    private void initializeDefaultHeaderActions() {
        /// ── RENOMMER ──────────────────────────────────────────────────────────
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
}