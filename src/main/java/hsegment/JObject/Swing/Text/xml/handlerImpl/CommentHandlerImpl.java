/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.handlerImpl;


import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.xml.handler.CommentHandler;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;

/**
 *
 * @author Ndzana Christophe, Hyacinthe Tsague
 */
public class CommentHandlerImpl implements CommentHandler {
    private final ErrorHandler errorHandler;

    public CommentHandlerImpl(ErrorHandler errorHandler){
        this.errorHandler = errorHandler;
    }

    @Override
    public void handleComment(String text, int rowIndex) {
        System.out.println("Call HandleComment:  ==> "+ text);
        if(text.contains("--")){
            errorHandler.errorHandler("In row : "+rowIndex, "Invalid comment syntax !",
                    "Comment must not content -- ", ErrorType.FatalError);
        }
    }
    
}
