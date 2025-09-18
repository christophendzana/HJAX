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
    
    
    // Ajoute un nœud dans la bonne catégorie
    public void addNode(NodeImpl node, String attrName) {
        nodesByAttr.computeIfAbsent(attrName, k -> new ArrayList<>()).add(node);
    }

    // Récupère tous les nœuds selon l'attribut
    public ArrayList<NodeImpl> getNodesByType(String attrName) {
        return (ArrayList) nodesByAttr.getOrDefault(attrName, null);
    }

    // Affiche un résumé des nœuds par attribut
    public void Summary() {
        for (String attrName : nodesByAttr.keySet()) {
            System.out.println(attrName + " : " + nodesByAttr.get(attrName).size() + " nœud(s)");
        }
    }
}
