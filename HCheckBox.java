/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HCheckBoxUI;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 * Composant HCheckBox - Une case à cocher personnalisée avec design moderne.
 * Étend JCheckBox pour offrir des fonctionnalités avancées : styles prédéfinis,
 * coins arrondis, ombres, bordures personnalisables et animations fluides.
 * 
 * <p>Ce composant suit la même philosophie de design que HButton et HToggleButton,
 * assurant une cohérence visuelle dans l'ensemble de l'application.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JCheckBox
 * @see HButtonStyle
 * @see HCheckBoxUI
 */
public class HCheckBox extends JCheckBox {
    
    /** Bordure personnalisée du composant */
    private HAbstractBorder hBorder;
    
    /** Ombre personnalisée du composant */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 6;
    
    /** Style visuel appliqué au composant */
    private HButtonStyle buttonStyle = HButtonStyle.PRIMARY;
    
    /** Espacement entre la case à cocher et le texte (en pixels) */
    private int gap = 8;
    
    /** Taille de la case à cocher (en pixels) */
    private int checkmarkSize = 20;
    
    /** Indique si l'ombre doit être affichée */
    private boolean showShadow = true;

    /**
     * Constructeur par défaut.
     * Crée une case à cocher sans texte, sans icône et non sélectionnée.
     */
    public HCheckBox() {
        this("", null, false);
    }
    
    /**
     * Constructeur avec texte.
     * 
     * @param text le texte à afficher à côté de la case à cocher
     */
    public HCheckBox(String text) {
        this(text, null, false);
    }
    
    /**
     * Constructeur avec Action.
     * Les propriétés du bouton sont extraites de l'Action fournie.
     * 
     * @param a l'Action définissant les propriétés du bouton
     */
    public HCheckBox(Action a) {
        super(a);
        updateUI();
    }
    
    /**
     * Constructeur avec icône.
     * 
     * @param icon l'icône à afficher à côté de la case à cocher
     */
    public HCheckBox(Icon icon) {
        this("", icon, false);
    }
    
    /**
     * Constructeur avec icône et état de sélection.
     * 
     * @param icon l'icône à afficher
     * @param selected l'état initial de sélection
     */
    public HCheckBox(Icon icon, boolean selected) {
        this("", icon, selected);
    }
    
    /**
     * Constructeur avec texte et état de sélection.
     * 
     * @param text le texte à afficher
     * @param selected l'état initial de sélection
     */
    public HCheckBox(String text, boolean selected) {
        this(text, null, selected);
    }
    
    /**
     * Constructeur avec texte et icône.
     * 
     * @param text le texte à afficher
     * @param icon l'icône à afficher
     */
    public HCheckBox(String text, Icon icon) {
        this(text, icon, false);
    }
    
    /**
     * Constructeur principal avec texte, icône et état de sélection.
     * 
     * @param text le texte à afficher
     * @param icon l'icône à afficher
     * @param selected l'état initial de sélection
     */
    public HCheckBox(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        updateUI();
    }
    
    /**
     * Met à jour l'interface utilisateur du composant.
     * Installe le HCheckBoxUI personnalisé et configure les propriétés de rendu.
     */
    @Override
    public void updateUI() {
        setUI(new HCheckBoxUI());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
    }
    
    /**
     * Définit l'alignement vertical du texte par rapport à la case à cocher.
     * Accepte les valeurs standards (TOP, CENTER, BOTTOM) plus NORTH et EAST.
     * 
     * @param alignment l'alignement vertical
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
     * Ajoute le support de NORTH et EAST en plus des valeurs standard.
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
     * Retourne la bordure personnalisée du composant.
     * 
     * @return la bordure HAbstractBorder, ou null si aucune bordure n'est définie
     */
    public HAbstractBorder getHBorder() { 
        return hBorder; 
    }
    
    /**
     * Définit une bordure personnalisée pour le composant.
     * 
     * @param border la nouvelle bordure à appliquer
     */
    public void setHBorder(HAbstractBorder border) { 
        this.hBorder = border; 
        repaint();
    }
    
    /**
     * Retourne l'ombre personnalisée du composant.
     * 
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() { 
        return hShadow; 
    }
    
    /**
     * Définit une ombre personnalisée pour le composant.
     * 
     * @param shadow la nouvelle ombre à appliquer
     */
    public void setShadow(HShadow shadow) { 
        this.hShadow = shadow; 
        repaint();
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
     * @param radius le nouveau rayon en pixels (doit être ≥ 0)
     */
    public void setCornerRadius(int radius) { 
        this.cornerRadius = radius; 
        repaint();
    }
    
    /**
     * Retourne l'espacement entre la case à cocher et le texte.
     * 
     * @return l'espacement en pixels
     */
    public int getGap() { 
        return gap; 
    }
    
    /**
     * Définit l'espacement entre la case à cocher et le texte.
     * 
     * @param gap le nouvel espacement en pixels
     */
    public void setGap(int gap) { 
        this.gap = gap; 
        repaint();
    }
    
    /**
     * Retourne la taille de la case à cocher.
     * 
     * @return la taille en pixels (entre 8 et 40)
     */
    public int getCheckmarkSize() { 
        return checkmarkSize; 
    }
    
    /**
     * Définit la taille de la case à cocher.
     * La taille est automatiquement limitée entre 8 et 40 pixels.
     * 
     * @param size la nouvelle taille en pixels
     */
    public void setCheckmarkSize(int size) { 
        this.checkmarkSize = Math.max(8, Math.min(40, size));
        repaint();
    }
    
    /**
     * Vérifie si l'ombre est activée.
     * 
     * @return true si l'ombre est affichée, false sinon
     */
    public boolean isShowShadow() { 
        return showShadow; 
    }
    
    /**
     * Active ou désactive l'affichage de l'ombre.
     * 
     * @param show true pour afficher l'ombre, false pour la masquer
     */
    public void setShowShadow(boolean show) { 
        this.showShadow = show; 
        repaint();
    }
    
    /**
     * Retourne le style visuel actuel du composant.
     * 
     * @return le HButtonStyle appliqué
     */
    public HButtonStyle getButtonStyle() { 
        return buttonStyle; 
    }
    
    /**
     * Définit le style visuel du composant.
     * Met également à jour la couleur du texte selon le style choisi.
     * 
     * @param style le nouveau style à appliquer
     */
    public void setButtonStyle(HButtonStyle style) { 
        this.buttonStyle = style; 
        setForeground(style.getTextColor());
        repaint();
    }
    
    /**
     * Méthode factory pour créer une HCheckBox avec un style prédéfini.
     * 
     * @param text le texte à afficher
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HCheckBox configurée avec le style spécifié
     */
    public static HCheckBox withStyle(String text, HButtonStyle style) {
        HCheckBox checkBox = new HCheckBox(text);
        checkBox.setButtonStyle(style);
        return checkBox;
    }
    
    /**
     * Méthode factory pour créer une HCheckBox avec texte, icône et style prédéfini.
     * 
     * @param text le texte à afficher
     * @param icon l'icône à afficher
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HCheckBox configurée avec les paramètres spécifiés
     */
    public static HCheckBox withStyle(String text, Icon icon, HButtonStyle style) {
        HCheckBox checkBox = new HCheckBox(text, icon);
        checkBox.setButtonStyle(style);
        return checkBox;
    }
}