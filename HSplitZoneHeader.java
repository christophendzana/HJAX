package hsplitpane;

import hcomponents.HButton;
import hcomponents.HLabel;
import hcomponents.vues.HLabelOrientation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.JButton;
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
    private static final int DEFAULT_HEIGHT = 22;

    /**
     * Taille du bouton toggle en pixels.
     */
    private static final int BUTTON_SIZE = 16;

    /**
     * Couleur de fond par défaut de la barre.
     */
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(60, 63, 65);

    /**
     * Couleur du texte du titre par défaut.
     */
    private static final Color DEFAULT_TITLE_COLOR = new Color(187, 187, 187);

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
    private Color backgroundColor;

    /**
     * Couleur du texte du titre.
     */
    private Color titleColor;

    /**
     * Épaisseur de la barre en pixels.
     */
    private int thickness;

    // -------------------------------------------------------------------------
    // Composants internes
    // -------------------------------------------------------------------------
    /**
     * Étiquette affichant le titre de la zone.
     * HLabel est utilisé pour supporter l'orientation verticale
     * sur les zones WEST et EAST.
     */
    private HLabel titleLabel;

    /**
     * Bouton dessiné programmatiquement pour le collapse/expand.
     */
    private HButton toggleButton;

    /**
     * État courant de la zone parente : true = réduite, false = développée.
     */
    private boolean isCollapsed;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée une barre de contrôle avec des valeurs par défaut.
     *
     * @param position la position de la zone parente dans le HSplitPane
     */
    public HSplitZoneHeader(ZonePosition position) {
        this(position, null, DEFAULT_BACKGROUND_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_HEIGHT);
    }

    /**
     * Crée une barre de contrôle avec un titre et des valeurs par défaut pour
     * le reste.
     *
     * @param position la position de la zone parente
     * @param titre le texte affiché dans la barre, ou null pour aucun titre
     */
    public HSplitZoneHeader(ZonePosition position, String titre) {
        this(position, titre, DEFAULT_BACKGROUND_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_HEIGHT);
    }

    /**
     * Crée une barre de contrôle entièrement paramétrée.
     *
     * @param position la position de la zone parente
     * @param titre le texte affiché, ou null
     * @param backgroundColor la couleur de fond de la barre
     * @param titleColor la couleur du texte du titre
     * @param thickness l'épaisseur de la barre en pixels
     */
    public HSplitZoneHeader(ZonePosition position, String titre,
            Color backgroundColor, Color titleColor, int thickness) {
        this.position = position;
        this.titre = titre;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.thickness = thickness;
        this.isCollapsed = false;

        initialiserComposants();
        applyDimensions();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    /**
     * Construit et positionne les composants internes de la barre.
     */
    private void initialiserComposants() {
    setOpaque(true);

    // Création du HLabel avec orientation automatique selon la position
    titleLabel = new HLabel(titre != null ? titre : "");
    titleLabel.setForeground(titleColor);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));
    titleLabel.setVisible(titre != null && !titre.isEmpty());

    switch (position) {
        case WEST:
            titleLabel.setOrientation(HLabelOrientation.VERTICAL_UP);
            break;
        case EAST:
            titleLabel.setOrientation(HLabelOrientation.VERTICAL_DOWN);
            break;
        default:
            titleLabel.setOrientation(HLabelOrientation.HORIZONTAL);
            break;
    }

    toggleButton = createToggleButton();

    // Layout selon l'orientation de la zone
    if (position == ZonePosition.NORTH || position == ZonePosition.SOUTH) {
        // Header horizontal — centrage vertical via BoxLayout
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
        add(javax.swing.Box.createHorizontalStrut(4));
        add(titleLabel);
        add(javax.swing.Box.createHorizontalGlue());
        add(toggleButton);
        add(javax.swing.Box.createHorizontalStrut(4));

        // Centrage vertical de chaque composant dans l'axe X
        titleLabel.setAlignmentY(CENTER_ALIGNMENT);
        toggleButton.setAlignmentY(CENTER_ALIGNMENT);

    } else {
        // Header vertical (EAST / WEST) — centrage horizontal via BoxLayout
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        add(javax.swing.Box.createVerticalStrut(4));
        add(toggleButton);
        add(javax.swing.Box.createVerticalGlue());
        add(titleLabel);
        add(javax.swing.Box.createVerticalStrut(4));

        // Centrage horizontal de chaque composant dans l'axe Y
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        toggleButton.setAlignmentX(CENTER_ALIGNMENT);
    }
}

    /**
     * Applique les contraintes de taille selon la position de la zone. Les
     * zones NORTH et SOUTH ont une barre horizontale (largeur libre, hauteur
     * fixe). Les zones EAST et WEST ont une barre verticale (largeur fixe,
     * hauteur libre).
     */
    private void applyDimensions() {
        if (position == ZonePosition.NORTH || position == ZonePosition.SOUTH) {
            setPreferredSize(new Dimension(Integer.MAX_VALUE, thickness));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, thickness));
            setMinimumSize(new Dimension(0, thickness));
        } else if (position == ZonePosition.EAST || position == ZonePosition.WEST) {
            setPreferredSize(new Dimension(thickness, Integer.MAX_VALUE));
            setMaximumSize(new Dimension(thickness, Integer.MAX_VALUE));
            setMinimumSize(new Dimension(thickness, 0));
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
    private HButton createToggleButton() {
        HButton bouton = new HButton() {

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

                g2.setColor(titleColor);

                // On détermine le sens de la flèche selon la position et l'état
                drawArrow(g2, cx, cy, taille);

                g2.dispose();
            }
        };

        bouton.setOpaque(false);
        bouton.setContentAreaFilled(false);
        bouton.setBorderPainted(false);
        bouton.setFocusPainted(false);
        bouton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
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
    private void drawArrow(Graphics2D g2, int cx, int cy, int taille) {
        int[] xPoints;
        int[] yPoints;

        switch (position) {
            case NORTH:
                if (!isCollapsed) {
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
                if (!isCollapsed) {
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
                if (!isCollapsed) {
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
                if (!isCollapsed) {
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
        g.setColor(backgroundColor);
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
    public void addToggleListener(ActionListener listener) {
        toggleButton.addActionListener(listener);
    }

    /**
     * Met à jour l'état visuel du bouton toggle pour refléter l'état courant de
     * la zone. Doit être appelé par HSplitZone après chaque changement d'état.
     *
     * @param collapsed true si la zone vient d'être réduite, false si
     * développée
     */
    public void updateCollapseState(boolean collapsed) {
        this.isCollapsed = collapsed;
        toggleButton.repaint();
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
        titleLabel.setText(titre != null ? titre : "");
        titleLabel.setVisible(titre != null && !titre.isEmpty());
        revalidate();
        repaint();
    }

    /**
     * Modifie la police du titre affiché dans la barre.
     *
     * @param font la nouvelle police
     */
    public void setTitleFont(Font font) {
        titleLabel.setFont(font);
        revalidate();
        repaint();
    }

    /**
     * Retourne la police courante du titre.
     *
     * @return la police du label titre
     */
    public Font getTitleFont() {
        return titleLabel.getFont();
    }

    /**
     * Modifie l'orientation du titre manuellement.
     * Par défaut l'orientation est appliquée automatiquement selon la position
     * de la zone, mais ce setter permet de la forcer si besoin.
     *
     * @param orientation la nouvelle orientation du titre
     */
    public void setTitleOrientation(HLabelOrientation orientation) {
        titleLabel.setOrientation(orientation);
        revalidate();
        repaint();
    }

    /**
     * Retourne l'orientation courante du titre.
     *
     * @return l'orientation du HLabel titre
     */
    public HLabelOrientation getTitleOrientation() {
        return titleLabel.getOrientation();
    }

    public Color getCouleurFond() {
        return backgroundColor;
    }

    /**
     * Modifie la couleur de fond de la barre et redessine immédiatement.
     *
     * @param backgroundColor la nouvelle couleur de fond
     */
    public void setCouleurFond(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getCouleurTitre() {
        return titleColor;
    }

    /**
     * Modifie la couleur du texte du titre et redessine immédiatement.
     *
     * @param titleColor la nouvelle couleur du texte
     */
    public void setCouleurTitre(Color titleColor) {
        this.titleColor = titleColor;
        titleLabel.setForeground(titleColor);
        repaint();
    }

    public int getEpaisseur() {
        return thickness;
    }

    /**
     * Modifie l'épaisseur de la barre et recalcule les dimensions.
     *
     * @param thickness la nouvelle épaisseur en pixels
     */
    public void setEpaisseur(int thickness) {
        this.thickness = thickness;
        applyDimensions();
        revalidate();
        repaint();
    }
    
    @Override
public Dimension getPreferredSize() {
    if (position == ZonePosition.NORTH || position == ZonePosition.SOUTH) {
        // Header horizontal : hauteur fixe, largeur illimitée
        return new Dimension(Short.MAX_VALUE, thickness);
    } else if (position == ZonePosition.WEST || position == ZonePosition.EAST) {
        // Header vertical : largeur fixe, hauteur illimitée
        return new Dimension(thickness, Short.MAX_VALUE);
    }
    return super.getPreferredSize();
}
}
