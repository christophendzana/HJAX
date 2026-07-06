package htextarea.attribute;

/**
 * Registre central de toutes les clés d'attributs personnalisées du composant HTextArea.
 *
 * Pourquoi cette classe existe.
 * Dans un StyledDocument de Swing, chaque attribut (gras, taille,
 * couleur…) est identifié par un objet "clé". Swing a déjà ses propres clés
 * via javax.swing.text.StyleConstants. Pour nos attributs custom
 * (effets typographiques, styles de paragraphe…), on doit créer nos propres clés.
 * Avec HAttributeKeys, toutes les clés sont au même endroit.
 * N'importe quelle classe du projet peut les lire et les utiliser sans
 * dépendre d'une autre classe métier.
 *
 * @author FIDELE
 * @version 1.0
 */
public final class HAttributeKeys {

    // =========================================================================
    // Clés pour les CARACTÈRES
    // =========================================================================

    /**
     * Liste des effets typographiques actifs sur un segment de texte.
     *
     * Type de la valeur stockée : {@code List<HEffetEntree>}
     *     
     * Un segment peut porter plusieurs effets simultanément (ombre + contour
     * par exemple) -> c'est pourquoi on stocke une liste et non un seul effet.
     */
    public static final Object EFFECTS_LIST = new AttributeKey("character.effects_list");

    /**
     * Couleur d'un effet typographique.
     *
     * <p>Type de la valeur stockée : {@code java.awt.Color}</p>
     *    
     * La couleur est encapsulée directement dans {HEffetEntree} 
     *     
     */
    @Deprecated
    public static final Object EFFECT_COLOR = new AttributeKey("character.effect_color");

    /**
     * Marqueur indiquant qu'un caractère est en exposant (simulation custom).
     *
     * <p>Type de la valeur stockée : {@code Boolean}</p>
     *
     * Ici On a essayeé avec StyleConstants.Superscript de Swing mais ça
     * provoque un bug de hauteur de ligne GlyphView augmente la
     * hauteur déclarée du glyphe, poussant les lignes voisines. À la place,
     * on réduit la taille de police à 58 % et on stocke ce marqueur pour
     * pouvoir détecter l'état et restaurer la taille d'origine. (A REVOIR)
     */
    public static final Object SUPERSCRIPT = new AttributeKey("character.superscript");

    /**
     * Marqueur indiquant qu'un caractère est en indice.
     *
     * Type de la valeur stockée : Boolean   
     */
    public static final Object SUBSCRIPT = new AttributeKey("character.subscript");

    // =========================================================================
    // Clés pour les PARAGRAPHES 
    // =========================================================================

    /**
     * Configuration complète du style d'un paragraphe.
     *
     * On stocke un : HParagraphConfig il contiendra l'alignement, retraits, 
     * interligne, espacement avant/après, style de liste, style de bordure, 
     * trame de fond, affichage des marques.
     */
    public static final Object PARAGRAPH_STYLE = new AttributeKey("paragraph.style");

    // =========================================================================
    // la clé proprement dite
    // =========================================================================

    /**
     * Implémentation d'une clé d'attribut pour le StyledDocument
     *
     * <p>Swing identifie les attributs par "identité d'objet
     * dans un AttributeSet". Chaque constante de HAttributeKeys est une 
     * instance distincte de cette classe, ce qui garantit qu'elles ne 
     * peuvent jamais entrer en collision entre elles ni avec les clés standard 
     * de StyleConstants</p>
     *
     * <p>La méthode "toString()" retourne le nom descriptif de la clé,
     * utilisé par Swing lors de la lecture des attributs.</p>
     */
    private static final class AttributeKey {

        /** Nom descriptif de la clé */
        private final String nom;

        /**
         * @param nom identifiant lisible de la clé
         */
        AttributeKey(String nom) {
            this.nom = nom;
        }

        @Override
        public String toString() {
            return "HAttributeKey(" + nom + ")";
        }
    }

    // =========================================================================
    // Constructeur privé — classe utilitaire, non instanciable
    // =========================================================================

    /**
     * Constructeur privé.     
     */
    private HAttributeKeys() {
        // non instanciable
    }
}