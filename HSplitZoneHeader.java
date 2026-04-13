package hsplitpane;

import hcomponents.HButton;
import hcomponents.HLabel;
import hcomponents.vues.HLabelOrientation;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import hsplitpane.HSplitPane.ZonePosition;

/**
 * Barre de contrôle affichée en bordure d'une HSplitZone.
 *
 * Contient trois boutons dessinés programmatiquement :
 * - Toggle      : collapse / expand de la zone
 * - FullScreen  : agrandit la zone / restaure les dimensions normales
 * - Float       : détache la zone dans une fenêtre flottante
 *
 * Pour EAST et WEST, le layout est vertical et le titre s'oriente
 * automatiquement pour rester lisible.
 */
public class HSplitZoneHeader extends JPanel {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------

    private static final int   DEFAULT_HEIGHT           = 22;
    private static final int   BUTTON_SIZE              = 16;
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(60, 63, 65);
    private static final Color DEFAULT_TITLE_COLOR      = new Color(187, 187, 187);

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    private final ZonePosition position;
    private String             title;
    private Color              backgroundColor;
    private Color              titleColor;
    private int                thickness;

    // -------------------------------------------------------------------------
    // États visuels
    // -------------------------------------------------------------------------

    /** true si la zone est réduite — détermine le sens de la flèche toggle. */
    private boolean isCollapsed;

    /** true si la zone est en mode fullscreen. */
    private boolean isFullScreen;

    /** true si la zone est actuellement flottante. */
    private boolean isFloating;

    // -------------------------------------------------------------------------
    // Composants internes
    // -------------------------------------------------------------------------

    private HLabel  titleLabel;
    private HButton toggleButton;
    private HButton fullScreenButton;
    private HButton floatButton;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    public HSplitZoneHeader(ZonePosition position) {
        this(position, null, DEFAULT_BACKGROUND_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_HEIGHT);
    }

    public HSplitZoneHeader(ZonePosition position, String title) {
        this(position, title, DEFAULT_BACKGROUND_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_HEIGHT);
    }

    public HSplitZoneHeader(ZonePosition position, String title,
            Color backgroundColor, Color titleColor, int thickness) {
        this.position        = position;
        this.title           = title;
        this.backgroundColor = backgroundColor;
        this.titleColor      = titleColor;
        this.thickness       = thickness;
        this.isCollapsed     = false;
        this.isFullScreen    = false;
        this.isFloating      = false;

        initialiserComposants();
        applyDimensions();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    /**
     * Construit les composants internes et applique le layout selon la position.
     *
     * NORTH / SOUTH : BoxLayout horizontal, titre à gauche, boutons à droite.
     * EAST  / WEST  : BoxLayout vertical, boutons en haut, titre en bas.
     */
    private void initialiserComposants() {
        setOpaque(true);

        // -- Titre orienté automatiquement --
        titleLabel = new HLabel(title != null ? title : "");
        titleLabel.setForeground(titleColor);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));
        titleLabel.setVisible(title != null && !title.isEmpty());

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

        // -- Boutons --
        toggleButton     = creerBoutonToggle();
        fullScreenButton = creerBoutonFullScreen();
        floatButton      = creerBoutonFloat();

        // -- Layout selon l'orientation --
        if (position == ZonePosition.NORTH || position == ZonePosition.SOUTH) {

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(Box.createHorizontalStrut(4));
            add(titleLabel);
            add(Box.createHorizontalGlue());
            add(floatButton);
            add(Box.createHorizontalStrut(2));
            add(fullScreenButton);
            add(Box.createHorizontalStrut(2));
            add(toggleButton);
            add(Box.createHorizontalStrut(4));

            titleLabel.setAlignmentY(CENTER_ALIGNMENT);
            floatButton.setAlignmentY(CENTER_ALIGNMENT);
            fullScreenButton.setAlignmentY(CENTER_ALIGNMENT);
            toggleButton.setAlignmentY(CENTER_ALIGNMENT);

        } else {

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(Box.createVerticalStrut(4));
            add(toggleButton);
            add(Box.createVerticalStrut(2));
            add(fullScreenButton);
            add(Box.createVerticalStrut(2));
            add(floatButton);
            add(Box.createVerticalGlue());
            add(titleLabel);
            add(Box.createVerticalStrut(4));

            titleLabel.setAlignmentX(CENTER_ALIGNMENT);
            toggleButton.setAlignmentX(CENTER_ALIGNMENT);
            fullScreenButton.setAlignmentX(CENTER_ALIGNMENT);
            floatButton.setAlignmentX(CENTER_ALIGNMENT);
        }
    }

    /**
     * Applique les contraintes de taille selon la position.
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
    }

    // =========================================================================
    // Création des boutons
    // =========================================================================

    /**
     * Crée le bouton toggle avec une flèche directionnelle.
     * Le sens de la flèche dépend de la position de la zone et de l'état
     * collapsed/expanded.
     */
    private HButton creerBoutonToggle() {
        HButton bouton = new HButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(titleColor);
                dessinerFleche(g2, getWidth() / 2, getHeight() / 2, 4);
                g2.dispose();
            }
        };
        styleButton(bouton);
        return bouton;
    }

    /**
     * Crée le bouton fullscreen.
     *
     * État normal    : carré vide avec coins marqués (agrandir)
     * État fullscreen: deux carrés imbriqués décalés (restaurer)
     */
    private HButton creerBoutonFullScreen() {
        HButton bouton = new HButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(titleColor);
                g2.setStroke(new BasicStroke(1.5f));

                int w  = getWidth();
                int h  = getHeight();
                int m  = 3;
                int sz = Math.min(w, h) - m * 2;
                int x0 = (w - sz) / 2;
                int y0 = (h - sz) / 2;

                if (!isFullScreen) {
                    // Icône "agrandir" : carré simple
                    g2.drawRect(x0, y0, sz, sz);
                } else {
                    // Icône "restaurer" : deux carrés décalés
                    int off = 3;
                    int sz2 = sz - off;
                    g2.drawRect(x0 + off, y0, sz2, sz2);
                    g2.drawRect(x0, y0 + off, sz2, sz2);
                }

                g2.dispose();
            }
        };
        styleButton(bouton);
        return bouton;
    }

    /**
     * Crée le bouton float.
     *
     * État normal  : petite fenêtre avec flèche sortante (détacher)
     * État flottant: flèche entrante (réintégrer)
     */
    private HButton creerBoutonFloat() {
        HButton bouton = new HButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(titleColor);
                g2.setStroke(new BasicStroke(1.5f));

                int w  = getWidth();
                int h  = getHeight();
                int m  = 3;
                int sz = Math.min(w, h) - m * 2;
                int x0 = (w - sz) / 2;
                int y0 = (h - sz) / 2;

                if (!isFloating) {
                    // Petite fenêtre en bas-gauche
                    int ws = (int) (sz * 0.65);
                    g2.drawRect(x0, y0 + sz - ws, ws, ws);
                    // Flèche diagonale vers le haut-droit
                    int ax = x0 + sz;
                    int ay = y0;
                    int as = 3;
                    g2.drawLine(x0 + ws - 1, y0 + sz - ws + 1, ax, ay);
                    g2.drawLine(ax, ay, ax - as, ay);
                    g2.drawLine(ax, ay, ax, ay + as);
                } else {
                    // Flèche diagonale vers le bas-gauche (réintégrer)
                    int as = 4;
                    g2.drawLine(x0 + sz, y0, x0, y0 + sz);
                    g2.drawLine(x0, y0 + sz, x0 + as * 2, y0 + sz);
                    g2.drawLine(x0, y0 + sz, x0, y0 + sz - as * 2);
                }

                g2.dispose();
            }
        };
        styleButton(bouton);
        return bouton;
    }

    /**
     * Applique le style commun (transparent, sans bordure, taille fixe)
     * à un bouton du header.
     */
    private void styleButton(HButton bouton) {
        bouton.setOpaque(false);
        bouton.setContentAreaFilled(false);
        bouton.setBorderPainted(false);
        bouton.setFocusPainted(false);
        bouton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        bouton.setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        bouton.setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        bouton.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    // =========================================================================
    // Dessin de la flèche toggle
    // =========================================================================

    /**
     * Dessine la flèche du bouton toggle.
     * Le sens dépend de la position de la zone et de l'état collapsed.
     */
    private void dessinerFleche(Graphics2D g2, int cx, int cy, int t) {
        int[] xPoints;
        int[] yPoints;

        switch (position) {
            case NORTH:
                xPoints = new int[]{cx - t, cx + t, cx};
                yPoints = isCollapsed
                        ? new int[]{cy - t, cy - t, cy + t}
                        : new int[]{cy + t, cy + t, cy - t};
                break;
            case SOUTH:
                xPoints = new int[]{cx - t, cx + t, cx};
                yPoints = isCollapsed
                        ? new int[]{cy + t, cy + t, cy - t}
                        : new int[]{cy - t, cy - t, cy + t};
                break;
            case WEST:
                xPoints = isCollapsed
                        ? new int[]{cx - t, cx - t, cx + t}
                        : new int[]{cx + t, cx + t, cx - t};
                yPoints = new int[]{cy - t, cy + t, cy};
                break;
            case EAST:
            default:
                xPoints = isCollapsed
                        ? new int[]{cx + t, cx + t, cx - t}
                        : new int[]{cx - t, cx - t, cx + t};
                yPoints = new int[]{cy - t, cy + t, cy};
                break;
        }

        g2.fillPolygon(xPoints, yPoints, 3);
    }

    // =========================================================================
    // Rendu du fond
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    // =========================================================================
    // API publique — enregistrement des écouteurs
    // =========================================================================

    /**
     * Enregistre l'écouteur du bouton toggle (collapse/expand).
     */
    public void addToggleListener(ActionListener listener) {
        toggleButton.addActionListener(listener);
    }

    /**
     * Enregistre l'écouteur du bouton fullscreen.
     */
    public void addFullScreenListener(ActionListener listener) {
        fullScreenButton.addActionListener(listener);
    }

    /**
     * Enregistre l'écouteur du bouton float.
     */
    public void addFloatListener(ActionListener listener) {
        floatButton.addActionListener(listener);
    }

    // =========================================================================
    // API publique — mise à jour des états visuels
    // =========================================================================

    /**
     * Met à jour l'icône du bouton toggle.
     *
     * @param collapsed true si la zone est réduite
     */
    public void updateCollapseState(boolean collapsed) {
        this.isCollapsed = collapsed;
        toggleButton.repaint();
    }

    /**
     * Met à jour l'icône du bouton fullscreen.
     *
     * @param fullScreen true si la zone est en mode fullscreen
     */
    public void updateFullScreenState(boolean fullScreen) {
        this.isFullScreen = fullScreen;
        fullScreenButton.repaint();
    }

    /**
     * Met à jour l'icône du bouton float.
     *
     * @param floating true si la zone est actuellement flottante
     */
    public void updateFloatingState(boolean floating) {
        this.isFloating = floating;
        floatButton.repaint();
    }

    // =========================================================================
    // Getters et Setters
    // =========================================================================

    public String getTitre() { return title; }

    public void setTitre(String title) {
        this.title = title;
        titleLabel.setText(title != null ? title : "");
        titleLabel.setVisible(title != null && !title.isEmpty());
        revalidate();
        repaint();
    }

    public void setTitleFont(Font font) {
        titleLabel.setFont(font);
        revalidate();
        repaint();
    }

    public Font getTitleFont() { return titleLabel.getFont(); }

    public void setTitleOrientation(HLabelOrientation orientation) {
        titleLabel.setOrientation(orientation);
        revalidate();
        repaint();
    }

    public HLabelOrientation getTitleOrientation() {
        return titleLabel.getOrientation();
    }

    public Color getCouleurFond()  { return backgroundColor; }

    public void setCouleurFond(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getCouleurTitre() { return titleColor; }

    public void setCouleurTitre(Color titleColor) {
        this.titleColor = titleColor;
        titleLabel.setForeground(titleColor);
        repaint();
    }

    public int getEpaisseur() { return thickness; }

    public void setEpaisseur(int thickness) {
        this.thickness = thickness;
        applyDimensions();
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (position == ZonePosition.NORTH || position == ZonePosition.SOUTH) {
            return new Dimension(Short.MAX_VALUE, thickness);
        } else if (position == ZonePosition.WEST || position == ZonePosition.EAST) {
            return new Dimension(thickness, Short.MAX_VALUE);
        }
        return super.getPreferredSize();
    }
}