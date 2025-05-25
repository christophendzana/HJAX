package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.dtd.*;
import hsegment.JObject.Swing.Text.xml.handler.ElementHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DTDParser extends Parser implements XMLValidator {
    private List<DTDAttributeContent> tempContent;
    private ContentModel contentModel;
    private HDTD hdtd;


    public HDTD getDTD(){
        return hdtd;
    }

    public void parse(Reader in) throws IOException {
        reader = new BufferedReader(in);
        tagStack = new TagStack();
        //todo set the name of hdtd
        hdtd = new HDTD("");
        logger = Logger.getLogger(this.getClass().getName());
        textValue = new StringBuilder();
        element = null;
        Entity entity = null;
        DTDAttribute dtdAttribute = null;
        rowIndex++;
        while(reader.ready()){
            columnIndex++;
            c = (char)reader.read();
            switch(c){
                case '<' -> {
                    if(isStartTag){
                        handleError(getErrorSource(),"Not found close blanket !",
                                "Remove open blanket or add close blanket", ErrorType.FatalError);
                    }
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
                                parseDTDElement();
                                isStartTagName = false;
                            }
                            case "ENTITY" -> {
                                entity = parseEntity();
                                System.out.println(entity);
                            }
                            case "ATTLIST" -> {
                                tempContent = new ArrayList<>();
                                dtdAttribute = parseDTDAttribute();
                                System.out.println(dtdAttribute);
                                dtdAttribute = null;
                            }
                            case "--" -> {
                                //handle comment
                                textValue.setLength(0);
                                parseComment();
                            }
                            default -> {
                                handleError(getErrorSource(), "Unknown tag !", "Remove or correct the tag", ErrorType.FatalError);
                            }
                        }
                        textValue.setLength(0);
                    }
                }
                case '>' -> {
                    handleError(getErrorSource(),"Not found open blanket !",
                            "Remove close blanket or add open blanket", ErrorType.FatalError);
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

    protected void parseDTDElement() throws IOException {
        char c;
        textValue = new StringBuilder();
        ContentModel parentContentModel = null;
        element = null;
        OUTER: while (reader.ready()){
            c = (char)reader.read();
            switch (c){
                case ' ' -> {
                    if(!textValue.isEmpty() && parentContentModel == null){
                        // set the element name
                        element = hdtd.getElement(textValue.toString());
                        parentContentModel = parseContentModel(parentContentModel);
                        textValue.setLength(0);
                    }

                }
                case '+', '?', '*', '|', ',' -> {
                    handleError(getErrorSource(),"Invalid position of operator/cardinal : "+c, "Remove the operator/cardinal",
                            ErrorType.FatalError);
                }
                case '>' -> {
                    if(element != null && parentContentModel == null && !textValue.isEmpty()){
                        element.setType(getType(textValue.toString()));
                    }
                    if(element != null && parentContentModel != null && !textValue.isEmpty()){
                        handleError(getErrorSource(),"Invalid content : "+textValue, "Remove the content !", ErrorType.FatalError);
                    }
                    if(element != null && parentContentModel != null){
                        element.setContentModel(parentContentModel);
                        System.out.println(parentContentModel.toString());
                    }
                    textValue.setLength(0);
                    isStartTag = false;
                    break OUTER;
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
    }

    protected ContentModel parseContentModel(ContentModel contentModel) throws IOException {
        char c;
        textValue = new StringBuilder();
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
                        handleError(getErrorSource(),"Invalid position of operator : "+c, "Remove the operator",
                                ErrorType.FatalError);
                    }
                    if(contentModel != null && content != null && !textValue.isEmpty()){
                        content.setName(textValue.toString());
                        contentModel.setValues(content);
                        content = new Content();
                    }
                    textValue.setLength(0);
                    // process of handling sequence operator
                    if(contentModel != null && contentModel.getOperator() == 0){
                        contentModel.setOperator(c);
                    }
                    if(contentModel != null && contentModel.getOperator() != 0 && contentModel.getOperator() != c){
                        //todo handle error
                        System.out.println("Error : Found different operator");
                    }

                    // process of handling choice operator
                }
                case '*', '+', '?' -> {
                    if(contentModel == null){
                        handleError(getErrorSource(),"Invalid position of cardinal : "+c, "Remove the cardinal",
                                ErrorType.FatalError);
                    }
                    // process of handling of many occurrence operator
                    // process of handling not null occurrence operator
                    // process of handling of zero or one occurrence operator
                    if(contentModel != null && !isEndReading){
                        content.setOperator(c);
                    }
                    if(contentModel != null && isEndReading && contentModel.getCardinal() == 0){
                        contentModel.setCardinal(c);
                        return contentModel;
                    }
                    if(contentModel != null && contentModel.getCardinal() != 0 && contentModel.getCardinal() != c){
                        handleError(getErrorSource(), "Found different cardinal",
                                "Check cardinal and remove it", ErrorType.Warning);
                    }
                }
                case '(' -> {
                    if(!textValue.isEmpty()){
                        textValue.append(c);
                        handleError(getErrorSource(),"Invalid content : "+textValue,
                                "Remove the content !", ErrorType.FatalError);
                    }
                    content = new Content();
                    // process of handling start elements group
                    if(contentModel == null){
                        contentModel = new ContentModel();
                    }else{
                        ContentModel temp = new ContentModel();
                        temp = parseContentModel(temp);
                        contentModel.setContent(temp);
                    }
                }
                case ')' -> {
                    if(contentModel == null){
                        handleError(getErrorSource(),"Not found open bracket of content model", "Remove this close bracket",
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

    protected DTDAttribute parseDTDAttribute() throws IOException {
        char c;
        DTDAttribute dtdAttribute = null;
        StringBuilder textValue = new StringBuilder();
        OUTER : while(reader.ready()){
            c = (char)reader.read();
            columnIndex++;
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
        boolean startReadingDefaultValue = false;
        OUTER : while(reader.ready()){
            c = (char)reader.read();
            columnIndex++;
            switch (c){
                case ' ' -> {
                    if(startReadingDefaultValue){
                        textValue.append(c);
                        break;
                    }
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
                    if(startReadingAttribute && attributeContent.getAttributeType() != 0
                            && defaultValue != null && checkDefaultType(textValue.toString())){
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
                    if(startReadingDefaultValue){
                        textValue.append(c);
                        break;
                    }
                    if(startReadingAttribute && attributeContent.getAttributeType() != 0){
                        startReadingDefaultValueType = true;
                        textValue.append(c);
                    }
                }
                case '"', '\'' -> {
                    if(startReadingDefaultValue && countDoubleQuotes == 1 && c == '\''){
                        textValue.append(c);
                        break;
                    }
                    if(startReadingDefaultValue && countSingleQuotes == 1 && c == '"'){
                        textValue.append(c);
                        break;
                    }
                    if(startReadingAttribute && countDoubleQuotes == 0 && c == '"' && defaultValue != null){
                        countDoubleQuotes++;
                        startReadingDefaultValue = true;
                        textValue.setLength(0);
                        break;
                    }
                    if(startReadingAttribute && countSingleQuotes == 0 && c == '\'' && defaultValue != null){
                        countSingleQuotes++;
                        startReadingDefaultValue = true;
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
                        startReadingDefaultValue = false;
                        startReadingAttribute = false;
                        startReadingDefaultValueType = false;
                        attributeContent.setDefaultValue(defaultValue);
                        tempContent.add(attributeContent);
                        attributeContent = null;
                        defaultValue = null;
                        textValue.setLength(0);
                        countDoubleQuotes = 0;
                        countSingleQuotes = 0;
                    }
                }
                case '>' -> {
                    if(startReadingDefaultValue){
                        textValue.append(c);
                        break;
                    }
                    if(startReadingAttribute && defaultValue != null && !textValue.isEmpty()){
                        if(defaultValue.getValue() == null && checkDefaultType(textValue.toString())
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
                    startReadingDefaultValue = false;
                    isStartTag = false;
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

    protected Entity parseEntity() throws IOException {
        char c;
        Entity entity = null;
        StringBuilder textValue = new StringBuilder();
        int countSingleQuotes = 0;
        int countDoubleQuotes = 0;
        boolean startReadingAttributeValue = false;
        while(reader.ready()){
            columnIndex++;
            c = (char)reader.read();
            switch(c){
                case ' ' -> {
                    if(startReadingAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(entity == null && !textValue.isEmpty()){
                        entity = new Entity();
                        entity.setName(textValue.toString());
                        textValue.setLength(0);
                        break;
                    }
                }
                case '"', '\'' -> {
                    if(startReadingAttributeValue && (countSingleQuotes == 1 && c == '"'
                            || countDoubleQuotes == 1 && c == '\'')){
                        textValue.append(c);
                        break;
                    }
                    if(entity != null && countDoubleQuotes == 0 && c == '"'){
                        countDoubleQuotes++;
                        startReadingAttributeValue = true;
                        break;
                    }
                    if(entity != null && countSingleQuotes == 0 && c == '\''){
                        countSingleQuotes++;
                        startReadingAttributeValue = true;
                        break;
                    }
                    if(entity != null && (countDoubleQuotes == 1 && c == '"' || countSingleQuotes == 1 && c == '\'')){
                        entity.setValue(textValue.toString());
                        startReadingAttributeValue = false;
                        countDoubleQuotes = 0;
                        textValue.setLength(0);
                        break;
                    }
                }
                case '>' -> {
                    if(startReadingAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(entity != null && !startReadingAttributeValue){
                        return entity;
                    }
                }
                default -> {
                    textValue.append(c);
                }
            }
        }
        return entity;
    }

    protected void parseNotation(){}

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

    private boolean checkDefaultType(String defaultValue){
        return switch (defaultValue){
            case "#REQUIRED","#IMPLIED","#FIXED" -> true;
            default -> throw new HJAXException("Unknown default type: " + defaultValue);
        };
    }
}
