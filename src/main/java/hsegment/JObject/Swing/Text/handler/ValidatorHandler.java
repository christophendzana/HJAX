/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsegment.JObject.Swing.Text.handler;

/**
 *Handle Doctype and return value into doctype declaration
 * @author Ndzana Christophe
 */
public interface ValidatorHandler {
    
    
    public void handleValidator(String ValidatorName, String Location, String validatorFilePath);
    /**
     * get DTD's name
     * @return DTD's name
     */
    public String getValidatorName();
    
    /**
     * get Location Type. Either SYSTEM or PUBLIC
     * @return location type
     */
    public String getLocationType();
    
    /**
     * get DTD file path
     * @return DTD file path
     */
    public String ValidatorFilePath();
}
