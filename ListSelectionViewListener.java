/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package IllustrationShape.model;

import java.util.EventListener;

/**
 * Écoute les changements de sélection produits par un ViewModelSelection.
 * 
 * @author FIDELE
 */
public interface ListSelectionViewListener extends EventListener {
    
    void valueChanged(ListViewSelectionEvent e);   
    
}
