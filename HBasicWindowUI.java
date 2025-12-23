/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HWindow;
import hcomponents.vues.shadow.HShadow;
import java.awt.*;
import javax.swing.*;

/**
 * Classe utilitaire pour le rendu personnalisé du composant HWindow.
 * Fournit un rendu moderne avec coins arrondis, dégradés de couleur,
 * ombres et bordures personnalisables.
 * 
 * <p>Cette classe gère le look and feel personnalisé du HWindow avec
 * support de l'antialiasing et des effets visuels avancés.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see HWindow
 */
public class HBasicWindowUI {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe contient uniquement des méthodes statiques.
     */
    private HBasicWindowUI() {
        // Utilitaire - pas d'instanciation
    }

    /**
     * Dessine le composant HWindow avec tous ses effets visuels.
     * Gère le rendu des ombres, dégradés, bordures et coins arrondis.
     * 
     * @param g le contexte graphique
     * @param component le composant panel à dessiner
     * @param window la fenêtre HWindow parente
     */
    public static void paintWindow(Graphics g, JComponent component, HWindow window) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Dimensions du composant
        int width = component.getWidth();
        int height = component.getHeight();
        int radius = window.getCornerRadius();
        
        // Activation de l'antialiasing pour un rendu lisse
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Récupération du style
        HWindowStyle style = window.getWindowStyle();
        
        // Dessin de l'ombre si définie
        HShadow shadow = window.getShadow();
        if (shadow != null) {
            shadow.paint(g2, component, width, height, radius);
        }
        
        // Dessin du fond avec dégradé
        Color baseColor = style.getBackgroundColor();
        GradientPaint gradient = new GradientPaint(
            0, 0, brighten(baseColor, 0.05f),
            0, height, darken(baseColor, 0.05f)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        
        // Application de la bordure personnalisée si définie
        if (window.getHBorder() != null) {
            window.getHBorder().paint(g2, component, width, height, radius);
        } else {
            // Bordure par défaut subtile
            g2.setColor(style.getBorderColor());
            g2.setStroke(new BasicStroke(style.getBorderWidth()));
            g2.drawRoundRect(
                style.getBorderWidth() / 2, 
                style.getBorderWidth() / 2,
                width - style.getBorderWidth(), 
                height - style.getBorderWidth(),
                radius, 
                radius
            );
        }
        
        g2.dispose();
    }

    /**
     * Éclaircit une couleur d'un facteur donné.
     * 
     * @param color la couleur de base
     * @param factor le facteur d'éclaircissement (0.0 à 1.0)
     * @return la couleur éclaircie
     */
    private static Color brighten(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b, color.getAlpha());
    }

    /**
     * Assombrit une couleur d'un facteur donné.
     * 
     * @param color la couleur de base
     * @param factor le facteur d'assombrissement (0.0 à 1.0)
     * @return la couleur assombrie
     */
    private static Color darken(Color color, float factor) {
        int r = Math.max(0, (int)(color.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(color.getBlue() * (1 - factor)));
        return new Color(r, g, b, color.getAlpha());
    }
}