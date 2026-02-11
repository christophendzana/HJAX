package rubban.layout;

import hcomponents.vues.HButtonStyle;
import rubban.HRibbonGroup;
import rubban.HRibbonModel;
import rubban.Ribbon;
import rubban.RibbonOverflowButton;
import rubban.GroupRenderer;
import java.awt.Component;
import javax.swing.JComponent;

/**
 * Renderer pour les groupes collapsed.
 * 
 * RESPONSABILITÉ UNIQUE :
 * -----------------------
 * Cette classe est l'ENDROIT UNIQUE où s'effectue le transfert de propriété
 * des composants du Ruban vers le RibbonOverflowButton.
 * 
 * PRINCIPE DE TRANSFERT EXCLUSIF :
 * --------------------------------
 * 1. Les composants sont RETIRÉS du Ruban (parent = null)
 * 2. Les composants sont AJOUTÉS au bouton (parent = null → parent = popup interne)
 * 3. Aucun composant n'est dupliqué
 * 4. Le Ruban perd totalement la possession des composants collapsed
 * 
 * @author FIDELE
 */
public class CollapsedGroupRenderer {

    private boolean debugMode = false; // Mettre à true pour tracer
    
    /**
     * Crée un RibbonOverflowButton représentant un groupe collapsed.
     * 
     * @param ribbon le ruban parent
     * @param group le groupe à représenter
     * @param groupIndex l'index du groupe
     * @return un bouton configuré contenant TOUS les composants du groupe
     * @throws IllegalArgumentException si ribbon ou group est null
     */
    public RibbonOverflowButton createCollapsedButton(
            Ribbon ribbon,
            HRibbonGroup group,
            int groupIndex) {

        // ============ VALIDATION ============
        if (ribbon == null) {
            throw new IllegalArgumentException("Ribbon cannot be null");
        }
        if (group == null) {
            throw new IllegalArgumentException("HRibbonGroup cannot be null");
        }

        // ============ TEXTE DU BOUTON ============
        // Priorité 1 : Header personnalisé
        // Priorité 2 : Identifiant du groupe
        // Priorité 3 : Fallback générique
        Object headerValue = group.getHeaderValue();
        if (headerValue == null) {
            headerValue = group.getGroupIdentifier();
        }
        if (headerValue == null) {
            headerValue = "Groupe " + groupIndex;
        }

        if (debugMode) {
            System.out.println("=== CRÉATION BOUTON COLLAPSED ===");
            System.out.println("Groupe " + groupIndex + " - Texte: " + headerValue);
        }

        // ============ CRÉATION DU BOUTON ============
        RibbonOverflowButton button = new RibbonOverflowButton(headerValue.toString());
        
        // Style discret pour un bouton de ruban
        button.setButtonStyle(HButtonStyle.FIELD);
        
        // ============ TRANSFERT EXCLUSIF DES COMPOSANTS ============
        // Cette section est CRITIQUE : c'est ici que s'opère le changement de propriétaire
        // ------------------------------------------------------------------------------
        HRibbonModel model = ribbon.getModel();
        GroupRenderer renderer = ribbon.getGroupRenderer();

        if (model != null) {
            int valueCount = model.getValueCount(groupIndex);
            
            if (debugMode) {
                System.out.println("Nombre de composants à transférer: " + valueCount);
            }

            for (int i = 0; i < valueCount; i++) {
                Object value = model.getValueAt(i, groupIndex);
                
                if (value == null) {
                    continue;
                }

                // ============ CAS 1 : VALEUR DÉJÀ COMPOSANT ============
                if (value instanceof JComponent) {
                    JComponent comp = (JComponent) value;
                    
                    // ÉTAPE 1 : RETIRER DU RUBAN (si présent)
                    // ----------------------------------------
                    if (comp.getParent() instanceof Ribbon) {
                        ribbon.removeComponentSafely(comp);
                        
                        if (debugMode) {
                            System.out.println("  → Retiré du Ribbon: " + 
                                             comp.getClass().getSimpleName());
                        }
                    }
                    
                    // ÉTAPE 2 : AJOUTER AU BOUTON
                    // ----------------------------
                    // Le bouton accepte les composants sans parent
                    button.addComponent(comp);
                    
                    if (debugMode) {
                        System.out.println("  → Ajouté au bouton: " + 
                                         comp.getClass().getSimpleName());
                    }
                
                // ============ CAS 2 : VALEUR À TRANSFORMER VIA RENDERER ============
                } else if (!(value instanceof Component) && renderer != null) {
                    try {
                        Component comp = renderer.getGroupComponent(
                                ribbon, 
                                value, 
                                groupIndex, 
                                i, 
                                false,      // isSelected (non utilisé)
                                false       // hasFocus (non utilisé)
                        );
                        
                        if (comp instanceof JComponent) {
                            // Important : ce composant est FRAIS, il n'a PAS de parent
                            // On l'ajoute DIRECTEMENT au bouton, JAMAIS au Ribbon
                            button.addComponent((JComponent) comp);
                            
                            if (debugMode) {
                                System.out.println("  → Créé et ajouté: " + 
                                                 comp.getClass().getSimpleName());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("ERREUR: Échec de création du composant pour " + value);
                        e.printStackTrace();
                    }
                }
            }
        }

        // ============ CONFIGURATION FINALE ============
        // Largeur fixe correspondant à l'espace réservé pour le groupe collapsed
        int collapsedWidth = group.getCollapsedWidth();
        if (collapsedWidth <= 0) {
            collapsedWidth = 30; // Valeur par défaut sécurisée
        }
        
        button.setPreferredSize(new java.awt.Dimension(
                collapsedWidth,
                button.getPreferredSize().height
        ));
        
        // S'assurer que le bouton n'est pas trop grand
        button.setMaximumSize(new java.awt.Dimension(
                collapsedWidth,
                Integer.MAX_VALUE
        ));

        if (debugMode) {
            System.out.println("Bouton configuré - Largeur: " + collapsedWidth + 
                             ", Hauteur pref: " + button.getPreferredSize().height);
            System.out.println("=== FIN CRÉATION BOUTON ===\n");
        }

        return button;
    }
    
    /**
     * Active/désactive les messages de debug.
     * 
     * @param enabled true pour activer les traces console
     */
    public void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }
}