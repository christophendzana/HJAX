/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author DELL
 */
public abstract class HReader extends Reader{
    
    /**
     * it contains character to be read by this class.
     */
    protected char[] text = new char[1024];
    
}
