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

/**
 * SortView
 * <p>
 * Classe complètement autonome qui réalise le tri « brut » sur une liste de
 * chaînes (List<String>) selon des options et jusqu'à 3 critères. Elle ne
 * touche pas au document ni aux styles ; l'affichage (Paint) reste disponible
 * en option pour l'utilisateur qui souhaite un rendu rapide, mais la classe
 * n'hérite plus d'aucun composant graphique : elle peut être utilisée dans un
 * contexte purement logique (tests, export, etc.).
 * </p>
 *
 * Utilisation typique : - Construire une instance avec la liste et les
 * options/critères, ou avec un Comparator<String> pour un tri entièrement
 * personnalisé. - Appeler sort() pour un résultat synchrone ou
 * sortAsync(executor) pour exécuter le tri hors de l'EDT.
 *
 * Le Result expose : getSortedLines() et getPermutation() tel que
 * permutation[newIndex] = oldIndex.
 */
public class SortView {

    // Formats de date reconnus
    private static final String[] DATE_FORMATS = {
        "dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy",
        "yyyy-MM-dd", "dd/MM/yy", "d MMMM yyyy"
    };

    // Pattern pour extraire le premier nombre
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+([.,]\\d+)?");

    // ---------------------------------------------------------------------
    // Classes internes pour Options, Critères et Result
    // ---------------------------------------------------------------------
    /**
     * Options générales de tri.
     */
    public static class Options {

        public enum Separator {
            NONE,
            TAB,
            SEMICOLON,
            OTHER
        }

        private Separator separator = Separator.NONE;
        private char otherSeparator = '-';
        private boolean respectCase = false; // false = ignore case
        private Locale locale = Locale.getDefault();
        private boolean headerLine = false;
        private boolean stable = true; // par défaut nous garantissons la stabilité

        public Options() {
        }

        // Getters / setters fluides
        public Separator getSeparator() {
            return separator;
        }

        public Options setSeparator(Separator s) {
            this.separator = s;
            return this;
        }

        public char getOtherSeparator() {
            return otherSeparator;
        }

        public Options setOtherSeparator(char c) {
            this.otherSeparator = c;
            return this;
        }

        public boolean isRespectCase() {
            return respectCase;
        }

        public Options setRespectCase(boolean b) {
            this.respectCase = b;
            return this;
        }

        public Locale getLocale() {
            return locale;
        }

        public Options setLocale(Locale locale) {
            this.locale = Objects.requireNonNull(locale);
            return this;
        }

        public boolean isHeaderLine() {
            return headerLine;
        }

        public Options setHeaderLine(boolean headerLine) {
            this.headerLine = headerLine;
            return this;
        }

        public boolean isStable() {
            return stable;
        }

        public Options setStable(boolean stable) {
            this.stable = stable;
            return this;
        }

        // Indique si un séparateur actif est défini
        public boolean hasSeparator() {
            return separator != Separator.NONE;
        }

        // Retourne le caractère séparateur effectif
        public char getSeparatorChar() {
            return switch (separator) {
                case TAB ->
                    '\t';
                case SEMICOLON ->
                    ';';
                case OTHER ->
                    otherSeparator;
                default ->
                    '\0';
            };
        }
    }

    /**
     * Criteria décrit un niveau de tri : champ ("Paragraphs" ou "Column N"),
     * type et sens.
     */
    public static class Criteria {

        public enum Type {
            TEXT,
            NUMBER,
            DATE
        }

        public enum Direction {
            ASCENDING,
            DESCENDING
        }

        private String field; // "Paragraphs" ou "Column N"
        private Type type = Type.TEXT;
        private Direction direction = Direction.ASCENDING;
        private boolean active = false;

        public Criteria() {
            this.field = null;
            this.type = Type.TEXT;
            this.direction = Direction.ASCENDING;
            this.active = false;
        }

        public Criteria(String field, Type type, Direction direction) {
            this.field = field;
            this.type = type == null ? Type.TEXT : type;
            this.direction = direction == null ? Direction.ASCENDING : direction;
            this.active = field != null && !field.isBlank();
        }

        // Getters / setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
            this.active = field != null && !field.isBlank();
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Direction getDirection() {
            return direction;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public String toString() {
            return "Criteria{" + "field='" + field + '\'' + ", type=" + type + ", direction=" + direction + ", active=" + active + '}';
        }
    }

    /**
     * Résultat du tri : la nouvelle liste et la permutation newIndex ->
     * oldIndex. Les deux informations sont accessibles séparément via
     * getSortedLines() et getPermutation().
     */
    public static class Result {

        private final List<String> sortedLines;
        private final int[] permutation; // permutation[newIndex] = oldIndex

        public Result(List<String> sortedLines, int[] permutation) {
            this.sortedLines = Collections.unmodifiableList(new ArrayList<>(sortedLines));
            this.permutation = permutation.clone();
        }

        /**
         * Retourne la liste triée (lecture seule).
         */
        public List<String> getSortedLines() {
            return sortedLines;
        }

        /**
         * Retourne la permutation newIndex -> oldIndex (copie défensive).
         */
        public int[] getPermutation() {
            return permutation.clone();
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

    // ---------------------------------------------------------------------
    // Instances
    // ---------------------------------------------------------------------
    private final List<String> lines; // copie locale
    private final Options options;
    private final List<Criteria> criteria; // peut être null/empty
    private final Comparator<String> customComparator; // si non-null, utilisé

    /**
     * Constructeur principal : options + critères (liste 0..3, les inactifs
     * sont ignorés)
     */
    public SortView(List<String> lines, Options options, List<Criteria> criteria) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.options = options != null ? options : new Options();
        this.criteria = criteria != null ? new ArrayList<>(criteria) : new ArrayList<>();
        this.customComparator = null;
    }

    /**
     * Constructeur pour un tri entièrement personnalisé via Comparator<String>.
     * Les options/critères sont ignorés dans ce mode (sauf headerLine qui est
     * néanmoins respectée).
     */
    public SortView(List<String> lines, Comparator<String> comparator) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        this.options = new Options();
        this.criteria = new ArrayList<>();
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
        List<String> toSort;
        int headerOffset = 0;
        if (options.isHeaderLine() && !lines.isEmpty()) {
            header = lines.get(0);
            toSort = new ArrayList<>(lines.subList(1, lines.size()));
            headerOffset = 1; // les indices de la liste d'origine sont décalés de +1
        } else {
            toSort = new ArrayList<>(lines);
        }

        int n = toSort.size();
        if (n == 0) {
            // rien à trier — indices vides, buildResult gère le cas de l'en-tête seul
            return buildResult(header, new ArrayList<>());
        }

        // Mode custom comparator : on l'utilise directement
        if (customComparator != null) {
            List<Integer> indices = absoluteIndices(n, headerOffset);
            indices.sort((ia, ib) -> customComparator.compare(lines.get(ia), lines.get(ib)));
            return buildResult(header, indices);
        }

        // Préparer la liste des critères actifs
        List<Criteria> active = new ArrayList<>();
        for (Criteria c : criteria) {
            if (c != null && c.isActive()) {
                active.add(c);
            }
        }

        // Si aucun critère actif, tri naturel sur le texte
        if (active.isEmpty()) {
            List<Integer> indices = absoluteIndices(n, headerOffset);
            indices.sort((ia, ib) -> lines.get(ia).compareTo(lines.get(ib)));
            return buildResult(header, indices);
        }

        // Pré-calculer les clés pour chaque ligne et chaque critère
        final int criteriaCount = active.size();
        Object[][] keys = new Object[n][criteriaCount];
        Collator[] collators = new Collator[criteriaCount];
        for (int c = 0; c < criteriaCount; c++) {
            Criteria cr = active.get(c);
            if (cr.getType() == Criteria.Type.TEXT) {
                Collator coll = Collator.getInstance(options.getLocale());
                coll.setStrength(options.isRespectCase() ? Collator.TERTIARY : Collator.SECONDARY);
                collators[c] = coll;
            } else {
                collators[c] = null;
            }
        }

        for (int i = 0; i < n; i++) {
            String line = toSort.get(i);
            for (int c = 0; c < criteriaCount; c++) {
                Criteria cr = active.get(c);
                String val = extractValue(line, cr.getField(), options);
                switch (cr.getType()) {
                    case NUMBER ->
                        keys[i][c] = parseNumber(val);
                    case DATE ->
                        keys[i][c] = parseDate(val);
                    default ->
                        keys[i][c] = val; // TEXT
                }
            }
        }

        // Indices relatifs (0..n-1) que nous trierons
        List<Integer> relativeIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            relativeIndices.add(i);
        }

        // Construire le comparator en utilisant les clés pré-calculées
        Comparator<Integer> indexComparator = (ia, ib) -> {
            for (int c = 0; c < criteriaCount; c++) {
                Criteria cr = active.get(c);
                int res;
                switch (cr.getType()) {
                    case NUMBER -> {
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
                        res = collators[c].compare(sa, sb);
                    }
                }

                if (res != 0) {
                    // Sens décroissant -> inverser le résultat
                    return cr.getDirection() == Criteria.Direction.DESCENDING ? -res : res;
                }
            }
            return 0; // tous les critères égaux
        };

        // indices.sort() (TimSort) garantit la stabilité
        relativeIndices.sort(indexComparator);

        // Reconvertir les indices relatifs en indices absolus (position dans `lines`)
        List<Integer> absolute = new ArrayList<>(relativeIndices.size());
        for (int relIdx : relativeIndices) {
            absolute.add(relIdx + headerOffset);
        }

        return buildResult(header, absolute);
    }

    /**
     * Tri asynchrone : exécute le tri hors du thread courant. Si executor ==
     * null, ForkJoinPool.commonPool() est utilisé.
     */
    public CompletableFuture<Result> sortAsync(Executor executor) {
        Executor exec = executor != null ? executor : ForkJoinPool.commonPool();
        return CompletableFuture.supplyAsync(this::sort, exec);
    }

    /**
     * Dessine la liste triée. Méthode publique et optionnelle : l'utilisateur
     * de l'API peut l'ignorer complètement et se contenter de sort().
     *
     * Comportement : - Calcule le résultat trié en appelant sort(). - Dessine
     * chaque ligne triée en partant de la position (x,y) fournie. - Utilise la
     * police/metrics courante du Graphics passé en paramètre.
     *
     * Remarque importante : - Cette méthode appelle sort() et exécute donc la
     * logique de tri sur le thread courant. Évite d'appeler Paint sur de très
     * grandes listes depuis l'EDT si le tri est lourd ; dans ce cas, préfère
     * pré-calculer le Result hors-EDT et stocker le résultat pour la peinture.
     */
    public void Paint(Graphics g, int x, int y) {
        if (g == null) {
            return;
        }

        Result result = sort();

        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int ascent = fm.getAscent();

        // Baseline de départ : y correspond au coin supérieur ; on dessine à la baseline
        int baseline = y + ascent;

        for (String line : result.getSortedLines()) {
            g.drawString(line != null ? line : "", x, baseline);
            baseline += lineHeight;
        }
    }

    // ---------------------------------------------------------------------
    // Fonctions utilitaires internes
    // ---------------------------------------------------------------------
    /**
     * Construit la liste des indices absolus [headerOffset .. headerOffset+n-1],
     * utilisée comme point de départ avant tri par les branches simples
     * (comparator personnalisé, tri naturel sans critère).
     */
    private List<Integer> absoluteIndices(int n, int headerOffset) {
        List<Integer> indices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            indices.add(i + headerOffset);
        }
        return indices;
    }

    /**
     * Factorise la reconstruction du Result à partir de l'en-tête (optionnel)
     * et de la liste des indices absolus déjà triés (positions dans `lines`).
     * Centralise ce qui était auparavant dupliqué dans chaque branche de sort().
     */
    private Result buildResult(String header, List<Integer> sortedAbsoluteIndices) {
        List<String> sorted = new ArrayList<>();
        if (header != null) {
            sorted.add(header);
        }

        int[] permutation = new int[sortedAbsoluteIndices.size() + (header == null ? 0 : 1)];
        int pos = (header == null) ? 0 : 1;
        // permutation[0] reste à 0 par défaut quand il y a un en-tête,
        // ce qui correspond bien à son indice d'origine (0 -> 0).
        for (int idx : sortedAbsoluteIndices) {
            sorted.add(lines.get(idx));
            permutation[pos++] = idx;
        }

        return new Result(sorted, permutation);
    }

    // Extrait la valeur à comparer : soit le texte entier, soit la colonne N
    private static String extractValue(String text, String field, Options options) {
        if (text == null) {
            return "";
        }
        if (!options.hasSeparator() || field == null || field.equalsIgnoreCase("Paragraphs")) {
            return text.trim();
        }
        int columnNumber = extractColumnNumber(field);
        if (columnNumber < 1) {
            return text.trim();
        }
        String sep = String.valueOf(options.getSeparatorChar());
        String[] parts = text.split(sep, -1);
        int index = columnNumber - 1;
        return (index < parts.length) ? parts[index].trim() : "";
    }

    private static int extractColumnNumber(String field) {
        if (field == null) {
            return -1;
        }
        try {
            String[] parts = field.trim().split("\\s+");
            return (parts.length >= 2) ? Integer.parseInt(parts[1]) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Parse nombre : retourne Double.MAX_VALUE si aucun nombre trouvé
    private static double parseNumber(String value) {
        if (value == null || value.isBlank()) {
            return Double.MAX_VALUE;
        }
        Matcher m = NUMBER_PATTERN.matcher(value);
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
    private static long parseDate(String value) {
        if (value == null || value.isBlank()) {
            return Long.MAX_VALUE;
        }
        // Chercher la première séquence ressemblant à une date
        Matcher m = Pattern.compile("\\d{1,4}([/\\- ]\\d{1,4})+").matcher(value);
        if (m.find()) {
            String candidate = m.group();
            for (String fmt : DATE_FORMATS) {
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
}