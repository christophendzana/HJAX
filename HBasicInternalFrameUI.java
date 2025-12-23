/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.vues;

import hcomponents.HButton;
import hcomponents.HInternalFrame;
import hcomponents.vues.shadow.HShadow;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 * Interface utilisateur personnalisée pour le composant HInternalFrame. Fournit
 * un rendu moderne avec barre de titre stylée, boutons personnalisés (HButton),
 * dégradés de couleur, ombres et bordures arrondies.
 *
 * <p>
 * Cette classe implémente le look and feel personnalisé du HInternalFrame avec
 * support des états (iconifié, maximisé) et animations.</p>
 *
 * @author FIDELE
 * @version 1.0
 * @see HInternalFrame
 * @see BasicInternalFrameUI
 */
public class HBasicInternalFrameUI extends BasicInternalFrameUI {

    /**
     * Panel de la barre de titre personnalisée
     */
    private JPanel titlePane;

    /**
     * Label du titre
     */
    private JLabel titleLabel;

    /**
     * Bouton d'iconification
     */
    private HButton iconButton;

    /**
     * Bouton de maximisation
     */
    private HButton maxButton;

    /**
     * Bouton de fermeture
     */
    private HButton closeButton;

    /**
     * Position X de la souris lors du drag (relatif à l'écran)
     */
    private int mouseScreenX;

    /**
     * Position Y de la souris lors du drag (relatif à l'écran)
     */
    private int mouseScreenY;

    /**
     * Constructeur avec la fenêtre interne.
     *
     * @param frame la fenêtre interne à personnaliser
     */
    public HBasicInternalFrameUI(JInternalFrame frame) {
        super(frame);
    }

    /**
     * Installe l'interface utilisateur sur le composant.
     *
     * @param c le composant JInternalFrame à personnaliser
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        // Retirer les composants par défaut
        setNorthPane(null);

        // Désactiver les bordures par défaut
        frame.setBorder(BorderFactory.createEmptyBorder());
        frame.setOpaque(false);

        // Créer la barre de titre personnalisée
        createTitlePane();

        // Ajouter la barre de titre
        setNorthPane(titlePane);
    }

    /**
     * Crée la barre de titre personnalisée avec boutons HButton.
     */
    protected void createTitlePane() {
        HInternalFrame hFrame = (HInternalFrame) frame;

        titlePane = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fond de la barre de titre avec dégradé
                HInternalFrameStyle style = hFrame.getFrameStyle();
                GradientPaint gradient = new GradientPaint(
                        0, 0, style.getTitleBarColor().brighter(),
                        0, getHeight(), style.getTitleBarColor()
                );
                g2.setPaint(gradient);

                // Dessiner un rectangle avec seulement les coins supérieurs arrondis
                int radius = hFrame.getCornerRadius();
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

                // Remplir le bas pour masquer les coins arrondis inférieurs
                g2.setColor(style.getTitleBarColor());
                g2.fillRect(0, getHeight() - radius, getWidth(), radius);

                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };

        titlePane.setPreferredSize(new Dimension(0, hFrame.getTitleBarHeight()));
        titlePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));

        // Panel gauche avec titre
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);

        titleLabel = new JLabel(frame.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(hFrame.getFrameStyle().getTitleColor());
        titleLabel.setBorder(BorderFactory.createEmptyBorder(
                (hFrame.getTitleBarHeight() - 20) / 2, 0, 0, 0
        ));
        leftPanel.add(titleLabel);

        // Panel des boutons de contrôle
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3,
                (hFrame.getTitleBarHeight() - 25) / 2));
        controlsPanel.setOpaque(false);

        // Bouton iconifier
        if (frame.isIconifiable()) {
            iconButton = new HButton(minimizeIcon(hFrame.getForeground()));
            styleControlButton(iconButton, hFrame.getFrameStyle().getIconifyButtonStyle());
            iconButton.addActionListener(e -> {
                try {
                    frame.setIcon(!frame.isIcon());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            controlsPanel.add(iconButton);
        }

        // Bouton maximiser
        if (frame.isMaximizable()) {
            maxButton = new HButton(maximizeIcon(hFrame.getForeground()));
            styleControlButton(maxButton, hFrame.getFrameStyle().getMaximizeButtonStyle());
            maxButton.addActionListener(e -> {
                try {
                    if (frame.isMaximum()) {
                        frame.setMaximum(false);
                    } else {
                        frame.setMaximum(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            controlsPanel.add(maxButton);
        }

        // Bouton fermer
        if (frame.isClosable()) {
            closeButton = new HButton(closeIcon(hFrame.getForeground()));
            styleControlButton(closeButton, hFrame.getFrameStyle().getCloseButtonStyle());
            closeButton.addActionListener(e -> {
                try {
                    frame.setClosed(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            controlsPanel.add(closeButton);
        }

        titlePane.add(leftPanel, BorderLayout.WEST);
        titlePane.add(controlsPanel, BorderLayout.EAST);

        // Gestion du drag pour déplacer la fenêtre
        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Stocker la position absolue de la souris sur l'écran
                mouseScreenX = e.getXOnScreen();
                mouseScreenY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    if (!frame.isMaximum()) {
                        // Calculer le déplacement absolu
                        int deltaX = e.getXOnScreen() - mouseScreenX;
                        int deltaY = e.getYOnScreen() - mouseScreenY;

                        // Mettre à jour la position de la fenêtre
                        frame.setLocation(
                                frame.getX() + deltaX,
                                frame.getY() + deltaY
                        );

                        // Mettre à jour les positions de référence
                        mouseScreenX = e.getXOnScreen();
                        mouseScreenY = e.getYOnScreen();
                    }
                } catch (Exception ex) {
                    // Ignore
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double-clic pour maximiser/restaurer
                if (e.getClickCount() == 2 && frame.isMaximizable()) {
                    try {
                        frame.setMaximum(!frame.isMaximum());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        titlePane.addMouseListener(dragListener);
        titlePane.addMouseMotionListener(dragListener);

        // Listener pour mettre à jour le titre
        frame.addPropertyChangeListener(JInternalFrame.TITLE_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                titleLabel.setText((String) evt.getNewValue());
            }
        });
    }

    /**
     * Applique le style à un bouton de contrôle.
     *
     * @param button le bouton à styliser
     * @param style le style HButtonStyle à appliquer
     */
    private void styleControlButton(HButton button, HButtonStyle style) {
        int tbh = ((HInternalFrame) frame).getTitleBarHeight();
        button.setButtonStyle(style);
        button.setPreferredSize(new Dimension(30, (tbh * 85) / 100));
        button.setCornerRadius(6);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Forcer la peinture centrée de l'icône
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
    }

    /**
     * Dessine le composant HInternalFrame avec tous ses effets visuels.
     *
     * @param g le contexte graphique
     * @param c le composant à dessiner
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        HInternalFrame hFrame = (HInternalFrame) c;
        Graphics2D g2 = (Graphics2D) g.create();

        int width = c.getWidth();
        int height = c.getHeight();
        int radius = hFrame.getCornerRadius();

        // Activation de l'antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Dessin de l'ombre
        HShadow shadow = hFrame.getShadow();
        if (shadow != null) {
            shadow.paint(g2, c, width, height, radius);
        }

        // Fond de la fenêtre
        g2.setColor(hFrame.getFrameStyle().getBackgroundColor());
        g2.fillRoundRect(0, 0, width, height, radius, radius);
        g2.dispose();

        // Appel du rendu parent pour dessiner les composants
        super.paint(g, c);
    }

    private static Icon closeIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();
                int iconSize = Math.min(width, height) - 15; // Taille de l'icône avec marge

                // Calculer la position pour centrer l'icône
                int centerX = (width - iconSize) / 3;
                int centerY = (height - iconSize) / 3;

                // Dessiner le X centré
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));

                // Première diagonale
                g2d.drawLine(
                        centerX,
                        centerY,
                        centerX + iconSize,
                        centerY + iconSize
                );

                // Seconde diagonale
                g2d.drawLine(
                        centerX + iconSize,
                        centerY,
                        centerX,
                        centerY + iconSize
                );

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    private Icon maximizeIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();
                int iconSize = Math.min(width, height) - 15; // Taille de l'icône 

                // Calculer la position pour centrer l'icône
                int centerX = (width - iconSize) / 3;
                int centerY = (height - iconSize) / 3;

                // Dessiner le carré centré
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(centerX, centerY, iconSize, iconSize);

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    private static Icon minimizeIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();
                int iconSize = Math.min(width, height) - 15; // Taille de l'icône avec marge

                // Calculer la position pour centrer l'icône
                int centerX = (width - iconSize) / 3;
                int centerY = (height - iconSize) / 3;

                // Dessiner la ligne horizontale centrée
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2));

                int lineY = centerY + iconSize / 2;
                g2d.drawLine(
                        centerX,
                        lineY,
                        centerX + iconSize,
                        lineY
                );

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        };
    }

    /**
     * Désinstalle l'interface utilisateur du composant.
     *
     * @param c le composant
     */
    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
    }
}
