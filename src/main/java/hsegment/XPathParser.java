/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package xpathAPI;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforme la chaine XPath en expresion comprehensible par le moteur
 *
 * @author FIDELE
 */
public class XPathParser {

    public ArrayList<XPathStep> parse(String expression) {
        ArrayList<XPathStep> steps = new ArrayList<>();

        if (expression == null || expression.isEmpty()) {
            return null;
        }

        // On découpe en / ou // ou tout ce qui n'est pas / 
        expression = expression.trim();

        Pattern pattern = Pattern.compile("//?|[^/]+");
        Matcher matcher = pattern.matcher(expression);
        ArrayList expressionPath = new ArrayList<>();

        while (matcher.find()) {
            expressionPath.add(matcher.group());
        }

        ArrayList<String> expressionStep = new ArrayList<>();

        for (int i = 1; i < expressionPath.size(); i += 2) {

            if (i < expressionPath.size()) {
                String operator = expressionPath.get(i - 1).toString();
                String step = expressionPath.get(i).toString();

                if ((operator.equals("/")) || operator.equals("//") && (!step.equals("/") || !step.equals("//"))) {
                    expressionStep.add(operator + "-" + step);
                }
            }
        }

        for (String step : expressionStep) {
            if (!step.isEmpty()) {
                String[] stp = step.split("-");

                if (getFiltersStep(stp[1]) != null) {
                    steps.add(new XPathStep(stp[0], getNameStep(step), getFiltersStep(stp[1])));
                } else {
                    steps.add(new XPathStep(stp[0], stp[1]));
                }
            }
        }

        return steps;
    }

    private String getNameStep(String expression) {
        Pattern pattern = Pattern.compile("^/(\\w+)");
        Matcher matcher = pattern.matcher("/" + expression);

        while (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private ArrayList<XPathFilter> getFiltersStep(String expression) {

        ArrayList<XPathFilter> filters = new ArrayList<>();

        if (expression == null || expression.trim().isEmpty()) {
            return filters;
        }

        // Pattern pour détecter les prédicats: [@attr='value'], [position()], [text()='value'], etc.
        Pattern predicatePattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = predicatePattern.matcher(expression);

        while (matcher.find()) {
            String predicateContent = matcher.group(1);
            ArrayList<XPathFilter> predicateFilters = parseComplexPredicate(predicateContent);
            filters.addAll(predicateFilters);
        }

        return filters;
    }

    private static ArrayList<XPathFilter> parseComplexPredicate(String predicate) {
        ArrayList<XPathFilter> filters = new ArrayList<>();

        if (predicate == null || predicate.trim().isEmpty()) {
            return filters;
        }

        predicate = predicate.trim();

        // Séparation des conditions avec opérateurs logiques
        String[] conditions = splitLogicalConditions(predicate);

        for (String condition : conditions) {
            XPathFilter filter = parseSingleCondition(condition.trim());
            if (filter != null) {
                filters.add(filter);
            }
        }

        return filters;
    }

    private static String[] splitLogicalConditions(String predicate) {
        ArrayList<String> conditions = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int parenDepth = 0; // suivi des niveaux de parenthèses

        for (int i = 0; i < predicate.length(); i++) {
            char c = predicate.charAt(i);

            // Gestion des guillemets
            if ((c == '"' || c == '\'') && (i == 0 || predicate.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }

            // Gestion des parenthèses (pour not() et autres fonctions)
            //on ne découpe pas les and et or qui sont dans les parenthèses 
            //et on modifie donc la position de parentDepth pour se situer
            if (!inQuotes) {
                if (c == '(') {
                    parenDepth++;
                } else if (c == ')') {
                    parenDepth--;
                }
            }

            // Vérification des opérateurs logiques seulement si :
            // - Pas dans des guillemets
            // - Pas dans des parenthèses (sauf niveau 0)
            // - Parenthèse fermante compte comme séparateur potentiel
            if (!inQuotes && parenDepth == 0) {

                // Vérification pour "and"
                if (i < predicate.length() - 3) {
                    String substring = predicate.substring(i, i + 3);
                    if ("and".equalsIgnoreCase(substring)) {
                        boolean isWholeWord = (i == 0 || !Character.isLetterOrDigit(predicate.charAt(i - 1)))
                                && (i + 3 >= predicate.length() || !Character.isLetterOrDigit(predicate.charAt(i + 3)));

                        if (isWholeWord) {
                            conditions.add(current.toString().trim());
                            current = new StringBuilder();
                            i += 2;
                            continue; // passe à l'itération suivante sans excécuter le reste du code donc on saute and
                        }
                    }
                }

                // Vérification pour "or"
                if (i < predicate.length() - 2) {
                    String substring = predicate.substring(i, i + 2);
                    if ("or".equalsIgnoreCase(substring)) {
                        boolean isWholeWord = (i == 0 || !Character.isLetterOrDigit(predicate.charAt(i - 1)))
                                && (i + 2 >= predicate.length() || !Character.isLetterOrDigit(predicate.charAt(i + 2)));

                        if (isWholeWord) {
                            conditions.add(current.toString().trim());
                            current = new StringBuilder();
                            i += 1;
                            continue;
                        }
                    }
                }
            }

            current.append(c);
        }

        if (current.length() > 0) {
            conditions.add(current.toString().trim());
        }

        return conditions.toArray(new String[0]);
    }

// Méthode utilitaire pour vérifier si c'est un mot entier
    private static boolean isWholeWord(String text, int start, int length) {
        // Vérifier le caractère avant
        boolean validStart = (start == 0) || !Character.isLetterOrDigit(text.charAt(start - 1));
        // Vérifier le caractère après
        boolean validEnd = (start + length >= text.length()) || !Character.isLetterOrDigit(text.charAt(start + length));

        return validStart && validEnd;
    }

    private static XPathFilter parseSingleCondition(String condition) {
        if (condition.isEmpty()) {
            return null;
        }

        // Détection du type de condition
        if (condition.startsWith("@")) {
            return parseAttributeCondition(condition);
        } else if (condition.contains("position()")) {
            return parsePositionCondition(condition);
        } else if (condition.contains("last()")) {
            return parseLastCondition(condition);
        } else if (condition.contains("text()")) {
            return parseTextCondition(condition);
        } else if (condition.matches("^\\d+$")) {
            return parseNumericPosition(condition);
        } else if (condition.contains("contains(")) {
            return parseContainsFunction(condition);
        } else if (condition.contains("starts-with(")) {
            return parseStartsWithFunction(condition);
        } else if (condition.contains("ends-with(")) {
            return parseEndsWithFunction(condition);
        } else if (condition.contains("count(")) {
            return parseCountFunction(condition);
        } else if (condition.contains("name(")) {
            return parseNameFunction(condition);
        } else if (condition.contains(condition)) {
            return parseNotFunction(condition);
        } else {
            return parseElementCondition(condition);
        }
    }

    private static XPathFilter parseAttributeCondition(String path) {
        // Formats: @name operator 'value', @name, @name operator value
        Pattern attrPattern = Pattern.compile("@(\\w+)(?:\\s*([=!<>]+)\\s*(?:['\"]([^'\"]*)['\"]|(\\d+(?:\\.\\d+)?)|(\\w+)))?");
        Matcher matcher = attrPattern.matcher(path);

        if (matcher.matches()) {
            String name = matcher.group(1);
            String operator = matcher.group(2) != null ? matcher.group(2) : "exist";
            String value;

            if (matcher.group(3) != null) {
                value = matcher.group(3);
            } else if (matcher.group(4) != null) {
                value = matcher.group(4);
            } else if (matcher.group(5) != null) {
                value = matcher.group(5);
            } else {
                value = null;
            }

            return new XPathFilter(name, operator, value, XPathFilter.Type.ATTRIBUTE);
        }

        return null;
    }

    private static XPathFilter parsePositionCondition(String condition) {
        // Formats: position() operator value, position() mod value operator value
        Pattern posPattern = Pattern.compile("position\\(\\)\\s*([=!<>]+)\\s*(\\d+)");
        Matcher matcher = posPattern.matcher(condition);

        if (matcher.matches()) {
            String operator = matcher.group(1);
            String value = matcher.group(2);
            return new XPathFilter("position", operator, value, XPathFilter.Type.POSITION);
        }

        // Position avec mod (ex: position() mod 2 = 0)
        Pattern modPattern = Pattern.compile("position\\(\\)\\s+mod\\s+(\\d+)\\s*([=!<>]+)\\s*(\\d+)");
        matcher = modPattern.matcher(condition);

        if (matcher.matches()) {
            String modValue = matcher.group(1);
            String operator = matcher.group(2);
            String value = matcher.group(3);
            return new XPathFilter("position-mod", operator, value + " (mod " + modValue + ")", XPathFilter.Type.POSITION);
        }

        return null;
    }

    private static XPathFilter parseLastCondition(String predicat) {
        // Formats: last(), position() = last()
        if (predicat.trim().equals("last()")) {
            return new XPathFilter("last", "=", "last()", XPathFilter.Type.POSITION);
        }

        return null;
    }

    private static XPathFilter parseTextCondition(String predicat) {
        // Formats: text() operator 'value', text()
        Pattern textPattern = Pattern.compile("text\\(\\)(?:\\s*([=!<>]+)\\s*['\"]([^'\"]*)['\"])?");
        Matcher matcher = textPattern.matcher(predicat);

        if (matcher.matches()) {
            String operator = matcher.group(1) != null ? matcher.group(1) : "exists";
            String value = matcher.group(2) != null ? matcher.group(2) : "";
            return new XPathFilter("text", operator, value, XPathFilter.Type.TEXT);
        }

        return null;
    }

    private static XPathFilter parseNumericPosition(String condition) {
        return new XPathFilter("position", "=", condition, XPathFilter.Type.POSITION);
    }

    private static XPathFilter parseContainsFunction(String predicat) {
        // Format: contains(@attr|text(), 'value')
        Pattern containsPattern = Pattern.compile("contains\\(\\s*(@?(\\w+)\\(\\)?)\\s*,\\s*['\"]([^'\"]*)['\"]\\s*\\)");
        Matcher matcher = containsPattern.matcher(predicat);

        if (matcher.matches()) {
            String target = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3);
            XPathFilter.Type type = target.startsWith("@") ? XPathFilter.Type.ATTRIBUTE
                    : target.equals("text()") ? XPathFilter.Type.TEXT : XPathFilter.Type.ELEMENT;

            return new XPathFilter(name, "contains", value, type);
        }

        return null;
    }

    private static XPathFilter parseStartsWithFunction(String predicat) {
        // Format: starts-with(@attr|text(), 'value')
        Pattern startsWithPattern = Pattern.compile("starts-with\\(\\s*(@?(\\w+)\\(\\)?)\\s*,\\s*['\"]([^'\"]*)['\"]\\s*\\)");
        Matcher matcher = startsWithPattern.matcher(predicat);

        if (matcher.matches()) {
            String target = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3);
            XPathFilter.Type type = target.startsWith("@") ? XPathFilter.Type.ATTRIBUTE
                    : target.equals("text()") ? XPathFilter.Type.TEXT : XPathFilter.Type.ELEMENT;

            return new XPathFilter(name, "starts-with", value, type);
        }

        return null;
    }

    private static XPathFilter parseEndsWithFunction(String predicat) {
        // Format: ends-with(@attr|text(), 'value')
        Pattern endsWithPattern = Pattern.compile("ends-with\\(\\s*(@?(\\w+)\\(\\)?)\\s*,\\s*['\"]([^'\"]*)['\"]\\s*\\)");
        Matcher matcher = endsWithPattern.matcher(predicat);

        if (matcher.matches()) {
            String target = matcher.group(1);
            String name = matcher.group(2);
            String value = matcher.group(3);
            XPathFilter.Type type = target.startsWith("@") ? XPathFilter.Type.ATTRIBUTE
                    : target.equals("text()") ? XPathFilter.Type.TEXT : XPathFilter.Type.ELEMENT;

            return new XPathFilter(name, "ends-with", value, type);
        }

        return null;
    }

    private static XPathFilter parseNotFunction(String condition) {
        // Format: not(condition)
        Pattern notPattern = Pattern.compile("not\\(\\s*(.*)\\s*\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = notPattern.matcher(condition.trim());

        if (matcher.matches()) {
            String innerCondition = matcher.group(1);
            XPathFilter innerFilter = parseSingleCondition(innerCondition);

            if (innerFilter != null) {
                // Inverser l'opérateur ou ajouter un préfixe "not"
                String newOperator = "not-" + innerFilter.getOperator();
                return new XPathFilter(innerFilter.getName(), newOperator,
                        innerFilter.getValue(), innerFilter.getType());
            }
        }
        return null;
    }

    private static XPathFilter parseCountFunction(String predicat) {
        // Format: count(element) operator value
        Pattern countPattern = Pattern.compile("count\\(\\s*([\\w/*]+)\\s*\\)\\s*([=!<>]+)\\s*(\\d+)");
        Matcher matcher = countPattern.matcher(predicat);

        if (matcher.matches()) {
            String element = matcher.group(1);
            String operator = matcher.group(2);
            String value = matcher.group(3);
            return new XPathFilter(element, operator, value, XPathFilter.Type.COUNT);
        }

        return null;
    }

    private static XPathFilter parseNameFunction(String predicat) {
        // Format: name() = 'element', name() != 'element'
        Pattern namePattern = Pattern.compile("name\\(\\)\\s*([=!]+)\\s*['\"]([^'\"]*)['\"]");
        Matcher matcher = namePattern.matcher(predicat);

        if (matcher.matches()) {
            String operator = matcher.group(1);
            String value = matcher.group(2);
            return new XPathFilter("name", operator, value, XPathFilter.Type.ELEMENT);
        }

        return null;
    }

    private static XPathFilter parseElementCondition(String predicat) {
        // Format: element operator 'value'
        Pattern elementPattern = Pattern.compile("(\\w+)\\s*([=!<>]+)\\s*['\"]([^'\"]*)['\"]");
        Matcher matcher = elementPattern.matcher(predicat);

        if (matcher.matches()) {
            String name = matcher.group(1);
            String operator = matcher.group(2);
            String value = matcher.group(3);
            return new XPathFilter(name, operator, value, XPathFilter.Type.ELEMENT);
        }

        return null;
    }

}
