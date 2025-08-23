/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *
 * @author FIDELE
 */
public class InvalidCharacterException extends RuntimeException {
    
    public InvalidCharacterException (){
        super();
    }
    
    public InvalidCharacterException (String message){
        super(message);
    }
    
    public InvalidCharacterException (String message, Throwable cause){
        super(message, cause);
    }
    
    public InvalidCharacterException (Throwable cause){
        super(cause);
    }
    
}
