/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.Document;

/**
 *Gère la connexion entre le document et le moteur XPath
 * @author FIDELE
 */
public class XPathConnexion {
    
    private Document document;    

    public XPathConnexion(Document document) {
        this.document = document;
    }

    // Crée une requête XPath
    public XPathQuery createQuery(String expression) {
        return new XPathQuery(expression, document);
    }

    // Exécute directement une requête XPath en une ligne
    public XPathResult execute(String expression) {
        XPathQuery query = new XPathQuery(expression, document);
        return query.execute();
    }

    public Document getDocument() {
        return document;
    }
    
    
}