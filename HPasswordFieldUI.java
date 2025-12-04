/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HPasswordField;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicPasswordFieldUI;

/**
 * Interface utilisateur pour HPasswordField avec bouton toggle visibilité.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HPasswordFieldUI extends BasicPasswordFieldUI {
    
    private Rectangle toggleButtonBounds;
    private boolean toggleButtonHovered = false;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof HPasswordField) {
            HPasswordField passwordField = (HPasswordField) c;
            c.setOpaque(false);
            
            // Définir les marges internes (Insets)
            updateInsets(passwordField);
            
            // Ajouter le listener pour le bouton toggle
            setupMouseListener(passwordField);
        }
    }
    
    /**
     * Met à jour les insets du composant en fonction des paramètres.
     */
    private void updateInsets(HPasswordField passwordField) {
        int top = passwordField.getVerticalPadding();
        int left = passwordField.getHorizontalPadding();
        int bottom = passwordField.getVerticalPadding();
        int right = passwordField.getHorizontalPadding() + passwordField.getToggleButtonWidth();
        
        // Appliquer les marges via un EmptyBorder
        passwordField.setMargin(new Insets(top, left, bottom, right));
    }
    
    /**
     * Configure le listener de souris pour le bouton toggle.
     */
    private void setupMouseListener(HPasswordField passwordField) {
        passwordField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (toggleButtonBounds != null && toggleButtonBounds.contains(e.getPoint())) {
                    passwordField.togglePasswordVisibility();
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (toggleButtonHovered) {
                    toggleButtonHovered = false;
                    passwordField.repaint();
                }
            }
        });
        
        passwordField.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasHovered = toggleButtonHovered;
                toggleButtonHovered = toggleButtonBounds != null && toggleButtonBounds.contains(e.getPoint());
                
                // Changer le curseur
                if (toggleButtonHovered) {
                    passwordField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    passwordField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
                
                if (wasHovered != toggleButtonHovered) {
                    passwordField.repaint();
                }
            }
        });
    }
    
    @Override
    protected void paintSafely(Graphics g) {
        HPasswordField passwordField = (HPasswordField) getComponent();
        updateInsets(passwordField);
        
        paintBackground(g);
        super.paintSafely(g);
        
        // Le bouton toggle est maintenant dessiné dans paintBackground()
    }
    
    @Override
    protected void paintBackground(Graphics g) {
        HPasswordField passwordField = (HPasswordField) getComponent();
        if (passwordField == null) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getComponent().getWidth();
        int height = getComponent().getHeight();
        int radius = passwordField.getCornerRadius();
        
        // === OMBRE ===
        if (passwordField.isShowShadow() && passwordField.getShadow() != null) {
            Graphics2D shadowG2 = (Graphics2D) g2.create();
            passwordField.getShadow().paint(shadowG2, getComponent(), width, height, radius);
            shadowG2.dispose();
        }
        
        // === FOND ===
        HButtonStyle style = passwordField.getButtonStyle();
        Color backgroundColor = style.getBaseColor();
        
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        
        // === BORDURE ===
        Color borderColor;
        float borderThickness;
        
        if (passwordField.hasFocus()) {
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
        if (passwordField.getHBorder() != null) {
            Graphics2D borderG2 = (Graphics2D) g2.create();
            passwordField.getHBorder().paint(borderG2, passwordField, width, height, radius);
            borderG2.dispose();
        }
        
        // === BOUTON TOGGLE (AJOUTÉ ICI) ===
        paintToggleButton(g2, passwordField);
        
        g2.dispose();
    }
    
    /**
     * Dessine le bouton toggle visibilité (œil).
     */
    private void paintToggleButton(Graphics g, HPasswordField passwordField) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = passwordField.getWidth();
        int height = passwordField.getHeight();
        int buttonSize = 24;
        int buttonX = width - passwordField.getHorizontalPadding() - buttonSize - 8;
        int buttonY = (height - buttonSize) / 2;
        
        // Mettre à jour les bounds du bouton pour la détection de clic
        toggleButtonBounds = new Rectangle(buttonX, buttonY, buttonSize, buttonSize);
        
        // Couleur du bouton
        Color buttonColor = toggleButtonHovered ? 
                passwordField.getButtonStyle().getHoverColor() : 
                new Color(100, 100, 100);
        
        g2.setColor(buttonColor);
        
        // Dessiner l'icône œil
        if (passwordField.isPasswordVisible()) {
            // Œil ouvert (mot de passe visible)
            drawEyeOpen(g2, buttonX, buttonY, buttonSize);
        } else {
            // Œil fermé/barré (mot de passe masqué)
            drawEyeClosed(g2, buttonX, buttonY, buttonSize);
        }
        
        g2.dispose();
    }
    
    /**
     * Dessine une icône d'œil ouvert.
     */
    private void drawEyeOpen(Graphics2D g2, int x, int y, int size) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Contour de l'œil (ellipse)
        g2.drawArc(x + 2, y + size / 2 - 4, size - 4, 8, 0, 180);
        g2.drawArc(x + 2, y + size / 2 - 4, size - 4, 8, 180, 180);
        
        // Pupille (cercle au centre)
        g2.fillOval(centerX - 3, centerY - 3, 6, 6);
    }
    
    /**
     * Dessine une icône d'œil fermé/barré.
     */
    private void drawEyeClosed(Graphics2D g2, int x, int y, int size) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Contour de l'œil (ellipse)
        g2.drawArc(x + 2, y + size / 2 - 4, size - 4, 8, 0, 180);
        g2.drawArc(x + 2, y + size / 2 - 4, size - 4, 8, 180, 180);
        
        // Pupille
        g2.fillOval(centerX - 3, centerY - 3, 6, 6);
        
        // Barre diagonale pour indiquer "fermé"
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(x + 4, y + size - 4, x + size - 4, y + 4);
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        HPasswordField passwordField = (HPasswordField) c;
        Dimension preferred = super.getPreferredSize(c);
        
        // Ajouter le padding interne
        preferred.width += passwordField.getHorizontalPadding() * 2;
        preferred.height += passwordField.getVerticalPadding() * 2;
        
        // Ajouter l'espace pour le bouton toggle
        preferred.width += passwordField.getToggleButtonWidth();
        
        // Hauteur minimale confortable
        int minHeight = passwordField.getFont().getSize() + passwordField.getVerticalPadding() * 2;
        preferred.height = Math.max(preferred.height, minHeight);
        
        return preferred;
    }
    
    @Override
    public Dimension getMinimumSize(JComponent c) {
        HPasswordField passwordField = (HPasswordField) c;
        Dimension min = super.getMinimumSize(c);
        
        // Largeur minimale : padding + espace bouton
        int minWidth = 100 + passwordField.getHorizontalPadding() * 2 + passwordField.getToggleButtonWidth();
        min.width = Math.max(min.width, minWidth);
        
        // Hauteur minimale
        int minHeight = passwordField.getFont().getSize() + passwordField.getVerticalPadding() * 2;
        min.height = Math.max(min.height, minHeight);
        
        return min;
    }
    
    @Override
    protected Rectangle getVisibleEditorRect() {
        Rectangle r = super.getVisibleEditorRect();
        
        if (r != null) {
            HPasswordField passwordField = (HPasswordField) getComponent();
            
            // Appliquer les marges
            r.x += passwordField.getHorizontalPadding();
            r.y += passwordField.getVerticalPadding();
            r.width -= passwordField.getHorizontalPadding() * 2;
            r.height -= passwordField.getVerticalPadding() * 2;
            
            // Réduire la largeur pour le bouton toggle
            r.width -= passwordField.getToggleButtonWidth();
            
            // S'assurer que la zone reste valide
            r.width = Math.max(0, r.width);
            r.height = Math.max(0, r.height);
        }
        
        return r;
    }
}