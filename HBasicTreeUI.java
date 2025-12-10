/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HTree;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;

/**
 * Interface utilisateur moderne pour HTree avec animations fluides.
 * 
 * @author FIDELE
 * @version 2.0
 */
public class HBasicTreeUI extends BasicTreeUI {
    
    private HTree hTree;
    
    // Gestion des animations hover
    private TreePath hoveredPath = null;
    private Map<TreePath, Float> hoverProgress = new HashMap<>();
    private Map<TreePath, Timer> hoverTimers = new HashMap<>();
    
    // Gestion des animations d'expansion
    private Map<TreePath, Float> expansionProgress = new HashMap<>();
    private Map<TreePath, Timer> expansionTimers = new HashMap<>();
    
    // Gestion de la rotation des chevrons
    private Map<TreePath, Float> chevronRotation = new HashMap<>();
    
    private static final int ANIMATION_DURATION = 250;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (c instanceof HTree) {
            hTree = (HTree) c;
        }
        
        // Configurer le tree
        if (tree != null) {
            tree.setRowHeight(0); // Hauteur dynamique
            tree.setCellRenderer(new ModernTreeCellRenderer());
            tree.setOpaque(false);
        }
        
        // Listener pour le hover
        c.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e, c);
            }
        });
        
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredPath != null) {
                    animateHover(hoveredPath, false, c);
                    hoveredPath = null;
                }
            }
        });
        
        // Listener pour les expansions avec animation
        if (tree != null) {
            tree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
                @Override
                public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
                    animateExpansion(event.getPath(), true, c);
                }
                
                @Override
                public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {
                    animateExpansion(event.getPath(), false, c);
                }
            });
        }
    }
    
    private void handleMouseMoved(MouseEvent e, JComponent c) {
        if (hTree == null || !hTree.isHoverEnabled()) return;
        
        if (c instanceof JTree) {
            JTree jtree = (JTree) c;
            TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
            
            if (path != hoveredPath) {
                if (hoveredPath != null) {
                    animateHover(hoveredPath, false, c);
                }
                hoveredPath = path;
                if (hoveredPath != null) {
                    animateHover(hoveredPath, true, c);
                }
            }
        }
    }
    
    private void animateHover(TreePath path, boolean in, JComponent c) {
        if (path == null || hTree == null || !hTree.isAnimationsEnabled()) {
            if (path != null) {
                hoverProgress.put(path, in ? 1f : 0f);
                repaintPath(path, c);
            }
            return;
        }
        
        Timer existing = hoverTimers.get(path);
        if (existing != null) existing.stop();
        
        float startProgress = hoverProgress.getOrDefault(path, in ? 0f : 1f);
        long startTime = System.currentTimeMillis();
        
        Timer timer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                float current = in ? (startProgress + (1f - startProgress) * progress)
                                   : (startProgress - startProgress * progress);
                
                hoverProgress.put(path, current);
                repaintPath(path, c);
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    hoverTimers.remove(path);
                }
            }
        });
        
        hoverTimers.put(path, timer);
        timer.start();
    }
    
    private void animateExpansion(TreePath path, boolean expanding, JComponent c) {
        if (path == null || hTree == null || !hTree.isAnimationsEnabled()) {
            if (path != null) {
                chevronRotation.put(path, expanding ? 90f : 0f);
                repaintPath(path, c);
            }
            return;
        }
        
        Timer existing = expansionTimers.get(path);
        if (existing != null) existing.stop();
        
        float startRotation = chevronRotation.getOrDefault(path, expanding ? 0f : 90f);
        float targetRotation = expanding ? 90f : 0f;
        long startTime = System.currentTimeMillis();
        
        Timer timer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Easing out cubic pour une animation plus naturelle
                float eased = 1f - (float) Math.pow(1f - progress, 3);
                float current = startRotation + (targetRotation - startRotation) * eased;
                
                chevronRotation.put(path, current);
                repaintPath(path, c);
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    expansionTimers.remove(path);
                }
            }
        });
        
        expansionTimers.put(path, timer);
        timer.start();
    }
    
    private void repaintPath(TreePath path, JComponent c) {
        if (path == null || !(c instanceof JTree)) return;
        JTree jtree = (JTree) c;
        Rectangle bounds = jtree.getPathBounds(path);
        if (bounds != null) {
            jtree.repaint(bounds);
        }
    }
    
    @Override
    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
        if (hTree != null && hTree.isShowConnectionLines()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color lineColor = hTree.getTreeStyle().getConnectionLineColor();
            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), 
                                 lineColor.getBlue(), 40)); // Très subtil
            g2.setStroke(new BasicStroke(1f));
            
            g2.drawLine(x, top, x, bottom);
            g2.dispose();
        }
    }
    
    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
        if (hTree != null && hTree.isShowConnectionLines()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color lineColor = hTree.getTreeStyle().getConnectionLineColor();
            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), 
                                 lineColor.getBlue(), 40)); // Très subtil
            g2.setStroke(new BasicStroke(1f));
            
            g2.drawLine(left, y, right, y);
            g2.dispose();
        }
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        // Nettoyer tous les timers
        for (Timer t : hoverTimers.values()) {
            if (t != null) t.stop();
        }
        for (Timer t : expansionTimers.values()) {
            if (t != null) t.stop();
        }
        hoverTimers.clear();
        expansionTimers.clear();
        hoverProgress.clear();
        expansionProgress.clear();
        chevronRotation.clear();
        
        super.uninstallUI(c);
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }
    
    /**
     * Renderer moderne pour les cellules de l'arbre.
     */
    private class ModernTreeCellRenderer extends JPanel implements TreeCellRenderer {
        
        private JLabel iconLabel;
        private JLabel textLabel;
        private TreePath currentPath;
        private boolean isSelected;
        private boolean isLeaf;
        private boolean isExpanded;
        
        public ModernTreeCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            setOpaque(false);
            
            iconLabel = new JLabel();
            textLabel = new JLabel();
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            add(iconLabel);
            add(textLabel);
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                     boolean sel, boolean expanded,
                                                     boolean leaf, int row, boolean hasFocus) {
            
            currentPath = tree.getPathForRow(row);
            isSelected = sel;
            isLeaf = leaf;
            isExpanded = expanded;
            
            // Définir le texte
            textLabel.setText(value.toString());
            
            // Définir la couleur du texte
            if (tree instanceof HTree) {
                HTree ht = (HTree) tree;
                if (ht.getTreeStyle() != null) {
                    textLabel.setForeground(ht.getTreeStyle().getTextColor());
                }
            } else {
                textLabel.setForeground(Color.BLACK);
            }
            
            // Ne pas afficher d'icône pour l'instant, on va dessiner le chevron directement
            iconLabel.setIcon(null);
            
            return this;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Récupérer HTree depuis le tree
            HTree ht = (tree instanceof HTree) ? (HTree) tree : null;
            int radius = (ht != null) ? ht.getCornerRadius() : 8;
            
            // Calculer la couleur de fond avec animation
            Color bgColor = null;
            if (ht != null && ht.getTreeStyle() != null) {
                HTreeStyle style = ht.getTreeStyle();
                
                if (isSelected) {
                    bgColor = style.getSelectedColor();
                } else {
                    Float progress = hoverProgress.getOrDefault(currentPath, 0f);
                    Color baseColor = isLeaf ? style.getChildNodeColor() : style.getParentNodeColor();
                    Color hoverColor = style.getHoverColor();
                    
                    // Rendre la couleur de base plus visible (alpha plus élevé)
                    Color subtleBase = new Color(baseColor.getRed(), baseColor.getGreen(), 
                                                baseColor.getBlue()); // Changé de 30 à 120
                    Color subtleHover = new Color(hoverColor.getRed(), hoverColor.getGreen(), 
                                                 hoverColor.getBlue()); // Changé de 100 à 180
                    
                    bgColor = interpolateColor(subtleBase, subtleHover, progress);
                }
            }
            
            // Dessiner le fond arrondi (pill shape)
            if (bgColor != null) {
                g2.setColor(bgColor);
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    4, 2, width - 8, height - 4, radius, radius
                );
                g2.fill(roundRect);
            }
            
            // Dessiner le chevron animé pour les nœuds non-feuilles
            if (!isLeaf) {
                drawAnimatedChevron(g2, ht);
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
        
        private void drawAnimatedChevron(Graphics2D g2, HTree ht) {
            int chevronSize = 8;
            int x = 10; // Position du chevron
            int y = getHeight() / 2;
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Récupérer l'angle de rotation
            Float rotation = chevronRotation.getOrDefault(currentPath, isExpanded ? 90f : 0f);
            
            // Créer le chevron (triangle pointant vers la droite)
            Path2D chevron = new Path2D.Float();
            chevron.moveTo(-chevronSize/2, -chevronSize/2);
            chevron.lineTo(chevronSize/2, 0);
            chevron.lineTo(-chevronSize/2, chevronSize/2);
            
            // Appliquer la rotation
            AffineTransform oldTransform = g2.getTransform();
            g2.translate(x, y);
            g2.rotate(Math.toRadians(rotation));
            
            // Dessiner le chevron
            if (ht != null && ht.getTreeStyle() != null) {
                g2.setColor(ht.getTreeStyle().getTextColor());
            } else {
                g2.setColor(Color.BLACK);
            }
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(chevron);
            
            g2.setTransform(oldTransform);
        }
    }
}