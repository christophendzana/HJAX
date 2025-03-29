/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsegment.JObject.Swing.Text.handler;

import javax.swing.text.html.parser.TagElement;

/**
 *
 * @author Ndzana christophe
 */
public interface TagHandler {
    public void handleEmptyTag(TagElement tag);
    public void handleStartTag(TagElement tag);
    public void handleEndTag(TagElement tag);
}
