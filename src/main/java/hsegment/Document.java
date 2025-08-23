/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.AttributeImpl;
import DOM.DocumentImpl;
import DOM.ElementImpl;
import DOM.NodeImpl;
import Interface.*;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

    /**
     *
     * @author PSM
     */
    public interface Document {
    
         /**
     * The Document Type Declaration (see <code>DocumentType</code>)
     * associated with this document.For XML documents without a document
 type declaration this returns <code>null</code>. For HTML documents,
 a <code>DocumentType</code> object may be returned, independently of
     * the presence or absence of document type declaration in the HTML
     * document.
     * <br>This provides direct access to the <code>DocumentType</code> node,
     * child node of this <code>Document</code>. This node can be set at
     * document creation time and later changed through the use of child
     * nodes manipulation methods, such as <code>Node.insertBefore</code>,
     * or <code>Node.replaceChild</code>. Note, however, that while some
     * implementations may instantiate different types of
     * <code>Document</code> objects supporting additional features than the
     * "Core", such as "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , based on the <code>DocumentType</code> specified at creation time,
     * changing it afterwards is very unlikely to result in a change of the
     * features supported.
     *
     * @return 
     * @since 1.4, DOM Level 3
     */
    public DocumentType getDoctype();
        
    /**     
     * @param doctype 
     */
    public void setDoctype(DocumentType doctype);
    
    /**
     * The <code>DOMImplementation</code> object that handles this document. A
     * DOM application may use objects from multiple implementations.
     */

    
     /**
     * This is a convenience attribute that allows direct access to the child
     * node that is the document element of the document.
     * @return 
     */
    public ElementImpl getDocumentElement();

    /**
     * Creates an element of the type specified.Note that the instance
 returned implements the <code>Element</code> interface, so attributes
 can be specified directly on the returned object.<br>In addition, if there are known attributes with default values,
     * <code>Attr</code> nodes representing them are automatically created
     * and attached to the element.
     * <br>To create an element with a qualified name and namespace URI, use
     * the <code>createElementNS</code> method.
     * @param tagName The name of the element type to instantiate. For XML,
     *   this is case-sensitive, otherwise it depends on the
     *   case-sensitivity of the markup language in use. In that case, the
     *   name is mapped to the canonical form of that markup by the DOM
     *   implementation.
     * @param holderDocument
     * @return A new <code>Element</code> object with the
     *   <code>nodeName</code> attribute set to <code>tagName</code>, and
     *   <code>localName</code>, <code>prefix</code>, and
     *   <code>namespaceURI</code> set to <code>null</code>.
     * @exception HJAXException
     *   INVALID_CHARACTER_ERR: Raised if the specified name is not an XML
     *   name according to the XML version in use specified in the
     *   <code>Document.xmlVersion</code> attribute.
     */
    public ElementImpl createElement(String tagName, Document holderDocument)
                                 throws HJAXException;

       /**
     * Creates a <code>Text</code> node given the specified string.
     * @param data The data for the node.
     * @return The new <code>Text</code> object.
     */
    public Text createTextNodeImpl(String data);
  
     /**
     * Creates an <code>Attr</code> of the given name.Note that the
    <code>Attr</code> instance can then be set on an <code>Element</code>
 using the <code>setAttributeNodeImpl</code> method.<br>To create an attribute with a qualified name and namespace URI, use
     * the <code>createAttributeNS</code> method.
     * @param name The name of the attribute.
     * @param value
     * @param holderDocument
     * @param specified
     * @param holderElement
     * @return A new <code>Attr</code> object with the <code>nodeName</code>
     *   attribute set to <code>name</code>, and <code>localName</code>,
     *   <code>prefix</code>, and <code>namespaceURI</code> set to
     *   <code>null</code>. The value of the attribute is the empty string.
     * @exception HJAXException
     *   INVALID_CHARACTER_ERR: Raised if the specified name is not an XML
     *   name according to the XML version in use specified in the
     *   <code>Document.xmlVersion</code> attribute.
     */
    public AttributeImpl createAttribute(String name, String value, DocumentImpl holderDocument, 
            boolean specified, ElementImpl holderElement)
                                throws HJAXException;

    /**
     * Returns a <code>NodeImplList</code> of all the <code>Elements</code> in
     * document order with a given tag name and are contained in the
     * document.
     * @param tagname  The name of the tag to match on. The special value "*"
     *   matches all tags. For XML, the <code>tagname</code> parameter is
     *   case-sensitive, otherwise it depends on the case-sensitivity of the
     *   markup language in use.
     * @return A new <code>NodeImplList</code> object containing all the matched
     *   <code>Elements</code>.
     */
    public NodeImpl getElementsByTagName(String tagname);

    
    /**
     * Supprime un Element et prend en paramètre un boolean pour déterminer si
     * les élements enfants seront également supprimé.Si le paramètre 
     * <code>deep</code> est passé à true les élement enfants
     * seront également supprimer si il est passé à false il faudra indiqué 
     * le noeud auquel les éléments enfants seront rattachés.
     * @param refNode                
     * @param newParent                
     * @return 
     */
      public NodeImpl removeNode(NodeImpl refNode, NodeImpl newParent );
   
    /**
     * Returns the <code>Element</code> that has an ID attribute with the
     * given value. If no such element exists, this returns <code>null</code>
     * . If more than one element has an ID attribute with that value, what
     * is returned is undefined.
     * <br> The DOM implementation is expected to use the attribute
     * <code>Attr.isId</code> to determine if an attribute is of type ID.
     * <p ><b>Note:</b> Attributes with the name "ID" or "id" are not of type
     * ID unless so defined.
     * @param elementId The unique <code>id</code> value for an element.
     * @return The matching element or <code>null</code> if there is none.
     * @since 1.4, DOM Level 2
     */
    public ElementImpl getElementById(String elementId);

  
    
    /**
     * Rename an existing node of type <code>ELEMENT_NODE</code> or
     * <code>ATTRIBUTE_NODE</code>.
     * <br>When possible this simply changes the name of the given node,
     * otherwise this creates a new node with the specified name and
     * replaces the existing node with the new node as described below.
     * <br>If simply changing the name of the given node is not possible, the
     * following operations are performed: a new node is created, any
     * registered event listener is registered on the new node, any user
     * data attached to the old node is removed from that node, the old node
     * is removed from its parent if it has one, the children are moved to
     * the new node, if the renamed node is an <code>Element</code> its
     * attributes are moved to the new node, the new node is inserted at the
     * position the old node used to have in its parent's child nodes list
     * if it has one, the user data that was attached to the old node is
     * attached to the new node.
     * <br>When the node being renamed is an <code>Element</code> only the
     * specified attributes are moved, default attributes originated from
     * the DTD are updated according to the new element name. In addition,
     * the implementation may update default attributes from other schemas.
     * Applications should use <code>Document.normalizeDocument()</code> to
     * guarantee these attributes are up-to-date.
     * <br>When the node being renamed is an <code>Attr</code> that is
     * attached to an <code>Element</code>, the node is first removed from
     * the <code>Element</code> attributes map. Then, once renamed, either
     * by modifying the existing node or creating a new one as described
     * above, it is put back.
     * <br>In addition,
     * <ul>
     * <li> a user data event <code>NODE_RENAMED</code> is fired,
     * </li>
     * <li>
     * when the implementation supports the feature "MutationNameEvents",
     * each mutation operation involved in this method fires the appropriate
     * event, and in the end the event {
     * <code>http://www.w3.org/2001/xml-events</code>,
     * <code>DOMElementNameChanged</code>} or {
     * <code>http://www.w3.org/2001/xml-events</code>,
     * <code>DOMAttributeNameChanged</code>} is fired.
     * </li>
     * </ul>
     * @param n The node to rename.
     * @param namespaceURI The new namespace URI.
     * @param qualifiedName The new qualified name.
     * @return The renamed node. This is either the specified node or the new
     *   node that was created to replace the specified node.
     * @exception HJAXException
     *   NOT_SUPPORTED_ERR: Raised when the type of the specified node is
     *   neither <code>ELEMENT_NODE</code> nor <code>ATTRIBUTE_NODE</code>,
     *   or if the implementation does not support the renaming of the
     *   document element.
     *   <br>INVALID_CHARACTER_ERR: Raised if the new qualified name is not an
     *   XML name according to the XML version in use specified in the
     *   <code>Document.xmlVersion</code> attribute.
     *   <br>WRONG_DOCUMENT_ERR: Raised when the specified node was created
     *   from a different document than this document.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is a
     *   malformed qualified name, if the <code>qualifiedName</code> has a
     *   prefix and the <code>namespaceURI</code> is <code>null</code>, or
     *   if the <code>qualifiedName</code> has a prefix that is "xml" and
     *   the <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *   http://www.w3.org/XML/1998/namespace</a>" [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     *   . Also raised, when the node being renamed is an attribute, if the
     *   <code>qualifiedName</code>, or its prefix, is "xmlns" and the
     *   <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>".
     * @since 1.5, DOM Level 3
     */
    public NodeImpl renameNodeImpl(NodeImpl n,
                           String namespaceURI,
                           String qualifiedName)
                           throws HJAXException;
    
}
