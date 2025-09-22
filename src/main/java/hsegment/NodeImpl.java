/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 * @author PSM
 */
public abstract class NodeImpl {

    protected String nodeName;
    protected String nodeValue;
    protected int depth;
       

    public NodeImpl(String name) {
        this.nodeName = name;        
    }    
    
    public String getNodeName() {
        return nodeName;
    }
    
    public String getNodeValue(){
        return nodeValue;
    }
   
}


