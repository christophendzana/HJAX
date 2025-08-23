/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import Interface.EntityReference;

/**
 *
 * @author FIDELE
 */
public class EntityReferenceImpl extends NodeImpl implements EntityReference{
    
    public EntityReferenceImpl(String name, short nodeType, DocumentImpl holderDocument) {
        super(name, nodeType, holderDocument);
    }
    
}
