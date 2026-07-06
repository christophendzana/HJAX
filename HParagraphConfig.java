package htextarea.paragraph;

import java.awt.Color;
import javax.swing.text.StyleConstants;

/**
 * Configuration complète du style d'un paragraphe.
 *
 * <h3>Principe Une config = UN paragraphe</h3>
 * 
 * Plutôt que de stocker dix attributs séparés dans le {@code StyledDocument}
 * (un pour l'alignement, un pour le retrait, un pour l'interligne...), on
 * stocke UN seul objet {@code HParagraphConfig} sous la clé
 * {@code HAttributeKeys.PARAGRAPH_STYLE}. C'est plus propre, plus facile à
 * lire, et cohérent avec ce qu'on a déjà fait pour les effets.
 * 
 * @author FIDELE
 * @version 1.0
 * @see HParagraphFeature
 * @see HParagraphEntry
 * @see HListConfig
 * @see HBorderConfig
 */
public class HParagraphConfig {

    // =========================================================================
    // Valeurs par défaut — les mêmes que celles de JTextPane
    // =========================================================================
    /**
     * Alignement par défaut : gauche (identique au comportement natif de
     * JTextPane).
     */
    public static final int ALIGNEMENT_DEFAUT = StyleConstants.ALIGN_LEFT;

    /**
     * Retrait gauche par défaut : 0 pixel (pas de retrait).
     */
    public static final float LEFT_INDENT_DEFAUT = 0f;

    /**
     * Retrait droit par défaut : 0 pixel.
     */
    public static final float RIGHT_INDENT_DEFAUT = 0f;

    /**
     * Retrait de première ligne par défaut : 0 pixel.
     */
    public static final float FIRST_LINE_DEFAUT = 0f;

    /**
     * Interligne par défaut : 0.0 = simple (comportement natif de JTextPane).
     */
    public static final float LINE_SPACING_DEFAUT = 0f;

    /**
     * Espace avant le paragraphe par défaut : 0 pixel.
     */
    public static final float SPACE_BEFORE_DEFAUT = 0f;

    /**
     * Espace après le paragraphe par défaut : 0 pixel.
     */
    public static final float SPACE_AFTER_DEFAUT = 0f;

    // =========================================================================
    // Champs — mise en forme standard (Swing natif)
    // =========================================================================
    
    private int alignement = ALIGNEMENT_DEFAUT;

    /**
     * Retrait gauche en pixels
     */
    private float leftIndent = LEFT_INDENT_DEFAUT;

    /**
     * Retrait droit en pixels.
     */
    private float rightIndent = RIGHT_INDENT_DEFAUT;

    /**
     * Retrait supplémentaire de la première ligne uniquement.
     */
    private float firstLineIndent = FIRST_LINE_DEFAUT;

    /**
     * Interligne     
     */
    private float lineSpacing = LINE_SPACING_DEFAUT;

    /**
     * Espace en pixels avant le paragraphe 
     */
    private float spaceBefore = SPACE_BEFORE_DEFAUT;

    /**
     * Espace en pixels après le paragraphe (entre ce paragraphe et le suivant).
     */
    private float spaceAfter = SPACE_AFTER_DEFAUT;

    // =========================================================================
    // Champs — mise en forme custom (dessinée par HParagraphPainter)
    // =========================================================================
    /**
     * Configuration de liste (puces ou numérotation). 
     */
    private HListConfig listConfig = null;

    /**
     * Configuration de bordure.      
     */
    private HBorderConfig borderConfig = null;

    /**
     * Couleur de trame de fond du paragraphe entier.      
     */
    private Color background = null;

    /**
     * Indique si les caractères invisibles (espaces, sauts de ligne) doivent
     * être affichés sur ce paragraphe.
     */
    private boolean showMarks = false;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Constructeur par défaut. Crée une config avec toutes les valeurs standard
     * de JTextPane (alignement gauche, pas de retrait, interligne simple, aucun
     * effet custom).
     */
    public HParagraphConfig() {        
    }
    
    /**
     * Définit l'alignement horizontal.
     *
     * @param alignement
     * @return
     */
    public HParagraphConfig setAlignement(int alignement) {
        this.alignement = alignement;
        return this;
    }

    /**
     * Définit le retrait gauche.
     *
     * @param pixels valeur ≥ 0
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setLeftIndent(float pixels) {
        this.leftIndent = Math.max(0f, pixels);
        return this;
    }

    /**
     * Définit le retrait droit.
     *
     * @param pixels valeur ≥ 0
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setRightIndent(float pixels) {
        this.rightIndent = Math.max(0f, pixels);
        return this;
    }

    /**
     * Définit le retrait de la première ligne.
     *
     * <p>
     * Accepte des valeurs négatives (retrait suspendu pour les listes).</p>
     *
     * @param pixels valeur en pixels (peut être négative)
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setFirstLineIndent(float pixels) {
        this.firstLineIndent = pixels;
        return this;
    }

    /**
     * Définit l'interligne.
     *
     * @param valeur 0.0 = simple, 0.5 = 1,5×, 1.0 = double
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setLineSpacing(float valeur) {
        this.lineSpacing = Math.max(0f, valeur);
        return this;
    }

    /**
     * Définit l'espace avant le paragraphe.
     *
     * @param pixels valeur ≥ 0
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setSpaceBefore(float pixels) {
        this.spaceBefore = Math.max(0f, pixels);
        return this;
    }

    /**
     * Définit l'espace après le paragraphe.
     *
     * @param pixels valeur ≥ 0
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setSpaceAfter(float pixels) {
        this.spaceAfter = Math.max(0f, pixels);
        return this;
    }

    /**
     * Définit la configuration de liste (puces ou numérotation).
     *
     * @param listConfig la config de liste, ou {@code null} pour supprimer
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setListConfig(HListConfig listConfig) {
        this.listConfig = listConfig;
        return this;
    }

    /**
     * Définit la configuration de bordure.
     *
     * @param borderConfig la config de bordure, ou {@code null} pour supprimer
     * @return {@code this} pour le chaînage
     */
    public HParagraphConfig setBorderConfig(HBorderConfig borderConfig) {
        this.borderConfig = borderConfig;
        return this;
    }

    /**
     * Définit la couleur de trame de fond du paragraphe.
     *
     * @param couleur la couleur, ou {@code null} pour supprimer la trame
     * @return pour le chaînage
     */
    public HParagraphConfig setBackground(Color couleur) {
        this.background = couleur;
        return this;
    }

    /**
     * Active ou désactive l'affichage des caractères invisibles.
     *
     * @param afficher true pour afficher les marques
     * @return 
     */
    public HParagraphConfig setShowMarks(boolean afficher) {
        this.showMarks = afficher;
        return this;
    }

    // =======================================================
    // Getters
    // ==========================================================
    /**
     * @return l'alignement horizontal
     */
    public int getAlignement() {
        return alignement;
    }

    /**
     * @return le retrait gauche en pixels
     */
    public float getLeftIndent() {
        return leftIndent;
    }

    /**
     * @return le retrait droit en pixels
     */
    public float getRightIndent() {
        return rightIndent;
    }

    /**
     * @return le retrait de première ligne en pixels
     */
    public float getFirstLineIndent() {
        return firstLineIndent;
    }

    /**
     * @return le coefficient d'interligne
     */
    public float getLineSpacing() {
        return lineSpacing;
    }

    /**
     * @return l'espace avant le paragraphe en pixels
     */
    public float getSpaceBefore() {
        return spaceBefore;
    }

    /**
     * @return l'espace après le paragraphe en pixels
     */
    public float getSpaceAfter() {
        return spaceAfter;
    }

    /**
     * @return la config de liste, ou null si aucune liste
     */
    public HListConfig getListConfig() {
        return listConfig;
    }

    /**
     * @return la config de bordure, ou null si aucune bordure
     */
    public HBorderConfig getBorderConfig() {
        return borderConfig;
    }

    /**
     * @return la couleur de trame, ou null si aucune trame
     */
    public Color getBackground() {
        return background;
    }

    /**
     * @return true si les marques invisibles sont affichées
     */
    public boolean isShowMarks() {
        return showMarks;
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================
    /**
     * Indique si ce paragraphe a une liste active (puces ou numérotation).
     *
     * @return true si listConfig est non null
     */
    public boolean hasListe() {
        return listConfig != null;
    }

    /**
     * Indique si ce paragraphe a une bordure active.
     *
     * @return true si borderConfig est non null
     */
    public boolean hasBordure() {
        return borderConfig != null;
    }

    /**
     * Indique si ce paragraphe a une trame de fond active.
     *
     * @return true si background est non null
     */
    public boolean hasTrame() {
        return background != null;
    }

    /**
     * Indique si ce paragraphe utilise uniquement des fonctionnalités Swing
     * natives (pas de liste, pas de bordure, pas de trame, pas de marques).
     *
     * <p>
     * à Utiler dans HParagraphPainter: si c'est  true, il n'a rien
     * à peindre on laisse Swing tout peindre</p>
     *
     * @return true si aucune fonctionnalité custom n'est active
     */
    public boolean estNatifSwingSeul() {
        return listConfig == null
                && borderConfig == null
                && background == null
                && !showMarks;
    }

    @Override
    public String toString() {
        return "HParagraphConfig{"
                + "alignement=" + alignement
                + ", leftIndent=" + leftIndent
                + ", lineSpacing=" + lineSpacing
                + ", hasListe=" + hasListe()
                + ", hasBordure=" + hasBordure()
                + '}';
    }
}
