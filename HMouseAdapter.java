/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.controllers;


import hcomponents.models.HDefaultButtonModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
/**
 *
 * @author FIDELE
 */
public class HMouseAdapter extends MouseAdapter {
    
    private final JButton button;
    private final HDefaultButtonModel model;

    public HMouseAdapter(JButton button, HDefaultButtonModel model) {
        this.button = button;
        this.model = model;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        model.setHovered(true);
        button.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        model.setHovered(false);
        button.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        model.setPressed(true);
        button.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        model.setPressed(false);
        button.repaint();
    }
    
}
