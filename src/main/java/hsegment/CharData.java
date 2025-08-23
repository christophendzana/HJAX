/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public interface CharData {
    
    public String getData() throws HJAXException;

    public void setData(String data) throws HJAXException;

    public int getLength();

    public String substringData(int offset, int count) throws HJAXException;

    public void appendData(String arg) throws HJAXException;

    public void insertData(int offset, String arg) throws HJAXException;

    public void deleteData(int offset, int count) throws HJAXException;

    public void replaceData(int offset, int count, String arg) throws HJAXException;
    
}
