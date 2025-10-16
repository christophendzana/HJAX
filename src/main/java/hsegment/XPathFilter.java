/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

/**
 *
 * @author FIDELE
 */
public class XPathFilter {
    
    private String name;
    private String operator;
    private Object value; 
    private Type type;
    
    public enum Type{
        ATTRIBUTE, POSITION, TEXT, CONTAINS, FUNCTION, START_WITH, ENDS_WITH, CUSTOM, ELEMENT, COUNT
    }
    
    public XPathFilter(String name, String operator, Object value, Type type){
        this.name = name;
        this.operator = operator;
        this.value = value;
        this.type = type;
    }
    
    public XPathFilter(String name, Type type){
        this.value = value;
    }
    
    public String getName(){
        return name;
    }
    
    public String getOperator(){
        return operator;
    }
    
    public Object getValue(){
        return value;
    }
    
    public Type getType(){
        return type;
    }
    
}
