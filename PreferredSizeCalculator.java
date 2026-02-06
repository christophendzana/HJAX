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
    
    private final Map<Object, Dimension> measureCache = new WeakHashMap<>();
    private static final Dimension DEFAULT_DIM = new Dimension(50, 24);

    public PreferredSizeCalculator() { }

    public void clearCache() { measureCache.clear(); }

    public int computeRequiredContentHeight(Ribbon ribbon,
                                            HRibbonLayoutContext ctx,
                                            int[] groupWidths,
                                            HRibbonModel model,
                                            HRibbonGroupModel groupModel) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("PreferredSizeCalculator.computeRequiredContentHeight must be called on the EDT");
        }
        if (ribbon == null || ctx == null || model == null || groupModel == null || groupWidths == null) return 0;

        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) return 0;

        int headerAlignment = ctx.getHeaderAlignment();
        int headerMargin = ctx.getHeaderMargin();
        int headerHeight = ribbon.getHeaderHeight();

        LineWrapper lw = new LineWrapper();
        int maxTotal = 0;
        GroupRenderer renderer = ribbon.getGroupRenderer();

        for (int gi = 0; gi < groupCount; gi++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(gi);
            if (group == null) continue;

            int totalGroupWidth = (gi < groupWidths.length) ? Math.max(0, groupWidths[gi]) : 0;
            int contentWidth = totalGroupWidth;
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                contentWidth = Math.max(totalGroupWidth - ctx.getHeaderWidth(), 1);
            }

            int padding = Math.max(0, group.getPadding());
            int innerWidth = Math.max(contentWidth - padding * 2, 1);
            int spacing = Math.max(0, group.getComponentSpacing());

            int valueCount = model.getValueCount(gi);
            if (valueCount <= 0) {
                int groupTotal = padding * 2;
                if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
                    groupTotal += headerHeight + headerMargin;
                }
                maxTotal = Math.max(maxTotal, groupTotal);
                continue;
            }

            List<Dimension> prefs = new ArrayList<>(valueCount);
            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, gi);
                Dimension pref = null;
                if (value instanceof Component) {
                    pref = ((Component) value).getPreferredSize();
                } else {
                    pref = measureCache.get(value);
                    if (pref == null && renderer != null) {
                        try {
                            Component mc = renderer.getGroupComponent(ribbon, value, gi, pos, false, false);
                            if (mc != null) pref = mc.getPreferredSize();
                        } catch (Throwable t) {
                            pref = null;
                        }
                        if (pref == null) pref = DEFAULT_DIM;
                        measureCache.put(value, new Dimension(pref.width, pref.height));
                    }
                }
                if (pref == null) pref = DEFAULT_DIM;
                prefs.add(new Dimension(pref.width, pref.height));
            }

            int contentHeight = lw.computeHeightForPreferences(prefs, innerWidth, spacing, padding);
            int groupTotal = contentHeight;
            if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
                groupTotal += headerHeight + headerMargin;
            }
            maxTotal = Math.max(maxTotal, groupTotal);
        }

        return Math.max(0, maxTotal);
    }
    
}
