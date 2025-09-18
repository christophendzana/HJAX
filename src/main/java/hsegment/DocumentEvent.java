/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package DOM;

import java.util.EventObject;

/**
 * @author FIDELE
 */
public class DocumentEvent extends EventObject {
    
    private transient Document doc;
    private int type;
    
    public DocumentEvent(NodeImpl source, Document doc) {
        super(source);
        this.doc = doc;        
    }        
    
    public int getNodeType(){      
        return type;
    }
    
    public Document getDocument(){
        return doc;
    }
}
