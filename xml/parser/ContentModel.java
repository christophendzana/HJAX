/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import java.io.Serializable;
import java.util.Vector;


/**
 *A representation of a content model. Content is an object
 * which describe an element, what are elem
 * @author Ndzana Christophe
 */
public class ContentModel implements Serializable{
    
    private  Operator operator;
    
    private String name = "";
    
    protected int minOccurs = -1;
    
    protected int maxOccurs = -1;
    
    protected Type type;
    
    private Element root;
    
    protected ContentModel content;
    
    protected ContentModel next;
    
    protected Vector<Integer> childs;
    
    protected ContentModel(Element root){
        this("", root);
    }
    protected ContentModel(String name, Element root){
        this.name = name;
        this.root = root;
        childs = new Vector<Integer>(10);
    }
    
    protected ContentModel(){
        this("", null);
    }
    
    public Element getRoot(){
        return root;
    }
    /**
     * Set this content Model's operator
     * @param operator this content Model's operator
     * @throws IllegalArgumentException if operator have been set already and that the new one is different
     */
    public void setOperator(Operator operator) throws IllegalArgumentException{
        if(this.operator == null || this.operator == operator)
            this.operator = operator;
        else
            throw new IllegalArgumentException("operator cannot be changed after initialisation");
    }
    
    protected boolean addElementIndex(int index){
        if(index < 0)
            return false;
        
        childs.add(index);
        return true;
    }
    
    public String getName(){
        return this.name;
    }
    
}
