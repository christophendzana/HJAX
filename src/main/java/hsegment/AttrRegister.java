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
public class AttrRegister {
    
    private HashMap<String, ArrayList<Integer>> colonnes;

    public AttrRegister() {
    }

    // Ajouter un index en fonction de l'attribut
    public void addNode(String attr, int index) {
        if (attr == null || attr.isEmpty()) {
            //Message d'erreur
        }
        colonnes.computeIfAbsent(attr, k -> new ArrayList<>()).add(index);
    }

    // Récupérer les indices de 
    public ArrayList<Integer> getNodes(String attr) {
        return colonnes.getOrDefault(attr, null);
    }

    // Supprimer un index dans une colonne
    public void remove(String attr, int index) {
        List<Integer> liste = colonnes.get(attr);
        if (liste != null) {
            liste.remove(index);
        }        
    }       
    
}
