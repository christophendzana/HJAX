/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

/**
 *
 * @author Ndzana Christophe
 */
public class SimpleType<T> extends Type<T>{
    
    /**
     * define witch category this type is.
     */
    private int category = SimpleType.NORMALYZED;
    
    /**
     * NORMALYZED type replace multiple space (Tabulation, Carrier return...) by a single one
     */
    protected static int  NORMALYZED = 1;
    
    /**
     * COMPACTED delete all beginning and finishing space 
     */
    protected static int COMPACTED = 2;
    
    /**
     * Facet is use to force Simple type to have certain characteristic like length of string using into element or 
     * attribute or list of value possible into element or attribute etc...
     */
    protected Facet facet;
    
    private Restriction restriction;
    
    private List list;
    
    private Union union;
    public SimpleType(String name){
        super(name);
    }
    
    
    
    /**
     * There are two category of simple type:<br/>
     * COMPACTED : delete all space carater at the beginning and the finishing of element or attribute<br/>
     * 
     * NORMALYZED :replace multiple space (Tabulation, Carrier return...) by a single one
     * @param category may be one of NORMALYZED or COMPACTED
     * 
     * @throws IllegalArgumentException if category is not one of NORMALYZED or COMPACTED value
     */
    protected void setCategory(int category){
        if(category != NORMALYZED || category != COMPACTED)
            throw new IllegalArgumentException("category may be egual to COMPACTED or NORMALYZED");
        
        this.category = category;
    }
    /**
     * Return this simple type's category
     * @return this simple type's category
     * @see #setCategory(int) 
     */
    public final int getCategory(){
        return this.category;
    }
    
    public void add(T value){
        
    }
    
    public Restriction getRestriction(){
        if(restriction == null){
            restriction = new Restriction();
        }
        return restriction;
    }
    
    public List getList(){
        if(list == null){
            list = new List();
        }
        return list;
    }
    
    public Union getUnion(){
        if(union == null){
            union = new Union();
        }
        
        return union;
    }
    
    public class Restriction{
        Facet facet;
        private Restriction(){}
        
        public void addFacetWhiteSpace(String value){
            
        }
        public void addFacet(Facet facet, T value){
            
        }
    }
    
    public class List{
        Facet facet;
        private List(){}
        
        public void addItem(T value){
            
        }
        public void addRestriction(Facet facet){
            
        }
    }
    
    public class Union{
        private Union(){}
        
        public void setType(Type... type){
            
        }
    }
    
    
    public enum Facet{
        
        PATTERN(""){
            protected void setValue(Object value){
                
            }
            
            public Object getValue(){
                return null;
            }
        },
        
        ENUMERATION(""){
            public void setValue(Object value){
                
            }
            
            public Object getValue(){
                return null;
            }
        },
        
        WHITESPACE(""){
            String value;
            
            public void setValue(Object value){
                if(!(value instanceof String))
                    throw new IllegalArgumentException("value into Facet WHITESPACE should be an instance of String");
                switch(((String)value).trim().toLowerCase()){
                    case "reserve":
                    case "replace":
                    case "collapse":
                        this.value = (String)value;
                }
                this.value = (String)value;
            }
            public String getValue(){
                return value;
            }
        };
        
        
        
        private Object value;
        
        private Facet(Object value){
            this.value = value;
        }
        
        
        protected abstract void setValue(Object value);
        protected abstract Object getValue();
    }
}
