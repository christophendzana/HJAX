/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import java.util.ArrayList;

/**
 *Représente une étape du chemin XPath: nom du noeud, filtre, opérateur
 * @author FIDELE
 */
public class XPathStep {
    
    private String depth;   
    private String nodeName; 
    private ArrayList<XPathFilter> filters;

    public XPathStep(String depth, String nodeName, ArrayList<XPathFilter> filters) {
        this.depth = depth;
        this.nodeName = nodeName;
        this.filters = filters;
    }
    
    public XPathStep(String operator, String nodeName){
        this.nodeName = nodeName;
        this.depth = operator;
    }

    public String getDepth() {
        return depth;
    }

    public String getNodeName() {
        return nodeName;
    }
    
    public void addFilter(XPathFilter filter){
        filters.add(filter);
    }
         
    public ArrayList<XPathFilter> getFilters(){
        return filters;
    }
    
}
