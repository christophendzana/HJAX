package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.handler.DoctypeHandler;
import hsegment.JObject.Swing.Text.xml.process.HandleDoctype;

public class DoctypeHandlerImpl extends HandleDoctype {
    @Override
    public void handleDoctype(String dtdName, String locationType, String dtdFilePath) {
        System.out.println("Handle doctype with external dtd in path " + dtdFilePath);
        //todo get the doctype from path and call parseDTD method
    }

    @Override
    public void handleDoctype(String dtdName, String content) {
        System.out.println("Handle doctype with internal dtd ");
        System.out.println("Content : " + content);
        //todo call parseDTD method
    }
}
