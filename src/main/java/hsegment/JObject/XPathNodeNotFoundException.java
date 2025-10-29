/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

/**
 *
 * @author FIDELE
 */
public class XPathNodeNotFoundException extends RuntimeException {
    
    public XPathNodeNotFoundException(String NodeName){
        super("Noeud XPath: " + NodeName + "inexistant dans le document");
    }
    
}
