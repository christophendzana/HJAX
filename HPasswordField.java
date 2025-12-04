/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HPasswordFieldUI;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.SolidBorder;
import hcomponents.vues.shadow.HShadow;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Composant HPasswordField - Un champ de mot de passe personnalisé avec design moderne.
 * Validation configurable, caractères interdits, et bouton toggle visibilité.
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HPasswordField extends JPasswordField {
    
    private HAbstractBorder hBorder;
    private HShadow hShadow;
    private int cornerRadius = 8;
    private boolean showShadow = true;
    
    // Style et couleurs
    private HButtonStyle buttonStyle = HButtonStyle.PRIMARY;
    private Color counterColor = new Color(128, 128, 128);
    private Color counterErrorColor = new Color(220, 53, 69);
    
    // Validation
    private int maxCharacters = 0;
    private final ArrayList<Character> forbiddenCharacters = new ArrayList<>();
    
    // Règles de validation du mot de passe
    private boolean minLengthEnabled = false;
    private int minLength = 8;
    private boolean uppercaseRequired = false;
    private boolean lowercaseRequired = false;
    private boolean digitRequired = false;
    private boolean specialCharRequired = false;
    
    // Notification tooltip
    private Timer tooltipTimer;
    private JWindow tooltipWindow;
    
    // Dimensions
    private int verticalPadding = 10;
    private int horizontalPadding = 12;
    private int toggleButtonWidth = 40; // Largeur réservée pour le bouton œil
    
    // État du toggle
    private boolean passwordVisible = false;
    
    /**
     * Constructeur par défaut.
     */
    public HPasswordField() {
        this("");
    }
    
    public HPasswordField(String text) {
        this(text, 0);
    }
    
    public HPasswordField(int columns) {
        this("", columns);
    }
    
    public HPasswordField(String text, int columns) {
        super(text, columns);
        
        // Initialisation de la bordure par défaut
        this.hBorder = new SolidBorder(Color.GRAY, 1, cornerRadius);
        
        updateUI();
        setupDocumentFilter();
        setupFocusListener();
        setupKeyListener();
        
        // Configuration initiale
        setOpaque(false);
        setBorder(null);
    }
    
    @Override
    public void updateUI() {
        setUI(new HPasswordFieldUI());
    }
    
    /**
     * Configure le filtre de document pour empêcher la saisie invalide.
     */
    private void setupDocumentFilter() {
        if (getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String text, 
                                         AttributeSet attr) throws BadLocationException {
                    if (isTextValidForInsert(fb, offset, text)) {
                        super.insertString(fb, offset, text, attr);
                    }
                }
                
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text,
                                    AttributeSet attrs) throws BadLocationException {
                    if (isTextValidForReplace(fb, offset, length, text)) {
                        super.replace(fb, offset, length, text, attrs);
                    }
                }
            });
        }
    }
    
    /**
     * Configure le listener de focus pour le repaint.
     */
    private void setupFocusListener() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }
    
    /**
     * Configure le listener de clavier pour détecter Entrée.
     */
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    validatePassword();
                }
            }
        });
    }
    
    /**
     * Vérifie si le texte à insérer est valide.
     */
    private boolean isTextValidForInsert(DocumentFilter.FilterBypass fb, int offset, String text) 
            throws BadLocationException {
        if (text == null) return false;
        
        // Vérifier limite de caractères
        if (maxCharacters > 0) {
            int currentLength = fb.getDocument().getLength();
            if (currentLength + text.length() > maxCharacters) {
                showTooltipNotification("Limite de " + maxCharacters + " caractères atteinte");
                return false;
            }
        }
        
        // Vérifier caractères interdits
        for (char c : text.toCharArray()) {
            if (forbiddenCharacters.contains(c)) {
                showTooltipNotification("Le caractère '" + c + "' n'est pas autorisé");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Vérifie si le texte de remplacement est valide.
     */
    private boolean isTextValidForReplace(DocumentFilter.FilterBypass fb, int offset, int length, String text) 
            throws BadLocationException {
        if (text == null) text = "";
        
        // Vérifier limite de caractères
        if (maxCharacters > 0) {
            int currentLength = fb.getDocument().getLength();
            int newLength = currentLength - length + text.length();
            if (newLength > maxCharacters) {
                showTooltipNotification("Limite de " + maxCharacters + " caractères atteinte");
                return false;
            }
        }
        
        // Vérifier caractères interdits
        for (char c : text.toCharArray()) {
            if (forbiddenCharacters.contains(c)) {
                showTooltipNotification("Le caractère '" + c + "' n'est pas autorisé");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Valide le mot de passe selon les règles configurées.
     * Affiche un tooltip si la validation échoue.
     * 
     * @return true si le mot de passe est valide, false sinon
     */
    public boolean validatePassword() {
        if (isPasswordValid()) {
            return true;
        } else {
            String message = getValidationMessage();
            showTooltipNotification(message);
            return false;
        }
    }
    
    /**
     * Vérifie si le mot de passe respecte toutes les règles activées.
     * 
     * @return true si valide, false sinon
     */
    public boolean isPasswordValid() {
        String password = new String(getPassword());
        
        // Vérifier longueur minimale
        if (minLengthEnabled && password.length() < minLength) {
            return false;
        }
        
        // Vérifier majuscule
        if (uppercaseRequired && !password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Vérifier minuscule
        if (lowercaseRequired && !password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Vérifier chiffre
        if (digitRequired && !password.matches(".*\\d.*")) {
            return false;
        }
        
        // Vérifier caractère spécial
        if (specialCharRequired && !password.matches(".*[^a-zA-Z0-9].*")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Retourne un message décrivant les règles non respectées.
     * 
     * @return Message de validation
     */
    public String getValidationMessage() {
        String password = new String(getPassword());
        StringBuilder message = new StringBuilder("<html><b>Le mot de passe doit contenir :</b><br>");
        boolean hasError = false;
        
        // Vérifier longueur minimale
        if (minLengthEnabled && password.length() < minLength) {
            message.append("• Au moins ").append(minLength).append(" caractères<br>");
            hasError = true;
        }
        
        // Vérifier majuscule
        if (uppercaseRequired && !password.matches(".*[A-Z].*")) {
            message.append("• Au moins 1 lettre majuscule<br>");
            hasError = true;
        }
        
        // Vérifier minuscule
        if (lowercaseRequired && !password.matches(".*[a-z].*")) {
            message.append("• Au moins 1 lettre minuscule<br>");
            hasError = true;
        }
        
        // Vérifier chiffre
        if (digitRequired && !password.matches(".*\\d.*")) {
            message.append("• Au moins 1 chiffre<br>");
            hasError = true;
        }
        
        // Vérifier caractère spécial
        if (specialCharRequired && !password.matches(".*[^a-zA-Z0-9].*")) {
            message.append("• Au moins 1 caractère spécial (@#$!%etc.)<br>");
            hasError = true;
        }
        
        message.append("</html>");
        
        return hasError ? message.toString() : "Mot de passe valide";
    }
    
    /**
     * Affiche un tooltip temporaire pour notifier l'utilisateur.
     */
    private void showTooltipNotification(String message) {
        // Annuler le timer précédent si existe
        if (tooltipTimer != null && tooltipTimer.isRunning()) {
            tooltipTimer.stop();
        }
        
        // Fermer la fenêtre précédente si existe
        if (tooltipWindow != null && tooltipWindow.isVisible()) {
            tooltipWindow.dispose();
        }
        
        // Créer la fenêtre de notification
        tooltipWindow = new JWindow();
        tooltipWindow.setAlwaysOnTop(true);
        
        // Créer le panel du tooltip
        JPanel tooltipPanel = new JPanel();
        tooltipPanel.setBackground(new Color(50, 50, 50));
        tooltipPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(getFont().deriveFont(12f));
        tooltipPanel.add(label);
        
        tooltipWindow.setContentPane(tooltipPanel);
        tooltipWindow.pack();
        
        // Positionner sous le champ de texte
        Point locationOnScreen = getLocationOnScreen();
        tooltipWindow.setLocation(
            locationOnScreen.x,
            locationOnScreen.y + getHeight() + 5
        );
        
        tooltipWindow.setVisible(true);
        
        // Timer pour fermer après 3.5 secondes (plus long pour lire les règles)
        tooltipTimer = new Timer(3500, e -> {
            if (tooltipWindow != null) {
                tooltipWindow.dispose();
                tooltipWindow = null;
            }
        });
        tooltipTimer.setRepeats(false);
        tooltipTimer.start();
    }
    
    /**
     * Toggle la visibilité du mot de passe.
     */
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            setEchoChar((char) 0); // Afficher le texte
        } else {
            setEchoChar('•'); // Masquer avec •
        }
        repaint();
    }
    
    // ===== GETTERS / SETTERS =====
    
    public HAbstractBorder getHBorder() { 
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
        this.cornerRadius = Math.max(0, radius);
        if (hBorder != null) {
            hBorder.setCornerRadius(this.cornerRadius);
        }
        repaint();
    }
    
    public boolean isShowShadow() { 
        return showShadow; 
    }
    
    public void setShowShadow(boolean show) { 
        this.showShadow = show; 
        repaint();
    }
    
    public HButtonStyle getButtonStyle() { 
        return buttonStyle; 
    }
    
    public void setButtonStyle(HButtonStyle style) { 
        this.buttonStyle = style; 
        repaint();
    }
    
    public Color getCounterColor() { 
        return counterColor; 
    }
    
    public void setCounterColor(Color color) { 
        this.counterColor = color; 
        repaint();
    }
    
    public Color getCounterErrorColor() { 
        return counterErrorColor; 
    }
    
    public void setCounterErrorColor(Color color) { 
        this.counterErrorColor = color; 
        repaint();
    }
    
    public int getMaxCharacters() { 
        return maxCharacters; 
    }
    
    public void setMaxCharacters(int max) { 
        this.maxCharacters = Math.max(0, max);
        repaint();
    }
    
    public ArrayList<Character> getForbiddenCharacters() { 
        return new ArrayList<>(forbiddenCharacters); 
    }
    
    public void addForbiddenCharacter(char c) {
        if (!forbiddenCharacters.contains(c)) {
            forbiddenCharacters.add(c);
        }
    }
    
    public void removeForbiddenCharacter(char c) {
        forbiddenCharacters.remove((Character) c);
    }
    
    public void clearForbiddenCharacters() {
        forbiddenCharacters.clear();
    }
    
    public int getVerticalPadding() { 
        return verticalPadding; 
    }
    
    public void setVerticalPadding(int padding) { 
        this.verticalPadding = Math.max(0, padding); 
        repaint();
    }
    
    public int getHorizontalPadding() { 
        return horizontalPadding; 
    }
    
    public void setHorizontalPadding(int padding) { 
        this.horizontalPadding = Math.max(0, padding); 
        repaint();
    }
    
    public int getToggleButtonWidth() { 
        return toggleButtonWidth; 
    }
    
    public void setToggleButtonWidth(int width) { 
        this.toggleButtonWidth = Math.max(0, width); 
        repaint();
    }
    
    public boolean isPasswordVisible() { 
        return passwordVisible; 
    }
    
    // === RÈGLES DE VALIDATION ===
    
    public void setMinLengthRule(boolean enabled, int minLength) {
        this.minLengthEnabled = enabled;
        this.minLength = Math.max(1, minLength);
    }
    
    public boolean isMinLengthEnabled() {
        return minLengthEnabled;
    }
    
    public int getMinLength() {
        return minLength;
    }
    
    public void setUppercaseRule(boolean enabled) {
        this.uppercaseRequired = enabled;
    }
    
    public boolean isUppercaseRequired() {
        return uppercaseRequired;
    }
    
    public void setLowercaseRule(boolean enabled) {
        this.lowercaseRequired = enabled;
    }
    
    public boolean isLowercaseRequired() {
        return lowercaseRequired;
    }
    
    public void setDigitRule(boolean enabled) {
        this.digitRequired = enabled;
    }
    
    public boolean isDigitRequired() {
        return digitRequired;
    }
    
    public void setSpecialCharRule(boolean enabled) {
        this.specialCharRequired = enabled;
    }
    
    public boolean isSpecialCharRequired() {
        return specialCharRequired;
    }
}