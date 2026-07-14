/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea.sort;

/**
 * Représente un critère de tri (un niveau hiérarchique).
 *
 * <p>
 * La boîte de dialogue Word propose 3 niveaux : "Trier par", "Puis par", "Puis
 * par". Chaque niveau est représenté par une instance de cette classe.</p>
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSortCritere {

    // =========================================================================
    // Énumérations
    // =========================================================================
    /**
     * Type de données à trier. Détermine comment les valeurs sont comparées.
     */
    public enum Type {
        /**
         * Tri alphabétique standard.
         */
        TEXTE,
        /**
         * Tri numérique — compare les valeurs numériques, pas les chaînes.
         */
        NOMBRE,
        /**
         * Tri chronologique — compare les dates.
         */
        DATE
    }

    /**
     * Sens du tri.
     */
    public enum Sens {
        /**
         * De A vers Z, de 1 vers 9, du plus ancien au plus récent.
         */
        CROISSANT,
        /**
         * De Z vers A, de 9 vers 1, du plus récent au plus ancien.
         */
        DECROISSANT
    }

    // =========================================================================
    // Champs
    // =========================================================================
    /**
     * Champ sur lequel trier.
     *
     * <p>
     * Valeurs possibles :</p>
     * <ul>
     * <li>{@code "Paragraphes"} — le texte entier du paragraphe</li>
     * <li>{@code "Colonne 1"}, {@code "Colonne 2"}... — un champ extrait par le
     * séparateur défini dans {@link HSortOptions}</li>
     * </ul>
     */
    private String champ;

    /**
     * Type de données.
     */
    private Type type;

    /**
     * Sens du tri.
     */
    private Sens sens;

    /**
     * Indique si ce critère est actif. Les critères "Puis par" sont inactifs si
     * aucun champ n'est sélectionné.
     */
    private boolean actif;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée un critère de tri complet.
     *
     * @param champ le champ à trier ("Paragraphes" ou "Colonne N")
     * @param type le type de données
     * @param sens le sens du tri
     */
    public HSortCritere(String champ, Type type, Sens sens) {
        this.champ = champ;
        this.type = type;
        this.sens = sens;
        this.actif = champ != null && !champ.isBlank();
    }

    /**
     * Crée un critère inactif (utilisé pour initialiser les niveaux 2 et 3).
     */
    public HSortCritere() {
        this.champ = null;
        this.type = Type.TEXTE;
        this.sens = Sens.CROISSANT;
        this.actif = false;
    }

    // =========================================================================
    // Getters / Setters
    // =========================================================================
    public String getChamp() {
        return champ;
    }

    public Type getType() {
        return type;
    }

    public Sens getSens() {
        return sens;
    }

    public boolean isActif() {
        return actif;
    }

    public void setChamp(String champ) {
        this.champ = champ;
        this.actif = champ != null && !champ.isBlank();
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setSens(Sens sens) {
        this.sens = sens;
    }

    @Override
    public String toString() {
        return "HSortCritere{champ='" + champ + "', type=" + type
                + ", sens=" + sens + ", actif=" + actif + '}';
    }
}
