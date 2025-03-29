/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml;

import javax.swing.text.html.parser.DTDConstants;

/**
 *
 * @author NDZANA Christophe
 */
public interface HDTDConstants extends DTDConstants{
    
    /**
     * Used to initialisate DTD. Make DTD to be only to status Readable
     * in this case DTD can't able to add new Element 
     */
    public static final int READABLE = 1;
    
    /**
     * Used to initialisate DTD. Make DTD to be only to status writable
     * in this case DTD can be able to add new Element 
     */
    public static final int WRITABLE = 2;
    
    /**
     * Variable using by DTD parser for Declaring element into DTD 
     */
    public static final String ELEMENT = "ELEMENT";
    /**
     * Variable using by DTD parser for Declaring Attribute into DTD 
     */
    public static final String ATTRIBUTE = "ATTRIBUTE";
    /**
     * Variable using by DTD parser for Declaring entity into DTD 
     */
    public static final String ENTITY = "ENTITY";
}
