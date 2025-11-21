/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;

import hcomponents.HList;
import hcomponents.models.HListModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author FIDELE
 */
public class HListController {
    
    private final HList<?> list;
    private final HListModel<?> model;
    
    public HListController(HList<?> list, HListModel<?> model) {
        this.list = list;
        this.model = model;
        
        setupListeners();
    }
    
    /**
     * Configure tous les listeners
     */
    private void setupListeners() {
        // Utiliser le système de notification du modèle pour rafraîchir l'affichage
        model.addStateListener(() -> list.repaint());
        
        // Gestion de la sélection : Evenement qui se déclenche quand : 
        //clic, flèche du clavier, selection changer par programmation
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    // Boolean: Selection en cours (true) ou Sélection terminée (false)
                    // Le repaint est maintenant géré par le stateListener
                }
            }
        });
        
        // Gestion du hover (survol)
        list.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredIndex(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                updateHoveredIndex(e);
            }
        });
        
        // Réinitialiser le hover quand la souris quitte
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                model.setHoveredIndex(-1);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Assurer que le hover est mis à jour lors du clic
                updateHoveredIndex(e);
            }
        });
    }
    
    /**
     * Met à jour l'index survolé basé sur la position de la souris
     */
    private void updateHoveredIndex(MouseEvent e) {
        int index = list.locationToIndex(e.getPoint());
        if (index != -1 && list.getCellBounds(index, index).contains(e.getPoint())) {
            model.setHoveredIndex(index);
        } else {
            model.setHoveredIndex(-1);
        }
    }
    
    /**
     * Nettoie les ressources
     */
    public void dispose() {
        // Les listeners seront garbage collected avec le composant
    }
    
}
