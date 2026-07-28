package htextarea.sort;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import view.HView;

/**
 * SortView
 * <p>
 * Classe complètement autonome qui réalise le tri « brut » sur une liste de
 * chaînes (List<String>) selon des options et jusqu'à 3 critères. Elle ne
 * touche pas au document, aux styles, ni à l'UI : elle retourne simplement la
 * liste triée et un tableau de permutation indiquant d'où venait chaque ligne.
 * </p>
 * 
 * Utilisation typique : - Construire une instance avec la liste et les
 * options/critères, ou avec un CustomComparator pour un tri entièrement
 * personnalisé. - Appeler sort() pour un résultat synchrone ou
 * sortAsync(executor) pour exécuter le tri hors de l'EDT.
 *
 * Le Result contient : List<String> sortedLines et int[] permutation tel que
 * permutation[newIndex] = oldIndex.
 */
public class SortView extends HView {

    // Formats de date reconnus (mêmes que HSortMoteur)
    private static final String[] FORMATS_DATE = {
        "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy",
        "yyyy-MM-dd", "dd/MM/yy", "d MMMM yyyy"
    };

    // Pattern pour extraire le premier nombre (même logique que HSortMoteur)
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+([.,]\\d+)?");

    // ---------------------------------------------------------------------
    // Classes internes pour Options, Criterion (copie de HSortCritere) et Result
    // ---------------------------------------------------------------------
    /**
     * Options générales de tri (reprend les champs de HSortOptions).
     */
    public static class Options {

        public enum Separateur {
            AUCUN,
            TABULATION,
            POINT_VIRGULE,
            AUTRE
        }

        private Separateur separateur = Separateur.AUCUN;
        private char separateurAutre = ',';
        private boolean respecterCasse = false; // false = ignore case
        private Locale locale = Locale.getDefault();
        private boolean ligneEnTete = false;
        private boolean stable = true; // par défaut nous garantissons la stabilité

        public Options() {
        }

        // Getters / setters fluides
        public Separateur getSeparateur() {
            return separateur;
        }

        public Options setSeparateur(Separateur s) {
            this.separateur = s;
            return this;
        }

        public char getSeparateurAutre() {
            return separateurAutre;
        }

        public Options setSeparateurAutre(char c) {
            this.separateurAutre = c;
            return this;
        }

        public boolean isRespecterCasse() {
            return respecterCasse;
        }

        public Options setRespecterCasse(boolean b) {
            this.respecterCasse = b;
            return this;
        }

        public Locale getLocale() {
            return locale;
        }

        public Options setLocale(Locale locale) {
            this.locale = Objects.requireNonNull(locale);
            return this;
        }

        public boolean isLigneEnTete() {
            return ligneEnTete;
        }

        public Options setLigneEnTete(boolean ligneEnTete) {
            this.ligneEnTete = ligneEnTete;
            return this;
        }

        public boolean isStable() {
            return stable;
        }

        public Options setStable(boolean stable) {
            this.stable = stable;
            return this;
        }

        // Helper : indique si un séparateur actif est défini
        public boolean aUnSeparateur() {
            return separateur != Separateur.AUCUN;
        }

        // Retourne le caractère séparateur effectif
        public char getCaractereSeparateur() {
            return switch (separateur) {
                case TABULATION ->
                    '\t';
                case POINT_VIRGULE ->
                    ';';
                case AUTRE ->
                    separateurAutre;
                default ->
                    '\0';
            };
        }
    }

    /**
     * Criterion est une copie de HSortCritere (simple et autonome). Il décrit
     * un niveau de tri : champ ("Paragraphes" ou "Colonne N"), type et sens.
     */
    public static class Criterion {

        public enum Type {
            TEXTE,
            NOMBRE,
            DATE
        }

        public enum Sens {
            CROISSANT,
            DECROISSANT
        }

        private String champ; // "Paragraphes" ou "Colonne N"
        private Type type = Type.TEXTE;
        private Sens sens = Sens.CROISSANT;
        private boolean actif = false;

        public Criterion() {
            this.champ = null;
            this.type = Type.TEXTE;
            this.sens = Sens.CROISSANT;
            this.actif = false;
        }

        public Criterion(String champ, Type type, Sens sens) {
            this.champ = champ;
            this.type = type == null ? Type.TEXTE : type;
            this.sens = sens == null ? Sens.CROISSANT : sens;
            this.actif = champ != null && !champ.isBlank();
        }

        // Getters / setters
        public String getChamp() {
            return champ;
        }

        public void setChamp(String champ) {
            this.champ = champ;
            this.actif = champ != null && !champ.isBlank();
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Sens getSens() {
            return sens;
        }

        public void setSens(Sens sens) {
            this.sens = sens;
        }

        public boolean isActif() {
            return actif;
        }

        public void setActif(boolean actif) {
            this.actif = actif;
        }

        @Override
        public String toString() {
            return "Criterion{" + "champ='" + champ + '\'' + ", type=" + type + ", sens=" + sens + ", actif=" + actif + '}';
        }
    }

    /**
     * Resultat du tri : la nouvelle liste et la permutation newIndex ->
     * oldIndex.
     */
    public static class Result {

        public final List<String> sortedLines;
        public final int[] permutation; // permutation[newIndex] = oldIndex

        public Result(List<String> sortedLines, int[] permutation) {
            this.sortedLines = Collections.unmodifiableList(new ArrayList<>(sortedLines));
            this.permutation = permutation.clone();
        }

        /**
         * Retourne l'inverse : oldIndex -> newIndex
         */
        public int[] inversePermutation() {
            int[] inv = new int[permutation.length];
            for (int newIdx = 0; newIdx < permutation.length; newIdx++) {
                int oldIdx = permutation[newIdx];
                if (oldIdx >= 0 && oldIdx < inv.length) {
                    inv[oldIdx] = newIdx;
                }
            }
            return inv;
        }
    }

    /**
     * Interface pour le tri personnalisé demandé par l'utilisateur. On a choisi
     * une interface simple (plutôt qu'utiliser java.util.Comparator) pour
     * rester explicite et autonome.
     */
    public interface CustomComparator {

        int compare(String a, String b);
    }

    // ---------------------------------------------------------------------
    // Instances
    // ---------------------------------------------------------------------
    private final List<String> lines; // copie locale
    private final Options options;
    private final List<Criterion> criteres; // peut être null/empty
    private final CustomComparator customComparator; // si non-null, utilisé

    /**
     * Constructeur principal : options + criteres (liste 0..3, les inactifs
     * sont ignorés)
     */
    public SortView(List<String> lines, Options options, List<Criterion> criteres) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.options = options != null ? options : new Options();
        this.criteres = criteres != null ? new ArrayList<>(criteres) : new ArrayList<>();
        this.customComparator = null;
    }

    /**
     * Constructeur pour un tri entièrement personnalisé via CustomComparator.
     * Les options/critères sont ignorés dans ce mode (sauf ligneEnTete qui est
     * néanmoins respectée).
     */
    public SortView(List<String> lines, CustomComparator comparator) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.options = new Options();
        this.criteres = new ArrayList<>();
        this.customComparator = comparator;
    }

    // ---------------------------------------------------------------------
    // API publique
    // ---------------------------------------------------------------------
    /**
     * Tri synchrone. Retourne le Result sans modifier la liste d'origine.
     */
    public Result sort() {
        // Respect de la ligne d'en-tête : on la met de côté
        String header = null;
        List<String> toSort = lines;
        int headerOffset = 0;
        if (options.isLigneEnTete() && toSort.size() > 0) {
            header = toSort.get(0);
            toSort = new ArrayList<>(toSort.subList(1, toSort.size()));
            headerOffset = 1; // indices in original list are shifted by +1
        } else {
            toSort = new ArrayList<>(toSort);
        }

        int n = toSort.size();
        if (n == 0) {
            // nothing to sort — return original with permutation identity
            List<String> resultLines = new ArrayList<>();
            if (header != null) {
                resultLines.add(header);
            }
            resultLines.addAll(toSort);
            int[] perm = new int[resultLines.size()];
            for (int i = 0; i < perm.length; i++) {
                perm[i] = i; // trivial
            }
            return new Result(resultLines, perm);
        }

        // Mode custom comparator : on l'utilise directement
        if (customComparator != null) {
            // Build index list then sort using comparator on the original strings
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                indices.add(i + headerOffset);
            }

            indices.sort((ia, ib) -> {
                String a = lines.get(ia);
                String b = lines.get(ib);
                return customComparator.compare(a, b);
            });

            // Reconstitute sortedLines and permutation
            List<String> sorted = new ArrayList<>();
            if (header != null) {
                sorted.add(header);
            }
            int[] permutation = new int[indices.size() + (header == null ? 0 : 1)];
            int pos = (header == null) ? 0 : 1;
            for (int idx : indices) {
                sorted.add(lines.get(idx));
                permutation[pos++] = idx;
            }
            return new Result(sorted, permutation);
        }

        // Préparer la liste des critères actifs
        List<Criterion> active = new ArrayList<>();
        for (Criterion c : criteres) {
            if (c != null && c.isActif()) {
                active.add(c);
            }
        }

        // Si aucun critère actif, tri naturel sur le texte
        if (active.isEmpty()) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                indices.add(i + headerOffset);
            }
            indices.sort((ia, ib) -> lines.get(ia).compareTo(lines.get(ib)));
            List<String> sorted = new ArrayList<>();
            if (header != null) {
                sorted.add(header);
            }
            int[] permutation = new int[indices.size() + (header == null ? 0 : 1)];
            int pos = (header == null) ? 0 : 1;
            for (int idx : indices) {
                sorted.add(lines.get(idx));
                permutation[pos++] = idx;
            }
            return new Result(sorted, permutation);
        }

        // Pré-calculer les clés pour chaque ligne et chaque critère
        final int criteriaCount = active.size();
        Object[][] keys = new Object[n][criteriaCount];
        Collator[] collators = new Collator[criteriaCount];
        for (int c = 0; c < criteriaCount; c++) {
            Criterion cr = active.get(c);
            if (cr.getType() == Criterion.Type.TEXTE) {
                Collator coll = Collator.getInstance(options.getLocale());
                coll.setStrength(options.isRespecterCasse() ? Collator.TERTIARY : Collator.SECONDARY);
                collators[c] = coll;
            } else {
                collators[c] = null;
            }
        }

        for (int i = 0; i < n; i++) {
            String ligne = toSort.get(i);
            for (int c = 0; c < criteriaCount; c++) {
                Criterion cr = active.get(c);
                String val = extraireValeur(ligne, cr.getChamp(), options);
                switch (cr.getType()) {
                    case NOMBRE ->
                        keys[i][c] = parseNombre(val);
                    case DATE ->
                        keys[i][c] = parseDate(val);
                    default ->
                        keys[i][c] = val; // TEXTE
                }
            }
        }

        // Indices relatifs (0..n-1) que nous trierons
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            indices.add(i);
        }

        // Construire le comparator en utilisant les clés pré-calculées
        Comparator<Integer> indexComparator = (ia, ib) -> {
            for (int c = 0; c < criteriaCount; c++) {
                Criterion cr = active.get(c);
                int res = 0;
                switch (cr.getType()) {
                    case NOMBRE -> {
                        double da = (double) keys[ia][c];
                        double db = (double) keys[ib][c];
                        res = Double.compare(da, db);
                    }
                    case DATE -> {
                        long la = (long) keys[ia][c];
                        long lb = (long) keys[ib][c];
                        res = Long.compare(la, lb);
                    }
                    default -> {
                        String sa = (String) keys[ia][c];
                        String sb = (String) keys[ib][c];
                        Collator coll = collators[c];
                        res = coll.compare(sa, sb);
                    }
                }

                if (res != 0) {
                    // Sens décroissant -> inverser le résultat
                    if (cr.getSens() == Criterion.Sens.DECROISSANT) {
                        return -res;
                    }
                    return res;
                }
            }
            return 0; // tous les critères égaux
        };

        // Si on veut la stabilité explicite et Java sort est stable (TimSort)
        // l'appel à sort() gardera la stabilité. Nous utilisons donc Collections.sort.
        indices.sort(indexComparator);

        // Construire la liste finale et la permutation (indices relatifs -> indices origine)
        List<String> sorted = new ArrayList<>();
        if (header != null) {
            sorted.add(header);
        }
        int[] permutation = new int[indices.size() + (header == null ? 0 : 1)];
        int pos = (header == null) ? 0 : 1;
        for (int relIdx : indices) {
            int origIdx = relIdx + headerOffset; // replacer dans l'index global
            sorted.add(lines.get(origIdx));
            permutation[pos++] = origIdx;
        }

        return new Result(sorted, permutation);
    }

    /**
     * Tri asynchrone : exécute le tri hors du thread courant. Si executor ==
     * null, ForkJoinPool.commonPool() est utilisé.
     */
    public CompletableFuture<Result> sortAsync(Executor executor) {
        Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
        return CompletableFuture.supplyAsync(this::sort, exec);
    }

    // ---------------------------------------------------------------------
    // Fonctions utilitaires (reprises de HSortMoteur)
    // ---------------------------------------------------------------------
    // Extrait la valeur à comparer : soit le texte entier, soit la colonne N
    private static String extraireValeur(String texte, String champ, Options options) {
        if (texte == null) {
            return "";
        }
        if (!options.aUnSeparateur() || champ == null || champ.equalsIgnoreCase("Paragraphes")) {
            return texte.trim();
        }
        int numColonne = extraireNumeroColonne(champ);
        if (numColonne < 1) {
            return texte.trim();
        }
        String sep = String.valueOf(options.getCaractereSeparateur());
        String[] parts = texte.split(sep, -1);
        int index = numColonne - 1;
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

    // Parse nombre : retourne Double.MAX_VALUE si aucun nombre trouvé
    private static double parseNombre(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Double.MAX_VALUE;
        }
        Matcher m = NUMBER_PATTERN.matcher(valeur);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group().replace(',', '.'));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        }
        return Double.MAX_VALUE;
    }

    // Parse date : retourne Long.MAX_VALUE si aucune date reconnue
    private static long parseDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Long.MAX_VALUE;
        }
        // Chercher la première séquence ressemblant à une date
        Matcher m = Pattern.compile("\\d{1,4}([/\\- ]\\d{1,4})+").matcher(valeur);
        if (m.find()) {
            String candidate = m.group();
            for (String fmt : FORMATS_DATE) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                    sdf.setLenient(false);
                    Date d = sdf.parse(candidate);
                    return d.getTime();
                } catch (ParseException ignored) {
                }
            }
        }
        return Long.MAX_VALUE;
    }

    /**
     * Dessine la liste triée.
     *
     * Comportement : - Calcule (s'il n'existe pas encore) le résultat trié en
     * appelant sort(). - Dessine chaque ligne triée en partant de la position
     * (x,y) fournie. - Utilise la police/metrics courante du Graphics passé en
     * paramètre.
     *
     * Remarque importante : - Cette méthode appelle sort() et exécute donc la
     * logique de tri sur le thread courant. Évite d'appeler Paint sur de très
     * grandes listes depuis l'EDT si le tri est lourd ; dans ce cas, préfère
     * pré-calculer le Result hors-EDT et stocker le résultat pour la peinture.
     */
    @Override
    public void Paint(Graphics g, int x, int y) {
        if (g == null) {
            return;
        }

        // Obtenir le résultat trié (synchronement) — sort() ne modifie pas la liste source
        Result result = sort();

        // Obtenir metrics pour calculer l'élévation de chaque ligne
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int ascent = fm.getAscent();

        // Baseline de départ : y correspond au coin supérieur ; on dessine à la baseline
        int baseline = y + ascent;

        // Parcourir les lignes triées et dessiner chacune
        for (String line : result.sortedLines) {
            if (line == null) {
                line = "";
            }
            g.drawString(line, x, baseline);
            baseline += lineHeight;
        }
    }

}
