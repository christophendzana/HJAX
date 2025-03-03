/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

import java.util.BitSet;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Entity;

/**
 *
 * @author Ndzana christophe
 */
public class DefaultDTD extends DTD{
    
    
    public DefaultDTD(String name){
        super(name);
    }
    
    
    @Override
    public Element getElement(String name){
        
        Element element = super.getElement(name);
        elements.remove(element);
        elementHash.remove(element);
        return element;
    }
    
    @Override
    public Element defineElement(String name, int type,
                       boolean omitStart, boolean omitEnd, ContentModel content,
                       BitSet exclusions, BitSet inclusions, AttributeList atts) {
        return null;
    }
    
    public Entity defineEntity(String name, int type, char[] data) {
        return null;
    }
}
