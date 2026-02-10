package rubban.layout;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import rubban.GroupRenderer;
import rubban.HRibbonGroup;
import rubban.HRibbonModel;
import rubban.Ribbon;

/**
 * Renderer pour les groupes collapsed.
 * Crée et configure les JComboBox utilisés pour représenter les groupes réduits.
 * 
 * RESPONSABILITÉS :
 * - Créer le JComboBox avec le header et les composants du groupe
 * - Configurer le renderer personnalisé pour afficher les composants
 * - Gérer les événements de sélection
 * - Assurer une présentation cohérente
 * 
 * @author FIDELE
 * @version 1.0
 */
public class CollapsedGroupRenderer {
    
    /**
     * Crée un JComboBox représentant un groupe collapsed.
     * 
     * CONTENU DU COMBO :
     * 1. Header du groupe (non-sélectionnable, affiché en haut)
     * 2. Séparateur visuel
     * 3. Tous les composants du groupe (sélectionnables)
     * 
     * @param ribbon le ruban contenant le groupe
     * @param group le groupe à représenter
     * @param groupIndex l'index du groupe dans le modèle
     * @return JComboBox configuré
     */
    public JComboBox<Object> createCollapsedComboBox(
            Ribbon ribbon,
            HRibbonGroup group,
            int groupIndex) {
        
        if (ribbon == null || group == null) {
            throw new IllegalArgumentException("Ribbon and group cannot be null");
        }
        
        // Créer le combo
        JComboBox<Object> combo = new JComboBox<>();
        
        // 1. Ajouter le header comme premier élément (sera affiché quand collapsed)
        Object headerValue = group.getHeaderValue();
        if (headerValue == null) {
            headerValue = group.getGroupIdentifier();
        }
        if (headerValue == null) {
            headerValue = "Groupe " + groupIndex;
        }
        combo.addItem(headerValue);
        
        // 2. Ajouter un séparateur visuel
        combo.addItem("────────");
        
        // 3. Ajouter les composants du groupe
        HRibbonModel model = ribbon.getModel();
        if (model != null) {
            int valueCount = model.getValueCount(groupIndex);
            
            for (int i = 0; i < valueCount; i++) {
                Object value = model.getValueAt(i, groupIndex);
                if (value != null) {
                    combo.addItem(value);
                }
            }
        }
        
        // 4. Configurer le renderer personnalisé
        combo.setRenderer(new CollapsedComboRenderer(ribbon, group, groupIndex));
        
        // 5. Sélectionner le header par défaut (index 0)
        combo.setSelectedIndex(0);
        
        // 6. Configurer la taille préférée
        combo.setPreferredSize(new Dimension(group.getCollapsedWidth(), 25));
        combo.setMaximumSize(new Dimension(group.getCollapsedWidth(), 25));
        
        // 7. Ajouter le listener pour gérer les sélections
        combo.addActionListener(e -> {
            int selectedIndex = combo.getSelectedIndex();
            
            // Ignorer le header (index 0) et le séparateur (index 1)
            if (selectedIndex <= 1) {
                combo.setSelectedIndex(0); // Remettre sur le header
                return;
            }
            
            // Récupérer l'élément sélectionné
            Object selected = combo.getSelectedItem();
            
            if (selected != null) {
                // Si c'est un Component avec action (JButton, etc.)
                if (selected instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) selected;
                    // Simuler un clic sur le bouton
                    button.doClick();
                }
                // Pour les autres composants, on pourrait ajouter d'autres logiques
                // Par exemple, déclencher un événement personnalisé
            }
            
            // Remettre le combo sur le header après l'action
            combo.setSelectedIndex(0);
        });
        
        return combo;
    }
    
    /**
     * Renderer personnalisé pour les éléments du JComboBox.
     * 
     * RESPONSABILITÉS :
     * - Afficher le header en gras
     * - Afficher le séparateur visuellement
     * - Afficher les composants de manière lisible
     */
    private static class CollapsedComboRenderer extends DefaultListCellRenderer {
        
        private final Ribbon ribbon;
        private final HRibbonGroup group;
        private final int groupIndex;
        
        public CollapsedComboRenderer(Ribbon ribbon, HRibbonGroup group, int groupIndex) {
            this.ribbon = ribbon;
            this.group = group;
            this.groupIndex = groupIndex;
        }
        
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            // Cas 1 : Header (index 0)
            if (index == 0) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                );
                label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
                return label;
            }
            
            // Cas 2 : Séparateur (index 1)
            if (index == 1) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                );
                label.setEnabled(false);
                return label;
            }
            
            // Cas 3 : Composant du groupe
            if (value instanceof Component) {
                Component comp = (Component) value;
                
                // Si c'est un composant Swing, essayer d'extraire du texte
                if (comp instanceof JLabel) {
                    value = ((JLabel) comp).getText();
                } else if (comp instanceof AbstractButton) {
                    value = ((AbstractButton) comp).getText();
                } else {
                    value = comp.getClass().getSimpleName();
                }
            }
            
            // Rendu par défaut
            return super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
            );
        }
    }
}
