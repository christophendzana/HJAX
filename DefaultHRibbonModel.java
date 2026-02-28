package rubban;

import java.util.ArrayList;

/**
 * Implémentation par défaut de HRibbonModel.
 * Stocke les données dans des ArrayList.
 * 
 * Cette classe contient la structure de données réelle :
 * - groupIdentifiers : les noms/identifiants des groupes
 * - dataGroup : les valeurs pour chaque groupe (index parallèles)
 * 
 * Analogie : similaire à DefaultTableModel pour JTable.
 * 
 * @author FIDELE
 */
public class DefaultHRibbonModel extends AbstractHRibbonModel {
    
    // =========================================================================
    // ATTRIBUTS (STRUCTURE DE DONNÉES)
    // =========================================================================
    
    /**
     * Stockage des données par groupe.
     * Chaque élément de cette liste correspond à un groupe et contient
     * la liste des valeurs de ce groupe.
     * L'index dans cette liste est parallèle à groupIdentifiers.
     */
    private ArrayList<ArrayList<Object>> dataGroup;
    
    /**
     * Identifiants des groupes (peut contenir des doublons).
     * L'index dans cette liste est parallèle à dataGroup.
     */
    private ArrayList<Object> groupIdentifiers;
    
    /**
     * Compteur de version du modèle.
     * Incrémenté à chaque modification pour permettre la détection des changements.
     */
    private long version = 0;
    
    
    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    
    /**
     * Constructeur par défaut.
     * Crée un modèle vide sans groupe ni donnée.
     */
    public DefaultHRibbonModel() {
        dataGroup = new ArrayList<>();
        groupIdentifiers = new ArrayList<>();
    }
    
    /**
     * Constructeur avec identifiants de groupes initiaux.
     * Crée des groupes vides avec les identifiants fournis.
     * 
     * @param groupIdentifiers liste des identifiants de groupes
     */
    public DefaultHRibbonModel(ArrayList<Object> groupIdentifiers) {
        this(); // Appelle le constructeur par défaut pour initialiser les listes
        
        if (groupIdentifiers != null) {
            for (Object groupIdentifier : groupIdentifiers) {
                addGroup(groupIdentifier);
            }
        }
    }
    
    /**
     * Constructeur avec identifiants et données.
     * Crée un modèle avec les groupes et valeurs fournis.
     * 
     * @param groupIdentifiers liste des identifiants de groupes
     * @param dataGroup données correspondantes pour chaque groupe
     * @throws IllegalArgumentException si les listes sont null ou de tailles différentes
     */
    public DefaultHRibbonModel(ArrayList<Object> groupIdentifiers, ArrayList<ArrayList<Object>> dataGroup) {
        if (groupIdentifiers == null || dataGroup == null) {
            throw new IllegalArgumentException("groupIdentifiers and dataGroup cannot be null");
        }
        if (groupIdentifiers.size() != dataGroup.size()) {
            throw new IllegalArgumentException("groupIdentifiers and dataGroup must have the same size");
        }
        
        // Initialisation avec les données fournies (copie défensive)
        this.groupIdentifiers = new ArrayList<>(groupIdentifiers);
        this.dataGroup = new ArrayList<>();
        
        for (ArrayList<Object> group : dataGroup) {
            // Copie défensive de chaque groupe
            this.dataGroup.add(new ArrayList<>(group));
        }
    }
    
    
    // =========================================================================
    // IMPLÉMENTATION DES MÉTHODES ABSTRAITES - GESTION DES GROUPES
    // =========================================================================
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount() {
        return groupIdentifiers.size();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getGroupIdentifier(int groupIndex) {
        checkGroupIndex(groupIndex);
        return groupIdentifiers.get(groupIndex);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addGroup(Object groupIdentifier) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException("Group identifier cannot be null");
        }
        
        groupIdentifiers.add(groupIdentifier);
        dataGroup.add(new ArrayList<>());
        version++;
        
        fireGroupAdded(groupIdentifiers.size() - 1);
    }
    
    /**
     * Supprime le groupe à l'index spécifié.
     * 
     * @param groupIndex l'index du groupe à supprimer
     */
    public void removeGroup(int groupIndex) {
        checkGroupIndex(groupIndex);
        
        groupIdentifiers.remove(groupIndex);
        dataGroup.remove(groupIndex);
        version++;
        
        fireGroupRemoved(groupIndex);
    }
    
    /**
     * Supprime le premier groupe ayant l'identifiant spécifié.
     * 
     * @param groupIdentifier l'identifiant du groupe à supprimer
     */
    public void removeGroup(Object groupIdentifier) {
        int index = findGroupIndex(groupIdentifier);
        if (index >= 0) {
            removeGroup(index);
        }
    }
    
    /**
     * Vérifie si un groupe avec l'identifiant spécifié existe.
     * 
     * @param groupIdentifier l'identifiant à rechercher
     * @return true si le groupe existe
     */
    public boolean containsGroup(Object groupIdentifier) {
        return findGroupIndex(groupIdentifier) >= 0;
    }
    
    
    // =========================================================================
    // IMPLÉMENTATION DES MÉTHODES ABSTRAITES - ACCÈS AUX VALEURS PAR INDEX
    // =========================================================================
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getValueCount(int groupIndex) {
        checkGroupIndex(groupIndex);
        return dataGroup.get(groupIndex).size();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int position, int groupIndex) {
        checkGroupIndex(groupIndex);
        checkPosition(position, groupIndex);
        
        return dataGroup.get(groupIndex).get(position);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int position, int groupIndex) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        checkGroupIndex(groupIndex);
        checkPosition(position, groupIndex);
        
        dataGroup.get(groupIndex).set(position, value);
        version++;
        
        fireValueUpdated(groupIndex, position);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
public void addValue(Object value, int groupIndex) {
    if (value == null) {
        throw new IllegalArgumentException("Value cannot be null");
    }
    
    if (groupIndex < 0) {
        throw new IllegalArgumentException("groupIndex cannot be negative");
    }
    
    if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
        ArrayList<Object> group = dataGroup.get(groupIndex);
        group.add(value);
        
        version++;
        fireValueAdded(groupIndex, group.size() - 1);
    }
}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void insertValueAt(Object value, int position, int groupIndex) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        checkGroupIndex(groupIndex);
        
        ArrayList<Object> group = dataGroup.get(groupIndex);
        if (position < 0 || position > group.size()) {
            throw new IndexOutOfBoundsException(
                "Position: " + position + " must be between 0 and " + group.size());
        }
        
        group.add(position, value);
        version++;
        
        fireValueAdded(groupIndex, position);
    }
    
    
    // =========================================================================
    // MÉTHODES ADDITIONNELLES DE MANIPULATION DES DONNÉES
    // =========================================================================
    
    /**
     * Supprime la valeur à la position spécifiée dans le groupe à l'index spécifié.
     * 
     * @param position la position de la valeur à supprimer
     * @param groupIndex l'index du groupe
     */
    public void removeValueAt(int position, int groupIndex) {
    if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
        ArrayList<Object> group = dataGroup.get(groupIndex);
        if (position >= 0 && position < group.size()) {
            group.remove(position);
            version++;
            fireValueRemoved(groupIndex, position);
        }
    }
}
    
    public void removeValueAt(int position, Object groupIdentifier) {
    int index = findFirstGroupIndex(groupIdentifier); // ou getIndexGroupIdentifier
    if (index >= 0) {
        removeValueAt(position, index);
    }
}
    public int findFirstGroupIndex(Object groupIdentifier) {
    if (groupIdentifier == null) {
        return -1;
    }
    for (int i = 0; i < groupIdentifiers.size(); i++) {
        if (groupIdentifier.equals(groupIdentifiers.get(i))) {
            return i;
        }
    }
    return -1;
}
    
    /**
     * Supprime la première occurrence de la valeur dans le groupe à l'index spécifié.
     * 
     * @param value la valeur à supprimer
     * @param groupIndex l'index du groupe
     * @return true si la valeur a été supprimée
     */
    public boolean removeValue(Object value, int groupIndex) {
        if (value == null) {
            return false;
        }
        
        checkGroupIndex(groupIndex);
        
        ArrayList<Object> group = dataGroup.get(groupIndex);
        int position = group.indexOf(value);
        
        if (position >= 0) {
            group.remove(position);
            version++;
            fireValueRemoved(groupIndex, position);
            return true;
        }
        
        return false;
    }
    
    /**
     * Supprime la première occurrence de la valeur dans le groupe ayant l'identifiant spécifié.
     * 
     * @param value la valeur à supprimer
     * @param groupIdentifier l'identifiant du groupe
     * @return true si la valeur a été supprimée
     */
    public boolean removeValue(Object value, Object groupIdentifier) {
        int groupIndex = findGroupIndex(groupIdentifier);
        if (groupIndex >= 0) {
            return removeValue(value, groupIndex);
        }
        return false;
    }
    
    /**
     * Supprime toutes les valeurs du groupe à l'index spécifié.
     * 
     * @param groupIndex l'index du groupe à vider
     */
    public void removeAllValues(int groupIndex) {
        checkGroupIndex(groupIndex);
        
        ArrayList<Object> group = dataGroup.get(groupIndex);
        if (!group.isEmpty()) {
            group.clear();
            version++;
            fireGroupValuesChanged(groupIndex);
        }
    }
    
    /**
     * Supprime toutes les valeurs du groupe ayant l'identifiant spécifié.
     * 
     * @param groupIdentifier l'identifiant du groupe à vider
     */
    public void removeAllValues(Object groupIdentifier) {
        int groupIndex = findGroupIndex(groupIdentifier);
        if (groupIndex >= 0) {
            removeAllValues(groupIndex);
        }
    }
    
    /**
     * Vérifie si le groupe à l'index spécifié contient la valeur.
     * 
     * @param value la valeur à rechercher
     * @param groupIndex l'index du groupe
     * @return true si la valeur existe dans le groupe
     */
    public boolean containsValue(Object value, int groupIndex) {
        if (value == null) {
            return false;
        }
        
        checkGroupIndex(groupIndex);
        return dataGroup.get(groupIndex).contains(value);
    }
    
    /**
     * Vérifie si le groupe ayant l'identifiant spécifié contient la valeur.
     * 
     * @param value la valeur à rechercher
     * @param groupIdentifier l'identifiant du groupe
     * @return true si la valeur existe dans le groupe
     */
    public boolean containsValue(Object value, Object groupIdentifier) {
        int groupIndex = findGroupIndex(groupIdentifier);
        if (groupIndex >= 0) {
            return containsValue(value, groupIndex);
        }
        return false;
    }
    
    
    // =========================================================================
    // IMPLÉMENTATION DE getVersion()
    // =========================================================================
    
    /**
     * {@inheritDoc}
     */
    @Override
    public long getVersion() {
        return version;
    }
    
    
    // =========================================================================
    // MÉTHODES DE VALIDATION (PRIVATE)
    // =========================================================================
    
    /**
     * Vérifie que l'index du groupe est valide.
     * 
     * @param groupIndex l'index à vérifier
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    private void checkGroupIndex(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= groupIdentifiers.size()) {
            throw new IndexOutOfBoundsException(
                "Group index: " + groupIndex + ", Size: " + groupIdentifiers.size());
        }
    }
    
    /**
     * Vérifie que la position est valide pour le groupe spécifié.
     * 
     * @param position la position à vérifier
     * @param groupIndex l'index du groupe
     * @throws IndexOutOfBoundsException si la position est invalide
     */
    private void checkPosition(int position, int groupIndex) {
        int size = dataGroup.get(groupIndex).size();
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException(
                "Position: " + position + ", Group size: " + size);
        }
    }
}