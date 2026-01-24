package hcomponents.HRibbon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;

/**
 * LayoutManager pour HRibbon.
 * 
 * Ce manager positionne les groupes horizontalement dans le ruban.
 * À l'intérieur de chaque groupe, les composants sont placés ligne par ligne.
 * Si un composant ne rentre pas sur la ligne, il passe à la ligne suivante.
 * 
 * C'est comme le ruban de Microsoft Word.
 */
public class HRibbonLayoutManager implements LayoutManager2 {
    
    // Référence vers le ruban
    private final HRibbon ribbon;
    
    // Mode de répartition : true = tous les groupes ont la même largeur
    private boolean equalDistribution = false;
    
    /**
     * Constructeur.
     * 
     * @param ribbon le ruban à gérer
     */
    public HRibbonLayoutManager(HRibbon ribbon) {
        // Vérifie que le ruban n'est pas null
        if (ribbon == null) {
            throw new IllegalArgumentException("Le ruban ne peut pas être null");
        }
        this.ribbon = ribbon;
    }
    
    /**
     * Cette méthode est appelée quand le ruban doit être redessiné.
     * Elle positionne tous les composants aux bonnes places.
     */
    @Override
    public void layoutContainer(Container parent) {
        // Vérifie que c'est bien un HRibbon
        if (!(parent instanceof HRibbon)) {
            return;
        }
        
        HRibbon hRibbon = (HRibbon) parent;
        
        // Récupère le modèle des groupes
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        if (groupModel == null) {
            return; // Pas de groupes à afficher
        }
        
        // Récupère les marges du ruban
        Insets insets = parent.getInsets();
        
        // Calcule l'espace disponible (largeur totale - marges)
        int availableWidth = parent.getWidth() - insets.left - insets.right;
        int availableHeight = parent.getHeight() - insets.top - insets.bottom;
        
        // Nombre de groupes
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return; // Aucun groupe à afficher
        }
        
        // 1. CALCUL DES LARGEURS DE CHAQUE GROUPE
        int[] groupWidths = calculateGroupWidths(groupModel, availableWidth);
        
        // 2. POSITIONNEMENT DES GROUPES ET DE LEURS COMPOSANTS
        positionGroupsAndComponents(hRibbon, groupModel, groupWidths, insets, availableHeight);
    }
    
    /**
     * Calcule la largeur de chaque groupe.
     * 
     * @param groupModel le modèle des groupes
     * @param availableWidth largeur disponible totale
     * @return tableau des largeurs pour chaque groupe
     */
    private int[] calculateGroupWidths(HRibbonGroupModel groupModel, int availableWidth) {
        int groupCount = groupModel.getGroupCount();
        int[] widths = new int[groupCount];
        
        // Marge entre les groupes
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        // Largeur disponible pour les groupes (total - marges entre groupes)
        int totalMargin = groupMargin * (groupCount - 1);
        int widthForGroups = availableWidth - totalMargin;
        
        if (equalDistribution) {
            // MODE ÉGALITAIRE : tous les groupes ont la même largeur
            int widthPerGroup = widthForGroups / groupCount;
            for (int i = 0; i < groupCount; i++) {
                widths[i] = widthPerGroup;
            }
        } else {
            // MODE LARGEUR PRÉFÉRÉE : chaque groupe a sa largeur préférée
            int totalPreferredWidth = 0;
            int[] preferredWidths = new int[groupCount];
            
            // Récupère les largeurs préférées de chaque groupe
            for (int i = 0; i < groupCount; i++) {
                HRibbonGroup group = groupModel.getHRibbonGroup(i);
                if (group != null) {
                    preferredWidths[i] = group.getPreferredWidth();
                } else {
                    preferredWidths[i] = 100; // Largeur par défaut
                }
                totalPreferredWidth += preferredWidths[i];
            }
            
            if (totalPreferredWidth <= widthForGroups) {
                // Assez de place : on utilise les largeurs préférées
                for (int i = 0; i < groupCount; i++) {
                    widths[i] = preferredWidths[i];
                }
            } else {
                // Pas assez de place : on réduit proportionnellement
                for (int i = 0; i < groupCount; i++) {
                    float ratio = (float) preferredWidths[i] / totalPreferredWidth;
                    widths[i] = (int) (widthForGroups * ratio);
                }
            }
        }
        
        // Assure que chaque groupe a au moins sa largeur minimale
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                widths[i] = Math.max(widths[i], group.getMinWidth());
                
                // Ne pas dépasser la largeur maximale
                if (group.getMaxWidth() > 0) {
                    widths[i] = Math.min(widths[i], group.getMaxWidth());
                }
            }
        }
        
        return widths;
    }
    
    /**
     * Positionne les groupes et leurs composants.
     */
    private void positionGroupsAndComponents(HRibbon ribbon, 
                                           HRibbonGroupModel groupModel,
                                           int[] groupWidths,
                                           Insets insets,
                                           int availableHeight) {
        int groupCount = groupModel.getGroupCount();
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        // Position X de départ (après la marge gauche)
        int currentX = insets.left;
        
        // Pour chaque groupe
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
            if (group == null) {
                // Passe au groupe suivant
                currentX += groupWidths[groupIndex] + groupMargin;
                continue;
            }
            
            // Largeur de ce groupe
            int groupWidth = groupWidths[groupIndex];
            
            // Récupère les composants de ce groupe
            List<Component> components = getComponentsForGroup(ribbon, groupIndex);
            
            // Positionne les composants dans le groupe
            int groupHeight = layoutComponentsInGroup(components, 
                                                     currentX, 
                                                     insets.top, 
                                                     groupWidth, 
                                                     group);
            
            // Met à jour la largeur actuelle du groupe
            group.setWidth(groupWidth);
            
            // Passe au groupe suivant
            currentX += groupWidth;
            if (groupIndex < groupCount - 1) {
                currentX += groupMargin; // Ajoute la marge entre groupes
            }
        }
    }
    
    /**
     * Récupère les composants d'un groupe.
     */
    private List<Component> getComponentsForGroup(HRibbon ribbon, int groupIndex) {
        List<Component> components = new ArrayList<>();
        
        // Récupère le modèle de données
        HRibbonModel model = ribbon.getModel();
        if (model == null) {
            return components; // Liste vide
        }
        
        // Nombre de composants dans ce groupe
        int componentCount = model.getValueCount(groupIndex);
        
        // Pour chaque position dans le groupe
        for (int i = 0; i < componentCount; i++) {
            // Récupère la valeur (qui devrait être un Component)
            Object value = model.getValueAt(i, groupIndex);
            
            // Vérifie que c'est bien un Component Swing
            if (value instanceof Component) {
                components.add((Component) value);
            }
        }
        
        return components;
    }
    
    /**
     * Positionne les composants à l'intérieur d'un groupe.
     * 
     * @param components liste des composants à positionner
     * @param groupX position X du groupe (dans le ruban)
     * @param groupY position Y du groupe (dans le ruban)
     * @param groupWidth largeur du groupe
     * @param groupe le groupe contenant les composants
     * @return la hauteur utilisée par le groupe
     */
    private int layoutComponentsInGroup(List<Component> components,
                                       int groupX,
                                       int groupY,
                                       int groupWidth,
                                       HRibbonGroup groupe) {
        if (components.isEmpty()) {
            return 0; // Groupe vide
        }
        
        // Récupère les paramètres du groupe
        int padding = groupe.getPadding(); // Marge interne
        int spacing = groupe.getComponentSpacing(); // Espace entre composants
        
        // Position de départ à l'intérieur du groupe (avec padding)
        int currentX = groupX + padding;
        int currentY = groupY + padding;
        
        // Largeur disponible pour les composants (moins les padding)
        int availableWidth = groupWidth - (2 * padding);
        
        // Hauteur de la ligne actuelle
        int lineHeight = 0;
        // Hauteur totale du groupe
        int totalHeight = 0;
        
        // Pour chaque composant
        for (Component comp : components) {
            // Taille préférée du composant
            Dimension prefSize = comp.getPreferredSize();
            int compWidth = prefSize.width;
            int compHeight = prefSize.height;
            
            // Vérifie si le composant rentre sur la ligne actuelle
            boolean fitsOnCurrentLine = (currentX + compWidth) <= (groupX + groupWidth - padding);
            
            if (!fitsOnCurrentLine && currentX > (groupX + padding)) {
                // Le composant ne rentre pas : on passe à la ligne suivante
                currentX = groupX + padding; // Retour au début
                currentY += lineHeight + spacing; // Descend d'une ligne
                totalHeight += lineHeight + spacing;
                lineHeight = 0; // Réinitialise la hauteur de ligne
            }
            
            // Positionne le composant
            comp.setBounds(currentX, currentY, compWidth, compHeight);
            
            // Avance la position X pour le prochain composant
            currentX += compWidth + spacing;
            
            // Met à jour la hauteur de la ligne
            lineHeight = Math.max(lineHeight, compHeight);
        }
        
        // Ajoute la hauteur de la dernière ligne
        totalHeight += lineHeight;
        
        // Ajoute les padding en haut et en bas
        return totalHeight + (2 * padding);
    }
    
    /**
     * Calcule la taille préférée du ruban.
     * C'est la taille que le ruban aimerait avoir.
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        if (!(parent instanceof HRibbon)) {
            return new Dimension(0, 0);
        }
        
        HRibbon hRibbon = (HRibbon) parent;
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        
        if (groupModel == null) {
            return new Dimension(0, 0);
        }
        
        Insets insets = parent.getInsets();
        int groupCount = groupModel.getGroupCount();
        
        if (groupCount == 0) {
            return new Dimension(
                insets.left + insets.right,
                insets.top + insets.bottom
            );
        }
        
        // Largeur totale : somme des largeurs préférées + marges
        int totalWidth = 0;
        int maxHeight = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalWidth += group.getPreferredWidth();
                
                // Hauteur : on prend la hauteur par défaut pour l'instant
                maxHeight = Math.max(maxHeight, 80); // Hauteur typique d'un groupe
            }
            
            // Ajoute la marge entre groupes (sauf après le dernier)
            if (i < groupCount - 1) {
                totalWidth += groupMargin;
            }
        }
        
        // Ajoute les marges du ruban
        return new Dimension(
            totalWidth + insets.left + insets.right,
            maxHeight + insets.top + insets.bottom
        );
    }
    
    /**
     * Calcule la taille minimale du ruban.
     * C'est la taille en dessous de laquelle on ne peut pas aller.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        if (!(parent instanceof HRibbon)) {
            return new Dimension(0, 0);
        }
        
        HRibbon hRibbon = (HRibbon) parent;
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        
        if (groupModel == null) {
            return new Dimension(0, 0);
        }
        
        Insets insets = parent.getInsets();
        int groupCount = groupModel.getGroupCount();
        
        if (groupCount == 0) {
            return new Dimension(
                insets.left + insets.right,
                insets.top + insets.bottom
            );
        }
        
        // Largeur minimale : somme des largeurs minimales
        int totalMinWidth = 0;
        int minHeight = 50; // Hauteur minimale raisonnable
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalMinWidth += group.getMinWidth();
            } else {
                totalMinWidth += 50; // Largeur minimale par défaut
            }
            
            // Ajoute la marge entre groupes
            if (i < groupCount - 1) {
                totalMinWidth += groupMargin;
            }
        }
        
        return new Dimension(
            totalMinWidth + insets.left + insets.right,
            minHeight + insets.top + insets.bottom
        );
    }
    
    /**
     * Calcule la taille maximale du ruban.
     * En théorie, le ruban peut être très grand.
     */
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    /**
     * Ajoute un composant avec un nom.
     * Non utilisé dans notre cas car les composants sont gérés par le modèle.
     */
    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Les composants sont ajoutés via le modèle, pas ici
    }
    
    /**
     * Ajoute un composant avec des contraintes.
     * Non utilisé dans notre cas.
     */
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        // Les composants sont ajoutés via le modèle, pas ici
    }
    
    /**
     * Supprime un composant.
     * Non utilisé dans notre cas.
     */
    @Override
    public void removeLayoutComponent(Component comp) {
        // Les composants sont supprimés via le modèle, pas ici
    }
    
    /**
     * Alignement horizontal.
     * 0.5 = centré.
     */
    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }
    
    /**
     * Alignement vertical.
     * 0.5 = centré.
     */
    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }
    
    /**
     * Invalide le layout (force un recalcul).
     */
    @Override
    public void invalidateLayout(Container target) {
        // Force un nouveau calcul au prochain layout
    }
    
    /**
     * Définit le mode de répartition.
     * 
     * @param equal true pour largeurs égales, false pour largeurs préférées
     */
    public void setEqualDistribution(boolean equal) {
        if (this.equalDistribution != equal) {
            this.equalDistribution = equal;
            if (ribbon != null) {
                ribbon.revalidate(); // Force un redessin
            }
        }
    }
    
    /**
     * Retourne le mode de répartition actuel.
     */
    public boolean isEqualDistribution() {
        return equalDistribution;
    }
}