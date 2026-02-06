/*
 * PreferredSizeCalculator.java
 * 
 * CALCULATEUR DE TAILLE PRÉFÉRÉE POUR LE RUBAN HRibbon
 * 
 * RÔLE PRINCIPAL :
 * Calcule la hauteur totale requise pour afficher tous les groupes du ruban
 * en simulant le wrapping des composants et en tenant compte de la configuration.
 * 
 * CONCEPT CLÉ : SIMULATION PRÉVISIONNELLE
 * - Simule le layout sans modifier l'interface utilisateur
 * - Utilise un cache pour optimiser les calculs répétés
 * - Calcule la hauteur maximale parmi tous les groupes
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import rubban.GroupRenderer;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.HRibbonModel;
import rubban.Ribbon;

/**
 * PreferredSizeCalculator - Calculateur de hauteur préférée pour le ruban
 * 
 * RESPONSABILITÉS PRÉCISES :
 * 1. Simule le wrapping des composants dans chaque groupe
 * 2. Calcule la hauteur de contenu requise pour chaque groupe
 * 3. Détermine la hauteur maximale parmi tous les groupes
 * 4. Gère un cache de dimensions pour optimiser les performances
 * 5. Tient compte des en-têtes, padding, espacement et autres contraintes
 * 
 * IMPORTANCE STRATÉGIQUE :
 * Cette classe est cruciale pour :
 * - Le calcul du preferredSize() du ruban
 * - La planification du layout avant le rendu effectif
 * - L'optimisation des performances via le cache
 * 
 * @see LineWrapper#computeHeightForPreferences(List, int, int, int)
 * @see WeakHashMap
 */
public class PreferredSizeCalculator {
    
    // =========================================================================
    // CACHE DE DIMENSIONS POUR L'OPTIMISATION
    // =========================================================================
    
    /**
     * Cache des dimensions préférées des valeurs non-Component
     * 
     * STRUCTURE : WeakHashMap<Object, Dimension>
     * - Clé : Valeur objet (String, Number, etc.)
     * - Valeur : Dimension préférée calculée pour cette valeur
     * 
     * CARACTÉRISTIQUES DU CACHE :
     * 1. WeakHashMap : Les clés sont référencées faiblement
     *    → Le cache peut être nettoyé automatiquement par le garbage collector
     *    → Évite les fuites mémoire avec des objets temporaires
     * 2. Réduction du coût : Évite de recalculer les mêmes dimensions
     * 3. Synchronisation : Non thread-safe (usage mono-thread Swing)
     * 
     * QUAND UTILISER LE CACHE :
     * - Pour les valeurs objets transformées en composants via GroupRenderer
     * - Pas pour les Component directs (ils ont déjà getPreferredSize())
     */
    private final Map<Object, Dimension> measureCache = new WeakHashMap<>();
    
    /**
     * Dimension par défaut utilisée comme fallback
     * 
     * UTILISATION :
     * - Quand une valeur n'a pas de dimension calculable
     * - Quand le renderer échoue ou retourne null
     * - Pour garantir une dimension minimale raisonnable
     * 
     * VALEUR : 50px de largeur × 24px de hauteur
     * (taille standard pour un composant Swing basique)
     */
    private static final Dimension DEFAULT_DIM = new Dimension(50, 24);
    
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    
    /**
     * Constructeur par défaut
     * 
     * Initialise un calculateur de taille préférée avec un cache vide
     */
    public PreferredSizeCalculator() {
        // Cache initialisé automatiquement par new WeakHashMap<>()
    }
    
    // =========================================================================
    // MÉTHODES DE GESTION DU CACHE
    // =========================================================================
    
    /**
     * Vide le cache des dimensions mesurées
     * 
     * QUAND UTILISER :
     * - Après un changement de renderer (nouvelles tailles possibles)
     * - Après une modification importante du modèle
     * - Pour libérer de la mémoire si nécessaire
     * - Avant une série de calculs avec de nouvelles données
     * 
     * EFFET :
     * Toutes les dimensions devront être recalculées au prochain appel
     */
    public void clearCache() {
        measureCache.clear();
    }
    
    // =========================================================================
    // MÉTHODE PRINCIPALE DE CALCUL
    // =========================================================================
    
    /**
     * Calcule la hauteur totale requise pour afficher tous les groupes du ruban
     * 
     * ALGORITHME PRINCIPAL :
     * 1. Vérification des préconditions (EDT, paramètres non-null)
     * 2. Pour chaque groupe :
     *    a. Calcule la largeur de contenu disponible
     *    b. Récupère les dimensions préférées de tous les composants
     *    c. Simule le wrapping avec LineWrapper
     *    d. Calcule la hauteur totale du groupe
     * 3. Détermine la hauteur maximale parmi tous les groupes
     * 4. Retourne cette hauteur maximale
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * car utilise Component.getPreferredSize() et GroupRenderer
     * 
     * @param ribbon Le ruban contenant la configuration et le renderer
     * @param ctx Contexte de layout (alignement des en-têtes, marges, etc.)
     * @param groupWidths Tableau des largeurs allouées à chaque groupe
     *                    Ces largeurs peuvent inclure l'espace des en-têtes
     * @param model Modèle de données contenant les valeurs à afficher
     * @param groupModel Modèle des groupes contenant la configuration
     *                   (padding, espacement, etc.)
     * 
     * @return Hauteur totale requise en pixels (≥ 0)
     *         Retourne 0 si les paramètres sont invalides ou s'il n'y a pas de groupes
     * 
     * @throws IllegalStateException si appelé hors de l'EDT
     * 
     * @see SwingUtilities#isEventDispatchThread()
     */
    public int computeRequiredContentHeight(Ribbon ribbon,
                                            HRibbonLayoutContext ctx,
                                            int[] groupWidths,
                                            HRibbonModel model,
                                            HRibbonGroupModel groupModel) {
        // VÉRIFICATION DU THREAD - Swing est single-threaded
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                "PreferredSizeCalculator.computeRequiredContentHeight must be called on the EDT"
            );
        }
        
        // VÉRIFICATION DES PARAMÈTRES OBLIGATOIRES
        if (ribbon == null || ctx == null || model == null || groupModel == null || groupWidths == null) {
            return 0; // Paramètres invalides → hauteur 0
        }
        
        // OBTENTION DU NOMBRE DE GROUPES
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return 0; // Pas de groupes → hauteur 0
        }
        
        // EXTRACTION DES PARAMÈTRES DE CONFIGURATION
        int headerAlignment = ctx.getHeaderAlignment();
        int headerMargin = ctx.getHeaderMargin();
        int headerHeight = ribbon.getHeaderHeight();
        
        // INITIALISATION DES OUTILS ET VARIABLES
        LineWrapper lineWrapper = new LineWrapper(); // Pour simuler le wrapping
        int maxTotalHeight = 0; // Hauteur maximale parmi tous les groupes
        GroupRenderer renderer = ribbon.getGroupRenderer(); // Renderer pour créer les composants
        
        // BOUCLE PRINCIPALE SUR TOUS LES GROUPES
        for (int gi = 0; gi < groupCount; gi++) {
            // RÉCUPÉRATION DU GROUPE COURANT
            HRibbonGroup group = groupModel.getHRibbonGroup(gi);
            if (group == null) {
                continue; // Groupe invalide, passer au suivant
            }
            
            // ÉTAPE 1 : CALCUL DE LA LARGEUR TOTALE DU GROUPE
            int totalGroupWidth = (gi < groupWidths.length) ? Math.max(0, groupWidths[gi]) : 0;
            
            // ÉTAPE 2 : CALCUL DE LA LARGEUR DE CONTENU DISPONIBLE
            int contentWidth = totalGroupWidth;
            
            // AJUSTEMENT POUR LES EN-TÊTES LATÉRAUX (WEST/EAST)
            // Les en-têtes prennent de la place sur le côté
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                // Soustraire la largeur de l'en-tête
                contentWidth = Math.max(totalGroupWidth - ctx.getHeaderWidth(), 1);
            }
            
            // ÉTAPE 3 : EXTRACTION DES PARAMÈTRES DE MISE EN PAGE DU GROUPE
            int padding = Math.max(0, group.getPadding());        // Marge interne
            int innerWidth = Math.max(contentWidth - padding * 2, 1); // Largeur interne disponible
            int spacing = Math.max(0, group.getComponentSpacing()); // Espacement entre composants
            
            // ÉTAPE 4 : OBTENTION DU NOMBRE DE VALEURS DANS CE GROUPE
            int valueCount = model.getValueCount(gi);
            
            // CAS SPÉCIAL : GROUPE VIDE (AUCUNE VALEUR)
            if (valueCount <= 0) {
                // Hauteur minimale = padding haut + bas
                int groupTotalHeight = padding * 2;
                
                // AJOUT DE LA HAUTEUR DE L'EN-TÊTE POUR LES ALIGNEMENTS HAUT/BAS
                if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
                    groupTotalHeight += headerHeight + headerMargin;
                }
                
                // MISE À JOUR DE LA HAUTEUR MAXIMALE
                maxTotalHeight = Math.max(maxTotalHeight, groupTotalHeight);
                continue; // Passer au groupe suivant
            }
            
            // ÉTAPE 5 : COLLECTE DES DIMENSIONS PRÉFÉRÉES DE TOUTES LES VALEURS
            List<Dimension> preferredSizes = new ArrayList<>(valueCount);
            
            for (int pos = 0; pos < valueCount; pos++) {
                // RÉCUPÉRATION DE LA VALEUR
                Object value = model.getValueAt(pos, gi);
                Dimension pref = null;
                
                // CAS 1 : VALEUR DÉJÀ COMPOSANT
                // La valeur est déjà un Component → utiliser directement sa preferredSize
                if (value instanceof Component) {
                    pref = ((Component) value).getPreferredSize();
                }
                // CAS 2 : VALEUR OBJET (NÉCESSITE UN RENDERER)
                else {
                    // RECHERCHE DANS LE CACHE
                    pref = measureCache.get(value);
                    
                    // NON TROUVÉ DANS LE CACHE → CALCUL
                    if (pref == null && renderer != null) {
                        try {
                            // CRÉATION TEMPORAIRE DU COMPOSANT VIA LE RENDERER
                            Component mockComponent = renderer.getGroupComponent(
                                ribbon, value, gi, pos, false, false
                            );
                            
                            // EXTRACTION DE LA DIMENSION PREFÉRÉE
                            if (mockComponent != null) {
                                pref = mockComponent.getPreferredSize();
                            }
                            
                        } catch (Throwable t) {
                            // GESTION ROBUSTE DES ERREURS
                            // En cas d'erreur dans le renderer, utiliser la dimension par défaut
                            pref = null;
                        }
                        
                        // UTILISATION DE LA DIMENSION PAR DÉFAUT SI ÉCHEC
                        if (pref == null) {
                            pref = DEFAULT_DIM;
                        }
                        
                        // MISE EN CACHE (COPIE DÉFENSIVE)
                        measureCache.put(value, new Dimension(pref.width, pref.height));
                    }
                }
                
                // FALLBACK : DIMENSION PAR DÉFAUT SI TOUT ÉCHOUE
                if (pref == null) {
                    pref = DEFAULT_DIM;
                }
                
                // AJOUT À LA LISTE (COPIE DÉFENSIVE)
                preferredSizes.add(new Dimension(pref.width, pref.height));
            }
            
            // ÉTAPE 6 : SIMULATION DU WRAPPING ET CALCUL DE HAUTEUR
            int contentHeight = lineWrapper.computeHeightForPreferences(
                preferredSizes, innerWidth, spacing, padding
            );
            
            // ÉTAPE 7 : AJOUT DE LA HAUTEUR DE L'EN-TÊTE SI NÉCESSAIRE
            int groupTotalHeight = contentHeight;
            if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
                groupTotalHeight += headerHeight + headerMargin;
            }
            
            // ÉTAPE 8 : MISE À JOUR DE LA HAUTEUR MAXIMALE
            maxTotalHeight = Math.max(maxTotalHeight, groupTotalHeight);
        }
        
        // ÉTAPE 9 : RETOUR DE LA HAUTEUR MAXIMALE (GARANTIE NON-NÉGATIVE)
        return Math.max(0, maxTotalHeight);
    }
}