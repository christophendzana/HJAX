package HRIbbonTabs.model;

import HRIbbonTabs.view.HRibbonTabsTheme;
import javax.swing.event.ChangeListener;

/**
 * RibbonThemeModel — cascade de thèmes (global -> par onglet) pour HRibbonTabs.
 * getEffectiveTheme() est le SEUL endroit où la cascade est évaluée.
 */
public interface RibbonThemeModel {

    // Accès/modification du thème global
    HRibbonTabsTheme getGlobalTheme();
    void setGlobalTheme(HRibbonTabsTheme theme);

    // Accès/modification du thème propre d'un onglet (prioritaire sur le global)
    HRibbonTabsTheme getTabTheme(int tabIndex);
    void setTabTheme(int tabIndex, HRibbonTabsTheme theme);
    void removeTabTheme(int tabIndex);

    // Thème propre à l'onglet s'il existe, sinon le thème global, sinon null
    HRibbonTabsTheme getEffectiveTheme(int tabIndex);

    // Réindexe les thèmes après suppression d'un onglet
    void reindexAfterTabRemoval(int removedIndex);

    void addChangeListener(ChangeListener listener);
    void removeChangeListener(ChangeListener listener);
}