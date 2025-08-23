/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIDOMException;

/**
 * Hierarchie non conforme
 * @author FIDELE
 */
public class NodeHierarchyException extends RuntimeException {
    
    public NodeHierarchyException(){
        super();
    }    
    
    public NodeHierarchyException (String message){
        super(message);
    }
    
    public NodeHierarchyException (String message, Throwable cause){
        super(message, cause);
    }
    
    public NodeHierarchyException (Throwable cause){
        super(cause);
    }
    
}
