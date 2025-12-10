package hcomponents.vues;

import hcomponents.HComboBox;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Interface utilisateur moderne pour HComboBox avec animations fluides,
 * bordures arrondies et flèche personnalisée.
 */
public class HBasicComboBoxUI extends BasicComboBoxUI {

    private HComboBox<?> hComboBox;
    private float hoverProgress = 0f;
    private Timer hoverTimer;
    private boolean isHovering = false;

    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        if (c instanceof HComboBox) {
            hComboBox = (HComboBox<?>) c;
        }

        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Listener pour le hover
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (hComboBox != null && hComboBox.isHoverEnabled()) {
                    isHovering = true;
                    animateHover(true, c);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hComboBox != null && hComboBox.isHoverEnabled()) {
                    isHovering = false;
                    animateHover(false, c);
                }
            }
        });
    }

    private void animateHover(boolean in, JComponent c) {
        if (hComboBox == null || !hComboBox.isAnimationsEnabled()) {
            hoverProgress = in ? 1f : 0f;
            c.repaint();
            return;
        }

        if (hoverTimer != null) {
            hoverTimer.stop();
        }

        float startProgress = hoverProgress;
        long startTime = System.currentTimeMillis();

        hoverTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);

                hoverProgress = in ? (startProgress + (1f - startProgress) * progress)
                                   : (startProgress - startProgress * progress);

                c.repaint();

                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });

        hoverTimer.start();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = c.getWidth();
        int height = c.getHeight();
        int radius = (hComboBox != null) ? hComboBox.getCornerRadius() : 8;

        // Dessiner le fond avec animation
        paintBackground(g2, c, width, height, radius);

        // Dessiner la bordure
        paintBorder(g2, c, width, height, radius);

        g2.dispose();

        // Appeler le rendu parent pour le contenu (flèche, texte courant, etc.)
        super.paint(g, c);
    }

    private void paintBackground(Graphics2D g2, JComponent c, int width, int height, int radius) {
        if (hComboBox == null || hComboBox.getComboStyle() == null) return;

        HComboBoxStyle style = hComboBox.getComboStyle();
        Color baseColor = style.getBackground();
        Color hoverColor = style.getHoverBackground();

        Color currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);

        g2.setColor(currentColor);
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            2, 2, width - 4, height - 4, radius, radius
        );
        g2.fill(roundRect);
    }

    private void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
        if (hComboBox == null || hComboBox.getComboStyle() == null) return;

        HComboBoxStyle style = hComboBox.getComboStyle();
        g2.setColor(style.getBorderColor());
        g2.setStroke(new BasicStroke(2f));

        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            2, 2, width - 4, height - 4, radius, radius
        );
        g2.draw(roundRect);
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Dessiner la flèche
                paintArrow(g2, width, height);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 20);
            }
        };

        button.setBorder(BorderFactory.createEmptyBorder());
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);

        return button;
    }

    private void paintArrow(Graphics2D g2, int width, int height) {
        if (hComboBox == null || hComboBox.getComboStyle() == null) return;

        int arrowSize = (hComboBox != null) ? hComboBox.getArrowSize() : 8;
        int x = width / 2;
        int y = height / 2;

        // Créer la flèche vers le bas
        Path2D arrow = new Path2D.Float();
        arrow.moveTo(-arrowSize/2, -arrowSize/4);
        arrow.lineTo(arrowSize/2, -arrowSize/4);
        arrow.lineTo(0, arrowSize/2);
        arrow.closePath();

        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y);

        HComboBoxStyle style = hComboBox.getComboStyle();
        g2.setColor(style.getArrowColor());
        g2.fill(arrow);

        g2.setTransform(oldTransform);
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            @Override
            protected void configurePopup() {
                super.configurePopup();
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                setOpaque(false);
            }

            @Override
            public void show() {
                // Configurer le renderer pour la liste
                if (list != null && hComboBox != null) {
                    list.setCellRenderer(new ModernComboBoxRenderer());

                    if (hComboBox.getComboStyle() != null) {
                        list.setBackground(hComboBox.getComboStyle().getPopupBackground());
                        list.setSelectionBackground(hComboBox.getComboStyle().getSelectedBackground());
                        list.setSelectionForeground(hComboBox.getComboStyle().getSelectedTextColor());
                    }
                }
                super.show();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int radius = (hComboBox != null) ? hComboBox.getCornerRadius() : 8;

                // Fond arrondi
                if (hComboBox != null && hComboBox.getComboStyle() != null) {
                    g2.setColor(hComboBox.getComboStyle().getPopupBackground());
                } else {
                    g2.setColor(Color.WHITE);
                }

                RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    0, 0, width, height, radius, radius
                );
                g2.fill(roundRect);

                // Bordure
                if (hComboBox != null && hComboBox.getComboStyle() != null) {
                    g2.setColor(hComboBox.getComboStyle().getBorderColor());
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(roundRect);
                }

                g2.dispose();
            }
        };
    }

    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }

    /**
     * Renderer personnalisé pour les items de la liste.
     * Corrigé : conserve l'état sélectionné et peint correctement le fond même
     * lorsque le renderer n'est pas opaque (pour permettre des coins arrondis).
     */
    private class ModernComboBoxRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        // champs pour conserver l'état fourni dans getListCellRendererComponent
        private boolean cellSelected = false;
        private int cellIndex = -1;
        private Object cellValue = null;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                     int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // On veut peindre un fond arrondi nous-mêmes, donc on laisse opaque = false (pour éviter
            // que super.paintComponent remplisse un rectangulaire), mais on mémorise l'état sélection.
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            cellSelected = isSelected;
            cellIndex = index;
            cellValue = value;

            if (hComboBox != null && hComboBox.getComboStyle() != null) {
                HComboBoxStyle style = hComboBox.getComboStyle();

                if (isSelected) {
                    setForeground(style.getSelectedTextColor());
                } else {
                    setForeground(style.getTextColor());
                }
            }

            // le texte affiché est déjà géré par super.getListCellRendererComponent
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int radius = (hComboBox != null) ? hComboBox.getCornerRadius() : 6;

            // Dessiner le fond de l'item (arrondi) selon l'état sélectionné
            if (hComboBox != null && hComboBox.getComboStyle() != null) {
                HComboBoxStyle style = hComboBox.getComboStyle();

                if (cellSelected) {
                    g2.setColor(style.getSelectedBackground());
                    RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                        2, 1, width - 4, height - 2, radius, radius
                    );
                    g2.fill(roundRect);
                } else {
                    // Optionnel : dessiner un fond neutre ou transparent pour les items non sélectionnés
                    // Ici on laisse transparent pour voir la popupBackground sous-jacente
                }
            }

            g2.dispose();

            // Appeler super pour dessiner le texte sur notre fond peint (super n'effacera pas le fond car opaque=false)
            super.paintComponent(g);
        }
    }
}