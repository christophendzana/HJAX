/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

import hsegment.JObject.util.Dictionnary;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;

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
public class HDTD extends DTD{
    
    
    Dictionnary<Element> elements;
    
    Dictionnary<Notation> notations = 
            new Dictionnary<Notation>(10, 2, true, false);
    
    private final int status;
    public HDTD(String name, int status){
        super(name);
        
        //if(status != HDTDConstants.READABLE || status != HDTDConstants.WRITABLE)
            //throw new IllegalArgumentException("bad parameter");
        
        this.status = status;
        elements = new Dictionnary<Element>(27, 2, true, false);
    }
    
    public HDTD(String name){
        this(name, -1);
    }
    /**
     * return an element which name is <code>name</code> into DTD else
     * return null if element with that name doesn't exist into DTD
     * @param name element name wanted
     * @return an element which name is <code>name</code>
     */
    @Override
    public Element getElement(String name) {
        
        Element element = super.getElement(name);
        return super.getElement(name);//elements.get(name);
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
    
    
    
    public Element defineElement(String name, int type,
                       boolean omitStart, boolean omitEnd, ContentModel content,
                       BitSet exclusions, BitSet inclusions, AttributeList atts) {
        Element element = getElement(name);
        if(element == null){
            element = super.getElement(name);
        }
        element.type = type;
        element.oStart = omitStart;
        element.oEnd = omitEnd;
        element.content = content;
        element.exclusions = exclusions;
        element.inclusions = inclusions;
        element.atts = atts;
        
        return element;
    }
    
    public Notation defNotation(){
        return null;
    }
}
