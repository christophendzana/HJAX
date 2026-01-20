/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon.HRibbonModelEvents;

import hcomponents.HRibbon.DefaultHRibbonModel;

/**
 *
 * @author FIDELE
 */
public class HRibbonModelEvent extends java.util.EventObject {

    private Object groupIdentifier;
    
    public HRibbonModelEvent(Object source) {
        super(source);
        this.groupIdentifier = groupIdentifier;
    }

    public Object getGroupIdentifier() {
        return groupIdentifier;
    }

    
    public class GroupAddedEvent extends HRibbonModelEvent {
    private int position;
    private int groupIndex;
    private DefaultHRibbonModel model;
    private Object groupIdentifier;

    public GroupAddedEvent(DefaultHRibbonModel model, Object groupIdentifier, int position) {
        super(model);
        this.position = position;
        this.model = model;
        this.groupIdentifier = groupIdentifier;
    }
    
    public GroupAddedEvent(DefaultHRibbonModel model, int groupIndex, int position) {
        super(model);
        this.position = position;
        this.groupIndex = groupIndex;        
    }

    public int getPosition() {
        return position;
    }
    
    public DefaultHRibbonModel getModel(){
        return model;
    }
    
    public int getGroupIndex(){
        return groupIndex;
    }
    
    public Object getGroupIdentifier(){
        return groupIdentifier;
    }
}

public class GroupRemovedEvent extends HRibbonModelEvent {
    private int positionBeforeRemoval;  
    private Object groupIdentifier;
    private int groupIndex;
    
    public GroupRemovedEvent(DefaultHRibbonModel model, Object groupIdentifier, int positionBeforeRemoval) {
        super(model);
        this.positionBeforeRemoval = positionBeforeRemoval;
    }
    
    public GroupRemovedEvent(DefaultHRibbonModel model, int groupIndex, int positionBeforeRemoval) {
        super(model);
        this.positionBeforeRemoval = positionBeforeRemoval;
    }

    public int getPositionBeforeRemoval() {
        return positionBeforeRemoval;
    }
    
    public Object groupIdentifier(){
        return groupIdentifier;
    }
    
    public int getGroupIndex(){
        return groupIndex;
    }
}

public class GroupsReorderedEvent extends HRibbonModelEvent {
    private final int[] oldOrder;
    private final int[] newOrder;

    public GroupsReorderedEvent(DefaultHRibbonModel model ,int[] oldOrder, int[] newOrder) {
        super(model);
        this.oldOrder = oldOrder;
        this.newOrder = newOrder;
    }

    public int[] getOldOrder() { return oldOrder; }
    public int[] getNewOrder() { return newOrder; }
}

public class RibbonClearedEvent extends HRibbonModelEvent {
    public RibbonClearedEvent(DefaultHRibbonModel model) { super(model); }
}
    
    public class ValueAddedEvent extends HRibbonModelEvent {
    private final Object value;
    private final int position;

    public ValueAddedEvent(DefaultHRibbonModel model, Object groupIdentifier, Object value, int position) {
        super(model);
        this.value = value;
        this.position = position;
    }

    public Object getValue() { return value; }
    public int getPosition() { return position; }
}

public class ValueRemovedEvent extends HRibbonModelEvent {
    private final Object value;
    private final int positionBeforeRemoval;

    public ValueRemovedEvent(DefaultHRibbonModel model ,Object groupIdentifier, Object value, int positionBeforeRemoval) {
        super(model);
        this.value = value;
        this.positionBeforeRemoval = positionBeforeRemoval;
    }

    public Object getValue() { return value; }
    public int getPositionBeforeRemoval() { return positionBeforeRemoval; }
}

public class ValueMovedEvent extends HRibbonModelEvent {
    private final Object value;
    private final int oldPosition;
    private final int newPosition;

    public ValueMovedEvent(DefaultHRibbonModel model, Object groupIdentifier, Object value, int oldPosition, int newPosition) {
        super(model);
        this.value = value;
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
    }

    public Object getValue() { return value; }
    public int getOldPosition() { return oldPosition; }
    public int getNewPosition() { return newPosition; }
}

public class ValueReplacedEvent extends HRibbonModelEvent {
    private final Object oldValue;
    private final Object newValue;
    private final int position;

    public ValueReplacedEvent(DefaultHRibbonModel model, Object groupIdentifier, Object oldValue, Object newValue, int position) {
        super(model);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.position = position;
    }

    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
    public int getPosition() { return position; }
}

public class GroupValuesClearedEvent extends HRibbonModelEvent {
    private final int clearedCount;

    public GroupValuesClearedEvent(DefaultHRibbonModel model, Object groupIdentifier, int clearedCount) {
        super(model);
        this.clearedCount = clearedCount;
    }

    public int getClearedCount() { return clearedCount; }
}
}
