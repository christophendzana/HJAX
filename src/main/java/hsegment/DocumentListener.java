/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.DocumentEvent;

/**
 *
 * @author FIDELE
 */
public interface DocumentListener {
    
    public void nodeRemoved(DocumentEvent evt);
    
    public void nodeAdded( DocumentEvent evt);
    
    public void nodeModified (DocumentEvent evt);
    
    public void nodeinsered (DocumentEvent evt);
    
    public void nodreplaced (DocumentEvent evt);

    public void nodecloned (DocumentEvent evt);
    
}
