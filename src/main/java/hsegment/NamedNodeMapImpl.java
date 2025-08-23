/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.NamedNodeMap;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cette Classe permet de manipuler les attibuts
 * @author PSM
 */
public class NamedNodeMapImpl implements NamedNodeMap {

    private final Map<String, NodeImpl> nodes;  
    
    public NamedNodeMapImpl(Map<String, ?extends NodeImpl> initialNodes){ // En plus de NodeImpl il peut contenir ses sous type 
        nodes = new LinkedHashMap<>(initialNodes);
    }    
       
    @Override
    public NodeImpl getNamedItem(String name) {
        return nodes.get(name);
    }

    @Override
    public NodeImpl setNamedItem(NodeImpl node) throws HJAXException {
        
        if (node == null) return null;
        
        if ( !(node instanceof AttributeImpl) ) {            
            // Message d'Erreur: Seul les Attributs peuvent être ajoutés.
        }
        
        return nodes.put(node.getNodeName(), node);
    }

    @Override
    public NodeImpl removeNamedItem(String name) throws HJAXException {
        return nodes.remove(name);
    }

    @Override
    public NodeImpl item(int index) {
        if (index < 0 || index >=nodes.size()) return null;
        
        int i=0;        
         
        // Si nodes comporte 200 attr (Processus lent)
        for(NodeImpl node : nodes.values()){
            if (i == index) {
                return node;
            }
            i++;
        }
        
       return null;
    }

    @Override
    public int getLength() {
        return nodes.size();
    }
    
    public void clear(){
        this.clear();
    }    
    
    public Map<String , NodeImpl> getNodes(){
        return nodes;
    }
     
}
