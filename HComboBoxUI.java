/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.models.HComboBoxModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;

/**
 *
 * @author FIDELE
 */
public class HComboBoxUI extends BasicComboBoxUI implements ActionListener{
    
     private final HComboBoxModel<?> model;
    private float animation = 0.0f;
    private Timer timer;

    // Un rouge plus doux façon Bootstrap (hex #dc311d)
    private final Color mainRed = new Color(220, 49, 29);        // Fond principal
    private final Color hoverRed = new Color(235, 85, 59);       // Survolé
    private final Color pressedRed = new Color(180, 39, 29);     // Pressé
    private final Color focusRed = new Color(250, 205, 205);     // Focus claire dans la bordure
    private final Color shadowColor = new Color(220,49,29,38);
    private final Color arrowBg = new Color(245, 170, 170);      // Arrière-plan du bouton flèche (clair)
    private final Color arrowColor = mainRed;                    // Flèche rouge
    private final Color fontColor = Color.WHITE;                 // Police blanche visible

    public HComboBoxUI(HComboBoxModel<?> model) {
        this.model = model;
        model.addStateListener(this::startAnimationTimer);
        timer = new Timer(15, this);
    }
    private void startAnimationTimer() {
        if (timer != null && !timer.isRunning()) timer.start();
    }
    public void actionPerformed(ActionEvent e) {
        boolean active = model.isHovered() || model.isPressed() || model.isFocused();
        float target = active ? 1f : 0f;
        if (!active && animation <= 0f) {
            timer.stop();
            if (comboBox != null) comboBox.repaint();
            return;
        }
        if (Math.abs(animation - target) < 0.06f) {
            animation = target;
            timer.stop();
            if (comboBox != null) comboBox.repaint();
            return;
        }
        animation += (target - animation) * 0.2;
        if (comboBox != null) comboBox.repaint();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = c.getWidth(), h = c.getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Couleurs interpolées
        Color bg = blendColor(mainRed, hoverRed, animation);
        if (model.isPressed()) bg = blendColor(bg, pressedRed, animation);

        // Ombre portée
        g2.setColor(shadowColor);
        g2.fill(new RoundRectangle2D.Float(4, 4, w - 8, h - 8, 18, 18));

        // Fond arrondi
        Shape round = new RoundRectangle2D.Float(0, 0, w, h, 18, 18);
        g2.setColor(bg);
        g2.fill(round);

        // Bordure arrondie plus claire si focus
        if (model.isFocused()) {
            g2.setColor(focusRed);
            g2.setStroke(new BasicStroke(4));
            g2.draw(round);
        } else {
            g2.setColor(bg.darker().darker());
            g2.setStroke(new BasicStroke(2));
            g2.draw(round);
        }

        // Affichage du texte dans la zone sélectionnée
        Object value = comboBox.getSelectedItem();
        if (value != null) {
            String txt = value.toString();
            g2.setFont(comboBox.getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int textY = (h + metrics.getAscent() - metrics.getDescent()) / 2 - 1;
            g2.setColor(fontColor);
            g2.drawString(txt, 15, textY);
        }
        g2.dispose();
    }

    // Bouton flèche moderne clair
    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(arrowBg);
                g2.fillRoundRect(2, 2, w-4, h-4, 12, 12);

                g2.setColor(arrowColor);
                int[] xs = { w/2-6, w/2, w/2+6 };
                int[] ys = { h/2-2, h/2+7, h/2-2 };
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(28,28));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        return button;
    }

    // Stylisation du popup menu
    @Override
    protected ComboPopup createPopup() {
        ComboPopup popup = super.createPopup();
        JList<?> list = popup.getList();
        list.setBackground(Color.WHITE);
        list.setSelectionBackground(hoverRed);
        list.setSelectionForeground(fontColor);
        list.setFont(list.getFont().deriveFont(Font.BOLD, 16f));
        list.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        popup.getList().setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object val, int idx, boolean sel, boolean foc) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, val, idx, sel, foc);
                label.setOpaque(true);
                if (sel) {
                    label.setBackground(hoverRed);
                    label.setForeground(fontColor);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(mainRed);
                }
                label.setBorder(BorderFactory.createEmptyBorder(6,15,6,15));
                label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
                return label;
            }
        });
        return popup;
    }

    // Méthode utilitaire de couleur
    private static Color blendColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int)(a.getRed() * (1-t) + b.getRed() * t);
        int g = (int)(a.getGreen() * (1-t) + b.getGreen() * t);
        int bval = (int)(a.getBlue() * (1-t) + b.getBlue() * t);
        return new Color(r, g, bval);
    }
    
}
