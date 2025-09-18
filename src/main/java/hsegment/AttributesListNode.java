/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import APIDOMException.InvalidAttributException;
import java.util.HashMap;

/**
 * Cette Classe permet de manipuler les attibuts
 * @author PSM
 */
public class AttributesListNode{

    private HashMap <String, NodeImpl> attrs = new HashMap<>();    
    
    public AttributesListNode(HashMap<String, NodeImpl> attributes){ 
        this.attrs = attributes;
    }    
       
    public NodeImpl getNamedItem(String name) {
        return attrs.get(name);
    }

    public NodeImpl addAttribut(NodeImpl attr)  {
       
        if (attrs == null) return null;
        
        if ( !(attr == null) ) {  
            throw new InvalidAttributException("Attribut cant be null");
        }        
        return attrs.put(attr.getNodeName(), attr);
    }

    public NodeImpl removeNamedItem(String name) {
        return attrs.remove(name);
    }

    public NodeImpl item(int index) {
        if (index < 0 || index >=attrs.size()) return null;
        
        int i=0;        
         
        // Si nodes comporte 200 attr (Processus lent)
        for(NodeImpl node : attrs.values()){
            if (i == index) {
                return node;
            }
            i++;
        }
        
       return null;
    }

    public int getLength() {
        return attrs.size();
    }
    
    public void removeAll(){
        attrs.clear();
    }    
    
    public HashMap<String , NodeImpl> getMap(){
        return attrs;
    }
     
}
