/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

import hsegment.JObject.Swing.Text.xml.Parser;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import Interface.XPathEventListener;
import javax.management.modelmbean.XMLParseException;

/**
 *
 * XpathParser lit une expression XPath caractère par caractère et génère des
 * événements selon les éléments détectés : - Nœud simple - Nœud avec prédicat -
 * Nœud avec fonction Les erreurs de syntaxe sont signalées via des exceptions.
 *
 * @author FIDELE
 */
public class XPathParser extends Parser {

    private XPathEventListener listener;  // Le processeur qui écoutera les événements
    private int position;                 // Position du caractère en cours de lecture

    public XPathParser(XPathEventListener listener) {
        this.listener = listener;
        this.position = 0;
    }

    @Override
    public void parse(Reader in) throws Exception {
        int currentChar;
        int depth = -1;      // 0 pour '/' et 1 pour '//'
        StringBuilder buffer = new StringBuilder();

        while ((currentChar = in.read()) != -1) {
            position++;
            char c = (char) currentChar;

            switch (c) {
                case '/':
                    // Si on rencontre un slash, il faut vérifier s'il y a un deuxième slash
                    in.mark(1);
                    int next = in.read();
                    position++;
                    if (next == '/') {
                        depth = 1; // double slash
                    } else {
                        depth = 0; // simple slash
                        in.reset(); // on revient car ce n'était pas un deuxième slash
                        position--;
                    }
                    buffer.setLength(0); // reset du buffer pour le nom du nœud
                    break;

                case '[':
                    
                    StringBuilder innerBuffer = new StringBuilder();
                    int innerChar;

                    // Lire tout ce qu’il y a entre [ et ]
                    while ((innerChar = in.read()) != -1 && innerChar != ']') {
                        innerBuffer.append((char) innerChar);
                        position++;
                    }

                    // Supprimer les espaces inutiles autour
                    String inside = innerBuffer.toString().trim();

                    // Vérification du contenu
                    if (inside.isEmpty()) {
                        throw new XpathSyntaxException("Erreur de syntaxe : crochets vides à la position " + position);
                    }

                    // Détection du type de contenu à l'intérieur des crochets
                    char first = inside.charAt(0);

                    switch (first) {
                        case '@':
                            // Cas d'un prédicat
                            // Exemple : [@category='java']
                            listener.onPredicate(
                                    new NodeEvent(
                                            buffer.toString().trim(), // nom du nœud courant
                                            inside, // prédicat brut
                                            depth // profondeur (0 = /, 1 = //)
                                    )
                            );
                            break;

                        default:
                            // Cas d'une fonction (ex: [contains(title,'Java')])
                            if (inside.contains("(") && inside.endsWith(")")) {
                                int open = inside.indexOf('(');
                                int close = inside.lastIndexOf(')');

                                if (open > 0 && close > open) {
                                    // Nom de la fonction : avant la parenthèse
                                    String functionName = inside.substring(0, open).trim();

                                    if (!XPathFunctions.isRecognized(functionName)) {
                                        throw new XMLParseException("Fonction XPath inconnue: " + functionName );
                                    }
                                    
                                    // Arguments : entre les parenthèses
                                    String argsPart = inside.substring(open + 1, close).trim();
                                    List<String> args = new ArrayList<>();

                                    if (!argsPart.isEmpty()) {
                                        // Découper les arguments séparés par des virgules
                                        for (String arg : argsPart.split("\\s*,\\s*")) {
                                            args.add(arg.trim());
                                        }
                                    }

                                    // Émettre un événement de Node avec fonction
                                    listener.onFunction(
                                            new NodeEvent(
                                                    buffer.toString().trim(), // nom du nœud
                                                    functionName, // nom de la fonction
                                                    args, // arguments de la fonction
                                                    depth // profondeur
                                            )
                                    );
                                } else {
                                    throw new XpathSyntaxException(
                                            "Erreur de syntaxe dans la fonction : " + inside + " à la position " + position
                                    );
                                }
                            } else {
                                // aucune parenthèse -> contenu non reconnu
                                throw new XpathSyntaxException(
                                        "Expression inconnue dans les crochets : " + inside + " à la position " + position
                                );
                            }
                    }

                    // 🔹 4. On réinitialise le buffer après traitement
                    buffer.setLength(0);
                    break;

                case '(':
                    // Début d’une fonction
                    String functionName = buffer.toString().trim();
                    if (functionName.isEmpty()) {
                        throw new XpathSyntaxException("Nom de fonction manquant avant la parenthèse", position);
                    }
                    List<String> args = readFunctionArgs(in);
                    listener.onFunction(new NodeEvent(functionName, functionName, args, depth));
                    buffer.setLength(0);
                    break;

                case ']':
                case ')':
                    // Ces caractères ne devraient pas apparaître seuls ici
                    throw new XpathSyntaxException("Caractère inattendu : '" + c + "'", position);

                case ' ':
                    // Ignorer les espaces inutiles (hors prédicat/fonction)
                    break;

                default:
                    // Lecture normale du nom de nœud
                    buffer.append(c);

                    // On regarde si le prochain caractère est un séparateur
                    in.mark(1);
                    int lookahead = in.read();
                    position++;
                    if (lookahead == '/' || lookahead == -1) {
                        String nodeName = buffer.toString().trim();
                        if (!nodeName.isEmpty()) {
                            listener.onNode(new NodeEvent(nodeName, depth));
                        }
                        buffer.setLength(0);
                    } else {
                        in.reset();
                        position--;
                    }
                    break;
            }
        }

        // Si un nom de nœud reste en mémoire après la boucle
        if (buffer.length() > 0) {
            String nodeName = buffer.toString().trim();
            if (!nodeName.isEmpty()) {
                listener.onNode(new NodeEvent(nodeName, depth));
            }
        }
    }

//    /**
//     * Lit le contenu d’un prédicat jusqu’à la fermeture du crochet ']'.
//     */
//    private String readPredicate(Reader in) throws IOException, XpathSyntaxException {
//        StringBuilder predicate = new StringBuilder();
//        int c;
//        while ((c = in.read()) != -1) {
//            position++;
//            if ((char) c == ']') {
//                return predicate.toString().trim();
//            }
//            predicate.append((char) c);
//        }
//        throw new XpathSyntaxException("Prédicat non fermé avec ']'", position);
//    }

    /**
     * Lit les arguments d’une fonction jusqu’à la parenthèse de fermeture ')'.
     */
    private List<String> readFunctionArgs(Reader in) throws IOException, XpathSyntaxException {
        List<String> args = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        int c;
        boolean inString = false;

        while ((c = in.read()) != -1) {
            position++;
            char ch = (char) c;

            switch (ch) {
                case '\'':
                case '"':
                    inString = !inString;
                    currentArg.append(ch);
                    break;
                case ',':
                    if (!inString) {
                        args.add(currentArg.toString().trim());
                        currentArg.setLength(0);
                    } else {
                        currentArg.append(ch);
                    }
                    break;
                case ')':
                    if (inString) {
                        throw new XpathSyntaxException("Chaîne non fermée avant ')'", position);
                    }
                    args.add(currentArg.toString().trim());
                    return args;
                default:
                    currentArg.append(ch);
                    break;
            }
        }
        throw new XpathSyntaxException("Fonction non fermée avec ')'", position);
    }

}
