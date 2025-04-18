package hsegment.JObject.Swing.Text.xml.handler;

import hsegment.JObject.Swing.Text.xml.ContentModel;
import hsegment.JObject.Swing.Text.xml.Element;

public interface ElementHandler {
    void handleElementType(String elementType, int rowIndex, int columnIndex);
    void handleContentModel(ContentModel contentModel, int rowIndex, int columnIndex);
    void handleElement(Element element);
}
