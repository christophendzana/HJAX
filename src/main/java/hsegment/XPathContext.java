/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.NodeImpl;

/**
 *Garde la trace du noeud courant
 * @author FIDELE
 */
public class XPathContext {
        
    private NodeImpl currentNode;
    private int position;
    private int size;

    public XPathContext(NodeImpl currentNode, int position, int size) {
        this.currentNode = currentNode;
        this.position = position;
        this.size = size;
    }

    public NodeImpl getCurrentNode() {
        return currentNode;
    }

    public int getPosition() {
        return position;
    }

    public int getSize() {
        return size;
    }
    
}
