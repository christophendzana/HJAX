/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.Attribute;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

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
    private ElementImpl holderElement;  
    private boolean specidied;
    
    public AttributeImpl(String name, String value, boolean specified){
        super(name, Node.ATTRIBUTE_NODE);
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
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }
    
}
