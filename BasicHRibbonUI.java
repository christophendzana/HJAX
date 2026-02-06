package rubban;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;

/**
 * Version améliorée de BasicHRibbonUI avec :
 * - Dégradé de fond par défaut (#B8EAE4 -> #B8CBEA)
 * - Transition fluide lors du hover
 * - Effets visibles améliorés
 */
public class BasicHRibbonUI extends ComponentUI {

    // Couleurs configurables
    private Color defaultGroupStartColor = new Color(184, 234, 228); // #B8EAE4
    private Color defaultGroupEndColor = new Color(184, 203, 234);   // #B8CBEA
    private Color hoverBorderColor = new Color(0x3B6EF7);           // Bleu pour bordure hover
    private Color hoverTint = new Color(18, 94, 84, 50);           // Teinte bleue transparente (alpha=50)
    private Color focusBg = new Color(0xE3F2FD);                   // Bleu clair pour fond focus
    private Color focusRing = new Color(0x1976D2);                 // Bleu foncé pour bordure focus
    private Color ribbonBorder = new Color(0xD0D6DD);              // Bordure du ruban
    private int cornerRadius = 15;                                 // Rayon des coins arrondis

    // État du composant
    private JComponent ribbon;
    private int hoveredGroupIndex = -1;     // Index du groupe sous la souris
    private int focusedGroupIndex = -1;     // Index du groupe qui a le focus
    
    // Animation de transition
    private float hoverAnimationProgress = 0f; // 0 = pas de hover, 1 = hover complet
    private Timer hoverAnimationTimer;
    private static final int ANIMATION_DURATION = 150; // ms
    private static final float ANIMATION_STEP = 0.05f; // Incrément par frame

    // Listeners
    private MouseMotionListener mouseMotionListener;
    private MouseListener mouseListener;
    private PropertyChangeListener focusListener;

    /**
     * Constructeur par défaut
     */
    public BasicHRibbonUI() {
        initAnimationTimer();
    }

    /**
     * Initialise le timer pour l'animation de transition
     */
    private void initAnimationTimer() {
        hoverAnimationTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Déterminer la cible
                float target = (hoveredGroupIndex != -1) ? 1.0f : 0.0f;
                
                // Animer progressivement
                if (hoverAnimationProgress < target) {
                    hoverAnimationProgress = Math.min(target, hoverAnimationProgress + ANIMATION_STEP);
                    if (ribbon != null) ribbon.repaint();
                } else if (hoverAnimationProgress > target) {
                    hoverAnimationProgress = Math.max(target, hoverAnimationProgress - ANIMATION_STEP);
                    if (ribbon != null) ribbon.repaint();
                }
                
                // Arrêter le timer si l'animation est terminée
                if (Math.abs(hoverAnimationProgress - target) < 0.001f) {
                    hoverAnimationTimer.stop();
                }
            }
        });
    }

    /**
     * Méthode statique requise par Swing pour créer l'UI
     */
    public static ComponentUI createUI(JComponent c) {
        return new BasicHRibbonUI();
    }

    @Override
    public void installUI(JComponent c) {
        this.ribbon = c;
        
        // Listener pour le survol de la souris
        mouseMotionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newHoveredIndex = getGroupIndexAtPoint(e.getPoint());
                if (newHoveredIndex != hoveredGroupIndex) {
                    hoveredGroupIndex = newHoveredIndex;
                    // Démarrer l'animation
                    if (!hoverAnimationTimer.isRunning()) {
                        hoverAnimationTimer.start();
                    }
                }
            }
        };

        // Listener pour la sortie de la souris
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredGroupIndex != -1) {
                    hoveredGroupIndex = -1;
                    // Démarrer l'animation de retour
                    if (!hoverAnimationTimer.isRunning()) {
                        hoverAnimationTimer.start();
                    }
                }
            }
        };

        // Listener pour le focus
        focusListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!"focusOwner".equals(evt.getPropertyName())) {
                    return;
                }
                
                Object focusOwner = evt.getNewValue();
                int newFocusedIndex = -1;
                
                if (focusOwner instanceof Component) {
                    Component focused = (Component) focusOwner;
                    newFocusedIndex = getGroupIndexForComponent(focused);
                }
                
                if (newFocusedIndex != focusedGroupIndex) {
                    focusedGroupIndex = newFocusedIndex;
                    ribbon.repaint();
                }
            }
        };

        // Enregistrer les listeners
        ribbon.addMouseMotionListener(mouseMotionListener);
        ribbon.addMouseListener(mouseListener);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("focusOwner", focusListener);

        // Rendre le composant transparent
        ribbon.setOpaque(false);
        ribbon.repaint();
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (hoverAnimationTimer != null) {
            hoverAnimationTimer.stop();
        }
        
        if (mouseMotionListener != null) {
            ribbon.removeMouseMotionListener(mouseMotionListener);
        }
        if (mouseListener != null) {
            ribbon.removeMouseListener(mouseListener);
        }
        if (focusListener != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .removePropertyChangeListener("focusOwner", focusListener);
        }

        mouseMotionListener = null;
        mouseListener = null;
        focusListener = null;
        ribbon = null;
        hoveredGroupIndex = -1;
        focusedGroupIndex = -1;
        hoverAnimationProgress = 0f;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle[] groupBounds = getGroupBounds();
            if (groupBounds == null) {
                return;
            }

            HRibbonLayoutManager lm = getLayoutManager();
            HRibbonGroupModel groupModel = (lm != null) ? lm.getRibbon().getGroupModel() : null;

            // Dessiner chaque groupe
            for (int i = 0; i < groupBounds.length; i++) {
                Rectangle bounds = groupBounds[i];
                if (bounds == null) {
                    continue;
                }

                boolean isHovered = (i == hoveredGroupIndex);
                boolean isFocused = (i == focusedGroupIndex);

                // Créer la forme arrondie
                RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                    bounds.x, bounds.y,
                    Math.max(0, bounds.width), Math.max(0, bounds.height),
                    cornerRadius, cornerRadius
                );

                // 1. FOND DU GROUPE AVEC DÉGRADÉ
                if (isFocused) {
                    // Focus : fond bleu clair solide
                    g2.setColor(focusBg);
                    g2.fill(roundedRect);
                } else {
                    // Normal : dégradé personnalisé ou par défaut
                    if (groupModel != null) {
                        HRibbonGroup group = groupModel.getHRibbonGroup(i);
                        if (group != null && group.getBackground() != null) {
                            // Utiliser la couleur personnalisée si définie
                            g2.setColor(group.getBackground());
                            g2.fill(roundedRect);
                        } else {
                            // Dégradé par défaut
                            GradientPaint gradient = new GradientPaint(
                                bounds.x, bounds.y, defaultGroupStartColor,
                                bounds.x + bounds.width, bounds.y + bounds.height, defaultGroupEndColor
                            );
                            g2.setPaint(gradient);
                            g2.fill(roundedRect);
                        }
                    } else {
                        // Dégradé par défaut
                        GradientPaint gradient = new GradientPaint(
                            bounds.x, bounds.y, defaultGroupStartColor,
                            bounds.x + bounds.width, bounds.y + bounds.height, defaultGroupEndColor
                        );
                        g2.setPaint(gradient);
                        g2.fill(roundedRect);
                    }
                }

                // 2. TEINTE HOVER ANIMÉE (par-dessus le fond si hover actif)
                if (isHovered && !isFocused && hoverAnimationProgress > 0) {
                    // Calculer la couleur d'overlay avec alpha animé
                    int alpha = (int)(hoverTint.getAlpha() * hoverAnimationProgress);
                    Color animatedHoverTint = new Color(
                        hoverTint.getRed(),
                        hoverTint.getGreen(),
                        hoverTint.getBlue(),
                        alpha
                    );
                    g2.setColor(animatedHoverTint);
                    g2.fill(roundedRect);
                }

                // 3. BORDURE FOCUS (prioritaire)
                if (isFocused) {
                    g2.setColor(focusRing);
                    g2.setStroke(new BasicStroke(3f)); // Bordure épaisse
                    RoundRectangle2D focusOutline = new RoundRectangle2D.Float(
                        bounds.x - 2, bounds.y - 2,
                        Math.max(0, bounds.width + 4), Math.max(0, bounds.height + 4),
                        cornerRadius + 2, cornerRadius + 2
                    );
                    g2.draw(focusOutline);
                }
                // 4. BORDURE HOVER ANIMÉE (si pas de focus)
                else if (isHovered && hoverAnimationProgress > 0) {
                    // Calculer l'épaisseur animée
                    float strokeWidth = 1f + (1f * hoverAnimationProgress);
                    g2.setColor(hoverBorderColor);
                    g2.setStroke(new BasicStroke(strokeWidth));
                    
                    // Calculer la position animée pour un effet de zoom subtil
                    float inset = (1 - hoverAnimationProgress) * 0.5f;
                    g2.drawRoundRect(
                        bounds.x + (int)inset, 
                        bounds.y + (int)inset,
                        Math.max(0, bounds.width - 1 - (int)(inset * 2)), 
                        Math.max(0, bounds.height - 1 - (int)(inset * 2)),
                        cornerRadius, 
                        cornerRadius
                    );
                }
                // 5. BORDURE NORMALE (si ni hover ni focus)
                else {
                    g2.setColor(new Color(0, 0, 0, 15)); // Bordure subtile
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(
                        bounds.x, bounds.y,
                        Math.max(0, bounds.width - 1), Math.max(0, bounds.height - 1),
                        cornerRadius, cornerRadius
                    );
                }
            }

            // Bordure inférieure du ruban
            paintRibbonBorder(g2, c);

        } finally {
            g2.dispose();
        }
    }

    /**
     * Dessine la bordure inférieure du ruban
     */
    private void paintRibbonBorder(Graphics2D g2, JComponent c) {
        int width = c.getWidth();
        int height = c.getHeight();
        g2.setColor(ribbonBorder);
        g2.drawLine(0, height - 1, width, height - 1);
    }

    /**
     * Récupère le LayoutManager du ruban
     */
    private HRibbonLayoutManager getLayoutManager() {
        if (ribbon == null) {
            return null;
        }
        LayoutManager lm = ribbon.getLayout();
        if (lm instanceof HRibbonLayoutManager) {
            return (HRibbonLayoutManager) lm;
        }
        return null;
    }

    /**
     * Récupère les limites de tous les groupes
     */
    private Rectangle[] getGroupBounds() {
        HRibbonLayoutManager lm = getLayoutManager();
        if (lm == null) {
            return null;
        }
        return lm.getGroupBounds();
    }

    /**
     * Trouve l'index du groupe contenant le point donné
     */
    private int getGroupIndexAtPoint(Point point) {
        Rectangle[] bounds = getGroupBounds();
        if (bounds == null) {
            return -1;
        }

        for (int i = 0; i < bounds.length; i++) {
            Rectangle rect = bounds[i];
            if (rect != null && rect.contains(point)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Trouve l'index du groupe contenant le composant donné
     */
    private int getGroupIndexForComponent(Component component) {
        HRibbonLayoutManager lm = getLayoutManager();
        if (lm == null) {
            return -1;
        }

        // Vérifier via la map des composants par groupe
        java.util.Map<Integer, java.util.List<Component>> componentsByGroup = 
            lm.getComponentsByGroup();
        
        if (componentsByGroup != null) {
            for (java.util.Map.Entry<Integer, java.util.List<Component>> entry : 
                 componentsByGroup.entrySet()) {
                
                java.util.List<Component> components = entry.getValue();
                if (components != null && components.contains(component)) {
                    return entry.getKey();
                }
            }
        }

        // Fallback : intersection avec les limites
        Rectangle[] bounds = getGroupBounds();
        if (bounds != null) {
            for (int i = 0; i < bounds.length; i++) {
                Rectangle groupRect = bounds[i];
                if (groupRect != null) {
                    Point componentLocation = SwingUtilities.convertPoint(
                        component, 0, 0, ribbon
                    );
                    Rectangle componentRect = new Rectangle(
                        componentLocation.x, componentLocation.y,
                        component.getWidth(), component.getHeight()
                    );
                    
                    if (groupRect.intersects(componentRect)) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    // =========================================================================
    // MÉTHODES PUBLIQUES POUR PERSONNALISER L'APPARENCE
    // =========================================================================

    /**
     * Définit les couleurs de départ et de fin du dégradé par défaut
     */
    public void setDefaultGroupGradient(Color startColor, Color endColor) {
        this.defaultGroupStartColor = startColor;
        this.defaultGroupEndColor = endColor;
        if (ribbon != null) {
            ribbon.repaint();
        }
    }

    /**
     * Définit la couleur de la bordure lors du survol
     */
    public void setHoverBorderColor(Color color) {
        this.hoverBorderColor = color;
        if (ribbon != null) {
            ribbon.repaint();
        }
    }

    /**
     * Définit la teinte appliquée lors du survol (couleur transparente)
     */
    public void setHoverTint(Color color) {
        this.hoverTint = color;
        if (ribbon != null) {
            ribbon.repaint();
        }
    }

    /**
     * Définit la couleur de fond lorsqu'un groupe a le focus
     */
    public void setFocusBackground(Color color) {
        this.focusBg = color;
        if (ribbon != null) {
            ribbon.repaint();
        }
    }

    /**
     * Définit la couleur de la bordure de focus
     */
    public void setFocusRing(Color color) {
        this.focusRing = color;
        if (ribbon != null) {
            ribbon.repaint();
        }
    }

    /**
     * Définit le rayon des coins arrondis
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = Math.max(0, radius);
        if (ribbon != null) {
            ribbon.repaint();
        }
    }

    /**
     * Récupère le rayon des coins arrondis
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit la durée de l'animation de transition (en ms)
     */
    public void setAnimationDuration(int durationMs) {
        hoverAnimationTimer.setDelay(durationMs / 20); // Ajuster le delay selon la durée
    }

    /**
     * Récupère la progression actuelle de l'animation de hover
     */
    public float getHoverAnimationProgress() {
        return hoverAnimationProgress;
    }
}