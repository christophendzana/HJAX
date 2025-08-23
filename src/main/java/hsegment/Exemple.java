/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 *
 * @author PSM
 */
public class Exemple {
    
    private Document document;      
    
    // Chargement du document XMl et création du DOM
    public Exemple (String cheminFichier)throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(new File(cheminFichier));
    }
    
    // Element Racine du document    
    public Element rootElement(){
        return document.getDocumentElement();
    }
    
     // Ajouter un élément
    public void ajouterElement(Element parent, String nomElement, String contenu) {
        Element nouvelElement = document.createElement(nomElement);
        nouvelElement.setTextContent(contenu);
        parent.appendChild(nouvelElement);
    }
        
     // Parcourir les éléments par nom de balise
    public void parcourirElements(String nomBalise) {
        NodeList nodeList = document.getElementsByTagName(nomBalise);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                System.out.println("Élément : " + element.getNodeName() + ", Valeur : " + element.getTextContent());
            }
        }
    }
    
    public void setAttributElement(Element element){
        
    }
    
    // Sauvegarder les modifications
    public void sauvegarder(String cheminFichier) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult resultat = new StreamResult(new File(cheminFichier));
        transformer.transform(source, resultat);
    }

  
            
    
}
