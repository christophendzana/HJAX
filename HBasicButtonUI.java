/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HButton;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author FIDELE
 */
public class HBasicButtonUI extends BasicButtonUI {

     private int cornerRadius = 48;
    private Color baseColor = new Color(66, 165, 245);
    private Color hoverColor = new Color(33, 150, 243);
    private Color pressColor = new Color(30, 136, 229);
    private Color textColor = Color.BLACK;

    private Color currentColor;
    private Timer hoverTimer;
    private float hoverFraction = 0f;
    private final int hoverDelay = 15;
    private final float hoverStep = 0.05f;

    public HBasicButtonUI() {
        currentColor = baseColor;
    }

    public static ComponentUI createUI(JComponent c) {
        return new HBasicButtonUI();
    }

    private Color blend(Color c1, Color c2, float fraction) {
        fraction = Math.min(1f, Math.max(0f, fraction));
        int r = (int)(c1.getRed() + fraction * (c2.getRed() - c1.getRed()));
        int g = (int)(c1.getGreen() + fraction * (c2.getGreen() - c1.getGreen()));
        int b = (int)(c1.getBlue() + fraction * (c2.getBlue() - c1.getBlue()));
        int a = (int)(c1.getAlpha() + fraction * (c2.getAlpha() - c1.getAlpha()));
        return new Color(r, g, b, a);
    }

    private void startHover(boolean entering, AbstractButton b) {
        if (hoverTimer != null && hoverTimer.isRunning()) hoverTimer.stop();

        hoverTimer = new Timer(hoverDelay, evt -> {
            if (entering) {
                hoverFraction += hoverStep;
                if (hoverFraction >= 1f) { hoverFraction = 1f; hoverTimer.stop(); }
            } else {
                hoverFraction -= hoverStep;
                if (hoverFraction <= 0f) { hoverFraction = 0f; hoverTimer.stop(); }
            }
            currentColor = blend(baseColor, hoverColor, hoverFraction);
            b.repaint();
        });
        hoverTimer.start();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof AbstractButton b) {
            b.setRolloverEnabled(true);
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) { startHover(true, b); }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) { startHover(false, b); }
            });
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        int width = c.getWidth();
        int height = c.getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (model.isPressed()) currentColor = pressColor;

        // ---- Ombre personnalisée ----
        if (b instanceof HButton hb) {
            g2.setColor(hb.getShadowColorCustom());
            g2.fillRoundRect(hb.getShadowOffsetX(), hb.getShadowOffsetY(),
                    width, height, cornerRadius, cornerRadius);
        }

        // ---- Fond ----
        GradientPaint gradient = new GradientPaint(0,0,currentColor.brighter(),0,height,currentColor.darker());
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

        // ---- Vertical stack ----
        int vAlign = b.getVerticalAlignment();
        if (vAlign != SwingConstants.NORTH && vAlign != SwingConstants.EAST) {
            super.paint(g2, c);
            g2.dispose();
            return;
        }

        FontMetrics fm = g2.getFontMetrics();
        String text = b.getText();
        Icon icon = b.getIcon();
        Rectangle viewRect = new Rectangle(b.getInsets().left, b.getInsets().top,
                width - b.getInsets().left - b.getInsets().right,
                height - b.getInsets().top - b.getInsets().bottom);

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        int gap = 4;
        if (b instanceof HButton hb) gap = hb.getIconTextGapCustom();

        int totalHeight = (icon != null ? icon.getIconHeight() : 0)
                + (text != null ? fm.getHeight() : 0)
                + gap;

        int startY = viewRect.y + (viewRect.height - totalHeight)/2; // centrage vertical parfait

        if (vAlign == SwingConstants.NORTH) {
            int iconH = (icon != null) ? icon.getIconHeight() : 0;
            if (icon != null) {
                iconRect.x = viewRect.x + (viewRect.width - icon.getIconWidth())/2;
                iconRect.y = startY;
                iconRect.width = icon.getIconWidth();
                iconRect.height = icon.getIconHeight();
            }
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text))/2;
            textRect.y = startY + iconH + gap;
            textRect.width = fm.stringWidth(text);
            textRect.height = fm.getHeight();

        } else if (vAlign == SwingConstants.EAST) {
            int textH = (text != null) ? fm.getHeight() : 0;
            textRect.x = viewRect.x + (viewRect.width - fm.stringWidth(text))/2;
            textRect.y = startY;
            textRect.width = fm.stringWidth(text);
            textRect.height = textH;
            if (icon != null) {
                iconRect.x = viewRect.x + (viewRect.width - icon.getIconWidth())/2;
                iconRect.y = startY + textH + gap;
                iconRect.width = icon.getIconWidth();
                iconRect.height = icon.getIconHeight();
            }
        }

        if (icon != null) icon.paintIcon(c, g2, iconRect.x, iconRect.y);

        g2.setFont(b.getFont());
        g2.setColor(textColor);
        if (text != null && !text.isEmpty()) {
            g2.drawString(text, textRect.x, textRect.y + textRect.height - fm.getDescent());
        }

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton)c;
        Icon icon = b.getIcon();
        String text = b.getText();
        Insets insets = b.getInsets();
        FontMetrics fm = b.getFontMetrics(b.getFont());

        int width=0, height=0, gap=4;
        if (b instanceof HButton hb) gap = hb.getIconTextGapCustom();

        int vAlign = b.getVerticalAlignment();
        if (vAlign == SwingConstants.NORTH || vAlign == SwingConstants.EAST) {
            int iconW = (icon != null) ? icon.getIconWidth() : 0;
            int iconH = (icon != null) ? icon.getIconHeight() : 0;
            int textW = (text != null && !text.isEmpty()) ? fm.stringWidth(text) : 0;
            int textH = (text != null && !text.isEmpty()) ? fm.getHeight() : 0;

            width = Math.max(iconW, textW) + insets.left + insets.right;
            height = iconH + textH + gap + insets.top + insets.bottom;
            return new Dimension(width, height);
        }

        return super.getPreferredSize(c);
    }

    // ---- Setters couleurs / style ----
    public void setBaseColor(Color color){this.baseColor=color;}
    public void setHoverColor(Color color){this.hoverColor=color;}
    public void setPressColor(Color color){this.pressColor=color;}
    public void setTextColor(Color color){this.textColor=color;}
    public void setCornerRadius(int r){this.cornerRadius=r;}

}
