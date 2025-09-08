/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.EntityImpl;

/**
 *
 * @author FIDELE
 */
public interface Entity {
    
     /**
     * The public identifier associated with the entity if specified, and
     * <code>null</code> otherwise.L'identifiant public associé à l'entité si spécidié, et null sinon.il permet au parseur de reconnaitre la DTD
     * @param entity
     * @return 
     */
    public String getPublicId(EntityImpl entity);

    /**
     * The system identifier associated with the entity if specified, and
     * <code>null</code> otherwise.This may be an absolute URI or not.L'identifiant public associé à l'entité si spécidié, et null sinon. Cela peut être un URI absolu ou non.
 Spécifie l'emplacement physique de la DTD
     * @param entity
     * @return 
     */
    public String getSystemId(EntityImpl entity);

    /**
     * For unparsed entities, the name of the notation for the entity.For
 parsed entities, this is <code>null</code>.retourne le nom de la notaion de l'entité pour les entités non analysées
 et null pour les entités analysées.
     * @param entity
     * @return 
     */
    public String getNotationName(EntityImpl entity);

    /**
     * An attribute specifying the encoding used for this entity at the time
     * of parsing, when it is an external parsed entity.This is
    <code>null</code> if it an entity from the internal subset or if it
 is not known.un attribut spécifiant l'encodage utilisé pour cette entité au moment de
 l'analyse lorsqu'il s'agit d'une entité analysée externe et null si elle
 n'est pas connue ou si il s'agit d'une entité du sous ensemble interne.
     * @param entity
     * @return 
     * @since 1.5, DOM Level 3
     */
    public String getInputEncoding(EntityImpl entity);

    /**
     * An attribute specifying, as part of the text declaration, the encoding
     * of this entity, when it is an external parsed entity.This is
    <code>null</code> otherwise.
     * @param entity
     * @return 
     * @since 1.5, DOM Level 3
     */
    public String getXmlEncoding(EntityImpl entity);

    /**
     * An attribute specifying, as part of the text declaration, the version
     * number of this entity, when it is an external parsed entity.This is
    <code>null</code> otherwise.
     * @param entity
     * @return 
     * @since 1.5, DOM Level 3
     */
    public String getXmlVersion(EntityImpl entity);
    
}
