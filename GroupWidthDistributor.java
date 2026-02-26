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
 * RESPONSABILITÉS PRÉCISES : 1. Répartit l'espace horizontal disponible entre
 * tous les groupes du ruban 2. Supporte deux modes de distribution : égale ou
 * proportionnelle/intelligente 3. Respecte les préférences de largeur définies
 * dans chaque HRibbonGroup 4. Applique les contraintes minimales et maximales
 * par groupe 5. Tient compte de l'espace nécessaire aux en-têtes pour les
 * alignements latéraux
 *
 * HIÉRARCHIE DES PRIORITÉS (du plus au moins prioritaire) : 1. Largeur
 * explicite définie (group.getWidth() > 0) 2. Largeur préférée
 * (group.getPreferredWidth() > 0) 3. Largeur par défaut (DEFAULT_GROUP_WIDTH =
 * 150px)
 *
 * MODES DE DISTRIBUTION : 1. MODE ÉGAL : Répartition uniforme
 * (equalDistribution = true) 2. MODE INTELLIGENT : Répartition proportionnelle
 * aux besoins (default)
 *
 * @see IGroupWidthDistributor
 * @see HRibbonGroup
 * @see HRibbonGroupModel
 */
public class GroupWidthDistributor implements IGroupWidthDistributor {

    // =========================================================================
    // VARIABLE D'INSTANCE
    // =========================================================================
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    /**
     * Constructeur par défaut
     *
     * Initialise un distributeur de largeurs en mode intelligent (non-égal) par
     * défaut
     */
    public GroupWidthDistributor() {
        // equalDistribution initialisé à false par défaut
    }

    // =========================================================================
    // IMPLÉMENTATION DE L'INTERFACE IGroupWidthDistributor
    // =========================================================================
    /**
     * Distribue les largeurs disponibles entre tous les groupes du ruban
     *
     * ALGORITHME GÉNÉRAL : 1. Vérifier les paramètres d'entrée (null, groupes
     * vides) 2. Calculer l'espace disponible après soustraction des marges 3.
     * Choisir le mode de distribution (égal vs intelligent) 4. Appliquer
     * l'algorithme de distribution correspondant 5. Appliquer les contraintes
     * min/max de chaque groupe 6. Garantir des largeurs minimales raisonnables
     *
     * @param ctx Contexte de layout contenant : - Alignement des en-têtes
     * (headerAlignment) - Largeur des en-têtes (headerWidth) - Marge entre
     * groupes (groupMargin) - Préférence de distribution égale
     * (isEqualDistribution)
     * @param groupModel Modèle des groupes contenant la configuration de chaque
     * HRibbonGroup (width, preferredWidth, min/max)
     * @param availableWidth Largeur totale disponible dans le conteneur parent
     * (déjà soustraite des marges gauche/droite du parent)
     * @return Tableau d'entiers de taille groupModel.getGroupCount() Chaque
     * élément représente la largeur totale allouée au groupe correspondant
     * (inclut l'espace pour l'en-tête si alignement WEST/EAST)
     *
     * Aucune exception levée, retourne un tableau vide pour les cas invalides
     */
    @Override
    public int[] distributeWidths(HRibbonLayoutContext ctx,
            HRibbonGroupModel groupModel,
            int availableWidth) {

        // =========================================================================
        // ÉTAPE 1 : VALIDATION DES PARAMÈTRES
        // =========================================================================
        // Vérifie que le modèle des groupes existe.
        // Sans modèle → aucun groupe → aucun calcul possible.
        if (groupModel == null) {
            return new int[0];
        }

        // Récupération du nombre total de groupes à distribuer.
        final int groupCount = groupModel.getGroupCount();

        // Si aucun groupe n'est présent, retourner un tableau vide.
        if (groupCount <= 0) {
            return new int[0];
        }

        // Tableau résultat : largeur finale attribuée à chaque groupe.
        int[] finalWidths = new int[groupCount];

        // =========================================================================
        // ÉTAPE 2 : EXTRACTION DU CONTEXTE
        // =========================================================================
        // Récupération sécurisée des paramètres du contexte.
        // Si ctx est null, on applique des valeurs par défaut raisonnables.
        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;
        int groupMargin = (ctx != null) ? ctx.getGroupMargin() : 0;
        int defaultWidth = (ctx != null) ? ctx.getDefautlGroupWidth() : 150;
        int absoluteMin = (ctx != null) ? ctx.getAbsoluteGroupMin() : 1;

        // Calcul de la largeur totale occupée par les marges inter-groupes.
        // Exemple : 4 groupes → 3 marges.
        int totalMargins = groupMargin * Math.max(0, groupCount - 1);

        // Largeur réellement disponible pour les groupes eux-mêmes.
        // On soustrait les marges et on garantit une valeur ≥ 0.
        int widthForGroups = Math.max(availableWidth - totalMargins, 0);

        // =========================================================================
        // ÉTAPE 3 : CLASSIFICATION DES GROUPES (FIXED / PREFERRED / DEFAULT)
        // =========================================================================
        // Pour chaque groupe, on détermine sa largeur "demandée"
        // selon la hiérarchie suivante :
        // 1. width explicite
        // 2. preferredWidth
        // 3. largeur par défaut
        int[] requested = new int[groupCount];

        boolean[] isFixed = new boolean[groupCount];   // width explicite
        boolean[] isPreferred = new boolean[groupCount];   // preferredWidth
        boolean[] isDefault = new boolean[groupCount];   // largeur par défaut

        int totalRequested = 0; // Somme totale des largeurs demandées

        for (int i = 0; i < groupCount; i++) {

            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            int req; // largeur demandée pour ce groupe

            if (group != null) {

                if (group.getWidth() != -1) {
                    // PRIORITÉ 1 : largeur fixée explicitement par l'utilisateur
                    req = group.getWidth();
                    isFixed[i] = true;

                } else if (group.getPreferredWidth() != -1) {
                    // PRIORITÉ 2 : largeur préférée calculée ou définie
                    req = group.getPreferredWidth();
                    isPreferred[i] = true;

                } else {
                    // PRIORITÉ 3 : largeur par défaut du ruban
                    req = defaultWidth;
                    isDefault[i] = true;
                }

            } else {
                // Groupe null → fallback sur largeur par défaut
                req = defaultWidth;
                isDefault[i] = true;
            }

            // Si le header est positionné à gauche ou à droite,
            // il consomme de la largeur dans le groupe.
            if (headerAlignment == Ribbon.HEADER_WEST
                    || headerAlignment == Ribbon.HEADER_EAST) {
                req += headerWidth;
            }

            // Sécurisation : aucun groupe ne descend sous le minimum absolu.
            requested[i] = Math.max(req, absoluteMin);

            // Accumulation pour comparer avec l'espace disponible.
            totalRequested += requested[i];
        }

        // =========================================================================
        // ÉTAPE 4 : SCÉNARIO 1 — ESPACE SUFFISANT
        // =========================================================================
        // Si la somme des largeurs demandées tient dans l'espace disponible,
        // on distribue normalement, avec éventuellement redistribution du surplus.
        if (totalRequested <= widthForGroups) {

            // Base : on copie les largeurs demandées telles quelles.
            System.arraycopy(requested, 0, finalWidths, 0, groupCount);

            // Calcul du surplus d'espace restant à distribuer.
            int remaining = widthForGroups - totalRequested;

            // Le surplus ne doit être distribué qu'aux groupes non FIXED.
            int flexibleCount = 0;
            for (int i = 0; i < groupCount; i++) {
                if (!isFixed[i]) {
                    flexibleCount++;
                }
            }

            if (flexibleCount > 0) {

                // Distribution uniforme du surplus.
                int extraPerGroup = remaining / flexibleCount;

                // Pixels restants dus aux divisions entières.
                int remainder = remaining % flexibleCount;

                for (int i = 0; i < groupCount; i++) {
                    if (!isFixed[i]) {

                        finalWidths[i] += extraPerGroup;

                        // Répartition du reste pixel par pixel
                        if (remainder > 0) {
                            finalWidths[i]++;
                            remainder--;
                        }
                    }
                }
            }
        } // =========================================================================
        // ÉTAPE 5 : SCÉNARIO 2 — RÉDUCTION (PRIORITÉ RESPECTÉE)
        // =========================================================================
        else {

            // 1️⃣ On réserve d'abord l'espace des groupes FIXED.
            int fixedSpace = 0;

            for (int i = 0; i < groupCount; i++) {
                if (isFixed[i]) {
                    finalWidths[i] = requested[i];
                    fixedSpace += requested[i];
                }
            }

            // Espace restant pour les autres catégories.
            int remaining = Math.max(widthForGroups - fixedSpace, 0);

            // 2️⃣ Réduction des groupes DEFAULT en premier
            remaining = reduceCategory(
                    remaining,
                    requested,
                    finalWidths,
                    isDefault,
                    absoluteMin
            );

            // 3️⃣ Si nécessaire, réduction des groupes PREFERRED
            remaining = reduceCategory(
                    remaining,
                    requested,
                    finalWidths,
                    isPreferred,
                    absoluteMin
            );
        }

        // =========================================================================
        // ÉTAPE 6 : APPLICATION DES CONTRAINTES MIN / MAX
        // =========================================================================
        // Chaque groupe peut définir ses propres contraintes supplémentaires.
        for (int i = 0; i < groupCount; i++) {

            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            // Sécurisation minimale si groupe null
            if (group == null) {
                finalWidths[i] = Math.max(finalWidths[i], absoluteMin);
                continue;
            }

            int minW = group.getMinWidth();
            int maxW = group.getMaxWidth();

            // Si header latéral, il influence aussi les contraintes.
            if (headerAlignment == Ribbon.HEADER_WEST
                    || headerAlignment == Ribbon.HEADER_EAST) {

                if (minW > 0) {
                    minW += headerWidth;
                }
                if (maxW > 0) {
                    maxW += headerWidth;
                }
            }

            // Application stricte des contraintes.
            if (minW > 0) {
                finalWidths[i] = Math.max(finalWidths[i], minW);
            }
            if (maxW > 0) {
                finalWidths[i] = Math.min(finalWidths[i], maxW);
            }

            // Garantie ultime de sécurité.
            finalWidths[i] = Math.max(finalWidths[i], absoluteMin);
        }

        // =========================================================================
        // ÉTAPE 7 : CORRECTION FINALE DU DELTA (ARRONDIS)
        // =========================================================================
        // À cause des divisions entières, il peut rester un écart
        // entre la somme réelle et widthForGroups.
        int used = 0;
        for (int w : finalWidths) {
            used += w;
        }

        int delta = widthForGroups - used;

        if (delta != 0) {

            // Correction appliquée sur le dernier groupe non FIXED
            // pour conserver la priorité hiérarchique.
            for (int i = groupCount - 1; i >= 0; i--) {
                if (!isFixed[i]) {
                    finalWidths[i] += delta;
                    break;
                }
            }
        }

        return finalWidths;
    }

    /**
     * Réduction proportionnelle d'une catégorie (DEFAULT ou PREFERRED) Respecte
     * une largeur minimale absolue.
     */
    private int reduceCategory(int available,
            int[] requested,
            int[] result,
            boolean[] mask,
            int absoluteMin) {

        // =========================================================================
        // ÉTAPE 1 : CALCUL DU TOTAL DES LARGEURS DEMANDÉES POUR CETTE CATÉGORIE
        // =========================================================================
        // On additionne uniquement les largeurs des groupes appartenant
        // à la catégorie ciblée (DEFAULT ou PREFERRED selon le masque).
        // Cela servira de base pour une redistribution proportionnelle.
        int totalCategoryRequest = 0;

        for (int i = 0; i < requested.length; i++) {
            if (mask[i]) {
                totalCategoryRequest += requested[i];
            }
        }

        // =========================================================================
        // ÉTAPE 2 : CAS DÉGÉNÉRÉ
        // =========================================================================
        // Si aucune largeur n’est demandée dans cette catégorie,
        // il n’y a rien à redistribuer.
        // On retourne donc l’espace disponible inchangé.
        if (totalCategoryRequest <= 0) {
            return available;
        }

        // =========================================================================
        // ÉTAPE 3 : REDISTRIBUTION PROPORTIONNELLE
        // =========================================================================
        // Chaque groupe de la catégorie reçoit une part de l’espace disponible
        // proportionnelle à sa demande initiale.
        //
        // Formule :
        //    ratio = requested[i] / totalCategoryRequest
        //    allocated = available * ratio
        //
        // Cela garantit :
        //  - conservation des proportions relatives
        //  - respect de la hiérarchie globale (DEFAULT puis PREFERRED)
        for (int i = 0; i < requested.length; i++) {

            // On ignore les groupes qui ne font pas partie de la catégorie
            if (!mask[i]) {
                continue;
            }

            // Calcul du poids relatif du groupe dans la catégorie
            float ratio = (float) requested[i] / (float) totalCategoryRequest;

            // Allocation proportionnelle de l’espace disponible
            int allocated = (int) (available * ratio);

            // Sécurité : on ne descend jamais sous la largeur minimale absolue
            result[i] = Math.max(allocated, absoluteMin);
        }

        // =========================================================================
        // ÉTAPE 4 : ESPACE CONSOMMÉ
        // =========================================================================
        // Toute la largeur disponible est considérée comme consommée
        // par cette catégorie : Les arrondis peuvent créer
        // un léger écart: A corrigé plus tard.
        //
        // On retourne donc 0 pour indiquer qu'il ne reste plus d'espace
        // à redistribuer aux catégories suivantes.
        return 0;
    }

}
