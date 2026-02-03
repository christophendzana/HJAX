package rubban;

import javax.swing.event.EventListenerList;

/**
 * Classe abstraite de base pour les modèles de ruban.
 * Fournit les implémentations par défaut pour la gestion des listeners
 * et les méthodes de notification.
 * Analogie : similaire à AbstractTableModel pour JTable.
 */
public abstract class AbstractHRibbonModel implements HRibbonModel {
    
    /** Liste des écouteurs enregistrés */
    protected EventListenerList listenerList = new EventListenerList();
    
    // IMPLÉMENTATION DES MÉTHODES DE HRibbonModel 
    
    /**
     * @param l    
     */
    @Override
    public void addRibbonModelListener(HRibbonModelListener l) {
        listenerList.add(HRibbonModelListener.class, l);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeModelListener(HRibbonModelListener l) {
        listenerList.remove(HRibbonModelListener.class, l);
    }
    
    // MÉTHODES DE NOTIFICATION (protected)    
    /**
     * Notifie tous les listeners que toutes les données du ruban ont changé.
     * À appeler après une modification majeure de la structure.
     */
    protected void fireRibbonDataChanged() {
        fireRibbonChanged(new HRibbonModelEvent(this));
    }
    
    /**
     * Notifie qu'un groupe a été ajouté.
     * 
     * @param groupIndex l'index du groupe ajouté
     */
    protected void fireGroupAdded(int groupIndex) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex, -1, -1, HRibbonModelEvent.INSERT));
    }
    
    /**
     * Notifie qu'un groupe a été supprimé.
     * 
     * @param groupIndex l'index du groupe supprimé
     */
    protected void fireGroupRemoved(int groupIndex) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex, -1, -1, HRibbonModelEvent.DELETE));
    }
    
    /**
     * Notifie qu'une valeur a été ajoutée dans un groupe.
     * 
     * @param groupIndex l'index du groupe
     * @param position la position où la valeur a été ajoutée
     */
    protected void fireValueAdded(int groupIndex, int position) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex, position, -1, HRibbonModelEvent.INSERT));
    }
    
    /**
     * Notifie qu'une valeur a été supprimée d'un groupe.
     * 
     * @param groupIndex l'index du groupe
     * @param position la position de la valeur supprimée
     */
    protected void fireValueRemoved(int groupIndex, int position) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex, position, -1, HRibbonModelEvent.DELETE));
    }
    
    /**
     * Notifie qu'une valeur a été mise à jour dans un groupe.
     * 
     * @param groupIndex l'index du groupe
     * @param position la position de la valeur mise à jour
     */
    protected void fireValueUpdated(int groupIndex, int position) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex, position, -1, HRibbonModelEvent.UPDATE));
    }
    
    /**
     * Notifie qu'une valeur a été déplacée dans un groupe.
     * 
     * @param groupIndex l'index du groupe
     * @param fromPosition position d'origine
     * @param toPosition nouvelle position
     */
    protected void fireValueMoved(int groupIndex, int fromPosition, int toPosition) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex, fromPosition, toPosition, HRibbonModelEvent.MOVE));
    }
    
    /**
     * Notifie que les valeurs d'un groupe ont toutes changé.
     * 
     * @param groupIndex l'index du groupe
     */
    protected void fireGroupValuesChanged(int groupIndex) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex));
    }
    
    /**
     * Méthode générique pour notifier les listeners.
     * 
     * @param e l'événement à propager
     */
    protected void fireRibbonChanged(HRibbonModelEvent e) {
        Object[] listeners = listenerList.getListenerList();
        
        // Parcourt la liste des listeners dans l'ordre inverse
        // (standard Swing pour permettre aux listeners d'interrompre la propagation)
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonModelListener.class) {
                ((HRibbonModelListener) listeners[i + 1]).ribbonChanged(e);
            }
        }
    }
    
    // MÉTHODES ABSTRAITES (doivent être implémentées par les sous-classes)
    
    /**
     */
    @Override
    public abstract int getGroupCount();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getGroupIdentifier(int groupIndex);
    
    /**     
     */
    @Override
    public abstract Object getValueAt(int position, int groupIndex);
    
    /**
     * @param value
     * @param position
     * @param groupIndex
     */
    @Override
    public abstract void setValueAt(Object value, int position, int groupIndex);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getValueCount(int groupIndex);
    
    
    
    // MÉTHODES PAR DÉFAUT (peuvent être surchargées)    
    /**
     * {@inheritDoc}
     * Implémentation par défaut qui cherche l'index du groupe puis appelle
     * getValueAt(position, groupIndex).
     */
    @Override
    public Object getValueAt(int position, Object groupIdentifier) {
        for (int i = 0; i < getGroupCount(); i++) {
            if (groupIdentifier.equals(getGroupIdentifier(i))) {
                return getValueAt(position, i);
            }
        }
        return null;
    }
    
    /**     
     * Implémentation par défaut qui cherche l'index du groupe puis appelle
     * getValueCount(groupIndex).
     * @param groupIdentifier
     * @return 
     */
    @Override
    public int getValueCount(Object groupIdentifier) {
        for (int i = 0; i < getGroupCount(); i++) {
            if (groupIdentifier.equals(getGroupIdentifier(i))) {
                return getValueCount(i);
            }
        }
        return 0;
    }
    
    /**
     * 
     * Implémentation par défaut qui cherche l'index du groupe puis appelle
     * setValueAt(value, position, groupIndex).
     * @param value
     * @param position
     * @param groupIdentifier
     */
    @Override
    public void setValueAt(Object value, int position, Object groupIdentifier) {
        for (int i = 0; i < getGroupCount(); i++) {
            if (groupIdentifier.equals(getGroupIdentifier(i))) {
                setValueAt(value, position, i);
                return;
            }
        }
    }    
     
    /**
     * Retourne un tableau de tous les listeners enregistrés.
     * @return 
     */
    public HRibbonModelListener[] getRibbonModelListeners() {
        return listenerList.getListeners(HRibbonModelListener.class);
    }
    
    /**
     * Vérifie si le modèle a des listeners enregistrés.
     * @return 
     */
    protected boolean hasListeners() {
        return listenerList.getListenerCount() > 0;
    }
}