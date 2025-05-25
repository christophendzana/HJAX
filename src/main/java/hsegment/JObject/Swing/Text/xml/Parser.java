package hsegment.JObject.Swing.Text.xml;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

import hsegment.JObject.Swing.Text.xml.handler.*;
import hsegment.JObject.Swing.Text.xml.process.Doctype;
import hsegment.JObject.Swing.Text.xml.process.HandleDoctype;
import hsegment.JObject.Swing.Text.xml.process.HandlePrologue;
import hsegment.JObject.Swing.Text.xml.process.Prologue;

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
public class Parser implements XMLValidator {
    protected Reader reader;
    protected boolean isStartTag;
    protected boolean isStartTagName;
    private boolean isStartTagAttributeName;
    private boolean isStartTagAttributeValue;
    private boolean isDoctypeTag;
    private boolean isCommentTag;
    private boolean isHandleText;
    private boolean isHandleAttributeValue;
    private boolean isHandleEntity;

    protected Element element;
    private TagElement tagElement;
    private List<AttributeKV> attributeKVList;
    protected StringBuilder textValue;
    private StringBuilder attributeName;
    private StringBuilder attributeValue;
    private int countSingleQuotes = 0;
    private int countDoubleQuotes = 0;
    private int countSlash = 0;
    private int processingInstructionMark = 0;
    private StringBuilder tagName;
    private StringBuilder textAfterTagName;
    protected char c;


    protected int rowIndex = 1;
    protected int columnIndex;
    private boolean isProcessInstruction;
    private boolean isCloseTag;
    private boolean isSingleTag;
    private Doctype doctype;
    private Prologue prologue;
    protected TagStack tagStack;

    private EntityHandler entityHandler;
    protected ErrorHandler errorHandler;
    private TagHandler tagHandler;
    private TextHandler textHandler;
    private CommentHandler commentHandler;
    private HandlePrologue prologueHandler;
    private HandleDoctype doctypeHandler;
    private ValidatorHandler validatorHandler;
    private InstructionTagHandler instructionTagHandler;
    private DequeueHandler<TagElement> dequeueHandler;
    private TagStackHandler tagStackHandler;
    protected Logger logger;
    protected XMLRules xmlRules;

    public EntityHandler getEntityHandler() {
        return entityHandler;
    }

    public void setEntityHandler(EntityHandler entityHandler) {
        this.entityHandler = entityHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public TagHandler getTagHandler() {
        return tagHandler;
    }

    public void setTagHandler(TagHandler tagHandler) {
        this.tagHandler = tagHandler;
    }

    public TextHandler getTextHandler() {
        return textHandler;
    }

    public void setTextHandler(TextHandler textHandler) {
        this.textHandler = textHandler;
    }

    public CommentHandler getCommentHandler() {
        return commentHandler;
    }

    public void setCommentHandler(CommentHandler commentHandler) {
        this.commentHandler = commentHandler;
    }

    public HandlePrologue getPrologueHandler() {
        return prologueHandler;
    }

    public void setPrologueHandler(HandlePrologue prologueHandler) {
        this.prologueHandler = prologueHandler;
    }

    public HandleDoctype getDoctypeHandler() {
        return doctypeHandler;
    }

    public void setDoctypeHandler(HandleDoctype doctypeHandler) {
        this.doctypeHandler = doctypeHandler;
    }

    public ValidatorHandler getValidatorHandler() {
        return validatorHandler;
    }

    public void setValidatorHandler(ValidatorHandler validatorHandler) {
        this.validatorHandler = validatorHandler;
    }

    public InstructionTagHandler getInstructionTagHandler() {
        return instructionTagHandler;
    }

    public void setInstructionTagHandler(InstructionTagHandler instructionTagHandler) {
        this.instructionTagHandler = instructionTagHandler;
    }

    public DequeueHandler<TagElement> getDequeueHandler() {
        return dequeueHandler;
    }

    public void setDequeueHandler(DequeueHandler<TagElement> dequeueHandler) {
        this.dequeueHandler = dequeueHandler;
    }

    public TagStackHandler getTagStackHandler() {
        return tagStackHandler;
    }

    public void setTagStackHandler(TagStackHandler tagStackHandler) {
        this.tagStackHandler = tagStackHandler;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Process to parse xml content, by detect and handling errors.
     * @param in the file
     * @throws IOException when reading file
     */
    public void parse(Reader in) throws IOException {
        reader = new BufferedReader(in);
        tagStack = new TagStack();
        logger = Logger.getLogger(this.getClass().getName());
        textValue = new StringBuilder();
        tagName = new StringBuilder();
        textAfterTagName = new StringBuilder();
        attributeName = new StringBuilder();
        attributeValue = new StringBuilder();
        xmlRules = new XMLRules();
        while (reader.ready()) {
            columnIndex++;
            c = (char) reader.read();
            switch (c) {
                // we found open blanket
                case '<' -> {
                    //if it is in attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    // check if the previous blanket has not closed
                    if(isStartTag){
                        handleError(getErrorSource(),"Tag without close blanket", "Add a close blanket",
                                ErrorType.FatalError);
                        break;
                    }
                    // handle the text content between open tag and close tag
                    if(isHandleText && !textValue.isEmpty()){
                        handleText(textValue.toString().toCharArray());
                    }
                    //parse a tag
                    parseTag();
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
                    // if we have found the text after the tag name of close tag
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // if the text contains an open blanket
                    if(isHandleText && c == '>'){
                        handleError(getErrorSource(),"Tag without open blanket", "Add the open blanket",
                                    ErrorType.FatalError);
                        break;
                    }
                    // append the character
                    textValue.append(c);
                }
            }
        }
        // check the empty state
        if(dequeueHandler != null && dequeueHandler.getEmptyState() != 1){
            handleError(getErrorSource(),"XML file don't have a single root tag",
                    "Add single root tag !", ErrorType.Warning);
        }
    }

    /**
     * Parse a xml tag. Tag can be open, single or close tag.
     * @throws IOException when reading tag
     */
    protected void parseTag() throws IOException {
        // change state to open tag state
        isStartTag = true;
        isStartTagName = true;
        isHandleText = false;
        textValue.setLength(0);
        isHandleAttributeValue = false;
        tagName.setLength(0);
        textAfterTagName.setLength(0);
        // start reading the tag
        while(reader.ready()){
            columnIndex++;
            c = (char) reader.read();
            switch (c) {
                // we have found an instruction or a text value
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
                        // tag name validation
                        validateName(textValue.toString());
                        isStartTagName = false;
                        isStartTagAttributeName = true;
                        element = new Element();
                        element.setName(textValue.toString());
                        tagElement = new TagElement();
                        tagElement.setElement(element);
                        attributeKVList = new ArrayList<>();
                        tagName.append(textValue);
                        textValue.setLength(0);
                        break;
                    }
                    //if we found empty tag name
                    if(isStartTag && isStartTagName && textValue.isEmpty()){
                        handleError(getErrorSource(),"Empty tag name", "Add a tag name !"+". ",
                                ErrorType.FatalError);
                        break;
                    }
                    // if the previous tag name text is doctype
                    if(isStartTag && isDoctypeTag && textValue.toString().equals("DOCTYPE")){
                        if(doctype != null){
                            handleError(getErrorSource(), "XML file must contains only on doctype tag",
                                    "Remove another doctype tag", ErrorType.FatalError);
                            break;
                        }
                        isCommentTag = false;
                        textValue.setLength(0);
                        parseDoctype();
                        return;
                    }
                    // if the previous tag name is comment
                    if(isStartTag && isCommentTag && textValue.toString().equals("--")){
                        isDoctypeTag = false;
                        textValue.setLength(0);
                        //check if the comment is on top in file
                        if(prologue == null && doctype == null && dequeueHandler.count() == 0){
                            handleError(getErrorSource(), "Invalid comment on file !",
                                    "XML file must not start with comment", ErrorType.FatalError);
                            return;
                        }
                        parseComment();
                        return;
                    }
                    if(isHandleText){
                        addSpace(textValue);
                    }
                    if(isStartTag && !isStartTagName && !isStartTagAttributeValue
                            && !attributeName.isEmpty() && !isDoctypeTag && !isCommentTag){
                        handleError(getErrorSource(),"Invalid space before attribute value",
                                "Remove space before attribute value", ErrorType.FatalError);
                    }
                }
                // we found equal symbol
                case '=' -> {
                    //if it is in attribute value
                    if(isStartTagAttributeValue){
                        textValue.append(c);
                        break;
                    }
                    if(isCloseTag && !isStartTagName){
                        textAfterTagName.append(c);
                        break;
                    }
                    // empty attribute name
                    if(isStartTag && !isCloseTag && textValue.isEmpty()){
                        handleError(getErrorSource(),"Empty attribute name", "Add attribute name !",
                                ErrorType.FatalError);
                        break;
                    }
                    // get attribute name
                    if(isStartTag && !isCloseTag){
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
                    // change state to attribute value state handling
                    if(isStartTag && !isStartTagAttributeName && !isStartTagAttributeValue && !isDoctypeTag){
                        if(c == '"'){countDoubleQuotes++;}
                        if(c == '\''){countSingleQuotes++;}
                        isStartTagAttributeValue = true;
                        break;
                    }
                    // end of reading attribute value
                    if(isStartTag && !isStartTagAttributeName && isStartTagAttributeValue && !isDoctypeTag){
                        isStartTagAttributeValue = false;
                        if(c == '"'){countDoubleQuotes++;}
                        if(c == '\''){countSingleQuotes++;}
                        attributeValue.append(textValue);
                        isHandleAttributeValue = true;
                    }
                    // add the quote of attribute name
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
                    //we have found comment or doctype
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
                        //invalid content in open or single tag
                        if(!isStartTagAttributeValue && !isDoctypeTag &&
                                !isCommentTag && !isStartTagName && !textValue.isEmpty()){
                            handleError(getErrorSource(),"Invalid content : "+textValue+", in tag : "+tagName,
                                    "Remove the content !", ErrorType.FatalError);
                            break;
                        }
                        // instruction validation
                        if(isProcessInstruction && processingInstructionMark != 2){
                            handleError(getErrorSource(),"Invalid instruction !",
                                    "Instruction must have only two question marks", ErrorType.Warning);
                        }
                        //handle instruction
                        if(isProcessInstruction && processingInstructionMark == 2){
                            if(prologue != null && tagName.toString().equals("xml")){
                                handleError(getErrorSource(),"File must contains only one prologue",
                                        "Remove another prologue", ErrorType.FatalError);
                                break;
                            }
                            createElement(attributeKVList);
                            tagElement.setElement(element);
                            tagElement.setType(Constants.INSTRUCTION_TAG);
                            handleInstructionTag(tagElement);
                        }
                        // close and single tag validation
                        if(countSlash > 1){
                            handleError(getErrorSource(),"Invalid tag, more than two slash !",
                                    "Close or Single tag must have only two slash", ErrorType.FatalError);
                            break;
                        }
                        // for tags without attributes
                        if(attributeKVList == null){
                            element = new Element();
                            element.setName(textValue.toString());
                            tagElement = new TagElement();
                            tagElement.setElement(element);
                            tagName.append(textValue);
                            validateName(textValue.toString());
                        }
                        // if we found text after a tag name of close tag, we handle error
                        if(isCloseTag && !textAfterTagName.isEmpty()){
                            handleError(getErrorSource(),"Invalid text content in close tag !",
                                    "Check the text "+textAfterTagName+" after tag name and remove it.",
                                    ErrorType.Warning);
                        }
                        // parse and save new element, element is not prolog, doctype or comment
                        if(!isDoctypeTag && !isCommentTag && !isProcessInstruction){
                            createElement(attributeKVList);
                            // save tag element
                            tagElement.setType(element.getType());
                            tagElement.setElement(element);
                            if(tagElement.getType() == Constants.OPEN_TAG){
                                handleStartTag(tagElement);
                            }
                            // handle end tag
                            if(tagElement.getType() == Constants.CLOSE_TAG){
                                handleEndTag(tagElement);
                            }
                            // handle empty tag
                            if(tagElement.getType() == Constants.SINGLE_TAG){
                                handleEmptyTag(tagElement);
                            }
                            dequeueHandler.autoProcess(tagElement);
                        }
                    }
                    reset();
                    textAfterTagName.setLength(0);
                    return;
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
                default -> textValue.append(c);
            }
            // handle attribute value
            if(isStartTag && isHandleAttributeValue){
                if(countSingleQuotes == 2 || countDoubleQuotes == 2){
                    attributeValue.setLength(0);
                    attributeValue.append(textValue);
                    if(!xmlRules.firstCharacterRule(attributeName.charAt(0))){
                        handleError(getErrorSource(), "Invalid first character of attribute name : "+attributeName,
                                "Add a valid first character of attribute name !", ErrorType.Warning);
                    }
                    if(!xmlRules.validateNameElement(attributeName.toString())){
                        handleError(getErrorSource(), "Invalid attribute name : "+attributeName,
                                "Add valid attribute name or remove space behind !", ErrorType.Warning);
                    }
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
                    handleError(getErrorSource(),"Invalid quote number of attribute value",
                            "Attribute value must have only two quotes", ErrorType.FatalError);
                }
            }
        }
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
        //clear attributes key/value list
        attributeKVList = null;
        processingInstructionMark = 0;
        countSlash = 0;
        textValue.setLength(0);
    }

    /**
     * Format of error source
     * @return the string format of error source
     */
    protected String getErrorSource(){
        return "Row :"+rowIndex+",Column:"+columnIndex;
    }

    /**
     * Parse comment of xml file
     * @throws IOException
     */
    protected void parseComment() throws IOException {
        // start reading comment
        while(reader.ready()){
            c = (char) reader.read();
            columnIndex++;
            switch (c){
                case '<' -> {
                    if(isCommentTag){
                        textValue.append(c);
                    }
                }
                //comment syntax validation
                case '>' -> {
                    if(!textValue.isEmpty() && !textValue.toString().endsWith("--")){
                        isCommentTag = true;
                        textValue.append(c);
                        break;
                    }
                    if(!textValue.isEmpty() && textValue.toString().endsWith("--")) {
                        // add the character when it is inside a comment text
                        String commentContent = textValue.substring(0, textValue.lastIndexOf("--"));
                        if (commentContent.contains("--")) {
                            handleError(getErrorSource(), "Invalid comment syntax !",
                                    "Comment must not contains --", ErrorType.FatalError);
                            break;
                        }
                        handleComment(commentContent.toCharArray());
                        reset();
                        return;
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
                default -> textValue.append(c);
            }

        }
    }

    /**
     * Parse a doctype tag
     * @throws IOException when reading tag
     */
    protected void parseDoctype() throws IOException {
        //start reading doctype
        while(reader.ready()){
            columnIndex++;
            c = (char) reader.read();
            switch (c){
                case '[' -> {
                    // if we are reading doctype tag
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
                case '>' -> {
                    parseDoctypeContent();
                    textAfterTagName.setLength(0);
                    reset();
                    return;
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
                default -> textValue.append(c);
            }
        }
    }

    /**
     * Handle the text data
     * @param text between open and close tags
     */
    protected void handleText(char[] text){
        try{
            if(textHandler != null){
                textHandler.handleText(text);
            }
        }catch (Exception e){
            handleError(e.getMessage());
        }
    }

    protected void handleTitle(char[] text){}

    /**
     * Handle the comment content
     * @param comment text content
     */
    protected void handleComment(char[] comment) {
        try {
            if(commentHandler != null){
                commentHandler.handleComment(comment);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Handle an empty tag
     * @param tag element
     */
    protected void handleEmptyTag(TagElement tag){
        try {
            if(tagHandler != null){
                tagHandler.handleEmptyTag(tag);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Handle start tag
     * @param tag element
     */
    protected void handleStartTag(TagElement tag){
        try {
            if(tagHandler != null){
                tagHandler.handleStartTag(tag);
            }
        } catch (HJAXException e) {
            handleError(e.getMessage());
        }
    }

    /**
     * Handle instruction
     * @param tag element
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
                prologue = new Prologue(version, encoding, standAlone);
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
     * Handle error message provide by throws
     * @param msg error
     */
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

    /**
     * Handle prologue
     * @param version a version provide from attribute
     * @param encoding an encoding provide from attribute
     * @param isStandalone a standalone provide by attribute
     * @throws HJAXException
     */
    protected void handlePrologue(String version, String encoding, Boolean isStandalone) throws HJAXException{
        try{
            if(prologueHandler != null){
                prologueHandler.handlePrologue(version, encoding, isStandalone);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    /**
     * Handle close tag
     * @param tag element
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
     * Handle entity
     * @param name of entity
     */
    private void handleEntity(String name){
        try {
            String entityValue;
            // The default xml entity
            entityValue = switch (name){
                case "&lt;" -> "<";
                case "&gt;" -> ">";
                case "&amp;" -> "&";
                case "&quot;" -> "\"";
                case "&apos;" -> "'";
                // if it's not a default xml entity, we can get it from dtd or schema
                default -> getExternalEntity(name);
            };
            if(entityHandler != null){
                entityHandler.handleEntity(name);
            }
        }catch (HJAXException e){
            handleError(e.getMessage());
        }
    }

    /**
     * Called when an error occurred into code. <code>type</code> can have two values either it
     * is equal to Fatal error in this case parser stop parse document, or it is equal to Warning
     * in this case parser continue to parse document after error declaration but event if
     * parser declare that error is a warning it's possible to stop parser immediately by
     * @param source error source line in file
     * @param message error message
     * @param debug message to correct the error
     * @param errorType error type
     */
    protected void handleError(String source, String message, String debug, ErrorType errorType){
        try {
            logger.severe("Source : "+source+"; Message : "+message+"; Debug : "+debug);
            if(errorHandler != null){
                errorHandler.errorHandler(source, message, debug, errorType);
            }
        } catch (NullPointerException e) {
            logger.severe("NullPointerException :"+e.getMessage());
        } catch(HJAXException e){
            logger.severe(e.getMessage());
        }finally{
            try {
                // stop the reading of file
                if(errorType == ErrorType.FatalError){
                    reader.close();
                }
            } catch (Exception e) {
                logger.severe("Exception :"+e.getMessage());
            }
        }
    }

    /**
     * Get the entity value in dtd or schema
     * @param entityName name of entity
     * @return entity value
     */
    private String getExternalEntity(String entityName) {
        //todo to be implemented
        return "";
    }

    /**
     * Save temporary attributes to the list
     * @param attributeKVList a list of attributeName/attributeValue
     * @param attributeName of tag attribute
     * @param attributeValue of tag attribute
     */
    private void addAttribute(final List<AttributeKV> attributeKVList, String attributeName, String attributeValue){
        if(attributeKVList.isEmpty()){
            attributeKVList.add(new AttributeKV(attributeName, attributeValue));
            return;
        }
        for(AttributeKV attributeKV : attributeKVList){
            if(attributeKV.getName().equals(attributeName)){
                handleError(getErrorSource(), "Duplicate attribute name : "+attributeName,
                        "Remove duplicate attribute", ErrorType.FatalError);
                break;
            }
        }
        attributeKVList.add(new AttributeKV(attributeName, attributeValue));
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
            handleError(getErrorSource(),"Empty DOCTYPE name directive ", "Add the 'DOCTYPE' directive name", ErrorType.Warning);
        }
        if(!xmlRules.firstCharacterRule(doctypeName.charAt(0))){
            handleError(getErrorSource(),"Invalid DOCTYPE first character !", "Add a valid first character", ErrorType.FatalError);
        }
        if(!xmlRules.validateNameElement(doctypeName)){
            handleError(getErrorSource(),"Invalid DOCTYPE name !", "Add a valid name ", ErrorType.FatalError);
        }
        doctype.setDtdName(doctypeName);
    }

    /**
     * Parse doctype content
     */
    private void parseDoctypeContent() {
        String doctypeContent = textValue.toString();
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
                    handleError(getErrorSource(),"Invalid doctype location type !",
                            "Add the valid doctype location type PUBLIC or SYSTEM", ErrorType.Warning);
                }
                // set doctype file path
                if(doctypeFilePath.startsWith("\"") && doctypeFilePath.endsWith("\"")
                        && doctypeFilePath.endsWith(".dtd\"") && doctypeFilePath.length() > 6){
                    doctype.setDtdFilePath(doctypeFilePath.substring(1, doctypeFilePath.length()-1));
                    doctype.setExternal(true);
                }else{
                    handleError(getErrorSource(),"Invalid doctype file path !", "Add the doctype file path", ErrorType.Warning);
                }
            }else{
                handleError(getErrorSource(),"Invalid doctype tag", "Add the valid directives of doctype tag", ErrorType.Warning);
            }
        }
    }

    /**
     * Create a tag element
     * @param attributeKVList a list of attributes of element
     */
    private void createElement(final List<AttributeKV> attributeKVList) {
        // create new Element
        setElementType();
        // save attributes values for single and open tag
        if((element.getType() == Constants.OPEN_TAG || element.getType() == Constants.SINGLE_TAG)
                && attributeKVList != null && !attributeKVList.isEmpty()){
            // create attributes for open tag only
            // save attributeList on element
            element.setAttributeList(getAttributeList(attributeKVList));
        }
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
     * Set the type of tag element
     */
    private void setElementType() {
        element.setType(Constants.OPEN_TAG);
        if(isCloseTag){
            element.setType(Constants.CLOSE_TAG);
        }
        if(isSingleTag){
            element.setType(Constants.SINGLE_TAG);
        }
    }

    /**
     * Validate name of tag or attribute name
     * @param name a name of tag or attribute name
     */
    private void validateName(String name){
        if(!xmlRules.firstCharacterRule(name.charAt(0))){
            handleError(getErrorSource(), "Invalid first character of tag name : "+name,
                    "Add a valid first character of tag name, " +
                            "it must begin with '_' or '-' or lower or upper case letter.", ErrorType.FatalError);
        }
        if(!xmlRules.validateNameElement(name)){
            handleError(getErrorSource(), "Invalid tag name : "+name,
                    "Add valid character(s) of tag name", ErrorType.FatalError);
        }
        if(!isProcessInstruction && name.equals("xml")){
            handleError(getErrorSource(),"Invalid tag name :"+name, "Tag name must not be 'xml'",
                    ErrorType.FatalError);
        }
    }
}

/**
 * An intern class for rules validation
 * @author Hyacinthe TSAGUE
 */
class XMLRules{
    /**
     * First character rule validation
     * @param c the first character
     * @return <code>true</code> if it's valid, otherwise <code>false</code>
     */
    public boolean firstCharacterRule(char c) {
        return switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', ':' -> true;
            default -> false;
        };
    }
    /**
     * Character rule validation. Used to validate characters that is not the first character
     * @param c the current character
     * @return <code>true</code> if it's valid, otherwise <code>false</code>
     */
    public boolean charRuleOnName(char c) {
        return switch (c) {
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', ':', '-', '.' -> true;
            default -> false;
        };
    }

    /**
     * Validate DTD element type
     * @param elementType DTD element type
     * @return true if it's matches, false else
     */
    public boolean validElementType(String elementType){
        return switch (elementType){
            case "#CDATA", "#PCDATA", "ANY", "EMPTY" -> true;
            default -> false;
        };
    }

    /**
     * Validate characters after the first character of tag name or attribute name
     * @param name a name of tag or attribute name
     * @return true if it's valid, false else
     */
    public boolean validateNameElement(String name){
        // Get the letters array of tag name or attribute name
        char[] letters = name.toCharArray();
        char c;
        boolean flag;
        for(int i = 1; i < letters.length; i++){
            c = letters[i];
            flag = charRuleOnName(c);
            // from ascii utf-8 table, invalid characters are between those intervals
            if(!flag){
                return false;
            }
        }
        return true;
    }
}