package rubban;

import java.awt.*;
import java.util.ArrayList;
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
import rubban.layout.ResizeManager;
import rubban.layout.ResizeAction;
import java.util.HashMap;

/**
 * HRibbonLayoutManager
 *
 * Délègue les responsabilités à des composants spécialisés : -
 * GroupWidthDistributor - GroupBoundsCalculator - HeaderManager -
 * ComponentOrganizer - LineWrapper - PreferredSizeCalculator
 *
 * Remarques : - Ce manager conserve des caches simples (groupBoundsCache,
 * componentsByGroupCache) qui sont invalidés via invalidateLayout(...).
 */
public class HRibbonLayoutManager implements LayoutManager2 {

    private final Ribbon ribbon;

    private final GroupWidthDistributor widthDistributor = new GroupWidthDistributor();
    private final GroupBoundsCalculator boundsCalculator = new GroupBoundsCalculator();
    private final HeaderManager headerManager = new HeaderManager();
    private final ComponentOrganizer componentOrganizer = new ComponentOrganizer();
    private final LineWrapper lineWrapper = new LineWrapper();
    private final PreferredSizeCalculator preferredCalculator = new PreferredSizeCalculator();
    private final ResizeManager resizeManager = new ResizeManager();

    private Rectangle[] groupBoundsCache = null;
    private java.util.Map<Integer, java.util.List<Component>> componentsByGroupCache = null;

    public HRibbonLayoutManager(Ribbon ribbon) {
        if (ribbon == null) {
            throw new IllegalArgumentException("Ribbon cannot be null");
        }
        this.ribbon = ribbon;
    }

    @Override
public void layoutContainer(Container parent) {
    if (!SwingUtilities.isEventDispatchThread()) {
        throw new IllegalStateException("HRibbonLayoutManager.layoutContainer must be called on the EDT");
    }
    if (!(parent instanceof Ribbon)) {
        return;
    }

    Ribbon hRibbon = (Ribbon) parent;
    HRibbonModel model = hRibbon.getModel();
    HRibbonGroupModel groupModel = hRibbon.getGroupModel();
    if (model == null || groupModel == null) {
        return;
    }

    // ============ NETTOYAGE PRÉALABLE ============
    // Supprimer les combos des groupes normaux pour éviter les fantômes
    if (groupModel != null) {
        int groupCount = groupModel.getGroupCount();
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null && !group.isCollapsed()) {
                // Groupe en mode NORMAL : pas de bouton
                RibbonOverflowButton oldButton = group.getCollapsedButton();
                if (oldButton != null) {
                    if (oldButton.getParent() == hRibbon) {
                        hRibbon.removeComponentSafely(oldButton);
                    }
                    group.setCollapsedButton(null);
                }
            }
        }
    }

    groupBoundsCache = null;
    componentsByGroupCache = null;

    Insets insets = parent.getInsets();
    int availableWidth = parent.getWidth() - (insets != null ? (insets.left + insets.right) : 0);
    int availableHeight = parent.getHeight() - (insets != null ? (insets.top + insets.bottom) : 0);

    int headerAlignment = hRibbon.getHeaderAlignment();
    boolean headersVisible = headerAlignment != Ribbon.HEADER_HIDDEN;
    int headerHeight = headersVisible ? hRibbon.getHeaderHeight() : 0;
    int groupMargin = groupModel.getHRibbonGroupMarggin();

    HRibbonLayoutContext ctx = new HRibbonLayoutContext(
            headerAlignment,
            hRibbon.getHeaderWidth(),
            hRibbon.isFillsViewportHeight(),
            widthDistributor.isEqualDistribution(),
            groupMargin,
            hRibbon.getHeaderMargin()
    );

    int groupCount = groupModel.getGroupCount();
    if (groupCount == 0) {
        return;
    }

    // ============ ÉTAPE 1 : CALCULER LES LARGEURS NORMALES ============
    int[] normalGroupWidths = widthDistributor.distributeWidths(ctx, groupModel, availableWidth);

    // ============ ÉTAPE 2 : CALCULER LA LARGEUR TOTALE SI TOUT ÉTAIT NORMAL ============
    int totalNormalWidth = 0;
    for (int i = 0; i < normalGroupWidths.length; i++) {
        totalNormalWidth += normalGroupWidths[i];
        if (i < normalGroupWidths.length - 1) {
            totalNormalWidth += groupMargin;
        }
    }

    // ============ ÉTAPE 3 : CONSTRUIRE LA MAP DES LARGEURS NORMALES ============
    Map<Integer, Integer> normalWidths = new HashMap<>();
    for (int i = 0; i < normalGroupWidths.length; i++) {
        normalWidths.put(i, normalGroupWidths[i]);
    }

    // ============ ÉTAPE 4 : DÉTERMINER LES ACTIONS DE RESIZE ============
    List<ResizeAction> resizeActions = resizeManager.calculateResizeActions(
            groupModel,
            availableWidth,
            normalWidths,
            groupMargin
    );

    // ============ ÉTAPE 5 : APPLIQUER LES ACTIONS DE RESIZE ============
    for (ResizeAction action : resizeActions) {
        if (action.isCollapse()) {
            resizeManager.collapseGroup(hRibbon, action.getGroupIndex());
        } else {
            resizeManager.expandGroup(hRibbon, action.getGroupIndex());
        }
    }

    // ============ ÉTAPE 5.5 : GESTION EXCLUSIVE DES TRANSFERTS ============
    // NOUVEAU : Assurer la séparation stricte après chaque action de resize
    // ----------------------------------------------------------------------
    for (ResizeAction action : resizeActions) {
        if (action.isExpand()) {
            // CAS 1 : Groupe restauré (collapsed → normal)
            // Tous les composants DOIVENT repasser du bouton vers le Ribbon
            int groupIndex = action.getGroupIndex();
            HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
            
            if (group != null && !group.isCollapsed()) {
                RibbonOverflowButton oldButton = group.getCollapsedButton();
                
                if (oldButton != null) {
                    // Transfert exclusif : OverflowButton → Ribbon
                    hRibbon.transferComponentsFromOverflow(groupIndex, oldButton);
                    
                    // Nettoyage final du bouton
                    if (oldButton.getParent() == hRibbon) {
                        hRibbon.removeComponentSafely(oldButton);
                    }
                    group.setCollapsedButton(null);
                }
            }
        }
    }

    // ============ ÉTAPE 6 : CALCULER LES LARGEURS FINALES ============
    int[] groupWidths = new int[groupCount];
    for (int i = 0; i < groupCount; i++) {
        HRibbonGroup g = groupModel.getHRibbonGroup(i);
        if (g != null && g.isCollapsed()) {
            groupWidths[i] = g.getCollapsedWidth();
        } else {
            groupWidths[i] = normalGroupWidths[i];
        }
    }

    // ============ ÉTAPE 7 : CALCUL DE LA HAUTEUR DU RUBAN ============
    int ribbonHeight;
    Ribbon.HeightPolicy policy = hRibbon.getHeightPolicy();

    if (policy == Ribbon.HeightPolicy.FIXED) {
        ribbonHeight = Math.max(0, availableHeight);
    } else {
        ribbonHeight = preferredCalculator.computeRequiredContentHeight(
                hRibbon, ctx, normalGroupWidths, model, groupModel
        );
        ribbonHeight = Math.max(ribbonHeight, 1);
        if (hRibbon.isFillsViewportHeight()) {
            ribbonHeight = Math.max(ribbonHeight, availableHeight);
        }
    }

    // ============ ÉTAPE 8 : CALCUL DES RECTANGLES DE CONTENU ============
    int headerMargin = ctx.getHeaderMargin();
    int contentHeightPerGroup;
    if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
        contentHeightPerGroup = Math.max(0, ribbonHeight - (headersVisible ? (headerHeight + headerMargin) : 0));
    } else {
        contentHeightPerGroup = Math.max(0, ribbonHeight);
    }

    Rectangle[] groupBounds = boundsCalculator.calculateGroupBounds(
            ctx, groupWidths, groupModel, insets, contentHeightPerGroup, headerHeight
    );
    if (groupBounds == null) {
        groupBounds = new Rectangle[groupCount];
    }

    int baseTopInset = (insets != null) ? insets.top : 0;
    for (int i = 0; i < groupBounds.length; i++) {
        Rectangle r = groupBounds[i];
        if (r == null) {
            r = new Rectangle(0, baseTopInset, (i < groupWidths.length ? groupWidths[i] : 0), contentHeightPerGroup);
            groupBounds[i] = r;
        } else {
            r.height = contentHeightPerGroup;
            if (headerAlignment == Ribbon.HEADER_NORTH) {
                r.y = baseTopInset + headerHeight + headerMargin;
            } else {
                r.y = baseTopInset;
            }
        }
    }
    groupBoundsCache = groupBounds;

    // ============ ÉTAPE 9 : POSITION DES HEADERS ============
    headerManager.updateAndPositionHeaders(hRibbon, ctx, groupModel, groupBounds);

    // ============ ÉTAPE 10 : COLLECTE DES COMPOSANTS ============
    ComponentOrganizer.ComponentCollectionResult coll = componentOrganizer.collectComponents(hRibbon, model);
    componentsByGroupCache = coll.componentsByGroup;

    // ============ ÉTAPE 11 : GESTION DE LA VISIBILITÉ ============
    // SIMPLIFIÉ : Les composants sont déjà au bon endroit grâce aux transferts
    // On ne fait que gérer la visibilité et le layout positionnel
    for (Map.Entry<Integer, List<Component>> e : componentsByGroupCache.entrySet()) {
        Integer gi = e.getKey();
        List<Component> comps = e.getValue();
        HRibbonGroup group = groupModel.getHRibbonGroup(gi);

        for (Component c : comps) {
            if (c == null) {
                continue;
            }

            // S'assurer que le composant est bien dans le conteneur
            if (c.getParent() != hRibbon) {
                hRibbon.addComponentToContainer(c);
            }

            // Gestion simplifiée de la visibilité
            if (group != null) {
                boolean isOverflowBtn = (c == group.getCollapsedButton());
                
                if (group.isCollapsed()) {
                    // Groupe collapsed : seul le bouton est visible
                    c.setVisible(isOverflowBtn);
                    
                    // Les composants normaux sont invisibles et gérés par le bouton
                    if (!isOverflowBtn) {
                        c.setVisible(false);
                    }
                } else {
                    // Groupe normal : tous les composants sauf le bouton sont visibles
                    if (isOverflowBtn) {
                        c.setVisible(false);
                        // Le bouton ne doit pas rester dans le Ribbon
                        if (c.getParent() == hRibbon) {
                            hRibbon.removeComponentSafely(c);
                        }
                        group.setCollapsedButton(null);
                    } else {
                        c.setVisible(true);
                    }
                }
            }
        }
    }

    // ============ ÉTAPE 12 : LAYOUT DES COMPOSANTS DANS CHAQUE GROUPE ============
    for (int gi = 0; gi < groupBounds.length; gi++) {
        Rectangle groupRect = groupBounds[gi];
        List<Component> comps = componentsByGroupCache.get(gi);
        if (comps == null || comps.isEmpty() || groupRect == null) {
            continue;
        }

        HRibbonGroup group = groupModel.getHRibbonGroup(gi);
        if (group == null) {
            continue;
        }

        if (group.isCollapsed()) {
            // Groupe collapsed : positionner uniquement le combo
            RibbonOverflowButton btn = group.getCollapsedButton();
            if (btn != null && btn.isVisible()) {
                int btnHeight = Math.min(btn.getPreferredSize().height, groupRect.height);
                int y = groupRect.y + (groupRect.height - btnHeight) / 2;
                btn.setBounds(groupRect.x, y, group.getCollapsedWidth(), btnHeight);
            }
        } else {
            // Groupe normal : layout multi-lignes
            List<Component> visibleComps = new ArrayList<>();
            for (Component c : comps) {
                if (c.isVisible()) {
                    visibleComps.add(c);
                }
            }
            
            if (!visibleComps.isEmpty()) {
                lineWrapper.layoutComponentsInGroup(
                        visibleComps, groupRect, group.getPadding(), group.getComponentSpacing()
                );
            }
        }
    }
}

    /**
     * Construit estimatedGroupWidths basé sur preferredWidth sinon somme des
     * preferredSize composants.
     */
    private int[] buildEstimatedGroupWidthsForPreferred(Ribbon hRibbon, HRibbonGroupModel groupModel, HRibbonModel model, int groupCount) {
        int[] estimatedGroupWidths = new int[groupCount];
        int headerAlignment = hRibbon.getHeaderAlignment();
        int headerWidth = hRibbon.getHeaderWidth();
        GroupRenderer renderer = hRibbon.getGroupRenderer();

        if (!SwingUtilities.isEventDispatchThread()) {
            for (int i = 0; i < groupCount; i++) {
                HRibbonGroup g = groupModel.getHRibbonGroup(i);
                int gw = (g != null && g.getPreferredWidth() > 0) ? g.getPreferredWidth() : 100;
                if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                    gw += headerWidth;
                }
                estimatedGroupWidths[i] = Math.max(gw, 20);
            }
            return estimatedGroupWidths;
        }

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup g = groupModel.getHRibbonGroup(i);
            if (g != null && g.getPreferredWidth() > 0) {
                int gw = g.getPreferredWidth();
                if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                    gw += headerWidth;
                }
                estimatedGroupWidths[i] = Math.max(gw, 20);
                continue;
            }
            int padding = (g != null) ? Math.max(0, g.getPadding()) : 0;
            int spacing = (g != null) ? Math.max(0, g.getComponentSpacing()) : 0;
            int valueCount = model.getValueCount(i);
            int totalWidth = 0;
            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, i);
                Dimension pref = null;
                if (value instanceof Component) {
                    pref = ((Component) value).getPreferredSize();
                } else if (renderer != null) {
                    try {
                        Component mc = renderer.getGroupComponent(hRibbon, value, i, pos, false, false);
                        if (mc != null) {
                            pref = mc.getPreferredSize();
                        }
                    } catch (Throwable t) {
                        pref = null;
                    }
                }
                if (pref == null) {
                    pref = new Dimension(50, 24);
                }
                if (totalWidth > 0) {
                    totalWidth += spacing;
                }
                totalWidth += pref.width;
            }
            int gw = totalWidth + padding * 2;
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                gw += headerWidth;
            }
            estimatedGroupWidths[i] = Math.max(gw, 20);
        }
        return estimatedGroupWidths;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        /* unchanged behavior: compute preferred as ribbon total height */
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

        int totalPreferredWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalPreferredWidth += group.getPreferredWidth();
            } else {
                totalPreferredWidth += 100;
            }
            if (i < groupCount - 1) {
                totalPreferredWidth += groupMargin;
            }
        }

        HRibbonLayoutContext ctx = new HRibbonLayoutContext(
                hRibbon.getHeaderAlignment(),
                hRibbon.getHeaderWidth(),
                hRibbon.isFillsViewportHeight(),
                widthDistributor.isEqualDistribution(),
                groupModel.getHRibbonGroupMarggin(),
                hRibbon.getHeaderMargin()
        );

        int[] estimatedGroupWidths = buildEstimatedGroupWidthsForPreferred(hRibbon, groupModel, model, groupCount);

        int ribbonHeight;
        if (!SwingUtilities.isEventDispatchThread()) {
            ribbonHeight = 80;
        } else {
            ribbonHeight = preferredCalculator.computeRequiredContentHeight(hRibbon, ctx, estimatedGroupWidths, model, groupModel);
        }
        int preferredHeight = Math.max(ribbonHeight, 80);

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
            if (i < groupCount - 1) {
                totalMinWidth += groupMargin;
            }
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

    public Rectangle[] getGroupBounds() {
        return groupBoundsCache;
    }

    public Map<Integer, List<Component>> getComponentsByGroup() {
        return componentsByGroupCache;
    }

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
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
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
        groupBoundsCache = null;
        componentsByGroupCache = null;
        componentOrganizer.clearCache();
        preferredCalculator.clearCache();
    }

    /**
     * Construit une map des largeurs normales (non-collapsed) pour chaque
     * groupe.
     */
    private Map<Integer, Integer> buildNormalWidthsMap(
            Ribbon hRibbon,
            HRibbonGroupModel groupModel,
            HRibbonModel model,
            int groupCount,
            int headerAlignment) {

        Map<Integer, Integer> normalWidths = new HashMap<>();
        int headerWidth = hRibbon.getHeaderWidth();
        GroupRenderer renderer = hRibbon.getGroupRenderer();

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup g = groupModel.getHRibbonGroup(i);

            // Si le groupe a une largeur préférée explicite
            if (g != null && g.getPreferredWidth() > 0) {
                int gw = g.getPreferredWidth();

                // Ajouter l'espace pour le header si latéral
                if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                    gw += headerWidth;
                }

                normalWidths.put(i, Math.max(gw, 20));
                continue;
            }

            // Sinon, calculer la largeur selon les composants
            int padding = (g != null) ? Math.max(0, g.getPadding()) : 0;
            int spacing = (g != null) ? Math.max(0, g.getComponentSpacing()) : 0;
            int valueCount = model.getValueCount(i);
            int totalWidth = 0;

            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, i);
                Dimension pref = null;

                if (value instanceof Component) {
                    pref = ((Component) value).getPreferredSize();
                } else if (renderer != null) {
                    try {
                        Component mc = renderer.getGroupComponent(hRibbon, value, i, pos, false, false);
                        if (mc != null) {
                            pref = mc.getPreferredSize();
                        }
                    } catch (Throwable t) {
                        pref = null;
                    }
                }

                if (pref == null) {
                    pref = new Dimension(50, 24);
                }
                if (totalWidth > 0) {
                    totalWidth += spacing;
                }
                totalWidth += pref.width;
            }

            int gw = totalWidth + padding * 2;
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                gw += headerWidth;
            }

            normalWidths.put(i, Math.max(gw, 20));
        }

        return normalWidths;
    }

    public void removeHeaderForGroup(int groupIndex) {
        headerManager.removeHeaderForGroup(ribbon, groupIndex);
    }

    public void invalidateAllHeaders() {
        headerManager.invalidateAllHeaders(ribbon);
    }

    public Ribbon getRibbon() {
        return ribbon;
    }
}
