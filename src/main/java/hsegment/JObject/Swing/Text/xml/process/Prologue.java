/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.process;

/**
 *
 * @author Ndzana christophe, Hyacinthe Tsague
 */
public class Prologue {

    private String version;
    private String encoding;
    private boolean isStandAlone;
    public Prologue(String version, String encoding, Boolean isStandAlone) {
        this.version = version;
        this.encoding = encoding;
        this.isStandAlone = isStandAlone;
    }
    public String getVersion() {
        return this.version;
    }
    public String getEncoding() {
        return this.encoding;
    }
    public boolean isStandalone() {
        return this.isStandAlone;
    }
    
}
