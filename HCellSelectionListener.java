/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hsupertable.model;

import java.util.EventListener;

/**
 *
 * @author FIDELE
 */
public interface HCellSelectionListener extends EventListener {
    void cellSelectionChanged(HCellSelectionEvent e);
}