package hcomponents.vues; // Package pour les UI delegates

// Importations nécessaires
import hcomponents.HLabel; // Notre composant personnalisé
import java.awt.AlphaComposite; // Pour les transparences
import java.awt.BasicStroke; // Pour les bordures
import java.awt.Color; // Pour les couleurs
import java.awt.FontMetrics; // Pour mesurer le texte
import java.awt.Graphics; // Contexte graphique de base
import java.awt.Graphics2D; // Contexte graphique avancé
import java.awt.RenderingHints; // Pour l'anti-crénelage
import java.awt.geom.AffineTransform; // Pour les transformations (rotation)
import javax.swing.JComponent; // Composant Swing de base
import javax.swing.JLabel; // Label Swing standard
import javax.swing.plaf.basic.BasicLabelUI; // UI de base à étendre

/**
 * HBasicLabelUI - UI Delegate moderne pour HLabel avec support d'orientation
 * 
 * CONCEPT CLÉ : Un JLabel est le composant Swing le plus simple pour afficher
 * du texte ou une image. Notre personnalisation ajoute des fonctionnalités
 * avancées comme l'orientation du texte et les fonds arrondis.
 * 
 * FONCTIONNALITÉS UNIQUES :
 * 1. Texte orientable : horizontal, vertical (haut), vertical (bas)
 * 2. Fond arrondi avec effet de profondeur
 * 3. Texte avec ombre portée pour meilleure lisibilité
 * 4. Padding personnalisable
 * 5. Texte désactivé avec transparence
 * 
 * TECHNIQUE CLÉ : Utilisation d'AffineTransform pour les rotations
 * C'est une application pratique des transformations géométriques 2D.
 * 
 * @author FIDELE
 */
public class HBasicLabelUI extends BasicLabelUI {
    
    // ===================================================================
    // VARIABLES D'INSTANCE
    // ===================================================================
    
    /** Référence vers notre HLabel personnalisé */
    private HLabel hLabel;
    
    // ===================================================================
    // MÉTHODE D'INSTALLATION DE L'UI
    // ===================================================================
    
    @Override
    public void installUI(JComponent c) {
        // Initialisation de base du LabelUI
        super.installUI(c);
        
        // Garder une référence vers notre composant personnalisé
        this.hLabel = (HLabel) c;
    }
    
    // ===================================================================
    // MÉTHODE PRINCIPALE DE DESSIN
    // ===================================================================
    
    @Override
    public void paint(Graphics g, JComponent c) {
        // Créer un contexte Graphics2D à partir du Graphics de base
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Activer l'anti-crénelage pour des bords lisses
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Récupérer le style du label
        HLabelStyle style = hLabel.getLabelStyle();
        
        // Dimensions du composant
        int width = c.getWidth();
        int height = c.getHeight();
        
        // Padding configuré
        int padding = hLabel.getPadding();
        
        // ===============================================================
        // ÉTAPE 1 : DESSINER LE FOND ARRONDI (SI ACTIVÉ)
        // ===============================================================
        
        if (hLabel.hasRoundedBackground()) {
            // Rayon des coins arrondis
            int radius = hLabel.getCornerRadius();
            
            // --- Dessiner le fond principal ---
            g2.setColor(style.getBackgroundColor());
            g2.fillRoundRect(0, 0, width, height, radius, radius);
            
            // --- Dessiner une bordure subtile ---
            g2.setColor(style.getTextColor().darker()); // Version plus foncée de la couleur du texte
            g2.setStroke(new BasicStroke(1.0f)); // Bordure fine de 1 pixel
            g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
            
            // --- Effet de profondeur (simulation de lumière) ---
            // Créer un dégradé subtil du haut vers le bas
            
            // 1. Appliquer une transparence de 10% (0.1f)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            
            // 2. Dessiner une forme blanche sur la moitié supérieure
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(1, 1, width - 2, height / 2, radius, radius);
            
            // 3. Restaurer l'opacité complète
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        // ===============================================================
        // ÉTAPE 2 : SAUVEGARDER LA TRANSFORMATION ORIGINALE
        // ===============================================================
        
        // AffineTransform représente une transformation géométrique
        // On la sauvegarde pour pouvoir la restaurer après les rotations
        AffineTransform originalTransform = g2.getTransform();
        
        // ===============================================================
        // ÉTAPE 3 : APPLIQUER L'ORIENTATION DU TEXTE
        // ===============================================================
        
        // Switch sur le type d'orientation configuré
        switch (hLabel.getOrientation()) {
            case HORIZONTAL:
                // Orientation normale : pas de transformation
                paintHorizontalText(g2, c, padding);
                break;
                
            case VERTICAL_UP:
            case VERTICAL:
                // Texte vertical orienté vers le haut
                // Rotation de -90 degrés (sens anti-horaire)
                g2.rotate(Math.toRadians(-90));
                
                // Translation pour compenser la rotation
                // Après rotation de -90°, le système de coordonnées est tourné
                // On doit translater de -height sur l'axe X pour recentrer
                g2.translate(-height, 0);
                
                // Dessiner le texte vertical
                paintVerticalText(g2, c, padding, false);
                break;
                
            case VERTICAL_DOWN:
                // Texte vertical orienté vers le bas
                // Rotation de +90 degrés (sens horaire)
                g2.rotate(Math.toRadians(90));
                
                // Translation pour compenser la rotation
                // Après rotation de +90°, on doit translater de -width sur l'axe Y
                g2.translate(0, -width);
                
                // Dessiner le texte vertical (inversé)
                paintVerticalText(g2, c, padding, true);
                break;
        }
        
        // ===============================================================
        // ÉTAPE 4 : RESTAURER LA TRANSFORMATION ORIGINALE
        // ===============================================================
        
        // Important : restaurer l'état initial du Graphics2D
        // pour ne pas affecter les autres opérations de dessin
        g2.setTransform(originalTransform);
        
        // Libérer le contexte graphique
        g2.dispose();
    }
    
    // ===================================================================
    // DESSIN DU TEXTE HORIZONTAL
    // ===================================================================
    
    /**
     * Dessine le texte horizontalement (orientation normale)
     * 
     * @param g2 Contexte graphique (déjà configuré)
     * @param c Le composant label
     * @param padding Le padding configuré
     */
    private void paintHorizontalText(Graphics2D g2, JComponent c, int padding) {
        // Récupérer le texte du label
        String text = hLabel.getText();
        if (text == null || text.isEmpty()) return;
        
        // Obtenir les métriques de la police pour mesurer le texte
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // ===============================================================
        // CALCUL DE LA POSITION CENTRÉE
        // ===============================================================
        
        // Position X : centré horizontalement
        int x = (c.getWidth() - textWidth) / 2;
        
        // Position Y : centré verticalement
        // Formule : (hauteur + ascent - descent) / 2
        // ascent : hauteur au-dessus de la ligne de base
        // descent : hauteur en dessous de la ligne de base
        int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        
        // ===============================================================
        // AJUSTEMENT AVEC PADDING (SI FOND ARRONDI)
        // ===============================================================
        
        if (hLabel.hasRoundedBackground()) {
            // Garantir un padding minimal
            x = Math.max(padding, x);
            y = Math.max(padding + fm.getAscent(), y);
        }
        
        // ===============================================================
        // EFFET D'OMBRE SOUS LE TEXTE (AMÉLIORE LA LISIBILITÉ)
        // ===============================================================
        
        if (hLabel.hasRoundedBackground()) {
            // Dessiner une ombre noire semi-transparente
            // Décalée de 1 pixel vers le bas-droite
            g2.setColor(new Color(0, 0, 0, 50)); // Noir à 20% d'opacité
            g2.drawString(text, x + 1, y + 1);
        }
        
        // ===============================================================
        // TEXTE PRINCIPAL
        // ===============================================================
        
        // Utiliser la couleur de premier plan du label
        g2.setColor(hLabel.getForeground());
        g2.drawString(text, x, y);
    }
    
    // ===================================================================
    // DESSIN DU TEXTE VERTICAL
    // ===================================================================
    
    /**
     * Dessine le texte verticalement (après rotation)
     * 
     * @param g2 Contexte graphique (déjà transformé par rotation)
     * @param c Le composant label
     * @param padding Le padding configuré
     * @param reversed true si le texte est inversé (VERTICAL_DOWN)
     */
    private void paintVerticalText(Graphics2D g2, JComponent c, int padding, boolean reversed) {
        // Récupérer le texte
        String text = hLabel.getText();
        if (text == null || text.isEmpty()) return;
        
        // Obtenir les métriques de la police
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // ===============================================================
        // INVERSION DES DIMENSIONS (À CAUSE DE LA ROTATION)
        // ===============================================================
        
        // Après rotation de 90° :
        // - La largeur du composant devient la hauteur dans le nouveau système
        // - La hauteur du composant devient la largeur dans le nouveau système
        int componentWidth = c.getHeight();  // Inversé
        int componentHeight = c.getWidth();  // Inversé
        
        // ===============================================================
        // CALCUL DE LA POSITION CENTRÉE (DANS LE SYSTÈME TOURNÉ)
        // ===============================================================
        
        // Position X centrée (dans le système tourné)
        int x = (componentWidth - textWidth) / 2;
        
        // Position Y centrée (dans le système tourné)
        int y = (componentHeight + fm.getAscent() - fm.getDescent()) / 2;
        
        // ===============================================================
        // AJUSTEMENT AVEC PADDING
        // ===============================================================
        
        if (hLabel.hasRoundedBackground()) {
            // Padding minimal
            x = Math.max(padding, x);
            y = Math.max(padding + fm.getAscent(), y);
            
            // Pour le texte inversé (VERTICAL_DOWN), ajuster différemment
            if (reversed) {
                // Aligner en bas plutôt qu'en haut
                y = componentHeight - padding - fm.getDescent();
            }
        }
        
        // ===============================================================
        // EFFET D'OMBRE (MÊME PRINCIPE QUE POUR L'HORIZONTAL)
        // ===============================================================
        
        if (hLabel.hasRoundedBackground()) {
            g2.setColor(new Color(0, 0, 0, 50));
            g2.drawString(text, x + 1, y + 1);
        }
        
        // ===============================================================
        // TEXTE PRINCIPAL
        // ===============================================================
        
        g2.setColor(hLabel.getForeground());
        g2.drawString(text, x, y);
    }
    
    // ===================================================================
    // PERSONNALISATION DU TEXTE DÉSACTIVÉ
    // ===================================================================
    
    /**
     * Redéfinit le dessin du texte désactivé
     * Utilise la transparence au lieu de la couleur grise standard
     * 
     * @param l Le label
     * @param g Contexte graphique
     * @param s Le texte à dessiner
     * @param textX Position X
     * @param textY Position Y
     */
    @Override
    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
        // Créer un contexte Graphics2D local
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Appliquer une transparence de 50%
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        
        // Appeler la méthode parente avec le contexte modifié
        super.paintDisabledText(l, g2, s, textX, textY);
        
        // Libérer le contexte
        g2.dispose();
    }
} 