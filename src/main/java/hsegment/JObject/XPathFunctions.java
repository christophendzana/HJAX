/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author FIDELE
 */
public class XPathFunctions {
    
    /**
     * Chaque fonction est enregistrée avec son nombre d'arguments attendu.
     * Exemple : contains(node, value) → 2 arguments
     */
    private static final Map<String, Integer> SUPPORTED_FUNCTIONS = new HashMap<>();

    // --- Initialisation statique des fonctions reconnues ---
    static {
        SUPPORTED_FUNCTIONS.put("text", 0);
        SUPPORTED_FUNCTIONS.put("contains", 2);
        SUPPORTED_FUNCTIONS.put("starts-with", 2);
        SUPPORTED_FUNCTIONS.put("ends-with", 2);
        SUPPORTED_FUNCTIONS.put("position", 0);
        SUPPORTED_FUNCTIONS.put("last", 0);
    }

    /**
     * Vérifie si une fonction est reconnue par le parser.
     * @param name Nom de la fonction (ex : "contains")
     * @return true si la fonction est supportée
     */
    public static boolean isRecognized(String name) {
        return SUPPORTED_FUNCTIONS.containsKey(name);
    }

    /**
     * Retourne le nombre d'arguments attendus pour une fonction donnée.
     * @param name Nom de la fonction
     * @return Nombre d'arguments, ou -1 si la fonction n'est pas reconnue
     */
    public static int getArgCount(String name) {
        return SUPPORTED_FUNCTIONS.getOrDefault(name, -1);
    }

    /**
     * Retourne la liste complète des fonctions supportées.
     * @return Map nom → nombre d'arguments
     */
    public static Map<String, Integer> getSupportedFunctions() {
        return SUPPORTED_FUNCTIONS;
    }
    
}
