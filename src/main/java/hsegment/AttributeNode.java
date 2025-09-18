/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;


/**
 * Herite de Node
 * Possède un nom
 * Possède une valeur
 * Peut contenir du texte
 * N'est pas considéré comme un enfant d'Element <code>getParentNode()</code>
 * doit toujour retourner null
 * @author PSM
 */
public class AttributeNode extends NodeImpl {

    private String name;
    private String value;
    private boolean specidied;
    
    public AttributeNode(String name, String value, boolean specified){
        super(name);
        this.name = name;
        this.value = value;
        this.specidied = specified;
    }    
    
    public String getName() {
        return name;
    }
    
    public boolean getSpecified() {
        // Détermine si l'Attribut a été explicitement defini
        return value !=null && !value.isEmpty();
    }
    
    public String getValue() {
        return value;
    }
}
