/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

/**
 *
 * @author PSM
 */
public class TextNode extends CharData {
    
    private String data;
    private ElementNode element;
    
    public TextNode(String data, ElementNode element) {
        super("#Text");
        this.data = data;
        this.element = element;
    }
    
    public String getData(){
        return data;
    }
  
    public void setData(String data){
        this.data = data;
    }
    
    public ElementNode getHolderElement(){
        return this.element;
    }
    
    public void setHolderElement(ElementNode element){
        this.element = element;
    }
}
    
