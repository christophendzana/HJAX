/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.NodeImpl;
import DOM.NodeListImpl;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public interface DocumentNode {

    // NodeType
    /**
     * The node is an <code>Element</code>.
     */
    public static final short ELEMENT_NODE = 1;
    /**
     * The node is an <code>Attr</code>.
     */
    public static final short ATTRIBUTE_NODE = 2;
    /**
     * The node is a <code>Text</code> node.
     */
    public static final short TEXT_NODE = 3;
    /**
     * The node is a <code>CDATASection</code>.
     */
    public static final short CDATA_SECTION_NODE = 4;
    /**
     * The node is an <code>EntityReference</code>.
     */
    public static final short ENTITY_REFERENCE_NODE = 5;
    /**
     * The node is an <code>Entity</code>.
     */
    public static final short ENTITY_NODE = 6;
    /**
     * The node is a <code>ProcessingInstruction</code>.
     */
    public static final short PROCESSING_INSTRUCTION_NODE = 7;
    /**
     * The node is a <code>Comment</code>.
     */
    public static final short COMMENT_NODE = 8;
    /**
     * The node is a <code>Document</code>.
     */
    public static final short DOCUMENT_NODE = 9;
    /**
     * The node is a <code>DocumentType</code>.
     */
    public static final short DOCUMENT_TYPE_NODE = 10;
    /**
     * The node is a <code>DocumentFragment</code>.
     */
    public static final short DOCUMENT_FRAGMENT_NODE = 11;
    /**
     * The node is a <code>Notation</code>.
     */
    public static final short NOTATION_NODE = 12;

   
    public String getNodeValue(NodeImpl node)
            throws HJAXException;

    /**
     * La valeur de ce nœud, selon son type ; voir le tableau ci-dessus.Lorsqu'il est défini comme <code>null</code>, sa définition n'a aucun
 effet, y compris si le nœud est en lecture seule.
     *
     * @param node
     * @param nodeValue
     * @exception HJAXException NO_MODIFICATION_ALLOWED_ERR : déclenché lorsque
     * le nœud est en lecture seule et s'il n'est pas défini comme
     * <code>null</code>.
     */
    public void setNodeValue(NodeImpl node, String nodeValue)
            throws HJAXException;


    /**
     * Le parent de ce nœud.Tous les nœuds, sauf <code>Attr</code>,
    <code>Document</code>, <code>DocumentFragment</code>, <code>Entity</code>
 et <code>Notation</code>, peuvent avoir un parent.Cependant, si un nœud
 vient d'être créé et n'a pas encore été ajouté à l'arborescence, ou s'il
 en a été supprimé, il est <code>null</code>.
     * @param node
     * @return 
     */
    public NodeImpl getParentNode(NodeImpl node);

    /**
     * Une <code>NodeList</code> contenant tous les enfants de ce nœud.S'il n'y
 a pas d'enfants, il s'agit d'une <code>NodeList</code> ne contenant aucun
 nœud.
     * @param node
     * @return 
     */
    public NodeListImpl getChildNodes(NodeImpl node);

    /**
     * Le premier enfant de ce nœud.S'il n'existe pas de , cette valeur renvoie
    <code>null</code>.
     * @param node
     * @return 
     */
    public NodeImpl getFirstChild(NodeImpl node);

    /**
     * Le dernier enfant de ce nœud.S'il n'existe pas de nœud , cette valeur
 renvoie <code>null</code>.
     * @param node
     * @return 
     */
    public NodeImpl getLastChild(NodeImpl node);

    /**
     * The node immediately preceding this node.If there is no such node, this
 returns <code>null</code>.
     * @param node
     * @return 
     */
    public NodeImpl getPreviousSibling(NodeImpl node);

    /**
     * The node immediately following this node.If there is no such node, this
 returns <code>null</code>.
     * @param node
     * @return 
     */
    public NodeImpl getNextSibling(NodeImpl node);    

    /**
     * Insère le nœud <code>newChild</code> avant le nœud enfant existant
     * <code>refChild</code>.Si <code>refChild</code> est <code>null</code>,
 insère <code>newChild</code> à la fin de la liste des enfants.<br>Si <code>newChild</code> est un objet <code>DocumentFragment</code>,
     * tous ses enfants sont insérés, dans le même ordre, avant
     * <code>refChild</code>. Si <code>newChild</code> est déjà dans
     * l'arborescence, il est d'abord supprimé.
     * <p ><b>Remarque :</b> L'insertion d'un nœud avant lui-même dépend de
     * l'implémentation.
     *
     * @param refNode
     * @param newChild Le nœud à insérer.
     * @param refChild Le nœud de référence, c'est-à-dire le nœud avant lequel
     * le nouveau nœud doit être inséré.
     * @return Le nœud en cours d'insertion.
     * @exception HJAXException HIERARCHY_REQUEST_ERR : déclenchée si ce nœud
     * est d’un type qui n’autorise pas les enfants du type du nœud
     * <code>newChild</code>, ou si le nœud à insérer est l’un de ses ancêtres
     * ou le nœud lui-même, ou si ce nœud est de type <code>Document</code> et
     * que l’application DOM tente d’insérer un deuxième nœud
     * <code>DocumentType</code> ou <code>Element</code>.
     * <br>WRONG_DOCUMENT_ERR : déclenchée si <code>newChild</code> a été créé à
     * partir d’un document différent de celui qui a créé ce nœud.
     * <br>NO_MODIFICATION_ALLOWED_ERR : déclenchée si ce nœud est en lecture
     * seule ou si le parent du nœud inséré est en lecture seule.
     * <br>NOT_FOUND_ERR : déclenchée si <code>refChild</code> n’est pas un
     * enfant de ce nœud.
     * <br>NON_SUPPORTÉ_ERR : si ce nœud est de type <code>Document</code>,
     * cette exception peut être levée si l'implémentation DOM ne prend pas en
     * charge l'insertion d'un nœud <code>DocumentType</code> ou
     * <code>Element</code>.
     *
     * @since 1.4, DOM Level 3
     */
    public NodeImpl insertBefore(NodeImpl refNode, NodeImpl newChild, NodeImpl refChild) throws HJAXException;

    /**
     * Remplace le nœud enfant <code>oldChild</code> par <code>newChild</code>
     * dans la liste des enfants et renvoie le nœud <code>oldChild</code>.
     * <br>Si <code>newChild</code> est un objet <code>DocumentFragment</code>,
     * <code>oldChild</code> est remplacé par tous les enfants
     * <code>DocumentFragment</code>, qui sont insérés dans le même ordre. Si le
     * <code>newChild</code> est déjà présent dans l'arborescence, il est
     * d'abord supprimé.
     * <p ><b>Remarque :</b> Le remplacement d'un nœud par lui-même dépend de
     * l'implémentation.
     *
     * @param newChild Le nouveau nœud à ajouter à la liste des enfants.
     * @param oldChild Le nœud remplacé dans la liste.
     * @return Le nœud remplacé.
     * @exception HJAXException HIERARCHY_REQUEST_ERR : déclenchée si ce nœud
     * est d'un type qui n'autorise pas les enfants du type du nœud
     * <code>newChild</code>, ou si le nœud à insérer est l'un de ses ancêtres
     * ou le nœud lui-même, ou si ce nœud est de type <code>Document</code> et
     * que le résultat de l'opération de remplacement ajoute un deuxième
     * <code>DocumentType</code> ou <code>Element</code> sur le nœud
     * <code>Document</code>.
     * <br>WRONG_DOCUMENT_ERR : déclenchée si <code>newChild</code> a été créé à
     * partir d'un document différent de celui qui a créé ce nœud.
     * <br>NO_MODIFICATION_ALLOWED_ERR : déclenchée si ce nœud ou le parent du
     * nouveau nœud est en lecture seule.
     * <br>NOT_FOUND_ERR : déclenché si <code>oldChild</code> n'est pas un
     * enfant de ce nœud.
     * <br>NOT_SUPPORTED_ERR : si ce nœud est de type <code>Document</code>,
     * cette exception peut être déclenchée si l'implémentation DOM ne prend pas
     * en charge le remplacement de l'enfant <code>DocumentType</code> ou de
     * l'enfant <code>Element</code>.
     *
     * @since 1.4, DOM Level 3
     */
    public NodeImpl replaceChild(NodeImpl refNode, NodeImpl newChild, NodeImpl oldChild) throws HJAXException;

    /**
     * Supprime le nœud enfant indiqué par <code>oldChild</code> de la liste des
     * enfants et le renvoie.
     *
     * @param refNode
     * @param oldChild Le nœud à supprimer.
     * @return Le nœud supprimé.
     * @exception HJAXException NO_MODIFICATION_ALLOWED_ERR : déclenchée si ce
     * nœud est en lecture seule.
     * <br>NOT_FOUND_ERR : déclenchée si <code>oldChild</code> n'est pas un
     * enfant de ce nœud.
     * <br>NOT_SUPPORTED_ERR : si ce nœud est de type <code>Document</code>,
     * cette exception peut être déclenchée si l'implémentation DOM ne prend pas
     * en charge la suppression de l'enfant <code>DocumentType</code> ou de
     * l'enfant <code>Element</code>.
     * @since 1.4, DOM Level 3
     */
    public NodeImpl removeChild(NodeImpl refNode, NodeImpl oldChild) throws HJAXException;

    /**
     * Ajoute le nœud <code>newChild</code> à la fin de la liste des enfants de
     * ce nœud.Si le <code>newChild</code> est déjà présent dans
 l'arborescence, il est d'abord supprimé.
     *
     * @param refnode
     * @param newChild Le nœud à ajouter. S'il s'agit d'un objet
     * <code>DocumentFragment</code>, l'intégralité du contenu du fragment de
     * document est déplacée dans la liste des enfants de ce nœud.
     * @return Le nœud ajouté.
     * @exception HJAXException HIERARCHY_REQUEST_ERR : déclenchée si ce nœud
     * est d’un type qui n’autorise pas les enfants du type du nœud
     * <code>newChild</code>, ou si le nœud à ajouter est l’un de ses ancêtres
     * ou le nœud lui-même, ou si ce nœud est de type <code>Document</code> et
     * que l’application DOM tente d’ajouter un deuxième nœud
     * <code>DocumentType</code> ou <code>Element</code>.
     * <br>WRONG_DOCUMENT_ERR : déclenchée si <code>newChild</code> a été créé à
     * partir d’un document différent de celui qui a créé ce nœud.
     * <br>NO_MODIFICATION_ALLOWED_ERR : déclenchée si ce nœud est en lecture
     * seule ou si le parent précédent du nœud inséré est en lecture seule.
     * <br>NOT_SUPPORTED_ERR : si le nœud <code>newChild</code> est un enfant du
     * nœud <code>Document</code>, cette exception peut être levée si
     * l'implémentation DOM ne prend pas en charge la suppression de l'enfant
     * <code>DocumentType</code> ou de l'enfant <code>Element</code>.
     * @since 1.4, DOM Level 3
     */
    public NodeImpl appendChild(NodeImpl refnode, NodeImpl newChild)
            throws HJAXException;
    /**
     * Renvoie si ce nœud a des enfants.
     *
     * @return Renvoie <code>true</code> si ce nœud a des enfants,
     * <code>false</code> sinon.
     */
    public boolean hasChildNodes(NodeImpl node);

    /**
     * Renvoie un doublon de ce nœud, c'est-à-dire qu'il sert de constructeur de
     * copie générique pour les nœuds.Le nœud dupliqué n'a pas de parent
 (  <code>parentNode</code> est <code>null</code>) ni de données
 utilisateur. Les données utilisateur associées au nœud importé ne sont
 pas reportées. Cependant, si des <code>UserDataHandlers</code> ont été
     * spécifiés avec les données associées, ces gestionnaires seront appelés
     * avec les paramètres appropriés avant le retour de cette méthode.
     * <br>Le clonage d'un <code>Element</code> copie tous les attributs et
     * leurs valeurs, y compris ceux générés par le processeur XML pour
     * représenter les attributs par défaut, mais cette méthode ne copie aucun
     * enfant qu'il contient, sauf s'il s'agit d'un clone profond. Cela inclut
     * le texte contenu dans l'<code>Element</code>, car il est contenu dans un
     * nœud enfant <code>Text</code> . Cloner un <code>Attr</code> directement,
     * par opposition à un clonage dans le cadre d'un clonage
     * d'<code>Element</code> Opération : renvoie un attribut spécifié
     * (<code>specified</code> est <code>true</code>). Cloner un
     * <code>Attr</code> clone toujours ses enfants, car ils représentent sa
     * valeur, qu'il s'agisse d'un clone profond ou non. Cloner une
     * <code>EntityReference</code> construit automatiquement son sous-arbre si
     * une <code>Entity</code> correspondante est disponible, qu'il s'agisse
     * d'un clone profond ou non. Cloner tout autre type de nœud renvoie
     * simplement une copie de ce nœud.
     * <br>Notez que le clonage d'un sous-arbre immuable produit une copie
     * mutable, mais les enfants d'un clone <code>EntityReference</code> sont en
     * lecture seule. De plus, les clones de nœuds <code>Attr</code> non
     * spécifiés sont spécifiés. De plus, le clonage des nœuds
     * <code>Document</code>, <code>DocumentType</code>, <code>Entity</code> et
     * <code>Notation</code> dépend de l'implémentation.
     *
     * @param node
     * @param deep Si <code>true</code>, clone récursivement le sous-arbre sous
     * le nœud spécifié ; si <code>false</code>, clone uniquement le nœud
     * lui-même (et ses attributs, s'il s'agit d'un <code>Element</code>).
     * @return Le nœud dupliqué.
     */
    public NodeImpl cloneNode(NodeImpl node, boolean deep);
    /**
     * Returns whether this node (if it is an element) has any attributes.
     *
     * @param node
     * @return Returns <code>true</code> if this node has any attributes,
     * <code>false</code> otherwise.
     *
     * @since 1.4, DOM Level 2
     */
    public boolean hasAttributes(NodeImpl node);

    // DocumentPosition
    /**
     * The two nodes are disconnected. Order between disconnected nodes is
     * always implementation-specific.
     */
    public static final short DOCUMENT_POSITION_DISCONNECTED = 0x01;
    /**
     * The second node precedes the reference node.
     */
    public static final short DOCUMENT_POSITION_PRECEDING = 0x02;
    /**
     * The node follows the reference node.
     */
    public static final short DOCUMENT_POSITION_FOLLOWING = 0x04;
    /**
     * The node contains the reference node. A node which contains is always
     * preceding, too.
     */
    public static final short DOCUMENT_POSITION_CONTAINS = 0x08;
    /**
     * The node is contained by the reference node. A node which is contained is
     * always following, too.
     */
    public static final short DOCUMENT_POSITION_CONTAINED_BY = 0x10;
    /**
     * The determination of preceding versus following is
     * implementation-specific.
     */
    public static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 0x20;

    /**
     * Compares the reference node, i.e. the node on which this method is being
     * called, with a node, i.e. the one passed as a parameter, with regard to
     * their position in the document and according to the document order.
     *
     * @param other The node to compare against the reference node.
     * @return Returns how the node is positioned relatively to the reference
     * node.
     * @exception HJAXException NOT_SUPPORTED_ERR: when the compared nodes are
     * from different DOM implementations that do not coordinate to return
     * consistent implementation-specific results.
     *
     * @since 1.5, DOM Level 3
     */
    public short compareDocumentPosition(NodeImpl other)
            throws HJAXException;

    /**
     * Cet attribut renvoie le contenu textuel de ce nœud et de ses descendants.Lorsqu'il est défini comme <code>null</code>, sa définition n'a aucun
 effet.Lors de sa définition, tous les enfants potentiels de ce nœud sont
 supprimés et, si la nouvelle chaîne n'est pas vide ou <code>null</code>,
 elle est remplacée par un seul nœud <code>Text</code> contenant la chaîne
 définie pour cet attribut.<br> Lors de la récupération, aucune sérialisation n'est effectuée ; la
     * chaîne renvoyée ne contient aucun balisage. Aucune normalisation des
     * espaces n'est effectuée et la chaîne renvoyée ne contient pas les espaces
     * du contenu de l'élément (voir l'attribut
     * <code>Text.isElementContentWhitespace</code>). De même, lors de sa
     * définition, aucune analyse n'est effectuée ; la chaîne d'entrée est
     * considérée comme du contenu textuel pur.
     * <br>La chaîne renvoyée est composée du contenu textuel de ce nœud, selon
     * son type, comme défini ci-dessous :
     * <table class="striped">
     * <caption style="display:none">Node/Content table</caption>
     * <thead>
     * <tr>
     * <th scope="col">Node type</th>
     * <th scope="col">Content</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <th scope="row">
     * ELEMENT_NODE, ATTRIBUTE_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE,
     * DOCUMENT_FRAGMENT_NODE</th>
     * <td>concatenation of the <code>textContent</code> attribute value of
     * every child node, excluding COMMENT_NODE and PROCESSING_INSTRUCTION_NODE
     * nodes. This is the empty string if the node has no children.</td>
     * </tr>
     * <tr>
     * <th scope="row">TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE,
     * PROCESSING_INSTRUCTION_NODE</th>
     * <td><code>nodeValue</code></td>
     * </tr>
     * <tr>
     * <th scope="row">DOCUMENT_NODE, DOCUMENT_TYPE_NODE, NOTATION_NODE</th>
     * <td><em>null</em></td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param node
     * @return 
     * @exception HJAXException DOMSTRING_SIZE_ERR: Raised when it would return
     * more characters than fit in a <code>DOMString</code> variable on the
     * implementation platform.
     *
     * @since 1.5, DOM Level 3
     */
    public String getTextContent(NodeImpl node)
            throws HJAXException;

    /**
     * Cet attribut renvoie le contenu textuel de ce nœud et de ses descendants.Lorsqu'il est défini comme <code>null</code>, sa définition n'a aucun
 effet.Lors de la définition, tous les enfants potentiels de ce nœud sont
 supprimés et, si la nouvelle chaîne n'est pas vide ou <code>null</code>,
 elle est remplacée par un seul nœud <code>Text</code> contenant la chaîne
 définie pour cet attribut.<br> Lors de la récupération, aucune sérialisation n'est effectuée ; la
     * chaîne renvoyée ne contient aucun balisage. Aucune normalisation des
     * espaces n'est effectuée et la chaîne renvoyée ne contient pas les espaces
     * du contenu de l'élément (voir l'attribut
     * <code>Text.isElementContentWhitespace</code>). De même, lors de la
     * définition, aucune analyse n'est effectuée ; la chaîne d'entrée est
     * considérée comme du contenu textuel pur.
     * <br>The string returned is made of the text content of this node
     * depending on its type, as defined below:
     * <table class="striped">
     * <caption style="display:none">Node/Content table</caption>
     * <thead>
     * <tr>
     * <th scope="col">Node type</th>
     * <th scope="col">Content</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <th scope="row">
     * ELEMENT_NODE, ATTRIBUTE_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE,
     * DOCUMENT_FRAGMENT_NODE</th>
     * <td>concatenation of the <code>textContent</code> attribute value of
     * every child node, excluding COMMENT_NODE and PROCESSING_INSTRUCTION_NODE
     * nodes. This is the empty string if the node has no children.</td>
     * </tr>
     * <tr>
     * <th scope="row">TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE,
     * PROCESSING_INSTRUCTION_NODE</th>
     * <td><code>nodeValue</code></td>
     * </tr>
     * <tr>
     * <th scope="row">DOCUMENT_NODE, DOCUMENT_TYPE_NODE, NOTATION_NODE</th>
     * <td><em>null</em></td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param node
     * @param textContent
     * @exception HJAXException NO_MODIFICATION_ALLOWED_ERR: Raised when the
     * node is readonly.
     *
     * @since 1.5, DOM Level 3
     */
    public void setTextContent(NodeImpl node, String textContent)
            throws HJAXException;

    /**
     * Renvoie si ce nœud est identique au nœud donné.<br>Cette méthode permet de déterminer si deux références
    <code>Node</code> renvoyées par la référence d'implémentation sont
 identiques.Lorsque deux références <code>Node</code> sont des références
     * au même objet, même via un proxy, elles peuvent être utilisées de manière
     * totalement interchangeable, de sorte que tous les attributs aient les
     * mêmes valeurs et que l'appel de la même méthode DOM sur l'une ou l'autre
     * référence ait toujours exactement le même effet.
     *
     * @param refnode
     * @param other Le nœud à tester.
     * @return Renvoie <code>true</code> si les nœuds sont identiques,
     * <code>false</code> sinon.
     *
     * @since 1.5, DOM Level 3
     */
    public boolean isSameNode(NodeImpl refnode, NodeImpl other);

}
