package rubban;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Interface pour le rendu des valeurs dans un Ribbon.
 * Similaire à TableCellRenderer pour JTable.
 * 
 * Responsabilité : Convertir n'importe quel Object Java en Component Swing
 * pour l'affichage dans le ruban.
 */
public interface GroupRenderer {
    
    /**
     * Retourne le Component pour afficher une valeur dans le ruban.
     * 
     * @param ribbon le ruban qui demande le rendu
     * @param value la valeur à afficher (peut être n'importe quel Object)
     * @param groupIndex l'index du groupe contenant la valeur
     * @param position la position de la valeur dans le groupe
     * @param isSelected true si la valeur est sélectionnée
     * @param hasFocus true si la valeur a le focus
     * @return un Component Swing pour afficher la valeur
     */
    Component getGroupComponent(Ribbon ribbon, Object value,
                               int groupIndex, int position,
                               boolean isSelected, boolean hasFocus);
    
    /**
     * Méthode pour préparer le renderer avant utilisation.
     * Peut être utilisée pour réinitialiser l'état ou configurer des propriétés.
     * 
     * @param ribbon le ruban
     * @param groupIndex l'index du groupe
     * @param position la position dans le groupe
     */
    default void prepareRenderer(Ribbon ribbon, int groupIndex, int position) {
        // Implémentation par défaut vide
    }
    
    /**
 * Retourne le Component pour afficher un header de groupe.
 * 
 * @param ribbon le ruban qui demande le rendu
 * @param headerValue la valeur du header à afficher (peut être n'importe quel Object)
 * @param groupIndex l'index du groupe
 * @param isSelected true si le groupe est sélectionné
 * @return un Component Swing pour afficher le header
 */
default Component getHeaderComponent(Ribbon ribbon, Object headerValue,
                                     int groupIndex, boolean isSelected) {
    // Implémentation par défaut simple
    JLabel label = new JLabel();
    if (headerValue != null) {
        label.setText(headerValue.toString());
    } else if (ribbon.getModel() != null) {
        // utiliser l'identifiant du groupe
        Object groupId = ribbon.getModel().getGroupIdentifier(groupIndex);
        if (groupId != null) {
            label.setText(groupId.toString());
        }
    }
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(new Color(240, 240, 240));
    label.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
        BorderFactory.createEmptyBorder(2, 4, 2, 4)
    ));
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    return label;
}
    
}