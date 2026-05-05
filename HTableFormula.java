package hsupertable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTableFormula — Moteur de gestion des formules pour HSuperTable.
 *
 * Classe utilitaire pure (pas d'état, que des méthodes statiques). Elle prend
 * une formule en String, la parse, résout les références de cellules et retourne
 * un résultat numérique ou un message d'erreur.
 *
 * Notation adoptée (identique à Excel/Word) :
 *   - Colonnes : lettres majuscules  A=col 0, B=col 1, Z=col 25, AA=col 26...
 *   - Lignes   : nombres 1-indexés   1=ligne 0, 2=ligne 1...
 *   - Plage    : A1:C3  = toutes les cellules du rectangle A1 → C3
 *
 * Formules supportées :
 *   =SUM(A1:A5)        somme d'une plage
 *   =AVERAGE(B1:B3)    moyenne d'une plage
 *   =COUNT(C1:C10)     nombre de cellules non vides et numériques
 *   =MAX(A1:A5)        valeur maximale
 *   =MIN(A1:A5)        valeur minimale
 *   =A1+B2             addition de deux cellules
 *   =A1-B2             soustraction
 *   =A1*B2             multiplication
 *   =A1/B2             division (retourne "#DIV/0!" si diviseur = 0)
 *   =42                constante numérique directe
 *
 * En cas d'erreur (référence invalide, données non numériques, division par
 * zéro), la méthode retourne un String commençant par "#" — convention
 * identique à Excel pour que l'utilisateur reconnaisse immédiatement le problème.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HTableFormula {

    // =========================================================================
    // PATTERNS DE RECONNAISSANCE
    // =========================================================================

    /** Référence simple : A1, B12, AA3... */
    private static final Pattern REF_SINGLE =
        Pattern.compile("^([A-Z]+)(\\d+)$");

    /** Plage rectangulaire : A1:C3, B2:B10... */
    private static final Pattern REF_RANGE =
        Pattern.compile("^([A-Z]+)(\\d+):([A-Z]+)(\\d+)$");

    /** Fonction avec argument : SUM(A1:A5), MAX(B1:B3)... */
    private static final Pattern FUNC_CALL =
        Pattern.compile("^(SUM|AVERAGE|COUNT|MAX|MIN)\\(([^)]+)\\)$",
                        Pattern.CASE_INSENSITIVE);

    /** Opération entre deux termes : A1+B2, A1*42, 10/B3... */
    private static final Pattern BINARY_OP =
        Pattern.compile("^(.+?)([+\\-*/])(.+)$");

    // Constructeur privé — cette classe n'est pas instanciable
    private HTableFormula() {}

    // =========================================================================
    // POINT D'ENTRÉE PRINCIPAL
    // =========================================================================

    /**
     * Évalue une formule et retourne son résultat.
     *
     * @param formula la formule brute (ex: "=SUM(A1:A5)"), doit commencer par "="
     * @param model   le modèle de données du tableau, utilisé pour lire les cellules
     * @return un Double si le calcul réussit, ou un String d'erreur (ex: "#REF!")
     */
    public static Object evaluate(String formula, HSuperDefaultTableModel model) {
        if (formula == null || formula.isEmpty()) return null;

        // On retire le "=" de tête et on nettoie les espaces
        String expr = formula.startsWith("=")
                      ? formula.substring(1).trim()
                      : formula.trim();

        if (expr.isEmpty()) return null;

        try {
            return evaluateExpression(expr.toUpperCase(), model);
        } catch (FormulaException e) {
            // On retourne le message d'erreur directement dans la cellule,
            // comme le fait Excel — l'utilisateur voit "#REF!" ou "#VALUE!" etc.
            return e.getMessage();
        } catch (Exception e) {
            return "#ERROR!";
        }
    }

    // =========================================================================
    // ÉVALUATION D'UNE EXPRESSION
    // =========================================================================

    /**
     * Évalue récursivement une expression.
     * L'ordre de reconnaissance est :
     *   1. Constante numérique directe
     *   2. Fonction (SUM, AVERAGE, etc.)
     *   3. Référence de cellule simple (A1)
     *   4. Opération binaire (A1+B2, 10*C3, etc.)
     */
    private static Object evaluateExpression(String expr, HSuperDefaultTableModel model)
            throws FormulaException {

        expr = expr.trim();

        // ── 1. Constante numérique ─────────────────────────────────────────
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException ignored) {
            // Pas un nombre, on continue
        }

        // ── 2. Fonction ────────────────────────────────────────────────────
        Matcher funcMatcher = FUNC_CALL.matcher(expr);
        if (funcMatcher.matches()) {
            String funcName = funcMatcher.group(1).toUpperCase();
            String argument = funcMatcher.group(2).trim();
            List<Double> values = resolveArgument(argument, model);
            return applyFunction(funcName, values);
        }

        // ── 3. Référence de cellule simple ─────────────────────────────────
        Matcher refMatcher = REF_SINGLE.matcher(expr);
        if (refMatcher.matches()) {
            String colStr = refMatcher.group(1);
            int rowNum    = Integer.parseInt(refMatcher.group(2));
            return resolveSingleRef(colStr, rowNum, model);
        }

        // ── 4. Opération binaire ───────────────────────────────────────────
        // On cherche l'opérateur le plus à droite (pour respecter la priorité
        // des opérations lors des appels récursifs) en parcourant de droite à
        // gauche. On gère + et - en priorité basse, * et / en priorité haute.
        return evaluateBinaryOp(expr, model);
    }

    /**
     * Recherche et évalue une opération binaire dans l'expression.
     *
     * On fait deux passes : d'abord on cherche un + ou - (priorité basse),
     * ensuite un * ou / (priorité haute). Cela permet de respecter l'ordre
     * des opérations sans avoir à construire un arbre syntaxique complet.
     */
    private static Object evaluateBinaryOp(String expr, HSuperDefaultTableModel model)
            throws FormulaException {

        // Cherche + ou - en dehors des parenthèses
        int idx = findOperatorOutsideParens(expr, new char[]{'+', '-'});
        if (idx > 0) {
            char op = expr.charAt(idx);
            double left  = toDouble(evaluateExpression(expr.substring(0, idx), model), expr);
            double right = toDouble(evaluateExpression(expr.substring(idx + 1), model), expr);
            return op == '+' ? left + right : left - right;
        }

        // Cherche * ou / en dehors des parenthèses
        idx = findOperatorOutsideParens(expr, new char[]{'*', '/'});
        if (idx > 0) {
            char op = expr.charAt(idx);
            double left  = toDouble(evaluateExpression(expr.substring(0, idx), model), expr);
            double right = toDouble(evaluateExpression(expr.substring(idx + 1), model), expr);
            if (op == '/' && right == 0) throw new FormulaException("#DIV/0!");
            return op == '*' ? left * right : left / right;
        }

        throw new FormulaException("#VALUE! Expression non reconnue : " + expr);
    }

    /**
     * Cherche le premier opérateur donné qui n'est pas à l'intérieur de
     * parenthèses. On parcourt de droite à gauche pour que la récursion
     * s'applique de gauche à droite (associativité gauche).
     *
     * @return l'index de l'opérateur, ou -1 si non trouvé
     */
    private static int findOperatorOutsideParens(String expr, char[] operators) {
        int depth = 0;
        // Parcours de droite à gauche pour associativité gauche
        for (int i = expr.length() - 1; i >= 0; i--) {
            char ch = expr.charAt(i);
            if (ch == ')') depth++;
            else if (ch == '(') depth--;
            else if (depth == 0) {
                for (char op : operators) {
                    // On s'assure que ce n'est pas un signe négatif en début d'expression
                    if (ch == op && i > 0) return i;
                }
            }
        }
        return -1;
    }

    // =========================================================================
    // RÉSOLUTION DES RÉFÉRENCES
    // =========================================================================

    /**
     * Résout l'argument d'une fonction — soit une plage (A1:C3) soit une
     * référence simple (B2) — et retourne la liste des valeurs numériques.
     * Les cellules vides ou non numériques sont silencieusement ignorées.
     */
    private static List<Double> resolveArgument(String arg, HSuperDefaultTableModel model)
            throws FormulaException {

        List<Double> values = new ArrayList<>();
        arg = arg.trim();

        // Plage rectangulaire : A1:C3
        Matcher rangeMatcher = REF_RANGE.matcher(arg);
        if (rangeMatcher.matches()) {
            String startCol = rangeMatcher.group(1);
            int    startRow = Integer.parseInt(rangeMatcher.group(2));
            String endCol   = rangeMatcher.group(3);
            int    endRow   = Integer.parseInt(rangeMatcher.group(4));

            int c1 = colLetterToIndex(startCol);
            int r1 = startRow - 1;  // passage en 0-indexé
            int c2 = colLetterToIndex(endCol);
            int r2 = endRow - 1;

            // On normalise pour gérer les plages saisies à l'envers (C3:A1)
            int rMin = Math.min(r1, r2), rMax = Math.max(r1, r2);
            int cMin = Math.min(c1, c2), cMax = Math.max(c1, c2);

            for (int r = rMin; r <= rMax; r++) {
                for (int c = cMin; c <= cMax; c++) {
                    Double val = extractNumeric(r, c, model);
                    if (val != null) values.add(val);
                }
            }
            return values;
        }

        // Référence simple : B2
        Matcher refMatcher = REF_SINGLE.matcher(arg);
        if (refMatcher.matches()) {
            String colStr = refMatcher.group(1);
            int rowNum    = Integer.parseInt(refMatcher.group(2));
            Double val = extractNumeric(rowNum - 1, colLetterToIndex(colStr), model);
            if (val != null) values.add(val);
            return values;
        }

        // Constante numérique directe dans l'argument (ex: SUM(42) — rare mais valide)
        try {
            values.add(Double.parseDouble(arg));
            return values;
        } catch (NumberFormatException ignored) {}

        throw new FormulaException("#REF! Référence invalide : " + arg);
    }

    /**
     * Lit la valeur numérique d'une cellule.
     * Retourne null si la cellule est vide, non numérique ou hors limites.
     * On ne lève pas d'exception ici — une cellule vide dans une plage
     * est simplement ignorée, comme dans Excel.
     */
    private static Double extractNumeric(int row, int col, HSuperDefaultTableModel model) {
        if (row < 0 || row >= model.getRowCount()) return null;
        if (col < 0 || col >= model.getColumnCount()) return null;

        Object val = model.getValueAt(row, col);
        if (val == null) return null;

        if (val instanceof Number) return ((Number) val).doubleValue();

        // Tentative de parsing si c'est une String
        try {
            return Double.parseDouble(val.toString().trim());
        } catch (NumberFormatException e) {
            return null;  // texte non numérique : ignoré silencieusement
        }
    }

    /**
     * Résout une référence de cellule simple (ex: "B3") et retourne sa valeur.
     *
     * @throws FormulaException si la référence est hors du tableau ou non numérique
     */
    private static double resolveSingleRef(String colStr, int rowNum,
                                            HSuperDefaultTableModel model)
            throws FormulaException {

        int row = rowNum - 1;
        int col = colLetterToIndex(colStr);

        Double val = extractNumeric(row, col, model);
        if (val == null) {
            throw new FormulaException("#VALUE! Cellule " + colStr + rowNum
                                       + " vide ou non numérique");
        }
        return val;
    }

    // =========================================================================
    // APPLICATION DES FONCTIONS
    // =========================================================================

    /**
     * Applique une fonction agrégative à une liste de valeurs.
     *
     * @param funcName nom de la fonction (SUM, AVERAGE, COUNT, MAX, MIN)
     * @param values   liste des valeurs numériques résolues
     * @return le résultat sous forme de Double
     * @throws FormulaException si la liste est vide ou la fonction inconnue
     */
    private static double applyFunction(String funcName, List<Double> values)
            throws FormulaException {

        // COUNT est le seul qui accepte une liste vide (retourne 0)
        if ("COUNT".equals(funcName)) return values.size();

        if (values.isEmpty()) {
            throw new FormulaException("#VALUE! Aucune valeur numérique dans la plage");
        }

        switch (funcName) {
            case "SUM": {
                double sum = 0;
                for (double v : values) sum += v;
                return sum;
            }
            case "AVERAGE": {
                double sum = 0;
                for (double v : values) sum += v;
                return sum / values.size();
            }
            case "MAX": {
                double max = values.get(0);
                for (double v : values) if (v > max) max = v;
                return max;
            }
            case "MIN": {
                double min = values.get(0);
                for (double v : values) if (v < min) min = v;
                return min;
            }
            default:
                throw new FormulaException("#NAME! Fonction inconnue : " + funcName);
        }
    }

    // =========================================================================
    // CONVERSION COLONNE LETTRE → INDEX
    // =========================================================================

    /**
     * Convertit une notation de colonne alphabétique en index 0-basé.
     *
     * Exemples :
     *   A  →  0
     *   B  →  1
     *   Z  →  25
     *   AA →  26
     *   AB →  27
     *
     * C'est la même logique qu'Excel — une colonne "AA" vaut 26*1 + 0 = 26.
     *
     * @param colStr la notation alphabétique (ex: "AA")
     * @return l'index 0-basé correspondant
     */
    public static int colLetterToIndex(String colStr) {
        int index = 0;
        for (int i = 0; i < colStr.length(); i++) {
            index = index * 26 + (colStr.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }

    /**
     * Convertit un index de colonne 0-basé en notation alphabétique.
     * Inverse de colLetterToIndex().
     *
     * Exemples :
     *   0  →  "A"
     *   25 →  "Z"
     *   26 →  "AA"
     *
     * @param index l'index 0-basé
     * @return la notation alphabétique correspondante
     */
    public static String colIndexToLetter(int index) {
        StringBuilder sb = new StringBuilder();
        index++;  // passage en 1-basé pour le calcul
        while (index > 0) {
            index--;
            sb.insert(0, (char) ('A' + (index % 26)));
            index /= 26;
        }
        return sb.toString();
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    /**
     * Convertit un Object en double.
     * Lève FormulaException si la conversion est impossible.
     *
     * @param obj     l'objet à convertir
     * @param context l'expression d'origine, pour le message d'erreur
     */
    private static double toDouble(Object obj, String context) throws FormulaException {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(obj.toString().trim());
        } catch (NumberFormatException e) {
            throw new FormulaException("#VALUE! Valeur non numérique dans : " + context);
        }
    }

    // =========================================================================
    // EXCEPTION INTERNE
    // =========================================================================

    /**
     * Exception interne utilisée pour remonter les erreurs de formule.
     * Elle n'est jamais exposée à l'utilisateur — on la convertit en String
     * d'erreur dans evaluate().
     */
    private static class FormulaException extends Exception {
        FormulaException(String message) {
            super(message);
        }
    }
}
