/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package htextarea.sort;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Moteur de tri des paragraphes.
 *
 * Cette classe est purement algorithmique — elle ne touche pas au document ni à
 * l'interface. Elle reçoit une liste de paragraphes avec leurs styles, les
 * critères et options de tri, et retourne la liste réordonnée.
 *
 * @author FIDELE
 * @version 1.0
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
    // Point d'entrée principal
    // =========================================================================
    /**
     * Trie une liste de paragraphes selon les critères et options donnés.
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

        // Séparer l'en-tête du reste si nécessaire
        HParagrapheAvecStyle enTete = null;
        List<HParagrapheAvecStyle> aTriar;

        if (options.isLigneEnTete() && paragraphes.size() > 1) {
            // La première ligne est figée — on ne la trie pas
            enTete = paragraphes.get(0);
            aTriar = new ArrayList<>(paragraphes.subList(1, paragraphes.size()));
        } else {
            aTriar = new ArrayList<>(paragraphes);
        }

        // Construire le Comparator depuis les critères actifs
        Comparator<HParagrapheAvecStyle> comparateur = construireComparateur(
                criteres, options);

        aTriar.sort(comparateur);

        // Reconstruire la liste finale avec l'en-tête en tête si nécessaire
        List<HParagrapheAvecStyle> resultat = new ArrayList<>();
        if (enTete != null) {
            resultat.add(enTete);
        }
        resultat.addAll(aTriar);

        return resultat;
    }

    // =========================================================================
    // Construction du Comparator 
    // =========================================================================
    /**
     * Construit un {@link Comparator} chaîné depuis la liste des critères.
     *
     * Les critères inactifs sont ignorés. Si aucun critère n'est actif, on
     * retourne un comparateur neutre.
     */
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

    /**
     * Crée un {@link Comparator} pour un seul critère de tri.
     *
     * <p>
     * Extrait la valeur à comparer depuis le paragraphe (texte entier ou
     * colonne spécifique), puis compare selon le type (texte, nombre, date) et
     * applique le sens (croissant ou décroissant).</p>
     */
    private static Comparator<HParagrapheAvecStyle> comparateurPourCritere(
            HSortCritere critere, HSortOptions options) {

        // Collator pour le tri textuel (gère les accents, la locale, la casse)
        Collator collator = Collator.getInstance(options.getLocale());
        collator.setStrength(options.isRespecterCasse()
                ? Collator.TERTIARY // distingue la casse
                : Collator.SECONDARY // ignore la casse, respecte les accents
        );

        Comparator<HParagrapheAvecStyle> comp;

        switch (critere.getType()) {

            case NOMBRE -> {
                // Tri numérique : extraire le nombre et comparer les doubles
                comp = Comparator.comparingDouble(p -> {
                    String val = extraireValeur(p.getText(), critere.getChamp(), options);
                    return parseNombre(val);
                });
            }

            case DATE -> {
                // Tri chronologique : parser la date et comparer les timestamps
                comp = Comparator.comparingLong(p -> {
                    String val = extraireValeur(p.getText(), critere.getChamp(), options);
                    return parseDate(val);
                });
            }

            default -> {
                // Tri textuel avec Collator (gère locale et casse)
                comp = (a, b) -> {
                    String va  = extraireValeur(a.getText(), critere.getChamp(), options);
                    String vb = extraireValeur(b.getText(), critere.getChamp(), options);
                    return collator.compare(va, vb);
                };
            }
        }

        // Inverser si décroissant
        if (critere.getSens() == HSortCritere.Sens.DECROISSANT) {
            comp = comp.reversed();
        }

        return comp;
    }

    // =========================================================================
    // Extraction de valeur depuis un paragraphe
    // =========================================================================
    /**
     * Extrait la valeur à comparer depuis le texte d'un paragraphe.
     *     
     * Si le champ est "Paragraphes" ou si pas de séparateur → texte entier. Si
     * le champ est "Colonne N" → on découpe le texte et on prend la Nème
     * colonne.
     *
     * @param texte le texte du paragraphe
     * @param champ le champ demandé ("Paragraphes", "Colonne 1", etc.)
     * @param options les options (pour le séparateur)
     * @return la valeur à comparer
     */
    private static String extraireValeur(String texte, String champ,
            HSortOptions options) {
        if (texte == null) {
            return "";
        }

        // Si pas de séparateur ou champ = "Paragraphes" → texte entier
        if (!options.aUnSeparateur() || champ == null
                || champ.equalsIgnoreCase("Paragraphes")) {
            return texte.trim();
        }

        // Extraire le numéro de colonne depuis "Colonne N"
        int numColonne = extraireNumeroColonne(champ);
        if (numColonne < 1) {
            return texte.trim();
        }

        // Découper le texte selon le séparateur
        String sep = String.valueOf(options.getCaractereSeparateur());
        String[] parts = texte.split(sep, -1);

        int index = numColonne - 1; // 1-based → 0-based
        return (index < parts.length) ? parts[index].trim() : "";
    }

    /**
     * Extrait le numéro depuis "Colonne N". Retourne -1 si le format est
     * invalide.
     */
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

    // =========================================================================
    // Parsing des types
    // =========================================================================
    /**
     * Parse une chaîne en double pour le tri numérique. Retourne
     * {@code Double.MAX_VALUE} si le parsing échoue (les valeurs non numériques
     * sont repoussées en fin de liste).
     */
    private static double parseNombre(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Double.MAX_VALUE;
        }
        try {
            // Remplacer la virgule décimale par un point pour la compatibilité
            return Double.parseDouble(valeur.replace(',', '.').replace(" ", ""));
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Parse une chaîne en timestamp pour le tri chronologique. Essaie plusieurs
     * formats de date courants. Retourne {@code Long.MAX_VALUE} si aucun format
     * ne correspond.
     */
    private static long parseDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Long.MAX_VALUE;
        }

        for (String format : FORMATS_DATE) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(valeur.trim()).getTime();
            } catch (ParseException ignored) {
                // Essayer le format suivant
            }
        }
        return Long.MAX_VALUE;
    }

    // =======================================================================
    // Utilitaire — déterminer le nombre de colonnes dans un ensemble de
    // paragraphes 
    // =============================================================
    /**
     * Calcule le nombre maximum de colonnes trouvé dans l'ensemble des
     * paragraphes, selon le séparateur défini dans les options.
     *
     * <p>
     * Utilisé par  HSortDialog pour construire les choix "Colonne 1",
     * "Colonne 2"... dans les combos "Trier par".</p>
     *
     * @param paragraphes la liste des paragraphes
     * @param options les options (pour le séparateur)
     * @return le nombre max de colonnes (0 si pas de séparateur)
     */
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
