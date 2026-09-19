package hsplitpane;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;

/**
 * 
 * @author FIDELE
 * 
 * Représente une action disponible dans le header d'une zone (toggle,
 * fullscreen, float...), sous la forme d'une icône dessinée à la main et
 * d'une liste d'écouteurs.
 *
 * ============================================================================
 * PROBLÈME RÉSOLU
 * ============================================================================
 * Avant cette classe, HSplitZoneHeader dupliquait 3 fois (toggle / fullscreen
 * / float) le même triplet : une List&lt;ActionListener&gt;, une méthode
 * addXxxListener(...), et un bouton stylé à la main. Ajouter une 4ème action
 * (ex : "pin"/auto-hide, courant dans ce type de composant docking) obligeait
 * à dupliquer une 4ème fois cette mécanique directement dans le header —
 * violation d'Open/Closed : le header devait être MODIFIÉ pour être ÉTENDU.
 *
 * Avec HeaderAction, le header manipule une liste d'objets uniformes : ajouter
 * une action ne demande plus de toucher au mécanisme d'écoute lui-même.
 *
 * ============================================================================
 * PRINCIPE RÉUTILISABLE
 * ============================================================================
 * Dès qu'un composant UI gère plusieurs "actions similaires mais distinctes"
 * (boutons de toolbar, entrées de menu, onglets...), représenter chacune
 * comme une instance d'une classe uniforme — plutôt que comme un champ nommé
 * + une liste + une méthode par action — est ce qui permet l'extension sans
 * modification. Tu retrouveras ce même réflexe pour : les items d'une barre
 * d'outils, les commandes d'un menu contextuel, les colonnes d'un tableau.
 *
 * ============================================================================
 * LIMITE ASSUMÉE (transparence sur le périmètre de la refonte)
 * ============================================================================
 * Le POSITIONNEMENT des boutons dans le header (ordre, alignement horizontal
 * vs vertical) reste géré "à la main" dans HSplitZoneHeader.initialiserComposants(),
 * car l'ordre d'affichage diffère selon l'orientation (le header vertical
 * affiche toggle/fullscreen/float alors que l'horizontal affiche
 * float/fullscreen/toggle) — une vraie généralisation demanderait de revoir
 * l'algorithme de layout du header, ce qui n'était pas dans le périmètre validé.
 * Ce qui est résolu ici, c'est le mécanisme de NOTIFICATION (listeners), qui
 * est le point d'extension le plus utile en pratique (brancher un comportement
 * personnalisé sur une action existante, ou enregistrer une future action).
 */
public class HeaderAction {

    /** Dessine l'icône du bouton avec la couleur courante du titre du header. */
    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2, int width, int height, Color color);
    }

    private final String id;
    private final IconPainter iconPainter;
    private final List<ActionListener> listeners = new ArrayList<>();
    private AbstractButton button;

    public HeaderAction(String id, IconPainter iconPainter) {
        this.id = id;
        this.iconPainter = iconPainter;
    }

    public String getId() {
        return id;
    }

    public IconPainter getIconPainter() {
        return iconPainter;
    }

    /**
     * Enregistre un écouteur. Si un bouton est déjà créé pour cette action
     * (cas d'un ajout après construction du header), l'écouteur y est branché
     * immédiatement — l'appelant n'a pas à se soucier de l'ordre des appels.
     */
    public void addListener(ActionListener listener) {
        listeners.add(listener);
        if (button != null) {
            button.addActionListener(listener);
        }
    }

    /**
     * Associe cette action à son bouton Swing concret. Rebranche automatiquement
     * tous les écouteurs déjà enregistrés — c'est ce qui permet à
     * HSplitZoneHeader de recréer ses boutons (ex: changement de position)
     * sans avoir à sauvegarder/restaurer les listeners manuellement comme
     * c'était le cas auparavant.
     */
    void bindButton(AbstractButton newButton) {
        this.button = newButton;
        for (ActionListener l : listeners) {
            newButton.addActionListener(l);
        }
    }

    /** Redessine le bouton associé (utilisé quand l'état visuel change : collapsed/floating/...). */
    void repaintButton() {
        if (button != null) {
            button.repaint();
        }
    }
}
