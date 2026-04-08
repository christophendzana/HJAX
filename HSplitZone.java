package hsplitpane;

import hcomponents.HScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import hsplitpane.HSplitPane.ZonePosition;
import hsplitpane.HSplitPane.WrapDirection;

/**
 * Représente une zone d'ancrage dans un HSplitPane.
 *
 * Chaque zone occupe une position fixe (NORTH, SOUTH, EAST, WEST ou CENTER) et
 * peut accueillir plusieurs composants enfants positionnés par
 * HSplitWrapLayout. La zone dispose d'une barre de contrôle (HSplitZoneHeader)
 * permettant de la réduire ou de la développer via un bouton toggle.
 *
 * Structure interne :
 * <pre>
 *   HSplitZone (BorderLayout)
 *   ├── HSplitZoneHeader  [bord selon la position]
 *   └── HScrollPane       [CENTER]
 *         └── panneauContenu (JPanel avec HSplitWrapLayout)
 * </pre>
 *
 * Quand la zone est réduite, sa taille est mise à zéro et la taille d'avant
 * réduction est mémorisée pour être restaurée au moment de l'expansion.
 */
public class HSplitZone extends JPanel {

    // -------------------------------------------------------------------------
    // Identité et position
    // -------------------------------------------------------------------------
    /**
     * Position de cette zone dans le HSplitPane parent.
     */
    private final ZonePosition position;

    /**
     * Titre affiché dans la barre de contrôle. Peut être null.
     */
    private String titre;

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------
    /**
     * Taille initiale fournie par la configuration. Null signifie que la zone
     * est flexible et prend l'espace restant.
     */
    private Dimension tailleInitiale;

    /**
     * Taille sauvegardée juste avant un collapse. Permet de restaurer
     * exactement la même taille lors de l'expansion.
     */
    private Dimension tailleAvantCollapse;

    // -------------------------------------------------------------------------
    // État
    // -------------------------------------------------------------------------
    /**
     * Indique si la zone est actuellement réduite.
     */
    private boolean collapsed;

    // -------------------------------------------------------------------------
    // Composants internes
    // -------------------------------------------------------------------------
    /**
     * Barre de contrôle avec titre et bouton toggle.
     */
    private HSplitZoneHeader header;

    /**
     * Panneau interne qui reçoit les composants de l'utilisateur.
     */
    private JPanel panneauContenu;

    /**
     * Conteneur de défilement qui enveloppe le panneau de contenu.
     */
    private HScrollPane scrollPane;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée une zone sans titre ni taille initiale définie.
     *
     * @param position la position de cette zone dans le HSplitPane
     */
    public HSplitZone(ZonePosition position) {
        this(position, null, null);
    }

    /**
     * Crée une zone avec un titre mais sans taille initiale définie.
     *
     * @param position la position de cette zone dans le HSplitPane
     * @param titre le titre affiché dans la barre de contrôle, ou null
     */
    public HSplitZone(ZonePosition position, String titre) {
        this(position, titre, null);
    }

    /**
     * Crée une zone entièrement paramétrée.
     *
     * @param position la position de cette zone dans le HSplitPane
     * @param titre le titre affiché dans la barre de contrôle, ou null
     * @param tailleInitiale la taille initiale souhaitée, ou null pour flexible
     */
    public HSplitZone(ZonePosition position, String titre, Dimension tailleInitiale) {
        this.position = position;
        this.titre = titre;
        this.tailleInitiale = tailleInitiale;
        this.collapsed = false;

        initialiserComposants();
        assemblerZone();
        brancherEcouteurs();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================
    /**
     * Instancie les composants internes de la zone.
     */
    private void initialiserComposants() {

        // La barre de contrôle n'est pas créée pour la zone CENTER
        if (position != ZonePosition.CENTER) {
            header = new HSplitZoneHeader(position, titre);
        }

        // Le panneau de contenu utilise le layout wrap
        panneauContenu = new JPanel(new HSplitWrapLayout());
        panneauContenu.setOpaque(false);

        // Le scroll pane enveloppe le panneau de contenu
        scrollPane = new HScrollPane(panneauContenu);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(HScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(HScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Assemble les composants dans la zone selon la position. La barre de
     * contrôle est placée du côté intérieur de la zone pour que le bouton
     * toggle soit visible près du séparateur adjacent.
     */
    private void assemblerZone() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(43, 43, 43));

        // Placement de la barre selon la position de la zone
        if (header != null) {
            switch (position) {
                case NORTH:
                    // La barre est en bas de la zone NORTH (côté intérieur)
                    add(header, BorderLayout.SOUTH);
                    break;
                case SOUTH:
                    // La barre est en haut de la zone SOUTH (côté intérieur)
                    add(header, BorderLayout.NORTH);
                    break;
                case WEST:
                    // La barre est à droite de la zone WEST (côté intérieur)
                    add(header, BorderLayout.EAST);
                    break;
                case EAST:
                    // La barre est à gauche de la zone EAST (côté intérieur)
                    add(header, BorderLayout.WEST);
                    break;
                default:
                    break;
            }
        }

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Connecte le bouton toggle de la barre au mécanisme de collapse/expand. À
     * chaque clic, on bascule l'état de la zone et on notifie le parent.
     */
    private void brancherEcouteurs() {
        if (header != null) {
            header.ajouterEcouteurToggle(e -> basculerCollapse());
        }
    }

    // =========================================================================
    // Logique collapse / expand
    // =========================================================================
    /**
     * Bascule l'état de la zone entre réduit et développé. Appelé
     * automatiquement par le clic sur le bouton toggle.
     */
    private void basculerCollapse() {
        if (collapsed) {
            expand();
        } else {
            collapse();
        }
    }

    /**
     * Réduit la zone à une taille nulle.
     *
     * La taille courante est mémorisée dans tailleAvantCollapse avant que la
     * zone soit masquée. Le HSplitPaneRootLayout détectera le changement lors
     * du prochain revalidate et redistribuera l'espace.
     */
    public void collapse() {
        if (collapsed) {
            return;
        }

        // On mémorise la taille courante avant de réduire
        tailleAvantCollapse = getSize();

        collapsed = true;

        // On masque le contenu mais on garde la barre visible
        scrollPane.setVisible(false);

        // On indique au layout parent que quelque chose a changé
        if (header != null) {
            header.mettreAJourEtatCollapse(true);
        }

        // On notifie le conteneur parent pour recalcul du layout
        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    /**
     * Développe la zone en restaurant sa taille d'avant le collapse.
     */
    public void expand() {
        if (!collapsed) {
            return;
        }

        collapsed = false;
        scrollPane.setVisible(true);

        if (header != null) {
            header.mettreAJourEtatCollapse(false);
        }

        if (getParent() != null) {
            getParent().revalidate();
            getParent().repaint();
        }
    }

    // =========================================================================
    // API publique
    // =========================================================================
    /**
     * Ajoute un composant dans la zone de contenu. Le HSplitWrapLayout se
     * chargera de le positionner avec les autres.
     *
     * @param composant le composant à ajouter dans cette zone
     */
    public void addContainer(Component composant) {
        panneauContenu.add(composant);
        panneauContenu.revalidate();
        panneauContenu.repaint();
    }

    /**
     * Retire un composant de la zone de contenu.
     *
     * @param composant le composant à retirer
     */
    public void removeContainer(Component composant) {
        panneauContenu.remove(composant);
        panneauContenu.revalidate();
        panneauContenu.repaint();
    }

    /**
     * Indique si la zone est vide, c'est-à-dire qu'elle ne contient aucun
     * composant. Une zone vide ne devrait pas prendre d'espace dans le layout
     * racine.
     *
     * @return true si aucun composant n'a été ajouté à cette zone
     */
    public boolean estVide() {
        return panneauContenu.getComponentCount() == 0;
    }

    /**
 * Modifie la direction de disposition des composants dans cette zone.
 * Les composants seront réorganisés immédiatement après l'appel.
 *
 * @param direction la nouvelle direction de wrapping
 */
public void setWrapDirection(WrapDirection direction) {
    HSplitWrapLayout layout = (HSplitWrapLayout) panneauContenu.getLayout();
    layout.setDirection(direction);
    panneauContenu.revalidate();
    panneauContenu.repaint();
}

/**
 * Active ou désactive l'étirement des composants dans cette zone.
 * Quand true, les composants occupent tout l'espace disponible sur leur ligne.
 * Quand false, ils conservent leur preferredSize.
 *
 * @param etirer true pour étirer, false pour respecter la preferredSize
 */
public void setEtirer(boolean etirer) {
    HSplitWrapLayout layout = (HSplitWrapLayout) panneauContenu.getLayout();
    layout.setEtirer(etirer);
    panneauContenu.revalidate();
    panneauContenu.repaint();
}

    
    // =========================================================================
    // Getters et Setters
    // =========================================================================
    public ZonePosition getPosition() {
        return position;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public Dimension getTailleInitiale() {
        return tailleInitiale;
    }

    public void setTailleInitiale(Dimension tailleInitiale) {
        this.tailleInitiale = tailleInitiale;
    }

    public Dimension getTailleAvantCollapse() {
        return tailleAvantCollapse;
    }

    public String getTitre() {
        return titre;
    }

    /**
     * Modifie le titre de la zone et met à jour la barre de contrôle.
     *
     * @param titre le nouveau titre, ou null pour masquer le titre
     */
    public void setTitre(String titre) {
        this.titre = titre;
        if (header != null) {
            header.setTitre(titre);
        }
    }

    /**
     * Retourne la barre de contrôle de cette zone. Null si la zone est CENTER
     * (pas de barre pour le centre).
     *
     * @return le header ou null
     */
    public HSplitZoneHeader getHeader() {
        return header;
    }
}
