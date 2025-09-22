/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import APIDOMException.*;
import Interface.*;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import org.w3c.dom.DOMException;
import hsegment.JObject.util.Dictionnary;
import java.util.HashMap;


/**
 *
 * @author BANLOCK
 */
public class Document {

    private DocumentTypeNode doctype;
    private ElementNode rootElement;
    private final boolean xml11Version = false;
    private ArrayList<DocumentListener> listenerList = new ArrayList<>();
    private Dictionnary NodesRegistry = new Dictionnary(27, 5, false, false);    
    private ArrayList<NodeImpl> ElementHasChilds = new ArrayList<>();
    private NodeByType nodeByType = new NodeByType();
    private NodeByAttribut nodeByAttribut = new NodeByAttribut(); 
    public Document() {
    }
    
    public void addDocumentListeners(DocumentListener doc) {
        listenerList.add(doc); 
    }

    public ElementNode getDocumentElement(){
        return rootElement;
    }

    public void setDocumentElement(ElementNode element) {
        if (getDocumentElement() != null) {
            throw new DomException((short) 1, "Document already has a root element");
        }
        this.rootElement = element;
    }

    public DocumentTypeNode getDoctype() {
        return doctype;
    }

    public void setDoctype(DocumentTypeNode doctype) {
        this.doctype = doctype;
    }

    public ElementNode createElement(String tagName, Document holderDocument) throws HJAXException {

        if (!isXMLName(tagName, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }

        ElementNode element = new ElementNode(tagName);

        DocumentEvent evt = new DocumentEvent(element, this);
        
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });
        
        NodesRegistry.add(element);
        nodeByType.addNode(element);
        
        return element;
    }

    public AttributeNode createAttribute(String name, String value, boolean specified, ElementNode holderElement) {
        AttributeNode attr = new AttributeNode(name, value, specified);
        holderElement.attributes.addAttribut(attr);
        nodeByType.addNode(attr);
        nodeByAttribut.addNode(holderElement, name);
        
        DocumentEvent evt = new DocumentEvent(attr, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                
                t.nodeAdded(evt);
            }
        });

        return attr;
    }

    public TextNode createTextNodeImpl(String data) {
        if (!isXMLName(data, xml11Version)) {
            throw new InvalidCharacterException("Invalid data");
        }
        TextNode text = new TextNode(data);
        nodeByType.addNode(text);
        
        DocumentEvent evt = new DocumentEvent(text, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });

        return text;
    }
    
    public ArrayList<NodeImpl> getNodesByAttr(String attrName){
        return nodeByAttribut.getNodesByAttribut(attrName);
    }
    
    public ArrayList<NodeImpl> getNodesByType(String type){
        return nodeByType.getNodesByType(type);
    }
    
     /**
     * Retourne le noeud dont le nom est <code>tagName</code> dans la liste 
     * des nouds ayant l'attribut <code>attrName</code> et la valeur
     * <code>value</code>
     * @param attrName
     * @param tagName
     * @param value
     * @return 
     */
    public NodeImpl getNodeByAttrAndNameAndValue(String attrName, String tagName, String value){
         ArrayList<NodeImpl> list = nodeByAttribut.getNodesByAttribut(attrName);
        for (int i = 0; i < list.size(); i++) {
            NodeImpl child = list.get(i);
            if (child.getNodeName().equalsIgnoreCase(tagName)) {
                String val = getAttribute( (ElementNode) child, attrName).getNodeValue();
                if (val.equalsIgnoreCase(value)) {
                    return child;
                }
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
        return nodeByAttribut.existNodeOfAttribut(attrName);
    }
    
    /**
     * Retourne le noeud dont le nom est passé en paramètre dans un type donné
     * @param type
     * @param tagName le nom du noeud
     * @return 
     */
    public NodeImpl getNodeByTypeAndName(String type, String tagName){
        return nodeByType.getNodeByTypeAndName(type, tagName);
    }
    
    public NodeImpl getElementByName(String tagname){
       NodeImpl node = (NodeImpl) NodesRegistry.get(tagname);
       return node;
    }

    public NodeImpl getChildInNode(ElementNode node, String tagname) {
        NodeList childNode = getChildNodes(node);
        if (childNode.indexNode(tagname) != -1) {
            return childNode.item(childNode.indexNode(tagname));
        }
        return null;
    }

    public ElementNode getChildById(ElementNode element, String elementId) {

        for (int i = 0; i < getChildNodes(element).getLength(); i++) {
            if (getChildNodes(element).item(i) instanceof ElementNode
                    && getAttribute((ElementNode) (getChildNodes(element).item(i)), "id").equals(elementId)) {
                return (ElementNode) getChildNodes(rootElement).item(i);
            }
        }
        return null;
    }    
    
    public NodeImpl removeNodeImpl(NodeImpl refNode, NodeImpl newParent) {

        if (refNode instanceof ElementNode) {

            if (!getChildNodes(rootElement).contain(refNode)) {
                throw new NodeNotFoundException("Not found node");
            }

            if (refNode == null) {
                // Message d'erreur : le noeud ne peut pas être null
            }

            if (getParentNode(refNode) == null) {
                throw new NodeHierarchyException("Impossible to delete root node");
            }

            if (newParent == null) {
                getChildNodes(refNode).removeAll();
                removeChild(getParentNode(refNode), refNode);
            } else {
                for (int i = 0; i < getChildNodes(refNode).getLength(); i++) {
                    appendChild(newParent, getChildNodes(refNode).item(i));
                }
                getChildNodes(refNode).removeAll();
                removeChild(getParentNode(refNode), refNode);
            }

        }
        DocumentEvent evt = new DocumentEvent(refNode, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeRemoved(evt);
            }

        });
        return refNode;
    }

    public NodeImpl removeNodeImpl(NodeImpl refNode) {
        DocumentEvent evt = new DocumentEvent(refNode, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });
        return removeNodeImpl(refNode, null);
    }

    public NodeImpl renameNodeImpl(NodeImpl node, String namespaceURI, String qualifiedName) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public CommentNode createCommentImpl(String data) {

        CommentNode comment = new CommentNode(data);
        nodeByType.addNode(comment);
        
        DocumentEvent evt = new DocumentEvent(comment, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });

        return comment;
    }

    public EntityNode createEntityImpl(String name) {

        if (isXMLName(name, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }
        nodeByType.addNode(new EntityNode(name));
        return new EntityNode(name);
    }

    public DocumentTypeNode createDocumentTypeImpl(String name, String publicId,
            String systemId, String internalSubSet) {

        DocumentTypeNode docType = new DocumentTypeNode(name, publicId, systemId, internalSubSet);
        nodeByType.addNode(docType);
        DocumentEvent evt = new DocumentEvent(docType, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });
        return docType;
    }

    public NotationNode createNotationImpl(String name, short nodeType) {
        if (!isXMLName(name, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }

        NotationNode notation = new NotationNode(name);
        nodeByType.addNode(notation);
        DocumentEvent evt = new DocumentEvent(notation, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });
        return notation;
    }

    public static final boolean isXMLName(String s, boolean xml11Version) {

        if (s == null) {
            return false;
        }
        if (!xml11Version) {
            return XMLChar.isValidName(s);
        } else {
            return XML11Char.isXML11ValidName(s);
        }

    }

    //Node Interface    
    public String getNodeValue(NodeImpl node) throws HJAXException {
        return node.getNodeValue();
    }

    public void setNodeValue(NodeImpl node, String nodeValue) throws HJAXException {
        node.nodeValue = nodeValue;
    }

    public NodeImpl getParentNode(NodeImpl node) {

        if (node instanceof ElementNode) { 
            for (int i = 0; i < ElementHasChilds.size(); i++) {

                NodeImpl parent = ElementHasChilds.get(i);
                NodeList ChildsNodeParent = getChildNodes(parent);
                
                if (hasAttributes( (ElementNode) node) && hasChildNodes(node)) {                                        
                    if (inThisSpecificList(ChildsNodeParent, true, true, node)) {
                        return parent;
                    }                                   
                }else if(!hasAttributes( (ElementNode) node) && hasChildNodes(node)){
                    if (inThisSpecificList(ChildsNodeParent, false, true, node)) {
                        return parent;
                    } 
                }else if(hasAttributes( (ElementNode) node) && !hasChildNodes(node)){
                    if (inThisSpecificList(ChildsNodeParent, true, false, node)) {
                        return parent;
                    } 
                }else{
                    if (inThisSpecificList(ChildsNodeParent, false, false, node)) {
                        return parent;
                    } 
                }
            }
        }
            
        return null;
    }
    
    /**
     * Retourne si oui ou non un element est dans une liste spécifiques 
     * @param list ici la liste dans laquelle le tri sera effectuée
     * @param hasAttr si oui ou non l'élément a des attributs
     * @param hasChilds si oui ou non l'élément a des enfants
     * @param node le noeud element pour qui on fait la vérification
     * @return 
     */
    public boolean inThisSpecificList(NodeList list, boolean hasAttr, boolean hasChilds, NodeImpl node){
        
        NodeList listBrowse = new NodeList() ;            
                    for (int j = 0; j < list.getLength(); j++) {
                        NodeImpl currentChild = list.item(j);
                        
                        //Création de la liste correspondante
                        if (hasAttr && hasChilds) {
                            if (hasAttributes((ElementNode) currentChild)
                                && hasChildNodes(currentChild)) {
                                listBrowse.addChild(currentChild);
                            }
                        }else if(!hasAttr && hasChilds){
                             if (!hasAttributes((ElementNode) currentChild)
                                && hasChildNodes(currentChild)) {
                                listBrowse.addChild(currentChild);
                            }
                        }else if(hasAttr && !hasChilds){
                             if (hasAttributes((ElementNode) currentChild)
                                && !hasChildNodes(currentChild)) {
                                listBrowse.addChild(currentChild);
                            }
                        }else{
                            if (!hasAttributes((ElementNode) currentChild)
                                && !hasChildNodes(currentChild)) {
                                listBrowse.addChild(currentChild);
                            }
                        }                     
                    }                    
        return listBrowse.contain(node);        
    }
    
    public int getDepth(NodeImpl node){
        return node.depth;        
    }

    public void affectDepth(NodeImpl nodeTarget){
         int depth = 0;
        NodeImpl current = nodeTarget; 
        
        if (nodeTarget == rootElement) {
            rootElement.depth = 0;
        }
        
        while(true){
            NodeImpl parent = getParentNode(current);
            if (parent == null) { //Noeud racine
                break;  
            }
            depth++;
            current = parent;
        }
        nodeTarget.depth = depth;
    }
    
    public NodeImpl whoIsSuperior(NodeImpl node1, NodeImpl node2){        
        if (getDepth(node1) - getDepth(node2) > 0 ) {
            return node1;
        }else if(getDepth(node1) - getDepth(node2) < 0) {
            return node2;
        }
        return null;
    }
       
    public NodeList getChildNodes(NodeImpl refNode) {
        if (refNode instanceof ElementNode) {
            return ((ElementNode) refNode).childNodes;
        }
        return null;
    }

    public NodeImpl getFirstChild(ElementNode element) {
        return hasChildNodes(element) ? getChildNodes(element).item(0) : null;
    }

    public NodeImpl getLastChild(ElementNode element) {
        return hasChildNodes(element) ? getChildNodes(element).item(getChildNodes(element).getLength() - 1) : null;
    }

    public NodeImpl getPreviousSibling(NodeImpl node) {
        NodeList children = getChildNodes(getParentNode(node));

        int index = children.indexNode(node.getNodeName());

        return children.item(index - 1);
    }

    public NodeImpl getNextSibling(NodeImpl node) {

        NodeList children = getChildNodes(getParentNode(node));

        int index = children.indexNode(node.getNodeName());

        return children.item(index + 1);

    }

    public AttributesListNode getAttributes(ElementNode element) {
        return element.attributes;
    }

    public NodeImpl insertBefore(NodeImpl refNode, NodeImpl newChild, NodeImpl refChild) throws HJAXException {
        if (refChild == null) {
            return appendChild(refNode, newChild);
        }

        int index = getChildNodes(refNode).indexNode(refChild.nodeName);

        if (index == -1) {
            // Fatal Error: Message d'erreur refChild n'existe pas dans la liste
        }
        getChildNodes(refNode).addNodeInIndex(index, newChild);
        return newChild;
    }

    public NodeImpl replaceChild(NodeImpl refNode, NodeImpl newChild, NodeImpl oldChild) throws HJAXException {
        int index = getChildNodes(refNode).indexNode(oldChild.getNodeName());

        getChildNodes(refNode).removeNode(oldChild.getNodeValue());

        getChildNodes(refNode).addNodeInIndex(index, newChild);

        return newChild;
    }

    
    public NodeImpl removeChild(NodeImpl refNode, NodeImpl oldChild) throws HJAXException {
        getChildNodes(refNode).removeNode(oldChild.getNodeName());
        return oldChild;
    }

    public NodeImpl appendChild(NodeImpl refNode, NodeImpl newChild) throws HJAXException {

        if (refNode instanceof ElementNode) {

            if (newChild == null) {
                throw new java.lang.IllegalArgumentException("Le noeud en paramètre ne peut être null");
            }
            if (newChild == refNode) {
                throw new DomException(DomException.HIERARCHY_REQUEST_ERR, "Un noeud ne peut être enfant de lui même");
            }

            if (newChild instanceof DocumentTypeNode) {
                throw new APIDOMException.IllegalArgumentException("Un Document ne peut avoir qu’un seul DocumentType.");
            }

            // newchild ne peut avoir deux parents
            if (getParentNode(newChild) != null) {
                removeChild(getParentNode(newChild), newChild);
            }

            getChildNodes(refNode).addChild(newChild);

            if (!ElementHasChilds.contains(refNode)) {
                ElementHasChilds.add(refNode);
            }            

            return newChild;

        }
        return null;
    }   
    
    /**
     * Déplace un noeud enfant vers un autre parent
     * @param fromParent Noeud source
     * @param toParent Noeud parent cible
     * @param child Noeud enfant
     */
    public void moveChild(NodeImpl fromParent, NodeImpl toParent, NodeImpl child) {
            if (getChildNodes(fromParent).contain(child)) {
                getChildNodes(fromParent).removeNode(child.nodeName);
                getChildNodes(toParent).addChild(child);
            }
            
            if (!ElementHasChilds.contains(toParent)) {
                ElementHasChilds.add(toParent);
            }
            
            if (!hasChildNodes(fromParent)) {
                ElementHasChilds.remove(fromParent);
            }        
    }

    /**
     * Encapsule le noeud cible dans un nouveau noued
     * @param target Noeud cible
     * @param wrapperName Nom du noeud à crée qui portera le noued cible
     * @return 
     */
    public NodeImpl wrap(NodeImpl target, String wrapperName) {
        ElementNode wrapper = new ElementNode(wrapperName);
        getChildNodes(wrapper).addChild(target);
        
        if (target != rootElement) {
             appendChild(getParentNode(target), wrapper);
        }        
        return wrapper;
    }

    /**
     * Supprime un noeud et rajoute la liste de tout ses enfant à son parent
     * @param parent
     * @param child 
     */
    public void unwrap(NodeImpl parent, NodeImpl child) {
        removeChild(parent, child);
        getChildNodes(parent).addAll(getChildNodes(child).getNodes());
    }

    /**
     * Duplique un enfant
     * @param parent
     * @param child 
     */
    public void duplicate(NodeImpl parent, NodeImpl child) {
        NodeImpl clone = cloneNode(child, true);
        appendChild(parent, clone);
    }

    /**
     * Remplace tous les noeuds enfants par une nouvelle collection de noeud
     * @param parent
     * @param newChildren 
     */
    public void replaceChildren(NodeImpl parent, ArrayList<NodeImpl> newChildren) {
        getChildNodes(parent).removeAll();
        getChildNodes(parent).addAll(newChildren);
        if (!ElementHasChilds.contains(parent) && !newChildren.isEmpty()) {
            ElementHasChilds.add(parent);
        }
    }

    /**
     * Ajoute une nouvelle collection d'enfant à partir de la liste des 
     * enfants d'un autre parent
     * @param targetParent
     * @param sourceParent 
     */
    public void mergeChildren(NodeImpl targetParent, NodeImpl sourceParent) {
        getChildNodes(targetParent).addAll(getChildNodes(sourceParent).getNodes());
        if (!ElementHasChilds.contains(targetParent)) {
            ElementHasChilds.add(targetParent);
        }
    }
    
    /**
     * Ajoute un enfant spécifique à partir de la liste des 
     * enfants d'un autre parent
     * @param targetParent
     * @param sourceParent 
     * @param tagname nom de l'élement enfant à ajouter
     */
    public void mergeSpecificChild(NodeImpl targetParent, NodeImpl sourceParent, String tagname) {        
        getChildNodes(targetParent).addChild(getChildInNode((ElementNode) sourceParent, tagname));
        if (!ElementHasChilds.contains(targetParent)) {
            ElementHasChilds.add(targetParent);
        }
    }
  

    /**
     * Permutter la position de deux noeuds enfants
     * @param parent
     * @param node1
     * @param node2 
     */
    public void swapNodes(NodeImpl parent, NodeImpl node1, NodeImpl node2) {
        ArrayList<NodeImpl> children = getChildNodes(parent).getNodes();
        int i1 = children.indexOf(node1);
        int i2 = children.indexOf(node2);
        if (i1 != -1 && i2 != -1) {
            children.set(i1, node2);
            children.set(i2, node1);
        }
    }

    /**
     * Déplacer un enfant vers le haut
     * @param parent
     * @param child 
     */
    public void moveUp(NodeImpl parent, NodeImpl child) {
        ArrayList<NodeImpl> children = getChildNodes(parent).getNodes();
        int index = children.indexOf(child);
        if (index > 0) {
            children.remove(index);
            children.add(index - 1, child);
        }
    }

    /**
     * Déplacer un enfant vers le bas
     * @param parent
     * @param child 
     */
    public void moveDown(NodeImpl parent, NodeImpl child) {
        List<NodeImpl> children = getChildNodes(parent).getNodes();
        int index = children.indexOf(child);
        if (index != -1 && index < children.size() - 1) {
            children.remove(index);
            children.add(index + 1, child);
        }
    }  
    
    public boolean hasChildNodes(NodeImpl refNode) {
        return ((ElementNode) refNode).childNodes.getLength() > 0;
    }

    public NodeImpl cloneNode(NodeImpl refNode, boolean deep) {
        
        if (refNode instanceof ElementNode) {
            ElementNode clone = new ElementNode(refNode.getNodeName());
            if ((hasAttributes((ElementNode) refNode))) {
                for (int i = 0; i < ((ElementNode) refNode).attributes.getLength(); i++) {
                    NodeImpl attr = ((ElementNode) refNode).attributes.item(i);
                    clone.attributes.addAttribut(attr);
                }
            }
            
            if (deep) {
                for (int i = 0; i < getChildNodes(refNode).getLength(); i++) {
                    NodeImpl child = getChildNodes(refNode).item(i);
                    clone.childNodes.addChild(cloneNode(child, deep));
                }
            }            
            return clone;
        }else if(refNode instanceof AttributeNode){
            
        }
        return null;
    }

    public String getTextContent(NodeImpl refNode) throws HJAXException {
        StringBuilder content = new StringBuilder();
        NodeList children = getChildNodes(refNode);
        for (int i = 0; i < children.getLength(); i++) {
            NodeImpl child = children.item(i);
            if (child instanceof TextNode) {
                content.append(child.getNodeValue());
            } else if (child instanceof ElementNode) {
                content.append(getTextContent(child));
            }
        }
        return content.toString();
    }

    public void setTextContent(NodeImpl node, String textContent) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public boolean isSameNode(NodeImpl refNode, NodeImpl other) {
        return refNode == other;
    }

    //Element Interface ///////////////////////////////
    public String getTagName(ElementNode element) {
        return element.tagName;
    }

    public AttributeNode getAttribute(ElementNode element, String name) {
        if (hasAttributes(element)) {
            return null;
        }
        AttributeNode attr = (AttributeNode) getAttributes(element).getNamedItem(name);
        return (attr != null) ? attr : null;
    }

    public void setAttribute(ElementNode element, String name, String value) {
        AttributeNode attr = new AttributeNode(name, value, true);
        if (getAttributes(element).getNamedItem(name) == null) {

            if (attr.getValue().isEmpty()) {
                throw new InvalidAttributException("The value of an attribute"
                        + "cannot be empty");
            } else {
                getAttributes(element).addAttribut(attr);
            }
        }
    }

    public AttributeNode setAttributeNode(ElementNode element, AttributeNode newAttribute) {
        getAttributes(element).addAttribut(newAttribute);
        return newAttribute;
    }

    public void removeAttribute(ElementNode element, String name) throws HJAXException {
        if (getAttribute(element, name) == null) {
            throw new AttributeNotFoundException("Attribut <" + name + "> not found");
        }
        getAttributes(element).removeNamedItem(name);
    }

    /**
     * Ajoute un attribut à un Element donné
     * @param element
     * @param newAttribute
     * @return 
     */
    public AttributeNode addAttributeNode(ElementNode element, AttributeNode newAttribute) {
        getAttributes(element).addAttribut(newAttribute);
        return newAttribute;
    }

    /**
     * Retourne le nombre d'enfant d'un Element donné
     * @param element
     * @return 
     */
    public int getChildCount(ElementNode element) {
        return getChildNodes(element).getLength();
    }

    /**
     * Retourne le premier Enfant de type ElementNode d'un Element donné
     * @param element
     * @return 
     */
    public ElementNode getFirstElementChild(ElementNode element) {
        if (getChildCount(element) == 0) {
            return null;
        }
        List<ElementNode> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChildNodes(element).item(i);
            if (child instanceof ElementNode) {
                list.add((ElementNode) child);
            }
        }
        return list.getFirst();
    }

    /**
     * Retourne le dernier Enfant de type ElementNode d'un Element donné
     * @param element
     * @return 
     */
    public ElementNode getLastElementChild(ElementNode element) {
        if (getChildCount(element) == 0) {
            return null;
        }
        List<ElementNode> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChildNodes(element).item(i);
            if (child instanceof ElementNode) {
                list.add((ElementNode) child);
            }
        }
        return list.getLast();
    }

    /**
     * Retourne l'index d'un Element enfant
     * @param element
     * @param childNode
     * @return 
     */
    public int getIndexChild(ElementNode element, ElementNode childNode) {
        if (getChildCount(element) == 0) {
            return -1;
        }
        return getChildNodes(element).indexNode(childNode.getNodeName());
    }

    /**
     * Retourne si oui ou non un Element à au moins un attribut
     * @param element
     * @return 
     */
    public boolean hasAttributes(ElementNode element) {
        return getAttributes(element).getLength() > 0;
    }
    
    /**
     * Returns a child element by matching the attribute passed en as a parameter
     * @param element
     * @param attrname
     * @return
     */
    public ArrayList<ElementNode> getChildByAttribute(ElementNode element, String attrname) {

        ArrayList<ElementNode> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChildNodes(element).item(i);
            if (child instanceof ElementNode
                    && getAttributeNode((ElementNode) child, attrname) != null) {
                list.add((ElementNode) child);
            }
        }
        return list;
    }

    /**
     * Retourne l'Attribut correspond a <code>name</name>
     * @param element
     * @param name
     * @return 
     */
    public AttributeNode getAttributeNode(ElementNode element, String name) {
        if (!hasAttributes(element)) {
            return null;
        }
        return (AttributeNode) getAttributes(element).getNamedItem(name);
    }

    /**
     * Efface tous les attributs d'un ElementNode
     * @param element 
     */
    public void removeAllAttribute(ElementNode element) {
        getAttributes(element).removeAll();
    }
    

    //// Text Node ////////////////////////////////////////////////////
    
    
    public TextNode splitText(TextNode text, int offset) throws DOMException {

        String data = text.getData();

        if (offset < 0 || offset > data.length()) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "Invalid offset");
        }
        String newData = data.substring(offset);
        data = data.substring(0, offset);
        TextNode newTextNodeImpl = new TextNode(newData);

        // Insère juste après ce nœud
        NodeImpl parent = getParentNode(text);
        if (parent != null && parent instanceof NodeImpl) {
            int index = getChildNodes(parent).indexNode(text.getNodeName());
            if (index != -1) {
                getChildNodes(parent).addNodeInIndex(index + 1, newTextNodeImpl);
            }
        }
        return newTextNodeImpl;

    }

    public boolean isElementContentWhitespace(TextNode text) {
        return text.getData().isEmpty();
    }

    public String getWholeText(TextNode text) {
        StringBuilder sb = new StringBuilder();
        NodeImpl parent = getParentNode(text);
        if (parent != null) {
            NodeList siblings = getChildNodes(parent);
            for (int i = 0; i < siblings.getLength(); i++) {
                NodeImpl node = siblings.item(i);
                if (node instanceof TextNode) {
                    sb.append(((TextNode) node).getData());
                }
            }
        } else {
            sb.append(text.getData());
        }
        return sb.toString();
    }

    public TextNode replaceWholeText(TextNode text, String content) throws DOMException {
        if (getParentNode(text) == null) {
            text.setData(content);
            return text;
        }

        //On parcours à l'envers pour éviter les problèmes d'indexation
        //Quand on supprime en cours de boucle.
        NodeList siblings = getChildNodes(getParentNode(text));
        for (int i = siblings.getLength() - 1; i >= 0; i--) {
            NodeImpl node = siblings.item(i);
            if (node instanceof TextNode) {
                removeChild(getParentNode(text), node);
            }
        }
        TextNode newTextNodeImpl = new TextNode(content);
        appendChild(getParentNode(text), newTextNodeImpl);
        return newTextNodeImpl;
    }

    public String getData(TextNode text) throws HJAXException {
        return text.getData();
    }

    public void setData(TextNode text, String data) throws HJAXException {
        text.setData(data);
    }

    public int getLength(TextNode text) {
        return text.getData().length();
    }

    public String substringData(TextNode text, int offset, int count) throws HJAXException {
        if (offset < 0 || offset > text.getData().length() || count < 0) {
            //renvoie une erreur
        }
        int end = Math.min(offset + count, text.getData().length());
        return text.getData().substring(offset, end);
    }

    public void appendData(TextNode text, String newdata) throws HJAXException {
        String data = text.getData();
        data += newdata;
        text.setData(data);
    }

    public void insertData(TextNode text, int offset, String newdata) throws HJAXException {
        if (offset < 0 || offset > text.getData().length()) {
            //renvoie une erreur
        }
        String data = text.getData().substring(0, offset) + newdata + text.getData().substring(offset);
        text.setData(data);
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void deleteData(TextNode text, int offset, int count) throws HJAXException {
        if (offset < 0 || offset > text.getData().length()) {
            //renvoie une erreur
        }
        int end = Math.min(offset + count, text.getData().length());
        String data = text.getData().substring(0, offset) + text.getData().substring(end);
        text.setData(data);
    }

    public void replaceData(TextNode text, int offset, int count, String newdata) throws HJAXException {
        deleteData(text, offset, count);
        insertData(text, offset, newdata);
    }

}
