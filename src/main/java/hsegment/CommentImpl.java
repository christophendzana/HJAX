/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public class CommentImpl extends CharDataImpl {

    public CommentImpl(String data) {
        super(data, COMMENT_NODE);
    }

    
    public String getData() throws HJAXException {
        return data;
    }

    
    public void setData(String data) throws HJAXException {
        this.data = data;
    }

    
    public int getLength() {
        return data.length();
    }

   
    
}
