package hsplitpane;

import hsplitpane.*;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Implémentation par défaut du UI delegate de HSplitPane.
 *
 */
public class BasicHSplitPaneUI extends HSplitPaneUI {

    /**
     * Point d'entrée attendu par UIManager.getUI() : instancie ce delegate par
     * réflexion à partir du nom de classe enregistré pour "HSplitPaneUI"
     * (voir HSplitPane.updateUI()). Signature imposée par la convention Swing.
     */
    public static ComponentUI createUI(JComponent c) {
        return new BasicHSplitPaneUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        if (!(c instanceof HSplitPane)) {
            throw new IllegalArgumentException(
                    "BasicHSplitPaneUI ne peut être installé que sur un HSplitPane.");
        }
        HSplitPane pane = (HSplitPane) c;

        installDefaults(pane);
        installZonesAndDividers(pane);
        installLayout(pane);
        installZoneReferences(pane);
    }

    @Override
    public void uninstallUI(JComponent c) {
        HSplitPane pane = (HSplitPane) c;
        uninstallZoneReferences(pane);
        pane.setLayout(null);
        super.uninstallUI(c);
    }

    /** Anciennement HSplitPane.assembleComponents() — partie "apparence". */
    private void installDefaults(HSplitPane pane) {
        pane.setOpaque(true);
        pane.setBackground(new Color(30, 30, 30));
    }

    /** Anciennement HSplitPane.initializeComponents() + la partie attach() d'assembleComponents(). */
    private void installZonesAndDividers(HSplitPane pane) {
        HSplitPaneConfig config = pane.getConfig();

        pane.zoneNorth = new HSplitZone(ZonePosition.NORTH, null, config.getNorthSize());
        pane.zoneSouth = new HSplitZone(ZonePosition.SOUTH, null, config.getSouthSize());
        pane.zoneWest = new HSplitZone(ZonePosition.WEST, null, config.getWestSize());
        pane.zoneEast = new HSplitZone(ZonePosition.EAST, null, config.getEastSize());

        if (config.isShowCenter()) {
            pane.zoneCenter = new HSplitZone(ZonePosition.CENTER, null, config.getCenterSize());
        }

        pane.northDivider = new HSplitDivider(WrapDirection.HORIZONTAL);
        pane.southDivider = new HSplitDivider(WrapDirection.HORIZONTAL);
        pane.westDivider = new HSplitDivider(WrapDirection.VERTICAL);
        pane.eastDivider = new HSplitDivider(WrapDirection.VERTICAL);

        // Passe par attachInternalComponent() et non add() : add(Component) est
        // fermé sur HSplitPane pour le code utilisateur (voir HSplitPane), le
        // delegate a besoin d'un accès réservé pour attacher ses propres enfants.
        pane.attachInternalComponent(pane.zoneNorth);
        pane.attachInternalComponent(pane.northDivider);
        pane.attachInternalComponent(pane.zoneWest);
        pane.attachInternalComponent(pane.westDivider);
        if (pane.zoneCenter != null) {
            pane.attachInternalComponent(pane.zoneCenter);
        }
        pane.attachInternalComponent(pane.eastDivider);
        pane.attachInternalComponent(pane.zoneEast);
        pane.attachInternalComponent(pane.southDivider);
        pane.attachInternalComponent(pane.zoneSouth);
    }

    /** Anciennement la partie "rootLayout" de HSplitPane.assembleComponents(). */
    private void installLayout(HSplitPane pane) {
        pane.rootLayout = new HSplitPaneRootLayout();
        pane.setLayout(pane.rootLayout);
        pane.rootLayout.saveZones(pane.zoneNorth, pane.zoneSouth, pane.zoneWest, pane.zoneCenter, pane.zoneEast);
        pane.rootLayout.saveDividers(pane.northDivider, pane.southDivider, pane.westDivider, pane.eastDivider);
    }

    /** Anciennement HSplitPane.enregistrerReferences(). */
    private void installZoneReferences(HSplitPane pane) {
        pane.zoneNorth.setSplitPaneRef(pane);
        pane.zoneSouth.setSplitPaneRef(pane);
        pane.zoneWest.setSplitPaneRef(pane);
        pane.zoneEast.setSplitPaneRef(pane);
        if (pane.zoneCenter != null) {
            pane.zoneCenter.setSplitPaneRef(pane);
        }
    }

    /** Symétrique d'installZoneReferences(), pour uninstallUI(). */
    private void uninstallZoneReferences(HSplitPane pane) {
        if (pane.zoneNorth != null) {
            pane.zoneNorth.setSplitPaneRef(null);
        }
        if (pane.zoneSouth != null) {
            pane.zoneSouth.setSplitPaneRef(null);
        }
        if (pane.zoneWest != null) {
            pane.zoneWest.setSplitPaneRef(null);
        }
        if (pane.zoneEast != null) {
            pane.zoneEast.setSplitPaneRef(null);
        }
        if (pane.zoneCenter != null) {
            pane.zoneCenter.setSplitPaneRef(null);
        }
    }
}