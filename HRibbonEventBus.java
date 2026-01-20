/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon.HRibbonModelEvents;

import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.GroupAddedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.GroupRemovedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.GroupValuesClearedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.GroupsReorderedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.RibbonClearedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.ValueAddedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.ValueMovedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.ValueRemovedEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent.ValueReplacedEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class HRibbonEventBus {
    // Liste des listeners globaux
    private final List<HRibbonModelEventListener> listeners = new ArrayList<>();

    // -------------------- Gestion des listeners --------------------

    public void addListener(HRibbonModelEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(HRibbonModelEventListener listener) {
        listeners.remove(listener);
    }

    // -------------------- Méthodes de propagation des événements --------------------

    public void fireGroupAdded(GroupAddedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onGroupAdded(e);
    }

    public void fireGroupRemoved(GroupRemovedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onGroupRemoved(e);
    }

    public void fireGroupsReordered(GroupsReorderedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onGroupsReordered(e);
    }

    public void fireRibbonCleared(RibbonClearedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onRibbonCleared(e);
    }

    public void fireValueAdded(ValueAddedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onValueAdded(e);
    }

    public void fireValueRemoved(ValueRemovedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onValueRemoved(e);
    }

    public void fireValueMoved(ValueMovedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onValueMoved(e);
    }

    public void fireValueReplaced(ValueReplacedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onValueReplaced(e);
    }

    public void fireGroupValuesCleared(GroupValuesClearedEvent e) {
        for (HRibbonModelEventListener l : listeners) l.onGroupValuesCleared(e);
    }

//    public void fireRibbonStructureChanged(RibbonStructureChangedEvent e) {
//        for (HRibbonModelEventListener l : listeners) l.onRibbonStructureChanged(e);
//    }
}
