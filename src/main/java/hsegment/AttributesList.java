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
public class AttributesList{

    private HashMap <String, AttributeNode> attrs = new HashMap<>();    
    
    public AttributesList(HashMap<String, AttributeNode> attributes){ 
        this.attrs = attributes;
    }    
       
    public AttributeNode getNamedItem(String name) {
        return attrs.get(name);
    }

    public AttributeNode addAttribut(AttributeNode attr)  {
       
        if (attrs == null) return null;
        
        if ( !(attr == null) ) {  
            throw new InvalidAttributException("Attribut cant be null");
        }        
        return attrs.put(attr.getNodeName(), attr);
    }

    public NodeImpl removeNamedItem(String name) {
        return attrs.remove(name);
    }

    public AttributeNode item(int index) {
        if (index < 0 || index >=attrs.size()) return null;
        
        int i=0;        
         
        // Si nodes comporte 200 attr (Processus lent)
        for(AttributeNode node : attrs.values()){
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
    
    public HashMap<String , AttributeNode> getList(){
        return attrs;
    }
     
}
