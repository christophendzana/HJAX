/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.TextImpl;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public interface CharData {
    
    public String getData(TextImpl text) throws HJAXException;

    public void setData(TextImpl text, String data) throws HJAXException;

    public int getLength(TextImpl text);

    public String substringData(TextImpl text, int offset, int count) throws HJAXException;

    public void appendData(TextImpl text, String arg) throws HJAXException;

    public void insertData(TextImpl text, int offset, String arg) throws HJAXException;

    public void deleteData(TextImpl text, int offset, int count) throws HJAXException;

    public void replaceData(TextImpl text, int offset, int count, String arg) throws HJAXException;
    
}
