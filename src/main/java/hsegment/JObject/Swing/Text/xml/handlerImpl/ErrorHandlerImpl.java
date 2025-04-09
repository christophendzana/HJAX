/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;

import java.util.logging.Logger;

/**
 *
 * @author Ndzana christophe, Hyacinthe Tsague
 */
public class ErrorHandlerImpl implements ErrorHandler{

    private final Logger logger = Logger.getLogger(ErrorHandlerImpl.class.getName());
    @Override
    public void errorHandler(String src, String msg, String debug, ErrorType type) throws HJAXException {
        switch (type){
            case Warning -> logger.warning(msg + "\n source : " + src + "\n debug : " + debug);
            case FatalError -> {
                logger.severe( msg + "\n source : " + src + "\n debug : " + debug);
                throw new HJAXException(HJAXException.class.getName());
            }
        }
    }
    
}
