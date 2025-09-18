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
    
    // Ajoute un nœud dans la bonne catégorie
    public void addNode(NodeImpl node) {
        String type = node.getClass().getSimpleName(); 
        
        //ComputerIfAbsent retourne la liste associée à cette clé (ce type)
        //Si ce type n'exixte pas alors elle crée une liste et l'associe à ce
        //nouveau type dans la Map puis retourne cette liste et on appelle 
        //.add(node) sur la liste renvoyée.
        nodesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
    }

    // Récupère tous les nœuds d’un type donné
    public ArrayList<NodeImpl> getNodesByType(String type) {
        
        for (String currentType : types) {
            if (currentType.equalsIgnoreCase(type)) {
                return (ArrayList) nodesByType.getOrDefault(type, null);
            }
        }
        return null;
    }

    // Affiche un résumé des nœuds par type
    public void printSummary() {
        for (String type : nodesByType.keySet()) {
            System.out.println(type + " : " + nodesByType.get(type).size() + " nœud(s)");
        }
    }
    
}
