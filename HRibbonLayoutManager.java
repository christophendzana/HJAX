package hcomponents.HRibbon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LayoutManager pour HRibbon.
 * 
 * Positionne les groupes horizontalement et les composants à l'intérieur des groupes.
 * S'inspire du ruban Microsoft Office : groupes côte à côte, composants organisés
 * en lignes à l'intérieur de chaque groupe.
 * 
 * Architecture :
 * 1. Calcule la largeur de chaque groupe
 * 2. Pour chaque composant, trouve sa position dans HRibbonModel
 * 3. Positionne le composant selon son groupe et sa position
 */
public class HRibbonLayoutManager implements LayoutManager2 {
    
    // =========================================================================
    // CHAMPS
    // =========================================================================
    
    /** Référence vers le ruban géré. */
    private final HRibbon ribbon;
    
    /** Mode de répartition : true = tous les groupes ont la même largeur. */
    private boolean equalDistribution = false;
    
    /** Cache des limites des groupes pour éviter de recalculer à chaque paint. */
    private Rectangle[] groupBoundsCache = null;
    
    /** Cache des composants par groupe pour optimisation. */
    private Map<Integer, List<Component>> componentsByGroupCache = null;
    
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    
    /**
     * Constructeur.
     * 
     * @param ribbon le ruban à gérer
     * @throws IllegalArgumentException si ribbon est null
     */
    public HRibbonLayoutManager(HRibbon ribbon) {
        if (ribbon == null) {
            throw new IllegalArgumentException("Le ruban ne peut pas être null");
        }
        this.ribbon = ribbon;
    }
    
    // =========================================================================
    // IMPLÉMENTATION DE LayoutManager2
    // =========================================================================
    
    /**
     * Positionne tous les composants dans le ruban.
     * Méthode principale appelée par Swing lors du layout.
     * 
     * @param parent le conteneur parent (doit être un HRibbon)
     */
    @Override
    public void layoutContainer(Container parent) {
        // Vérification de type
        if (!(parent instanceof HRibbon)) {
            return;
        }
        
        HRibbon hRibbon = (HRibbon) parent;
        
        // Récupère les modèles nécessaires
        HRibbonModel model = hRibbon.getModel();
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        
        if (model == null || groupModel == null) {
            return; // Pas de données à afficher
        }
        
        // Invalide les caches
        groupBoundsCache = null;
        componentsByGroupCache = null;
        
        // Récupère les marges du ruban
        Insets insets = parent.getInsets();
        
        // Calcule l'espace disponible
        int availableWidth = parent.getWidth() - insets.left - insets.right;
        int availableHeight = parent.getHeight() - insets.top - insets.bottom;
        
        // Nombre de groupes
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return; // Aucun groupe à afficher
        }
        
        // 1. CALCUL DES LARGEURS DE CHAQUE GROUPE
        int[] groupWidths = calculateGroupWidths(groupModel, availableWidth);
        
        // 2. CALCUL DES POSITIONS DES GROUPES
        Rectangle[] groupBounds = calculateGroupBounds(groupWidths, groupModel, insets, availableHeight);
        groupBoundsCache = groupBounds; // Mise en cache
        
        // 3. ORGANISATION DES COMPOSANTS PAR GROUPE
        Map<Integer, List<Component>> componentsByGroup = organizeComponentsByGroup(hRibbon, model);
        componentsByGroupCache = componentsByGroup; // Mise en cache
        
        // 4. POSITIONNEMENT DES COMPOSANTS DANS CHAQUE GROUPE
        positionAllComponents(componentsByGroup, groupBounds, groupModel);
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
        int widthForGroups = Math.max(availableWidth - totalMargin, 0);
        
        if (equalDistribution) {
            // MODE ÉGALITAIRE : tous les groupes ont la même largeur
            int widthPerGroup = widthForGroups / groupCount;
            for (int i = 0; i < groupCount; i++) {
                widths[i] = Math.max(widthPerGroup, 0);
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
                System.arraycopy(preferredWidths, 0, widths, 0, groupCount);
            } else {
                // Pas assez de place : on réduit proportionnellement
                for (int i = 0; i < groupCount; i++) {
                    float ratio = (float) preferredWidths[i] / totalPreferredWidth;
                    widths[i] = (int) (widthForGroups * ratio);
                }
            }
        }
        
        // Applique les contraintes min/max de chaque groupe
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                // Largeur minimale
                if (group.getMinWidth() > 0) {
                    widths[i] = Math.max(widths[i], group.getMinWidth());
                }
                
                // Largeur maximale
                if (group.getMaxWidth() > 0) {
                    widths[i] = Math.min(widths[i], group.getMaxWidth());
                }
            }
            
            // Assure une largeur positive
            widths[i] = Math.max(widths[i], 1);
        }
        
        return widths;
    }
    
    /**
     * Calcule les limites (bounds) de chaque groupe.
     * 
     * @param groupWidths largeurs calculées pour chaque groupe
     * @param groupModel le modèle des groupes
     * @param insets les marges du ruban
     * @param availableHeight hauteur disponible
     * @return tableau des rectangles définissant chaque groupe
     */
    private Rectangle[] calculateGroupBounds(int[] groupWidths, HRibbonGroupModel groupModel,
                                            Insets insets, int availableHeight) {
        int groupCount = groupModel.getGroupCount();
        Rectangle[] bounds = new Rectangle[groupCount];
        
        // Position X de départ (après la marge gauche)
        int currentX = insets.left;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        for (int i = 0; i < groupCount; i++) {
            int groupWidth = groupWidths[i];
            
            // Crée le rectangle pour ce groupe
            bounds[i] = new Rectangle(
                currentX,              // x
                insets.top,           // y
                groupWidth,           // width
                availableHeight       // height
            );
            
            // Avance pour le prochain groupe
            currentX += groupWidth + groupMargin;
        }
        
        return bounds;
    }
    
    /**
     * Organise les composants du HRibbon par groupe.
     * Parcourt le modèle pour associer chaque composant à son groupe.
     * 
     * @param hRibbon le ruban
     * @param model le modèle de données
     * @return map groupIndex -> liste des composants dans ce groupe
     */
    private Map<Integer, List<Component>> organizeComponentsByGroup(HRibbon hRibbon, HRibbonModel model) {
        Map<Integer, List<Component>> componentsByGroup = new HashMap<>();
        
        // Parcourt tous les groupes du modèle
        for (int groupIndex = 0; groupIndex < model.getGroupCount(); groupIndex++) {
            List<Component> groupComponents = new ArrayList<>();
            
            // Parcourt toutes les positions dans ce groupe
            int valueCount = model.getValueCount(groupIndex);
            for (int position = 0; position < valueCount; position++) {
                Object value = model.getValueAt(position, groupIndex);
                if (value instanceof Component) {
                    Component comp = (Component) value;
                    
                    // Vérifie que le composant est bien un enfant du HRibbon
                    if (comp.getParent() == hRibbon) {
                        groupComponents.add(comp);
                    }
                }
            }
            
            // Ajoute la liste au map (même si vide)
            componentsByGroup.put(groupIndex, groupComponents);
        }
        
        return componentsByGroup;
    }
    
    /**
     * Positionne tous les composants dans leurs groupes respectifs.
     * 
     * @param componentsByGroup map des composants par groupe
     * @param groupBounds limites de chaque groupe
     * @param groupModel le modèle des groupes
     */
    private void positionAllComponents(Map<Integer, List<Component>> componentsByGroup,
                                      Rectangle[] groupBounds,
                                      HRibbonGroupModel groupModel) {
        // Pour chaque groupe
        for (int groupIndex = 0; groupIndex < groupBounds.length; groupIndex++) {
            Rectangle groupRect = groupBounds[groupIndex];
            List<Component> components = componentsByGroup.get(groupIndex);
            
            if (components == null || components.isEmpty() || groupRect == null) {
                continue; // Groupe vide ou inexistant
            }
            
            // Récupère la configuration du groupe
            HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
            if (group == null) {
                continue; // Groupe non trouvé
            }
            
            // Positionne les composants dans ce groupe
            layoutComponentsInGroup(components, groupRect, group);
        }
    }
    
    /**
     * Positionne les composants à l'intérieur d'un groupe.
     * Les composants sont organisés en lignes (comme un FlowLayout vertical).
     * 
     * @param components liste des composants à positionner
     * @param groupRect limites du groupe
     * @param group configuration du groupe (padding, spacing)
     */
    private void layoutComponentsInGroup(List<Component> components,
                                        Rectangle groupRect,
                                        HRibbonGroup group) {
        if (components.isEmpty()) {
            return; // Rien à positionner
        }
        
        // Récupère les paramètres du groupe
        int padding = group.getPadding();
        int spacing = group.getComponentSpacing();
        
        // Limites intérieures du groupe (avec padding)
        int innerX = groupRect.x + padding;
        int innerY = groupRect.y + padding;
        int innerWidth = groupRect.width - (2 * padding);
        int innerHeight = groupRect.height - (2 * padding);
        
        // Position courante à l'intérieur du groupe
        int currentX = innerX;
        int currentY = innerY;
        
        // Hauteur de la ligne courante
        int lineHeight = 0;
        
        for (Component comp : components) {
            // Taille préférée du composant
            Dimension prefSize = comp.getPreferredSize();
            int compWidth = prefSize.width;
            int compHeight = prefSize.height;
            
            // Vérifie si le composant rentre sur la ligne actuelle
            boolean fitsOnCurrentLine = (currentX + compWidth) <= (innerX + innerWidth);
            
            if (!fitsOnCurrentLine && currentX > innerX) {
                // Le composant ne rentre pas : passe à la ligne suivante
                currentX = innerX;
                currentY += lineHeight + spacing;
                lineHeight = 0;
            }
            
            // Ajuste la largeur si nécessaire (composant trop large)
            if (compWidth > innerWidth) {
                compWidth = innerWidth;
            }
            
            // Positionne le composant
            comp.setBounds(currentX, currentY, compWidth, compHeight);
            
            // Avance la position X
            currentX += compWidth + spacing;
            
            // Met à jour la hauteur de ligne
            lineHeight = Math.max(lineHeight, compHeight);
            
            // Vérifie si on dépasse la hauteur du groupe
            if (currentY + lineHeight > innerY + innerHeight) {
                // Pas assez de place verticale, on arrête
                break;
            }
        }
    }
    
    // =========================================================================
    // CALCUL DES TAILLES PRÉFÉRÉES, MINIMALES ET MAXIMALES
    // =========================================================================
    
    /**
     * Calcule la taille préférée du ruban.
     * 
     * @param parent le conteneur parent
     * @return la taille préférée
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
                insets.top + insets.bottom + 80 // Hauteur par défaut
            );
        }
        
        // 1. Calcule la largeur totale préférée
        int totalPreferredWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalPreferredWidth += group.getPreferredWidth();
            } else {
                totalPreferredWidth += 100; // Largeur par défaut
            }
            
            // Ajoute la marge entre groupes (sauf après le dernier)
            if (i < groupCount - 1) {
                totalPreferredWidth += groupMargin;
            }
        }
        
        // 2. Calcule la hauteur préférée (basée sur le plus grand composant)
        int maxComponentHeight = calculateMaxComponentHeight(hRibbon);
        int preferredHeight = Math.max(maxComponentHeight, 80); // Minimum 80px
        
        // 3. Ajoute les marges
        return new Dimension(
            totalPreferredWidth + insets.left + insets.right,
            preferredHeight + insets.top + insets.bottom
        );
    }
    
    /**
     * Calcule la taille minimale du ruban.
     * 
     * @param parent le conteneur parent
     * @return la taille minimale
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
                insets.top + insets.bottom + 50 // Hauteur minimale
            );
        }
        
        // Largeur minimale : somme des largeurs minimales
        int totalMinWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalMinWidth += Math.max(group.getMinWidth(), 30); // Minimum 30px
            } else {
                totalMinWidth += 50; // Largeur minimale par défaut
            }
            
            if (i < groupCount - 1) {
                totalMinWidth += groupMargin;
            }
        }
        
        return new Dimension(
            totalMinWidth + insets.left + insets.right,
            50 + insets.top + insets.bottom // Hauteur minimale fixe
        );
    }
    
    /**
     * Calcule la taille maximale du ruban.
     * 
     * @param target le conteneur parent
     * @return la taille maximale (presque infinie)
     */
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    /**
     * Calcule la hauteur maximale des composants dans le ruban.
     * 
     * @param hRibbon le ruban
     * @return la hauteur maximale trouvée
     */
    private int calculateMaxComponentHeight(HRibbon hRibbon) {
        int maxHeight = 0;
        HRibbonModel model = hRibbon.getModel();
        
        if (model == null) {
            return 80; // Hauteur par défaut
        }
        
        // Parcourt tous les composants du modèle
        for (int groupIndex = 0; groupIndex < model.getGroupCount(); groupIndex++) {
            int valueCount = model.getValueCount(groupIndex);
            for (int position = 0; position < valueCount; position++) {
                Object value = model.getValueAt(position, groupIndex);
                if (value instanceof Component) {
                    Component comp = (Component) value;
                    Dimension prefSize = comp.getPreferredSize();
                    maxHeight = Math.max(maxHeight, prefSize.height);
                }
            }
        }
        
        // Ajoute le padding des groupes
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        if (groupModel != null && groupModel.getGroupCount() > 0) {
            HRibbonGroup firstGroup = groupModel.getHRibbonGroup(0);
            if (firstGroup != null) {
                maxHeight += firstGroup.getPadding() * 2; // Padding haut + bas
            }
        }
        
        return Math.max(maxHeight, 30); // Minimum 30px
    }
    
    // =========================================================================
    // MÉTHODES D'UTILITÉ POUR L'UI
    // =========================================================================
    
    /**
     * Retourne les limites des groupes (calculées lors du dernier layout).
     * 
     * @return tableau des rectangles des groupes, ou null si non calculé
     */
    public Rectangle[] getGroupBounds() {
        return groupBoundsCache;
    }
    
    /**
     * Retourne les composants organisés par groupe.
     * 
     * @return map des composants par groupe, ou null si non calculé
     */
    public Map<Integer, List<Component>> getComponentsByGroup() {
        return componentsByGroupCache;
    }
    
    /**
     * Définit le mode de répartition des largeurs de groupe.
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
     * 
     * @return true si les groupes ont des largeurs égales
     */
    public boolean isEqualDistribution() {
        return equalDistribution;
    }
    
    // =========================================================================
    // MÉTHODES NON UTILISÉES (obligatoires pour LayoutManager2)
    // =========================================================================
    
    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Les composants sont ajoutés via le modèle, pas ici
    }
    
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        // Les composants sont ajoutés via le modèle, pas ici
    }
    
    @Override
    public void removeLayoutComponent(Component comp) {
        // Les composants sont retirés via le modèle, pas ici
    }
    
    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f; // Centré horizontalement
    }
    
    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f; // Centré verticalement
    }
    
    @Override
    public void invalidateLayout(Container target) {
        // Invalide les caches
        groupBoundsCache = null;
        componentsByGroupCache = null;
    }
}