/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import DOM.AttributeNode;
import DOM.Document;
import DOM.ElementNode;
import DOM.NodeImpl;
import DOM.NodeList;
import java.util.ArrayList;
import java.util.List;

/**
 * Coeur logique Analyse les expression XPath et execute les requêtes
 *
 * @author FIDELE
 */
public class XPathProcessor {

    private Document document;

    public XPathProcessor(Document document) {
        this.document = document;
    }

    public XPathResult evaluate(String expression) {
        XPathParser parser = new XPathParser();
        ArrayList<XPathStep> steps = parser.parse(expression);

        NodeImpl root = document.getDocumentElement();
        ArrayList<NodeImpl> resultNodes = evaluateSteps(root, steps, 0);
        return new XPathResult(resultNodes);
    }

    // Parcours le document selon les étapes décrite par la requete
    private ArrayList<NodeImpl> evaluateSteps(NodeImpl currentNode, List<XPathStep> steps, int index) {
        ArrayList<NodeImpl> result = new ArrayList<>();

        if (index >= steps.size()) {
            result.add(currentNode);
            return result;
        }

        XPathStep step = steps.get(index);
        List<NodeImpl> matchingChildren = new ArrayList<>();

        if (step.getOperator().equalsIgnoreCase("/")) {
            for (int i = 0; i < document.getChildCount((ElementNode) currentNode); i++) {
                NodeImpl child = document.getChilds(currentNode).item(i);
                if (matchingStep(child, step)) {
                    matchingChildren.add(child);
                }
            }
        } else if (step.getOperator().equalsIgnoreCase("//")) {
            ArrayList childs = getAllDescendants(currentNode);
            for (int i = 0; i < childs.size(); i++) {
                NodeImpl child = (NodeImpl) childs.get(i);
                if (matchingStep(child, step)) {
                    matchingChildren.add(child);
                }
            }
        }

        //Pour chaque enfant trouvé de l'étape précédente on applique 
        //evaluateSteps avec l'étape suivante et recursivement
        for (NodeImpl child : matchingChildren) {
            result.addAll(evaluateSteps(child, steps, index + 1));
        }

        return result;
    }

    private ArrayList<NodeImpl> getAllDescendants(NodeImpl node) {

        ArrayList<NodeImpl> descendants = new ArrayList<>();

        for (int i = 0; i < document.getChildCount((ElementNode) node); i++) {
            NodeImpl child = document.getChilds(node).item(i);
            descendants.add(child);
            descendants.addAll(getAllDescendants(child));
        }

        return descendants;
    }

    private boolean matchingStep(NodeImpl node, XPathStep step) {

        if (!node.getNodeName().equalsIgnoreCase(step.getNodeName())) {
            return false;
        }

        if (step.getAttribut() != null) {
            AttributeNode attrStep = step.getAttribut();

            AttributeNode attrNode = document.getAttributeNode((ElementNode) node, attrStep.getNodeName());

            if (attrNode == null) {
                return false;
            }

            return attrNode.getNodeValue().equalsIgnoreCase(attrStep.getNodeValue());
        }
        return true;
    }

}
