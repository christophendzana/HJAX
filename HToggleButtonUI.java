/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HToggleButton;
import hcomponents.models.HToggleButtonModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author FIDELE
 */
public class HToggleButtonUI extends BasicButtonUI implements ActionListener {
 
    private final HToggleButtonModel model;
    private float animation = 0f;
    private Timer timer;

    // Couleurs bootstrap-like
    private final Color mainRed = new Color(220, 49, 29);
    private final Color hoverRed = new Color(235, 85, 59);
    private final Color pressedRed = new Color(180, 39, 29);
    private final Color selectedRed = new Color(200, 45, 45);
    private final Color focusBorder = new Color(250, 210, 210);
    private final Color fontColor = Color.WHITE;

    public HToggleButtonUI(HToggleButtonModel model) {
        this.model = model;
        model.addStateListener(this::startAnimationTimer);
        timer = new Timer(15, this);
    }

    private void startAnimationTimer() {
        if (timer != null && !timer.isRunning()) timer.start();
    }

    public void actionPerformed(ActionEvent e) {
        boolean active = model.isHovered() || model.isPressed() || model.isFocused() || model.isSelected();
        float target = active ? 1f : 0f;
        if (!active && animation <= 0f) {
            timer.stop();
            if (model != null && HToggleButton.lastPainted != null)
                HToggleButton.lastPainted.repaint();
            return;
        }
        if (Math.abs(animation - target) < 0.08f) {
            animation = target;
            timer.stop();
            if (HToggleButton.lastPainted != null) HToggleButton.lastPainted.repaint();
            return;
        }
        animation += (target - animation) * 0.2;
        if (HToggleButton.lastPainted != null) HToggleButton.lastPainted.repaint();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        HToggleButton.lastPainted = c; // permet à l’UI d’appeler repaint sur le bon bouton

        Graphics2D g2 = (Graphics2D) g.create();
        int w = c.getWidth(), h = c.getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = blendColor(mainRed, hoverRed, animation);
        if (model.isPressed()) bg = blendColor(bg, pressedRed, animation);
        if (model.isSelected()) bg = blendColor(bg, selectedRed, 0.5f * animation);

        // Ombre portée
        g2.setColor(new Color(220,49,29,45));
        g2.fill(new RoundRectangle2D.Float(2, 2, w-4, h-4, 18, 18));

        // Fond arrondi
        Shape round = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
        g2.setColor(bg);
        g2.fill(round);

        // Bordure
        if (model.isFocused()) {
            g2.setColor(focusBorder);
            g2.setStroke(new BasicStroke(4));
            g2.draw(round);
        } else {
            g2.setColor(bg.darker().darker());
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
        }

        // Texte centré
        AbstractButton ab = (AbstractButton) c;
        String txt = ab.getText();
        FontMetrics metrics = g2.getFontMetrics(ab.getFont());
        int textX = (w - metrics.stringWidth(txt)) / 2;
        int textY = (h + metrics.getAscent() - metrics.getDescent()) / 2 - 1;
        g2.setColor(fontColor);
        g2.setFont(ab.getFont());
        g2.drawString(txt, textX, textY);

        // Si sélectionné, affichage d’un tick moderne
        if (model.isSelected()) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(fontColor);
            int cx = w-24, cy = h/2;
            g2.drawLine(cx, cy+2, cx+5, cy+6);
            g2.drawLine(cx+5, cy+6, cx+12, cy-6);
        }

        g2.dispose();
    }

    private static Color blendColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int)(a.getRed() * (1-t) + b.getRed() * t);
        int g = (int)(a.getGreen() * (1-t) + b.getGreen() * t);
        int bval = (int)(a.getBlue() * (1-t) + b.getBlue() * t);
        return new Color(r, g, bval);
    }
    
}
