/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author FIDELE
 */
public interface HRibbonGroupListener extends java.util.EventListener {
    
     /**
     * Tells listeners that a group was added to the model.
     *
     * @param e a {@code HRibbonGroupEvent}
     */
    public void groupAdded(HRibbonGroupEvent e);

    /**
     * Tells listeners that a group was removed from the model.
     *
     * @param e a {@code HRibbonGroupEvent}
     */
    public void groupRemoved(HRibbonGroupEvent e);

    /**
     * Tells listeners that a group was repositioned.
     *
     * @param e a {@code HRibbonGroupEvent}
     */
    public void groupMoved(HRibbonGroupEvent e);

    /**
     * Tells listeners that a group was moved due to a margin change.
     *
     * @param e a {@code ChangeEvent}
     */
    public void groupMarginChanged(ChangeEvent e);

    /**
     * Tells listeners that the selection model of the
     * HRibbonGroupModel changed.
     *
     * @param e a {@code ListSelectionEvent}
     */
    public void groupSelectionChanged(ListSelectionEvent e);
    
}
