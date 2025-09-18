/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import java.util.ArrayList;

/**
 *
 * @author Banlock
 */
public class NodeList{
    
    public NodeList(){
        
    }
    
    private ArrayList <NodeImpl> nodes = new ArrayList<>() ;
        
    public NodeImpl item(int index) {
        if (index <0 || index >= getLength()) {
            return null;
        }
        return nodes.get(index);
    }    
    
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

    public void addChild(NodeImpl node){
        nodes.add(node);
    }
    
    public void addNodeInIndex(int index, NodeImpl node){
        nodes.set(index, node);
    }  
    
    public void removeAll(){
        nodes.clear();
    }
    
    public ArrayList getNodes(){
        return nodes;
    }
    
}
