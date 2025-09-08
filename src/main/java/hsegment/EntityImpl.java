/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;


/**
 *
 * @author FIDELE
 */
public class EntityImpl extends NodeImpl {
    
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
    
    protected String version;

    protected String notationName;    


    public EntityImpl(String name, short nodeType) {
        super(name, NodeImpl.ENTITY_NODE);
    }    
       
    public String getName (){
        return name;
    }
}
