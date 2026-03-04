/*
 * HRibbonTabs.java
 *
 * Composant combinant un HTabbedPane et des Ribbon.
 * Chaque onglet contient exactement un Ribbon, exactement comme dans Microsoft Word.
 *
 * ARCHITECTURE :
 * HRibbonTabs (JComponent, BorderLayout)
 *     └── HTabbedPane (CENTER)
 *             ├── Onglet "Accueil"  → Ribbon
 *             ├── Onglet "Insertion" → Ribbon
 *             └── ...
 *
 * RÈGLE DE TAILLE :
 * - HRibbonTabs prend toujours toute la largeur de son parent
 * - La hauteur totale est définie une seule fois par l'utilisateur via setHeight()
 * - HTabbedPane occupe toute la surface de HRibbonTabs (BorderLayout.CENTER)
 * - Chaque Ribbon occupe toute la largeur de l'onglet
 * - La hauteur de chaque Ribbon = hauteur totale - hauteur barre onglets (35px par défaut)
 *
 * USAGE TYPIQUE :
 *   HRibbonTabs ribbonTabs = new HRibbonTabs();
 *   ribbonTabs.setHeight(150);
 *   ribbonTabs.addTab("Accueil");
 *   ribbonTabs.addTab("Insertion");
 *   ribbonTabs.addRibbon("Accueil", monRibbon);
 *   ribbonTabs.addRibbon(1, autreRibbon);
 */
package rubban;

import hcomponents.HTabbedPane;
import hcomponents.vues.HTabbedPaneStyle;
import java.awt.*;
import javax.swing.*;

/**
 * HRibbonTabs — Composant de ruban à onglets style Microsoft Word.
 *
 * Combine un HTabbedPane et un Ribbon par onglet dans un composant
 * prêt à l'emploi. L'utilisateur définit la hauteur totale une seule fois,
 * et tous les composants internes s'ajustent automatiquement.
 *
 * @author FIDELE
 * @version 1.0
 */
public class HRibbonTabs extends JComponent {

    // =========================================================================
    // CONSTANTES
    // =========================================================================

    /**
     * Hauteur estimée de la barre d'onglets du HTabbedPane en pixels.
     * Utilisée pour calculer la hauteur disponible pour les Ribbon.
     * Cette valeur est une approximation conservative qui fonctionne
     * correctement avant que le composant soit affiché à l'écran.
     */
    private static final int HAUTEUR_BARRE_ONGLETS = 35;

    /**
     * Hauteur totale par défaut du composant en pixels.
     * Correspond à la hauteur typique d'un ruban Word.
     */
    private static final int HAUTEUR_PAR_DEFAUT = 150;

    // =========================================================================
    // COMPOSANTS INTERNES
    // =========================================================================

    /**
     * Le TabbedPane qui contient visuellement tous les onglets.
     * C'est lui qui gère la navigation entre les onglets.
     */
    private final HRibbonTabbedPane tabbedPane;

    // =========================================================================
    // ÉTAT INTERNE
    // =========================================================================

    /**
     * Hauteur totale souhaitée pour ce composant.
     * Définie par l'utilisateur via setHeight().
     * La hauteur disponible pour chaque Ribbon est :
     * hauteurTotale - HAUTEUR_BARRE_ONGLETS
     */
    private int hauteurTotale = HAUTEUR_PAR_DEFAUT;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================

    /**
     * Constructeur par défaut.
     * Crée un HRibbonTabs avec la hauteur par défaut (150px) et le style PRIMARY.
     */
    public HRibbonTabs() {
        this(HAUTEUR_PAR_DEFAUT, HTabbedPaneStyle.PRIMARY);
    }

    /**
     * Constructeur avec hauteur personnalisée.
     * Le style visuel PRIMARY est appliqué par défaut.
     *
     * @param hauteur hauteur totale souhaitée en pixels
     */
    public HRibbonTabs(int hauteur) {
        this(hauteur, HTabbedPaneStyle.PRIMARY);
    }

    /**
     * Constructeur avec hauteur et style personnalisés.
     * C'est le constructeur principal — tous les autres lui délèguent.
     *
     * @param hauteur hauteur totale souhaitée en pixels
     * @param style   style visuel du HTabbedPane
     */
    public HRibbonTabs(int hauteur, HTabbedPaneStyle style) {
        super();

        // Mémoriser la hauteur souhaitée
        this.hauteurTotale = hauteur;

        // Créer le HTabbedPane avec le style demandé
        this.tabbedPane = new HRibbonTabbedPane();
        this.tabbedPane.setTabbedStyle(style);

        // BorderLayout : le HTabbedPane occupera toujours toute la surface
        setLayout(new BorderLayout());
        add(this.tabbedPane, BorderLayout.CENTER);

        // Forcer la taille préférée dès la création
        appliquerTaillePreferee();
    }

    // =========================================================================
    // GESTION DE LA TAILLE
    // =========================================================================

    /**
     * Définit la hauteur totale du composant.
     *
     * Cette méthode est le point central de contrôle des tailles.
     * Elle met à jour la taille préférée du composant global, puis propage
     * la hauteur correcte à tous les Ribbon déjà associés aux onglets.
     *
     * @param hauteur nouvelle hauteur totale en pixels (minimum 60px)
     */
    public void setHeight(int hauteur) {
        // Garantir une hauteur minimale raisonnable
        this.hauteurTotale = Math.max(60, hauteur);

        // Mettre à jour la taille préférée du composant global
        appliquerTaillePreferee();

        // Propager la nouvelle hauteur à tous les Ribbon existants
        propagerHauteurAuxRibbons();

        // Déclencher le recalcul de l'affichage
        revalidate();
        repaint();
    }

    /**
     * Retourne la hauteur totale actuellement configurée.
     *
     * @return hauteur totale en pixels
     */
    public int getHeight() {
        return hauteurTotale;
    }

    /**
     * Calcule la hauteur disponible pour les Ribbon.
     *
     * La hauteur d'un Ribbon = hauteur totale - hauteur barre onglets.
     * On retire aussi les insets du contenu du HTabbedPane (5px haut + 5px bas
     * selon getContentBorderInsets dans HBasicTabbedPaneUI).
     *
     * @return hauteur disponible pour un Ribbon en pixels
     */
    private int calculerHauteurRibbon() {
        // Soustraire la barre d'onglets et les insets du contenu (5 + 5 = 10px)
        return Math.max(40, hauteurTotale - HAUTEUR_BARRE_ONGLETS - 10);
    }

    /**
     * Configure la taille préférée de ce composant.
     *
     * On fixe la hauteur préférée à hauteurTotale.
     * La largeur préférée est mise à 0 pour signaler à Swing que ce composant
     * doit prendre toute la largeur disponible de son parent (via son LayoutManager).
     */
    private void appliquerTaillePreferee() {
        // Largeur 0 = le LayoutManager du parent décide de la largeur
        // Hauteur = celle définie par l'utilisateur
        setPreferredSize(new Dimension(0, hauteurTotale));
        setMinimumSize(new Dimension(0, hauteurTotale));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, hauteurTotale));
    }

    /**
     * Propage la hauteur correcte à tous les Ribbon actuellement enregistrés.
     *
     * Parcourt tous les onglets du HTabbedPane et ajuste la hauteur de chaque
     * Ribbon trouvé dans le contenu des onglets.
     */
    private void propagerHauteurAuxRibbons() {
        int hauteurRibbon = calculerHauteurRibbon();
        int nombreOnglets = tabbedPane.getTabCount();

        for (int i = 0; i < nombreOnglets; i++) {
            Component contenu = tabbedPane.getComponentAt(i);

            // Vérifier que le contenu est bien un Ribbon avant de le modifier
            if (contenu instanceof Ribbon) {
                Ribbon ribbon = (Ribbon) contenu;

                // Appliquer la hauteur calculée au Ribbon
                appliquerTailleAuRibbon(ribbon, hauteurRibbon);
            }
        }
    }

    /**
     * Applique la hauteur calculée à un Ribbon spécifique.
     *
     * On configure les tailles préférée, minimale et maximale pour que le
     * Ribbon occupe toujours toute la largeur disponible et exactement
     * la hauteur qui lui est allouée.
     *
     * @param ribbon       le Ribbon à redimensionner
     * @param hauteurRibbon hauteur à appliquer en pixels
     */
    private void appliquerTailleAuRibbon(Ribbon ribbon, int hauteurRibbon) {
        // Largeur Integer.MAX_VALUE = le Ribbon s'étire sur toute la largeur
        ribbon.setPreferredSize(new Dimension(Integer.MAX_VALUE, hauteurRibbon));
        ribbon.setMinimumSize(new Dimension(0, hauteurRibbon));
        ribbon.setMaximumSize(new Dimension(Integer.MAX_VALUE, hauteurRibbon));

        // Propager la hauteur interne au Ribbon pour ses propres calculs de layout
        ribbon.setRibbonHeight(hauteurRibbon);
    }

    // =========================================================================
    // API PUBLIQUE — GESTION DES ONGLETS
    // =========================================================================

    /**
     * Ajoute un onglet vide avec un titre.
     * Une icône colorée est attribuée automatiquement depuis le cycle de couleurs
     * du HTabbedPane. Le contenu de l'onglet reste vide jusqu'à l'appel de
     * addRibbon().
     *
     * @param titre titre affiché sur l'onglet
     */
    public void addTab(String titre) {
        // JPanel vide transparent comme placeholder jusqu'à addRibbon()
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);

        // Déléguer à HTabbedPane qui gère l'icône automatique
        tabbedPane.addTab(titre, placeholder);
    }

    /**
     * Ajoute un onglet vide avec un titre et une couleur d'icône explicite.
     *
     * @param titre  titre affiché sur l'onglet
     * @param couleur couleur de l'icône ronde de l'onglet
     */
    public void addTab(String titre, Color couleur) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);

        // Déléguer à HTabbedPane avec la couleur choisie
        tabbedPane.addTab(titre, couleur, placeholder);
    }

    /**
     * Ajoute un onglet vide avec un titre et une icône fournie explicitement.
     *
     * @param titre titre affiché sur l'onglet
     * @param icone icône à afficher à côté du titre
     */
    public void addTab(String titre, Icon icone) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        tabbedPane.addTab(titre, icone, placeholder);
    }

    // =========================================================================
    // API PUBLIQUE — ASSOCIATION DES RIBBON
    // =========================================================================

    /**
     * Associe un Ribbon à un onglet identifié par son titre.
     *
     * Cette méthode retrouve l'index de l'onglet via son titre, puis délègue
     * à addRibbon(int, Ribbon) qui contient toute la logique réelle.
     *
     * Si le titre ne correspond à aucun onglet existant, un avertissement
     * est affiché et rien n'est fait.
     *
     * @param titre  titre de l'onglet cible
     * @param ribbon le Ribbon à injecter dans cet onglet
     */
    public void addRibbon(String titre, Ribbon ribbon) {
        // Retrouver l'index de l'onglet via son titre (méthode native JTabbedPane)
        int index = tabbedPane.indexOfTab(titre);

        if (index == -1) {
            // L'onglet n'existe pas : avertir sans planter
            System.err.println("HRibbonTabs.addRibbon : aucun onglet avec le titre \"" + titre + "\"");
            return;
        }

        // Déléguer à la méthode par index qui contient la logique réelle
        addRibbon(index, ribbon);
    }

    /**
     * Associe un Ribbon à un onglet identifié par son index.
     *
     * C'est la méthode centrale de HRibbonTabs. Elle :
     * 1. Vérifie que l'index est valide
     * 2. Applique la hauteur correcte au Ribbon
     * 3. Remplace le placeholder par le Ribbon dans le HTabbedPane
     *
     * @param indexOnglet index de l'onglet cible (0-based)
     * @param ribbon      le Ribbon à injecter dans cet onglet
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public void addRibbon(int indexOnglet, Ribbon ribbon) {
        // Vérifier que l'index est dans les bornes valides
        if (indexOnglet < 0 || indexOnglet >= tabbedPane.getTabCount()) {
            throw new IndexOutOfBoundsException(
                "HRibbonTabs.addRibbon : index invalide " + indexOnglet +
                " (nombre d'onglets : " + tabbedPane.getTabCount() + ")"
            );
        }

        if (ribbon == null) {
            throw new IllegalArgumentException("HRibbonTabs.addRibbon : le Ribbon ne peut pas être null");
        }

        // Calculer et appliquer la hauteur correcte au Ribbon
        appliquerTailleAuRibbon(ribbon, calculerHauteurRibbon());

        // Remplacer le placeholder (ou l'ancien Ribbon) par le nouveau Ribbon
        // setComponentAt() est la méthode de JTabbedPane pour remplacer le contenu
        tabbedPane.setComponentAt(indexOnglet, ribbon);

        // Forcer le recalcul de l'affichage
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    // =========================================================================
    // API PUBLIQUE — ACCÈS AUX COMPOSANTS
    // =========================================================================

    /**
     * Retourne le Ribbon associé à l'onglet identifié par son titre.
     * Retourne null si l'onglet n'existe pas ou ne contient pas de Ribbon.
     *
     * @param titre titre de l'onglet recherché
     * @return le Ribbon de cet onglet, ou null
     */
    public Ribbon getRibbon(String titre) {
        int index = tabbedPane.indexOfTab(titre);
        if (index == -1) return null;
        return getRibbon(index);
    }

    /**
     * Retourne le Ribbon associé à l'onglet à l'index donné.
     * Retourne null si l'onglet ne contient pas encore de Ribbon.
     *
     * @param indexOnglet index de l'onglet (0-based)
     * @return le Ribbon de cet onglet, ou null
     */
    public Ribbon getRibbon(int indexOnglet) {
        if (indexOnglet < 0 || indexOnglet >= tabbedPane.getTabCount()) return null;

        Component contenu = tabbedPane.getComponentAt(indexOnglet);

        // Vérifier que le contenu est bien un Ribbon (pas un placeholder)
        if (contenu instanceof Ribbon) {
            return (Ribbon) contenu;
        }

        return null;
    }

    /**
     * Retourne le nombre d'onglets actuellement présents.
     *
     * @return nombre d'onglets
     */
    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    /**
     * Retourne le HTabbedPane interne.
     * Permet un accès avancé au TabbedPane pour des configurations spécifiques
     * (style, animations, placement des onglets, etc.).
     *
     * @return le HTabbedPane interne
     */
    public HRibbonTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    /**
     * Retourne l'index de l'onglet actuellement sélectionné.
     *
     * @return index de l'onglet sélectionné, ou -1 si aucun
     */
    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }

    /**
     * Sélectionne l'onglet à l'index donné.
     *
     * @param index index de l'onglet à sélectionner
     */
    public void setSelectedIndex(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    // =========================================================================
    // SURCHARGE DE getPreferredSize
    // =========================================================================

    /**
     * Retourne la taille préférée du composant.
     *
     * La largeur est toujours celle du parent (le composant prend toute la
     * largeur disponible). La hauteur est celle définie par l'utilisateur.
     *
     * @return la dimension préférée
     */
    @Override
    public Dimension getPreferredSize() {
        // Si une taille a été définie explicitement par l'utilisateur, la respecter
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }

        // Calculer la largeur depuis le parent
        Container parent = getParent();
        int largeur = (parent != null) ? parent.getWidth() : 800;

        return new Dimension(largeur, hauteurTotale);
    }
}
