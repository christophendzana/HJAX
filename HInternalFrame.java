/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicInternalFrameUI;
import hcomponents.vues.HInternalFrameStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.*;

/**
 * Composant HInternalFrame - Une fenêtre interne Swing personnalisée avec
 * design moderne. Étend JInternalFrame pour offrir des fonctionnalités avancées
 * : barre de titre stylée, coins arrondis, ombres, boutons personnalisés avec
 * HButton.
 *
 * <p>
 * Ce composant fournit une API cohérente avec les autres composants HComponents
 * (HButton, HDialog, HFrame, HToolBar) pour une expérience utilisateur
 * uniforme.</p>
 *
 * @author FIDELE
 * @version 1.0
 * @see JInternalFrame
 * @see HInternalFrameStyle
 * @see HBasicInternalFrameUI
 */
public class HInternalFrame extends JInternalFrame {

    /**
     * Bordure personnalisée de la fenêtre interne
     */
    private HBorder hBorder;

    /**
     * Ombre personnalisée de la fenêtre interne
     */
    private HShadow hShadow;

    /**
     * Rayon des coins arrondis (en pixels)
     */
    private int cornerRadius = 15;

    /**
     * Style visuel appliqué à la fenêtre interne
     */
    private HInternalFrameStyle frameStyle = HInternalFrameStyle.PRIMARY;

    /**
     * Hauteur de la barre de titre
     */
    private int titleBarHeight = 35;

    /**
     * Constructeur par défaut. Crée une fenêtre interne sans titre, non
     * redimensionnable, non fermable, non maximisable et non iconifiable.
     */
    public HInternalFrame() {
        this("", false, false, false, false);
        this.cornerRadius = 15;
    }

    /**
     * Constructeur avec titre. Crée une fenêtre interne redimensionnable,
     * fermable, maximisable et iconifiable.
     *
     * @param title le titre de la fenêtre interne
     */
    public HInternalFrame(String title) {
        this(title, true, true, true, true);
        this.cornerRadius = 15;
    }

    /**
     * Constructeur avec titre et redimensionnement.
     *
     * @param title le titre de la fenêtre interne
     * @param resizable true si la fenêtre est redimensionnable
     */
    public HInternalFrame(String title, boolean resizable) {
        this(title, resizable, false, false, false);
        this.cornerRadius = 15;
    }

    /**
     * Constructeur avec titre, redimensionnement et fermeture.
     *
     * @param title le titre de la fenêtre interne
     * @param resizable true si la fenêtre est redimensionnable
     * @param closable true si la fenêtre est fermable
     */
    public HInternalFrame(String title, boolean resizable, boolean closable) {
        this(title, resizable, closable, false, false);
        this.cornerRadius = 15;
    }

    /**
     * Constructeur avec titre, redimensionnement, fermeture et maximisation.
     *
     * @param title le titre de la fenêtre interne
     * @param resizable true si la fenêtre est redimensionnable
     * @param closable true si la fenêtre est fermable
     * @param maximizable true si la fenêtre est maximisable
     */
    public HInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
        this(title, resizable, closable, maximizable, false);
        this.cornerRadius = 15;
    }

    /**
     * Constructeur complet.
     *
     * @param title le titre de la fenêtre interne
     * @param resizable true si la fenêtre est redimensionnable
     * @param closable true si la fenêtre est fermable
     * @param maximizable true si la fenêtre est maximisable
     * @param iconifiable true si la fenêtre est iconifiable
     */
    public HInternalFrame(String title, boolean resizable, boolean closable,
            boolean maximizable, boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);
        this.cornerRadius = 15;
        init();
    }

    /**
     * Initialise la fenêtre interne avec ses propriétés de base.
     */
    private void init() {
        updateUI();
    }

    /**
     * Met à jour l'interface utilisateur de la fenêtre interne. Installe le
     * HBasicInternalFrameUI personnalisé.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicInternalFrameUI(this));
        setOpaque(false);
    }

    // ========== GETTERS ET SETTERS ==========
    /**
     * Retourne la bordure personnalisée de la fenêtre interne.
     *
     * @return la bordure HBorder, ou null si aucune bordure n'est définie
     */
    public HBorder getHBorder() {
        return hBorder;
    }

    /**
     * Définit une bordure personnalisée pour la fenêtre interne.
     *
     * @param border la nouvelle bordure à appliquer
     */
    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
    }

    /**
     * Retourne l'ombre personnalisée de la fenêtre interne.
     *
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() {
        return hShadow;
    }

    /**
     * Définit une ombre personnalisée pour la fenêtre interne.
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
     * Retourne le style visuel actuel de la fenêtre interne.
     *
     * @return le HInternalFrameStyle appliqué
     */
    public HInternalFrameStyle getFrameStyle() {

        if (frameStyle != null) {
            return frameStyle;
        }

        return HInternalFrameStyle.PRIMARY;
    }

    /**
     * Définit le style visuel de la fenêtre interne.
     *
     * @param style le nouveau style à appliquer
     */
    public void setFrameStyle(HInternalFrameStyle style) {
        this.frameStyle = style;
        repaint();
    }

    /**
     * Retourne la hauteur de la barre de titre.
     *
     * @return la hauteur en pixels
     */
    public int getTitleBarHeight() {
        return titleBarHeight;
    }

    /**
     * Définit la hauteur de la barre de titre.
     *
     * @param height la nouvelle hauteur en pixels
     */
    public void setTitleBarHeight(int height) {
        this.titleBarHeight = height;
        revalidate();
        repaint();
    }

    @Override
    public boolean isOpaque() {
        return !isIcon(); // Transparent quand réduit pour que le desktop le peigne
    }

    /**
     * Méthode factory pour créer un HInternalFrame avec un style prédéfini.
     *
     * @param title le titre de la fenêtre interne
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HInternalFrame configurée avec le style
     * spécifié
     */
    public static HInternalFrame withStyle(String title, HInternalFrameStyle style) {
        HInternalFrame frame = new HInternalFrame(title);
        frame.setFrameStyle(style);
        return frame;
    }

    /**
     * Méthode factory pour créer un HInternalFrame avec style et options.
     *
     * @param title le titre de la fenêtre interne
     * @param style le style visuel à appliquer
     * @param resizable true si la fenêtre est redimensionnable
     * @param closable true si la fenêtre est fermable
     * @param maximizable true si la fenêtre est maximisable
     * @param iconifiable true si la fenêtre est iconifiable
     * @return une nouvelle instance de HInternalFrame configurée
     */
    public static HInternalFrame withStyle(String title, HInternalFrameStyle style,
            boolean resizable, boolean closable,
            boolean maximizable, boolean iconifiable) {
        HInternalFrame frame = new HInternalFrame(title, resizable, closable, maximizable, iconifiable);
        frame.setFrameStyle(style);
        return frame;
    }
}
