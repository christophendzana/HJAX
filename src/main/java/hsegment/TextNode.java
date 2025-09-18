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
    
    public TextNode(String data) {
        super("#Text");
        this.data = data;
    }
    
    public String getData(){
        return data;
    }
  
    public void setData(String data){
        this.data = data;
    }
}
    
