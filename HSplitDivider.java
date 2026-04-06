package hsplitpane;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * Séparateur draggable entre deux zones adjacentes d'un HSplitPane.
 *
 * Ce composant se positionne entre deux zones et permet à l'utilisateur
 * de les redimensionner en le faisant glisser. Il change de couleur
 * au survol et pendant le drag pour un retour visuel clair.
 *
 * Le divider notifie le HSplitPaneRootLayout via un callback chaque fois
 * que l'utilisateur le déplace, en lui transmettant le déplacement delta.
 * C'est le layout racine qui se charge du redimensionnement effectif des zones.
 *
 * Un divider peut être verrouillé dynamiquement pour empêcher tout déplacement.
 */
public class HSplitDivider extends JPanel {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    /** Épaisseur par défaut du séparateur en pixels. */
    private static final int EPAISSEUR_DEFAUT = 4;

    /** Couleur normale par défaut. */
    private static final Color COULEUR_DEFAUT = new Color(50, 50, 50);

    /** Couleur au survol par défaut. */
    private static final Color COULEUR_HOVER_DEFAUT = new Color(80, 130, 200);

    /** Couleur pendant le drag par défaut. */
    private static final Color COULEUR_DRAG_DEFAUT = new Color(100, 160, 230);

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    /**
     * Orientation du séparateur.
     * HORIZONTAL = sépare une zone du haut et une du bas (drag vertical).
     * VERTICAL   = sépare une zone de gauche et une de droite (drag horizontal).
     */
    private final WrapDirection orientation;

    /** Épaisseur du séparateur en pixels. */
    private int epaisseur;

    /** Couleur affichée dans l'état normal. */
    private Color couleur;

    /** Couleur affichée quand la souris survole le séparateur. */
    private Color couleurHover;

    /** Couleur affichée pendant que l'utilisateur déplace le séparateur. */
    private Color couleurDrag;

    // -------------------------------------------------------------------------
    // État
    // -------------------------------------------------------------------------

    /** Indique si le séparateur est en cours de déplacement. */
    private boolean isDragging;

    /** Indique si le séparateur est survolé par la souris. */
    private boolean isHovered;

    /**
     * Indique si le séparateur est verrouillé.
     * Quand true, la souris ne change pas de forme et le drag est ignoré.
     */
    private boolean locked;

    /** Position X de la souris au début du drag. */
    private int dragStartX;

    /** Position Y de la souris au début du drag. */
    private int dragStartY;

    // -------------------------------------------------------------------------
    // Callback de notification
    // -------------------------------------------------------------------------

    /**
     * Fonction appelée à chaque mouvement pendant le drag.
     * Reçoit le delta de déplacement (positif ou négatif) sur l'axe concerné.
     * C'est le HSplitPaneRootLayout qui s'enregistre ici.
     */
    private Consumer<Integer> onDragCallback;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    /**
     * Crée un séparateur avec les valeurs par défaut.
     *
     * @param orientation l'orientation du séparateur
     */
    public HSplitDivider(WrapDirection orientation) {
        this(orientation, EPAISSEUR_DEFAUT, COULEUR_DEFAUT,
             COULEUR_HOVER_DEFAUT, COULEUR_DRAG_DEFAUT);
    }

    /**
     * Crée un séparateur entièrement paramétré.
     *
     * @param orientation  l'orientation du séparateur
     * @param epaisseur    l'épaisseur en pixels
     * @param couleur      la couleur normale
     * @param couleurHover la couleur au survol
     * @param couleurDrag  la couleur pendant le drag
     */
    public HSplitDivider(WrapDirection orientation, int epaisseur,
                          Color couleur, Color couleurHover, Color couleurDrag) {
        this.orientation  = orientation;
        this.epaisseur    = epaisseur;
        this.couleur      = couleur;
        this.couleurHover = couleurHover;
        this.couleurDrag  = couleurDrag;
        this.locked       = false;

        configurerCurseur();
        brancherEcouteursSouris();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    /**
     * Configure le curseur affiché selon l'orientation du séparateur.
     * Le curseur change en double flèche pour indiquer la direction de drag.
     */
    private void configurerCurseur() {
        if (!locked) {
            if (orientation == WrapDirection.HORIZONTAL) {
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            }
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Branche les écouteurs souris pour gérer le survol et le drag.
     */
    private void brancherEcouteursSouris() {
        MouseAdapter adaptateur = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!locked) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!locked) {
                    isDragging = true;
                    dragStartX = e.getXOnScreen();
                    dragStartY = e.getYOnScreen();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!locked && isDragging && onDragCallback != null) {

                    // On calcule le déplacement selon l'axe pertinent
                    int delta;
                    if (orientation == WrapDirection.HORIZONTAL) {
                        delta      = e.getYOnScreen() - dragStartY;
                        dragStartY = e.getYOnScreen();
                    } else {
                        delta      = e.getXOnScreen() - dragStartX;
                        dragStartX = e.getXOnScreen();
                    }

                    // On notifie le layout racine avec le delta
                    if (delta != 0) {
                        onDragCallback.accept(delta);
                    }
                }
            }
        };

        addMouseListener(adaptateur);
        addMouseMotionListener(adaptateur);
    }

    // =========================================================================
    // Rendu
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // On choisit la couleur selon l'état courant
        if (isDragging) {
            g.setColor(couleurDrag);
        } else if (isHovered) {
            g.setColor(couleurHover);
        } else {
            g.setColor(couleur);
        }

        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // =========================================================================
    // API publique
    // =========================================================================

    /**
     * Enregistre le callback qui sera appelé à chaque déplacement du séparateur.
     * C'est le HSplitPaneRootLayout qui s'enregistre ici.
     *
     * @param callback une fonction qui reçoit le delta de déplacement en pixels
     */
    public void setOnDragCallback(Consumer<Integer> callback) {
        this.onDragCallback = callback;
    }

    // =========================================================================
    // Getters et Setters
    // =========================================================================

    public boolean isLocked() {
        return locked;
    }

    /**
     * Verrouille ou déverrouille le séparateur.
     * Quand verrouillé, le drag est ignoré et le curseur revient à la normale.
     *
     * @param locked true pour verrouiller, false pour déverrouiller
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        configurerCurseur();
        repaint();
    }

    public WrapDirection getOrientation() {
        return orientation;
    }

    public int getEpaisseur() {
        return epaisseur;
    }

    public void setEpaisseur(int epaisseur) {
        this.epaisseur = epaisseur;
        revalidate();
        repaint();
    }

    public Color getCouleur() {
        return couleur;
    }

    public void setCouleur(Color couleur) {
        this.couleur = couleur;
        repaint();
    }

    public Color getCouleurHover() {
        return couleurHover;
    }

    public void setCouleurHover(Color couleurHover) {
        this.couleurHover = couleurHover;
    }

    public Color getCouleurDrag() {
        return couleurDrag;
    }

    public void setCouleurDrag(Color couleurDrag) {
        this.couleurDrag = couleurDrag;
    }
}
