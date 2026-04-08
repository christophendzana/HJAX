package hsplitpane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import hsplitpane.HSplitPane.ZonePosition;

/**
 * Barre de contrôle affichée en bordure d'une HSplitZone.
 *
 * Cette fine bande contient un titre optionnel et un bouton toggle qui permet à
 * l'utilisateur de réduire ou d'agrandir la zone parente. Sa position dans la
 * zone (haut, bas, gauche, droite) est déterminée automatiquement selon la
 * position de la zone dans le HSplitPane.
 *
 * Le bouton toggle est dessiné programmatiquement sous forme de flèche
 * directionnelle dont le sens indique l'action disponible.
 */
public class HSplitZoneHeader extends JPanel {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------
    /**
     * Épaisseur par défaut de la barre en pixels.
     */
    private static final int HAUTEUR_DEFAUT = 22;

    /**
     * Taille du bouton toggle en pixels.
     */
    private static final int TAILLE_BOUTON = 16;

    /**
     * Couleur de fond par défaut de la barre.
     */
    private static final Color COULEUR_FOND_DEFAUT = new Color(60, 63, 65);

    /**
     * Couleur du texte du titre par défaut.
     */
    private static final Color COULEUR_TITRE_DEFAUT = new Color(187, 187, 187);

    // -------------------------------------------------------------------------
    // Champs de configuration
    // -------------------------------------------------------------------------
    /**
     * Position de la zone parente, détermine l'orientation et le sens des
     * flèches.
     */
    private final ZonePosition position;

    /**
     * Titre affiché dans la barre. Peut être null pour ne rien afficher.
     */
    private String titre;

    /**
     * Couleur de fond personnalisable de la barre.
     */
    private Color couleurFond;

    /**
     * Couleur du texte du titre.
     */
    private Color couleurTitre;

    /**
     * Épaisseur de la barre en pixels.
     */
    private int epaisseur;

    // -------------------------------------------------------------------------
    // Composants internes
    // -------------------------------------------------------------------------
    /**
     * Étiquette affichant le titre de la zone.
     */
    private JLabel labelTitre;

    /**
     * Bouton dessiné programmatiquement pour le collapse/expand.
     */
    private JButton boutonToggle;

    /**
     * État courant de la zone parente : true = réduite, false = développée.
     */
    private boolean estCollapsed;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée une barre de contrôle avec des valeurs par défaut.
     *
     * @param position la position de la zone parente dans le HSplitPane
     */
    public HSplitZoneHeader(ZonePosition position) {
        this(position, null, COULEUR_FOND_DEFAUT, COULEUR_TITRE_DEFAUT, HAUTEUR_DEFAUT);
    }

    /**
     * Crée une barre de contrôle avec un titre et des valeurs par défaut pour
     * le reste.
     *
     * @param position la position de la zone parente
     * @param titre le texte affiché dans la barre, ou null pour aucun titre
     */
    public HSplitZoneHeader(ZonePosition position, String titre) {
        this(position, titre, COULEUR_FOND_DEFAUT, COULEUR_TITRE_DEFAUT, HAUTEUR_DEFAUT);
    }

    /**
     * Crée une barre de contrôle entièrement paramétrée.
     *
     * @param position la position de la zone parente
     * @param titre le texte affiché, ou null
     * @param couleurFond la couleur de fond de la barre
     * @param couleurTitre la couleur du texte du titre
     * @param epaisseur l'épaisseur de la barre en pixels
     */
    public HSplitZoneHeader(ZonePosition position, String titre,
            Color couleurFond, Color couleurTitre, int epaisseur) {
        this.position = position;
        this.titre = titre;
        this.couleurFond = couleurFond;
        this.couleurTitre = couleurTitre;
        this.epaisseur = epaisseur;
        this.estCollapsed = false;

        initialiserComposants();
        appliquerDimensions();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    /**
     * Construit et positionne les composants internes de la barre.
     */
    private void initialiserComposants() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        setOpaque(true);

        // Création et configuration du label titre
        labelTitre = new JLabel(titre != null ? titre : "");
        labelTitre.setForeground(couleurTitre);
        labelTitre.setFont(labelTitre.getFont().deriveFont(Font.PLAIN, 11f));
        labelTitre.setVisible(titre != null && !titre.isEmpty());

        // Création du bouton toggle dessiné programmatiquement
        boutonToggle = creerBoutonToggle();

        add(labelTitre);
        add(boutonToggle);
    }

    /**
     * Applique les contraintes de taille selon la position de la zone. Les
     * zones NORTH et SOUTH ont une barre horizontale (largeur libre, hauteur
     * fixe). Les zones EAST et WEST ont une barre verticale (largeur fixe,
     * hauteur libre).
     */
    private void appliquerDimensions() {
        if (position == ZonePosition.NORTH || position == ZonePosition.SOUTH) {
            setPreferredSize(new Dimension(Integer.MAX_VALUE, epaisseur));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, epaisseur));
            setMinimumSize(new Dimension(0, epaisseur));
        } else if (position == ZonePosition.EAST || position == ZonePosition.WEST) {
            setPreferredSize(new Dimension(epaisseur, Integer.MAX_VALUE));
            setMaximumSize(new Dimension(epaisseur, Integer.MAX_VALUE));
            setMinimumSize(new Dimension(epaisseur, 0));
        }
        // La zone CENTER n'a pas de barre de contrôle
    }

    /**
     * Crée le bouton toggle avec un rendu personnalisé. La flèche dessinée
     * change de sens selon l'état collapsed/expanded et selon la position de la
     * zone dans le HSplitPane.
     *
     * @return le bouton toggle prêt à l'emploi
     */
    private JButton creerBoutonToggle() {
        JButton bouton = new JButton() {

            @Override
            protected void paintComponent(Graphics g) {
                // On ne dessine pas le fond standard du bouton
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;
                int taille = 5; // demi-taille de la flèche

                g2.setColor(couleurTitre);

                // On détermine le sens de la flèche selon la position et l'état
                dessinerFleche(g2, cx, cy, taille);

                g2.dispose();
            }
        };

        bouton.setOpaque(false);
        bouton.setContentAreaFilled(false);
        bouton.setBorderPainted(false);
        bouton.setFocusPainted(false);
        bouton.setPreferredSize(new Dimension(TAILLE_BOUTON, TAILLE_BOUTON));
        bouton.setBorder(new EmptyBorder(0, 0, 0, 0));

        return bouton;
    }

    /**
     * Dessine une flèche triangulaire dans la direction appropriée.
     *
     * La logique est la suivante : - Zone NORTH : flèche vers le haut quand
     * développée (cliquer = réduire vers le haut) - Zone SOUTH : flèche vers le
     * bas quand développée - Zone WEST : flèche vers la gauche quand développée
     * - Zone EAST : flèche vers la droite quand développée Quand la zone est
     * réduite, la flèche pointe dans le sens inverse.
     *
     * @param g2 le contexte graphique
     * @param cx centre X de la flèche
     * @param cy centre Y de la flèche
     * @param taille la demi-taille de la flèche en pixels
     */
    private void dessinerFleche(Graphics2D g2, int cx, int cy, int taille) {
        int[] xPoints;
        int[] yPoints;

        switch (position) {
            case NORTH:
                if (!estCollapsed) {
                    // Flèche vers le haut
                    xPoints = new int[]{cx - taille, cx + taille, cx};
                    yPoints = new int[]{cy + taille, cy + taille, cy - taille};
                } else {
                    // Flèche vers le bas
                    xPoints = new int[]{cx - taille, cx + taille, cx};
                    yPoints = new int[]{cy - taille, cy - taille, cy + taille};
                }
                break;

            case SOUTH:
                if (!estCollapsed) {
                    // Flèche vers le bas
                    xPoints = new int[]{cx - taille, cx + taille, cx};
                    yPoints = new int[]{cy - taille, cy - taille, cy + taille};
                } else {
                    // Flèche vers le haut
                    xPoints = new int[]{cx - taille, cx + taille, cx};
                    yPoints = new int[]{cy + taille, cy + taille, cy - taille};
                }
                break;

            case WEST:
                if (!estCollapsed) {
                    // Flèche vers la gauche
                    xPoints = new int[]{cx + taille, cx + taille, cx - taille};
                    yPoints = new int[]{cy - taille, cy + taille, cy};
                } else {
                    // Flèche vers la droite
                    xPoints = new int[]{cx - taille, cx - taille, cx + taille};
                    yPoints = new int[]{cy - taille, cy + taille, cy};
                }
                break;

            case EAST:
            default:
                if (!estCollapsed) {
                    // Flèche vers la droite
                    xPoints = new int[]{cx - taille, cx - taille, cx + taille};
                    yPoints = new int[]{cy - taille, cy + taille, cy};
                } else {
                    // Flèche vers la gauche
                    xPoints = new int[]{cx + taille, cx + taille, cx - taille};
                    yPoints = new int[]{cy - taille, cy + taille, cy};
                }
                break;
        }

        g2.fillPolygon(xPoints, yPoints, 3);
    }

    // =========================================================================
    // Rendu personnalisé de la barre
    // =========================================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(couleurFond);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // =========================================================================
    // API publique
    // =========================================================================
    /**
     * Enregistre l'action à déclencher quand l'utilisateur clique sur le bouton
     * toggle. C'est la HSplitZone parente qui s'enregistre ici pour réagir au
     * clic.
     *
     * @param listener l'écouteur à notifier au clic
     */
    public void ajouterEcouteurToggle(ActionListener listener) {
        boutonToggle.addActionListener(listener);
    }

    /**
     * Met à jour l'état visuel du bouton toggle pour refléter l'état courant de
     * la zone. Doit être appelé par HSplitZone après chaque changement d'état.
     *
     * @param collapsed true si la zone vient d'être réduite, false si
     * développée
     */
    public void mettreAJourEtatCollapse(boolean collapsed) {
        this.estCollapsed = collapsed;
        boutonToggle.repaint();
    }

    // =========================================================================
    // Getters et Setters
    // =========================================================================
    public String getTitre() {
        return titre;
    }

    /**
     * Modifie le titre affiché dans la barre. Passer null ou une chaîne vide
     * masque le label.
     *
     * @param titre le nouveau titre
     */
    public void setTitre(String titre) {
        this.titre = titre;
        labelTitre.setText(titre != null ? titre : "");
        labelTitre.setVisible(titre != null && !titre.isEmpty());
        revalidate();
        repaint();
    }

    public Color getCouleurFond() {
        return couleurFond;
    }

    /**
     * Modifie la couleur de fond de la barre et redessine immédiatement.
     *
     * @param couleurFond la nouvelle couleur de fond
     */
    public void setCouleurFond(Color couleurFond) {
        this.couleurFond = couleurFond;
        repaint();
    }

    public Color getCouleurTitre() {
        return couleurTitre;
    }

    /**
     * Modifie la couleur du texte du titre et redessine immédiatement.
     *
     * @param couleurTitre la nouvelle couleur du texte
     */
    public void setCouleurTitre(Color couleurTitre) {
        this.couleurTitre = couleurTitre;
        labelTitre.setForeground(couleurTitre);
        repaint();
    }

    public int getEpaisseur() {
        return epaisseur;
    }

    /**
     * Modifie l'épaisseur de la barre et recalcule les dimensions.
     *
     * @param epaisseur la nouvelle épaisseur en pixels
     */
    public void setEpaisseur(int epaisseur) {
        this.epaisseur = epaisseur;
        appliquerDimensions();
        revalidate();
        repaint();
    }
}
