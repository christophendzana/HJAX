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
import java.awt.Insets;
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
 * RESPONSABILITÉS PRÉCISES : 1. Simule le wrapping des composants dans chaque
 * groupe 2. Calcule la hauteur de contenu requise pour chaque groupe 3.
 * Détermine la hauteur maximale parmi tous les groupes 4. Gère un cache de
 * dimensions pour optimiser les performances 5. Tient compte des en-têtes,
 * padding, espacement et autres contraintes
 *
 * IMPORTANCE STRATÉGIQUE : Cette classe est cruciale pour : - Le calcul du
 * preferredSize() du ruban - La planification du layout avant le rendu effectif
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
     * - Clé : Valeur objet (String, Number, etc.) - Valeur : Dimension préférée
     * calculée pour cette valeur
     *
     * CARACTÉRISTIQUES DU CACHE : 1. WeakHashMap : Les clés sont référencées
     * faiblement → Le cache peut être nettoyé automatiquement par le garbage
     * collector → Évite les fuites mémoire avec des objets temporaires 2.
     * Réduction du coût : Évite de recalculer les mêmes dimensions 3.
     * Synchronisation : Non thread-safe (usage mono-thread Swing)
     *
     * QUAND UTILISER LE CACHE : - Pour les valeurs objets transformées en
     * composants via GroupRenderer - Pas pour les Component directs (ils ont
     * déjà getPreferredSize())
     */
    private final Map<Object, Dimension> measureCache = new WeakHashMap<>();

    /**
     * Dimension par défaut utilisée comme fallback
     *
     * UTILISATION : - Quand une valeur n'a pas de dimension calculable - Quand
     * le renderer échoue ou retourne null - Pour garantir une dimension
     * minimale raisonnable
     *
     * VALEUR : 50px de largeur × 24px de hauteur (taille standard pour un
     * composant Swing basique)
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
     * QUAND UTILISER : - Après un changement de renderer (nouvelles tailles
     * possibles) - Après une modification importante du modèle - Pour libérer
     * de la mémoire si nécessaire - Avant une série de calculs avec de
     * nouvelles données
     *
     * EFFET : Toutes les dimensions devront être recalculées au prochain appel
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
     * ALGORITHME PRINCIPAL : 1. Vérification des préconditions (EDT, paramètres
     * non-null) 2. Pour chaque groupe : a. Calcule la largeur de contenu
     * disponible b. Récupère les dimensions préférées de tous les composants c.
     * Simule le wrapping avec LineWrapper d. Calcule la hauteur totale du
     * groupe 3. Détermine la hauteur maximale parmi tous les groupes 4.
     * Retourne cette hauteur maximale
     *
     * CONTRAINTE THREAD : Doit être appelé sur l'Event Dispatch Thread (EDT)
     * car utilise Component.getPreferredSize() et GroupRenderer
     *
     * @param ribbon Le ruban contenant la configuration et le renderer
     * @param ctx Contexte de layout (alignement des en-têtes, marges, etc.)
     * @param groupWidths Tableau des largeurs allouées à chaque groupe Ces
     * largeurs peuvent inclure l'espace des en-têtes
     * @param model Modèle de données contenant les valeurs à afficher
     * @param groupModel Modèle des groupes contenant la configuration (padding,
     * espacement, etc.)
     *
     * @return Hauteur totale requise en pixels (≥ 0) Retourne 0 si les
     * paramètres sont invalides ou s'il n'y a pas de groupes
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

        int headerAlignment = ctx.getHeaderAlignment();
        int headerMargin = ctx.getHeaderMargin();
        int headerHeight = ribbon.getHeaderHeight();
        Insets insets = ribbon.getInsets();

        int heightGroup = 0;
        
        if (headerAlignment == Ribbon.HEADER_EAST || headerAlignment == Ribbon.HEADER_WEST) {

            for (int gi = 0; gi < groupModel.getGroupCount(); gi++) {
                 heightGroup = ribbon.getRibbonHeight() - (insets.top + insets.bottom + headerMargin);
                HRibbonGroup group = groupModel.getHRibbonGroup(gi);
                group.setHeigth(heightGroup);
                return heightGroup;                
            }
        }else{
            for (int gi = 0; gi < groupModel.getGroupCount(); gi++) {
                heightGroup = ribbon.getRibbonHeight() - (insets.top + insets.bottom + headerMargin + headerHeight);
                HRibbonGroup group = groupModel.getHRibbonGroup(gi);
                group.setHeigth(heightGroup);
                return heightGroup;                
            }
        }
    
        return heightGroup;
        
        
//        // OBTENTION DU NOMBRE DE GROUPES
//        int groupCount = groupModel.getGroupCount();
//        if (groupCount == 0) {
//            return 0; // Pas de groupes → hauteur 0
//        }
//
//        // EXTRACTION DES PARAMÈTRES DE CONFIGURATION
//        int headerAlignment = ctx.getHeaderAlignment();
//        int headerMargin = ctx.getHeaderMargin();
//        int headerHeight = ribbon.getHeaderHeight();
//
//        // INITIALISATION DES OUTILS ET VARIABLES
//        LineWrapper lineWrapper = new LineWrapper(); // Pour simuler le wrapping
//        int maxTotalHeight = 0; // Hauteur maximale parmi tous les groupes
//        GroupRenderer renderer = ribbon.getGroupRenderer(); // Renderer pour créer les composants
//
//        // BOUCLE PRINCIPALE SUR TOUS LES GROUPES
//        for (int gi = 0; gi < groupCount; gi++) {
//            // RÉCUPÉRATION DU GROUPE COURANT
//            HRibbonGroup group = groupModel.getHRibbonGroup(gi);
//            if (group == null) {
//                continue; // Groupe invalide, passer au suivant
//            }
//
//            // ÉTAPE 1 : CALCUL DE LA LARGEUR TOTALE DU GROUPE
//            int totalGroupWidth = (gi < groupWidths.length) ? Math.max(0, groupWidths[gi]) : 0;
//
//            // ÉTAPE 2 : CALCUL DE LA LARGEUR DE CONTENU DISPONIBLE
//            int contentWidth = totalGroupWidth;
//
//            // AJUSTEMENT POUR LES EN-TÊTES LATÉRAUX (WEST/EAST)
//            // Les en-têtes prennent de la place sur le côté
//            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
//                // Soustraire la largeur de l'en-tête
//                contentWidth = Math.max(totalGroupWidth - ctx.getHeaderWidth(), 1);
//            }
//
//            // ÉTAPE 3 : EXTRACTION DES PARAMÈTRES DE MISE EN PAGE DU GROUPE
//            int padding = Math.max(0, group.getPadding());        // Marge interne
//            int innerWidth = Math.max(contentWidth - padding * 2, 1); // Largeur interne disponible
//            int spacing = Math.max(0, group.getComponentMargin()); // Espacement entre composants
//
//            // ÉTAPE 4 : OBTENTION DU NOMBRE DE VALEURS DANS CE GROUPE
//            int valueCount = model.getValueCount(gi);
//
//            // CAS SPÉCIAL : GROUPE VIDE (AUCUNE VALEUR)
//            if (valueCount <= 0) {
//                // Hauteur minimale = padding haut + bas
//                int groupTotalHeight = padding * 2;
//
//                // AJOUT DE LA HAUTEUR DE L'EN-TÊTE POUR LES ALIGNEMENTS HAUT/BAS
//                if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
//                    groupTotalHeight += headerHeight + headerMargin;
//                }
//
//                // MISE À JOUR DE LA HAUTEUR MAXIMALE
//                maxTotalHeight = Math.max(maxTotalHeight, groupTotalHeight);
//                continue; // Passer au groupe suivant
//            }
//
//            // ÉTAPE 5 : COLLECTE DES DIMENSIONS PRÉFÉRÉES DE TOUTES LES VALEURS
//            List<Dimension> preferredSizes = new ArrayList<>(valueCount);
//
//            for (int pos = 0; pos < valueCount; pos++) {
//                // RÉCUPÉRATION DE LA VALEUR
//                Object value = model.getValueAt(pos, gi);
//                Dimension pref = null;
//
//                // CAS 1 : VALEUR DÉJÀ COMPOSANT
//                // La valeur est déjà un Component → utiliser directement sa preferredSize
//                if (value instanceof Component) {
//                    pref = ((Component) value).getPreferredSize();
//                } // CAS 2 : VALEUR OBJET (NÉCESSITE UN RENDERER)
//                else {
//                    // RECHERCHE DANS LE CACHE
//                    pref = measureCache.get(value);
//
//                    // NON TROUVÉ DANS LE CACHE → CALCUL
//                    if (pref == null && renderer != null) {
//                        try {
//                            // CRÉATION TEMPORAIRE DU COMPOSANT VIA LE RENDERER
//                            Component mockComponent = renderer.getGroupComponent(
//                                    ribbon, value, gi, pos, false, false
//                            );
//
//                            // EXTRACTION DE LA DIMENSION PREFÉRÉE
//                            if (mockComponent != null) {
//                                pref = mockComponent.getPreferredSize();
//                            }
//
//                        } catch (Throwable t) {
//                            // GESTION ROBUSTE DES ERREURS
//                            // En cas d'erreur dans le renderer, utiliser la dimension par défaut
//                            pref = null;
//                        }
//
//                        // UTILISATION DE LA DIMENSION PAR DÉFAUT SI ÉCHEC
//                        if (pref == null) {
//                            pref = DEFAULT_DIM;
//                        }
//
//                        // MISE EN CACHE (COPIE DÉFENSIVE)
//                        measureCache.put(value, new Dimension(pref.width, pref.height));
//                    }
//                }
//
//                // FALLBACK : DIMENSION PAR DÉFAUT SI TOUT ÉCHOUE
//                if (pref == null) {
//                    pref = DEFAULT_DIM;
//                }
//
//                // AJOUT À LA LISTE (COPIE DÉFENSIVE)
//                preferredSizes.add(new Dimension(pref.width, pref.height));
//            }
//
//            // ÉTAPE 6 : SIMULATION DU WRAPPING ET CALCUL DE HAUTEUR
//            int contentHeight = lineWrapper.computeHeightForPreferences(
//                    preferredSizes, innerWidth, spacing, padding
//            );
//
//            // ÉTAPE 7 : AJOUT DE LA HAUTEUR DE L'EN-TÊTE SI NÉCESSAIRE
//            int groupTotalHeight = contentHeight;
//            if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
//                groupTotalHeight += headerHeight + headerMargin;
//            }
//
//            // ÉTAPE 8 : MISE À JOUR DE LA HAUTEUR MAXIMALE
//            maxTotalHeight = Math.max(maxTotalHeight, groupTotalHeight);
//        }
        // ÉTAPE 9 : RETOUR DE LA HAUTEUR MAXIMALE (GARANTIE NON-NÉGATIVE)
//        return Math.max(0, maxTotalHeight);
    }

    /**
     * Estime la largeur idéale de chaque groupe pour le calcul du
     * preferredLayoutSize.Deux modes selon le thread appelant : - Hors EDT :
     * estimation rapide (preferredWidth ou 100px) sans toucher aux composants
     * Swing - Sur EDT : calcul précis par sommation des preferredSize des
     * composants
     *
     * Cette méthode est le pendant en largeur de computeRequiredContentHeight()
     * en hauteur.Les deux ensemble permettent à preferredLayoutSize() d'avoir
     * une Dimension complète.
     *
     * @param ribbon
     * @param ctx
     * @param groupModel
     * @param groupCount
     * @param model
     */
    public int[] estimateGroupWidths(Ribbon ribbon,
            HRibbonGroupModel groupModel,
            HRibbonModel model,
            HRibbonLayoutContext ctx) {

        int[] estimatedWidths = new int[groupModel.getGroupCount()];
        int headerAlignment = ctx.getHeaderAlignment();
        int headerWidth = ctx.getHeaderWidth();
        GroupRenderer renderer = ribbon.getGroupRenderer();

        // Hors EDT : on ne peut pas appeler getPreferredSize() sur des composants Swing.
        // On retourne une estimation mais thread-safe.
        if (!SwingUtilities.isEventDispatchThread()) {
             throw new IllegalStateException(
                    "PreferredSizeCalculator.estimateGroupWidths must be called on the EDT"
            );
        }

        // Sur EDT : calcul précis par sommation des composants
        for (int i = 0; i < groupModel.getGroupCount(); i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            // Priorité 1 : preferredWidth explicitement défini sur le groupe
            if (group != null && group.getPreferredWidth() > 0) {
                int gw = group.getPreferredWidth();
                if (headerAlignment == Ribbon.HEADER_WEST
                        || headerAlignment == Ribbon.HEADER_EAST) {
                    gw += headerWidth;
                }
                estimatedWidths[i] = Math.max(gw, 20);
                continue;
            }

            // Priorité 2 : calculer depuis les composants du modèle
            int padding = (group != null) ? Math.max(0, group.getPadding()) : 0;
            int spacing = (group != null) ? Math.max(0, group.getComponentMargin()) : 0;
            int valueCount = model.getValueCount(i);
            int totalWidth = 0;

            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, i);
                Dimension pref = null;

                if (value instanceof Component) {
                    pref = ((Component) value).getPreferredSize();
                } else if (renderer != null) {
                    try {
                        Component mc = renderer.getGroupComponent(
                                ribbon, value, i, pos, false, false
                        );
                        if (mc != null) {
                            pref = mc.getPreferredSize();
                        }
                    } catch (Throwable t) {
                        pref = null;
                    }
                }

                if (pref == null) {
                    pref = new Dimension(40, 20);
                }
                if (totalWidth > 0) {
                    totalWidth += spacing;
                }
                totalWidth += pref.width;
            }

            int gw = totalWidth + padding * 2;
            if (headerAlignment == Ribbon.HEADER_WEST
                    || headerAlignment == Ribbon.HEADER_EAST) {
                gw += headerWidth;
            }
            estimatedWidths[i] = Math.max(gw, 20);
        }

        return estimatedWidths;
    }

    /**
     * Calcule et assigne la preferredWidth de chaque groupe de façon
     * intelligente.
     *
     * ALGORITHME EN 3 PASSES : Passe 1 : Calculer la largeur naturelle de
     * chaque groupe (somme des preferredSize.width de ses composants + marges +
     * padding) Passe 2 : Calculer le poids de chaque groupe
     * (largeurNaturelle[i] / totalNaturel) Passe 3 : Distribuer availableWidth
     * proportionnellement aux poids
     *
     * Le résultat est assigné directement via group.setPreferredWidth().
     * GroupWidthDistributor s'en sert naturellement sans modification.
     *
     * @param ribbon le ruban parent
     * @param model le modèle de données
     * @param groupModel le modèle de groupes
     * @param groupCount le nombre de groupes
     * @param availableWidth la largeur nette disponible pour les groupes
     * @param ctx le contexte de layout
     */
    public void computeAndAssignPreferredWidths(Ribbon ribbon,
            HRibbonModel model,
            HRibbonGroupModel groupModel,
            int groupCount,
            int availableWidth,
            HRibbonLayoutContext ctx) {

        if (ribbon == null || model == null || groupModel == null
                || groupCount == 0 || availableWidth <= 0) {
            return;
        }

        GroupRenderer renderer = ribbon.getGroupRenderer();
        int headerAlignment = ctx.getHeaderAlignment();
        int headerWidth = ctx.getHeaderWidth();

        // =====================================================================
        // PASSE 1 : CALCUL DE LA LARGEUR NATURELLE DE CHAQUE GROUPE
        //
        // La largeur naturelle = espace qu'occuperaient tous les composants
        // du groupe placés sur une seule ligne, avec leurs marges et padding.
        //
        // Formule :
        //   largeurNaturelle = somme(preferredSize.width de chaque composant)
        //                    + (valueCount - 1) × componentMargin
        //                    + padding × 2
        //
        // Si le header est WEST ou EAST, il empiète sur la largeur du groupe.
        // On l'ajoute donc à la largeur naturelle pour que GroupWidthDistributor
        // puisse le déduire correctement lors du layout.
        //
        // Seuiler de 50px : garantit qu'aucun groupe n'a une largeur naturelle
        // nulle, ce qui fausserait le calcul des poids.
        // =====================================================================
        double[] largeurNaturelle = new double[groupCount];
        double totalNaturel = 0;

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group == null) {
                largeurNaturelle[i] = 50;
                totalNaturel += 50;
                continue;
            }

            int padding = Math.max(0, group.getPadding());
            int compMargin = Math.max(0, group.getComponentMargin());
            int valueCount = model.getValueCount(i);
            int sommeLargeurs = 0;

            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, i);
                Dimension pref = null;

                if (value instanceof Component) {
                    pref = ((Component) value).getPreferredSize();
                } else if (renderer != null) {
                    try {
                        Component c = renderer.getGroupComponent(
                                ribbon, value, i, pos, false, false
                        );
                        if (c != null) {
                            pref = c.getPreferredSize();
                        }
                    } catch (Throwable t) {
                        pref = null;
                    }
                }

                if (pref == null) {
                    pref = DEFAULT_DIM;
                }
                sommeLargeurs += pref.width;
            }

            // Largeur naturelle = somme composants + marges entre eux + padding
            double ln = sommeLargeurs + (valueCount > 1 ? (valueCount - 1) * compMargin : 0) + padding * 2;

            // Ajouter la largeur du header latéral si applicable
            if (headerAlignment == Ribbon.HEADER_WEST
                    || headerAlignment == Ribbon.HEADER_EAST) {
                ln += headerWidth;
            }

            largeurNaturelle[i] = Math.max(ln, 50);
            totalNaturel += largeurNaturelle[i];
        }

        // =====================================================================
        // PASSE 2 : CALCUL DES POIDS
        //
        // poids[i] = largeurNaturelle[i] / totalNaturel
        //
        // Un groupe dont les composants sont plus larges
        // obtiendra un poids plus élevé et donc plus d'espace.
        // =====================================================================
        double[] poids = new double[groupCount];
        for (int i = 0; i < groupCount; i++) {
            poids[i] = (totalNaturel > 0) ? largeurNaturelle[i] / totalNaturel : 1.0 / groupCount;
        }

        // =====================================================================
        // PASSE 3 : DISTRIBUTION PROPORTIONNELLE ET ASSIGNATION
        //
        // preferredWidth[i] = availableWidth × poids[i]
        //
        // On déduit les marges entre groupes de l'espace à distribuer pour
        // que la somme des preferredWidth ne dépasse pas availableWidth.
        //
        // Seuil de 30px : évite une preferredWidth nulle ou trop petite
        // qui rendrait un groupe invisible.
        // =====================================================================
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        int totalMargins = groupMargin * (groupCount - 1);
        int espaceNet = Math.max(0, availableWidth - totalMargins);

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group == null) {
                continue;
            }

            int pw = (int) Math.round(espaceNet * poids[i]);
            pw = Math.max(pw, 30);
            group.setPreferredWidth(pw);
            System.out.println(group.toString());
        }
    }

}