/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.xml.parser.Element;

/**
 *
 * @author Ndzana Christophe
 */
public interface Document {
    
    /**
     * Return the root element of this Document. The root element is the first
     * declared element to this document
     * @return root element of this document
     */
    public Element getRootElement();
    
    /**
     * Return element whose name correspond to <code>name</codde>
     * @param name name of element which want to get
     * @return element whose name correspond to <code>name</codde>
     */
    public Element getElement(String name);
}
