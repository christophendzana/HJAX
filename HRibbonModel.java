/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

/**
 *
 * @author FIDELE
 */
public interface HRibbonModel {

    
    /**
     * Returns the number of group in the model. A
     * <code>HRibbon</code> uses this method to determine how many groups it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of groups in the model
     * @see #getGroupCount
     */
    public int getGroupCount();
    
    /**
     * Returns the name of the group at <code>groupIndex</code>.  This is used
     * to initialize the ribbon's group header name.  Note: this name does
     * not need to be unique; two groups in a ribbon can have the same name.
     *
     * @param   groupIndex     the index of the group
     * @return  the name of the group
     */
    public String getGroupName(int groupIndex);

    /**
     * Returns the Object at this <code>position</code> in the group at this
     * <code>groupIndex</code>
     * @param position
     * @param groupIndex
     * @return 
     */
    public Object getValueAt(int position, int groupIndex);
    
    /**
     * Returns the Object at this <code>position</code> in the group have this 
     * name <code>groupIndex</code>
     * @param position
     * @param groupName
     * @return 
     */
    public Object getValueAt(int position, Object groupIdentifier);     
    
    public void setValueAt(Object value, int position, int groupIndex);
    
    public int getValueCount(Object groupIdentifier);

    public int getValueCount(int groupIndex);
    
    public void setValueAt(Object value, int position, Object groupIdentifier);
    
    public void addRibbonModelListener(HRibbonModelListener l);
    
    public void removeModelListener (HRibbonModelListener l);
}
