/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DOM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class NameRegister {
        
    private HashMap<Character, ArrayList<Integer>> colonnes;

    public NameRegister() {
    }

    // Ajouter un index en fonction du nom
    public void addNode(String nom, int index) {
        if (nom == null || nom.isEmpty()) {
            //Message d'erreur
        }
        char initial = Character.toUpperCase(nom.charAt(0));
        colonnes.computeIfAbsent(initial, k -> new ArrayList<>()).add(index);
    }

    // Récupérer les indices d'une lettre donnée
    public ArrayList<Integer> getNodes(char lettre) {
        return colonnes.getOrDefault(Character.toUpperCase(lettre), null);
    }

    // Supprimer un index dans une colonne
    public void remove(String nom, int index) {
        char initial = Character.toUpperCase(nom.charAt(0));
        List<Integer> liste = colonnes.get(initial);
        if (liste != null) {
            liste.remove(index);
        }        
    }       
}
