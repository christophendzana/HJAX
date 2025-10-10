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
        while (expression.startsWith("/")) {
            expression = expression.substring(1);
        }

        // Découper sur les '/'
        String[] parts = expression.split("/");

        for (String part : parts) {
            if (!part.isEmpty()) {
                if (part.contains("[@")&&part.contains("=") ) {
                    
                    Pattern pattern = Pattern.compile("\\[@(\\w+)='(\\w+)'\\]");
                    Matcher matcher = pattern.matcher(part);
                    String attrname = matcher.group(1);
                    String attrvalue = matcher.group(2);   
                    steps.add(new XPathStep("/", part, attrname, attrvalue));
                    
                }else{
                   steps.add(new XPathStep("/", part, null,null));
                }
            }
        }

        return steps;
    }
    
}
