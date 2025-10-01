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

/**
 *
 * @author BANLOCK
 */
public class Document {

    private DocumentTypeNode doctype;
    private ElementNode rootElement = null;
    private final boolean xml11Version = false;
    private ArrayList<DocumentListener> listenerList = new ArrayList<>();

    //Stockage des noeuds
    private ArrayList<NodeImpl> tree = new ArrayList<>();
    private ArrayList<NodeImpl> nodesRegister = new ArrayList<>();
    private NameRegister nameRegister = new NameRegister();
    private TypeRegister typeRegister = new TypeRegister();
    private AttrRegister attrRegister = new AttrRegister();
    
    public Document() {
    }

    /// @param doc
    public void addDocumentListeners(DocumentListener doc) {
        listenerList.add(doc);
    }

    ///
    /// @return
    public ElementNode getDocumentElement() {
        return rootElement;
    }

    ///
    /// @param element
    public void setDocumentElement(ElementNode element) {
        if (getDocumentElement() != null) {
            throw new DomException((short) 1, "Document already has a root element");
        }
        this.rootElement = element;
    }

    ///
    /// @return
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

        nodesRegister.add(element);
        nameRegister.addNode(tagName, nodesRegister.size() - 1);
        typeRegister.addNode("ElementNode", nodesRegister.size() - 1);

        return element;
    }

    public AttributeNode createAttribute(String name, String value, boolean specified, ElementNode holderElement) {
        AttributeNode attr = new AttributeNode(name, value, specified);
        holderElement.attributes.addAttribut(attr);

        typeRegister.addNode("AttributNode", nodesRegister.size() - 1);
        attrRegister.addNode(name, nodesRegister.size() - 1);

        DocumentEvent evt = new DocumentEvent(attr, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {

                t.nodeAdded(evt);
            }
        });

        return attr;
    }

    public TextNode createTextNodeImpl(String data, ElementNode element) {
        if (!isXMLName(data, xml11Version)) {
            throw new InvalidCharacterException("Invalid data");
        }
        TextNode text = new TextNode(data, element);
        typeRegister.addNode("TextNode", nodesRegister.size() - 1);

        DocumentEvent evt = new DocumentEvent(text, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });

        return text;
    }

    public ArrayList<Integer> getNodesByAttribut(String attributeName) {
        return attrRegister.getNodes(attributeName);
    }

    public ArrayList<Integer> getNodesByType(String type) {
        return typeRegister.getNodes(type);
    }

    public ArrayList<NodeImpl> matching(ArrayList<Integer> list) {
        ArrayList<NodeImpl> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            int index = list.get(i);

            for (int j = 0; j < nodesRegister.size(); j++) {
                if (index == j) {
                    result.add(nodesRegister.get(j));
                }
            }
        }
        return result;
    }

    /**
     * Retourne si oui ou non au moins un noeud a cet attribut
     *
     * @param attrName
     * @return
     */
    public boolean existNodeOfAttribut(String attrName) {
        return !attrRegister.getNodes(attrName).isEmpty();
    }

    /**
     * Retourne le noeud dont le nom est passé en paramètre dans un type donné
     *
     * @param type
     * @param tagName le nom du noeud
     * @return
     */
    public NodeImpl getNodeByType(String type, String tagName) {
        return matching(typeRegister.getNodes(type)).getFirst();
    }

    public NodeImpl getElement(String name) {
        char initial = Character.toUpperCase(name.charAt(0));

        ArrayList liste = nameRegister.getNodes(initial);

        return (NodeImpl) matching(liste).getFirst();
    }

    /**
     * Retourne l'element dont le nom est <code>@tagName</code> ayant l'attribut
     * <code>@attrName</code> et la valeur est <code>value</code>
     *
     * @param attrName nom de l'attribut
     * @param value valeur de l'attribut
     * @return
     */
    public ElementNode getElement(String attrName, String value) {

        ArrayList<NodeImpl> list = matching(attrRegister.getNodes(attrName));

        for (int i = 0; i < list.size(); i++) {
            ElementNode element = (ElementNode) list.get(i);

            if (value.equalsIgnoreCase(element.attributes.getNamedItem(attrName).getValue())) {
                return element;
            }
        }
        return null;
    }
    
    /**
     * Retourne le noeud enfant correspondant au nom passé en paramètre
     * @param ParentNode
     * @param childname
     * @return 
     */
    public NodeImpl getChildInNode(ElementNode ParentNode, String childname) {
        NodeList childsNodes = getChilds(ParentNode);
        if (childsNodes.indexNode(childname) != -1) {
            return childsNodes.item(childsNodes.indexNode(childname));
        }
        return null;
    }

    public ElementNode getChildById(ElementNode element, String elementId) {
        for (int i = 0; i < getChilds(element).getLength(); i++) {
            if (getChilds(element).item(i) instanceof ElementNode
                    && getAttribute((ElementNode) (getChilds(element).item(i)), "id").equals(elementId)) {
                return (ElementNode) getChilds(rootElement).item(i);
            }
        }
        return null;
    }

    public NodeImpl removeNode(NodeImpl refNode, NodeImpl newParent) {

        if (refNode instanceof ElementNode) {

            if (!getChilds(rootElement).contain(refNode)) {
                throw new NodeNotFoundException("Not found node");
            }

            if (refNode == null) {
                // Message d'erreur : le noeud ne peut pas être null
            }

            if (getParent(refNode) == null) {
                throw new NodeHierarchyException("Impossible to delete root node");
            }

            if (newParent == null) {
                getChilds(refNode).removeAll();
                removeChild(getParent(refNode), refNode);
            } else {
                for (int i = 0; i < getChilds(refNode).getLength(); i++) {
                    appendChild(newParent, getChilds(refNode).item(i));
                }
                getChilds(refNode).removeAll();
                removeChild(getParent(refNode), refNode);
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

    public NodeImpl removeNode(NodeImpl refNode) {
        DocumentEvent evt = new DocumentEvent(refNode, this);
        listenerList.forEach(new Consumer<DocumentListener>() {

            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }
        });
        return removeNode(refNode, null);
    }

    public CommentNode createComment(String data) {

        CommentNode comment = new CommentNode(data);
        typeRegister.addNode("CommentNode", nodesRegister.size() - 1);

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
        typeRegister.addNode("EntityNode", nodesRegister.size() - 1);
        return new EntityNode(name);
    }

    public DocumentTypeNode createDocumentTypeImpl(String name, String publicId,
            String systemId, String internalSubSet) {

        DocumentTypeNode docType = new DocumentTypeNode(name, publicId, systemId, internalSubSet);
        typeRegister.addNode("DocumentTypeNode", nodesRegister.size() - 1);
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
        typeRegister.addNode("NotationNode", nodesRegister.size() - 1);
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

    public NodeImpl getParent(NodeImpl node) {

        if (node instanceof ElementNode) {
            for (int i = 0; i < tree.size(); i++) {

                NodeImpl parent = tree.get(i);
                NodeList ChildsNodeParent = getChilds(parent);

                if (hasAttributes((ElementNode) node) && hasChildNodes(node)) {
                    if (inThisSpecificList(ChildsNodeParent, true, true, node)) {
                        return parent;
                    }
                } else if (!hasAttributes((ElementNode) node) && hasChildNodes(node)) {
                    if (inThisSpecificList(ChildsNodeParent, false, true, node)) {
                        return parent;
                    }
                } else if (hasAttributes((ElementNode) node) && !hasChildNodes(node)) {
                    if (inThisSpecificList(ChildsNodeParent, true, false, node)) {
                        return parent;
                    }
                } else {
                    if (inThisSpecificList(ChildsNodeParent, false, false, node)) {
                        return parent;
                    }
                }
            }
        }
        return null;
    }
    
    public NodeImpl getParent (String tagName){
        return getParent(getElement(tagName));
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
            if (getParent(newChild) != null) {
                removeChild(getParent(newChild), newChild);
            }

            getChilds(refNode).addChild(newChild);
            
            if (refNode == rootElement) {
                newChild.depth = 1;
            }else{
                affectDepth(newChild);
            }
            
            tree.add(newChild);           

            return newChild;
        }
        return null;
    }
    
    /**
     * Retourne si oui ou non un element est dans une liste spécifiques
     *
     * @param list ici la liste dans laquelle le tri sera effectuée
     * @param hasAttr si oui ou non l'élément a des attributs
     * @param hasChilds si oui ou non l'élément a des enfants
     * @param node le noeud element pour qui on fait la vérification
     * @return
     */
    public boolean inThisSpecificList(NodeList list, boolean hasAttr, boolean hasChilds, NodeImpl node) {

        NodeList listBrowse = new NodeList();
        for (int j = 0; j < list.getLength(); j++) {
            NodeImpl currentChild = list.item(j);

            //Création de la liste correspondante
            if (hasAttr && hasChilds) {
                if (hasAttributes((ElementNode) currentChild)
                        && hasChildNodes(currentChild)) {
                    listBrowse.addChild(currentChild);
                }
            } else if (!hasAttr && hasChilds) {
                if (!hasAttributes((ElementNode) currentChild)
                        && hasChildNodes(currentChild)) {
                    listBrowse.addChild(currentChild);
                }
            } else if (hasAttr && !hasChilds) {
                if (hasAttributes((ElementNode) currentChild)
                        && !hasChildNodes(currentChild)) {
                    listBrowse.addChild(currentChild);
                }
            } else {
                if (!hasAttributes((ElementNode) currentChild)
                        && !hasChildNodes(currentChild)) {
                    listBrowse.addChild(currentChild);
                }
            }
        }
        return listBrowse.contain(node);
    }

    public int getDepth(NodeImpl node) {
        return node.depth;
    }

    public void affectDepth(NodeImpl nodeTarget) {
        int depth = 0;
        NodeImpl current = nodeTarget;

        if (nodeTarget == rootElement) {
            rootElement.depth = 0;
        }

        while (true) {
            NodeImpl parent = getParent(current);
            if (parent == null) { //Noeud racine
                break;
            }
            depth++;
            current = parent;
        }
        nodeTarget.depth = depth;
    }

    public NodeImpl whoIsSuperior(NodeImpl node1, NodeImpl node2) {
        if (getDepth(node1) - getDepth(node2) > 0) {
            return node1;
        } else if (getDepth(node1) - getDepth(node2) < 0) {
            return node2;
        }
        return null;
    }

    public NodeList getChilds(NodeImpl refNode) {
        if (refNode instanceof ElementNode) {
            return ((ElementNode) refNode).childNodes;
        }
        return null;
    }
    
    public NodeImpl getFirstChild(ElementNode element) {
        return hasChildNodes(element) ? getChilds(element).item(0) : null;
    }

    public NodeImpl getLastChild(ElementNode element) {
        return hasChildNodes(element) ? getChilds(element).item(getChilds(element).getLength() - 1) : null;
    }

    public NodeImpl getPreviousSibling(NodeImpl node) {
        NodeList children = getChilds(getParent(node));

        int index = children.indexNode(node.getNodeName());

        return children.item(index - 1);
    }

    public NodeImpl getNextSibling(NodeImpl node) {

        NodeList children = getChilds(getParent(node));

        int index = children.indexNode(node.getNodeName());

        return children.item(index + 1);

    }

    public AttributesList getAttributes(ElementNode element) {
        return element.attributes;
    }

    public NodeImpl insertBefore(NodeImpl refNode, NodeImpl newChild, NodeImpl refChild) throws HJAXException {
        if (refChild == null) {
            return appendChild(refNode, newChild);
        }

        int index = getChilds(refNode).indexNode(refChild.nodeName);

        if (index == -1) {
            // Fatal Error: Message d'erreur refChild n'existe pas dans la liste
        }
        getChilds(refNode).addNodeInIndex(index, newChild);
        return newChild;
    }

    public NodeImpl replaceChild(NodeImpl refNode, NodeImpl newChild, NodeImpl oldChild) throws HJAXException {
        int index = getChilds(refNode).indexNode(oldChild.getNodeName());

        getChilds(refNode).removeNode(oldChild.getNodeValue());

        getChilds(refNode).addNodeInIndex(index, newChild);

        return newChild;
    }

    public NodeImpl removeChild(NodeImpl refNode, NodeImpl oldChild) throws HJAXException {
        getChilds(refNode).removeNode(oldChild.getNodeName());
        return oldChild;
    }

    /**
     * Déplace un noeud enfant vers un autre parent
     *
     * @param fromParent Noeud source
     * @param toParent Noeud parent cible
     * @param child Noeud enfant
     */
    public void moveChild(NodeImpl fromParent, NodeImpl toParent, NodeImpl child) {
        if (getChilds(fromParent).contain(child)) {
            getChilds(fromParent).removeNode(child.nodeName);
            getChilds(toParent).addChild(child);
        }

        if (!tree.contains(toParent)) {
            tree.add(toParent);
        }

        if (!hasChildNodes(fromParent)) {
            tree.remove(fromParent);
        }
    }

    /**
     * Encapsule le noeud cible dans un nouveau noued
     *
     * @param target Noeud cible
     * @param wrapperName Nom du noeud à crée qui portera le noued cible
     * @return
     */
    public NodeImpl wrap(NodeImpl target, String wrapperName) {
        ElementNode wrapper = new ElementNode(wrapperName);
        getChilds(wrapper).addChild(target);

        if (target != rootElement) {
            appendChild(getParent(target), wrapper);
        }
        return wrapper;
    }

    /**
     * Supprime un noeud et rajoute la liste de tout ses enfant à son parent
     *
     * @param parent
     * @param child
     */
    public void unwrap(NodeImpl parent, NodeImpl child) {
        removeChild(parent, child);
        getChilds(parent).addAll(getChilds(child).getNodes());
    }

    /**
     * Duplique un enfant
     *
     * @param parent
     * @param child
     */
    public void duplicate(NodeImpl parent, NodeImpl child) {
        NodeImpl clone = cloneNode(child, true);
        appendChild(parent, clone);
    }

    /**
     * Remplace tous les noeuds enfants par une nouvelle collection de noeud
     *
     * @param parent
     * @param newChildren
     */
    public void replaceChildren(NodeImpl parent, ArrayList<NodeImpl> newChildren) {
        getChilds(parent).removeAll();
        getChilds(parent).addAll(newChildren);
        if (!tree.contains(parent) && !newChildren.isEmpty()) {
            tree.add(parent);
        }
    }

    /**
     * Ajoute une nouvelle collection d'enfant à partir de la liste des enfants
     * d'un autre parent
     *
     * @param targetParent
     * @param sourceParent
     */
    public void mergeChildren(NodeImpl targetParent, NodeImpl sourceParent) {
        getChilds(targetParent).addAll(getChilds(sourceParent).getNodes());
        if (!tree.contains(targetParent)) {
            tree.add(targetParent);
        }
    }

    /**
     * Ajoute un enfant spécifique à partir de la liste des enfants d'un autre
     * parent
     *
     * @param targetParent
     * @param sourceParent
     * @param tagname nom de l'élement enfant à ajouter
     */
    public void mergeSpecificChild(NodeImpl targetParent, NodeImpl sourceParent, String tagname) {
        getChilds(targetParent).addChild(getChildInNode((ElementNode) sourceParent, tagname));
        if (!tree.contains(targetParent)) {
            tree.add(targetParent);
        }
    }

    /**
     * Permutter la position de deux noeuds enfants
     *
     * @param parent
     * @param node1
     * @param node2
     */
    public void swapNodes(NodeImpl parent, NodeImpl node1, NodeImpl node2) {
        ArrayList<NodeImpl> children = getChilds(parent).getNodes();
        int i1 = children.indexOf(node1);
        int i2 = children.indexOf(node2);
        if (i1 != -1 && i2 != -1) {
            children.set(i1, node2);
            children.set(i2, node1);
        }
    }

    /**
     * Déplacer un enfant vers le haut
     *
     * @param parent
     * @param child
     */
    public void moveUp(NodeImpl parent, NodeImpl child) {
        ArrayList<NodeImpl> children = getChilds(parent).getNodes();
        int index = children.indexOf(child);
        if (index > 0) {
            children.remove(index);
            children.add(index - 1, child);
        }
    }

    /**
     * Déplacer un enfant vers le bas
     *
     * @param parent
     * @param child
     */
    public void moveDown(NodeImpl parent, NodeImpl child) {
        List<NodeImpl> children = getChilds(parent).getNodes();
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
                    AttributeNode attr = ((ElementNode) refNode).attributes.item(i);
                    clone.attributes.addAttribut(attr);
                }
            }

            if (deep) {
                for (int i = 0; i < getChilds(refNode).getLength(); i++) {
                    NodeImpl child = getChilds(refNode).item(i);
                    clone.childNodes.addChild(cloneNode(child, deep));
                }
            }
            return clone;
        } else if (refNode instanceof AttributeNode) {

        }
        return null;
    }

    public String getTextContent(NodeImpl refNode) throws HJAXException {
        StringBuilder content = new StringBuilder();
        NodeList children = getChilds(refNode);
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
     *
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
     *
     * @param element
     * @return
     */
    public int getChildCount(ElementNode element) {
        return getChilds(element).getLength();
    }

    /**
     * Retourne le premier Enfant de type ElementNode d'un Element donné
     *
     * @param element
     * @return
     */
    public ElementNode getFirstElementChild(ElementNode element) {
        if (getChildCount(element) == 0) {
            return null;
        }
        List<ElementNode> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChilds(element).item(i);
            if (child instanceof ElementNode) {
                list.add((ElementNode) child);
            }
        }
        return list.getFirst();
    }

    /**
     * Retourne le dernier Enfant de type ElementNode d'un Element donné
     *
     * @param element
     * @return
     */
    public ElementNode getLastElementChild(ElementNode element) {
        if (getChildCount(element) == 0) {
            return null;
        }
        List<ElementNode> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChilds(element).item(i);
            if (child instanceof ElementNode) {
                list.add((ElementNode) child);
            }
        }
        return list.getLast();
    }

    /**
     * Retourne l'index d'un Element enfant
     *
     * @param element
     * @param childNode
     * @return
     */
    public int getIndexChild(ElementNode element, ElementNode childNode) {
        if (getChildCount(element) == 0) {
            return -1;
        }
        return getChilds(element).indexNode(childNode.getNodeName());
    }

    /**
     * Retourne si oui ou non un Element à au moins un attribut
     *
     * @param element
     * @return
     */
    public boolean hasAttributes(ElementNode element) {
        return getAttributes(element).getLength() > 0;
    }

    /**
     * Returns a child element by matching the attribute passed en as a
     * parameter
     *
     * @param element
     * @param attrname
     * @return
     */
    public ArrayList<ElementNode> getChildByAttribute(ElementNode element, String attrname) {

        ArrayList<ElementNode> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChilds(element).item(i);
            if (child instanceof ElementNode
                    && getAttributeNode((ElementNode) child, attrname) != null) {
                list.add((ElementNode) child);
            }
        }
        return list;
    }

    /**
     * Retourne l'Attribut correspond a <code>name</name>
     *
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
     *
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
        TextNode newTextNodeImpl = new TextNode(newData, text.getHolderElement());

        // Insère juste après ce nœud
        NodeImpl parent = getParent(text);
        if (parent != null && parent instanceof NodeImpl) {
            int index = getChilds(parent).indexNode(text.getNodeName());
            if (index != -1) {
                getChilds(parent).addNodeInIndex(index + 1, newTextNodeImpl);
            }
        }
        return newTextNodeImpl;

    }

    public boolean isElementContentWhitespace(TextNode text) {
        return text.getData().isEmpty();
    }

    public String getWholeText(TextNode text) {
        StringBuilder sb = new StringBuilder();
        NodeImpl parent = getParent(text);
        if (parent != null) {
            NodeList siblings = getChilds(parent);
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
        if (getParent(text) == null) {
            text.setData(content);
            return text;
        }

        //On parcours à l'envers pour éviter les problèmes d'indexation
        //Quand on supprime en cours de boucle.
        NodeList siblings = getChilds(getParent(text));
        for (int i = siblings.getLength() - 1; i >= 0; i--) {
            NodeImpl node = siblings.item(i);
            if (node instanceof TextNode) {
                removeChild(getParent(text), node);
            }
        }
        TextNode newTextNodeImpl = new TextNode(content, text.getHolderElement());
        appendChild(getParent(text), newTextNodeImpl);
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
