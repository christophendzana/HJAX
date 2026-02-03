/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
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
     * List of HRibbonGroupListener Classe utilitaire qui gère de manière
 thread-safe une liste d'écouteurs d'évènements(listeners)
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
        hRibbonGroups = new ArrayList<HRibbonGroup>();
        setSelectionModel(createSelectionModel());
        setGroupMargin(3);
        invalidateWidthCache();
        groupSelectionAllowed = false;
    }

    @Override
    public void addGroup(HRibbonGroup group) {
        if (group == null) {
        throw new IllegalArgumentException("Group cannot be null");
    }

    hRibbonGroups.add(group);
    group.setModelIndex(hRibbonGroups.size() - 1);
    
    //  CHANGEMENTS DU GROUPE
    setupGroupListener(group);
    
    recalculateWidthCache();
    fireGroupAdded(new HRibbonGroupEvent(this, -1, getGroupCount() - 1));
    }
    
    @Override
    public void addGroup(Object groupIdentifier) {
        HRibbonGroup group = new HRibbonGroup(groupIdentifier);
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

        hRibbonGroups.remove(groupIndex);
        fireGroupRemoved(new HRibbonGroupEvent(this, groupIndex, -1));
        
        // Réindexer les groupes suivants
        for (int i = groupIndex; i < hRibbonGroups.size(); i++) {
            hRibbonGroups.get(i).setModelIndex(i);
        }
        recalculateWidthCache();        
    }

    @Override
    public void removeGroup(Object groupIdentifier) {
        if (groupIdentifier == null) {
            return;
        }

        for (int i = 0; i < hRibbonGroups.size(); i++) {
            HRibbonGroup group = hRibbonGroups.get(i);
            if (groupIdentifier.equals(group.getGroupIdentifier())) {
                removeGroup(i);
                return;
            }
        }
    }

    @Override
    public void moveGroup(int groupIndex, int newIndex) {
        if (groupIndex < 0 || groupIndex >= hRibbonGroups.size()) {
            throw new IndexOutOfBoundsException("Invalid old index: " + groupIndex);
        }
        if (newIndex < 0 || newIndex >= hRibbonGroups.size()) {
            throw new IndexOutOfBoundsException("Invalid new index: " + newIndex);
        }

        if (groupIndex == newIndex) {
            return;
        }

        HRibbonGroup group = hRibbonGroups.remove(groupIndex);
        hRibbonGroups.add(newIndex, group);

        // Réindexer les groupes affectés
        int start = Math.min(groupIndex, newIndex);
        int end = Math.max(groupIndex, newIndex);
        for (int i = start; i <= end; i++) {
            hRibbonGroups.get(i).setModelIndex(i);
        }
        fireGroupMoved(new HRibbonGroupEvent(this, groupIndex, newIndex));
    }

    @Override
    public void moveGroup(Object groupIdentifier, int newIndex) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException("Group name cannot be null");
        }

        for (int i = 0; i < hRibbonGroups.size(); i++) {
            HRibbonGroup group = hRibbonGroups.get(i);
            if (groupIdentifier.equals(group.getGroupIdentifier())) {
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
        fireGroupAdded(new HRibbonGroupEvent(this, -1, index));
    }

    @Override
    public void insertGroup(Object groupIdentifier, int index) {
        HRibbonGroup group = new HRibbonGroup(groupIdentifier);
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
    public int getHRibbonGroupIndexAtX(int xPosition) {
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
    public void addHRibbonGroupModelListener(HRibbonGroupListener l) {
                listenerList.add(HRibbonGroupListener.class, l);
    }

    @Override
    public void removeHRibbonGroupModelListener(HRibbonGroupListener l) {
                listenerList.remove(HRibbonGroupListener.class, l);
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
     * Recalcule la largeur totale de tous les groupes.
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
    
    public HRibbonGroupListener[] getHRibbonGroupModelListeners() {
        return listenerList.getListeners(HRibbonGroupListener.class);
    }
    
    /**
     * Returns an array of all the objects currently registered
     * as <code><em>Foo</em>Listener</code>s
     * upon this model.<code><em>Foo</em>Listener</code>s are registered using the
    <code>add<em>Foo</em>Listener</code> method.<p>
     *
     * You can specify the <code>listenerType</code> argument
     * with a class literal,
     * such as
     * <code><em>Foo</em>Listener.class</code>.
     * For example, you can query a
     * <code>DefaultHRibbonGroupModelModel</code> <code>m</code>
     * for its group model listeners with the following code:
     *
     * <pre>HRibbonGroupListener[] cmls = (HRibbonGroupListener[])
 (m.getListeners(HRibbonGroupListener.class));</pre>
     *
     * If no such listeners exist, this method returns an empty array.
     *
     * @param <T> the listener type
     * @param listenerType the type of listeners requested
     * @return an array of all objects registered as
     *          <code><em>Foo</em>Listener</code>s on this model,
     *          or an empty array if no such
     *          listeners have been added
     * @throws ClassCastException if <code>listenerType</code>
     *          doesn't specify a class or interface that implements
     *          <code>java.util.EventListener</code>
     *
     * @see #getColumnModelListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return listenerList.getListeners(listenerType);
    }
    
    
    // PENDING(alan)
    // implements java.beans.PropertyChangeListener
    /**
     * Property Change Listener change method.  Used to track changes
     * to the column width or preferred column width.
     *
     * @param  evt  <code>PropertyChangeEvent</code>
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();

        if ("width".equals(name) || "preferredWidth".equals(name)) {
            invalidateWidthCache();            
            fireGroupMarginChanged();
        }
    }
    
    /**
     * Notifie les listeners qu'un groupe a été ajouté.
     */
    protected void fireGroupAdded(HRibbonGroupEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupListener.class) {
                ((HRibbonGroupListener) listeners[i + 1]).groupAdded(e);
            }
        }
    }

    /**
     * Notifie les listeners qu'un groupe a été supprimé.
     */
    protected void fireGroupRemoved(HRibbonGroupEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupListener.class) {
                ((HRibbonGroupListener) listeners[i + 1]).groupRemoved(e);
            }
        }
    }

    /**
     * Notifie les listeners qu'un groupe a été déplacé.
     */
    protected void fireGroupMoved(HRibbonGroupEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupListener.class) {
                ((HRibbonGroupListener) listeners[i + 1]).groupMoved(e);
            }
        }
    }

    /**
     * Notifie les listeners que la sélection a changé.
     */
    protected void fireGroupSelectionChanged(ListSelectionEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupListener.class) {
                ((HRibbonGroupListener) listeners[i + 1]).groupSelectionChanged(e);
            }
        }
    }

    /**
     * Notifie les listeners que la marge a changé.
     */
    protected void fireGroupMarginChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonGroupListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((HRibbonGroupListener) listeners[i + 1]).groupMarginChanged(changeEvent);
            }
        }
    }
    
    private void invalidateWidthCache() {
        totalGroupWidth = -1;
    }
    
    
    /**
 * S'abonne aux changements d'un groupe pour propager les notifications.
 */
private void setupGroupListener(HRibbonGroup group) {
    if (group == null) return;
    
    group.addPropertyChangeListener(evt -> {
        String propertyName = evt.getPropertyName();
        
        // Si une propriété visuelle du groupe change
        if ("headerValue".equals(propertyName) || 
            "headerAlignment".equals(propertyName) ||
            "width".equals(propertyName) ||
            "preferredWidth".equals(propertyName)) {
            
            // Trouver l'index du groupe dans la liste
            int groupIndex = hRibbonGroups.indexOf(group);
            if (groupIndex >= 0) {
                // Notifier les listeners que ce groupe a changé
                fireGroupChanged(groupIndex, propertyName);
            }
        }
    });
}

/**
 * Notifie les listeners qu'une propriété d'un groupe a changé.
 */
protected void fireGroupChanged(int groupIndex, String propertyName) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] == HRibbonGroupListener.class) {
            // Créer un événement spécial pour les changements de propriétés
            // On va réutiliser HRibbonGroupEvent avec fromIndex = toIndex
            HRibbonGroupEvent e = new HRibbonGroupEvent(this, groupIndex, groupIndex);
            
            // Pour l'instant, on appelle groupMoved (à adapter si besoin)
            ((HRibbonGroupListener) listeners[i + 1]).groupMoved(e);
        }
    }
}
}
