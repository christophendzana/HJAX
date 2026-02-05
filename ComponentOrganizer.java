/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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

/**
 ** ComponentOrganizer
 *
 * Responsabilité :
 * - Pour un HRibbonModel donné, parcourir les valeurs et obtenir le Component
 *   correspondant (sans l'ajouter au conteneur).
 * - Fournir une structure résultat contenant :
 *     * la Map groupIndex -> List<Component>
 *     * la Map Component -> ComponentData (groupIndex, position, value)
 * - Cacher la création des composants pour éviter recréations inutiles.
 *
 * Notes : 
 * - Le Ribbon reste responsable d'ajouter physiquement les composants au
 *   conteneur (via ribbon.addComponentToContainer(...)).
 * @author FIDELE
 */
public class ComponentOrganizer {
    
    
    /**
     * Clef pour le cache : combinaison du groupIndex + position + valeur (equals).
     */
    private static final class CacheKey {
        final int groupIndex;
        final int position;
        final Object value;

        CacheKey(int groupIndex, int position, Object value) {
            this.groupIndex = groupIndex;
            this.position = position;
            this.value = value;
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

    /**
     * Métadonnées pour un Component créé/associé.
     */
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

    /**
     * Résultat de la collecte.
     */
    public static final class ComponentCollectionResult {
        public final Map<Integer, List<Component>> componentsByGroup;
        public final Map<Component, ComponentData> componentInfoMap;

        ComponentCollectionResult(Map<Integer, List<Component>> componentsByGroup,
                                  Map<Component, ComponentData> componentInfoMap) {
            this.componentsByGroup = componentsByGroup;
            this.componentInfoMap = componentInfoMap;
        }
    }

    // Cache interne : évite de recréer le même component plusieurs fois.
    // Clef basée sur groupIndex+position+value.equals(...)
    private final Map<CacheKey, Component> cache = new HashMap<>();

    public ComponentOrganizer() {
    }

    /**
     * Vide le cache des composants créés.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Collecte tous les composants du modèle, en demandant au renderer de
     * créer les composants si nécessaire. NE modifie PAS l'arbre Swing (n'ajoute
     * pas les components au Ribbon) : l'ajout physique est laissé au Ribbon.
     *
     * @param ribbon     le Ribbon (utilisé pour obtenir renderer)
     * @param model      le HRibbonModel
     * @return ComponentCollectionResult contenant composants par groupe et metadatas
     * @throws IllegalStateException si appelé hors EDT
     */
    public ComponentCollectionResult collectComponents(final Ribbon ribbon, final HRibbonModel model) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("ComponentOrganizer.collectComponents must be called on the EDT");
        }

        Map<Integer, List<Component>> componentsByGroup = new HashMap<>();
        Map<Component, ComponentData> componentInfoMap = new HashMap<>();

        if (ribbon == null || model == null) {
            return new ComponentCollectionResult(componentsByGroup, componentInfoMap);
        }

        GroupRenderer renderer = ribbon.getGroupRenderer();

        final int groupCount = model.getGroupCount();
        for (int gi = 0; gi < groupCount; gi++) {
            List<Component> list = new ArrayList<>();
            int valueCount = model.getValueCount(gi);
            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, gi);
                Component comp = null;

                if (value == null) {
                    // skip null values
                    continue;
                }

                
                if (value instanceof Component) {
                    comp = (Component) value;
                } else {
                    
                    CacheKey key = new CacheKey(gi, pos, value);
                    comp = cache.get(key);
                    if (comp == null) {
                        
                        if (renderer != null) {
                            try {
                                comp = renderer.getGroupComponent(ribbon, value, gi, pos, false, false);
                            } catch (Throwable t) {
                                comp = null;
                            }
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

    /**
     * Créer le Component correspondant à une valeur précise.     
     * Doit être appelé sur l'EDT.
     *
     * @param ribbon     le Ribbon
     * @param value      la valeur (Component ou autre)
     * @param groupIndex index du groupe
     * @param position   position dans le groupe
     * @return Component ou null si non trouvée/créée
     */
    public Component getOrCreateComponent(final Ribbon ribbon, final Object value, final int groupIndex, final int position) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("ComponentOrganizer.getOrCreateComponent must be called on the EDT");
        }

        if (value == null) return null;
        if (value instanceof Component) return (Component) value;

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
    
}
