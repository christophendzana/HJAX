/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

/**
 *
 * @author PSM
 */
public interface NodeList {
    
    /**
     * Renvoie l'élément <code>index</code> de la collection. Si
* <code>index</code> est supérieur ou égal au nombre de nœuds de
* la liste, cette opération renvoie <code>null</code>.
* @param index Index dans la collection.
* @return Le nœud à la position <code>index</code> dans
* <code>NodeList</code>, ou <code>null</code> s'il ne s'agit pas d'un index valide.
     */
    public Node item(int index);

    /**
     * Nombre de nœuds dans la liste. La plage d'indices de nœuds enfants valides est comprise entre 0 et <code>length-1</code> inclus.
     */
    public int getLength();
    
}
