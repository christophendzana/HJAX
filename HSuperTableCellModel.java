package stable;

import hsupertable.*;
import java.awt.Color;
import java.awt.Insets;
import java.io.Serializable;

/**
 * HTableCellModel — Contient toutes les métadonnées visuelles d'une cellule.
 *
 * Chaque cellule du tableau peut avoir ses propres réglages indépendants :
 * couleurs, bordures, alignement, marges et direction du texte. Cette classe
 * joue le rôle de "fiche signalétique" d'une cellule — le renderer la consulte
 * pour savoir comment dessiner la cellule en question.
 *
 * On la garde intentionnellement simple : pas de logique métier ici, juste du
 * stockage et des accesseurs propres.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HSuperTableCellModel implements Serializable {

    // Nécessaire pour la sérialisation (bonne pratique avec Serializable)
    private static final long serialVersionUID = 1L;

    // =========================================================================
    // COULEURS
    // =========================================================================
    /**
     * Couleur de fond de la cellule. null = on utilise la couleur par défaut du
     * style.
     */
    private Color background;

    /**
     * Couleur du texte. null = couleur par défaut du style.
     */
    private Color foreground;

    // =========================================================================
    // BORDURES PAR CÔTÉ
    // Chaque côté est géré indépendamment, comme dans Word.
    // On stocke 3 informations par côté : couleur, épaisseur (en pixels), style.
    // =========================================================================
    // -- Côté HAUT --
    private Color borderTopColor;
    private float borderTopThickness;
    private int borderTopStyle;     // constantes définies dans HSuperTable

    // -- Côté BAS --
    private Color borderBottomColor;
    private float borderBottomThickness;
    private int borderBottomStyle;

    // -- Côté GAUCHE --
    private Color borderLeftColor;
    private float borderLeftThickness;
    private int borderLeftStyle;

    // -- Côté DROIT --
    private Color borderRightColor;
    private float borderRightThickness;
    private int borderRightStyle;

    // =========================================================================
    // ALIGNEMENT
    // On utilise les constantes SwingConstants : LEFT, CENTER, RIGHT pour
    // l'horizontal, et TOP, CENTER, BOTTOM pour le vertical.
    // =========================================================================
    /**
     * Alignement horizontal du contenu (SwingConstants.LEFT / CENTER / RIGHT).
     */
    private int horizontalAlignment;

    /**
     * Alignement vertical du contenu (SwingConstants.TOP / CENTER / BOTTOM).
     */
    private int verticalAlignment;

    // =========================================================================
    // MARGES INTERNES (padding)
    // Espace entre le bord de la cellule et son contenu, en pixels.
    // =========================================================================
    /**
     * Marges internes de la cellule. Insets(top, left, bottom, right) — même
     * convention qu'en Swing. null = on utilise les marges globales définies
     * dans HSuperTable.
     */
    private Insets margins;

    // =========================================================================
    // DIRECTION DU TEXTE
    // Les constantes valides sont déclarées dans HSuperTable.
    // =========================================================================
    /**
     * Direction du texte dans la cellule. Valeurs attendues :
     * HSuperTable.TEXT_HORIZONTAL, HSuperTable.TEXT_VERTICAL_UP,
     * HSuperTable.TEXT_VERTICAL_DOWN.
     */
    private int textDirection;

    private String formula;
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================

    /**
     * Constructeur par défaut.
     */
    public HSuperTableCellModel() {
        this(0, 0, 0);
    }

    /**
     * Crée une cellule avec les valeurs par défaut. Tout est à null (couleurs)
     * ou aux valeurs neutres (alignement centré verticalement, gauche
     * horizontalement, pas de bordures, marges nulles).
     *
     * @param defaultHAlign alignement horizontal par défaut (ex:
     * SwingConstants.LEFT)
     * @param defaultVAlign alignement vertical par défaut (ex:
     * SwingConstants.CENTER)
     * @param defaultTextDirection direction du texte par défaut
     */
    public HSuperTableCellModel(int defaultHAlign, int defaultVAlign, int defaultTextDirection) {
        this.background = null;
        this.foreground = null;

        // Pas de bordures par défaut — épaisseur à 0 signifie "non définie"
        this.borderTopColor = null;
        this.borderTopThickness = 0f;
        this.borderTopStyle = 0;

        this.borderBottomColor = null;
        this.borderBottomThickness = 0f;
        this.borderBottomStyle = 0;

        this.borderLeftColor = null;
        this.borderLeftThickness = 0f;
        this.borderLeftStyle = 0;

        this.borderRightColor = null;
        this.borderRightThickness = 0f;
        this.borderRightStyle = 0;

        this.horizontalAlignment = defaultHAlign;
        this.verticalAlignment = defaultVAlign;
        this.margins = null;
        this.textDirection = defaultTextDirection;
    }

    // =========================================================================
    // COULEURS — getters & setters
    // =========================================================================
    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public boolean hasBackground() {
        return background != null;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public boolean hasForeground() {
        return foreground != null;
    }

    // =========================================================================
    // BORDURES — getters & setters par côté
    // =========================================================================
    // --- HAUT ---
    public Color getBorderTopColor() {
        return borderTopColor;
    }

    public void setBorderTopColor(Color c) {
        this.borderTopColor = c;
    }

    public float getBorderTopThickness() {
        return borderTopThickness;
    }

    public void setBorderTopThickness(float t) {
        this.borderTopThickness = t;
    }

    public int getBorderTopStyle() {
        return borderTopStyle;
    }

    public void setBorderTopStyle(int s) {
        this.borderTopStyle = s;
    }

    /**
     * Vrai si ce côté a une bordure définie (épaisseur > 0 et couleur non
     * nulle).
     */
    public boolean hasBorderTop() {
        return borderTopColor != null && borderTopThickness > 0f;
    }

    // --- BAS ---
    public Color getBorderBottomColor() {
        return borderBottomColor;
    }

    public void setBorderBottomColor(Color c) {
        this.borderBottomColor = c;
    }

    public float getBorderBottomThickness() {
        return borderBottomThickness;
    }

    public void setBorderBottomThickness(float t) {
        this.borderBottomThickness = t;
    }

    public int getBorderBottomStyle() {
        return borderBottomStyle;
    }

    public void setBorderBottomStyle(int s) {
        this.borderBottomStyle = s;
    }

    public boolean hasBorderBottom() {
        return borderBottomColor != null && borderBottomThickness > 0f;
    }

    // --- GAUCHE ---
    public Color getBorderLeftColor() {
        return borderLeftColor;
    }

    public void setBorderLeftColor(Color c) {
        this.borderLeftColor = c;
    }

    public float getBorderLeftThickness() {
        return borderLeftThickness;
    }

    public void setBorderLeftThickness(float t) {
        this.borderLeftThickness = t;
    }

    public int getBorderLeftStyle() {
        return borderLeftStyle;
    }

    public void setBorderLeftStyle(int s) {
        this.borderLeftStyle = s;
    }

    public boolean hasBorderLeft() {
        return borderLeftColor != null && borderLeftThickness > 0f;
    }

    // --- DROIT ---
    public Color getBorderRightColor() {
        return borderRightColor;
    }

    public void setBorderRightColor(Color c) {
        this.borderRightColor = c;
    }

    public float getBorderRightThickness() {
        return borderRightThickness;
    }

    public void setBorderRightThickness(float t) {
        this.borderRightThickness = t;
    }

    public int getBorderRightStyle() {
        return borderRightStyle;
    }

    public void setBorderRightStyle(int s) {
        this.borderRightStyle = s;
    }

    public boolean hasBorderRight() {
        return borderRightColor != null && borderRightThickness > 0f;
    }

    /**
     * Applique la même bordure sur les quatre côtés d'un coup. Sera utilisée pour
     * les méthodes setBorderAll() de HSuperTable.
     *
     * @param color couleur de la bordure
     * @param thickness épaisseur en pixels
     * @param style style (constante HSuperTable.BORDER_SOLID, etc.)
     */
    public void setBorderAll(Color color, float thickness, int style) {
        setBorderTopColor(color);
        setBorderTopThickness(thickness);
        setBorderTopStyle(style);
        setBorderBottomColor(color);
        setBorderBottomThickness(thickness);
        setBorderBottomStyle(style);
        setBorderLeftColor(color);
        setBorderLeftThickness(thickness);
        setBorderLeftStyle(style);
        setBorderRightColor(color);
        setBorderRightThickness(thickness);
        setBorderRightStyle(style);
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * Supprime toutes les bordures de la cellule (remet à zéro).
     */
    public void clearAllBorders() {
        borderTopColor = null;
        borderTopThickness = 0f;
        borderTopStyle = 0;
        borderBottomColor = null;
        borderBottomThickness = 0f;
        borderBottomStyle = 0;
        borderLeftColor = null;
        borderLeftThickness = 0f;
        borderLeftStyle = 0;
        borderRightColor = null;
        borderRightThickness = 0f;
        borderRightStyle = 0;
    }

    /**
     * Vrai si au moins un côté a une bordure définie.
     */
    public boolean hasAnyBorder() {
        return hasBorderTop() || hasBorderBottom() || hasBorderLeft() || hasBorderRight();
    }

    // =========================================================================
    // ALIGNEMENT — getters & setters
    // =========================================================================
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(int alignment) {
        this.horizontalAlignment = alignment;
    }

    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(int alignment) {
        this.verticalAlignment = alignment;
    }

    /**
     * Raccourci pour définir les deux alignements en une seule ligne.
     */
    public void setAlignment(int hAlign, int vAlign) {
        this.horizontalAlignment = hAlign;
        this.verticalAlignment = vAlign;
    }

    // =========================================================================
    // MARGES — getters & setters
    // =========================================================================
    public Insets getMargins() {
        return margins;
    }

    /**
     * Définit les marges internes. Passe null pour revenir aux marges globales
     * du tableau (comportement par défaut).
     */
    public void setMargins(Insets margins) {
        // On stocke une copie pour éviter que l'appelant modifie nos données par surprise
        this.margins = (margins != null) ? new Insets(margins.top, margins.left, margins.bottom, margins.right) : null;
    }

    public boolean hasCustomMargins() {
        return margins != null;
    }

    // =========================================================================
    // DIRECTION DU TEXTE — getters & setters
    // =========================================================================
    public int getTextDirection() {
        return textDirection;
    }

    public void setTextDirection(int direction) {
        this.textDirection = direction;
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================
    /**
     * Remet la cellule dans son état initial — comme si elle venait d'être
     * créée. Sera utilisée quand on efface le formatage d'une cellule 
     */
    public void reset() {
        background = null;
        foreground = null;
        clearAllBorders();
        horizontalAlignment = 0;
        verticalAlignment = 0;
        margins = null;
        textDirection = 0;
    }

    /**
     * Crée une copie indépendante de cette cellule. Sera utilisée lors du déplacement
     * ou de la duplication de lignes/colonnes.
     */
    public HSuperTableCellModel copy() {
        HSuperTableCellModel copy = new HSuperTableCellModel();

        copy.background = this.background;
        copy.foreground = this.foreground;

        copy.borderTopColor = this.borderTopColor;
        copy.borderTopThickness = this.borderTopThickness;
        copy.borderTopStyle = this.borderTopStyle;

        copy.borderBottomColor = this.borderBottomColor;
        copy.borderBottomThickness = this.borderBottomThickness;
        copy.borderBottomStyle = this.borderBottomStyle;

        copy.borderLeftColor = this.borderLeftColor;
        copy.borderLeftThickness = this.borderLeftThickness;
        copy.borderLeftStyle = this.borderLeftStyle;

        copy.borderRightColor = this.borderRightColor;
        copy.borderRightThickness = this.borderRightThickness;
        copy.borderRightStyle = this.borderRightStyle;

        copy.horizontalAlignment = this.horizontalAlignment;
        copy.verticalAlignment = this.verticalAlignment;
        copy.textDirection = this.textDirection;

        // Copie défensive des marges
        copy.margins = (this.margins != null)
                ? new Insets(this.margins.top, this.margins.left, this.margins.bottom, this.margins.right)
                : null;

        return copy;
    }

    @Override
    public String toString() {
        return "HTableCellModel{"
                + "bg=" + background
                + ", fg=" + foreground
                + ", hAlign=" + horizontalAlignment
                + ", vAlign=" + verticalAlignment
                + ", textDir=" + textDirection
                + ", hasCustomMargins=" + hasCustomMargins()
                + ", hasAnyBorder=" + hasAnyBorder()
                + "}";
    }
}
