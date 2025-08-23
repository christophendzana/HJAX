/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.Comment;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public class CommentImpl extends CharDataImpl implements Comment{

    public CommentImpl(String data, DocumentImpl holderDocument) {
        super(data, COMMENT_NODE, holderDocument);
    }

    @Override
    public String getData() throws HJAXException {
        return data;
    }

    @Override
    public void setData(String data) throws HJAXException {
        this.data = data;
    }

    @Override
    public int getLength() {
        return data.length();
    }

   @Override
    public String substringData(int offset, int count) throws HJAXException {
        if (offset<0 || offset > data.length() || count<0) {
            //renvoie une erreur
        }
        int end = Math.min(offset + count, data.length());
        return data.substring(offset, end);
    }

    @Override
    public void appendData(String newdata) throws HJAXException {
        data += data;
    }

    @Override
    public void insertData(int offset, String newdata) throws HJAXException {
        if (offset<0 || offset > data.length()) {
            //renvoie une erreur
        }        
        data = data.substring(0, offset) + newdata + data.substring(offset);        
    }

    @Override
    public void deleteData(int offset, int count) throws HJAXException {
        if (offset<0 || offset > data.length()) {
            //renvoie une erreur
        }   
        int end = Math.min(offset + count, data.length());
        data = data.substring(0, offset) + data.substring(end);
    }

    @Override
    public void replaceData(int offset, int count, String newdata) throws HJAXException {
        deleteData(offset, count);
        insertData(offset, newdata);
    }
    
}
