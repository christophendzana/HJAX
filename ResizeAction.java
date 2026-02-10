package rubban.layout;

/**
 * Action de redimensionnement à appliquer à un groupe.
 * 
 * DESIGN PATTERN : DATA TRANSFER OBJECT (DTO)
 * Cette classe est un simple conteneur de données immuable
 * qui décrit une action de collapse ou expand à effectuer.
 * 
 * UTILISATION :
 * ResizeManager calcule une liste de ResizeAction,
 * puis HRibbonLayoutManager les applique séquentiellement.
 * 
 * @author FIDELE
 * @version 1.0
 */
public final class ResizeAction {
    
    /** Index du groupe concerné */
    private final int groupIndex;
    
    /** true = collapse, false = expand */
    private final boolean collapse;
    
    /**
     * Constructeur
     * 
     * @param groupIndex index du groupe (0-based)
     * @param collapse true pour collapser, false pour expander
     */
    public ResizeAction(int groupIndex, boolean collapse) {
        this.groupIndex = groupIndex;
        this.collapse = collapse;
    }
    
    /**
     * Retourne l'index du groupe
     * 
     * @return index du groupe
     */
    public int getGroupIndex() {
        return groupIndex;
    }
    
    /**
     * Vérifie si c'est une action de collapse
     * 
     * @return true si collapse
     */
    public boolean isCollapse() {
        return collapse;
    }
    
    /**
     * Vérifie si c'est une action d'expand
     * 
     * @return true si expand
     */
    public boolean isExpand() {
        return !collapse;
    }
    
    @Override
    public String toString() {
        return (collapse ? "COLLAPSE" : "EXPAND") + " group " + groupIndex;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResizeAction)) return false;
        ResizeAction other = (ResizeAction) obj;
        return this.groupIndex == other.groupIndex && this.collapse == other.collapse;
    }
    
    @Override
    public int hashCode() {
        return 31 * groupIndex + (collapse ? 1 : 0);
    }
}
