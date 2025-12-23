/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HBasicWindowUI;
import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HWindowStyle;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.shadow.HShadow;
import java.awt.*;
import javax.swing.*;

/**
 * Composant HWindow - Une fenêtre Swing personnalisée sans décoration avec design moderne.
 * Étend JWindow pour offrir des fonctionnalités avancées : coins arrondis, ombres,
 * bordures personnalisables, dégradés, animations et utilitaires intégrés.
 *
 * <p>Ce composant inclut des fonctionnalités prêtes à l'emploi pour :
 * splash screens, notifications, tooltips, popups et fenêtres flottantes.</p>
 *
 *
 * @author FIDELE
 * @version 1.1
 * @see JWindow
 * @see HWindowStyle
 * @see HBasicWindowUI
 */
public class HWindow extends JWindow {

    /** Panel racine pour le rendu personnalisé */
    private JPanel rootPane;

    /** Bordure personnalisée de la fenêtre */
    private HBorder hBorder;

    /** Ombre personnalisée de la fenêtre */
    private HShadow hShadow;

    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 20;

    /** Style visuel appliqué à la fenêtre */
    private HWindowStyle windowStyle = HWindowStyle.PRIMARY;

    /** Opacité de la fenêtre (0.0 à 1.0) */
    private float windowOpacity = 1.0f;

    /** Indique si la fenêtre peut être déplacée par drag */
    private boolean draggable = false;

    /** Position X de la souris lors du drag */
    private int mouseX;

    /** Position Y de la souris lors du drag */
    private int mouseY;

    /** Timer pour l'auto-fermeture */
    private Timer autoCloseTimer;

    /** Timer pour les animations de fondu */
    private Timer fadeTimer;

    /** Opacité actuelle lors des animations */
    private float currentOpacity = 0f;

    /**
     * Constructeur par défaut.
     * Crée une fenêtre sans propriétaire.
     */
    public HWindow() {
        this((Frame) null);
    }

    /**
     * Constructeur avec fenêtre propriétaire.
     *
     * @param owner la fenêtre propriétaire
     */
    public HWindow(Frame owner) {
        super(owner);
        init();
    }

    /**
     * Constructeur avec fenêtre propriétaire.
     *
     * @param owner la fenêtre propriétaire
     */
    public HWindow(Window owner) {
        super(owner);
        init();
    }

    /**
     * Constructeur avec contexte graphique.
     *
     * @param owner la fenêtre propriétaire
     * @param gc la configuration graphique
     */
    public HWindow(Window owner, GraphicsConfiguration gc) {
        super(owner, gc);
        init();
    }

    /**
     * Initialise la fenêtre avec ses propriétés de base.
     */
    private void init() {
        // Rendre la fenêtre transparente pour permettre les coins arrondis
        setBackground(new Color(0, 0, 0, 0));

        // Créer le panel racine avec rendu personnalisé
        rootPane = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                HBasicWindowUI.paintWindow(g, this, HWindow.this);
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        rootPane.setOpaque(false);

        // Configurer le content pane
        setContentPane(rootPane);

        // Appliquer l'opacité si supportée
        try {
            if (isOpacitySupported()) {
                setOpacity(windowOpacity);
            }
        } catch (Exception e) {
            // Ignore si non supporté
        }
    }

    /**
     * Vérifie si l'opacité est supportée par la plateforme.
     *
     * @return true si l'opacité est supportée
     */
    private boolean isOpacitySupported() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        return gc != null && gc.getDevice().isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.TRANSLUCENT);
    }

    /**
     * Définit le content pane de la fenêtre.
     * Encapsule le contenu dans le rootPane personnalisé.
     *
     * @param contentPane le nouveau content pane
     */
    @Override
    public void setContentPane(Container contentPane) {
        if (rootPane != null && contentPane != rootPane) {
            rootPane.removeAll();
            rootPane.add(contentPane, BorderLayout.CENTER);
            rootPane.revalidate();
            rootPane.repaint();
        } else {
            super.setContentPane(contentPane);
        }
    }

    /**
     * Retourne le content pane réel (sans le rootPane wrapper).
     *
     * @return le content pane utilisateur
     */
    @Override
    public Container getContentPane() {
        if (rootPane != null && rootPane.getComponentCount() > 0) {
            Component comp = rootPane.getComponent(0);
            if (comp instanceof Container) {
                return (Container) comp;
            }
        }
        return super.getContentPane();
    }

    // ========== MÉTHODES UTILITAIRES INTÉGRÉES ==========

    /**
     * Configure la fenêtre comme un splash screen.
     * Ajoute automatiquement un titre, sous-titre et barre de progression.
     *
     * @param title le titre principal
     * @param subtitle le sous-titre (peut être null)
     * @param showProgress true pour afficher une barre de progression indéterminée
     * @return cette instance pour chaînage
     */
    public HWindow asSplashScreen(String title, String subtitle, boolean showProgress) {
        setSize(400, 250);
        setCornerRadius(25);
        setWindowStyle(HWindowStyle.SPLASH);
//        setShadow(new HShadow(new Color(0, 0, 0, 100), 15, 0, 5));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(13, 110, 253));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);

        if (subtitle != null) {
            JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitleLabel.setForeground(Color.GRAY);
            centerPanel.add(subtitleLabel, BorderLayout.CENTER);
        }

        content.add(titleLabel, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);

        if (showProgress) {
            JProgressBar progress = new JProgressBar();
            progress.setIndeterminate(true);
            content.add(progress, BorderLayout.SOUTH);
        }

        setContentPane(content);
        centerOnScreen();

        return this;
    }

    /**
     * Configure la fenêtre comme une notification.
     *
     * @param title le titre de la notification
     * @param message le message
     * @param style le style de notification (SUCCESS, DANGER, WARNING, INFO)
     * @param showCloseButton true pour afficher un bouton de fermeture
     * @return cette instance pour chaînage
     */
    public HWindow asNotification(String title, String message, HWindowStyle style, boolean showCloseButton) {
        setSize(350, 120);
        setCornerRadius(15);
        setWindowStyle(style);
//        setShadow(new HShadow(new Color(0, 0, 0, 80), 10, 0, 3));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Color.DARK_GRAY);

        content.add(titleLabel, BorderLayout.NORTH);
        content.add(messageLabel, BorderLayout.CENTER);

        if (showCloseButton) {
            HButton closeButton = HButton.withStyle("OK", HButtonStyle.SECONDARY);
            closeButton.setPreferredSize(new Dimension(80, 30));
            closeButton.addActionListener(e -> dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            buttonPanel.add(closeButton);

            content.add(buttonPanel, BorderLayout.SOUTH);
        }

        setContentPane(content);
        positionBottomRight(20, 60);

        return this;
    }

    /**
     * Configure la fenêtre comme un tooltip personnalisé.
     *
     * @param text le texte du tooltip (supporte HTML)
     * @return cette instance pour chaînage
     */
    public HWindow asTooltip(String text) {
        setSize(300, 80);
        setCornerRadius(10);
        setWindowStyle(HWindowStyle.TOOLTIP);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        content.add(label);
        setContentPane(content);

        return this;
    }

    /**
     * Configure la fenêtre comme un popup personnalisé avec titre et contenu.
     *
     * @param title le titre du popup
     * @param contentPanel le panel de contenu personnalisé
     * @param width la largeur
     * @param height la hauteur
     * @return cette instance pour chaînage
     */
    public HWindow asPopup(String title, JPanel contentPanel, int width, int height) {
        setSize(width, height);
        setCornerRadius(20);
//        setShadow(new HShadow(new Color(0, 0, 0, 120), 20, 0, 8));

        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            if (windowStyle == HWindowStyle.DARK) {
                titleLabel.setForeground(Color.WHITE);
            }
            mainContent.add(titleLabel, BorderLayout.NORTH);
        }

        contentPanel.setOpaque(false);
        mainContent.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainContent);
        centerOnScreen();

        return this;
    }

    /**
     * Configure la fenêtre comme un dialog de confirmation.
     *
     * @param title le titre du dialog
     * @param message le message
     * @param onConfirm action à exécuter lors de la confirmation
     * @param onCancel action à exécuter lors de l'annulation (peut être null)
     * @return cette instance pour chaînage
     */
    public HWindow asConfirmDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        setSize(400, 180);
        setCornerRadius(18);
//        setShadow(new HShadow(new Color(0, 0, 0, 100), 15, 0, 5));

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        HButton confirmButton = HButton.withStyle("Confirmer", HButtonStyle.SUCCESS);
        confirmButton.setPreferredSize(new Dimension(100, 35));
        confirmButton.addActionListener(e -> {
            if (onConfirm != null) onConfirm.run();
            dispose();
        });

        HButton cancelButton = HButton.withStyle("Annuler", HButtonStyle.SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
            dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        content.add(titleLabel, BorderLayout.NORTH);
        content.add(messageArea, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(content);
        centerOnScreen();

        return this;
    }

    /**
     * Configure la fenêtre comme un loading screen.
     *
     * @param message le message de chargement
     * @return cette instance pour chaînage
     */
    public HWindow asLoadingScreen(String message) {
        setSize(300, 150);
        setCornerRadius(20);
        setWindowStyle(HWindowStyle.PRIMARY);
//        setShadow(new HShadow(new Color(0, 0, 0, 100), 15, 0, 5));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);

        content.add(messageLabel, BorderLayout.CENTER);
        content.add(progress, BorderLayout.SOUTH);

        setContentPane(content);
        centerOnScreen();

        return this;
    }

    // ========== MÉTHODES DE POSITIONNEMENT ==========

    /**
     * Centre la fenêtre sur l'écran.
     *
     * @return cette instance pour chaînage
     */
    public HWindow centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(
            (screenSize.width - windowSize.width) / 2,
            (screenSize.height - windowSize.height) / 2
        );
        return this;
    }

    /**
     * Centre la fenêtre par rapport à un composant parent.
     *
     * @param parent le composant parent
     * @return cette instance pour chaînage
     */
    public HWindow centerRelativeTo(Component parent) {
        setLocationRelativeTo(parent);
        return this;
    }

    /**
     * Positionne la fenêtre en haut à gauche.
     *
     * @param marginX marge horizontale
     * @param marginY marge verticale
     * @return cette instance pour chaînage
     */
    public HWindow positionTopLeft(int marginX, int marginY) {
        setLocation(marginX, marginY);
        return this;
    }

    /**
     * Positionne la fenêtre en haut à droite.
     *
     * @param marginX marge depuis le bord droit
     * @param marginY marge depuis le haut
     * @return cette instance pour chaînage
     */
    public HWindow positionTopRight(int marginX, int marginY) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width - getWidth() - marginX, marginY);
        return this;
    }

    /**
     * Positionne la fenêtre en bas à gauche.
     *
     * @param marginX marge horizontale
     * @param marginY marge depuis le bas
     * @return cette instance pour chaînage
     */
    public HWindow positionBottomLeft(int marginX, int marginY) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(marginX, screenSize.height - getHeight() - marginY);
        return this;
    }

    /**
     * Positionne la fenêtre en bas à droite.
     *
     * @param marginX marge depuis le bord droit
     * @param marginY marge depuis le bas
     * @return cette instance pour chaînage
     */
    public HWindow positionBottomRight(int marginX, int marginY) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
            screenSize.width - getWidth() - marginX,
            screenSize.height - getHeight() - marginY
        );
        return this;
    }

    /**
     * Positionne la fenêtre à des coordonnées personnalisées.
     *
     * @param x position X
     * @param y position Y
     * @return cette instance pour chaînage
     */
    public HWindow positionAt(int x, int y) {
        setLocation(x, y);
        return this;
    }

    // ========== ANIMATIONS ET EFFETS ==========

    /**
     * Affiche la fenêtre avec une animation de fondu entrant.
     *
     * @param durationMs durée de l'animation en millisecondes
     * @return cette instance pour chaînage
     */
    public HWindow showWithFadeIn(int durationMs) {
        if (!isOpacitySupported()) {
            setVisible(true);
            return this;
        }

        currentOpacity = 0f;
        setOpacity(0f);
        setVisible(true);

        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        final float targetOpacity = windowOpacity;
        final long startTime = System.currentTimeMillis();

        fadeTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, elapsed / (float) durationMs);
            currentOpacity = targetOpacity * progress;

            try {
                setOpacity(currentOpacity);
            } catch (Exception ex) {
                // Ignore
            }

            if (progress >= 1f) {
                fadeTimer.stop();
            }
        });
        fadeTimer.start();

        return this;
    }

    /**
     * Ferme la fenêtre avec une animation de fondu sortant.
     *
     * @param durationMs durée de l'animation en millisecondes
     * @return cette instance pour chaînage
     */
    public HWindow hideWithFadeOut(int durationMs) {
        if (!isOpacitySupported()) {
            dispose();
            return this;
        }

        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }

        final float startOpacity = currentOpacity > 0 ? currentOpacity : windowOpacity;
        final long startTime = System.currentTimeMillis();

        fadeTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, elapsed / (float) durationMs);
            currentOpacity = startOpacity * (1f - progress);

            try {
                setOpacity(currentOpacity);
            } catch (Exception ex) {
                // Ignore
            }

            if (progress >= 1f) {
                fadeTimer.stop();
                dispose();
            }
        });
        fadeTimer.start();

        return this;
    }

    /**
     * Configure un délai d'auto-fermeture pour la fenêtre.
     *
     * @param delayMs délai en millisecondes avant fermeture automatique
     * @param useFadeOut true pour utiliser un fondu sortant
     * @return cette instance pour chaînage
     */
    public HWindow autoClose(int delayMs, boolean useFadeOut) {
        if (autoCloseTimer != null && autoCloseTimer.isRunning()) {
            autoCloseTimer.stop();
        }

        autoCloseTimer = new Timer(delayMs, e -> {
            if (useFadeOut) {
                hideWithFadeOut(300);
            } else {
                dispose();
            }
        });
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();

        return this;
    }

    /**
     * Annule l'auto-fermeture si elle est active.
     *
     * @return cette instance pour chaînage
     */
    public HWindow cancelAutoClose() {
        if (autoCloseTimer != null && autoCloseTimer.isRunning()) {
            autoCloseTimer.stop();
        }
        return this;
    }

    // ========== GESTION DU NETTOYAGE ==========

    /**
     * Nettoie les ressources lors de la fermeture.
     */
    @Override
    public void dispose() {
        if (autoCloseTimer != null) {
            autoCloseTimer.stop();
        }
        if (fadeTimer != null) {
            fadeTimer.stop();
        }
        super.dispose();
    }

    // ========== GETTERS ET SETTERS ==========

    /**
     * Retourne la bordure personnalisée de la fenêtre.
     *
     * @return la bordure HBorder, ou null si aucune bordure n'est définie
     */
    public HBorder getHBorder() {
        return hBorder;
    }

    /**
     * Définit une bordure personnalisée pour la fenêtre.
     *
     * @param border la nouvelle bordure à appliquer
     * @return cette instance pour chaînage
     */
    public HWindow setHBorder(HAbstractBorder border) {
        this.hBorder = border;
        repaint();
        return this;
    }

    /**
     * Retourne l'ombre personnalisée de la fenêtre.
     *
     * @return l'ombre HShadow, ou null si aucune ombre n'est définie
     */
    public HShadow getShadow() {
        return hShadow;
    }

    /**
     * Définit une ombre personnalisée pour la fenêtre.
     *
     * @param shadow la nouvelle ombre à appliquer
     * @return cette instance pour chaînage
     */
    public HWindow setShadow(HShadow shadow) {
        this.hShadow = shadow;
        repaint();
        return this;
    }

    /**
     * Retourne le rayon des coins arrondis.
     *
     * @return le rayon en pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis.
     *
     * @param radius le nouveau rayon en pixels
     * @return cette instance pour chaînage
     */
    public HWindow setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
        return this;
    }

    /**
     * Retourne le style visuel actuel de la fenêtre.
     *
     * @return le HWindowStyle appliqué
     */
    public HWindowStyle getWindowStyle() {
        return windowStyle;
    }

    /**
     * Définit le style visuel de la fenêtre.
     *
     * @param style le nouveau style à appliquer
     * @return cette instance pour chaînage
     */
    public HWindow setWindowStyle(HWindowStyle style) {
        this.windowStyle = style;
        repaint();
        return this;
    }

    /**
     * Retourne l'opacité de la fenêtre.
     *
     * @return l'opacité (0.0 à 1.0)
     */
    public float getWindowOpacity() {
        return windowOpacity;
    }

    /**
     * Définit l'opacité de la fenêtre.
     *
     * @param opacity la nouvelle opacité (0.0 = transparent, 1.0 = opaque)
     * @return cette instance pour chaînage
     */
    public HWindow setWindowOpacity(float opacity) {
        this.windowOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
        try {
            if (isOpacitySupported()) {
                setOpacity(this.windowOpacity);
            }
        } catch (Exception e) {
            // Ignore si non supporté
        }
        repaint();
        return this;
    }

    /**
     * Vérifie si la fenêtre peut être déplacée par drag.
     *
     * @return true si le drag est activé
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Active ou désactive le déplacement par drag de la fenêtre.
     *
     * @param draggable true pour activer le drag
     * @return cette instance pour chaînage
     */
    public HWindow setDraggable(boolean draggable) {
        this.draggable = draggable;

        if (draggable) {
            enableDragging();
        } else {
            disableDragging();
        }
        return this;
    }

    /**
     * Active le déplacement de la fenêtre par drag.
     */
    private void enableDragging() {
        if (rootPane == null) return;

        java.awt.event.MouseAdapter dragListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mouseX = e.getXOnScreen() - getX();
                mouseY = e.getYOnScreen() - getY();
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
            }
        };

        rootPane.addMouseListener(dragListener);
        rootPane.addMouseMotionListener(dragListener);
    }

    /**
     * Désactive le déplacement de la fenêtre par drag.
     */
    private void disableDragging() {
        if (rootPane == null) return;

        // Retirer tous les listeners de souris
        for (java.awt.event.MouseListener listener : rootPane.getMouseListeners()) {
            rootPane.removeMouseListener(listener);
        }
        for (java.awt.event.MouseMotionListener listener : rootPane.getMouseMotionListeners()) {
            rootPane.removeMouseMotionListener(listener);
        }
    }

    /**
     * Retourne le panel racine personnalisé pour le rendu.
     *
     * @return le JPanel racine personnalisé
     */
    protected JPanel getCustomRootPane() {
        return rootPane;
    }

    // ========== MÉTHODES FACTORY ==========
    /**
     * Méthode factory pour créer un HWindow avec un style prédéfini.
     *
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HWindow configurée avec le style spécifié
     */
    public static HWindow withStyle(HWindowStyle style) {
        HWindow window = new HWindow();
        window.setWindowStyle(style);
        return window;
    }

    /**
     * Méthode factory pour créer un HWindow avec propriétaire et style.
     *
     * @param owner la fenêtre propriétaire
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HWindow configurée
     */
    public static HWindow withStyle(Frame owner, HWindowStyle style) {
        HWindow window = new HWindow(owner);
        window.setWindowStyle(style);
        return window;
    }

    /**
     * Méthode factory pour créer un HWindow avec propriétaire et style.
     *
     * @param owner la fenêtre propriétaire
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HWindow configurée
     */
    public static HWindow withStyle(Window owner, HWindowStyle style) {
        HWindow window = new HWindow(owner);
        window.setWindowStyle(style);
        return window;
    }

    // ========== WRAPPERS PRÊTS-À-L'EMPLOI ==========
    // Ces méthodes simples et chainables configurent, affichent et (si pertinent) auto-ferment
    // les HWindow pour des usages courants. Elles appellent les méthodes asX existantes
    // afin de ne pas casser ce qui fonctionne déjà dans la classe.

    // ---- SPLASH SCREEN ----
    public HWindow splashScreen(String title) {
        return splashScreen(title, null, true);
    }

    public HWindow splashScreen(String title, String subtitle) {
        return splashScreen(title, subtitle, true);
    }

    public HWindow splashScreen(String title, String subtitle, boolean showProgress) {
        // valeurs par défaut raisonnables
        final int fadeInMs = 600;
        final int autoCloseMs = 1800;
        final float opacity = 0.97f;
        final int corner = 25;

        this.asSplashScreen(title, subtitle, showProgress)
            .setCornerRadius(corner)
            .setWindowOpacity(opacity)
            .centerOnScreen();

        // afficher + auto-close (sur EDT)
        SwingUtilities.invokeLater(() -> this.showWithFadeIn(fadeInMs).autoClose(autoCloseMs, true));
        return this;
    }

    public static HWindow splashScreen(Window owner, String title) {
        return splashScreen(owner, title, null, true);
    }

    public static HWindow splashScreen(Window owner, String title, String subtitle, boolean showProgress) {
        HWindow win = new HWindow(owner);
        return win.splashScreen(title, subtitle, showProgress);
    }

    // ---- NOTIFICATION ----
    public HWindow notification(String title, String message) {
        return notification(title, message, HWindowStyle.PRIMARY, true, 4000);
    }

    public HWindow notification(String title, String message, HWindowStyle style) {
        return notification(title, message, style, true, 4000);
    }

    public HWindow notification(String title, String message, HWindowStyle style, boolean showCloseButton) {
        return notification(title, message, style, showCloseButton, 4000);
    }

    public HWindow notification(String title, String message, HWindowStyle style, boolean showCloseButton, int autoCloseMs) {
        final int fadeInMs = 350;
        final float opacity = 0.95f;
        final int corner = 15;

        this.asNotification(title, message, style, showCloseButton)
            .setCornerRadius(corner)
            .setWindowOpacity(opacity)
            .positionBottomRight(20, 60);

        SwingUtilities.invokeLater(() -> {
            this.showWithFadeIn(fadeInMs);
            if (autoCloseMs > 0) {
                this.autoClose(autoCloseMs, true);
            }
        });
        return this;
    }

    public static HWindow notification(Window owner, String title, String message) {
        return notification(owner, title, message, HWindowStyle.PRIMARY, true, 4000);
    }

    public static HWindow notification(Window owner, String title, String message, HWindowStyle style, boolean showCloseButton, int autoCloseMs) {
        HWindow win = new HWindow(owner);
        return win.notification(title, message, style, showCloseButton, autoCloseMs);
    }

    // ---- TOOLTIP ----
    public HWindow tooltip(String text) {
        return tooltip(text, 2000);
    }

    public HWindow tooltip(String text, int autoCloseMs) {
        final int fadeInMs = 200;
        final float opacity = 0.98f;
        final int corner = 10;

        this.asTooltip(text)
            .setCornerRadius(corner)
            .setWindowOpacity(opacity);

        // positionner près du curseur
        Point p = MouseInfo.getPointerInfo().getLocation();
        this.positionAt(p.x + 15, p.y + 15);

        SwingUtilities.invokeLater(() -> {
            this.showWithFadeIn(fadeInMs);
            if (autoCloseMs > 0) this.autoClose(autoCloseMs, true);
        });
        return this;
    }

    public static HWindow tooltip(Window owner, String text) {
        HWindow win = new HWindow(owner);
        return win.tooltip(text);
    }

    // ---- POPUP ----
    public HWindow popup(String title, JPanel content) {
        return popup(title, content, 420, 220);
    }

    public HWindow popup(String title, JPanel content, int width, int height) {
        final int corner = 20;
        final float opacity = 0.98f;
        final int fadeInMs = 350;

        this.asPopup(title, content, width, height)
            .setCornerRadius(corner)
            .setWindowOpacity(opacity)
            .centerOnScreen();

        SwingUtilities.invokeLater(() -> this.showWithFadeIn(fadeInMs));
        return this;
    }

    public static HWindow popup(Window owner, String title, JPanel content, int width, int height) {
        HWindow win = new HWindow(owner);
        return win.popup(title, content, width, height);
    }

    // ---- CONFIRM DIALOG ----
    public HWindow confirmDialog(String title, String message, Runnable onConfirm) {
        return confirmDialog(title, message, onConfirm, null);
    }

    public HWindow confirmDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        final int corner = 18;
        final int fadeInMs = 250;

        this.asConfirmDialog(title, message, onConfirm, onCancel)
            .setCornerRadius(corner)
            .centerOnScreen();

        SwingUtilities.invokeLater(() -> this.showWithFadeIn(fadeInMs));
        return this;
    }

    public static HWindow confirmDialog(Window owner, String title, String message, Runnable onConfirm, Runnable onCancel) {
        HWindow win = new HWindow(owner);
        return win.confirmDialog(title, message, onConfirm, onCancel);
    }

    // ---- LOADING SCREEN ----
    public HWindow loadingScreen(String message) {
        final int corner = 20;
        final float opacity = 0.98f;
        final int fadeInMs = 200;

        this.asLoadingScreen(message)
            .setCornerRadius(corner)
            .setWindowOpacity(opacity)
            .centerOnScreen();

        SwingUtilities.invokeLater(() -> this.showWithFadeIn(fadeInMs));
        return this;
    }

    public static HWindow loadingScreen(Window owner, String message) {
        HWindow win = new HWindow(owner);
        return win.loadingScreen(message);
    }

    // ========== FIN WRAPPERS ==========

}