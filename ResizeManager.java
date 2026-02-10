package rubban.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/**
 * Gestionnaire du redimensionnement adaptatif des groupes du ruban.
 * 
 * RESPONSABILITÉS :
 * - Calculer les largeurs minimales et optimales du ruban
 * - Déterminer quels groupes doivent être collapsed/expanded
 * - Appliquer les changements d'état aux groupes
 * - Fournir la logique de décision pour le redimensionnement
 * 
 * ALGORITHME :
 * - RÉDUCTION : De droite à gauche (comme Word)
 * - EXPANSION : De gauche à droite (comme Word)
 * 
 * @author FIDELE
 * @version 1.0
 */
public class ResizeManager {
    
    /**
     * Calcule la largeur minimale absolue du ruban.
     * C'est la largeur quand tous les groupes sont collapsed.
     * 
     * @param groupModel modèle des groupes
     * @param groupMargin marge entre les groupes
     * @return largeur minimale en pixels
     */
    public int calculateMinimumWidth(HRibbonGroupModel groupModel, int groupMargin) {
        if (groupModel == null) {
            return 0;
        }
        
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return 0;
        }
        
        int totalWidth = 0;
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                // Largeur collapsed
                totalWidth += group.getCollapsedWidth();
                
                // Ajouter la marge (sauf après le dernier groupe)
                if (i < groupCount - 1) {
                    totalWidth += groupMargin;
                }
            }
        }
        
        return totalWidth;
    }
    
    /**
     * Calcule la largeur actuelle du ruban selon les états des groupes.
     * 
     * @param groupModel modèle des groupes
     * @param groupMargin marge entre les groupes
     * @param normalWidths map des largeurs normales par index de groupe
     * @return largeur actuelle en pixels
     */
    public int calculateCurrentWidth(
            HRibbonGroupModel groupModel,
            int groupMargin,
            Map<Integer, Integer> normalWidths) {
        
        if (groupModel == null || normalWidths == null) {
            return 0;
        }
        
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return 0;
        }
        
        int totalWidth = 0;
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                // Utiliser la largeur selon l'état actuel
                if (group.isCollapsed()) {
                    totalWidth += group.getCollapsedWidth();
                } else {
                    Integer normalWidth = normalWidths.get(i);
                    totalWidth += (normalWidth != null) ? normalWidth : group.getPreferredWidth();
                }
                
                // Ajouter la marge
                if (i < groupCount - 1) {
                    totalWidth += groupMargin;
                }
            }
        }
        
        return totalWidth;
    }
    
    /**
     * Trouve le prochain groupe à collapser.
     * 
     * RÈGLE : Le groupe le plus à DROITE qui est encore en mode NORMAL.
     * 
     * @param groupModel modèle des groupes
     * @return index du groupe à collapser, ou -1 si aucun
     */
    public int findNextGroupToCollapse(HRibbonGroupModel groupModel) {
        if (groupModel == null) {
            return -1;
        }
        
        int groupCount = groupModel.getGroupCount();
        
        // Parcourir de droite à gauche
        for (int i = groupCount - 1; i >= 0; i--) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null && group.isNormal()) {
                return i; // Premier groupe non-collapsed trouvé
            }
        }
        
        return -1; // Tous les groupes sont déjà collapsed
    }
    
    /**
     * Trouve le prochain groupe à expander.
     * 
     * RÈGLE : Le groupe le plus à GAUCHE qui est en mode COLLAPSED.
     * 
     * @param groupModel modèle des groupes
     * @return index du groupe à expander, ou -1 si aucun
     */
    public int findNextGroupToExpand(HRibbonGroupModel groupModel) {
        if (groupModel == null) {
            return -1;
        }
        
        int groupCount = groupModel.getGroupCount();
        
        // Parcourir de gauche à droite
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null && group.isCollapsed()) {
                return i; // Premier groupe collapsed trouvé
            }
        }
        
        return -1; // Tous les groupes sont déjà expanded
    }
    
    /**
     * Calcule les actions de resize nécessaires pour une largeur disponible donnée.
     * 
     * ALGORITHME :
     * 1. Si largeur_disponible < largeur_actuelle → COLLAPSE progressif
     * 2. Si largeur_disponible > largeur_actuelle → EXPAND progressif
     * 
     * @param groupModel modèle des groupes
     * @param availableWidth largeur disponible
     * @param normalWidths map des largeurs normales
     * @param groupMargin marge entre groupes
     * @return liste des actions à effectuer (peut être vide)
     */
    public List<ResizeAction> calculateResizeActions(
            HRibbonGroupModel groupModel,
            int availableWidth,
            Map<Integer, Integer> normalWidths,
            int groupMargin) {
        
        List<ResizeAction> actions = new ArrayList<>();
        
        if (groupModel == null || normalWidths == null) {
            return actions;
        }
        
        // Calculer la largeur actuelle
        int currentWidth = calculateCurrentWidth(groupModel, groupMargin, normalWidths);
        
        // CAS 1 : RÉDUCTION NÉCESSAIRE
        if (availableWidth < currentWidth) {
            int workingWidth = currentWidth;
            
            while (availableWidth < workingWidth) {
                // Trouver le prochain groupe à collapser
                int groupToCollapse = findNextGroupToCollapse(groupModel);
                
                if (groupToCollapse == -1) {
                    // Plus de groupes à collapser
                    break;
                }
                
                // Ajouter l'action
                actions.add(new ResizeAction(groupToCollapse, true));
                
                // Simuler le collapse pour calculer la nouvelle largeur
                HRibbonGroup group = groupModel.getHRibbonGroup(groupToCollapse);
                Integer normalWidth = normalWidths.get(groupToCollapse);
                int widthBefore = (normalWidth != null) ? normalWidth : group.getPreferredWidth();
                int widthAfter = group.getCollapsedWidth();
                
                workingWidth = workingWidth - widthBefore + widthAfter;
                
                // Temporairement marquer comme collapsed pour les prochaines itérations
                CollapseLevel savedLevel = group.getCurrentLevel();
                group.setCurrentLevel(CollapseLevel.COLLAPSED);
                
                // Note : on ne restaure pas immédiatement car on veut continuer la simulation
                // Le restore sera fait après toutes les itérations
            }
            
            // Restaurer les états originaux après la simulation
            // (les vraies modifications seront faites par le layoutManager)
            for (ResizeAction action : actions) {
                HRibbonGroup g = groupModel.getHRibbonGroup(action.getGroupIndex());
                if (g != null && action.isCollapse()) {
                    g.setCurrentLevel(CollapseLevel.NORMAL);
                }
            }
        }
        
        // CAS 2 : EXPANSION POSSIBLE
        else if (availableWidth > currentWidth) {
            int workingWidth = currentWidth;
            
            while (true) {
                // Trouver le prochain groupe à expander
                int groupToExpand = findNextGroupToExpand(groupModel);
                
                if (groupToExpand == -1) {
                    // Plus de groupes à expander
                    break;
                }
                
                // Calculer la largeur après expansion de ce groupe
                HRibbonGroup group = groupModel.getHRibbonGroup(groupToExpand);
                Integer normalWidth = normalWidths.get(groupToExpand);
                int widthBefore = group.getCollapsedWidth();
                int widthAfter = (normalWidth != null) ? normalWidth : group.getPreferredWidth();
                
                int widthAfterExpand = workingWidth - widthBefore + widthAfter;
                
                // Vérifier si on a assez d'espace
                if (widthAfterExpand <= availableWidth) {
                    // OK, on peut expander
                    actions.add(new ResizeAction(groupToExpand, false));
                    workingWidth = widthAfterExpand;
                    
                    // Temporairement marquer comme normal pour les prochaines itérations
                    group.setCurrentLevel(CollapseLevel.NORMAL);
                } else {
                    // Pas assez d'espace, on arrête
                    break;
                }
            }
            
            // Restaurer les états originaux après la simulation
            for (ResizeAction action : actions) {
                HRibbonGroup g = groupModel.getHRibbonGroup(action.getGroupIndex());
                if (g != null && action.isExpand()) {
                    g.setCurrentLevel(CollapseLevel.COLLAPSED);
                }
            }
        }
        
        return actions;
    }
    
    /**
     * Applique un collapse à un groupe.
     * 
     * @param ribbon le ruban
     * @param groupIndex index du groupe à collapser
     */
    public void collapseGroup(Ribbon ribbon, int groupIndex) {
        if (ribbon == null) {
            return;
        }
        
        HRibbonGroupModel groupModel = ribbon.getGroupModel();
        if (groupModel == null) {
            return;
        }
        
        HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
        if (group != null && group.isNormal()) {
            group.collapse();
        }
    }
    
    /**
     * Applique un expand à un groupe.
     * 
     * @param ribbon le ruban
     * @param groupIndex index du groupe à expander
     */
    public void expandGroup(Ribbon ribbon, int groupIndex) {
        if (ribbon == null) {
            return;
        }
        
        HRibbonGroupModel groupModel = ribbon.getGroupModel();
        if (groupModel == null) {
            return;
        }
        
        HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
        if (group != null && group.isCollapsed()) {
            group.expand();
            // Invalider le composant collapsed pour forcer sa recréation
            group.invalidateCollapsedComponent();
        }
    }
}
