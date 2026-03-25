package hcomponents.vues; 


import hcomponents.HTree; // Notre composant personnalisé
import java.awt.*; // Classes graphiques (Graphics, Color, etc.)
import java.awt.event.*; // Événements de souris et d'actions
import java.awt.geom.*; // Formes géométriques (Path2D, RoundRectangle2D)
import java.util.*; // Collections (HashMap, Map)
import javax.swing.*; // Composants Swing (JTree, Timer, etc.)
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicTreeUI; // UI de base à étendre
import javax.swing.tree.*; // Classes pour les arbres (TreePath, etc.)

/** 
 * CONCEPT CLÉ : Cette classe étend BasicTreeUI pour personnaliser complètement
 * le rendu d'un JTree. C'est l'une des UI les plus complexes de Swing car
 * un arbre a de nombreux états et éléments à dessiner.
 * 
 * RESPONSABILITÉS PRINCIPALES :
 * 1. Dessiner les nœuds avec coins arrondis et effets visuels
 * 2. Gérer les animations de survol (hover) sur les nœuds
 * 3. Animer les chevrons d'expansion/collapse
 * 4. Dessiner les lignes de connexion stylisées
 * 5. Fournir un renderer de cellule personnalisé
 * 
 * ARCHITECTURE DES ANIMATIONS :
 * Chaque animation utilise un Timer Swing qui s'exécute dans l'EDT.
 * Les progrès sont stockés dans des Maps pour chaque TreePath.
 * 
 * PERFORMANCE : Les Maps sont utilisées pour éviter de redessiner
 * tout l'arbre à chaque animation. Seul le nœud concerné est repaint.
 * 
 * @author FIDELE
 * @version 2.0
 */
public class HBasicTreeUI extends BasicTreeUI {
    // ===================================================================
    // VARIABLES D'INSTANCE - RÉFÉRENCES ET ÉTATS
    // ===================================================================
    
    /** Référence vers notre HTree personnalisé pour accéder aux propriétés */
    private HTree hTree;
    
    // ===================================================================
    // SYSTÈMES D'ANIMATIONS MULTIPLES
    // ===================================================================
    
    /** 
     * Animation de survol (hover) : 
     * - TreePath : Le chemin du nœud survolé
     * - Float : Progression de l'animation (0.0 à 1.0)
     */
    private Map<TreePath, Float> hoverProgress = new HashMap<>();
    
    /** 
     * Animation d'expansion :
     * - TreePath : Le chemin du nœud en train de s'ouvrir/fermer
     * - Float : Progression de l'animation (0.0 à 1.0)
     */
    private Map<TreePath, Float> expansionProgress = new HashMap<>();
    
    /** 
     * Rotation des chevrons :
     * - TreePath : Le chemin du nœud
     * - Float : Angle de rotation en degrés (0° = fermé, 90° = ouvert)
     */
    private Map<TreePath, Float> chevronRotation = new HashMap<>();
    
    /** 
     * Timers pour les animations de survol :
     * - TreePath : Le nœud animé
     * - Timer : Le timer qui gère l'animation
     */
    private Map<TreePath, Timer> hoverTimers = new HashMap<>();
    
    /** 
     * Timers pour les animations d'expansion :
     * - TreePath : Le nœud animé
     * - Timer : Le timer qui gère l'animation
     */
    private Map<TreePath, Timer> expansionTimers = new HashMap<>();
    
    /** 
     * Chemin actuellement survolé par la souris
     * Utilisé pour suivre les changements de survol
     */
    private TreePath hoveredPath = null;
    
    // ===================================================================
    // CONSTANTES D'ANIMATION
    // ===================================================================
    
    /** Durée totale des animations en millisecondes */
    private static final int ANIMATION_DURATION = 250;
    
    /** Images par seconde cible pour les animations */
    private static final int FPS = 60;
    
    /** Délai entre les frames en millisecondes */
    private static final int FRAME_DELAY = 1000 / FPS; // ≈ 16ms

    // ===================================================================
    // MÉTHODE D'INSTALLATION DE L'UI
    // ===================================================================
    
    @Override
    public void installUI(JComponent c) {
        // Appeler la méthode parente pour l'initialisation de base
        // Cette méthode configure les listeners de base de JTree
        super.installUI(c);
        
        // Vérifier que le composant est bien notre HTree personnalisé
        if (c instanceof HTree) {
            hTree = (HTree) c;
        }
        
        // Configuration initiale du JTree
        if (tree != null) { // 'tree' est une variable héritée de BasicTreeUI
            // Hauteur dynamique : 0 signifie que chaque ligne peut avoir une hauteur différente
            tree.setRowHeight(0);
            
            // Installer notre renderer de cellule personnalisé
            tree.setCellRenderer(new ModernTreeCellRenderer());
            
            // Désactiver l'opacité pour dessiner notre propre fond
            tree.setOpaque(false);
        }
        
        // ===============================================================
        // ÉCOUTEURS D'ÉVÉNEMENTS DE SOURIS POUR LE SURVOL
        // ===============================================================
        
        // MouseMotionListener pour détecter le mouvement de la souris
        c.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Appelé quand la souris bouge dans le composant
                handleMouseMoved(e, c);
            }
        });
        
        // MouseListener pour détecter quand la souris quitte le composant
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                // Si un nœud était survolé, déclencher l'animation de sortie
                if (hoveredPath != null) {
                    animateHover(hoveredPath, false, c);
                    hoveredPath = null;
                }
            }
        });
        
        // ===============================================================
        // ÉCOUTEUR D'EXPANSION/COLLAPSE POUR ANIMER LES CHEVRONS
        // ===============================================================
        
        if (tree != null) {
            // TreeExpansionListener pour détecter l'ouverture/fermeture des nœuds
            tree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
                @Override
                public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
                    // Nœud ouvert : animer le chevron vers 90°
                    animateExpansion(event.getPath(), true, c);
                }
                
                @Override
                public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {
                    // Nœud fermé : animer le chevron vers 0°
                    animateExpansion(event.getPath(), false, c);
                }
            });
        }
    }
    
    // ===================================================================
    // GESTION DU SURVOL DE LA SOURIS
    // ===================================================================
    
    /**
     * Gère le mouvement de la souris pour détecter les survols de nœuds
     */
    private void handleMouseMoved(MouseEvent e, JComponent c) {
        // Vérifier que l'arbre existe et que les effets hover sont activés
        if (hTree == null || !hTree.isHoverEnabled()) return;
        
        // Vérifier que le composant est bien un JTree
        if (c instanceof JTree) {
            JTree jtree = (JTree) c;
            
            // Trouver le nœud sous le curseur de la souris
            // getPathForLocation() retourne le TreePath correspondant aux coordonnées
            TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
            
            // Si le nœud survolé a changé
            if (path != hoveredPath) {
                // Animer la sortie du nœud précédemment survolé
                if (hoveredPath != null) {
                    animateHover(hoveredPath, false, c);
                }
                
                // Mettre à jour le nœud survolé
                hoveredPath = path;
                
                // Animer l'entrée sur le nouveau nœud
                if (hoveredPath != null) {
                    animateHover(hoveredPath, true, c);
                }
            }
        }
    }
    
    // ===================================================================
    // ANIMATION DE SURVOL (HOVER)
    // ===================================================================
    
    /**
     * Anime l'effet de survol sur un nœud
     * 
     * @param path Le chemin du nœud à animer
     * @param in true pour entrée (survol), false pour sortie
     * @param c Le composant à redessiner
     */
    private void animateHover(TreePath path, boolean in, JComponent c) {
        // Vérifications de base
        if (path == null || hTree == null || !hTree.isAnimationsEnabled()) {
            // Si pas d'animation, mettre directement l'état final
            if (path != null) {
                hoverProgress.put(path, in ? 1f : 0f);
                repaintPath(path, c);
            }
            return;
        }
        
        // Arrêter un timer existant pour ce nœud (éviter les conflits)
        Timer existing = hoverTimers.get(path);
        if (existing != null) existing.stop();
        
        // Déterminer la progression de départ
        float startProgress = hoverProgress.getOrDefault(path, in ? 0f : 1f);
        long startTime = System.currentTimeMillis();
        
        // Créer un nouveau timer pour cette animation
        Timer timer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calculer le temps écoulé
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Calculer la progression normalisée (0.0 à 1.0)
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Calculer la progression courante
                // Si entrée : de startProgress vers 1.0
                // Si sortie : de startProgress vers 0.0
                float current = in ? 
                    (startProgress + (1f - startProgress) * progress) : 
                    (startProgress - startProgress * progress);
                
                // Mettre à jour la progression dans la Map
                hoverProgress.put(path, current);
                
                // Redessiner uniquement ce nœud (optimisation)
                repaintPath(path, c);
                
                // Vérifier si l'animation est terminée
                if (progress >= 1f) {
                    // Arrêter le timer
                    ((Timer) e.getSource()).stop();
                    
                    // Nettoyer la Map des timers
                    hoverTimers.remove(path);
                }
            }
        });
        
        // Stocker le timer et le démarrer
        hoverTimers.put(path, timer);
        timer.start();
    }
    
    // ===================================================================
    // ANIMATION D'EXPANSION/COLLAPSE
    // ===================================================================
    
    /**
     * Anime la rotation du chevron lors de l'expansion/collapse
     * 
     * @param path Le chemin du nœud en train de s'ouvrir/fermer
     * @param expanding true pour expansion, false pour collapse
     * @param c Le composant à redessiner
     */
    private void animateExpansion(TreePath path, boolean expanding, JComponent c) {
        // Vérifications de base
        if (path == null || hTree == null || !hTree.isAnimationsEnabled()) {
            // Sans animation, mettre directement l'état final
            if (path != null) {
                chevronRotation.put(path, expanding ? 90f : 0f);
                repaintPath(path, c);
            }
            return;
        }
        
        // Arrêter un timer existant
        Timer existing = expansionTimers.get(path);
        if (existing != null) existing.stop();
        
        // Déterminer la rotation de départ
        float startRotation = chevronRotation.getOrDefault(path, expanding ? 0f : 90f);
        float targetRotation = expanding ? 90f : 0f;
        long startTime = System.currentTimeMillis();
        
        // Créer un nouveau timer
        Timer timer = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calculer le temps écoulé
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION);
                
                // Appliquer un easing "cubic out" pour un effet plus naturel
                // La formule : 1 - (1 - t)³ donne un démarrage rapide qui ralentit à la fin
                float eased = 1f - (float) Math.pow(1f - progress, 3);
                
                // Calculer la rotation courante
                float current = startRotation + (targetRotation - startRotation) * eased;
                
                // Mettre à jour la rotation
                chevronRotation.put(path, current);
                
                // Redessiner le nœud
                repaintPath(path, c);
                
                // Arrêter le timer si terminé
                if (progress >= 1f) {
                    ((Timer) e.getSource()).stop();
                    expansionTimers.remove(path);
                }
            }
        });
        
        // Démarrer le timer
        expansionTimers.put(path, timer);
        timer.start();
    }
    
    // ===================================================================
    // MÉTHODE UTILITAIRE : REDESSINER UN NŒUD SPÉCIFIQUE
    // ===================================================================
    
    /**
     * Redessine uniquement le rectangle d'un nœud spécifique
     * C'est une optimisation pour éviter de redessiner tout l'arbre
     * 
     * @param path Le chemin du nœud à redessiner
     * @param c Le composant JTree
     */
    private void repaintPath(TreePath path, JComponent c) {
        // Vérifications
        if (path == null || !(c instanceof JTree)) return;
        
        JTree jtree = (JTree) c;
        
        // Obtenir les limites (bounds) du nœud dans l'arbre
        // C'est le rectangle où le nœud est dessiné
        Rectangle bounds = jtree.getPathBounds(path);
        
        // Si le nœud est visible, demander son redessin
        if (bounds != null) {
            // repaint(bounds) ne redessine que la zone spécifiée
            jtree.repaint(bounds);
        }
    }
    
    // ===================================================================
    // PERSONNALISATION DES LIGNES DE CONNEXION
    // ===================================================================
    
    /**
     * Redéfinit le dessin des lignes verticales entre les nœuds
     * Ces lignes relient les parents à leurs enfants
     */
    @Override
    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
        // Vérifier si les lignes de connexion sont activées
        if (hTree != null && hTree.isShowConnectionLines()) {
            // Créer un contexte graphique local
            Graphics2D g2 = (Graphics2D) g.create();
            
            // Activer l'anti-crénelage pour des lignes lisses
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Récupérer la couleur des lignes depuis le style
            Color lineColor = hTree.getTreeStyle().getConnectionLineColor();
            
            // Créer une version très transparente (alpha = 40/255 ≈ 16%)
            // Cela donne des lignes très subtiles
            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), 
                                 lineColor.getBlue(), 40));
            
            // Définir l'épaisseur du trait
            g2.setStroke(new BasicStroke(1f));
            
            // Dessiner la ligne verticale
            g2.drawLine(x, top, x, bottom);
            
            // Libérer le contexte graphique
            g2.dispose();
        }
    }
    
    /**
     * Redéfinit le dessin des lignes horizontales entre les nœuds
     * Ces lignes relient les nœuds frères
     */
    @Override
    protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
        // Même logique que pour les lignes verticales
        if (hTree != null && hTree.isShowConnectionLines()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color lineColor = hTree.getTreeStyle().getConnectionLineColor();
            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), 
                                 lineColor.getBlue(), 40));
            g2.setStroke(new BasicStroke(1f));
            
            g2.drawLine(left, y, right, y);
            g2.dispose();
        }
    }
    
    // ===================================================================
    // MÉTHODE DE NETTOYAGE
    // ===================================================================
    
    @Override
    public void uninstallUI(JComponent c) {
        // Arrêter tous les timers en cours
        for (Timer t : hoverTimers.values()) {
            if (t != null) t.stop();
        }
        for (Timer t : expansionTimers.values()) {
            if (t != null) t.stop();
        }
        
        // Vider toutes les Maps pour libérer la mémoire
        hoverTimers.clear();
        expansionTimers.clear();
        hoverProgress.clear();
        expansionProgress.clear();
        chevronRotation.clear();
        
        // Appeler le nettoyage de la classe parente
        super.uninstallUI(c);
    }
    
    // ===================================================================
    // MÉTHODE UTILITAIRE : INTERPOLATION DE COULEURS
    // ===================================================================
    
    /**
     * Interpole linéairement entre deux couleurs
     * Utilisée pour les transitions fluides entre états
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        // S'assurer que la progression est entre 0 et 1
        progress = Math.max(0, Math.min(1, progress));
        
        // Interpolation linéaire pour chaque composante RGBA
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);
        
        return new Color(r, g, b, a);
    }
    
    // ===================================================================
    // CLASSE INTERNE : RENDERER DE CELLULE PERSONNALISÉ
    // ===================================================================
    
    /**
     * ModernTreeCellRenderer - Renderer personnalisé pour les cellules de l'arbre
     * 
     * CONCEPT : Un TreeCellRenderer est responsable du rendu de chaque nœud.
     * Swing appelle getTreeCellRendererComponent() pour chaque nœud visible,
     * puis paintComponent() pour le dessiner.
     * 
     * PARTICULARITÉ : Ce renderer étend JPanel plutôt que DefaultTreeCellRenderer.
     * Cela donne plus de contrôle mais est plus complexe.
     */
    private class ModernTreeCellRenderer extends JPanel implements TreeCellRenderer {
        
        // Composants enfants pour l'icône et le texte
        private JLabel iconLabel;
        private JLabel textLabel;
        
        // État actuel du nœud (mis à jour par getTreeCellRendererComponent)
        private TreePath currentPath;
        private boolean isSelected;
        private boolean isLeaf;
        private boolean isExpanded;
        
        /**
         * Constructeur du renderer
         */
        public ModernTreeCellRenderer() {
            // Utiliser un FlowLayout pour aligner icône et texte horizontalement
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
            
            // Ajouter du padding autour du contenu
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            
            // Transparent pour laisser notre paintComponent() dessiner le fond
            setOpaque(false);
            
            // Créer les labels pour l'icône et le texte
            iconLabel = new JLabel();
            textLabel = new JLabel();
            
            // Définir une police moderne
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            // Ajouter les labels au panel
            add(iconLabel);
            add(textLabel);
        }
        
        /**
         * Méthode appelée par Swing pour configurer le renderer pour un nœud spécifique
         * 
         * C'est ici que nous récupérons toutes les informations sur le nœud
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                     boolean sel, boolean expanded,
                                                     boolean leaf, int row, boolean hasFocus) {
            
            // Stocker l'état du nœud pour l'utiliser dans paintComponent()
            currentPath = tree.getPathForRow(row);
            isSelected = sel;
            isLeaf = leaf;
            isExpanded = expanded;
            
            // Définir le texte du nœud (la valeur toString() de l'objet)
            textLabel.setText(value.toString());
            
            // Définir la couleur du texte selon le style de l'arbre
            if (tree instanceof HTree) {
                HTree ht = (HTree) tree;
                if (ht.getTreeStyle() != null) {
                    textLabel.setForeground(ht.getTreeStyle().getTextColor());
                }
            } else {
                // Fallback si ce n'est pas un HTree
                textLabel.setForeground(Color.BLACK);
            }
            
            // Ne pas utiliser d'icône standard, nous dessinerons notre propre chevron
            iconLabel.setIcon(null);
            
            // Retourner ce panel comme composant de rendu
            return this;
        }
        
        /**
         * Méthode de dessin principale du renderer
         * C'est ici que nous dessinons le fond arrondi et les effets visuels
         */
        @Override
        protected void paintComponent(Graphics g) {
            // Créer un contexte Graphics2D pour les fonctionnalités avancées
            Graphics2D g2 = (Graphics2D) g.create();
            
            // Activer l'anti-crénelage pour des bords lisses
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Dimensions du composant
            int width = getWidth();
            int height = getHeight();
            
            // Récupérer l'instance HTree et son rayon de coin
            HTree ht = (tree instanceof HTree) ? (HTree) tree : null;
            int radius = (ht != null) ? ht.getCornerRadius() : 8;
            
            // ===============================================================
            // ÉTAPE 1 : CALCULER LA COULEUR DE FOND
            // ===============================================================
            
            Color bgColor = null;
            if (ht != null && ht.getTreeStyle() != null) {
                HTreeStyle style = ht.getTreeStyle();
                
                if (isSelected) {
                    // Si le nœud est sélectionné, utiliser la couleur de sélection
                    bgColor = style.getSelectedColor();
                } else {
                    // Sinon, interpoler entre la couleur normale et la couleur de survol
                    Float progress = hoverProgress.getOrDefault(currentPath, 0f);
                    
                    // Choisir la couleur de base selon le type de nœud
                    Color baseColor = isLeaf ? style.getChildNodeColor() : style.getParentNodeColor();
                    Color hoverColor = style.getHoverColor();
                    
                    // Interpoler avec la progression de l'animation de survol
                    bgColor = interpolateColor(baseColor, hoverColor, progress);
                }
            }
            
            // ===============================================================
            // ÉTAPE 2 : DESSINER LE FOND ARRONDI
            // ===============================================================
            
            if (bgColor != null) {
                g2.setColor(bgColor);
                
                // Créer un rectangle arrondi avec un peu de marge
                RoundRectangle2D roundRect = new RoundRectangle2D.Float(
                    4, 2,           // Position (x, y) avec marge
                    width - 8,      // Largeur moins marges gauche/droite
                    height - 4,     // Hauteur moins marges haut/bas
                    radius, radius  // Rayon des coins
                );
                
                // Remplir le rectangle arrondi
                g2.fill(roundRect);
            }
            
            // ===============================================================
            // ÉTAPE 3 : DESSINER LE CHEVRON ANIMÉ (pour les nœuds non-feuilles)
            // ===============================================================
            
            if (!isLeaf) {
                drawAnimatedChevron(g2, ht);
            }
            
            // Libérer le contexte graphique
            g2.dispose();
            
            // Appeler la méthode parente pour dessiner les enfants (icône et texte)
            super.paintComponent(g);
        }
        
        /**
         * Dessine le chevron animé pour indiquer l'état d'expansion
         */
        private void drawAnimatedChevron(Graphics2D g2, HTree ht) {
            // Taille du chevron
            int chevronSize = 8;
            
            // Position du chevron (gauche, centré verticalement)
            int x = 10;
            int y = getHeight() / 2;
            
            // Récupérer l'angle de rotation actuel (animé)
            // Par défaut : 0° pour fermé, 90° pour ouvert
            Float rotation = chevronRotation.getOrDefault(currentPath, isExpanded ? 90f : 0f);
            
            // ===============================================================
            // CRÉATION DE LA FORME DU CHEVRON (triangle)
            // ===============================================================
            
            // Path2D permet de créer des formes vectorielles
            Path2D chevron = new Path2D.Float();
            
            // Point de départ : haut gauche
            chevron.moveTo(-chevronSize/2, -chevronSize/2);
            
            // Point milieu : droite centre
            chevron.lineTo(chevronSize/2, 0);
            
            // Point final : bas gauche
            chevron.lineTo(-chevronSize/2, chevronSize/2);
            
            // La forme est maintenant un triangle pointant vers la droite
            
            // ===============================================================
            // APPLICATIONS DES TRANSFORMATIONS
            // ===============================================================
            
            // Sauvegarder la transformation actuelle
            AffineTransform oldTransform = g2.getTransform();
            
            // Déplacer le point d'origine à la position du chevron
            g2.translate(x, y);
            
            // Appliquer la rotation en degrés (convertis en radians)
            // Math.toRadians() convertit des degrés en radians
            g2.rotate(Math.toRadians(rotation));
            
            // ===============================================================
            // DESSIN DU CHEVRON
            // ===============================================================
            
            // Définir la couleur selon le style
            if (ht != null && ht.getTreeStyle() != null) {
                g2.setColor(ht.getTreeStyle().getTextColor());
            } else {
                g2.setColor(Color.BLACK);
            }
            
            // Définir un trait arrondi pour un look plus doux
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            // Dessiner le contour du chevron (pas de remplissage)
            g2.draw(chevron);
            
            // Restaurer la transformation originale
            g2.setTransform(oldTransform);
        }
    }
} 