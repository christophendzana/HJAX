/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package rubban;

import javax.swing.ListSelectionModel;
import java.util.Enumeration;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FIDELE
 */
public interface HRibbonGroupModel {
    
    public void addGroup(HRibbonGroup group);
    
    public void addGroup(Object groupIdentifier); 
    
    public void removeGroup(HRibbonGroup group);
    
    public void removeGroup(int groupindex);
    
    public void removeGroup(Object groupIdentifier);
    
    public void moveGroup(int oldIndex, int newIndex);
    
    public void moveGroup(Object groupIdentifier, int newIndex);
    
    public void insertGroup(HRibbonGroup group, int index);
    
    public void insertGroup(Object groupIdentifier, int index);
    
    public void setGroupMargin(int margin);
    
    public int getGroupCount();
    
    public Enumeration<HRibbonGroup> getHRibbonGroups();
    
    public int getGroupIndex(Object groupIdentifier);
    
    public HRibbonGroup getHRibbonGroup(int position);
    
    public int getHRibbonGroupMarggin();
    
    public int getHRibbonGroupIndexAtX(int position);
    
    public int getTotalHRibbonGroupWidth();
    
    public void setHRibbonGroupSelectionAllowed(boolean flag);
    
    public boolean getHRibbonGroupSelectionAllowed();
    
    public int[] getSelectionHRibbonGroup();
    
    public int getSelectedHRibbonHRibbonCount();
    
    public void setSelectionModel(ListSelectionModel model);
    
    public ListSelectionModel getSelectionModel();
    
    public void addHRibbonGroupModelListener(HRibbonGroupListener l);
    
    public void removeHRibbonGroupModelListener(HRibbonGroupListener l);
        
    
}
