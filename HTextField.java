/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents;

import hcomponents.vues.HButtonStyle;
import hcomponents.vues.HTextFieldUI;
import hcomponents.vues.border.HAbstractBorder;
import hcomponents.vues.border.SolidBorder;
import hcomponents.vues.shadow.HShadow;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Composant HTextField - Un champ de texte personnalisé avec design moderne.
 * Validation en temps réel, compteur de caractères et caractères interdits.
 * 
 * @author FIDELE
 * @version 2.1
 */
public class HTextField extends JTextField {
    
    private HAbstractBorder hBorder;
    private HShadow hShadow;
    private int cornerRadius = 8;
    private boolean showShadow = true;    
    
    // Style et couleurs
    private HButtonStyle buttonStyle = HButtonStyle.PRIMARY;
    private Color counterColor = new Color(128, 128, 128); // Gris
    private Color counterErrorColor = new Color(220, 53, 69);
    
    // Validation
    private int maxCharacters = 0;
    private final ArrayList<Character> forbiddenCharacters = new ArrayList<>();
    
    // Notification tooltip
    private Timer tooltipTimer;
    private JWindow tooltipWindow;
    
    // Dimensions
    private int verticalPadding = 10;
    private int horizontalPadding = 12;
    private int counterWidth = 60; // Largeur réservée pour le compteur à droite
    private int counterFontSize = 10; // Taille de police du compteur
    
    /**
     * Constructeur par défaut.
     */
    public HTextField() {
        this("");
    }
    
    public HTextField(String text) {
        this(text, 0);
    }
    
    public HTextField(int columns) {
        this("", columns);
    }
    
    public HTextField(String text, int columns) {
        super(text, columns);
        
        // Initialisation de la bordure par défaut
        this.hBorder = new SolidBorder(Color.GRAY, 1, cornerRadius);
        
        updateUI();
        setupDocumentFilter();
        setupDocumentListener();
        setupFocusListener();
        
        // Configuration initiale
        setOpaque(false);
        setBorder(null);
    }
    
    @Override
    public void updateUI() {
        setUI(new HTextFieldUI());
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
     * Configure le listener de document pour la validation en temps réel.
     */
    private void setupDocumentListener() {
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateText();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateText();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateText();
            }
        });
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
        
        // Timer pour fermer après 2.5 secondes
        tooltipTimer = new Timer(2500, e -> {
            if (tooltipWindow != null) {
                tooltipWindow.dispose();
                tooltipWindow = null;
            }
        });
        tooltipTimer.setRepeats(false);
        tooltipTimer.start();
    }
    
    /**
     * Valide le texte actuel (pour compatibilité, ne fait plus rien).
     */
    private void validateText() {
        // Méthode conservée pour compatibilité mais ne fait plus rien
        // La validation se fait maintenant dans le DocumentFilter
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
        // Mettre à jour le rayon de la bordure par défaut si elle existe
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
    
    public Color getErrorBorderColor() { 
        return counterErrorColor; // Utilise la couleur d'erreur du compteur
    }
    
    public void setErrorBorderColor(Color color) { 
        this.counterErrorColor = color; 
        repaint();
    }
    
    public Color getCounterColor() { 
        return counterColor; 
    }
    
    public void setCounterColor(Color color) { 
        this.counterColor = color; 
        if (maxCharacters > 0) repaint();
    }
    
    public Color getCounterErrorColor() { 
        return counterErrorColor; 
    }
    
    public void setCounterErrorColor(Color color) { 
        this.counterErrorColor = color; 
        if (maxCharacters > 0) repaint();
    }
    
    public int getMaxCharacters() { 
        return maxCharacters; 
    }
    
    public void setMaxCharacters(int max) { 
        this.maxCharacters = Math.max(0, max);
        validateText();
        repaint();
    }
    
    public ArrayList<Character> getForbiddenCharacters() { 
        return new ArrayList<>(forbiddenCharacters); 
    }
    
    public void addForbiddenCharacter(char c) {
        if (!forbiddenCharacters.contains(c)) {
            forbiddenCharacters.add(c);
            validateText();
        }
    }
    
    public void removeForbiddenCharacter(char c) {
        forbiddenCharacters.remove((Character) c);
        validateText();
    }
    
    public void clearForbiddenCharacters() {
        forbiddenCharacters.clear();
        validateText();
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
    
    public int getCounterWidth() { 
        return counterWidth; 
    }
    
    public void setCounterWidth(int width) { 
        this.counterWidth = Math.max(0, width); 
        repaint();
    }
    
    public int getCounterFontSize() { 
        return counterFontSize; 
    }
    
    public void setCounterFontSize(int size) { 
        this.counterFontSize = Math.max(8, size); 
        repaint();
    }
    
    public int getRemainingCharacters() {
        if (maxCharacters <= 0) return -1;
        return Math.max(0, maxCharacters - getText().length());
    }
}