package hcomponents.HRibbon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicPanelUI;

/**
 * Implémentation basique de l'interface utilisateur pour HRibbon.
 */
public class BasicHRibbonUI extends BasicPanelUI {
    
    private static final BasicHRibbonUI instance = new BasicHRibbonUI();
    
    // Couleurs par défaut (peuvent être surchargées par le L&F)
    protected Color ribbonBackground;
    protected Color groupDividerColor;
    protected Color groupBackground;
    
    /**
     * Retourne une instance partagée (singleton pattern).
     */
    public static BasicHRibbonUI createUI(JComponent c) {
        return instance;
    }
    
    public BasicHRibbonUI() {
        // Initialise les couleurs avec des valeurs par défaut
        ribbonBackground = UIManager.getColor("Panel.background");
        if (ribbonBackground == null) {
            ribbonBackground = new Color(250, 20, 4);
        }
        
        groupDividerColor = UIManager.getColor("Separator.foreground");
        if (groupDividerColor == null) {
            groupDividerColor = new Color(200, 200, 200);
        }
        
        groupBackground = UIManager.getColor("Panel.background");
    }
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        HRibbon ribbon = (HRibbon) c;
        
        // Configure les propriétés par défaut du ruban
        ribbon.setOpaque(true);
        ribbon.setBackground(ribbonBackground);
        ribbon.setFocusable(true);
        
        // Installe les listeners spécifiques (à compléter selon besoin)
        installListeners(ribbon);
    }
    
    @Override
    public void uninstallUI(JComponent c) {
        HRibbon ribbon = (HRibbon) c;
        uninstallListeners(ribbon);
        super.uninstallUI(c);
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        HRibbon ribbon = (HRibbon) c;
        
        // 1. Peint l'arrière-plan
        paintBackground(g, ribbon);
        
        // 2. Récupère les positions des groupes (délégué au LayoutManager)
        Rectangle[] groupBounds = getGroupBounds(ribbon);
        
        // 3. Peint les séparateurs entre groupes
        if (groupBounds != null) {
            paintGroupDividers(g, ribbon, groupBounds);
        }
        
        // 4. La peinture des composants est gérée par Swing
        super.paint(g, c); // IMPORTANT: Appel parent pour peindre les enfants
    }
    
    public void paintBackground(Graphics g, HRibbon ribbon) {
        if (ribbon.isOpaque()) {
            g.setColor(ribbon.getBackground());
            g.fillRect(0, 0, ribbon.getWidth(), ribbon.getHeight());
        }
    }
    
    public void paintGroupDividers(Graphics g, HRibbon ribbon, Rectangle[] groupBounds) {
        g.setColor(groupDividerColor);
        
        // Peint des séparateurs entre les groupes (sauf après le dernier)
        for (int i = 0; i < groupBounds.length - 1; i++) {
            Rectangle bounds = groupBounds[i];
            int x = bounds.x + bounds.width;
            g.drawLine(x, bounds.y + 2, x, bounds.y + bounds.height - 4);
        }
    }
    
    public Insets getPreferredMargins(HRibbon ribbon) {
        // Marges internes du ruban
        return new Insets(4, 4, 4, 4);
    }
    
    public int getPreferredHeight(HRibbon ribbon) {
        // Logique de calcul de hauteur
        int maxComponentHeight = 0;
        
        if (ribbon.getGroupModel() != null) {
            for (int i = 0; i < ribbon.getGroupModel().getGroupCount(); i++) {
                HRibbonGroup group = ribbon.getGroupModel().getHRibbonGroup(i);
                // Calcule la hauteur maximale des composants dans ce groupe
                maxComponentHeight = Math.max(maxComponentHeight, calculateGroupHeight(group));
            }
        }
        
        Insets margins = getPreferredMargins(ribbon);
        return maxComponentHeight + margins.top + margins.bottom;
    }
    
    /**
     * Calcule la hauteur d'un groupe basée sur ses composants.
     */
    private int calculateGroupHeight(HRibbonGroup group) {
        // Pour l'instant, hauteur fixe
        // À améliorer : calculer basé sur les composants réels
        return 80; // Hauteur par défaut pour un groupe
    }
    
    /**
     * Calcule les limites de chaque groupe pour le peinture.
     */
    protected Rectangle[] getGroupBounds(HRibbon ribbon) {
        if (ribbon.getGroupModel() == null) {
            return null;
        }
        
        int groupCount = ribbon.getGroupModel().getGroupCount();
        Rectangle[] bounds = new Rectangle[groupCount];
        
        // Cette logique devrait idéalement venir du LayoutManager
        int x = 0;
        int margin = ribbon.getGroupModel().getHRibbonGroupMarggin();
        
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = ribbon.getGroupModel().getHRibbonGroup(i);
            if (group != null) {
                bounds[i] = new Rectangle(x, 0, group.getWidth(), ribbon.getHeight());
                x += group.getWidth() + margin;
            }
        }
        
        return bounds;
    }
    
    /**
     * Installe les listeners spécifiques au ruban.
     */
    protected void installListeners(HRibbon ribbon) {
        // À compléter avec les listeners spécifiques
        // (gestion souris, focus, etc.)
    }
    
    /**
     * Désinstalle les listeners.
     */
    protected void uninstallListeners(HRibbon ribbon) {
        // À compléter
    }
}