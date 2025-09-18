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
public class CommentNode extends CharData {

    private String data;
    
    public CommentNode(String data) {
        super(data);
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
