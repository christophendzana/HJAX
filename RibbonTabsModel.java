/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package HRIbbonTabs.model;

import HRIbbonTabs.HRibbonTabs.RibbonTabsState;
import java.beans.PropertyChangeListener;

/**
 * RibbonTabsModel — état et dimensions de HRibbonTabs, sans dépendance Swing/AWT.
 * Noms de propriétés notifiés : "state", "expandedHeight", "collapsedHeight",
 * "minContentHeight", "autoCollapseEnabled".
 */
public interface RibbonTabsModel {

    // Accès/modification de l'état collapse/expand
    RibbonTabsState getState();
    void setState(RibbonTabsState state);

    // Accès/modification de la hauteur en mode EXPANDED
    int getExpandedHeight();
    void setExpandedHeight(int height);

    // Accès/modification de la hauteur en mode COLLAPSED
    int getCollapsedHeight();
    void setCollapsedHeight(int height);

    // Accès/modification du seuil d'auto-collapse
    int getMinContentHeight();
    void setMinContentHeight(int height);

    // Accès/modification de l'activation de l'auto-collapse
    boolean isAutoCollapseEnabled();
    void setAutoCollapseEnabled(boolean enabled);

    // Abonnement aux changements de propriétés
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
}