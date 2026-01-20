/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

import javax.swing.JComponent;
import javax.swing.CellRendererPane;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

/**
 * HRuban est le composant Swing principal.
 * HRuban :
 *  - interroge le modèle
 *  - interroge le layout
 *  - interroge le renderer
 *  - délègue la peinture à Swing
 */
public class HRibbon extends JComponent {

        
    /**
     * @see #getUIClassID
     * @see #readObject
     */
    private static final String uiClassID = "RibbonUI";

    /** Do not adjust column widths automatically; use a horizontal scrollbar instead. */
    public static final int     AUTO_RESIZE_OFF = 0;

    /** When a column is adjusted in the UI, adjust the next column the opposite way. */
    public static final int     AUTO_RESIZE_NEXT_GROUP= 1;

    /** During UI adjustment, change subsequent columns to preserve the total width;
      * this is the default behavior. */
    public static final int     AUTO_RESIZE_SUBSEQUENT_GROUPS = 2;

    /** During all resize operations, apply adjustments to the last column only. */
    public static final int     AUTO_RESIZE_LAST_GROUP= 3;

    /** During all resize operations, proportionately resize all columns. */
    public static final int     AUTO_RESIZE_ALL_GROUPS = 4;
    
    /** The <code>TableModel</code> of the table. */
    protected HRibbonModel        model;

    /** The <code>TableColumnModel</code> of the table. */
    protected HRibbonGroupModel  groupModel;

    /** The <code>ListSelectionModel</code> of the table, used to keep track of row selections. */
    protected ListSelectionModel selectionModel;
    
    /**
     
     */

    /**
     * Layout spécifique au ruban.
     * Il est responsable du calcul :
     * - des tailles
     * - des positions
     */
    private HRibbonLayoutManager layout;

    /**
     * CellRendererPane est un composant Swing spécial.
     * Il permet de peindre des composants Swing
     * sans les ajouter réellement dans la hiérarchie.
     */
    private CellRendererPane rendererPane;

    protected HRibbonFooter footer;
    
    /** The table draws vertical lines between cells if <code>showVerticalLines</code> is true. */
    protected boolean           showVerticalLines;

    /**
     *  Determines if the ribbon automatically resizes the
     *  width of the table's columns to take up the entire width of the
     *  table, and how it does the resizing.
     */
    protected int               autoResizeMode;

    /**
     *  The table will query the <code>TableModel</code> to build the default
     *  set of columns if this is true.
     */
    protected boolean           autoCreateGroupsFromModel;

    /** Used by the <code>Scrollable</code> interface to determine the initial visible area. */
    protected Dimension         preferredViewportSize;
    
        /** Identifies the column of the cell being edited. */
    protected transient int             editingColumn;
    
    /**
     * A table of objects that display the contents of a cell,
     * indexed by class as declared in <code>getColumnClass</code>
     * in the <code>TableModel</code> interface.
     */
    protected transient Hashtable<Object, Object> defaultRenderersByGroupClass;
    
    private boolean dragEnabled;
    
    /**
     * Flag to indicate UI update is in progress
     */
    private transient boolean updateInProgress;
    
       /**
     * Whether or not the table always fills the viewport height.
     * @see #setFillsViewportHeight
     * @see #getScrollableTracksViewportHeight
     */
    private boolean fillsViewportHeight;

    /**
     * The last value of getValueIsAdjusting from the column selection models
     * columnSelectionChanged notification. Used to test if a repaint is
     * needed.
     */
    private boolean columnSelectionAdjusting;
    
    private PropertyChangeListener editorRemover = null;

    public HRibbon(){
        this(null, null, null);
    }
    
    /**
     * @param model    modèle de données
     * @param groupModel
     */
    public HRibbon(HRibbonModel model, HRibbonGroupModel groupModel) {

        this(model, groupModel, null);
    }
       
    public HRibbon(HRibbonModel ribbonModel, HRibbonGroupModel groupModel, ListSelectionModel selectionModel){
        super();
        
        if (ribbonModel == null) {
            ribbonModel = createDefaultHRibbonModel();
        }
        
        if (groupModel == null) {
            groupModel = createDefaultHRibbonGroupModel();
        }
        
        if (selectionModel == null) {
            selectionModel = createDefaultListSelectionModel();
        }
        
        setLayout(createDefaultLayoutManager());
    } 
    
    public HRibbon(int numGroup){        
    }
    
    public HRibbon( Vector<?> groupIdentifiers){
        
    }
    
    public HRibbon( Object[] groupIdentifiers){
        
    }
    
   
     protected LayoutManager createDefaultLayoutManager(){
       return new HRibbonLayoutManager(this);
   }
     
     protected HRibbonGroupModel createDefaultHRibbonGroupModel(){
         return new DefaultHRibbonGroupModel();
     }
     
     protected HRibbonModel createDefaultHRibbonModel(){
      return new DefaultHRibbonModel();   
     }
     
     protected ListSelectionModel createDefaultListSelectionModel(){
         return new DefaultListSelectionModel();
     }
    
          
    /**
     * Taille préférée du HRuban.
     *
     * HRuban ne calcule rien lui-même.
     * Il délègue entièrement cette responsabilité au layout.
     */
    @Override
    public Dimension getPreferredSize() {
        // Le layout connaît la taille totale du ruban
        return null;
    }
    
    /**
     * Accès au modèle.     
     * Utile pour :
     * - les listeners
     * - les composants externes
     */
    public HRibbonModel getModel() {
        return model;
    }
    
    public HRibbonLayoutManager getRubanLayout() {
        return layout;
    }
}