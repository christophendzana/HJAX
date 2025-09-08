/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Interface;

import DOM.ElementImpl;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import org.w3c.dom.TypeInfo;

/**
 *
 * @author PSM
 */
public interface Attribute {
    
     /**
     * Renvoie le nom de cet attribut.Si <code>Node.localName</code> est
     *différent de <code>null</code>, cet attribut est un nom qualifié.
     * @return 
     */
    public String getName();
    
    /**
     * <code>True</code> si une valeur a été explicitement attribuée à cet 
     * attribut dans le document d'instance, <code>false</code> sinon.Si 
     * l'application modifie la valeur de ce nœud d'attribut (même s'il finit
     * par avoir la même valeur que la valeur par défaut), elle est définie sur
     * <code>true</code>. L'implémentation peut gérer les attributs avec des valeurs
     * par défaut provenant d'autres schémas de la même manière, mais les applications 
     * doivent utiliser <code>Document.normalizeDocument()</code> pour garantir que ces 
     * informations sont à jour.
     * @return 
     */
    public boolean getSpecified();

     /**
     * Lors de la récupération, la valeur de l'attribut est renvoyée sous forme de chaîne.
     * Les références de caractères et d'entités générales sont remplacées par leurs valeurs.
     * Voir également la méthode <code>getAttribute</code> sur l'interface
     * <code>Element</code>.
     * <br>Lors de la définition, un nœud <code>Text</code> est créé avec le contenu non analysé 
     * de la chaîne, c'est-à-dire que tous les caractères qu'un processeur XML reconnaîtrait 
     * comme du balisage sont traités comme du texte littéral.
     * Voir également la méthode <code>Element.setAttribute()</code>.
     * <br> Certaines implémentations spécialisées, telles que certaines 
     * [<a href='http://www.w3.org/TR/2003/REC-SVG11-20030114/'>SVG 1.1</a>]
     * peuvent effectuer une normalisation automatique, même après une
     * mutation ; dans ce cas, la valeur lors de la récupération peut différer de la valeur 
     * lors du paramétrage.
     * @return 
     */
    public String getValue();
    
    
    /**
    * Lors de la récupération, la valeur de l'attribut est renvoyée sous forme de chaîne. Les références de caractères et d'entités générales sont remplacées par leurs valeurs.
    * Voir également la méthode <code>getAttribute</code> sur l'interface
    * <code>Element</code>.
    * <br>Lors de la définition, un nœud <code>Text</code> est créé avec le contenu non analysé
    * de la chaîne, c'est-à-dire que tous les caractères qu'un processeur XML
    * reconnaîtrait comme du balisage sont traités comme du texte littéral. Voir
    * également la méthode <code>Element.setAttribute()</code>.
    * <br> Certaines implémentations spécialisées, telles que certaines [<a href='http://www.w3.org/TR/2003/REC-SVG11-20030114/'>SVG 1.1</a>]
    * peuvent effectuer une normalisation automatique, même après
    * mutation ; dans ce cas, la valeur récupérée peut différer de la
    * valeur paramétrée.
    * @param value
    * @exception HJAXException
    * NO_MODIFICATION_ALLOWED_ERR : déclenché lorsque le nœud est en lecture seule.
     */
    public void setValue(String value) throws HJAXException;

    /**
    * Le nœud <code>Element</code> auquel cet attribut est attaché ou
    * <code>null</code> si cet attribut n'est pas utilisé.
    * @return 
    * @since 1.4, DOM niveau 2
     */
    public ElementImpl getHolderElement();

    /**
     * Les informations de type associées à cet attribut. Bien que les informations de type contenues dans cet attribut soient garanties correctes après le chargement du document ou l'appel de <code>Document.normalizeDocument()</code>, <code>schemaTypeInfo</code>
    peuvent ne pas être fiables si le nœud a été déplacé.
    * @return 
    * @since 1.5, DOM niveau 3
     */
    public TypeInfo getSchemaTypeInfo();

    /**
     * Indique si cet attribut est de type ID (c'est-à-dire qu'il contient un identifiant pour son élément propriétaire) ou non.Si c'est le cas et que sa valeur est unique, l'<code>ownerElement</code> de cet attribut peut être récupéré à l'aide de la méthode <code>Document.getElementById</code>
. L'implémentation pourrait utiliser plusieurs méthodes pour déterminer si un nœud d'attribut contient un identifiant :
 <ul>
    * <li> Si la validation
    * a été effectuée à l'aide d'un schéma XML [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>Schéma XML Partie 1</a>]
    * lors du chargement du document ou de l'appel de
    * <code>Document.normalizeDocument()</code>, les valeurs des contributions de l'ensemble d'informations post-validation du schéma (contributions PSVI) sont utilisées pour
    * déterminer si cet attribut est un attribut d'ID déterminé par le schéma à l'aide de
    * la définition de l'<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/#term-sdi'>
    * ID déterminé par le schéma</a> dans [<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/'>XPointer</a>]
    * .
        * </li>
   * <li> Si la validation a été effectuée à l'aide d'une DTD lors du chargement du document ou
   * lors de l'appel de <code>Document.normalizeDocument()</code>, la valeur <b>[définition de type]</b> de l'infoset est utilisée pour déterminer si cet attribut est un ID déterminé par la DTD
   * attribut utilisant la définition <a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/#term-ddi'>
   * ID déterminé par la DTD</a> dans [<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/'>XPointer</a>]
   * .
   * </li>
   * <li> de l'utilisation des méthodes <code>Element.setIdAttribute()</code>,
   * <code>Element.setIdAttributeNS()</code> ou
   * <code>Element.setIdAttributeNode()</code>, c'est-à-dire qu'il s'agit d'un attribut d'ID déterminé par l'utilisateur ;
   * <p ><b>Remarque :</b> le framework XPointer (voir la section 3.2 dans [<a href='http://www.w3.org/TR/2003/REC-xptr-framework-20030325/'>XPointer</a>]
   * ) considère l'attribut d'ID DOM déterminé par l'utilisateur comme faisant partie de la définition d'ID XPointer déterminée en externe.
   * </li>
   * <li> en utilisant des mécanismes qui
   * sortent du cadre de cette spécification, il s'agit alors d'un attribut d'ID déterminé en externe. Cela inclut l'utilisation de langages de schéma différents du schéma XML et de la DTD.
     * </li>
     * </ul>
     * Si une validation a eu lieu lors de l'appel de la fonction
    * <code>Document.normalizeDocument()</code>, tous les attributs d'ID déterminés par l'utilisateur
    * sont réinitialisés et les informations d'ID de tous les nœuds d'attribut sont ensuite
    * réévaluées conformément au schéma utilisé. Par conséquent, si l'attribut
    * <code>Attr.schemaTypeInfo</code> contient un type d'ID,
    * <code>isId</code> renvoie toujours vrai.
     * @return 
    * @since 1.5, DOM niveau 3
    
         */
    public boolean isId();
    
}
