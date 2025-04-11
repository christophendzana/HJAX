package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.Constants;
import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.TagStack;
import hsegment.JObject.Swing.Text.xml.handler.TagStackHandler;

import java.util.List;

public class TagStackHandlerImpl implements TagStackHandler {
    private final TagStack tagStack = new TagStack();
    @Override
    public int countStack(TagStack tagStack) {
        return tagStack.count();
    }

    @Override
    public void autoProcess(TagElement tagElement) {
        switch (tagElement.getType()){
            case Constants.OPEN_TAG -> tagStack.stack(tagElement);
            case Constants.CLOSE_TAG -> {
                if(!tagStack.pullOut(tagElement)){
                    throw new HJAXException("Not corresponding tag : "+tagElement.getElement().getName());
                }
            }
            default -> {}
        }
    }
}
