/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import hsegment.JObject.Swing.Text.xml.Document;

/**
 *
 * @author Ndzana Christophe
 */
public class HSchema implements Document{
    
    private String name;
    
    public HSchema(String name){
        this.name = name;
    }

    @Override
    public Element getRootElement() {
        return null;
    }

    @Override
    public Element getElement(String name) {
        return null;
    }
}
