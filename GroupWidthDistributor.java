/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban.layout;

import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/**
 * Implémentation responsable de calculer la largeur allouée à chaque groupe. 
 * Principes :
 * - Respecte priorité : explicit width (>0) > preferredWidth (>0) > default
 * - Respecte contraintes minWidth / maxWidth des HRibbonGroup
 * - Prend en compte headerWidth lorsque headerAlignment est WEST/EAST
 * - Supporte mode equalDistribution
 * @author FIDELE
 */
public class GroupWidthDistributor implements IGroupWidthDistributor{
    
     private static final int DEFAULT_GROUP_WIDTH = 150;
    private static final int ABSOLUTE_MIN = 20;

    private volatile boolean equalDistribution = false;

    public GroupWidthDistributor() {
    }

    @Override
    public void setEqualDistribution(boolean equal) {
        this.equalDistribution = equal;
    }

    @Override
    public boolean isEqualDistribution() {
        return this.equalDistribution;
    }

    /**
     * Distribue les largeurs pour chaque groupe.
     *
     * @param ctx            contexte (header alignment/width, groupMargin, equalDistribution recommandé)
     * @param groupModel     le HRibbonGroupModel (peut être null -> retourne tableau vide)
     * @param availableWidth largeur disponible totale (déjà soustraite des marges du parent)
     * @return tableau d'entiers de taille groupModel.getGroupCount()
     */
    @Override
    public int[] distributeWidths(HRibbonLayoutContext ctx, HRibbonGroupModel groupModel, int availableWidth) {
        if (groupModel == null) {
            return new int[0];
        }

        final int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return new int[0];
        }

        int[] widths = new int[groupCount];
        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;
        int groupMargin = (ctx != null) ? ctx.getGroupMargin() : 0;

        // Espace disponible pour les groupes, en retirant les marges inter-groupes
        int totalMargin = groupMargin * Math.max(0, groupCount - 1);
        int widthForGroups = Math.max(availableWidth - totalMargin, 0);

        boolean useEqual = (ctx != null && ctx.isEqualDistribution()) || this.equalDistribution;

        if (useEqual) {
            int widthPerGroup = (groupCount > 0) ? (widthForGroups / groupCount) : 0;
            for (int i = 0; i < groupCount; i++) {
                widths[i] = Math.max(widthPerGroup, 0);
            }
            // Ensure minimal sensible width
            for (int i = 0; i < groupCount; i++) {
                widths[i] = Math.max(widths[i], ABSOLUTE_MIN);
            }
            return widths;
        }

        // ---- Intelligent distribution ----
        int totalRequestedWidth = 0;
        int[] requestedWidths = new int[groupCount];
        boolean[] isFixed = new boolean[groupCount]; // width explicitly set by HRibbonGroup

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                int req;
                if (group.getWidth() > 0) {
                    req = group.getWidth();
                    isFixed[i] = true;
                } else if (group.getPreferredWidth() > 0) {
                    req = group.getPreferredWidth();
                    isFixed[i] = false;
                } else {
                    req = DEFAULT_GROUP_WIDTH;
                    isFixed[i] = false;
                }

                // Pour HEADER_WEST/EAST : la largeur demandée inclut l'espace du header
                if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                    req += headerWidth;
                }

                requestedWidths[i] = Math.max(req, ABSOLUTE_MIN);
            } else {
                requestedWidths[i] = Math.max(DEFAULT_GROUP_WIDTH, ABSOLUTE_MIN);
                isFixed[i] = false;
            }

            totalRequestedWidth += requestedWidths[i];
        }

        if (totalRequestedWidth <= widthForGroups) {
            // Il y a assez de place : on donne ce qui est demandé
            System.arraycopy(requestedWidths, 0, widths, 0, groupCount);

            // Distribuer l'espace restant aux groupes non-fixes
            int remainingSpace = widthForGroups - totalRequestedWidth;
            int flexibleCount = 0;
            for (int i = 0; i < groupCount; i++) {
                if (!isFixed[i]) flexibleCount++;
            }
            if (flexibleCount > 0 && remainingSpace > 0) {
                int extraPerGroup = remainingSpace / flexibleCount;
                for (int i = 0; i < groupCount; i++) {
                    if (!isFixed[i]) {
                        widths[i] += extraPerGroup;
                    }
                }
            }
        } else {
            // Pas assez de place : réduction intelligente
            int fixedSpace = 0;
            int flexibleSpace = 0;
            for (int i = 0; i < groupCount; i++) {
                if (isFixed[i]) fixedSpace += requestedWidths[i];
                else flexibleSpace += requestedWidths[i];
            }

            int availableForFlexible = Math.max(widthForGroups - fixedSpace, 0);

            for (int i = 0; i < groupCount; i++) {
                if (isFixed[i]) {
                    widths[i] = requestedWidths[i];
                } else {
                    if (flexibleSpace > 0) {
                        float ratio = (float) requestedWidths[i] / (float) flexibleSpace;
                        widths[i] = Math.max((int) (availableForFlexible * ratio), ABSOLUTE_MIN);
                    } else {
                        widths[i] = ABSOLUTE_MIN;
                    }
                }
            }
        }

        // Appliquer contraintes min/max définies dans HRibbonGroup (en tenant compte du header pour W/E)
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group == null) {
                widths[i] = Math.max(widths[i], ABSOLUTE_MIN);
                continue;
            }

            int minW = group.getMinWidth();
            int maxW = group.getMaxWidth();

            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                if (minW > 0) minW += headerWidth;
                if (maxW > 0) maxW += headerWidth;
            }

            if (minW > 0) {
                widths[i] = Math.max(widths[i], minW);
            }
            if (maxW > 0) {
                widths[i] = Math.min(widths[i], maxW);
            }

            // Sécurité minimale
            widths[i] = Math.max(widths[i], ABSOLUTE_MIN);
        }

        return widths;
    }
    
}
