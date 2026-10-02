package hsupertable.menu;


/**
 * Ajouter une action au menu contextuel
 * @author FIDELE
 */
public abstract class ContextAction {

    private final String name;

    public ContextAction(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract boolean isVisible(TableContext ctx);
    public abstract boolean isEnabled(TableContext ctx);
    public abstract void perform(TableContext ctx);
}