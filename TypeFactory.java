/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import java.util.Vector;

/**
 *
 * @author Ndzana Christophe
 */
public class TypeFactory {
    
    public static final String ID = "ID";
    public static final String IDREF = "IDREF";
    
    public static final String IDREFS = "IDREFS";
    public static final String NMTOKEN = "NMTOKEN";
    
    public static final String ENUMERATION = "ENUMERATION";
    
    public static final String NMTOKENS = "NMTOKENS";
    public Type type = null;
    public TypeFactory(){}
    
    public Type newIntance(String typeName){
        
        switch(typeName.toUpperCase()){
            case ID:
                type =  new IDType(ID);
                break;
            case IDREF:
                type = new IDRefType(IDREF);
                break;
            case IDREFS:
                type = new IDRefsType(IDREFS);
            case ENUMERATION :
                type = new Enumeration(ENUMERATION);
                break;
            default :
                throw new IllegalArgumentException("Bad Type name");
                
        }
        return type;
    }
    
    private class IDType extends Type{
        
        public IDType(String name){
            super(name);
        }
    }
    
    private class IDRefType extends Type{
        
        public IDRefType(String name){
            super(name);
        }
    }
    
    private class IDRefsType extends Type{
        
        public IDRefsType(String name){
            super(name);
        }
    }
    
    private class Enumeration<T> extends Type<T>{
        private Vector<T> values;
        private int currentValueIndex = 0;
        public Enumeration(String name){
            super(name);
            values = new Vector<T>();
        }
        
        protected void setValue(T value){
            values.add(value);
            super.setValue(value);
        }
        
        public T getValue(){
            try {
                return values.get(currentValueIndex++);
                
            } catch (Exception e) {
                return null;
            }
        }
            
            
    }
}
