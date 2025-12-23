package hcomponents.vues; 

import javax.swing.*;       // Pour JDialog, JPanel, Timer
import java.awt.*;          // Pour Graphics, Dimension, etc.
import java.awt.event.ActionEvent;    // Pour les événements de timer
import java.awt.event.ActionListener; // Pour écouter les ticks du timer

/**
 * HDialogUI - Classe utilitaire pour les animations de HDialog
 * 
 * PRINCIPE FONDAMENTAL : Cette classe NE PAS étendre un UI Delegate Swing.
 * C'est une classe utilitaire statique qui gère uniquement les animations.
 * 
 * POURQUOI UNE CLASSE SÉPARÉE ? 
 * 1. Séparation des préoccupations : Animation ≠ Rendu
 * 2. Réutilisabilité : Peut être utilisée par d'autres composants
 * 3. Testabilité : Plus facile à tester unitairement
 * 
 * TECHNIQUE D'ANIMATION : Utilise javax.swing.Timer qui s'exécute dans
 * l'EDT (Event Dispatch Thread) de Swing, ce qui est SÉCURISÉ.
 * 
 * EFFETS IMPLÉMENTÉS :
 * 1. Fade (apparition/disparition en fondu)
 * 2. Scale (agrandissement/réduction)
 * 3. Combinaison des deux pour un effet "material design"
 * 
 * @author FIDELE
 * @version 1.0
 * @see HDialog
 */
public class HDialogUI {
    
    // CONSTANTES D'ANIMATION    
    /** 
     * Durée de l'animation en millisecondes.
     * 
     * CHOIX UX : 250ms est une durée optimale pour :
     * - Assez rapide pour ne pas faire attendre
     * - Assez lente pour être perceptible
     * - Correspond aux standards Material Design
     */
    private static final int ANIMATION_DURATION = 250;
    
    /** 
     * Intervalle entre les frames de l'animation (en ms).
     * 
     * CALCUL : 1000ms / 60 FPS ≈ 16.67ms par frame
     * On arrondit à 16ms pour ~62.5 FPS
     * 
     * POURQUOI 60 FPS ? 
     * - Fréquence de rafraîchissement standard des écrans
     * - Fluide à l'œil humain
     * - Équilibre performance/fluidité
     */
    private static final int ANIMATION_FPS = 16;

    // MÉTHODE : ANIMATION D'APPARITION
    
    /**
     * Anime l'apparition du dialog avec effet fade + scale.
     * 
     * ALGORITHME :
     * 1. Initialisation : Opacité 0%, taille réduite à 80%
     * 2. Boucle d'animation : Chaque 16ms, calcul du progrès
     * 3. Interpolation : Applique l'easing aux valeurs
     * 4. Mise à jour : Opacité et taille
     * 5. Finalisation : Remise à l'état final
     * 
     * @param dialog Le JDialog à animer (NE PAS utiliser Window.setOpacity() directement)
     * @param contentPanel Le JPanel intérieur qui sera redimensionné
     */
    public static void animateShow(JDialog dialog, JPanel contentPanel) {
        // ÉTAT INITIAL (avant l'animation)
        
        // Rendre le dialog complètement transparent
        // Valeur : 0.0f = invisible, 1.0f = opaque
        dialog.setOpacity(0f);
        
        // Calcul de la taille initiale réduite
        // Objectif : Créer un effet "qui grossit depuis le centre"
        
        // Récupère la taille finale souhaitée
        // getPreferredSize() : Taille idéale calculée par les layout managers
        Dimension finalSize = contentPanel.getPreferredSize();
        
        // Lignes 4-6 : Calcule la taille initiale à 80% de la finale
        // Pourquoi 80% ? Suffisamment petit pour voir l'effet, assez grand pour être lisible
        Dimension initialSize = new Dimension(
            (int)(finalSize.width * 0.8),   // 80% de la largeur
            (int)(finalSize.height * 0.8)   // 80% de la hauteur
        );
        
        // Applique la taille initiale réduite
        // setPreferredSize() : Indique au layout manager la taille souhaitée
        contentPanel.setPreferredSize(initialSize);
        
        // Enregistre l'heure de début pour calculer le progrès
        // System.currentTimeMillis() : Temps en ms depuis le 1er janvier 1970
        long startTime = System.currentTimeMillis();
        
        // ÉTAPE 2 : CRÉATION DU TIMER D'ANIMATION
        
        // Crée un Timer Swing
        // Premier paramètre : délai entre les ticks (16ms = ~60 FPS)
        // Deuxième paramètre : initialement null, sera rempli plus tard
        Timer timer = new Timer(ANIMATION_FPS, null);
        
        // Ligne 12 : Ajoute un ActionListener au timer
        // Ce code sera exécuté toutes les 16ms
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ÉTAPE 3 : CALCUL DU PROGRÈS DE L'ANIMATION
                
                // Temps écoulé depuis le début
                // elapsed = temps_actuel - temps_début
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Ligne 18 : Progrès normalisé de 0.0 à 1.0
                // Math.min(1f, ...) : Garantit qu'on ne dépasse pas 1.0
                // (float) conversion : Évite la division entière
                float progress = Math.min(1f, elapsed / (float)ANIMATION_DURATION);
                
                // APPLICATION DE L'EASING (LISSAGE)
                
                // Transforme le progrès linéaire en progrès "ease-out"
                // easeOutCubic : Démarre vite, ralentit à la fin (plus naturel)
                float easedProgress = easeOutCubic(progress);
                
                //  MISE À JOUR DE L'OPACITÉ (FADE)                
                // Modifie progressivement l'opacité
                // easedProgress : 0.0 → 1.0 pendant l'animation
                dialog.setOpacity(easedProgress);
                
                // ÉTAPE 6 : MISE À JOUR DE LA TAILLE (SCALE)
                
                // Calcule la taille courante
                // Formule : taille_courante = taille_initiale + (différence × progrès)
                // Exemple : Si initial=80, final=100, progrès=0.5 → 80 + (20×0.5) = 90
                int currentWidth = (int)(initialSize.width 
                    + (finalSize.width - initialSize.width) * easedProgress);
                int currentHeight = (int)(initialSize.height 
                    + (finalSize.height - initialSize.height) * easedProgress);
                
                // Ligne 31 : Applique la taille courante
                contentPanel.setPreferredSize(new Dimension(currentWidth, currentHeight));
                
                // =======================================================
                // MISE À JOUR AFFICHAGE
                // =======================================================
                
                // Recalcule le layout avec la nouvelle taille
                // pack() : Redimensionne la fenêtre pour s'adapter au contenu
                dialog.pack();
                
                // Recentre le dialog par rapport à son parent
                // Important car la taille change, le centre doit rester stable
                dialog.setLocationRelativeTo(dialog.getParent());
                
                // VÉRIFICATION FIN D'ANIMATION                
                // Si l'animation est terminée (progrès >= 100%)
                if (progress >= 1f) {
                    // Ligne 39 : Arrête le timer (plus de ticks)
                    timer.stop();
                    
                    // Lignes 40-41 : État final garantit
                    // Parfois nécessaire à cause des approximations numériques
                    contentPanel.setPreferredSize(finalSize);
                    dialog.setOpacity(1f);
                }
            } 
        }); 
        
        //DÉMARRAGE DE L'ANIMATION
        
        // Démarre le timer
        // Le timer commencera à envoyer des ActionEvent toutes les 16ms
        timer.start();
    }

    // MÉTHODE : ANIMATION DE FERMETURE    
    /**
     * Anime la fermeture du dialog avec effet fade + scale inversé.
     * 
     * DIFFÉRENCES AVEC animateShow() :
     * 1. Progrès inversé : 1.0 → 0.0
     * 2. Easing différent : ease-in (lent → rapide)
     * 3. Callback de fin : Permet de fermer réellement la fenêtre après l'animation
     * 
     * @param dialog Le dialog à animer
     * @param contentPanel Le panel de contenu
     * @param onComplete Runnable exécuté quand l'animation termine
     */
    public static void animateClose(JDialog dialog, JPanel contentPanel, 
                                   Runnable onComplete) {
        // Taille actuelle (au début de la fermeture)
        Dimension initialSize = contentPanel.getSize();
        
        //  Taille finale (80% de l'initiale)
        // Inversé par rapport à animateShow()
        Dimension finalSize = new Dimension(
            (int)(initialSize.width * 0.8), 
            (int)(initialSize.height * 0.8)
        );
        
        //Heure de début (comme précédemment)
        long startTime = System.currentTimeMillis();
        
        // Ligne 66 : Timer pour l'animation de fermeture
        Timer timer = new Timer(ANIMATION_FPS, null);
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Calcul du progrès (identique)
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, elapsed / (float)ANIMATION_DURATION);
                
                // Easing DIFFÉRENT pour la fermeture
                // easeInCubic : Lent au début, rapide à la fin
                // Donne une sensation de "qui part vite"
                float easedProgress = easeInCubic(progress);
                
                //Opacité inversée (1.0 → 0.0)
                dialog.setOpacity(1f - easedProgress);
                
                // Taille inversée (grand → petit)
                int currentWidth = (int)(initialSize.width 
                    - (initialSize.width - finalSize.width) * easedProgress);
                int currentHeight = (int)(initialSize.height 
                    - (initialSize.height - finalSize.height) * easedProgress);
                
                // Application (identique)
                contentPanel.setPreferredSize(new Dimension(currentWidth, currentHeight));
                dialog.pack();
                dialog.setLocationRelativeTo(dialog.getParent());
                
                // Fin d'animation avec callback
                if (progress >= 1f) {
                    timer.stop();
                    //Exécute le callback si fourni
                    // Typiquement : dialog.dispose() pour fermer la fenêtre
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
        
        timer.start();
    }

    // ===================================================================
    // FONCTIONS D'EASING - MATHÉMATIQUES DES ANIMATIONS
    // ===================================================================
    
    /**
     * Fonction d'easing "ease-out cubic".
     * 
     * FORMULE MATHÉMATIQUE : f(t) = 1 - (1 - t)³
     * 
     * COMPORTEMENT VISUEL :
     * - t=0.0 → f(t)=0.0
     * - t=0.5 → f(t)=0.875 (avancé à 87.5% du chemin)
     * - t=1.0 → f(t)=1.0
     * 
     * POURQUOI CETTE FONCTION ?
     * Simule l'inertie : commence vite, ralentit progressivement.
     * Correspond aux lois de la physique (frottements).
     * 
     * @param t Le temps normalisé [0.0, 1.0]
     * @return La position transformée [0.0, 1.0]
     */
    private static float easeOutCubic(float t) {
        // 1 - (1 - t)^3
        // Math.pow : puissance (1-t)³
        // (float) : conversion double → float
        return 1 - (float)Math.pow(1 - t, 3);
    }

    /**
     * Fonction d'easing "ease-in cubic".
     * 
     * FORMULE MATHÉMATIQUE : f(t) = t³
     * 
     * COMPORTEMENT VISUEL :
     * - t=0.0 → f(t)=0.0
     * - t=0.5 → f(t)=0.125 (seulement 12.5% du chemin)
     * - t=1.0 → f(t)=1.0
     * 
     * UTILISATION : Pour les disparitions (effet "qui s'échappe")
     * 
     * @param t Le temps normalisé [0.0, 1.0]
     * @return La position transformée [0.0, 1.0]
     */
    private static float easeInCubic(float t) {
        // t^3
        return (float)Math.pow(t, 3);
    }

    // ===================================================================
    // MÉTHODE UTILITAIRE : INTERPOLATION DE COULEURS
    // ===================================================================
    
    /**
     * Interpolation linéaire entre deux couleurs.
     * 
     * PRINCIPE : Pour chaque composante (R, G, B, A), calcule :
     * composante_resultat = c1 + (c2 - c1) × progress
     * 
     * EXEMPLE : Rouge (255,0,0) → Vert (0,255,0) à progress=0.5
     * R = 255 + (0-255)×0.5 = 127.5 ≈ 128
     * G = 0 + (255-0)×0.5 = 127.5 ≈ 128
     * B = 0 + (0-0)×0.5 = 0
     * Résultat : Gris-rougeâtre (128,128,0)
     * 
     * UTILISATION : Transitions de couleur fluides (hover, focus, etc.)
     * 
     * @param c1 Couleur de départ
     * @param c2 Couleur d'arrivée
     * @param progress Progression [0.0, 1.0]
     * @return Couleur interpolée
     */
    public static Color interpolateColor(Color c1, Color c2, float progress) {
        //Clamp (limitation) de la progression entre 0 et 1
        // Évite les valeurs hors limites qui créeraient des couleurs invalides
        progress = Math.max(0, Math.min(1, progress));
        
        // Interpolation linéaire pour chaque canal
        // getRed()/getAlpha() : retournent des valeurs 0-255
        // (int) : conversion float → int (troncature)
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        int a = (int)(c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * progress);        
 
        return new Color(r, g, b, a);
    }
} 