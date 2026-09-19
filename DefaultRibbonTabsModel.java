package HRIbbonTabs.model;

import HRIbbonTabs.HRibbonTabs.RibbonTabsState;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * DefaultRibbonTabsModel — implémentation standard de RibbonTabsModel. 
 * 
 */
public class DefaultRibbonTabsModel implements RibbonTabsModel {

    private static final int TAB_BAR_HEIGHT = 35;
    private static final int MIN_EXPANDED_HEIGHT = TAB_BAR_HEIGHT + 40;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private RibbonTabsState state = RibbonTabsState.EXPANDED;
    private int expandedHeight;
    private int collapsedHeight = TAB_BAR_HEIGHT;
    private int minContentHeight = 150;
    private boolean autoCollapseEnabled = false;

    // Construit le modèle avec une hauteur EXPANDED initiale, clampée au minimum
    public DefaultRibbonTabsModel(int expandedHeight) {
        this.expandedHeight = Math.max(MIN_EXPANDED_HEIGHT, expandedHeight);
    }

    // Retourne l'état courant
    @Override
    public RibbonTabsState getState() {
        return state;
    }

    // Change l'état et notifie — no-op si état identique ou null
    @Override
    public void setState(RibbonTabsState newState) {
        if (newState == null || newState == state) {
            return;
        }
        RibbonTabsState old = state;
        state = newState;
        support.firePropertyChange("state", old, newState);
    }

    // Retourne la hauteur configurée en mode EXPANDED
    @Override
    public int getExpandedHeight() {
        return expandedHeight;
    }

    // Change la hauteur EXPANDED, clampée au minimum, et notifie
    @Override
    public void setExpandedHeight(int height) {
        int clamped = Math.max(MIN_EXPANDED_HEIGHT, height);
        if (clamped == expandedHeight) {
            return;
        }
        int old = expandedHeight;
        expandedHeight = clamped;
        support.firePropertyChange("expandedHeight", old, clamped);
    }

    // Retourne la hauteur configurée en mode COLLAPSED
    @Override
    public int getCollapsedHeight() {
        return collapsedHeight;
    }

    // Change la hauteur COLLAPSED, clampée au minimum, et notifie
    @Override
    public void setCollapsedHeight(int height) {
        int clamped = Math.max(TAB_BAR_HEIGHT, height);
        if (clamped == collapsedHeight) {
            return;
        }
        int old = collapsedHeight;
        collapsedHeight = clamped;
        support.firePropertyChange("collapsedHeight", old, clamped);
    }

    // Retourne l'espace minimal réservé au contenu sous le ruban
    @Override
    public int getMinContentHeight() {
        return minContentHeight;
    }

    // Change le seuil d'auto-collapse et notifie
    @Override
    public void setMinContentHeight(int height) {
        int clamped = Math.max(0, height);
        if (clamped == minContentHeight) {
            return;
        }
        int old = minContentHeight;
        minContentHeight = clamped;
        support.firePropertyChange("minContentHeight", old, clamped);
    }

    // Indique si l'auto-collapse est actif
    @Override
    public boolean isAutoCollapseEnabled() {
        return autoCollapseEnabled;
    }

    // Active/désactive l'auto-collapse et notifie
    @Override
    public void setAutoCollapseEnabled(boolean enabled) {
        if (enabled == autoCollapseEnabled) {
            return;
        }
        boolean old = autoCollapseEnabled;
        autoCollapseEnabled = enabled;
        support.firePropertyChange("autoCollapseEnabled", old, enabled);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
