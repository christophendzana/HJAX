/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.UnitaryTest;

import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.xml.AttributeList;
import hsegment.JObject.Swing.Text.xml.Parser;
import hsegment.JObject.Swing.Text.xml.TagElement;
import hsegment.JObject.Swing.Text.xml.handler.ErrorHandler;
import hsegment.JObject.Swing.Text.xml.handler.TagHandler;
import hsegment.JObject.util.FunctionUtils;

import java.util.logging.Logger;

import static hsegment.JObject.util.FunctionUtils.getSourceError;


/**
 *
 * @author Ndzana Christophe, Hyacinthe Tsague
 */
public class TagHandlerUT implements TagHandler {

    private final ErrorHandler errorHandler;

    public TagHandlerUT(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    @Override
    public void handleEmptyTag(TagElement tag) throws HJAXException {
        System.out.println("appel de la methode empty tag:  ==>"+tag.getElement().getName());
        validateTagName(tag);
        validateAttributes(tag);
    }

    @Override
    public void handleStartTag(TagElement tag) throws HJAXException {
        System.out.println("appel de la methode open tag:  ==> "+tag.getElement().getName());
        validateTagName(tag);
        validateAttributes(tag);
    }

    @Override
    public void handleEndTag(TagElement tag) throws HJAXException {
        System.out.println("appel de la methode close tag: "+tag.getElement().getName());
        validateTagName(tag);
    }

    private void validateTagName(TagElement tag) throws HJAXException{
        String tagName = tag.getElement().getName();
        if(FunctionUtils.verifyEmptyName(tagName)){
            errorHandler.errorHandler(getSourceError(tag), "Empty tag name : "+tagName, "Add a tag name !"+". ",
                    ErrorType.FatalError);
        }
        if(!FunctionUtils.verifyStartName(tagName)){
            errorHandler.errorHandler(getSourceError(tag), "Invalid first character of tag name : "+tagName+". ",
                    "Add a valid first character of tag name, " +
                            "it must begin with '_' or '-' or lower or upper case letter.", ErrorType.Warning);
        }
        if(!FunctionUtils.verifyName(tagName)){
            errorHandler.errorHandler(getSourceError(tag), "Invalid tag name : "+tagName+". ",
                    "Add valid character(s) of tag name", ErrorType.Warning);
        }
    }

    private void validateAttributes(TagElement tag) throws HJAXException{
        //get attribute from tagElement
        if(tag.getElement().getAttributeList() != null){
            //get the current attribute list
            AttributeList attributeList = tag.getElement().getAttributeList();
            //attribute name validation
            validateAttributeName(tag, attributeList.getName());
            //get the next attribute list
            AttributeList next = attributeList.getNext();
            while (next != null){
                validateAttributeName(tag, next.getName());
                //get the next attribute list from current attribute linked list
                next = next.getNext();
            }
        }
    }

    private void validateAttributeName(TagElement tag, String attributeName) throws HJAXException{
        //empty attribute name validation
        System.out.println("Attribute name :"+attributeName);
        if(FunctionUtils.verifyEmptyName(attributeName)){
            errorHandler.errorHandler(getSourceError(tag), "Empty attribute name", "Add attribute name !",
                    ErrorType.FatalError);
        }
        //start attribute name validation
        if(!FunctionUtils.verifyStartName(attributeName)){
            errorHandler.errorHandler(getSourceError(tag), "Invalid first character of attribute name : "+attributeName,
                    "Add a valid first character of attribute name !", ErrorType.Warning);
        }
        //attribute name characters validation
        if(!FunctionUtils.verifyName(attributeName)){
            errorHandler.errorHandler(getSourceError(tag), "Invalid attribute name : "+attributeName,
                    "Add valid attribute name or remove space behind !", ErrorType.Warning);
        }
    }

}
