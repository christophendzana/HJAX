/*
 * GroupBoundsCalculator.java
 * 
 * CALCULATEUR DE LIMITES DE GROUPES POUR LE RUBAN HRibbon
 * 
 * RÔLE PRINCIPAL :
 * Calcule les rectangles (bounds) de la zone "contenu" pour chaque groupe du ruban,
 * en tenant compte de la position des en-têtes (headers) et des marges.
 * 
 * CONCEPT CLÉ : COORDONNÉES RELATIVES
 * - Transforme des largeurs théoriques en rectangles positionnés
 * - Gère l'exclusion des zones d'en-tête selon leur position
 * - Calcule les positions X successives des groupes
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban.layout;

import java.awt.Insets;
import java.awt.Rectangle;
import rubban.HRibbonGroup;
import rubban.HRibbonGroupModel;
import rubban.Ribbon;

/**
 * GroupBoundsCalculator - Calculateur de limites géométriques des groupes
 * 
 * RESPONSABILITÉS PRÉCISES :
 * 1. Transforme les largeurs allouées en rectangles positionnés dans l'espace du ruban
 * 2. Exclut la zone réservée aux en-têtes selon leur alignement
 * 3. Calcule les positions successives des groupes avec marges
 * 4. Fournit la zone de contenu disponible pour les composants de chaque groupe
 * 
 * CONCEPTS IMPORTANTS :
 * - Largeur totale : Largeur allouée au groupe (inclut potentiellement l'en-tête)
 * - Zone de contenu : Espace disponible pour les composants (exclut l'en-tête)
 * - Header area : Zone réservée à l'affichage de l'en-tête
 * 
 * SCÉNARIOS D'ALIGNEMENT DES EN-TÊTES :
 * 1. HEADER_NORTH  : En-tête au-dessus du contenu → Réserve de l'espace en haut
 * 2. HEADER_SOUTH  : En-tête en dessous du contenu → Pas d'impact sur Y
 * 3. HEADER_WEST   : En-tête à gauche du contenu → Réserve de l'espace à gauche
 * 4. HEADER_EAST   : En-tête à droite du contenu → Réserve de l'espace à droite
 * 
 * @see HRibbonLayoutContext
 * @see HRibbonGroupModel
 */
public class GroupBoundsCalculator {
    
    // =========================================================================
    // MÉTHODE PRINCIPALE DE CALCUL
    // =========================================================================
    
    /**
     * Calcule les limites (bounds) de la zone de contenu pour chaque groupe
     * 
     * ALGORITHME PRINCIPAL :
     * 1. Pour chaque groupe, déterminer la largeur totale allouée
     * 2. Soustraire la zone réservée à l'en-tête selon sa position
     * 3. Calculer la position X en accumulant les largeurs + marges
     * 4. Calculer la position Y selon la position de l'en-tête
     * 5. Retourner un rectangle pour chaque groupe
     * 
     * PARAMÈTRES D'ENTRÉE :
     * @param ctx Contexte de layout contenant la configuration d'affichage
     *            (alignement des en-têtes, largeur d'en-tête, marges, etc.)
     * @param groupWidths Tableau des largeurs totales allouées à chaque groupe
     *                    Ces largeurs peuvent inclure l'espace pour l'en-tête
     *                    selon son alignement (WEST/EAST)
     * @param groupModel Modèle des groupes fournissant le nombre de groupes
     *                   et la marge entre les groupes
     * @param insets Marges internes du conteneur parent (gauche, haut, droite, bas)
     *               Utilisées pour décaler le contenu depuis les bords
     * @param contentHeight Hauteur disponible pour le contenu des groupes
     *                      Cette valeur tient déjà compte des en-têtes NORD/SUD
     * @param headerHeight Hauteur des en-têtes (utilisée pour HEADER_NORTH)
     * 
     * @return Tableau de Rectangle de taille groupModel.getGroupCount()
     *         Chaque rectangle représente la zone de contenu (x, y, width, height)
     *         pour le groupe correspondant.
     *         Retourne un tableau vide si les paramètres sont invalides.
     * 
     * EXCEPTIONS :
     * - Aucune exception levée, retourne des tableaux vides pour les cas invalides
     * - Les largeurs négatives sont corrigées à 0
     * 
     * @see Rectangle
     * @see Insets
     */
    public Rectangle[] calculateGroupBounds(HRibbonLayoutContext ctx,
                                            int[] groupWidths,
                                            HRibbonGroupModel groupModel,
                                            Insets insets,
                                            int contentHeight,
                                            int headerHeight) {
        // VÉRIFICATION DES PARAMÈTRES D'ENTRÉE
        if (groupWidths == null) {
            return new Rectangle[0]; // Cas limite : pas de largeurs définies
        }
        
        // INITIALISATION DES RÉSULTATS
        int groupCount = groupWidths.length;
        Rectangle[] rects = new Rectangle[groupCount];
        
        // EXTRACTION DES PARAMÈTRES DE POSITIONNEMENT
        // Position de départ X (décalage depuis la bordure gauche)
        int left = (insets != null) ? insets.left : 0;
        
        // Position de départ Y (décalage depuis la bordure haute)
        int topInset = (insets != null) ? insets.top : 0;
        
        // EXTRACTION DES PARAMÈTRES DE CONFIGURATION
        // Valeurs par défaut si le contexte est null
        int headerAlignment = (ctx != null) ? ctx.getHeaderAlignment() : Ribbon.HEADER_NORTH;
        int headerMargin = (ctx != null) ? ctx.getHeaderMargin() : 0;
        int headerWidth = (ctx != null) ? ctx.getHeaderWidth() : 0;
        int groupMargin = (groupModel != null) ? groupModel.getHRibbonGroupMarggin() : 0;
        
        // POSITION X COURANTE (ACCUMULATEUR)
        int x = left;
        
        // BOUCLE PRINCIPALE SUR TOUS LES GROUPES
        for (int i = 0; i < groupCount; i++) {
            // ÉTAPE 1 : LARGEUR TOTALE DU GROUPE
            // Garantir une largeur non négative
            int totalW = Math.max(0, groupWidths[i]);
            
            // ÉTAPE 2 : CALCUL DE LA ZONE DE CONTENU (EXCLUT L'EN-TÊTE SI NÉCESSAIRE)
            int contentX = x;   // Position X initiale
            int contentW = totalW; // Largeur initiale
            
            //Vérifier si le groupe est collapsed
            HRibbonGroup group = (groupModel != null) ? groupModel.getHRibbonGroup(i) : null;
            boolean isCollapsed = (group != null && group.isCollapsed());
            
            // CAS DES EN-TÊTES LATÉRAUX (GAUCHE/DROITE)
            //Ne pas soustraire l'espace du header si le groupe est collapsed
            if (!isCollapsed && headerAlignment == Ribbon.HEADER_WEST) {
                // EN-TÊTE À GAUCHE : réserver de l'espace sur le côté gauche
                // Calcul de la zone réservée (header + marge)
                int reserved = Math.min(totalW, headerWidth + headerMargin);
                
                // Décaler le contenu vers la droite
                contentX = x + reserved;
                
                // Réduire la largeur disponible
                contentW = Math.max(0, totalW - reserved);
                
            } else if (!isCollapsed && headerAlignment == Ribbon.HEADER_EAST) {
                // EN-TÊTE À DROITE : réserver de l'espace sur le côté droit
                // Calcul de la zone réservée (header + marge)
                int reserved = Math.min(totalW, headerWidth + headerMargin);
                
                // Le contenu reste à gauche, l'en-tête est à droite
                contentX = x; // Pas de décalage
                
                // Réduire la largeur disponible
                contentW = Math.max(0, totalW - reserved);
                
            } else {
                // EN-TÊTES HAUT/BAS OU GROUPE COLLAPSED : le contenu utilise toute la largeur
                contentX = x;
                contentW = totalW;
            }
            
            // ÉTAPE 3 : CALCUL DE LA POSITION Y
            int y;
            if (headerAlignment == Ribbon.HEADER_NORTH) {
                // EN-TÊTE EN HAUT : laisser de l'espace pour l'en-tête + marge
                y = topInset + headerHeight + headerMargin;
            } else {
                // AUTRES CAS : aligner sur le bord haut
                y = topInset;
            }
            
            // ÉTAPE 4 : CRÉATION DU RECTANGLE DE CONTENU
            rects[i] = new Rectangle(contentX, y, contentW, contentHeight);
            
            // ÉTAPE 5 : AVANCEMENT DE LA POSITION X POUR LE GROUPE SUIVANT
            // Ajouter la largeur totale du groupe + la marge entre groupes
            x += totalW + Math.max(0, groupMargin);
        }
        
        // RETOUR DES RÉSULTATS
        return rects;
    }
}