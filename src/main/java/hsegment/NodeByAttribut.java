/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class NodeByAttribut {
    
    private HashMap<String, List<NodeImpl>> nodesByAttr = new HashMap<>();
    
    public NodeByAttribut() {
    }
    
    
    /**
     * Ajoute un noeud dans la liste correspondante ou la crée si elle est
     * inexistante et ensuite ajoute le noeud à cette liste
     * @param node
     * @param attrName 
     */
    public void addNode(NodeImpl node, String attrName) {
        nodesByAttr.computeIfAbsent(attrName, k -> new ArrayList<>()).add(node);
    }

    /**
     * Renvoie une liste de noeud correspondante au nom de l'attribut passé
     * en paramètre et null si il n'y a aucune occurrence.
     * @param attrName
     * @return 
     */
    public ArrayList<NodeImpl> getNodesByAttribut(String attrName) {
        return (ArrayList) nodesByAttr.getOrDefault(attrName, null);
    }
    
    /**
     * Retourne le noeud dont le nom est <code>tagName</code> dans la liste 
     * des nouds ayant l'attribut <code>attrName</code>
     * @param attrName
     * @param tagName
     * @return 
     */
    public NodeImpl getNodeByAttrAndName(String attrName, String tagName){
         ArrayList<NodeImpl> list = getNodesByAttribut(attrName);
        for (int i = 0; i < list.size(); i++) {
            NodeImpl child = list.get(i);
            if (child.getNodeName().equalsIgnoreCase(tagName)) {
                return child;
            }
        }
        return null;
    }   
  
    
    /**
     * Retourne si oui ou non au moins un noeud a cet attribut
     * @param attrName
     * @return 
     */
    public boolean existNodeOfAttribut(String attrName){
        return !getNodesByAttribut(attrName).isEmpty();
    }
       

    /**
     * Bilan des noeuds par attributs
     */
    public void Summary() {
        for (String attrName : nodesByAttr.keySet()) {
            System.out.println(attrName + " : " + nodesByAttr.get(attrName).size() + " nœud(s)");
        }
    }
}
