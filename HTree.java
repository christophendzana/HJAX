/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicTreeUI;
import hcomponents.vues.HTreeStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * Composant HTree - Un arbre Swing personnalisé avec design moderne.
 * Étend JTree pour offrir des fonctionnalités avancées : styles prédéfinis,
 * nœuds avec bordures arrondies, effets hover, animations fluides, 
 * lignes de connexion stylisées et icônes personnalisées.
 * 
 * <p>Ce composant fournit une API cohérente avec les autres composants HComponents
 * pour une expérience utilisateur uniforme et moderne.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JTree
 * @see HTreeStyle
 * @see HBasicTreeUI
 */
public class HTree extends JTree {

    /** Bordure personnalisée de l'arbre */
    private HBorder hBorder;
    
    /** Ombre personnalisée de l'arbre */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis des nœuds (en pixels) */
    private int cornerRadius = 8;
    
    /** Style visuel appliqué à l'arbre */
    private HTreeStyle treeStyle = HTreeStyle.PRIMARY;
    
    /** Espacement horizontal entre l'icône et le texte (en pixels) */
    private int iconTextGap = 8;
    
    /** Espacement vertical entre les nœuds (en pixels) */
    private int nodeVerticalSpacing = 4;
    
    /** Espacement horizontal pour l'indentation des niveaux (en pixels) */
    private int levelIndent = 20;
    
    /** Épaisseur des lignes de connexion (en pixels) */
    private int connectionLineThickness = 2;
    
    /** Activer/désactiver les animations d'expansion/collapse */
    private boolean animationsEnabled = true;
    
    /** Activer/désactiver les effets hover */
    private boolean hoverEnabled = true;
    
    /** Activer/désactiver les lignes de connexion entre nœuds */
    private boolean showConnectionLines = true;
    
    /** Padding horizontal des nœuds (en pixels) */
    private int nodePaddingX = 8;
    
    /** Padding vertical des nœuds (en pixels) */
    private int nodePaddingY = 4;
    
    /** Icône personnalisée pour les nœuds parents fermés */
    private Icon customClosedIcon;
    
    /** Icône personnalisée pour les nœuds parents ouverts */
    private Icon customOpenIcon;
    
    /** Icône personnalisée pour les nœuds feuilles */
    private Icon customLeafIcon;

    /**
     * Constructeur par défaut.
     * Crée un arbre avec un modèle par défaut.
     */
    public HTree() {
        super();
        updateUI();
    }

    /**
     * Constructeur avec tableau de valeurs.
     * 
     * @param value le tableau d'objets à afficher dans l'arbre
     */
    public HTree(Object[] value) {
        super(value);
        updateUI();
    }

    /**
     * Constructeur avec vecteur de valeurs.
     * 
     * @param value le vecteur d'objets à afficher dans l'arbre
     */
    public HTree(java.util.Vector<?> value) {
        super(value);
        updateUI();
    }

    /**
     * Constructeur avec hashtable de valeurs.
     * 
     * @param value la hashtable d'objets à afficher dans l'arbre
     */
    public HTree(Hashtable<?, ?> value) {
        super(value);
        updateUI();
    }

    /**
     * Constructeur avec nœud racine.
     * 
     * @param root le nœud racine de l'arbre
     */
    public HTree(TreeNode root) {
        super(root);
        updateUI();
    }

    /**
     * Constructeur avec nœud racine et option d'affichage.
     * 
     * @param root le nœud racine de l'arbre
     * @param asksAllowsChildren si true, demande à chaque nœud s'il peut avoir des enfants
     */
    public HTree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        updateUI();
    }

    /**
     * Constructeur avec modèle d'arbre.
     * 
     * @param newModel le modèle de données de l'arbre
     */
    public HTree(TreeModel newModel) {
        super(newModel);
        updateUI();
    }

    /**
     * Met à jour l'interface utilisateur de l'arbre.
     * Installe le HBasicTreeUI personnalisé et configure les propriétés de rendu.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicTreeUI());
        setOpaque(false);
    }

    /**
     * Retourne la bordure personnalisée de l'arbre.
     * 
     * @return la bordure HBorder, ou null si aucune bordure n'est définie
     */
    public HBorder getHBorder() {
        return hBorder;
    }

    /**
     * Définit une bordure personnalisée pour l'arbre.
     * 
     * @param border la nouvelle bordure à appliquer
     */
    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
    }

    /**
     * Retourne l'ombre personnalisée de l'arbre.
     * 
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() {
        return hShadow;
    }

    /**
     * Définit une ombre personnalisée pour l'arbre.
     * 
     * @param shadow la nouvelle ombre à appliquer
     */
    public void setShadow(HShadow shadow) {
        this.hShadow = shadow;
        repaint();
    }

    /**
     * Retourne le rayon des coins arrondis des nœuds.
     * 
     * @return le rayon en pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis des nœuds.
     * 
     * @param radius le nouveau rayon en pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Retourne le style visuel actuel de l'arbre.
     * 
     * @return le HTreeStyle appliqué
     */
    public HTreeStyle getTreeStyle() {
        return treeStyle;
    }

    /**
     * Définit le style visuel de l'arbre.
     * 
     * @param style le nouveau style à appliquer
     */
    public void setTreeStyle(HTreeStyle style) {
        this.treeStyle = style;
        repaint();
    }

    /**
     * Retourne l'espacement entre l'icône et le texte.
     * 
     * @return l'espacement en pixels
     */
    public int getIconTextGap() {
        return iconTextGap;
    }

    /**
     * Définit l'espacement entre l'icône et le texte.
     * 
     * @param gap le nouvel espacement en pixels
     */
    public void setIconTextGap(int gap) {
        this.iconTextGap = gap;
        repaint();
    }

    /**
     * Retourne l'espacement vertical entre les nœuds.
     * 
     * @return l'espacement en pixels
     */
    public int getNodeVerticalSpacing() {
        return nodeVerticalSpacing;
    }

    /**
     * Définit l'espacement vertical entre les nœuds.
     * 
     * @param spacing le nouvel espacement en pixels
     */
    public void setNodeVerticalSpacing(int spacing) {
        this.nodeVerticalSpacing = spacing;
        repaint();
    }

    /**
     * Retourne l'indentation par niveau.
     * 
     * @return l'indentation en pixels
     */
    public int getLevelIndent() {
        return levelIndent;
    }

    /**
     * Définit l'indentation par niveau.
     * 
     * @param indent la nouvelle indentation en pixels
     */
    public void setLevelIndent(int indent) {
        this.levelIndent = indent;
        repaint();
    }

    /**
     * Retourne l'épaisseur des lignes de connexion.
     * 
     * @return l'épaisseur en pixels
     */
    public int getConnectionLineThickness() {
        return connectionLineThickness;
    }

    /**
     * Définit l'épaisseur des lignes de connexion.
     * 
     * @param thickness la nouvelle épaisseur en pixels
     */
    public void setConnectionLineThickness(int thickness) {
        this.connectionLineThickness = thickness;
        repaint();
    }

    /**
     * Vérifie si les animations sont activées.
     * 
     * @return true si les animations sont activées
     */
    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    /**
     * Active ou désactive les animations.
     * 
     * @param enabled true pour activer les animations
     */
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    /**
     * Vérifie si les effets hover sont activés.
     * 
     * @return true si les effets hover sont activés
     */
    public boolean isHoverEnabled() {
        return hoverEnabled;
    }

    /**
     * Active ou désactive les effets hover.
     * 
     * @param enabled true pour activer les effets hover
     */
    public void setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
    }

    /**
     * Vérifie si les lignes de connexion sont affichées.
     * 
     * @return true si les lignes de connexion sont affichées
     */
    public boolean isShowConnectionLines() {
        return showConnectionLines;
    }

    /**
     * Active ou désactive l'affichage des lignes de connexion.
     * 
     * @param show true pour afficher les lignes de connexion
     */
    public void setShowConnectionLines(boolean show) {
        this.showConnectionLines = show;
        repaint();
    }

    /**
     * Retourne l'icône personnalisée pour les nœuds fermés.
     * 
     * @return l'icône ou null
     */
    public Icon getCustomClosedIcon() {
        return customClosedIcon;
    }

    /**
     * Définit l'icône personnalisée pour les nœuds fermés.
     * 
     * @param icon la nouvelle icône
     */
    public void setCustomClosedIcon(Icon icon) {
        this.customClosedIcon = icon;
        repaint();
    }

    /**
     * Retourne l'icône personnalisée pour les nœuds ouverts.
     * 
     * @return l'icône ou null
     */
    public Icon getCustomOpenIcon() {
        return customOpenIcon;
    }

    /**
     * Définit l'icône personnalisée pour les nœuds ouverts.
     * 
     * @param icon la nouvelle icône
     */
    public void setCustomOpenIcon(Icon icon) {
        this.customOpenIcon = icon;
        repaint();
    }

    /**
     * Retourne l'icône personnalisée pour les feuilles.
     * 
     * @return l'icône ou null
     */
    public Icon getCustomLeafIcon() {
        return customLeafIcon;
    }

    /**
     * Définit l'icône personnalisée pour les feuilles.
     * 
     * @param icon la nouvelle icône
     */
    public void setCustomLeafIcon(Icon icon) {
        this.customLeafIcon = icon;
        repaint();
    }

    /**
     * Méthode factory pour créer un HTree avec un style prédéfini.
     * 
     * @param root le nœud racine de l'arbre
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HTree configurée avec le style spécifié
     */
    public static HTree withStyle(TreeNode root, HTreeStyle style) {
        HTree tree = new HTree(root);
        tree.setTreeStyle(style);
        return tree;
    }

    /**
     * Méthode factory pour créer un HTree avec un style prédéfini et un modèle.
     * 
     * @param model le modèle de données de l'arbre
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HTree configurée avec les paramètres spécifiés
     */
    public static HTree withStyle(TreeModel model, HTreeStyle style) {
        HTree tree = new HTree(model);
        tree.setTreeStyle(style);
        return tree;
    }
}