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
    private final int groupMargin;
    private final int headerMargin;
    private final int defaultGroupWidth;
    private final int AbsoluteGroupMin;


    public HRibbonLayoutContext(int headerAlignment,
                                int headerWidth,
                                int groupMargin,
                                int headerMargin,
                                int defaultGroupWidth,
                                int AbsoluteGroupMin
                                ) {
        this.headerAlignment = headerAlignment;
        this.headerWidth = Math.max(0, headerWidth);
        this.groupMargin = Math.max(0, groupMargin);
        this.headerMargin = Math.max(0, headerMargin);
        this.defaultGroupWidth = Math.max(0, defaultGroupWidth);
        this.AbsoluteGroupMin = Math.max(0, AbsoluteGroupMin);
        
    }

    public int getHeaderAlignment() { return headerAlignment; }
    public int getHeaderWidth() { return headerWidth; }
      public int getGroupMargin() { return groupMargin; }
    public int getHeaderMargin() { return headerMargin; }
    public int getDefautlGroupWidth(){ return defaultGroupWidth; }
    public int getAbsoluteGroupMin() { return AbsoluteGroupMin; }
    
    @Override
    public String toString() {
        return "HRibbonLayoutContext[headerAlignment=" + headerAlignment
                + ", headerWidth=" + headerWidth               
                + ", groupMargin=" + groupMargin
                + ", headerMargin=" + headerMargin + "]";
    }
    
}
