package hcomponents.vues; 

// Importations nécessaires
import hcomponents.HComboBox; 
import java.awt.*; // Classes graphiques
import java.awt.event.*; // Événements
import java.awt.geom.*; // Formes géométriques
import javax.swing.*; // Composants Swing
import javax.swing.plaf.basic.BasicComboBoxUI; // UI de base à étendre
import javax.swing.plaf.basic.BasicComboPopup; // Popup de base
import javax.swing.plaf.basic.ComboPopup; // Interface pour la popup

/**
 * HBasicComboBoxUI - UI Delegate pour HComboBox
 * 
 * CONCEPT CLÉ : Un HComboBox est un composant composite qui combine :
 * 1. Un champ d'affichage (pour l'élément sélectionné)
 * 2. Un bouton flèche (pour ouvrir la liste)
 * 3. Une popup avec liste déroulante
 * 
 * ARCHITECTURE COMPLEXE :
 * BasicComboBoxUI → gère le champ + flèche
 * BasicComboPopup → gère la liste déroulante
 * ListCellRenderer → gère le rendu de chaque item
 * 
 * PERSONNALISATIONS :
 * 1. Animation de survol sur le combo principal
 * 2. Flèche personnalisée 
 * 3. Popup avec coins arrondis
 * 4. Items de liste avec fond arrondi
 * 5. Design cohérent entre combo et popup
 * 
 * @author FIDELE
 */
public class HBasicComboBoxUI extends BasicComboBoxUI {
    
    // ===================================================================
    // VARIABLES D'INSTANCE
    // ===================================================================
    
    /** Référence vers notre HComboBox personnalisé */
    private HComboBox<?> hComboBox;
    
    /** Progression de l'animation de survol (0.0 à 1.0) */
    private float hoverProgress = 0f;
    
    /** Timer pour l'animation de survol */
    private Timer hoverTimer;
    
    /** État de survol actuel */
    private boolean isHovering = false;
    
    // ===================================================================
    // CONSTANTES D'ANIMATION
    // ===================================================================
    
    /** Durée de l'animation en millisecondes */
    private static final int ANIMATION_DURATION = 200;
    
    /** Images par seconde cible */
    private static final int FPS = 60;
    
    /** Délai entre les frames (1000ms / 60 FPS ≈ 16ms) */
    private static final int FRAME_DELAY = 1000 / FPS;

    // ===================================================================
    // MÉTHODE D'INSTALLATION DE L'UI
    // ===================================================================
    
    @Override
    public void installUI(JComponent c) {
        // Initialisation de base du ComboBoxUI
        super.installUI(c);
        
        // Garder une référence vers notre composant
        if (c instanceof HComboBox) {
            hComboBox = (HComboBox<?>) c;
        }
        
        // Configuration visuelle
        c.setOpaque(false); // Transparent pour notre dessin personnalisé
        c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding interne
        
        // ===============================================================
        // ÉCOUTEURS DE SOURIS POUR L'ANIMATION DE SURVOL
        // ===============================================================
        
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Souris entrée dans le combo
                if (hComboBox != null && hComboBox.isHoverEnabled()) {
                    isHovering = true;
                    animateHover(true, c);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Souris sortie du combo
                if (hComboBox != null && hComboBox.isHoverEnabled()) {
                    isHovering = false;
                    animateHover(false, c);
                }
            }
        });
    }
    
    // ===================================================================
    // ANIMATION DU SURVOL
    // ===================================================================
    
    /**
     * Anime l'effet de survol sur le combo
     * 
     * @param in true pour entrée (survol), false pour sortie
     * @param c Le composant à redessiner
     */
    private void animateHover(boolean in, JComponent c) {
        // Si animations désactivées, état final immédiat
        if (hComboBox == null || !hComboBox.isAnimationsEnabled()) {
            hoverProgress = in ? 1f : 0f;
            c.repaint();
            return;
        }
        
        // Arrêter un timer existant
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        
        // Calcul de l'animation
        float startProgress = hoverProgress;
        long startTime = System.currentTimeMillis();
        
        // Créer un nouveau timer
        hoverTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Interpolation
                hoverProgress = in ? 
                    (startProgress + (1f - startProgress) * progress) :
                    (startProgress - startProgress * progress);
                
                c.repaint();
                
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        // Démarrer le timer
        hoverTimer.start();
    }
    
    // ===================================================================
    // MÉTHODE PRINCIPALE DE DESSIN
    // ===================================================================
    
    @Override
    public void paint(Graphics g, JComponent c) {
        // Créer un contexte Graphics2D
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dimensions
        int width = c.getWidth();
        int height = c.getHeight();
        int radius = (hComboBox != null) ? hComboBox.getCornerRadius() : 10;
        
        // ===============================================================
        // ÉTAPE 1 : DESSINER LE FOND AVEC ANIMATION
        // ===============================================================
        
        paintBackground(g2, c, width, height, radius);
        
        // ===============================================================
        // ÉTAPE 2 : DESSINER LA BORDURE
        // ===============================================================
        
        paintBorder(g2, c, width, height, radius);
        
        // Libérer le contexte
        g2.dispose();
        
        // ===============================================================
        // ÉTAPE 3 : DESSINER LE CONTENU (PARENT)
        // ===============================================================
        
        // La classe parente dessine :
        // - Le texte de l'élément sélectionné
        // - Le bouton flèche (via createArrowButton())
        super.paint(g, c);
    }
    
    // ===================================================================
    // DESSIN DU FOND
    // ===================================================================
    
    /**
     * Dessine le fond arrondi avec animation de survol
     */
    private void paintBackground(Graphics2D g2, JComponent c, int width, int height, int radius) {
        if (hComboBox == null || hComboBox.getComboStyle() == null) return;
        
        HComboBoxStyle style = hComboBox.getComboStyle();
        Color baseColor = style.getBackground();
        Color hoverColor = style.getHoverBackground();
        
        // Interpolation entre couleur normale et survol
        Color currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
        
        // Dessiner le rectangle arrondi
        g2.setColor(currentColor);
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            2, 2,           // Position avec petite marge
            width - 4,      // Largeur moins marges
            height - 4,     // Hauteur moins marges
            radius, radius  // Rayon des coins
        );
        g2.fill(roundRect);
    }
    
    // ===================================================================
    // DESSIN DE LA BORDURE
    // ===================================================================
    
    /**
     * Dessine la bordure arrondie
     */
    private void paintBorder(Graphics2D g2, JComponent c, int width, int height, int radius) {
        if (hComboBox == null || hComboBox.getComboStyle() == null) return;
        
        HComboBoxStyle style = hComboBox.getComboStyle();
        g2.setColor(style.getBorderColor());
        g2.setStroke(new BasicStroke(2f)); // Bordure épaisse de 2px
        
        // Dessiner le contour
        g2.draw(new RoundRectangle2D.Float(
            2, 2, width - 4, height - 4, radius, radius
        ));
    }
    
    // ===================================================================
    // CRÉATION DU BOUTON FLÈCHE PERSONNALISÉ
    // ===================================================================
    
    /**
     * Crée le bouton flèche personnalisé (triangle vers le bas)
     * Cette méthode est appelée par BasicComboBoxUI
     */
    @Override
    protected JButton createArrowButton() {
        // Créer un JButton anonyme avec rendu personnalisé
        JButton button = new JButton() {
            /**
             * Dessin personnalisé du bouton flèche
             */
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dimensions
                int width = getWidth();
                int height = getHeight();
                
                // Dessiner la flèche
                paintArrow(g2, width, height);
                
                g2.dispose();
            }
            
            /**
             * Taille préférée du bouton
             */
            @Override
            public Dimension getPreferredSize() {
                // Largeur fixe de 30px, hauteur adaptative de 20px
                return new Dimension(30, 20);
            }
        };
        
        // Configuration pour un bouton invisible (sans décorations Swing)
        button.setBorder(BorderFactory.createEmptyBorder()); // Pas de bordure
        button.setOpaque(false);                            // Transparent
        button.setFocusPainted(false);                      // Pas d'indicateur de focus
        button.setContentAreaFilled(false);                 // Pas de fond Swing
        
        return button;
    }
    
    // ===================================================================
    // DESSIN DE LA FLÈCHE VECTORIELLE
    // ===================================================================
    
    /**
     * Dessine la flèche (triangle pointant vers le bas)
     */
    private void paintArrow(Graphics2D g2, int width, int height) {
        if (hComboBox == null || hComboBox.getComboStyle() == null) return;
        
        // Taille configurable ou par défaut
        int arrowSize = (hComboBox != null) ? hComboBox.getArrowSize() : 8;
        
        // Position centrée
        int x = width / 2;
        int y = height / 2;
        
        // ===============================================================
        // CRÉATION DU TRIANGLE
        // ===============================================================
        
        Path2D arrow = new Path2D.Float();
        
        // Point 1 : haut gauche
        arrow.moveTo(-arrowSize/2, -arrowSize/4);
        
        // Point 2 : haut droit
        arrow.lineTo(arrowSize/2, -arrowSize/4);
        
        // Point 3 : bas centre
        arrow.lineTo(0, arrowSize/2);
        
        // Fermer le triangle
        arrow.closePath();
        
        // ===============================================================
        // APPLIQUER LES TRANSFORMATIONS
        // ===============================================================
        
        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y); // Centrer
        
        // Remplir le triangle avec la couleur du style
        HComboBoxStyle style = hComboBox.getComboStyle();
        g2.setColor(style.getArrowColor());
        g2.fill(arrow);
        
        // Restaurer la transformation
        g2.setTransform(oldTransform);
    }
    
    // ===================================================================
    // CRÉATION DE LA POPUP PERSONNALISÉE
    // ===================================================================
    
    /**
     * Crée la popup (liste déroulante) personnalisée
     * Surchargé pour personnaliser l'apparence de la liste
     */
    @Override
    protected ComboPopup createPopup() {
        // Créer une sous-classe de BasicComboPopup
        return new BasicComboPopup(comboBox) {
            
            /**
             * Configure la popup lors de sa création
             */
            @Override
            protected void configurePopup() {
                super.configurePopup(); // Configuration de base
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding
                setOpaque(false); // Transparent pour notre dessin
            }
            
            /**
             * Appelé quand la popup s'ouvre
             */
            @Override
            public void show() {
                // Configurer le renderer pour la liste
                if (list != null && hComboBox != null) {
                    // Installer notre renderer personnalisé
                    list.setCellRenderer(new ModernComboBoxRenderer());
                    
                    // Configurer les couleurs de la liste
                    if (hComboBox.getComboStyle() != null) {
                        HComboBoxStyle style = hComboBox.getComboStyle();
                        list.setBackground(style.getPopupBackground());
                        list.setSelectionBackground(style.getSelectedBackground());
                        list.setSelectionForeground(style.getSelectedTextColor());
                    }
                }
                
                // Appeler la méthode parente pour afficher la popup
                super.show();
            }
            
            /**
             * Dessin personnalisé de la popup
             */
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dimensions
                int width = getWidth();
                int height = getHeight();
                int radius = (hComboBox != null) ? hComboBox.getCornerRadius() : 8;
                
                // =======================================================
                // FOND ARRONDI
                // =======================================================
                
                if (hComboBox != null && hComboBox.getComboStyle() != null) {
                    g2.setColor(hComboBox.getComboStyle().getPopupBackground());
                } else {
                    g2.setColor(Color.WHITE); // Fallback
                }
                
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    0, 0, width, height, radius, radius
                );
                g2.fill(roundRect);
                
                // =======================================================
                // BORDURE
                // =======================================================
                
                if (hComboBox != null && hComboBox.getComboStyle() != null) {
                    g2.setColor(hComboBox.getComboStyle().getBorderColor());
                    g2.setStroke(new BasicStroke(1f)); // Bordure fine
                    g2.draw(roundRect);
                }
                
                g2.dispose();
            }
        };
    }
    
    // ===================================================================
    // MÉTHODE UTILITAIRE : INTERPOLATION DE COULEURS
    // ===================================================================
    
    /**
     * Interpole linéairement entre deux couleurs
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }
    
    // ===================================================================
    // MÉTHODE DE NETTOYAGE
    // ===================================================================
    
    @Override
    public void uninstallUI(JComponent c) {
        // Arrêter le timer d'animation
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        
        // Appeler le nettoyage parent
        super.uninstallUI(c);
    }
    
    // ===================================================================
    // RENDERER PERSONNALISÉ POUR LES ITEMS DE LA LISTE
    // ===================================================================
    
    /**
     * ModernComboBoxRenderer - Renderer personnalisé pour les items
     * 
     * IMPORTANT : Ce renderer étend DefaultListCellRenderer mais
     * modifie son comportement pour dessiner des fonds arrondis.
     * 
     * PROBLÈME RÉSOLU : Par défaut, DefaultListCellRenderer est opaque
     * et remplit un rectangle. Nous le rendons transparent (opaque=false)
     * et dessinons notre propre fond arrondi.
     */
    private class ModernComboBoxRenderer extends DefaultListCellRenderer {
        
        private static final long serialVersionUID = 1L;
        
        // Variables pour conserver l'état entre les appels
        private boolean cellSelected = false;
        private int cellIndex = -1;
        private Object cellValue = null;
        
        /**
         * Configure le renderer pour un item spécifique
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            
            // Appeler la méthode parente pour la configuration de base
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            // ===========================================================
            // CONFIGURATION POUR LE FOND ARRONDI
            // ===========================================================
            
            // IMPORTANT : opaque=false pour que notre paintComponent()
            // puisse dessiner le fond arrondi sans qu'il soit effacé
            setOpaque(false);
            
            // Ajouter du padding interne
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            
            // ===========================================================
            // SAUVEGARDE DE L'ÉTAT POUR LE DESSIN
            // ===========================================================
            
            // Sauvegarder l'état pour l'utiliser dans paintComponent()
            cellSelected = isSelected;
            cellIndex = index;
            cellValue = value;
            
            // ===========================================================
            // CONFIGURATION DES COULEURS
            // ===========================================================
            
            if (hComboBox != null && hComboBox.getComboStyle() != null) {
                HComboBoxStyle style = hComboBox.getComboStyle();
                
                if (isSelected) {
                    // Texte blanc sur fond coloré pour l'item sélectionné
                    setForeground(style.getSelectedTextColor());
                } else {
                    // Texte normal pour les autres items
                    setForeground(style.getTextColor());
                }
            }
            
            // Le texte est déjà défini par la méthode parente
            return this;
        }
        
        /**
         * Dessin personnalisé de l'item avec fond arrondi
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Dimensions
            int width = getWidth();
            int height = getHeight();
            int radius = (hComboBox != null) ? hComboBox.getCornerRadius() : 6;
            
            // ===========================================================
            // DESSIN DU FOND ARRONDI POUR L'ITEM SÉLECTIONNÉ
            // ===========================================================
            
            if (hComboBox != null && hComboBox.getComboStyle() != null) {
                HComboBoxStyle style = hComboBox.getComboStyle();
                
                if (cellSelected) {
                    // Dessiner un fond arrondi pour l'item sélectionné
                    g2.setColor(style.getSelectedBackground());
                    RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                        2, 1,           // Position avec petite marge
                        width - 4,      // Largeur moins marges
                        height - 2,     // Hauteur moins marges
                        radius, radius  // Rayon des coins
                    );
                    g2.fill(roundRect);
                }
                // Pour les items non sélectionnés, on laisse le fond transparent
                // pour voir la couleur de fond de la popup
            }
            
            g2.dispose();
            
            // ===========================================================
            // DESSIN DU TEXTE (PARENT)
            // ===========================================================
            
            // IMPORTANT : Appeler super.paintComponent() APRÈS avoir dessiné notre fond
            // Comme opaque=false, la méthode parente ne dessinera pas de fond rectangulaire
            super.paintComponent(g);
        }
    }
} 