/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsegment.JObject.Swing.Text;

/**
 *Handle Doctype and return value into doctype declaration
 * @author Ndzana Christophe
 */
public interface DoctypeHandler {
    
    
    
    /**
     * get DTD's name
     * @return DTD's name
     */
    public String getDTDName();
    
    /**
     * get Location Type. Either SYSTEM or PUBLIC
     * @return location type
     */
    public String getLocationType();
    
    /**
     * get DTD file path
     * @return DTD file path
     */
    public String dtdFilePath();
}
