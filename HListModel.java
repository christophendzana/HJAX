/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.models;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 *
 * @author FIDELE
 * @param <E>
 */
public class HListModel<E> extends DefaultListModel<E> {

    private final List<Runnable> stateListeners = new ArrayList<>();
    private int hoveredIndex = -1;

    /**
     * Ajoute un listener pour les changements d'état
     * @param listener
     */
    public void addStateListener(Runnable listener) {
        if (listener != null && !stateListeners.contains(listener)) {
            stateListeners.add(listener);
        }
    }

    /**
     * Supprime un listener
     * @param listener
     */
    public void removeStateListener(Runnable listener) {
        stateListeners.remove(listener);
    }

    /**
     * Notifie tous les listeners du changement d'état
     */
    public void notifyStateChanged() {
        for (Runnable listener : new ArrayList<>(stateListeners)) {
            listener.run();
        }
    }

    /**
     * Définit l'index survolé et notifie les listeners (appel repaint())
     * @param index
     */
    public void setHoveredIndex(int index) {
        if (this.hoveredIndex != index) {
            this.hoveredIndex = index;
            notifyStateChanged();
        }
    }

    public int getHoveredIndex() {
        return hoveredIndex;
    }

    // Surcharge des méthodes pour notifier les changements
    @Override
    public void addElement(E element) {
        super.addElement(element);
        notifyStateChanged();
    }

    @Override
    public void insertElementAt(E element, int index) {
        if (index >= 0 && index <= size()) {
            super.insertElementAt(element, index);
            notifyStateChanged();
        }
    }

    @Override
    public E remove(int index) {

        if (index >= 0 && index <= size()) {
            E element = super.remove(index);
            notifyStateChanged();
            return element;
        }
        throw new IndexOutOfBoundsException("index invalide");
    }

    @Override
    public boolean removeElement(Object obj) {
        boolean removed = super.removeElement(obj);
        if (removed) {
            notifyStateChanged();
        }
        return removed;
    }

    @Override
    public void removeAllElements() {
        super.removeAllElements();
        notifyStateChanged();
    }

    @Override
    public E set(int index, E element) {
        E oldElement = super.set(index, element);
        notifyStateChanged();
        return oldElement;
    }

    @Override
    public void clear() {
        super.clear();
        notifyStateChanged();
    }
}
