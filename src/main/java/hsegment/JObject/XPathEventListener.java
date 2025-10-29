/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import APIXPath.NodeEvent;

/**
 * Interface que doit implémenter toute classe qui souhaite
 * recevoir les événements générés par le XpathParser.
 *
 * Elle définit les méthodes de rappel (callbacks)
 * appelées à chaque étape du parsing.
 * 
 * @author FIDELE
 */
public interface XPathEventListener {
    
    /**
     * Appelée lorsqu'un nœud simple est détecté dans l'expression XPath.
     * Exemple : /book/title  → "book", "title"
     *
     * @param event L'événement contenant les informations du nœud.
     */
    void onNode(NodeEvent event);

    /**
     * Appelée lorsqu'un nœud avec prédicat est détecté.
     * Exemple : /book[price>10]
     *
     * @param event L'événement décrivant le nœud et son prédicat.
     */
    void onPredicate(NodeEvent event);

    /**
     * Appelée lorsqu'un nœud avec fonction est détecté.
     * Exemple : /book[contains(title, "Java")]
     *
     * @param event L'événement décrivant le nœud, la fonction et ses arguments.
     */
    void onFunction(NodeEvent event);
    
}
