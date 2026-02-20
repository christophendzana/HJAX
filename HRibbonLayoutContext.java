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
    private final boolean equalDistribution;
    private final int groupMargin;
    private final int headerMargin;

    public HRibbonLayoutContext(int headerAlignment,
                                int headerWidth,
                                boolean equalDistribution,
                                int groupMargin,
                                int headerMargin) {
        this.headerAlignment = headerAlignment;
        this.headerWidth = Math.max(0, headerWidth);
        this.equalDistribution = equalDistribution;
        this.groupMargin = Math.max(0, groupMargin);
        this.headerMargin = Math.max(0, headerMargin);
    }

    public int getHeaderAlignment() { return headerAlignment; }
    public int getHeaderWidth() { return headerWidth; }
    public boolean isEqualDistribution() { return equalDistribution; }
    public int getGroupMargin() { return groupMargin; }
    public int getHeaderMargin() { return headerMargin; }

    @Override
    public String toString() {
        return "HRibbonLayoutContext[headerAlignment=" + headerAlignment
                + ", headerWidth=" + headerWidth
                + ", equalDistribution=" + equalDistribution
                + ", groupMargin=" + groupMargin
                + ", headerMargin=" + headerMargin + "]";
    }
    
}
