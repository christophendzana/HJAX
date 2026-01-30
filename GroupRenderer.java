package hcomponents.HRibbon;

import java.awt.Component;

/**
 * Interface pour le rendu des valeurs dans un HRibbon.
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
    Component getGroupComponent(HRibbon ribbon, Object value,
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
    default void prepareRenderer(HRibbon ribbon, int groupIndex, int position) {
        // Implémentation par défaut vide
    }
}