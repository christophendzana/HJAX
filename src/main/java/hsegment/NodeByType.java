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
public class NodeByType {
    
    private HashMap<String, List<NodeImpl>> nodesByType = new HashMap<>();
    
    public NodeByType() {
    }

    String[] types = {"AttributeNode", "CharData", "CommentNode", "ElementNode", "DocumentTypeNode", 
                       "EntityNode", "NodeImpl", "NotationNode", "ProcessusInstructionNode", "TextNode"}  ;
    
    /**
     * Ajoute un noeud dans la catégorie correspondante ou ajoute la cotégorie
     * si elle est inexistante puis l'ajoute.
     * @param node 
     */
    public void addNode(NodeImpl node) {
        String type = node.getClass().getSimpleName(); 
        
        //ComputerIfAbsent retourne la liste associée à cette clé (ce type)
        //Si ce type n'exixte pas alors elle crée une liste et l'associe à ce
        //nouveau type dans la Map puis retourne cette liste et on appelle 
        //.add(node) sur la liste renvoyée.
        nodesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
    }

    /**
     * Renvoie une liste de noeud correspondante au type donné passé
     * en paramètre et null si il n'y a aucune occurrence
     * @param type
     * @return 
     */
    public ArrayList<NodeImpl> getNodesByType(String type) {
        
        for (String currentType : types) {
            if (currentType.equalsIgnoreCase(type)) {
                return (ArrayList) nodesByType.getOrDefault(type, null);
            }
        }
        return null;
    }
    
    /**
     * Retourne le noeud dont le nom est passé en paramètre dans un type donné
     * @param type
     * @param tagName le nom du noeud
     * @return 
     */
    public NodeImpl getNodeByTypeAndName(String type, String tagName){
        ArrayList<NodeImpl> list = getNodesByType(type);
        for (int i = 0; i < list.size(); i++) {
            NodeImpl child = list.get(i);
            if (child.getNodeName().equalsIgnoreCase(tagName)) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Retourne si oui ou non au moins un noeud de ce type existe
     * @param type
     * @return 
     */
    public boolean existNodeOfType(String type){
        return !getNodesByType(type).isEmpty();
    }

    // Affiche un résumé des nœuds par type
    public void printSummary() {
        for (String type : nodesByType.keySet()) {
            System.out.println(type + " : " + nodesByType.get(type).size() + " nœud(s)");
        }
    }
    
}
