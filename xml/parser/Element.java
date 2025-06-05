/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import java.io.Serializable;

/**
 *
 * @author Ndzana Christophe
 */
public class Element implements Serializable{
    
    private String name;
    
    protected Type type;
    
    protected int maxOccurs = 1;
    
    protected int minOccurs = 1;
    
    protected int index;
    
    protected ContentModel content;
    
    protected AttributeList atts;
    
    protected boolean oEnd;
    
    Element (){}
    protected Element(String name){
        this.name = name;
        
    }
    /**
     * Return this element's content model
     * @return this element's content model
     */
    public ContentModel getContentModel(){
        return content;
    }
    
    /**
     * Return this element name
     * @return this element Name
     */
    public final String getName(){
        return this.name;
    }
    
    /**
     * Get the attributes.
     *
     * @return  the {@code AttributeList} specifying the element
     */
    public AttributeList getAttributes() {
        return atts;
    }
    
    /**
     * Set this element name
     * @param name element name
     * @throws IllegalArgumentException if name is null
     */
    protected void setName(String name){
        if(name == null){
            throw new IllegalArgumentException("name can't be null");
        }
        this.name = name;
    }
    
    
    
    /**
     * set this element attribute 
     * @param atts this element attribute
     * @throws IllegalArgumentException if atts is null
     */
    protected void setAttributeList(AttributeList atts){
        if(atts == null)
            throw new IllegalArgumentException("name can't be null");
        this.atts = atts;
    }
    
    /**
     * set this element type
     * @param type this element type
     */
    protected void setType(Type type){
        this.type = type;
    }
    
    public String toString(){
        return this.name;
    }
}
