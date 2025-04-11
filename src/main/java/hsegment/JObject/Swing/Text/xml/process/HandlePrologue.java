/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.process;

import hsegment.JObject.Swing.Text.handler.PrologHandler;

/**
 *
 * @author Ndzana christophe
 */
public abstract class HandlePrologue implements PrologHandler{
    public abstract void handlePrologue(final String version, final String encoding, Boolean isStandAlone);
}
