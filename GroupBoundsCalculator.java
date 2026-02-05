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
        if (groupModel == null) {
            return new Rectangle[0];
        }

        final int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return new Rectangle[0];
        }

        Rectangle[] bounds = new Rectangle[groupCount];

        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin(); // margin between groups

        // Position X de départ : après la marge gauche
        int currentX = (insets != null) ? insets.left : 0;

        // Position Y du contenu selon l'alignement des headers
        int contentY = (insets != null) ? insets.top : 0;
        if (headerAlignment == Ribbon.HEADER_NORTH) {
            // Le contenu commence après la zone des headers en haut
            contentY += headerHeight;
        }
        // pour SOUTH/WEST/EAST : on garde contentY = insets.top

        // Défauts de sécurité
        if (groupWidths == null) {
            groupWidths = new int[groupCount];
        }

        for (int i = 0; i < groupCount; i++) {
            int totalGroupWidth = (i < groupWidths.length) ? Math.max(0, groupWidths[i]) : 0;

            int contentX = currentX;
            int contentWidth = totalGroupWidth;

            // Si le header est à l'ouest (gauche), le contenu commence après le header
            if (headerAlignment == Ribbon.HEADER_WEST) {
                contentX = currentX + headerWidth;
                contentWidth = totalGroupWidth - headerWidth;
            } else if (headerAlignment == Ribbon.HEADER_EAST) {
                // Header à droite : contenu occupe la partie gauche du space alloué
                contentWidth = totalGroupWidth - headerWidth;
            }
            // Pour NORTH/SOUTH : contentX reste equal currentX et header est hors du content rectangle

            // Sécurité : garantir une largeur minimale pour le contenu
            contentWidth = Math.max(contentWidth, 1);

            // Construire le rectangle pour la zone de contenu
            bounds[i] = new Rectangle(
                    contentX,
                    contentY,
                    contentWidth,
                    Math.max(0, contentHeight)
            );

            // Avancer pour le groupe suivant (toujours horizontalement)
            currentX += totalGroupWidth + groupMargin;
        }

        return bounds;
    }
    
}
