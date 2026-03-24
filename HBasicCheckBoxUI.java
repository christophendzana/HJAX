/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HCheckBox;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicCheckBoxUI;

/**
 * Interface utilisateur personnalisée pour le composant HCheckBox.
 * Fournit un rendu moderne avec animations fluides, styles prédéfinis,
 * alignement vertical avancé (NORTH/EAST) et effets visuels.
 * 
 * <p>Cette classe implémente le look and feel personnalisé du HCheckBox
 * avec support des états (normal, hover, pressed, selected) et animations progressives.</p>
 * 
 * @author FIDELE
 * @version 1.1
 * @see HCheckBox
 * @see BasicCheckBoxUI
 */
public class HBasicCheckBoxUI extends BasicCheckBoxUI {
    
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
     * @param c le composant JCheckBox à personnaliser
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
     * Dessine le composant HCheckBox avec tous ses effets visuels.
     * Gère l'alignement vertical avancé (NORTH/EAST) comme dans HButton.
     * 
     * @param g le contexte graphique
     * @param c le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        HCheckBox checkBox = (HCheckBox) c;
        ButtonModel model = checkBox.getModel();
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Activation de l'antialiasing pour un rendu lisse
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Récupération de l'alignement vertical et du gap
        int vAlign = checkBox.getVerticalAlignment();
        int gap = checkBox.getGap();
        
        // Pour les alignements standard (TOP, CENTER, BOTTOM), utiliser le rendu parent
        if (vAlign != SwingConstants.NORTH && vAlign != SwingConstants.EAST) {
            // Dessin standard
            paintStandardCheckbox(g2, checkBox, model);
            g2.dispose();
            return;
        }
        
        // Pour NORTH et EAST : calcul personnalisé comme dans HButton
        FontMetrics fm = g2.getFontMetrics();
        String text = checkBox.getText();
        int checkSize = checkBox.getCheckmarkSize();
        
        // Zone de dessin disponible (hors bordures)
        Rectangle viewRect = new Rectangle();
        viewRect.x = checkBox.getInsets().left;
        viewRect.y = checkBox.getInsets().top;
        viewRect.width = checkBox.getWidth() - checkBox.getInsets().left - checkBox.getInsets().right;
        viewRect.height = checkBox.getHeight() - checkBox.getInsets().top - checkBox.getInsets().bottom;
        
        // Calcul des rectangles pour le texte et la case
        Rectangle checkRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        
        if (vAlign == SwingConstants.NORTH) {
            // NORTH : texte AU-DESSUS de la case
            int textHeight = fm.getHeight();
            int totalContentHeight = checkSize + textHeight + gap;
            int startY = viewRect.y + (viewRect.height - totalContentHeight) / 2;
            
            // Position du texte (en haut)
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = startY;
            textRect.width = fm.stringWidth(text);
            textRect.height = textHeight;
            
            // Position de la case (en bas)
            checkRect.x = viewRect.x + (viewRect.width - checkSize) / 2;
            checkRect.y = startY + textHeight + gap;
            checkRect.width = checkSize;
            checkRect.height = checkSize;
            
        } else if (vAlign == SwingConstants.EAST) {
            // EAST : texte EN DESSOUS de la case
            int textHeight = fm.getHeight();
            int totalContentHeight = checkSize + textHeight + gap;
            int startY = viewRect.y + (viewRect.height - totalContentHeight) / 2;
            
            // Position de la case (en haut)
            checkRect.x = viewRect.x + (viewRect.width - checkSize) / 2;
            checkRect.y = startY;
            checkRect.width = checkSize;
            checkRect.height = checkSize;
            
            // Position du texte (en bas)
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text)) / 2;
            textRect.y = startY + checkSize + gap;
            textRect.width = fm.stringWidth(text);
            textRect.height = textHeight;
        }
        
        // Dessin de la case à cocher
        paintCheckboxAtPosition(g2, checkBox, model, checkRect.x, checkRect.y);
        
        // Dessin du texte
        g2.setColor(checkBox.getForeground());
        g2.setFont(checkBox.getFont());
        g2.drawString(text, textRect.x, textRect.y + textRect.height - fm.getDescent());
        
        g2.dispose();
    }
    
    /**
     * Dessine une case à cocher standard (alignement TOP/CENTER/BOTTOM).
     * 
     * @param g2 le contexte graphique 2D
     * @param checkBox le composant HCheckBox
     * @param model le modèle du bouton
     */
    private void paintStandardCheckbox(Graphics2D g2, HCheckBox checkBox, ButtonModel model) {
        int checkSize = checkBox.getCheckmarkSize();
        int gap = checkBox.getGap();
        FontMetrics fm = g2.getFontMetrics();
        String text = checkBox.getText();
        int textHeight = fm.getHeight();
        int height = Math.max(checkSize, textHeight);
        
        // Position de la case à cocher (toujours à gauche)
        int checkX = 0;
        int checkY = (height - checkSize + 1) / 2; // +1 pour arrondi supérieur
        
        // Position du texte (à droite de la case)
        int textX = checkSize + gap;
        int textBaseline = checkY + checkSize/2 + (fm.getAscent() - fm.getDescent())/2;
        
        // Dessin de la case
        paintCheckboxAtPosition(g2, checkBox, model, checkX, checkY);
        
        // Dessin du texte
        g2.setColor(checkBox.getForeground());
        g2.setFont(checkBox.getFont());
        g2.drawString(text, textX, textBaseline);
    }
    
    /**
     * Dessine une case à cocher à une position spécifique.
     * 
     * @param g2 le contexte graphique 2D
     * @param checkBox le composant HCheckBox
     * @param model le modèle du bouton
     * @param x la position X
     * @param y la position Y
     */
    private void paintCheckboxAtPosition(Graphics2D g2, HCheckBox checkBox, ButtonModel model, int x, int y) {
        int checkSize = checkBox.getCheckmarkSize();
        
        // Récupération du style appliqué
        HButtonStyle style = checkBox.getButtonStyle();
        Color baseColor = style.getBaseColor();
        Color hoverColor = style.getHoverColor();
        Color selectedColor = style.getPressColor();
        Color checkmarkColor = style.getTextColor();
        
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
        if (checkBox.isShowShadow() && checkBox.getShadow() != null) {
            Graphics2D shadowG2 = (Graphics2D) g2.create();
            checkBox.getShadow().paint(shadowG2, checkBox, checkSize, checkSize, 
                                       checkBox.getCornerRadius());
            shadowG2.dispose();
        }
        
        // Dessin du fond de la case à cocher avec dégradé
        GradientPaint gradient = new GradientPaint(
            x, y, currentColor.brighter(),
            x, y + checkSize, currentColor.darker()
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(x, y, checkSize, checkSize, 
                         checkBox.getCornerRadius(), checkBox.getCornerRadius());
        
        // Dessin de la bordure
        if (checkBox.getHBorder() != null) {
            Graphics2D borderG2 = (Graphics2D) g2.create();
            checkBox.getHBorder().paint(borderG2, checkBox, checkSize, checkSize, 
                                        checkBox.getCornerRadius());
            borderG2.dispose();
        } else {
            // Bordure par défaut
            g2.setColor(currentColor.darker());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, checkSize, checkSize, 
                            checkBox.getCornerRadius(), checkBox.getCornerRadius());
        }
        
        // Dessin du checkmark si la case est cochée
        if (model.isSelected()) {
            drawCheckmark(g2, x, y, checkSize, checkmarkColor);
        }
        
        // Indicateur de focus (si activé)
        if (checkBox.isFocusPainted() && checkBox.isFocusOwner()) {
            g2.setColor(new Color(style.getBaseColor().getRed(), 
                                  style.getBaseColor().getGreen(), 
                                  style.getBaseColor().getBlue(), 100));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x - 2, y - 2, checkSize + 4, checkSize + 4, 
                            checkBox.getCornerRadius() + 2, checkBox.getCornerRadius() + 2);
        }
    }
    
    /**
     * Dessine un checkmark stylisé (✓) dans la case à cocher.
     * 
     * @param g2 le contexte graphique 2D
     * @param x la position X de la case
     * @param y la position Y de la case
     * @param size la taille de la case
     * @param color la couleur du checkmark
     */
    private void drawCheckmark(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        int padding = size / 4;
        int startX = x + padding;
        int startY = y + size / 2;
        int midX = x + size / 2;
        int midY = y + size - padding;
        int endX = x + size - padding;
        int endY = y + padding;
        
        g2.drawLine(startX, startY, midX, midY);
        g2.drawLine(midX, midY, endX, endY);
    }
    
    private void startHoverAnimation(boolean toHover) {
        animatingToHover = toHover;
        animationStartTime = System.currentTimeMillis();
        if (!hoverTimer.isRunning()) {
            hoverTimer.start();
        }
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        HCheckBox checkBox = (HCheckBox) c;
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int checkSize = checkBox.getCheckmarkSize();
        int gap = checkBox.getGap();
        String text = checkBox.getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int vAlign = checkBox.getVerticalAlignment();
        
        if (vAlign == SwingConstants.NORTH || vAlign == SwingConstants.EAST) {
            // Pour NORTH/EAST : largeur = max(case, texte), hauteur = case + texte + gap
            int width = Math.max(checkSize, textWidth) + 5;
            int height = checkSize + textHeight + gap + 5;
            return new Dimension(width, height);
        } else {
            // Pour TOP/CENTER/BOTTOM : largeur = case + gap + texte, hauteur = max(case, texte)
            int width = checkSize + gap + textWidth + 5;
            int height = Math.max(checkSize, textHeight);
            return new Dimension(width, height);
        }
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null && hoverTimer.isRunning()) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }
}