/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 *
 * @author FIDELE
 */
public abstract class HRibbonUI extends ComponentUI {
   /**
     * Peint le ruban.
     * 
     * @param g le contexte graphique
     * @param c le composant HRibbon à peindre
     */
    public abstract void paint(Graphics g, JComponent c);
    
    /**
     * Peint l'arrière-plan du ruban.
     * 
     * @param g le contexte graphique
     * @param c le composant HRibbon
     */
    public abstract void paintBackground(Graphics g, HRibbon ribbon);
    
    /**
     * Peint les bordures entre les groupes.
     * 
     * @param g le contexte graphique
     * @param ribbon le ruban
     * @param groupBounds les limites de chaque groupe
     */
    public abstract void paintGroupDividers(Graphics g, HRibbon ribbon, Rectangle[] groupBounds);
    
    /**
     * Retourne les marges préférées du ruban.
     */
    public abstract java.awt.Insets getPreferredMargins(HRibbon ribbon);
    
    /**
     * Retourne la hauteur préférée du ruban.
     */
    public abstract int getPreferredHeight(HRibbon ribbon);
    
    /**
     * Installe les listeners et propriétés par défaut.
     */
    public abstract void installUI(JComponent c);
    
    /**
     * Désinstalle les listeners et propriétés.
     */
    public abstract void uninstallUI(JComponent c);
}
