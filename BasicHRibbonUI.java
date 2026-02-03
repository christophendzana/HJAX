package rubban;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicPanelUI;

/**
 * Implémentation basique de l'interface utilisateur pour Ribbon.
 * Fournit le rendu visuel de base avec séparateurs de groupes.
 */
public class BasicHRibbonUI extends BasicPanelUI {
    
    private static final BasicHRibbonUI instance = new BasicHRibbonUI();
    
    // Référence vers le ruban actuel
    protected Ribbon ribbon;
    
    // Couleurs dérivées du Look and Feel
    protected Color ribbonBackground;
    protected Color groupDividerColor;
    protected Color groupBorderColor;
    protected Color groupBackground;
    
    /**
     * Retourne une instance partagée (singleton pattern).
     * 
     * @param c le composant Ribbon
     * @return l'instance BasicRibbonUI
     */
    public static BasicHRibbonUI createUI(JComponent c) {
        return instance;
    }
    
    /**
     * Constructeur. Initialise les couleurs depuis le Look and Feel.
     */
    public BasicHRibbonUI() {
        initializeColors();
    }
    
    /**
     * Initialise les couleurs depuis le Look and Feel.
     * Utilise des couleurs système pour une meilleure intégration.
     */
    protected void initializeColors() {
        // Fond du ruban
        ribbonBackground = UIManager.getColor("Panel.background");
        if (ribbonBackground == null) {
            ribbonBackground = UIManager.getColor("control");
        }
        if (ribbonBackground == null) {
            ribbonBackground = new Color(240, 240, 240); // Gris clair par défaut
        }
        
        // Couleur des séparateurs entre groupes
        groupDividerColor = UIManager.getColor("Separator.foreground");
        if (groupDividerColor == null) {
            groupDividerColor = UIManager.getColor("controlShadow");
        }
        if (groupDividerColor == null) {
            groupDividerColor = new Color(200, 200, 200); // Gris moyen
        }
        
        // Couleur des bordures de groupe
        groupBorderColor = UIManager.getColor("InternalFrame.borderShadow");
        if (groupBorderColor == null) {
            groupBorderColor = groupDividerColor;
        }
        
        // Fond des groupes
        groupBackground = UIManager.getColor("Panel.background");
        if (groupBackground == null) {
            groupBackground = Color.WHITE;
        }
    }
    
    /**
     * Installe l'UI sur le composant.
     * 
     * @param c le composant Ribbon
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        if (!(c instanceof Ribbon)) {
            throw new IllegalArgumentException("BasicRibbonUI can only be installed on Ribbon");
        }
        
        ribbon = (Ribbon) c;
        
        // Configure les propriétés par défaut du ruban
        ribbon.setOpaque(true);
        ribbon.setBackground(ribbonBackground);
        ribbon.setFocusable(true);
        
        // Installe les listeners spécifiques
        installListeners(ribbon);
    }
    
    /**
     * Désinstalle l'UI du composant.
     * 
     * @param c le composant Ribbon
     */
    @Override
    public void uninstallUI(JComponent c) {
        if (ribbon != null) {
            uninstallListeners(ribbon);
            ribbon = null;
        }
        super.uninstallUI(c);
    }
    
    /**
     * Peint le composant Ribbon.
     * 
     * @param g le contexte graphique
     * @param c le composant Ribbon
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        if (!(c instanceof Ribbon)) {
            super.paint(g, c);
            return;
        }
        
        Ribbon hRibbon = (Ribbon) c;
        
        // 1. Peint l'arrière-plan
        paintBackground(g, hRibbon);
        
        // 2. Peint les groupes et leurs bordures
        paintGroups(g, hRibbon);
        
        // 3. La peinture des composants enfants est gérée par Swing
        super.paint(g, c);
    }
    
    /**
     * Peint l'arrière-plan du ruban.
     * 
     * @param g le contexte graphique
     * @param ribbon le ruban à peindre
     */
    public void paintBackground(Graphics g, Ribbon ribbon) {
        if (ribbon.isOpaque()) {
            g.setColor(ribbon.getBackground());
            g.fillRect(0, 0, ribbon.getWidth(), ribbon.getHeight());
        }
    }
    
    /**
     * Peint les groupes et leurs bordures.
     * 
     * @param g le contexte graphique
     * @param ribbon le ruban à peindre
     */
    public void paintGroups(Graphics g, Ribbon ribbon) {
        HRibbonLayoutManager layout = ribbon.getRubanLayout();
        if (layout == null) {
            return;
        }
        
        // Récupère les limites des groupes depuis le LayoutManager
        Rectangle[] groupBounds = layout.getGroupBounds();
        if (groupBounds == null || groupBounds.length == 0) {
            return;
        }
        
        // Peint les séparateurs entre groupes
        paintGroupDividers(g, ribbon, groupBounds);
        
        // Optionnel: peint les fonds des groupes
        paintGroupBackgrounds(g, ribbon, groupBounds);
    }
    
    /**
     * Peint les séparateurs entre les groupes.
     * 
     * @param g le contexte graphique
     * @param ribbon le ruban
     * @param groupBounds les limites de chaque groupe
     */
    public void paintGroupDividers(Graphics g, Ribbon ribbon, Rectangle[] groupBounds) {
        g.setColor(groupDividerColor);
        
        // Peint des séparateurs verticaux entre les groupes
        for (int i = 0; i < groupBounds.length - 1; i++) {
            Rectangle bounds = groupBounds[i];
            if (bounds != null) {
                int x = bounds.x + bounds.width;
                int top = bounds.y + 4;          // Marge en haut
                int bottom = bounds.y + bounds.height - 4; // Marge en bas
                
                // Ligne verticale fine
                g.drawLine(x, top, x, bottom);
                
                // Optionnel: ajoute un petit effet d'ombre
                g.setColor(new Color(255, 255, 255, 100));
                g.drawLine(x + 1, top, x + 1, bottom);
                g.setColor(groupDividerColor);
            }
        }
    }
    
    /**
     * Peint les fonds des groupes (optionnel).
     * 
     * @param g le contexte graphique
     * @param ribbon le ruban
     * @param groupBounds les limites de chaque groupe
     */
    public void paintGroupBackgrounds(Graphics g, Ribbon ribbon, Rectangle[] groupBounds) {
        g.setColor(groupBackground);
        
        for (Rectangle bounds : groupBounds) {
            if (bounds != null) {
                // Peint un fond légèrement différent pour chaque groupe
                g.fillRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
                
                // Bordures légères autour du groupe
                g.setColor(groupBorderColor);
                g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
                g.setColor(groupBackground);
            }
        }
    }
    
    /**
     * Retourne les marges préférées autour du ruban.
     * 
     * @param ribbon le ruban
     * @return les marges (insets)
     */
    public Insets getPreferredMargins(Ribbon ribbon) {
        // Marges internes standard
        return new Insets(4, 8, 4, 8);
    }
    
    /**
     * Retourne la hauteur préférée du ruban.
     * Calcule dynamiquement basé sur le contenu.
     * 
     * @param ribbon le ruban
     * @return la hauteur préférée en pixels
     */
    public int getPreferredHeight(Ribbon ribbon) {
        if (ribbon == null || ribbon.getGroupModel() == null) {
            return 80; // Hauteur par défaut
        }
        
        int maxComponentHeight = 0;
        HRibbonModel model = ribbon.getModel();
        
        if (model != null) {
            // Parcourt tous les composants pour trouver la hauteur maximale
            for (int i = 0; i < model.getGroupCount(); i++) {
                int valueCount = model.getValueCount(i);
                for (int j = 0; j < valueCount; j++) {
                    Object value = model.getValueAt(j, i);
                    if (value instanceof java.awt.Component) {
                        java.awt.Component comp = (java.awt.Component) value;
                        maxComponentHeight = Math.max(maxComponentHeight, comp.getPreferredSize().height);
                    }
                }
            }
        }
        
        // Ajoute le padding des groupes
        if (ribbon.getGroupModel().getGroupCount() > 0) {
            HRibbonGroup firstGroup = ribbon.getGroupModel().getHRibbonGroup(0);
            if (firstGroup != null) {
                maxComponentHeight += firstGroup.getPadding() * 2;
            }
        }
        
        // Ajoute les marges du ruban
        Insets margins = getPreferredMargins(ribbon);
        int totalHeight = maxComponentHeight + margins.top + margins.bottom;
        
        // Hauteur minimale garantie
        return Math.max(totalHeight, 60);
    }
    
    /**
     * Installe les listeners spécifiques au ruban.
     * 
     * @param ribbon le ruban
     */
    protected void installListeners(Ribbon ribbon) {
        // À compléter selon les besoins :
        // - Listeners de souris pour la sélection
        // - Listeners de focus
        // - Listeners de propriétés
    }
    
    /**
     * Désinstalle les listeners.
     * 
     * @param ribbon le ruban
     */
    protected void uninstallListeners(Ribbon ribbon) {
        // À compléter pour nettoyer les listeners installés
    }
    
    /**
     * Met à jour les couleurs depuis le Look and Feel.
     * Utile quand le Look and Feel change dynamiquement.
     */
    public void updateColors() {
        initializeColors();
        if (ribbon != null) {
            ribbon.setBackground(ribbonBackground);
            ribbon.repaint();
        }
    }
}