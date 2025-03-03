/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

import java.io.Serializable;
import javax.swing.text.html.parser.Element;

/**
 *
 * @author Ndzana Christophe
 */
public class HContentModel implements Serializable{
    
    private int type;
    
    private int cardinality;
    
    private Element root;
    
    private Object content;
    
    private HContentModel next;
    
    private int[] ElementsIndex;
    
    public HContentModel(Element root){
        this.root = root;
    }
    
    public Element getRoot(){
        return root;
    }
    
    public int getType(){
        return type;
    }
    
    public int cardinality(){
        return type;
    }
}
