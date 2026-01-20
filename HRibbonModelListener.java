/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hcomponents.HRibbon;

import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent;

/**
 *HRibbonModelListener Définit l'interface d'un objet qui écoute les modifications
 dans un HTableModel 
 * 
 * @author FIDELE
 */

public interface HRibbonModelListener extends java.util.EventListener {
        
    public void modelChanged(HRibbonModelEvent e);
    
}
