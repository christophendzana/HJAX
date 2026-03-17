package rubban;

import hcomponents.HScrollBar;
import hcomponents.vues.HScrollBarStyle;
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
 * Ce manager conserve des caches simples (groupBoundsCache,
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
    private Map<Integer, List<Component>> componentsByGroupCache = null;

    //Element pour le scroll Horizontale
    // Scrollbar horizontale pour le défilement des groupes collapsed
    private HScrollBar horizontalScrollBar = null;

    // Offset de défilement en pixels, appliqué à la position X de chaque groupe
    private int scrollOffset = 0;

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
        //Si le contenaire n'est pas un ruban
        if (!(parent instanceof Ribbon)) {
            return;
        }

        Ribbon hRibbon = (Ribbon) parent;
        Insets insets = parent.getInsets();

//         Positionnement tu boutton collapse du Ruban
//        int collapseBtnWidth = 60; // Largeur arbitraire, à ajuster
//        int collapseBtnHeight = 30;
//
//        int posX = parent.getWidth() - collapseBtnWidth - (insets != null ? insets.right : 0);
//        int posY = parent.getHeight() - collapseBtnHeight - (insets != null ? insets.bottom : 0);
//
//        Component collapseBtn = hRibbon.getCollapseButton();
//
//        if (hRibbon.getRibbonState() == Ribbon.RibbonState.VERTIVAL_COLLAPSED) {
//            // Mode VERTIVAL_COLLAPSED : bouton en haut à droite du ruban réduit
//            if (collapseBtn != null) {
//                if (collapseBtn.getParent() != hRibbon) {
//                    hRibbon.addComponentToContainer(collapseBtn);
//                }
//                collapseBtn.setBounds(posX, posY, collapseBtnWidth, collapseBtnHeight);
//            }
//        } else { // Mode EXPANDED : bouton en bas à droite du ruban complet
//
//            posY = parent.getHeight() - collapseBtnHeight - (insets != null ? insets.bottom : 0);
//
//            if (collapseBtn != null) {
//                if (collapseBtn.getParent() != hRibbon) {
//                    hRibbon.addComponentToContainer(collapseBtn);
//                }
//                collapseBtn.setBounds(posX, posY, collapseBtnWidth, collapseBtnHeight);
//            }
//        }

        HRibbonModel model = hRibbon.getModel();
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        if (model == null || groupModel == null) {
            return;
        }

        groupBoundsCache = null;
        componentsByGroupCache = null;

        int availableWidth = parent.getWidth() - (insets != null ? (insets.left + insets.right) : 5);

        int headerAlignment = hRibbon.getHeaderAlignment();
        boolean headersVisible = headerAlignment != Ribbon.HEADER_HIDDEN;
        int headerHeight = headersVisible ? hRibbon.getHeaderHeight() : 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        int defaultGroupWidth = hRibbon.getDefaultGroupWidth();
        int absoluteGroupMin = hRibbon.getDefaultAbsoluteGroupWidth();
        boolean useEntireWidth = hRibbon.getUseEntireWidth();

        HRibbonLayoutContext ctx = new HRibbonLayoutContext(
                headerAlignment,
                hRibbon.getHeaderWidth(),
                groupMargin,
                hRibbon.getHeaderMargin(),
                defaultGroupWidth,
                absoluteGroupMin
        );

        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return;
        }

        // ============ ÉTAPE 1 : CALCULER LES LARGEURS NORMALES ============
        //Calcul des tailles préférées avant la distribution
        if (useEntireWidth) {
            // Mode étendu : on calcule les preferredWidths proportionnellement
            // à availableWidth avant de distribuer
            preferredCalculator.computeAndAssignPreferredWidths(
                    ribbon, model, groupModel, groupCount, availableWidth, ctx
            );
        }

        int[] normalGroupWidths = widthDistributor.distributeWidths(ctx, hRibbon, availableWidth, useEntireWidth);

        Map<Integer, Integer> normalWidths = new HashMap<>();
        for (int i = 0; i < normalGroupWidths.length; i++) {
            normalWidths.put(i, normalGroupWidths[i]);
        }

        // ============ ÉTAPE 2 : DÉTERMINER LES ACTIONS DE RESIZE ============
        List<ResizeAction> resizeActions = resizeManager.calculateResizeActions(
                groupModel,
                availableWidth,
                normalWidths,
                groupMargin
        );

        // ============ ÉTAPE 3 : APPLIQUER LES ACTIONS DE RESIZE ============
        for (ResizeAction action : resizeActions) {
            if (action.isCollapse()) {
                resizeManager.collapseGroup(hRibbon, action.getGroupIndex());
            } else {
                resizeManager.expandGroup(hRibbon, action.getGroupIndex());
            }
        }

        // ============ ÉTAPE 4 : CALCULER LES LARGEURS FINALES ============
        int[] groupWidths = new int[groupCount];
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup g = groupModel.getHRibbonGroup(i);
            if (g != null && g.isCollapsed()) {
                // Largeur collapsed simple (sans ajout de headerWidth)
                groupWidths[i] = g.getCollapsedWidth();
            } else {
                groupWidths[i] = normalGroupWidths[i];
            }
        }

        // ============ ÉTAPE 5 : CALCUL DE LA HAUTEUR DU RUBAN ============
        int ribbonHeight = preferredCalculator.computeRequiredContentHeight(
                hRibbon, ctx, normalGroupWidths, model, groupModel
        );

        // ============ ÉTAPE 6 : CALCUL DES RECTANGLES DE CONTENU ============
        int headerMargin = ctx.getHeaderMargin();
        int contentHeightPerGroup;

        if (headerAlignment == Ribbon.HEADER_NORTH
                || headerAlignment == Ribbon.HEADER_SOUTH) {
            // Les headers prennent de la hauteur : on la déduit de l'espace disponible
            int headerSpace = headersVisible ? (headerHeight + headerMargin) : 0;
            contentHeightPerGroup = Math.max(0, ribbonHeight - headerSpace);
        } else {
            // Les headers ne prennent pas de hauteur : espace complet disponible
            contentHeightPerGroup = Math.max(0, ribbonHeight);
        }

        int baseTopInset = (insets != null) ? insets.top : 0;

        int contentY = (headerAlignment == Ribbon.HEADER_NORTH)
                ? baseTopInset + headerHeight + headerMargin // Sous l'en-tête
                : baseTopInset;

        Rectangle[] groupBounds = boundsCalculator.calculateGroupBounds(
                ctx,
                groupWidths,
                groupModel,
                insets,
                contentHeightPerGroup,
                contentY
        );
        if (groupBounds == null) {
            groupBounds = new Rectangle[groupCount];
        }

        for (int i = 0; i < groupBounds.length; i++) {
            Rectangle r = groupBounds[i];

            if (r == null) {
                int width = (i < groupWidths.length) ? groupWidths[i] : 0;
                groupBounds[i] = new Rectangle(0, contentY, width, contentHeightPerGroup);

            } else {
                r.height = contentHeightPerGroup;
                r.y = contentY;
            }
        }
        groupBoundsCache = groupBounds;

        // =============ÉTAPE 6b : GESTION DU SCROLL HORIZONTAL=================
        // Si tous les groupes sont collapsed et que la largeur totale
        // dépasse availableWidth, on active la scrollbar horizontale.
        // La scrollbar occupe la zone libérée par les headers.
        //
        // Paramètres de la scrollbar :
        //   minimum      = 0 (début du défilement)
        //   maximum      = largeurTotaleCollapsed (fin du défilement)
        //   visibleAmount = availableWidth (portion visible)
        //   value        = scrollOffset (position actuelle)
        if (shouldEnableHorizontalScroll(groupModel, availableWidth)) {

            // Créer la scrollbar si elle n'existe pas encore
            ensureScrollBarExists(ribbon);

            // Calculer la largeur totale de tous les groupes collapsed
            int totalCollapsedWidth = 0;
            for (int i = 0; i < groupCount; i++) {
                HRibbonGroup group = groupModel.getHRibbonGroup(i);
                totalCollapsedWidth += group.getCollapsedWidth();
                if (i < groupCount - 1) {
                    totalCollapsedWidth += groupMargin;
                }
            }

            // Configurer les paramètres de la scrollbar
            horizontalScrollBar.setMinimum(0);
            horizontalScrollBar.setMaximum(totalCollapsedWidth);
            horizontalScrollBar.setVisibleAmount(availableWidth);
            horizontalScrollBar.setValue(scrollOffset);

            // Positionner la scrollbar dans la zone des headers
            int insetLeft = (insets != null) ? insets.left : 0;
            int scrollBarY = contentY + contentHeightPerGroup + headerMargin;
            int scrollBarH = headerHeight;
            int scrollBarW = availableWidth;

            horizontalScrollBar.setBounds(insetLeft, scrollBarY, scrollBarW, scrollBarH);
            horizontalScrollBar.setVisible(true);

        } else {
            // Pas de scroll nécessaire : masquer la scrollbar et réinitialiser l'offset
            if (horizontalScrollBar != null) {
                horizontalScrollBar.setVisible(false);
            }
            scrollOffset = 0;
        }

        // ============ ÉTAPE 7 : POSITION DES HEADERS ============
        headerManager.updateAndPositionHeaders(hRibbon, ctx, groupModel, groupBounds);

        // ============ ÉTAPE 8 : COLLECTE DES COMPOSANTS ============
        ComponentOrganizer.ComponentCollectionResult coll = componentOrganizer.collectComponents(hRibbon, model);

        // ============ ÉTAPE 9 : GESTION DE LA VISIBILITÉ ET AJOUT AU CONTENEUR ============
        for (Map.Entry<Integer, List<Component>> e : coll.componentsByGroup.entrySet()) {
            Integer gi = e.getKey();
            List<Component> comps = e.getValue();
            HRibbonGroup group = groupModel.getHRibbonGroup(gi);

            for (Component c : comps) {
                if (c == null) {
                    continue;
                }

                // Ajouter au conteneur si pas déjà présent
                if (c.getParent() != hRibbon) {
                    hRibbon.addComponentToContainer(c);

//                    System.out.println( "group " + group.getModelIndex() + " :Comp" + "Dim = " + c.getPreferredSize());
                }

                //Gestion stricte de la visibilité du OverflowButton
                if (group != null) {
                    boolean isOverflowBtn = (c == group.getCollapsedButton());

                    if (group.isCollapsed()) {
                        // Groupe collapsed
                        if (isOverflowBtn) {
                            c.setVisible(true);  // Seul le OverflowButton est visible
                        } else {
                            c.setVisible(false); // Composants normaux invisibles
                        }
                    } else {
                        // Groupe normal
                        if (isOverflowBtn) {
                            // OverflowButton ne doit jamais être visible en mode normal
                            c.setVisible(false);
                            // Supprimer du conteneur pour éviter 
                            if (c.getParent() == hRibbon) {
                                hRibbon.removeComponentSafely(c);
                            }
                            // Invalider le cache
                            group.setCollapsedButton(null);
                        } else {
                            c.setVisible(true);  // Composants normaux visibles
                        }
                    }
                }
            }
        }

        componentsByGroupCache = coll.componentsByGroup;

        // ============ ÉTAPE 10 : ALIGNEMENT DES COMPOSANT DANS SON GROUPE 
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
                // Groupe collapsed : positionner uniquement le OverflowButton
                RibbonOverflowButton btn = group.getCollapsedButton();
                if (btn != null && btn.isVisible()) {
                    // Centrer le OverflowButton verticalement dans le groupRect
                    int btnHeight = Math.min(btn.getPreferredSize().height, groupRect.height);
                    int y = groupRect.y + (groupRect.height - btnHeight) / 2;

                    // Appliquer scrollOffset à X :
                    // - scrollOffset = 0 → comportement identique à avant
                    // - scrollOffset > 0 → les groupes défilent vers la gauche
                    int x = groupRect.x - scrollOffset;
                    btn.setBounds(x, y, group.getCollapsedWidth(), btnHeight);

                }
            } else {
                // Groupe normal : layout multi-lignes normal              
                lineWrapper.layoutComponentsInGroup(
                        comps, groupRect, group.getPadding(), group.getComponentMargin()
                );
            }
        }
    }

    // Getter public pour que BasicHRibbonUI puisse lire le scrollOffset
    public int getScrollOffset() {
        return scrollOffset;
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
                groupModel.getHRibbonGroupMarggin(),
                hRibbon.getHeaderMargin(),
                hRibbon.getDefaultGroupWidth(),
                hRibbon.getDefaultAbsoluteGroupWidth()
        );

        int[] estimatedGroupWidths = preferredCalculator.estimateGroupWidths(ribbon, groupModel, model, ctx);

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
     * Détermine si le scroll horizontal doit être activé.
     *
     * Conditions : 1. Tous les groupes sont collapsed 2. La largeur totale des
     * groupes collapsed > availableWidth
     *
     * @return true si le scroll doit être activé
     */
    private boolean shouldEnableHorizontalScroll(HRibbonGroupModel groupModel,
            int availableWidth) {
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return false;
        }

        // Vérifier que TOUS les groupes sont collapsed
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group == null || !group.isCollapsed()) {
                return false; // Au moins un groupe n'est pas collapsed
            }
        }

        // Calculer la largeur totale nécessaire
        int totalCollapsedWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            totalCollapsedWidth += group.getCollapsedWidth();
            if (i < groupCount - 1) {
                totalCollapsedWidth += groupMargin;
            }
        }

        return totalCollapsedWidth > availableWidth;
    }

    /**
     * Crée et configure la scrollbar horizontale si elle n'existe pas encore.
     * L'ajoute physiquement au Ribbon via addComponentToContainer().
     */
    private void ensureScrollBarExists(Ribbon ribbon) {
        if (horizontalScrollBar == null) {
            horizontalScrollBar = new HScrollBar(JScrollBar.HORIZONTAL);
            horizontalScrollBar.setScrollStyle(HScrollBarStyle.PRIMARY);

            // Écouter les changements de valeur pour mettre à jour scrollOffset
            horizontalScrollBar.addAdjustmentListener(e -> {
                scrollOffset = e.getValue();
                ribbon.revalidate();
                ribbon.repaint(); // Redessiner avec le nouvel offset
            });

            ribbon.addComponentToContainer(horizontalScrollBar);
        }
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
