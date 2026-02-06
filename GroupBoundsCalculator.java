/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban.layout;

import java.awt.Insets;
import java.awt.Rectangle;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/**
 * Calcule les rectangles (bounds) de la zone "contenu" pour chaque groupe,
 * à partir des largeurs totales allouées par groupe.
 *
 * Règles :
 * - groupWidths[i] est la largeur TOTALE occupée par le groupe, éventuellement
 *   incluant l'espace réservé au header pour HEADER_WEST / HEADER_EAST.
 * - Le rectangle retourné représente la zone "contenu" (espace disponible pour
 *   les composants du groupe), i.e. exclut la zone header lorsqu'il est à gauche
 *   ou à droite. 
 * @author FIDELE
 */
public class GroupBoundsCalculator {
    
    /**
     * Calcule les bounds (zone de contenu) pour chaque groupe.
     *
     * @param ctx           contexte layout (headerAlignment, headerWidth, etc.)
     * @param groupWidths   largeurs totales par groupe (peut inclure header area)
     * @param groupModel    modèle des groupes (pour le nombre de groupes et margin)
     * @param insets        insets du parent (gauche/droite/haut/bas)
     * @param contentHeight hauteur disponible pour le contenu (déjà tenant compte des headers N/S)
     * @param headerHeight  hauteur du header (utile si HEADER_NORTH / SOUTH)
     * @return tableau de Rectangle de longueur groupModel.getGroupCount(); chaque rectangle
     *         représente la zone de contenu (x,y,width,height) pour le groupe correspondant.
     *         Retourne un tableau vide si groupModel == null ou groupCount == 0.
     */
    
    public Rectangle[] calculateGroupBounds(HRibbonLayoutContext ctx,
                                            int[] groupWidths,
                                            HRibbonGroupModel groupModel,
                                            Insets insets,
                                            int contentHeight,
                                            int headerHeight) {
        if (groupWidths == null) return new Rectangle[0];
        int groupCount = groupWidths.length;
        Rectangle[] rects = new Rectangle[groupCount];

        int left = (insets != null) ? insets.left : 0;
        int topInset = (insets != null) ? insets.top : 0;

        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerMargin = (ctx != null) ? ctx.getHeaderMargin() : 0;

        // compute available top Y such that header (N) is visible if needed.
        int contentYForNorthHeader = topInset + ((headerAlignment == Ribbon.HEADER_NORTH) ? (headerHeight + headerMargin) : 0);
        int contentYDefault = topInset;

        int x = left;
        for (int i = 0; i < groupCount; i++) {
            int w = Math.max(0, groupWidths[i]);
            int y = (headerAlignment == Ribbon.HEADER_NORTH) ? contentYForNorthHeader : contentYDefault;
            rects[i] = new Rectangle(x, y, w, contentHeight);
            // Advance X by width + group margin
            int gm = (groupModel != null) ? groupModel.getHRibbonGroupMarggin() : 0;
            x += w + Math.max(0, gm);
        }

        return rects;
    }
    
}
