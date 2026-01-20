/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hcomponents.HRibbon.HRibbonModelEvents;

import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.*;



/**
 *
 * @author FIDELE
 */
public interface HRibbonModelEventListener {
    
    default void onGroupAdded(GroupAddedEvent e) {}
    default void onGroupRemoved(GroupRemovedEvent e) {}
    default void onGroupsReordered(GroupsReorderedEvent e) {}
    default void onRibbonCleared(RibbonClearedEvent e) {}

    // -------------------- Événements liés aux valeurs dans les groupes --------------------

    default void onValueAdded(ValueAddedEvent e) {}
    default void onValueRemoved(ValueRemovedEvent e) {}
    default void onValueMoved(ValueMovedEvent e) {}
    default void onValueReplaced(ValueReplacedEvent e) {}
    default void onGroupValuesCleared(GroupValuesClearedEvent e) {}

    // -------------------- Événement global --------------------

//    default void onRibbonStructureChanged(RibbonStructureChangedEvent e) {}
    
}
