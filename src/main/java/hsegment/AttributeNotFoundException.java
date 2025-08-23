/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *Manipulation d'un attribut inexistant
 * @author FIDELE
 */
public class AttributeNotFoundException extends RuntimeException {
    
    public AttributeNotFoundException(){
        super();
    }
    
    public AttributeNotFoundException(String message){
        super(message);
    }    
    
    public AttributeNotFoundException (String message, Throwable cause){
        super(message, cause);
    }
    
    public AttributeNotFoundException (Throwable cause){
        super(cause);
    }
    
}
