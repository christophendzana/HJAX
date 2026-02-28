/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package rubban.layout;

import rubban.Ribbon;

/**
 * Interface pour la distribution des largeurs des groupes.
 * @author FIDELE
 */
public interface IGroupWidthDistributor {
    
     /**
     * Calcule et retourne un tableau de largeurs (en pixels) pour chaque groupe.
     *
     * @param ctx           contexte de layout (informations sur header, margins, flags)     
     * @param ribbon     
     * @param availableWidth largeur totale disponible (après insets)
     * @param useEntireWidth
     * @return tableau d'entiers de longueur groupModel.getGroupCount()
     */
    int[] distributeWidths(HRibbonLayoutContext ctx, Ribbon ribbon, int availableWidth, boolean useEntireWidth);
    
}
