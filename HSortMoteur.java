package htextarea.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Moteur de tri des paragraphes.
 *
 * Cette classe est purement algorithmique — elle ne touche pas au document ni à
 * l'interface. Elle reçoit une liste de paragraphes avec leurs styles, les
 * critères et options de tri, et retourne la liste réordonnée.
 *
 * La logique brute de comparaison est entièrement déléguée à SortView.
 * HSortMoteur prépare les données (List<String>), appelle SortView et reconstruit
 * la liste de HParagrapheAvecStyle selon la permutation renvoyée.
 *
 * @author FIDELE
 * @version 1.2
 */
public class HSortMoteur {

    // =========================================================================
    // Point d'entrée principal (synchrone)
    // =========================================================================
    /**
     * Trie une liste de paragraphes selon les critères et options donnés.
     *
     * Cette implémentation délègue le tri « brut » à SortView (qui travaille
     * uniquement sur des Strings), puis reconstruit la liste de paragraphes
     * stylés selon la permutation renvoyée.
     *
     * @param paragraphes la liste des paragraphes à trier (avec leurs styles)
     * @param criteres les 1 à 3 critères de tri (les inactifs sont ignorés)
     * @param options les options globales de tri
     * @return une nouvelle liste triée
     */
    public static List<HParagrapheAvecStyle> trier(
            List<HParagrapheAvecStyle> paragraphes,
            List<HSortCritere> criteres,
            HSortOptions options) {

        if (paragraphes == null || paragraphes.isEmpty()) {
            return new ArrayList<>();
        }

        SortView sorter = buildSorter(paragraphes, criteres, options);
        SortView.Result result = sorter.sort();

        return reconstruire(paragraphes, result);
    }

    // =========================================================================
    // Version asynchrone
    // =========================================================================
    /**
     * Version asynchrone du tri : exécute le calcul de tri hors du thread
     * appelant (par ex. hors de l'EDT) et renvoie un CompletableFuture qui
     * contient la liste de paragraphes réordonnée.
     *
     * Important : cette méthode calcule et reconstruit les paragraphes, mais
     * n'applique pas les changements dans le StyledDocument. L'appelant (par
     * exemple HTextArea) doit appliquer la liste retournée sur l'EDT et gérer
     * l'undo si nécessaire.
     *
     * @param paragraphes la liste d'origine (avec style)
     * @param criteres les critères HSortCritere (1..3) - inactifs ignorés
     * @param options options HSortOptions
     * @param executor executor optionnel; si null, ForkJoinPool.commonPool() est utilisé
     * @return CompletableFuture qui contiendra la liste réordonnée
     */
    public static CompletableFuture<List<HParagrapheAvecStyle>> trierAsync(
            List<HParagrapheAvecStyle> paragraphes,
            List<HSortCritere> criteres,
            HSortOptions options,
            Executor executor) {

        if (paragraphes == null || paragraphes.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        SortView sorter = buildSorter(paragraphes, criteres, options);
        Executor exec = executor != null ? executor : ForkJoinPool.commonPool();

        return sorter.sortAsync(exec)
                .thenApply(result -> reconstruire(paragraphes, result));
    }

    // =========================================================================
    // Fabrication du SortView à partir du modèle métier (facteur commun aux
    // deux points d'entrée ci-dessus)
    // =========================================================================
    /**
     * Construit un SortView prêt à trier, à partir des paragraphes stylés et
     * du modèle métier (HSortCritere / HSortOptions). Centralise la
     * traduction vers les types de SortView, auparavant dupliquée entre
     * trier() et trierAsync().
     */
    private static SortView buildSorter(
            List<HParagrapheAvecStyle> paragraphes,
            List<HSortCritere> criteres,
            HSortOptions options) {

        // 1) Construire la liste de chaînes (texte brut) depuis les paragraphes
        List<String> texts = new ArrayList<>(paragraphes.size());
        for (HParagrapheAvecStyle p : paragraphes) {
            texts.add(p == null ? "" : p.getText());
        }

        // 2) Construire les Options pour SortView depuis HSortOptions
        SortView.Options svOptions = new SortView.Options()
                .setRespectCase(options.isRespecterCasse())
                .setLocale(options.getLocale())
                .setHeaderLine(options.isLigneEnTete())
                .setStable(true);

        // Traduire le séparateur
        switch (options.getSeparateur()) {
            case TABULATION -> svOptions.setSeparator(SortView.Options.Separator.TAB);
            case POINT_VIRGULE -> svOptions.setSeparator(SortView.Options.Separator.SEMICOLON);
            case AUTRE -> svOptions.setSeparator(SortView.Options.Separator.OTHER)
                    .setOtherSeparator(options.getSeparateurAutre());
            default -> svOptions.setSeparator(SortView.Options.Separator.NONE);
        }

        // 3) Traduire les critères actifs vers SortView.Criteria
        List<SortView.Criteria> svCriteria = new ArrayList<>();
        if (criteres != null) {
            for (HSortCritere c : criteres) {
                if (c == null || !c.isActif()) {
                    continue;
                }
                SortView.Criteria.Type t = switch (c.getType()) {
                    case NOMBRE -> SortView.Criteria.Type.NUMBER;
                    case DATE -> SortView.Criteria.Type.DATE;
                    default -> SortView.Criteria.Type.TEXT;
                };
                SortView.Criteria.Direction d = (c.getSens() == HSortCritere.Sens.DECROISSANT)
                        ? SortView.Criteria.Direction.DESCENDING
                        : SortView.Criteria.Direction.ASCENDING;
                svCriteria.add(new SortView.Criteria(c.getChamp(), t, d));
            }
        }

        return new SortView(texts, svOptions, svCriteria);
    }

    // =========================================================================
    // Reconstruction de la liste de paragraphes stylés à partir du résultat
    // =========================================================================
    /**
     * Recompose la liste de HParagrapheAvecStyle à partir de la permutation
     * renvoyée par SortView. Factorisé car utilisé par trier() et par le
     * thenApply() de trierAsync().
     */
    private static List<HParagrapheAvecStyle> reconstruire(
            List<HParagrapheAvecStyle> paragraphes,
            SortView.Result result) {

        int[] permutation = result.getPermutation();
        List<HParagrapheAvecStyle> resultat = new ArrayList<>(permutation.length);
        for (int origIndex : permutation) {
            if (origIndex >= 0 && origIndex < paragraphes.size()) {
                resultat.add(paragraphes.get(origIndex));
            }
        }
        return resultat;
    }

    // =========================================================================
    // Utilitaire — déterminer le nombre de colonnes dans un ensemble de
    // paragraphes
    // =========================================================================
    public static int compterColonnesMax(List<HParagrapheAvecStyle> paragraphes,
            HSortOptions options) {
        if (!options.aUnSeparateur() || paragraphes == null) {
            return 0;
        }

        String sep = String.valueOf(options.getCaractereSeparateur());
        int max = 0;

        for (HParagrapheAvecStyle p : paragraphes) {
            if (p.getText() == null) {
                continue;
            }
            int nb = p.getText().split(sep, -1).length;
            if (nb > max) {
                max = nb;
            }
        }
        return max;
    }
}