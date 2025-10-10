/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *Transforme la chaine XPath en expresion comprehensible par le moteur
 * @author FIDELE
 */
public class XPathParser {
    
     public ArrayList<XPathStep> parse(String expression) {
        ArrayList<XPathStep> steps = new ArrayList<>();

        if (expression == null || expression.isEmpty()) {
            return null;
        }

        // On nettoie le début de l'expression : supprimer les '/' initiaux
        // Pour pouvoir découper avec les '/'
        expression = expression.trim();
        
        Pattern pattern = Pattern.compile("//?|[^/]+");
        Matcher matcher = pattern.matcher(expression);
        ArrayList expressionPath = new ArrayList<>();
        
        while (matcher.find()) {
            expressionPath.add(matcher.group());          
        }

        ArrayList expressionStep = new ArrayList<>();
        
        for (int i = 1; i < expressionPath.size(); i+=2) {
            
            if (i<expressionPath.size()) {
                String operator = expressionPath.get(i-1).toString();
                String step = expressionPath.get(i).toString();
                
                if ((operator.equals("/"))||operator.equals("//") && (!step.equals("/") || !step.equals("//"))) {
                    expressionStep.add(operator+"-"+step);
                }                
            }            
        }
        
        // Découper sur les '/'
        String[] parts = expression.split("//?|[^/]+");

        for (Object step : expressionStep) {
            if (!step.toString().isEmpty()) {
                
                String[] stp = step.toString().split("-");                
                
                if (step.toString().contains("[@")&&step.toString().contains("=") ) {
                    
                    Pattern pattern2 = Pattern.compile("@(\\w+)='([^']+)'");
                    Matcher matcher2 = pattern2.matcher(stp[1].toString());
                    String attrname = matcher2.group(1);
                    String attrvalue = matcher2.group(2);   
                    steps.add(new XPathStep(stp[0], stp[1], attrname, attrvalue));
                    
                }else{
                   steps.add(new XPathStep(stp[0], stp[1], null,null));
                }
            }
        }

        return steps;
    }
    
}
