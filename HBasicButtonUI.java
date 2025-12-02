/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HButton;
import hcomponents.vues.shadow.HShadow;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Interface utilisateur personnalisée pour le composant HButton.
 * Fournit un rendu moderne avec animations fluides, dégradés de couleur,
 * ombres, bordures arrondies et gestion avancée du positionnement.
 * 
 * <p>Cette classe implémente le look and feel personnalisé du HButton
 * avec support des états (normal, hover, pressed) et animations progressives.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see HButton
 * @see BasicButtonUI
 */
public class HBasicButtonUI extends BasicButtonUI {

    /** Rayon par défaut des coins arrondis */
    private int cornerRadius = 12;
    
    /** Couleur de base par défaut (utilisée comme fallback) */
    private Color baseColor = new Color(66, 165, 245);
    
    /** Couleur de survol par défaut (utilisée comme fallback) */
    private Color hoverColor = new Color(33, 150, 243);
    
    /** Couleur de pression par défaut (utilisée comme fallback) */
    private Color pressColor = new Color(30, 136, 229);
    
    /** Couleur du texte par défaut */
    private Color textColor = Color.BLACK;
    
    /** Couleur actuelle du bouton */
    private Color currentColor;
    
    /** Indique si la souris survole actuellement le bouton */
    private boolean isHovering = false;
    
    /** Timer pour gérer les animations fluides */
    private Timer hoverTimer;
    
    /** Progression actuelle de l'animation (0.0 à 1.0) */
    private float hoverProgress = 0f;
    
    /** Durée totale de l'animation en millisecondes */
    private static final int ANIMATION_DURATION = 200;
    
    /** Indique si l'animation va vers l'état hover (true) ou vers l'état normal (false) */
    private boolean animatingToHover = false;
    
    /** Temps de début de l'animation actuelle */
    private long animationStartTime = 0;

    /**
     * Constructeur par défaut.
     * Initialise la couleur actuelle avec la couleur de base.
     */
    public HBasicButtonUI() {
        currentColor = baseColor;
    }

    /**
     * Installe l'interface utilisateur sur le composant.
     * Initialise les listeners et le système d'animation.
     * 
     * @param c le composant JButton à personnaliser
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        // Initialisation du timer d'animation (environ 60 FPS)
        hoverTimer = new Timer(16, new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - animationStartTime;
                float progress = Math.min(1f, elapsed / (float)ANIMATION_DURATION);
                
                if (animatingToHover) {
                    hoverProgress = progress;
                } else {
                    hoverProgress = 1f - progress;
                }
                
                if (elapsed >= ANIMATION_DURATION) {
                    hoverTimer.stop();
                    hoverProgress = animatingToHover ? 1f : 0f;
                }                
                c.repaint();
            }
        });
        
        // Ajout des listeners pour détecter le survol de la souris
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovering = true;
                startHoverAnimation(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                startHoverAnimation(false);
            }
        });
    }  
    
    /**
     * Dessine le composant HButton avec tous ses effets visuels.
     * Gère le rendu des ombres, dégradés, bordures et positionnement du contenu.
     * 
     * @param g le contexte graphique
     * @param c le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        HButton hButton = (HButton) c;
        int vAlign = b.getVerticalAlignment();
        int gap = hButton.getGap();
        ButtonModel model = b.getModel();
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Dimensions du composant
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = hButton.getCornerRadius();
        
        // Activation de l'antialiasing pour un rendu lisse
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Récupération des couleurs selon le style
        Color baseColor = getBaseColor(c);
        Color hoverColor = getHoverColor(c);
        Color pressColor = getPressColor(c);
        
        // Détermination de la couleur actuelle avec interpolation pour animation fluide
        Color currentColor;
        if (model.isPressed()) {
            currentColor = pressColor;
        } else {
            currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
        }        
        
        // Dessin de l'ombre si définie
        if (hButton != null) {
            HShadow shadow = hButton.getShadow();
            if (shadow != null) {
                shadow.paint(g2, c, width, height, radius);
            }
        }
        
        // Dessin du fond avec dégradé
        GradientPaint gradient = new GradientPaint(0, 0, currentColor.brighter(), 
                                                   0, height, currentColor.darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width - 5, height - 5, radius, radius);
        
        // Pour les alignements standard, utiliser le rendu parent
        if (vAlign != SwingConstants.NORTH && vAlign != SwingConstants.EAST) {
            if (hButton != null) {
                HShadow shadow = hButton.getShadow();
                if (shadow != null) {
                    shadow.paint(g2, c, width, height, radius);
                }
            }
            super.paint(g, c);
            return;
        }
        
        // Calcul du positionnement pour les alignements NORTH et EAST
        FontMetrics fm = g2.getFontMetrics();
        String text = b.getText();
        Icon icon = b.getIcon();
        
        // Zone de dessin disponible (hors bordures)
        Rectangle viewRect = new Rectangle();
        viewRect.x = b.getInsets().left;
        viewRect.y = b.getInsets().top;
        viewRect.width = b.getWidth() - b.getInsets().left - b.getInsets().right;
        viewRect.height = b.getHeight() - b.getInsets().top - b.getInsets().bottom;
        
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        
        // Alignement NORTH (icône au-dessus du texte)
        if (vAlign == SwingConstants.NORTH) {
            int iconH = (icon != null) ? icon.getIconHeight() : 0;
            int textH = fm.getHeight();
            int totalContentHeight = iconH + textH + gap;
            int startY = viewRect.y + (viewRect.height - totalContentHeight) / 2;
            
            if (icon != null) {
                iconRect.x = viewRect.x + (viewRect.width - icon.getIconWidth()) / 2;
                iconRect.y = startY;
                iconRect.width = icon.getIconWidth();
                iconRect.height = icon.getIconHeight();
            }
            
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = startY + iconH + gap;
            textRect.width = fm.stringWidth(text);
            textRect.height = textH;
            
        } 
        // Alignement EAST (texte au-dessus de l'icône)
        else if (vAlign == SwingConstants.EAST) {
            int iconH = (icon != null) ? icon.getIconHeight() : 0;
            int textH = fm.getHeight();
            int totalContentHeight = iconH + textH;
            int startY = viewRect.y + (viewRect.height - totalContentHeight) / 2;
            
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = startY;
            textRect.width = fm.stringWidth(text);
            textRect.height = textH;
            
            if (icon != null) {
                iconRect.x = viewRect.x + (viewRect.width - icon.getIconWidth()) / 2;
                iconRect.y = startY + textH + gap;
                iconRect.width = iconH;
                iconRect.height = icon.getIconHeight();
            }
        }
        
        // Dessin de l'icône
        if (icon != null) {
            icon.paintIcon(c, g, iconRect.x, iconRect.y);
        }
        
        // Dessin du texte
        g.setFont(b.getFont());
        g.setColor(b.getForeground());
        g.drawString(text, textRect.x, textRect.y + textRect.height - fm.getDescent());
        
        // Application de la bordure personnalisée si définie
        if (hButton.getHBorder() != null) {
            hButton.getHBorder().paint((Graphics2D) g, hButton, hButton.getWidth(), hButton.getHeight(), radius);
        }
        
        g2.dispose();
    }
    
    /**
     * Désinstalle l'interface utilisateur du composant.
     * Nettoie les ressources utilisées (notamment le timer).
     * 
     * @param c le composant
     */
    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }
    
    /**
     * Démarre l'animation de transition entre les états normal et hover.
     * 
     * @param toHover true pour animer vers l'état hover, false pour animer vers l'état normal
     */
    private void startHoverAnimation(boolean toHover) {
        animatingToHover = toHover;
        animationStartTime = System.currentTimeMillis();
        
        if (!hoverTimer.isRunning()) {
            hoverTimer.start();
        }
    }
    
    /**
     * Interpole linéairement entre deux couleurs avec transparence.
     * 
     * @param c1 la couleur de départ
     * @param c2 la couleur d'arrivée
     * @param progress le pourcentage de progression (0.0 à 1.0)
     * @return la couleur interpolée
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int)(c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        
        return new Color(r, g, b, a);
    }
    
    /**
     * Récupère la couleur de base du composant selon son style.
     * 
     * @param c le composant
     * @return la couleur de base
     */
    private Color getBaseColor(JComponent c) {
        if (c instanceof HButton) {
            HButton button = (HButton) c;
            return button.getButtonStyle().getBaseColor();
        }
        return new Color(66, 165, 245); // Valeur par défaut
    }
    
    /**
     * Récupère la couleur de survol du composant selon son style.
     * 
     * @param c le composant
     * @return la couleur de survol
     */
    private Color getHoverColor(JComponent c) {
        if (c instanceof HButton) {
            HButton button = (HButton) c;
            return button.getButtonStyle().getHoverColor();
        }
        return new Color(33, 150, 243); // Valeur par défaut
    }
    
    /**
     * Récupère la couleur de pression du composant selon son style.
     * 
     * @param c le composant
     * @return la couleur de pression
     */
    private Color getPressColor(JComponent c) {
        if (c instanceof HButton) {
            HButton button = (HButton) c;
            return button.getButtonStyle().getPressColor();
        }
        return new Color(30, 136, 229); // Valeur par défaut
    }    
}