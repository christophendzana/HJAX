/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.NodeImpl;
import java.util.List;

/**
 *Contient le resultat de l'excecution
 * @author FIDELE
 */
public class XPathResult {
    
    private List<NodeImpl> nodes;

    public XPathResult (List<NodeImpl> nodes) {
        this.nodes = nodes;
    }

    public List<NodeImpl> getNodes() {
        return nodes;
    }
    
    public boolean getBoolean() {
        return nodes != null && !nodes.isEmpty();
    }
    
}
