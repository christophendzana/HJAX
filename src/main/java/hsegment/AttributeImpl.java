/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.Attribute;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import DOM.ElementImpl;
import DOM.NodeImpl;
import Interface.Node;
import org.w3c.dom.TypeInfo;


/**
 * Herite de Node
 * Possède un nom
 * Possède une valeur
 * Peut contenir du texte
 * N'est pas considéré comme un enfant d'Element <code>getParentNode()</code>
 * doit toujour retourner null
 * @author PSM
 */
public class AttributeImpl extends NodeImpl implements Attribute {

    private String name;
    private String value;
    private NodeImpl parentNode;
    private ElementImpl holderElement;  
    private boolean specidied;
    protected DocumentImpl holderDocument;
    
    public AttributeImpl(String name, String value, DocumentImpl holderDocument, boolean specified){
        super(name, Node.ATTRIBUTE_NODE, holderDocument);
        this.name = name;
        this.value = value;
        this.specidied = specified;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean getSpecified() {
        // Détermine si l'Attribut a été explicitement defini
        return value !=null && !value.isEmpty();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) throws HJAXException {
        this.value = value;
        this.specidied = true; //attribut explicite
    }

    @Override
    public ElementImpl getHolderElement() {
        return holderElement;
    }
    
    @Override
    public TypeInfo getSchemaTypeInfo() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isId() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getNodeName() {
        return name;
    }

    @Override
    public String getNodeValue() throws HJAXException {
        return value;
    }

    @Override
    public void setNodeValue(String nodeValue) throws HJAXException {
        setValue(nodeValue);
    }

     @Override
    public String getTextContent() throws HJAXException {
        return value;
    }

    @Override
    public void setTextContent(String textContent) throws HJAXException {
        this.value = textContent;
    }
    
    public ElementImpl setHolderElement(ElementImpl element){
        this.holderElement = element;
        return element;
    }

    @Override
    public boolean isSameNode(NodeImpl other) {
        return other.getNodeName().equalsIgnoreCase(this.getNodeName()) &&                
                other.getNodeType() == this.getNodeType();
         
    }
    
    @Override
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    @Override
    public NodeImpl getParentNode() {
        return parentNode;
    }

    @Override
    public NodeListImpl getChildNodes() {
        return null;
    }

    @Override
    public NodeImpl getPreviousSibling() {
        return null;
    }

    @Override
    public NodeImpl getNextSibling() {
        return null;
    }

    

    @Override
    public NodeImpl cloneNode(boolean deep) {
        return new AttributeImpl(name, value, holderDocument, specidied);
    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    @Override
    public short compareDocumentPosition(NodeImpl other) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

   
    
}
