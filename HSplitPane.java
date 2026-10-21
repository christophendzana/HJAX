package hsplitpane;

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Conteneur multi-zones.
 * HSplitPane divise son espace en cinq zones positionnelles : NORTH, SOUTH,
 * WEST, CENTER et EAST.
 */
public class HSplitPane extends JPanel {

    // Zones et séparateurs : créés une seule fois dans le constructeur. Ils portent le
    // contenu de l'utilisateur, donc ils doivent survivre à un changement de UI delegate.
    private final Map<ZonePosition, HSplitZone> zones = new EnumMap<>(ZonePosition.class);

    // Pas d'entrée pour CENTER : il n'a pas de séparateur
    private final Map<ZonePosition, HSplitDivider> dividers = new EnumMap<>(ZonePosition.class);

    // =========================================================================
    // Constructeurs
    // =========================================================================
    public HSplitPane() {
        this(new HSplitPaneConfig());
    }

    public HSplitPane(HSplitPaneConfig config) {
        createStructure(config != null ? config : new HSplitPaneConfig());
        updateUI();
    }

    /**
     * Crée les zones et les séparateurs d'après la config et les attache comme
     * enfants Swing. L'ordre d'ajout fixe l'ordre Z. Appelée une seule fois.
     */
    private void createStructure(HSplitPaneConfig config) {
        zones.put(ZonePosition.NORTH, new HSplitZone(ZonePosition.NORTH, null, config.getNorthSize()));
        zones.put(ZonePosition.SOUTH, new HSplitZone(ZonePosition.SOUTH, null, config.getSouthSize()));
        zones.put(ZonePosition.WEST, new HSplitZone(ZonePosition.WEST, null, config.getWestSize()));
        zones.put(ZonePosition.EAST, new HSplitZone(ZonePosition.EAST, null, config.getEastSize()));
        if (config.isShowCenter()) {
            zones.put(ZonePosition.CENTER, new HSplitZone(ZonePosition.CENTER, null, config.getCenterSize()));
        }

        dividers.put(ZonePosition.NORTH, new HSplitDivider(WrapDirection.HORIZONTAL));
        dividers.put(ZonePosition.SOUTH, new HSplitDivider(WrapDirection.HORIZONTAL));
        dividers.put(ZonePosition.WEST, new HSplitDivider(WrapDirection.VERTICAL));
        dividers.put(ZonePosition.EAST, new HSplitDivider(WrapDirection.VERTICAL));

        // super.add() : add(Component) est fermé pour le code utilisateur (voir plus bas)
        super.add(zones.get(ZonePosition.NORTH));
        super.add(dividers.get(ZonePosition.NORTH));
        super.add(zones.get(ZonePosition.WEST));
        super.add(dividers.get(ZonePosition.WEST));
        if (zones.containsKey(ZonePosition.CENTER)) {
            super.add(zones.get(ZonePosition.CENTER));
        }
        super.add(dividers.get(ZonePosition.EAST));
        super.add(zones.get(ZonePosition.EAST));
        super.add(dividers.get(ZonePosition.SOUTH));
        super.add(zones.get(ZonePosition.SOUTH));

        for (HSplitZone zone : zones.values()) {
            zone.setFullScreenHandler(this::toggleFullScreen);
        }
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
     * enregistré). Peut être rappelée à tout moment (changement de L&F) : le
     * delegate ne touche ni aux zones ni à leur contenu.
     * ATTENTION — JPanel appelle déjà updateUI() lui-même à la fin de SON
     * propre constructeur (super()), avant que nos champs ne soient initialisés
     * (zones vaut encore null). Cet appel prématuré doit être ignoré ; c'est
     * l'appel explicite fait en fin de constructeur de HSplitPane, une fois la
     * structure créée, qui déclenche la vraie installation.
     */
    @Override
    public void updateUI() {
        if (zones == null) {
            return;
        }
        if (UIManager.get(getUIClassID()) == null) {
            UIManager.put(getUIClassID(), BasicHSplitPaneUI.class.getName());
        }
        setUI((HSplitPaneUI) UIManager.getUI(this));
    }

    public void setUI(HSplitPaneUI ui) {
        super.setUI(ui);
    }

    public HSplitPaneUI getUI() {
        return (HSplitPaneUI) ui;
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
        Objects.requireNonNull(component, "Le composant ne peut pas être null.");
        HSplitZone zone = requireZone(position);
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
        HSplitZone zone = requireZone(position);
        zone.removeContainer(component);
        revalidate();
        repaint();
    }

    // =========================================================================
    // API publique — fullscreen
    //
    // Seule opération conservée dans la façade au-delà de add/remove, car
    // c'est la seule qui a réellement besoin de connaître ET coordonner
    // TOUTES les zones en même temps.
    // =========================================================================
    public void enterFullScreen(ZonePosition position) {
        HSplitZone zone = requireZone(position);

        // Rien à faire : zone vide, déjà en plein écran, ou détachée dans sa fenêtre
        if (zone.isEmpty() || zone.isFullScreen() || zone.isFloating()) {
            return;
        }

        // On ne sort d'un autre plein écran qu'une fois la demande jugée valide
        HSplitZone dejaEnFullScreen = findFullScreenZone();
        if (dejaEnFullScreen != null) {
            exitFullScreen();
        }

        for (HSplitZone z : zones.values()) {
            if (z != zone) {
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

        for (HSplitZone z : zones.values()) {
            if (z != zonePleinEcran) {
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
        for (HSplitZone z : zones.values()) {
            if (z.isFullScreen()) {
                return z;
            }
        }
        return null;
    }

    /** Appelée par une zone quand l'utilisateur clique sur son bouton plein écran. */
    private void toggleFullScreen(HSplitZone zone) {
        if (zone.isFullScreen()) {
            exitFullScreen();
        } else {
            enterFullScreen(zone.getPosition());
        }
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
        Objects.requireNonNull(position, "La position ne peut pas être null.");
        return zones.get(position);
    }

    /**
     * Comme getZone(), mais échoue explicitement au lieu de renvoyer null :
     * position null ou zone absente de cette instance (ex: CENTER avec
     * showCenter=false).
     */
    private HSplitZone requireZone(ZonePosition position) {
        if (position == null) {
            throw new IllegalArgumentException("La position ne peut pas être null.");
        }
        HSplitZone zone = getZone(position);
        if (zone == null) {
            throw new IllegalStateException("La zone " + position
                    + " n'est pas disponible dans cette instance.");
        }
        return zone;
    }

    /**
     * Retourne le séparateur associé à la position donnée.
     *
     * @param position la zone dont on veut le séparateur (CENTER n'en a pas)
     * @return le séparateur correspondant, ou null pour CENTER
     */
    public HSplitDivider getDivider(ZonePosition position) {
        Objects.requireNonNull(position, "La position ne peut pas être null.");
        return dividers.get(position);
    }

}
