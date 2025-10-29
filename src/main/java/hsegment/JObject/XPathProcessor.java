/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

import DOM.Document;
import DOM.ElementNode;
import DOM.NodeImpl;
import Interface.XPathEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author FIDELE
 */
public class XPathProcessor implements XPathEventListener{
    
     private Document document;
    private List<NodeImpl> currentNodes; // Liste des nœuds en cours de traitement

    public XPathProcessor(Document document) {
        this.document = document;
        this.currentNodes = new ArrayList<>();
    }

    @Override
    public void onNode(NodeEvent event) {
        
        if (document.getElement(event.getNodeName()) == null ) throw new XPathNodeNotFoundException(event.getNodeName());
        
        List<NodeImpl> resultNodes = new ArrayList<>();
        
        //Si la liste de parcours est vide on commence la recherche par la racine
        for (NodeImpl node : currentNodes.isEmpty() ? List.of(document.getRootElement()) : currentNodes) {
            if (event.getDepth() == 0) { // recherche sur les enfants directs du noeud courant
                
                
                //Selectionne tous les noeuds qui correspondent au nom recherché. 
                //fait un for sur la liste de recherche et un if sur le nom 
                resultNodes.addAll(document.getChilds(node).getNodes().stream() 
                        .filter(n -> n.getNodeName().equals(event.getNodeName()))
                        .collect(Collectors.toList()));
            } else { // depth == 1 pour recherche sur tous les descandants
                resultNodes.addAll(document.getAllDescendants(node).stream()
                        .filter(n -> n.getNodeName().equals(event.getNodeName()))
                        .collect(Collectors.toList()));
            }
        }
        currentNodes = resultNodes;
    }

  
    
    @Override
    public void onPredicate(NodeEvent event) {
        List<NodeImpl> filtered = new ArrayList<>();
        for (NodeImpl node : currentNodes) {
            if (evaluatePredicate(node, event.getPredicate())) {
                filtered.add(node);
            }
        }
        currentNodes = filtered;
    }

    @Override
    public void onFunction(NodeEvent event) {
        List<NodeImpl> processed = new ArrayList<>();
        for (NodeImpl node : currentNodes) {
            if (applyFunction(node, event.getFunctionName(), event.getFunctionArgs())) {
                processed.add(node);
            }
        }
        currentNodes = processed;
    }

    /**
     * Évalue un prédicat sur un nœud.
     * Cette version applique les fonctions imbriquées si nécessaire.
     */
   private boolean evaluatePredicate(NodeImpl node, String predicate) {
    predicate = predicate.trim();

    // 🔹 Cas d'une fonction (ex: contains(title,'Java'))
    if (predicate.contains("(")) { 
        String functionName = predicate.substring(0, predicate.indexOf('(')).trim();
        String argsPart = predicate.substring(predicate.indexOf('(') + 1, predicate.lastIndexOf(')')).trim();
        List<String> args = new ArrayList<>();
        if (!argsPart.isEmpty()) {
            for (String arg : argsPart.split("\\s*,\\s*")) {
                args.add(arg.trim());
            }
        }
        return applyFunction(node, functionName, args);
    }

    // 🔹 Cas d'un prédicat simple avec opérateur
    String[] operators = {"!=", "<=", ">=", "=", "<", ">"};
    for (String op : operators) {
        int index = predicate.indexOf(op);
        if (index > 0) {
            String left = predicate.substring(0, index).replace("@", "").trim();  // attribut
            String right = predicate.substring(index + op.length()).replace("'", "").trim(); // valeur
            String nodeValue = document.getAttributeNode( (ElementNode) node, left).getValue();
            if (nodeValue == null) return false;

            // Comparaison selon l'opérateur
            switch (op) {
                case "=":  return nodeValue.equals(right);
                case "!=": return !nodeValue.equals(right);
                case "<":  return Double.parseDouble(nodeValue) < Double.parseDouble(right);
                case ">":  return Double.parseDouble(nodeValue) > Double.parseDouble(right);
                case "<=": return Double.parseDouble(nodeValue) <= Double.parseDouble(right);
                case ">=": return Double.parseDouble(nodeValue) >= Double.parseDouble(right);
            }
        }
    }

    // 🔹 Aucun opérateur reconnu → rejet
    return false;
}


    /**
     * Applique une fonction sur un nœud et retourne true si le nœud doit être conservé.
     */
    private boolean applyFunction(NodeImpl node, String functionName, List<String> args) {
   switch (functionName) {
        case "contains" -> {
            if (args.size() != 2) 
                throw new XPathFunctionException("contains() nécessite exactement 2 arguments");
            String nodeValue = getNodeValue(node, args.get(0));
            String search = args.get(1).replace("'", "");
            return nodeValue != null && nodeValue.contains(search);
             }

        case "starts-with" -> {
            if (args.size() != 2) 
                throw new XPathFunctionException("starts-with() nécessite exactement 2 arguments");
                 String nodeValue = getNodeValue(node, args.get(0));
                 String search = args.get(1).replace("'", "");
                 return nodeValue != null && nodeValue.startsWith(search);
             }

        case "ends-with" -> {
            if (args.size() != 2) 
                throw new XPathFunctionException("ends-with() nécessite exactement 2 arguments");
                 String nodeValue = getNodeValue(node, args.get(0));
                 String search = args.get(1).replace("'", "");
                 return nodeValue != null && nodeValue.endsWith(search);
             }

        case "text" -> {
            if (!args.isEmpty()) 
                throw new XPathFunctionException("text() ne prend pas d'arguments");
            String text = getNodeValue(node, null);
            return text != null && !text.isEmpty();
             }

        case "position" -> {
            if (args.size() != 1) 
                throw new XPathFunctionException("position() nécessite exactement 1 argument");
            int pos;
            try { pos = Integer.parseInt(args.get(0)); } 
            catch (NumberFormatException e) {
                throw new XPathFunctionException("position() : argument non numérique");
            }
            
            List<NodeImpl> siblings = document.getChilds(document.getParent(node)).getNodes();
            return siblings.indexOf(node) + 1 == pos; // XPath commence à 1
             }
        case "last" -> {
            
            if (!args.isEmpty()) throw new XPathFunctionException("La fonction last() : ne prend aucun argument");                            
            List<NodeImpl> siblingsLast = document.getChilds(document.getParent(node)).getNodes();            
            int lastPos = siblingsLast.size();
            return siblingsLast.indexOf(node) + 1 == lastPos;
            }

        default -> throw new XPathFunctionException("Fonction inconnue : " + functionName);
    }
}

    /**
     * Retourne la valeur d'un nœud pour un attribut ou text.
     */
    private String getNodeValue(NodeImpl node, String attr) {
        if (attr == null || attr.isEmpty()) {
            return document.getTextContent(node);
        } else {
            return document.getAttributeNode( (ElementNode) node, attr).getValue();
        }
    }

    /**
     * Retourne la liste des nœuds résultants après tout le parsing.
     */
    public List<NodeImpl> getResultNodes() {
        return currentNodes;
    }
    
}
