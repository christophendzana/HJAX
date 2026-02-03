/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package rubban;

import java.util.EventListener;

/**
 * Interface pour écouter les changements dans un HRibbonModel.
 * Analogie : similaire à TableModelListener pour JTable.
 * @author Banlock
 */
public interface HRibbonModelListener extends EventListener {
    
    /**
     * Notifie que la structure du ruban a changé.
     * Équivalent à tableChanged() dans TableModelListener.
     * 
     * @param e l'événement décrivant le changement
     */
    void ribbonChanged(HRibbonModelEvent e);
}
