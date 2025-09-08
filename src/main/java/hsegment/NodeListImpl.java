/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.NodeList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Banlock
 */
public class NodeListImpl implements NodeList {
    
    private final List <NodeImpl> nodes = new ArrayList<>() ;
    
    @Override
    public NodeImpl item(int index) {
        if (index <0 || index >= getLength()) {
            return null;
        }
        return nodes.get(index);
    }
    
    @Override
    public int getLength() {
        return nodes.size();
    }
    
    public int indexNode(String name){
        return nodes.indexOf(name);
    }
    
    public boolean contain(NodeImpl node){
        String name = node.getNodeName();        
        return (indexNode(name)== -1) ;        
    }
    
    public void removeNode(String name){
        nodes.remove(indexNode(name));
    }

    public void addNode(NodeImpl node){
        nodes.add(node);
    }
    
    public void addNodeInIndex(int index, NodeImpl node){
        nodes.set(index, node);
    }
  
    public Boolean hasChilds(){
       return nodes.isEmpty() ;
    }
    
    public void removeAll(){
        nodes.clear();
    }
    
}
