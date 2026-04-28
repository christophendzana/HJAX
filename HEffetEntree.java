package htextarea;

import java.awt.Color;

/**
 * Entrée unique dans la liste d'effets d'un segment de texte.
 *
 * <p>Un segment peut porter plusieurs effets simultanément (ex: SHADOW + OUTLINE).
 * Chaque effet est représenté par une instance de cette classe, qui regroupe
 * le type d'effet, sa couleur et sa configuration.</p>
 *
 * <p>Les instances sont stockées dans une {@link java.util.List} sous la clé
 * {@link HEffectPainter#EFFECTS_LIST_ATTRIBUTE} dans le {@code StyledDocument}.
 * {@link HEffectPainter} itère sur cette liste pour peindre chaque effet
 * dans l'ordre d'insertion.</p>
 *
 * <h3>Exemple — deux effets sur la même sélection</h3>
 * <pre>
 *     textArea.setShadow(Color.DARK_GRAY, HEffectDirection.BAS_DROITE, 3, 2, 120);
 *     // puis, sans changer la sélection :
 *     textArea.setOutline(Color.BLUE, 1, 180);
 *     // les deux effets sont maintenant actifs sur la sélection
 * </pre>
 *
 * @author FIDELE
 * @version 1.0
 * @see HEffectPainter
 * @see HTextEffectConfig
 */
public final class HEffetEntree {

    /** Le type d'effet à appliquer. Ne doit pas être {@code null} ni {@code NONE}. */
    private final HTextEffect effet;

    /**
     * La couleur associée à cet effet.
     * Peut être {@code null} — {@link HEffectPainter} utilisera alors la couleur
     * de texte du segment comme repli.
     */
    private final Color couleur;

    /**
     * Les paramètres de configuration (direction, distance, flou, transparence).
     * Ne doit jamais être {@code null} — utiliser {@link HTextEffectConfig} par défaut
     * plutôt que {@code null}.
     */
    private final HTextEffectConfig config;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------

    /**
     * Crée une entrée d'effet.
     *
     * @param effet   le type d'effet (ne doit pas être {@code null})
     * @param couleur la couleur de l'effet ({@code null} accepté)
     * @param config  la configuration ({@code null} remplacé par les valeurs par défaut)
     */
    public HEffetEntree(HTextEffect effet, Color couleur, HTextEffectConfig config) {
        if (effet == null) {
            throw new IllegalArgumentException("L'effet ne peut pas être null");
        }
        this.effet   = effet;
        this.couleur = couleur;
        this.config  = (config != null) ? config : new HTextEffectConfig();
    }

    // -------------------------------------------------------------------------
    // Accesseurs
    // -------------------------------------------------------------------------

    /** @return le type d'effet */
    public HTextEffect getEffet() {
        return effet;
    }

    /** @return la couleur de l'effet, ou {@code null} si non spécifiée */
    public Color getCouleur() {
        return couleur;
    }

    /** @return la configuration de l'effet, jamais {@code null} */
    public HTextEffectConfig getConfig() {
        return config;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "HEffetEntree{effet=" + effet
                + ", couleur=" + couleur
                + ", config=" + config + '}';
    }
}
