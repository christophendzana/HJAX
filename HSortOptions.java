/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea.sort;

import java.util.Locale;

/**
 * Options globales de tri applicables à l'ensemble de l'opération.
 *
 * <p>
 * Correspond au contenu de la boîte de dialogue "Options de tri" accessible via
 * le bouton "Options..." dans {@link HSortDialog}.</p>
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSortOptions {

    // =========================================================================
    // Énumérations
    // =========================================================================
    /**
     * Séparateur utilisé pour découper chaque paragraphe en colonnes.
     *
     * <p>
     * Si {@code AUCUN}, chaque paragraphe est traité comme une seule valeur. Si
     * un séparateur est défini, chaque paragraphe est découpé en champs, et les
     * clés de tri affichent "Colonne 1", "Colonne 2"...</p>
     */
    public enum Separateur {
        /**
         * Pas de découpage — le paragraphe est une seule valeur.
         */
        AUCUN,
        /**
         * Découpage sur les caractères de tabulation {@code \t}.
         */
        TABULATION,
        /**
         * Découpage sur les points-virgules {@code ;}.
         */
        POINT_VIRGULE,
        /**
         * Découpage sur un caractère défini par l'utilisateur.
         */
        AUTRE
    }

    // =========================================================================
    // Champs
    // =========================================================================
    /**
     * Séparateur de champs. Valeur par défaut : {@code AUCUN} (tri par
     * paragraphe entier).
     */
    private Separateur separateur = Separateur.AUCUN;

    /**
     * Caractère personnalisé utilisé si {@link #separateur} = {@code AUTRE}.
     */
    private char separateurAutre = ',';

    /**
     * Si {@code true}, la casse est prise en compte lors du tri textuel. 
     */
    private boolean respecterCasse = false;

    /**
     * Locale utilisée pour le tri textuel. Détermine l'ordre alphabétique (ex:
     * les accents en français). Valeur par défaut : locale du système.
     */
    private Locale locale = Locale.getDefault();

    /**
     * Si {@code true}, la première ligne/paragraphe de la sélection est traitée
     * comme un en-tête et exclue du tri. Valeur par défaut : {@code false}.
     */
    private boolean ligneEnTete = false;

    // =========================================================================
    // Constructeur
    // =========================================================================
    /**
     * Crée des options avec toutes les valeurs par défaut.
     */
    public HSortOptions() {
    }

    // =========================================================================
    // Getters / Setters fluides
    // =========================================================================
    public Separateur getSeparateur() {
        return separateur;
    }

    public char getSeparateurAutre() {
        return separateurAutre;
    }

    public boolean isRespecterCasse() {
        return respecterCasse;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isLigneEnTete() {
        return ligneEnTete;
    }

    public HSortOptions setSeparateur(Separateur s) {
        this.separateur = s;
        return this;
    }

    public HSortOptions setSeparateurAutre(char c) {
        this.separateurAutre = c;
        return this;
    }

    public HSortOptions setRespecterCasse(boolean b) {
        this.respecterCasse = b;
        return this;
    }

    public HSortOptions setLocale(Locale l) {
        this.locale = l;
        return this;
    }

    public HSortOptions setLigneEnTete(boolean b) {
        this.ligneEnTete = b;
        return this;
    }

    /**
     * Retourne le caractère réel utilisé pour découper les champs.
     *
     * @return le caractère de séparation, ou {@code 0} si pas de séparateur
     */
    public char getCaractereSeparateur() {
        return switch (separateur) {
            case TABULATION ->
                '\t';
            case POINT_VIRGULE ->
                ';';
            case AUTRE ->
                separateurAutre;
            default ->
                0;
        };
    }

    /**
     * Indique si un séparateur de champs est actif.
     *
     * @return {@code true} si le texte doit être découpé en colonnes
     */
    public boolean aUnSeparateur() {
        return separateur != Separateur.AUCUN;
    }
}
