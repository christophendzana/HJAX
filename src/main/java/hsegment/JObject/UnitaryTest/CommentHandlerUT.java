/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.CommentHandler;

/**
 *
 * @author Ndzana Christophe
 */
public class CommentHandlerUT implements CommentHandler{

    @Override
    public void handleComment(char[] text) {
        System.out.println("Appel de la methode HandleComment: \n ==> "+new String(text));
    }
    
}
