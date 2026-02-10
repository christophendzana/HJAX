package rubban.layout;

/**
 * Niveaux de réduction possibles pour un groupe du ruban.
 * 
 * DESIGN EXTENSIBLE :
 * Cette énumération est conçue pour être facilement étendue.
 * De nouveaux niveaux intermédiaires peuvent être ajoutés entre NORMAL et COLLAPSED 
 * 
 * NIVEAUX ACTUELS :
 * - NORMAL    : Affichage complet avec tous les composants visibles
 * - COLLAPSED : Réduit à un JComboBox avec header masqué
 * 
 * @author FIDELE
 * @version 1.0
 */
public enum CollapseLevel {
    
    NORMAL(0, "Affichage normal"),
    COLLAPSED(999, "Réduit");
    
    private final int level;
    private final String description;
    
    CollapseLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }
    
    public int getLevel() { return level; }
    public String getDescription() { return description; }
    
    public boolean isMoreCollapsedThan(CollapseLevel other) {
        return this.level > other.level;
    }
    
    public boolean isLessCollapsedThan(CollapseLevel other) {
        return this.level < other.level;
    }
    
    public CollapseLevel getNextMoreCollapsed() {
        return (this == NORMAL) ? COLLAPSED : COLLAPSED;
    }
    
    public CollapseLevel getNextLessCollapsed() {
        return (this == COLLAPSED) ? NORMAL : NORMAL;
    }
    
    public boolean isNormal() { return this == NORMAL; }
    public boolean isCollapsed() { return this == COLLAPSED; }
    
    @Override
    public String toString() {
        return description + " (niveau " + level + ")";
    }
}
