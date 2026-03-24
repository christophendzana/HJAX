package hcomponents.vues; // Package pour les UI delegates

// Importations nécessaires
import hcomponents.HMenu; // Notre composant personnalisé
import java.awt.*; // Classes graphiques
import java.awt.event.*; // Événements
import java.awt.geom.RoundRectangle2D; // Formes arrondies
import javax.swing.*; // Composants Swing
import javax.swing.plaf.basic.BasicMenuUI; // UI de base à étendre

/**
 * HBasicMenuUI - UI Delegate moderne pour HMenu
 * 
 * CONCEPT CLÉ : Un JMenu est un composant qui s'insère dans une JMenuBar
 * et qui peut contenir des JMenuItem. Notre personnalisation ajoute des
 * animations de survol et un design moderne aux menus.
 * 
 * HIÉRARCHIE DES MENUS SWING :
 * JMenuBar (barre de menus)
 *   └── JMenu (menu déroulant, comme "Fichier")
 *        ├── JMenuItem (élément de menu, comme "Ouvrir")
 *        ├── JSeparator (séparateur)
 *        └── JMenu (sous-menu)
 * 
 * PARTICULARITÉS DES MENUS :
 * 1. Ils peuvent être "armés" (prêts à s'ouvrir)
 * 2. Ils ont des états de survol et de sélection
 * 3. Ils peuvent contenir d'autres menus (hiérarchie)
 * 4. Ils s'ouvrent en popup quand on clique dessus
 * 
 * PERSONNALISATIONS :
 * 1. Animation de survol fluide
 * 2. Couleurs personnalisées depuis un style
 * 3. Éclairage progressif au survol
 * 
 * @author FIDELE
 * @version 1.0
 */
public class HBasicMenuUI extends BasicMenuUI {
    
    // ===================================================================
    // VARIABLES D'INSTANCE
    // ===================================================================
    
    /** Référence vers notre HMenu personnalisé */
    private HMenu hMenu;
    
    /** Progression de l'animation de survol (0.0 à 1.0) */
    private float hoverProgress = 0f;
    
    /** Timer pour l'animation de survol */
    private Timer hoverTimer;
    
    /** État de survol actuel (souris sur le menu) */
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
        // Initialisation de base du MenuUI
        super.installUI(c);
        
        // Garder une référence vers notre composant personnalisé
        if (c instanceof HMenu) {
            hMenu = (HMenu) c;
        }
        
        // ===============================================================
        // ÉCOUTEURS DE SOURIS POUR LES ANIMATIONS DE SURVOL
        // ===============================================================
        
        // MouseListener pour détecter l'entrée et la sortie de la souris
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // La souris est entrée dans le menu
                if (hMenu != null && hMenu.isHoverEnabled()) {
                    isHovering = true;
                    animateHover(true, c); // Animer l'entrée
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // La souris a quitté le menu
                if (hMenu != null && hMenu.isHoverEnabled()) {
                    isHovering = false;
                    animateHover(false, c); // Animer la sortie
                }
            }
        });
    }
    
    // ===================================================================
    // ANIMATION DU SURVOL
    // ===================================================================
    
    /**
     * Anime l'effet de survol sur le menu
     * 
     * @param in true pour entrée (survol), false pour sortie
     * @param c Le composant menu à redessiner
     */
    private void animateHover(boolean in, JComponent c) {
        // Si les animations sont désactivées, état final immédiat
        if (hMenu == null || !hMenu.isAnimationsEnabled()) {
            hoverProgress = in ? 1f : 0f;
            c.repaint();
            return;
        }
        
        // Arrêter un timer existant
        if (hoverTimer != null) {
            hoverTimer.stop();
        }
        
        // Déterminer la progression de départ
        float startProgress = hoverProgress;
        long startTime = System.currentTimeMillis();
        
        // Créer un nouveau timer
        hoverTimer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calculer le temps écoulé
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Calculer la progression normalisée
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Calculer la progression courante
                // Pour l'entrée : de startProgress vers 1.0
                // Pour la sortie : de startProgress vers 0.0
                hoverProgress = in ? 
                    (startProgress + (1f - startProgress) * progress) :
                    (startProgress - startProgress * progress);
                
                // Redessiner le menu
                c.repaint();
                
                // Arrêter le timer si terminé
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        
        // Démarrer le timer
        hoverTimer.start();
    }
    
    // ===================================================================
    // PERSONNALISATION DU FOND DU MENU
    // ===================================================================
    
    /**
     * Redéfinit le dessin du fond du menu
     * Cette méthode est appelée par Swing pour dessiner l'arrière-plan
     * du menu dans la barre de menus
     * 
     * @param g Contexte graphique
     * @param menuItem Le menu (ici c'est un HMenu)
     * @param bgColor La couleur de fond par défaut (ignorée)
     */
    @Override
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
        // Créer un contexte Graphics2D
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dimensions du menu
        int width = menuItem.getWidth();
        int height = menuItem.getHeight();
        
        // Vérifier que nous avons accès au style
        if (hMenu != null && hMenu.getMenuStyle() != null) {
            HMenuStyle style = hMenu.getMenuStyle();
            
            // Récupérer la couleur de base de la barre de menus
            Color baseColor = style.getMenuBarColor();
            
            // Créer une version plus claire pour l'effet hover
            Color hoverColor = brighten(baseColor, 0.2f);
            
            // Interpoler entre la couleur normale et hover
            Color currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
            
            // Remplir le fond avec la couleur courante
            g2.setColor(currentColor);
            g2.fillRect(0, 0, width, height);
        }
        
        // Libérer le contexte
        g2.dispose();
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
    // MÉTHODE UTILITAIRE : ÉCLAIRCIR UNE COULEUR
    // ===================================================================
    
    /**
     * Éclaire une couleur en l'approchant du blanc
     * 
     * @param color Couleur d'origine
     * @param facteur Facteur d'éclairage (0.0 = pas de changement, 1.0 = blanc pur)
     * @return La couleur éclaircie
     */
    private Color brighten(Color color, float facteur) {
        // Formule : composante_resultante = composante + (255 - composante) × facteur
        // Cela approche chaque composante de 255 (blanc)
        
        int r = Math.min(255, (int) (color.getRed() + (255 - color.getRed()) * facteur));
        int g = Math.min(255, (int) (color.getGreen() + (255 - color.getGreen()) * facteur));
        int b = Math.min(255, (int) (color.getBlue() + (255 - color.getBlue()) * facteur));
        
        return new Color(r, g, b);
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
} 