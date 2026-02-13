/*
 * GroupWidthDistributor.java
 * 
 * DISTRIBUTEUR INTELLIGENT DE LARGEURS POUR LES GROUPES DU RUBAN HRibbon
 * 
 * RÔLE PRINCIPAL :
 * Calcule et répartit la largeur disponible entre les différents groupes du ruban,
 * en respectant les préférences, contraintes et modes de distribution configurés.
 * 
 * CONCEPT CLÉ : ALGORITHME DE DISTRIBUTION ADAPTATIVE
 * - Gère deux modes : distribution égale vs distribution intelligente
 * - Respecte la hiérarchie des priorités (width > preferredWidth > default)
 * - Applique les contraintes min/max de chaque groupe
 * - Tient compte de l'espace des en-têtes pour les alignements latéraux
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban.layout;

import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/**
 * GroupWidthDistributor - Gestionnaire de répartition des largeurs de groupes
 * 
 * RESPONSABILITÉS PRÉCISES :
 * 1. Répartit l'espace horizontal disponible entre tous les groupes du ruban
 * 2. Supporte deux modes de distribution : égale ou proportionnelle/intelligente
 * 3. Respecte les préférences de largeur définies dans chaque HRibbonGroup
 * 4. Applique les contraintes minimales et maximales par groupe
 * 5. Tient compte de l'espace nécessaire aux en-têtes pour les alignements latéraux
 * 
 * HIÉRARCHIE DES PRIORITÉS (du plus au moins prioritaire) :
 * 1. Largeur explicite définie (group.getWidth() > 0)
 * 2. Largeur préférée (group.getPreferredWidth() > 0)
 * 3. Largeur par défaut (DEFAULT_GROUP_WIDTH = 150px)
 * 
 * MODES DE DISTRIBUTION :
 * 1. MODE ÉGAL : Répartition uniforme (equalDistribution = true)
 * 2. MODE INTELLIGENT : Répartition proportionnelle aux besoins (default)
 * 
 * @see IGroupWidthDistributor
 * @see HRibbonGroup
 * @see HRibbonGroupModel
 */
public class GroupWidthDistributor implements IGroupWidthDistributor {
    
    // =========================================================================
    // CONSTANTES DE CONFIGURATION
    // =========================================================================
    
    /**
     * Largeur par défaut d'un groupe lorsqu'aucune préférence n'est définie
     * Utilisée comme fallback quand group.getPreferredWidth() <= 0
     * Valeur : 150 pixels 
     */
    private static final int DEFAULT_GROUP_WIDTH = 150;
    
    /**
     * Largeur minimale absolue garantie pour tout groupe
     * Empêche les groupes de devenir invisibles ou trop étroits
     * Valeur : 20 pixels (minimum pour afficher un contenu minimal)
     */
    private static final int ABSOLUTE_MIN = 20;
    
    // =========================================================================
    // VARIABLE D'INSTANCE
    // =========================================================================
    
    /**
     * Indicateur de mode de distribution égale
     * 
     * VALEURS :
     * - true  : Tous les groupes ont la même largeur (répartition uniforme)
     * - false : Largeurs proportionnelles aux besoins (mode intelligent)
     * 
     * VOLATILE : Garantit la visibilité des modifications entre threads
     * (bien que Swing soit principalement single-threaded, c'est une bonne pratique)
     */
    private volatile boolean equalDistribution = false;
    
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    
    /**
     * Constructeur par défaut
     * 
     * Initialise un distributeur de largeurs en mode intelligent (non-égal)
     * par défaut
     */
    public GroupWidthDistributor() {
        // equalDistribution initialisé à false par défaut
    }
    
    // =========================================================================
    // IMPLÉMENTATION DE L'INTERFACE IGroupWidthDistributor
    // =========================================================================
    
    /**
     * Active ou désactive le mode de distribution égale
     * 
     * @param equal true pour distribution égale, false pour distribution intelligente
     * 
     * EFFET :
     * - true  : Tous les groupes auront exactement la même largeur
     * - false : Les largeurs seront proportionnelles aux besoins de chaque groupe
     */
    @Override
    public void setEqualDistribution(boolean equal) {
        this.equalDistribution = equal;
    }
    
    /**
     * Vérifie si le mode de distribution égale est activé
     * 
     * @return true si la distribution égale est activée, false sinon
     */
    @Override
    public boolean isEqualDistribution() {
        return this.equalDistribution;
    }
    
    /**
     * Distribue les largeurs disponibles entre tous les groupes du ruban
     * 
     * ALGORITHME GÉNÉRAL :
     * 1. Vérifier les paramètres d'entrée (null, groupes vides)
     * 2. Calculer l'espace disponible après soustraction des marges
     * 3. Choisir le mode de distribution (égal vs intelligent)
     * 4. Appliquer l'algorithme de distribution correspondant
     * 5. Appliquer les contraintes min/max de chaque groupe
     * 6. Garantir des largeurs minimales raisonnables
     * 
     * @param ctx Contexte de layout contenant :
     *            - Alignement des en-têtes (headerAlignment)
     *            - Largeur des en-têtes (headerWidth)
     *            - Marge entre groupes (groupMargin)
     *            - Préférence de distribution égale (isEqualDistribution)
     * @param groupModel Modèle des groupes contenant la configuration
     *                   de chaque HRibbonGroup (width, preferredWidth, min/max)
     * @param availableWidth Largeur totale disponible dans le conteneur parent
     *                       (déjà soustraite des marges gauche/droite du parent)
     * @return Tableau d'entiers de taille groupModel.getGroupCount()
     *         Chaque élément représente la largeur totale allouée au groupe correspondant
     *         (inclut l'espace pour l'en-tête si alignement WEST/EAST)
     * 
     * @throws Aucune exception levée, retourne un tableau vide pour les cas invalides
     */
    @Override
    public int[] distributeWidths(HRibbonLayoutContext ctx, 
                                  HRibbonGroupModel groupModel, 
                                  int availableWidth) {
        // ÉTAPE 1 : VÉRIFICATION DES PARAMÈTRES D'ENTRÉE
        if (groupModel == null) {
            return new int[0]; // Pas de modèle → pas de groupes
        }
        
        final int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return new int[0]; // Modèle vide → pas de groupes
        }
        
        // ÉTAPE 2 : INITIALISATION DES RÉSULTATS
        int[] widths = new int[groupCount];
        
        // ÉTAPE 3 : EXTRACTION DES PARAMÈTRES DE CONFIGURATION
        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;
        int groupMargin = (ctx != null) ? ctx.getGroupMargin() : 0;
        
        // ÉTAPE 4 : CALCUL DE L'ESPACE DISPONIBLE POUR LES GROUPES
        // Soustraction des marges inter-groupes
        int totalMargin = groupMargin * Math.max(0, groupCount - 1);
        int widthForGroups = Math.max(availableWidth - totalMargin, 0);
        
        // ÉTAPE 5 : DÉTERMINATION DU MODE DE DISTRIBUTION
        // Priorité : contexte → paramètre d'instance → false par défaut
        boolean useEqual = (ctx != null && ctx.isEqualDistribution()) || this.equalDistribution;
        
        // =====================================================================
        // MODE DE DISTRIBUTION ÉGALE (SIMPLIFIÉ)
        // =====================================================================
        if (useEqual) {
            // Calcul de la largeur par groupe (division entière)
            int widthPerGroup = (groupCount > 0) ? (widthForGroups / groupCount) : 0;
            
            // Attribution de la même largeur à tous les groupes
            for (int i = 0; i < groupCount; i++) {
                widths[i] = Math.max(widthPerGroup, 0); // Garantir non-négatif
            }
            
            // Application de la largeur minimale absolue
            for (int i = 0; i < groupCount; i++) {
                widths[i] = Math.max(widths[i], ABSOLUTE_MIN);
            }
            
            return widths; // Distribution terminée
        }
        
        // =====================================================================
        // MODE DE DISTRIBUTION INTELLIGENTE (PROPORTIONNELLE)
        // =====================================================================
        
        // SOUS-ÉTAPE 5.1 : CALCUL DES LARGEURS DEMANDÉES PAR CHAQUE GROUPE
        int totalRequestedWidth = 0; // Somme de toutes les largeurs demandées
        int[] requestedWidths = new int[groupCount]; // Largeurs demandées par groupe
        boolean[] isFixed = new boolean[groupCount]; // Indicateur "largeur fixe"
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            int req; // Largeur demandée pour ce groupe
            
            if (group != null) {
                // HIÉRARCHIE DES PRIORITÉS POUR LA LARGEUR DEMANDÉE
                if (group.getWidth() > 0) {
                    // PRIORITÉ 1 : Largeur explicite définie
                    req = group.getWidth();
                    isFixed[i] = true; // Marqué comme "fixe" (prioritaire)
                    
                } else if (group.getPreferredWidth() > 0) {
                    // PRIORITÉ 2 : Largeur préférée
                    req = group.getPreferredWidth();
                    isFixed[i] = false; // Flexible (peut être ajustée)
                    
                } else {
                    // PRIORITÉ 3 : Largeur par défaut
                    req = DEFAULT_GROUP_WIDTH;
                    isFixed[i] = false; // Flexible
                }
                
                // AJUSTEMENT POUR LES EN-TÊTES LATÉRAUX (WEST/EAST)
                // Les en-têtes à gauche/droite prennent de la place dans la largeur du groupe
                if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                    req += headerWidth; // Ajouter l'espace nécessaire à l'en-tête
                }
                
                // GARANTIR UN MINIMUM ABSOLU
                requestedWidths[i] = Math.max(req, ABSOLUTE_MIN);
                
            } else {
                // CAS : GROUPE NULL (ne devrait pas arriver normalement)
                requestedWidths[i] = Math.max(DEFAULT_GROUP_WIDTH, ABSOLUTE_MIN);
                isFixed[i] = false; // Flexible par défaut
            }
            
            // MISE À JOUR DE LA SOMME TOTALE
            totalRequestedWidth += requestedWidths[i];
        }
        
        // SOUS-ÉTAPE 5.2 : DÉTERMINATION DU SCÉNARIO (ESPACE SUFFISANT OU NON)
        if (totalRequestedWidth <= widthForGroups) {
            // SCÉNARIO 1 : ESPACE SUFFISANT POUR TOUTES LES DEMANDES
            // Copier les largeurs demandées comme base
            System.arraycopy(requestedWidths, 0, widths, 0, groupCount);
            
            // DISTRIBUTION DE L'ESPACE RESTANT AUX GROUPES FLEXIBLES
            int remainingSpace = widthForGroups - totalRequestedWidth;
            int flexibleCount = 0;
            
            // Compter les groupes flexibles (non-fixes)
            for (int i = 0; i < groupCount; i++) {
                if (!isFixed[i]) flexibleCount++;
            }
            
            // Répartition équitable de l'espace restant entre les groupes flexibles
            if (flexibleCount > 0 && remainingSpace > 0) {
                int extraPerGroup = remainingSpace / flexibleCount;
                
                for (int i = 0; i < groupCount; i++) {
                    if (!isFixed[i]) {
                        widths[i] += extraPerGroup;
                    }
                }
            }
            
        } else {
            // SCÉNARIO 2 : ESPACE INSUFFISANT → RÉDUCTION INTELLIGENTE
            // Calculer l'espace occupé par les groupes fixes vs flexibles
            int fixedSpace = 0;   // Espace réservé aux groupes fixes
            int flexibleSpace = 0; // Espace demandé par les groupes flexibles
            
            for (int i = 0; i < groupCount; i++) {
                if (isFixed[i]) {
                    fixedSpace += requestedWidths[i];
                } else {
                    flexibleSpace += requestedWidths[i];
                }
            }
            
            // Calculer l'espace disponible pour les groupes flexibles
            int availableForFlexible = Math.max(widthForGroups - fixedSpace, 0);
            
            // RÉPARTITION PROPORTIONNELLE POUR LES GROUPES FLEXIBLES
            for (int i = 0; i < groupCount; i++) {
                if (isFixed[i]) {
                    // Groupes fixes : conserver leur largeur demandée
                    widths[i] = requestedWidths[i];
                    
                } else {
                    // Groupes flexibles : réduction proportionnelle
                    if (flexibleSpace > 0) {
                        // Calcul du ratio de réduction pour ce groupe
                        float ratio = (float) requestedWidths[i] / (float) flexibleSpace;
                        
                        // Application proportionnelle de l'espace disponible
                        widths[i] = Math.max((int) (availableForFlexible * ratio), ABSOLUTE_MIN);
                        
                    } else {
                        // Cas limite : pas d'espace flexible demandé
                        widths[i] = ABSOLUTE_MIN;
                    }
                }
            }
        }
        
        // ÉTAPE 6 : APPLICATION DES CONTRAINTES MIN/MAX PAR GROUPE
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            
            if (group == null) {
                // Groupe null : garantir le minimum absolu
                widths[i] = Math.max(widths[i], ABSOLUTE_MIN);
                continue;
            }
            
            // EXTRACTION DES CONTRAINTES DU GROUPE
            int minW = group.getMinWidth();  // Largeur minimale configurée
            int maxW = group.getMaxWidth();  // Largeur maximale configurée
            
            // AJUSTEMENT POUR LES EN-TÊTES LATÉRAUX
            // Les contraintes min/max doivent inclure l'espace de l'en-tête
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                if (minW > 0) minW += headerWidth;
                if (maxW > 0) maxW += headerWidth;
            }
            
            // APPLICATION DE LA CONTRAINTE MINIMALE
            if (minW > 0) {
                widths[i] = Math.max(widths[i], minW);
            }
            
            // APPLICATION DE LA CONTRAINTE MAXIMALE
            if (maxW > 0) {
                widths[i] = Math.min(widths[i], maxW);
            }
            
            // GARANTIE DE SÉCURITÉ : MINIMUM ABSOLU
            widths[i] = Math.max(widths[i], ABSOLUTE_MIN);
        }
        
        // ÉTAPE 7 : RETOUR DES LARGEURS CALCULÉES
        return widths;
    }
}