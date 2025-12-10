/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HMenuItem;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;

/**
 * Interface utilisateur moderne pour HMenuItem avec animations fluides,
 * bordures arrondies, chevrons pour sous-menus et raccourcis clavier.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicMenuItemUI extends BasicMenuItemUI {
    
    private HMenuItem hMenuItem;
    private float hoverProgress = 0f;
    private Timer hoverTimer;
    private boolean isHovering = false;
    
    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    private static final int ICON_TEXT_GAP = 8;
    private static final int PADDING_LEFT = 8;
    private static final int PADDING_RIGHT = 8;
    private static final int PADDING_TOP = 6;
    private static final int PADDING_BOTTOM = 6;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (c instanceof HMenuItem) {
            hMenuItem = (HMenuItem) c;
        }
        
        c.setOpaque(false);
        
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovering = true;
                animateHover(true, c);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovering = false;
                animateHover(false, c);
            }
        });
    }
    
    private void animateHover(boolean in, JComponent c) {
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        
        float startProgress = hoverProgress;
        long startTime = System.currentTimeMillis();
        
        hoverTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                hoverProgress = in ? (startProgress + (1f - startProgress) * progress)
                                   : (startProgress - startProgress * progress);
                
                c.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        hoverTimer.start();
    }
    
    @Override
    protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon,
                                 Color background, Color foreground, int defaultTextIconGap) {
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        JMenuItem menuItem = (JMenuItem) c;
        ButtonModel model = menuItem.getModel();
        
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = (hMenuItem != null) ? hMenuItem.getCornerRadius() : 8;
        
        // Dessiner le fond avec animation
        paintBackground(g2, menuItem, width, height, radius, model);
        
        // Dessiner l'icône
        Icon icon = menuItem.getIcon();
        int x = PADDING_LEFT;
        int y = (height - (icon != null ? icon.getIconHeight() : 16)) / 2;
        
        if (icon != null) {
            icon.paintIcon(c, g2, x, y);
            x += icon.getIconWidth() + ICON_TEXT_GAP;
        } else {
            x += 24 + ICON_TEXT_GAP; // Espace réservé pour l'icône
        }
        
        // Dessiner le texte
        String text = menuItem.getText();
        if (text != null && !text.isEmpty()) {
            g2.setFont(menuItem.getFont());
            
            Color textColor = foreground;
            if (hMenuItem != null && hMenuItem.getMenuStyle() != null) {
                // Si hover, utiliser du blanc, sinon la couleur normale
                if (model.isArmed() || model.isSelected()) {
                    textColor = Color.WHITE;
                } else {
                    textColor = hMenuItem.getMenuStyle().getItemTextColor();
                }
            }
            
            if (!model.isEnabled()) {
                textColor = new Color(textColor.getRed(), textColor.getGreen(), 
                                     textColor.getBlue(), 100);
            }
            
            if (hMenuItem != null ) {                        
            g2.setColor(textColor);
            g2.setFont(new Font("Segeo UI", Font.PLAIN, hMenuItem.getFontMenuItemSize()));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, x, textY);
            }                       
            
        }
        
        // Dessiner le raccourci clavier 
        KeyStroke accelerator = menuItem.getAccelerator();
        if (accelerator != null) {
            String accText = getAcceleratorText(accelerator);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
            int accWidth = fm.stringWidth(accText);
            int accX = width - accWidth - PADDING_RIGHT - 24; // 24 pour le chevron
            int accY = (height + fm.getAscent() - fm.getDescent()) / 2;
            
            Color accColor;
            if (model.isArmed() || model.isSelected()) {
                accColor = new Color(255, 255, 255, 180); // Blanc semi-transparent
            } else {
                accColor = new Color(128, 128, 128);
            }
            g2.setColor(accColor);
            g2.drawString(accText, accX, accY);
        }
        
        // Dessiner le chevron pour les sous-menus
        if (menuItem instanceof JMenu) {
            paintChevron(g2, width, height);
        }
        
        g2.dispose();
    }
    
    private void paintBackground(Graphics2D g2, JMenuItem menuItem, int width, 
                                int height, int radius, ButtonModel model) {
        
        Color bgColor = null;
        
        if (hMenuItem != null && hMenuItem.getMenuStyle() != null) {
            HMenuStyle style = hMenuItem.getMenuStyle();
            
            if (model.isArmed() || model.isSelected()) {
                Color baseColor = style.getBaseColor();
                Color hoverColor = style.getHoverColor();
                bgColor = interpolateColor(baseColor, hoverColor, hoverProgress);
            } else {
                Color transparent = new Color(style.getBaseColor().getRed(), 
                                             style.getBaseColor().getGreen(), 
                                             style.getBaseColor().getBlue(), 0);
                Color baseColor = new Color(style.getBaseColor().getRed(), 
                                           style.getBaseColor().getGreen(), 
                                           style.getBaseColor().getBlue(), 50);
                bgColor = interpolateColor(transparent, baseColor, hoverProgress);
            }
        }
        
        if (bgColor != null) {
            g2.setColor(bgColor);
            RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                4, 2, width - 8, height - 4, radius, radius
            );
            g2.fill(roundRect);
        }
    }
    
    private void paintChevron(Graphics2D g2, int width, int height) {
        int chevronSize = 6;
        int x = width - PADDING_RIGHT - 8;
        int y = height / 2;
        
        // Créer le chevron (triangle pointant vers la droite)
        Path2D chevron = new Path2D.Float();
        chevron.moveTo(-chevronSize/2, -chevronSize/2);
        chevron.lineTo(chevronSize/2, 0);
        chevron.lineTo(-chevronSize/2, chevronSize/2);
        
        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y);
        
        // Utiliser blanc si hover, sinon couleur normale
        Color chevronColor = Color.GRAY;
        if (hMenuItem != null && hMenuItem.getMenuStyle() != null) {
            ButtonModel model = ((JMenuItem) hMenuItem).getModel();
            if (model.isArmed() || model.isSelected()) {
                chevronColor = Color.WHITE;
            } else {
                chevronColor = hMenuItem.getMenuStyle().getItemTextColor();
            }
        }
        
        g2.setColor(chevronColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(chevron);
        
        g2.setTransform(oldTransform);
    }
    
    private String getAcceleratorText(KeyStroke accelerator) {
        StringBuilder sb = new StringBuilder();
        
        int modifiers = accelerator.getModifiers();
        if (modifiers > 0) {
            sb.append(KeyEvent.getKeyModifiersText(modifiers));
            sb.append("+");
        }
        
        int keyCode = accelerator.getKeyCode();
        if (keyCode != 0) {
            sb.append(KeyEvent.getKeyText(keyCode));
        } else {
            sb.append(accelerator.getKeyChar());
        }
        
        return sb.toString();
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        return new Dimension(Math.max(200, d.width), d.height + PADDING_TOP + PADDING_BOTTOM);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }
}