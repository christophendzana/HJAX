package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;
import hsegment.JObject.Swing.Text.xml.process.Prologue;

/**
 * An implementation of HandlePrologue interface.
 * @author Hyacinthe Tsague
 */
public class PrologueHandlerImpl implements HandlePrologue {
    //Prologue initialisation
    private Prologue prologue;
    @Override
    public void handlePrologue(String version, String encoding, Boolean isStandAlone) {
        //Prologue initialisation
        prologue = new Prologue(version, encoding, isStandAlone);
        System.out.println("Prologue handler");
        System.out.println("Version: " + version);
        System.out.println("Encoding: " + encoding);
        System.out.println("isStandAlone: " + isStandAlone);
    }

    //The prologue version attribute
    @Override
    public String getVersion() {
        return prologue.getVersion();
    }

    //The prologue encoding attribute
    @Override
    public String getEncoding() {
        return prologue.getEncoding();
    }

    //The prologue standalone attribute
    @Override
    public boolean isStandalone() {
        return prologue.isStandalone();
    }
}
