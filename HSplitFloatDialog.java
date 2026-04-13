package hsplitpane;

import hcomponents.HDialog;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Fenêtre flottante utilisée quand une HSplitZone est détachée du HSplitPane.
 *
 * Cette classe encapsule un HDialog configuré pour être non-modal et sans
 * overlay visible. Elle accueille le scrollPane de la zone pendant la
 * flottaison et notifie la zone via un callback à sa fermeture, ce qui
 * déclenche la réintégration automatique du contenu dans le HSplitPane.
 *
 * Cycle de vie :
 * 1. HSplitZone crée ce dialog et lui passe son scrollPane
 * 2. L'utilisateur interagit avec le contenu dans la fenêtre flottante
 * 3. L'utilisateur ferme la fenêtre → le callback notifie HSplitZone
 * 4. HSplitZone récupère son scrollPane et se réintègre dans le HSplitPane
 */
public class HSplitFloatDialog extends HDialog {

    // -------------------------------------------------------------------------
    // Champs
    // -------------------------------------------------------------------------

    /**
     * Titre de la zone flottante — affiché dans la barre de titre du dialog.
     */
    private final String titreZone;

    /**
     * Callback déclenché à la fermeture du dialog.
     * C'est HSplitZone qui s'enregistre ici pour gérer la réintégration.
     */
    private Runnable onFermetureCallback;

    // =========================================================================
    // Constructeur
    // =========================================================================

    /**
     * Crée une fenêtre flottante pour la zone spécifiée.
     *
     * Le dialog est configuré comme suit :
     * - Non-modal : ne bloque pas le HSplitPane
     * - Overlay transparent : pas de fond semi-transparent
     * - Overlay non cliquable : évite une fermeture accidentelle
     * - Taille initiale : 600x400 pixels, redimensionnable
     *
     * @param titreZone le titre de la zone, devient le titre du dialog
     */
    public HSplitFloatDialog(String titreZone) {
        // On passe null comme owner pour que la fenêtre soit indépendante
        super();

        this.titreZone = titreZone != null ? titreZone : "Zone flottante";

        configurerDialog();
        brancherEcouteurFermeture();
    }

    // =========================================================================
    // Configuration
    // =========================================================================

    /**
     * Configure le HDialog pour un comportement de fenêtre flottante.
     *
     * Points clés :
     * - setModal(false)        : ne bloque pas l'interface principale
     * - overlayOpacity à 0     : pas de fond semi-transparent affiché
     * - closeOnOverlayClick    : désactivé pour éviter les fermetures accidentelles
     * - showFooter à false     : pas de boutons d'action dans le footer
     * - titre                  : nom de la zone flottante
     */
    private void configurerDialog() {
        setModal(false);
        setOverlayOpacity(0.0f);
        setCloseOnOverlayClick(false);
        setShowFooter(false);
        setTitle(titreZone);
        setDialogSize(600, 400);
        setResizable(true);
        
        ajouterDragSurHeader();
    }

    /**
     * Rend le headerPanel de HDialog draggable.
     *
     * On enregistre la position de la souris au mousePressed, puis à chaque
     * mouseDragged on calcule le déplacement et on repositionne la fenêtre.
     */
    private void ajouterDragSurHeader() {
        JPanel header = getHeaderPanel();
        if (header == null) return;

        final Point[] pointDepart = {null};

        MouseAdapter dragAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                // On mémorise la position de la souris à l'écran au début du drag
                pointDepart[0] = e.getLocationOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (pointDepart[0] == null) return;

                Point posActuelle = e.getLocationOnScreen();
                Point posDialog   = getLocation();

                // Déplacement = position actuelle - position de départ
                int deltaX = posActuelle.x - pointDepart[0].x;
                int deltaY = posActuelle.y - pointDepart[0].y;

                // Nouvelle position de la fenêtre
                setLocation(posDialog.x + deltaX, posDialog.y + deltaY);

                // On met à jour le point de départ pour le prochain événement
                pointDepart[0] = posActuelle;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pointDepart[0] = null;
            }
        };

        header.addMouseListener(dragAdapter);
        header.addMouseMotionListener(dragAdapter);
    }

    /**
     * Branche l'écouteur de fermeture.
     *
     * Note : HDialog est undecorated — il n'y a pas de X natif du système.
     * La fermeture passe par le closeButton interne de HDialog qui appelle
     * closeWithAnimation(). On surcharge cette méthode pour intercepter
     * la fermeture et déclencher la réintégration avant la destruction.
     * Le windowClosing reste branché comme garde supplémentaire.
     */
    private void brancherEcouteurFermeture() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                declencherFermeture();
            }
        });
    }

    /**
     * Surcharge de closeWithAnimation() de HDialog.
     *
     * HDialog appelle cette méthode depuis son closeButton interne.
     * On intercepte ici pour déclencher la réintégration AVANT que
     * HDialog ne ferme la fenêtre et ne dispose les ressources.
     */
    @Override
    public void closeWithAnimation() {
        declencherFermeture();
    }

    /**
     * Déclenche la réintégration puis ferme la fenêtre.
     *
     * Ordre strict :
     * 1. On exécute le callback (réintégration du scrollPane dans la zone)
     * 2. On neutralise le callback pour éviter une double exécution
     * 3. On ferme la fenêtre via la méthode parente
     */
    private void declencherFermeture() {
        if (onFermetureCallback != null) {
            Runnable callback = onFermetureCallback;
            // On neutralise avant d'appeler pour éviter toute récursion
            onFermetureCallback = null;
            callback.run();
        }
        // Fermeture effective via la méthode originale de HDialog
        super.closeWithAnimation();
    }

    // =========================================================================
    // API publique
    // =========================================================================

    /**
     * Définit le composant à afficher dans la fenêtre flottante.
     * Typiquement, c'est le scrollPane de la HSplitZone.
     *
     * @param composant le composant à afficher
     */
    public void setContenu(JComponent composant) {
        setContent(composant);
    }

    /**
     * Enregistre le callback à déclencher à la fermeture du dialog.
     * C'est HSplitZone qui s'enregistre ici.
     *
     * @param callback l'action à exécuter lors de la fermeture
     */
    public void setOnFermetureCallback(Runnable callback) {
        this.onFermetureCallback = callback;
    }

    /**
     * Affiche la fenêtre flottante.
     */
    public void afficher() {
        showWithAnimation();
    }

    /**
     * Ferme la fenêtre flottante depuis l'extérieur (ex : réintégration
     * déclenchée par le bouton float du header).
     * Dans ce cas le callback est déjà neutralisé par HSplitZone avant
     * cet appel pour éviter la récursion.
     */
    public void fermer() {
        super.closeWithAnimation();
    }

    // =========================================================================
    // Getter
    // =========================================================================

    public String getTitreZone() {
        return titreZone;
    }
}