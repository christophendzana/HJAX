/*
 * ComponentOrganizer.java
 * 
 * ORGANISATEUR DE COMPOSANTS POUR LE RUBAN HRibbon
 * 
 * RÔLE PRINCIPAL :
 * Gère la création, le cache et l'organisation des composants Swing pour le ruban.
 * Sépare la logique de création des composants de leur ajout physique au conteneur.
 * 
 * CONCEPT CLÉ : PATTERN ORGANIZER
 * - Collecte intelligente des composants sans modifier l'arbre Swing
 * - Cache pour éviter les recréations inutiles
 * - Métadonnées pour tracer l'origine de chaque composant
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban.layout;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.SwingUtilities;
import rubban.GroupRenderer;
import rubban.HRibbonModel;
import rubban.Ribbon;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.HRibbonModelEvent;
import rubban.HRibbonModelListener;
import rubban.RibbonOverflowButton;

/**
 * ComponentOrganizer - Gestionnaire intelligent de composants pour HRibbon
 *
 * RESPONSABILITÉS PRÉCISES : 1. Parcourt un HRibbonModel et obtient les
 * composants Swing correspondants 2. Gère un cache pour éviter la recréation
 * inutile de composants identiques 3. Fournit une structure de résultats avec
 * métadonnées complètes 4. Sépare la création logique de l'ajout physique au
 * conteneur Swing
 *
 * PRINCIPE D'ARCHITECTURE : - Le Ribbon reste responsable de l'ajout physique
 * des composants au conteneur - ComponentOrganizer ne fait que la logique de
 * création et d'organisation - Séparation claire des préoccupations (Separation
 * of Concerns) 
 */
public class ComponentOrganizer implements HRibbonModelListener {
    
    // =========================================================================
    // CLASSE INTERNE POUR LA CLÉ DE CACHE
    // =========================================================================
    /**
     * La clé utilise toujours groupIndex et position, mais maintenant
     * ces valeurs sont mises à jour dynamiquement quand le modèle change.     
     */
    private static final class CacheKey {
        int groupIndex;  
        int position;   
        final Object value;
        
        CacheKey(int groupIndex, int position, Object value) {
            this.groupIndex = groupIndex;
            this.position = position;
            this.value = value;
        }
        
        /**
         * Met à jour la position (utilisé lors des insertions/suppressions)
         */
        void updatePosition(int newPosition) {
            this.position = newPosition;
        }
        
        /**
         * Met à jour le groupe (utilisé lors des déplacements entre groupes)
         */
        void updateGroup(int newGroupIndex) {
            this.groupIndex = newGroupIndex;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey ck = (CacheKey) o;
            return groupIndex == ck.groupIndex 
                && position == ck.position 
                && Objects.equals(value, ck.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(groupIndex, position, value);
        }
    }
    
    // =========================================================================
    // MÉTADONNÉES D'UN COMPOSANT
    // =========================================================================
    public static final class ComponentData {
        public final int groupIndex;
        public final int position;
        public final Object value;
        
        public ComponentData(int groupIndex, int position, Object value) {
            this.groupIndex = groupIndex;
            this.position = position;
            this.value = value;
        }
    }
    
    // =========================================================================
    // RÉSULTAT DE COLLECTE
    // =========================================================================
    public static final class ComponentCollectionResult {
        public final Map<Integer, List<Component>> componentsByGroup;
        public final Map<Component, ComponentData> componentInfoMap;
        
        ComponentCollectionResult(Map<Integer, List<Component>> componentsByGroup,
                                  Map<Component, ComponentData> componentInfoMap) {
            this.componentsByGroup = componentsByGroup;
            this.componentInfoMap = componentInfoMap;
        }
    }
    
    // =========================================================================
    // VARIABLES D'INSTANCE
    // =========================================================================
    private final Map<CacheKey, Component> cache = new HashMap<>();
    private Ribbon currentRibbon;  // Référence vers le ruban pour créer des composants
    
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    public ComponentOrganizer() {        
    }
    
    /**
     * À appeler quand le composant est attaché à un ruban.S'abonne aux événements du modèle.
     * @param ribbon
     */
    public void install(Ribbon ribbon) {
        if (ribbon == null) return;
        
        // Se désabonner de l'ancien modèle si nécessaire
        if (currentRibbon != null && currentRibbon.getModel() != null) {
            currentRibbon.getModel().removeModelListener(this);
        }
        
        this.currentRibbon = ribbon;
        
        // S'abonner au nouveau modèle
        if (ribbon.getModel() != null) {
            ribbon.getModel().addRibbonModelListener(this);
        }
    }
    
    /**
     * À appeler quand le composant est détaché.
     */
    public void uninstall() {
        if (currentRibbon != null && currentRibbon.getModel() != null) {
            currentRibbon.getModel().removeModelListener(this);
        }
        currentRibbon = null;
        clearCache(); // On vide le cache car plus de contexte
    }
    
    // =========================================================================
    // GESTION DU CACHE
    // =========================================================================
    public void clearCache() {
        cache.clear();
    }
    
    // =========================================================================
    // COLLECTE PRINCIPALE (utilisée par le LayoutManager)
    // =========================================================================
    public ComponentCollectionResult collectComponents(Ribbon ribbon, HRibbonModel model) {
        
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("ComponentOrganizer.collectComponents must be called on the EDT");
        }
        
        // S'assurer qu'on écoute le bon modèle
        if (this.currentRibbon != ribbon) {
            install(ribbon);
        }
        
        Map<Integer, List<Component>> componentsByGroup = new HashMap<>();
        Map<Component, ComponentData> componentInfoMap = new HashMap<>();
        
        if (ribbon == null || model == null) {
            return new ComponentCollectionResult(componentsByGroup, componentInfoMap);
        }
        
        GroupRenderer renderer = ribbon.getGroupRenderer();
        HRibbonGroupModel groupModel = ribbon.getGroupModel();
        
        final int groupCount = model.getGroupCount();
        for (int gi = 0; gi < groupCount; gi++) {
            List<Component> list = new ArrayList<>();
            HRibbonGroup group = (groupModel != null) ? groupModel.getHRibbonGroup(gi) : null;
            
            // Gestion des groupes collapsed 
            if (group != null && group.isCollapsed()) {
                Component collapsedComp = group.getCollapsedComponent();
                if (collapsedComp == null) {
                    CollapsedGroupRenderer collapsedRenderer = new CollapsedGroupRenderer();
                    
                    RibbonOverflowButton btn = null;
                    
                    if (ribbon.getIconRibbonOverflowButton() == null) {
                        btn = collapsedRenderer.createCollapsedButton(ribbon, group, gi);
                    }else{
                        btn = collapsedRenderer.createCollapsedButton(ribbon, group, gi,ribbon.getIconRibbonOverflowButton());
                    }                    
                    
                    group.setCollapsedButton(btn);
                    collapsedComp = btn;
                }
                list.add(collapsedComp);
                componentInfoMap.put(collapsedComp, new ComponentData(gi, -1, group.getHeaderValue()));
            }
            
            // Collecte des composants normaux - on utilise le cache existant
            int valueCount = model.getValueCount(gi);
            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, gi);
                if (value == null) continue;
                
                Component comp = null;
                
                if (value instanceof Component) {
                    comp = (Component) value;
                } else {
                    // Recherche dans le cache avec la clé actuelle
                    CacheKey key = new CacheKey(gi, pos, value);
                    comp = cache.get(key);
                    
                    // Si pas dans le cache, on le crée
                    if (comp == null && renderer != null) {
                        try {
                            comp = renderer.getGroupComponent(ribbon, value, gi, pos, false, false);
                        } catch (Throwable t) {
                            comp = null;
                        }
                        if (comp != null) {
                            cache.put(key, comp);
                        }
                    }
                }
                
                if (comp != null) {
                    list.add(comp);
                    componentInfoMap.put(comp, new ComponentData(gi, pos, value));
                }
            }
            
            componentsByGroup.put(gi, list);
        }
        
        return new ComponentCollectionResult(componentsByGroup, componentInfoMap);
    }
    
    // =========================================================================
    // MÉTHODES DE CRÉATION INDIVIDUELLE
    // =========================================================================
    public Component getOrCreateComponent(final Ribbon ribbon, final Object value,
                                         final int groupIndex, final int position) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("ComponentOrganizer.getOrCreateComponent must be called on the EDT");
        }
        
        if (value == null) return null;
        
        if (value instanceof Component) {
            return (Component) value;
        }
        
        GroupRenderer renderer = ribbon != null ? ribbon.getGroupRenderer() : null;
        CacheKey key = new CacheKey(groupIndex, position, value);
        
        Component comp = cache.get(key);
        if (comp != null) return comp;
        
        if (renderer != null) {
            try {
                comp = renderer.getGroupComponent(ribbon, value, groupIndex, position, false, false);
            } catch (Throwable t) {
                comp = null;
            }
        }
        
        if (comp != null) {
            cache.put(key, comp);
        }
        
        return comp;
    }
    
    // =========================================================================
    // IMPLÉMENTATION DE HRibbonModelListener
    // =========================================================================
    
    @Override
    public void ribbonChanged(HRibbonModelEvent e) {
        if (currentRibbon == null) return;
        
        switch (e.getType()) {
            case HRibbonModelEvent.INSERT:
                handleInsert(e);
                break;
            case HRibbonModelEvent.DELETE:
                handleDelete(e);
                break;            
            case HRibbonModelEvent.UPDATE:
                handleUpdate(e);
                break;
            default:
                // En cas de doute, on vide tout (sécurité)
                cache.clear();
        }
    }
    
    /**
     * Une insertion décale toutes les positions suivantes vers la droite.
     * On parcourt le cache et on met à jour les clés concernées.
     */
    private void handleInsert(HRibbonModelEvent e) {
    int group = e.getGroupIndex();
    int fromPos = e.getPosition();
    
    System.out.println("=== handleInsert CRÉATION ===");
    System.out.println("  groupe: " + group);
    System.out.println("  position: " + fromPos);
    
    // 1. Décaler les clés existantes vers la droite
    List<CacheKey> toUpdate = new ArrayList<>();
    for (CacheKey key : cache.keySet()) {
        if (key.groupIndex == group && key.position >= fromPos) {
            toUpdate.add(key);
        }
    }
    
    for (CacheKey oldKey : toUpdate) {
        Component comp = cache.remove(oldKey);
        if (comp != null) {
            CacheKey newKey = new CacheKey(group, oldKey.position + 1, oldKey.value);
            cache.put(newKey, comp);
        }
    }
    
    // 2. Créer le nouveau composant
    if (currentRibbon == null || currentRibbon.getModel() == null) {
        System.out.println("  ⚠️ currentRibbon ou modèle null");
        return;
    }
    
    Object value = currentRibbon.getModel().getValueAt(fromPos, group);
    if (value == null) {
        System.out.println("  ⚠️ valeur null à la position " + fromPos);
        return;
    }
    
    Component newComp = null;
    
    if (value instanceof Component) {
        // Cas 1 : la valeur est déjà un composant
        newComp = (Component) value;
        System.out.println("  valeur déjà composant: " + value.getClass().getSimpleName());
    } else {
        // Cas 2 : utiliser le renderer
        GroupRenderer renderer = currentRibbon.getGroupRenderer();
        if (renderer != null) {
            try {
                newComp = renderer.getGroupComponent(
                    currentRibbon, value, group, fromPos, false, false
                );
                System.out.println("  composant créé via renderer: " + 
                    (newComp != null ? newComp.getClass().getSimpleName() : "null"));
            } catch (Throwable t) {
                System.out.println(" erreur renderer: " + t.getMessage());
                newComp = null;
            }
        }
    }
    
    if (newComp != null) {
        CacheKey newKey = new CacheKey(group, fromPos, value);
        cache.put(newKey, newComp);
        System.out.println("composant ajouté au cache. Taille cache: " + cache.size());
    } else {
        System.out.println(" échec création composant");
    }
}

    
    /**
     * Une suppression décale toutes les positions suivantes vers la gauche.
     * On supprime aussi l'entrée correspondant à la valeur supprimée.
     */
    private void handleDelete(HRibbonModelEvent e) {
        int group = e.getGroupIndex();
        int fromPos = e.getPosition(); // Position supprimée
        
        // 1. Supprimer l'entrée correspondant à la valeur supprimée
        // Malheureusement, on ne connaît pas la valeur ici (pas dans l'événement)
        // On doit donc chercher par groupe/position
        CacheKey toRemove = null;
        for (CacheKey key : cache.keySet()) {
            if (key.groupIndex == group && key.position == fromPos) {
                toRemove = key;
                break;
            }
        }
        if (toRemove != null) {
            cache.remove(toRemove);
        }
        
        // 2. Décaler les positions suivantes
        List<CacheKey> toUpdate = new ArrayList<>();
        for (CacheKey key : cache.keySet()) {
            if (key.groupIndex == group && key.position > fromPos) {
                toUpdate.add(key);
            }
        }
        
        for (CacheKey oldKey : toUpdate) {
            Component comp = cache.remove(oldKey);
            if (comp != null) {
                CacheKey newKey = new CacheKey(group, oldKey.position - 1, oldKey.value);
                cache.put(newKey, comp);
            }
        }
        
    }
    
    /**
     * Une mise à jour : on peut soit recréer le composant, soit le mettre à jour.
     * Par simplicité, on le supprime du cache (il sera recréé au prochain layout).
     */
    private void handleUpdate(HRibbonModelEvent e) {
        int group = e.getGroupIndex();
        int pos = e.getPosition();
        
        CacheKey toRemove = null;
        for (CacheKey key : cache.keySet()) {
            if (key.groupIndex == group && key.position == pos) {
                toRemove = key;
                break;
            }
        }
        if (toRemove != null) {
            cache.remove(toRemove);
        }
    }
}
