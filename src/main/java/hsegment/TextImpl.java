/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.Text;
import org.w3c.dom.DOMException;

/**
 *
 * @author PSM
 */
public class TextImpl extends CharDataImpl implements Text {
    
    public TextImpl(String data, DocumentImpl holderDocument) {
        super("#Text", TEXT_NODE, holderDocument);
    }
    
    // Découpe un noeud Text en deux et crée un nouveau noeud frère en y insérant
    // le contenu découpé
    @Override
    public Text splitText(int offset) throws DOMException {
        if (offset < 0 || offset > data.length()) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "Invalid offset");
        }
        String newData = data.substring(offset);
        data = data.substring(0, offset);
        TextImpl newTextNodeImpl = new TextImpl(newData, holderDocument);

        // Insère juste après ce nœud
        if (nodeParent != null && nodeParent instanceof NodeImpl) {
            NodeImpl parent = (NodeImpl) nodeParent;
            int index = parent.getChildNodes().indexNode(this.getNodeName());
            if (index != -1) {
                parent.getChildNodes().addNodeInIndex(index + 1, newTextNodeImpl);
                newTextNodeImpl.nodeParent = parent;
            }
        }

        return newTextNodeImpl;
    }

    @Override
    public boolean isElementContentWhitespace() {
        return data.trim().isEmpty();
    }

    
    //Récupère sous forme de chaîne unique le texte de tous les noeuds Text enfant 
    @Override
    public String getWholeText() {
        StringBuilder sb = new StringBuilder();
        if (nodeParent != null) {
            NodeListImpl siblings = nodeParent.getChildNodes();
            for (int i = 0; i < siblings.getLength(); i++) {
                NodeImpl node = siblings.item(i);
                if (node.getNodeType() == NodeImpl.TEXT_NODE) {
                    sb.append(((TextImpl) node).getData());
                }
            }
        } else {
            sb.append(data);
        }
        return sb.toString();
    }

    //Supprime tous les noeuds Text frères et ajoute un noeud Text au parent 
    
    @Override
    public Text replaceWholeText(String content) throws DOMException {
        if (nodeParent == null) {
            this.data = content;
            return this;
        }

        //On parcours à l'envers pour éviter les problèmes d'indexation
        //Quand on supprime en cours de boucle.
        NodeListImpl siblings = nodeParent.getChildNodes();
        for (int i = siblings.getLength() - 1; i >= 0; i--) {
            NodeImpl node = siblings.item(i);
            if (node.getNodeType() == NodeImpl.TEXT_NODE) {
                nodeParent.removeChild(node);
            }
        }

        TextImpl newTextNodeImpl = new TextImpl(content, holderDocument);
        nodeParent.appendChild(newTextNodeImpl);
        return newTextNodeImpl;
    }
}
    
