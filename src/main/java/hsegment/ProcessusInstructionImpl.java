/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 *
 * @author FIDELE
 */
public class ProcessusInstructionImpl extends NodeImpl {
    
    private String target;
    private String instruction;

    public ProcessusInstructionImpl(String target, String instruction, DocumentImpl holderDocument) {
        super(target, NodeImpl.PROCESSING_INSTRUCTION_NODE, holderDocument);
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
    
    public short getNodeType() {
        return NodeImpl.PROCESSING_INSTRUCTION_NODE;
    }

    /**
     * Returns the target
     */
    public String getNodeName() {        
        return target;
    }

    
}
