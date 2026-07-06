package htextarea.paragraph;

import javax.swing.Icon;

/**
 * Configuration d'une liste (puces ou numérotation) pour un paragraphe.
 *
 * <p>
 * Cette classe décrit comment une liste doit être affichée : quel type de
 * symbole, quel niveau d'imbrication, quel format de numérotation, et quel
 * suffixe utiliser.</p>
 *
 * @author FIDELE
 * @version 2.0
 */
public class HListConfig {

    // =========================================================================
    // Constante
    // =========================================================================
    /**
     * Largeur de la zone réservée au symbole de puce en pixels.
     */
    public static final float LARGEUR_ZONE_PUCE = 36f;

    // =================================================================
    // Types de liste
    // ===================================================
    /**
     * Type de liste — détermine quel symbole ou quel format de numérotation est
     * affiché devant le paragraphe.
     */
    public enum Type {

        // ---- Puces ----
        /**
         * Puce ronde pleine
         */
        BULLET_ROUND,
        /**
         * Puce ronde vide
         */
        BULLET_CIRCLE,
        /**
         * Puce carrée
         */
        BULLET_SQUARE,
        /**
         * Puce personnalisée : utilise HListConfig#getSymboleTexte() ou
         * HListConfig#getSymboleIcon() selon ce qui est défini.
         */
        CUSTOM,
        // ---- Numérotation ----

        /**
         * Numérotation arabe → 1. 2. 3.
         */
        NUMBER,
        /**
         * Lettres minuscules → a) b) c)
         */
        LETTER_LOWER,
        /**
         * Lettres majuscules → A. B. C.
         */
        LETTER_UPPER,
        /**
         * Chiffres romains majusc → I II III
         */
        ROMAN_UPPER,
        /**
         * Chiffres romains minusc → i ii iii
         */
        ROMAN_LOWER
    }

    // ================================================
    // Champs
    // ========================================================
    /**
     * Type de liste. Valeur par défaut : puce ronde.
     */
    private Type type = Type.BULLET_ROUND;

    /**
     * Niveau d'imbrication.
     */
    private int niveau = 0;

    /**
     * Numéro de départ pour les listes numérotées.
     */
    private int debut = 1;

    /**
     * Si true, la numérotation repart de "debut" même s'il existe des
     * paragraphes numérotés du même type juste au-dessus.
     */
    private boolean restart = true;

    // ---- Symbole hybride (puces) ----
    /**
     * Symbole Unicode à afficher pour les puces de type CUSTOM. Valeur par
     * défaut : "•". Ignoré si symboleIcon est non null.
     */
    private String symboleTexte = "•";

    /**
     * Icône Swing à afficher pour les puces de type CUSTOM. Peut être un
     * ImageIcon, une icône SVG, etc.
     */
    private Icon symboleIcon = null;

    // ---- Numérotation ----
    /**
     * Suffixe affiché après le numéro ou la lettre.
     *
     * Exemples : "1." "A." "I."
     */
    private String suffixe = ".";

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Constructeur par défaut. Crée une puce ronde (•) de niveau 0 avec le
     * suffixe ".".
     */
    public HListConfig() {
    }

    /**
     * Constructeur avec type uniquement.
     *
     * @param type le type de liste
     */
    public HListConfig(Type type) {
        this.type = (type != null) ? type : Type.BULLET_ROUND;
    }

    /**
     * Constructeur avec type et niveau.
     *
     * @param type le type de liste
     * @param niveau le niveau d'imbrication (0 = premier niveau)
     */
    public HListConfig(Type type, int niveau) {
        this.type = (type != null) ? type : Type.BULLET_ROUND;
        this.niveau = Math.max(0, niveau);
    }

    /**
     * Constructeur complet pour les numérotations.
     *
     * @param type le type (NUMBER, LETTER_LOWER, etc.)
     * @param niveau le niveau d'imbrication
     * @param debut numéro de départ
     * @param suffixe le suffixe (".", ")", " "...)
     */
    public HListConfig(Type type, int niveau, int debut, String suffixe) {
        this.type = (type != null) ? type : Type.NUMBER;
        this.niveau = Math.max(0, niveau);
        this.debut = Math.max(1, debut);
        this.suffixe = (suffixe != null) ? suffixe : ".";
    }

    // =================================================================
    // Setters fluides
    // =====================================================
    /**
     * Définit le type de liste.
     *
     * @param type le type souhaité
     * @return {@code this} pour le chaînage
     */
    public HListConfig setType(Type type) {
        if (type != null) {
            this.type = type;
        }
        return this;
    }

    /**
     * Définit le niveau d'imbrication.
     *
     * @param niveau valeur ≥ 0
     * @return this
     */
    public HListConfig setNiveau(int niveau) {
        this.niveau = Math.max(0, niveau);
        return this;
    }

    /**
     * Définit le numéro de départ pour les listes numérotées.
     *
     * @param debut valeur ≥ 1
     * @return {@code this} pour le chaînage
     */
    public HListConfig setDebut(int debut) {
        this.debut = Math.max(1, debut);
        return this;
    }

    /**
     * Définit si la numérotation repart de #debut quand la liste est
     * interrompue.
     *
     * @param restart true pour repartir, false pour continuer
     * @return this pour le chaînage
     */
    public HListConfig setRestart(boolean restart) {
        this.restart = restart;
        return this;
    }

    /**
     * Définit le symbole texte pour les puces de type CUSTOM.
     *
     * @param symbole un caractère ou chaîne Unicode
     * @return {@code this} pour le chaînage
     */
    public HListConfig setSymboleTexte(String symbole) {
        if (symbole != null) {
            this.symboleTexte = symbole;
        }
        return this;
    }

    /**
     * Définit l'icône pour les puces de type #CUSTOM. Si non null, elle sera
     * prioritaire sur #symboleTexte.
     *
     * @param icon une icône Swing (ImageIcon, SVG, etc.)
     * @return this pour le chaînage
     */
    public HListConfig setSymboleIcon(Icon icon) {
        this.symboleIcon = icon;
        return this;
    }

    /**
     * Définit le suffixe affiché après le numéro ou la lettre.
     *
     * @param suffixe
     * @return this pour le chaînage
     */
    public HListConfig setSuffixe(String suffixe) {
        if (suffixe != null) {
            this.suffixe = suffixe;
        }
        return this;
    }

    // =============================
    // Getters
    // =====================================
    /**
     * @return le type de liste
     */
    public Type getType() {
        return type;
    }

    /**
     * @return le niveau d'imbrication
     */
    public int getNiveau() {
        return niveau;
    }

    /**
     * @return le numéro de départ
     */
    public int getDebut() {
        return debut;
    }

    /**
     * @return true si la numérotation repart à chaque interruption
     */
    public boolean isRestart() {
        return restart;
    }

    /**
     * @return le symbole texte pour les puces CUSTOM
     */
    public String getSymboleTexte() {
        return symboleTexte;
    }

    /**
     * @return l'icône pour les puces CUSTOM, ou null
     */
    public Icon getSymboleIcon() {
        return symboleIcon;
    }

    /**
     * @return le suffixe de numérotation
     */
    public String getSuffixe() {
        return suffixe;
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================
    /**
     * Indique si ce type de liste est une puce (pas une numérotation).
     *
     * @return true si c'est une puce
     */
    public boolean estUnePuce() {
        return type == Type.BULLET_ROUND
                || type == Type.BULLET_CIRCLE
                || type == Type.BULLET_SQUARE
                || type == Type.CUSTOM;
    }

    /**
     * Indique si ce type de liste est une numérotation.
     *
     * @return true si c'est une numérotation
     */
    public boolean estUneNumerotation() {
        return !estUnePuce();
    }

    /**
     * Retourne le symbole Unicode par défaut selon le type et le niveau.    
     *
     * Pour les numérotations, retourne toujours "1" — le vrai numéro est
     * calculé dynamiquement par HParagraphPainter
     *
     * @return le symbole à afficher (avant calcul du numéro réel)
     */
    public String getSymboleParDefaut() {
        return switch (type) {
            case BULLET_ROUND ->
                (niveau == 0) ? "•" : (niveau == 1) ? "○" : "■";
            case BULLET_CIRCLE ->
                "○";
            case BULLET_SQUARE ->
                "■";
            case CUSTOM ->
                symboleTexte;
            default ->
                "1";
        };
    }

    /**
     * Calcule l'indentation gauche totale pour ce niveau.
     *
     * @return l'indentation en pixels
     */
    public float calculerIndentation() {
        return (niveau + 1) * LARGEUR_ZONE_PUCE;
    }

    /**
     * Formate un numéro selon le type de liste.    
     * @param numero le numéro à formater (1-based)
     * @return la chaîne formatée avec le suffixe
     */
    public String formaterNumero(int numero) {
        String base = switch (type) {
            case NUMBER ->
                String.valueOf(numero);
            case LETTER_LOWER ->
                String.valueOf((char) ('a' + numero - 1));
            case LETTER_UPPER ->
                String.valueOf((char) ('A' + numero - 1));
            case ROMAN_UPPER ->
                versRomain(numero).toUpperCase();
            case ROMAN_LOWER ->
                versRomain(numero).toLowerCase();
            default ->
                String.valueOf(numero);
        };
        return base + suffixe;
    }

    /**
     * Convertit un entier en chiffres romains.    
     * @param nombre l'entier à convertir (doit être ≥ 1)
     * @return la représentation en chiffres romains majuscules
     */
    private String versRomain(int nombre) {
        // Les valeurs romaines dans l'ordre décroissant
        int[] valeurs = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symboles = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder resultat = new StringBuilder();
        for (int i = 0; i < valeurs.length; i++) {
            // Tant que le nombre courant est ≥ à cette valeur,
            // on ajoute le symbole et on soustrait la valeur
            while (nombre >= valeurs[i]) {
                resultat.append(symboles[i]);
                nombre -= valeurs[i];
            }
        }
        return resultat.toString();
    }

    @Override
    public String toString() {
        return "HListConfig{type=" + type
                + ", niveau=" + niveau
                + ", suffixe='" + suffixe + "'"
                + "}";
    }
}
