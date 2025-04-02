package hsegment.JObject.Swing.Text.xml;


import hsegment.JObject.Swing.Text.ErrorType;
import  hsegment.JObject.Swing.Text.ErrorType.*;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.xml.process.DefaultDoctypeHandler;
import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;

import javax.swing.text.ChangedCharSetException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Parser {
    private Reader reader;
    private boolean isStartTag;
    private boolean isStartTagName;
    private boolean isStartTagAttributeName;
    private boolean isStartTagAttributeValue;
    private boolean isDoctypeTag;
    private boolean isCommentTag;
    private boolean isHandleText;
    private boolean isHandleAttributeValue;
    private int rowIndex = 1;
    private int columnIndex;
    private boolean isProcessInstruction;
    private boolean isCloseTag;
    private boolean isSingleTag;
    private DefaultDoctypeHandler doctypeHandler;
    private TagStack tagStack;

    private EntityHandler entityHandler;
    private ErrorHandler errorHandler;
    private TagHandler tagHandler;
    private TextHandler textHandler;
    private CommentHandler commentHandler;
    private HandlePrologue prologueHandler;
    private ValidatorHandler validatorHandler;
    private InstructionTagHandler instructionTagHandler;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public InstructionTagHandler getInstructionTagHandler() {
        return instructionTagHandler;
    }

    public void setInstructionTagHandler(InstructionTagHandler instructionTagHandler) {
        this.instructionTagHandler = instructionTagHandler;
    }

    public ValidatorHandler getValidatorHandler() {
        return validatorHandler;
    }

    public void setValidatorHandler(ValidatorHandler validatorHandler) {
        this.validatorHandler = validatorHandler;
    }

    public HandlePrologue getPrologueHandler() {
        return prologueHandler;
    }

    public void setPrologueHandler(HandlePrologue prologueHandler) {
        this.prologueHandler = prologueHandler;
    }

    public CommentHandler getCommentHandler() {
        return commentHandler;
    }

    public void setCommentHandler(CommentHandler commentHandler) {
        this.commentHandler = commentHandler;
    }

    public TextHandler getTextHandler() {
        return textHandler;
    }

    public void setTextHandler(TextHandler textHandler) {
        this.textHandler = textHandler;
    }

    public TagHandler getTagHandler() {
        return tagHandler;
    }

    public void setTagHandler(TagHandler tagHandler) {
        this.tagHandler = tagHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public EntityHandler getEntityHandler() {
        return entityHandler;
    }

    public void setEntityHandler(EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    public TagStack getTagStack() {
        return tagStack;
    }

    public DefaultDoctypeHandler getDoctypeHandler() {
        return doctypeHandler;
    }

    public void parse(Reader in) throws IOException {
        this.reader = new BufferedReader(in);
        tagStack = new TagStack();
        char c;
        StringBuilder tagName = new StringBuilder();
        Element element;
        TagElement tagElement;
        AttributeKV attributeKV;
        List<AttributeKV> attributeKVList = new ArrayList<>();
        StringBuilder textValue = new StringBuilder();
        StringBuilder attributeName = new StringBuilder();
        StringBuilder attributeValue = new StringBuilder();
        int countSingleQuotes = 0;
        int countDoubleQuotes = 0;
        int countSlash = 0;
        int processingInstructionMark = 0;
        //save invalid attributes syntax to handleError
        StringBuilder textAfterTagName = new StringBuilder();
        while (reader.ready()) {
            columnIndex++;
            c = (char) reader.read();
            switch (c) {
                // we found open blanket
                case '<' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    // if the blanket is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // check if the previous blanket has not closed
                    if(isStartTag){
                        handleError("Tag without close blanket", "Add a close blanket", ErrorType.FatalError);
                    }
                    // handle text content between open and close tag
                    if(!isStartTag && isHandleText){
                        handleText(textValue.toString());
                    }
                    // change state to open tag state
                    isStartTag = true;
                    isStartTagName = true;
                    isHandleText = false;
                    textValue.setLength(0);
                    isHandleAttributeValue = false;
                    tagName.setLength(0);
                    textAfterTagName.setLength(0);
                }
                // we found instruction
                case '?' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }

                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the question mark is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    if(isStartTag){
                        processingInstructionMark++;
                        isProcessInstruction = true;
                    }
                    isHandleAttributeValue = false;
                }
                // we found space
                case ' ' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    // add the text content found after a tag name of close tag
                    // like </tagname another content>, we add 'another content' for show  it on error handling
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if we found space outside or inside attribute name
                    // we add this space to the attribute name for error handler
                    if(isStartTag && !isStartTagName && !textValue.isEmpty()){
                        textValue.append(c);
                        System.out.println("ATT Name :"+textValue);
                    }
                    // add tagName
                    if(isStartTag && isStartTagName && !isHandleAttributeValue
                            && !textValue.isEmpty() && !isDoctypeTag && !isCommentTag){
                        tagName.append(textValue);
                        textValue.setLength(0);
                        isStartTagName = false;
                    }
                    // add attributes
                    if(isStartTag && isStartTagAttributeValue && !isDoctypeTag && isHandleAttributeValue
                            && !isCommentTag && !tagName.isEmpty()){
                        attributeKV = new AttributeKV(attributeName.toString(), attributeValue.toString());
                        attributeKVList.add(attributeKV);
                        attributeName.setLength(0);
                        attributeValue.setLength(0);
                    }
                    // if the previous tag name text is doctype
                    if(isStartTag && isDoctypeTag && textValue.toString().equals("DOCTYPE")){
                        isCommentTag = false;
                        textValue.setLength(0);
                    }
                    // if the previous tag name is comment
                    if(isStartTag && isCommentTag && textValue.toString().equals("--")){
                        isDoctypeTag = false;
                        textValue.setLength(0);
                    }
                    // get the doctype content
                    if(isCommentTag || isDoctypeTag){
                        textValue.append(c);
                    }
                    if(isHandleText){
                       addSpace(textValue);
                    }
                }
                // we found open bracket to read internal dtd
                case '[' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the bracket is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // if we are reading doctype tag
                    if(isDoctypeTag){
                        char ch;
                        // we add a char '['
                        textValue.append(c);
                        // we add the dtd content present on doctype tag
                        while (reader.ready()) {
                            ch = (char) reader.read();
                            textValue.append(ch);
                            System.out.println("TEXT VALUE :"+textValue);
                            if(ch == ']'){
                                break;
                            }
                        }

                    }
                    // if the char is present on text data
                    if(isHandleText){
                        textValue.append(c);
                    }
                }
                // we found equal symbol
                case '=' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the equal sign is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // get attribute name
                    if(isStartTag && !isCloseTag){
                        // change state to attribute name state handling
                        isStartTagAttributeName = true;
                        attributeName.append(textValue);
                        textValue.setLength(0);
                    }
                }
                // we found quote
                case '"' -> {
                    //if it is attribute value, and it inside single quote
                    //like <tagname att='value"""'>
                    if(isStartTagAttributeValue && countSingleQuotes == 1){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the quote is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // change state to attribute value state handling
                    if(isStartTag && !isStartTagAttributeValue && !isDoctypeTag){
                        countDoubleQuotes++;
                        isStartTagAttributeValue = true;
                        break;
                    }
                    // end of value
                   if(isStartTag && isStartTagAttributeValue && !isDoctypeTag){
                       isStartTagAttributeValue = false;
                       countDoubleQuotes++;
                       attributeValue.append(textValue);
                       isHandleAttributeValue = true;
                   }
                   // count dtd quote of file path
                   if(isStartTag && isDoctypeTag){
                       textValue.append(c);
                   }
                }
                case '\'' -> {
                    //if it is attribute value, and it inside single quote
                    //like <tagname att="value'''">
                    if(isStartTagAttributeValue && countDoubleQuotes == 1){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the quote is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // change state to attribute value state handling
                    if(isStartTag && !isStartTagAttributeValue && !isDoctypeTag){
                        countSingleQuotes++;
                        isStartTagAttributeValue = true;
                        break;
                    }
                    // end of value
                    if(isStartTag && isStartTagAttributeValue && !isDoctypeTag){
                        isStartTagAttributeValue = false;
                        countSingleQuotes++;
                        attributeValue.append(textValue);
                        isHandleAttributeValue = true;
                    }
                }
                // we found slash
                case '/' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the slash is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // if it inside a tag: maybe a close or single tag
                    if(isStartTag){
                        // for close tag
                        if(textValue.isEmpty()){
                            isCloseTag = true;
                        }else {
                            isSingleTag = true;
                        }
                        countSlash++;
                    }else{
                        textValue.append(c);
                    }
                }
                // we found exclamation symbol, we can handle doctype or comment
                case '!' -> {
                    //if it is attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the symbol is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    if(isStartTag && !isCloseTag && !isSingleTag) {
                        isStartTagName = true;
                        isDoctypeTag = true;
                        isCommentTag = true;
                    }
                }
                // we found close blanket
                case '>' -> {
                    if(isStartTag){
                        //if it is attribute value
                        if(isStartTagAttributeValue){
                            textValue.append(c);
                            break;
                        }
                        // add the character when it is inside a comment text
                        if(isCommentTag && !textValue.toString().contains("--")){
                            textValue.append(c);
                            break;
                        }
                        // instruction validation
                        if(isProcessInstruction && processingInstructionMark != 2){
                            handleError("Invalid instruction !", "Instruction must have only two question marks", ErrorType.Warning);
                        }
                        if(isProcessInstruction && processingInstructionMark == 2){
                            //@todo handle instruction here
                            System.out.println("Handle instruction");
                        }
                        if(isProcessInstruction){
                            //clear attributes key/value list
                            attributeKVList.clear();
                        }
                        // close and single tag validation
                        if(countSlash > 1){
                            handleError("Invalid tag, more than two slash !",
                                    "Close or Single tag must have only two slash", ErrorType.FatalError);
                        }
                        // for tags without attributes
                        if(attributeKVList.isEmpty()){
                            tagName.append(textValue);
                        }
                        // doctype validation
                        if(isDoctypeTag){
                            parseDoctype(textValue.toString());
                        }
                        //comment validation
                        if(isCommentTag && textValue.toString().endsWith("--")){
                            parseComment(textValue.toString());
                        }
                        if(isCommentTag && !textValue.toString().contains("--")){
                            handleError("Invalid comment syntax", "Comment must have -- before the close blanket",
                                    ErrorType.FatalError);
                        }
                        // parse and save new element, element is not prolog, doctype or comment
                        if(!isProcessInstruction && !isDoctypeTag && !isCommentTag){
                            if(isCloseTag && !textAfterTagName.isEmpty()){
                                handleError("Invalid text content in close tag !",
                                        "Check the text "+textAfterTagName+" after tag name and remove it.",
                                        ErrorType.Warning);
                            }
                            element = parseElement(tagName.toString(), attributeKVList);
                            // save tag element
                            tagElement = new TagElement(element, element.getType());
                            //handle start tag
                            if(tagElement.getType() == Constants.OPEN_TAG){
                                handleStartTag(tagElement);
                            }
                            if(tagElement.getType() == Constants.CLOSE_TAG){
                                handleEndTag(tagElement);
                            }
                            if(tagElement.getType() == Constants.SINGLE_TAG){
                                handleEmptyTag(tagElement);
                            }

                            // add tag element to the stack
                            tagStack.stack(tagElement);
                        }
                        reset();
                        //clear attributes key/value list
                        attributeKVList.clear();
                        processingInstructionMark = 0;
                        countSlash = 0;
                        textValue.setLength(0);
                        textAfterTagName.setLength(0);

                    }else{
                        handleError("Tag without open blanket", "Add the open blanket", ErrorType.FatalError);
                    }
                }
                // we found next line
                case '\n' -> {
                    if(isHandleText){
                        addSpace(textValue);
                    }
                    columnIndex = 0;
                    rowIndex++;
                }
                // we found tabulation
                case '\t' -> {
                    if(isHandleText){
                        addSpace(textValue);
                    }
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                    }
                    columnIndex += 8;
                }
                // we found carriage return
                case '\r' -> {
                    columnIndex = 0;
                }

                default -> {
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    textValue.append(c);
                }
            }
            // handle attribute value
            if(isStartTag && isHandleAttributeValue){
                if(countSingleQuotes == 2 || countDoubleQuotes == 2){
                    attributeValue.setLength(0);
                    attributeValue.append(textValue);
                    // save attribute name/value
                    attributeKV = new AttributeKV(attributeName.toString(), attributeValue.toString());
                    attributeKVList.add(attributeKV);
                    attributeName.setLength(0);
                    textValue.setLength(0);
                    // reset quotes counters to count another quote of attributes value
                    countSingleQuotes = 0;
                    countDoubleQuotes = 0;
                    // change state to get another attribute name/value
                    isStartTagAttributeValue = false;
                    // end of handling attribute value
                    isHandleAttributeValue = false;
                    textAfterTagName.setLength(0);
                }else{
                    handleError("Invalid quote number of attribute value", "Attribute value must have only two quotes",
                            ErrorType.FatalError);
                }

            }
        }
    }

    private void reset(){
        // change state to handle next content of tag
        isStartTag = false;
        isHandleText = true;
        isProcessInstruction = false;
        isCommentTag = false;
        isDoctypeTag = false;
        isHandleAttributeValue = false;
        isCloseTag = false;
        isSingleTag = false;
    }

    private void parseComment(String textComment){
        // get and handle comment content
        String commentContent = textComment.substring(0,textComment.length()-2);
        handleComment(commentContent, rowIndex);
    }

    private void setElementType(Element element) {
        element.setType(Constants.OPEN_TAG);
        if(isCloseTag){
            element.setType(Constants.CLOSE_TAG);
        }
        if(isSingleTag){
            element.setType(Constants.SINGLE_TAG);
        }
    }

    private void addSpace(StringBuilder content){
        if(!content.isEmpty() && content.charAt(content.length()-1) != ' '){
            content.append(' ');
        }
    }

    private void parseDoctypeName(String doctypeName){
        if(doctypeName.isBlank()){
            handleError("Empty doctype name directive ", "Add the 'DOCTYPE' directive name", ErrorType.Warning);
        }else{
            doctypeHandler.setDtdName(doctypeName);
        }
    }
    private void parseDoctype(String doctypeContent) {
        String doctypeName;
        doctypeHandler = new DefaultDoctypeHandler();
        // if doctype has internal dtd
        if(doctypeContent.contains("[") && doctypeContent.contains("]")){
            doctypeName = doctypeContent.substring(0, doctypeContent.indexOf("["));
            parseDoctypeName(doctypeName);
            String dtdContent = doctypeContent.substring(doctypeContent.indexOf("[") + 1, doctypeContent.indexOf("]"));
            doctypeHandler.setInternal(true);
            doctypeHandler.setDtdContent(dtdContent);
            //todo call dtd parser method to parse dtd
        }
        // if doctype has external dtd
        if(!doctypeContent.contains("[") && !doctypeContent.contains("]")){
            String[] values = doctypeContent.split(" ");
            List<String> contents = new ArrayList<>();
            for(int i = 0; i < values.length; i++){
                if(!values[i].isBlank()){
                    contents.add(values[i]);
                }
            }
            System.out.println(Arrays.toString(contents.toArray()));
            // doctype with external dtd
            if(contents.size() == 3){
                doctypeName = contents.get(0);
                String doctypeLocationType = contents.get(1);
                String doctypeFilePath = contents.get(2);
                parseDoctypeName(doctypeName);

                // set doctype location
                if(doctypeLocationType.equals("SYSTEM") || doctypeLocationType.equals("PUBLIC")){
                    doctypeHandler.setLocationType(doctypeLocationType);
                }else{
                    handleError("Invalid doctype location type !",
                            "Add the valid doctype location type PUBLIC or SYSTEM", ErrorType.Warning);
                }
                // set doctype file path
                if(doctypeFilePath.startsWith("\"") && doctypeFilePath.endsWith("\"")
                        && doctypeFilePath.endsWith(".dtd\"") && doctypeFilePath.length() > 6){
                    doctypeHandler.setDtdFilePath(doctypeFilePath.substring(1, doctypeFilePath.length()-1));
                    doctypeHandler.setExternal(true);
                    //todo get the doctype from path and call parse dtd
                }else{
                    handleError("Invalid doctype file path !", "Add the doctype file path", ErrorType.Warning);
                }
            }else{
                handleError("Invalid doctype tag", "Add the valid directives of doctype tag", ErrorType.Warning);
            }
        }
    }

    private Element parseElement(String tagName, final List<AttributeKV> attributeKVList) {
        // create new Element
        Element element = new Element(rowIndex, columnIndex, tagName);
        setElementType(element);
        System.out.println("----------------------------");
        // save attributes values for single and open tag
        if((element.getType() == Constants.OPEN_TAG || element.getType() == Constants.SINGLE_TAG)
                && !attributeKVList.isEmpty()){
            // create attributes for open tag only
            AttributeList attributeList = getAttributeList(attributeKVList);
            // save attributeList on element
            element.setAttributeList(attributeList);
        }
        if(element.getType() == Constants.CLOSE_TAG && !attributeKVList.isEmpty()){
            handleError("Close tag must not have attributes !", "Remove attributes from close tag !",
                    ErrorType.FatalError);
        }
        return element;
    }

    private AttributeList getAttributeList(List<AttributeKV> attributeKVList) {
        AttributeList attributeList = new AttributeList();
        // add the first attribute
        attributeList.setName(attributeKVList.get(0).getName());
        attributeList.setValue(attributeKVList.get(0).getValue());
        System.out.println("Attribute value :"+attributeList.getValue());
        AttributeList newAttributeList;
        // add the rest of attributes on the attributeList chain 'next'
        for(int i = 1; i < attributeKVList.size(); i++){
            newAttributeList = new AttributeList();
            newAttributeList.setName(attributeKVList.get(i).getName());
            System.out.println("Attribute value : " + attributeKVList.get(i).getValue());
            newAttributeList.setValue(attributeKVList.get(i).getValue());
            attributeList.checkNext(newAttributeList);
        }
        return attributeList;
    }

    protected synchronized void handleText(String text) {
        if(text.trim().isEmpty())
            return;
        this.textHandler.handleText(text);
    }

    protected void handleTitle(String text) {
        // default behavior is to call handleText. Subclasses
        // can override if necessary.
        handleText(text);
    }


    protected void handleComment(String comment, int rowIndex) {
        try {
            commentHandler.handleComment(comment, rowIndex);
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }


    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        try {
            tagHandler.handleEmptyTag(tag);
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }

    }

    protected void handleStartTag(TagElement tag) {
        tagStack.stack(tag);
        try {
            tagHandler.handleStartTag(tag);
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }

    }

    protected void handleInstructionTag(TagElement tag){
        try {
            instructionTagHandler.handleInstruction(tag);
        } catch (Exception e) {
        }
    }

    protected void handleEndTag(TagElement tag) {
        try {
            tagHandler.handleEndTag(tag);
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Called when an error occured into code. <code>type</code> can have two value either it
     * is egal to Fatal error in this case parser stop parse document or it is egal to Warning
     * in this case parser continue to parse document after error declaration but event if
     * parser declare that error is a warning it's possible to stop parser immidiatly by
     * throwing HJAXException
     *
     * @param msg message error
     * @param debug how to debug error
     * @param type error type
     * @throws HJAXException if parser should stop parse
     */
    protected void handleError(String msg, String debug, ErrorType type) throws HJAXException{
        try {
            errorHandler.errorHandler("Row :"+rowIndex+", Column : "+columnIndex, msg, debug, type);
        } catch (NullPointerException e) {
            logger.severe("NullPointerException :"+e.getMessage());
        } catch(HJAXException e){
            logger.severe(e.getMessage());
        }finally{
            try {
                if(type == ErrorType.FatalError){
                    reader.close();
                }
            } catch (Exception e) {
                logger.severe("Exception :"+e.getMessage());
            }
        }
    }

    private void handleError(String msg){
        logger.severe(msg);
        try {
            reader.close();
        }catch (Exception e){
            logger.severe(e.getMessage());
        }
    }

}
