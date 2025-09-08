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
public class DocumentImpl extends NodeImpl implements Document, DocumentNode, 
                                            Element, Entity, Text{

    private DocumentType doctype;
    private ElementImpl documentElement;    
    private final boolean xml11Version = false;
    ArrayList<DocumentListener> listenerList = new ArrayList<>();

    public DocumentImpl() {
        super("#document", NodeImpl.DOCUMENT_NODE);
    }

    public void addDocumentListeners(DocumentListener doc){
       listenerList.add(doc);
    }
    
    @Override
    public ElementImpl getDocumentElement() throws HJAXException {        
        return documentElement;
    }
    
    public void setDocumentElement(ElementImpl element){
        if (getDocumentElement() != null ) {
            throw new DomException( (short) 1, "Document already has a root element");
        }
        this.documentElement = element;
    }
    
    @Override
    public DocumentType getDoctype() {
        return doctype;
    }

    @Override
    public void setDoctype(DocumentType doctype) {
        this.doctype = doctype;
    }

    @Override
    public ElementImpl createElement(String tagName, Document holderDocument) throws HJAXException {
        
        if (!isXMLName(tagName, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }
        
        ElementImpl element = new ElementImpl(tagName);         
        
        DocumentEvent evt = new DocumentEvent(element, this);
        listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } );        
        return element;
    }

    @Override
    public AttributeImpl createAttribute(String name, String value, boolean specified, ElementImpl holderElement){
        AttributeImpl attr = new AttributeImpl(name, value, specified);
        this.appendChild(holderElement, attr);
        
        DocumentEvent evt = new DocumentEvent(attr, this);
        listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } );
        
        return attr;
    }

    @Override
    public TextImpl createTextNodeImpl(String data) {
        if (!isXMLName(data, xml11Version)) {
            throw new InvalidCharacterException("Invalid data");
        }
        TextImpl text = new TextImpl(data);
        
         DocumentEvent evt = new DocumentEvent(text, this);
        listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } );  
        
        return text;
    }

    @Override
    public NodeImpl getElementsByTagName(NodeImpl node, String tagname) {
        NodeListImpl childNode = getChildNodes(node);
        if (childNode.indexNode(tagname) != -1) {
            return childNode.item(childNode.indexNode(tagname));
        }
        return null;
    }

    @Override
    public ElementImpl getElementById(ElementImpl element, String elementId) {

        for (int i = 0; i < getChildNodes(element).getLength(); i++) {
            if (getChildNodes(element).item(i) instanceof ElementImpl
                    && getAttribute( (ElementImpl) (getChildNodes(element).item(i)), "id").equals(elementId)) {
                return (ElementImpl) getChildNodes(this).item(i);
            }
        }
        return null;
    }

    @Override
    public NodeImpl removeNodeImpl(NodeImpl refNode, NodeImpl newParent) {

        if (refNode.getNodeType() == NodeImpl.ELEMENT_NODE) {

            if (!getChildNodes(this).contain(refNode)) {
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
        listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeRemoved(evt);
            }
            
        } );
        return refNode;
    }

    public NodeImpl removeNodeImpl(NodeImpl refNode) {
        DocumentEvent evt = new DocumentEvent(refNode, this);
            listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } );  
        return removeNodeImpl(refNode, null);
    }

    
    
    @Override
    public NodeImpl renameNodeImpl(NodeImpl node, String namespaceURI, String qualifiedName) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public CommentImpl createCommentImpl(String data) {
        
            CommentImpl comment = new CommentImpl(data);
            
            DocumentEvent evt = new DocumentEvent(comment, this);
            listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } ); 
            
        return comment;
    }

    public EntityImpl createEntityImpl(String name) {

        if (isXMLName(name, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }
        return new EntityImpl(name, NodeImpl.ENTITY_NODE);
    }

    public DocumentTypeImpl createDocumentTypeImpl(String name, String publicId,
            String systemId, String internalSubSet) {
        
        DocumentTypeImpl docType = new DocumentTypeImpl(name, publicId, systemId, internalSubSet);
        
        DocumentEvent evt = new DocumentEvent(docType, this);
            listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } );         
        return docType;        
    }
    
    public NotationImpl createNotationImpl(String name, short nodeType){
        if (!isXMLName(name, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }
        
        NotationImpl notation = new NotationImpl(name, nodeType);
        
        DocumentEvent evt = new DocumentEvent(notation, this);
            listenerList.forEach(new Consumer<DocumentListener>(){
            @Override
            public void accept(DocumentListener t) {
                t.nodeAdded(evt);
            }            
        } ); 
        
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
    
    @Override
    public String getNodeValue(NodeImpl node) throws HJAXException {
        return node.getNodeValue();
    }

    @Override
    public void setNodeValue(NodeImpl node, String nodeValue) throws HJAXException {
        node.nodeValue = nodeValue;
    }

    @Override
    public NodeImpl getParentNode(NodeImpl node) {
        
        short typeNode = node.getNodeType();
        
        if (getElementsByTagName(documentElement, node.getNodeName()) != null ) {
            return documentElement;
        } else {
           NodeList children = getChildNodes(documentElement);         
           for (int i = 0; i < getChildCount(documentElement); i++) {
            NodeImpl child = children.item(i); 
               if (child.getNodeType() == typeNode) {
                   return getElementsByTagName(child, node.getNodeName());
               }              
        }
        }
        return null;
    }
    
    @Override
    public NodeListImpl getChildNodes(NodeImpl node) {
        return node.childNodes;
    }

    @Override
    public NodeImpl getFirstChild(NodeImpl node) {
        return hasChildNodes(node) ? getChildNodes(node).item(0) : null;
    }

    @Override
    public NodeImpl getLastChild(NodeImpl node) {
        return hasChildNodes(node) ? getChildNodes(node).item(getChildNodes(node).getLength()-1) : null;
    }

    @Override
    public NodeImpl getPreviousSibling(NodeImpl node) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public NodeImpl getNextSibling(NodeImpl node) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public NamedNodeMapImpl getAttributes(ElementImpl element) {
        return element.attributes;
    }

    @Override
    public NodeImpl insertBefore(NodeImpl refNode, NodeImpl newChild, NodeImpl refChild) throws HJAXException {
        if (refChild == null) {
            return appendChild(refNode, newChild);
        }

        int index = childNodes.indexNode(refChild.nodeName);

        if (index == -1) {
            // Fatal Error: Message d'erreur refChild n'existe pas dans la liste
        }
        getChildNodes(refNode).addNodeInIndex(index, newChild);
        appendChild(refNode, newChild);
        return newChild;
    }

    @Override
    public NodeImpl replaceChild(NodeImpl refNode, NodeImpl newChild, NodeImpl oldChild) throws HJAXException {
         int index = getChildNodes(refNode).indexNode(oldChild.getNodeName());

        getChildNodes(refNode).removeNode(oldChild.getNodeValue());

        getChildNodes(refNode).addNodeInIndex(index, newChild);        

        return newChild;
    }

    @Override
    public NodeImpl removeChild(NodeImpl refNode, NodeImpl oldChild) throws HJAXException {
        getChildNodes(refNode).removeNode(oldChild.getNodeName());
        return oldChild;
    }

    @Override
    public NodeImpl appendChild(NodeImpl refNode, NodeImpl newChild) throws HJAXException {
        if (newChild == null) {
            throw new java.lang.IllegalArgumentException("Le noeud en paramètre ne peut être null");
        }
        if (newChild == refNode) {
            throw new DomException(DomException.HIERARCHY_REQUEST_ERR, "Un noeud ne peut être enfant de lui même");
        }

        AllowedChildren allowedChildren = new AllowedChildren();

        if (!allowedChildren.isAllowed(this, newChild)) {
            
            throw new java.lang.IllegalArgumentException("Document can have only root Element");
        }
        
        if (refNode.getNodeType() == NodeImpl.DOCUMENT_NODE) {
            if (newChild.getNodeType() == NodeImpl.ELEMENT_NODE && hasTypeChildNode(refNode, newChild.getNodeType())) {
                throw new java.lang.IllegalArgumentException("Un Document ne peut avoir qu’un seul élément racine.");
            }
            if (newChild.getNodeType() == NodeImpl.DOCUMENT_TYPE_NODE && hasTypeChildNode(refNode, newChild.getNodeType())) {
                throw new java.lang.IllegalArgumentException("Un Document ne peut avoir qu’un seul DocumentType.");
            }
        }

        // newchild ne peut avoir deux parents
        if (getParentNode(newChild) != null) {
            removeChild(getParentNode(newChild), newChild);
        }
        
        
        getChildNodes(refNode).addNode(newChild);
        return newChild;
    }

    public boolean hasTypeChildNode(NodeImpl refNode, short type ){
        for (int i = 0; i < getChildNodes(refNode).getLength(); i++) {
            short typeChild  = getChildNodes(refNode).item(i).getNodeType();
            return type == typeChild;
        }
        return false;
    }
    
    @Override
    public boolean hasChildNodes(NodeImpl refNode) {
        return this.getChildNodes(refNode).getLength() > 0;
    }

    @Override
    public NodeImpl cloneNode(NodeImpl node, boolean deep) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean hasAttributes(NodeImpl node) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public short compareDocumentPosition(NodeImpl other) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getTextContent(NodeImpl refNode) throws HJAXException {
        StringBuilder content = new StringBuilder();
        NodeListImpl children = getChildNodes(refNode);
        for (int i = 0; i < children.getLength(); i++) {
            NodeImpl child = children.item(i);
            if (child instanceof TextImpl) {
                content.append(child.getNodeValue());
            } else if (child instanceof ElementImpl) {
                content.append(getTextContent(child));
            }
        }
        return content.toString();
    }

    @Override
    public void setTextContent(NodeImpl node, String textContent) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isSameNode(NodeImpl refNode, NodeImpl other) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    
    //Element Interface ///////////////////////////////
    
    @Override
    public String getTagName(ElementImpl element) {
        return element.tagName;
    }
   
    @Override
    public String getAttribute(ElementImpl element, String name) {
        if (hasAttributes(element)) {
            return null; 
        }      
        AttributeImpl attr = (AttributeImpl) getAttributes(element).getNamedItem(name);
        return (attr != null) ? attr.getValue():null;
    }

    @Override
    public void setAttribute(ElementImpl element, String name, String value) {
        AttributeImpl attr = new AttributeImpl(name, value, true);
        if (getAttributes(element).getNamedItem(name) == null ) {
            
            if (attr.getValue().isEmpty()) {
                throw new InvalidAttributException("The value of an attribute"
                        + "cannot be empty");
            }else{
                   getAttributes(element).setNamedItem(attr);
            }     
        }
    }    
    
    public AttributeImpl setAttributeNode(ElementImpl element, AttributeImpl newAttribute) {
        getAttributes(element).setNamedItem(newAttribute);
        return newAttribute;
    }
    
    @Override
    public void removeAttribute(ElementImpl element, String name) throws HJAXException {
        if (!hasAttribute(element, name)) {            
            throw new AttributeNotFoundException("Attribut <" +name+ "> not find");
        }        
        getAttributes(element).removeNamedItem(name);
    }
    
    @Override
    public AttributeImpl addAttributeNode(ElementImpl element, AttributeImpl newAttribute){
        getAttributes(element).setNamedItem(newAttribute);
        return newAttribute;
    }

    @Override
    public int getChildCount(ElementImpl element) {
        return getChildNodes(element).getLength();
    }

    @Override
    public ElementImpl getFirstElementChild(ElementImpl element) {
        if (getChildCount(element) == 0 ) {
            return null;
        }        
        List<ElementImpl> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChildNodes(element).item(i);                     
            if (child.getNodeType() == NodeImpl.ELEMENT_NODE ) {                 
                list.add( (ElementImpl) child);
            }
        }
        return list.getFirst();
    }
    
    @Override
    public ElementImpl getLastElementChild(ElementImpl element) {
        if (getChildCount(element) == 0 ) {
            return null;
        }        
        List<ElementImpl> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChildNodes(element).item(i);                     
            if (child.getNodeType() == NodeImpl.ELEMENT_NODE ) {                 
                list.add( (ElementImpl) child);
            }
        }
        return list.getLast();
    }
   
    public int getIndexChild (ElementImpl element, ElementImpl childNode ){
        if ( getChildCount(element) == 0 ) {
            return -1;
        } 
           return getChildNodes(element).indexNode(childNode.getNodeName());        
    }
    
    @Override
    public boolean hasAttributes(ElementImpl element) {
        return getAttributes(element).getLength() > 0;
    }

    @Override
    public boolean hasAttribute(ElementImpl element, String name) {
        return getAttribute(element, name) == null;
    }

    @Override
    public void setIdAttribute(ElementImpl element, String name, boolean isId) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /**
    *Returns a child element by matching the attribute passed en as a parameter    
     * @param element
     * @param attrname
     * @return 
    */
    public List<ElementImpl> getElementExempterAttribute (ElementImpl element, String attrname){
        
        List<ElementImpl> list = new ArrayList<>();
        for (int i = 0; i < getChildCount(element); i++) {
            NodeImpl child = getChildNodes(element).item(i);
            if (child.getNodeType() == NodeImpl.ELEMENT_NODE 
                    &&  getAttributeNode( (ElementImpl) child, attrname) != null ) {
                list.add( (ElementImpl) child);
            }
        }
        return list;
    }    
    
     public AttributeImpl getAttributeNode(ElementImpl element, String name) {        
        if (!hasAttributes(element)) {
            return null;            
        }       
        return (AttributeImpl) getAttributes(element).getNamedItem(name);        
    }
    
    public void removeAllAttribute(ElementImpl element){
        getAttributes(element).removeAll();
    }

    
    /// Entity Interface ///////////////////////////////////////////////
    
    @Override
    public String getPublicId(EntityImpl entity) {
       return entity.publicId;
    }

    @Override
    public String getSystemId( EntityImpl entity ) {
        return entity.systemId;
    }

    @Override
    public String getNotationName(EntityImpl entity) {
        return entity.notationName;
    }

    @Override
    public String getInputEncoding(EntityImpl entity) {
        return entity.inputEncoding;
    }

    @Override
    public String getXmlEncoding(EntityImpl entity) {
        return entity.encoding;
    }

    @Override
    public String getXmlVersion(EntityImpl entity) {
        return entity.version;
    }
    
    public void setPublicId(EntityImpl entity, String id) {
        entity.publicId = id;
    }
    
     public void setXmlEncoding(EntityImpl entity, String value) {
        entity.encoding = value;
    }
     
     public void setInputEncoding(EntityImpl entity, String value){        
         entity.inputEncoding = value ;
    } 
    
     public void setXmlVersion(EntityImpl entity, String value) {        
        entity.version = value;
    }
     
     public void setSystemId(EntityImpl entity, String id) {
        entity.systemId = id;
    }
    
    public void setNotationName(EntityImpl entity, String name) {       
        entity.notationName = name;
    }        

    //// Text Interface ////////////////////////////////////////////////////
    
    @Override
    public TextImpl splitText(TextImpl text, int offset) throws DOMException {
        
        String data = text.getData();
        
        if (offset < 0 || offset > data.length()) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "Invalid offset");
        }
        String newData = data.substring(offset);
        data = data.substring(0, offset);
        TextImpl newTextNodeImpl = new TextImpl(newData);

        // Insère juste après ce nœud
            NodeImpl parent = getParentNode(text);
        if (parent != null && parent instanceof NodeImpl) {
            int index = getChildNodes(parent).indexNode(this.getNodeName());
            if (index != -1) {
                getChildNodes(parent).addNodeInIndex(index + 1, newTextNodeImpl);               
            }
        }
        return newTextNodeImpl;
        
    }

    @Override
    public boolean isElementContentWhitespace(TextImpl text) {
        return text.getData().isEmpty();
    }

    @Override
    public String getWholeText(TextImpl text) {
        StringBuilder sb = new StringBuilder();
        NodeImpl parent = getParentNode(text);
        if (parent != null) {
            NodeListImpl siblings = getChildNodes(parent);
            for (int i = 0; i < siblings.getLength(); i++) {
                NodeImpl node = siblings.item(i);
                if (node.getNodeType() == NodeImpl.TEXT_NODE) {
                    sb.append(((TextImpl) node).getData());
                }
            }
        } else {
            sb.append(text.getData());
        }
        return sb.toString();
    }

    @Override
    public TextImpl replaceWholeText(TextImpl text, String content) throws DOMException {
         if (getParentNode(text) == null) {
            text.setData(content);
            return text;
        }

        //On parcours à l'envers pour éviter les problèmes d'indexation
        //Quand on supprime en cours de boucle.
        NodeListImpl siblings = getChildNodes(getParentNode(text));
        for (int i = siblings.getLength() - 1; i >= 0; i--) {
            NodeImpl node = siblings.item(i);
            if (node.getNodeType() == NodeImpl.TEXT_NODE) {
                removeChild(getParentNode(text), node);
            }
        }
        TextImpl newTextNodeImpl = new TextImpl(content);
        appendChild(getParentNode(text), newTextNodeImpl);
        return newTextNodeImpl;
    }

    @Override
    public String getData(TextImpl text) throws HJAXException {
        return text.getData();
    }

    @Override
    public void setData(TextImpl text, String data) throws HJAXException {                
        text.setData(data);
    }

    @Override
    public int getLength(TextImpl text) {        
        return text.getData().length();
    }

    @Override
    public String substringData(TextImpl text, int offset, int count) throws HJAXException {
        if (offset<0 || offset > text.getData().length() || count<0) {
            //renvoie une erreur
        }
        int end = Math.min(offset + count, text.getData().length());
        return text.getData().substring(offset, end);
    }

    @Override
    public void appendData(TextImpl text, String newdata) throws HJAXException {
       String data = text.getData();
       data += newdata;
       text.setData(data);
    }

    @Override
    public void insertData(TextImpl text, int offset, String newdata) throws HJAXException {
        if (offset<0 || offset > text.getData().length()) {
            //renvoie une erreur
        }        
        String data = text.getData().substring(0, offset) + newdata + text.getData().substring(offset);
        text.setData(data);
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void deleteData(TextImpl text, int offset, int count) throws HJAXException {
        if (offset<0 || offset > text.getData().length()) {
            //renvoie une erreur
        }   
        int end = Math.min(offset + count, text.getData().length());
        String data = text.getData().substring(0, offset) + text.getData().substring(end);
        text.setData(data);
    }

    @Override
    public void replaceData(TextImpl text, int offset, int count, String newdata) throws HJAXException {
        deleteData(text, offset, count);
        insertData(text, offset, newdata);
    }
    
}
