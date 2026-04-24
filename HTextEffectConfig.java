package htextarea;

/**
 * Paramètres de configuration d'un effet typographique pour {@link HTextArea}.
 *
 * <p>Il accompagne toujours un {@link HTextEffect} stocké dans le
 * {@code StyledDocument}.
 *
 * <h3>Paramètres disponibles par effet</h3>
 * <ul>
 *   <li><b>SHADOW</b> : direction, distance, flou (simulé), transparence</li>
 *   <li><b>OUTLINE</b> : distance (= épaisseur du trait), transparence</li>
 *   <li><b>LIGHT</b>   : distance (= rayon du halo), transparence</li>
 *   <li><b>REFLECTION</b> : distance (= espace sous le texte), transparence</li>
 * </ul>
 *
 * <p>Les paramètres sans effet sur un type d'effet donné sont simplement
 * ignorés par {@link HEffectPainter} — aucune erreur n'est levée.</p>
 *
 * <h3>Exemple d'utilisation</h3>
 * <pre>
 *     HTextEffectConfig config = new HTextEffectConfig()
 *         .setDirection(HEffectDirection.BAS_GAUCHE)
 *         .setDistance(4)
 *         .setFlou(2)
 *         .setTransparence(100);
 *
 *     textArea.setTextEffect(HTextEffect.SHADOW, Color.DARK_GRAY, config);
 * </pre>
 *
 * @author FIDELE
 * @version 1.0
 * @see HTextEffect
 * @see HEffectDirection
 * @see HEffectPainter
 */
public class HTextEffectConfig {

    // -------------------------------------------------------------------------
    // Valeurs par défaut
    // -------------------------------------------------------------------------

    /** Direction par défaut : bas-droite (classique Word). */
    public static final HEffectDirection DIRECTION_DEFAUT  = HEffectDirection.BAS_DROITE;

    /**
     * Distance par défaut en pixels.
     * Pour SHADOW : décalage de l'ombre.
     * Pour OUTLINE : épaisseur du trait.
     * Pour LIGHT : rayon du halo.
     * Pour REFLECTION : espace entre le texte et son reflet.
     */
    public static final int DISTANCE_DEFAUT    = 2;

    /**
     * Niveau de flou par défaut (0 = aucun flou, 5 = flou maximal).
     * Utilisé uniquement par SHADOW.
     */
    public static final int FLOU_DEFAUT        = 1;

    /**
     * Transparence par défaut (0 = invisible, 255 = totalement opaque).
     * Correspond à la valeur alpha de l'effet.
     */
    public static final int TRANSPARENCE_DEFAUT = 120;

    // -------------------------------------------------------------------------
    // Champs de configuration
    // -------------------------------------------------------------------------

    /**
     * Direction de projection de l'effet.
     * Pertinent uniquement pour {@link HTextEffect#SHADOW}.
     */
    private HEffectDirection direction = DIRECTION_DEFAUT;

    /**
     * Distance en pixels.
     * Sémantique variable selon l'effet (voir Javadoc de la classe).
     */
    private int distance = DISTANCE_DEFAUT;

    /**
     * Niveau de flou simulé (0 à 5).
     * Traduit en nombre de couches d'ombres supplémentaires dans le rendu.
     * Pertinent uniquement pour {@link HTextEffect#SHADOW}.
     */
    private int flou = FLOU_DEFAUT;

    /**
     * Transparence de l'effet (0 à 255, valeur alpha).
     * Applicable à tous les effets.
     */
    private int transparence = TRANSPARENCE_DEFAUT;

    // -------------------------------------------------------------------------
    // Constructeurs
    // -------------------------------------------------------------------------

    /**
     * Constructeur par défaut : crée une config avec toutes les valeurs
     * standard (direction BAS_DROITE, distance 2px, flou 1, transparence 120).
     */
    public HTextEffectConfig() {
        // Valeurs par défaut définies dans les champs
    }

    /**
     * Constructeur complet pour SHADOW.
     *
     * @param direction    direction de l'ombre
     * @param distance     décalage en pixels
     * @param flou         niveau de flou (0–5)
     * @param transparence alpha de l'ombre (0–255)
     */
    public HTextEffectConfig(HEffectDirection direction, int distance,
                              int flou, int transparence) {
        this.direction    = direction != null ? direction : DIRECTION_DEFAUT;
        this.distance     = Math.max(0, distance);
        this.flou         = Math.clamp(flou, 0, 5);
        this.transparence = Math.clamp(transparence, 0, 255);
    }

    /**
     * Constructeur simplifié : distance + transparence uniquement.
     * Pratique pour OUTLINE, LIGHT et REFLECTION où direction et flou
     * ne sont pas utilisés.
     *
     * @param distance     valeur de distance / épaisseur / rayon
     * @param transparence alpha de l'effet (0–255)
     */
    public HTextEffectConfig(int distance, int transparence) {
        this.distance     = Math.max(0, distance);
        this.transparence = Math.clamp(transparence, 0, 255);
    }

    /**
     * Définit la direction de l'effet.
     *
     * @param direction la nouvelle direction (ne doit pas être {@code null})
     * @return {@code this} pour le chaînage
     */
    public HTextEffectConfig setDirection(HEffectDirection direction) {
        if (direction != null) {
            this.direction = direction;
        }
        return this;
    }

    /**
     * Définit la distance de l'effet en pixels.
     *
     * @param distance valeur ≥ 0
     * @return {@code this} pour le chaînage
     */
    public HTextEffectConfig setDistance(int distance) {
        this.distance = Math.max(0, distance);
        return this;
    }

    /**
     * Définit le niveau de flou simulé (uniquement pour SHADOW).
     *
     * @param flou valeur entre 0 (aucun flou) et 5 (flou maximal)
     * @return {@code this} pour le chaînage
     */
    public HTextEffectConfig setFlou(int flou) {
        this.flou = Math.clamp(flou, 0, 5);
        return this;
    }

    /**
     * Définit la transparence de l'effet (valeur alpha).
     *
     * @param transparence valeur entre 0 (invisible) et 255 (opaque)
     * @return {@code this} pour le chaînage
     */
    public HTextEffectConfig setTransparence(int transparence) {
        this.transparence = Math.clamp(transparence, 0, 255);
        return this;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return la direction de projection de l'effet */
    public HEffectDirection getDirection() {
        return direction;
    }

    /** @return la distance en pixels */
    public int getDistance() {
        return distance;
    }

    /** @return le niveau de flou (0–5) */
    public int getFlou() {
        return flou;
    }

    /** @return la transparence / alpha (0–255) */
    public int getTransparence() {
        return transparence;
    }

    // -------------------------------------------------------------------------
    // Méthodes factory statiques — raccourcis pratiques
    // -------------------------------------------------------------------------

    /**
     * Crée une config d'ombre avec toutes les options.
     *
     * @param direction    direction de l'ombre
     * @param distance     décalage en pixels
     * @param flou         niveau de flou (0–5)
     * @param transparence alpha (0–255)
     * @return une nouvelle config
     */
    public static HTextEffectConfig ombre(HEffectDirection direction, int distance,
                                           int flou, int transparence) {
        return new HTextEffectConfig(direction, distance, flou, transparence);
    }

    /**
     * Crée une config d'ombre avec la direction par défaut (BAS_DROITE).
     *
     * @param distance     décalage en pixels
     * @param flou         niveau de flou (0–5)
     * @param transparence alpha (0–255)
     * @return une nouvelle config
     */
    public static HTextEffectConfig ombre(int distance, int flou, int transparence) {
        return new HTextEffectConfig(DIRECTION_DEFAUT, distance, flou, transparence);
    }

    /**
     * Crée une config pour OUTLINE (contour).
     *
     * @param epaisseur    épaisseur du trait en pixels (correspond à {@code distance})
     * @param transparence alpha (0–255)
     * @return une nouvelle config
     */
    public static HTextEffectConfig contour(int epaisseur, int transparence) {
        return new HTextEffectConfig(epaisseur, transparence);
    }

    /**
     * Crée une config pour LIGHT (halo lumineux).
     *
     * @param rayon        rayon du halo en pixels (correspond à {@code distance})
     * @param transparence alpha maximal du halo (0–255)
     * @return une nouvelle config
     */
    public static HTextEffectConfig lumiere(int rayon, int transparence) {
        return new HTextEffectConfig(rayon, transparence);
    }

    /**
     * Crée une config pour REFLECTION.
     *
     * @param espacement   espace en pixels entre le texte et son reflet
     * @param transparence alpha du reflet (0–255)
     * @return une nouvelle config
     */
    public static HTextEffectConfig reflection(int espacement, int transparence) {
        return new HTextEffectConfig(espacement, transparence);
    }

    // -------------------------------------------------------------------------
    // toString — utile pour le débogage
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "HTextEffectConfig{"
                + "direction=" + direction
                + ", distance=" + distance
                + ", flou=" + flou
                + ", transparence=" + transparence
                + '}';
    }
}
