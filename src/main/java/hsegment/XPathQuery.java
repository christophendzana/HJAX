/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.Document;

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

    public XPathResult execute() {
        XPathProcessor engine = new XPathProcessor(document);
        return engine.evaluate(expression);
    }

    public String getExpression() {
        return expression;
    }
    
}
