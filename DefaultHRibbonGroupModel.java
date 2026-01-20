/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author FIDELE
 */
public class DefaultHRibbonGroupModel implements HRibbonGroupModel, ListSelectionListener {

    private ArrayList<HRibbonGroup> hRibbonGroups;
    
    private ListSelectionModel selectionModel;

    /**
     * Width margin between each group
     */
    private int groupMargin;

    /**
     * List of HRibbonGroupModelListener Classe utilitaire qui gère de manière
     * thread-safe une liste d'écouteurs d'évènements(listeners)
     */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Note: classe qui sert à notifer les composants intéressés (listeners)
     * qu'un état interne d'un composant source (JProcessbar etc.) a changé,
     * principalement utilisé dans les applications swing pour signaler
     *
     */
    private transient ChangeEvent changeEvent = null;

    /**
     * Column selection allowed in this column model
     */
    private boolean groupSelectionAllowed;

    /**
     * A local cache of the combined width of all columns
     */
    private int totalGroupWidth;

    public DefaultHRibbonGroupModel() {
        super();
        hRibbonGroups = new ArrayList<>();
        setSelectionModel(createSelectionModel());
        setGroupMargin(3);
        groupSelectionAllowed = false;
    }

    @Override
    public void addGroup(HRibbonGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }

        hRibbonGroups.add(group);
        group.setModelIndex(hRibbonGroups.size() - 1);
        recalculateWidthCache();
        fireGroupAdded(new HRibbonGroupModelEvent(this, group, hRibbonGroups.size() - 1, -1));
    }
    
    @Override
    public void addGroup(String groupName) {
        HRibbonGroup group = new HRibbonGroup(groupName);
        addGroup(group);

    }

    @Override
    public void removeGroup(HRibbonGroup group) {
        if (group == null) {
            return;
        }

        int index = hRibbonGroups.indexOf(group);
        if (index >= 0) {
            removeGroup(index);
        }
    }

    @Override
    public void removeGroup(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= hRibbonGroups.size()) {
            throw new IndexOutOfBoundsException("Invalid group index: " + groupIndex);
        }

        HRibbonGroup removedGroup = hRibbonGroups.remove(groupIndex);

        // Réindexer les groupes suivants
        for (int i = groupIndex; i < hRibbonGroups.size(); i++) {
            hRibbonGroups.get(i).setModelIndex(i);
        }
        recalculateWidthCache();
        fireGroupRemoved(new HRibbonGroupModelEvent(this, removedGroup, groupIndex, -1));
    }

    @Override
    public void removeGroup(String groupName) {
        if (groupName == null) {
            return;
        }

        for (int i = 0; i < hRibbonGroups.size(); i++) {
            HRibbonGroup group = hRibbonGroups.get(i);
            if (groupName.equals(group.getGroupName())) {
                removeGroup(i);
                return;
            }
        }
    }

    @Override
    public void moveGroup(int oldIndex, int newIndex) {
        if (oldIndex < 0 || oldIndex >= hRibbonGroups.size()) {
            throw new IndexOutOfBoundsException("Invalid old index: " + oldIndex);
        }
        if (newIndex < 0 || newIndex >= hRibbonGroups.size()) {
            throw new IndexOutOfBoundsException("Invalid new index: " + newIndex);
        }

        if (oldIndex == newIndex) {
            return;
        }

        HRibbonGroup group = hRibbonGroups.remove(oldIndex);
        hRibbonGroups.add(newIndex, group);

        // Réindexer les groupes affectés
        int start = Math.min(oldIndex, newIndex);
        int end = Math.max(oldIndex, newIndex);
        for (int i = start; i <= end; i++) {
            hRibbonGroups.get(i).setModelIndex(i);
        }

        fireGroupMoved(new HRibbonGroupModelEvent(this, group, oldIndex, newIndex));
    }

    @Override
    public void moveGroup(String groupName, int newIndex) {
        if (groupName == null) {
            throw new IllegalArgumentException("Group name cannot be null");
        }

        for (int i = 0; i < hRibbonGroups.size(); i++) {
            HRibbonGroup group = hRibbonGroups.get(i);
            if (groupName.equals(group.getGroupName())) {
                moveGroup(i, newIndex);
                return;
            }
        }
    }

    @Override
    public void insertGroup(HRibbonGroup group, int index) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }
        if (index < 0 || index > hRibbonGroups.size()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        hRibbonGroups.add(index, group);

        // Réindexer à partir de l'insertion
        for (int i = index; i < hRibbonGroups.size(); i++) {
            hRibbonGroups.get(i).setModelIndex(i);
        }
        recalculateWidthCache();
        fireGroupAdded(new HRibbonGroupModelEvent(this, group, index, -1));
    }

    @Override
    public void insertGroup(String groupName, int index) {
        HRibbonGroup group = new HRibbonGroup(groupName);
        insertGroup(group, index);
    }

    @Override
    public void setGroupMargin(int newMargin) {
        if (newMargin != groupMargin) {
            groupMargin = newMargin;
            // Notifier tous les listeners.
            recalculateWidthCache();
            fireGroupMarginChanged();
        }
    }

    @Override
    public int getGroupCount() {
        return hRibbonGroups.size();
    }

    @Override
    public Enumeration<HRibbonGroup> getHRibbonGroups() {
        return Collections.enumeration(hRibbonGroups);
    }

    @Override
    public int getGroupIndex(Object groupIdentifier) {
        if (groupIdentifier == null) {
            return -1;
        }

        for (int i = 0; i < hRibbonGroups.size(); i++) {
            HRibbonGroup group = hRibbonGroups.get(i);
            if (groupIdentifier.equals(group.getIdentifier())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public HRibbonGroup getHRibbonGroup(int position) {
        if (position < 0 || position >= hRibbonGroups.size()) {
            return null;
        }
        return hRibbonGroups.get(position);
    }

    @Override
    public int getHRibbonGroupMarggin() {
        return groupMargin;
    }

    @Override
    public int getHRibbonIndexAtX(int xPosition) {
        if (xPosition < 0) {
            return -1;
        }

        int currentX = 0;
        for (int i = 0; i < hRibbonGroups.size(); i++) {
            HRibbonGroup group = hRibbonGroups.get(i);
            currentX += group.getWidth();

            if (xPosition < currentX) {
                return i;
            }

            currentX += groupMargin;
        }

        return -1;
    }

    @Override
    public int getTotalHRibbonGroupWidth() {
        return totalGroupWidth;
    }

    @Override
    public void setHRibbonGroupSelectionAllowed(boolean flag) {
        groupSelectionAllowed = flag;
    }

    @Override
    public boolean getHRibbonGroupSelectionAllowed() {
        return groupSelectionAllowed;
    }

    @Override
    public int[] getSelectionHRibbonGroup() {
        if (selectionModel == null) {
            return new int[0];
        }

        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();

        if (min == -1 || max == -1) {
            return new int[0];
        }

        ArrayList<Integer> selectedIndices = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                selectedIndices.add(i);
            }
        }

        int[] result = new int[selectedIndices.size()];
        for (int i = 0; i < selectedIndices.size(); i++) {
            result[i] = selectedIndices.get(i);
        }

        return result;
    }

    @Override
    public int getSelectedHRibbonHRibbonCount() {
        int[] selected = getSelectionHRibbonGroup();
        return selected.length;
    }

    /**
     * Sets the selection model for this <code>TableColumnModel</code> to
     * <code>newModel</code> and registers for listener notifications from the
     * new selection model. If <code>newModel</code> is <code>null</code>, an
     * exception is thrown.
     *
     * @param newModel the new selection model
     */
    @Override
    public void setSelectionModel(ListSelectionModel newModel) {
        if (newModel == null) {
            throw new IllegalArgumentException("Cannot set a null SelectionModel");
        }

        ListSelectionModel oldModel = selectionModel;

        if (newModel != oldModel) {
            if (oldModel != null) {
                oldModel.removeListSelectionListener(this);
            }

            selectionModel = newModel;
            newModel.addListSelectionListener(this);
        }
    }

    @Override
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void addHRibbonGroupListener(HRibbonGroupModelListener l) {
                listenerList.add(HRibbonGroupModelListener.class, l);
    }

    @Override
    public void removeHRibbonGroupModelListener(HRibbonGroupModelListener l) {
                listenerList.remove(HRibbonGroupModelListener.class, l);
    }

    // implements javax.swing.table.TableColumnModel
    /**
     * Sets whether column selection is allowed. The default is false.
     *
     * @param flag true if column selection will be allowed, false otherwise
     */
    public void setGroupSelectionAllowed(boolean flag) {
         this.groupSelectionAllowed = flag;
    }

    //Méthode implémentée de ListSelectionListener
    @Override
    public void valueChanged(ListSelectionEvent e) {
        fireGroupSelectionChanged(e);
    }

    /**
     * Creates a new default list selection model.
     *
     * @return
     */
    protected ListSelectionModel createSelectionModel() {
        return new DefaultListSelectionModel();
    }

    /**
     * Recalcule la largeur totale en cache.
     */
    protected void recalculateWidthCache() {
        totalGroupWidth = 0;
        for (HRibbonGroup group : hRibbonGroups) {
            totalGroupWidth += group.getWidth();
        }
        if (hRibbonGroups.size() > 0) {
            totalGroupWidth += groupMargin * (hRibbonGroups.size() - 1);
        }
    }
    
    /**
     * Notifie les listeners qu'un groupe a été ajouté.
     */
    protected void fireGroupAdded(HRibbonGroupModelEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupModelListener.class) {
                ((HRibbonGroupModelListener) listeners[i + 1]).groupAdded(e);
            }
        }
    }

    /**
     * Notifie les listeners qu'un groupe a été supprimé.
     */
    protected void fireGroupRemoved(HRibbonGroupModelEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupModelListener.class) {
                ((HRibbonGroupModelListener) listeners[i + 1]).groupRemoved(e);
            }
        }
    }

    /**
     * Notifie les listeners qu'un groupe a été déplacé.
     */
    protected void fireGroupMoved(HRibbonGroupModelEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupModelListener.class) {
                ((HRibbonGroupModelListener) listeners[i + 1]).groupMoved(e);
            }
        }
    }

    /**
     * Notifie les listeners que la sélection a changé.
     */
    protected void fireGroupSelectionChanged(ListSelectionEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupModelListener.class) {
                ((HRibbonGroupModelListener) listeners[i + 1]).groupSelectionChanged(e);
            }
        }
    }

    /**
     * Notifie les listeners que la marge a changé.
     */
    protected void fireGroupMarginChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupModelListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((HRibbonGroupModelListener) listeners[i + 1]).groupMarginChanged(changeEvent);
            }
        }
    }
}
