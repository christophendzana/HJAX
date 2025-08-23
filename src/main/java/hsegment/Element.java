/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.AttributeImpl;
import DOM.ElementImpl;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;

/**
 *
 * @author PSM
 */
public interface Element {

    /**
     * Renvoie le nom de l'Element
     *
     * @return a String
     */
    public String getTagName();

    /**
     * Récupère la valeur d'un attribut.
     *
     * @param name Le nom de l'attribut à retrouver.
     * @return la valeur de <code>l'Attributeibut</code> sous forme de chaine,
     * ou de chaîne vide si cet attribut n'a pas de valeur spécifiéé
     */
    public String getAttribute(String name);

    /**
     * Ajoute un nouvel attribut.Si il existe un attribut portant le nom passé
     * en paramètre est déjà présent dans l'élément sa valeur est changée par la
     * nouvelle valeur passée en paramètre. La valeur est un <code>String</code>
     *
     * @param name Le nom de l'attribut à retrouver.
     * @param value
     */
    public void setAttribute(String name, String value) throws HJAXException;

    /**
     * Supprime l'attribut dont le nom est fourni est fourni en paramètre. Si
     * c'est un attribut Obligatoire la méthode doit renvoyer une exception.
     *
     * @param name The name of the attribute to remove.
     */
    public void removeAttribute(String name) throws HJAXException;

    /**
     * Renvoie un noeud attribut dont le nom est passé en paramètre.
     *
     * @param name Le nom (<code>nodeName</code>) de l'attribut à renvoyer.
     * @return Le noeud <code>Attribute</code> qui a été spécifié en paramètre (
     * <code>nodeName</code>) ou <code>null</code> si l'attribut n'existe pas.
     */
    public AttributeImpl getAttributeNode(String name);

    /**
     * Ajoute un nouveau nœud d'attribut. Si un attribut portant ce nom (
     * <code>nodeName</code>) est déjà présent dans l'élément, il est remplacé
     * par le nouveau. Remplacer un nœud d'attribut seul n'a aucun effet. Pour
     * ajouter un nouveau nœud d'attribut avec un nom qualifié et un espace de
     * noms URI, utilisez la méthode <code>setAttributeibuteNodeNS</code>.
     *
     * @param newAttribute : le nœud <code>Attribute</code> à ajouter à la liste
     * des attributs.
     * @return : si l'attribut <code>newAttribute</code> remplace un attribut
     * existant, le nœud <code>Attribute</code> remplacé est renvoyé. sinon, la
     * valeur <code>null</code> est renvoyée.
     * @exception HJAXException WRONG_DOCUMENT_ERR : déclenché si
     * <code>newAttribute</code> a été créé à partir d'un document différent de
     * celui qui a créé l'élément.
     * <br>NO_MODIFICATION_ALLOWED_ERR : déclenché si ce nœud est en lecture
     * seule.
     * <br>INUSE_ATTRIBUTE_ERR : déclenché si <code>newAttribute</code> est déjà
     * un attribut d'un autre objet <code>Element</code>. L'utilisateur DOM doit
     * cloner explicitement les nœuds <code>Attribute</code> pour les réutiliser
     * dans d'autres éléments.
     */
    public AttributeImpl setAttributeNode(AttributeImpl newAttribute) throws HJAXException;

    /**
     * Retourne le nombre d'enfant     
     */
    public int getChildCount();  
    
    /**
     * Retourne le premier enfant de type Element
     * @return 
     */    
    public ElementImpl getFirstElementChild();
    
    /**
     * Supprime le nœud d'attribut spécifié. Si une valeur par défaut pour le
     * nœud <code>Attribute</code> supprimé est définie dans la DTD, un nouveau
     * nœud apparaît immédiatement avec la valeur par défaut, ainsi que l'URI de
     * l'espace de noms, le nom local et le préfixe correspondants, le cas
     * échéant. L'implémentation peut gérer les valeurs par défaut d'autres
     * schémas de la même manière, mais les applications doivent utiliser
     * <code>Document.normalizeDocument()</code> pour garantir que ces
     * informations sont à jour.
     *
     * @param oldAttribute Le nœud <code>Attribute</code> à supprimer de la
     * liste des attributs.
     * @return Le nœud <code>Attribute</code> supprimé.
     * @exception HJAXException NO_MODIFICATION_ALLOWED_ERR : déclenché si ce
     * nœud est en lecture seule.
     * <br>NOT_FOUND_ERR : déclenché si <code>oldAttribute</code> n'est pas un
     * attribut de l'élément.
     */
    public AttributeImpl removeAttributeNode(AttributeImpl oldAttribute) throws HJAXException;

    /**
     * Renvoie une liste de nœuds (NodeList) de tous les éléments (Elements)
     * descendants portant le même nom de balise, dans l'ordre du document.
     *
     * @param name : le nom de la balise à utiliser. La valeur spéciale « * »
     * correspond à toutes les balises.
     * @return : une liste de nœuds (Elements) correspondants.
     */
    public Interface.NodeList getElementsByTagName(String name);
    
    /**
     * renvoie un boolean pour déterminer si l'élement possède au moins
     * un attribbut
     * @return 
     */
    public boolean hasAttributes();
    
    /**
     * Renvoie <code>true</code> lorsqu'un attribut portant le nom donné est
     * spécifié sur cet élément ou a une valeur par défaut, <code>false</code>
     * sinon.
     *
     * @param name Le nom de l'attribut à rechercher.
     * @return <code>true</code> si un attribut portant le nom donné est
     * spécifié sur cet élément ou a une valeur par défaut, <code>false</code>
     * sinon.
     * @since 1.4, DOM Niveau 2
     */
    public boolean hasAttribute(String name);

    /**
     * Si le paramètre <code>isId</code> est <code>true</code>, cette méthode
     * déclare l'attribut spécifié comme étant un attribut d'ID déterminé par
     * l'utilisateur. Cela affecte la valeur de <code>Attribute.isId</code> et
     * le comportement de <code>Document.getElementById</code>, mais ne modifie
     * aucun schéma utilisé. En particulier, cela n'affecte pas le
     * <code>Attribute.schemaTypeInfo</code> du nœud <code>Attribute</code>
     * spécifié. Utilisez la valeur <code>false</code> pour le paramètre
     * <code>isId</code> afin de déclarer un attribut comme étant un attribut
     * d'ID déterminé par l'utilisateur.
     * <br> Pour spécifier un attribut par nom local et URI d'espace de noms,
     * utilisez la méthode <code>setIdAttributeibuteNS</code>.
     *
     * @param name : nom de l'attribut.
     * @param isId Indique si l'attribut est de type ID.
     * @exception HJAXException NO_MODIFICATION_ALLOWED_ERR : déclenchée si ce
     * nœud est en lecture seule.
     * <br>NOT_FOUND_ERR : déclenchée si le nœud spécifié n'est pas un attribut
     * de cet élément.
     * @since 1.5, DOM niveau 3
     */
    public void setIdAttribute(String name, boolean isId) throws HJAXException;

}
