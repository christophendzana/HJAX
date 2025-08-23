/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import APIDOMException.NoModificationAllowedException;
import Interface.Entity;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author FIDELE
 */
public class EntityImpl extends NodeImpl implements Entity {
    
     /** Entity name. */
    protected String name;

    /** Identifiant public de la DTD */
    protected String publicId;

    /** System identifier. */
    protected String systemId;

    /** Encoding 
     * Spécifie l'encodage des caractères utilisé pour représenter un doc XML
     */
    protected String encoding;


    /** Input Encoding
     * Spécifie le codage des caractères utilisé pour lire les données
     * d'entrée, comme lors de la lecture d'un fichier XML
     */
    protected String inputEncoding;

    /** Version */
    protected String version;


    /** Notation name. */
    protected String notationName;

    /** base uri*/
    protected String baseURI;

    public EntityImpl(String name, short nodeType, DocumentImpl holderDocument) {
        super(name, NodeImpl.ENTITY_NODE, holderDocument);
    }
    
    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public String getNotationName() {
        return notationName;
    }

    @Override
    public String getInputEncoding() {
        return inputEncoding;
    }

    @Override
    public String getXmlEncoding() {
        return encoding;
    }

    @Override
    public String getXmlVersion() {
        return version;
    }
        
    ///////////////////////////////////////////////////////
    
    @Override
    public String getNodeName() {
        return name;
    }

    @Override
    public String getNodeValue() throws HJAXException {
        return name;
    }

    @Override
    public void setNodeValue(String nodeValue) throws HJAXException {
        throw new NoModificationAllowedException("No modification allowed");
    }
    
    @Override
    public short getNodeType() {
        return NodeImpl.ENTITY_NODE;
    }

    @Override
    public NodeImpl getParentNode() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
   
    
    public NodeImpl cloneNode(boolean deep) {
        return (NodeImpl) super.cloneNode(deep);
    }

    public void setPublicId(String id) {
        publicId = id;
    }
    
     public void setXmlEncoding(String value) {
        encoding = value;
    }
     
     public void setInputEncoding(String value){        
         this.inputEncoding = value ;
    } 
    
     public void setXmlVersion(String value) {        
        this.version = value;
    }
     
     public void setSystemId(String id) {
        this.systemId = id;
    }
    
    public void setNotationName(String name) {       
        notationName = name;
    } 
    
     public String getBaseURI() {
         // A modifier //////////////////////////////////
           return null;        
    }

    /** NON-DOM: set base uri*/
    public void setBaseURI(String uri){        
        baseURI = uri;
    }
     
    
}
