/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicDesktopPaneUI;
import hcomponents.vues.HDesktopPaneStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.*;

/**
 * Composant HDesktopPane - Un conteneur de fenêtres internes Swing personnalisé avec design moderne.
 * Étend JDesktopPane pour offrir des fonctionnalités avancées : fonds personnalisés,
 * coins arrondis, dégradés, motifs et styles prédéfinis.
 * 
 * <p>Ce composant fournit une API cohérente avec les autres composants HComponents
 * (HButton, HDialog, HFrame, HInternalFrame) pour une expérience utilisateur uniforme.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JDesktopPane
 * @see HDesktopPaneStyle
 * @see HBasicDesktopPaneUI
 */
public class HDesktopPane extends JDesktopPane {

    /** Bordure personnalisée du desktop pane */
    private HBorder hBorder;
    
    /** Ombre personnalisée du desktop pane */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 15;
    
    /** Style visuel appliqué au desktop pane */
    private HDesktopPaneStyle desktopStyle = HDesktopPaneStyle.LIGHT;
    
    /** Indique si le fond utilise un dégradé */
    private boolean useGradient = true;
    
    /** Indique si le fond affiche un motif (grid/dots) */
    private boolean showPattern = false;
    
    /** Type de motif (grid, dots, none) */
    private PatternType patternType = PatternType.GRID;
    
    /** Couleur du motif */
    private java.awt.Color patternColor = new java.awt.Color(0, 0, 0, 20);
    
    /** Espacement du motif (en pixels) */
    private int patternSpacing = 30;

    /**
     * Énumération des types de motifs disponibles.
     */
    public enum PatternType {
        /** Aucun motif */
        NONE,
        /** Motif grille */
        GRID,
        /** Motif points */
        DOTS
    }

    /**
     * Constructeur par défaut.
     * Crée un desktop pane avec le style par défaut.
     */
    public HDesktopPane() {
        init();
    }

    /**
     * Initialise le desktop pane avec ses propriétés de base.
     */
    private void init() {
        updateUI();
    }

    /**
     * Met à jour l'interface utilisateur du desktop pane.
     * Installe le HBasicDesktopPaneUI personnalisé et configure les propriétés de rendu.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicDesktopPaneUI());
        setOpaque(false);
    }

    // ========== GETTERS ET SETTERS ==========

    /**
     * Retourne la bordure personnalisée du desktop pane.
     * 
     * @return la bordure HBorder, ou null si aucune bordure n'est définie
     */
    public HBorder getHBorder() {
        return hBorder;
    }

    /**
     * Définit une bordure personnalisée pour le desktop pane.
     * 
     * @param border la nouvelle bordure à appliquer
     */
    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
    }

    /**
     * Retourne l'ombre personnalisée du desktop pane.
     * 
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() {
        return hShadow;
    }

    /**
     * Définit une ombre personnalisée pour le desktop pane.
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
     * @param radius le nouveau rayon en pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Retourne le style visuel actuel du desktop pane.
     * 
     * @return le HDesktopPaneStyle appliqué
     */
    public HDesktopPaneStyle getDesktopStyle() {
        return desktopStyle;
    }

    /**
     * Définit le style visuel du desktop pane.
     * 
     * @param style le nouveau style à appliquer
     */
    public void setDesktopStyle(HDesktopPaneStyle style) {
        this.desktopStyle = style;
        repaint();
    }

    /**
     * Indique si le dégradé est activé.
     * 
     * @return true si le dégradé est activé
     */
    public boolean isUseGradient() {
        return useGradient;
    }

    /**
     * Active ou désactive le dégradé.
     * 
     * @param useGradient true pour activer le dégradé
     */
    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
        repaint();
    }

    /**
     * Indique si le motif est affiché.
     * 
     * @return true si le motif est affiché
     */
    public boolean isShowPattern() {
        return showPattern;
    }

    /**
     * Active ou désactive l'affichage du motif.
     * 
     * @param showPattern true pour afficher le motif
     */
    public void setShowPattern(boolean showPattern) {
        this.showPattern = showPattern;
        repaint();
    }

    /**
     * Retourne le type de motif actuel.
     * 
     * @return le type de motif
     */
    public PatternType getPatternType() {
        return patternType;
    }

    /**
     * Définit le type de motif.
     * 
     * @param patternType le nouveau type de motif
     */
    public void setPatternType(PatternType patternType) {
        this.patternType = patternType;
        repaint();
    }

    /**
     * Retourne la couleur du motif.
     * 
     * @return la couleur du motif
     */
    public java.awt.Color getPatternColor() {
        return patternColor;
    }

    /**
     * Définit la couleur du motif.
     * 
     * @param color la nouvelle couleur du motif
     */
    public void setPatternColor(java.awt.Color color) {
        this.patternColor = color;
        repaint();
    }

    /**
     * Retourne l'espacement du motif.
     * 
     * @return l'espacement en pixels
     */
    public int getPatternSpacing() {
        return patternSpacing;
    }

    /**
     * Définit l'espacement du motif.
     * 
     * @param spacing le nouvel espacement en pixels
     */
    public void setPatternSpacing(int spacing) {
        this.patternSpacing = spacing;
        repaint();
    }

    /**
     * Méthode factory pour créer un HDesktopPane avec un style prédéfini.
     * 
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HDesktopPane configurée avec le style spécifié
     */
    public static HDesktopPane withStyle(HDesktopPaneStyle style) {
        HDesktopPane desktop = new HDesktopPane();
        desktop.setDesktopStyle(style);
        return desktop;
    }

    /**
     * Méthode factory pour créer un HDesktopPane avec style et motif.
     * 
     * @param style le style visuel à appliquer
     * @param patternType le type de motif à afficher
     * @return une nouvelle instance de HDesktopPane configurée
     */
    public static HDesktopPane withStyleAndPattern(HDesktopPaneStyle style, PatternType patternType) {
        HDesktopPane desktop = new HDesktopPane();
        desktop.setDesktopStyle(style);
        desktop.setShowPattern(true);
        desktop.setPatternType(patternType);
        return desktop;
    }
}