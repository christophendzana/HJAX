/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

import DOM.NodeImpl;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class XPathResult implements Iterable<NodeImpl>{
    
    private List<NodeImpl> nodes;
    private Iterator<NodeImpl> iterator;

    public XPathResult(List<NodeImpl> nodes) {
        this.nodes = nodes;
        this.iterator = nodes.iterator();
    }

    /**
     * Vérifie s’il reste un nœud suivant.
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Récupère le nœud suivant.
     */
    public NodeImpl next() {
        return iterator.next();
    }

    /**
     * Retourne tous les nœuds sous forme de liste.
     */
    public List<NodeImpl> getAll() {
        return nodes;
    }

    @Override
    public Iterator<NodeImpl> iterator() {
        return nodes.iterator();
    }
    
}
