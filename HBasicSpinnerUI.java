package hcomponents.vues; // Package pour les UI delegates

// Importations nécessaires
import hcomponents.HSpinner; // Notre composant personnalisé
import java.awt.*; // Classes graphiques
import java.awt.event.*; // Événements
import java.awt.geom.*; // Formes géométriques
import javax.swing.*; // Composants Swing
import javax.swing.plaf.basic.BasicSpinnerUI; // UI de base à étendre

/**
 * HBasicSpinnerUI - UI Delegate moderne pour HSpinner
 * 
 * CONCEPT CLÉ : Un JSpinner est un composant qui permet d'incrémenter/décrémenter
 * une valeur via des boutons fléchés. Notre personnalisation ajoute des effets
 * visuels modernes et des animations.
 * 
 * STRUCTURE D'UN SPINNER :
 * 1. Un champ de texte (pour afficher/modifier la valeur)
 * 2. Un bouton "suivant" (flèche vers le haut)
 * 3. Un bouton "précédent" (flèche vers le bas)
 * 
 * PERSONNALISATIONS :
 * 1. Effet de focus animé sur le champ de texte
 * 2. Boutons fléchés avec effets hover
 * 3. Coins arrondis et bordures stylisées
 * 4. Interpolation de couleurs pour les transitions
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicSpinnerUI extends BasicSpinnerUI {
    
    // ===================================================================
    // VARIABLES D'INSTANCE
    // ===================================================================
    
    /** Référence vers notre HSpinner personnalisé */
    private HSpinner hSpinner;
    
    /** État du focus (si le champ de texte a le focus) */
    private boolean isFocused = false;
    
    /** Progression de l'animation de focus (0.0 à 1.0) */
    private float focusProgress = 0f;
    
    /** Timer pour l'animation du focus */
    private Timer focusTimer;
    
    /** Durée de l'animation de focus en millisecondes */
    private static final int ANIMATION_DURATION = 200;

    // ===================================================================
    // MÉTHODE D'INSTALLATION DE L'UI
    // ===================================================================
    
    @Override
    public void installUI(JComponent c) {
        // Initialisation de base du SpinnerUI
        super.installUI(c);
        
        // Garder une référence vers notre composant
        if (c instanceof HSpinner) {
            hSpinner = (HSpinner) c;
        }
        
        // Désactiver l'opacité pour dessiner notre propre fond
        c.setOpaque(false);
        
        // ===============================================================
        // CONFIGURATION DE L'ÉDITEUR (CHAMP DE TEXTE)
        // ===============================================================
        
        // Récupérer l'éditeur du spinner (le champ de texte)
        JComponent editor = spinner.getEditor();
        
        // Vérifier que c'est l'éditeur par défaut de JSpinner
        if (editor instanceof JSpinner.DefaultEditor) {
            // Récupérer le champ de texte réel
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            
            // Configurer le champ de texte
            textField.setOpaque(false); // Transparent pour notre fond
            textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding interne
            
            // Définir la couleur du texte selon le style
            if (hSpinner != null && hSpinner.getSpinnerStyle() != null) {
                textField.setForeground(hSpinner.getSpinnerStyle().getTextColor());
            }
            
            // ===========================================================
            // ÉCOUTEURS DE FOCUS POUR L'ANIMATION
            // ===========================================================
            
            // FocusListener pour détecter quand le champ de texte prend/perd le focus
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    // Le champ de texte a reçu le focus
                    isFocused = true;
                    animateFocus(true); // Animer vers l'état "focused"
                }
                
                @Override
                public void focusLost(FocusEvent e) {
                    // Le champ de texte a perdu le focus
                    isFocused = false;
                    animateFocus(false); // Animer vers l'état "non-focused"
                }
            });
        }
    }
    
    // ===================================================================
    // ANIMATION DU FOCUS
    // ===================================================================
    
    /**
     * Anime la transition entre les états "focused" et "non-focused"
     * 
     * @param focusing true pour animer vers l'état focused, false pour l'inverse
     */
    private void animateFocus(boolean focusing) {
        // Si les animations sont désactivées, mettre directement l'état final
        if (hSpinner == null || !hSpinner.isAnimationsEnabled()) {
            focusProgress = focusing ? 1f : 0f;
            hSpinner.repaint(); // Redessiner avec le nouvel état
            return;
        }
        
        // Arrêter un timer existant
        if (focusTimer != null) {
            focusTimer.stop();
        }
        
        // Déterminer la progression de départ
        float startProgress = focusProgress;
        long startTime = System.currentTimeMillis();
        
        // Créer un nouveau timer (16ms = environ 60 FPS)
        focusTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calculer le temps écoulé
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Calculer la progression normalisée (0.0 à 1.0)
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Calculer la progression courante
                // Si on se focalise : de startProgress vers 1.0
                // Si on perd le focus : de startProgress vers 0.0
                focusProgress = focusing ? 
                    (startProgress + (1f - startProgress) * progress) :
                    (startProgress - startProgress * progress);
                
                // Redessiner le spinner
                hSpinner.repaint();
                
                // Arrêter le timer si l'animation est terminée
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        // Démarrer le timer
        focusTimer.start();
    }
    
    // ===================================================================
    // CRÉATION DES BOUTONS FLÉCHÉS
    // ===================================================================
    
    /**
     * Crée le bouton "suivant" (flèche vers le haut)
     * Cette méthode est appelée par BasicSpinnerUI
     */
    @Override
    protected Component createNextButton() {
        return createButton(true); // true = bouton "suivant"
    }
    
    /**
     * Crée le bouton "précédent" (flèche vers le bas)
     * Cette méthode est appelée par BasicSpinnerUI
     */
    @Override
    protected Component createPreviousButton() {
        return createButton(false); // false = bouton "précédent"
    }
    
    /**
     * Crée un bouton fléché personnalisé
     * 
     * @param isNext true pour bouton "suivant" (flèche haut), false pour "précédent" (flèche bas)
     * @return Le bouton JButton personnalisé
     */
    private Component createButton(boolean isNext) {
        // Créer un JButton anonyme avec un rendu personnalisé
        JButton button = new JButton() {
            // État de survol pour ce bouton
            private boolean isHovered = false;
            
            // Bloc d'initialisation d'instance (exécuté à la création)
            {
                // Configuration de base du bouton
                setOpaque(false);           // Pas de fond Swing standard
                setFocusPainted(false);     // Pas d'indicateur de focus
                setBorderPainted(false);    // Pas de bordure Swing standard
                setContentAreaFilled(false); // Pas de remplissage Swing
                setPreferredSize(new Dimension(25, 20)); // Taille fixe
                
                // =======================================================
                // ÉCOUTEURS DE SOURIS POUR L'EFFET HOVER
                // =======================================================
                
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        // La souris est entrée dans le bouton
                        isHovered = true;
                        repaint(); // Redessiner avec l'état hover
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        // La souris a quitté le bouton
                        isHovered = false;
                        repaint(); // Redessiner sans l'état hover
                    }
                });
            }
            
            /**
             * Méthode de dessin personnalisée du bouton
             */
            @Override
            protected void paintComponent(Graphics g) {
                // Créer un contexte Graphics2D
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vérifications
                if (hSpinner == null || hSpinner.getSpinnerStyle() == null) {
                    // Fallback : utiliser le rendu standard
                    super.paintComponent(g);
                    g2.dispose();
                    return;
                }
                
                // Récupérer le style
                HSpinnerStyle style = hSpinner.getSpinnerStyle();
                int w = getWidth();
                int h = getHeight();
                
                // =======================================================
                // DESSIN DU FOND DU BOUTON
                // =======================================================
                
                // Choisir la couleur selon l'état hover
                Color bgColor = isHovered ? 
                    style.getButtonHoverColor() : 
                    style.getButtonColor();
                
                g2.setColor(bgColor);
                
                // Dessiner le fond (arrondi ou non selon la position)
                // Note : ici les coins arrondis sont à 0 pour les boutons
                // On pourrait utiliser hSpinner.getCornerRadius() pour arrondir
                g2.fillRoundRect(0, 0, w, h, 0, hSpinner.getCornerRadius());
                
                // =======================================================
                // DESSIN DE LA FLÈCHE
                // =======================================================
                
                g2.setColor(style.getButtonIconColor());
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Centre du bouton
                int cx = w / 2;
                int cy = h / 2;
                int arrowSize = 4; // Taille de la flèche
                
                // Dessiner la flèche selon la direction
                if (isNext) {
                    // Flèche vers le haut
                    // Ligne gauche : du centre-gauche vers le centre-haut
                    g2.drawLine(cx - arrowSize, cy + 1, cx, cy - arrowSize + 1);
                    // Ligne droite : du centre vers le centre-droite
                    g2.drawLine(cx, cy - arrowSize + 1, cx + arrowSize, cy + 1);
                } else {
                    // Flèche vers le bas
                    // Ligne gauche : du centre-gauche vers le centre-bas
                    g2.drawLine(cx - arrowSize, cy - 1, cx, cy + arrowSize - 1);
                    // Ligne droite : du centre vers le centre-droite
                    g2.drawLine(cx, cy + arrowSize - 1, cx + arrowSize, cy - 1);
                }
                
                // Libérer le contexte
                g2.dispose();
            }
        };
        
        // ===============================================================
        // INSTALLATION DES ÉCOUTEURS D'ÉVÉNEMENTS
        // ===============================================================
        
        // BasicSpinnerUI fournit des méthodes pour installer les bons listeners
        // sur les boutons (pour incrémenter/décrémenter la valeur)
        if (isNext) {
            installNextButtonListeners(button); // Écouteurs pour "suivant"
        } else {
            installPreviousButtonListeners(button); // Écouteurs pour "précédent"
        }
        
        return button;
    }
    
    // ===================================================================
    // MÉTHODE PRINCIPALE DE DESSIN DU SPINNER
    // ===================================================================
    
    /**
     * Méthode principale de rendu du composant spinner
     * Dessine le fond et la bordure, puis laisse Swing dessiner le reste
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        // Créer un contexte Graphics2D
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vérifications
        if (hSpinner == null || hSpinner.getSpinnerStyle() == null) {
            // Fallback : utiliser le rendu standard
            super.paint(g, c);
            g2.dispose();
            return;
        }
        
        // Récupérer le style et les dimensions
        HSpinnerStyle style = hSpinner.getSpinnerStyle();
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = hSpinner.getCornerRadius();
        
        // ===============================================================
        // DESSIN DU FOND (AVEC ANIMATION DE FOCUS)
        // ===============================================================
        
        // Interpoler la couleur de fond entre l'état normal et l'état focused
        Color bgColor = interpolateColor(
            style.getBackground(),          // Couleur normale
            style.getFocusBackground(),     // Couleur focused
            focusProgress                   // Progression de l'animation
        );
        
        g2.setColor(bgColor);
        
        // Créer un rectangle arrondi avec une petite marge
        RoundRectangle2D background = new RoundRectangle2D.Float(
            2, 2,                    // Position avec marge de 2px
            width - 4, height - 4,   // Dimensions moins marges
            radius, radius           // Rayon des coins
        );
        
        // Remplir le fond
        g2.fill(background);
        
        // ===============================================================
        // DESSIN DE LA BORDURE (AVEC ANIMATION D'ÉPAISSEUR)
        // ===============================================================
        
        // Interpoler la couleur de bordure
        Color borderColor = interpolateColor(
            style.getBorderColor(),          // Couleur normale
            style.getFocusBorderColor(),     // Couleur focused
            focusProgress                    // Progression
        );
        
        g2.setColor(borderColor);
        
        // L'épaisseur de la bordure augmente avec le focus
        // 1.5px normal + jusqu'à 1px supplémentaire quand focused
        g2.setStroke(new BasicStroke(1.5f + focusProgress));
        
        // Dessiner la bordure
        g2.draw(background);
        
        // Libérer le contexte
        g2.dispose();
        
        // Appeler la méthode parente pour dessiner le reste (champ texte, boutons)
        super.paint(g, c);
    }
    
    // ===================================================================
    // MÉTHODE UTILITAIRE : INTERPOLATION DE COULEURS
    // ===================================================================
    
    /**
     * Interpole linéairement entre deux couleurs (sans canal alpha)
     * 
     * @param c1 Première couleur
     * @param c2 Deuxième couleur
     * @param progress Progression (0.0 = c1, 1.0 = c2)
     * @return La couleur interpolée
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        // S'assurer que la progression est entre 0 et 1
        progress = Math.max(0, Math.min(1, progress));
        
        // Interpolation linéaire pour chaque composante RGB
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        
        return new Color(r, g, b);
    }
    
    // ===================================================================
    // MÉTHODE DE NETTOYAGE
    // ===================================================================
    
    @Override
    public void uninstallUI(JComponent c) {
        // Arrêter le timer d'animation s'il est en cours
        if (focusTimer != null) {
            focusTimer.stop();
        }
        
        // Appeler le nettoyage parent
        super.uninstallUI(c);
    }
} // Fin de la classe HBasicSpinnerUI