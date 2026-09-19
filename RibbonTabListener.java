package HRIbbonTabs.model;

import java.util.EventListener;

/** Point d'extension : réagir à l'ajout/suppression d'un onglet dans HRibbonTabs. */
public interface RibbonTabListener extends EventListener {
    void tabChanged(RibbonTabEvent e);
}