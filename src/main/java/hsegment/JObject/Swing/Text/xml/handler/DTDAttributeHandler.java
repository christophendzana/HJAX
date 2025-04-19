package hsegment.JObject.Swing.Text.xml.handler;

import hsegment.JObject.Swing.Text.xml.dtd.DTDAttribute;
import hsegment.JObject.Swing.Text.xml.error.SourceError;

public interface DTDAttributeHandler {
    void handleDTDAttribute(DTDAttribute dtdAttribute, SourceError sourceError);
}
