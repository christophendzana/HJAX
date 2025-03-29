/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.process;

import hsegment.JObject.Swing.Text.handler.DoctypeHandler;

/**
 *Abstract class which is implementation of {@link DoctypeHandler} to store doctype value;
 * @author Ndzana Christophe
 */
public abstract class HandleDoctype implements DoctypeHandler{
    
    
    /**
     * handle Doctype element like DTDT's name, DTD file path
     * @param dtdName DTD's name
     * @param locationType location type either SYSTEM or PUBLIC
     * @param dtdFilePath DTD's file
     */
    protected abstract void handleDoctype(final String dtdName, final String locationType, final String dtdFilePath);
}
