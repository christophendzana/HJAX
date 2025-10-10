/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.AttributeNode;

/**
 *Représente une étape du chemin XPath: nom du noeud, filtre, opérateur
 * @author FIDELE
 */
public class XPathStep {
    
    private String operator;   
    private String nodeName;   
    private String attrName;
    private String attrValue;

    public XPathStep(String operator, String nodeName, String attrName, String attrValue) {
        this.operator = operator;
        this.nodeName = nodeName;
        this.attrName = attrName;
        this.attrValue = attrValue;
    }
    
    public XPathStep(String operator, String nodeName){
        this.nodeName = nodeName;
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public String getNodeName() {
        return nodeName;
    }

    public AttributeNode getAttribut() {
        if (attrName!=null && attrValue !=null) {
            return new AttributeNode(attrName, attrValue, true);
        }else{
            return null;
        }
    }
    
}
