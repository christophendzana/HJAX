/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.text.html.parser.DTDConstants;

/**
 *
 * @author Ndzana Christophe
 */
public class AttributeList {
    /**
     * use to define that this attribute is prohibited
     */
    public static final int PROHIBITED = 1;
    
    /**
     * use to define that this attribute is Optional
     */
    public static final int OPTIONAL = 2;
    /**
     * use to define that this attribute is REQUIRED
     */
    public static final int REQUIRED = 3;
    /**
     * The attribute name
     */
    protected String name;
    
    protected Type type;

    /**
     * The possible attribute values
     */
    protected Vector<?> values;

    /**
     * The attribute modifier
     */
    protected int modifier;

    /**
     * The default attribute value
     */
    protected String value;

    /**
     * The next attribute in the list
     */
    protected AttributeList next;
    
    /**
     * can be egual to:<br/>
     * PROHIBITED : specify this attribute is prohibited<br/>
     * 
     * OPTIONAL : specify this attribute is optional<br/>
     * 
     * REQUIRED : specify this attribute is Required<br/>
     */
    protected int use;

    AttributeList() {
    }

    /**
     * Create an attribute list element.
     *
     * @param name  the attribute name
     */
    public AttributeList(String name) {
        this.name = name;
    }
    
    /**
     * {@return the attribute name}
     */
    public final String getName() {
        return name;
    }

    /**
     * {@return the attribute type}
     * @see DTDConstants
     */
    public final Type getType() {
        return type;
    }

    /**
     * {@return the attribute modifier}
     * @see DTDConstants
     */
    public int getModifier() {
        return modifier;
    }

    /**
     * {@return possible attribute values}
     */
    public Enumeration<?> getValues() {
        return (values != null) ? values.elements() : null;
    }

    /**
     * {@return default attribute value}
     */
    public final String getValue() {
        return value;
    }
    
    public int getValueCount(){
        int valueCount = 0;
        valueCount = value == null? valueCount: ++valueCount;
        valueCount = values == null? valueCount: (values.size() + valueCount);
        return valueCount;
    }
    
    public Object getValue(int index){
        try {
            return values.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@return the next attribute in the list}
     */
    public AttributeList getNext() {
        return next;
    }
    
    /**
     * Set this attribute Name
     * @param name attribute's name
     * @throws IllegalArgumentException if <code>name</code> is null or empty
     */
    protected void setName(String name){
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("null or empty string is not allowed");
        }
        this.name = name;
    }
    
    /**
     * 
     * @param value 
     */
    protected void setValue(String value){
        if(value == null){
            throw new IllegalArgumentException("null argument is not allowed");
        }
        this.value = value;
    }
    
    /**
     * set usability of this attribute can be one of this value :<br/>
     * PROHIBITED : to specify that this attribute is prohibited<br/>
     * 
     * OPTIONAL : to specify that this attribute is optional<br/>
     * 
     * REQUIRED : to specify that this attribute is optional
     * @param usability this element usability
     */
    protected void setUsability(int usability){
        if(usability != PROHIBITED || usability != OPTIONAL || usability != REQUIRED)
            throw new IllegalArgumentException("bad Argument");
        
        this.use = usability;
    }

    /**
     * @return string representation
     */
    @Override
    public String toString() {
        return name;
    }
}
