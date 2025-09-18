/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;


/**
 *
 * @author FIDELE
 */
public class EntityNode extends NodeImpl {
    
     /** Entity name. */
    protected String name;
    protected String nodeValue;

    public EntityNode(String name) {
        super(name);
    }
         
    public String getName (){
        return name;
    }
    
    public String getValue(){
        return nodeValue;
    }
}
