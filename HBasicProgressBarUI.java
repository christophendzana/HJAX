/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HProgressBar;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 * Interface utilisateur moderne pour HProgressBar.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicProgressBarUI extends BasicProgressBarUI {
    
    private HProgressBar hProgressBar;
    private float animationOffset = 0f;
    private Timer animationTimer;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (c instanceof HProgressBar) {
            hProgressBar = (HProgressBar) c;
        }
        
        // Animation pour les rayures
        animationTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (hProgressBar != null && hProgressBar.isAnimated()) {
                    animationOffset += 0.5f;
                    if (animationOffset > 20) animationOffset = 0;
                    c.repaint();
                }
            }
        });
        animationTimer.start();
    }
    
    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Insets insets = progressBar.getInsets();
        int width = progressBar.getWidth() - insets.left - insets.right;
        int height = progressBar.getHeight() - insets.top - insets.bottom;
        
        if (hProgressBar == null || hProgressBar.getProgressStyle() == null) {
            super.paintDeterminate(g, c);
            g2.dispose();
            return;
        }
        
        HProgressBarStyle style = hProgressBar.getProgressStyle();
        int radius = hProgressBar.getCornerRadius();
        
        // Dessiner le fond
        g2.setColor(style.getBackgroundColor());
        RoundRectangle2D background = new RoundRectangle2D.Float(
            insets.left, insets.top, width, height, radius, radius
        );
        g2.fill(background);
        
        // Calculer la largeur de progression
        int amountFull = getAmountFull(insets, width, height);
        
        if (amountFull > 0) {
            // Dégradé de progression
            GradientPaint gradient = new GradientPaint(
                insets.left, insets.top, style.getProgressColor(),
                insets.left + amountFull, insets.top, style.getProgressEndColor()
            );
            g2.setPaint(gradient);
            
            RoundRectangle2D progress = new RoundRectangle2D.Float(
                insets.left, insets.top, amountFull, height, radius, radius
            );
            g2.fill(progress);
            
            // Rayures si activées
            if (hProgressBar.isStriped()) {
                paintStripes(g2, insets.left, insets.top, amountFull, height, radius);
            }
        }
        
        // Texte du pourcentage
        if (hProgressBar.isShowPercentage() && progressBar.isStringPainted()) {
            paintString(g2, insets.left, insets.top, width, height, amountFull, insets);
        }
        
        g2.dispose();
    }
    
    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Insets insets = progressBar.getInsets();
        int width = progressBar.getWidth() - insets.left - insets.right;
        int height = progressBar.getHeight() - insets.top - insets.bottom;
        
        if (hProgressBar == null || hProgressBar.getProgressStyle() == null) {
            super.paintIndeterminate(g, c);
            g2.dispose();
            return;
        }
        
        HProgressBarStyle style = hProgressBar.getProgressStyle();
        int radius = hProgressBar.getCornerRadius();
        
        // Fond
        g2.setColor(style.getBackgroundColor());
        RoundRectangle2D background = new RoundRectangle2D.Float(
            insets.left, insets.top, width, height, radius, radius
        );
        g2.fill(background);
        
        // Animation indéterminée
        Rectangle box = getBox(null);
        if (box != null) {
            g2.setColor(style.getProgressColor());
            RoundRectangle2D progressBox = new RoundRectangle2D.Float(
                box.x, box.y, box.width, box.height, radius, radius
            );
            g2.fill(progressBox);
        }
        
        g2.dispose();
    }
    
    private void paintStripes(Graphics2D g2, int x, int y, int width, int height, int radius) {
        g2.setClip(new RoundRectangle2D.Float(x, y, width, height, radius, radius));
        
        g2.setColor(new Color(255, 255, 255, 30));
        int stripeWidth = 20;
        
        for (int i = (int) (-stripeWidth + animationOffset); i < width + height; i += stripeWidth) {
            Polygon stripe = new Polygon();
            stripe.addPoint(x + i, y);
            stripe.addPoint(x + i + stripeWidth / 2, y);
            stripe.addPoint(x + i - height + stripeWidth / 2, y + height);
            stripe.addPoint(x + i - height, y + height);
            g2.fill(stripe);
        }
        
        g2.setClip(null);
    }
    
    @Override
    protected void paintString(Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
        if (hProgressBar == null || !hProgressBar.isShowPercentage()) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        String progressString = progressBar.getString();
        g2.setFont(progressBar.getFont());
        
        Point renderLocation = getStringPlacement(g2, progressString, x, y, width, height);
        
        if (hProgressBar.getProgressStyle() != null) {
            g2.setColor(hProgressBar.getProgressStyle().getTextColor());
        }
        
        // Ombre du texte
        g2.setColor(new Color(0, 0, 0, 50));
        g2.drawString(progressString, renderLocation.x + 1, renderLocation.y + 1);
        
        // Texte principal
        if (hProgressBar.getProgressStyle() != null) {
            g2.setColor(hProgressBar.getProgressStyle().getTextColor());
        }
        g2.drawString(progressString, renderLocation.x, renderLocation.y);
        
        g2.dispose();
    }
    
    @Override
    protected int getBoxLength(int availableLength, int otherDimension) {
        return (int) Math.round(availableLength / 6.0);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        super.uninstallUI(c);
    }
}