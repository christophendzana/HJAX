/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package rubban.layout;

import rubban.HRibbonGroupModel;

/**
 * Interface pour la distribution des largeurs des groupes.
 * @author FIDELE
 */
public interface IGroupWidthDistributor {
    
     /**
     * Calcule et retourne un tableau de largeurs (en pixels) pour chaque groupe.
     *
     * @param ctx           contexte de layout (informations sur header, margins, flags)
     * @param groupModel    modèle des HRibbonGroup
     * @param availableWidth largeur totale disponible (après insets)
     * @return tableau d'entiers de longueur groupModel.getGroupCount()
     */
    int[] distributeWidths(HRibbonLayoutContext ctx, HRibbonGroupModel groupModel, int availableWidth);

    /**
     * Active ou désactive la distribution égale (tous les groupes même largeur).
     *
     * @param equal true => égalitaire
     */
    void setEqualDistribution(boolean equal);

    /**
     * Retourne l'état de la distribution égale.
     *
     * @return true si distribution égale active
     */
    boolean isEqualDistribution();
    
}
