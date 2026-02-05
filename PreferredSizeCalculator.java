/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import rubban.GroupRenderer;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.HRibbonModel;
import rubban.Ribbon;

/**
 * PreferredSizeCalculator
 *
 * Responsabilité :
 * - Simuler le wrapping des composants par groupe et calculer la hauteur
 *   de contenu requise (excluant les insets). 
 *
 * Remarques :
 * - Utilise un cache pour mémoriser les preferredSizes des
 *   valeurs non-Component créées via le renderer afin de réduire le coût des
 *   mesures répétées. Le cache peut être vidé via clearCache().
 * @author FIDELE
 */
public class PreferredSizeCalculator {
    
     // Cache faible : value -> Dimension (preferred size)
    private final Map<Object, Dimension> measureCache = new WeakHashMap<>();

    // Valeurs par défaut lorsque la mesure échoue
    private static final Dimension DEFAULT_DIM = new Dimension(50, 24);

    public PreferredSizeCalculator() {
    }

    /**
     * Vide le cache de mesures.
     */
    public void clearCache() {
        measureCache.clear();
    }

    /**
     * Simule le wrapping et calcule la hauteur de contenu nécessaire (padding + lignes).
     *
     * Doit être appelé sur l'EDT car il interroge getPreferredSize() sur les Components
     * potentiellement créés par le renderer.
     *
     * @param ribbon       le Ribbon (utilisé pour obtenir renderer)
     * @param ctx          contexte layout (header alignment/width, etc.)
     * @param groupWidths  largeurs totales par groupe (peuvent inclure header area)
     * @param model        HRibbonModel (valeurs)
     * @param groupModel   HRibbonGroupModel (configuration des groupes)
     * @return hauteur de contenu requise (en pixels), excluant insets; 0 si pas de groupes
     * @throws IllegalStateException si appelé hors EDT
     */
    public int computeRequiredContentHeight(Ribbon ribbon,
                                            HRibbonLayoutContext ctx,
                                            int[] groupWidths,
                                            HRibbonModel model,
                                            HRibbonGroupModel groupModel) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("PreferredSizeCalculator.computeRequiredContentHeight must be called on the EDT");
        }

        if (ribbon == null || model == null || groupModel == null || groupWidths == null) {
            return 0;
        }

        final int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) return 0;

        final int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        final int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;

        LineWrapper lw = new LineWrapper();
        int maxRequired = 0;
        GroupRenderer renderer = ribbon.getGroupRenderer();

        for (int gi = 0; gi < groupCount; gi++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(gi);
            if (group == null) {
                continue;
            }

            // largeur totale du groupe (peut inclure header area)
            int totalGroupWidth = (gi < groupWidths.length) ? Math.max(0, groupWidths[gi]) : 0;
            int contentWidth = totalGroupWidth;
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                contentWidth = Math.max(totalGroupWidth - headerWidth, 1);
            }

            int padding = Math.max(0, group.getPadding());
            int innerWidth = Math.max(contentWidth - padding * 2, 1);
            int spacing = Math.max(0, group.getComponentSpacing());

            int valueCount = model.getValueCount(gi);
            if (valueCount <= 0) {
                maxRequired = Math.max(maxRequired, padding * 2);
                continue;
            }

            // Collecter preferred sizes (Dimensions) pour chaque valeur
            List<Dimension> prefs = new ArrayList<>(valueCount);
            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, gi);
                Dimension pref = null;

                if (value instanceof Component) {
                    Component comp = (Component) value;
                    pref = comp.getPreferredSize();
                } else {
                    // Vérifier cache
                    pref = measureCache.get(value);
                    if (pref == null && renderer != null) {
                        try {
                            Component measureComp = renderer.getGroupComponent(ribbon, value, gi, pos, false, false);
                            if (measureComp != null) {
                                pref = measureComp.getPreferredSize();
                                // NOT adding measureComp to the container -- renderer must not have side-effects
                            }
                        } catch (Throwable t) {
                            pref = null;
                        }
                        if (pref == null) {
                            pref = DEFAULT_DIM;
                        }
                        // Stocker un clone de Dimension (defensive)
                        measureCache.put(value, new Dimension(pref.width, pref.height));
                    }
                }

                if (pref == null) {
                    pref = DEFAULT_DIM;
                }

                prefs.add(new Dimension(pref.width, pref.height));
            }

            // Calcul de la hauteur nécessaire pour ce groupe
            int totalHeight = lw.computeHeightForPreferences(prefs, innerWidth, spacing, padding);
            maxRequired = Math.max(maxRequired, totalHeight);
        }

        return Math.max(0, maxRequired);
    }
    
}
