/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HToolBar;
import hcomponents.vues.shadow.HShadow;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

/**
 * Interface utilisateur personnalisée pour le composant HToolBar.
 * Fournit un rendu moderne avec dégradés de couleur, ombres,
 * bordures arrondies et gestion de l'orientation.
 * 
 * <p>Cette classe implémente le look and feel personnalisé du HToolBar
 * avec support des styles et des deux orientations (horizontale/verticale).</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see HToolBar
 * @see BasicToolBarUI
 */
public class HBasicToolBarUI extends BasicToolBarUI {

    /**
     * Constructeur par défaut.
     */
    public HBasicToolBarUI() {
        super();
    }

    /**
     * Installe l'interface utilisateur sur le composant.
     * 
     * @param c le composant JToolBar à personnaliser
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
    }

    /**
     * Dessine le composant HToolBar avec tous ses effets visuels.
     * Gère le rendu des ombres, dégradés et bordures selon l'orientation.
     * 
     * @param g le contexte graphique
     * @param c le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        HToolBar toolBar = (HToolBar) c;
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Dimensions du composant
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = toolBar.getCornerRadius();
        
        // Activation de l'antialiasing pour un rendu lisse
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Dessin de l'ombre si définie
        HShadow shadow = toolBar.getShadow();
        if (shadow != null) {
            shadow.paint(g2, c, width, height, radius);
        }
        
        // Récupération du style
        HToolBarStyle style = toolBar.getToolBarStyle();
        Color baseColor = style.getBaseColor();
        
        // Dessin du fond avec ou sans dégradé
        if (toolBar.isUseGradient()) {
            // Dégradé selon l'orientation
            GradientPaint gradient;
            if (toolBar.getOrientation() == JToolBar.HORIZONTAL) {
                // Dégradé vertical pour toolbar horizontale
                gradient = new GradientPaint(
                    0, 0, baseColor.brighter(),
                    0, height, baseColor
                );
            } else {
                // Dégradé horizontal pour toolbar verticale
                gradient = new GradientPaint(
                    0, 0, baseColor.brighter(),
                    width, 0, baseColor
                );
            }
            g2.setPaint(gradient);
        } else {
            // Fond uni
            g2.setColor(baseColor);
        }
        
        // Dessin du fond arrondi
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        
        // Application de la bordure personnalisée si définie
        if (toolBar.getHBorder() != null) {
            toolBar.getHBorder().paint(g2, toolBar, width, height, radius);
        }
        
        g2.dispose();
        
        // Appel du rendu parent pour dessiner les composants enfants
        super.paint(g, c);
    }

    /**
     * Désinstalle l'interface utilisateur du composant.
     * Nettoie les ressources utilisées.
     * 
     * @param c le composant
     */
    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
    }
}