/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.DocumentType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author PSM
 */
public class DocumentTypeImpl extends NodeImpl implements DocumentType {
       
    private final String name;
    private final NamedNodeMapImpl entities;
    private final NamedNodeMapImpl notations;
    private final String publicId;
    private final String systemId;
    private final String internalSubset;
    private  DocumentImpl holderDocucment;

    public DocumentTypeImpl(String name, String publicId, String systemId, String internalSubset, DocumentImpl  holderDocument) {
        super(name, DOCUMENT_TYPE_NODE, holderDocument);
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
        this.internalSubset = internalSubset;
        this.holderDocument = holderDocucment;
        
        this.entities = new NamedNodeMapImpl(new LinkedHashMap<>());
        this.notations = new NamedNodeMapImpl(new LinkedHashMap<>());
    }

    // Retourne le nom du type de document
    @Override
    public String getName() {
        return name;
    }

    // Retourne les entités déclarées dans la DTD
    @Override
    public NamedNodeMapImpl getEntities() {
        return entities;
    }

    // Retourne les notations déclarées
    @Override
    public NamedNodeMapImpl getNotations() {
        return notations;
    }

    // Identifiant public de la DTD
    @Override
    public String getPublicId() {
        return publicId;
    }

    // Identifiant système (URL) de la DTD
    @Override
    public String getSystemId() {
        return systemId;
    }

    // Sous-ensemble interne (ex: définitions internes dans <!DOCTYPE ... [ ... ]>)
    @Override
    public String getInternalSubset() {
        return internalSubset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<!DOCTYPE ").append(getName());
        if (publicId != null) sb.append(" PUBLIC \"").append(publicId).append("\"");
        if (systemId != null) sb.append(" \"").append(systemId).append("\"");
        if (internalSubset != null) sb.append(" [").append(internalSubset).append("]");
        sb.append(">");
        return sb.toString();

    }
}