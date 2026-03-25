/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HOptionPaneStyle;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * OptionPane personnalisé avec design moderne.
 * Alternative stylisée à JOptionPane avec animations, bordures arrondies,
 * icônes personnalisées et boutons modernes.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JOptionPane
 * @see HOptionPaneStyle
 */
public class HOptionPane {
    
    private static HOptionPaneStyle defaultStyle = HOptionPaneStyle.PRIMARY;
    private static int cornerRadius = 15;
    private static boolean animationsEnabled = true;
    
    // Types de messages
    public static final int INFORMATION_MESSAGE = 0;
    public static final int WARNING_MESSAGE = 1;
    public static final int ERROR_MESSAGE = 2;
    public static final int QUESTION_MESSAGE = 3;
    public static final int SUCCESS_MESSAGE = 4;
    
    // Types d'options
    public static final int DEFAULT_OPTION = 0;
    public static final int YES_NO_OPTION = 1;
    public static final int YES_NO_CANCEL_OPTION = 2;
    public static final int OK_CANCEL_OPTION = 3;
    
    // Valeurs de retour
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 0;
    public static final int CLOSED_OPTION = -1;
    
    /**
     * Affiche un message d'information.
     */
    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        showMessageDialog(parent, message, title, messageType, getStyleForType(messageType));
    }
    
    /**
     * Affiche un message d'information avec style personnalisé.
     */
    public static void showMessageDialog(Component parent, Object message, String title, 
                                        int messageType, HOptionPaneStyle style) {
        ModernDialog dialog = new ModernDialog(
            getFrameForComponent(parent),
            title,
            message,
            messageType,
            DEFAULT_OPTION,
            style
        );
        dialog.setVisible(true);
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation.
     */
    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, QUESTION_MESSAGE);
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation avec type de message.
     */
    public static int showConfirmDialog(Component parent, Object message, String title, 
                                       int optionType, int messageType) {
        return showConfirmDialog(parent, message, title, optionType, messageType, getStyleForType(messageType));
    }
    
    /**
     * Affiche une boîte de dialogue de confirmation avec style personnalisé.
     */
    public static int showConfirmDialog(Component parent, Object message, String title, 
                                       int optionType, int messageType, HOptionPaneStyle style) {
        ModernDialog dialog = new ModernDialog(
            getFrameForComponent(parent),
            title,
            message,
            messageType,
            optionType,
            style
        );
        dialog.setVisible(true);
        return dialog.getResult();
    }
    
    /**
     * Affiche une boîte de dialogue de saisie.
     */
    public static String showInputDialog(Component parent, Object message, String title, int messageType) {
        return showInputDialog(parent, message, title, messageType, getStyleForType(messageType));
    }
    
    /**
     * Affiche une boîte de dialogue de saisie avec style personnalisé.
     */
    public static String showInputDialog(Component parent, Object message, String title, 
                                        int messageType, HOptionPaneStyle style) {
        ModernInputDialog dialog = new ModernInputDialog(
            getFrameForComponent(parent),
            title,
            message,
            messageType,
            style
        );
        dialog.setVisible(true);
        return dialog.getInputValue();
    }
    
    /**
     * Retourne le style par défaut selon le type de message.
     */
    private static HOptionPaneStyle getStyleForType(int messageType) {
        switch (messageType) {
            case INFORMATION_MESSAGE: return HOptionPaneStyle.INFO;
            case WARNING_MESSAGE: return HOptionPaneStyle.WARNING;
            case ERROR_MESSAGE: return HOptionPaneStyle.DANGER;
            case QUESTION_MESSAGE: return HOptionPaneStyle.QUESTION;
            case SUCCESS_MESSAGE: return HOptionPaneStyle.SUCCESS;
            default: return defaultStyle;
        }
    }
    
    /**
     * Retourne le Frame parent d'un composant.
     */
    private static Frame getFrameForComponent(Component parent) {
        if (parent == null) return null;
        if (parent instanceof Frame) return (Frame) parent;
        return (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
    }
    
    // Getters / Setters globaux
    public static void setDefaultStyle(HOptionPaneStyle style) {
        defaultStyle = style;
    }
    
    public static HOptionPaneStyle getDefaultStyle() {
        return defaultStyle;
    }
    
    public static void setCornerRadius(int radius) {
        cornerRadius = radius;
    }
    
    public static int getCornerRadius() {
        return cornerRadius;
    }
    
    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }
    
    public static boolean isAnimationsEnabled() {
        return animationsEnabled;
    }
    
    /**
     * Dialog moderne interne.
     */
    private static class ModernDialog extends JDialog {
        private int result = CLOSED_OPTION;
        private HOptionPaneStyle style;
        private int messageType;
        private float opacity = 0f;
        private Timer fadeTimer;
        
        public ModernDialog(Frame owner, String title, Object message, 
                          int messageType, int optionType, HOptionPaneStyle style) {
            super(owner, title, true);
            this.style = style;
            this.messageType = messageType;
            
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));
            
            initComponents(message, optionType);
            pack();
            setLocationRelativeTo(owner);
            
            if (animationsEnabled) {
                animateFadeIn();
            } else {
                opacity = 1f;
            }
        }
        
        private void initComponents(Object message, int optionType) {
            JPanel mainPanel = new JPanel(new BorderLayout(0, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Appliquer l'opacité
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                    
                    // Ombre
                    paintShadow(g2, getWidth(), getHeight());
                    
                    // Fond arrondi
                    g2.setColor(style.getBackgroundColor());
                    RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                        10, 10, getWidth() - 20, getHeight() - 20, cornerRadius, cornerRadius
                    );
                    g2.fill(roundRect);
                    
                    g2.dispose();
                }
                
                private void paintShadow(Graphics2D g2, int width, int height) {
                    int shadowSize = 10;
                    for (int i = 0; i < shadowSize; i++) {
                        float alpha = (shadowSize - i) / (float) shadowSize * 0.15f;
                        g2.setColor(new Color(0, 0, 0, (int) (alpha * 255 * opacity)));
                        
                        RoundRectangle2D shadow = new RoundRectangle2D.Float(
                            10 - i, 10 - i,
                            width - 20 + i * 2, height - 20 + i * 2,
                            cornerRadius + i, cornerRadius + i
                        );
                        g2.draw(shadow);
                    }
                }
            };
            
            mainPanel.setOpaque(false);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // En-tête avec icône
            JPanel headerPanel = createHeaderPanel();
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            
            // Contenu
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            JLabel messageLabel = new JLabel("<html><body style='width: 350px'>" + message + "</body></html>");
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            messageLabel.setForeground(style.getTextColor());
            contentPanel.add(messageLabel, BorderLayout.CENTER);
            
            mainPanel.add(contentPanel, BorderLayout.CENTER);
            
            // Boutons
            JPanel buttonPanel = createButtonPanel(optionType);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(mainPanel);
        }
        
        private JPanel createHeaderPanel() {
            JPanel headerPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(style.getHeaderBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, 0);
                    
                    g2.dispose();
                }
            };
            
            headerPanel.setOpaque(false);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            
            // Icône
            JLabel iconLabel = new JLabel(createIcon());
            headerPanel.add(iconLabel, BorderLayout.WEST);
            
            // Titre
            JLabel titleLabel = new JLabel(getTitle());
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(style.getTextColor());
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
            headerPanel.add(titleLabel, BorderLayout.CENTER);
            
            return headerPanel;
        }
        
        private Icon createIcon() {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(style.getIconColor());
                    
                    int size = 32;
                    int cx = x + size / 2;
                    int cy = y + size / 2;
                    
                    switch (messageType) {
                        case INFORMATION_MESSAGE:
                        case SUCCESS_MESSAGE:
                            // Cercle avec "i" ou checkmark
                            g2.setStroke(new BasicStroke(3f));
                            g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                            if (messageType == SUCCESS_MESSAGE) {
                                // Checkmark
                                g2.drawLine(x + 8, y + 16, x + 14, y + 22);
                                g2.drawLine(x + 14, y + 22, x + 24, y + 10);
                            } else {
                                // "i"
                                g2.fillOval(cx - 2, y + 8, 4, 4);
                                g2.fillRoundRect(cx - 2, y + 16, 4, 10, 2, 2);
                            }
                            break;
                        case WARNING_MESSAGE:
                            // Triangle avec "!"
                            Path2D triangle = new Path2D.Float();
                            triangle.moveTo(cx, y + 4);
                            triangle.lineTo(x + size - 4, y + size - 4);
                            triangle.lineTo(x + 4, y + size - 4);
                            triangle.closePath();
                            g2.setStroke(new BasicStroke(3f));
                            g2.draw(triangle);
                            g2.fillOval(cx - 2, y + 12, 4, 4);
                            g2.fillRoundRect(cx - 2, y + 18, 4, 8, 2, 2);
                            break;
                        case ERROR_MESSAGE:
                            // Cercle avec "X"
                            g2.setStroke(new BasicStroke(3f));
                            g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                            g2.drawLine(x + 10, y + 10, x + 22, y + 22);
                            g2.drawLine(x + 22, y + 10, x + 10, y + 22);
                            break;
                        case QUESTION_MESSAGE:
                            // Cercle avec "?"
                            g2.setStroke(new BasicStroke(3f));
                            g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                            g2.setStroke(new BasicStroke(2.5f));
                            Arc2D arc = new Arc2D.Float(x + 10, y + 8, 12, 12, 20, 200, Arc2D.OPEN);
                            g2.draw(arc);
                            g2.fillOval(cx - 2, y + 24, 4, 4);
                            break;
                    }
                    
                    g2.dispose();
                }
                
                @Override
                public int getIconWidth() { return 32; }
                @Override
                public int getIconHeight() { return 32; }
            };
        }
        
        private JPanel createButtonPanel(int optionType) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            buttonPanel.setOpaque(false);
            
            HButtonStyle btnStyle = getButtonStyleForMessageType();
            
            switch (optionType) {
                case YES_NO_OPTION:
                    addButton(buttonPanel, "Oui", YES_OPTION, btnStyle);
                    addButton(buttonPanel, "Non", NO_OPTION, HButtonStyle.SECONDARY);
                    break;
                case YES_NO_CANCEL_OPTION:
                    addButton(buttonPanel, "Oui", YES_OPTION, btnStyle);
                    addButton(buttonPanel, "Non", NO_OPTION, HButtonStyle.SECONDARY);
                    addButton(buttonPanel, "Annuler", CANCEL_OPTION, HButtonStyle.SECONDARY);
                    break;
                case OK_CANCEL_OPTION:
                    addButton(buttonPanel, "OK", OK_OPTION, btnStyle);
                    addButton(buttonPanel, "Annuler", CANCEL_OPTION, HButtonStyle.SECONDARY);
                    break;
                default:
                    addButton(buttonPanel, "OK", OK_OPTION, btnStyle);
                    break;
            }
            
            return buttonPanel;
        }
        
        private HButtonStyle getButtonStyleForMessageType() {
            switch (messageType) {
                case SUCCESS_MESSAGE: return HButtonStyle.SUCCESS;
                case ERROR_MESSAGE: return HButtonStyle.DANGER;
                case WARNING_MESSAGE: return HButtonStyle.WARNING;
                case INFORMATION_MESSAGE: return HButtonStyle.INFO;
                default: return HButtonStyle.PRIMARY;
            }
        }
        
        private void addButton(JPanel panel, String text, int resultValue, HButtonStyle btnStyle) {
            HButton button = new HButton(text);
            button.setButtonStyle(btnStyle);
            button.setPreferredSize(new Dimension(100, 35));
            button.addActionListener(e -> {
                result = resultValue;
                dispose();
            });
            panel.add(button);
        }
        
        private void animateFadeIn() {
            opacity = 0f;
            long startTime = System.currentTimeMillis();
            
            fadeTimer = new Timer(16, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    opacity = Math.min(1f, elapsed / 200f);
                    repaint();
                    
                    if (opacity >= 1f) {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            
            fadeTimer.start();
        }
        
        public int getResult() {
            return result;
        }
    }
    
    /**
     * Dialog d'input moderne.
     */
    private static class ModernInputDialog extends ModernDialog {
        private JTextField inputField;
        private String inputValue = null;
        
        public ModernInputDialog(Frame owner, String title, Object message, 
                               int messageType, HOptionPaneStyle style) {
            super(owner, title, message, messageType, OK_CANCEL_OPTION, style);
        }
        
        protected void initComponents(Object message, int optionType) {
            // Réutiliser la logique parent mais ajouter un champ de saisie
            super.initComponents(message, optionType);
            
            // Ajouter le champ de texte
            JPanel contentPanel = (JPanel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
            
            inputField = new JTextField(20);
            inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            
            contentPanel.add(inputField, BorderLayout.SOUTH);
            inputField.requestFocusInWindow();
        }
        
        @Override
        public void dispose() {
            if (getResult() == OK_OPTION) {
                inputValue = inputField.getText();
            }
            super.dispose();
        }
        
        public String getInputValue() {
            return inputValue;
        }
    }
}