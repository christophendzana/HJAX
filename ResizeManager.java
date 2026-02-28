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
     * 1. Si largeur_disponible suppérieure à largeur_actuelle → COLLAPSE progressif
     * 2. Si largeur_disponible inférieure à largeur_actuelle → EXPAND progressif
     * 3. Si un seul groupe atteint sa largeur minimal → COllCOLLAPSE progressif // à implémenter
     * @param groupModel modèle des groupes
     * @param availableWidth largeur disponible
     * @param normalWidths map des largeurs normales
     * @param groupMargin marge entre groupes
     * @return liste des actions à effectuer (peut être vide)
     */
    /**
 * Calcule les actions de redimensionnement nécessaires pour adapter le ruban
 * à la largeur disponible.
 * 
 * @param groupModel Le modèle contenant tous les groupes du ruban
 * @param availableWidth La largeur totale disponible pour afficher le ruban
 * @param normalWidths Map contenant les largeurs normales (non-collapsed) de chaque groupe
 * @param groupMargin L'espace entre chaque groupe
 * @return Liste des actions à effectuer (collapse ou expand) pour s'adapter à l'espace
 */
public List<ResizeAction> calculateResizeActions(
        HRibbonGroupModel groupModel,
        int availableWidth,
        Map<Integer, Integer> normalWidths,
        int groupMargin) {
    
    // Liste des actions à retourner (vide par défaut)
    List<ResizeAction> actions = new ArrayList<>();
    
    // Vérification de sécurité : si pas de modèle ou pas de largeurs, on ne fait rien
    if (groupModel == null || normalWidths == null) {
        return actions;
    }
    
    // =========================================================================
    // ÉTAPE 1 : CALCULER LA LARGEUR ACTUELLE DU RUBAN
    // =========================================================================
    // La largeur actuelle dépend de l'état de chaque groupe :
    // - Si un groupe est NORMAL : on utilise sa largeur normale
    // - Si un groupe est COLLAPSED : on utilise sa largeur réduite
    // =========================================================================
    
    int currentTotalWidth = 0;  // Largeur totale actuelle du ruban
    
    for (int i = 0; i < groupModel.getGroupCount(); i++) {
        HRibbonGroup group = groupModel.getHRibbonGroup(i);
        if (group != null) {
            // Déterminer la largeur de CE groupe selon son état
            if (group.isCollapsed()) {
                // Groupe réduit : il prend sa largeur "collapsed"
                currentTotalWidth += group.getCollapsedWidth();
            } else {
                // Groupe normal : il prend sa largeur normale (celle du layout)                
                currentTotalWidth += (normalWidths.get(i) != null) ? normalWidths.get(i) : group.getPreferredWidth();
            }
            
            // Ajouter la marge après chaque groupe SAUF le dernier
            if (i < groupModel.getGroupCount() - 1) {
                currentTotalWidth += groupMargin;
            }
        }
    }
    
    // =========================================================================
    // ÉTAPE 2 : COMPARER L'ESPACE DISPONIBLE AVEC LA LARGEUR ACTUELLE
    // =========================================================================
    // Trois cas possibles :
    // 1. availableWidth < currentTotalWidth  → On manque d'espace → COLLAPSER
    // 2. availableWidth > currentTotalWidth  → On a trop d'espace → EXPANDRE
    // 3. availableWidth == currentTotalWidth → Rien à faire
    // =========================================================================
    
    // CAS 1 : RÉDUCTION NÉCESSAIRE
// Vérifier si au moins un groupe a besoin d'être collapsé
// Calculer la somme des minWidth de tous les groupes normaux + collapsed
int totalMinWidth = 0;
for (int i = 0; i < groupModel.getGroupCount(); i++) {
    HRibbonGroup group = groupModel.getHRibbonGroup(i);
    if (group == null) continue;
    if (group.isCollapsed()) {
        totalMinWidth += group.getCollapsedWidth();
    } else {
        totalMinWidth += group.getMinWidth();
    }
    if (i < groupModel.getGroupCount() - 1) {
        totalMinWidth += groupMargin;
    }
//    System.out.println("Groupe " + i 
//        + " | minWidth=" + group.getMinWidth()
//        + " | isCollapsed=" + group.isCollapsed()
//        + " | contribution=" + (group.isCollapsed() ? group.getCollapsedWidth() : group.getMinWidth()));
}
//System.out.println("availableWidth=" + availableWidth 
//    + " | totalMinWidth=" + totalMinWidth
//    + " | needsCollapse=" + (totalMinWidth > availableWidth));

boolean needsCollapse = totalMinWidth > availableWidth;

if (needsCollapse) {
    
    // On part de la largeur réelle actuelle, pas de totalMinWidth
    int workingWidth = currentTotalWidth;
    
    for (int i = groupModel.getGroupCount() - 1; i >= 0; i--) {
        if (workingWidth <= availableWidth) break;
        
        HRibbonGroup group = groupModel.getHRibbonGroup(i);
        if (group == null || group.isCollapsed()) continue;
        
        // Largeur normale allouée à ce groupe
        Integer normalWidth = normalWidths.get(i);
        int allocatedWidth = (normalWidth != null) ? normalWidth : group.getPreferredWidth();
        
        // En collapsant ce groupe, on perd allocatedWidth et on gagne collapsedWidth
        actions.add(new ResizeAction(i, true));
        workingWidth = workingWidth - allocatedWidth + group.getCollapsedWidth();
    }
    
}
// CAS 2 : EXPANSION POSSIBLE (seulement si pas de collapse nécessaire)
else {
    int workingWidth = currentTotalWidth;
    
    while (true) {
        int groupToExpand = findNextGroupToExpand(groupModel);
        if (groupToExpand == -1) break;
        
        HRibbonGroup group = groupModel.getHRibbonGroup(groupToExpand);
        Integer normalWidth = normalWidths.get(groupToExpand);
        int widthAfterExpand = (normalWidth != null) ? normalWidth : group.getPreferredWidth();
        int widthBeforeExpand = group.getCollapsedWidth();
        
        int newWorkingWidth = workingWidth - widthBeforeExpand + widthAfterExpand;
        if (newWorkingWidth <= availableWidth) {
            actions.add(new ResizeAction(groupToExpand, false));
            workingWidth = newWorkingWidth;
            group.setCurrentLevel(CollapseLevel.NORMAL);
        } else {
            break;
        }
    }
    
    for (ResizeAction action : actions) {
        if (action.isExpand()) {
            HRibbonGroup group = groupModel.getHRibbonGroup(action.getGroupIndex());
            if (group != null) group.setCurrentLevel(CollapseLevel.COLLAPSED);
        }
    }
}
    
//    System.out.println("availableWidth=" + availableWidth 
//    + " | currentTotalWidth=" + currentTotalWidth);
    // Si availableWidth == currentTotalWidth, on ne fait rien (actions reste vide)
    
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
            group.invalidateCollapsedButton();
           
        }
    }
}
