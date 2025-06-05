 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml.parser;

import hsegment.JObject.Swing.Text.xml.Parser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Vector;
import javax.swing.text.html.parser.Entity;

/**
 *this class parse a DTD file and make DTD ready to validate XML file associated 
 * with. this class is subclass of {@link Parser} and the given to constructor 
 * is filled by the DTDParser.
 * @author Ndzana christophe
 */
public class DTDParser extends XMLParser{
    
    
    private HDTD dtd;
    public DTDParser(String name) {
        super();
        dtd = new HDTD(name);
    }
    
    /**
     * Return this parser DTD. if null parser was given the default one was provided by 
     * the parser.
     * @return provider or generated parser.
     */
    public HDTD getDTDDocument(){
        return dtd;
    }
    
    
    public synchronized void parse(Reader in){
        
        try {
        int tag = 0;
        Entity entity = null;//if parameter entity englobe an attribut it should be store
        this.in = in;
        StringBuffer buffer = null;
            while((ch = readCh()) != -1){
                switch(ch){
                    case '<':
                        ch = readCh();
                        
                        if(ch != '!')
                            error("Error.DTD", "Expected: ! read: "+(char)ch);
                        buffer = new StringBuffer();
                        break;
                    case '[' ://this character is read if only Entity had been already verified
                        if(tag != '%')
                            error("Error.DTD", "Missplaced character [");
                        tag = ch;
                        //skipSpace();
                        resetBuffer();
                        break;
                    case ' ' :
                        if(!buffer.isEmpty())
                            switch(buffer.toString().toUpperCase()){
                                case "ELEMENT" :
                                    buffer.delete(0, buffer.length());
                                    parseElement();
                                    break;
                                case "ATTLIST" : 
                                    resetBuffer();
                                    boolean b = true;
                                    String param = null;
                                    if(entity != null){
                                        param = new String(entity.data);
                                        b = param.equalsIgnoreCase("INCLUDE");
                                    }
                                    parseAttributeList(b);
                                    break;
                                case "Entity" :
                                    if(entity != null)
                                        error("Error.DTD", "Syntax error");
                                    resetBuffer();
                                    parseEntity();
                                    break;
                                case "NOTATION" :
                                    if(entity != null)
                                        error("Error.DTD", "Syntax error");
                                    parseNotation();
                                    break;
                                default :
                                    error("Error.DTD", "");
                            }
                        break;
                    case ']' : 
                        if(tag != '[' || tag != ']')
                            error("Error.DTD", "missplaced character : "+(char)ch);
                        //skipSpace();
                        if((ch = in.read()) != ']')
                            error("Error.DTD", "Misplaced character : "+(char)ch);
                        tag = ch;
                        break;
                    case '>' :
                        if(tag != ']' || tag != '<')
                            error("Error.DTD", "Misplaced character : "+(char)ch);
                        break;
                        
                    default : 
                        buffer.append((char)ch);
                }
                
            }
        } catch (Exception e) {
            error("file is reading");
        }
    }
    
    /**
     * verify if character <code>ch</code> at index <code>index</code> 
     * into XML element name is valid character. example according to XML 
     * rules, name of element can't start with a number like <em>6tagName</em>
     *  is an invalid element's name but <em>tag6name</em> is a valid one that 
     * why character index is important to make validation
     * @param ch element's name character
     * @param index index of character
     * @return true if <code>ch</code> at index <code>index</code> is valid 
     * according to XML rule or return false otherwise
     */
    protected boolean isAvalidElementNameChar(char ch, int index){
        boolean isValidated = false;
        switch(ch){
            case '.' :
            case '-' : 
            case ',' :
                
                isValidated = false;
                break;
            default: 
                if(!Character.isAlphabetic(ch) && index == 0)
                    isValidated = false;
                
            
        }
        
        return isValidated;
    }
    
    /**
     * we have to verify that element's name respect conventions like 
     * an element should start with alphabet, element should not contain 
     * accentued caracter and so one
     * @param element element's name
     * @return element name if it well spelled and return null otherwise
     */
    private String validateElementName(String element){
        
        if(element.equals("ELEMENT"))
            error("Error.Element", "Element is a XML reserved name");
        
        
        for(int i = 0; i < element.length(); i++){
            if(!isAvalidElementNameChar(element.charAt(i), i)){
                error("Error.Element", element+" is not a valid element's name");
                return null;
            }
                
        }
        
        return element;
    }
    
    /**
     * Parse whole Element's name and return it when delimiter is 
     * encounter
     * @return 
     */
    private String parseValueName(){
        String valueName = new String();
        try {
            
            while(true){
                ch = in.read();
                switch(ch){
                    case -1 : 
                        error("En Of File");
                        break;
                    case '(' :    //parser reach here into DTD document  <!ELEMENT elementName (
                    case ',' :    //parser reach here into DTD document  <!ELEMENT elementName (...., childElementName, 
                    case ')':     //parser reach here into DTD document  <!ELEMENT elementName (...., ..., lastElementChild)
                    case ' ' :    //parser reach here into DTD document  <!ELEMENT elementName (..., childElementName 
                        //skipSpace();
                        return valueName;
                    default : 
                        valueName = valueName.concat(""+(char)ch);
                        break;
                }
                
            }
                
        } catch (Exception e) {
        }
 
        
        return valueName;
    }
    
    /**
     * 
     */
    protected void parseElement(){
        ContentModel bufferContent = null;
        ArrayList<ContentModel> wrappers = new ArrayList<ContentModel>();
        Element rootElement = null;
        
        
        
        int minOccurs = -1;
        int maxOccurs = -1;
        int currentOperator = -1;
        //Identifier can be one of that value (, (, < or >
        int identifier = 0;
        StringBuffer buffer = new StringBuffer();
        try {
            
            while((ch = readCh()) != -1){
                
                switch(ch){
                    case '('://when parser reach this caracter two case are possible
                        
                        
                        //first case is: parser has already read those caracters '<!ELEMENT rootElement (' 
                        //so string buffer has already accumuluted rootElement's name bufferContent is 
                        //still null and identifier is still egual to 0
                        if(rootElement == null && !buffer.isEmpty()){
                            xmlRules.validateNameSyntax(buffer.toString());
                            rootElement = dtd.defineElement(buffer.toString());
                            wrappers.add(bufferContent = (rootElement.content  = new ContentModel()));
                            bufferContent.type = new ComplexType("Root Type");
                            buffer.delete(0, buffer.length());
                            
                        }
                        //second case is: parser has already read those caracters 
                            //'<!Element rootElement (content1 | (' so
                            //rootElement has been already created and one or some content 
                            //model have been already created too
                        
                        else if(rootElement != null && bufferContent != null && buffer.isEmpty()){
                            
                            try {
                                if(wrappers.getLast() != bufferContent){
                                    wrappers.add(bufferContent);
                                }
                            } catch (NoSuchElementException e) {
                                error("DTD.Error", "( character is misplaced");
                            }
                            
                            bufferContent = bufferContent.content == null? (bufferContent.content = new ContentModel()) : 
                                                                 (bufferContent.next = new ContentModel());
                            
                            
                        }else{
                            error("Error.DTD", "( character is misplaced");
                        }
                        break;
                    case ')':
                        
                        if(!buffer.isEmpty() && bufferContent != null){
                            try {
                                xmlRules.validateNameSyntax(buffer.toString());
                                Element element = dtd.getElement(buffer.toString());//element can be null
                                error("DTD.Error", "element "+element.getName()+" already exist into DTD");
                            } catch (NullPointerException e) {
                                Element element = dtd.defineElement(buffer.toString());
                                bufferContent.addElementIndex(element.index);
                            }
                            buffer.delete(0, buffer.length());
                        } else if(bufferContent == null){
                            error("DTD.Error", ") caracter is missPlaced");
                        }
                        
                        
                        
                        try {
                            if(bufferContent == wrappers.getLast())
                                wrappers.removeLast();
                            bufferContent = wrappers.getLast();
                        } catch (NoSuchElementException e) {
                            if(bufferContent != rootElement.content)
                                error("DTD.Error", ") caracter is missPlaced");
                            bufferContent = null;
                        }
                        break;
                    case ' ':
                        /*if(rootElement == null && !buffer.isEmpty()){
                            xmlRules.validateNameSyntax(buffer.toString());
                            rootElement = dtd.defineElement(buffer.toString());
                            buffer.delete(0, buffer.length());
                        }*/
                            
                        break;
                    case '>':
                        //this below case correspond at case neither root element nor cont model haven't been initialized
                        //case exemple: <!ELEMENT element EMPTY>
                        //this case occur because ParserElement algorithm does nothing when space is parse
                        if(!buffer.isEmpty() && rootElement == null){
                            
                            for(int i = 0; i < 3; i++){
                                switch(i){
                                    case 0: 
                                        if(buffer.substring(buffer.length() - 3, buffer.length())
                                                    .toUpperCase().equals("ANY")){
                                            rootElement = dtd.defineElement(buffer.substring(0, buffer.length() - 3));
                                            rootElement.content = new ContentModel();
                                            ComplexType any = new ComplexType("Any");
                                            any.setContentType(true);
                                            rootElement.content.type = any;
                                            i = 3;
                                            break;
                                        }
                                        break;
                                    case 1: 
                                        if(buffer.substring(buffer.length() - 5, buffer.length())
                                                    .toString().toUpperCase().equals("EMPTY")){
                                            rootElement = dtd.defineElement(buffer.substring(0, buffer.length() - 5));
                                            rootElement.content = new ContentModel();
                                            SimpleType empty = new SimpleType("Empty");
                                            rootElement.content.type = empty;
                                            i = 3;
                                            break;
                                        }
                                        break;
                                    case 3:
                                        error("DTD.Error", "bad elements definition");
                                        
                                }
                            }
                        }
                        /*
                        if(rootElement != null && rootElement.content == null && !buffer.isEmpty())
                            switch(buffer.toString().toUpperCase()){
                                case "EMPTY":
                                    rootElement.content = new ContentModel();
                                    rootElement.content.type = HDTDConstants.EMPTY;
                                    break;
                                case "ANY" :
                                    rootElement.content = new ContentModel();
                                    rootElement.content.type = HDTDConstants.ANY;
                                    buffer = null;
                                    break;
                                default :
                                    error("DTD.Error", buffer+" is not a content model type", 
                                            "Replace by EMPTY or ANY content type");
                            } else  if(rootElement != null && bufferContent == null && 
                                                      buffer.isEmpty() && rootElement.content != null){
                            //this element have been well parsed and we have to : return;
                        }  else {
                            error("Error.DTD", "> character is misplaced");
                        }*/
                        bufferContent = null;
                        return;
                    case '#' :
                        
                        //we first have to verify if buffer is empty or not because we can have this case
                        //<!Element element #PCDATA>
                        if(!buffer.isEmpty() && rootElement == null){
                            xmlRules.validateNameSyntax(buffer.toString());
                            rootElement = dtd.defineElement(buffer.toString());
                            bufferContent = rootElement.content = new ContentModel();
                            SimpleType<String> pcdata = new SimpleType<String>("PCDATA");
                            pcdata.setCategory(SimpleType.NORMALYZED);
                            bufferContent.type = pcdata;
                            buffer.delete(0, buffer.length());
                        }
                        
                        for(int i = 0; i < 6; i++){
                            buffer.append((char)readCh());
                        }
                        if(!buffer.toString().equalsIgnoreCase("PCDATA"))
                            error("DTD.Error", "character # is misplaced");
                        
                        if(bufferContent.type != null && bufferContent.type.getName().equalsIgnoreCase("PCDATA"))
                            break;
                        
                        try {
                            //According to XML rules '#' character is the beginning of PCDATA content model so:
                            
                                
                            if(rootElement == null //root element not to have be null
                                 || bufferContent.childs.size() != 0 //PCDATA have to be the first element into content
                                           || bufferContent.content != null)//there aren't to have another content
                                error("DTD.Error", "character # is misplaced");
                            
                            bufferContent.content = new ContentModel();
                            SimpleType<String> pcdata = new SimpleType<String>("PCDATA");
                            bufferContent.content.type = pcdata;
                        } catch (NullPointerException e) {
                            
                            
                            error("DTD.Error", "character # is misplaced");
                            
                        }
                        buffer.delete(0, buffer.length());
                        break;
                        //cardinality caracter should be apply either to the text (rootElement and PCDATA except) or to content model
                    case '*' :
                        minOccurs = 0;
                        maxOccurs = Integer.MAX_VALUE;
                    case '+' :
                        minOccurs = minOccurs == -1? 1 : minOccurs;
                        maxOccurs = Integer.MAX_VALUE;
                    case '?' :
                        minOccurs = minOccurs == -1? 0 : minOccurs;
                        maxOccurs = maxOccurs == -1? 1 : maxOccurs;
                        
                         if(buffer.isEmpty() && !wrappers.isEmpty() && bufferContent == wrappers.getLast()){
                            ContentModel targetContent = bufferContent.content;
                             try {
                                 while(targetContent.next != null){
                                     targetContent = targetContent.next;
                                 }
                             } catch (NullPointerException e) {
                                 targetContent = bufferContent;
                             }
                            
                            targetContent.maxOccurs = maxOccurs; maxOccurs = -1;
                            targetContent.minOccurs = minOccurs; minOccurs = -1;
                        } else if(bufferContent != null){
                            bufferContent.maxOccurs = maxOccurs; maxOccurs = -1;
                            bufferContent.minOccurs = minOccurs; minOccurs = -1;
                        }
                        
                        break;
                    case ',' :  
                        if(bufferContent != null && bufferContent.content != null && 
                                    bufferContent.content.type.name.equalsIgnoreCase("PCDATA"))
                            error("DTD.Error", "character ',' is misplaced", 
                                    "change sequential ',' operator by choise operator '|'");
                    case '|' :
                        
                        
                        if(!buffer.isEmpty() && bufferContent != null){
                            try {
                                
                                xmlRules.validateNameSyntax(buffer.toString());
                                Element element = dtd.getElement(buffer.toString());
                                if(element == null){
                                    element = dtd.defineElement(buffer.toString());
                                    element.maxOccurs = maxOccurs == -1? 1: maxOccurs;
                                    element.minOccurs = minOccurs == -1? 1: minOccurs;
                                    bufferContent.addElementIndex(element.index);
                                    ((ComplexType)bufferContent.type).setConnector(
                                            ch == ','?ComplexType.Connectors.SEQUENCE : ComplexType.Connectors.CHOISE);
                                } else{
                                    error("DTD.Error", element+" already exist into DTD","Change element Name");
                                }
                                
                                
                                
                            } catch (Exception e) {
                                error("DTD.Error", "element "+buffer.toString()+" already exist into DTD");
                            }
                            
                            
                        }else if(buffer.isEmpty() && bufferContent != null && 
                                bufferContent.content.type.name.equalsIgnoreCase("PCDATA")){
                        }else{
                            error("Error.DTD", Character.toString(ch)+"character is misplaced");
                        }
                        buffer.delete(0, buffer.length());
                        break;
                        
                    case -1 : 
                            error("Error.DTD", "End of file");
                        break;
                    default :
                        
                        buffer.append((char)ch);
                }
            } 
        
        } catch (Exception e) {
            e.printStackTrace();
        
        }
    
    }
    
    
    
    private ContentModel get(ContentModel content, ContentModel token){
        for(ContentModel cm = content; cm == token; cm = content.next){
            if(cm == token)
                return cm;
        }
        return null;
    }
    
    
    private void parseAttributeList(boolean include){
        //buffer which will store non specific String like #, ), ( and so else
        StringBuffer attElement = new StringBuffer();
        //element on witch attribute belong
        Element element = null;
        //list of attributes
        AttributeList attList = null;
        
        try {
            while((ch = in.read()) != -1){
            switch(ch){
                
                case '#' :
                    if(attList == null){
                        error("Error.DTD", "bad declaration attribute");
                    }
                    resetBuffer();
                    break;
                    
                case '"' :
                    if(element != null && attList != null && attList.values != null 
                            && !attList.values.isEmpty() && !getString(0).isEmpty()){
                        attList.value = getString(0);
                    } else {
                        error("Error.DTD", "Default value is misplaced");
                    }
                    resetBuffer();
                    break;
                case ' ' :
                    if(element == null && !getString(0).isEmpty()){
                         //element = dtd.getElement(getString(0).toString());
                         
                         if(element == null)
                             error("Error.DTD", "Element "+attElement+" does not exist");
                         attList = element.atts;
                         resetBuffer();
                    } else if(element != null && attList == null && !getString(0).isEmpty()){
                        attList = element.atts;
                        for(;;){
                            if(attList != null)
                                attList = attList.next;
                            else 
                                break;
                            
                        }
                        attList = new AttributeList(getString(0).toString());
                        attList.modifier = HDTDConstants.IMPLIED;
                        resetBuffer();
                    } else if(element != null && attList != null && !getString(0).isEmpty()){
                        
                                
                        switch(getString(0).toUpperCase()){
                            case "CDATA":
                                //attList.type = HDTDConstants.CDATA;
                                break;
                            case "ID" :
                                //attList.type = HDTDConstants.ID;
                                break;
                            case "IDREF" :
                                //attList.type = HDTDConstants.IDREF;
                                break;
                            case "IDREFS" :
                                //attList.type = HDTDConstants.IDREFS;
                                break;
                            case "NMTOKEN" : 
                                //attList.type = HDTDConstants.NMTOKEN;
                                break;
                            case "NMTOKENS" :
                                //attList.type = HDTDConstants.NMTOKENS;
                                break;
                            case "REQUIRED" :
                                attList.modifier = HDTDConstants.REQUIRED;
                                break;
                            case "FIXED" :
                                attList.modifier = HDTDConstants.FIXED;
                                break;
                            default :
                                error("Error.DTD", "( character is missed");
                                Vector values = new Vector();
                                fillAttributeValue(values);
                                if(!values.isEmpty())
                                    attList.values = values;
                                attList = null;
                        }
                    }
                    resetBuffer();
                    break;
                case '(' :
                    Vector values = new Vector();
                    fillAttributeValue(values);
                    if(!values.isEmpty())
                        attList.values = values;
                    attList = null;
                    break;
                case '>' :
                    
                    if(element == null || attList == null)
                        error("Error.DTD", "inexpoitable attribute");
                    //this character can be read without read space character like this IMPLIED> so in this case first thing 
                    //to do is to verify if string buffer is not empty in this case try to determine modifier
                    if(!getString(0).isEmpty()){
                        switch(getString(0).toUpperCase()){
                            case "REQUIRED" :
                                attList.modifier = HDTDConstants.REQUIRED;
                                break;
                            case "FIXED" :
                                attList.modifier = HDTDConstants.FIXED;
                            case "IMPLIED" : 
                                attList.modifier = HDTDConstants.IMPLIED;
                                break;
                            default :
                                error("Error.DTD", "Unexpected modifier");
                        }
                    }
                    
                    if(attList.modifier == HDTDConstants.FIXED && attList.value == null){
                        error("Error.DTD", "FIXED attribut should have default attribute");
                    }
                    if(!include) attList = null;//if include is false, this attribute must not be addeed into element
                    break;
                default :
                    addString((char)ch);
            }
            }
        } catch (Exception e) {
        }
        
    }
    
    private void fillAttributeValue(Collection values){
        int specialChar = 0;
        try {
            while((ch = in.read()) != -1){
                switch(ch){
                    case '|' :
                        String buf = getString(0);
                        if(!buf.isEmpty()){
                            values.add(buf);
                            resetBuffer();
                        }else {
                            error("Error.DTD", "Values should be specified after | character");
                        }
                        specialChar = -1;
                        break;
                    case ' ' : 
                        specialChar = ' ';
                        error("Error.DTD", "Extra space");
                        break;
                    case ')' : 
                        if(!getString(0).isEmpty()){
                            values.add(getString(0));
                        }
                        resetBuffer();
                        specialChar = -1;
                        return;
                    case '>' : 
                        String buff = getString(0);
                        if(!buff.isEmpty()){
                            error("Error.DTD", "Default attribut value is missed");
                            
                        } else {
                            error("Error.DTD", "value must follow | character");
                        }
                        values.add(buff);
                        specialChar = -1;
                        return;
                    case '"' : 
                        resetBuffer();
                        specialChar = -1;
                        return;
                    default :
                        //here we test case user write something like this ( value1 value2| that means nothing
                        //because two value must be separated by pipe character
                        if(specialChar == ' ' && !getString(0).isEmpty())
                            error("Error.DTD", "Bad attibute character");
                        addString((char)ch);
                }
            }
        } catch (Exception e) {
        }
    }
    
    private void parseEntity(){
        
        try {
            Entity entity = null;
            int paramEntity = -1;
            while((ch = in.read()) != -1){
            switch(ch){
                case ' ':
                    if(entity == null && !getString(0).isEmpty()){
                        //entity = dtd.defEntity(getString(0), paramEntity, -1);
                    } else if(entity != null && entity.type == - 1  && !getString(0).isEmpty()){
                        
                        switch(getString(0).toUpperCase()){
                            case "SYSTEM" : 
                                entity.type = HDTDConstants.SYSTEM;
                                break;
                            case "PUBLIC" : 
                                entity.type = HDTDConstants.PUBLIC;
                                break;
                            default : 
                                error("Error.DTD", "Entity type not recognized");
                        }
                    }
                    resetBuffer();
                    break;
                case '%' : 
                    paramEntity = HDTDConstants.PARAMETER;
                    break;
                case '"' :
                    if(entity != null && entity.type != -1 && !getString(0).isEmpty()){
                        entity.data = getString(0).toCharArray();
                        
                    } 
                    resetBuffer();
                    break;
                case '\'' :
                    if(entity != null && entity.type == HDTDConstants.PARAMETER && getString(0).isEmpty()){
                        switch(getString(0).toUpperCase()){
                            case "INCLUDE" :
                            case "IGNORE" :
                                entity.data = getString(0).toCharArray();
                                resetBuffer();
                                break;
                            default : 
                                error("Error.DTD", "Type parameter entity must be INCLUDE or IGNORE");
                        }
                    }
                default :
                    addString((char)ch);
            }
            }
        } catch (Exception e) {
        }
        
    }
    
    private void parseNotation(){
        
        try {
            while((ch = in.read()) != -1){
                
            
            }
        } catch (Exception e) {
        }
        
    }
}
