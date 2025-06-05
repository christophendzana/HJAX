/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import hsegment.JObject.Swing.Text.xml.Document;
import hsegment.JObject.Swing.Text.xml.TagElement;

/**
 *
 * @author Ndzana Christophe
 */
public class SchemaParser extends XMLParser{
    
    private HSchema schema;
    
    public SchemaParser(String name){
        schema = new HSchema(name);
    }
    
    
    @Override
    public void handleEmptyTag(TagElement tag) {
        
    }

    @Override
    public void handleStartTag(TagElement tag) {
        
    }

    @Override
    public void handleEndTag(TagElement tag) {
        
    }
    
    public Document getSchemaDocument(){
        return schema;
    }
}
