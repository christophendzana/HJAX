package hsegment.JObject.Swing.Text.xml.dtd;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.*;
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
    private List<DTDAttributeContent> tempContent;
    private boolean isStartTag;
    private boolean isStartTagName;
    private boolean isEndReadingAttributes;
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
        Element element = null;
        Entity entity = null;
        DTDAttribute dtdAttribute = null;
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
                            }
                            case "ENTITY" -> {
                                entity = parseEntity();
                            }
                            case "ATTLIST" -> {
                                tempContent = new ArrayList<>();
                                isEndReadingAttributes = false;
                                dtdAttribute = parseDTDAttribute();
                                System.out.println(dtdAttribute);
                                dtdAttribute = null;
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

    private DTDAttribute parseDTDAttribute() throws IOException {
        char c;
        DTDAttribute dtdAttribute = null;
        StringBuilder textValue = new StringBuilder();
        OUTER : while(reader.ready()){
            c = (char)reader.read();
            switch (c){
                case ' ' -> {
                    if(dtdAttribute == null && !textValue.isEmpty()){
                        dtdAttribute = new DTDAttribute();
                        dtdAttribute.setElementName(textValue.toString());
                        parseAttributeList(dtdAttribute);
                        textValue.setLength(0);
                        break OUTER;
                    }
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
        return dtdAttribute;
    }

    private DTDAttribute parseAttributeList(final DTDAttribute dtdAttribute) throws IOException {
        char c;
        StringBuilder textValue = new StringBuilder();
        DTDAttributeContent attributeContent = null;
        int countDoubleQuotes = 0;
        int countSingleQuotes = 0;
        DefaultValue defaultValue = null;
        boolean startReadingAttribute = false;
        boolean startReadingDefaultValueType = false;
        OUTER : while(reader.ready()){
            c = (char)reader.read();
            switch (c){
                case ' ' -> {
                    // get and set attribute name
                    if(attributeContent == null && !textValue.isEmpty()){
                        attributeContent = new DTDAttributeContent();
                        attributeContent.setAttributeName(textValue.toString());
                        textValue.setLength(0);
                        startReadingAttribute = true;
                        break;
                    }
                    // get and set attribute type
                    if(startReadingAttribute && attributeContent.getAttributeName() != null && defaultValue == null){
                        attributeContent.setAttributeType(getType(textValue.toString()));
                        defaultValue = new DefaultValue();
                        textValue.setLength(0);
                        break;
                    }
                    // get and set attribute default value
                    if(startReadingAttribute && attributeContent.getAttributeType() != 0 && defaultValue != null){
                        defaultValue.setType(textValue.toString());
                        if(textValue.toString().equals("#IMPLIED") || textValue.toString().equals("#REQUIRED")){
                            attributeContent.setDefaultValue(defaultValue);
                            tempContent.add(attributeContent);
                            startReadingDefaultValueType = false;
                            startReadingAttribute = false;
                            textValue.setLength(0);
                            attributeContent = null;
                            defaultValue = null;
                            break;
                        }
                    }
                }
                case '#' -> {
                    if(startReadingAttribute && attributeContent.getAttributeType() != 0){
                        startReadingDefaultValueType = true;
                        textValue.append(c);
                    }
                }
                case '"', '\'' -> {
                    if(startReadingAttribute && countDoubleQuotes == 0 && c == '"' && defaultValue != null){
                        countDoubleQuotes++;
                        textValue.setLength(0);
                        break;
                    }
                    if(startReadingAttribute && countSingleQuotes == 0 && c == '\'' && defaultValue != null){
                        countSingleQuotes++;
                        textValue.setLength(0);
                        break;
                    }
                    if(startReadingAttribute && (countDoubleQuotes == 1 && c == '"' || countSingleQuotes == 1 && c == '\'')
                            && defaultValue != null){
                        if(!startReadingDefaultValueType && defaultValue.getType() == null
                                && defaultValue.getValue() == null){
                            defaultValue.setValue(textValue.toString());
                        }
                        if(startReadingDefaultValueType && defaultValue.getType() == null || (defaultValue.getType() != null
                                && defaultValue.getType().equals("#FIXED"))){
                            defaultValue.setValue(textValue.toString());
                        }

                        startReadingAttribute = false;
                        startReadingDefaultValueType = false;
                        attributeContent.setDefaultValue(defaultValue);
                        tempContent.add(attributeContent);
                        attributeContent = null;
                        defaultValue = null;
                        textValue.setLength(0);
                        countDoubleQuotes = 0;
                    }
                }
                case '>' -> {
                    if(startReadingAttribute && defaultValue != null && !textValue.isEmpty()){
                        if(defaultValue.getValue() == null
                                && (textValue.toString().equals("#REQUIRED") || textValue.toString().equals("#IMPLIED"))){
                            defaultValue.setType(textValue.toString());
                            attributeContent.setDefaultValue(defaultValue);
                            tempContent.add(attributeContent);
                            attributeContent = null;
                            defaultValue = null;
                            textValue.setLength(0);
                            startReadingAttribute = false;
                            startReadingDefaultValueType = false;
                        }
                    }
                    if(tempContent != null){
                        dtdAttribute.setAll(tempContent);
                        tempContent = null;
                        break OUTER;
                    }
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
        return dtdAttribute;
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
            default -> throw new HJAXException("Unknown element type: " + text);
        };
    }

    private DefaultValue parseDefaultValue(String defaultValue){

        return null;
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
