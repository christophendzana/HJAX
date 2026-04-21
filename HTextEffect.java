package htextarea;

/**
 * Effets typographiques applicables par caractère dans {@link hcomponents.HTextArea}.
 *
 * <p>Ces effets ne sont pas natifs dans Swing — ils sont peints manuellement
 * par {@link HEffectPainter} en interceptant le rendu dans
 * {@link HBasicTextAreaUI#paintSafely(java.awt.Graphics)}.</p>
 *
 * <p>Chaque constante est stockée comme attribut custom dans le
 * {@link javax.swing.text.StyledDocument} du composant, ce qui permet
 * d'avoir des effets différents sur des portions de texte distinctes.</p>
 *
 * @author FIDELE
 * @version 1.0
 */
public enum HTextEffect {

    /** Aucun effet — rendu standard de Swing. */
    NONE,

    /**
     * Ombre portée : le texte est dupliqué en gris semi-transparent,
     * décalé de quelques pixels vers le bas-droite.
     */
    SHADOW,

    /**
     * Contour : seul le contour des glyphes est dessiné,
     * le remplissage intérieur reste transparent.
     */
    OUTLINE,

    /**
     * Relief : donne l'impression que le texte "sort" de la surface.
     * Obtenu par deux ombres : claire en haut-gauche, sombre en bas-droite.
     */
    EMBOSS,

    /**
     * Gravure : inverse du relief — le texte semble "enfoncé" dans la surface.
     * Obtenu par deux ombres : sombre en haut-gauche, claire en bas-droite.
     */
    ENGRAVE,

    /**
     * Lueur (glow) : halo coloré rayonnant autour du texte.
     * Simulé par plusieurs couches d'ombres concentriques de plus en plus transparentes.
     */
    GLOW
}
