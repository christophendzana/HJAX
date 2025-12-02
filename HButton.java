/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicButtonUI;
import hcomponents.vues.HButtonStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Composant HButton - Un bouton Swing personnalisé avec design moderne.
 * Étend JButton pour offrir des fonctionnalités avancées : styles prédéfinis,
 * coins arrondis, ombres, bordures personnalisables et animations fluides.
 * 
 * <p>Ce composant fournit une API cohérente avec les autres composants HComponents
 * (HToggleButton, HCheckBox) pour une expérience utilisateur uniforme.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JButton
 * @see HButtonStyle
 * @see HBasicButtonUI
 */
public class HButton extends JButton {

    /** Bordure personnalisée du bouton */
    private HBorder hBorder;
    
    /** Ombre personnalisée du bouton */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 12;
    
    /** Style visuel appliqué au bouton */
    private HButtonStyle buttonStyle = HButtonStyle.PRIMARY;
    
    /** Espacement entre l'icône et le texte (en pixels) */
    private int gap = 0;

    /**
     * Constructeur avec texte.
     * 
     * @param text le texte à afficher sur le bouton
     */
    public HButton(String text) {
        super(text);
        updateUI();
    }

    /**
     * Constructeur par défaut.
     * Crée un bouton sans texte ni icône.
     */
    public HButton() {
        this(null, null);
    }

    /**
     * Constructeur avec icône.
     * 
     * @param icon l'icône à afficher sur le bouton
     */
    public HButton(Icon icon) {
        this(null, icon);
    }

    /**
     * Constructeur avec Action.
     * Les propriétés du bouton sont extraites de l'Action fournie.
     * 
     * @param a l'Action définissant les propriétés du bouton
     * @since 1.3
     */
    public HButton(Action a) {
        this();
        setAction(a);
    }

    /**
     * Constructeur avec texte et icône.
     * 
     * @param text le texte à afficher sur le bouton
     * @param icon l'icône à afficher sur le bouton
     */
    public HButton(String text, Icon icon) {
        // Création du modèle de bouton
        setModel(new DefaultButtonModel());

        // Initialisation des propriétés
        init(text, icon);
        updateUI();
    }

    /**
     * Met à jour l'interface utilisateur du bouton.
     * Installe le HBasicButtonUI personnalisé et configure les propriétés de rendu.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicButtonUI());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
    }

    /**
     * Définit l'alignement vertical du contenu du bouton.
     * Surchargé pour permettre des alignements supplémentaires (EAST, NORTH).
     * 
     * @param alignment l'alignement vertical (TOP, CENTER, BOTTOM, EAST, NORTH)
     */
    public void setVerticalAlignment(int alignment) {
        super.setVerticalAlignment(alignment);

        if (alignment == this.getVerticalAlignment()) {
            return;
        }
        int oldValue = this.getVerticalAlignment();
        super.setVerticalAlignment(checkVerticalKey(alignment, "verticalAlignment Error"));
    }

    /**
     * Vérifie la validité d'une clé d'alignement vertical.
     * 
     * @param key la clé d'alignement à vérifier
     * @param exception le message d'erreur en cas de clé invalide
     * @return la clé d'alignement si valide
     * @throws IllegalArgumentException si la clé n'est pas valide
     */
    @Override
    protected int checkVerticalKey(int key, String exception) {
        if ((key == TOP) || (key == CENTER) || (key == BOTTOM) || (key == EAST) || (key == NORTH)) {
            return key;
        } else {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Retourne la bordure personnalisée du bouton.
     * 
     * @return la bordure HBorder, ou null si aucune bordure n'est définie
     */
    public HBorder getHBorder() {
        return hBorder;
    }

    /**
     * Retourne le rayon des coins arrondis.
     * 
     * @return le rayon en pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis.
     * 
     * @param radius le nouveau rayon en pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Définit une bordure personnalisée pour le bouton.
     * 
     * @param border la nouvelle bordure à appliquer
     */
    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
    }

    /**
     * Retourne l'ombre personnalisée du bouton.
     * 
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() {
        return hShadow;
    }

    /**
     * Définit une ombre personnalisée pour le bouton.
     * 
     * @param shadow la nouvelle ombre à appliquer
     */
    public void setShadow(HShadow shadow) {
        this.hShadow = shadow;
    }

    /**
     * Retourne l'espacement entre l'icône et le texte.
     * 
     * @return l'espacement en pixels
     */
    public int getGap() {
        return gap;
    }

    /**
     * Définit l'espacement entre l'icône et le texte.
     * Cet espacement est utilisé pour les alignements NORTH et EAST.
     * 
     * @param gap le nouvel espacement en pixels
     */
    public void setGap(int gap) {
        this.gap = gap;
    }

    /**
     * Retourne le style visuel actuel du bouton.
     * 
     * @return le HButtonStyle appliqué
     */
    public HButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    /**
     * Définit le style visuel du bouton.
     * Met également à jour la couleur du texte selon le style choisi.
     * 
     * @param style le nouveau style à appliquer
     */
    public void setButtonStyle(HButtonStyle style) {
        this.buttonStyle = style;
        this.setForeground(style.getTextColor());
        repaint();
    }

    /**
     * Méthode factory pour créer un HButton avec un style prédéfini.
     * 
     * @param text le texte à afficher sur le bouton
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HButton configurée avec le style spécifié
     */
    public static HButton withStyle(String text, HButtonStyle style) {
        HButton button = new HButton(text);
        button.setButtonStyle(style);
        button.setForeground(style.getTextColor());
        return button;
    }

    /**
     * Méthode factory pour créer un HButton avec texte, icône et style prédéfini.
     * 
     * @param text le texte à afficher sur le bouton
     * @param icon l'icône à afficher sur le bouton
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HButton configurée avec les paramètres spécifiés
     */
    public static HButton withStyle(String text, Icon icon, HButtonStyle style) {
        HButton button = new HButton(text, icon);
        button.setButtonStyle(style);
        button.setForeground(style.getTextColor());
        return button;
    }
}