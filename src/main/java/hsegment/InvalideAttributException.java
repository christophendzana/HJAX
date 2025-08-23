/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *
 * @author FIDELE
 */
public class InvalideAttributException extends RuntimeException {
    
    public InvalideAttributException (){
        super();
    }
    
    public InvalideAttributException (String message){
        super(message);
    }
    
    public InvalideAttributException (String message, Throwable cause){
        super(message, cause);
    }
    
    public InvalideAttributException (Throwable cause){
        super(cause);
    }
    
}
