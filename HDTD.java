/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import hsegment.JObject.util.Dictionnary;
import java.util.BitSet;

/**
 *The representation of an SGML DTD.  DTD describes a document
 * syntax and is used in parsing of XML documents.  It contains
 * a list of elements and their attributes as well as a list of
 * entities defined in the DTD.
 *
 * @see Element
 * @see AttributeList
 * @see ContentModel
 * @see Parser
 * @author NDZANA Christophe
 */
public class HDTD{
    
    
    private Dictionnary<Element> elements;
    
    private Dictionnary<Notation> notations = 
            new Dictionnary<Notation>(10, 2, true, false);
    
    public HDTD(String name){
        elements = new Dictionnary<Element>(27, 2, true, false);
        
    }
    /**
     * return an element which name is <code>name</code> into DTD else
     * return null if element with that name doesn't exist into DTD
     * @param name element name wanted
     * @return an element which name is <code>name</code>
     */
    public Element getElement(String name) {
        return elements.get(name);
    }
    
    /**
     * Return Notation which name is <code>id</code> into DTD else 
     * return null if notation with that name doesn't exist into DTD
     * @param id notation name
     * @return notation wich name is <code>name</code> else null
     */
    public Notation getNotation(String id){
        return notations.get(id);
    }
    /**
     * Return element which name is <code>name</code>.if an element with name <code>name</code> already exist, 
     * it will return else a new one is instantiated an returned.
     * 
     * @param name the name of element to return
     * @return element wich name is <code>name</code>
     */
    public Element defineElement(String name){
        return defineElement(name, -1, true, null, null, null, null);
    }
    /**
     * Return element which name is <code>name</code> type is <code>type</code> and attribute is <code>atts</code> 
     * if an element with that name is not yet instantiated, a new one is; and returned
     * 
     * @param name element's name
     * @param type element' type
     * @param omitEnd true if close tag can be ommitted
     * @param content element's content Model
     * @param exclusions bitset of element which can be exclude to this element's child
     * @param inclusions bitset of elements wich can be include to this element's childs
     * @param atts element's attribute
     * @return  Element wich name is <code>name</code>
     */
    public Element defineElement(String name, int type,
                        boolean omitEnd, ContentModel content,
                       BitSet exclusions, BitSet inclusions, AttributeList atts) {
        
        Element element = getElement(name);
        if(element == null){
            element = new Element(name);
            element.type = type;
            element.oEnd = omitEnd;
            element.content = content;
            //element.exclusions = exclusions;
            //element.inclusions = inclusions;
            element.index = elements.size();
            element.atts = atts;
            elements.add(element);
        }
        
        return element;
    }
    
    public Notation defNotation(){
        return null;
    }
}
