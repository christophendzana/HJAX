/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.models.HListModel;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicListUI;

/**
 *
 * @author FIDELE
 */
public class HListUI extends BasicListUI {
    
 private final HListTheme theme;
    private HListModel<?> model;

    public HListUI(HListTheme theme) {
        this.theme = theme;
    }
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        JList<?> list = (JList<?>) c;
        
        // Récupérer le modèle HListModel
        if (list.getModel() instanceof HListModel) {
            this.model = (HListModel<?>) list.getModel();
        }
        
        // Configuration de l'apparence via le thème
        list.setBackground(theme.getNormalBackground());
        list.setForeground(theme.getNormalForeground());
        list.setBorder(new RoundedListBorder());
        list.setCellRenderer(new ModernListCellRenderer());
        
        // Transparent pour voir le fond arrondi
        list.setOpaque(false);
    }

    /**
     * Redessiner le fond arrondi de la liste
     * @param g
     * @param c
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        // Dessiner le fond arrondi de la liste
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = c.getWidth();
        int height = c.getHeight();
        
        // Fond principal arrondi
        RoundRectangle2D backgroundRect = new RoundRectangle2D.Float(
            1, 1, width - 2, height - 2, 
            theme.getBorderRadius(), theme.getBorderRadius()
        );
        
        g2.setColor(theme.getNormalBackground());
        g2.fill(backgroundRect);
        
        g2.dispose();
        
        // Appeler la peinture normale (items, etc.)
        super.paint(g, c);
    }

    /**
     * Renderer personnalisé pour les items
     */
    private class ModernListCellRenderer extends JLabel implements ListCellRenderer<Object> {
        
        private boolean isSelected;
        private boolean isHovered;
        
        public ModernListCellRenderer() {
            setOpaque(false); //Transparent pour voir le fond de la liste
            setHorizontalAlignment(LEFT);
        }
        
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            this.isSelected = isSelected;
            this.isHovered = (model != null && index == model.getHoveredIndex());
            
            setText(value == null ? "" : value.toString());
            setFont(list.getFont());
            
            // Calcul de la taille préférée
            FontMetrics fm = getFontMetrics(getFont());
            String text = getText();
            int textWidth = fm.stringWidth(text);
            int preferredWidth = textWidth + 32; // Padding
            
            setPreferredSize(new java.awt.Dimension(
                Math.max(preferredWidth, 120), 
                theme.getItemHeight()
            ));
            
            return this;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Fond pour hover
            if (isHovered && !isSelected) {
                RoundRectangle2D hoverRect = new RoundRectangle2D.Float(
                    4, 2, width - 8, height - 4, // Marge pour s'aligner avec le fond
                    theme.getItemRadius(), theme.getItemRadius()
                );
                g2.setColor(theme.getHoverBackground());
                g2.fill(hoverRect);
            }
            
            // Fond pour sélection
            if (isSelected) {
                RoundRectangle2D selectedRect = new RoundRectangle2D.Float(
                    4, 2, width - 8, height - 4, // Marge pour s'aligner avec le fond
                    theme.getItemRadius(), theme.getItemRadius()
                );
                g2.setColor(theme.getSelectionBackground());
                g2.fill(selectedRect);
            }
            
            // Texte
            g2.setFont(getFont());
            g2.setColor(isSelected ? theme.getSelectionForeground() : theme.getNormalForeground());
            
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            if (text != null) {
                int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, 16, textY); // Ajustement du padding
            }
            
            g2.dispose();
        }
    }
    
    /**
     * Bordure arrondie pour la liste
     */
    private class RoundedListBorder implements Border {
        private final Insets insets;
        
        public RoundedListBorder() {
            this.insets = new Insets(
                theme.getItemMargin(), 
                theme.getItemMargin(), 
                theme.getItemMargin(), 
                theme.getItemMargin()
            );
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(theme.getBorderColor());
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 
                           theme.getBorderRadius(), theme.getBorderRadius());
            
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
    
}
