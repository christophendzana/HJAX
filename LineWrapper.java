/*
 * LineWrapper.java
 * 
 * MOTEUR DE DISPOSITION EN LIGNES POUR LES GROUPES DU RUBAN HRibbon
 * 
 * RÔLE PRINCIPAL :
 * Organise les composants Swing en lignes avec retour à la ligne automatique
 * (wrapping) selon la largeur disponible, et gère leur positionnement vertical.
 * 
 * CONCEPT CLÉ : ALGORITHME DE WRAPPING ADAPTATIF
 * - Regroupement dynamique des composants en lignes selon la largeur disponible
 * - Centrage vertical des composants dans leur ligne
 * - Calcul de hauteur totale pour la pré-planification du layout
 * 
 * @author FIDELE
 * @version 1.0
 */
package rubban.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * LineWrapper - Moteur de disposition en lignes avec retour automatique
 * 
 * RESPONSABILITÉS PRÉCISES :
 * 1. Organise une liste de composants en lignes selon la largeur disponible
 * 2. Calcule la hauteur de chaque ligne (maximum des hauteurs des composants)
 * 3. Positionne les composants horizontalement avec espacement
 * 4. Centre verticalement chaque composant dans sa ligne
 * 5. Calcule la hauteur totale requise pour un ensemble de dimensions prédéfinies
 * 6. Orchestre le layout complet des composants dans un groupe
 * 
 * CONCEPTS IMPORTANTS :
 * - Ligne (Line) : Groupe de composants qui tiennent sur une même largeur
 * - Wrapping : Retour à la ligne quand la largeur est insuffisante
 * - Espacement (Spacing) : Distance horizontale entre composants
 * - Padding : Marge interne entre le bord du groupe et les composants
 * 
 * @see Component#getPreferredSize()
 * @see Rectangle
 */
public class LineWrapper {
    
    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    
    /**
     * Constructeur par défaut
     * 
     * Initialise un moteur de disposition en lignes sans configuration spécifique
     */
    public LineWrapper() {
        // Aucune initialisation spécifique nécessaire
    }
    
    // =========================================================================
    // MÉTHODE PRINCIPALE DE WRAPPING
    // =========================================================================
    
    /**
     * Organise les composants en lignes avec retour automatique selon la largeur
     * 
     * ALGORITHME DE WRAPPING :
     * 1. Parcourt les composants dans l'ordre
     * 2. Pour chaque composant, vérifie s'il tient dans la ligne actuelle
     * 3. Si oui, l'ajoute à la ligne actuelle
     * 4. Sinon, termine la ligne actuelle et commence une nouvelle ligne
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * car utilise Component.getPreferredSize()
     * 
     * @param components Liste ordonnée des composants à organiser en lignes
     *                   L'ordre d'origine est préservé dans le wrapping
     * @param innerWidth Largeur disponible pour les composants (en pixels)
     *                   Après soustraction du padding gauche/droite
     * @param spacing Espacement horizontal entre composants (en pixels)
     * 
     * @return Liste de lignes, où chaque ligne est une liste de composants
     *         Retourne une liste vide si les paramètres sont invalides
     * 
     * @throws IllegalStateException si appelé hors de l'EDT
     * 
     * @see #ensureEdt()
     */
    public List<List<Component>> wrapIntoLines(List<Component> components, int innerWidth, int spacing) {
        ensureEdt(); // Vérification thread safety
        
        List<List<Component>> lines = new ArrayList<>();
        
        // CAS LIMITE : Pas de composants ou paramètres invalides
        if (components == null || components.isEmpty()) {
            return lines; // Liste vide
        }
        
        // INITIALISATION DES VARIABLES DE CONTRÔLE
        List<Component> currentLine = new ArrayList<>(); // Ligne en cours de construction
        int currentLineWidth = 0; // Largeur cumulée de la ligne actuelle
        
        // PARCOURS DE TOUS LES COMPOSANTS
        for (Component comp : components) {
            // OBTENTION DE LA TAILLE PRÉFÉRÉE DU COMPOSANT
            Dimension pref = comp.getPreferredSize();
            int compWidth = (pref != null) ? pref.width : 0;
            
            // CALCUL DE LA LARGEUR REQUISE POUR CE COMPOSANT
            // (inclut l'espacement si ce n'est pas le premier de la ligne)
            int requiredWidth = compWidth;
            if (!currentLine.isEmpty()) {
                requiredWidth += spacing; // Ajouter l'espacement avant le composant
            }
            
            // DÉCISION : AJOUT À LA LIGNE ACTUELLE OU NOUVELLE LIGNE
            if (currentLine.isEmpty() || currentLineWidth + requiredWidth <= innerWidth) {
                // CAS 1 : LA LIGNE EST VIDE OU LE COMPOSANT TIENT DANS L'ESPACE RESTANT
                currentLine.add(comp);
                
                // MISE À JOUR DE LA LARGEUR DE LIGNE
                if (currentLine.isEmpty()) {
                    // Premier composant de la ligne (pas d'espacement avant)
                    currentLineWidth = compWidth;
                } else {
                    // Ajout avec espacement
                    currentLineWidth = currentLineWidth + requiredWidth;
                }
                
            } else {
                // CAS 2 : LE COMPOSANT NE TIENT PAS → TERMINER LA LIGNE ACTUELLE
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine); // Ajouter la ligne complétée à la liste
                }
                
                // DÉMARRER UNE NOUVELLE LIGNE AVEC CE COMPOSANT
                currentLine = new ArrayList<>();
                currentLine.add(comp);
                currentLineWidth = compWidth; // Réinitialiser la largeur
            }
        }
        
        // AJOUT DE LA DERNIÈRE LIGNE (SI NON VIDE)
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        
        return lines;
    }
    
    // =========================================================================
    // MÉTHODES DE CALCUL DE HAUTEUR
    // =========================================================================
    
    /**
     * Calcule la hauteur d'une ligne de composants
     * 
     * PRINCIPE :
     * La hauteur d'une ligne est la hauteur maximale parmi tous ses composants.
     * Cette hauteur détermine l'espace vertical nécessaire pour afficher la ligne.
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * car utilise Component.getPreferredSize()
     * 
     * @param line Liste des composants formant une ligne (non null)
     * @return Hauteur de la ligne en pixels (≥ 0)
     *         Retourne 0 si la ligne est null ou vide
     * 
     * @throws IllegalStateException si appelé hors de l'EDT
     */
    public int calculateLineHeight(List<Component> line) {
        ensureEdt(); // Vérification thread safety
        
        int maxHeight = 0;
        
        // CAS LIMITE : LIGNE NULLE OU VIDE
        if (line == null || line.isEmpty()) {
            return maxHeight; // 0
        }
        
        // RECHERCHE DE LA HAUTEUR MAXIMALE
        for (Component c : line) {
            Dimension pref = c.getPreferredSize();
            if (pref != null) {
                maxHeight = Math.max(maxHeight, pref.height);
            }
        }
        
        return maxHeight;
    }
    
    /**
     * Calcule la hauteur totale requise pour afficher un ensemble de dimensions
     * avec wrapping selon la largeur disponible
     * 
     * CARACTÉRISTIQUES :
     * - Méthode purement algorithmique (n'utilise pas de composants Swing)
     * - Utile pour la pré-planification du layout (PreferredSizeCalculator)
     * - Opère sur des Dimensions pré-calculées
     * 
     * @param prefs Liste des dimensions préférées des composants (dans l'ordre)
     * @param innerWidth Largeur disponible pour le wrapping (en pixels)
     * @param spacing Espacement horizontal entre composants (en pixels)
     * @param padding Padding vertical (haut + bas) à ajouter (en pixels)
     * 
     * @return Hauteur totale requise en pixels (padding inclus)
     * 
     * ALGORITHME SPÉCIAL :
     * - Si innerWidth ≤ 1 : Empilement vertical forcé (pas de wrapping)
     * - Sinon : Wrapping normal selon l'algorithme standard
     */
    public int computeHeightForPreferences(List<Dimension> prefs, int innerWidth, int spacing, int padding) {
        // CAS LIMITE : PAS DE DIMENSIONS
        if (prefs == null || prefs.isEmpty()) {
            // Hauteur minimale = seulement le padding
            return Math.max(0, padding * 2);
        }
        
        // CAS SPÉCIAL : LARGEUR TRÈS FAIBLE → EMPILEMENT VERTICAL FORCÉ
        if (innerWidth <= 1) {
            int totalHeight = 0;
            
            for (int i = 0; i < prefs.size(); i++) {
                // Ajouter l'espacement entre les composants (sauf pour le premier)
                if (i > 0) {
                    totalHeight += spacing;
                }
                
                // Ajouter la hauteur du composant
                totalHeight += prefs.get(i).height;
            }
            
            // Ajouter le padding haut et bas
            totalHeight += padding * 2;
            
            return totalHeight;
        }
        
        // ALGORITHME NORMAL DE WRAPPING POUR CALCUL DE HAUTEUR
        int totalHeight = 0;          // Hauteur totale accumulée
        int currentLineWidth = 0;     // Largeur de la ligne en cours
        int currentLineHeight = 0;    // Hauteur de la ligne en cours
        boolean firstInLine = true;   // Indicateur "premier composant de la ligne"
        
        Iterator<Dimension> it = prefs.iterator();
        
        while (it.hasNext()) {
            Dimension pd = it.next();
            int compW = pd.width;   // Largeur du composant
            int compH = pd.height;  // Hauteur du composant
            
            // Largeur requise pour ce composant (inclut l'espacement si nécessaire)
            int requiredW = compW;
            if (!firstInLine) {
                requiredW += spacing;
            }
            
            // DÉCISION : AJOUT À LA LIGNE ACTUELLE OU NOUVELLE LIGNE
            if (firstInLine || currentLineWidth + requiredW <= innerWidth) {
                // AJOUT À LA LIGNE ACTUELLE
                if (firstInLine) {
                    currentLineWidth = compW;          // Premier composant
                } else {
                    currentLineWidth = currentLineWidth + spacing + compW; // Avec espacement
                }
                
                currentLineHeight = Math.max(currentLineHeight, compH); // Mise à jour hauteur max
                firstInLine = false; // Plus le premier de la ligne
                
            } else {
                // TERMINER LA LIGNE ACTUELLE ET COMMENCER UNE NOUVELLE
                if (totalHeight > 0) {
                    totalHeight += spacing; // Espacement entre lignes
                }
                
                totalHeight += currentLineHeight; // Ajouter la hauteur de la ligne terminée
                
                // RÉINITIALISATION POUR LA NOUVELLE LIGNE
                currentLineWidth = compW;
                currentLineHeight = compH;
                firstInLine = false;
            }
        }
        
        // AJOUT DE LA DERNIÈRE LIGNE (SI ELLE CONTIENT DES COMPOSANTS)
        if (!firstInLine || currentLineHeight > 0) {
            if (totalHeight > 0) {
                totalHeight += spacing; // Espacement avant la dernière ligne
            }
            totalHeight += currentLineHeight; // Hauteur de la dernière ligne
        }
        
        // AJOUT DU PADDING VERTICAL
        totalHeight += padding * 2;
        
        return totalHeight;
    }
    
    // =========================================================================
    // MÉTHODES DE POSITIONNEMENT
    // =========================================================================
    
    /**
     * Positionne une ligne de composants horizontalement avec centrage vertical
     * 
     * ALGORITHME :
     * 1. Parcourt les composants de la ligne dans l'ordre
     * 2. Positionne chaque composant avec l'espacement spécifié
     * 3. Centre verticalement chaque composant dans la hauteur de ligne
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * car utilise Component.setBounds()
     * 
     * @param line Ligne de composants à positionner
     * @param startX Position X de départ pour le premier composant
     * @param lineY Position Y de la ligne (haut de la ligne)
     * @param spacing Espacement horizontal entre composants (en pixels)
     * @param lineHeight Hauteur de la ligne (pour le centrage vertical)
     * 
     * @throws IllegalStateException si appelé hors de l'EDT
     */
    public void positionLine(List<Component> line, int startX, int lineY, int spacing, int lineHeight) {
        ensureEdt(); // Vérification thread safety
        
        // CAS LIMITE : LIGNE NULLE OU VIDE
        if (line == null || line.isEmpty()) {
            return;
        }
        
        int currentX = startX; // Position X courante
        
        for (Component comp : line) {
            // OBTENTION DES DIMENSIONS PRÉFÉRÉES DU COMPOSANT
            Dimension pref = comp.getPreferredSize();
            int compW = (pref != null) ? pref.width : 0;
            int compH = (pref != null) ? pref.height : 0;
            
            // CALCUL DE LA POSITION Y POUR CENTRAGE VERTICAL
            // Formule : lineY + (lineHeight - compH) / 2
            int compY = lineY + Math.max(0, (lineHeight - compH) / 2);
            
            // APPLICATION DE LA POSITION ET DES DIMENSIONS
            // Note : La hauteur est limitée à lineHeight pour éviter le débordement
            comp.setBounds(currentX, compY, compW, Math.min(compH, lineHeight));
            
            // AVANCEMENT DE LA POSITION X POUR LE PROCHAIN COMPOSANT
            currentX += compW + spacing;
        }
    }
    
    // =========================================================================
    // MÉTHODE ORCHESTRATRICE DE LAYOUT COMPLET
    // =========================================================================
    
    /**
     * Dispose les composants dans un rectangle de groupe avec padding et espacement
     * 
     * ORCHESTRATION COMPLÈTE :
     * 1. Calcule la zone interne disponible (après padding)
     * 2. Organise les composants en lignes avec wrapping
     * 3. Pour chaque ligne, calcule sa hauteur et la positionne
     * 4. Gère le débordement vertical (arrêt si plus d'espace)
     * 
     * CONTRAINTE THREAD :
     * Doit être appelé sur l'Event Dispatch Thread (EDT)
     * 
     * @param components Liste des composants du groupe (ordre préservé)
     * @param groupRect Rectangle définissant la zone du groupe
     * @param padding Marge interne entre le bord du groupe et les composants
     * @param spacing Espacement horizontal entre composants
     * 
     * @throws IllegalStateException si appelé hors de l'EDT
     */
    public void layoutComponentsInGroup(List<Component> components, 
                                        Rectangle groupRect, 
                                        int padding, 
                                        int spacing) {
        ensureEdt(); // Vérification thread safety
        
        // VÉRIFICATION DES PARAMÈTRES D'ENTRÉE
        if (components == null || components.isEmpty() || groupRect == null) {
            return; // Rien à faire
        }
        
        // CALCUL DE LA ZONE INTERNE DISPONIBLE (après padding)
        int innerX = groupRect.x + padding;
        int innerY = groupRect.y + padding;
        int innerWidth = groupRect.width - (2 * padding);
        int innerHeight = groupRect.height - (2 * padding);
        
        // VÉRIFICATION DES DIMENSIONS INTERNES VALIDES
        if (innerWidth <= 0 || innerHeight <= 0) {
            return; // Zone trop petite pour afficher quoi que ce soit
        }
        
        // ÉTAPE 1 : ORGANISATION DES COMPOSANTS EN LIGNES
        List<List<Component>> lines = wrapIntoLines(components, innerWidth, spacing);
        
        // ÉTAPE 2 : POSITIONNEMENT DE CHAQUE LIGNE
        int currentY = innerY; // Position Y courante
        
        for (List<Component> line : lines) {
            // CALCUL DE LA HAUTEUR DE LA LIGNE
            int lineHeight = calculateLineHeight(line);
            
            // VÉRIFICATION DE L'ESPACE VERTICAL RESTANT
            if (currentY + lineHeight > innerY + innerHeight) {
                break; // Plus d'espace vertical disponible
            }
            
            // POSITIONNEMENT DE LA LIGNE
            positionLine(line, innerX, currentY, spacing, lineHeight);
            
            // AVANCEMENT DE LA POSITION Y POUR LA LIGNE SUIVANTE
            // (inclut l'espacement entre lignes)
            currentY += lineHeight + spacing;
        }
    }
    
    // =========================================================================
    // MÉTHODE UTILITAIRE DE VÉRIFICATION THREAD
    // =========================================================================
    
    /**
     * Vérifie que la méthode est appelée sur l'Event Dispatch Thread
     * 
     * Swing est single-threaded : toutes les opérations sur les composants
     * doivent être effectuées sur l'EDT pour éviter les problèmes de concurrence.
     * 
     * @throws IllegalStateException si appelé hors de l'EDT
     * 
     * @see SwingUtilities#isEventDispatchThread()
     */
    private void ensureEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("LineWrapper: method must be called on the EDT");
        }
    }
}