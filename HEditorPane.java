/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HEditorPaneStyle;
import hcomponents.vues.border.HBorder;
import hcomponents.vues.border.SolidBorder;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * EditorPane personnalisé avec design moderne, validation et gestion d'erreurs.
 * Étend JEditorPane pour offrir des bordures arrondies, animations fluides,
 * placeholder, validation en temps réel et bordures rouges en cas d'erreur.
 *
 * @author FIDELE
 * @version 1.0
 * @see JEditorPane
 * @see HEditorPaneStyle
 */
public class HEditorPane extends JEditorPane {
    
    private int cornerRadius = 8;
    private int maxLength = 0;
    private String placeholder = "";
    private String errorMessage = "";
    private boolean hasError = false;
    private boolean isFocused = false;
    private boolean animationsEnabled = true;
    private float focusProgress = 0f;
    private Timer focusTimer;
    private HBorder hBorder = new SolidBorder();
    private HEditorPaneStyle editorStyle = HEditorPaneStyle.PRIMARY;

    // Système de validation
    private List<Predicate<String>> validators = new ArrayList<>();
    private boolean autoValidate = true;

    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;

    /**
     * Constructeur par défaut.
     */
    public HEditorPane() {
        super();
        init();
    }

    /**
     * Constructeur avec type de contenu et texte.
     */
    public HEditorPane(String type, String text) {
        super(type, text);
        init();
    }

    /**
     * Constructeur avec URL.
     */
    public HEditorPane(java.net.URL initialPage) throws java.io.IOException {
        super(initialPage);
        init();
    }

    /**
     * Constructeur avec URL string.
     */
    public HEditorPane(String url) throws java.io.IOException {
        super(url);
        init();
    }

    private void init() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setForeground(editorStyle.getTextColor());

        // Listener pour le focus
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                animateFocus(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                animateFocus(false);
                if (autoValidate) {
                    validateContent();
                }
            }
        });

        // Listener pour la validation en temps réel
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (autoValidate) {
                    validateContent();
                }
                repaint();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (autoValidate) {
                    validateContent();
                }
                repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (autoValidate) {
                    validateContent();
                }
                repaint();
            }
        });
    }

    private void animateFocus(boolean focusing) {
        if (!animationsEnabled) {
            focusProgress = focusing ? 1f : 0f;
            repaint();
            return;
        }

        if (focusTimer != null) {
            focusTimer.stop();
        }

        float startProgress = focusProgress;
        long startTime = System.currentTimeMillis();

        focusTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);

                focusProgress = focusing ? (startProgress + (1f - startProgress) * progress)
                        : (startProgress - startProgress * progress);

                repaint();

                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });

        focusTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Dessiner le fond
        paintBackground(g2, width, height);

        // Dessiner la bordure
        paintBorder(g2, width, height);

        g2.dispose();

        // Dessiner le placeholder si vide
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            paintPlaceholder(g);
        }

        super.paintComponent(g);

        // Dessiner le message d'erreur si présent
        if (hasError && !errorMessage.isEmpty()) {
            paintErrorMessage(g);
        }
    }

    private void paintBackground(Graphics2D g2, int width, int height) {
        if (editorStyle == null) {
            return;
        }

        Color bgColor;
        if (hasError) {
            // Mélange subtil entre fond normal et fond d'erreur
            Color normalBg = editorStyle.getBackground();
            Color errorBg = editorStyle.getErrorBackground();
            bgColor = interpolateColor(normalBg, errorBg, 0.3f);
        } else {
            Color normalBg = editorStyle.getBackground();
            Color focusBg = editorStyle.getFocusBackground();
            bgColor = interpolateColor(normalBg, focusBg, focusProgress);
        }

        g2.setColor(bgColor);
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                2, 2, width - 4, height - 4, cornerRadius, cornerRadius
        );
        g2.fill(roundRect);
    }

    private void paintBorder(Graphics2D g2, int width, int height) {
        if (editorStyle == null) {
            return;
        }

        Color borderColor;
        float borderWidth;

        if (hasError) {
            borderColor = editorStyle.getErrorBorderColor();
            borderWidth = 2.5f;
        } else {
            Color normalBorder = editorStyle.getBorderColor();
            Color focusBorder = editorStyle.getFocusBorderColor();
            borderColor = interpolateColor(normalBorder, focusBorder, focusProgress);
            borderWidth = 1.5f + (focusProgress * 1f); // 1.5px à 2.5px
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));

        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                2, 2, width - 4, height - 4, cornerRadius, cornerRadius
        );
        g2.draw(roundRect);
    }

    private void paintPlaceholder(Graphics g) {
        if (editorStyle == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(editorStyle.getPlaceholderColor());
        g2.setFont(getFont().deriveFont(Font.ITALIC));

        Insets insets = getInsets();
        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(placeholder, insets.left, insets.top + fm.getAscent());

        g2.dispose();
    }

    private void paintErrorMessage(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(editorStyle.getErrorBorderColor());
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        FontMetrics fm = g2.getFontMetrics();
        int y = getHeight() + fm.getHeight();

        g2.drawString("⚠ " + errorMessage, 5, y);

        g2.dispose();
    }

    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }

    // ========== SYSTÈME DE VALIDATION ==========
    /**
     * Ajoute un validateur personnalisé. Le validateur retourne true si la
     * validation réussit, false sinon.
     */
    public void addValidator(Predicate<String> validator) {
        validators.add(validator);
    }

    /**
     * Valide le contenu actuel avec tous les validateurs.
     *
     * @return true si valide, false sinon
     */
    public boolean validateContent() {
        String text = getText();

        for (Predicate<String> validator : validators) {
            if (!validator.test(text)) {
                setError(true);
                return false;
            }
        }

        setError(false);
        return true;
    }

    /**
     * Définit si le champ est en erreur.
     */
    public void setError(boolean error) {
        this.hasError = error;
        if (!error) {
            this.errorMessage = "";
        }
        repaint();
    }

    /**
     * Définit l'état d'erreur avec un message.
     */
    public void setError(boolean error, String message) {
        this.hasError = error;
        this.errorMessage = message;
        repaint();
    }

    /**
     * Vérifie si le champ est en erreur.
     */
    public boolean hasError() {
        return hasError;
    }

    /**
     * Retourne le message d'erreur actuel.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Active/désactive la validation automatique.
     */
    public void setAutoValidate(boolean auto) {
        this.autoValidate = auto;
    }

    public boolean isAutoValidate() {
        return autoValidate;
    }

    // ========== GETTERS / SETTERS ==========
    public HEditorPaneStyle getEditorStyle() {
        return editorStyle;
    }

    public void setEditorStyle(HEditorPaneStyle style) {
        this.editorStyle = style;
        setForeground(style.getTextColor());
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

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }
    
    public HBorder getHBorder(){
        return hBorder;
    }
    
    public void setHBorder(HBorder border){
        this.hBorder = border;
    }
    
    public int getMaxLength(){
        return maxLength;
    }
    
    public void setMaxLength(int max){
        this.maxLength = max;
    }

    /**
     * Méthode factory pour créer un HEditorPane avec style.
     */
    public static HEditorPane withStyle(HEditorPaneStyle style) {
        HEditorPane editor = new HEditorPane();
        editor.setEditorStyle(style);
        return editor;
    }

    /**
     * Méthode factory avec style et placeholder.
     */
    public static HEditorPane withStyle(HEditorPaneStyle style, String placeholder) {
        HEditorPane editor = new HEditorPane();
        editor.setEditorStyle(style);
        editor.setPlaceholder(placeholder);
        return editor;
    }
}
