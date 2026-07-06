package htextarea.paragraph;

import java.awt.Color;

/**
 * Configuration de la bordure d'un paragraphe.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HParagraphConfig
 */
public class HBorderConfig {

    // =========================================================================
    // Type de bordure quels côtés sont tracés
    // =========================================================================
    /**
     * Définit quels côtés du paragraphe sont bordés.
     */
    public enum Type {
        /**
         * Encadré complet (les 4 côtés).
         */
        ALL,
        /**
         * Seulement le bord du haut.
         */
        TOP,
        /**
         * Seulement le bord du bas.
         */
        BOTTOM,
        /**
         * Seulement le bord gauche (style "citation").
         */
        LEFT,
        /**
         * Seulement le bord droit.
         */
        RIGHT,
        /**
         * Bords haut et bas uniquement.
         */
        TOP_BOTTOM
    }

    // =========================================================================
    // Champs
    // =========================================================================
    /**
     * Côtés à dessiner. Valeur par défaut : encadré complet.
     */
    private Type type = Type.ALL;

    /**
     * Épaisseur du trait.
     */
    private float epaisseur = 1f;

    /**
     * Couleur du trait.
     */
    private Color couleur = Color.BLACK;

    /**
     * Espace entre le texte du paragraphe et la bordure (en pixels). Évite que
     * la bordure "colle" au texte.
     */
    private float padding = 4f;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée une bordure avec les valeurs par défaut
     */
    public HBorderConfig() {
    }

    /**
     * Crée une bordure avec type et couleur.
     *
     * @param type les côtés à tracer
     * @param couleur la couleur du trait
     */
    public HBorderConfig(Type type, Color couleur) {
        this.type = (type != null) ? type : Type.ALL;
        this.couleur = (couleur != null) ? couleur : Color.BLACK;
    }

    /**
     * Crée une bordure complète.
     *
     * @param type les côtés à tracer
     * @param epaisseur épaisseur du trait en pixels
     * @param couleur couleur du trait
     * @param padding espace entre texte et bordure
     */
    public HBorderConfig(Type type, float epaisseur, Color couleur, float padding) {
        this.type = (type != null) ? type : Type.ALL;
        this.epaisseur = Math.max(0.5f, epaisseur);
        this.couleur = (couleur != null) ? couleur : Color.BLACK;
        this.padding = Math.max(0f, padding);
    }

    // =========================================================================
    // Setters fluides
    // =========================================================================
    /**
     * @param type les côtés à tracer ; retourne {@code this}
     */
    public HBorderConfig setType(Type type) {
        if (type != null) {
            this.type = type;
        }
        return this;
    }

    /**
     * @param epaisseur épaisseur ≥ 0.5px ; retourne {@code this}
     */
    public HBorderConfig setEpaisseur(float epaisseur) {
        this.epaisseur = Math.max(0.5f, epaisseur);
        return this;
    }

    /**
     * @param couleur couleur du trait ; retourne {@code this}
     */
    public HBorderConfig setCouleur(Color couleur) {
        if (couleur != null) {
            this.couleur = couleur;
        }
        return this;
    }

    /**
     * @param padding espace texte-bordure
     */
    public HBorderConfig setPadding(float padding) {
        this.padding = Math.max(0f, padding);
        return this;
    }

    // =========================================================================
    // Getters
    // =========================================================================
    /**
     * @return le type de bordure
     */
    public Type getType() {
        return type;
    }

    /**
     * @return l'épaisseur du trait
     */
    public float getEpaisseur() {
        return epaisseur;
    }

    /**
     * @return la couleur du trait
     */
    public Color getCouleur() {
        return couleur;
    }

    /**
     * @return l'espace entre texte et bordure
     */
    public float getPadding() {
        return padding;
    }

    @Override
    public String toString() {
        return "HBorderConfig{type=" + type
                + ", epaisseur=" + epaisseur
                + ", couleur=" + couleur + "}";
    }
}
