/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HTextField;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTextFieldUI;

/**
 * Interface utilisateur pour HTextField avec compteur interne à droite.
 * 
 * @author FIDELE
 * @version 2.1
 */
public class HTextFieldUI extends BasicTextFieldUI {
    
   @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof HTextField) {
            HTextField textField = (HTextField) c;
            c.setOpaque(false);
            
            // Définir les marges internes (Insets)
            updateInsets(textField);
        }
    }
    
    /**
     * Met à jour les insets du composant en fonction des paramètres.
     */
    private void updateInsets(HTextField textField) {
        int top = textField.getVerticalPadding();
        int left = textField.getHorizontalPadding();
        int bottom = textField.getVerticalPadding();
        int right = textField.getHorizontalPadding();
        
        // Réserver de l'espace à droite si le compteur est activé
        if (textField.getMaxCharacters() > 0) {
            right += textField.getCounterWidth();
        }
        
        // Appliquer les marges via un EmptyBorder
        textField.setMargin(new Insets(top, left, bottom, right));
    }
    
    @Override
    protected void paintSafely(Graphics g) {
        // Mettre à jour les insets au cas où les paramètres auraient changé
        HTextField textField = (HTextField) getComponent();
        updateInsets(textField);
        
        // Dessiner l'arrière-plan d'abord
        paintBackground(g);
        
        // Laisser Swing dessiner le texte et le caret
        super.paintSafely(g);
        
        // Dessiner le compteur par-dessus (si activé)
        
    }
    
    @Override
    protected void paintBackground(Graphics g) {
        HTextField textField = (HTextField) getComponent();
        if (textField == null) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getComponent().getWidth();
        int height = getComponent().getHeight();
        int radius = textField.getCornerRadius();
        
        // === OMBRE ===
        if (textField.isShowShadow() && textField.getShadow() != null) {
            Graphics2D shadowG2 = (Graphics2D) g2.create();
            textField.getShadow().paint(shadowG2, getComponent(), width, height, radius);
            shadowG2.dispose();
        }
        
        // === FOND ===
        HButtonStyle style = textField.getButtonStyle();
        Color backgroundColor = style.getBaseColor();
        
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        
        // === BORDURE ===
        Color borderColor;
        float borderThickness;
        
        if (textField.hasFocus()) {
            borderColor = style.getPressColor();
            borderThickness = 2.0f;
        } else {
            borderColor = style.getBaseColor().darker();
            borderThickness = 1.5f;
        }
        
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderThickness));
        g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
        
        // === BORDURE PERSONNALISÉE ===
        if (textField.getHBorder() != null) {
            Graphics2D borderG2 = (Graphics2D) g2.create();
            textField.getHBorder().paint(borderG2, textField, width, height, radius);
            borderG2.dispose();
        }
        if (textField.getMaxCharacters() > 0) {
            paintCharacterCounter(g, textField);
        }
        g2.dispose();
    }
    
    /**
     * Dessine le compteur de caractères à l'intérieur du champ, à droite.
     */
    private void paintCharacterCounter(Graphics g, HTextField textField) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        String text = textField.getText();
        int current = text.length();
        int max = textField.getMaxCharacters();
        
        // Couleur du compteur
        Color counterColor = (current > max) 
                ? textField.getCounterErrorColor() 
                : textField.getCounterColor();
        
        // Police du compteur
        Font counterFont = textField.getFont().deriveFont((float) textField.getCounterFontSize());
        g2.setFont(counterFont);
        FontMetrics fm = g2.getFontMetrics();
        
        // Texte du compteur
        String counterText = current + "/" + max;
        int textWidth = fm.stringWidth(counterText);
        int textHeight = fm.getHeight();
        
        // Position : en haut à droite avec marges
        int x = textField.getWidth() - textField.getHorizontalPadding() - textWidth - 5;
        int y = textField.getVerticalPadding() + fm.getAscent();
        
        // Dessiner le compteur
        g2.setColor(counterColor);
        g2.drawString(counterText, x, y);
        
        // === BARRE DE PROGRESSION ===
        float progress = (float) current / max;
        int barWidth = 40; // Largeur de la barre
        int barHeight = 3; // Hauteur de la barre
        int barX = textField.getWidth() - textField.getHorizontalPadding() - barWidth - 5;
        int barY = y + 4; // Juste en dessous du texte
        
        // Fond de la barre
        g2.setColor(new Color(220, 220, 220));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 2, 2);
        
        // Couleur de progression
        Color progressColor;
        if (progress > 1.0f) {
            progressColor = textField.getCounterErrorColor();
        } else if (progress > 0.8f) {
            progressColor = Color.RED; // Orange
        } else {
            progressColor = new Color(40, 167, 69); // Vert
        }
        
        // Barre de progression
        int filledWidth = (int) (barWidth * Math.min(progress, 1.0f));
        g2.setColor(progressColor);
        g2.fillRoundRect(barX, barY, filledWidth, barHeight, 2, 2);
        
        g2.dispose();
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        HTextField textField = (HTextField) c;
        Dimension preferred = super.getPreferredSize(c);
        
        // Ajouter le padding interne
        preferred.width += textField.getHorizontalPadding() * 2;
        preferred.height += textField.getVerticalPadding() * 2;
        
        // Ajouter l'espace pour le compteur à droite
        if (textField.getMaxCharacters() > 0) {
            preferred.width += textField.getCounterWidth();
        }
        
        // Hauteur minimale confortable
        int minHeight = textField.getFont().getSize() + textField.getVerticalPadding() * 2;
        preferred.height = Math.max(preferred.height, minHeight);
        
        return preferred;
    }
    
    @Override
    public Dimension getMinimumSize(JComponent c) {
        HTextField textField = (HTextField) c;
        Dimension min = super.getMinimumSize(c);
        
        // Largeur minimale : padding + espace compteur
        int minWidth = 100 + textField.getHorizontalPadding() * 2;
        if (textField.getMaxCharacters() > 0) {
            minWidth += textField.getCounterWidth();
        }
        
        min.width = Math.max(min.width, minWidth);
        
        // Hauteur minimale
        int minHeight = textField.getFont().getSize() + textField.getVerticalPadding() * 2;
        min.height = Math.max(min.height, minHeight);
        
        return min;
    }
    
    @Override
    protected Rectangle getVisibleEditorRect() {
        Rectangle r = super.getVisibleEditorRect();
        
        if (r != null) {
            HTextField textField = (HTextField) getComponent();
            
            // Appliquer les marges
            r.x += textField.getHorizontalPadding();
            r.y += textField.getVerticalPadding();
            r.width -= textField.getHorizontalPadding() * 2;
            r.height -= textField.getVerticalPadding() * 2;
            
            // Réduire la largeur si le compteur est activé
            if (textField.getMaxCharacters() > 0) {
                r.width -= textField.getCounterWidth();
            }
            
            // S'assurer que la zone reste valide
            r.width = Math.max(0, r.width);
            r.height = Math.max(0, r.height);
        }
        
        return r;
    }
}