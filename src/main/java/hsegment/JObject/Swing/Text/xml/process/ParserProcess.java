package hsegment.JObject.Swing.Text.xml.process;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.Parser;
import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.XMLValidator;
import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.xml.handlerImpl.*;

import java.io.IOException;
import java.io.Reader;

public class ParserProcess implements ErrorHandler, CommentHandler, TextHandler, TagHandler,
        InstructionTagHandler, HandleDoctype, HandlePrologue, EntityHandler, XMLValidator {

    private Parser parser;
    private ErrorHandler errorHandler;
    private CommentHandler commentHandler;
    private TextHandler textHandler;
    private TagHandler tagHandler;
    private InstructionTagHandler instructionTagHandler;
    private HandleDoctype doctypeHandler;
    private HandlePrologue prologueHandler;
    private EntityHandler entityHandler;
    private final DequeueHandler<TagElement> dequeueHandler = new TagDequeueHandler();
    private XMLValidator xmlValidator;

    public XMLValidator getXmlValidator() {
        return xmlValidator;
    }

    public ParserProcess(){
        parser = new Parser();
        parser.setErrorHandler(this);
        parser.setCommentHandler(this);
        parser.setTextHandler(this);
        parser.setTagHandler(this);
        parser.setInstructionTagHandler(this);
        parser.setEntityHandler(this);
        parser.setPrologueHandler(this);
        parser.setDoctypeHandler(this);
        parser.setDequeueHandler(dequeueHandler);
    }

    public void parse(Reader in) throws IOException, HJAXException {
        this.parser.parse(in);
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public String getEncoding() {
        return "";
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public void handleComment(char[] text) {
        System.out.println("handle comment: " + new String(text));
    }

    @Override
    public void handleEntity(String name, String value) {
        System.out.println("handle entity: " + name);
    }

    @Override
    public String handleEntity(String name) {
        return "";
    }

    @Override
    public void errorHandler(String src, String msg, String debug, ErrorType type) throws HJAXException {

    }

    @Override
    public void handleInstruction(TagElement tag) {

    }

    @Override
    public void handleEmptyTag(TagElement tag) {
        System.out.println("handle empty tag : "+tag.getElement().getName());
    }

    @Override
    public void handleStartTag(TagElement tag) {
        System.out.println("handle start tag : "+tag.getElement().getName());
    }

    @Override
    public void handleEndTag(TagElement tag) {
        System.out.println("handle end tag : "+tag.getElement().getName());
    }

    @Override
    public void handleText(char[] text) {
        System.out.println("handle text : "+new String(text));
    }

    @Override
    public void handleDoctype(String dtdName, String locationType, String dtdFilePath) {
        System.out.println("handle doctype : dtd :"+dtdName+", location type :"+locationType+", location file path :"+dtdFilePath);
    }

    @Override
    public void handleDoctype(String dtdName, String content) {

    }

    @Override
    public void handlePrologue(String version, String encoding, Boolean isStandAlone) {
        System.out.println("version :"+version+", encoding :"+encoding+", isStandAlone :"+isStandAlone);
    }
}
