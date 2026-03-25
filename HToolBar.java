/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicToolBarUI;
import hcomponents.vues.HToolBarStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.*;
import java.awt.*;

/**
 * Composant HToolBar - Une barre d'outils Swing personnalisée avec design moderne.
 * Étend JToolBar pour offrir des fonctionnalités avancées : styles prédéfinis,
 * coins arrondis, ombres, séparateurs stylés et orientation configurable.
 * 
 * <p>Ce composant fournit une API cohérente avec les autres composants HComponents
 * (HButton, HDialog, HFrame) pour une expérience utilisateur uniforme.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JToolBar
 * @see HToolBarStyle
 * @see HBasicToolBarUI
 */
public class HToolBar extends JToolBar {

    /** Bordure personnalisée de la toolbar */
    private HBorder hBorder;
    
    /** Ombre personnalisée de la toolbar */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 12;
    
    /** Style visuel appliqué à la toolbar */
    private HToolBarStyle toolBarStyle = HToolBarStyle.PRIMARY;
    
    /** Espacement entre les composants (en pixels) */
    private int componentSpacing = 5;
    
    /** Hauteur de la toolbar en orientation horizontale */
    private int toolBarHeight = 50;
    
    /** Largeur de la toolbar en orientation verticale */
    private int toolBarWidth = 60;
    
    /** Indique si la toolbar affiche des dégradés */
    private boolean useGradient = true;

    /**
     * Constructeur par défaut.
     * Crée une toolbar horizontale.
     */
    public HToolBar() {
        this(HORIZONTAL);
    }

    /**
     * Constructeur avec orientation.
     * 
     * @param orientation l'orientation (HORIZONTAL ou VERTICAL)
     */
    public HToolBar(int orientation) {
        super(orientation);
        init();
    }

    /**
     * Constructeur avec nom.
     * 
     * @param name le nom de la toolbar
     */
    public HToolBar(String name) {
        super(name);
        init();
    }

    /**
     * Constructeur avec nom et orientation.
     * 
     * @param name le nom de la toolbar
     * @param orientation l'orientation (HORIZONTAL ou VERTICAL)
     */
    public HToolBar(String name, int orientation) {
        super(name, orientation);
        init();
    }

    /**
     * Initialise la toolbar avec ses propriétés de base.
     */
    private void init() {
        updateUI();
        
        // Définir la taille selon l'orientation
        if (getOrientation() == HORIZONTAL) {
            setPreferredSize(new Dimension(0, toolBarHeight));
        } else {
            setPreferredSize(new Dimension(toolBarWidth, 0));
        }
    }

    /**
     * Met à jour l'interface utilisateur de la toolbar.
     * Installe le HBasicToolBarUI personnalisé et configure les propriétés de rendu.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicToolBarUI());
        setBorderPainted(false);
        setOpaque(false);
        setFloatable(false);
        
        // Configuration du layout avec espacement
        if (getOrientation() == HORIZONTAL) {
            setLayout(new FlowLayout(FlowLayout.LEFT, componentSpacing, 
                (toolBarHeight - 40) / 2));
        } else {
            setLayout(new FlowLayout(FlowLayout.CENTER, 
                (toolBarWidth - 40) / 2, componentSpacing));
        }
    }

    /**
     * Ajoute un séparateur stylé à la toolbar.
     * Le séparateur s'adapte à l'orientation de la toolbar.
     */
    @Override
    public void addSeparator() {
        addSeparator(null);
    }

    /**
     * Ajoute un séparateur stylé avec dimension personnalisée.
     * 
     * @param size la dimension du séparateur (peut être null)
     */
    @Override
    public void addSeparator(Dimension size) {
        JSeparator separator = new JSeparator(
            getOrientation() == HORIZONTAL ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL
        ) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Couleur du séparateur
                g2.setColor(toolBarStyle.getSeparatorColor());
                
                if (getOrientation() == SwingConstants.VERTICAL) {
                    // Séparateur vertical (ligne dans toolbar horizontale)
                    int x1 = getWidth() / 2;
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(x1, 5, x1, getHeight() - 5);
                } else {
                    // Séparateur horizontal (ligne dans toolbar verticale)
                    int y1 = getHeight() / 2;
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(5, y1, getWidth() - 5, y1);
                }
                
                g2.dispose();
            }
        };
        
        // Définir la taille du séparateur
        if (size != null) {
            separator.setPreferredSize(size);
        } else {
            if (getOrientation() == HORIZONTAL) {
                separator.setPreferredSize(new Dimension(2, toolBarHeight - 10));
            } else {
                separator.setPreferredSize(new Dimension(toolBarWidth - 10, 2));
            }
        }
        
        separator.setOpaque(false);
        add(separator);
    }

    /**
     * Ajoute un espacement flexible entre les composants.
     * Utile pour pousser les composants suivants vers la droite/bas.
     */
    public void addGlue() {
        if (getOrientation() == HORIZONTAL) {
            add(Box.createHorizontalGlue());
        } else {
            add(Box.createVerticalGlue());
        }
    }

    /**
     * Ajoute un espace rigide de taille fixe.
     * 
     * @param size la taille de l'espace en pixels
     */
    public void addSpace(int size) {
        if (getOrientation() == HORIZONTAL) {
            add(Box.createRigidArea(new Dimension(size, 0)));
        } else {
            add(Box.createRigidArea(new Dimension(0, size)));
        }
    }

    // ========== GETTERS ET SETTERS ==========

    /**
     * Retourne la bordure personnalisée de la toolbar.
     * 
     * @return la bordure HBorder, ou null si aucune bordure n'est définie
     */
    public HBorder getHBorder() {
        return hBorder;
    }

    /**
     * Définit une bordure personnalisée pour la toolbar.
     * 
     * @param border la nouvelle bordure à appliquer
     */
    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
    }

    /**
     * Retourne l'ombre personnalisée de la toolbar.
     * 
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() {
        return hShadow;
    }

    /**
     * Définit une ombre personnalisée pour la toolbar.
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
     * Retourne le style visuel actuel de la toolbar.
     * 
     * @return le HToolBarStyle appliqué
     */
    public HToolBarStyle getToolBarStyle() {
        return toolBarStyle;
    }

    /**
     * Définit le style visuel de la toolbar.
     * 
     * @param style le nouveau style à appliquer
     */
    public void setToolBarStyle(HToolBarStyle style) {
        this.toolBarStyle = style;
        repaint();
    }

    /**
     * Retourne l'espacement entre les composants.
     * 
     * @return l'espacement en pixels
     */
    public int getComponentSpacing() {
        return componentSpacing;
    }

    /**
     * Définit l'espacement entre les composants.
     * 
     * @param spacing le nouvel espacement en pixels
     */
    public void setComponentSpacing(int spacing) {
        this.componentSpacing = spacing;
        if (getOrientation() == HORIZONTAL) {
            setLayout(new FlowLayout(FlowLayout.LEFT, spacing, 
                (toolBarHeight - 40) / 2));
        } else {
            setLayout(new FlowLayout(FlowLayout.CENTER, 
                (toolBarWidth - 40) / 2, spacing));
        }
        revalidate();
        repaint();
    }

    /**
     * Retourne la hauteur de la toolbar (orientation horizontale).
     * 
     * @return la hauteur en pixels
     */
    public int getToolBarHeight() {
        return toolBarHeight;
    }

    /**
     * Définit la hauteur de la toolbar (orientation horizontale).
     * 
     * @param height la nouvelle hauteur en pixels
     */
    public void setToolBarHeight(int height) {
        this.toolBarHeight = height;
        if (getOrientation() == HORIZONTAL) {
            setPreferredSize(new Dimension(0, height));
        }
        revalidate();
        repaint();
    }

    /**
     * Retourne la largeur de la toolbar (orientation verticale).
     * 
     * @return la largeur en pixels
     */
    public int getToolBarWidth() {
        return toolBarWidth;
    }

    /**
     * Définit la largeur de la toolbar (orientation verticale).
     * 
     * @param width la nouvelle largeur en pixels
     */
    public void setToolBarWidth(int width) {
        this.toolBarWidth = width;
        if (getOrientation() == VERTICAL) {
            setPreferredSize(new Dimension(width, 0));
        }
        revalidate();
        repaint();
    }

    /**
     * Indique si les dégradés sont activés.
     * 
     * @return true si les dégradés sont activés
     */
    public boolean isUseGradient() {
        return useGradient;
    }

    /**
     * Active ou désactive les dégradés.
     * 
     * @param useGradient true pour activer les dégradés
     */
    public void setUseGradient(boolean useGradient) {
        this.useGradient = useGradient;
        repaint();
    }

    /**
     * Méthode factory pour créer une HToolBar avec un style prédéfini.
     * 
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HToolBar configurée avec le style spécifié
     */
    public static HToolBar withStyle(HToolBarStyle style) {
        HToolBar toolBar = new HToolBar();
        toolBar.setToolBarStyle(style);
        return toolBar;
    }

    /**
     * Méthode factory pour créer une HToolBar avec style et orientation.
     * 
     * @param style le style visuel à appliquer
     * @param orientation l'orientation (HORIZONTAL ou VERTICAL)
     * @return une nouvelle instance de HToolBar configurée
     */
    public static HToolBar withStyle(HToolBarStyle style, int orientation) {
        HToolBar toolBar = new HToolBar(orientation);
        toolBar.setToolBarStyle(style);
        return toolBar;
    }
}