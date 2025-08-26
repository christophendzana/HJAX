/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import APIDOMException.*;
import Interface.*;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public class DocumentImpl extends NodeImpl implements Document {

    private DocumentType doctype;
    private ElementImpl documentElement;    
    private final boolean xml11Version = false;

    public DocumentImpl() {
        super("#document", NodeImpl.DOCUMENT_NODE, null);
        this.holderDocument = this; // le document est son propre holderDocument        
    }

    @Override
    public ElementImpl getDocumentElement() throws HJAXException {        
        return documentElement;
    }
    
    public void setDocumentElement(ElementImpl element){
        if (getDocumentElement() != null ) {
            throw new DomException("Document already has a root element");
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
        
        // Exception à lever si la syntax de tagname n'est pas correcte
        ElementImpl element = new ElementImpl(tagName, this);        
        return element;
    }

    @Override
    public AttributeImpl createAttribute(String name, String value, DocumentImpl holderDocument, boolean specified, ElementImpl holderElement){
        AttributeImpl attr = new AttributeImpl(name, value, this, specified);
        attr.setHolderElement(holderElement);
        return attr;
    }

    @Override
    public Text createTextNodeImpl(String data) {
        if (!isXMLName(data, xml11Version)) {
            throw new InvalidCharacterException("Invalid data");
        }
        TextImpl text = new TextImpl(data, this);
        return text;
    }

    @Override
    public NodeImpl getElementsByTagName(String tagname) {
        NodeListImpl childNode = this.getChildNodes();
        if (childNodes.indexNode(tagname) != -1) {
            return null;
        }
        return childNode.item(childNode.indexNode(tagname));
    }

    @Override
    public ElementImpl getElementById(String elementId) {

        for (int i = 0; i < this.getChildNodes().getLength(); i++) {
            if (this.getChildNodes().item(i) instanceof ElementImpl
                    && ((ElementImpl) this.getChildNodes().item(i)).getAttribute("id").equals(elementId)) {
                return (ElementImpl) this.getChildNodes().item(i);
            }
        }
        return null;
    }

    @Override
    public NodeImpl removeNode(NodeImpl refNode, NodeImpl newParent) {

        if (refNode.getNodeType() == NodeImpl.ELEMENT_NODE) {

            if (!this.getChildNodes().contain(refNode)) {
                throw new NodeNotFoundException("Not found node");
            }

            if (refNode == null) {
                // Message d'erreur : le noeud ne peut pas être null
            }

            if (refNode.getParentNode() == null) {
                throw new NodeHierarchyException("Impossible to delete root node");
            }

            if (newParent == null) {
                refNode.getChildNodes().removeAll();
                refNode.getParentNode().removeChild(refNode);
            } else {
                for (int i = 0; i < refNode.getChildNodes().getLength(); i++) {
                    newParent.appendChild(refNode.getChildNodes().item(i));
                }
                refNode.getChildNodes().removeAll();
                refNode.getParentNode().removeChild(refNode);
            }

        }

        return refNode;
    }

    public NodeImpl removeNode(NodeImpl refNode) {
        return removeNode(refNode, null);
    }

    @Override
    public NodeImpl renameNodeImpl(NodeImpl node, String namespaceURI, String qualifiedName) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public CommentImpl createCommentImpl(String data, DocumentImpl holderdocument) {
        return new CommentImpl(data, this);
    }

    public EntityImpl createEntityImpl(String name) {

        if (isXMLName(name, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }

        return new EntityImpl(name, NodeImpl.ENTITY_NODE, this);
    }

    public DocumentTypeImpl createDocumentTypeImpl(String name, String publicId,
            String systemId, String internalSubSet, DocumentImpl holderDocument) {
        
        return new DocumentTypeImpl(name, publicId, systemId, internalSubSet, this);
        
    }
    
    public NotationImpl createNotaionImpl(String name, short nodeType, DocumentImpl holderDocument){
        if (!isXMLName(name, xml11Version)) {
            throw new InvalidCharacterException("Invalid name");
        }
        return new NotationImpl(name, NodeImpl.NOTATION_NODE, this);
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

}
