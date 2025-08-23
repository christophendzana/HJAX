/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

/**
 *
 * @author FIDELE
 */
public interface Notation extends Node {
    
    /**
     * The public identifier of this notation. If the public identifier was
     * not specified, this is <code>null</code>.
     */
    public String getPublicId();

    /**
     * The system identifier of this notation. If the system identifier was
     * not specified, this is <code>null</code>. This may be an absolute URI
     * or not.
     */
    public String getSystemId();
    
}
