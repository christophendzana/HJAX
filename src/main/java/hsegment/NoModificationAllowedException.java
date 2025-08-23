/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *
 * @author FIDELE
 */
public class NoModificationAllowedException extends RuntimeException {
    
     public NoModificationAllowedException(){
        super();
    }    
    
    public NoModificationAllowedException (String message){
        super(message);
    }
    
    public NoModificationAllowedException (String message, Throwable cause){
        super(message, cause);
    }
    
    public NoModificationAllowedException (Throwable cause){
        super(cause);
    }
    
}
