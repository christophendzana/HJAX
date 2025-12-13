/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HFormattedTextFieldStyle;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.*;
import java.util.function.Predicate;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * FormattedTextField personnalisé avec design moderne, validation et gestion d'erreurs.
 * Étend JFormattedTextField pour offrir des bordures arrondies, animations fluides,
 * placeholder, validation en temps réel, formats prédéfinis et bordures colorées selon l'état.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JFormattedTextField
 * @see HFormattedTextFieldStyle
 */
public class HFormattedTextField extends JFormattedTextField {
    
    private HFormattedTextFieldStyle fieldStyle = HFormattedTextFieldStyle.PRIMARY;
    private int cornerRadius = 8;
    private boolean animationsEnabled = true;
    private String placeholder = "";
    private ValidationState validationState = ValidationState.NORMAL;
    private String statusMessage = "";
    private boolean isFocused = false;
    private float focusProgress = 0f;
    private Timer focusTimer;
    private Predicate<String> customValidator;
    private boolean showStatusIcon = true;
    
    private static final int ANIMATION_DURATION = 200;
    private static final int FPS = 60;
    private static final int FRAME_DELAY = 1000 / FPS;
    
    /**
     * États de validation possibles.
     */
    public enum ValidationState {
        NORMAL,    // État normal
        ERROR,     // Erreur de validation
        SUCCESS    // Validation réussie
    }
    
    /**
     * Formats prédéfinis.
     */
    public enum FormatType {
        INTEGER,         // Nombres entiers
        DECIMAL,         // Nombres décimaux
        CURRENCY,        // Devise
        PERCENT,         // Pourcentage
        DATE,            // Date
        TIME,            // Heure
        PHONE,           // Téléphone
        EMAIL,           // Email (validation uniquement)
        CUSTOM           // Format personnalisé
    }
    
    /**
     * Constructeur par défaut.
     */
    public HFormattedTextField() {
        super();
        init();
    }
    
    /**
     * Constructeur avec Format.
     */
    public HFormattedTextField(Format format) {
        super(format);
        init();
    }
    
    /**
     * Constructeur avec AbstractFormatter.
     */
    public HFormattedTextField(AbstractFormatter formatter) {
        super(formatter);
        init();
    }
    
    /**
     * Constructeur avec AbstractFormatterFactory.
     */
    public HFormattedTextField(AbstractFormatterFactory factory) {
        super(factory);
        init();
    }
    
    /**
     * Constructeur avec valeur initiale.
     */
    public HFormattedTextField(Object value) {
        super(value);
        init();
    }
    
  /**
 * Constructeur avec type de format prédéfini.
 */
public HFormattedTextField(FormatType formatType) {
    super(createFormatterForType(formatType));
    init();
    
    // Ajouter validateur pour email
    if (formatType == FormatType.EMAIL) {
        setCustomValidator(text -> {
            if (text.isEmpty()) return true;
            return text.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        });
    }
}


    
    private void init() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 35, 10, 35));
        setForeground(fieldStyle.getTextColor());
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
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
                validateInput();
            }
        });
        
        // Listener pour validation en temps réel
        getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateInput(); }
            public void removeUpdate(DocumentEvent e) { validateInput(); }
            public void insertUpdate(DocumentEvent e) { validateInput(); }
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
    
    private void validateInput() {
        String text = getText();
        
        // Validation avec validateur personnalisé
        if (customValidator != null) {
            if (text.isEmpty()) {
                setValidationState(ValidationState.NORMAL, "");
            } else if (customValidator.test(text)) {
                setValidationState(ValidationState.SUCCESS, "Valide");
            } else {
                setValidationState(ValidationState.ERROR, "Format invalide");
            }
            return;
        }
        
        // Validation par défaut avec le formatter
        try {
            if (!text.isEmpty()) {
                commitEdit();
                setValidationState(ValidationState.SUCCESS, "Valide");
            } else {
                setValidationState(ValidationState.NORMAL, "");
            }
        } catch (ParseException e) {
            setValidationState(ValidationState.ERROR, "Format invalide");
        }
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
        
        // Dessiner l'icône de statut
        if (showStatusIcon && validationState != ValidationState.NORMAL) {
            paintStatusIcon(g2, width, height);
        }
        
        g2.dispose();
        
        // Dessiner le placeholder si vide
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            paintPlaceholder(g);
        }
        
        super.paintComponent(g);
        
        // Dessiner le message de statut si présent
        if (!statusMessage.isEmpty() && validationState != ValidationState.NORMAL) {
            paintStatusMessage(g);
        }
    }
    
    private void paintBackground(Graphics2D g2, int width, int height) {
        if (fieldStyle == null) return;
        
        Color bgColor;
        switch (validationState) {
            case ERROR:
                Color errorBg = fieldStyle.getErrorBackground();
                bgColor = interpolateColor(fieldStyle.getBackground(), errorBg, 0.3f);
                break;
            case SUCCESS:
                Color successBg = fieldStyle.getSuccessBackground();
                bgColor = interpolateColor(fieldStyle.getBackground(), successBg, 0.2f);
                break;
            default:
                Color normalBg = fieldStyle.getBackground();
                Color focusBg = fieldStyle.getFocusBackground();
                bgColor = interpolateColor(normalBg, focusBg, focusProgress);
                break;
        }
        
        g2.setColor(bgColor);
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            2, 2, width - 4, height - 4, cornerRadius, cornerRadius
        );
        g2.fill(roundRect);
    }
    
    private void paintBorder(Graphics2D g2, int width, int height) {
        if (fieldStyle == null) return;
        
        Color borderColor;
        float borderWidth;
        
        switch (validationState) {
            case ERROR:
                borderColor = fieldStyle.getErrorBorderColor();
                borderWidth = 2.5f;
                break;
            case SUCCESS:
                borderColor = fieldStyle.getSuccessBorderColor();
                borderWidth = 2f;
                break;
            default:
                Color normalBorder = fieldStyle.getBorderColor();
                Color focusBorder = fieldStyle.getFocusBorderColor();
                borderColor = interpolateColor(normalBorder, focusBorder, focusProgress);
                borderWidth = 1.5f + (focusProgress * 1f);
                break;
        }
        
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            2, 2, width - 4, height - 4, cornerRadius, cornerRadius
        );
        g2.draw(roundRect);
    }
    
    private void paintPlaceholder(Graphics g) {
        if (fieldStyle == null) return;
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.setColor(fieldStyle.getPlaceholderColor());
        g2.setFont(getFont().deriveFont(Font.ITALIC));
        
        Insets insets = getInsets();
        FontMetrics fm = g2.getFontMetrics();
        
        g2.drawString(placeholder, insets.left, insets.top + fm.getAscent());
        
        g2.dispose();
    }
    
    private void paintStatusIcon(Graphics2D g2, int width, int height) {
        int iconSize = 16;
        int x = width - iconSize - 12;
        int y = (height - iconSize) / 2;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (validationState == ValidationState.ERROR) {
            // Cercle rouge avec X
            g2.setColor(fieldStyle.getErrorBorderColor());
            g2.fillOval(x, y, iconSize, iconSize);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x + 5, y + 5, x + 11, y + 11);
            g2.drawLine(x + 11, y + 5, x + 5, y + 11);
        } else if (validationState == ValidationState.SUCCESS) {
            // Cercle vert avec checkmark
            g2.setColor(fieldStyle.getSuccessBorderColor());
            g2.fillOval(x, y, iconSize, iconSize);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x + 4, y + 8, x + 7, y + 11);
            g2.drawLine(x + 7, y + 11, x + 12, y + 5);
        }
    }
    
    private void paintStatusMessage(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Color messageColor = validationState == ValidationState.ERROR ? 
                            fieldStyle.getErrorBorderColor() : 
                            fieldStyle.getSuccessBorderColor();
        
        g2.setColor(messageColor);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        String icon = validationState == ValidationState.ERROR ? "⚠ " : "✓ ";
        FontMetrics fm = g2.getFontMetrics();
        int y = getHeight() + fm.getHeight();
        
        g2.drawString(icon + statusMessage, 5, y);
        
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

    /**
 * Crée un AbstractFormatter selon le type de format.
 */
private static AbstractFormatter createFormatterForType(FormatType type) {
    switch (type) {
        case INTEGER:
            return new InternationalFormatter(NumberFormat.getIntegerInstance()) {{
                setOverwriteMode(false);
            }};
        case DECIMAL:
            NumberFormat decimalFormat = NumberFormat.getNumberInstance();
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(2);
            return new InternationalFormatter(decimalFormat) {{
                setOverwriteMode(false);
            }};
        case CURRENCY:
            return new InternationalFormatter(NumberFormat.getCurrencyInstance()) {{
                setOverwriteMode(false);
            }};
        case PERCENT:
            return new InternationalFormatter(NumberFormat.getPercentInstance()) {{
                setOverwriteMode(false);
            }};
        case DATE:
            return new InternationalFormatter(new SimpleDateFormat("dd/MM/yyyy")) {{
                setOverwriteMode(false);
            }};
        case TIME:
            return new InternationalFormatter(new SimpleDateFormat("HH:mm:ss")) {{
                setOverwriteMode(false);
            }};
        case PHONE:
            try {
                MaskFormatter maskFormatter = new MaskFormatter("(###) ###-####");
                maskFormatter.setPlaceholderCharacter('_');
                maskFormatter.setOverwriteMode(false);
                return maskFormatter;
            } catch (ParseException e) {
                return new DefaultFormatter();
            }
        case EMAIL:
            // Pas de formatter, juste validation
            return new DefaultFormatter();
        default:
            return new DefaultFormatter();
    }
}
    
    // ========== GETTERS / SETTERS ==========
    
    public HFormattedTextFieldStyle getFieldStyle() {
        return fieldStyle;
    }
    
    public void setFieldStyle(HFormattedTextFieldStyle style) {
        this.fieldStyle = style;
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
    
    public ValidationState getValidationState() {
        return validationState;
    }
    
    public void setValidationState(ValidationState state, String message) {
        this.validationState = state;
        this.statusMessage = message;
        repaint();
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public boolean isShowStatusIcon() {
        return showStatusIcon;
    }
    
    public void setShowStatusIcon(boolean show) {
        this.showStatusIcon = show;
        repaint();
    }
    
    public void setCustomValidator(Predicate<String> validator) {
        this.customValidator = validator;
    }
    
    /**
     * Méthode factory avec style.
     */
    public static HFormattedTextField withStyle(HFormattedTextFieldStyle style) {
        HFormattedTextField field = new HFormattedTextField();
        field.setFieldStyle(style);
        return field;
    }
    
    /**
     * Méthode factory avec format prédéfini et style.
     */
    public static HFormattedTextField withFormat(FormatType formatType, HFormattedTextFieldStyle style) {
        HFormattedTextField field = new HFormattedTextField(formatType);
        field.setFieldStyle(style);
        return field;
    }
    
    /**
     * Méthode factory avec format prédéfini, style et placeholder.
     */
    public static HFormattedTextField withFormat(FormatType formatType, HFormattedTextFieldStyle style, String placeholder) {
        HFormattedTextField field = new HFormattedTextField(formatType);
        field.setFieldStyle(style);
        field.setPlaceholder(placeholder);
        return field;
    }
}