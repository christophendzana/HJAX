package hsegment.JObject.Swing.Text.xml.handlerImpl;

import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.handler.InstructionTagHandler;

public class InstructionTagHandlerImpl implements InstructionTagHandler {
    @Override
    public void handleInstruction(TagElement tag) {
        System.out.println("Instruction tag name => "+tag.getElement().getName());
    }
}
