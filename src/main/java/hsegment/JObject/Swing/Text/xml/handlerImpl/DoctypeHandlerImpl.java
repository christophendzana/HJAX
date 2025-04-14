package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.process.HandleDoctype;

/**
 * An implementation of HandleDoctype interface.
 * @author Hyacinthe Tsague
 */
public class DoctypeHandlerImpl implements HandleDoctype {
    // Handle an external dtd. Get from absolute path or download from internet
    @Override
    public void handleDoctype(String dtdName, String locationType, String dtdFilePath) {
        System.out.println("Handle doctype with external dtd in path " + dtdFilePath);
        //todo get the doctype from path and call parseDTD method
    }

    // Handle an internal dtd. Get it from xml file as a String.
    @Override
    public void handleDoctype(String dtdName, String content) {
        System.out.println("Handle doctype with internal dtd ");
        System.out.println("Content : " + content);
        //todo call parseDTD method
    }
}
