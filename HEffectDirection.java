package htextarea;

/**
 * Direction de projection d'un effet typographique.
 *
 * <p>Utilisée principalement par l'effet {@link HTextEffect#SHADOW} pour
 * indiquer dans quelle direction l'ombre est projetée par rapport au texte.</p>
 *
 * <p>Pour les effets dont la direction n'a pas de sens ({@link HTextEffect#LIGHT},
 * {@link HTextEffect#REFLECTION}, {@link HTextEffect#OUTLINE}), cette valeur
 * est ignorée par {@link HEffectPainter} — elle peut quand même être stockée
 * dans la config sans provoquer d'erreur.</p>
 *
 * <p>Convention de nommage : VERTICALE_HORIZONTALE.</p>
 *
 * @author FIDELE
 * @version 1.0
 * @see HTextEffectConfig
 * @see HEffectPainter
 */
public enum HEffectDirection {

    /**
     * Ombre projetée vers le bas et vers la droite.
     * C'est la direction classique utilisée dans la plupart des logiciels
     * de traitement de texte (Word, LibreOffice…).
     */
    BAS_DROITE,

    /**
     * Ombre projetée vers le bas et vers la gauche.
     */
    BAS_GAUCHE,

    /**
     * Ombre projetée vers le haut et vers la droite.
     */
    HAUT_DROITE,

    /**
     * Ombre projetée vers le haut et vers la gauche.
     */
    HAUT_GAUCHE,

    /**
     * Ombre centrée : projetée dans les quatre directions simultanément.
     * Produit un effet "halo d'ombre" uniforme autour de chaque lettre,
     * similaire à l'option "Flou" de Word sans décalage directionnel.
     */
    CENTREE
}
