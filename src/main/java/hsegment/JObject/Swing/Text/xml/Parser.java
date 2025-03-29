package hsegment.JObject.Swing.Text.xml;


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

/**This class read the xml file character by character, detect the doctype and prolog on file, detect the root tag,
 * and construct the dom of nodes that we use to execute xpath requests
 * @author Hyacinthe TSAGUE
 *
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

    public hsegment.JObject.Swing.Text.xml.handler.TextHandler getTextHandler() {
        return textHandler;
    }

    public void setTextHandler(TextHandler textHandler) {
        this.textHandler = textHandler;
    }

    public hsegment.JObject.Swing.Text.xml.handler.TagHandler getTagHandler() {
        return tagHandler;
    }

    public void setTagHandler(hsegment.JObject.Swing.Text.xml.handler.TagHandler tagHandler) {
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

    /**
     * Read the xml file, construct the dom
     * @param in
     * @return
     * @throws IOException
     */
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
        StringBuilder textContent = new StringBuilder();
        int countSingleQuotes = 0;
        int countDoubleQuotes = 0;
        int countSlash = 0;
        int processingInstructionMark = 0;
        while (reader.ready()) {
            columnIndex++;
            c = (char) reader.read();
            textContent.append(c);
            switch (c) {
                // we found open blanket
                case '<' -> {
                    // if the blanket is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // check if the previous blanket has not closed
                    if(isStartTag){
                        //@todo handle fatal error of lack of close blanket
                        System.out.println("Fatal error : Tag without close blanket");
                    }
                    // handle text content between open and close tag
                    if(!isStartTag && isHandleText){
                        //@todo handle text content tag
                        handleText(textValue.toString());
                        System.out.println("Text between tag:"+textValue);
                    }
                    // change state to open tag state
                    isStartTag = true;
                    isStartTagName = true;
                    isHandleText = false;
                    textValue.setLength(0);
                    isHandleAttributeValue = false;
                    tagName.setLength(0);
                }
                // we found instruction
                case '?' -> {
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
                    // add tagName
                    if(isStartTag && isStartTagName && !isHandleAttributeValue
                            && !textValue.isEmpty() && !isDoctypeTag && !isCommentTag){
                        tagName.append(textValue);
                        textValue.setLength(0);
                        isStartTagName = false;
                    }
                    // if we have empty tag name
                    if(isStartTag && !isDoctypeTag && !isCommentTag && tagName.isEmpty()){
                        //@todo handle error of empty tag name
                        System.out.println("Error : Empty Tag Name !");
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
                    // if the equal sign is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // get attribute name
                    if(isStartTag){
                        // change state to attribute name state handling
                        isStartTagAttributeName = true;
                        attributeName.append(textValue);
                        textValue.setLength(0);
                    }
                }
                // we found quote
                case '"' -> {
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
                    // if the slash is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    // if it inside a tag: maybe a close or single tag
                    if(isStartTag){
                        // for close tag
                        if(tagName.isEmpty()){
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
                    // if the symbol is in comment
                    if(isCommentTag){
                        textValue.append(c);
                        break;
                    }
                    if(isStartTag) {
                        isStartTagName = true;
                        isDoctypeTag = true;
                        isCommentTag = true;
                    }
                }
                // we found close blanket
                case '>' -> {
                    if(isStartTag){
                        // add the character when it is inside a comment text
                        if(isCommentTag && !textValue.toString().contains("--")){
                            textValue.append(c);
                            break;
                        }
                        // instruction validation
                        if(isProcessInstruction && processingInstructionMark != 2){
                            //@todo handle process instruction error
                            System.out.println("Error : ProcessingInstruction");
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
                            //@todo handle multiple slash on tag
                            System.out.println("Error : Multiple Slash");
                        }
                        // for tags without attributes
                        if(attributeKVList.isEmpty()){
                            tagName.append(textValue);
                        }
                        // doctype validation
                        if(isDoctypeTag){
                            parseDoctype(textValue.toString());
                            System.out.println("DTD name : "+doctypeHandler.getDTDName());
                            System.out.println("DTD type location : "+doctypeHandler.getLocationType());
                            System.out.println("DTD file path : "+doctypeHandler.dtdFilePath());
                            System.out.println("Doctype content :"+textValue);
                        }
                        //comment validation
                        if(isCommentTag && textValue.toString().contains("--")){
                            parseComment(textValue.toString());
                        }
                        // parse and save new element, element is not prolog, doctype or comment
                        if(!isProcessInstruction && !isDoctypeTag && !isCommentTag){
                            element = parseElement(tagName.toString(), attributeKVList);
                            // save tag element
                            //@todo change tag element type her
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
                        textContent.setLength(0);
                        processingInstructionMark = 0;
                        countSlash = 0;
                        textValue.setLength(0);

                    }else{
                        //todo handle fatal error of lack of open blanket
                        System.out.println("Fatal error : Tag without open blanket");
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
                    columnIndex += 8;
                }
                // we found carriage return
                case '\r' -> {
                    columnIndex = 0;
                }

                default -> {
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
                }else{
                    // @todo handle attribute value quote error
                    System.out.println("Error : Quote");
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
        if(textComment.endsWith("--")){
            // get and handle comment content
            String commentContent = textComment.substring(0,textComment.length()-2);
            //todo handle comment
            handleComment(commentContent);
            System.out.println("Comment :"+commentContent);
        }else{
            //todo error syntax
            System.out.println("Error : Comment syntax error");
        }
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
            //todo error empty doctype name
            System.out.println("Error : Invalid doctype name");
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
                    //todo error doctype location type
                    System.out.println("Error : Invalid doctype location type !");
                }
                // set doctype file path
                if(doctypeFilePath.startsWith("\"") && doctypeFilePath.endsWith("\"")
                        && doctypeFilePath.contains(".dtd") && doctypeFilePath.length() > 5){
                    doctypeHandler.setDtdFilePath(doctypeFilePath.substring(1, doctypeFilePath.length()-1));
                    doctypeHandler.setExternal(true);
                    //todo get the doctype from path and call parse dtd
                }else{
                    //todo error doctype filepath
                    System.out.println("Error : Invalid doctype file path!");
                }
            }else{
                //todo error invalid doctype tag
                System.out.println("Error : Invalid doctype tag !");
            }
        }
    }

    private Element parseElement(String tagName, final List<AttributeKV> attributeKVList) {
        // create new Element
        Element element = new Element(rowIndex, columnIndex, tagName);
        setElementType(element);
        System.out.println("----------------------------");
        System.out.println("Row :"+rowIndex);
        System.out.println("Column :"+columnIndex);
        attributeKVList.forEach(attributeKV1 -> {
            System.out.println("name :"+attributeKV1.getName());
            System.out.println("value :"+attributeKV1.getValue());
        });

        // save values
        if(!attributeKVList.isEmpty()){
            // create attributes for open tag only
            AttributeList attributeList = getAttributeList(attributeKVList);
            // save attributeList on element
            element.setAttributeList(attributeList);
        }
        return element;
    }

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


    protected void handleComment(String comment) {
        try {
            commentHandler.handleComment(comment);
        } catch (HJAXException e) {
        }
    }


    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        try {
            tagHandler.handleEmptyTag(tag);
        } catch (HJAXException e) {

        }

    }

    protected void handleStartTag(TagElement tag) {
        tagStack.stack(tag);
        try {
            tagHandler.handleStartTag(tag);
        } catch (HJAXException e) {
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

        }
    }

}
