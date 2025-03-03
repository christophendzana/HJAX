/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

/**
 *Notation is created for Entity purpose. it discribe entity type and 
 * and it provide and application with the necessary identification to call the correct 
 * helper or plug-in
 * @author Ndzana christophe
 */
public final 
        class Notation implements HDTDConstants{
    
    /**
     *Annotation identification which is created by Entity
     */
    public String id;
    /**
     * Notation type is allways egual to SYSTEM
     */
    public final int type = HDTDConstants.SYSTEM;
    /**
     * ressource locator; it content path where the resource describe by 
     * this notation is located
     */
    public String contentType;
    /**
     * Entity owner; enttity whose this Notation belong to.
     */
    public String entityOwner;
    public Notation(String entityOwner, String id, String contentType){
        this.id = id;
        this.contentType = contentType;
        this.entityOwner = entityOwner;
    }
    
    public String getID(){
        return this.id;
    }
    
    public String getContentype(){
        return this.contentType;
    }
    
    public int getType(){
        return type;
    }
    
    
}
