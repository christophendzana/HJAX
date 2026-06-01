package hsupertable;

import java.util.*;

/**
 * HTableFormula — Moteur de formules pour notre HSuperTable.
 *
 * pipeline : Lexer -> Parser -> Evaluator
 *
 * Liste des Fonctions : SUM, AVERAGE, COUNT, MAX, MIN, PRODUCT, IF, AND, OR,
 * NOT, ABS, INT, MOD, ROUND, SIGN, DEFINED, TRUE, FALSE
 *
 * Arguments de position : LEFT, RIGHT, ABOVE, BELOW Références de cellules :
 * style A1 (A1, B2:D5) et RnCn (R1C2, R1C2:R3C4)
 *
 * @author FIDELE
 * @version 2.0
 */
public class HTableFormula {

    /**
     * Types de tokens produits par le Lexer.
     */
    enum TokenType {
        NUMBER, // 42, 3.14
        STRING, // "texte"
        IDENTIFIER, // SUM, IF, A1, LEFT, R1C2...
        LPAREN, // (
        RPAREN, // )
        COMMA, // ,
        SEMICOLON, // ;
        COLON, // :
        PLUS, // +
        MINUS, // -
        MULTIPLY, // *
        DIVIDE, // /
        EQUALS, // =
        LT, // 
        GT, // >
        LTE, // <=
        GTE, // >=
        NEQ, // <>
        EOF // fin de l'expression
    }

    /**
     * Unité lexicale produite par le Lexer.
     */
    static class Token {

        /**
         * Type du token.
         */
        final TokenType type;

        /**
         * Valeur textuelle brute.
         */
        final String value;

        /**
         * Position dans l'expression (pour les messages d'erreur).
         */
        final int position;

        Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            return "Token[" + type + ", '" + value + "', pos=" + position + "]";
        }
    }

    /**
     * Lexer — il lit l'expression caractère par caractère et produit une liste
     * de Token.
     *
     * Gère : - Nombres entiers et décimaux (42, 3.14, .5) - Chaînes entre
     * guillemets ("texte") - Identifiants : fonctions, références, arguments de
     * position - Tous les opérateurs arithmétiques et de comparaison -
     * Parenthèses, virgules, points-virgules, deux-points - Espaces ignorés
     */
    static class Lexer {

        /**
         * Expression source.
         */
        private final String input;

        /**
         * Position courante dans l'expression.
         */
        private int pos;

        /**
         * Longueur totale de l'expression.
         */
        private final int length;

        Lexer(String input) {
            this.input = input.trim();
            this.pos = 0;
            this.length = this.input.length();
        }

        /**
         * Produit la liste complète des tokens depuis l'expression.
         *
         * @return liste de tokens, terminée par EOF
         * @throws FormulaException si un caractère invalide est rencontré
         */
        List<Token> tokenize() throws FormulaException {
            List<Token> tokens = new ArrayList<>();

            while (pos < length) {
                char c = input.charAt(pos);

                // Espaces ignorés 
                if (Character.isWhitespace(c)) {
                    pos++;
                    continue;
                }

                //  Nombres 
                if (Character.isDigit(c) || (c == '.' && isNextDigit())) {
                    tokens.add(readNumber());
                    continue;
                }

                // Identifiants (fonctions, refs, positions) 
                if (Character.isLetter(c) || c == '_') {
                    tokens.add(readIdentifier());
                    continue;
                }

                // ── Chaînes entre guillemets ──────────────────────────────
                if (c == '"') {
                    tokens.add(readString());
                    continue;
                }

                // ── Opérateurs et symboles ────────────────────────────────
                int startPos = pos;
                switch (c) {
                    case '(' -> {
                        tokens.add(new Token(TokenType.LPAREN, "(", startPos));
                        pos++;
                    }
                    case ')' -> {
                        tokens.add(new Token(TokenType.RPAREN, ")", startPos));
                        pos++;
                    }
                    case ',' -> {
                        tokens.add(new Token(TokenType.COMMA, ",", startPos));
                        pos++;
                    }
                    case ';' -> {
                        tokens.add(new Token(TokenType.SEMICOLON, ";", startPos));
                        pos++;
                    }
                    case ':' -> {
                        tokens.add(new Token(TokenType.COLON, ":", startPos));
                        pos++;
                    }
                    case '+' -> {
                        tokens.add(new Token(TokenType.PLUS, "+", startPos));
                        pos++;
                    }
                    case '-' -> {
                        tokens.add(new Token(TokenType.MINUS, "-", startPos));
                        pos++;
                    }
                    case '*' -> {
                        tokens.add(new Token(TokenType.MULTIPLY, "*", startPos));
                        pos++;
                    }
                    case '/' -> {
                        tokens.add(new Token(TokenType.DIVIDE, "/", startPos));
                        pos++;
                    }
                    case '=' -> {
                        tokens.add(new Token(TokenType.EQUALS, "=", startPos));
                        pos++;
                    }
                    case '<' -> {
                        pos++;
                        if (pos < length && input.charAt(pos) == '=') {
                            tokens.add(new Token(TokenType.LTE, "<=", startPos));
                            pos++;
                        } else if (pos < length && input.charAt(pos) == '>') {
                            tokens.add(new Token(TokenType.NEQ, "<>", startPos));
                            pos++;
                        } else {
                            tokens.add(new Token(TokenType.LT, "<", startPos));
                        }
                    }
                    case '>' -> {
                        pos++;
                        if (pos < length && input.charAt(pos) == '=') {
                            tokens.add(new Token(TokenType.GTE, ">=", startPos));
                            pos++;
                        } else {
                            tokens.add(new Token(TokenType.GT, ">", startPos));
                        }
                    }
                    default ->
                        throw new FormulaException(
                                "#SYNTAX! Caractère invalide '" + c
                                + "' à la position " + pos);
                }
            }

            tokens.add(new Token(TokenType.EOF, "", pos));
            return tokens;
        }

        /**
         * Lit un nombre entier ou décimal. Exemples : 42, 3.14, .5
         */
        private Token readNumber() throws FormulaException {
            int start = pos;
            StringBuilder sb = new StringBuilder();
            boolean hasDot = false;

            while (pos < length) {
                char c = input.charAt(pos);
                if (Character.isDigit(c)) {
                    sb.append(c);
                    pos++;
                } else if (c == '.' && !hasDot) {
                    hasDot = true;
                    sb.append(c);
                    pos++;
                } else {
                    break;
                }
            }

            String numStr = sb.toString();
            if (numStr.equals(".")) {
                throw new FormulaException("#SYNTAX! Nombre invalide à la position " + start);
            }

            return new Token(TokenType.NUMBER, numStr, start);
        }

        /**
         * Lit un identifiant : fonction, référence de cellule, argument de
         * position. Exemples : SUM, A1, LEFT, R1C2, ABOVE Cas spécial :
         * "AU-DESSUS" et "EN DESSOUS" contiennent des tirets et espaces — gérés
         * ici.
         */
        private Token readIdentifier() {
            int start = pos;
            StringBuilder sb = new StringBuilder();

            while (pos < length) {
                char c = input.charAt(pos);
                if (Character.isLetterOrDigit(c) || c == '_') {
                    sb.append(c);
                    pos++;
                } else if (c == '-' && pos + 1 < length
                        && Character.isLetter(input.charAt(pos + 1))) {
                    // Tiret dans un identifiant (ex: AU-DESSUS)
                    sb.append(c);
                    pos++;
                } else {
                    break;
                }
            }

            return new Token(TokenType.IDENTIFIER,
                    sb.toString().toUpperCase(), start);
        }

        /**
         * Lit une chaîne entre guillemets doubles. Exemple : "texte"
         */
        private Token readString() throws FormulaException {
            int start = pos;
            pos++; // sauter le guillemet ouvrant
            StringBuilder sb = new StringBuilder();

            while (pos < length) {
                char c = input.charAt(pos);
                if (c == '"') {
                    pos++; // sauter le guillemet fermant
                    return new Token(TokenType.STRING, sb.toString(), start);
                }
                sb.append(c);
                pos++;
            }

            throw new FormulaException(
                    "#SYNTAX! Chaîne non fermée à la position " + start);
        }

        /**
         * Vrai si le prochain caractère est un chiffre. Utilisé pour distinguer
         * "." décimal de l'opérateur ".".
         */
        private boolean isNextDigit() {
            return pos + 1 < length
                    && Character.isDigit(input.charAt(pos + 1));
        }
    }

    // =========================================================================
    // COMPOSANT 4 — NŒUDS DE L'ARBRE SYNTAXIQUE
    // =========================================================================
    /**
     * Nœud de base de l'arbre syntaxique abstrait (AST). Chaque type de nœud
     * représente une construction syntaxique.
     */
    abstract static class Node {

        abstract Object evaluate(Evaluator eval) throws FormulaException;
    }

    /**
     * Nœud nombre littéral. Ex: 42, 3.14
     */
    static class NumberNode extends Node {

        final double value;

        NumberNode(double value) {
            this.value = value;
        }

        @Override
        Object evaluate(Evaluator eval) {
            return value;
        }
    }

    /**
     * Nœud chaîne littérale. Ex: "texte"
     */
    static class StringNode extends Node {

        final String value;

        StringNode(String value) {
            this.value = value;
        }

        @Override
        Object evaluate(Evaluator eval) {
            return value;
        }
    }

    /**
     * Nœud opération binaire. Ex: A1+B2, 3*4
     */
    static class BinaryOpNode extends Node {

        final Node left;
        final String op;
        final Node right;

        BinaryOpNode(Node left, String op, Node right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        Object evaluate(Evaluator eval) throws FormulaException {
            Object l = left.evaluate(eval);
            Object r = right.evaluate(eval);
            double lv = eval.toDouble(l, op);
            double rv = eval.toDouble(r, op);

            return switch (op) {
                case "+" ->
                    lv + rv;
                case "-" ->
                    lv - rv;
                case "*" ->
                    lv * rv;
                case "/" -> {
                    if (rv == 0) {
                        throw new FormulaException("#DIV/0!");
                    }
                    yield lv / rv;
                }
                case "=" ->
                    (lv == rv) ? 1.0 : 0.0;
                case "<" ->
                    (lv < rv) ? 1.0 : 0.0;
                case ">" ->
                    (lv > rv) ? 1.0 : 0.0;
                case "<=" ->
                    (lv <= rv) ? 1.0 : 0.0;
                case ">=" ->
                    (lv >= rv) ? 1.0 : 0.0;
                case "<>" ->
                    (lv != rv) ? 1.0 : 0.0;
                default ->
                    throw new FormulaException(
                            "#OP! Opérateur inconnu : " + op);
            };
        }
    }

    /**
     * Nœud opération unaire. Ex: -A1
     */
    static class UnaryOpNode extends Node {

        final String op;
        final Node operand;

        UnaryOpNode(String op, Node operand) {
            this.op = op;
            this.operand = operand;
        }

        @Override
        Object evaluate(Evaluator eval) throws FormulaException {
            Object v = operand.evaluate(eval);
            double dv = eval.toDouble(v, op);
            return op.equals("-") ? -dv : dv;
        }
    }

    /**
     * Nœud appel de fonction. Ex: SUM(A1:A5)
     */
    static class FunctionCallNode extends Node {

        final String name;
        final List<Node> args;

        FunctionCallNode(String name, List<Node> args) {
            this.name = name;
            this.args = args;
        }

        @Override
        Object evaluate(Evaluator eval) throws FormulaException {
            return eval.evaluateFunction(name, args);
        }
    }

    /**
     * Nœud référence de cellule simple. Ex: A1, R1C2
     */
    static class CellRefNode extends Node {

        final int row; // 0-indexé
        final int col; // 0-indexé

        CellRefNode(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        Object evaluate(Evaluator eval) throws FormulaException {
            return eval.getCellValue(row, col);
        }
    }

    /**
     * Nœud plage de cellules. Ex: A1:C3, R1C1:R3C3
     */
    static class RangeNode extends Node {

        final int rowStart, colStart, rowEnd, colEnd;

        RangeNode(int rowStart, int colStart, int rowEnd, int colEnd) {
            this.rowStart = rowStart;
            this.colStart = colStart;
            this.rowEnd = rowEnd;
            this.colEnd = colEnd;
        }

        @Override
        Object evaluate(Evaluator eval) throws FormulaException {
            // Une plage seule n'a pas de valeur scalaire
            throw new FormulaException(
                    "#VALUE! Une plage ne peut pas être utilisée ici");
        }

        /**
         * Retourne toutes les valeurs numériques de la plage.
         */
        List<Double> collectValues(Evaluator eval) {
            List<Double> values = new ArrayList<>();
            for (int r = rowStart; r <= rowEnd; r++) {
                for (int c = colStart; c <= colEnd; c++) {
                    Double v = eval.getNumericCellValue(r, c);
                    if (v != null) {
                        values.add(v);
                    }
                }
            }
            return values;
        }
    }

    /**
     * Nœud argument de position. Ex: LEFT, RIGHT, ABOVE, BELOW
     */
    static class PositionArgNode extends Node {

        final String direction;

        PositionArgNode(String direction) {
            this.direction = direction;
        }

        @Override
        Object evaluate(Evaluator eval) throws FormulaException {
            throw new FormulaException(
                    "#VALUE! L'argument de position '" + direction
                    + "' ne peut être utilisé que dans une fonction");
        }

        /**
         * Collecte les valeurs dans la direction indiquée.
         */
        List<Double> collectValues(Evaluator eval) {
            return eval.collectPositionValues(direction);
        }
    }

    // =========================================================================
    // COMPOSANT 5 — PARSER
    // =========================================================================
    /**
     * Parser récursif descendant. Consomme les tokens du Lexer et construit
     * l'AST.
     *
     * Grammaire : expression → comparison comparison → addition
     * (('='|'<'|'>'|'<='|'>='|'<>') addition)* addition → multiplication (('+'
     * | '-') multiplication)* multiplication → unary (('*' | '/') unary)* unary
     * → '-' unary | primary primary → NUMBER | STRING | function_or_ref | '('
     * expression ')' function_or_ref → IDENTIFIER ['(' args ')'] args →
     * expression (';' | ',' expression)*
     */
    static class Parser {

        private final List<Token> tokens;
        private int pos;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        /**
         * Point d'entrée — parse l'expression complète.
         */
        Node parse() throws FormulaException {
            Node node = parseExpression();
            expect(TokenType.EOF);
            return node;
        }

        // ── Niveaux de précédence ─────────────────────────────────────────
        private Node parseExpression() throws FormulaException {
            return parseComparison();
        }

        private Node parseComparison() throws FormulaException {
            Node left = parseAddition();

            while (peek().type == TokenType.EQUALS
                    || peek().type == TokenType.LT
                    || peek().type == TokenType.GT
                    || peek().type == TokenType.LTE
                    || peek().type == TokenType.GTE
                    || peek().type == TokenType.NEQ) {
                Token op = consume();
                Node right = parseAddition();
                left = new BinaryOpNode(left, op.value, right);
            }

            return left;
        }

        private Node parseAddition() throws FormulaException {
            Node left = parseMultiplication();

            while (peek().type == TokenType.PLUS
                    || peek().type == TokenType.MINUS) {
                Token op = consume();
                Node right = parseMultiplication();
                left = new BinaryOpNode(left, op.value, right);
            }

            return left;
        }

        private Node parseMultiplication() throws FormulaException {
            Node left = parseUnary();

            while (peek().type == TokenType.MULTIPLY
                    || peek().type == TokenType.DIVIDE) {
                Token op = consume();
                Node right = parseUnary();
                left = new BinaryOpNode(left, op.value, right);
            }

            return left;
        }

        private Node parseUnary() throws FormulaException {
            if (peek().type == TokenType.MINUS) {
                Token op = consume();
                return new UnaryOpNode(op.value, parseUnary());
            }
            return parsePrimary();
        }

        private Node parsePrimary() throws FormulaException {
            Token token = peek();

            // ── Nombre ────────────────────────────────────────────────────
            if (token.type == TokenType.NUMBER) {
                consume();
                return new NumberNode(Double.parseDouble(token.value));
            }

            // ── Chaîne ────────────────────────────────────────────────────
            if (token.type == TokenType.STRING) {
                consume();
                return new StringNode(token.value);
            }

            // ── Expression entre parenthèses ──────────────────────────────
            if (token.type == TokenType.LPAREN) {
                consume(); // (
                Node node = parseExpression();
                expect(TokenType.RPAREN); // )
                return node;
            }

            // ── Identifiant : fonction, référence, position ───────────────
            if (token.type == TokenType.IDENTIFIER) {
                return parseFunctionOrRef();
            }

            throw new FormulaException(
                    "#SYNTAX! Token inattendu : " + token + " à la position "
                    + token.position);
        }

        /**
         * Parse un identifiant — peut être : - Un appel de fonction : SUM(...)
         * - Une référence de cellule : A1, R1C2 - Une plage : A1:C3, R1C1:R3C3
         * - Un argument de position : LEFT, RIGHT, ABOVE, BELOW - TRUE / FALSE
         */
        private Node parseFunctionOrRef() throws FormulaException {
            Token id = consume();
            String name = id.value;

            // ── Appel de fonction ─────────────────────────────────────────
            if (peek().type == TokenType.LPAREN) {
                consume(); // (
                List<Node> args = new ArrayList<>();

                if (peek().type != TokenType.RPAREN) {
                    args.add(parseArgument());
                    while (peek().type == TokenType.SEMICOLON
                            || peek().type == TokenType.COMMA) {
                        consume(); // ; ou ,
                        args.add(parseArgument());
                    }
                }

                expect(TokenType.RPAREN);
                return new FunctionCallNode(name, args);
            }

            // ── TRUE / FALSE ──────────────────────────────────────────────
            if (name.equals("TRUE")) {
                return new NumberNode(1.0);
            }
            if (name.equals("FALSE")) {
                return new NumberNode(0.0);
            }

            // ── Arguments de position ─────────────────────────────────────
            if (isPositionArg(name)) {
                return new PositionArgNode(name);
            }

            // ── Référence de cellule style RnCn ───────────────────────────
            // Format : R<n>C<n> ou R<n>C<n>:R<n>C<n>
            if (name.matches("R\\d+C\\d+")) {
                CellRefNode ref = parseRnCnRef(name);
                // Vérifier si c'est une plage
                if (peek().type == TokenType.COLON) {
                    consume(); // :
                    Token next = peek();
                    if (next.type == TokenType.IDENTIFIER
                            && next.value.matches("R\\d+C\\d+")) {
                        consume();
                        CellRefNode ref2 = parseRnCnRef(next.value);
                        return new RangeNode(
                                Math.min(ref.row, ref2.row),
                                Math.min(ref.col, ref2.col),
                                Math.max(ref.row, ref2.row),
                                Math.max(ref.col, ref2.col));
                    }
                }
                return ref;
            }

            // ── Référence de cellule style A1 ─────────────────────────────
            // Format : [A-Z]+[0-9]+ ou [A-Z]+[0-9]+:[A-Z]+[0-9]+
            if (name.matches("[A-Z]+\\d+")) {
                CellRefNode ref = parseA1Ref(name);
                // Vérifier si c'est une plage
                if (peek().type == TokenType.COLON) {
                    consume(); // :
                    Token next = peek();
                    if (next.type == TokenType.IDENTIFIER
                            && next.value.matches("[A-Z]+\\d+")) {
                        consume();
                        CellRefNode ref2 = parseA1Ref(next.value);
                        return new RangeNode(
                                Math.min(ref.row, ref2.row),
                                Math.min(ref.col, ref2.col),
                                Math.max(ref.row, ref2.row),
                                Math.max(ref.col, ref2.col));
                    }
                }
                return ref;
            }

            throw new FormulaException(
                    "#NAME! Identifiant inconnu : " + name);
        }

        /**
         * Parse un argument de fonction — peut être une expression normale ou
         * un argument de position (LEFT, RIGHT, ABOVE, BELOW).
         */
        private Node parseArgument() throws FormulaException {
            Token token = peek();
            if (token.type == TokenType.IDENTIFIER
                    && isPositionArg(token.value)) {
                consume();
                return new PositionArgNode(token.value);
            }
            return parseExpression();
        }

        // ── Utilitaires de parsing ────────────────────────────────────────
        /**
         * Vrai si l'identifiant est un argument de position Word.
         */
        private boolean isPositionArg(String name) {
            return switch (name) {
                case "LEFT", "RIGHT", "ABOVE", "BELOW" ->
                    true;
                default ->
                    false;
            };
        }

        /**
         * Parse une référence style A1. La lettre = colonne (A=0, B=1,
         * AA=26...) Le chiffre = ligne (1=0, 2=1...)
         */
        private CellRefNode parseA1Ref(String name) throws FormulaException {
            int i = 0;
            while (i < name.length()
                    && Character.isLetter(name.charAt(i))) {
                i++;
            }

            String colStr = name.substring(0, i);
            String rowStr = name.substring(i);

            if (colStr.isEmpty() || rowStr.isEmpty()) {
                throw new FormulaException(
                        "#REF! Référence invalide : " + name);
            }

            int col = colLetterToIndex(colStr);
            int row = Integer.parseInt(rowStr) - 1; // 1-indexé → 0-indexé

            return new CellRefNode(row, col);
        }

        /**
         * Parse une référence style RnCn. R<n> = ligne (1-indexée), C<n> =
         * colonne (1-indexée)
         */
        private CellRefNode parseRnCnRef(String name) throws FormulaException {
            // Format attendu : R<n>C<n>
            int rIdx = name.indexOf('R');
            int cIdx = name.indexOf('C');

            if (rIdx < 0 || cIdx < 0 || cIdx <= rIdx) {
                throw new FormulaException(
                        "#REF! Référence RnCn invalide : " + name);
            }

            int row = Integer.parseInt(name.substring(rIdx + 1, cIdx)) - 1;
            int col = Integer.parseInt(name.substring(cIdx + 1)) - 1;

            return new CellRefNode(row, col);
        }

        /**
         * Retourne le token courant sans le consommer.
         */
        private Token peek() {
            return tokens.get(pos);
        }

        /**
         * Consomme et retourne le token courant.
         */
        private Token consume() {
            return tokens.get(pos++);
        }

        /**
         * Consomme un token du type attendu ou lève une exception.
         */
        private void expect(TokenType type) throws FormulaException {
            Token t = consume();
            if (t.type != type) {
                throw new FormulaException(
                        "#SYNTAX! Attendu " + type
                        + " mais obtenu " + t.type
                        + " ('" + t.value + "') à la position " + t.position);
            }
        }

        /**
         * Convertit une notation alphabétique de colonne en index 0-basé.
         */
        static int colLetterToIndex(String colStr) {
            int index = 0;
            for (int i = 0; i < colStr.length(); i++) {
                index = index * 26 + (colStr.charAt(i) - 'A' + 1);
            }
            return index - 1;
        }
    }

    // =========================================================================
    // COMPOSANT 6 — EVALUATOR
    // =========================================================================
    /**
     * Évaluateur — parcourt l'AST et calcule le résultat.
     *
     * Connaît la position de la cellule courante (formulaRow, formulaCol) pour
     * résoudre les arguments de position LEFT, RIGHT, ABOVE, BELOW.
     */
    static class Evaluator {

        /**
         * Modèle de données du tableau.
         */
        private final HSuperDefaultTableModel model;

        /**
         * Ligne de la cellule contenant la formule (0-indexée).
         */
        private final int formulaRow;

        /**
         * Colonne de la cellule contenant la formule (0-indexée).
         */
        private final int formulaCol;

        Evaluator(HSuperDefaultTableModel model, int formulaRow, int formulaCol) {
            this.model = model;
            this.formulaRow = formulaRow;
            this.formulaCol = formulaCol;
        }

        /**
         * Évalue un nœud de l'AST et retourne son résultat.
         */
        Object evaluate(Node node) throws FormulaException {
            return node.evaluate(this);
        }

        /**
         * Évalue un appel de fonction. Dispatche vers la méthode correspondante
         * selon le nom.
         */
        Object evaluateFunction(String name, List<Node> args)
                throws FormulaException {

            return switch (name) {
                // ── Fonctions agrégatives ─────────────────────────────────
                case "SUM" ->
                    funcSum(args);
                case "AVERAGE" ->
                    funcAverage(args);
                case "COUNT" ->
                    funcCount(args);
                case "MAX" ->
                    funcMax(args);
                case "MIN" ->
                    funcMin(args);
                case "PRODUCT" ->
                    funcProduct(args);

                // ── Logique ───────────────────────────────────────────────
                case "IF" ->
                    funcIf(args);
                case "AND" ->
                    funcAnd(args);
                case "OR" ->
                    funcOr(args);
                case "NOT" ->
                    funcNot(args);

                // ── Mathématiques ─────────────────────────────────────────
                case "ABS" ->
                    funcAbs(args);
                case "INT" ->
                    funcInt(args);
                case "MOD" ->
                    funcMod(args);
                case "ROUND" ->
                    funcRound(args);
                case "SIGN" ->
                    funcSign(args);

                // ── Utilitaires ───────────────────────────────────────────
                case "DEFINED" ->
                    funcDefined(args);
                case "TRUE" ->
                    1.0;
                case "FALSE" ->
                    0.0;

                default ->
                    throw new FormulaException(
                            "#NAME! Fonction inconnue : " + name);
            };
        }

        // ── Fonctions agrégatives ─────────────────────────────────────────
        private Object funcSum(List<Node> args) throws FormulaException {
            List<Double> values = collectAllValues(args);
            double sum = 0;
            for (double v : values) {
                sum += v;
            }
            return sum;
        }

        private Object funcAverage(List<Node> args) throws FormulaException {
            List<Double> values = collectAllValues(args);
            if (values.isEmpty()) {
                throw new FormulaException(
                        "#DIV/0! AVERAGE sur une plage vide");
            }
            double sum = 0;
            for (double v : values) {
                sum += v;
            }
            return sum / values.size();
        }

        private Object funcCount(List<Node> args) throws FormulaException {
            return (double) collectAllValues(args).size();
        }

        private Object funcMax(List<Node> args) throws FormulaException {
            List<Double> values = collectAllValues(args);
            if (values.isEmpty()) {
                throw new FormulaException(
                        "#VALUE! MAX sur une plage vide");
            }
            double max = values.get(0);
            for (double v : values) {
                if (v > max) {
                    max = v;
                }
            }
            return max;
        }

        private Object funcMin(List<Node> args) throws FormulaException {
            List<Double> values = collectAllValues(args);
            if (values.isEmpty()) {
                throw new FormulaException(
                        "#VALUE! MIN sur une plage vide");
            }
            double min = values.get(0);
            for (double v : values) {
                if (v < min) {
                    min = v;
                }
            }
            return min;
        }

        private Object funcProduct(List<Node> args) throws FormulaException {
            List<Double> values = collectAllValues(args);
            if (values.isEmpty()) {
                return 0.0;
            }
            double product = 1;
            for (double v : values) {
                product *= v;
            }
            return product;
        }

        // ── Fonctions logiques ────────────────────────────────────────────
        private Object funcIf(List<Node> args) throws FormulaException {
            if (args.size() != 3) {
                throw new FormulaException(
                        "#ARGS! IF requiert exactement 3 arguments");
            }
            Object condition = evaluate(args.get(0));
            double cond = toDouble(condition, "IF");
            return cond != 0
                    ? evaluate(args.get(1))
                    : evaluate(args.get(2));
        }

        private Object funcAnd(List<Node> args) throws FormulaException {
            for (Node arg : args) {
                double v = toDouble(evaluate(arg), "AND");
                if (v == 0) {
                    return 0.0;
                }
            }
            return 1.0;
        }

        private Object funcOr(List<Node> args) throws FormulaException {
            for (Node arg : args) {
                double v = toDouble(evaluate(arg), "OR");
                if (v != 0) {
                    return 1.0;
                }
            }
            return 0.0;
        }

        private Object funcNot(List<Node> args) throws FormulaException {
            if (args.size() != 1) {
                throw new FormulaException(
                        "#ARGS! NOT requiert exactement 1 argument");
            }
            double v = toDouble(evaluate(args.get(0)), "NOT");
            return v == 0 ? 1.0 : 0.0;
        }

        // ── Fonctions mathématiques ───────────────────────────────────────
        private Object funcAbs(List<Node> args) throws FormulaException {
            if (args.size() != 1) {
                throw new FormulaException(
                        "#ARGS! ABS requiert exactement 1 argument");
            }
            return Math.abs(toDouble(evaluate(args.get(0)), "ABS"));
        }

        private Object funcInt(List<Node> args) throws FormulaException {
            if (args.size() != 1) {
                throw new FormulaException(
                        "#ARGS! INT requiert exactement 1 argument");
            }
            return Math.floor(toDouble(evaluate(args.get(0)), "INT"));
        }

        private Object funcMod(List<Node> args) throws FormulaException {
            if (args.size() != 2) {
                throw new FormulaException(
                        "#ARGS! MOD requiert exactement 2 arguments");
            }
            double a = toDouble(evaluate(args.get(0)), "MOD");
            double b = toDouble(evaluate(args.get(1)), "MOD");
            if (b == 0) {
                throw new FormulaException("#DIV/0! MOD par zéro");
            }
            double result = a % b;
            return result == 0 ? 0.0 : result;
        }

        private Object funcRound(List<Node> args) throws FormulaException {
            if (args.size() != 2) {
                throw new FormulaException(
                        "#ARGS! ROUND requiert exactement 2 arguments");
            }
            double value = toDouble(evaluate(args.get(0)), "ROUND");
            double decimals = toDouble(evaluate(args.get(1)), "ROUND");
            int dec = (int) decimals;

            if (dec > 0) {
                double factor = Math.pow(10, dec);
                return Math.round(value * factor) / factor;
            } else if (dec == 0) {
                return (double) Math.round(value);
            } else {
                double factor = Math.pow(10, -dec);
                return Math.floor(value / factor) * factor;
            }
        }

        private Object funcSign(List<Node> args) throws FormulaException {
            if (args.size() != 1) {
                throw new FormulaException(
                        "#ARGS! SIGN requiert exactement 1 argument");
            }
            double v = toDouble(evaluate(args.get(0)), "SIGN");
            return v > 0 ? 1.0 : v < 0 ? -1.0 : 0.0;
        }

        // ── Fonctions utilitaires ─────────────────────────────────────────
        private Object funcDefined(List<Node> args) throws FormulaException {
            if (args.size() != 1) {
                throw new FormulaException(
                        "#ARGS! DEFINED requiert exactement 1 argument");
            }
            try {
                Object v = evaluate(args.get(0));
                return (v != null) ? 1.0 : 0.0;
            } catch (FormulaException e) {
                return 0.0;
            }
        }

        // ── Collecte de valeurs ───────────────────────────────────────────
        /**
         * Collecte toutes les valeurs numériques depuis une liste d'arguments.
         * Gère les scalaires, plages et arguments de position.
         */
        List<Double> collectAllValues(List<Node> args)
                throws FormulaException {
            List<Double> values = new ArrayList<>();

            for (Node arg : args) {
                if (arg instanceof RangeNode rangeNode) {
                    values.addAll(rangeNode.collectValues(this));
                } else if (arg instanceof PositionArgNode posNode) {
                    values.addAll(posNode.collectValues(this));
                } else {
                    Object v = evaluate(arg);
                    Double d = tryParseDouble(v);
                    if (d != null) {
                        values.add(d);
                    }
                }
            }

            return values;
        }

        /**
         * Collecte les valeurs dans une direction de position. LEFT = cellules
         * à gauche sur la même ligne RIGHT = cellules à droite sur la même
         * ligne ABOVE = cellules au-dessus sur la même colonne BELOW = cellules
         * en-dessous sur la même colonne
         */
        List<Double> collectPositionValues(String direction) {
            List<Double> values = new ArrayList<>();

            switch (direction) {
                case "LEFT" -> {
                    for (int c = 0; c < formulaCol; c++) {
                        Double v = getNumericCellValue(formulaRow, c);
                        if (v != null) {
                            values.add(v);
                        }
                    }
                }
                case "RIGHT" -> {
                    for (int c = formulaCol + 1;
                            c < model.getColumnCount(); c++) {
                        Double v = getNumericCellValue(formulaRow, c);
                        if (v != null) {
                            values.add(v);
                        }
                    }
                }
                case "ABOVE" -> {
                    for (int r = 0; r < formulaRow; r++) {
                        Double v = getNumericCellValue(r, formulaCol);
                        if (v != null) {
                            values.add(v);
                        }
                    }
                }
                case "BELOW" -> {
                    for (int r = formulaRow + 1;
                            r < model.getRowCount(); r++) {
                        Double v = getNumericCellValue(r, formulaCol);
                        if (v != null) {
                            values.add(v);
                        }
                    }
                }
            }

            return values;
        }

        // ── Accès aux données du tableau ──────────────────────────────────
        /**
         * Retourne la valeur d'une cellule comme Object. Lève une exception si
         * les coordonnées sont hors limites.
         */
        Object getCellValue(int row, int col) throws FormulaException {
            if (row < 0 || row >= model.getRowCount()
                    || col < 0 || col >= model.getColumnCount()) {
                throw new FormulaException(
                        "#REF! Cellule hors limites : R"
                        + (row + 1) + "C" + (col + 1));
            }
            return model.getValueAt(row, col);
        }

        /**
         * Retourne la valeur numérique d'une cellule. Retourne null si vide ou
         * non numérique — pas d'exception.
         */
        Double getNumericCellValue(int row, int col) {
            if (row < 0 || row >= model.getRowCount()
                    || col < 0 || col >= model.getColumnCount()) {
                return null;
            }
            // Ignorer la cellule de la formule elle-même
            if (row == formulaRow && col == formulaCol) {
                return null;
            }
            Object val = model.getValueAt(row, col);
            return tryParseDouble(val);
        }

        // ── Utilitaires ───────────────────────────────────────────────────
        /**
         * Convertit un Object en double. Lève FormulaException si impossible.
         */
        double toDouble(Object obj, String context) throws FormulaException {
            if (obj instanceof Number n) {
                return n.doubleValue();
            }
            if (obj instanceof String s) {
                try {
                    return Double.parseDouble(s.trim());
                } catch (NumberFormatException e) {
                    throw new FormulaException(
                            "#VALUE! Valeur non numérique dans " + context
                            + " : '" + s + "'");
                }
            }
            throw new FormulaException(
                    "#VALUE! Impossible de convertir en nombre : " + obj);
        }

        /**
         * Tente de convertir un Object en Double. Retourne null si impossible
         * sans exception.
         */
        Double tryParseDouble(Object val) {
            if (val == null) {
                return null;
            }
            if (val instanceof Number n) {
                return n.doubleValue();
            }
            try {
                return Double.parseDouble(val.toString().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * Exception interne du moteur de formules. Convertie en String d'erreur
     * dans evaluate() — jamais exposée.
     */
    static class FormulaException extends Exception {

        FormulaException(String message) {
            super(message);
        }
    }

    /**
     * Constructeur privé — classe utilitaire non instanciable.
     */
    private HTableFormula() {
    }

    /**
     * Évalue une formule et retourne son résultat.
     *
     * @param formula la formule brute (ex: "=SUM(A1:A5)"), doit commencer par
     * "="
     * @param model le modèle de données du tableau
     * @param formulaRow ligne de la cellule contenant la formule (0-indexée)
     * @param formulaCol colonne de la cellule contenant la formule (0-indexée)
     * @return un Double si le calcul réussit, ou un String d'erreur (ex:
     * "#REF!")
     */
    public static Object evaluate(String formula,
            HSuperDefaultTableModel model,
            int formulaRow, int formulaCol) {

        if (formula == null || formula.isEmpty()) {
            return null;
        }

        // Retirer le "=" de tête
        String expr = formula.startsWith("=")
                ? formula.substring(1).trim()
                : formula.trim();

        if (expr.isEmpty()) {
            return null;
        }

        try {
            Lexer lexer = new Lexer(expr);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            Node ast = parser.parse();

            Evaluator evaluator = new Evaluator(model, formulaRow, formulaCol);
            return evaluator.evaluate(ast);

        } catch (FormulaException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "#ERROR! " + e.getMessage();
        }
    }

    /**
     * Surcharge de compatibilité avec l'ancienne signature. Utilisée par le
     * code existant qui ne connaît pas la position de la cellule — les
     * arguments de position ne fonctionneront pas.
     *
     * @param formula la formule
     * @param model le modèle
     * @return résultat ou erreur
     */
    public static Object evaluate(String formula,
            HSuperDefaultTableModel model) {
        return evaluate(formula, model, -1, -1);
    }

    /**
     * Convertit un index de colonne 0-basé en notation alphabétique. 0 → "A",
     * 25 → "Z", 26 → "AA"
     *
     * @param index l'index 0-basé
     * @return la notation alphabétique correspondante
     */
    public static String colIndexToLetter(int index) {
        StringBuilder sb = new StringBuilder();
        index++;
        while (index > 0) {
            index--;
            sb.insert(0, (char) ('A' + (index % 26)));
            index /= 26;
        }
        return sb.toString();
    }

    /**
     * Convertit une notation alphabétique de colonne en index 0-basé. "A" → 0,
     * "Z" → 25, "AA" → 26
     *
     * @param colStr la notation alphabétique
     * @return l'index 0-basé
     */
    public static int colLetterToIndex(String colStr) {
        return Parser.colLetterToIndex(colStr);
    }
}
