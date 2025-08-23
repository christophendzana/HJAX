/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.NamedNodeMapImpl;

/**
 *
 * @author PSM
 */
public interface DocumentType extends Node {
    
    /**
     * The name of DTD; i.e., the name immediately following the
     * <code>DOCTYPE</code> keyword.
     */
    public String getName();

    /**
     * A <code>NamedNodeMap</code> containing the general entities, both
     * external and internal, declared in the DTD. Parameter entities are
     * not contained. Duplicates are discarded. For example in:
     * <pre>&lt;!DOCTYPE
     * ex SYSTEM "ex.dtd" [ &lt;!ENTITY foo "foo"&gt; &lt;!ENTITY bar
     * "bar"&gt; &lt;!ENTITY bar "bar2"&gt; &lt;!ENTITY % baz "baz"&gt;
     * ]&gt; &lt;ex/&gt;</pre>
     *  the interface provides access to <code>foo</code>
     * and the first declaration of <code>bar</code> but not the second
     * declaration of <code>bar</code> or <code>baz</code>. Every node in
     * this map also implements the <code>Entity</code> interface.
     * <br>The DOM Level 2 does not support editing entities, therefore
     * <code>entities</code> cannot be altered in any way.
     */
    public NamedNodeMapImpl getEntities();

    /**
     * A <code>NamedNodeMap</code> containing the notations declared in the
     * DTD. Duplicates are discarded. Every node in this map also implements
     * the <code>Notation</code> interface.
     * <br>The DOM Level 2 does not support editing notations, therefore
     * <code>notations</code> cannot be altered in any way.
     */
    public NamedNodeMapImpl getNotations();

    /**
     * The public identifier of the external subset.
     * @since 1.4, DOM Level 2
     */
    public String getPublicId();

    /**
     * The system identifier of the external subset. This may be an absolute
     * URI or not.
     * @since 1.4, DOM Level 2
     */
    public String getSystemId();

    /**
     * The internal subset as a string, or <code>null</code> if there is none.
     * This is does not contain the delimiting square brackets.
     * <p ><b>Note:</b> The actual content returned depends on how much
     * information is available to the implementation. This may vary
     * depending on various parameters, including the XML processor used to
     * build the document.
     * @since 1.4, DOM Level 2
     */
    public String getInternalSubset();
    
}
