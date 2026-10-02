package hsupertable.menu;

import javax.swing.JComponent;
import hcomponents.HMenuItem;

/**
 * Fonctionnalité portant sur le Header
 * @author FIDELE
 */
public abstract class HeaderAction {

    private final String name;

    public HeaderAction(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract boolean isVisible(HeaderContext ctx);
    public abstract boolean isEnabled(HeaderContext ctx);
    public abstract void perform(HeaderContext ctx);

    public JComponent buildMenuItem(HeaderContext ctx) {
        HMenuItem item = new HMenuItem(getName());
        item.setEnabled(isEnabled(ctx));
        item.addActionListener(e -> perform(ctx));
        return item;
    }
}