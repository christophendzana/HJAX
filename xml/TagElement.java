/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.xml.parser.Element;

/**
 *
 * @author Ndzana Christophe
 */
public class TagElement {
    
    private Element element;
    
    public TagElement(Element element){
        this.element = element;
    }
    
    public Element getElement(){
        return element;
    }
}
