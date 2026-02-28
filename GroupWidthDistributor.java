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

    // =========================================================================
    // IMPLÉMENTATION DE L'INTERFACE IGroupWidthDistributor
    // =========================================================================
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
     * @return Tableau d'entiers de taille groupModel.getGroupCount() Chaque
     * élément représente la largeur totale allouée au groupe correspondant
     * (inclut l'espace pour l'en-tête si alignement WEST/EAST)
     *
     * Aucune exception levée, retourne un tableau vide pour les cas invalides
     */
    
    @Override
public int[] distributeWidths(HRibbonLayoutContext ctx,
                              Ribbon ribbon,
                              int availableWidth) {

    // =========================================================================
    // ÉTAPE 1 : VALIDATION DES PARAMÈTRES
    // =========================================================================

    if (ribbon == null) {
        return new int[0];
    }

    HRibbonGroupModel groupModel = ribbon.getGroupModel();
    HRibbonModel model = ribbon.getModel();

    if (groupModel == null) {
        return new int[0];
    }

    final int groupCount = groupModel.getGroupCount();

    if (groupCount <= 0) {
        return new int[0];
    }

    int[] finalWidths = new int[groupCount]; // Largeur finale après distribution

    // =========================================================================
    // ÉTAPE 2 : EXTRACTION DU CONTEXTE
    // =========================================================================

    int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
    int headerWidth     = (ctx != null) ? ctx.getHeaderWidth() : 0;
    int groupMargin     = (ctx != null) ? ctx.getGroupMargin() : 0;
    int defaultWidth    = (ctx != null) ? ctx.getDefautlGroupWidth() : 150;
    int absoluteMin     = (ctx != null) ? ctx.getAbsoluteGroupMin() : 1;

    int totalMargins   = groupMargin * Math.max(0, groupCount - 1);
    int widthForGroups = Math.max(availableWidth - totalMargins, 0);

    // =========================================================================
    // ÉTAPE 3 : CALCUL DES LARGEURS DEMANDÉES + MINIMUM DYNAMIQUE DE CHAQUE GROUPE
    // =========================================================================

    int[] requested = new int[groupCount];
    int[] effectiveMin = new int[groupCount];

    boolean[] isFixed     = new boolean[groupCount];
    boolean[] isPreferred = new boolean[groupCount];
    boolean[] isDefault   = new boolean[groupCount];

    int totalRequested = 0;

    LineWrapper lineWrapper = new LineWrapper();

    for (int i = 0; i < groupCount; i++) {

        HRibbonGroup group = groupModel.getHRibbonGroup(i);

        int req;

        // ---------------------------
        // Détermination largeur demandée
        // ---------------------------

        if (group != null) {

            if (group.getWidth() != -1) {
                req = group.getWidth();
                isFixed[i] = true;

            } else if (group.getPreferredWidth() != -1) {
                req = group.getPreferredWidth();
                isPreferred[i] = true;

            } else {
                req = defaultWidth;
                isDefault[i] = true;
            }

        } else {
            throw new IllegalArgumentException("Group cannot be null");
        }

        // Ajout header latéral si nécessaire
        if (headerAlignment == Ribbon.HEADER_WEST
                || headerAlignment == Ribbon.HEADER_EAST) {
            req += headerWidth;
        }

        // ---------------------------
        // Calcul dynamique du minWidth du groupe
        // ---------------------------

        int dynamicMin = 0;

        if (group != null) {
            
            //On récupère tous ses composants
            List<Object> values =
                    model.getComponentsAt(groupModel.getGroupIndex(group.getGroupIdentifier()));

            List<Component> comps = new ArrayList<>();

            for (Object value : values) {
                comps.add(ribbon.getComponentForValue(
                        value,
                        groupModel.getGroupIndex(group.getGroupIdentifier()),
                        i));
            }

            dynamicMin = lineWrapper.computeDynamicMinWidth( // <- Méthode pour calculer la minWidth dynamique
                    comps,
                    group.getHeigth(),              // hauteur FIXE
                    group.getComponentMargin(),     // spacing horizontal
                    group.getComponentMargin(),     // spacing vertical
                    group.getPadding()              // padding interne
            );
            
            group.setMinWidth(dynamicMin);
            
//            System.out.println("==== Groupe " + i + " === " + dynamicMin);
            
        }

        // Minimum effectif = max(min défini, dynamicMin, absoluteMin)
        effectiveMin[i] = dynamicMin;

        // La largeur demandée ne peut jamais être < minimum effectif
        requested[i] = Math.max(req, effectiveMin[i]);

        totalRequested += requested[i];
    }

    // =========================================================================
    // ÉTAPE 4 : CAS ESPACE SUFFISANT
    // =========================================================================

    if (totalRequested <= widthForGroups) {

        System.arraycopy(requested, 0, finalWidths, 0, groupCount);

        int remaining = widthForGroups - totalRequested;

        int flexibleCount = 0;
        for (int i = 0; i < groupCount; i++) {
            if (!isFixed[i]) flexibleCount++;
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
    }

    // =========================================================================
    // ÉTAPE 5 : ESPACE INSSUFFISANT CAS RÉDUCTION
    // =========================================================================
    else {

        int fixedSpace = 0;

        for (int i = 0; i < groupCount; i++) {
            if (isFixed[i]) {
                finalWidths[i] = requested[i];
                fixedSpace += requested[i];
            }
        }

        int remaining = Math.max(widthForGroups - fixedSpace, 0);

        // Distribution proportionnelle DEFAULT puis PREFERRED
        remaining = reduceCategory(remaining, requested, finalWidths, isDefault, effectiveMin);
        remaining = reduceCategory(remaining, requested, finalWidths, isPreferred, effectiveMin);
    }

    // =========================================================================
    // ÉTAPE 6 : CONTRAINTES MIN/MAX STRICTES
    // =========================================================================

    for (int i = 0; i < groupCount; i++) {

        HRibbonGroup group = groupModel.getHRibbonGroup(i);

        int minW = effectiveMin[i];
        int maxW = Integer.MAX_VALUE;

        if (group != null && group.getMaxWidth() > 0) {
            maxW = group.getMaxWidth();
        }

        finalWidths[i] = Math.max(finalWidths[i], minW);
        finalWidths[i] = Math.min(finalWidths[i], maxW);
    }

    // =========================================================================
    // ÉTAPE 7 : GESTION DES ARRONDIS RESTANTS
    // =========================================================================

    int used = 0;
    for (int w : finalWidths) used += w;

    int delta = widthForGroups - used;

    while (delta != 0) {

        boolean progress = false;

        for (int i = 0; i < groupCount && delta != 0; i++) {

            if (isFixed[i]) continue;

            if (delta > 0) {
                finalWidths[i]++;
                delta--;
                progress = true;
            } else {
                if (finalWidths[i] > effectiveMin[i]) {
                    finalWidths[i]--;
                    delta++;
                    progress = true;
                }
            }
        }

        if (!progress) break;
    }

    return finalWidths;
}

    /**
 * Réduction proportionnelle d'une catégorie de groupes en respectant
 * un minimum dynamique individuel pour chaque groupe.
 *
 * CONTRAT :
 * - Chaque groupe i possède son minimum incompressible effectiveMin[i].
 * - Aucun résultat ne pourra être inférieur à effectiveMin[i].
 * - La somme finale distribuée ne dépassera jamais "available".
 * - La méthode retourne le reste réel non consommé.
 *
 * @param available     largeur totale disponible pour cette catégorie
 * @param requested     largeurs demandées par chaque groupe
 * @param result        tableau résultat à remplir (largeurs finales)
 * @param category      masque indiquant les groupes concernés
 * @param effectiveMin  minimum dynamique individuel par groupe
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
