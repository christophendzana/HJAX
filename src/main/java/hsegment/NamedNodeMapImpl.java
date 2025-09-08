/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import APIDOMException.InvalidAttributException;
import Interface.NamedNodeMap;
import java.util.HashMap;

/**
 * Cette Classe permet de manipuler les attibuts
 * @author PSM
 */
public class NamedNodeMapImpl implements NamedNodeMap {

    private HashMap <String, AttributeImpl> attrs = new HashMap<>();    
    
    public NamedNodeMapImpl(HashMap<String, AttributeImpl> attributes){ 
        this.attrs = attributes;
    }    
       
    @Override
    public NodeImpl getNamedItem(String name) {
        return attrs.get(name);
    }

    @Override
    public NodeImpl setNamedItem(AttributeImpl attr)  {
       
        if (attrs == null) return null;
        
        if ( !(attr == null) ) {  
            throw new InvalidAttributException("Attribut cant be null");
        }        
        return attrs.put(attr.getNodeName(), attr);
    }

    @Override
    public NodeImpl removeNamedItem(String name) {
        return attrs.remove(name);
    }

    @Override
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

    @Override
    public int getLength() {
        return attrs.size();
    }
    
    public void removeAll(){
        attrs.clear();
    }    
    
    public HashMap<String , AttributeImpl> getMap(){
        return attrs;
    }
     
}
