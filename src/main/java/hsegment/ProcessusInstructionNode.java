/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 *
 * @author FIDELE
 */
public class ProcessusInstructionNode extends NodeImpl {
    
    private String target;
    private String instruction;

    public ProcessusInstructionNode(String target, String instruction) {
        super(target);
    }
    
    public String getTarget(){
        return target;
    }
    
    public String getInstruction(){
        return instruction;
    }
    
    public void setInstruction( String instruction ){
        this.instruction = instruction;
    }
        
    /**
     * Returns the target
     */
    public String getNodeName() {        
        return target;
    }

    
}
