/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban;

/**
 *
 * @author FIDELE
 */
public class HRibbonGroupEvent extends java.util.EventObject {
    
    /** The index of the column from where it was moved or removed */
    protected int       fromIndex;

    /** The index of the column to where it was moved or added */
    protected int       toIndex;
    
    /**
     * Constructs a {@code TableColumnModelEvent} object.
     *
     * @param source  the {@code TableColumnModel} that originated the event
     * @param from    an int specifying the index from where the column was
     *                moved or removed
     * @param to      an int specifying the index to where the column was
     *                moved or added
     * @see #getFromIndex
     * @see #getToIndex
     */
    public HRibbonGroupEvent(Object source, int from, int to) {
        super(source);
        this.fromIndex = from;
        this.toIndex = to;
        
    }    

    /**
     * Returns the fromIndex.  Valid for removed or moved events
     *
     * @return int value for index from which the column was moved or removed
     */
    public int getFromIndex() { return fromIndex; }

    /**
     * Returns the toIndex.  Valid for add and moved events
     *
     * @return int value of column's new index
     */
    public int getToIndex() { return toIndex; }
   
}
