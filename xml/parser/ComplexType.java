/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

/**
 *
 * @author Ndzana Christophe
 */
public class ComplexType extends Type{
    
    private boolean mixed;
    
    private Connectors connector;
    protected ComplexType(String name){
        super(name);
    }
    /**
     * <code>isMixed</code> egual to true if this type allow mixture of elements and text and 
     * false if not
     * @param isMixed set to true if this type allow element and text.
     * @see #isMixedType() 
     */
    protected void setContentType(boolean isMixed){
        this.mixed = mixed;
    }
    /**
     * return true if this type allow mixture of element and text
     * @return true if this type accept element and text
     * @see #setContentType(boolean) 
     */
    public boolean isMixedType(){
        return mixed;
    }
    
    /**
     * Set connector of this Type.
     * @param connector this type connector
     * @see #getConnector() 
     */
    protected void setConnector(Connectors connector){
        this.connector = connector;
    }
    /**
     * Retun this type connector
     * @return connector
     * @see #setConnector(hsegment.JObject.Swing.Text.xml.parser.ComplexType.Connectors) 
     */
    public final Connectors getConnector(){
        return this.connector;
    }
    
    
    public enum Connectors{
        SEQUENCE("Sequence"){
            int minOccurs = 1;
            int maxOccurs = 1;
        }, CHOISE("Choice"){
            int minOccrurs = 1;
            int maxOccurs = 1;
        }, ALL("All"){
            int minOccurs = 1;
            int maxOccurs = 1;
        };
        
        private String name;
        Connectors(String name){
           this.name = name; 
        }
        @Override
        public String toString(){
            return this.name;
        }
    }
    
}
