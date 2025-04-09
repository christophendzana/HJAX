package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.TagStack;
import hsegment.JObject.Swing.Text.xml.handler.TagStackHandler;

import java.util.List;

public class TagStackHandlerImpl implements TagStackHandler {
    @Override
    public int countStack(TagStack tagStack) {
        return tagStack.count();
    }

    @Override
    public List<TagElement> handleStack(TagStack tagStack) {
        return List.of();
    }
}
