package hcomponents.HRibbon;

import java.awt.*;
import java.util.Arrays;

/**
 * RubanLayout est responsable UNIQUEMENT du calcul de la géométrie du HRuban.
 *
 * Il traduit une position logique (row, column) en coordonnées graphiques (x,
 * y, width, height).
 *
 * Il dépend du modèle, mais ne fait AUCUN rendu.
 */
public class HRibbonLayoutManager implements LayoutManager {

    /**
     * Référence vers le HRuban. 
     */
    private HRibbon hribbon;

    /**
     * Constructeur du RubanLayout.     *
     * @param hribbon
     */
    public HRibbonLayoutManager(HRibbon hribbon ) {
        // On conserve les références pour les utiliser lors des calculs
        this.hribbon = hribbon;
    }
       
    @Override
    public void addLayoutComponent(String name, Component comp) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void layoutContainer(Container parent) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
