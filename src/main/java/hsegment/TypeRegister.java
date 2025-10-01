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
public class TypeRegister {
 
      private HashMap<String, ArrayList<Integer>> colonnes;

    public TypeRegister() {
    }

    // Ajouter un index en fonction du nom
    public void addNode(String type, int index) {
        if (type == null || type.isEmpty()) {
            //Message d'erreur
        }
        colonnes.computeIfAbsent(type, k -> new ArrayList<>()).add(index);
    }

    // Récupérer les indices d'une lettre donnée
    public ArrayList<Integer> getNodes(String type) {
        return colonnes.getOrDefault(type, null);
    }

    // Supprimer un index dans une colonne
    public void remove(String type, int index) {
        List<Integer> liste = colonnes.get(type);
        if (liste != null) {
            liste.remove(index);
        }        
    }       
    
}
