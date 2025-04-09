package hsegment.JObject.Swing.Text.xml.handler;

import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.TagStack;

import java.util.List;

public interface TagStackHandler {
    int countStack(TagStack tagStack);
    List<TagElement> handleStack(TagStack tagStack);
}
