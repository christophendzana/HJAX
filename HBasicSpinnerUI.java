/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HSpinner;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;

/**
 * Interface utilisateur moderne pour HSpinner.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicSpinnerUI extends BasicSpinnerUI {
    
    private HSpinner hSpinner;
    private boolean isFocused = false;
    private float focusProgress = 0f;
    private Timer focusTimer;
    
    private static final int ANIMATION_DURATION = 200;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (c instanceof HSpinner) {
            hSpinner = (HSpinner) c;
        }
        
        c.setOpaque(false);
        
        // Configurer l'éditeur
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setOpaque(false);
            textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if (hSpinner != null && hSpinner.getSpinnerStyle() != null) {
                textField.setForeground(hSpinner.getSpinnerStyle().getTextColor());
            }
            
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    animateFocus(true);
                }
                
                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    animateFocus(false);
                }
            });
        }
    }
    
    private void animateFocus(boolean focusing) {
        if (hSpinner == null || !hSpinner.isAnimationsEnabled()) {
            focusProgress = focusing ? 1f : 0f;
            hSpinner.repaint();
            return;
        }
        
        if (focusTimer != null) {
            focusTimer.stop();
        }
        
        float startProgress = focusProgress;
        long startTime = System.currentTimeMillis();
        
        focusTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                focusProgress = focusing ? (startProgress + (1f - startProgress) * progress)
                                         : (startProgress - startProgress * progress);
                
                hSpinner.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        focusTimer.start();
    }
    
    @Override
    protected Component createNextButton() {
        return createButton(true);
    }
    
    @Override
    protected Component createPreviousButton() {
        return createButton(false);
    }
    
    private Component createButton(boolean isNext) {
        JButton button = new JButton() {
            private boolean isHovered = false;
            
            {
                setOpaque(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setPreferredSize(new Dimension(25, 20));
                
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (hSpinner == null || hSpinner.getSpinnerStyle() == null) {
                    super.paintComponent(g);
                    g2.dispose();
                    return;
                }
                
                HSpinnerStyle style = hSpinner.getSpinnerStyle();
                int w = getWidth();
                int h = getHeight();
                
                // Fond du bouton
                Color bgColor = isHovered ? style.getButtonHoverColor() : style.getButtonColor();
                g2.setColor(bgColor);
                
                if (isNext) {
                    g2.fillRoundRect(0, 0, w, h, 0, hSpinner.getCornerRadius());
                } else {
                    g2.fillRoundRect(0, 0, w, h, 0, hSpinner.getCornerRadius());
                }
                
                // Flèche
                g2.setColor(style.getButtonIconColor());
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int cx = w / 2;
                int cy = h / 2;
                int arrowSize = 4;
                
                if (isNext) {
                    // Flèche vers le haut
                    g2.drawLine(cx - arrowSize, cy + 1, cx, cy - arrowSize + 1);
                    g2.drawLine(cx, cy - arrowSize + 1, cx + arrowSize, cy + 1);
                } else {
                    // Flèche vers le bas
                    g2.drawLine(cx - arrowSize, cy - 1, cx, cy + arrowSize - 1);
                    g2.drawLine(cx, cy + arrowSize - 1, cx + arrowSize, cy - 1);
                }
                
                g2.dispose();
            }
        };
        
        if (isNext) {
            installNextButtonListeners(button);
        } else {
            installPreviousButtonListeners(button);
        }
        
        return button;
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (hSpinner == null || hSpinner.getSpinnerStyle() == null) {
            super.paint(g, c);
            g2.dispose();
            return;
        }
        
        HSpinnerStyle style = hSpinner.getSpinnerStyle();
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = hSpinner.getCornerRadius();
        
        // Fond
        Color bgColor = interpolateColor(style.getBackground(), style.getFocusBackground(), focusProgress);
        g2.setColor(bgColor);
        RoundRectangle2D background = new RoundRectangle2D.Float(
            2, 2, width - 4, height - 4, radius, radius
        );
        g2.fill(background);
        
        // Bordure
        Color borderColor = interpolateColor(style.getBorderColor(), style.getFocusBorderColor(), focusProgress);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.5f + focusProgress));
        g2.draw(background);
        
        g2.dispose();
        
        super.paint(g, c);
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (focusTimer != null) {
            focusTimer.stop();
        }
        super.uninstallUI(c);
    }
}