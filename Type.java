/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

/**
 *
 * @author Ndzana Christophe
 */
public abstract class Type<T> {
    
    /**
     * Name of this Type
     */
    protected String name;
    
    private T value;
    
    private int valueCount;
    public Type(String name){
        this.name = name;
    }
    
    /**
     * Set this Type's name
     * @param name this type's name
     * @throws IllegalArgumentException if <code>name</code> is null or empty
     */
    protected void setName(String name){
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Type's name can't be null or empty");
        this.name = name;
    }
    
    /**
     * set this type value
     * @param value this type value
     * @see #getValue() 
     * @throws IllegalArgumentException if value is null
     */
    protected void setValue(T value){
        if(value == null)
            throw new IllegalArgumentException("value cannot be null");
        this.value = value;
    }
    
    /**
     * Return this type value or null if type don't have a value
     * @return this type value
     */
    public T getValue(){
        return this.value;
    }
    
    /**
     * Return fulling name of this type;
     * @return this type's name
     * @see #setName(java.lang.String) 
     */
    public final String getName(){
        return this.name;
    }
}
