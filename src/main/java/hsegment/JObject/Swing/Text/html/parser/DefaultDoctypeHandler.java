/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

/**
 *Default class which store Doctype value
 * @author Ndzana christophe
 */
public class DefaultDoctypeHandler extends HandleDoctype{
    private String dtdName;
    private String locationType;
    private String dtdFilePath;
    
    @Override
    protected void handleDoctype(String dtdName, String locationType, String dtdFilePath) {
         this.dtdName = dtdName; this.dtdFilePath = dtdFilePath; this.locationType = locationType;
    }

    @Override
    public String getDTDName() {
        return this.dtdName;
    }

    @Override
    public String getLocationType() {
        return this.locationType;
    }

    @Override
    public String dtdFilePath() {
        return this.dtdFilePath;
    }
    
}
