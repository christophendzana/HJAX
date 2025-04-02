/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.UnitaryTest;


import hsegment.JObject.Swing.Text.xml.handler.TextHandler;

/**
 *
 * @author Ndzana christophe
 */
public class TextHandlerUT implements TextHandler {

    @Override
    public void handleText(String text) {
        System.out.println("Appelle de la méthode handleText : ==> "+text);
    }
}
