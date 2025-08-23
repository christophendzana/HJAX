/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;
/**
 *Ajout d'un type de noeud interdit
 * @author FIDELE
 */
public class InvalidNodeTypeException extends RuntimeException {    
    
    public InvalidNodeTypeException(){
        super();
    }    
    
    public InvalidNodeTypeException (String message){
        super(message);
    }
    
    public InvalidNodeTypeException (String message, Throwable cause){
        super(message, cause);
    }
    
    public InvalidNodeTypeException (Throwable cause){
        super(cause);
    }
}
