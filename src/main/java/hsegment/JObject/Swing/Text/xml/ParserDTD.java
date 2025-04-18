package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.handler.ElementHandler;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ParserDTD {
    private Reader reader;
    private int rowIndex;
    private int columnIndex;
    private boolean isStartTag;
    private boolean isStartTagName;
    private TagStack tagStack;

    private Logger logger;
    private ErrorHandler errorHandler;
    private ElementHandler elementHandler;

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setElementHandler(ElementHandler elementHandler) {
        this.elementHandler = elementHandler;
    }

    public void parse(Reader in) throws IOException {
        reader = new BufferedReader(in);
        tagStack = new TagStack();
        logger = Logger.getLogger(this.getClass().getName());
        char c;
        StringBuilder textValue = new StringBuilder();
        TagElement tagElement;
        Element element = null;
        Entity entity = null;
        AttributeList attributeList = null;
        while(reader.ready()){
            columnIndex++;
            c = (char)reader.read();
            switch(c){
                case '<' -> {
                    isStartTag = true;
                    //
                }
                case '!' -> {
                    isStartTagName = true;
                    //
                }
                case ' ' -> {
                    if(isStartTag && !textValue.isEmpty() && isStartTagName){
                        isStartTagName = false;
                        switch (textValue.toString()){
                            case "ELEMENT" -> {
                                element = parseElement();
                                isStartTag = false;
                                isStartTagName = false;
                                tagElement = new TagElement(element, Constants.ELEMENT_TAG);
                            }
                            case "ENTITY" -> {
                                entity = parseEntity();
                            }
                            case "ATTLIST" -> {
                                attributeList = parseAttributeList();
                            }
                            case "--" -> {
                                //handle comment
                            }
                            default -> {
                                handleError("Unknown tag !", "Remove or correct the tag", ErrorType.FatalError);
                            }
                        }
                        textValue.setLength(0);
                    }
                }
                case '\n' -> {
                    rowIndex++;
                    columnIndex = 0;
                }
                case '\t' -> {
                    columnIndex += 8;
                }
                case '\r' -> {
                    columnIndex = 0;
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
    }

    private Element parseElement() throws IOException {
        char c;
        StringBuilder textValue = new StringBuilder();
        ContentModel parentContentModel = null;
        Element element = null;
        OUTER: while (reader.ready()){
            c = (char)reader.read();
            switch (c){
                case ' ' -> {
                    if(!textValue.isEmpty() && parentContentModel == null){
                        // set the element name
                        element = new Element(rowIndex, columnIndex, textValue.toString());
                        parentContentModel = parseContentModel(parentContentModel);
                        handleContentModel(parentContentModel);
                        textValue.setLength(0);
                    }

                }
                case '+', '?', '*', '|', ',' -> {
                    handleError("Invalid position of operator : "+c, "Remove the operator",
                            ErrorType.FatalError);
                }
                case '>' -> {
                    if(element != null && parentContentModel == null && !textValue.isEmpty()){
                        element.setType(getType(textValue.toString()));
                    }
                    if(element != null && parentContentModel != null && !textValue.isEmpty()){
                        handleError("Invalid content : "+textValue, "Remove the content !", ErrorType.FatalError);
                    }
                    if(element != null && parentContentModel != null){
                        element.setContentModel(parentContentModel);
                        System.out.println(parentContentModel.toString());
                    }
                    textValue.setLength(0);
                    break OUTER;
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
        return element;
    }

    private ContentModel parseContentModel(ContentModel contentModel) throws IOException {
        char c;
        StringBuilder textValue = new StringBuilder();
        boolean isEndReading = false;
        Content content = new Content();
        while(reader.ready()){
            c = (char)reader.read();
            switch (c){
                case ' ' -> {
                    if(contentModel == null){
                        textValue.append(c);
                    }
                }
                case ',', '|' -> {
                    if(contentModel == null){
                        handleError("Invalid position of operator : "+c, "Remove the operator",
                                ErrorType.FatalError);
                    }
                    if(contentModel != null && content != null && !textValue.isEmpty()){
                        content.setName(textValue.toString());
                        contentModel.setValues(content);
                        content = new Content();
                    }
                    textValue.setLength(0);
                    // process of handling sequence operator
                    if(contentModel != null && contentModel.getSplitOperator() == 0){
                        contentModel.setSplitOperator(c);
                    }
                    if(contentModel != null && contentModel.getSplitOperator() != 0 && contentModel.getSplitOperator() != c){
                        //todo handle error
                        System.out.println("Error : Found different split operator");
                    }

                    // process of handling choice operator
                }
                case '*', '+', '?' -> {
                    if(contentModel == null){
                        handleError("Invalid position of operator : "+c, "Remove the operator",
                                ErrorType.FatalError);
                    }
                    // process of handling of many occurrence operator
                    // process of handling not null occurrence operator
                    // process of handling of zero or one occurrence operator
                    if(contentModel != null && !isEndReading){
                        content.setOperator(c);
                    }
                    if(contentModel != null && isEndReading && contentModel.getOperator() == 0){
                        contentModel.setOperator(c);
                        return contentModel;
                    }
                    if(contentModel != null && contentModel.getOperator() != 0 && contentModel.getOperator() != c){
                        System.out.println("Error : Found different operator");
                    }
                }
                case '(' -> {
                    if(!textValue.isEmpty()){
                        textValue.append(c);
                        handleError("Invalid content : "+textValue, "Remove the content !", ErrorType.FatalError);
                    }
                    content = new Content();
                    // process of handling start elements group
                    if(contentModel == null){
                        contentModel = new ContentModel();
                    }else{
                        ContentModel temp = new ContentModel();
                        temp = parseContentModel(temp);
                        handleContentModel(temp);
                        contentModel.setContent(temp);
                    }
                }
                case ')' -> {
                    if(contentModel == null){
                        handleError("Not found open bracket of content model", "Remove this close bracket",
                                ErrorType.FatalError);
                    }
                    // process of handling end elements group
                    if(contentModel != null && content != null && !textValue.isEmpty()){
                        content.setName(textValue.toString());
                        contentModel.setValues(content);
                        content = null;
                    }
                    if(contentModel != null){
                        reader.mark(0);
                        char next = (char)reader.read();
                        reader.reset();
                        if(next != '*' && next != '+' && next != '?'){
                            return contentModel;
                        }else {
                            isEndReading = true;
                        }
                    }
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
        return contentModel;
    }

    private AttributeList parseAttributeList() throws IOException {
        return null;
    }

    private Entity parseEntity() throws IOException {
        return null;
    }

    private int getType(String text){
        return switch (text){
            case "EMPTY" -> Constants.EMPTY;
            case "CDATA" -> Constants.CDATA;
            case "ID" -> Constants.ID;
            case "IDREF" -> Constants.IDREF;
            case "NOTATION" -> Constants.NOTATION;
            case "IDREFS" -> Constants.IDREFS;
            case "NMTOKEN" -> Constants.NMTOKEN;
            case "NMTOKENS" -> Constants.NMTOKENS;
            case "#REQUIRED" -> Constants.REQUIRED;
            case "#FIXED" -> Constants.FIXED;
            case "#IMPLIED" -> Constants.IMPLIED;
            default -> throw new HJAXException("Unknown element type: " + text);
        };
    }

    protected void handleElement(Element element){
        try {
            if(elementHandler != null){
                elementHandler.handleElement(element);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    private void handleContentModel(ContentModel contentModel){
        try {
            if(elementHandler != null){
                elementHandler.handleContentModel(contentModel, rowIndex, columnIndex);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    protected void handleError(String msg){
        try {
            if(errorHandler != null){
                logger.severe(msg);
                reader.close();
            }
        }catch (Exception e){
            logger.severe(e.getMessage());
        }
    }

    protected void handleError(String message, String debug, ErrorType errorType){
        try{
            if(errorHandler != null){
                errorHandler.errorHandler("In Row : "+rowIndex+" , Column : "+columnIndex, message, debug, errorType);
            }
        }catch (HJAXException e){
            logger.severe("HJAXException :"+e.getMessage());
        }catch (Exception e){
            logger.severe(e.getMessage());
        }finally {
            try {
                // stop the reading of file
                if(errorHandler != null && errorType == ErrorType.FatalError){
                    reader.close();
                }
            } catch (Exception e) {
                logger.severe("Exception :"+e.getMessage());
            }
        }
    }
}
