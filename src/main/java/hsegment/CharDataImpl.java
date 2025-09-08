/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 *
 * @author PSM
 */
public abstract class CharDataImpl extends NodeImpl {

    public String data;
    
    public CharDataImpl(String data, short nodeType) {
        super("#Text", nodeType);
        this.data = data !=null ? data:"";
    }    
    
}
