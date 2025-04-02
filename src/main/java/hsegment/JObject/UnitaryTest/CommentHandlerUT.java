/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.UnitaryTest;


import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.xml.handler.CommentHandler;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;
import static hsegment.JObject.util.FunctionUtils.getSourceError;

/**
 *
 * @author Ndzana Christophe
 */
public class CommentHandlerUT implements CommentHandler {
    private final ErrorHandler errorHandler;

    public CommentHandlerUT(ErrorHandler errorHandler){
        this.errorHandler = errorHandler;
    }

    @Override
    public void handleComment(String text, int rowIndex) {
        System.out.println("Appel de la methode HandleComment:  ==> "+ text);
        if(text.contains("--")){
            errorHandler.errorHandler("In row : "+rowIndex, "Invalid comment syntax !",
                    "Comment must not content -- ", ErrorType.FatalError);
        }
    }
    
}
