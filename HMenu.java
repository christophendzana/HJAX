/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicMenuUI;
import hcomponents.vues.HMenuStyle;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.*;

/**
 * Menu personnalisé avec design moderne. Étend JMenu pour offrir des animations
 * fluides et styles prédéfinis.
 *
 * @author FIDELE
 * @version 1.0
 * @see JMenu
 * @see HMenuStyle
 */
public class HMenu extends JMenu {

    private HMenuStyle menuStyle = HMenuStyle.PRIMARY;
    private int cornerRadius = 8;
    private boolean animationsEnabled = true;
    private boolean hoverEnabled = true;

    public HMenu() {
        super();
        initPopup();
        updateUI();
    }

    public HMenu(String text) {
        super(text);
        initPopup();
        updateUI();
    }

    public HMenu(Action a) {
        super(a);
        initPopup();
        updateUI();
    }

    public HMenu(String text, boolean tearOff) {
        super(text, tearOff);
        initPopup();
        updateUI();
    }

    private void initPopup() {
        JPopupMenu popup = getPopupMenu();
        popup.setOpaque(false);
        popup.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Créer un popup avec fond arrondi personnalisé
        popup.setUI(new javax.swing.plaf.basic.BasicPopupMenuUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond arrondi
                if (menuStyle != null) {
                    g2.setColor(menuStyle.getPopupBackground());
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                
                g2.dispose();
            }
        });
    }

    @Override
    public void setPopupMenuVisible(boolean b) {
        if (b) {
            JPopupMenu popup = getPopupMenu();
            popup.setLocation(getWidth() - 5, 0); // Décale vers la droite
        }
        super.setPopupMenuVisible(b);
    }

    @Override
    public void updateUI() {
        setUI(new HBasicMenuUI());

        // Configurer le popup avec bordures arrondies
        JPopupMenu popup = getPopupMenu();
        if (popup != null) {
            popup.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            popup.setOpaque(false);
        }
    }

    public HMenuStyle getMenuStyle() {
        return menuStyle;
    }

    public void setMenuStyle(HMenuStyle style) {
        this.menuStyle = style;
        setForeground(style.getMenuBarTextColor());

        // Mettre à jour tous les items enfants
        for (int i = 0; i < getItemCount(); i++) {
            JMenuItem item = getItem(i);
            if (item instanceof HMenuItem) {
                ((HMenuItem) item).setMenuStyle(style);
            } else if (item instanceof HMenu) {
                ((HMenu) item).setMenuStyle(style);
            }
        }

        // Mettre à jour le popup
        if (getPopupMenu() != null) {
            getPopupMenu().setBackground(style.getPopupBackground());
        }

        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    public boolean isHoverEnabled() {
        return hoverEnabled;
    }

    public void setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
    }

    public static HMenu withStyle(String text, HMenuStyle style) {
        HMenu menu = new HMenu(text);
        menu.setMenuStyle(style);
        return menu;
    }
}
