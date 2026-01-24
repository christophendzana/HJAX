package hcomponents.HRibbon;

import java.util.EventObject;

/**
 * Événement décrivant un changement dans le modèle du ruban.
 * Analogie : similaire à TableModelEvent pour JTable.
 */
public class HRibbonModelEvent extends EventObject {
    
    /** Type de changement : insertion */
    public static final int INSERT = 1;
    
    /** Type de changement : mise à jour */
    public static final int UPDATE = 0;
    
    /** Type de changement : suppression */
    public static final int DELETE = -1;
    
    /** Type de changement : déplacement */
    public static final int MOVE = 2;
    
    private final int type;
    private final int groupIndex;
    private final int position;
    private final int toPosition; // Pour MOVE
    
    /**
     * Constructeur pour changement global (toutes données).
     */
    public HRibbonModelEvent(Object source) {
        this(source, -1, -1, -1, UPDATE);
    }
    
    /**
     * Constructeur pour changement sur un groupe spécifique.
     */
    public HRibbonModelEvent(Object source, int groupIndex) {
        this(source, groupIndex, -1, -1, UPDATE);
    }
    
    /**
     * Constructeur pour changement sur une valeur spécifique.
     */
    public HRibbonModelEvent(Object source, int groupIndex, int position) {
        this(source, groupIndex, position, -1, UPDATE);
    }
    
    /**
     * Constructeur complet.
     */
    public HRibbonModelEvent(Object source, int groupIndex, int position, int toPosition, int type) {
        super(source);
        this.groupIndex = groupIndex;
        this.position = position;
        this.toPosition = toPosition;
        this.type = type;
    }
    
    // Getters
    public int getType() { return type; }
    public int getGroupIndex() { return groupIndex; }
    public int getPosition() { return position; }
    public int getToPosition() { return toPosition; }
    
    /**
     * Vérifie si l'événement concerne toutes les données.
     */
    public boolean isGlobalChange() {
        return groupIndex == -1;
    }
    
    /**
     * Vérifie si l'événement concerne un groupe entier.
     */
    public boolean isGroupChange() {
        return groupIndex >= 0 && position == -1;
    }
    
    /**
     * Vérifie si l'événement concerne une valeur spécifique.
     */
    public boolean isValueChange() {
        return groupIndex >= 0 && position >= 0;
    }
    
    /**
     * Vérifie si l'événement est un déplacement.
     */
    public boolean isMove() {
        return type == MOVE;
    }
}