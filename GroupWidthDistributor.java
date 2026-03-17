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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.HRibbonModel;
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

    /**
     * Distribue les largeurs disponibles entre tous les groupes du ruban
     *
     * ALGORITHME GÉNÉRAL : 1.Vérifier les paramètres d'entrée (null, groupes
 vides) 2. Calculer l'espace disponible après soustraction des marges 3.
 Choisir le mode de distribution (égal vs intelligent) 4. Appliquer
 l'algorithme de distribution correspondant 5. Appliquer les contraintes
 min/max de chaque groupe 6. Garantir des largeurs minimales raisonnables
     *
     * @param ctx Contexte de layout contenant : - Alignement des en-têtes
     * (headerAlignment) - Largeur des en-têtes (headerWidth) - Marge entre
     * groupes (groupMargin) - Préférence de distribution égale
     * (isEqualDistribution)
     * @param availableWidth Largeur totale disponible dans le conteneur parent
     * (déjà soustraite des marges gauche/droite du parent)
     * @param useEntireSpace
     * @return Tableau d'entiers de taille groupModel.getGroupCount() Chaque
     * élément représente la largeur totale allouée au groupe correspondant
     * (inclut l'espace pour l'en-tête si alignement WEST/EAST)
     *
     * Aucune exception levée, retourne un tableau vide pour les cas invalides
     */
    @Override
    public int[] distributeWidths(HRibbonLayoutContext ctx,
            Ribbon ribbon,
            int availableWidth, boolean useEntireSpace) {

        // =====================================================================
        // ÉTAPE 1 : VALIDATION DES PARAMÈTRES
        // =====================================================================
        // Objectif : Vérifier que tout ce dont on a besoin existe
        // Si un élément critique est manquant, on retourne un tableau vide
        // plutôt que de planter (robustesse)
        if (ribbon == null) {
            return new int[0];  // Pas de ruban = pas de distribution possible
        }

        // Récupération du modèle de groupes (contient la liste des groupes)
        HRibbonGroupModel groupModel = ribbon.getGroupModel();
        // Récupération du modèle de données (contient les composants)
        HRibbonModel model = ribbon.getModel();

        final int groupCount = groupModel.getGroupCount();

        if (groupModel == null) {
            return new int[0];  // Pas de modèle de groupes = pas de distribution
        }
        if (groupCount <= 0) {
            return new int[0];  // Aucun groupe à distribuer
        }
        // On calcul la largeur minimale nécessaire pour afficher tous les 
        //composants d'un groupe sans débordement
        LineWrapper lineWrapper = new LineWrapper();

        int[] dynamicMin = new int[groupCount];

        for (int i = 0; i < groupCount; i++) {

            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            if (group != null) {
                // On récupère tous les composants de ce groupe depuis le modèle
                List<Object> values
                        = model.getComponentsAt(groupModel.getGroupIndex(group.getGroupIdentifier()));

                List<Component> comps = new ArrayList<>();

                // Pour chaque valeur, on obtient le composant Swing correspondant
                for (Object value : values) {
                    comps.add(ribbon.getComponentForValue(
                            value,
                            groupModel.getGroupIndex(group.getGroupIdentifier()),
                            i));
                }

                int groupCurrentWidth = group.getWidth() > 0
                        ? group.getWidth()
                        : group.getPreferredWidth();

                dynamicMin[i] = lineWrapper.computeDynamicMinWidth(
                        comps,
                        groupCurrentWidth, // largeur actuelle du groupe pour simuler le wrapping
                        group.getComponentMargin(),
                        group.getPadding()
                );
                
//                 System.out.println("Groupe " + i 
//    + " | getWidth()=" + group.getWidth()
//    + " | getPreferredWidth()=" + group.getPreferredWidth()
//    + " | groupCurrentWidth=" + groupCurrentWidth
//    + " | dynamicMin=" + dynamicMin);
                
            }

        }

        // Tableau qui contiendra les largeurs finales de chaque groupe
        int[] finalWidths = new int[groupCount];

        // =========================================================================
        // ÉTAPE 2 : EXTRACTION DU CONTEXTE
        // =========================================================================
        // Objectif : Récupérer tous les paramètres de configuration
        // On utilise l'opérateur ternaire pour fournir des valeurs par défaut
        // si le contexte est null (robustesse)
        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;
        int groupMargin = (ctx != null) ? ctx.getGroupMargin() : 0;
        int defaultWidth = (ctx != null) ? ctx.getDefautlGroupWidth() : 150;
        int absoluteMin = (ctx != null) ? ctx.getAbsoluteGroupMin() : 1;

        // Calcul de l'espace réellement disponible pour les groupes
        // totalMargins = marge * (nombre d'espaces entre groupes)
        // S'il y a N groupes, il y a (N-1) espaces entre eux
        int totalMargins = groupMargin * Math.max(0, groupCount - 1);
        // widthForGroups = espace total - espace des marges (jamais négatif)
        int widthForGroups = Math.max(availableWidth - totalMargins, 0);

        // =========================================================================
        // ÉTAPE 3 : CALCUL DES LARGEURS DEMANDÉES + MINIMUM DYNAMIQUE DE CHAQUE GROUPE
        // =========================================================================
        // Objectif : Déterminer ce que chaque groupe "voudrait" comme largeur
        // et calculer son minimum absolu basé sur son contenu
        // requested[i] = largeur idéale que le groupe i voudrait avoir
        int[] requested = new int[groupCount];
        // effectiveMin[i] = largeur minimale que le groupe i peut accepter
        int[] effectiveMin = new int[groupCount];

        // Trackers pour savoir comment chaque groupe a obtenu sa largeur
        // Cela influencera la façon dont on réduit/agrandit plus tard
        boolean[] isFixed = new boolean[groupCount];  // Largeur FIXE (explicite)
        boolean[] isPreferred = new boolean[groupCount];  // Largeur PRÉFÉRÉE
        boolean[] isDefault = new boolean[groupCount];  // Largeur PAR DÉFAUT

        int totalRequested = 0;  // Somme de toutes les largeurs demandées

        // On parcourt tous les groupes
        for (int i = 0; i < groupCount; i++) {

            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            int req;  // Largeur demandée pour ce groupe

            // ---------------------------
            // Détermination largeur demandée (ordre de priorité)
            // ---------------------------
            // Règle : 
            // 1. Si une largeur FIXE est définie (group.getWidth()), on l'utilise
            // 2. Sinon, si une largeur PRÉFÉRÉE est définie, on l'utilise
            // 3. Sinon, on utilise la largeur PAR DÉFAUT du contexte
            if (group != null) {

                // Priorité 1 : Largeur fixe explicite
                if (group.getWidth() != -1) {
                    req = group.getWidth();
                    isFixed[i] = true;  // On marque ce groupe comme "fixe"
                } else if (!useEntireSpace) {
                    req = dynamicMin[i];
                    isPreferred[i] = true;
                } else if (group.getPreferredWidth() != -1) { // Priorité 2 : Largeur préférée
                    req = group.getPreferredWidth();
                    isPreferred[i] = true;  // On marque ce groupe comme "préféré"
                } // Priorité 3 : Largeur par défaut
                else {
                    req = defaultWidth;
                    isDefault[i] = true;  // On marque ce groupe comme "défaut"
                }

            } else {
                // Normalement on ne devrait jamais arriver ici
                throw new IllegalArgumentException("Group cannot be null");
            }

            // Si les en-têtes sont à gauche ou à droite, ils prennent de la place
            // Il faut donc ajouter leur largeur à la largeur totale du groupe
            if (headerAlignment == Ribbon.HEADER_WEST
                    || headerAlignment == Ribbon.HEADER_EAST) {
                req += headerWidth;
            }

            // ---------------------------
            // Calcul dynamique du minWidth du groupe
            // ---------------------------
            // C'est le cœur de l'intelligence : on calcule la largeur MINIMALE
            // nécessaire pour que tous les composants du groupe soient visibles
            // correctement, en tenant compte de leur disposition
            if (group != null) {
                // On sauvegarde ce minimum dynamique dans le groupe
                group.setMinWidth(dynamicMin[i]);

            }
            
            effectiveMin[i] = dynamicMin[i];
            // La largeur demandée ne peut jamais être inférieure au minimum effectif
            // C'est logique : on ne peut pas demander moins que le minimum viable
            requested[i] = Math.max(req, effectiveMin[i]);

            totalRequested += requested[i];  // On accumule pour la somme totale
        }

        // =========================================================================
        // ÉTAPE 4 : CAS ESPACE SUFFISANT (totalRequested <= widthForGroups)
        // =========================================================================
        // Objectif : Si on a assez de place pour donner à chaque groupe ce qu'il demande,
        // on le fait, puis on distribue l'espace restant équitablement
        if (totalRequested <= widthForGroups) {

            if (useEntireSpace) {
                // Redistribuer tout l'espace disponible entre les groupes flexibles
                System.arraycopy(requested, 0, finalWidths, 0, groupCount);
                int remaining = widthForGroups - totalRequested;
                int flexibleCount = 0;
                for (int i = 0; i < groupCount; i++) {
                    if (!isFixed[i]) {
                        flexibleCount++;
                    }
                }
                if (flexibleCount > 0) {
                    int extraPerGroup = remaining / flexibleCount;
                    int remainder = remaining % flexibleCount;
                    for (int i = 0; i < groupCount; i++) {
                        if (!isFixed[i]) {
                            finalWidths[i] += extraPerGroup;
                            if (remainder > 0) {
                                finalWidths[i]++;
                                remainder--;
                            }
                        }
                    }
                }

            } else {
                // Mode compact : chaque groupe reçoit exactement dynamicMin
                // Les groupes fixes gardent leur largeur explicite
                // Aucune redistribution de l'espace restant
                for (int i = 0; i < groupCount; i++) {
                    if (isFixed[i]) {
                        finalWidths[i] = requested[i];
                    } else {
                        finalWidths[i] = dynamicMin[i];
                    }
                }
                // Retour immédiat : on saute les étapes 5, 6 et 7
                // qui redistribueraient ou ajusteraient les largeurs
                return finalWidths;
            }

        } // =========================================================================
        // ÉTAPE 5 : CAS ESPACE INSUFFISANT (totalRequested > widthForGroups)
        // =========================================================================
        // Objectif : On n'a pas assez de place, il faut réduire certains groupes
        // Stratégie : 
        // 1. Les groupes FIXES gardent leur taille (on ne peut pas les réduire)
        // 2. On réduit d'abord les groupes "DEFAULT"
        // 3. Puis on réduit les groupes "PREFERRED"
        // (Les groupes FIXES ne sont jamais réduits)
        else {

            // Étape 5.1 : Calcul de l'espace pris par les groupes FIXES
            int fixedSpace = 0;

            for (int i = 0; i < groupCount; i++) {
                if (isFixed[i]) {
                    finalWidths[i] = requested[i];  // Ils gardent leur taille
                    fixedSpace += requested[i];
                }
            }

            // Espace restant pour les groupes non-fixes
            int remaining = Math.max(widthForGroups - fixedSpace, 0);

            // Étape 5.2 : Réduction des groupes DEFAULT d'abord (priorité basse)
            remaining = reduceCategory(remaining, requested, finalWidths, isDefault, effectiveMin);

            // Étape 5.3 : Réduction des groupes PREFERRED ensuite (priorité moyenne)
            remaining = reduceCategory(remaining, requested, finalWidths, isPreferred, effectiveMin);

            // Note : Les groupes FIXES ont déjà été traités et ne sont pas réduits
            // Si après ces réductions il reste encore de l'espace à répartir,
            // ce serait aux groupes "inconnus" (ni DEFAULT ni PREFERRED) mais normalement
            // tout groupe est dans l'une des trois catégories
        }

        // =========================================================================
        // ÉTAPE 6 : CONTRAINTES MIN/MAX STRICTES
        // =========================================================================
        // Objectif : S'assurer qu'aucun groupe ne dépasse ses limites
        // Après toutes les distributions/réductions, on vérifie que chaque groupe
        // respecte ses bornes (minWidth et maxWidth)
        for (int i = 0; i < groupCount; i++) {

            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            int minW = effectiveMin[i];  // Minimum calculé à l'étape 3
            int maxW = Integer.MAX_VALUE;  // Par défaut, pas de maximum

            // Si un maxWidth est défini, on l'utilise
            if (group != null && group.getMaxWidth() > 0) {
                maxW = group.getMaxWidth();
            }

            // On applique les bornes : jamais en dessous du min, jamais au-dessus du max
            finalWidths[i] = Math.max(finalWidths[i], minW);
            finalWidths[i] = Math.min(finalWidths[i], maxW);
        }

        // =========================================================================
        // ÉTAPE 7 : GESTION DES ARRONDIS RESTANTS
        // =========================================================================
        // Objectif : Corriger les erreurs d'arrondi dues aux divisions entières
        // Après toutes ces opérations, la somme des largeurs peut ne pas correspondre
        // exactement à widthForGroups (à cause des divisions et des contraintes)
        // On ajuste au fur et à mesure pour atteindre la largeur exacte
        int used = 0;
        for (int w : finalWidths) {
            used += w;  // On recalcule la somme utilisée
        }
        int delta = widthForGroups - used;  // Écart entre ce qu'on a et ce qu'on devrait avoir

        // Tant qu'il y a un écart
        while (delta != 0) {

            boolean progress = false;  // Pour détecter les blocages

            // On parcourt tous les groupes
            for (int i = 0; i < groupCount && delta != 0; i++) {

                // On ignore les groupes fixes (on ne modifie pas leur taille)
                if (isFixed[i]) {
                    continue;
                }

                if (delta > 0) {
                    // Il manque de l'espace : on ajoute 1 pixel
                    finalWidths[i]++;
                    delta--;
                    progress = true;
                } else {
                    // Il y a trop d'espace : on retire 1 pixel si possible
                    if (finalWidths[i] > effectiveMin[i]) {
                        finalWidths[i]--;
                        delta++;
                        progress = true;
                    }
                }
            }

            // Si on a fait un tour complet sans pouvoir ajuster, on arrête
            // (pour éviter une boucle infinie)
            if (!progress) {
                break;
            }
        }

        return finalWidths;  // On retourne les largeurs finales calculées
    }

    /**
     * Réduction proportionnelle d'une catégorie de groupes en respectant un
     * minimum dynamique individuel pour chaque groupe.
     *
     * CONTRAT : - Chaque groupe i possède son minimum incompressible
     * effectiveMin[i]. - Aucun résultat ne pourra être inférieur à
     * effectiveMin[i]. - La somme finale distribuée ne dépassera jamais
     * "available". - La méthode retourne le reste réel non consommé.
     *
     * @param available largeur totale disponible pour cette catégorie
     * @param requested largeurs demandées par chaque groupe
     * @param result tableau résultat à remplir (largeurs finales)
     * @param category masque indiquant les groupes concernés
     * @param effectiveMin minimum dynamique individuel par groupe
     *
     * @return largeur restante non consommée
     */
    private int reduceCategory(
            int available,
            int[] requested,
            int[] result,
            boolean[] category,
            int[] effectiveMin) {

        // =========================================================================
        // ÉTAPE 1 : Identifier les groupes concernés et calculer les totaux
        // =========================================================================
        int totalRequested = 0;      // Somme des demandes des groupes concernés
        int totalMinRequired = 0;    // Somme des minimums incompressibles
        int count = 0;               // Nombre de groupes concernés

        for (int i = 0; i < requested.length; i++) {
            if (category[i]) {
                totalRequested += requested[i];
                totalMinRequired += effectiveMin[i];
                count++;
            }
        }

        // Aucun groupe concerné → rien à faire
        if (count == 0) {
            return available;
        }

        // =========================================================================
        // ÉTAPE 2 : Vérification du minimum incompressible global
        // =========================================================================
        // Si la somme des minimums dépasse ou égale l'espace disponible,
        // on distribue uniquement les minimums.
        // Cela garantit la cohérence structurelle du layout.
        if (totalMinRequired >= available) {

            int distributed = 0;

            for (int i = 0; i < requested.length; i++) {
                if (category[i]) {
                    result[i] = effectiveMin[i];
                    distributed += effectiveMin[i];
                }
            }

            // On retourne le reste réel (peut être 0)
            return Math.max(available - distributed, 0);
        }

        // =========================================================================
        // ÉTAPE 3 : Distribution proportionnelle contrôlée
        // =========================================================================
        // On distribue proportionnellement aux demandes,
        // mais jamais sous le minimum individuel.
        int distributed = 0;

        for (int i = 0; i < requested.length; i++) {

            if (!category[i]) {
                continue;
            }

            // Ratio de la demande individuelle sur la demande totale
            float ratio = (float) requested[i] / (float) totalRequested;

            // Allocation proportionnelle
            int allocated = (int) (available * ratio);

            // Respect du minimum dynamique individuel
            allocated = Math.max(allocated, effectiveMin[i]);

            result[i] = allocated;
            distributed += allocated;
        }

        // =========================================================================
        // ÉTAPE 4 : Correction des dépassements dus aux minimums
        // =========================================================================
        // Il est possible que la distribution dépasse "available"
        // à cause des minimums imposés.
        int remainder = available - distributed;

        if (remainder < 0) {

            // On doit réduire certains groupes,
            // mais jamais sous leur minimum individuel.
            for (int i = requested.length - 1; i >= 0 && remainder < 0; i--) {

                if (!category[i]) {
                    continue;
                }

                // On peut réduire seulement si on est au-dessus du minimum
                while (result[i] > effectiveMin[i] && remainder < 0) {
                    result[i]--;
                    remainder++;
                }
            }
        }

        // =========================================================================
        // ÉTAPE 5 : Distribution des pixels restants (si positif)
        // =========================================================================
        // On distribue pixel par pixel pour garantir
        // une somme finale parfaitement exacte.
        if (remainder > 0) {

            for (int i = 0; i < requested.length && remainder > 0; i++) {

                if (!category[i]) {
                    continue;
                }

                result[i]++;
                remainder--;
            }
        }

        // =========================================================================
        // ÉTAPE 6 : Retour du reste réel non consommé
        // =========================================================================
        return remainder;
    }

}
