/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.DocumentNode;

/**
 * @author PSM
 */
public abstract class DocumentNodeImpl implements DocumentNode {

    protected String nodeName;
    protected String nodeValue;
    protected short nodeType;
    protected NodeImpl nodeParent;
    protected NodeListImpl childNodes;

    public DocumentNodeImpl(String name, short nodeType) {
        this.nodeName = name;
        this.nodeType = nodeType;
        this.childNodes = new NodeListImpl();
    }

    
    
}


