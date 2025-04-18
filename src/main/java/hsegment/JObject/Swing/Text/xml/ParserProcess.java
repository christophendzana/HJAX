package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.handler.PrologHandler;
import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.xml.handlerImpl.*;
import hsegment.JObject.Swing.Text.xml.process.HandleDoctype;
import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;

import java.io.IOException;
import java.io.Reader;

public class ParserProcess {

    private Parser parser;
    private ErrorHandler errorHandlerUT;
    private CommentHandler commentHandlerUT;
    private TextHandler textHandlerUT;
    private TagHandler tagHandlerUT;
    private InstructionTagHandler instructionTagHandlerUT;
    private HandleDoctype doctypeHandler;
    private HandlePrologue prologueHandler;
    private EntityHandler entityHandler;
    private DequeueHandler<TagElement> dequeueHandler;

    public ParserProcess(){
        errorHandlerUT = new ErrorHandlerImpl();
        commentHandlerUT = new CommentHandlerImpl(errorHandlerUT);
        textHandlerUT = new TextHandlerImpl();
        tagHandlerUT = new TagHandlerImpl(errorHandlerUT);
        dequeueHandler = new TagDequeueHandlerImpl();
        doctypeHandler = new DoctypeHandlerImpl();
        prologueHandler = new PrologueHandlerImpl();
        entityHandler = new EntityHandlerImpl();
        instructionTagHandlerUT = new InstructionTagHandlerImpl();

        parser = new Parser();
        parser.setErrorHandler(this.errorHandlerUT);
        parser.setCommentHandler(this.commentHandlerUT);
        parser.setTextHandler(this.textHandlerUT);
        parser.setTagHandler(this.tagHandlerUT);
        parser.setInstructionTagHandler(this.instructionTagHandlerUT);
        parser.setEntityHandler(this.entityHandler);
        parser.setPrologueHandler(this.prologueHandler);
        parser.setDoctypeHandler(this.doctypeHandler);
        parser.setDequeueHandler(this.dequeueHandler);
    }

    public void parse(Reader in) throws IOException, HJAXException {
        this.parser.parse(in);
    }

}
