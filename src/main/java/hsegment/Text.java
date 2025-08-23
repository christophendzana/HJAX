/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import org.w3c.dom.DOMException;

/** Text hérite de CharData qui hérite lui même de Node
 * <code>nodeType</code> ici renvoie <code>TEXT_NODE</node>
 * Contient uniquement du text (pas de balises)
 * @author PSM
 */
public interface Text extends CharData {
    
    /**
     * Breaks this node into two nodes at the specified <code>offset</code>,
     * keeping both in the tree as siblings. After being split, this node
     * will contain all the content up to the <code>offset</code> point. A
     * new node of the same type, which contains all the content at and
     * after the <code>offset</code> point, is returned. If the original
     * node had a parent node, the new node is inserted as the next sibling
     * of the original node. When the <code>offset</code> is equal to the
     * length of this node, the new node has no data.
     * @param offset The 16-bit unit offset at which to split, starting from
     *   <code>0</code>.
     * @return The new node, of the same type as this node.
     * @exception DOMException
     *   INDEX_SIZE_ERR: Raised if the specified offset is negative or greater
     *   than the number of 16-bit units in <code>data</code>.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public Text splitText(int offset)
                          throws DOMException;

    /**
     * Returns whether this text node contains <a href='http://www.w3.org/TR/2004/REC-xml-infoset-20040204#infoitem.character'>
     * element content whitespace</a>, often abusively called "ignorable whitespace". The text node is
     * determined to contain whitespace in element content during the load
     * of the document or if validation occurs while using
     * <code>Document.normalizeDocument()</code>.
     * @since 1.5, DOM Level 3
     */
    public boolean isElementContentWhitespace();

    /**
     * Returns all text of <code>Text</code> nodes logically-adjacent text
     * nodes to this node, concatenated in document order.
     * <br>For instance, in the example below <code>wholeText</code> on the
     * <code>Text</code> node that contains "bar" returns "barfoo", while on
     * the <code>Text</code> node that contains "foo" it returns "barfoo".
     *
     * <pre>
     *                     +-----+
     *                     | &lt;p&gt; |
     *                     +-----+
     *                       /\
     *                      /  \
     *               /-----\    +-------+
     *               | bar |    | &amp;ent; |
     *               \-----/    +-------+
     *                              |
     *                              |
     *                           /-----\
     *                           | foo |
     *                           \-----/
     * </pre>
     * <em>Figure: barTextNode.wholeText value is "barfoo"</em>
     *
     * @since 1.5, DOM Level 3
     */
    public String getWholeText();

    /**
     * Replaces the text of the current node and all logically-adjacent text
     * nodes with the specified text. All logically-adjacent text nodes are
     * removed including the current node unless it was the recipient of the
     * replacement text.
     * <p>This method returns the node which received the replacement text.
     * The returned node is:</p>
     * <ul>
     * <li><code>null</code>, when the replacement text is
     * the empty string;
     * </li>
     * <li>the current node, except when the current node is
     * read-only;
     * </li>
     * <li> a new <code>Text</code> node of the same type (
     * <code>Text</code> or <code>CDATASection</code>) as the current node
     * inserted at the location of the replacement.
     * </li>
     * </ul>
     * <p>For instance, in the above example calling
     * <code>replaceWholeText</code> on the <code>Text</code> node that
     * contains "bar" with "yo" in argument results in the following:</p>
     *
     * <pre>
     *                     +-----+
     *                     | &lt;p&gt; |
     *                     +-----+
     *                        |
     *                        |
     *                     /-----\
     *                     | yo  |
     *                     \-----/
     * </pre>
     * <em>Figure: barTextNode.replaceWholeText("yo") modifies the
     * textual content of barTextNode with "yo"</em>
     *
     * <p>Where the nodes to be removed are read-only descendants of an
     * <code>EntityReference</code>, the <code>EntityReference</code> must
     * be removed instead of the read-only nodes. If any
     * <code>EntityReference</code> to be removed has descendants that are
     * not <code>EntityReference</code>, <code>Text</code>, or
     * <code>CDATASection</code> nodes, the <code>replaceWholeText</code>
     * method must fail before performing any modification of the document,
     * raising a <code>DOMException</code> with the code
     * <code>NO_MODIFICATION_ALLOWED_ERR</code>.</p>
     * <p>For instance, in the example below calling
     * <code>replaceWholeText</code> on the <code>Text</code> node that
     * contains "bar" fails, because the <code>EntityReference</code> node
     * "ent" contains an <code>Element</code> node which cannot be removed.</p>
     * @param content The content of the replacing <code>Text</code> node.
     * @return The <code>Text</code> node created with the specified content.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if one of the <code>Text</code>
     *   nodes being replaced is readonly.
     * @since 1.5, DOM Level 3
     */
    public Text replaceWholeText(String content)
                                 throws DOMException;
    
}
