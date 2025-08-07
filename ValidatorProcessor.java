/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.xml;


import hsegment.JObject.Swing.Text.ErrorHandler;
import hsegment.JObject.Swing.Text.ErrorType;

import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.TagHandler;

import hsegment.JObject.Swing.Text.xml.parser.AttributeList;
import hsegment.JObject.Swing.Text.xml.parser.ContentModel;
import hsegment.JObject.Swing.Text.xml.parser.Element;
import hsegment.JObject.Swing.Text.xml.parser.Operator;
import hsegment.JObject.Swing.Text.xml.parser.Type;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Ndzana Christophe
 */
public class ValidatorProcessor implements TagHandler, ErrorHandler{   
    
    private Document doc;
    private Element element;  
    private Element validatorElement;
    private ArrayList<Element> AllElements = new ArrayList<>();      
 
    public ValidatorProcessor(){
        
    }
    
    public void InitValidator(Document validator){
        doc = validator;
    }

    @Override
    public void handleEmptyTag(TagElement tag) {
        
    }

    @Override
    public void handleStartTag(TagElement tag) {
        
      element = tag.getElement();    
      AttributeList validatorAtt = null;
      AttributeList xmlAtt = null; 
      Element rootXmlElement = null;
      Element rootValidatorElement = null;
      
      ///// Vérification si élément autorisé
        elementExist(element.getName());
                        
            AllElements.add(element); 
            validatorElement = doc.getElement(element.getName());
            validatorAtt = validatorElement.getAttributes();
            xmlAtt = element.getAttributes(); 
            
        ///// Restrictions sur les attributs
        attributRestriction( validatorAtt, xmlAtt );
        
        
        ///// Vérifaication de la correspondance du type des attributs
        attributType(validatorAtt, xmlAtt);
        
        ///// Verification de la hierachie des éléments
            
            if (AllElements.getLast().getName().equalsIgnoreCase(element.getName())) {                
                  errorHandler("Error.Validator", "Error parent element", "", ErrorType.FatalError);                
            }else{
                
                rootXmlElement = AllElements.getLast(); // Element parent de l'élément courant
                rootValidatorElement = validatorElement.getContentModel().getRoot();
                                
                Operator operator = null;
                                                
                if ( rootXmlElement.getName().equalsIgnoreCase(rootValidatorElement.getName()) ) {
                                        
                    operator = rootValidatorElement.getContentModel().getOperator();
                    
                    Iterator<Element> valElementChilds = doc.getChild(rootValidatorElement).iterator();
                    
                    int indexXmlElement = 0;                    
                    int occurenceXmlElement = 0;
                    
                    int indexValElement = 0;
                    int MaxOccurence = 0;
                    int MinOccurence = 0;
                    
                    while (valElementChilds.hasNext()) {                    

                        if (element.getName().equalsIgnoreCase(valElementChilds.next().getName())) {                              
                                                        
                            MaxOccurence = valElementChilds.next().getMaxOccurency(); 
                            MinOccurence = valElementChilds.next().getMinOccurency();
                            occurenceXmlElement++;
                            
                        }else{
                            indexValElement++;
                        }
                        
                        indexXmlElement++;
                    }                    
                    
                    switch (operator) {
                        case Operator.SEQUENCE -> {
                            if (indexValElement != indexXmlElement) {
                                errorHandler("Validator.Error", "The index of the element is not respected", "the element must be at index"+indexValElement, ErrorType.Warning);
                            }else if( occurenceXmlElement > MaxOccurence ){                                
                                int surplus = Math.abs( occurenceXmlElement - MaxOccurence);
                                errorHandler("Validator.Error", "MaxOccurence is not respected", "there are "+surplus+ " more occurence(s)", ErrorType.Warning);
                            }else if(occurenceXmlElement < MinOccurence) {
                                int less = Math.abs( MinOccurence - occurenceXmlElement);
                                errorHandler("Validator.Error", "MinOccurence is not respected", "there are "+less+ " less occurence(s)", ErrorType.Warning);
                            }
                        }
                        case Operator.ALL -> {
                            if (occurenceXmlElement > MaxOccurence) {
                                int surplus = Math.abs( occurenceXmlElement - MaxOccurence);
                                errorHandler("Validator.Error", "MaxOccurence is not respected", "there are "+surplus+ " more occurences", ErrorType.Warning);
                            } else if(occurenceXmlElement < MinOccurence) {
                                int less = Math.abs( MinOccurence - occurenceXmlElement);
                                errorHandler("Validator.Error", "MinOccurence is not respected", "there are "+less+ " less occurence(s)", ErrorType.Warning);
                            }
                        }
                        case Operator.CHOISE -> {                            
                            
                        }
                        default -> throw new AssertionError();
                    }
                }else{
                    errorHandler("Validator.Error", "The root element no match", "", ErrorType.FatalError);
                }
            }
    }
    
    
    private void elementExist(String name){
        if (doc.getElement(name) == null) {
            errorHandler("Error.Validator", "Element non permis", "Remove it", ErrorType.FatalError);
        } 
    }
    
    private void attributRestriction( AttributeList validatorAtt, AttributeList xmlAtt ){
        
        try {
                while (validatorAtt != null){ //can throw NullPointerException
                    switch (validatorAtt.getUsability()) {
                        case AttributeList.REQUIRED -> {
                            while (xmlAtt != null) {
                                
                                if (xmlAtt.getName().equalsIgnoreCase(validatorAtt.getName())) {
                                    break;
                                }
                                xmlAtt = xmlAtt.getNext();
                            }   if (xmlAtt == null) {
                                errorHandler("Validator.Error", "Missed attribute", validatorAtt.getName()+" Should be declared", ErrorType.FatalError);
                            }
                        }
                        case AttributeList.PROHIBITED -> {
                            while (xmlAtt != null) {
                                if (xmlAtt.getName().equalsIgnoreCase(validatorAtt.getName())) {
                                    errorHandler("Validator.Error", "Prohibited attribute", "Remove "+xmlAtt.getName()+" attribute", ErrorType.FatalError);
                                }
                            }
                        }
                        default -> {
                        }
                    }
                    
                    validatorAtt = validatorAtt.getNext();
                }
            } catch (NullPointerException e) {
                errorHandler("Validator.Error", element.getName()+" should not have an attributes", "Remove all attributes", ErrorType.FatalError);
            } 
    }
    
    private void attributType( AttributeList validatorAtt, AttributeList xmlAtt ){
        while (xmlAtt != null) {                
                
                String typeXmlAtt = xmlAtt.getType().getName();
                
                while (validatorAtt != null) {                    
                    if (xmlAtt.getName().equalsIgnoreCase(validatorAtt.getName())) {
                        String typeValAtt = validatorAtt.getType().getName();
                        
                        if (!typeXmlAtt.equalsIgnoreCase(typeValAtt)) {
                            errorHandler("Validator.Error", "Attibute type does no match", "Replace the value with a "+typeValAtt+ "type" , ErrorType.FatalError);
                        }else{
                            break;
                        }
                    }
                }
                xmlAtt = xmlAtt.getNext();                
            }
    }
    
    @Override
    public void handleEndTag(TagElement tag) {
        Element endTag = tag.getElement();
        
        Element lastElement = AllElements.getLast();
        
        if (endTag.getName().equalsIgnoreCase(lastElement.getName())) {
            AllElements.removeLast();
        }
    }

    @Override
    public void errorHandler(String src, String msg, String debug, ErrorType type) throws HJAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }   
 
}
