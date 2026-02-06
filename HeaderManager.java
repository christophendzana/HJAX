/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban.layout;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import rubban.GroupRenderer;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/** 
 * Gère la création, le cache et le positionnement des composants "header"
 * pour les groupes du Ribbon.
 *
 * Règles:
 * - HeaderManager conserve un cache Map<groupIndex, Component>.
 * - La création des headers se fait via GroupRenderer.getHeaderComponent(...)
 *   et l'ajout/retrait effectif des composants est délégué au Ribbon
 *   (ribbon.addHeaderComponent / ribbon.removeHeaderComponent).
 * @author FIDELE
 */
public class HeaderManager {
    
      private final Map<Integer, Component> headerCache = new HashMap<>();

    public HeaderManager() {
    }

    /**
     * Met à jour le cache des headers et positionne chaque header selon
     * l'alignement relatif au groupBounds fournis.
     */
    public Map<Integer, Component> updateAndPositionHeaders(final Ribbon ribbon,
                                                           final HRibbonLayoutContext ctx,
                                                           final HRibbonGroupModel groupModel,
                                                           final Rectangle[] groupBounds) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HeaderManager.updateAndPositionHeaders must be called on the EDT");
        }
        if (ribbon == null || ctx == null || groupModel == null || groupBounds == null) {
            return Collections.unmodifiableMap(new HashMap<>(headerCache));
        }

        // Remove headers for groups that disappeared
        Set<Integer> current = new HashSet<>();
        for (int i = 0; i < groupBounds.length; i++) current.add(i);
        for (Integer idx : new HashSet<>(headerCache.keySet())) {
            if (!current.contains(idx)) {
                Component old = headerCache.remove(idx);
                if (old != null) ribbon.removeHeaderComponent(old);
            }
        }

        int headerAlignment = ctx.getHeaderAlignment();
        int headerWidth = ctx.getHeaderWidth();
        int headerMargin = ctx.getHeaderMargin();
        Insets insets = ribbon.getInsets();

        for (int i = 0; i < groupBounds.length; i++) {
            Rectangle contentRect = groupBounds[i];
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group == null || contentRect == null) continue;

            Component header = headerCache.get(i);
            if (header == null) {
                GroupRenderer renderer = ribbon.getGroupRenderer();
                if (renderer != null) {
                    Object headerValue = ribbon.getHeaderValue(i);
                    try {
                        header = renderer.getHeaderComponent(ribbon, headerValue, i, false);
                    } catch (Throwable t) { header = null; }
                    if (header != null) {
                        ribbon.addHeaderComponent(header);
                        headerCache.put(i, header);
                    }
                }
            }

            if (header == null) continue;

            int hx = 0, hy = 0, hw = 0, hh = 0;
            switch (headerAlignment) {
                case Ribbon.HEADER_NORTH:
                    hw = contentRect.width;
                    hh = ribbon.getHeaderHeight();
                    hx = contentRect.x;
                    hy = contentRect.y - headerMargin - hh;
                    break;
                case Ribbon.HEADER_SOUTH:
                    hw = contentRect.width;
                    hh = ribbon.getHeaderHeight();
                    hx = contentRect.x;
                    hy = contentRect.y + contentRect.height + headerMargin;
                    break;
                case Ribbon.HEADER_WEST:
                    hw = headerWidth;
                    hh = contentRect.height;
                    hx = contentRect.x - headerMargin - hw;
                    hy = contentRect.y;
                    break;
                case Ribbon.HEADER_EAST:
                    hw = headerWidth;
                    hh = contentRect.height;
                    hx = contentRect.x + contentRect.width + headerMargin;
                    hy = contentRect.y;
                    break;
                default:
                    header.setVisible(false);
                    continue;
            }

            hw = Math.max(0, hw);
            hh = Math.max(0, hh);
            header.setBounds(hx, hy, hw, hh);
            header.setVisible(true);
        }

        return Collections.unmodifiableMap(new HashMap<>(headerCache));
    }

    public void removeHeaderForGroup(Ribbon ribbon, int groupIndex) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HeaderManager.removeHeaderForGroup must be called on the EDT");
        }
        Component old = headerCache.remove(groupIndex);
        if (old != null && ribbon != null) ribbon.removeHeaderComponent(old);
    }

    public void invalidateAllHeaders(Ribbon ribbon) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HeaderManager.invalidateAllHeaders must be called on the EDT");
        }
        if (ribbon != null) {
            for (Component h : headerCache.values()) {
                if (h != null) ribbon.removeHeaderComponent(h);
            }
        }
        headerCache.clear();
    }
}
