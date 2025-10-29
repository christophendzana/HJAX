/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

import DOM.Document;
import DOM.NodeImpl;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class XPathConnexion {
    
       private Document document; // Ton DOM maison

    public XPathConnexion(Document document) {
        this.document = document;
    }

    /**
     * Exécute une requête XPath sur le Document.
     *
     * @param expression Expression XPath
     * @return XpathResult contenant la liste des nœuds correspondants
     * @throws Exception en cas d’erreur de parsing ou de syntaxe
     */
    public XPathResult executeQuery(String expression) throws Exception {
        // Créer un processeur pour stocker les résultats
        XPathProcessor processor = new XPathProcessor(document);

        // Créer un parser avec le processeur comme listener
        XPathParser parser = new XPathParser(processor);

        // Lire la requête via un Reader
        Reader reader = new StringReader(expression);

        // Exécuter le parsing
        parser.parse(reader);

        // Récupérer les nœuds trouvés
        List<NodeImpl> resultNodes = processor.getResultNodes();

        // Retourner un objet XpathResult
        return new XPathResult(resultNodes);
    }
    
}
