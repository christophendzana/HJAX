/*
 * OverflowProxyFactory.java
 * 
 * Interface pour la création de proxies de composants Swing.
 * 
 * RÔLE :
 * Transformer un composant original (déjà parenté, vivant dans le Ribbon)
 * en un proxy léger qui peut être ajouté au RibbonOverflowButton.
 * 
 * Le proxy partage les MODÈLES du composant original (Document, Action,
 * ComboBoxModel, BoundedRangeModel, ButtonModel, etc.) pour que l'état
 * reste synchronisé entre le Ruban et le popup de débordement.
 * 
 * Le proxy n'a PAS de parent à sa création. C'est la responsabilité
 * de l'appelant de l'ajouter au conteneur approprié.
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban;

import javax.swing.JComponent;

public interface OverflowProxyFactory {

    /**
     * Crée un proxy pour le composant original.
     * 
     * @param original le composant original (parent = Ribbon, jamais null)
     * @return un proxy du même type, partageant les modèles,
     *         ou null si le type n'est pas supporté
     * 
     * CONTRAT :
     * - L'original n'est PAS modifié
     * - Le proxy est une nouvelle instance, sans parent
     * - Les modèles sont PARTAGÉS (pas de copie)
     * - Les listeners sont forwardés ou copiés selon le type
     * - Si le type n'est pas supporté, retourne null (le composant
     *   n'apparaîtra pas dans le popup)
     * 
     * IMPLÉMENTATION PAR DÉFAUT :
     * Voir DefaultOverflowProxyFactory pour la liste des types supportés.
     */
    JComponent createProxy(JComponent original);
}