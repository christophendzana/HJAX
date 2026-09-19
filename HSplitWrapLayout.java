package hsplitpane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;


public class HSplitWrapLayout implements LayoutManager2 {

    private WrapDirection direction;

    private int hGap;

    private int vGap;

    private boolean stretch = false;

    public HSplitWrapLayout() {
        this(WrapDirection.HORIZONTAL, 0, 0);
    }

    public HSplitWrapLayout(WrapDirection direction) {
        this(direction, 0, 0);
    }

    public HSplitWrapLayout(WrapDirection direction, int hGap, int vGap) {
        this.direction = direction;
        this.hGap = hGap;
        this.vGap = vGap;
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {

            Insets insets = parent.getInsets();
            int availableW = parent.getWidth() - insets.left - insets.right;
            int availableH = parent.getHeight() - insets.top - insets.bottom;
            int startX = insets.left;
            int startY = insets.top;

            List<Component> visibles = getVisibleComponents(parent);

            if (visibles.isEmpty()) {
                return;
            }

            if (direction == WrapDirection.HORIZONTAL) {
                layoutHorizontal(visibles, startX, startY, availableW, availableH);
            } else {
                layoutVertical(visibles, startX, startY, availableW, availableH);
            }
        }
    }

    public boolean isEtirer() {
        return stretch;
    }

    public void setEtirer(boolean stretch) {
        this.stretch = stretch;
    }

    private void layoutHorizontal(List<Component> components,
            int startX, int startY,
            int availableW, int availableH) {

        List<List<Component>> rows = wrapComponents(components, WrapDirection.HORIZONTAL, availableW, availableH);

        int y = startY;

        for (List<Component> ligne : rows) {

            int hauteurLigne = stretch
                    ? distributeCrossPerGroup(rows.size(), availableH, WrapDirection.HORIZONTAL)
                    : maxCross(ligne, WrapDirection.HORIZONTAL);

            int[] largeurs = stretch ? distributeMain(ligne, availableW, WrapDirection.HORIZONTAL) : null;

            int x = startX;
            int i = 0;

            for (Component c : ligne) {
                int w = stretch ? largeurs[i] : c.getPreferredSize().width;
                int h = hauteurLigne;
                c.setBounds(x, y, w, h);
                x += w + hGap;
                i++;
            }

            y += hauteurLigne + vGap;
        }
    }

    private void layoutVertical(List<Component> components,
            int startX, int startY,
            int availableW, int availableH) {

        List<List<Component>> colonnes = wrapComponents(components, WrapDirection.VERTICAL, availableH, availableW);

        int x = startX;

        for (List<Component> colonne : colonnes) {

            int largeurColonne = stretch
                    ? distributeCrossPerGroup(colonnes.size(), availableW, WrapDirection.VERTICAL)
                    : maxCross(colonne, WrapDirection.VERTICAL);

            int[] hauteurs = stretch ? distributeMain(colonne, availableH, WrapDirection.VERTICAL) : null;

            int y = startY;
            int i = 0;

            for (Component c : colonne) {
                int w = stretch ? largeurColonne : c.getPreferredSize().width;
                int h = stretch ? hauteurs[i] : c.getPreferredSize().height;
                c.setBounds(x, y, w, h);
                y += h + vGap;
                i++;
            }

            x += largeurColonne + hGap;
        }
    }

    /**
     * Regroupe les composants en "lignes" (axe HORIZONTAL) ou "colonnes" (axe
     * VERTICAL) selon l'espace disponible. Remplace wrapToRows + wrapToColumns.
     */
    private List<List<Component>> wrapComponents(List<Component> components, WrapDirection axis,
            int mainAvailable, int crossAvailable) {

        List<List<Component>> groups = new ArrayList<>();
        List<Component> current = new ArrayList<>();
        int mainGap = mainGap(axis);
        int crossGap = crossGap(axis);
        int mainUsed = 0;
        int crossUsed = 0;

        for (Component c : components) {
            int mainComp = axis.mainSize(c.getPreferredSize());
            int crossComp = axis.crossSize(c.getPreferredSize());

            boolean fitsInGroup = current.isEmpty() || mainUsed + mainGap + mainComp <= mainAvailable;

            if (!fitsInGroup) {
                int crossOfCurrent = maxCross(current, axis);
                boolean nouveauGroupePossible = crossUsed + crossOfCurrent + crossGap + crossComp <= crossAvailable;

                if (nouveauGroupePossible) {
                    groups.add(current);
                    crossUsed += crossOfCurrent + crossGap;
                    current = new ArrayList<>();
                    mainUsed = 0;
                }
            }

            current.add(c);
            mainUsed += (current.size() > 1 ? mainGap : 0) + mainComp;
        }

        if (!current.isEmpty()) {
            groups.add(current);
        }

        return groups;
    }

    /** Répartit l'espace "principal" disponible entre les composants d'un même groupe. */
    private int[] distributeMain(List<Component> group, int mainAvailable, WrapDirection axis) {
        int n = group.size();
        int gapsTotal = (n - 1) * mainGap(axis);
        int espace = Math.max(0, mainAvailable - gapsTotal);
        int base = espace / n;
        int reste = espace % n;

        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = base + (i < reste ? 1 : 0);
        }
        return result;
    }

    /** Répartit l'espace "transverse" disponible entre tous les groupes (mode étiré). */
    private int distributeCrossPerGroup(int nbGroupes, int crossAvailable, WrapDirection axis) {
        int gapsTotal = (nbGroupes - 1) * crossGap(axis);
        int espace = Math.max(0, crossAvailable - gapsTotal);
        return nbGroupes > 0 ? espace / nbGroupes : 0;
    }

    private int maxCross(List<Component> group, WrapDirection axis) {
        int max = 0;
        for (Component c : group) {
            max = Math.max(max, axis.crossSize(c.getPreferredSize()));
        }
        return max;
    }

    private int totalMain(List<Component> group, WrapDirection axis) {
        int total = 0;
        for (Component c : group) {
            total += axis.mainSize(c.getPreferredSize());
        }
        total += (group.size() - 1) * mainGap(axis);
        return total;
    }

    private int mainGap(WrapDirection axis) {
        return axis == WrapDirection.HORIZONTAL ? hGap : vGap;
    }

    private int crossGap(WrapDirection axis) {
        return axis == WrapDirection.HORIZONTAL ? vGap : hGap;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {

            Insets insets = parent.getInsets();

            int refW = parent.getWidth() - insets.left - insets.right;
            if (refW <= 0 && parent.getParent() != null) {
                refW = parent.getParent().getWidth() - insets.left - insets.right;
            }
            if (refW <= 0) {
                refW = Integer.MAX_VALUE;
            }

            int refH = parent.getHeight() - insets.top - insets.bottom;
            if (refH <= 0 && parent.getParent() != null) {
                refH = parent.getParent().getHeight() - insets.top - insets.bottom;
            }
            if (refH <= 0) {
                refH = Integer.MAX_VALUE;
            }

            List<Component> visibles = getVisibleComponents(parent);

            if (visibles.isEmpty()) {
                return new Dimension(
                        insets.left + insets.right,
                        insets.top + insets.bottom
                );
            }

            int totalW = 0;
            int totalH = insets.top + insets.bottom;

            if (direction == WrapDirection.HORIZONTAL) {
                List<List<Component>> rows = wrapComponents(visibles, WrapDirection.HORIZONTAL, refW, refH);

                for (List<Component> ligne : rows) {
                    totalH += maxCross(ligne, WrapDirection.HORIZONTAL) + vGap;
                    int largeurLigne = totalMain(ligne, WrapDirection.HORIZONTAL);
                    totalW = Math.max(totalW, largeurLigne + insets.left + insets.right);
                }
                if (!rows.isEmpty()) {
                    totalH -= vGap;
                }

            } else {
                List<List<Component>> colonnes = wrapComponents(visibles, WrapDirection.VERTICAL, refH, refW);

                for (List<Component> colonne : colonnes) {
                    totalW += maxCross(colonne, WrapDirection.VERTICAL) + hGap;
                    int hauteurColonne = totalMain(colonne, WrapDirection.VERTICAL);
                    totalH = Math.max(totalH, hauteurColonne + insets.top + insets.bottom);
                }
                if (!colonnes.isEmpty()) {
                    totalW -= hGap;
                }
            }

            return new Dimension(Math.max(totalW, insets.left + insets.right), totalH);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    private List<Component> getVisibleComponents(Container parent) {
        List<Component> visibles = new ArrayList<>();
        for (Component c : parent.getComponents()) {
            if (c.isVisible()) {
                visibles.add(c);
            }
        }
        return visibles;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
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
    }

    public WrapDirection getDirection() {
        return direction;
    }

    public void setDirection(WrapDirection direction) {
        this.direction = direction;
    }

    public int getHGap() {
        return hGap;
    }

    public void setHGap(int hGap) {
        this.hGap = hGap;
    }

    public int getVGap() {
        return vGap;
    }

    public void setVGap(int vGap) {
        this.vGap = vGap;
    }
}
