/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.Notation;

/**
 *C'est une déclaration qui défini un format de donnée non XML tel qu'une image
 * ou un fichier audio.
 * Elles sont utilisées pour spécifier le format de données d'un élément ou d'un 
 * attribut dans un document XML.
 * Déclaration dans un document XML <code>!NOTATION</code>
 * @author FIDELE
 */
public class NotationImpl extends NodeImpl implements Notation {
    
    
    protected String name;
    
    protected String publicId;
   
    protected String systemId;

    
    public NotationImpl(String name, short nodeType){
        super(name, nodeType);
        this.name = name;
    }
    
    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }
    
    public String getName(){
        return name;
    }
    
    public short getNodetype(){
        return NodeImpl.NOTATION_NODE;
    }
    
    public String getNodeName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
}
