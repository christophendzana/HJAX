package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;
import hsegment.JObject.Swing.Text.xml.process.Prologue;

public class PrologueHandlerImpl extends HandlePrologue {
    private Prologue prologue;
    @Override
    public void handlePrologue(String version, String encoding, boolean isStandAlone) {
        prologue = new Prologue(version, encoding, isStandAlone);
        System.out.println("Prologue handler");
        System.out.println("Version: " + version);
        System.out.println("Encoding: " + encoding);
        System.out.println("isStandAlone: " + isStandAlone);
    }

    @Override
    public String getVersion() {
        return prologue.getVersion();
    }

    @Override
    public String getEncoding() {
        return prologue.getEncoding();
    }

    @Override
    public boolean isStandalone() {
        return prologue.isStandalone();
    }
}
