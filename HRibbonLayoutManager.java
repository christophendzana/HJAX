package rubban;

import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import rubban.layout.ComponentOrganizer;
import rubban.layout.GroupBoundsCalculator;
import rubban.layout.GroupWidthDistributor;
import rubban.layout.HRibbonLayoutContext;
import rubban.layout.HeaderManager;
import rubban.layout.LineWrapper;
import rubban.layout.PreferredSizeCalculator;

/**
 * HRibbonLayoutManager 
 *
 * Délègue les responsabilités à des composants spécialisés :
 * - GroupWidthDistributor
 * - GroupBoundsCalculator
 * - HeaderManager
 * - ComponentOrganizer
 * - LineWrapper
 * - PreferredSizeCalculator
 *
 * Remarques :
 * - Ce manager conserve des caches simples (groupBoundsCache, componentsByGroupCache)
 *   qui sont invalidés via invalidateLayout(...).
 */
public class HRibbonLayoutManager implements LayoutManager2 {

    private final Ribbon ribbon;

    // Services délégués
    private final GroupWidthDistributor widthDistributor = new GroupWidthDistributor();
    private final GroupBoundsCalculator boundsCalculator = new GroupBoundsCalculator();
    private final HeaderManager headerManager = new HeaderManager();
    private final ComponentOrganizer componentOrganizer = new ComponentOrganizer();
    private final LineWrapper lineWrapper = new LineWrapper();
    private final PreferredSizeCalculator preferredCalculator = new PreferredSizeCalculator();

    // Caches (remplis lors du dernier layout)
    private Rectangle[] groupBoundsCache = null;
    private java.util.Map<Integer, java.util.List<Component>> componentsByGroupCache = null;

    public HRibbonLayoutManager(Ribbon ribbon) {
        if (ribbon == null) {
            throw new IllegalArgumentException("Ribbon cannot be null");
        }
        this.ribbon = ribbon;
    }

    // --------------------------
    // LayoutManager2 impl
    // --------------------------

    @Override
    public void layoutContainer(Container parent) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("HRibbonLayoutManager.layoutContainer must be called on the EDT");
        }

        if (!(parent instanceof Ribbon)) return;

        Ribbon hRibbon = (Ribbon) parent;
        HRibbonModel model = hRibbon.getModel();
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        if (model == null || groupModel == null) return;

        // Invalider caches locaux avant recalcul
        groupBoundsCache = null;
        componentsByGroupCache = null;

        Insets insets = parent.getInsets();
        int availableWidth = parent.getWidth() - (insets != null ? (insets.left + insets.right) : 0);
        int availableHeight = parent.getHeight() - (insets != null ? (insets.top + insets.bottom) : 0);

        int headerAlignment = hRibbon.getHeaderAlignment();
        boolean headersVisible = headerAlignment != Ribbon.HEADER_HIDDEN;
        int headerHeight = headersVisible ? hRibbon.getHeaderHeight() : 0;

        // Construire le contexte
        HRibbonLayoutContext ctx = new HRibbonLayoutContext(
                headerAlignment,
                hRibbon.getHeaderWidth(),
                hRibbon.isFillsViewportHeight(),
                widthDistributor.isEqualDistribution(),
                groupModel.getHRibbonGroupMarggin()
        );

        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) return;

        // 1) Calcul des largeurs par groupe
        int[] groupWidths = widthDistributor.distributeWidths(ctx, groupModel, availableWidth);

        // 2) Calcul de la hauteur disponible pour le contenu (hors headers N/S)
        int contentHeight = availableHeight;
        if (headersVisible) {
            if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
                contentHeight -= headerHeight;
                if (contentHeight < 0) contentHeight = 0;
            }
        }

        // 3) Si on ne remplit pas le viewport, limiter la hauteur au nécessaire
        if (!hRibbon.isFillsViewportHeight()) {
            int requiredContentHeight = preferredCalculator.computeRequiredContentHeight(hRibbon, ctx, groupWidths, model, groupModel);
            if (requiredContentHeight > 0) {
                contentHeight = Math.min(contentHeight, requiredContentHeight);
            }
        }

        // 4) Calcul des rects de contenu pour chaque groupe
        Rectangle[] groupBounds = boundsCalculator.calculateGroupBounds(ctx, groupWidths, groupModel, insets, contentHeight, headerHeight);
        groupBoundsCache = groupBounds;

        // 5) Header creation/positioning via HeaderManager (EDT)
        headerManager.updateAndPositionHeaders(hRibbon, groupModel, groupBounds, insets, headerHeight);

        // 6) Récupération / création des components via ComponentOrganizer (ne les ajoute pas)
        ComponentOrganizer.ComponentCollectionResult coll = componentOrganizer.collectComponents(hRibbon, model);

        // 7) Ajout physique des composants au Ribbon si nécessaire et organisation par groupe
        for (Map.Entry<Integer, List<Component>> e : coll.componentsByGroup.entrySet()) {
            Integer gi = e.getKey();
            List<Component> comps = e.getValue();
            for (Component c : comps) {
                if (c != null && c.getParent() != hRibbon) {
                    // Ribbon expose addComponentToContainer(...) package-private
                    hRibbon.addComponentToContainer(c);
                }
            }
        }
        componentsByGroupCache = coll.componentsByGroup;

        // 8) Positionner composants dans chaque groupe (layout lines)
        for (int gi = 0; gi < groupBounds.length; gi++) {
            Rectangle groupRect = groupBounds[gi];
            List<Component> comps = componentsByGroupCache.get(gi);
            if (comps == null || comps.isEmpty() || groupRect == null) continue;

            HRibbonGroup group = groupModel.getHRibbonGroup(gi);
            if (group == null) continue;

            // Delegation to LineWrapper which will setBounds on components (EDT)
            lineWrapper.layoutComponentsInGroup(comps, groupRect, group.getPadding(), group.getComponentSpacing());
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        if (!(parent instanceof Ribbon)) {
            return new Dimension(0, 0);
        }

        Ribbon hRibbon = (Ribbon) parent;
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        HRibbonModel model = hRibbon.getModel();

        if (groupModel == null || model == null) {
            return new Dimension(0, 0);
        }

        Insets insets = parent.getInsets();
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return new Dimension(
                    insets != null ? (insets.left + insets.right) : 0,
                    (insets != null ? (insets.top + insets.bottom) : 0) + 80
            );
        }

        // 1) largeur totale préférée
        int totalPreferredWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalPreferredWidth += group.getPreferredWidth();
            } else {
                totalPreferredWidth += 100;
            }
            if (i < groupCount - 1) totalPreferredWidth += groupMargin;
        }

        // 2) estimer la largeur disponible = somme des preferred
        int estimatedAvailableWidth = totalPreferredWidth;
        HRibbonLayoutContext ctx = new HRibbonLayoutContext(
                hRibbon.getHeaderAlignment(),
                hRibbon.getHeaderWidth(),
                hRibbon.isFillsViewportHeight(),
                widthDistributor.isEqualDistribution(),
                groupModel.getHRibbonGroupMarggin()
        );

        int[] estimatedGroupWidths = widthDistributor.distributeWidths(ctx, groupModel, estimatedAvailableWidth);

        // 3) calculer la hauteur de contenu requise via PreferredSizeCalculator
        // NOTE: preferredCalculator.computeRequiredContentHeight exige d'être sur l'EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            // Si appelé hors EDT (rare pour preferredLayoutSize), on calcule une approximation conservatrice
            int approxHeight = 80;
            return new Dimension(totalPreferredWidth + (insets != null ? (insets.left + insets.right) : 0),
                    Math.max(approxHeight, 80) + (insets != null ? (insets.top + insets.bottom) : 0));
        }

        int contentHeight = preferredCalculator.computeRequiredContentHeight(hRibbon, ctx, estimatedGroupWidths, model, groupModel);
        int preferredHeight = Math.max(contentHeight, 80);

        return new Dimension(
                totalPreferredWidth + (insets != null ? (insets.left + insets.right) : 0),
                preferredHeight + (insets != null ? (insets.top + insets.bottom) : 0)
        );
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        if (!(parent instanceof Ribbon)) {
            return new Dimension(0, 0);
        }

        Ribbon hRibbon = (Ribbon) parent;
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        if (groupModel == null) {
            return new Dimension(0, 0);
        }

        Insets insets = parent.getInsets();
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return new Dimension(
                    insets != null ? (insets.left + insets.right) : 0,
                    (insets != null ? (insets.top + insets.bottom) : 0) + 50
            );
        }

        int totalMinWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalMinWidth += Math.max(group.getMinWidth(), 30);
            } else {
                totalMinWidth += 50;
            }
            if (i < groupCount - 1) totalMinWidth += groupMargin;
        }

        return new Dimension(
                totalMinWidth + (insets != null ? (insets.left + insets.right) : 0),
                50 + (insets != null ? (insets.top + insets.bottom) : 0)
        );
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // --------------------------
    // Utility / cache / API
    // --------------------------

    /**
     * Retourne les bounds calculés lors du dernier layout (ou null si non calculé).
     */
    public Rectangle[] getGroupBounds() {
        return groupBoundsCache;
    }

    /**
     * Retourne les components organisés par groupe lors du dernier layout
     * (ou null si non calculé).
     */
    public Map<Integer, List<Component>> getComponentsByGroup() {
        return componentsByGroupCache;
    }

    /**
     * Définit le mode de distribution égalitaire.
     */
    public void setEqualDistribution(boolean equal) {
        widthDistributor.setEqualDistribution(equal);
        if (ribbon != null) {
            ribbon.revalidate();
        }
    }

    public boolean isEqualDistribution() {
        return widthDistributor.isEqualDistribution();
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // No-op : components are managed via the model
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        // No-op
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // No-op
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // Invalidate caches and delegate to subcomponents caches when appropriate
        groupBoundsCache = null;
        componentsByGroupCache = null;
        componentOrganizer.clearCache();
        preferredCalculator.clearCache();
        // Note: headers handled explicitly by Ribbon when alignment/size changes
    }

    /**
     * Supprime le header d'un groupe spécifique du cache et du Ribbon.
     */
    public void removeHeaderForGroup(int groupIndex) {
        // Must be called on EDT
        headerManager.removeHeaderForGroup(ribbon, groupIndex);
    }

    /**
     * Invalide tous les headers (supprime du Ribbon et vide le cache).
     */
    public void invalidateAllHeaders() {
        headerManager.invalidateAllHeaders(ribbon);
    }
}