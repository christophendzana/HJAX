/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import APIDOMException.AttributeNotFoundException;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import Interface.*;
import APIDOMException.InvalideAttributException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.w3c.dom.Entity;

/**
 *
 * @author FIDELE
 */
public class ElementImpl extends NodeImpl implements Element {

    private String tagName;    
    private NamedNodeMapImpl attributes;    

    public ElementImpl(String tagName, DocumentImpl holderDocument) {
        super(tagName, Node.DOCUMENT_NODE, holderDocument);
        this.tagName = tagName;
        this.attributes = new NamedNodeMapImpl(new LinkedHashMap<>());
    }       

    @Override
    public String getTagName() {
        return tagName;
    }
    
    @Override
    public short getNodeType(){
        return Node.ELEMENT_NODE;
    }

    @Override
    public String getNodeName(){
        return tagName;        
    }   
    
    @Override
    public int getChildCount(){
        int count = 0;
        
        for (int i = 0; i < this.getChildCount(); i++) {
            count += count;
        }
        return count;
    }
    
    @Override
    public ElementImpl getFirstElementChild(){
        
        if (!hasChildNodes()) {
            return null;
        }
        
        for (int i = 0; i < this.getChildCount(); i++) {
            if (this.getChildNodes().item(i).getNodeType() == NodeImpl.ELEMENT_NODE) {
                return (ElementImpl) this.getChildNodes().item(i);
            }
        }
        return null;
    }
    
    //Liste des élément enfant par attribut
    public List<ElementImpl> getElementByAttribute (String attrname){
        
        List<ElementImpl> list = new ArrayList<>();
        for (int i = 0; i < this.getChildCount(); i++) {
            if (this.getChildNodes().item(i).getNodeType() == NodeImpl.ELEMENT_NODE 
                    && ((ElementImpl) this.getChildNodes().item(i)).getAttributeNode(attrname) != null ) {
                list.add( (ElementImpl) this.getChildNodes().item(i));
            }
        }
        return list;
    }
    
    public void removeAllChildren(){
        this.childNodes.removeAll();
    }
    
    @Override
    public NodeList getElementsByTagName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public boolean hasAttributes(){
        return attributes.getLength() != 1;
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.getNamedItem(name) != null;
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
     public NamedNodeMapImpl getAttributes(){
        return attributes;
    }
    
    //return the value of attribut
    @Override
    public String getAttribute(String name){
        if (!this.hasAttributes()) {
            return null; 
        }      
        AttributeImpl attr = (AttributeImpl) attributes.getNamedItem(name);
        return (attr != null) ? attr.getValue():"";
    }
    
    @Override
    public void setAttribute(String name, String value) throws HJAXException {
        AttributeImpl attr = new AttributeImpl(name, value, holderDocument, true);
        if (attributes.getNamedItem(name) == null ) {
            
            if (attr.getValue().isEmpty()) {
                throw new InvalideAttributException("The value of an attribute"
                        + "cannot be empty");
            }else{
                   attributes.setNamedItem(attr);
            }     
        }
    }

    @Override
    public void removeAttribute(String name) throws HJAXException {        
        if (!hasAttribute(name)) {            
            throw new AttributeNotFoundException("Attribut <" +name+ "> not find");
        }        
        attributes.removeNamedItem(name);
    }

    //return the node object correspondant
    @Override
    public AttributeImpl getAttributeNode(String name) {        
        if (!this.hasAttributes()) {
            return null;            
        }       
        return (AttributeImpl) this.getAttributes().getNamedItem(name);        
    }

    @Override
    public AttributeImpl setAttributeNode(AttributeImpl newAttribute) {
        attributes.setNamedItem(newAttribute);
        return newAttribute;
    }

    @Override
    public AttributeImpl removeAttributeNode(AttributeImpl oldAttribute) {
        attributes.removeNamedItem(oldAttribute.getName());
        return oldAttribute;
    }
    
    public void removeAllAttribute(){
        this.attributes.clear();
    }
    
    public ElementImpl getElementById(String value){
        
        if (this.getAttribute("id").equals(value)) {
            return this;
        }
        
        NodeListImpl children = this.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            NodeImpl child = children.item(i);
            if (child instanceof ElementImpl && ((ElementImpl) child).getAttribute("id").equals(value) ) {
               return (ElementImpl) child;
            }
        }
        return null;
    }
    
    public List<ElementImpl> getElementbyTagName(String TagName){
        List<ElementImpl> elements = new ArrayList<>();
        
        NodeListImpl children = this.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            NodeImpl child = children.item(i);
            
            if (child.getNodeName().equalsIgnoreCase(TagName) ) {
                elements.add( (ElementImpl) child);
            }            
        }        
        return elements;
    }  
}
