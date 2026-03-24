package hcomponents.vues; // Package pour les UI delegates

// Importations nécessaires
import hcomponents.HEditorPane; // Notre composant personnalisé
import hcomponents.vues.border.HBorder; // Pour les bordures personnalisées
import java.awt.*; // Classes graphiques
import java.awt.event.FocusEvent; // Événements de focus
import java.awt.event.FocusListener; // Écouteur de focus
import java.io.IOException; // Pour les erreurs I/O
import java.io.StringReader; // Pour lire du HTML depuis une String
import javax.swing.JComponent; // Composant Swing de base
import javax.swing.Timer; // Pour les animations (non utilisé ici mais présent)
import javax.swing.plaf.basic.BasicEditorPaneUI; // UI de base à étendre
import javax.swing.text.BadLocationException; // Pour les erreurs de position dans le texte
import javax.swing.text.JTextComponent; // Composant texte de base
import javax.swing.text.html.HTMLDocument; // Document HTML
import javax.swing.text.html.HTMLEditorKit; // Kit d'édition HTML

/**
 * HBasicEditorPaneUI - UI Delegate moderne pour HEditorPane
 * 
 * CONCEPT CLÉ : Un JEditorPane est un composant sophistiqué qui peut
 * afficher et éditer du texte riche (HTML, RTF). Notre personnalisation
 * ajoute un design moderne avec placeholder, validation de contenu
 * et effets de focus.
 * 
 * PARTICULARITÉS DES EDITOR PANES :
 * 1. Peut afficher du HTML, RTF ou texte simple
 * 2. Gestion complexe du texte via Document model
 * 3. Possibilité de liens cliquables (HTML)
 * 4. Rendu personnalisable via EditorKit
 * 
 * FONCTIONNALITÉS AJOUTÉES :
 * 1. Placeholder animé (texte indicatif quand vide)
 * 2. Validation du contenu (HTML valide, longueur max)
 * 3. Bordures personnalisées avec effet de focus
 * 4. Design avec coins arrondis
 * 
 * @author FIDELE
 */
public class HBasicEditorPaneUI extends BasicEditorPaneUI {
    
    // ===================================================================
    // VARIABLES D'INSTANCE - ÉTATS ET ANIMATIONS
    // ===================================================================
    
    /** État du focus (si le composant a le focus clavier) */
    private boolean hasFocus = false;
    
    /** État d'erreur de contenu (si le contenu est invalide) */
    private boolean contentError = false;
    
    /** Position Y actuelle du placeholder (pour animation) */
    private float placeholderY;
    
    /** Taille de police actuelle du placeholder (pour animation) */
    private float placeholderFont;

    // ===================================================================
    // MÉTHODE D'INSTALLATION DE L'UI
    // ===================================================================
    
    @Override
    public void installUI(JComponent c) {
        // Initialisation de base de l'EditorPaneUI
        super.installUI(c);
        
        // Cast vers JTextComponent (parent de JEditorPane)
        JTextComponent txt = (JTextComponent) c;
        
        // ===============================================================
        // GESTION DU FOCUS POUR LES EFFETS VISUELS
        // ===============================================================
        
        c.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Le composant a reçu le focus clavier
                hasFocus = true;
                c.repaint(); // Redessiner pour montrer l'effet de focus
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                // Le composant a perdu le focus clavier
                hasFocus = false;
                c.repaint(); // Redessiner pour retirer l'effet de focus
            }
        });
        
        // ===============================================================
        // SURVEILLANCE DES CHANGEMENTS DE TEXTE
        // ===============================================================
        
        // DocumentListener pour détecter les modifications du texte
        // Note : Les méthodes sont vides car la validation est faite ailleurs
        // Mais la structure est prête pour des extensions
        txt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override 
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                // Appelé quand du texte est inséré
                // Pourrait déclencher la validation
            }
            
            @Override 
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                // Appelé quand du texte est supprimé
                // Pourrait déclencher la validation
            }
            
            @Override 
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                // Appelé pour les changements d'attributs (style, etc.)
                // Pourrait déclencher la validation
            }
        });
    }
    
    // ===================================================================
    // DESSIN DU FOND
    // ===================================================================
    
    /**
     * Redéfinit le dessin du fond de l'éditeur
     * Cette méthode est appelée par Swing pour dessiner l'arrière-plan
     */
    @Override
    protected void paintBackground(Graphics g) {
        // Récupérer le composant
        JComponent c = getComponent();
        
        // Vérifier que c'est bien notre HEditorPane personnalisé
        if (!(c instanceof HEditorPane)) {
            // Fallback : utiliser le rendu standard
            super.paintBackground(g);
            return;
        }
        
        // Cast vers notre composant
        HEditorPane editorPane = (HEditorPane) c;
        
        // Créer un contexte Graphics2D
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Activer l'anti-crénelage
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dimensions
        int width = c.getWidth();
        int height = c.getHeight();
        
        // Remplir avec la couleur de fond du style
        g2.setColor(editorPane.getEditorStyle().getBackground());
        
        // Dessiner un rectangle arrondi
        g2.fillRoundRect(0, 0, width, height, 
                        editorPane.getCornerRadius(), 
                        editorPane.getCornerRadius());
        
        // Libérer le contexte
        g2.dispose();
    }
    
    // ===================================================================
    // MÉTHODE PRINCIPALE DE DESSIN (SÉCURISÉE)
    // ===================================================================
    
    /**
     * Version sécurisée de paint() - garantit un contexte graphique sûr
     * Cette méthode orchestre tout le dessin du composant
     */
    @Override
    protected void paintSafely(Graphics g) {
        // Récupérer le composant
        JComponent c = getComponent();
        
        // Vérifier le type
        if (!(c instanceof HEditorPane)) {
            // Fallback : utiliser le rendu standard
            super.paintSafely(g);
            return;
        }
        
        // Cast vers notre composant
        HEditorPane editorPane = (HEditorPane) c;
        
        // Créer un contexte Graphics2D
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Activer l'anti-crénelage
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // ===============================================================
        // ÉTAPE 1 : DESSINER LE FOND
        // ===============================================================
        
        paintBackground(g2);
        
        // ===============================================================
        // ÉTAPE 2 : DESSINER LE TEXTE (VIA LA CLASSE PARENTE)
        // ===============================================================
        
        // Appeler la méthode parente pour dessiner le texte
        // Celle-ci gère tout le rendu complexe du texte (HTML, RTF, etc.)
        super.paintSafely(g2);
        
        // ===============================================================
        // ÉTAPE 3 : DESSINER LE PLACEHOLDER (SI NÉCESSAIRE)
        // ===============================================================
        
        paintPlaceholder(g2, editorPane);
        
        // ===============================================================
        // ÉTAPE 4 : DESSINER LA BORDURE PERSONNALISÉE
        // ===============================================================
        
        HBorder border = editorPane.getHBorder();
        if (border != null) {
            // Déléguer le dessin de la bordure à l'objet HBorder
            border.paint(g2, editorPane, c.getWidth(), c.getHeight(), 
                        editorPane.getCornerRadius());
        }
        
        // ===============================================================
        // ÉTAPE 5 : DESSINER LA BORDURE DE FOCUS (SI LE COMPOSANT A LE FOCUS)
        // ===============================================================
        
        if (hasFocus && editorPane.getEditorStyle().getFocusBorderColor() != null) {
            g2.setColor(editorPane.getEditorStyle().getFocusBorderColor());
            g2.setStroke(new BasicStroke(2)); // Bordure épaisse de 2px
            g2.drawRoundRect(1, 1,             // Position avec décalage de 1px
                           c.getWidth() - 3,   // Largeur ajustée
                           c.getHeight() - 3,  // Hauteur ajustée
                           editorPane.getCornerRadius(), 
                           editorPane.getCornerRadius());
        }
        
        // Libérer le contexte
        g2.dispose();
    }
    
    // ===================================================================
    // DESSIN DU PLACEHOLDER
    // ===================================================================
    
    /**
     * Dessine le texte de placeholder (texte indicatif quand l'éditeur est vide)
     * 
     * @param g2 Contexte graphique
     * @param editorPane Notre HEditorPane
     */
    private void paintPlaceholder(Graphics2D g2, HEditorPane editorPane) {
        // Récupérer le composant texte
        JTextComponent txt = (JTextComponent) getComponent();
        
        // Récupérer le texte actuel
        String text = txt.getText();
        
        // Si il y a du texte, ne pas dessiner le placeholder
        if (text != null && !text.trim().isEmpty())
            return;
        
        // Récupérer le texte du placeholder
        String placeholder = editorPane.getPlaceholder();
        
        // Si pas de placeholder défini, ne rien faire
        if (placeholder == null || placeholder.isEmpty())
            return;
        
        // Récupérer les marges internes (insets)
        Insets in = getComponent().getInsets();
        
        // Configurer la couleur et la police
        g2.setColor(editorPane.getEditorStyle().getPlaceholderColor());
        g2.setFont(getComponent().getFont().deriveFont(placeholderFont));
        
        // Dessiner le texte du placeholder
        // Note : Les variables placeholderY et placeholderFont pourraient être animées
        g2.drawString(placeholder, in.left + 4, placeholderY);
    }
    
    // ===================================================================
    // VALIDATION DU CONTENU
    // ===================================================================
    
    /**
     * Valide le contenu de l'éditeur selon plusieurs critères
     * 
     * @param c Le composant
     * @return true si le contenu est valide, false sinon
     */
    private boolean validateContent(JComponent c) {
        // Récupérer le composant texte
        JTextComponent txt = (JTextComponent) getComponent();
        
        // Récupérer le texte
        String text = txt.getText();
        
        // Cast vers notre HEditorPane
        HEditorPane pane = (HEditorPane) c;
        
        // ===============================================================
        // CRITÈRE 1 : TEXTE NON VIDE
        // ===============================================================
        
        if (text == null || text.trim().isEmpty()) {
            this.contentError = true;
            // Note : On pourrait afficher un message d'erreur spécifique
        }
        
        // ===============================================================
        // CRITÈRE 2 : LONGUEUR MAXIMALE
        // ===============================================================
        
        // Note : Il y a une inversion logique ici, probablement une erreur
        // Devrait être : if (text.trim().length() > pane.getMaxLength())
        if (text.trim().length() < pane.getMaxLength()) {
            this.contentError = true;
            // Note : On pourrait afficher un message d'erreur spécifique
        }
        
        // ===============================================================
        // CRITÈRE 3 : HTML VALIDE (SI MODE HTML)
        // ===============================================================
        
        // Vérifier si l'éditeur est configuré pour du HTML
        if (pane.getEditorKit() instanceof HTMLEditorKit) {
            // Créer un nouveau HTMLEditorKit pour la validation
            HTMLEditorKit kit = new HTMLEditorKit();
            
            try {
                // Essayer de parser le HTML
                // Si le HTML est mal formé, une exception sera levée
                kit.read(new StringReader(text), new HTMLDocument(), 0);
            } catch (IOException | BadLocationException e) {
                // Le HTML est mal formé
                this.contentError = true;
                // Note : On pourrait capturer le message d'erreur
            }
        }
        
        return contentError;
    }
    
} 