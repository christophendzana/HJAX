/*
 * CollapsedGroupRenderer.java
 * 
 * Renderer pour les groupes collapsed.
 * 
 * NOUVELLE VERSION - SIMPLIFIÉE RADICALEMENT
 * 
 * RESPONSABILITÉ UNIQUE :
 * Créer un RibbonOverflowButton vide, configuré, et le remplir
 * UNE SEULE FOIS (au moment du collapse) avec des PROXIES créés
 * via OverflowProxyFactory.
 * 
 * PLUS AUCUN TRANSFERT DE COMPOSANTS ORIGINAUX.
 * PLUS AUCUN PROBLÈME DE PARENT SWING.
 * 
 * @author FIDELE
 * @version 2.0
 */
package rubban.layout;

import hcomponents.ArrowIcon;
import hcomponents.vues.HButtonStyle;
import rubban.*;
import javax.swing.*;
import java.awt.*;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.NORTH;

public class CollapsedGroupRenderer {

    /**
     * Crée un RibbonOverflowButton représentant un groupe collapsed.
     * 
     * @param ribbon le ruban parent
     * @param group le groupe à représenter
     * @param groupIndex l'index du groupe
     * @param icon
     * @return un bouton configuré, contenant déjà les proxies des composants
     */
    public RibbonOverflowButton createCollapsedButton(
            Ribbon ribbon,
            HRibbonGroup group,
            int groupIndex,
            Icon icon) {

        if (ribbon == null || group == null) {
            throw new IllegalArgumentException("Ribbon and group cannot be null");
        }

        // ============================================================
        // 1. TEXTE DU BOUTON (header du groupe)
        // ============================================================
        Object headerValue = group.getHeaderValue();
        if (headerValue == null) {
            headerValue = group.getGroupIdentifier();
        }
        if (headerValue == null) {
            headerValue = "Groupe " + groupIndex;
        }

        // ============================================================
        // 2. CRÉATION DU BOUTON VIDE
        // ============================================================
        RibbonOverflowButton button = new RibbonOverflowButton(headerValue.toString() );
        button.setButtonStyle(HButtonStyle.FIELD);
        button.setPreferredSize(new Dimension(
            group.getCollapsedWidth(),
            button.getPreferredSize().height +50
        ));
        button.setVerticalAlignment(EAST);
        if (icon == null) {
            button.setIcon(new ArrowIcon(Color.yellow, ArrowIcon.Direction.DOWN, 0.4f, 5));            
        }else{
            button.setIcon(icon);
        }
        // Lier le bouton à son groupe (indispensable pour le rebuild)
        button.setGroupIndex(groupIndex);

        // ============================================================
        // 3. RÉCUPÉRATION DE LA FACTORY DE PROXIES
        // ============================================================
        OverflowProxyFactory proxyFactory = ribbon.getOverflowProxyFactory();
        if (proxyFactory == null) {
            // Normalement impossible (Ribbon garantit une factory non null)
            return button;
        }

        // ============================================================
        // 4. CRÉATION DES PROXIES ET REMPLISSAGE DU BOUTON
        // ============================================================
        HRibbonModel model = ribbon.getModel();
        GroupRenderer groupRenderer = ribbon.getGroupRenderer();

        if (model != null) {
            int valueCount = model.getValueCount(groupIndex);

            for (int i = 0; i < valueCount; i++) {
                Object value = model.getValueAt(i, groupIndex);
                JComponent proxy = null;

                // --------------------------------------------------------
                // CAS 1 : La valeur est DÉJÀ un composant Swing
                // --------------------------------------------------------
                if (value instanceof JComponent) {
                    JComponent original = (JComponent) value;
                    proxy = proxyFactory.createProxy(original);
                }
                // --------------------------------------------------------
                // CAS 2 : La valeur est un OBJET (à rendre via GroupRenderer)
                // --------------------------------------------------------
                else if (value != null && groupRenderer != null) {
                    try {
                        Component original = groupRenderer.getGroupComponent(
                            ribbon, value, groupIndex, i, false, false
                        );
                        if (original instanceof JComponent) {
                            proxy = proxyFactory.createProxy((JComponent) original);
                        }
                    } catch (Exception e) {
                        // Échec du rendu → ignorer silencieusement
                        // Le composant n'apparaîtra pas dans le popup
                    }
                }

                // --------------------------------------------------------
                // Ajout du proxy au bouton (s'il a été créé)
                // --------------------------------------------------------
                if (proxy != null) {
                    button.addComponent(proxy);
                }
            }
        }

        return button;
    }
    
    public RibbonOverflowButton createCollapsedButton(
            Ribbon ribbon,
            HRibbonGroup group,
            int groupIndex) {
                
            return  createCollapsedButton(ribbon, group, groupIndex, null);
        
            }
    
}