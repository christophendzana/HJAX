package rubban;

import java.util.List;
import javax.swing.event.EventListenerList;

/**
 * Classe abstraite de base pour les modèles de ruban. Fournit les
 * implémentations par défaut pour la gestion des listeners et les méthodes de
 * notification.
 *
 * Cette classe ne contient PAS de données. Elle sert de fondation pour les
 * implémentations concrètes qui gèrent leurs propres structures de données.
 *
 * Analogie : similaire à AbstractTableModel pour JTable.
 *
 * @author FIDELE
 */
public abstract class AbstractHRibbonModel implements HRibbonModel {

    // =========================================================================
    // ATTRIBUTS
    // =========================================================================
    /**
     * Liste des écouteurs enregistrés (pattern EventListenerList de Swing)
     */
    protected EventListenerList listenerList = new EventListenerList();

    // =========================================================================
    // IMPLÉMENTATION DE HRibbonModel - GESTION DES LISTENERS
    // =========================================================================
    /**
     * {@inheritDoc}
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

    // =========================================================================
    // MÉTHODES DE NOTIFICATION (protected)
    // =========================================================================
    /**
     * Notifie tous les listeners que toutes les données du ruban ont changé. À
     * appeler après une modification majeure de la structure.
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
     * Notifie que toutes les valeurs d'un groupe ont changé.
     *
     * @param groupIndex l'index du groupe
     */
    protected void fireGroupValuesChanged(int groupIndex) {
        fireRibbonChanged(new HRibbonModelEvent(this, groupIndex));
    }

    /**
     * Méthode générique pour notifier les listeners. Parcourt la liste des
     * listeners dans l'ordre inverse (standard Swing).
     *
     * @param e l'événement à propager
     */
    protected void fireRibbonChanged(HRibbonModelEvent e) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HRibbonModelListener.class) {
                ((HRibbonModelListener) listeners[i + 1]).ribbonChanged(e);
            }
        }
    }

    // =========================================================================
    // IMPLÉMENTATIONS PAR DÉFAUT DES MÉTHODES DE HRibbonModel
    // (basées sur d'autres méthodes abstraites)
    // =========================================================================
    /**
     * {@inheritDoc} Implémentation par défaut qui cherche l'index du groupe
     * puis appelle getValueAt(position, groupIndex). Les sous-classes peuvent
     * surcharger cette méthode pour de meilleures performances.
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
     * {@inheritDoc} Implémentation par défaut qui cherche l'index du groupe
     * puis appelle getValueCount(groupIndex).
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
     * {@inheritDoc} Implémentation par défaut qui cherche l'index du groupe
     * puis appelle setValueAt(value, position, groupIndex).
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
     * {@inheritDoc} Implémentation par défaut qui cherche l'index du groupe
     * puis appelle addValue(value, groupIndex).
     */
    @Override
    public void addValue(Object value, Object groupIdentifier) {
        int groupIndex = findGroupIndex(groupIdentifier);
        if (groupIndex >= 0) {
            addValue(value, groupIndex);
        }
    }

    /**
     * {@inheritDoc} Implémentation par défaut qui cherche l'index du groupe
     * puis appelle insertValueAt(value, position, groupIndex).
     */
    @Override
    public void insertValueAt(Object value, int position, Object groupIdentifier) {
        int groupIndex = findGroupIndex(groupIdentifier);
        if (groupIndex >= 0) {
            insertValueAt(value, position, groupIndex);
        }
    }

    /**
     * {@inheritDoc} Implémentation par défaut qui construit une liste à partir
     * des valeurs du groupe.
     */
    @Override
    public List<Object> getComponentsAt(int groupIndex) {
        List<Object> list = new java.util.ArrayList<>();
        int valueCount = getValueCount(groupIndex);
        for (int i = 0; i < valueCount; i++) {
            list.add(getValueAt(i, groupIndex));
        }
        return list;
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES PROTECTED
    // =========================================================================
    /**
     * Trouve l'index du premier groupe ayant l'identifiant spécifié. Méthode
     * utilitaire pour les sous-classes.
     *
     * @param groupIdentifier l'identifiant du groupe à chercher
     * @return l'index du groupe, ou -1 si non trouvé
     */
    protected int findGroupIndex(Object groupIdentifier) {
        if (groupIdentifier == null) {
            return -1;
        }
        for (int i = 0; i < getGroupCount(); i++) {
            Object id = getGroupIdentifier(i);
            if (groupIdentifier.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retourne un tableau de tous les listeners enregistrés.
     *
     * @return un tableau des listeners de type HRibbonModelListener
     */
    public HRibbonModelListener[] getRibbonModelListeners() {
        return listenerList.getListeners(HRibbonModelListener.class);
    }

    /**
     * Vérifie si le modèle a des listeners enregistrés.
     *
     * @return true si au moins un listener est enregistré
     */
    protected boolean hasListeners() {
        return listenerList.getListenerCount() > 0;
    }

    // =========================================================================
    // MÉTHODES ABSTRAITES (à implémenter par les sous-classes)
    // =========================================================================
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getGroupCount();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getGroupIdentifier(int groupIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int getValueCount(int groupIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Object getValueAt(int position, int groupIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void setValueAt(Object value, int position, int groupIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void addValue(Object value, int groupIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void insertValueAt(Object value, int position, int groupIndex);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void addGroup(Object groupIdentifier);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract long getVersion();
}
