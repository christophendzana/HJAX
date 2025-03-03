/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

/**
 *
 * @author Ndzana christophe
 */
public class DefaultPrologueHandler extends HandlePrologue{

    private long version;
    private String encoding;
    private boolean isStandAlone;
    @Override
    protected void handlePrologue(long version, String encoding, boolean isStandAlone) {
        this.version = version; 
        this.encoding = encoding; 
        this.isStandAlone = isStandAlone;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

    @Override
    public boolean isStandalone() {
        return this.isStandAlone;
    }
    
}
