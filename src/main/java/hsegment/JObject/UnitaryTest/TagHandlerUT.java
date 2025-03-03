/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.TagHandler;
import javax.swing.text.html.parser.TagElement;

/**
 *
 * @author Ndzana Christophe
 */
public class TagHandlerUT implements TagHandler{

    @Override
    public void handleEmptyTag(TagElement tag) {
        System.out.println("appel de la methode empty tag: \n ==>"+tag.getElement());
    }

    @Override
    public void handleStartTag(TagElement tag) {
        System.out.println("appel de la methode open tag: \n ==>"+tag.getElement());
    }

    @Override
    public void handleEndTag(TagElement tag) {
        System.out.println("appe de la methode close tag: \n"+tag.getElement());
    }
    
}
