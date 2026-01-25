package hcomponents.HRibbon;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.CellRendererPane;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;

/**
 * HRibbon est le composant Swing principal qui affiche un ruban style Office.
 *
 * Fonctionnalités : - Affiche des groupes de composants (comme Word/Excel) -
 * Supporte le multi-ligne dans les groupes - Gère les modèles de données et de
 * présentation - Utilise un LayoutManager personnalisé pour le positionnement
 */
public class HRibbon extends JComponent implements HRibbonModelListener, HRibbonGroupListener {

    /**
     * Identifiant pour le système de Look and Feel.
     */
    private static final String uiClassID = "HRibbonUI";

    /**
     * Constantes pour le mode de redimensionnement automatique. Inspirées de
     * JTable.
     */
    public static final int AUTO_RESIZE_OFF = 0;
    public static final int AUTO_RESIZE_NEXT_GROUP = 1;
    public static final int AUTO_RESIZE_SUBSEQUENT_GROUPS = 2;
    public static final int AUTO_RESIZE_LAST_GROUP = 3;
    public static final int AUTO_RESIZE_ALL_GROUPS = 4;

    /**
     * Modèle de données du ruban. Contient les données affichées dans les
     * groupes.
     */
    protected HRibbonModel model;

    /**
     * Modèle des groupes du ruban. Contient la configuration des groupes
     * (largeurs, positions, etc.).
     */
    protected HRibbonGroupModel groupModel;

    /**
     * Modèle de sélection (pour la sélection future de groupes/composants).
     */
    protected ListSelectionModel selectionModel;

    /**
     * LayoutManager personnalisé pour le ruban. Positionne les groupes et les
     * composants à l'intérieur.
     */
    private HRibbonLayoutManager layout;

    /**
     * Composant pour le rendu des cellules (comme dans JTable). Utilisé pour
     * afficher les composants sans les ajouter à la hiérarchie.
     */
    private CellRendererPane rendererPane;

    /**
     * Footer optionnel du ruban (comme dans certains rubans Office).
     */
    protected HRibbonFooter footer;

    /**
     * Afficher les lignes verticales entre les groupes.
     */
    protected boolean showVerticalLines;

    /**
     * Mode de redimensionnement automatique des groupes.
     */
    protected int autoResizeMode;

    /**
     * Créer automatiquement les groupes depuis le modèle de données.
     */
    protected boolean autoCreateGroupsFromModel;

    /**
     * Taille préférée pour l'affichage dans un viewport.
     */
    protected Dimension preferredViewportSize;

    /**
     * Index du groupe en cours d'édition (si édition supportée).
     */
    protected transient int editingColumn;

    /**
     * Table des renderers par classe de composant. Comme dans JTable pour le
     * rendu des cellules.
     */
    protected transient Hashtable<Object, Object> defaultRenderersByGroupClass;

    /**
     * Activer le drag & drop des groupes/composants.
     */
    private boolean dragEnabled;

    /**
     * Empêcher les mises à jour récursives de l'UI.
     */
    private transient boolean updateInProgress;

    /**
     * Le ruban remplit toujours la hauteur du viewport.
     */
    private boolean fillsViewportHeight;

    /**
     * Suivi de la sélection de groupes.
     */
    private boolean groupSelectionAdjusting;

    /**
     * Listener pour la suppression des éditeurs.
     */
    private PropertyChangeListener editorRemover = null;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    /**
     * Constructeur par défaut.
     */
    public HRibbon() {
        this(null, null, null);
    }

    /**
     * Constructeur avec modèle de données.
     */
    public HRibbon(HRibbonModel model) {
        this(model, null, null);
    }

    /**
     * Constructeur avec modèle de groupes.
     */
    public HRibbon(HRibbonGroupModel groupModel) {
        this(null, groupModel, null);
    }

    /**
     * Constructeur avec modèle de sélection.
     */
    public HRibbon(ListSelectionModel selectionModel) {
        this(null, null, selectionModel);
    }

    /**
     * Constructeur avec modèle de données et modèle de groupes.
     */
    public HRibbon(HRibbonModel model, HRibbonGroupModel groupModel) {
        this(model, groupModel, null);
    }

    /**
     * Constructeur avec données brutes.
     */
    public HRibbon(ArrayList<ArrayList<Object>> datagroup, ArrayList<Object> groupIdentifiers) {
        this(new DefaultHRibbonModel(groupIdentifiers, datagroup), null, null);
    }

    /**
     * Constructeur principal.
     */
    public HRibbon(HRibbonModel ribbonModel, HRibbonGroupModel groupModel, ListSelectionModel selectionModel) {
        super();

        // Initialise les modèles avec des valeurs par défaut si null
        if (ribbonModel == null) {
            ribbonModel = createDefaultHRibbonModel();
        }

        if (groupModel == null) {
            groupModel = createDefaultHRibbonGroupModel();
        }

        if (selectionModel == null) {
            selectionModel = createDefaultListSelectionModel();
        }

        // Stocke les références
        this.model = ribbonModel;
        this.groupModel = groupModel;
        this.selectionModel = selectionModel;

        // CRÉATION DU LAYOUTMANAGER
        this.layout = new HRibbonLayoutManager(this);
        setLayout(this.layout);

        // S'inscrit comme listener des modèles
        this.model.addRibbonModelListener(this);
        this.groupModel.addHRibbonGroupModelListener(this);
        syncComponentsFromModel();
    }

    /**
     * Constructeur avec nombre de groupes (à compléter).
     */
    public HRibbon(int numGroup) {
        this();
        // TODO: Initialiser avec 'numGroup' groupes vides
    }

    /**
     * Constructeur avec identifiants de groupes (Vector).
     */
    public HRibbon(Vector<?> groupIdentifiers) {
        this();
        if (groupIdentifiers != null) {
            for (Object identifier : groupIdentifiers) {
                groupModel.addGroup(identifier);
            }
        }
    }

    /**
     * Constructeur avec identifiants de groupes (tableau).
     */
    public HRibbon(Object[] groupIdentifiers) {
        this();
        if (groupIdentifiers != null) {
            for (Object identifier : groupIdentifiers) {
                groupModel.addGroup(identifier);
            }
        }
    }

    // =========================================================================
    // MÉTHODES FACTORY POUR LES DÉFAUTS
    // =========================================================================
    /**
     * Crée le LayoutManager par défaut.
     */
    protected LayoutManager createDefaultLayoutManager() {
        return new HRibbonLayoutManager(this);
    }

    /**
     * Crée le modèle de groupes par défaut.
     */
    protected HRibbonGroupModel createDefaultHRibbonGroupModel() {
        return new DefaultHRibbonGroupModel();
    }

    /**
     * Crée le modèle de données par défaut.
     */
    protected HRibbonModel createDefaultHRibbonModel() {
        return new DefaultHRibbonModel();
    }

    /**
     * Crée le modèle de sélection par défaut.
     */
    protected ListSelectionModel createDefaultListSelectionModel() {
        return new DefaultListSelectionModel();
    }

    // =========================================================================
    // GESTION DE L'UI (Look and Feel)
    // =========================================================================
    /**
     * Retourne l'objet UI qui gère le rendu de ce composant.
     */
    public HRibbonUI getUI() {
        return (HRibbonUI) ui;
    }

    /**
     * Définit l'objet UI qui gère le rendu de ce composant.
     */
    public void setUI(HRibbonUI ui) {
        if (this.ui != ui) {
            super.setUI(ui);
            repaint();
        }
    }

    /**
     * Met à jour l'UI quand le Look and Feel change.
     */
    @Override
    public void updateUI() {
        if (updateInProgress) {
            return;
        }

        updateInProgress = true;

        try {
            // Met à jour les renderers des groupes
            if (groupModel != null) {
                updateGroupRenderersUI();
            }

            // Met à jour l'UI du ruban
            setUI((HRibbonUI) UIManager.getUI(this));

            // Revalide l'affichage
            revalidate();

        } finally {
            updateInProgress = false;
        }
    }

    /**
     * Retourne l'identifiant de classe UI.
     */
    @Override
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * Met à jour les UI des renderers associés aux groupes.
     */
    private void updateGroupRenderersUI() {
        // À compléter quand un système de renderers sera implémenté
    }

//    /**
//     * Supprime un groupe du ruban.
//     */
//    public void removeGroup(HRibbonGroup group) {
//        groupModel.removeGroup(group);
//    }
//    /**
//     * Déplace un groupe à une nouvelle position.
//     */
//    public void moveGroup(int groupIndex, int targetIndex) {
//        if (groupIndex < 0 || groupIndex >= groupModel.getGroupCount()
//                || targetIndex < 0 || targetIndex >= groupModel.getGroupCount()) {
//            throw new IllegalArgumentException("Indice de groupe invalide");
//        }
//
//        groupModel.moveGroup(groupIndex, targetIndex);
//    }
    /**
     * Retourne l'index du groupe à la position du point.
     */
    public int groupAtPoint(Point point) {
        int x = point.x;

        if (!getComponentOrientation().isLeftToRight()) {
            x = getWidth() - x - 1;
        }

        return groupModel.getHRibbonGroupIndexAtX(x);
    }

    /**
     * Définit la largeur de chaque groupe selon sa largeur préférée.
     */
    public void setWidthsFromPreferredWidths(boolean respectMinimum) {
        for (int i = 0; i < groupModel.getGroupCount(); i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            int prefWidth = group.calculatePreferredWidth();

            if (respectMinimum) {
                prefWidth = Math.max(prefWidth, group.getMinWidth());
            }

            group.setWidth(prefWidth);
        }

        revalidate();
        repaint();
    }

    /**
     * Définit le mode de répartition des groupes.
     *
     * @param equal true pour largeurs égales, false pour largeurs préférées
     */
    public void setEqualGroupDistribution(boolean equal) {
        if (layout != null) {
            layout.setEqualDistribution(equal);
            revalidate();
            repaint();
        }
    }

    /**
     * Retourne le mode de répartition actuel.
     */
    public boolean isEqualGroupDistribution() {
        return layout != null && layout.isEqualDistribution();
    }

    // =========================================================================
    // ACCÈS AUX MODÈLES ET LAYOUT
    // =========================================================================
    /**
     * Retourne le modèle de données.
     */
    public HRibbonModel getModel() {
        return model;
    }

    /**
     * Retourne le modèle de groupes.
     */
    public HRibbonGroupModel getGroupModel() {
        return groupModel;
    }

    /**
     * Retourne le LayoutManager du ruban.
     */
    public HRibbonLayoutManager getRubanLayout() {
        return layout;
    }

    /**
     * Retourne l'espacement entre les composants dans un groupe.
     */
    public int getComponentSpacing(HRibbonGroup group) {
        return group.getComponentSpacing();
    }

    // =========================================================================
    // GESTION DES TAILLES
    // =========================================================================
    /**
     * Calcule la taille préférée du ruban.
     */
    @Override
    public Dimension getPreferredSize() {
        // 1. Taille explicitement définie
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }

        // 2. Calcul via le LayoutManager
        if (layout != null) {
            return layout.preferredLayoutSize(this);
        }

        // 3. Fallback
        return new Dimension(400, 80);
    }

    /**
     * Calcule la largeur préférée du ruban. Méthode utilisée si pas de
     * LayoutManager.
     */
    private int calculatePreferredWidth() {
        if (groupModel == null) {
            return 400;
        }

        int totalWidth = 0;
        int margin = groupModel.getHRibbonGroupMarggin();
        int groupCount = groupModel.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalWidth += group.getWidth();
                if (i < groupCount - 1) {
                    totalWidth += margin;
                }
            }
        }

        Insets insets = getInsets();
        if (insets != null) {
            totalWidth += insets.left + insets.right;
        }

        return Math.max(totalWidth, 100);
    }

    // =========================================================================
    // GESTION DES MODÈLES (setModel / setGroupModel)
    // =========================================================================
    /**
     * Définit le modèle de données du ruban.
     */
    public void setModel(HRibbonModel model) {
        if (model == null) {
            throw new IllegalArgumentException("Cannot set a null HRibbonModel");
        }

        if (this.model != model) {
            HRibbonModel old = this.model;

            // Désinscrit de l'ancien modèle
            if (old != null) {
                old.removeModelListener(this);
            }

            // Assigne et s'inscrit au nouveau
            this.model = model;
            model.addRibbonModelListener(this);

            // Notifie le changement (comme JTable)
            ribbonChanged(new HRibbonModelEvent(model));

            // Notification PropertyChange
            firePropertyChange("model", old, model);

            // Crée les groupes si auto-création activée
            if (autoCreateGroupsFromModel && groupModel != null) {
                createGroupsFromModel();
            }

            // Force le layout à recalculer
            if (layout != null) {
                layout.invalidateLayout(this);
            }

            revalidate();
            repaint();
        }
    }

    /**
     * Définit le modèle de groupes du ruban.
     */
    public void setGroupModel(HRibbonGroupModel groupModel) {
        if (groupModel == null) {
            throw new IllegalArgumentException("Cannot set a null HRibbonGroupModel");
        }

        if (this.groupModel != groupModel) {
            HRibbonGroupModel old = this.groupModel;

            // Désinscrit de l'ancien modèle
            if (old != null) {
                old.removeHRibbonGroupModelListener(this);
            }

            // Assigne le nouveau modèle
            this.groupModel = groupModel;

            // S'inscrit au nouveau modèle
            groupModel.addHRibbonGroupModelListener(this);

            // Notification PropertyChange
            firePropertyChange("groupModel", old, groupModel);

            // Force le layout à recalculer
            if (layout != null) {
                layout.invalidateLayout(this);
            }

            revalidate();
            repaint();
        }
    }

    // =========================================================================
    // AUTO-CRÉATION DES GROUPES
    // =========================================================================
    /**
     * Active/désactive l'auto-création des groupes depuis le modèle.
     */
    public void setAutoCreateGroupsFromModel(boolean autoCreate) {
        if (this.autoCreateGroupsFromModel != autoCreate) {
            this.autoCreateGroupsFromModel = autoCreate;

            if (autoCreate && model != null && groupModel != null) {
                createGroupsFromModel();
            }
        }
    }

    /**
     * Retourne si l'auto-création est activée.
     */
    public boolean getAutoCreateGroupsFromModel() {
        return autoCreateGroupsFromModel;
    }

    /**
     * Crée les groupes depuis le modèle de données.
     */
    private void createGroupsFromModel() {
        if (model == null || groupModel == null) {
            return;
        }

        // Supprime les groupes existants
        while (groupModel.getGroupCount() > 0) {
            groupModel.removeGroup(0);
        }

        // Crée un groupe pour chaque identifiant
        for (int i = 0; i < model.getGroupCount(); i++) {
            Object groupIdentifier = model.getGroupIdentifier(i);
            groupModel.addGroup(groupIdentifier);
        }
    }

    // =========================================================================
    // IMPLÉMENTATION DE HRibbonModelListener
    // =========================================================================
    /**
     * Reçoit les notifications de changement du modèle de données.
     */
    @Override
    public void ribbonChanged(HRibbonModelEvent e) {
        syncComponentsFromModel(); 
        if (e.isGlobalChange()) {
            if (autoCreateGroupsFromModel && groupModel != null) {
                createGroupsFromModel();
            }
        } else if (e.isGroupChange()) {
            handleGroupChange(e);
        } else if (e.isValueChange()) {
            handleValueChange(e);
        }

        // Force le layout à recalculer
        if (layout != null) {
            layout.invalidateLayout(this);
        }

        revalidate();
        repaint();
    }

    /**
     * Gère les changements au niveau groupe.
     */
    private void handleGroupChange(HRibbonModelEvent e) {
        int groupIndex = e.getGroupIndex();

        switch (e.getType()) {
            case HRibbonModelEvent.INSERT:
                if (autoCreateGroupsFromModel && groupModel != null) {
                    Object groupIdentifier = model.getGroupIdentifier(groupIndex);
                    groupModel.addGroup(groupIdentifier);
                }
                break;

            case HRibbonModelEvent.DELETE:
                if (autoCreateGroupsFromModel && groupModel != null) {
                    groupModel.removeGroup(groupIndex);
                }
                break;

            case HRibbonModelEvent.UPDATE:
                // Rafraîchissement simple
                break;

            case HRibbonModelEvent.MOVE:
                if (autoCreateGroupsFromModel && groupModel != null) {
                    groupModel.moveGroup(e.getPosition(), e.getToPosition());
                }
                break;
        }
    }

    /**
     * Gère les changements au niveau valeur.
     */
    private void handleValueChange(HRibbonModelEvent e) {
        // À compléter si nécessaire
    }

    // =========================================================================
    // IMPLÉMENTATION DE HRibbonGroupListener
    // =========================================================================
    @Override
    public void groupAdded(HRibbonGroupEvent e) {
        if (isEditing()) {
            stopEditing();
        }

        if (layout != null) {
            layout.invalidateLayout(this);
        }

        revalidate();
        repaint();
    }

    @Override
    public void groupRemoved(HRibbonGroupEvent e) {
        if (isEditing()) {
            stopEditing();
        }

        if (layout != null) {
            layout.invalidateLayout(this);
        }

        revalidate();
        repaint();
    }

    @Override
    public void groupMoved(HRibbonGroupEvent e) {
        if (isEditing()) {
            stopEditing();
        }

        if (layout != null) {
            layout.invalidateLayout(this);
        }

        revalidate();
        repaint();
    }

    @Override
    public void groupMarginChanged(ChangeEvent e) {
        if (layout != null) {
            layout.invalidateLayout(this);
        }

        revalidate();
        repaint();
    }

    @Override
    public void groupSelectionChanged(ListSelectionEvent e) {
        // Pour la sélection future
        repaint();
    }

    // =========================================================================
    // GESTION DE L'ÉDITION (pour le futur)
    // =========================================================================
    /**
     * Vérifie si le ruban est en cours d'édition.
     */
    public boolean isEditing() {
        // À compléter quand l'édition sera implémentée
        return false;
    }

    /**
     * Arrête l'édition en cours.
     */
    public void stopEditing() {
        // À compléter quand l'édition sera implémentée
    }

    // =========================================================================
// MÉTHODES PUBLIQUES POUR MANIPULER LES DONNÉES
// =========================================================================
    /**
     * Ajoute un composant à un groupe.
     *
     * @param component le composant à ajouter
     * @param groupIndex l'index du groupe
     */
    public void addComponent(Component component, int groupIndex) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.addValue(component, groupIndex);
        } else {
            // Pour les autres implémentations de HRibbonModel
            // Il faut que le modèle fournisse une méthode addValue()
            throw new UnsupportedOperationException(
                    "Model does not support adding components directly");
        }
    }

    /**
     * Ajoute un composant à un groupe identifié par son nom.
     *
     * @param component le composant à ajouter
     * @param groupIdentifier l'identifiant du groupe
     */
    public void addComponent(Component component, Object groupIdentifier) {
    // 1. Ajoute au modèle
    if (model instanceof DefaultHRibbonModel) {
        ((DefaultHRibbonModel) model).addValue(component, groupIdentifier);
    }
    
    // 2. Ajoute DIRECTEMENT au conteneur Swing
    add(component);
    
    // 3. Force le re-layout
    revalidate();
    repaint();
}

    /**
     * Déplace un composant dans un groupe.
     *
     * @param oldPosition position actuelle
     * @param newPosition nouvelle position
     * @param groupIndex index du groupe
     */
    public void moveComponent(int oldPosition, int newPosition, int groupIndex) {
        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.moveValue(oldPosition, newPosition, groupIndex);
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support moving components");
        }
    }

    /**
     * Supprime un composant d'un groupe.
     *
     * @param position position du composant
     * @param groupIndex index du groupe
     */
    public void removeComponent(int position, int groupIndex) {
        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.removeValueAt(position, groupIndex);
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support removing components");
        }
    }

    // =========================================================================
// MÉTHODES POUR MANIPULER LES GROUPES
// =========================================================================
    /**
     * Retourne le nombre de groupes dans le ruban.
     *
     * @return le nombre de groupes
     */
    public int getGroupCount() {
        return groupModel != null ? groupModel.getGroupCount() : 0;
    }

    /**
     * Retourne le groupe à l'index spécifié.
     *
     * @param index l'index du groupe
     * @return le groupe, ou null si l'index est invalide
     */
    public HRibbonGroup getGroup(int index) {
        if (groupModel != null && index >= 0 && index < groupModel.getGroupCount()) {
            return groupModel.getHRibbonGroup(index);
        }
        return null;
    }

    /**
     * Retourne l'index d'un groupe par son identifiant.
     *
     * @param groupIdentifier l'identifiant du groupe
     * @return l'index du groupe, ou -1 si non trouvé
     */
    public int getGroupIndex(Object groupIdentifier) {
        if (groupModel != null && groupIdentifier != null) {
            return groupModel.getGroupIndex(groupIdentifier);
        }
        return -1;
    }

    /**
     * Retourne l'identifiant d'un groupe à l'index spécifié.
     *
     * @param index l'index du groupe
     * @return l'identifiant du groupe, ou null si l'index est invalide
     */
    public Object getGroupIdentifier(int index) {
        if (model != null && index >= 0 && index < model.getGroupCount()) {
            return model.getGroupIdentifier(index);
        }
        return null;
    }

    /**
     * Ajoute un nouveau groupe au ruban avec un identifiant.
     *
     * @param groupIdentifier l'identifiant du nouveau groupe
     * @throws IllegalArgumentException si l'identifiant est null
     */
    public void addGroup(Object groupIdentifier) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException("Group identifier cannot be null");
        }

        if (groupModel != null) {
            groupModel.addGroup(groupIdentifier);
        }
    }

    /**
     * Ajoute un groupe existant au ruban.
     *
     * @param group le groupe à ajouter
     * @throws IllegalArgumentException si le groupe est null
     */
    public void addGroup(HRibbonGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }

        if (groupModel != null) {
            groupModel.addGroup(group);
        }
    }

    /**
     * Supprime un groupe à l'index spécifié.
     *
     * @param groupIndex l'index du groupe à supprimer
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public void removeGroup(int groupIndex) {
        if (groupModel != null) {
            if (groupIndex < 0 || groupIndex >= groupModel.getGroupCount()) {
                throw new IndexOutOfBoundsException("Invalid group index: " + groupIndex);
            }
            groupModel.removeGroup(groupIndex);
        }
    }

    /**
     * Supprime un groupe par son identifiant.
     *
     * @param groupIdentifier l'identifiant du groupe à supprimer
     * @return true si le groupe a été trouvé et supprimé
     */
    public boolean removeGroup(Object groupIdentifier) {
        if (groupModel != null && groupIdentifier != null) {
            groupModel.removeGroup(groupIdentifier);
            return true;
        }
        return false;
    }

    /**
     * Supprime un groupe spécifique.
     *
     * @param group le groupe à supprimer
     * @return true si le groupe a été trouvé et supprimé
     */
    public boolean removeGroup(HRibbonGroup group) {
        if (groupModel != null && group != null) {
            groupModel.removeGroup(group);
            return true;
        }
        return false;
    }

    /**
     * Déplace un groupe vers une nouvelle position.
     *
     * @param oldIndex position actuelle du groupe
     * @param newIndex nouvelle position du groupe
     * @throws IndexOutOfBoundsException si un index est invalide
     */
    public void moveGroup(int oldIndex, int newIndex) {
        if (groupModel != null) {
            if (oldIndex < 0 || oldIndex >= groupModel.getGroupCount()
                    || newIndex < 0 || newIndex >= groupModel.getGroupCount()) {
                throw new IndexOutOfBoundsException(
                        "Invalid group index: old=" + oldIndex + ", new=" + newIndex);
            }
            groupModel.moveGroup(oldIndex, newIndex);
        }
    }

    /**
     * Déplace un groupe identifié par son nom vers une nouvelle position.
     *
     * @param groupIdentifier identifiant du groupe à déplacer
     * @param newIndex nouvelle position
     * @throws IllegalArgumentException si l'identifiant est null
     * @throws IndexOutOfBoundsException si newIndex est invalide
     */
    public void moveGroup(Object groupIdentifier, int newIndex) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException("Group identifier cannot be null");
        }

        if (groupModel != null) {
            if (newIndex < 0 || newIndex >= groupModel.getGroupCount()) {
                throw new IndexOutOfBoundsException("Invalid group index: " + newIndex);
            }
            groupModel.moveGroup(groupIdentifier, newIndex);
        }
    }

    /**
     * Insère un groupe à une position spécifique.
     *
     * @param group le groupe à insérer
     * @param index position d'insertion
     * @throws IllegalArgumentException si le groupe est null
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public void insertGroup(HRibbonGroup group, int index) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }

        if (groupModel != null) {
            if (index < 0 || index > groupModel.getGroupCount()) {
                throw new IndexOutOfBoundsException("Invalid group index: " + index);
            }
            groupModel.insertGroup(group, index);
        }
    }

    /**
     * Insère un groupe avec un identifiant à une position spécifique.
     *
     * @param groupIdentifier identifiant du nouveau groupe
     * @param index position d'insertion
     * @throws IllegalArgumentException si l'identifiant est null
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public void insertGroup(Object groupIdentifier, int index) {
        if (groupIdentifier == null) {
            throw new IllegalArgumentException("Group identifier cannot be null");
        }

        if (groupModel != null) {
            if (index < 0 || index > groupModel.getGroupCount()) {
                throw new IndexOutOfBoundsException("Invalid group index: " + index);
            }
            groupModel.insertGroup(groupIdentifier, index);
        }
    }

    /**
     * Vérifie si un groupe existe par son identifiant.
     *
     * @param groupIdentifier l'identifiant à vérifier
     * @return true si le groupe existe
     */
    public boolean containsGroup(Object groupIdentifier) {
        if (groupModel != null && groupIdentifier != null) {
            return groupModel.getGroupIndex(groupIdentifier) >= 0;
        }
        return false;
    }

    /**
     * Retourne la largeur totale de tous les groupes.
     *
     * @return la largeur totale en pixels
     */
    public int getTotalGroupWidth() {
        return groupModel != null ? groupModel.getTotalHRibbonGroupWidth() : 0;
    }

    /**
     * Définit la marge entre les groupes.
     *
     * @param margin la nouvelle marge en pixels
     */
    public void setGroupMargin(int margin) {
        if (groupModel != null) {
            groupModel.setGroupMargin(margin);
        }
    }

    /**
     * Retourne la marge entre les groupes.
     *
     * @return la marge en pixels
     */
    public int getGroupMargin() {
        return groupModel != null ? groupModel.getHRibbonGroupMarggin() : 0;
    }

    /**
     * Active ou désactive la sélection des groupes.
     *
     * @param allowed true pour autoriser la sélection
     */
    public void setGroupSelectionAllowed(boolean allowed) {
        if (groupModel != null) {
            groupModel.setHRibbonGroupSelectionAllowed(allowed);
        }
    }

    /**
     * Vérifie si la sélection des groupes est autorisée.
     *
     * @return true si la sélection est autorisée
     */
    public boolean isGroupSelectionAllowed() {
        return groupModel != null && groupModel.getHRibbonGroupSelectionAllowed();
    }

    /**
     * Retourne les indices des groupes sélectionnés.
     *
     * @return tableau des indices sélectionnés
     */
    public int[] getSelectedGroupIndices() {
        return groupModel != null ? groupModel.getSelectionHRibbonGroup() : new int[0];
    }

    /**
     * Retourne le nombre de groupes sélectionnés.
     *
     * @return le nombre de groupes sélectionnés
     */
    public int getSelectedGroupCount() {
        return groupModel != null ? groupModel.getSelectedHRibbonHRibbonCount() : 0;
    }

    // =========================================================================
// MÉTHODES POUR MANIPULER LES COMPOSANTS DANS LES GROUPES
// =========================================================================
    /**
     * Insère un composant à une position spécifique dans un groupe.
     *
     * @param component le composant à insérer
     * @param position position d'insertion
     * @param groupIndex index du groupe
     * @throws IllegalArgumentException si le composant est null
     * @throws IndexOutOfBoundsException si position ou groupIndex est invalide
     */
    public void insertComponent(Component component, int position, int groupIndex) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.insertValueAt(component, position, groupIndex);
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support inserting components");
        }
    }

    /**
     * Retire un composant spécifique d'un groupe.
     *
     * @param component le composant à retirer
     * @param groupIndex index du groupe
     * @return true si le composant a été trouvé et retiré
     */
    public boolean removeComponent(Component component, int groupIndex) {
        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.removeValue(component, groupIndex);
            return true;
        }
        return false;
    }

    /**
     * Retourne le composant à une position donnée dans un groupe.
     *
     * @param position position du composant
     * @param groupIndex index du groupe
     * @return le composant, ou null si non trouvé
     */
    public Component getComponent(int position, int groupIndex) {
        if (model != null) {
            Object value = model.getValueAt(position, groupIndex);
            return (value instanceof Component) ? (Component) value : null;
        }
        return null;
    }

    /**
     * Retourne le nombre de composants dans un groupe.
     *
     * @param groupIndex index du groupe
     * @return nombre de composants
     */
    public int getComponentCount(int groupIndex) {
        return model != null ? model.getValueCount(groupIndex) : 0;
    }

    /**
     * Vérifie si un groupe contient un composant spécifique.
     *
     * @param component le composant à chercher
     * @param groupIndex index du groupe
     * @return true si le composant est dans le groupe
     */
    public boolean containsComponent(Component component, int groupIndex) {
        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            return defaultModel.containsValue(component, groupIndex);
        }
        return false;
    }

    /**
     * Vide tous les composants d'un groupe.
     *
     * @param groupIndex index du groupe
     */
    public void clearGroup(int groupIndex) {
        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.removeAllValues(groupIndex);
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support clearing groups");
        }
    }

     /**
     * Synchronise les composants du modèle vers le conteneur Swing.
     * À appeler quand le modèle change.
     */
    private void syncComponentsFromModel() {
        // 1. Retire tous les composants existants
        removeAll();
        
        // 2. Parcours tous les groupes et valeurs
        if (model != null) {
            for (int groupIndex = 0; groupIndex < model.getGroupCount(); groupIndex++) {
                for (int valueIndex = 0; valueIndex < model.getValueCount(groupIndex); valueIndex++) {
                    Object value = model.getValueAt(valueIndex, groupIndex);
                    
                    // 3. Ajoute les Component au conteneur Swing
                    if (value instanceof Component) {
                        add((Component) value);
                    }
                }
            }
        }
        
        // 4. Force le re-layout
        revalidate();
        repaint();
    }
    
    
}
