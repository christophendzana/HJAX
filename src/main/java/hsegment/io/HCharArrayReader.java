/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.io;

import java.io.CharArrayReader;

/**
 *
 * @author DELL
 */
public class HCharArrayReader extends CharArrayReader{
    
    
    public HCharArrayReader(char[] buf) {
        super(buf);
    }
    
    public HCharArrayReader(char[] buf, int offset, int length) {
        super(buf, offset, length);
    }
    
    
}
