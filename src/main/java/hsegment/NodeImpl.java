/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.Node;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.DOMException;

/**
 *
 * @author PSM
 */
public abstract class NodeImpl implements Node {

    protected String nodeName;
    protected String nodeValue;
    protected short nodeType;
    protected NodeImpl nodeParent;
    protected NodeListImpl childNodes;
    protected DocumentImpl holderDocument;

    static {

    }

    public NodeImpl(String name, short nodeType, DocumentImpl holderDocument) {
        this.nodeName = name;
        this.nodeType = nodeType;
        this.holderDocument = holderDocument;
        this.childNodes = new NodeListImpl();
    }

    // --- Propriétés de base ---
    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public String getNodeValue() throws HJAXException {
        return nodeValue;
    }

    @Override
    public void setNodeValue(String nodeValue) throws HJAXException {
        this.nodeValue = nodeValue;
    }

    @Override
    public short getNodeType() {
        return nodeType;
    }

    @Override
    public NodeImpl getParentNode() {
        if (this.getNodeType() == NodeImpl.DOCUMENT_NODE) {
            return null;
        }
        return nodeParent;
    }

    @Override
    public NodeListImpl getChildNodes() {
        return getNodeType() == Node.ELEMENT_NODE ? childNodes : null;
    }

    @Override
    public NodeImpl getFirstChild() {
        return childNodes.getLength() > 0 ? childNodes.item(0) : null;
    }

    @Override
    public NodeImpl getLastChild() {
        return childNodes.getLength() > 0 ? childNodes.item(childNodes.getLength() - 1) : null;
    }

    @Override
    public NodeImpl getPreviousSibling() {
        return childNodes.getLength() > 0 ? childNodes.item(childNodes.indexNode(nodeName) - 1) : null;
    }

    @Override
    public NodeImpl getNextSibling() {
        return childNodes.getLength() > 0 ? childNodes.item(childNodes.indexNode(nodeName) + 1) : null;
    }

    @Override
    public NamedNodeMapImpl getAttributes() {
        return null;
    }

    @Override
    public DocumentImpl getOwnerDocument() {
        return holderDocument;
    }

    public NodeImpl appendChild(NodeImpl newChild) {
        if (newChild == null) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "child is null");
        }
        if (newChild == this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "il ne peut pas être enfant de lui même");
        }

        AllowedChildren allowedChildren = new AllowedChildren();

        if (!allowedChildren.isAllowed(this, newChild)) {
            
            throw new IllegalArgumentException("Document can have only root Element");
        }
        
        if (this.getNodeType() == DOCUMENT_NODE) {
            if (newChild.getNodeType() == ELEMENT_NODE && this.hasTypeChildNode(newChild.getNodeType())) {
                throw new IllegalArgumentException("Un Document ne peut avoir qu’un seul élément racine.");
            }
            if (newChild.getNodeType() == DOCUMENT_TYPE_NODE && this.hasTypeChildNode(newChild.getNodeType())) {
                throw new IllegalArgumentException("Un Document ne peut avoir qu’un seul DocumentType.");
            }
        }

        // newchild ne peut avoir deux parents
        if (newChild.getParentNode() != null) {
            newChild.getParentNode().removeChild(newChild);
        }
        newChild.nodeParent = this;
        this.childNodes.addNode(newChild);
        return newChild;
    }

    @Override
    public NodeImpl insertBefore(NodeImpl newChild, NodeImpl refChild) throws HJAXException {

        if (refChild == null) {
            return appendChild(newChild);
        }

        int index = childNodes.indexNode(refChild.nodeName);

        if (index == -1) {
            // Fatal Error: Message d'erreur refChild n'existe pas dans la liste
        }
        childNodes.addNodeInIndex(index, newChild);
        newChild.nodeParent = this;
        return newChild;
    }

    @Override
    public NodeImpl replaceChild(NodeImpl newChild, NodeImpl oldChild) throws HJAXException {

        int index = this.getChildNodes().indexNode(oldChild.getNodeName());

        this.getChildNodes().removeNode(oldChild.getNodeValue());

        this.getChildNodes().addNodeInIndex(index, newChild);

        newChild.nodeParent = this;

        return newChild;
    }

    @Override
    public NodeImpl removeChild(NodeImpl oldChild) throws HJAXException {
        this.getChildNodes().removeNode(oldChild.getNodeName());
        return oldChild;
    }

    @Override
    public boolean hasChildNodes() {
        return this.getChildNodes().getLength() > 0;
    }

    public boolean hasTypeChildNode( short type ){
        for (int i = 0; i < this.getChildNodes().getLength(); i++) {
            short typeChild  = this.getChildNodes().item(i).getNodeType();
            return type == typeChild;
        }
        return false;
    }
    
    @Override
    public NodeImpl cloneNode(boolean deep) {

        try {

            NodeImpl clone = (NodeImpl) this.getClass().getDeclaredConstructor().newInstance();
            clone.nodeName = this.nodeName;
            clone.nodeValue = this.nodeValue;
            clone.nodeType = this.nodeType;
            clone.holderDocument = this.holderDocument;

            if (deep && hasChildNodes()) {
                for (int i = 0; i < childNodes.getLength(); i++) {

                    NodeImpl child = childNodes.item(i);

                    if (child.getNodeType() == NodeImpl.ATTRIBUTE_NODE) {
                        HashMap<String, NodeImpl> attrList = new HashMap<>();
                        attrList.put(child.getNodeName(), child);
                    }

                    clone.appendChild(child.cloneNode(true));

                    return clone;
                }
            }

        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | DOMException e) {
        }
        return null;
    }

    @Override
    public boolean hasAttributes() {
        return this.getAttributes().getLength() > 0;
    }

    @Override
    public short compareDocumentPosition(NodeImpl other) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getTextContent() {
        StringBuilder content = new StringBuilder();
        NodeListImpl children = this.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            NodeImpl child = children.item(i);
            if (child instanceof TextImpl) {
                content.append(child.getNodeValue());
            } else if (child instanceof ElementImpl) {
                content.append(child.getTextContent());
            }
        }
        return content.toString();
    }

    @Override
    public void setTextContent(String textContent) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isSameNode(NodeImpl other) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
