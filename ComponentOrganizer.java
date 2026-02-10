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

/**
 * ComponentOrganizer - Gestionnaire intelligent de composants pour HRibbon
 * 
 * RESPONSABILITÉS PRÉCISES :
 * 1. Parcourt un HRibbonModel et obtient les composants Swing correspondants
 * 2. Gère un cache pour éviter la recréation inutile de composants identiques
 * 3. Fournit une structure de résultats avec métadonnées complètes
 * 4. Sépare la création logique de l'ajout physique au conteneur Swing
 * 
 * PRINCIPE D'ARCHITECTURE :
 * - Le Ribbon reste responsable de l'ajout physique des composants au conteneur
 * - ComponentOrganizer ne fait que la logique de création et d'organisation
 * - Séparation claire des préoccupations (Separation of Concerns)
 * 
 * @see rubban.Ribbon#addComponentToContainer(Component)
 * @see rubban.GroupRenderer
 */
public class ComponentOrganizer {
    
    // =========================================================================
    // CLASSES INTERNES POUR LA GESTION DU CACHE ET DES MÉTADONNÉES
    // =========================================================================
    
    /**
     * Clé de cache - Combinaison unique pour identifier un composant
     * 
     * STRUCTURE DE LA CLÉ :
     * - groupIndex : Index du groupe dans le ruban
     * - position   : Position dans le groupe (ligne)
     * - value      : Valeur objet à afficher (utilise equals() pour comparaison)
     * 
     * UTILISATION :
     * Permet de réutiliser un composant si la même valeur apparaît à la même position
     * Évite la recréation inutile de composants identiques
     * 
     * DESIGN PATTERN : VALUE OBJECT (objet valeur)
     */
    private static final class CacheKey {
        /** Index du groupe dans le modèle HRibbon */
        final int groupIndex;
        
        /** Position verticale dans le groupe (numéro de ligne) */
        final int position;
        
        /** Valeur objet à afficher (peut être null) */
        final Object value;

        /**
         * Constructeur de la clé de cache
         * 
         * @param groupIndex index du groupe (0-based)
         * @param position position dans le groupe (0-based)
         * @param value valeur à afficher (peut être null)
         */
        CacheKey(int groupIndex, int position, Object value) {
            this.groupIndex = groupIndex;
            this.position = position;
            this.value = value;
        }

        /**
         * Implémentation de equals() pour la comparaison de clés
         * 
         * @param o objet à comparer
         * @return true si les clés sont équivalentes
         * 
         * LOGIQUE DE COMPARAISON :
         * 1. Référence identique → true
         * 2. Type différent → false
         * 3. Même groupIndex, position et value.equals() → true
         */
        @Override
        public boolean equals(Object o) {
            // Optimisation : même référence mémoire
            if (this == o) return true;
            
            // Vérification du type
            if (!(o instanceof CacheKey)) return false;
            
            // Comparaison détaillée
            CacheKey ck = (CacheKey) o;
            return groupIndex == ck.groupIndex
                    && position == ck.position
                    && Objects.equals(value, ck.value); // Null-safe comparison
        }

        /**
         * Implémentation de hashCode() cohérente avec equals()
         * 
         * @return code de hachage calculé à partir des trois champs
         * 
         * CONTRAT JAVA :
         * - Si deux objets sont égaux (equals() retourne true), ils doivent avoir
         *   le même hashCode()
         * - L'inverse n'est pas obligatoire (collisions possibles)
         */
        @Override
        public int hashCode() {
            return Objects.hash(groupIndex, position, value);
        }
    }

    /**
     * Métadonnées d'un composant - Informations sur son origine
     * 
     * UTILITÉ :
     * - Permet de retracer l'origine d'un composant dans le ruban
     * - Utilisé pour les événements, la sélection, le débogage
     * - Structure immuable (safety thread)
     * 
     * DESIGN PATTERN : DATA HOLDER (porteur de données)
     */
    public static final class ComponentData {
        /** Index du groupe source */
        public final int groupIndex;
        
        /** Position dans le groupe source */
        public final int position;
        
        /** Valeur objet d'origine */
        public final Object value;

        /**
         * Constructeur des métadonnées de composant
         * 
         * @param groupIndex index du groupe d'origine
         * @param position position dans le groupe
         * @param value valeur objet d'origine
         */
        public ComponentData(int groupIndex, int position, Object value) {
            this.groupIndex = groupIndex;
            this.position = position;
            this.value = value;
        }
    }

    /**
     * Résultat de la collecte de composants - Structure de retour
     * 
     * CONTENU :
     * - componentsByGroup : Map groupIndex → Liste des composants du groupe
     * - componentInfoMap : Map Component → Métadonnées du composant
     * 
     * UTILISATION :
     * Retourné par collectComponents() pour fournir une vue organisée
     * des composants sans les ajouter physiquement au conteneur
     */
    public static final class ComponentCollectionResult {
        /** Composants organisés par groupe (index → liste) */
        public final Map<Integer, List<Component>> componentsByGroup;
        
        /** Métadonnées associées à chaque composant */
        public final Map<Component, ComponentData> componentInfoMap;

        /**
         * Constructeur du résultat de collecte
         * 
         * @param componentsByGroup map des composants par groupe
         * @param componentInfoMap map des métadonnées par composant
         */
        ComponentCollectionResult(Map<Integer, List<Component>> componentsByGroup,
                                  Map<Component, ComponentData> componentInfoMap) {
            this.componentsByGroup = componentsByGroup;
            this.componentInfoMap = componentInfoMap;
        }
    }

    // =========================================================================
    // VARIABLES D'INSTANCE
    // =========================================================================
    
    /**
     * Cache des composants créés - Optimisation des performances
     * 
     * STRUCTURE : CacheKey → Component
     * 
     * AVANTAGES :
     * - Évite la recréation de composants identiques
     * - Réduit l'empreinte mémoire
     * - Améliore les performances lors des redessinages
     * 
     * IMPORTANT : Le cache doit être vidé lorsque le modèle change
     * @see #clearCache()
     */
    private final Map<CacheKey, Component> cache = new HashMap<>();

    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    
    /**
     * Constructeur par défaut
     * 
     * Initialise un nouvel organisateur de composants avec un cache vide
     */
    public ComponentOrganizer() {
        // Cache initialisé automatiquement par new HashMap<>()
    }

    // =========================================================================
    // MÉTHODES PUBLIQUES DE GESTION DU CACHE
    // =========================================================================
    
    /**
     * Vide le cache des composants créés
     * 
     * QUAND UTILISER :
     * - Lorsque le modèle HRibbon change (nouvelles données)
     * - Lorsque le renderer change (nouveau style d'affichage)
     * - Lors d'un nettoyage mémoire
     * 
     * EFFET :
     * Tous les composants devront être recréés au prochain appel de collectComponents()
     */
    public void clearCache() {
        cache.clear();
    }

    // =========================================================================
    // MÉTHODE PRINCIPALE DE COLLECTE
    // =========================================================================
    
    /**
     * Collecte tous les composants du modèle sans les ajouter physiquement
     * 
     * FONCTIONNEMENT :
     * 1. Parcourt tous les groupes du modèle
     * 2. Pour chaque valeur, obtient ou crée le composant correspondant
     * 3. Organise les composants dans des structures de données
     * 4. Ne modifie pas l'arbre Swing (pas d'add() au parent)
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * 
     * @param ribbon le Ruban contenant le renderer et la configuration
     * @param model le modèle HRibbon contenant les données à afficher
     * @return ComponentCollectionResult avec composants organisés et métadonnées
     * @throws IllegalStateException si appelé hors de l'EDT
     * 
     * @see SwingUtilities#isEventDispatchThread()
     */
    public ComponentCollectionResult collectComponents(final Ribbon ribbon, final HRibbonModel model) {
    // VÉRIFICATION DU THREAD - Swing est single-threaded
    if (!SwingUtilities.isEventDispatchThread()) {
        throw new IllegalStateException(
            "ComponentOrganizer.collectComponents must be called on the EDT"
        );
    }

    // Structures de résultats vides par défaut
    Map<Integer, List<Component>> componentsByGroup = new HashMap<>();
    Map<Component, ComponentData> componentInfoMap = new HashMap<>();

    // CAS LIMITE : Paramètres invalides
    if (ribbon == null || model == null) {
        return new ComponentCollectionResult(componentsByGroup, componentInfoMap);
    }

    // Récupération du renderer (peut être null)
    GroupRenderer renderer = ribbon.getGroupRenderer();
    
    // Récupération du groupModel pour vérifier l'état collapsed
    HRibbonGroupModel groupModel = ribbon.getGroupModel();

    // PARCOURS DE TOUS LES GROUPES
    final int groupCount = model.getGroupCount();
    for (int gi = 0; gi < groupCount; gi++) {
        
        // Liste des composants pour ce groupe
        List<Component> list = new ArrayList<>();
        
        // Récupérer le groupe pour vérifier son état
        HRibbonGroup group = null;
        if (groupModel != null) {
            group = groupModel.getHRibbonGroup(gi);
        }
        
        // ============ NOUVEAU : GÉRER LE COMBOBOX SI COLLAPSED ============
        if (group != null && group.isCollapsed()) {
            // Créer ou récupérer le JComboBox
            Component collapsedComp = group.getCollapsedComponent();
            
            if (collapsedComp == null) {
                // Créer le combo via CollapsedGroupRenderer
                CollapsedGroupRenderer collapsedRenderer = new CollapsedGroupRenderer();
                collapsedComp = collapsedRenderer.createCollapsedComboBox(ribbon, group, gi);
                group.setCollapsedComponent(collapsedComp);
            }
            
            // Ajouter le combo à la liste
            list.add(collapsedComp);
            
            // Métadonnées : position = -1 pour indiquer "groupe collapsed"
            componentInfoMap.put(collapsedComp, new ComponentData(gi, -1, group.getHeaderValue()));
        }
        
        // ============ TOUJOURS COLLECTER LES COMPOSANTS NORMAUX ============
        // Même si le groupe est collapsed, on collecte les composants normaux
        // Le LayoutManager décidera de leur visibilité
        
        int valueCount = model.getValueCount(gi);
        for (int pos = 0; pos < valueCount; pos++) {
            Object value = model.getValueAt(pos, gi);
            Component comp = null;

            // IGNORER LES VALEURS NULLES
            if (value == null) {
                continue;
            }

            // CAS 1 : VALEUR DÉJÀ COMPOSANT
            if (value instanceof Component) {
                comp = (Component) value;
            } 
            // CAS 2 : VALEUR OBJET (NÉCESSITE UN RENDERER)
            else {
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

            // AJOUT DU COMPOSANT AUX RÉSULTATS
            if (comp != null) {
                list.add(comp);
                componentInfoMap.put(comp, new ComponentData(gi, pos, value));
            }
        }
        
        // AJOUT DE LA LISTE À LA MAP (même si vide)
        componentsByGroup.put(gi, list);
    }

    // RETOUR DU RÉSULTAT COMPLET
    return new ComponentCollectionResult(componentsByGroup, componentInfoMap);
}

    // =========================================================================
    // MÉTHODE UTILITAIRE DE CRÉATION/OBTENTION
    // =========================================================================
    
    /**
     * Obtient ou crée un composant pour une valeur spécifique
     * 
     * UTILISATION TYPIQUE :
     * - Création individuelle de composants (hors collecte complète)
     * - Mise à jour incrémentale
     * - Gestion d'événements spécifiques
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * 
     * @param ribbon le Ruban contenant le renderer
     * @param value la valeur à transformer en composant
     * @param groupIndex index du groupe d'origine
     * @param position position dans le groupe
     * @return le Component correspondant, ou null si échec
     * @throws IllegalStateException si appelé hors de l'EDT
     */
    public Component getOrCreateComponent(final Ribbon ribbon, final Object value, 
                                          final int groupIndex, final int position) {
        // VÉRIFICATION DU THREAD
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                "ComponentOrganizer.getOrCreateComponent must be called on the EDT"
            );
        }

        // CAS LIMITE : VALEUR NULLE
        if (value == null) return null;
        
        // CAS 1 : DÉJÀ UN COMPOSANT
        if (value instanceof Component) return (Component) value;

        // CAS 2 : VALEUR OBJET → TRANSFORMATION NÉCESSAIRE
        GroupRenderer renderer = ribbon != null ? ribbon.getGroupRenderer() : null;
        CacheKey key = new CacheKey(groupIndex, position, value);

        // RECHERCHE DANS LE CACHE
        Component comp = cache.get(key);
        if (comp != null) return comp;

        // CRÉATION PAR LE RENDERER
        if (renderer != null) {
            try {
                comp = renderer.getGroupComponent(ribbon, value, groupIndex, position, false, false);
            } catch (Throwable t) {
                // Gestion robuste des erreurs
                comp = null;
            }
        }

        // MISE EN CACHE SI CRÉATION RÉUSSIE
        if (comp != null) {
            cache.put(key, comp);
        }

        return comp;
    }
}