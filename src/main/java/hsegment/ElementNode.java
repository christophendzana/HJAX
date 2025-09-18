/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;


import java.util.LinkedHashMap;

/**
 *
 * @author FIDELE
 */
public class ElementNode extends NodeImpl {

    protected String tagName;    
    protected AttributesListNode attributes;    
    protected NodeList childNodes;
    
    
    
    public ElementNode(String tagName) {
        super(tagName);
        this.tagName = tagName;
        this.attributes = new AttributesListNode(new LinkedHashMap<>());
    }       
   
    @Override
    public String getNodeName(){
        return tagName;        
    }       
    
    public boolean getThisChild(NodeImpl refNode){
        return this.childNodes.contain(refNode);
    }
    
}
