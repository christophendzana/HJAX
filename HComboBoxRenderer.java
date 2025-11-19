/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author FIDELE
 */
public class HComboBoxRenderer extends DefaultListCellRenderer {
    
     @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setOpaque(true);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        if (isSelected || cellHasFocus) {
            label.setBackground(new Color(143, 1, 1));
            label.setForeground(Color.WHITE);
        } else {
            label.setBackground(Color.WHITE);
            label.setForeground(new Color(143, 1, 1));
        }
        label.setBorder(BorderFactory.createEmptyBorder(6,15,6,15));
        return label;
    }
    
}
