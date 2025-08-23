/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 *Manipulation d'un  noeud inexistant
 * @author FIDELE
 */

public class NodeNotFoundException extends RuntimeException {
    
    public NodeNotFoundException (){
        super();
    }
    
    public NodeNotFoundException (String message){
        super(message);
    }
    
    public NodeNotFoundException (String message, Throwable cause){
        super(message, cause);
    }
    
    public NodeNotFoundException (Throwable cause){
        super(cause);
    }
    
}
