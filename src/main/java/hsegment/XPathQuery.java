/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.Document;
import java.util.ArrayList;

/**
 *Représente une requête 
 * Stocke l'expression permet l'excution
 * @author FIDELE
 */
public class XPathQuery {
    
     private String expression;
    private Document document;

    public XPathQuery(String expression, Document document) {
        this.expression = expression;
        this.document = document;
    }
    
    public boolean isValidExpression(String expression){
        
        if (expression == null || expression.isEmpty()) {
            return false;
        }
        
        XPathParser parser = new XPathParser();
        
        try {
            ArrayList<XPathStep> steps = parser.parse(expression);
            return (steps != null && !steps.isEmpty());
        } catch (Exception e) {
            return false;
        }
        
    }

    public XPathResult execute() {
        XPathProcessor engine = new XPathProcessor(document);
        return engine.evaluate(expression);
    }

    public String getExpression() {
        return expression;
    }
    
}
