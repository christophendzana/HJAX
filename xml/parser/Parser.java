 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;


import hsegment.JObject.Swing.Text.CommentHandler;
import hsegment.JObject.Swing.Text.EntityHandler;
import hsegment.JObject.Swing.Text.ErrorHandler;
import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.InstructionTagHandler;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.PrologHandler;
import hsegment.JObject.Swing.Text.TagHandler;
import hsegment.JObject.Swing.Text.TextHandler;
import hsegment.JObject.Swing.Text.ValidatorHandler;
import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.util.Dictionnary;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.System.Logger;
import java.util.Vector;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;

/**
 *<p> A simple DTD and SCHEMA driven XML parser. this parser reads 
 * a XML file from an InputStream and call various methods.
 * <p>
 * <em>it is a validated parser</em> means that during parsing 
 * it check if file is well formated and if respect DTD or schema 
 * structure if not it call error or warning methods.
 * <p> 
 * <em>it is a event parser </em> means that during parsing it call 
 * some methods to handling different event like tag start and end, 
 * comment tag and so one. all that callback method are Interface 
 * that can be implemented.
 * <p> 
 * During parser parse doctype declaration, some conflict's error will appear.
 * <ul><li> the first one is if dtd is provided to parser's constructor and his name 
 * doesn't matches with name declared to doctype</li>
 *  <li> the second one is if dtd is provided to parser's constructor and a new dtd 
 * path is given into doctype declaration</li></ul>
 * 
 * 
 * @author Ndzana Christophe
 */
public class Parser implements DTDConstants{
    
    protected char[] text;
    protected char[] stream;
    protected char[] str;
    
    private Element recent;
    private SimpleAttributeSet attributes;
    private int currentPosition = 0;
    protected Reader in;
    protected int ch = -1;
    private TagStack tagStack;
    //track index off the next character into buffer
    private int strPos = -1;
    
    private int textPos = -1;
    private int ln;
    //
    private int step = -1;
    protected XMLRulesImpl xmlRules;
    private EntityHandler entHandler;
    private ErrorHandler errHandler;
    private TagHandler tagHandler;
    private TextHandler texHandler;
    private CommentHandler comHandler;
    private HandlePrologue proHandler;
    private ValidatorHandler doctHandler;
    private InstructionTagHandler instHandler;
    public Parser(){
        
        
        str = new char[128];
        text = new char[10];
        stream = new char[1024];
        tagStack = new TagStack();
        xmlRules = new XMLRulesImpl();
    }
    
    public void setEntityHandler(EntityHandler entHandler){
        this.entHandler = entHandler;
    }
    
    public void setErrorHandler(ErrorHandler errHandler){
        this.errHandler = errHandler;
    }
    
    public void setTagHandler(TagHandler tagHandler){
        this.tagHandler = tagHandler;
    }
    
    public void setTextHandler(TextHandler textHandler){
        this.texHandler = textHandler;
    }

    public void setComHandler(CommentHandler comHandler) {
        this.comHandler = comHandler;
    }
    
    public void setValidatorHandler(ValidatorHandler doctHandler){
        this.doctHandler = doctHandler;
    }

    public CommentHandler getComHandler() {
        return comHandler;
    }

    public void setProHandler(HandlePrologue proHandler) {
        this.proHandler = proHandler;
    }
    
    public void setInstructionTagHandler(InstructionTagHandler inTagHandler){
        this.instHandler = inTagHandler;
    }

    public PrologHandler getProHandler() {
        return proHandler;
    }
    
    public EntityHandler getEntHandler() {
        return entHandler;
    }

    public ErrorHandler getErrHandler() {
        return errHandler;
    }

    public TagHandler getTagHandler() {
        return tagHandler;
    }

    public TextHandler getTexHandler() {
        return texHandler;
    }
    public ValidatorHandler getValidatorHandler(){
        return doctHandler;
    }
    
    /**
     * Return this parser DTD. if null parser was given the default one was provided by 
     * the parser.
     * @return provider or generated parser.
     */
    public DTD getDTD(){
        return null;
    }
    
    protected TagElement makeTag(Element element){
        if(element == null){
            throw new NullPointerException("tag cannot be null");
        }
        
        return new TagElement(element);
    }
    protected synchronized void handleText(char[] text) {
        if(new String(text).trim().isEmpty())
            return;
        this.texHandler.handleText(text);
    }
    
    protected void handleTitle(char[] text) {
        // default behavior is to call handleText. Subclasses
        // can override if necessary.
        handleText(text);
    }
    
    
    protected void handleComment(char[] text) {
        try {
            comHandler.handleComment(text);
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
        Element element = null;
        step++;
        /**if(element.getName().equalsIgnoreCase("xml") && step != 0){
            error("XML.Error", "Misplaced XML instruction");
        } else if(!element.getName().equalsIgnoreCase("xml") && (step != 0 || step != 3)){
            error("XML.Error", "Instruction tag is Misplaced");
        }**/
        try {
            instHandler.handleIntruction(tag);
        } catch (Exception e) {
        }
    }
    
    protected void handleEndTag(TagElement tag) {
        
        try {
            tagHandler.handleEndTag(tag);
        } catch (HJAXException e) {
            
        }
        
        if(tagStack.pullOut(tag)){
            return;
        } else {
            error("XML.error","Misplaced element "+tag.getElement().getName());
        }
    }
    
    /**
     * Called when an error occured into code. <code>type</code> can have two value either it 
     * is egal to Fatal error in this case parser stop parse document or it is egal to Warning 
     * in this case parser continue to parse document after error declaration but event if 
     * parser declare that error is a warning it's possible to stop parser immidiatly by 
     * throwing HJAXException
     * 
     * 
     * @param src error source
     * @param msg message error
     * @param debug how to debug error
     * @param type error type
     * @throws HJAXException if parser should stop parse
     */
    protected void handleError(String src, String msg, String debug, ErrorType type) throws HJAXException{
        try {
            
            errHandler.errorHandler(src, msg, debug, type);
            
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch(HJAXException e){
            type = ErrorType.FatalError;
            throw new HJAXException(e.getMessage());
            
        }finally{
            try {
                if(type == ErrorType.FatalError){
                    in.close(); 
                    stream = null;
                    currentPosition = 0;
                }
            } catch (Exception e) {
            }
        }
    }
    
    
    public synchronized void parse(Reader in){
        this.in = in;
        
        try {
            while ((ch = readCh()) != -1){
                
            switch(ch){
                case '<' :
                    //mark();
                    parseTag();
                    
                    /*char[] buff = new char[getCurrentPos() - marker];
                    resetStreamCursor(); read(buff);
                    char[] newText = new char[strPos + buff.length + 1];
                    System.arraycopy(str, 0, newText, 0, strPos + 1);
                    System.arraycopy(buff, 0, newText, strPos + 1, buff.length);
                    str = newText; handleText(str); 
                    **/
                    break;
                default : 
                    //if(tagStack.count() > 0){
                        addString(ch);
                    //} else {
                        //addText(ch);
                    //}
                    
            }
            }
        } catch (Exception e) {
            
        }
    }
    
    /**
     * Return a string with <code>length</code> number of caracter. the first fetched 
     * character is the one where pointer is,
     * @param length number of caracter to return
     * @return text with <code>length</code> number of carater or less if <code>length</code> 
     * is greater than parsed text capacity
     */
    private String readString(int length){
        if(length < 0){
            throw new IllegalArgumentException("Bad length parameter");
        }
        String text = "";
        //we should not have to fetch more than we have text if length is greater than 
        //text capacity.
        int minLength = Math.min(length, text.length() - currentPosition);
        int c;
        try {
            for(int i = 0; i < minLength; i++){
                c = readCh();
                text.concat(""+(char)c);
            }
        } catch (Exception e) {
            
        }
        
        
        return text;
    }
    
    /**
     * Copy and return characters. start copy to <code>startIndex</code> index and 
     * copy <code>length</code> number of character. Note that IllegalArgument exception 
     * is thrown if non parsed character should be copy.
     * 
     * @param startIndex started copying index
     * @param length number of character to be read and copy
     * @return and array which contained copied caracters
     * @throws IllegalArgumentException if attemps to copy non parsed characters
     */
    private char[] getChars(int startIndex, int length){
        if(startIndex >= getCurrentPos() || (startIndex + length) > getCurrentPos()){
            throw new IllegalArgumentException("cannot read non parsed characters");
        }
        char[] chars = new char[length];
        System.arraycopy(text, startIndex, chars, 0, length);
        return chars;
    }
    
    
    private void parseTag() throws HJAXException{
        TagElement tag;
        Element element = null;
        AttributeList attList = null;
        boolean isClosableTag = false;
        int isInstructionTag = 0;
        int mark = getCurrentPos();
        int delimiterType = - 1;
        boolean isSpaceRead = false;
        StringBuffer buffer = new StringBuffer();
        try {
            
            while(true){
                ch = readCh();
                switch(ch){
                    case '!': //this is the comment or Doctype charater starter
                        boolean loop = true;
                        
                        while(loop){
                            ch = readCh();
                            switch(ch){
                                case '-' :
                                    buffer.append("-");
                                    if(buffer.toString().equals("--"))
                                        loop = false;
                                    break;
                                case ' ' :
                                    if(!buffer.isEmpty())
                                        loop = false;
                                     break;   
                                case -1 :
                                    error("XML.Error", "Bad EOF syntax");
                                    break;
                                default :
                                    buffer.append((char)ch);
                                    if(buffer.length() == 7)
                                        loop = false;
                            }
                        }
                        
                        if(buffer.toString().equals("--")){
                            parseComment();
                            return;
                        } else if(buffer.toString().equalsIgnoreCase("DOCTYPE")){
                            parseDoctype();
                            return;
                        } else 
                            error("Error.XML", "Bad tag syntaxe");
                        
                        break;
                    case '?' :
                        if(isClosableTag)
                            error("XML.Error", "Bad Tag Syntaxe combinaition", "Delete '/' or '?' caracter");
                        //this case is when ? is read for the first time
                        if((buffer.isEmpty() && element == null && isInstructionTag == 0) || 
                                //this one is when is read for the second time
                                (element != null && buffer.isEmpty() && isInstructionTag == 1))
                            isInstructionTag += 1;
                        else 
                            error("XML.Error", "Bad instruction tag syntax", "Delete '?' caracter");
                        break;
                    case ' ': //there are two possibility when space is read
                        isSpaceRead = true;
                        
                        // first one is when element's name is read. to this case buffer may not be null but element may be
                        if(!buffer.isEmpty() && element == null){
                            element = getElement(buffer.toString());
                            buffer.delete(0, buffer.length());
                            //second case is when attribut value is read
                        } else if(element != null && !buffer.isEmpty() 
                                    && attList != null && attList.getValue() != null){
                            growAttributesValues(attList, buffer.toString());
                            buffer.delete(0, buffer.length()); 
                        } else if(buffer.isEmpty() && element ==null){
                            error("XML.Error", "Extra space is forbiden", "Delete start space", "w");
                        }
                        break;
                    case '=': 
                        if(!buffer.isEmpty() && element != null){
                            xmlRules.validateAttrNameSyntax(buffer.toString());
                            
                            try {
                                attList = element.atts;
                                while(true)
                                    if(attList.next == null){
                                        
                                        attList = attList.next = new AttributeList(buffer.toString());
                                        break;
                                    } else {
                                        attList = attList.next;
                                    }
                            } catch (NullPointerException e) {
                                attList = element.atts = new AttributeList(buffer.toString());
                            }
                            delimiterType = -1;
                        } else {
                            error("Error.XML", "bad Tag synstaxe");
                        }
                        buffer.delete(0, buffer.length());
                        
                        break;
                    case '\'':
                    case '"' : 
                        delimiterType = delimiterType == -1? ch: delimiterType;
                        
                        if(ch != delimiterType){
                          buffer.append((char)ch);
                          isSpaceRead = false;
                          break;
                        }
                        
                        
                        if(!buffer.isEmpty() && attList != null && attList.getValue() != null){
                            growAttributesValues(attList, buffer.toString());
                            
                            buffer.delete(0, buffer.length()); attList = null; delimiterType = -1;
                        } else if(buffer.isEmpty() && attList != null && attList.getValue() == null){
                            attList.setValue(new String());
                        } else {
                            error("XML.Error", "Attibute init Error");
                        }
                        break;
                    case '>' :
                        //this case is when this tag has no attribut ex: <tag>
                        if(!buffer.isEmpty() && element == null){
                            element = getElement(buffer.toString());
                            buffer.delete(0, buffer.length());
                        } 
                        //element is not null when name has been read already and an attribute is null when 
                        if(element != null && attList == null && buffer.isEmpty()){
                            tag = makeTag(element);
                            if(strPos > 0)
                                handleText(str);
                            
                            if(isInstructionTag == 2)
                                handleInstructionTag(tag);
                            else if(isClosableTag)
                                handleEndTag(tag);
                            else if(!isClosableTag)
                                handleStartTag(tag);
                        } else {
                            error("Error.XML", "Attribute of tag = "+element, "Initialise attribute", "w");
                        }
                        buffer = null;
                        return;
                        
                    case '/' : 
                        if(isClosableTag)
                            error("XML.Error", "Bad closable tag syntax", "Delete '/' caracter");
                        if(isInstructionTag != 0)
                            error("XML.Error", "Bad tag combinaition", "Delete '/' or '?' caracter");
                        isClosableTag = true;
                        break;
                    case '\r' : //carrier return case
                        break;
                    case '\n'://line jump case
                        break;
                    case '\t' ://tabulation case
                        break;
                    case -1:
                        for(int i = mark; i < getCurrentPos(); i++){
                           this.addString(stream[i]);
                        }
                        
                        handleText(str);
                        resetBuffer();
                        buffer = null;
                        return;
                    default :
                        if(isSpaceRead && !buffer.isEmpty())
                            error("Error.XML", "Extrat text added", "Delete text '"+buffer+"'");
                        isSpaceRead = false;
                        buffer.append((char)ch);
                        //addString(ch);
                }
            }
        } catch (HJAXException e) {
            throw new HJAXException(e.getMessage());
        }catch(Exception e){
            
        }
    }
    
    private void parsePrologue(){
        
    }
    
    private void parseInstructionTag(){
        
    }
    
    private void growAttributesValues(AttributeList attList, String value){
        
        if(attList.value.isEmpty()){
            attList.value = value;
        } else {
            Vector values = new Vector();
            try {
                for(int i = 0; i< attList.values.size(); i++){
                    values.add(attList.values.get(i));
                }
            } catch (Exception e) {
                
            }
            
            values.add(value);
            attList.values = values;
        }
    }
    private void parseDoctype(){
        String dtdName = null;
        String locationType = null;
        String dtdFilePath = null;
        String publicIdentifier = null;
        StringBuffer buffer = new StringBuffer();
        int delimiter = 0;
        step++;
        try {
            while((ch = readCh()) != - 1){
                
                switch(ch){
                    case ' ' :
                        if(!buffer.isEmpty() && dtdName == null){
                            dtdName = buffer.toString();
                            buffer.delete(0, buffer.length());
                        } else if(!buffer.isEmpty()){
                            
                            switch(buffer.toString().toUpperCase()){
                                case "SYSTEM":
                                case "PUBLIC":
                                    locationType = buffer.toString().toUpperCase();
                                    buffer.delete(0, buffer.length());
                                    break;
                                default :
                                    error("XML.Error", "wrong DTD identifier", "Replace with PUBLIC or SYSTEM");
                            }
                        }
                        break;
                    case '"' :
                        //avoid case excessive delimiter is encountered
                        if(delimiter > 1)
                            error("XML.Error", "bad DTD declaration", "remove delimiter");
                        if(delimiter == 0 && buffer.isEmpty())
                            break;
                        delimiter++;
                        
                        if(!buffer.isEmpty() && dtdName != null && locationType != null && 
                                publicIdentifier == null && dtdFilePath == null){
                            
                            switch(locationType){
                                case "SYSTEM":
                                    dtdFilePath = buffer.toString();
                                    buffer.delete(0, buffer.length());
                                    break;
                                case "PUBLIC":
                                    publicIdentifier = buffer.toString();
                                    buffer.delete(0, buffer.length());
                                    break;
                                default :
                                    error("XML.Error", "wrong dtd identifier declaration", 
                                            "set DTD file path or public identifier");
                            }
                        }else if(!buffer.isEmpty() && dtdName != null && locationType != null 
                                                   && publicIdentifier != null && dtdFilePath == null){
                            locationType = buffer.toString();
                            buffer.delete(0, buffer.length());
                        } else{
                            error("XML.Error", "Bad DTD declaration");
                        }
                            
                        break;
                    case '[' : 
                        DTDParser dtdParser = null;
                        //dtd.name = dtdName;
                        if(dtdName != null && locationType == null && dtdFilePath == null && publicIdentifier == null){
                            
                            //dtdParser = new DTDParser(dtd);
                            while((ch = readCh()) != -1){
                                if(ch == ']')
                                    break; 
                                
                                buffer.append((char)ch);
                            }
                            
                            if(ch == -1)
                                error("End Of File");
                            
                            dtdFilePath = buffer.toString();
                        } else{
                            error("Error.XML", "Bad DOCTYPE declaration");
                        }
                        buffer.delete(0, buffer.length());
                        break;
                    case '>' :
                        if(dtdName != null && dtdFilePath != null){
                            
                            this.doctHandler.handleValidator(dtdName, locationType, dtdFilePath, publicIdentifier);
                        
                        } else {
                            error("XML.Error", "ad DOCTYPE declaration");
                        }
                        return;
                    default : 
                        buffer.append((char)ch);
                }
            }
        } catch (Exception e) {
        }
        
    }
    
    /**
     * Because an xml's element have to respect a syntaxe like : <ul>
     * <li> An element's name start necessary by an alphabet </li>
     * <li> an element's name don't have to be separated by space</li>
     * <li>An element's name not to be empty</li>
     * </ul> so this method verify that <code>elementName</code> respect 
     * xml spelling before return an element witch name is parameter 
     * value.
     * 
     * 
     * @param elementName name of the element returned
     * @return well formated element
     * @throws : IllegalArgumentException if <code>elementName</code> is null 
     *           or empty
     */
    private Element getElement(String elementName){
        if(elementName == null || elementName.trim().isEmpty())
            throw new IllegalArgumentException("null Element Name");
        xmlRules.validateNameSyntax(elementName);
        return new Element(elementName);//dtd.getElement(elementName);
    }
    
    
    /**
     * Add character into parser buffer
     * @param c character to add
     */
    protected void addString(int c){
        if(++strPos >= str.length){
            char[] newStr = new char[str.length + 50];
            System.arraycopy(str, 0, newStr, 0, str.length);
            str = newStr;
        }
        str[strPos] = (char)c;
        
    }
    /**
     * Add charaters into buffer
     * @param c 
     */
    protected void addString(int[] c){
        for(int i = 0; i < c.length; i++)
            addString(c[i]);
    }
    
    protected void addString(char[] c){
        for(int i = 0; i <c.length; i++){
            addString(c[i]);
        }
    }
    
    /**
     * return accumulated caracter. index should start to 0
     * 
     * 
     * @param index index where fetching character chould start
     * @return string with accumulated character starting to <code>
     * index</code>
     * @exception IllegualArgumentException
     */
    protected String getString(int index){
        char[] newChar = new char[(strPos + 1) - index];
        System.arraycopy(str, index, newChar, 0, newChar.length);
        return new String(newChar);
    }
    /**
     * Reset buffer which is used while tag is parsing
     */
    protected void resetBuffer(){
        str = new char[50];
        strPos = 0;
    }
    
    
    /**
     * Method called when pattern {@literal '<--'} is encountred
     */
    protected void parseComment(){
        StringBuffer pattern = new StringBuffer();
        int patternPos = 0;
        StringBuffer buffer = new StringBuffer();
        try {
              while(true){
                  
                  ch = readCh();
                  switch(ch){
                      case '-':
                          if(pattern.length() == 2){
                              buffer.append((char)ch);
                              break;
                          }
                          pattern.append((char)ch);
                          break;
                      case '!':
                          if(pattern.length() == 2){
                              pattern.append((char)ch);
                              break;
                          } 
                          buffer.append((char)ch);
                      case '>' :
                          if(pattern.length() == 3){
                              handleText(buffer.toString().toCharArray());
                              pattern = buffer = null;
                              return;
                          } else {
                              error("XML.Error", "bad end comment syntax", "add pattern --! before >");
                          }
                      case '\t':
                      case ' ':
                          if(!(pattern.length() == 3)){
                              buffer.append(pattern.toString());
                              pattern.delete(0, pattern.length());
                              buffer.append((char)ch);
                          }
                              
                          break;
                      case '\r':
                      case '\n' :
                          break;
                      case -1:
                              error("End of File");
                              break;
                          default :
                              buffer.append(pattern.toString());
                              pattern.delete(0, pattern.length());
                                  
                              buffer.append((char)ch);
                              
                      }
                  
              } 
            } catch (Exception e) {
            }
        
        
    }
    
    protected void error(String src){
        error(src, null);
    }
    
    
    protected void error(String src, String errorMessage){
        error(src, errorMessage, null);
    }
    
    
    protected void error(String src, String errorMessage, String debug){
        error(src, errorMessage, debug, "F");
    }
    
    
    protected void error(String scr, String errorMessage, String debug, String errorType) throws HJAXException{
        
        if(errorType == null)
            throw new NullPointerException("errorType cannot be null");
        ErrorType type = !errorType.equalsIgnoreCase("w")? ErrorType.FatalError : ErrorType.Warning;
        
        try {
            
            handleError(scr, errorMessage, debug, type);
        } catch (HJAXException e) {
            throw new HJAXException(e.getMessage());
        }
        if(type == ErrorType.FatalError)
            throw new HJAXException(errorMessage);
    }
    
    
    private char[] buff = new char[1];
    private int pos;
    private int len;
    private int marker = -1;
    
    /**
     * Read and return character or -1 if end of a stream is reached
     * @return
     * @throws IOException 
     */
    protected int readCh() throws IOException{
         if (pos >= len) {

            // This loop allows us to ignore interrupts if the flag
            // says so
            for (;;) {
                try {
                    len = in.read(buff);
                    break;
                } catch (InterruptedIOException ex) {
                    throw ex;
                }
            }

            if (len <= 0) {
                
                return -1;      // eof
            }
            pos = 0;
        }
         growStreamIfNecessary();

        return stream[currentPosition++] = buff[pos++];
        
    }
    
    private void growStreamIfNecessary(){
        if(currentPosition > stream.length){
            char[] newStream = new char[stream.length + 1024];
            System.arraycopy(stream, 0, newStream, 0, stream.length);
            stream = newStream;
        }
    }
    
    /**
     * Mark specific position index into stream to when you call <code>reset</code> method, 
     * cursor should be egal to that specific position and the stream reading should start 
     * at that specific marked index.
     * @see #resetStream()  
     */
    protected void mark(){
        marker = getCurrentPos() - 1;
    }
    
    /**
     * Reset stream cursor to Marked Index.
     */
    protected void resetStreamCursor(){
        currentPosition = marker;
    }
    
    /**
     * Read caracter into buffer <code>buff</code> and return the number of 
     * character which have been read.
     * 
     * 
     * @param buff buffer into which characters must be read;
     * @return number of character which have been read
     * @throws IOException
     */
    protected int read(char[] buff) throws IOException{
        
        if(buff.length == 0)
            return -1;
        
        
        int readCount = getCurrentPos();
        for(int i = 0; i< buff.length; i++){
            try {
                buff[i] = (char)stream[currentPosition++];
            } catch (Exception e) {
                
            }
         
        }
        
        return getCurrentPos() - readCount;
    }
    
    protected void read(char[] buff, int offSet){
        
        if(buff.length == 0)
            return;
        for(int i = 0; i < buff.length; i++){
            buff[i] = (char)stream[Math.min(offSet + i, getCurrentPos())];
        }
    }
    
    
    /**
     * return index where reader's cursor is on. Note that, that current index is not yet 
     * read so only character on index 0 up to {@code getCurrentPos - 1} have already been read.
     * @return cursor's index
     */
    protected int getCurrentPos(){
        return currentPosition;
    }
    
    
    public class XMLRulesImpl {
        Dictionnary<String> attName;
        XMLRulesImpl(){
            attName = new Dictionnary<String>();
            attName.setCaseSensitiveTo(true);
        }
        /**
         * 
         * @param str 
         */
        public  void validateNameSyntax(String str){
            
            switch(str.charAt(0)){
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
                case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
                case 's': case 't': case 'u': case 'v': case 'w': case 'x':
                case 'y': case 'z':
                    break;
            
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
                case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
                case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
                case 'Y': case 'Z': case'_':
                    break;
                default:
                    error("XML.Error", "Syntaxe name error");
            
            }
        
        }
        
        private void validateAttrNameSyntax(String str){
            validateNameSyntax(str);
            if(!attName.add(str))
                error("XML.Error", "Two attribut have same name", "Change or delete one of them");
        }
        private void validatePublicDTDURL(String url){
            
        }
        
    }
    
}
