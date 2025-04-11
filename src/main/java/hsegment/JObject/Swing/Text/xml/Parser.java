package hsegment.JObject.Swing.Text.xml;


import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.xml.process.Doctype;
import hsegment.JObject.Swing.Text.xml.process.HandleDoctype;
import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;
import hsegment.JObject.util.FunctionUtils;

import javax.swing.text.ChangedCharSetException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A parser use to read a xml file character by character, detect and handle a prolog, a doctype, an entity,
 * a processing instruction, a comment and a text data between tag.
 * Tag element is created after reading a close tag, and it is added in a LIFO queue, retrieve only when the close
 * tag with the same name is found.
 * @author Hyacinthe Tsague, Ndzana Christophe
 */
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
    private boolean isHandleEntity;
    private int rowIndex = 1;
    private int columnIndex;
    private boolean isProcessInstruction;
    private boolean isCloseTag;
    private boolean isSingleTag;
    private Doctype doctype;
    private TagStack tagStack;

    private EntityHandler entityHandler;
    private ErrorHandler errorHandler;
    private TagHandler tagHandler;
    private TextHandler textHandler;
    private CommentHandler commentHandler;
    private HandlePrologue prologueHandler;
    private HandleDoctype doctypeHandler;
    private ValidatorHandler validatorHandler;
    private InstructionTagHandler instructionTagHandler;
    private DequeueHandler<TagElement> dequeueHandler;
    private TagStackHandler tagStackHandler;
    private Logger logger;

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

    public HandleDoctype getDoctypeHandler() {
        return doctypeHandler;
    }

    public void setDoctypeHandler(HandleDoctype doctypeHandler) {
        this.doctypeHandler = doctypeHandler;
    }

    public void setEntityHandler(EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    public TagStack getTagStack() {
        return tagStack;
    }

    public Doctype getDoctype() {
        return doctype;
    }

    public void setDequeueHandler(DequeueHandler<TagElement> dequeueHandler) {
        this.dequeueHandler = dequeueHandler;
    }

    public void setTagStackHandler(TagStackHandler tagStackHandler) {
        this.tagStackHandler = tagStackHandler;
    }

    /**
     * Process to parse xml content, by detect and handling errors.
     * @param in the file or string data
     * @throws IOException
     */
    public void parse(Reader in) throws IOException {
        this.reader = new BufferedReader(in);
        tagStack = new TagStack();
        logger = Logger.getLogger(this.getClass().getName());
        char c;
        StringBuilder tagName = new StringBuilder();
        Element element;
        TagElement tagElement;
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
                        break;
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
                    // get the inappropriate text after close tag name
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the question mark is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // found instruction tag
                    if(isStartTag){
                        processingInstructionMark++;
                        isProcessInstruction = true;
                    }
                    isHandleAttributeValue = false;
                }
                // we found space
                case ' ' -> {
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
                    }
                    // add tagName
                    if(isStartTag && isStartTagName && !isHandleAttributeValue
                            && !textValue.isEmpty() && !isDoctypeTag && !isCommentTag){
                        tagName.append(textValue);
                        textValue.setLength(0);
                        isStartTagName = false;
                        isStartTagAttributeName = true;
                        break;
                    }
                    //if we found empty tag name
                    if(isStartTag && isStartTagName && textValue.isEmpty()){
                        handleError("Empty tag name : "+tagName, "Add a tag name !"+". ",
                                ErrorType.FatalError);
                        break;
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
                    // empty attribute name
                    if(isStartTag && !isCloseTag && textValue.isEmpty()){
                        handleError("Empty attribute name", "Add attribute name !",
                                ErrorType.FatalError);
                        break;
                    }
                    // get attribute name
                    if(isStartTag && !isCloseTag && !textValue.isEmpty()){
                        // change state to attribute name state handling
                        isStartTagAttributeName = false;
                        attributeName.append(textValue);
                        textValue.setLength(0);
                    }

                }
                // we found quote
                case '"', '\'' -> {
                    //if it is attribute value, and it inside single quote
                    //like <tagname att='value"""'>
                    if(c == '"' && isStartTagAttributeValue && countSingleQuotes == 1){
                        textValue.append(c);
                        break;
                    }
                    //if it is attribute value, and it inside single quote
                    //like <tagname att="value'''">
                    if( c == '\'' && isStartTagAttributeValue && countDoubleQuotes == 1){
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
                    if(isStartTag && !isStartTagAttributeName && !isStartTagAttributeValue && !isDoctypeTag){
                        if(c == '"'){countDoubleQuotes++;}
                        if(c == '\''){countSingleQuotes++;}
                        isStartTagAttributeValue = true;
                        break;
                    }
                    // end of value
                   if(isStartTag && !isStartTagAttributeName && isStartTagAttributeValue && !isDoctypeTag){
                       isStartTagAttributeValue = false;
                       if(c == '"'){countDoubleQuotes++;}
                       if(c == '\''){countSingleQuotes++;}
                       attributeValue.append(textValue);
                       isHandleAttributeValue = true;
                   }
                   if(isStartTag && !isStartTagName && isStartTagAttributeName){
                       textValue.append(c);
                       break;
                   }
                   // count dtd quote of file path
                   if(c == '"' && isStartTag && isDoctypeTag){
                       textValue.append(c);
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
                //start handle entity
                case '&' -> {
                    textValue.append(c);
                    if((isStartTagAttributeValue && !isDoctypeTag && !isCommentTag) || isHandleText){
                        isHandleEntity = true;
                    }
                }
                // end handle entity
                case ';' -> {
                    textValue.append(c);
                    if(isHandleEntity){
                        String entityName = textValue.substring(textValue.indexOf("&"));
                        isHandleEntity = false;
                        handleEntity(entityName);
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
                        if(!isStartTagAttributeValue && !isDoctypeTag &&
                                !isCommentTag && !isStartTagName && !textValue.isEmpty()){
                            handleError("Invalid content :"+textValue+" in tag", "Remove the content !", ErrorType.FatalError);
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
                            element = parseElement(tagName.toString(), attributeKVList);
                            tagElement = new TagElement(element, Constants.INSTRUCTION_TAG);
                            handleInstructionTag(tagElement);
                        }
                        // close and single tag validation
                        if(countSlash > 1){
                            handleError("Invalid tag, more than two slash !",
                                    "Close or Single tag must have only two slash", ErrorType.FatalError);
                            break;
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
                        if(isCommentTag && !textValue.toString().contains("--")){
                            handleError("Invalid comment syntax", "Comment must have -- before the close blanket",
                                    ErrorType.FatalError);
                            break;
                        }
                        if(isCommentTag && textValue.toString().endsWith("--")){
                            parseComment(textValue.toString());
                        }
                        // if we found text after a tag name of close tag, we handle error
                        if(isCloseTag && !textAfterTagName.isEmpty()){
                            handleError("Invalid text content in close tag !",
                                    "Check the text "+textAfterTagName+" after tag name and remove it.",
                                    ErrorType.Warning);
                        }
                        // parse and save new element, element is not prolog, doctype or comment
                        if(!isDoctypeTag && !isCommentTag && !isProcessInstruction){
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
                            handleTagDequeue(tagElement);
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
                    addAttribute(attributeKVList, attributeName.toString(), attributeValue.toString());
                    attributeName.setLength(0);
                    attributeValue.setLength(0);
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
        // check the empty state
        if(dequeueHandler.getEmptyState() != 1){
            handleError("XML file don't have a single root tag", "Add single root tag !", ErrorType.Warning);
        }
    }

    /**
     * Save temporary attributes to the list
     * @param attributeKVList
     * @param attributeName
     * @param attributeValue
     */
    private void addAttribute(final List<AttributeKV> attributeKVList, String attributeName, String attributeValue){
        if(attributeKVList.isEmpty()){
            attributeKVList.add(new AttributeKV(attributeName, attributeValue));
            return;
        }
        for(AttributeKV attributeKV : attributeKVList){
            if(attributeKV.getName().equals(attributeName)){
                handleError("Duplicate attribute name : "+attributeName, "Remove duplicate attribute",
                        ErrorType.FatalError);
                break;
            }
        }
        attributeKVList.add(new AttributeKV(attributeName, attributeValue));
    }

    /**
     * Reset for reading next character
     */
    private void reset(){
        // change state to handle next content of tag
        isStartTag = false;
        isHandleText = true;
        isStartTagAttributeName = false;
        isProcessInstruction = false;
        isCommentTag = false;
        isDoctypeTag = false;
        isHandleAttributeValue = false;
        isHandleEntity = false;
        isCloseTag = false;
        isSingleTag = false;
    }

    /**
     * Parse comment content
     * @param textComment inside comment tag
     */
    private void parseComment(String textComment){
        // get and handle comment content
        String commentContent = textComment.substring(0,textComment.length()-2);
        handleComment(commentContent, rowIndex);
    }

    /**
     * Set the type of tag element
     * @param element
     */
    private void setElementType(Element element) {
        element.setType(Constants.OPEN_TAG);
        if(isCloseTag){
            element.setType(Constants.CLOSE_TAG);
        }
        if(isSingleTag){
            element.setType(Constants.SINGLE_TAG);
        }
    }

    /**
     * check and add space character in text data when we found large space,
     * line return or tabulation on a text data.
     * @param content
     */
    private void addSpace(StringBuilder content){
        if(!content.isEmpty() && content.charAt(content.length()-1) != ' '){
            content.append(' ');
        }
    }

    /**
     * Parse doctype name, the doctype name is the first tag of xml content, so it is the root tag.
     * As a tag name, it must be validated like tag name.
     * @param doctypeName
     */
    private void parseDoctypeName(String doctypeName){
        if(doctypeName.isBlank()){
            handleError("Empty DOCTYPE name directive ", "Add the 'DOCTYPE' directive name", ErrorType.Warning);
        }
        if(!FunctionUtils.verifyStartName(doctypeName)){
            handleError("Invalid DOCTYPE first character !", "Add a valid first character", ErrorType.FatalError);
        }
        if(!FunctionUtils.verifyName(doctypeName)){
            handleError("Invalid DOCTYPE name !", "Add a valid name ", ErrorType.FatalError);
        }
        doctype.setDtdName(doctypeName);
    }

    /**
     * Parse doctype tag
     * @param doctypeContent
     */
    private void parseDoctype(String doctypeContent) {
        String doctypeName;
        doctype = new Doctype();
        // if doctype has internal dtd
        if(doctypeContent.contains("[") && doctypeContent.contains("]")){
            doctypeName = doctypeContent.substring(0, doctypeContent.indexOf("["));
            parseDoctypeName(doctypeName);
            // retrieving content between [ and ] of internal dtd
            String dtdContent = doctypeContent.substring(doctypeContent.indexOf("[") + 1, doctypeContent.indexOf("]"));
            doctype.setInternal(true);
            doctype.setDtdContent(dtdContent);
            handleDoctype(doctypeName, dtdContent);
        }
        // if doctype has external dtd
        if(!doctypeContent.contains("[") && !doctypeContent.contains("]")){
            // get the content of doctype tag separate by space
            String[] values = doctypeContent.split(" ");
            List<String> contents = new ArrayList<>();
            // remove extra space
            for(int i = 0; i < values.length; i++){
                if(!values[i].isBlank()){
                    contents.add(values[i]);
                }
            }
            // doctype with external dtd
            if(contents.size() == 3){
                doctypeName = contents.get(0);
                String doctypeLocationType = contents.get(1);
                String doctypeFilePath = contents.get(2);
                parseDoctypeName(doctypeName);
                // set doctype location
                if(doctypeLocationType.equals("SYSTEM") || doctypeLocationType.equals("PUBLIC")){
                    doctype.setLocationType(doctypeLocationType);
                }else{
                    handleError("Invalid doctype location type !",
                            "Add the valid doctype location type PUBLIC or SYSTEM", ErrorType.Warning);
                }
                // set doctype file path
                if(doctypeFilePath.startsWith("\"") && doctypeFilePath.endsWith("\"")
                        && doctypeFilePath.endsWith(".dtd\"") && doctypeFilePath.length() > 6){
                    doctype.setDtdFilePath(doctypeFilePath.substring(1, doctypeFilePath.length()-1));
                    doctype.setExternal(true);
                    handleDoctype(doctypeName, doctypeLocationType, doctypeFilePath);
                }else{
                    handleError("Invalid doctype file path !", "Add the doctype file path", ErrorType.Warning);
                }
            }else{
                handleError("Invalid doctype tag", "Add the valid directives of doctype tag", ErrorType.Warning);
            }
        }
    }

    /**
     * Create a tag element
     * @param tagName a tag name of element
     * @param attributeKVList a list of attributes of element
     * @return
     */
    private Element parseElement(String tagName, final List<AttributeKV> attributeKVList) {
        // create new Element
        Element element = new Element(rowIndex, columnIndex, tagName);
        setElementType(element);
        // save attributes values for single and open tag
        if((element.getType() == Constants.OPEN_TAG || element.getType() == Constants.SINGLE_TAG)
                && !attributeKVList.isEmpty()){
            // create attributes for open tag only
            // save attributeList on element
            element.setAttributeList(getAttributeList(attributeKVList));
        }
        return element;
    }

    /**
     * Get attributes list of elements from a list parameter
     * @param attributeKVList
     * @return a attributeList of element
     */
    private AttributeList getAttributeList(List<AttributeKV> attributeKVList) {
        AttributeList attributeList = new AttributeList();
        // add the first attribute
        attributeList.setName(attributeKVList.get(0).getName());
        attributeList.setValue(attributeKVList.get(0).getValue());
        AttributeList newAttributeList;
        // add the rest of attributes on the attributeList chain 'next'
        for(int i = 1; i < attributeKVList.size(); i++){
            newAttributeList = new AttributeList();
            newAttributeList.setName(attributeKVList.get(i).getName());
            newAttributeList.setValue(attributeKVList.get(i).getValue());
            attributeList.checkNext(newAttributeList);
        }
        return attributeList;
    }

    /**
     * Handle the text data
     * @param text
     */
    protected synchronized void handleText(String text) {
        if(text.trim().isEmpty())
            return;
        if(textHandler != null){
            this.textHandler.handleText(text);
        }
    }

    /**
     * Handle the comment content
     * @param comment
     * @param rowIndex
     */
    protected void handleComment(String comment, int rowIndex) {
        try {
            if(commentHandler != null){
                commentHandler.handleComment(comment, rowIndex);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Handle an empty tag
     * @param tag
     * @throws ChangedCharSetException
     */
    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        try {
            if(tagHandler != null){
                tagHandler.handleEmptyTag(tag);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }

    }

    /**
     * Handle an open tag
     * @param tag
     */
    protected void handleStartTag(TagElement tag) {
        try {
            if(tagHandler != null){
                tagHandler.handleStartTag(tag);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }

    }

    /**
     * Handle an instruction tag
     * @param tag
     */
    protected void handleInstructionTag(TagElement tag){
        try {
            // prologue handling
            if(prologueHandler != null && tag.getElement().getName().equals("xml")){
                String version = null;
                String encoding = null;
                Boolean standAlone = null;
                AttributeList att = tag.getElement().getAttributeList();
                while(att != null){
                    switch(att.getName()){
                        case "version" -> version = att.getValue();
                        case "encoding" -> encoding = att.getValue();
                        case "standalone" -> standAlone = switch (att.getValue().trim()){
                            case "yes" -> true;
                            case "no" -> false;
                            default -> throw new HJAXException("Unknown attribute value '" + att.getValue() + "' " +
                                    "of attribute name : "+att.getName());
                        };
                        default -> throw new HJAXException("Unknown attribute name : "+att.getName()+" in prologue tag");
                    }
                    att = att.getNext();
                }
                handlePrologue(version, encoding, standAlone);
                return;
            }
            // instruction handling
            if(instructionTagHandler != null){
                instructionTagHandler.handleInstruction(tag);
            }
        } catch (Exception e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Handle close tag
     * @param tag
     */
    protected void handleEndTag(TagElement tag) {
        try {
            if(tagHandler != null){
                tagHandler.handleEndTag(tag);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Called when an error occurred into code. <code>type</code> can have two values either it
     * is equal to Fatal error in this case parser stop parse document, or it is equal to Warning
     * in this case parser continue to parse document after error declaration but event if
     * parser declare that error is a warning it's possible to stop parser immediately by
     * throwing HJAXException
     *
     * @param msg message error
     * @param debug how to debug error
     * @param type error type
     * @throws HJAXException if parser should stop parse
     */
    protected void handleError(String msg, String debug, ErrorType type) throws HJAXException{
        try {
           if(errorHandler != null){
               errorHandler.errorHandler("Row :"+rowIndex+", Column : "+columnIndex, msg, debug, type);
           }
        } catch (NullPointerException e) {
            logger.severe("NullPointerException :"+e.getMessage());
        } catch(HJAXException e){
            logger.severe(e.getMessage());
        }finally{
            try {
                // stop the reading of file
                if(errorHandler != null && type == ErrorType.FatalError){
                    reader.close();
                }
            } catch (Exception e) {
                logger.severe("Exception :"+e.getMessage());
            }
        }
    }

    /**
     * Handle error message provide by throws
     * @param msg
     */
    private void handleError(String msg){
        try {
            if(errorHandler != null){
                logger.severe(msg);
                reader.close();
            }
        }catch (Exception e){
            logger.severe(e.getMessage());
        }
    }

    /**
     * Handle a doctype with external dtd
     * @param dtdName a name of doctype
     * @param locationType a location type of doctype with external dtd
     * @param dtdFilePath a path of doctype with external dtd
     * @throws HJAXException
     */
    private void handleDoctype(String dtdName, String locationType, String dtdFilePath) throws HJAXException{
        try{
            if(doctypeHandler != null){
                doctypeHandler.handleDoctype(dtdName, locationType, dtdFilePath);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    /**
     * Handle a doctype with internal dtd
     * @param dtdName a name of doctype
     * @param content a content of dtd
     * @throws HJAXException
     */
    private void handleDoctype(String dtdName, String content) throws HJAXException{
        try{
            if(doctypeHandler != null){
                doctypeHandler.handleDoctype(dtdName, content);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    /**
     * Handle prologue
     * @param version a version provide from attribute
     * @param encoding an encoding provide from attribute
     * @param isStandalone a standalone provide by attribute
     * @throws HJAXException
     */
    private void handlePrologue(String version, String encoding, Boolean isStandalone) throws HJAXException{
        try{
            if(prologueHandler != null){
                prologueHandler.handlePrologue(version, encoding, isStandalone);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    /**
     * Handle entity
     * @param name
     */
    private void handleEntity(String name){
        try {
            if(entityHandler != null){
                entityHandler.handleEntity(name);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    /**
     * Handle queue
     * @param tagElement
     */
    private void handleTagDequeue(TagElement tagElement){
        try {
            if(dequeueHandler != null){
                dequeueHandler.autoProcess(tagElement);
            }
        }catch (HJAXException e){
            handleError(e.getMessage(), "Check corresponding tag on file", ErrorType.FatalError);
        }
    }

    /**
     * Handle tag stack
     * @param tagElement
     */
    private void handleTagStack(TagElement tagElement){
        try{
            if(tagStackHandler != null){
                tagStackHandler.autoProcess(tagElement);
            }
        }catch (HJAXException e){
            handleError(e.getMessage(), "Check corresponding tag on file", ErrorType.FatalError);
        }
    }
}
