/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HRadioButton;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicRadioButtonUI;

/**
 * Interface utilisateur personnalisée pour le composant HRadioButton.
 * Fournit un rendu moderne avec animations fluides, styles prédéfinis,
 * alignement vertical avancé (NORTH/EAST) et effets visuels.
 * 
 * <p>Cette classe implémente le look and feel personnalisé du HRadioButton
 * avec support des états (normal, hover, pressed, selected) et animations progressives.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see HRadioButton
 * @see BasicRadioButtonUI
 */
public class HRadioButtonUI extends BasicRadioButtonUI {
    
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
    
    /** Indique si la souris survole actuellement le composant */
    private boolean isHovering = false;
    
    /**
     * Installe l'interface utilisateur sur le composant.
     * Initialise les listeners et le système d'animation.
     * 
     * @param c le composant JRadioButton à personnaliser
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        // Initialisation du timer d'animation (60 FPS)
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
     * Dessine le composant HRadioButton avec tous ses effets visuels.
     * Gère l'alignement vertical avancé (NORTH/EAST) comme dans HButton.
     * 
     * @param g le contexte graphique
     * @param c le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        HRadioButton radioButton = (HRadioButton) c;
        ButtonModel model = radioButton.getModel();
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Activation de l'antialiasing pour un rendu lisse
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Récupération de l'alignement vertical et du gap
        int vAlign = radioButton.getVerticalAlignment();
        int gap = radioButton.getGap();
        
        // Pour les alignements standard (TOP, CENTER, BOTTOM), utiliser le rendu personnalisé standard
        if (vAlign != SwingConstants.NORTH && vAlign != SwingConstants.EAST) {
            // Dessin standard (texte à droite)
            paintStandardRadioButton(g2, radioButton, model);
            g2.dispose();
            return;
        }
        
        // Pour NORTH et EAST : calcul personnalisé 
        FontMetrics fm = g2.getFontMetrics();
        String text = radioButton.getText();
        int radioSize = radioButton.getRadioSize();
        
        // Zone de dessin disponible (hors bordures)
        Rectangle viewRect = new Rectangle();
        viewRect.x = radioButton.getInsets().left;
        viewRect.y = radioButton.getInsets().top;
        viewRect.width = radioButton.getWidth() - radioButton.getInsets().left - radioButton.getInsets().right;
        viewRect.height = radioButton.getHeight() - radioButton.getInsets().top - radioButton.getInsets().bottom;
        
        // Calcul des rectangles pour le texte et le bouton radio
        Rectangle radioRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        
        if (vAlign == SwingConstants.NORTH) {
            // NORTH : texte AU-DESSUS du bouton radio
            int textHeight = fm.getHeight();
            int totalContentHeight = radioSize + textHeight + gap;
            int startY = viewRect.y + (viewRect.height - totalContentHeight) / 2;
            
            // Position du texte (en haut)
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = startY;
            textRect.width = fm.stringWidth(text);
            textRect.height = textHeight;
            
            // Position du bouton radio (en bas)
            radioRect.x = viewRect.x + (viewRect.width - radioSize) / 2;
            radioRect.y = startY + textHeight + gap;
            radioRect.width = radioSize;
            radioRect.height = radioSize;
            
        } else if (vAlign == SwingConstants.EAST) {
            // EAST : texte EN DESSOUS du bouton radio
            int textHeight = fm.getHeight();
            int totalContentHeight = radioSize + textHeight + gap;
            int startY = viewRect.y + (viewRect.height - totalContentHeight) / 2;
            
            // Position du bouton radio (en haut)
            radioRect.x = viewRect.x + (viewRect.width - radioSize) / 2;
            radioRect.y = startY;
            radioRect.width = radioSize;
            radioRect.height = radioSize;
            
            // Position du texte (en bas)
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = startY + radioSize + gap;
            textRect.width = fm.stringWidth(text);
            textRect.height = textHeight;
        }
        
        // Dessin du bouton radio
        paintRadioButtonAtPosition(g2, radioButton, model, radioRect.x, radioRect.y);
        
        // Dessin du texte
        g2.setColor(radioButton.getForeground());
        g2.setFont(radioButton.getFont());
        g2.drawString(text, textRect.x, textRect.y + textRect.height - fm.getDescent());
        
        g2.dispose();
    }
    
    /**
     * Dessine un bouton radio standard (alignement TOP/CENTER/BOTTOM).
     * 
     * @param g2 le contexte graphique 2D
     * @param radioButton le composant HRadioButton
     * @param model le modèle du bouton
     */
    private void paintStandardRadioButton(Graphics2D g2, HRadioButton radioButton, ButtonModel model) {
        int radioSize = radioButton.getRadioSize();
        int gap = radioButton.getGap();
        FontMetrics fm = g2.getFontMetrics();
        String text = radioButton.getText();
        int textHeight = fm.getHeight();
        int height = Math.max(radioSize, textHeight);
        
        // Position du bouton radio (toujours à gauche)
        int radioX = 0;
        int radioY = (height - radioSize + 1) / 2; // +1 pour arrondi supérieur
        
        // Position du texte (à droite du bouton radio)
        int textX = radioSize + gap;
        int textBaseline = radioY + radioSize/2 + (fm.getAscent() - fm.getDescent())/2;
        
        // Dessin du bouton radio
        paintRadioButtonAtPosition(g2, radioButton, model, radioX, radioY);
        
        // Dessin du texte
        g2.setColor(radioButton.getForeground());
        g2.setFont(radioButton.getFont());
        g2.drawString(text, textX, textBaseline);
    }
    
    /**
     * Dessine un bouton radio à une position spécifique.
     * 
     * @param g2 le contexte graphique 2D
     * @param radioButton le composant HRadioButton
     * @param model le modèle du bouton
     * @param x la position X
     * @param y la position Y
     */
    private void paintRadioButtonAtPosition(Graphics2D g2, HRadioButton radioButton, ButtonModel model, int x, int y) {
        int radioSize = radioButton.getRadioSize();
        int cornerRadius = radioButton.getCornerRadius();
        
        // Récupération du style appliqué
        HButtonStyle style = radioButton.getButtonStyle();
        Color baseColor = style.getBaseColor();
        Color hoverColor = style.getHoverColor();
        Color selectedColor = style.getPressColor();
        Color dotColor = style.getTextColor();
        
        // Détermination de la couleur actuelle selon l'état
        Color currentColor;
        if (model.isSelected()) {
            currentColor = selectedColor;
        } else if (model.isPressed()) {
            currentColor = hoverColor.darker();
        } else {
            currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
        }
        
        // Dessin de l'ombre (si activée)
        if (radioButton.isShowShadow() && radioButton.getShadow() != null) {
            Graphics2D shadowG2 = (Graphics2D) g2.create();
            radioButton.getShadow().paint(shadowG2, radioButton, radioSize, radioSize, cornerRadius);
            shadowG2.dispose();
        }
        
        // Dessin du fond du bouton radio avec dégradé
        GradientPaint gradient = new GradientPaint(
            x, y, currentColor.brighter(),
            x, y + radioSize, currentColor.darker()
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(x, y, radioSize, radioSize, cornerRadius, cornerRadius);
        
        // Dessin de la bordure
        if (radioButton.getHBorder() != null) {
            Graphics2D borderG2 = (Graphics2D) g2.create();
            radioButton.getHBorder().paint(borderG2, radioButton, radioSize, radioSize, cornerRadius);
            borderG2.dispose();
        } else {
            // Bordure par défaut
            g2.setColor(currentColor.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, radioSize, radioSize, cornerRadius, cornerRadius);
        }
        
        // Dessin du point intérieur si sélectionné
        if (model.isSelected()) {
            drawRadioDot(g2, x, y, radioSize, radioButton.getDotSize(), dotColor);
        }
        
        // Indicateur de focus (si activé)
        if (radioButton.isFocusPainted() && radioButton.isFocusOwner()) {
            g2.setColor(new Color(style.getBaseColor().getRed(), 
                                  style.getBaseColor().getGreen(), 
                                  style.getBaseColor().getBlue(), 100));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x - 2, y - 2, radioSize + 4, radioSize + 4, 
                            cornerRadius + 2, cornerRadius + 2);
        }
    }
    
    /**
     * Dessine le point de sélection à l'intérieur du bouton radio.
     * 
     * @param g2 le contexte graphique 2D
     * @param x la position X du bouton radio
     * @param y la position Y du bouton radio
     * @param radioSize la taille du bouton radio
     * @param dotSize la taille du point intérieur
     * @param color la couleur du point
     */
    private void drawRadioDot(Graphics2D g2, int x, int y, int radioSize, int dotSize, Color color) {
        g2.setColor(color);
        
        // Calcul de la position centrée pour le point
        int dotX = x + (radioSize - dotSize) / 2;
        int dotY = y + (radioSize - dotSize) / 2;
        
        // Dessin du point (cercle rempli)
        g2.fillOval(dotX, dotY, dotSize, dotSize);
        
        // Optionnel: ajouter un léger effet de brillance
        g2.setColor(color.brighter());
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(dotX, dotY, dotSize, dotSize);
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
     * Interpole linéairement entre deux couleurs.
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
        return new Color(r, g, b);
    }
    
    /**
     * Calcule la taille préférée du composant.
     * Prend en compte l'alignement vertical pour le calcul des dimensions.
     * 
     * @param c le composant
     * @return la dimension préférée
     */
    @Override
    public Dimension getPreferredSize(JComponent c) {
        HRadioButton radioButton = (HRadioButton) c;
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int radioSize = radioButton.getRadioSize();
        int gap = radioButton.getGap();
        String text = radioButton.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int vAlign = radioButton.getVerticalAlignment();
        
        if (vAlign == SwingConstants.NORTH || vAlign == SwingConstants.EAST) {
            // Pour NORTH/EAST : largeur = max(bouton, texte), hauteur = bouton + texte + gap
            int width = Math.max(radioSize, textWidth) + 5;
            int height = radioSize + textHeight + gap + 5;
            return new Dimension(width, height);
        } else {
            // Pour TOP/CENTER/BOTTOM : largeur = bouton + gap + texte, hauteur = max(bouton, texte)
            int width = radioSize + gap + textWidth + 5;
            int height = Math.max(radioSize, textHeight);
            return new Dimension(width, height);
        }
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
}