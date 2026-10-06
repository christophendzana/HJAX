/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsupertable.model.selection;

import hsupertable.model.selection.CellSelectionEvent;
import java.util.EventListener;

/**
 *
 * @author FIDELE
 */
public interface CellSelectionListener extends EventListener {
    void cellSelectionChanged(CellSelectionEvent e);
}