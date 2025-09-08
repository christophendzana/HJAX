/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;


import Interface.*;
import java.util.LinkedHashMap;

/**
 *
 * @author FIDELE
 */
public class ElementImpl extends NodeImpl {

    protected String tagName;    
    protected NamedNodeMapImpl attributes;    

    public ElementImpl(String tagName) {
        super(tagName, Node.DOCUMENT_NODE);
        this.tagName = tagName;
        this.attributes = new NamedNodeMapImpl(new LinkedHashMap<>());
    }       
   
    
    @Override
    public short getNodeType(){
        return Node.ELEMENT_NODE;
    }

    @Override
    public String getNodeName(){
        return tagName;        
    }       
    
}
