package hsplitpane;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Conteneur multi-zones.
 *
 * HSplitPane divise son espace en cinq zones positionnelles : NORTH, SOUTH,
 * WEST, CENTER et EAST.
 *
 * ============================================================================
 * ARCHITECTURE — UI delegate, sur le modèle de JSplitPane
 * ============================================================================
 * Ce composant ne construit plus lui-même son squelette (zones, dividers,
 * layout). Comme JSplitPane délègue à un SplitPaneUI/BasicSplitPaneUI, cette
 * classe délègue à un HSplitPaneUI/BasicHSplitPaneUI. HSplitPane ne conserve
 * que ce qui est indépendant du rendu : l'API publique (add/remove, fullscreen)
 * et l'état structurel (les champs zoneXxx/xxxDivider), remplis PAR le delegate
 * à l'installation, jamais construits ici directement.
 *
 * Ce que ça permet concrètement : remplacer BasicHSplitPaneUI par une autre
 * implémentation (UIManager.put("HSplitPaneUI", "mon.AutreImpl")) sans changer
 * une seule ligne de HSplitPane ni du code qui l'utilise.
 *
 * ============================================================================
 * add()/remove() sont fermés et redéfinis
 * ============================================================================
 * HSplitPane a une composition interne FIXE (5 zones + 4 dividers, gérés par
 * lui-même) — contrairement à un JPanel générique où l'utilisateur empile ce
 * qu'il veut. Les surcharges add()/remove() héritées de Container restaient
 * sinon utilisables et produisaient un résultat cassé en silence (le composant
 * devient un enfant du HSplitPane sans jamais être positionné par son layout).
 * Elles sont donc bloquées ; add(Component, ZonePosition) est le seul chemin
 * public pour ajouter du contenu. Le UI delegate, qui a besoin d'attacher
 * lui-même les zones/dividers en interne, passe par attachInternalComponent()
 * qui contourne ce blocage via super.add().
 */
public class HSplitPane extends JPanel {

    // -------------------------------------------------------------------------
    // Zones — package-private : remplies par le UI delegate (BasicHSplitPaneUI),
    // jamais construites par HSplitPane lui-même.
    // -------------------------------------------------------------------------
    HSplitZone zoneNorth;
    HSplitZone zoneSouth;
    HSplitZone zoneWest;
    HSplitZone zoneCenter;
    HSplitZone zoneEast;

    // -------------------------------------------------------------------------
    // Séparateurs — idem, remplis par le UI delegate.
    // -------------------------------------------------------------------------
    HSplitDivider northDivider;
    HSplitDivider southDivider;
    HSplitDivider westDivider;
    HSplitDivider eastDivider;

    // -------------------------------------------------------------------------
    // Layout racine — idem.
    // -------------------------------------------------------------------------
    HSplitPaneRootLayout rootLayout;

    /**
     * Config lue par le UI delegate à l'installation (tailles initiales des
     * zones).
     */
    private final HSplitPaneConfig config;

    // =========================================================================
    // Constructeurs
    // =========================================================================
    public HSplitPane() {
        this(new HSplitPaneConfig());
    }

    public HSplitPane(HSplitPaneConfig config) {
        this.config = (config != null) ? config : new HSplitPaneConfig();
        updateUI();
    }

    // =========================================================================
    // UI delegate — mécanique, sur le modèle exact de JComponent/JSplitPane
    // =========================================================================
    private static final String uiClassID = "HSplitPaneUI";

    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Installe le delegate courant (celui enregistré dans UIManager pour
     * "HSplitPaneUI", ou BasicHSplitPaneUI par défaut si rien n'est
     * enregistré).
     *
     * ATTENTION — JPanel appelle déjà updateUI() lui-même à la fin de SON
     * propre constructeur (super()), avant que le corps du constructeur de
     * HSplitPane n'ait pu assigner `config`. Cet appel prématuré doit être
     * ignoré ; c'est l'appel explicite fait en fin de constructeur de
     * HSplitPane, une fois `config` assigné, qui déclenche la vraie
     * installation. (JComponent nu n'a pas ce problème — il n'appelle jamais
     * updateUI() lui-même, c'est justement ce qui manquait à JPanel ici.)
     */
    @Override
    public void updateUI() {
        if (config == null) {
            return;
        }
        if (UIManager.get(getUIClassID()) == null) {
            UIManager.put(getUIClassID(), "hsplitpane.view.BasicHSplitPaneUI");
        }
        setUI((HSplitPaneUI) UIManager.getUI(this));
    }

    public void setUI(HSplitPaneUI ui) {
        super.setUI(ui);
    }

    public HSplitPaneUI getUI() {
        return (HSplitPaneUI) ui;
    }

    /**
     * Lu par le UI delegate à l'installation — jamais par du code utilisateur.
     */
    public HSplitPaneConfig getConfig() {
        return config;
    }

    /**
     * Point de passage réservé au UI delegate pour attacher les zones/dividers
     * comme vrais enfants Swing du HSplitPane, en contournant le blocage de
     * add(Component) ci-dessous (qui, lui, reste fermé pour le code
     * utilisateur).
     */
    void attachInternalComponent(Component comp) {
        super.add(comp);
    }

    // =========================================================================
    // add()/remove() hérités de Container — fermés, voir le commentaire de
    // classe. Un seul chemin reste ouvert pour le contenu : add(Component,
    // ZonePosition) plus bas.
    // =========================================================================
    @Override
    public Component add(Component comp) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez add(Component, ZonePosition).");
    }

    @Override
    public Component add(Component comp, int index) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez add(Component, ZonePosition).");
    }

    @Override
    public void add(Component comp, Object constraints) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez add(Component, ZonePosition).");
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez add(Component, ZonePosition).");
    }

    @Override
    public Component add(String name, Component comp) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez add(Component, ZonePosition).");
    }

    @Override
    public void remove(Component comp) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez remove(Component, ZonePosition).");
    }

    @Override
    public void remove(int index) {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes). "
                + "Utilisez remove(Component, ZonePosition).");
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException(
                "HSplitPane gère sa composition en interne (5 zones fixes).");
    }

    // =========================================================================
    // API publique — ajout et retrait de composants (seul chemin ouvert)
    // =========================================================================
    /**
     * Ajoute un composant dans la zone spécifiée.
     *
     * @param component le composant à ajouter
     * @param position la zone cible
     */
    public void add(Component component, ZonePosition position) {
        if (position == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }
        HSplitZone zone = getZone(position);
        if (zone == null) {
            System.err.println("HSplitPane : la zone " + position
                    + " n'est pas disponible dans cette instance.");
            return;
        }
        zone.addContainer(component);
        revalidate();
        repaint();
    }

    /**
     * Retire un composant de la zone spécifiée.
     *
     * @param component le composant à retirer
     * @param position la zone dont il faut le retirer
     */
    public void remove(Component component, ZonePosition position) {
        HSplitZone zone = getZone(position);
        if (zone != null) {
            zone.removeContainer(component);
            revalidate();
            repaint();
        }
    }

    // =========================================================================
    // API publique — fullscreen
    //
    // Seule opération conservée dans la façade au-delà de add/remove, car
    // c'est la seule qui a réellement besoin de connaître ET coordonner
    // TOUTES les zones en même temps.
    // =========================================================================
    public void enterFullScreen(ZonePosition position) {
        HSplitZone dejaEnFullScreen = findFullScreenZone();
        if (dejaEnFullScreen != null && dejaEnFullScreen.getPosition() != position) {
            exitFullScreen();
        }

        HSplitZone zone = getZone(position);
        if (zone == null || zone.isEmpty()) {
            return;
        }

        for (ZonePosition pos : ZonePosition.values()) {
            if (pos == position) {
                continue;
            }
            HSplitZone z = getZone(pos);
            if (z != null) {
                z.setVisible(false);
            }
        }

        zone.setFullScreenState(true);

        int totalW = getWidth();
        int totalH = getHeight();
        zone.animerVersBounds(0, 0, totalW, totalH);

        revalidate();
        repaint();
    }

    public void exitFullScreen() {
        HSplitZone zonePleinEcran = findFullScreenZone();
        if (zonePleinEcran == null) {
            return;
        }

        zonePleinEcran.setFullScreenState(false);

        for (ZonePosition pos : ZonePosition.values()) {
            if (pos == zonePleinEcran.getPosition()) {
                continue;
            }
            HSplitZone z = getZone(pos);
            if (z != null) {
                z.setVisible(true);
            }
        }

        revalidate();
        repaint();
    }

    public boolean isZoneFullScreen(ZonePosition position) {
        HSplitZone zone = getZone(position);
        return zone != null && zone.isFullScreen();
    }

    private HSplitZone findFullScreenZone() {
        for (ZonePosition pos : ZonePosition.values()) {
            HSplitZone z = getZone(pos);
            if (z != null && z.isFullScreen()) {
                return z;
            }
        }
        return null;
    }

    // =========================================================================
    // Méthodes utilitaires internes
    // =========================================================================
    /**
     * Retourne la zone à la position donnée.
     *
     * @param position la zone recherchée
     * @return la zone correspondante, ou null si cette instance n'en a pas à
     * cette position (ex: CENTER si showCenter=false)
     */
    public HSplitZone getZone(ZonePosition position) {
        return switch (position) {
            case NORTH ->
                zoneNorth;
            case SOUTH ->
                zoneSouth;
            case WEST ->
                zoneWest;
            case CENTER ->
                zoneCenter;
            case EAST ->
                zoneEast;
        };
    }

    /**
     * Retourne le séparateur associé à la position donnée.
     *
     * @param position la zone dont on veut le séparateur (CENTER n'en a pas)
     * @return le séparateur correspondant, ou null pour CENTER
     */
    public HSplitDivider getDivider(ZonePosition position) {
        return switch (position) {
            case NORTH ->
                northDivider;
            case SOUTH ->
                southDivider;
            case WEST ->
                westDivider;
            case EAST ->
                eastDivider;
            case CENTER ->
                null;
        };
    }

}
