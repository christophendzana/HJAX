/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.process;


/**
 * @author Hyacinthe Tsague
 */
public interface HandleDoctype{

    void handleDoctype(final String dtdName, final String locationType, final String dtdFilePath);
    void handleDoctype(final String dtdName, final String content);
}
