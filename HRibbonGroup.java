/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.TableColumn;

/**
 *
 * @author FIDELE
 */
public class HRibbonGroup {

    private String groupName;
    
    private int modelIndex;
    
    private ArrayList<Component> components;
    
    private Object identifier;
    
    /**
     * The width of the column.
     */
    private int width;

    /**
     * The minimum width of the column.
     */
    private int minWidth;
    
    /**
     * The preferred width of the column.
     */
    private int preferredWidth;

    /**
     * The maximum width of the column.
     */
    private int maxWidth;

    private int padding = 2;
    
    /**
     * If true, the user is allowed to resize the column; the default is true.
     */
    private boolean isResizable;

    private SwingPropertyChangeSupport changeSupport;

    public HRibbonGroup() {        
        this(null, null, 0);
    }

    public HRibbonGroup(String groupName) {
        this(groupName, null, 0);
    }

    public HRibbonGroup(ArrayList<Component> objects) {
        this(null, objects, 0);
    }

    public HRibbonGroup(String groupName, ArrayList<Component> components, int modelIndex) {
        this.groupName = groupName;
        this.components = components;      
        this.modelIndex = modelIndex;        
        isResizable = true;
    }

    public String getGroupName(){
        return groupName;
    }
    
    public void setGroupName(String newGroupName){
        if (!groupName.equals(newGroupName)  ) {
            this.groupName = newGroupName;
        }
    }
    
    public int getComponentsCount() {
        return components.size();
    }

    public int getModelIndex(){
        return modelIndex;
    }
    
    public void setModelIndex(int newModelIndex){
            this.modelIndex = newModelIndex;        
    }
    
     /**
     * Sets the <code>TableColumn</code>'s identifier to    
     * @param newIdentifier an identifier for this group     
     */
   
    public void setIdentifier(Object newIdentifier) {
           this.identifier = newIdentifier;
    }

    /**
     *  Returns the <code>identifier</code> object for this group.    
     * @return 
     */
    public Object getIdentifier() {
        return identifier;
    }
    
    /**
     * Return the with of the group
     * @return 
     */
    public int getWidth(){
        return width;
    }
    
    public void setwidth(int newWidth){
        this.width = newWidth;
        
    }
    
    public int getPreferedWidth(){        
        return preferredWidth;
    }
    
    //A modifier: il faut calculer la somme de la longueur appropriée 
    // des composants alignés horizontalement
    public void setpreferredWidth(int width){
        this.preferredWidth = width;
    }
    
    public int getmaxWidth(){
        return maxWidth;
    }
    
    public void setmaxWidth(int maxWidth){
        this.maxWidth = maxWidth;
    }
    
    public int getminWidth(){
        return minWidth;
    }
    
    public void setminWidth(int minWidth){
        this.minWidth = minWidth;
    }
    
    public boolean getIsresizable(){
        return isResizable;
    }
    
    public void setIsresizable(boolean isResizable){
        this.isResizable = isResizable;
    }
    
}
