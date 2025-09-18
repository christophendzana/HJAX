/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 *C'est une déclaration qui défini un format de donnée non XML tel qu'une image
 * ou un fichier audio.
 * Elles sont utilisées pour spécifier le format de données d'un élément ou d'un 
 * attribut dans un document XML.
 * Déclaration dans un document XML <code>!NOTATION</code>
 * @author FIDELE
 */
public class NotationNode extends NodeImpl{
    
    
    protected String name;
    
    protected String publicId;
   
    protected String systemId;

    
    public NotationNode(String name){
        super(name);
        this.name = name;
    }
        
    public String getPublicId() {
        return publicId;
    }
   
    public String getSystemId() {
        return systemId;
    }
    
    public String getName(){
        return name;
    }

    public String getNodeName(){
        return name;
    }
   
}
