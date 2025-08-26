/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author FIDELE
 */
public class AllowedChildren {
    
    private static final Map<Short, Set<Short>> allowedChildren = new HashMap();
        
    static{
          // Document: 1 seul Element, 0-1 DocumentType, + Comment, PI
        allowedChildren.put(NodeImpl.DOCUMENT_NODE, Set.of(
                NodeImpl.ELEMENT_NODE, NodeImpl.DOCUMENT_TYPE_NODE,
                NodeImpl.COMMENT_NODE, NodeImpl.PROCESSING_INSTRUCTION_NODE
        ));

        // DocumentType: aucun enfant
        allowedChildren.put(NodeImpl.DOCUMENT_TYPE_NODE, Collections.emptySet());

        // Element: peut contenir éléments, texte, cdata, commentaire, PI, EntityRef
        allowedChildren.put(NodeImpl.ELEMENT_NODE, Set.of(
                NodeImpl.ELEMENT_NODE, NodeImpl.TEXT_NODE, NodeImpl.CDATA_SECTION_NODE,
                NodeImpl.COMMENT_NODE, NodeImpl.PROCESSING_INSTRUCTION_NODE,
                NodeImpl.ENTITY_REFERENCE_NODE
        ));

        // Attr: peut contenir uniquement Text ou EntityRef
        allowedChildren.put(NodeImpl.ATTRIBUTE_NODE, Set.of(
                NodeImpl.TEXT_NODE, NodeImpl.ENTITY_REFERENCE_NODE
        ));

        // Text, CDATA, Comment, PI, Notation: pas d'enfants
        allowedChildren.put(NodeImpl.TEXT_NODE, Collections.emptySet());
        allowedChildren.put(NodeImpl.CDATA_SECTION_NODE, Collections.emptySet());
        allowedChildren.put(NodeImpl.COMMENT_NODE, Collections.emptySet());
        allowedChildren.put(NodeImpl.PROCESSING_INSTRUCTION_NODE, Collections.emptySet());
        allowedChildren.put(NodeImpl.NOTATION_NODE, Collections.emptySet());

        // EntityReference et Entity: en théorie peuvent contenir divers enfants
        allowedChildren.put(NodeImpl.ENTITY_REFERENCE_NODE, Set.of(
                NodeImpl.ELEMENT_NODE, NodeImpl.TEXT_NODE, NodeImpl.COMMENT_NODE,
                NodeImpl.PROCESSING_INSTRUCTION_NODE, NodeImpl.CDATA_SECTION_NODE
        ));
        allowedChildren.put(NodeImpl.ENTITY_NODE, Set.of(
                NodeImpl.ELEMENT_NODE, NodeImpl.TEXT_NODE, NodeImpl.COMMENT_NODE,
                NodeImpl.PROCESSING_INSTRUCTION_NODE, NodeImpl.CDATA_SECTION_NODE,
                NodeImpl.ENTITY_REFERENCE_NODE
        ));
    }
    
    public boolean isAllowed( NodeImpl parentNode, NodeImpl childNode){
        
        Set<Short> allowed = allowedChildren.getOrDefault(parentNode.getNodeType(), Collections.emptySet());
                        
        return allowed.contains(childNode.getNodeType()) ;
    }
    
}
