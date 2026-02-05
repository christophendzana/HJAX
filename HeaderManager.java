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
     * l'alignement (NORTH/SOUTH/WEST/EAST).
     *
     * IMPORTANT: doit être appelé sur l'EDT (layoutContainer est appelé sur l'EDT).
     *
     * @param ribbon       le Ribbon (utilisé pour obtenir renderer, headerWidth, etc.)
     * @param groupModel   le modèle des groupes
     * @param groupBounds  rectangles de contenu calculés pour chaque groupe
     * @param insets       insets du parent (utilisé pour calculer y pour NORTH/SOUTH)
     * @param headerHeight hauteur en pixels des headers (utile pour NORTH/SOUTH)
     * @return une copie immuable du map index->Component correspondant aux headers actuels
     * @throws IllegalStateException si la méthode est appelée hors EDT
     */
    public Map<Integer, Component> updateAndPositionHeaders(final Ribbon ribbon,
                                                           final HRibbonGroupModel groupModel,
                                                           final Rectangle[] groupBounds,
                                                           final Insets insets,
                                                           final int headerHeight) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HeaderManager.updateAndPositionHeaders must be called on the EDT");
        }

        if (ribbon == null || groupModel == null || groupBounds == null) {
            // nothing to do, return current cache snapshot
            return Collections.unmodifiableMap(new HashMap<>(headerCache));
        }

        // 1) Supprimer les headers des groupes qui n'existent plus
        Set<Integer> currentGroups = new HashSet<>();
        for (int i = 0; i < groupBounds.length; i++) {
            currentGroups.add(i);
        }

        // itérer sur une copie pour éviter ConcurrentModification
        for (Integer idx : new HashSet<>(headerCache.keySet())) {
            if (!currentGroups.contains(idx)) {
                Component old = headerCache.remove(idx);
                if (old != null) {
                    ribbon.removeHeaderComponent(old);
                }
            }
        }

        // 2) Créer / positionner les headers restants
        final int headerAlignment = ribbon.getHeaderAlignment();
        final int headerWidth = ribbon.getHeaderWidth();

        for (int i = 0; i < groupBounds.length; i++) {
            Rectangle contentRect = groupBounds[i];
            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            if (group == null || contentRect == null) {
                continue;
            }

            Component header = headerCache.get(i);

            if (header == null) {
                // création via le renderer (defensive: renderer peut être null)
                GroupRenderer renderer = ribbon.getGroupRenderer();
                if (renderer != null) {
                    Object headerValue = ribbon.getHeaderValue(i);
                    try {
                        header = renderer.getHeaderComponent(ribbon, headerValue, i, false);
                    } catch (Throwable t) {
                        header = null; // defensive: ignore renderer failures
                    }
                    if (header != null) {
                        ribbon.addHeaderComponent(header);
                        headerCache.put(i, header);
                    }
                }
            }

            if (header != null) {
                int headerX, headerY, headerW, headerH;

                switch (headerAlignment) {
                    case Ribbon.HEADER_NORTH:
                        headerX = contentRect.x;
                        headerY = (insets != null) ? insets.top : 0;
                        headerW = contentRect.width;
                        headerH = headerHeight;
                        break;

                    case Ribbon.HEADER_SOUTH:
                        headerX = contentRect.x;
                        headerY = ribbon.getHeight() - ((insets != null) ? insets.bottom : 0) - headerHeight;
                        headerW = contentRect.width;
                        headerH = headerHeight;
                        break;

                    case Ribbon.HEADER_WEST:
                        headerX = contentRect.x - headerWidth;
                        headerY = contentRect.y;
                        headerW = headerWidth;
                        headerH = contentRect.height;
                        break;

                    case Ribbon.HEADER_EAST:
                        headerX = contentRect.x + contentRect.width;
                        headerY = contentRect.y;
                        headerW = headerWidth;
                        headerH = contentRect.height;
                        break;

                    default:
                        //En-tête masqué ou alignement inconnu -> ignorer le positionnement
                        continue;
                }

                // Bounds defensives : largeur/hauteur non négatives
                headerW = Math.max(0, headerW);
                headerH = Math.max(0, headerH);

                header.setBounds(headerX, headerY, headerW, headerH);
                header.setVisible(true);
            }
        }

        return Collections.unmodifiableMap(new HashMap<>(headerCache));
    }

    /**
     * Retire le header d'un groupe spécifique si présent.
     * @param ribbon     le Ribbon
     * @param groupIndex index du groupe
     */
    public void removeHeaderForGroup(Ribbon ribbon, int groupIndex) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HeaderManager.removeHeaderForGroup must be called on the EDT");
        }
        Component old = headerCache.remove(groupIndex);
        if (old != null && ribbon != null) {
            ribbon.removeHeaderComponent(old);
        }
    }

    /**
     * Invalide tous les headers : les retire du Ribbon et vide le cache.
     *
     * @param ribbon le Ribbon
     */
    public void invalidateAllHeaders(Ribbon ribbon) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HeaderManager.invalidateAllHeaders must be called on the EDT");
        }
        if (ribbon != null) {
            for (Component header : headerCache.values()) {
                if (header != null) {
                    ribbon.removeHeaderComponent(header);
                }
            }
        }
        headerCache.clear();
    }

    /**
     * Retourne une copie immuable du cache actuel des headers.     
     *
     * @return map index->Component (immutably wrapped)
     */
    public Map<Integer, Component> getHeaderComponents() {
        return Collections.unmodifiableMap(new HashMap<>(headerCache));
    }
    
}
