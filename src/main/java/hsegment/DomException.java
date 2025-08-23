/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *
 * @author FIDELE
 */
public class DomException extends RuntimeException {
    
    public DomException(){
        super();
    } 
    
    public DomException(String message){
        super(message);
    }    
    
    public DomException (String message, Throwable cause){
        super(message, cause);
    }
    
    public DomException (Throwable cause){
        super(cause);
    }
}
