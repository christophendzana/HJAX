/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

import hcomponents.HRibbon.HRibbonModelEvents.HRibbonEventBus;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEvent;
import hcomponents.HRibbon.HRibbonModelEvents.HRibbonModelEventListener;
import java.util.ArrayList;
import javax.swing.event.EventListenerList;

/**
 * Implémentation par défaut de HRibbonModel.
 *
 *
 * @author FIDELE
 */
public class DefaultHRibbonModel implements HRibbonModel {

    /**
     * Stockage des données par groupe (index parallèle à groupIdentifiers)
     */
    private ArrayList<ArrayList<Object>> dataGroup;

    /**
     * Noms des groupes (peut contenir des doublons)
     */
    private ArrayList<Object> groupIdentifiers;

    private HRibbonEventBus eventBus = new HRibbonEventBus();

    /**
     * Liste des listeners
     */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Constructeur par défaut. Crée un modèle vide.
     */
    public DefaultHRibbonModel() {
        dataGroup = new ArrayList<>();
        groupIdentifiers = new ArrayList<>();
    }

    /**
     * Constructeur avec noms de groupes initiaux. Crée des groupes vides avec
     * les noms fournis.
     *
     * @param groupIdentifiers liste des noms de groupes
     */
    public DefaultHRibbonModel(ArrayList<String> groupIdentifiers) {
        if (groupIdentifiers != null) {
            for (String groupIdentifier : groupIdentifiers) {
                addGroup(groupIdentifier);
            }
        }
    }

    /**
     * Constructeur avec noms et données.
     *
     * @param groupIdentifiers liste des noms de groupes
     * @param dataGroup données correspondantes
     * @throws IllegalArgumentException si les tailles ne correspondent pas
     */
    public DefaultHRibbonModel(ArrayList<String> groupIdentifiers, ArrayList<ArrayList<Object>> dataGroup) {
        if (groupIdentifiers == null || dataGroup == null) {
            throw new IllegalArgumentException("GroupNames and dataGroup cannot be null");
        }
        if (groupIdentifiers.size() != dataGroup.size()) {
            throw new IllegalArgumentException("GroupNames and dataGroup must have the same size");
        }

        this.groupIdentifiers = new ArrayList<>(groupIdentifiers);
        this.dataGroup = new ArrayList<>();
        for (ArrayList<Object> group : dataGroup) {
            this.dataGroup.add(new ArrayList<>(group));
        }
    }

    @Override
    public int getGroupCount() {
        return groupIdentifiers.size();
    }

    @Override
    public int getValueCount(Object groupIdentifier) {
        return getValueCount(getIndexGroupIdentifier(groupIdentifier));
    }

    public int getIndexGroupIdentifier(Object groupIdentifier) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException(" groupIdentifier cannot be null ");
        }
        int index = 0;
        for (; index < groupIdentifiers.size(); index++) {
            if (groupIdentifiers.get(index).equals(groupIdentifier)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Retourne le nombre de valeurs dans un groupe à l'index donné.
     *
     * @param groupIndex index du groupe
     * @return nombre de valeurs
     */
    @Override
    public int getValueCount(int groupIndex) {
        try {
            return dataGroup.get(groupIndex).size();
        } catch (IndexOutOfBoundsException e) {
            if (groupIndex != -1) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    @Override
    public Object getValueAt(int position, Object groupIdentifier) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException(" groupIdentifier cannot be null ");
        }

        return getValueAt(position, getIndexGroupIdentifier(groupIdentifier));
    }

    /**
     * Retourne la valeur à la position donnée dans le groupe à l'index donné.
     *
     * @param position position dans le groupe
     * @param groupIndex index du groupe
     * @return la valeur ou null
     */
    @Override
    public Object getValueAt(int position, int groupIndex) {
        try {
            return dataGroup.get(groupIndex).get(position);
        } catch (IndexOutOfBoundsException e) {
            if (groupIndex != -1) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int position, Object groupIdentifier) {

        if (value == null || groupIdentifier == null) {
            throw new IllegalArgumentException(" value or groupIdentifier cannot be null");
        }

        int index = getIndexGroupIdentifier(groupIdentifier);

        try {
            dataGroup.get(index).set(position, value);
        } catch (IndexOutOfBoundsException e) {
            if (index == -1) {
                throw new ArrayStoreException("groupIdentifier not exist");
            }
            if (position == 0) {
                dataGroup.get(index).add(value);
            } else {
                e.initCause(new Throwable("position" + position + "doesn't exist"));
            }
        }

    }

    /**
     * Définit la valeur à la position donnée dans le groupe à l'index donné.
     *
     * @param value nouvelle valeur
     * @param position position dans le groupe
     * @param groupIndex index du groupe
     */
    @Override
    public void setValueAt(Object value, int position, int groupIndex) {
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            if (position >= 0 && position < group.size()) {
                group.set(position, value);
            }
        }
    }

    public void addValue(Object value, String groupIdentifier) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            addValue(value, index);
        }
    }

    /**
     * Ajoute une valeur au groupe à l'index donné.
     *
     * @param value valeur à ajouter
     * @param groupIndex index du groupe
     */
    public void addValue(Object value, int groupIndex) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            group.add(value);
            eventBus.fireValueAdded(new HRibbonModelEvent.ValueAddedEvent(this, groupIndex, value, 1));
        }
    }

    public void insertValueAt(Object value, int position, String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            insertValueAt(value, position, index);
        }
    }

    /**
     * Insère une valeur à la position donnée dans le groupe à l'index donné.
     *
     * @param value valeur à insérer
     * @param position position d'insertion
     * @param groupIndex index du groupe
     */
    public void insertValueAt(Object value, int position, int groupIndex) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            if (position < 0 || position > group.size()) {
                throw new IndexOutOfBoundsException("Position: " + position + ", Size: " + group.size());
            }
            group.add(position, value);
        }
    }

    public void removeValueAt(int position, String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            removeValueAt(position, index);
        }
    }

    /**
     * Supprime la valeur à la position donnée dans le groupe à l'index donné.
     *
     * @param position position de la valeur à supprimer
     * @param groupIndex index du groupe
     */
    public void removeValueAt(int position, int groupIndex) {
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            if (position >= 0 && position < group.size()) {
                Object removedValue = group.remove(position);
            }
        }
    }

    public void removeValue(Object value, String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            removeValue(value, index);
        }
    }

    /**
     * Supprime la première occurrence de la valeur dans le groupe à l'index
     * donné.
     *
     * @param value valeur à supprimer
     * @param groupIndex index du groupe
     */
    public void removeValue(Object value, int groupIndex) {
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            int position = group.indexOf(value);
            if (position >= 0) {
                removeValueAt(position, groupIndex);
            }
        }
    }

    public void removeAllValues(String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            removeAllValues(index);
        }
    }

    /**
     * Supprime toutes les valeurs du groupe à l'index donné.
     *
     * @param groupIndex index du groupe
     */
    public void removeAllValues(int groupIndex) {
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            if (!group.isEmpty()) {
                group.clear();
            }
        }
    }

    public void moveValue(int oldPosition, int newPosition, String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            moveValue(oldPosition, newPosition, index);
        }
    }

    /**
     * Déplace une valeur dans le groupe à l'index donné.
     *
     * @param oldPosition ancienne position
     * @param newPosition nouvelle position
     * @param groupIndex index du groupe
     */
    public void moveValue(int oldPosition, int newPosition, int groupIndex) {
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            ArrayList<Object> group = dataGroup.get(groupIndex);
            if (oldPosition >= 0 && oldPosition < group.size()
                    && newPosition >= 0 && newPosition < group.size()) {
                Object value = group.remove(oldPosition);
                group.add(newPosition, value);
            }
        }
    }

    public boolean containsGroup(String groupIdentifier) {
        return findFirstGroupIndex(groupIdentifier) >= 0;
    }

    public boolean containsValue(Object value, String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            return dataGroup.get(index).contains(value);
        }
        return false;
    }

    /**
     * Vérifie si un groupe à l'index donné contient la valeur.
     *
     * @param value valeur à chercher
     * @param groupIndex index du groupe
     * @return true si la valeur existe
     */
    public boolean containsValue(Object value, int groupIndex) {
        if (groupIndex >= 0 && groupIndex < dataGroup.size()) {
            return dataGroup.get(groupIndex).contains(value);
        }
        return false;
    }

    public String getGroupName(String groupIdentifier) {
        return containsGroup(groupIdentifier) ? groupIdentifier : null;
    }

    public String getGroupName(int groupIdentifier) {
        return containsGroup(groupIdentifier) ? groupIdentifier : null;
    }

    /**
     * Retourne le nom du groupe à l'index donné.
     *
     * @param groupIndex index du groupe
     * @return nom du groupe ou null
     */
    public Object getGroupIndex(int groupIndex) {
        if (groupIndex >= 0 && groupIndex < groupIdentifiers.size()) {
            return groupIdentifiers.get(groupIndex);
        }
        return null;
    }

    /**
     * Ajoute un nouveau groupe vide. Accepte les doublons de noms.
     *
     * @param groupIdentifier nom du groupe
     */
    public void addGroup(String groupIdentifier) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException("Group name cannot be null");
        }
        groupIdentifiers.add(groupIdentifier);
        dataGroup.add(new ArrayList<>());
    }

    /**
     * Supprime le groupe à l'index donné.
     *
     * @param groupIndex index du groupe à supprimer
     */
    public void removeGroup(int groupIndex) {
        if (groupIndex >= 0 && groupIndex < groupIdentifiers.size()) {
            String removedName = groupIdentifiers.remove(groupIndex);
            dataGroup.remove(groupIndex);
        }
    }

    /**
     * Supprime le premier groupe avec le nom donné.
     *
     * @param groupIdentifier nom du groupe à supprimer
     */
    public void removeGroup(String groupIdentifier) {
        int index = findFirstGroupIndex(groupIdentifier);
        if (index >= 0) {
            removeGroup(index);
        }
    }

    @Override
    public void addRibbonModelListener(HRibbonModelListener l) {
        listenerList.add(HRibbonModelListener.class, l);
    }

    @Override
    public void removeModelListener(HRibbonModelListener l) {
        listenerList.remove(HRibbonModelListener.class, l);
    }

    /**
     * Trouve l'index du premier groupe avec le nom donné.
     *
     * @param groupIdentifier nom du groupe à chercher
     * @return index du groupe ou -1 si non trouvé
     */
    private int findFirstGroupIndex(String groupIdentifier) {
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

    public void addRibbonEventListener(HRibbonModelEventListener listener) {
        eventBus.addListener(listener);
    }

    public void removeRibbonEventListener(HRibbonModelEventListener listener) {
        eventBus.removeListener(listener);
    }

}
