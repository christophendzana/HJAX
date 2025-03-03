/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.ErrorHandler;
import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author Ndzana christophe
 */
public class ErrorHandlerUT implements ErrorHandler{

    @Override
    public void errorHandler(String src, String msg, String debug, ErrorType type) {
        
        System.out.println("Appelle de la méthode errorHandler : \n ==> Origine erreur = "+src+"\n"+
                                       "==> Message d'erreur = "+msg+"\n solution possible = "+debug+"\n"+
                                            "Type d'erreur = "+type.toString());
    }
    
}
