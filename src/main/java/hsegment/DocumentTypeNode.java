/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 * @author PSM
 */
public class DocumentTypeNode extends NodeImpl {
       
    protected String name;
    
    protected String publicId;
    protected String systemId;
    protected String internalSubset;

    public DocumentTypeNode(String name, String publicId, String systemId, String internalSubset) {
        super(name);
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
        this.internalSubset = internalSubset;        
       
    }

    // Retourne le nom du type de document
    
    public String getName() {
        return name;
    }
    
    // Identifiant public de la DTD
    
    public String getPublicId() {
        return publicId;
    }

    // Identifiant système (URL) de la DTD
    
    public String getSystemId() {
        return systemId;
    }
    
    // Sous-ensemble interne (ex: définitions internes dans <!DOCTYPE ... [ ... ]>)    
    public String getInternalSubset() {
        return internalSubset;
    }
        
    public void setInternalSubset(String data){
        this.internalSubset = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<!DOCTYPE ").append(getName());
        if (publicId != null) sb.append(" PUBLIC \"").append(publicId).append("\"");
        if (systemId != null) sb.append(" \"").append(systemId).append("\"");
        if (internalSubset != null) sb.append(" [").append(internalSubset).append("]");
        sb.append(">");
        return sb.toString();

    }
}