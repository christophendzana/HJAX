/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsegment.JObject.Swing.Text;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author Ndzana Christophe
 */
public interface ErrorHandler {
    public void errorHandler(String src, String msg, String debug, ErrorType type) throws HJAXException;
}
