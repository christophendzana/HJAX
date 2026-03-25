/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HToggleButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * HToggleButtonUI - UI Delegate personnalisé pour HToggleButton
 * 
 * PRINCIPE FONDAMENTAL : Ceci est un "UI Delegate" Swing qui sépare l'apparence
 *                        du comportement. Il redéfinit uniquement le rendu visuel.
 * 
 * HERITAGE : Étend BasicButtonUI pour hériter du comportement standard des boutons
 *            et ne redéfinir que l'apparence.
 * 
 * CUSTOMISATION : 
 * 1. Effets de survol animés
 * 2. État "sélectionné" (toggle)
 * 3. Marque de vérification (checkmark) quand sélectionné
 * 4. Dégradés et coins arrondis
 * 
 * IMPORTANT : Cette classe ne gère PAS les événements de clic, seulement l'affichage.
 *             Le comportement reste celui de JToggleButton.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HToggleButtonUI extends BasicButtonUI {
    
    // ===================================================================
    // VARIABLES D'ANIMATION
    // ===================================================================
    
    /**
     * Timer pour gérer les animations fluides.
     * PRINCIPE : Swing utilise des Timers pour les animations car ils s'exécutent
     *            dans le thread EDT (Event Dispatch Thread).
     */
    private Timer hoverTimer;
    
    /**
     * Progression de l'animation de survol (0 à 1).
     * UTILISATION : Interpolation entre l'état normal et l'état survolé.
     */
    private float hoverProgress = 0f;
    
    /**
     * Durée de l'animation en millisecondes.
     * CONSEIL : 200ms est une durée standard pour les animations d'interface.
     */
    private static final int ANIMATION_DURATION = 200;
    
    /**
     * Direction de l'animation en cours.
     * true = vers l'état survolé, false = vers l'état normal.
     */
    private boolean animatingToHover = false;
    
    /**
     * Moment où l'animation a commencé.
     * UTILISATION : Calcul du temps écoulé pour l'interpolation.
     */
    private long animationStartTime = 0;
    
    /**
     * État de survol actuel.
     * NOTE : Différent de hoverProgress qui est l'animation en cours.
     */
    private boolean isHovering = false;
    
    // ===================================================================
    // MÉTHODE PRINCIPALE D'INSTALLATION
    // ===================================================================
    
    /**
     * Méthode appelée quand cet UI est installé sur un composant.
     * 
     * ROLE SWING : Swing appelle cette méthode automatiquement quand on
     *              appelle setUI() ou updateUI() sur le composant.
     * 
     * ÉTAPES CRITIQUES :
     * 1. Appeler super.installUI() pour l'initialisation de base
     * 2. Configurer les écouteurs d'événements
     * 3. Initialiser les ressources (timers, etc.)
     * 
     * @param c Le composant JComponent sur lequel cet UI est installé
     */
    @Override
    public void installUI(JComponent c) {
       //Initialisation de base 
        super.installUI(c);
        
        HToggleButton button = (HToggleButton) c;
        
        // CONFIGURATION DU TIMER D'ANIMATION
        // PRINCIPE : Le Timer s'exécute environ 60 fois/seconde (1000ms/16ms ≈ 60 FPS)
        hoverTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calcul du temps écoulé depuis le début de l'animation
                long elapsed = System.currentTimeMillis() - animationStartTime;
                
                // Progression linéaire de 0 à 1
                float progress = Math.min(1f, elapsed / (float)ANIMATION_DURATION);
                
                // Mise à jour de la progression selon la direction
                if (animatingToHover) {
                    hoverProgress = progress; // Vers survolé (0 → 1)
                } else {
                    hoverProgress = 1f - progress; // Vers normal (1 → 0)
                }
                
                // Arrêt du timer quand l'animation est terminée
                if (elapsed >= ANIMATION_DURATION) {
                    hoverTimer.stop();
                    hoverProgress = animatingToHover ? 1f : 0f; // Valeur finale
                }
                
                // Demande de redessin du composant
                c.repaint();
            }
        });
        
        
        // CONFIGURATION DES ÉCOUTEURS DE SOURIS      
        c.addMouseListener(new MouseAdapter() {
            /**
             * Appelé quand la souris entre dans la zone du composant.
             */
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovering = true;
                startHoverAnimation(true); // Animation vers survolé
            }
            
            /**
             * Appelé quand la souris quitte la zone du composant.
             */
            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                startHoverAnimation(false); // Animation vers normal
            }
        });
    }
    
    
    /**    
     * @param g L'objet Graphics fourni par Swing
     * @param c Le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        // Cast vers notre composant personnalisé
        HToggleButton button = (HToggleButton) c;
        
        // Récupération du modèle d'état du bouton
        ButtonModel model = button.getModel();
        
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Dimensions du composant
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = button.getCornerRadius();
        
        // Activation de l'anti-crénelage (lissage des bords)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // ÉTAPE 2 : DÉTERMINATION DES COULEURS
        HButtonStyle style = button.getButtonStyle();
        
        // Palette de couleurs selon le style
        Color baseColor = style.getBaseColor();      // État normal
        Color hoverColor = style.getHoverColor();    // État survolé
        Color pressColor = style.getPressColor();    // État pressé
        Color selectedColor = style.getPressColor().darker(); // État sélectionné
        
        // Décision de la couleur actuelle selon l'état
        Color currentColor;
        if (model.isSelected()) {
            currentColor = selectedColor;
        } else if (model.isPressed()) {
            currentColor = pressColor;
        } else {
            currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
        }
        
        // ÉTAPE 3 : DESSIN DE L'OMBRE
        if (button.getShadow() != null) {
            button.getShadow().paint(g2, c, width, height, radius);
        }
        
        // ÉTAPE 4 : DESSIN DU FOND AVEC DÉGRADÉ
        // Création d'un dégradé vertical
        GradientPaint gradient = new GradientPaint(
            0, 0, currentColor.brighter(),        // Couleur en haut
            0, height, currentColor.darker()      // Couleur en bas
        );
        g2.setPaint(gradient);
        
        // Dessin du rectangle arrondi
        // NOTE : -5 pour laisser de la place à l'ombre
        g2.fillRoundRect(0, 0, width - 5, height - 5, radius, radius);
        
        // ÉTAPE 5 : DESSIN DE LA BORDURE
        if (button.getHBorder() != null) {
            // Utiliser la bordure personnalisée si définie
            button.getHBorder().paint(g2, button, width, height, radius);
        } else if (model.isSelected()) {
            // Bordure par défaut pour l'état sélectionné
            g2.setColor(style.getTextColor());
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, width - 3, height - 3, radius, radius);
        }
        
        // ÉTAPE 6 : DESSIN DE LA MARQUE DE VÉRIFICATION
        if (model.isSelected()) {
            int checkMargin = button.getCheckMarkMargin();
            drawCheckmark(g2, width, height, style.getTextColor(), checkMargin);
        }
        
        // ÉTAPE 7 : DESSIN DU TEXTE ET DE L'ICÔNE (PARENT)
        // IMPORTANT : Appeler la méthode parent pour le texte et l'icône standard
        super.paint(g2, c);
        
        // ÉTAPE 8 : NETTOYAGE
        g2.dispose(); // LIBÉRER LA RESSOURCE GRAPHIQUE
    }
    
    // MÉTHODE AUXILIAIRE : DESSIN DE LA MARQUE DE VÉRIFICATION
    
    /**
     * Dessine une marque de vérification (checkmark).
     * 
     * POSITIONNEMENT : Centré verticalement, aligné à droite avec une marge.
     * 
     * @param g2        Le contexte graphique
     * @param width     Largeur du composant
     * @param height    Hauteur du composant
     * @param color     Couleur de la marque
     * @param checkMargin Marge depuis le bord droit
     */
    private void drawCheckmark(Graphics2D g2, int width, int height, 
                              Color color, int checkMargin) {
        
        // Configuration du style de trait
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Calcul de la taille proportionnelle mais limitée
        int size = Math.min(16, Math.min(width, height) / 4);
        
        // Calcul de la position (centré verticalement, avec marge à droite)
        int x = width - size - checkMargin;
        int y = height / 2;
        
        // Dessin des deux segments de la marque
        // Segment 1 : oblique descendant
        g2.drawLine(x, y, x + size/3, y + size/2);
        // Segment 2 : oblique ascendant
        g2.drawLine(x + size/3, y + size/2, x + size, y - size/3);
    }
    
    // MÉTHODE AUXILIAIRE : DÉMARRAGE DE L'ANIMATION
    
    /**
     * Démarre l'animation de survol.
     * 
     * @param toHover true pour animer vers l'état survolé,
     *                false pour animer vers l'état normal
     */
    private void startHoverAnimation(boolean toHover) {
        animatingToHover = toHover;
        animationStartTime = System.currentTimeMillis();
        if (!hoverTimer.isRunning()) {
            hoverTimer.start();
        }
    }
    
    // ===================================================================
    // MÉTHODE AUXILIAIRE : INTERPOLATION DE COULEUR
    // ===================================================================
    
    /**
     * Interpole linéairement entre deux couleurs.
     * 
     * FORMULE : C = C1 + (C2 - C1) * progress
     * 
     * @param c1      Couleur de départ
     * @param c2      Couleur d'arrivée
     * @param progress Progression de 0 (c1) à 1 (c2)
     * @return La couleur interpolée
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        // Clamp de la progression entre 0 et 1
        progress = Math.max(0, Math.min(1, progress));
        
        // Interpolation linéaire pour chaque composante RGB
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        
        return new Color(r, g, b);
    }
    
    // MÉTHODE DE NETTOYAGE    
    /**
     * Méthode appelée quand cet UI est désinstallé du composant.
     * 
     * IMPORTANT : Toujours nettoyer les ressources (timers, listeners)
     *             pour éviter les fuites de mémoire.
     * 
     * @param c Le composant dont cet UI est désinstallé
     */
    @Override
    public void uninstallUI(JComponent c) {
        // Arrêt du timer s'il est en cours
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        
        // Nettoyage de la classe parente
        super.uninstallUI(c);
    }
    
}
