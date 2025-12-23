/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HDesktopPane;
import hcomponents.vues.shadow.HShadow;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicDesktopPaneUI;

/**
 * Interface utilisateur personnalisée pour le composant HDesktopPane.
 * Fournit un rendu moderne avec dégradés de couleur, motifs (grille/points),
 * ombres et bordures arrondies.
 * 
 * <p>Cette classe implémente le look and feel personnalisé du HDesktopPane
 * avec support des styles et des motifs de fond.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see HDesktopPane
 * @see BasicDesktopPaneUI
 */
public class HBasicDesktopPaneUI extends BasicDesktopPaneUI {

    /**
     * Constructeur par défaut.
     */
    public HBasicDesktopPaneUI() {
        super();
    }

    /**
     * Installe l'interface utilisateur sur le composant.
     * 
     * @param c le composant JDesktopPane à personnaliser
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
    }

    /**
     * Dessine le composant HDesktopPane avec tous ses effets visuels.
     * Gère le rendu des ombres, dégradés, motifs et bordures.
     * 
     * @param g le contexte graphique
     * @param c le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        HDesktopPane desktop = (HDesktopPane) c;
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Dimensions du composant
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = desktop.getCornerRadius();
        
        // Activation de l'antialiasing pour un rendu lisse
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Dessin de l'ombre si définie
        HShadow shadow = desktop.getShadow();
        if (shadow != null) {
            shadow.paint(g2, c, width, height, radius);
        }
        
        // Récupération du style
        HDesktopPaneStyle style = desktop.getDesktopStyle();
        Color baseColor = style.getBackgroundColor();
        
        // Dessin du fond avec ou sans dégradé
        if (desktop.isUseGradient()) {
            // Dégradé diagonal
            GradientPaint gradient = new GradientPaint(
                0, 0, baseColor.brighter(),
                width, height, baseColor.darker()
            );
            g2.setPaint(gradient);
        } else {
            // Fond uni
            g2.setColor(baseColor);
        }
        
        // Dessin du fond arrondi
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        
        // Dessin du motif si activé
        if (desktop.isShowPattern()) {
            drawPattern(g2, desktop, width, height);
        }
        
        // Application de la bordure personnalisée si définie
        if (desktop.getHBorder() != null) {
            desktop.getHBorder().paint(g2, desktop, width, height, radius);
        }
        
        g2.dispose();
        
        // Appel du rendu parent pour dessiner les fenêtres internes
        super.paint(g, c);
    }

    /**
     * Dessine le motif de fond selon le type choisi.
     * 
     * @param g2 le contexte graphique 2D
     * @param desktop le desktop pane
     * @param width la largeur du composant
     * @param height la hauteur du composant
     */
    private void drawPattern(Graphics2D g2, HDesktopPane desktop, int width, int height) {
        g2.setColor(desktop.getPatternColor());
        int spacing = desktop.getPatternSpacing();
        
        switch (desktop.getPatternType()) {
            case GRID:
                drawGridPattern(g2, width, height, spacing);
                break;
            case DOTS:
                drawDotsPattern(g2, width, height, spacing);
                break;
            case NONE:
            default:
                // Pas de motif
                break;
        }
    }

    /**
     * Dessine un motif de grille.
     * Trace des lignes verticales et horizontales espacées régulièrement.
     * 
     * @param g2 le contexte graphique 2D
     * @param width la largeur du composant
     * @param height la hauteur du composant
     * @param spacing l'espacement entre les lignes
     */
    private void drawGridPattern(Graphics2D g2, int width, int height, int spacing) {
        g2.setStroke(new BasicStroke(1));
        
        // Lignes verticales
        for (int x = spacing; x < width; x += spacing) {
            g2.drawLine(x, 0, x, height);
        }
        
        // Lignes horizontales
        for (int y = spacing; y < height; y += spacing) {
            g2.drawLine(0, y, width, y);
        }
    }

    /**
     * Dessine un motif de points.
     * Place des cercles espacés régulièrement sur toute la surface.
     * 
     * @param g2 le contexte graphique 2D
     * @param width la largeur du composant
     * @param height la hauteur du composant
     * @param spacing l'espacement entre les points
     */
    private void drawDotsPattern(Graphics2D g2, int width, int height, int spacing) {
        int dotSize = 3;
        
        // Parcours de la grille pour placer les points
        for (int x = spacing; x < width; x += spacing) {
            for (int y = spacing; y < height; y += spacing) {
                g2.fillOval(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize);
            }
        }
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