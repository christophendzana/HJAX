/*
 * HeaderManager.java
 * 
 * GESTIONNAIRE D'EN-TÊTES POUR LE RUBAN HRibbon
 * 
 * RÔLE PRINCIPAL :
 * Gère le cycle de vie, le cache et le positionnement des composants d'en-tête
 * pour chaque groupe du ruban, en coordonnant avec le GroupRenderer.
 * 
 * CONCEPT CLÉ : CYCLE DE VIE DES EN-TÊTES
 * - Création via GroupRenderer.getHeaderComponent()
 * - Cache pour éviter les recréations inutiles
 * - Positionnement relatif aux groupes selon l'alignement
 * - Suppression propre quand les groupes disparaissent
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban.layout;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import rubban.GroupRenderer;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/**
 * HeaderManager - Gestionnaire centralisé des en-têtes de groupes
 * 
 * RESPONSABILITÉS PRÉCISES :
 * 1. Crée et met en cache les composants d'en-tête via GroupRenderer
 * 2. Positionne chaque en-tête relativement à son groupe selon l'alignement
 * 3. Gère le cycle de vie (ajout/retrait) des en-têtes dans le conteneur Ribbon
 * 4. Nettoie automatiquement les en-têtes des groupes supprimés
 * 5. Garantit l'exécution sur l'Event Dispatch Thread (EDT)
 * 
 * ARCHITECTURE DÉLÉGUÉE :
 * - Création : Déléguée à GroupRenderer.getHeaderComponent()
 * - Ajout/Retrait : Délégué à Ribbon via addHeaderComponent()/removeHeaderComponent()
 * - Positionnement : Calculé localement selon l'alignement
 * 
 * @see GroupRenderer#getHeaderComponent(Ribbon, Object, int, boolean)
 * @see Ribbon#addHeaderComponent(Component)
 * @see Ribbon#removeHeaderComponent(Component)
 */
public class HeaderManager {
    
    // =========================================================================
    // VARIABLE D'INSTANCE : CACHE DES EN-TÊTES
    // =========================================================================
    
    /**
     * Cache des composants d'en-tête indexés par l'index du groupe
     * 
     * STRUCTURE : Map<Integer, Component>
     * - Clé : index du groupe (0-based)
     * - Valeur : composant d'en-tête Swing associé au groupe
     * 
     * AVANTAGES DU CACHE :
     * - Évite la recréation des en-têtes à chaque redessin
     * - Maintient les références aux composants existants
     * - Permet la mise à jour incrémentale
     * 
     * GESTION DE LA MÉMOIRE :
     * - Nettoyé automatiquement quand les groupes disparaissent
     * - Vidable manuellement via invalidateAllHeaders()
     */
    private final Map<Integer, Component> headerCache = new HashMap<>();
    
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    
    /**
     * Constructeur par défaut
     * 
     * Initialise un gestionnaire d'en-têtes avec un cache vide
     */
    public HeaderManager() {
        // Cache initialisé automatiquement par new HashMap<>()
    }
    
    // =========================================================================
    // MÉTHODE PRINCIPALE DE MISE À JOUR ET POSITIONNEMENT
    // =========================================================================
    
    /**
     * Met à jour le cache des en-têtes et positionne chaque en-tête
     * relativement aux limites de son groupe
     * 
     * ALGORITHME PRINCIPAL :
     * 1. Vérification des préconditions (EDT, paramètres non-null)
     * 2. Nettoyage des en-têtes des groupes qui ont disparu
     * 3. Création des nouveaux en-têtes manquants via GroupRenderer
     * 4. Positionnement de chaque en-tête selon l'alignement configuré
     * 5. Mise à jour de la visibilité et des dimensions
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * 
     * @param ribbon Le ruban parent (contenant et fournissant le renderer)
     * @param ctx Contexte de layout (alignement, dimensions des en-têtes, marges)
     * @param groupModel Modèle des groupes (pour obtenir les groupes existants)
     * @param groupBounds Tableau des rectangles de contenu de chaque groupe
     *                    Ces rectangles définissent la zone de contenu principale
     * 
     * @return Map immuable du cache actuel des en-têtes (copie défensive)
     * @throws IllegalStateException si appelé hors de l'EDT
     * 
     * @see SwingUtilities#isEventDispatchThread()
     */
    public Map<Integer, Component> updateAndPositionHeaders(final Ribbon ribbon,
                                                           final HRibbonLayoutContext ctx,
                                                           final HRibbonGroupModel groupModel,
                                                           final Rectangle[] groupBounds) {
        // VÉRIFICATION DU THREAD - Swing est single-threaded
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                "HeaderManager.updateAndPositionHeaders must be called on the EDT"
            );
        }
        
        // VÉRIFICATION DES PARAMÈTRES OBLIGATOIRES
        if (ribbon == null || ctx == null || groupModel == null || groupBounds == null) {
            // Retourner une copie défensive du cache actuel (non modifiable)
            return Collections.unmodifiableMap(new HashMap<>(headerCache));
        }
        
        // ÉTAPE 1 : IDENTIFICATION DES GROUPES COURANTS
        // Créer un ensemble des indices de groupes actuellement présents
        Set<Integer> currentGroupIndices = new HashSet<>();
        for (int i = 0; i < groupBounds.length; i++) {
            currentGroupIndices.add(i);
        }
        
        // ÉTAPE 2 : NETTOYAGE DES EN-TÊTES OBSOLÈTES
        // Supprimer les en-têtes des groupes qui n'existent plus
        for (Integer cachedIndex : new HashSet<>(headerCache.keySet())) {
            if (!currentGroupIndices.contains(cachedIndex)) {
                // Groupe disparu : retirer du cache et du conteneur
                Component oldHeader = headerCache.remove(cachedIndex);
                if (oldHeader != null) {
                    ribbon.removeHeaderComponent(oldHeader);
                }
            }
        }
        
        // ÉTAPE 3 : EXTRACTION DES PARAMÈTRES DE CONFIGURATION
        int headerAlignment = ctx.getHeaderAlignment();
        int headerWidth = ctx.getHeaderWidth();
        int headerMargin = ctx.getHeaderMargin();
        Insets insets = ribbon.getInsets(); // Marges du ruban (non utilisées ici mais disponibles)
        
        // ÉTAPE 4 : PARCOURS DE TOUS LES GROUPES COURANTS
        for (int i = 0; i < groupBounds.length; i++) {
            // Rectangle de contenu du groupe (zone pour les composants)
            Rectangle contentRect = groupBounds[i];
            
            // Groupe correspondant (pour la configuration)
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            
            // VÉRIFICATION DES DONNÉES REQUISES
            if (group == null || contentRect == null) {
                continue; // Groupe invalide, passer au suivant
            }
            
            // RECHERCHE DANS LE CACHE
            Component header = headerCache.get(i);
            
            // ÉTAPE 5 : CRÉATION D'UN NOUVEL EN-TÊTE SI NÉCESSAIRE
            if (header == null) {
                GroupRenderer renderer = ribbon.getGroupRenderer();
                
                if (renderer != null) {
                    // Récupération de la valeur d'en-tête (peut être null)
                    Object headerValue = ribbon.getHeaderValue(i);
                    
                    try {
                        // CRÉATION VIA LE RENDERER
                        // Note : isSelected = false (la sélection est gérée ailleurs)
                        header = renderer.getHeaderComponent(ribbon, headerValue, i, false);
                        
                    } catch (Throwable t) {
                        // GESTION ROBUSTE DES ERREURS
                        // En cas d'erreur dans le renderer, l'en-tête reste null
                        header = null;
                    }
                    
                    // AJOUT AU CACHE ET AU CONTENEUR SI CRÉATION RÉUSSIE
                    if (header != null) {
                        ribbon.addHeaderComponent(header); // Ajout physique
                        headerCache.put(i, header);        // Mise en cache
                    }
                }
            }
            
            // ÉTAPE 6 : POSITIONNEMENT DE L'EN-TÊTE (SI EXISTANT)
            if (header == null) {
                continue; // Pas d'en-tête à positionner
            }
            
            // VARIABLES DE POSITIONNEMENT
            int hx = 0; // Position X (coordonnée horizontale)
            int hy = 0; // Position Y (coordonnée verticale)
            int hw = 0; // Largeur (width)
            int hh = 0; // Hauteur (height)
            
            // CALCUL DU POSITIONNEMENT SELON L'ALIGNEMENT
            switch (headerAlignment) {
                case Ribbon.HEADER_NORTH:
                    // EN-TÊTE AU-DESSUS DU CONTENU
                    hw = contentRect.width;               // Même largeur que le contenu
                    hh = ribbon.getHeaderHeight();        // Hauteur configurée
                    hx = contentRect.x;                   // Aligné à gauche du contenu
                    hy = contentRect.y - headerMargin - hh; // Au-dessus avec marge
                    break;
                    
                case Ribbon.HEADER_SOUTH:
                    // EN-TÊTE EN-DESSOUS DU CONTENU
                    hw = contentRect.width;               // Même largeur que le contenu
                    hh = ribbon.getHeaderHeight();        // Hauteur configurée
                    hx = contentRect.x;                   // Aligné à gauche du contenu
                    hy = contentRect.y + contentRect.height + headerMargin; // En dessous avec marge
                    break;
                    
                case Ribbon.HEADER_WEST:
                    // EN-TÊTE À GAUCHE DU CONTENU
                    hw = headerWidth;                     // Largeur configurée
                    hh = contentRect.height;              // Même hauteur que le contenu
                    hx = contentRect.x - headerMargin - hw; // À gauche avec marge
                    hy = contentRect.y;                   // Aligné en haut du contenu
                    break;
                    
                case Ribbon.HEADER_EAST:
                    // EN-TÊTE À DROITE DU CONTENU
                    hw = headerWidth;                     // Largeur configurée
                    hh = contentRect.height;              // Même hauteur que le contenu
                    hx = contentRect.x + contentRect.width + headerMargin; // À droite avec marge
                    hy = contentRect.y;                   // Aligné en haut du contenu
                    break;
                    
                default:
                    // ALIGNEMENT INCONNU : CACHER L'EN-TÊTE
                    header.setVisible(false);
                    continue; // Passer au groupe suivant
            }
            
            // ÉTAPE 7 : APPLICATION DES DIMENSIONS ET POSITION
            // Garantir des dimensions non-négatives
            hw = Math.max(0, hw);
            hh = Math.max(0, hh);
            
            // Appliquer les calculs au composant
            header.setBounds(hx, hy, hw, hh);
            header.setVisible(true); // S'assurer qu'il est visible
        }
        
        // ÉTAPE 8 : RETOUR DU CACHE ACTUEL (COPIE DÉFENSIVE)
        return Collections.unmodifiableMap(new HashMap<>(headerCache));
    }
    
    // =========================================================================
    // MÉTHODES DE GESTION MANUELLE DU CACHE
    // =========================================================================
    
    /**
     * Supprime l'en-tête d'un groupe spécifique
     * 
     * UTILISATION TYPIQUE :
     * - Suppression manuelle d'un groupe
     * - Réinitialisation d'un en-tête corrompu
     * - Changement de configuration d'un groupe spécifique
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * 
     * @param ribbon Le ruban contenant l'en-tête
     * @param groupIndex Index du groupe dont l'en-tête doit être supprimé
     * @throws IllegalStateException si appelé hors de l'EDT
     */
    public void removeHeaderForGroup(Ribbon ribbon, int groupIndex) {
        // VÉRIFICATION DU THREAD
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                "HeaderManager.removeHeaderForGroup must be called on the EDT"
            );
        }
        
        // RETRAIT DU CACHE
        Component oldHeader = headerCache.remove(groupIndex);
        
        // RETRAIT DU CONTENEUR (SI EXISTANT)
        if (oldHeader != null && ribbon != null) {
            ribbon.removeHeaderComponent(oldHeader);
        }
    }
    
    /**
     * Invalide et supprime tous les en-têtes
     * 
     * UTILISATION TYPIQUE :
     * - Réinitialisation complète du ruban
     * - Changement de thème ou de renderer
     * - Nettoyage avant destruction
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * 
     * @param ribbon Le ruban contenant les en-têtes
     * @throws IllegalStateException si appelé hors de l'EDT
     */
    public void invalidateAllHeaders(Ribbon ribbon) {
        // VÉRIFICATION DU THREAD
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                "HeaderManager.invalidateAllHeaders must be called on the EDT"
            );
        }
        
        // NETTOYAGE DE TOUS LES EN-TÊTES
        if (ribbon != null) {
            // Retrait de tous les en-têtes du conteneur
            for (Component header : headerCache.values()) {
                if (header != null) {
                    ribbon.removeHeaderComponent(header);
                }
            }
        }
        
        // VIDAGE COMPLET DU CACHE
        headerCache.clear();
    }
}