/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import hsegment.JObject.Swing.Text.PrologHandler;

/**
 *
 * @author Ndzana christophe
 */
public abstract class HandlePrologue implements PrologHandler{
    
    
    protected abstract void handlePrologue(long version, final String encoding, boolean isStandAlone);
}
