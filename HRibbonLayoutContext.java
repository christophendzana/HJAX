/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban.layout;

/**
 * Contient uniquement les informations utilisées par GroupWidthDistributor.
 * @author FIDELE
 */
public final class HRibbonLayoutContext {
    
    private final int headerAlignment;
    private final int headerWidth;
    private final boolean fillsViewportHeight;
    private final boolean equalDistribution;
    private final int groupMargin;

    public HRibbonLayoutContext(int headerAlignment,
                                int headerWidth,
                                boolean fillsViewportHeight,
                                boolean equalDistribution,
                                int groupMargin) {
        this.headerAlignment = headerAlignment;
        this.headerWidth = Math.max(0, headerWidth);
        this.fillsViewportHeight = fillsViewportHeight;
        this.equalDistribution = equalDistribution;
        this.groupMargin = Math.max(0, groupMargin);
    }

    public int getHeaderAlignment() {
        return headerAlignment;
    }

    public int getHeaderWidth() {
        return headerWidth;
    }

    public boolean isFillsViewportHeight() {
        return fillsViewportHeight;
    }

    public boolean isEqualDistribution() {
        return equalDistribution;
    }

    public int getGroupMargin() {
        return groupMargin;
    }

    @Override
    public String toString() {
        return "HRibbonLayoutContext[headerAlignment=" + headerAlignment
                + ", headerWidth=" + headerWidth
                + ", fillsViewportHeight=" + fillsViewportHeight
                + ", equalDistribution=" + equalDistribution
                + ", groupMargin=" + groupMargin + "]";
    }
    
}
