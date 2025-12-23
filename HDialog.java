/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HDialogUI;
import hcomponents.vues.HDialogStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Composant HDialog - Une boîte de dialogue Swing personnalisée avec design moderne.
 * Étend JDialog pour offrir des fonctionnalités avancées : styles prédéfinis,
 * coins arrondis, ombres, animations fluides et overlay semi-transparent.
 * 
 * <p>Ce composant fournit une API cohérente avec les autres composants HComponents
 * pour une expérience utilisateur uniforme.</p>
 * 
 * @author FIDELE
 * @version 1.0
 * @see JDialog
 * @see HDialogStyle
 * @see HDialogUI
 */
public class HDialog extends JDialog {

    /** Bordure personnalisée du dialog */
    private HBorder hBorder;
    
    /** Ombre personnalisée du dialog */
    private HShadow hShadow;
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 20;
    
    /** Style visuel appliqué au dialog */
    private HDialogStyle dialogStyle = HDialogStyle.PRIMARY;
    
    /** Panel overlay semi-transparent en arrière-plan */
    private JPanel overlayPanel;
    
    /** Panel principal contenant le contenu du dialog */
    private JPanel contentPanel;
    
    /** Panel de titre (header) */
    private JPanel headerPanel;
    
    /** Panel des boutons d'action (footer) */
    private JPanel footerPanel;
    
    /** Label pour le titre du dialog */
    private JLabel titleLabel;
    
    /** Bouton de fermeture dans le header */
    private JButton closeButton;
    
    /** Opacité de l'overlay (0.0 à 1.0) */
    private float overlayOpacity = 0.5f;
    
    /** Indique si l'overlay est cliquable pour fermer le dialog */
    private boolean closeOnOverlayClick = true;
    
    /** Indique si le dialog affiche un header */
    private boolean showHeader = true;
    
    /** Indique si le dialog affiche un footer */
    private boolean showFooter = true;

    /**
     * Constructeur avec parent Frame et titre.
     * 
     * @param parent le Frame parent
     * @param title le titre du dialog
     */
    public HDialog(Frame parent, String title) {
        super(parent, title, true);
        init();
    }

    /**
     * Constructeur avec parent Dialog et titre.
     * 
     * @param parent le Dialog parent
     * @param title le titre du dialog
     */
    public HDialog(Dialog parent, String title) {
        super(parent, title, true);
        init();
    }

    /**
     * Constructeur par défaut.
     * Crée un dialog sans parent ni titre.
     */
    public HDialog() {
        this((Frame) null, "");
    }

    /**
     * Constructeur avec titre uniquement.
     * 
     * @param title le titre du dialog
     */
    public HDialog(String title) {
        this((Frame) null, title);
    }

    /**
     * Initialise le dialog avec ses composants et sa structure.
     */
    private void init() {
        // Configuration de base pour transparence totale
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        // Support de la transparence (Java 7+)
        try {
            Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
            java.lang.reflect.Method setWindowOpaque = awtUtilitiesClass.getMethod(
                "setWindowOpaque", Window.class, boolean.class);
            setWindowOpaque.invoke(null, this, false);
        } catch (Exception e) {
            // Si la méthode n'existe pas, on continue (Java 11+)
        }
        
        setLayout(new BorderLayout());

        // Création de l'overlay
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fond semi-transparent
                g2.setColor(new Color(0, 0, 0, (int)(overlayOpacity * 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                g2.dispose();
            }
        };
        overlayPanel.setOpaque(false);
        overlayPanel.setLayout(new GridBagLayout());

        // Gestion du clic sur l'overlay
        overlayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (closeOnOverlayClick && !contentPanel.getBounds().contains(e.getPoint())) {
                    closeWithAnimation();
                }
            }
        });

        // Création du panel principal avec transparence
        contentPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                // Ne pas appeler super.paintComponent pour éviter le fond par défaut
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Dessin de l'ombre
                if (hShadow != null) {
                    hShadow.paint(g2, this, getWidth(), getHeight(), cornerRadius);
                }
                
                // Fond du dialog avec dégradé
                Color baseColor = dialogStyle.getBaseColor();
                GradientPaint gradient = new GradientPaint(
                    0, 0, baseColor.brighter(),
                    0, getHeight(), baseColor
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                
                // Bordure personnalisée
                if (hBorder != null) {
                    hBorder.paint(g2, this, getWidth(), getHeight(), cornerRadius);
                }
                
                g2.dispose();
            }
            
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        contentPanel.setPreferredSize(new Dimension(400, 300));

        // Création du header
        createHeader();

        // Création du footer
        createFooter();

        // Ajout du content panel à l'overlay
        overlayPanel.add(contentPanel);

        // Ajout de l'overlay au dialog
        add(overlayPanel, BorderLayout.CENTER);
    }

    /**
     * Crée le panel header avec titre et bouton de fermeture.
     */
    private void createHeader() {
        headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout(10, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Titre
        titleLabel = new JLabel(getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(dialogStyle.getTextColor());

        // Bouton de fermeture
        closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 24));
        closeButton.setForeground(dialogStyle.getTextColor());
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(30, 30));
        
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(dialogStyle.getHoverColor());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(dialogStyle.getTextColor());
            }
        });
        
        closeButton.addActionListener(e -> closeWithAnimation());

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(closeButton, BorderLayout.EAST);

        if (showHeader) {
            contentPanel.add(headerPanel, BorderLayout.NORTH);
        }
    }

    /**
     * Crée le panel footer pour les actions.
     */
    private void createFooter() {
        footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        if (showFooter) {
            contentPanel.add(footerPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * Affiche le dialog avec animation.
     */
    public void showWithAnimation() {
        pack();
        setLocationRelativeTo(getParent());
        
        // Animation d'apparition
        HDialogUI.animateShow(this, contentPanel);
        
        setVisible(true);
    }

    /**
     * Ferme le dialog avec animation.
     */
    public void closeWithAnimation() {
        HDialogUI.animateClose(this, contentPanel, () -> {
            setVisible(false);
            dispose();
        });
    }

    /**
     * Définit le contenu principal du dialog.
     * 
     * @param content le composant à afficher dans le corps du dialog
     */
    public void setContent(JComponent content) {
        // Retrait de l'ancien contenu (sauf header et footer)
        for (Component comp : contentPanel.getComponents()) {
            if (comp != headerPanel && comp != footerPanel) {
                contentPanel.remove(comp);
            }
        }
        
        content.setOpaque(false);
        contentPanel.add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Ajoute un bouton au footer.
     * 
     * @param button le bouton à ajouter
     */
    public void addFooterButton(JButton button) {
        footerPanel.add(button);
        revalidate();
        repaint();
    }

    // ========== GETTERS ET SETTERS ==========

    public HBorder getHBorder() {
        return hBorder;
    }

    public void setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
    }

    public HShadow getShadow() {
        return hShadow;
    }

    public void setShadow(HShadow shadow) {
        this.hShadow = shadow;
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public HDialogStyle getDialogStyle() {
        return dialogStyle;
    }

    public void setDialogStyle(HDialogStyle style) {
        this.dialogStyle = style;
        titleLabel.setForeground(style.getTextColor());
        closeButton.setForeground(style.getTextColor());
        repaint();
    }

    public float getOverlayOpacity() {
        return overlayOpacity;
    }

    public void setOverlayOpacity(float opacity) {
        this.overlayOpacity = Math.max(0f, Math.min(1f, opacity));
        repaint();
    }

    public boolean isCloseOnOverlayClick() {
        return closeOnOverlayClick;
    }

    public void setCloseOnOverlayClick(boolean closeOnOverlayClick) {
        this.closeOnOverlayClick = closeOnOverlayClick;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        if (showHeader && headerPanel.getParent() == null) {
            contentPanel.add(headerPanel, BorderLayout.NORTH, 0);
        } else if (!showHeader && headerPanel.getParent() != null) {
            contentPanel.remove(headerPanel);
        }
        revalidate();
        repaint();
    }

    public boolean isShowFooter() {
        return showFooter;
    }

    public void setShowFooter(boolean showFooter) {
        this.showFooter = showFooter;
        if (showFooter && footerPanel.getParent() == null) {
            contentPanel.add(footerPanel, BorderLayout.SOUTH);
        } else if (!showFooter && footerPanel.getParent() != null) {
            contentPanel.remove(footerPanel);
        }
        revalidate();
        repaint();
    }

    public JPanel getHeaderPanel() {
        return headerPanel;
    }

    public JPanel getFooterPanel() {
        return footerPanel;
    }

    public void setDialogSize(int width, int height) {
        contentPanel.setPreferredSize(new Dimension(width, height));
        pack();
    }

    /**
     * Méthode factory pour créer un HDialog avec un style prédéfini.
     * 
     * @param parent le parent du dialog
     * @param title le titre du dialog
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HDialog configurée avec le style spécifié
     */
    public static HDialog withStyle(Frame parent, String title, HDialogStyle style) {
        HDialog dialog = new HDialog(parent, title);
        dialog.setDialogStyle(style);
        return dialog;
    }

    /**
     * Méthode factory pour créer un HDialog simple avec message.
     * 
     * @param parent le parent du dialog
     * @param title le titre du dialog
     * @param message le message à afficher
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HDialog configurée
     */
    public static HDialog createMessage(Frame parent, String title, String message, HDialogStyle style) {
        HDialog dialog = withStyle(parent, title, style);
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(style.getTextColor());
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        dialog.setContent(messagePanel);
        
        // Bouton OK
        HButton okButton = HButton.withStyle("OK", 
            style == HDialogStyle.PRIMARY ? HButtonStyle.PRIMARY : 
            style == HDialogStyle.DANGER ? HButtonStyle.DANGER :
            style == HDialogStyle.SUCCESS ? HButtonStyle.SUCCESS :
            HButtonStyle.PRIMARY
        );
        okButton.addActionListener(e -> dialog.closeWithAnimation());
        dialog.addFooterButton(okButton);
        
        dialog.setDialogSize(400, 250);
        
        return dialog;
    }
}