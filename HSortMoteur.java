package htextarea.sort;

import java.text.Collator;
import java.util.*;
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
 * NOTE: la logique « brute » de comparaison a été extraite vers SortView.
 * HSortMoteur prépare les données (List<String>), appelle SortView et reconstruit
 * la liste de HParagrapheAvecStyle selon la permutation renvoyée.
 *
 * @author FIDELE
 * @version 1.1
 */
public class HSortMoteur {

    /**
     * Formats de date reconnus pour le tri chronologique.
     */
    private static final String[] FORMATS_DATE = {
        "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy",
        "yyyy-MM-dd", "dd/MM/yy", "d MMMM yyyy"
    };

    // =========================================================================
    // Point d'entrée principal (synchrones)
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

        // 1) Construire la liste de chaînes (texte brut) depuis les paragraphes
        List<String> texts = new ArrayList<>(paragraphes.size());
        for (HParagrapheAvecStyle p : paragraphes) {
            texts.add(p == null ? "" : p.getText());
        }

        // 2) Construire les Options pour SortView depuis HSortOptions
        SortView.Options svOptions = new SortView.Options()
                .setRespecterCasse(options.isRespecterCasse())
                .setLocale(options.getLocale())
                .setLigneEnTete(options.isLigneEnTete())
                .setStable(true);

        // Traduire le séparateur
        switch (options.getSeparateur()) {
            case TABULATION -> svOptions.setSeparateur(SortView.Options.Separateur.TABULATION);
            case POINT_VIRGULE -> svOptions.setSeparateur(SortView.Options.Separateur.POINT_VIRGULE);
            case AUTRE -> svOptions.setSeparateur(SortView.Options.Separateur.AUTRE).setSeparateurAutre(options.getSeparateurAutre());
            default -> svOptions.setSeparateur(SortView.Options.Separateur.AUCUN);
        }

        // 3) Traduire les critères actifs vers SortView.Criterion
        List<SortView.Criterion> svCriteria = new ArrayList<>();
        if (criteres != null) {
            for (HSortCritere c : criteres) {
                if (c == null || !c.isActif()) continue;
                SortView.Criterion.Type t = switch (c.getType()) {
                    case NOMBRE -> SortView.Criterion.Type.NOMBRE;
                    case DATE -> SortView.Criterion.Type.DATE;
                    default -> SortView.Criterion.Type.TEXTE;
                };
                SortView.Criterion.Sens s = (c.getSens() == HSortCritere.Sens.DECROISSANT)
                        ? SortView.Criterion.Sens.DECROISSANT
                        : SortView.Criterion.Sens.CROISSANT;
                svCriteria.add(new SortView.Criterion(c.getChamp(), t, s));
            }
        }

        // 4) Déléguer le tri à SortView
        SortView sorter = new SortView(texts, svOptions, svCriteria);
        SortView.Result result = sorter.sort();

        // 5) Recomposer la liste de paragraphes stylés à partir de la permutation
        List<HParagrapheAvecStyle> resultat = new ArrayList<>(result.permutation.length);
        for (int i = 0; i < result.permutation.length; i++) {
            int origIndex = result.permutation[i];
            if (origIndex >= 0 && origIndex < paragraphes.size()) {
                resultat.add(paragraphes.get(origIndex));
            }
        }

        return resultat;
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

        // Construire la liste de textes
        List<String> texts = new ArrayList<>(paragraphes.size());
        for (HParagrapheAvecStyle p : paragraphes) {
            texts.add(p == null ? "" : p.getText());
        }

        // Construire options SortView
        SortView.Options svOptions = new SortView.Options()
                .setRespecterCasse(options.isRespecterCasse())
                .setLocale(options.getLocale())
                .setLigneEnTete(options.isLigneEnTete())
                .setStable(true);

        switch (options.getSeparateur()) {
            case TABULATION -> svOptions.setSeparateur(SortView.Options.Separateur.TABULATION);
            case POINT_VIRGULE -> svOptions.setSeparateur(SortView.Options.Separateur.POINT_VIRGULE);
            case AUTRE -> svOptions.setSeparateur(SortView.Options.Separateur.AUTRE).setSeparateurAutre(options.getSeparateurAutre());
            default -> svOptions.setSeparateur(SortView.Options.Separateur.AUCUN);
        }

        // Construire critères
        List<SortView.Criterion> svCriteria = new ArrayList<>();
        if (criteres != null) {
            for (HSortCritere c : criteres) {
                if (c == null || !c.isActif()) continue;
                SortView.Criterion.Type t = switch (c.getType()) {
                    case NOMBRE -> SortView.Criterion.Type.NOMBRE;
                    case DATE -> SortView.Criterion.Type.DATE;
                    default -> SortView.Criterion.Type.TEXTE;
                };
                SortView.Criterion.Sens s = (c.getSens() == HSortCritere.Sens.DECROISSANT)
                        ? SortView.Criterion.Sens.DECROISSANT
                        : SortView.Criterion.Sens.CROISSANT;
                svCriteria.add(new SortView.Criterion(c.getChamp(), t, s));
            }
        }

        Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
        SortView sorter = new SortView(texts, svOptions, svCriteria);

        // Lancer le tri asynchrone et reconstruire la liste à la complétion
        return sorter.sortAsync(exec)
                .thenApply(result -> {
                    List<HParagrapheAvecStyle> reordered = new ArrayList<>(result.permutation.length);
                    for (int i = 0; i < result.permutation.length; i++) {
                        int origIndex = result.permutation[i];
                        if (origIndex >= 0 && origIndex < paragraphes.size()) {
                            reordered.add(paragraphes.get(origIndex));
                        }
                    }
                    return reordered;
                });
    }

    // =========================================================================
    // Les méthodes suivantes (comparateurs, extraction, parsing) sont conservées
    // pour compatibilité et référence ; la logique de tri principale est faite
    // par SortView.
    // =========================================================================

    private static Comparator<HParagrapheAvecStyle> construireComparateur(
            List<HSortCritere> criteres, HSortOptions options) {

        Comparator<HParagrapheAvecStyle> comp = null;

        for (HSortCritere critere : criteres) {
            if (!critere.isActif()) {
                continue;
            }

            Comparator<HParagrapheAvecStyle> niveau
                    = comparateurPourCritere(critere, options);

            comp = (comp == null)
                    ? niveau
                    : comp.thenComparing(niveau);
        }

        // Si aucun critère actif, ordre naturel du texte
        return (comp != null) ? comp : Comparator.comparing(HParagrapheAvecStyle::getText);
    }

    private static Comparator<HParagrapheAvecStyle> comparateurPourCritere(
            HSortCritere critere, HSortOptions options) {

        Collator collator = Collator.getInstance(options.getLocale());
        collator.setStrength(options.isRespecterCasse()
                ? Collator.TERTIARY
                : Collator.SECONDARY
        );

        Comparator<HParagrapheAvecStyle> comp;

        switch (critere.getType()) {

            case NOMBRE -> {
                comp = Comparator.comparingDouble(p -> {
                    String val = extraireValeur(p.getText(), critere.getChamp(), options);
                    return parseNombre(val);
                });
            }

            case DATE -> {
                comp = Comparator.comparingLong(p -> {
                    String val = extraireValeur(p.getText(), critere.getChamp(), options);
                    return parseDate(val);
                });
            }

            default -> {
                comp = (a, b) -> {
                    String va  = extraireValeur(a.getText(), critere.getChamp(), options);
                    String vb = extraireValeur(b.getText(), critere.getChamp(), options);
                    return collator.compare(va, vb);
                };
            }
        }

        if (critere.getSens() == HSortCritere.Sens.DECROISSANT) {
            comp = comp.reversed();
        }

        return comp;
    }

    private static String extraireValeur(String texte, String champ,
            HSortOptions options) {
        if (texte == null) {
            return "";
        }

        if (!options.aUnSeparateur() || champ == null
                || champ.equalsIgnoreCase("Paragraphes")) {
            return texte.trim();
        }

        int numColonne = extraireNumeroColonne(champ);
        if (numColonne < 1) {
            return texte.trim();
        }

        String sep = String.valueOf(options.getCaractereSeparateur());
        String[] parts = texte.split(sep, -1);

        int index = numColonne - 1; // 1-based → 0-based
        return (index < parts.length) ? parts[index].trim() : "";
    }

    private static int extraireNumeroColonne(String champ) {
        if (champ == null) {
            return -1;
        }
        try {
            String[] parts = champ.trim().split("\\s+");
            return (parts.length >= 2) ? Integer.parseInt(parts[1]) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double parseNombre(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Double.MAX_VALUE;
        }

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("-?\\d+([.,]\\d+)?")
                .matcher(valeur);

        if (m.find()) {
            try {
                return Double.parseDouble(m.group().replace(',', '.'));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        }
        return Double.MAX_VALUE;
    }

    private static long parseDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Long.MAX_VALUE;
        }

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}"
                        + "|\\d{4}[/\\-]\\d{1,2}[/\\-]\\d{1,2}")
                .matcher(valeur);

        while (m.find()) {
            String candidat = m.group();
            for (String format : FORMATS_DATE) {
                try {
                    java.text.SimpleDateFormat sdf
                            = new java.text.SimpleDateFormat(format,
                                    java.util.Locale.getDefault());
                    sdf.setLenient(false);
                    return sdf.parse(candidat).getTime();
                } catch (java.text.ParseException ignored) {
                }
            }
        }

        return Long.MAX_VALUE;
    }

    // =======================================================================
    // Utilitaire — déterminer le nombre de colonnes dans un ensemble de
    // paragraphes
    // =======================================================================
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