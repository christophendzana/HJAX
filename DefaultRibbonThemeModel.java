package HRIbbonTabs.model;

import HRIbbonTabs.view.HRibbonTabsTheme;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.HashMap;
import java.util.Map;

public class DefaultRibbonThemeModel implements RibbonThemeModel {

    private final EventListenerList listeners = new EventListenerList();
    private final Map<Integer, HRibbonTabsTheme> tabThemes = new HashMap<>();
    private HRibbonTabsTheme globalTheme;

    @Override
    public HRibbonTabsTheme getGlobalTheme() {
        return globalTheme;
    }

    // Change le thème global et notifie
    @Override
    public void setGlobalTheme(HRibbonTabsTheme theme) {
        this.globalTheme = theme;
        fireChanged();
    }

    @Override
    public HRibbonTabsTheme getTabTheme(int tabIndex) {
        return tabThemes.get(tabIndex);
    }

    // Fixe (ou retire, si theme == null) le thème propre d'un onglet
    @Override
    public void setTabTheme(int tabIndex, HRibbonTabsTheme theme) {
        if (theme == null) {
            tabThemes.remove(tabIndex);
        } else {
            tabThemes.put(tabIndex, theme);
        }
        fireChanged();
    }

    @Override
    public void removeTabTheme(int tabIndex) {
        tabThemes.remove(tabIndex);
        fireChanged();
    }

    // Seul point de résolution de la cascade global -> par-onglet
    @Override
    public HRibbonTabsTheme getEffectiveTheme(int tabIndex) {
        HRibbonTabsTheme own = tabThemes.get(tabIndex);
        return own != null ? own : globalTheme;
    }

    // Décale les index des thèmes suivant l'onglet supprimé
    @Override
    public void reindexAfterTabRemoval(int removedIndex) {
        Map<Integer, HRibbonTabsTheme> reindexed = new HashMap<>();
        for (Map.Entry<Integer, HRibbonTabsTheme> entry : tabThemes.entrySet()) {
            int oldIndex = entry.getKey();
            if (oldIndex == removedIndex) {
                continue; // le thème de l'onglet supprimé disparaît avec lui
            }
            reindexed.put(oldIndex > removedIndex ? oldIndex - 1 : oldIndex, entry.getValue());
        }
        tabThemes.clear();
        tabThemes.putAll(reindexed);
        fireChanged();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(ChangeListener.class, listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
    }

    // Notifie tous les ChangeListener enregistrés
    private void fireChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener l : listeners.getListeners(ChangeListener.class)) {
            l.stateChanged(event);
        }
    }
}