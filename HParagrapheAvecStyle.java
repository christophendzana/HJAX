/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea.sort;

import htextarea.paragraph.HParagraphConfig;
import javax.swing.text.AttributeSet;

/**
 * Conteneur qui associe le texte d'un paragraphe à ses attributs de style. 
 * 
 * Utilisé par HSortMoteur pour trier les paragraphes tout en préservant
 * leurs styles (gras, couleur, puce, bordure...).
 *
 * @author FIDELE
 * @version 1.0
 */
public class HParagrapheAvecStyle {

    // =========================================================================
    // Champs
    // =========================================================================
    /**
     * Texte brut du paragraphe (sans le {@code \n} final). C'est sur ce texte
     * que portent les comparaisons de tri.
     */
    private final String texte;

    /**
     * Attributs de caractères du paragraphe (police, gras, couleur...). Stockés
     * sous forme d'une liste d'attributs par position de caractère. Peut être
     * null si le paragraphe n'a pas d'attributs custom.
     */
    private final AttributeSet[] attrsParCaractere;

    /**
     * Configuration de paragraphe (alignement, retrait, puce, bordure...). Peut
     * être {@code null} si le paragraphe n'a pas de style custom.
     */
    private final HParagraphConfig configParagraphe;

    // =========================================================================
    // Constructeur
    // =========================================================================
    /**
     * Crée un conteneur paragraphe + style.
     *
     * @param texte le texte brut du paragraphe
     * @param attrsParCaractere les attributs de chaque caractère (peut être
     * null)
     * @param configParagraphe la config de paragraphe (peut être null)
     */
    public HParagrapheAvecStyle(String texte,
            AttributeSet[] attrsParCaractere,
            HParagraphConfig configParagraphe) {
        this.texte = texte != null ? texte : "";
        this.attrsParCaractere = attrsParCaractere;
        this.configParagraphe = configParagraphe;
    }

    // =========================================================================
    // Getters
    // =========================================================================
    /**
     * @return le texte brut du paragraphe
     */
    public String getText() {
        return texte;
    }

    /**
     * @return les attributs par caractère, ou {@code null}
     */
    public AttributeSet[] getAttrsParCaractere() {
        return attrsParCaractere;
    }

    /**
     * @return la config de paragraphe, ou {@code null}
     */
    public HParagraphConfig getConfigParagraphe() {
        return configParagraphe;
    }

    @Override
    public String toString() {
        return "HParagrapheAvecStyle{texte='" + texte + "'}";
    }
}
