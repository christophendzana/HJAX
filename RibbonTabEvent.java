package HRIbbonTabs.model;

import java.util.EventObject;

/**
 * RibbonTabEvent — Eveènnement dans HRibbonTabs ajout ou suppression d'un onglet dans HRibbonTabs. 
 */
public class RibbonTabEvent extends EventObject {

    public enum Type { ADDED, REMOVED }

    private final Type type;
    private final int tabIndex;
    private final String tabTitle;

    public RibbonTabEvent(Object source, Type type, int tabIndex, String tabTitle) {
        super(source);
        this.type = type;
        this.tabIndex = tabIndex;
        this.tabTitle = tabTitle;
    }

    public Type getType() { return type; }
    public int getTabIndex() { return tabIndex; }
    public String getTabTitle() { return tabTitle; }
}