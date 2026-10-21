package hsplitpane;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import java.awt.*;

/**
 * Implémentation par défaut du UI delegate de HSplitPane : apparence et layout.
 * Les zones et leur contenu appartiennent à HSplitPane, pas au delegate.
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
        installLayout(pane);
    }

    @Override
    public void uninstallUI(JComponent c) {
        c.setLayout(null);
        super.uninstallUI(c);
    }

    /**
     * Apparence par défaut, sans écraser ce que le développeur a lui-même défini :
     * un UIResource est une valeur posée par le L&F, donc remplaçable ; une couleur
     * "normale" est celle de l'utilisateur et doit être conservée.
     */
    private void installDefaults(HSplitPane pane) {
        LookAndFeel.installProperty(pane, "opaque", Boolean.TRUE);

        // isBackgroundSet() : getBackground() renverrait celui du parent si rien n'est posé
        if (!pane.isBackgroundSet() || pane.getBackground() instanceof UIResource) {
            pane.setBackground(new ColorUIResource(30, 30, 30));
        }
    }

    /** Crée le layout racine et lui donne les zones et séparateurs du pane. */
    private void installLayout(HSplitPane pane) {
        HSplitPaneRootLayout layout = new HSplitPaneRootLayout();
        pane.setLayout(layout);
        layout.saveZones(pane.getZone(ZonePosition.NORTH), pane.getZone(ZonePosition.SOUTH),
                pane.getZone(ZonePosition.WEST), pane.getZone(ZonePosition.CENTER),
                pane.getZone(ZonePosition.EAST));
        layout.saveDividers(pane.getDivider(ZonePosition.NORTH), pane.getDivider(ZonePosition.SOUTH),
                pane.getDivider(ZonePosition.WEST), pane.getDivider(ZonePosition.EAST));
    }
}