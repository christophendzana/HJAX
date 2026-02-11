/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

/**
 *
 * @author FIDELE
 */
public class Ribbon extends JComponent implements HRibbonModelListener, HRibbonGroupListener {

    /**
     * Identifiant pour le système de Look and Feel.
     */
    private static final String uiClassID = "BasicHRibbonUI";

    /**
     * Constantes pour le mode de redimensionnement automatique.
     */
    public final int AUTO_RESIZE_OFF = 0;
    public final int AUTO_RESIZE_NEXT_GROUP = 1;
    public final int AUTO_RESIZE_SUBSEQUENT_GROUPS = 2;
    public final int AUTO_RESIZE_LAST_GROUP = 3;
    public final int AUTO_RESIZE_ALL_GROUPS = 4;

    /**
     * Constante pour le positionnement du header
     */
    public static final int HEADER_NORTH = 0;
    public static final int HEADER_SOUTH = 1;  // Comme Word - en bas du ruban
    public static final int HEADER_WEST = 2;   // À gauche de chaque groupe
    public static final int HEADER_EAST = 3;   // À droite de chaque groupe
    public static final int HEADER_HIDDEN = -1;

    private int headerAlignment = HEADER_NORTH;
    private int headerHeight = 25; // Hauteur par défaut pour NORTH/SOUTH
    private int headerWidth = 25;  // Largeur par défaut pour WEST/EAST 

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
    
    private transient boolean isLayoutInProgress = false;

    /**
     * Le ruban remplit toujours la hauteur du viewport.
     */
    private boolean fillsViewportHeight;

    /**
     * Suivi de la sélection de groupes.
     */
    private boolean groupSelectionAdjusting;

    /**
     * Ensemble des composants actuellement affichés dans le ruban. Utilisé pour
     * synchroniser l'affichage avec le modèle.
     */
    private Set<Component> displayedComponents = new HashSet<>();

    /**
     * Indique si une synchronisation avec le modèle est en cours. Empêche les
     * boucles de notification récursives.
     */
    private boolean syncingWithModel = false;

    private GroupRenderer groupRenderer;

    private int headerMargin = 0;

    /**
     * Listener pour la suppression des éditeurs.
     */
    private PropertyChangeListener editorRemover = null;

    /**
     * Cache des informations des components créés par le renderer. Map :
     * Component -> (groupIndex, position, value)
     */
    private Map<Component, ComponentInfo> componentInfoMap = new HashMap<>();

    private PropertyChangeListener groupPropertyChangeListener;

    public enum HeightPolicy {
        PREFERRED_ONLY, // Le Ribbon impose sa hauteur calculée (par défaut)
        FILL_PARENT, // Le Ribbon s'étire pour remplir la hauteur du parent
        FIXED           // Hauteur fixe définie par setFixedHeight(...)
    }

    /**
     * Politique de hauteur appliquée au Ribbon (default = impose sa hauteur)
     */
    private HeightPolicy heightPolicy = HeightPolicy.PREFERRED_ONLY;

    /**
     * Hauteur fixe (en pixels) utilisée si heightPolicy == FIXED. -1 = non
     * défini
     */
    private int fixedHeight = -1;

    /**
     * Couleur de fond par défaut pour tous les en-têtes du ruban. Utilisée
     * lorsqu'un groupe n'a pas de couleur spécifique définie.
     */
    private Color defaultHeaderBackground = new Color(197, 199, 228); // Gris-bleu clair

    /**
     * Couleur de texte par défaut pour tous les en-têtes du ruban. Utilisée
     * lorsqu'un groupe n'a pas de couleur spécifique définie.
     */
    private Color defaultHeaderForeground = new Color(60, 60, 60); // Gris foncé

    /**
     * Couleur de bordure par défaut pour tous les en-têtes du ruban. Utilisée
     * lorsqu'un groupe n'a pas de couleur spécifique définie.
     */
    private Color defaultHeaderBorderColor = new Color(200, 200, 200); // Gris clair

    /**
     * Taille de police par défaut pour tous les en-têtes du ruban (en points).
     * Utilisée lorsqu'un groupe n'a pas de taille spécifique définie.
     */
    private int defaultHeaderFontSize = 11;

    /**
     * Indicateur de police en gras par défaut pour tous les en-têtes du ruban.
     * Utilisée lorsqu'un groupe n'a pas de paramètre spécifique défini.
     */
    private boolean defaultHeaderFontBold = true;

    /**
     * Couleur de fond par défaut pour les en-têtes au survol. Utilisée
     * lorsqu'un groupe n'a pas de couleur spécifique définie.
     */
    private Color defaultHeaderHoverBackground = new Color(180, 200, 255); // Bleu très clair

    /**
     * Couleur de fond par défaut pour les en-têtes sélectionnés. Utilisée
     * lorsqu'un groupe n'a pas de couleur spécifique définie.
     */
    private Color defaultHeaderSelectedBackground = new Color(150, 180, 255); // Bleu clair

    /**
     * Rayon des coins arrondis par défaut pour tous les en-têtes (en pixels).
     * Utilisée lorsqu'un groupe n'a pas de rayon spécifique défini. 0 = coins
     * carrés, 5 = légèrement arrondi, 15 = très arrondi.
     */
    private int defaultHeaderCornerRadius = 5;

    /**
     * Listener pour détecter les changements de taille du conteneur parent.
     * Utilisé pour le redimensionnement adaptatif des groupes.
     */
    private transient ComponentListener resizeListener = null;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    /**
     * Constructeur par défaut.
     */
    public Ribbon() {
        this(null, null, null);
    }

    /**
     * Constructeur avec modèle de données.
     */
    public Ribbon(HRibbonModel model) {
        this(model, null, null);
    }

    /**
     * Constructeur avec modèle de groupes.
     */
    public Ribbon(HRibbonGroupModel groupModel) {
        this(null, groupModel, null);
    }

    /**
     * Constructeur avec modèle de sélection.
     */
    public Ribbon(ListSelectionModel selectionModel) {
        this(null, null, selectionModel);
    }

    /**
     * Constructeur avec modèle de données et modèle de groupes.
     */
    public Ribbon(HRibbonModel model, HRibbonGroupModel groupModel) {
        this(model, groupModel, null);
    }

    /**
     * Constructeur avec données brutes.
     */
    public Ribbon(ArrayList<ArrayList<Object>> datagroup, ArrayList<Object> groupIdentifiers) {
        this(new DefaultHRibbonModel(groupIdentifiers, datagroup), null, null);
    }

    /**
     * Constructeur principal.
     */
    public Ribbon(HRibbonModel ribbonModel, HRibbonGroupModel groupModel, ListSelectionModel selectionModel) {
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

        this.autoCreateGroupsFromModel = true;

        this.groupRenderer = createDefaultGroupRenderer();

         // Installer le listener de redimensionnement adaptatif
        installResizeListener();

        
        updateUI();

    }

//    /**
//     * Constructeur avec nombre de groupes (à compléter).
//     */
//    public Ribbon(int numGroup) {
//        this();
//         // Penser à initialiser
//    }
    /**
     * Constructeur avec identifiants de groupes (Vector).
     */
    public Ribbon(Vector<?> groupIdentifiers) {
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
    public Ribbon(Object[] groupIdentifiers) {
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

    protected GroupRenderer createDefaultGroupRenderer() {
        return new DefaultGroupRenderer();
    }

    // =========================================================================
    // GESTION DE L'UI (Look and Feel)
    // =========================================================================
    /**
     * Retourne l'objet UI qui gère le rendu de ce composant.
     */
    public BasicHRibbonUI getUI() {
        return (BasicHRibbonUI) ui;
    }

    /**
     * Définit l'objet UI qui gère le rendu de ce composant.
     */
    public void setUI(BasicHRibbonUI ui) {
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

            if (UIManager.get("BasicHRibbonUI") == null) {
                UIManager.put("BasicHRibbonUI", "rubban.BasicHRibbonUI");
            }

            this.setOpaque(true);

            ComponentUI ui = UIManager.getUI(this);
            if (ui != null) {
                setUI((BasicHRibbonUI) ui);
            } else {
                // Fallback : créer directement une instance
                setUI(new BasicHRibbonUI());
            }
            setUI((BasicHRibbonUI) UIManager.getUI(this));

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

    public GroupRenderer getGroupRenderer() {
        return groupRenderer;
    }

    public void setGroupRenderer(GroupRenderer renderer) {
        if (renderer == null) {
            throw new IllegalArgumentException("GroupRenderer cannot be null");
        }
        this.groupRenderer = renderer;
        // Force une mise à jour de l'affichage
        syncComponentsWithModel();
        revalidate();
        repaint();
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
        // 1) Si l'utilisateur a explicitement défini une preferredSize, la respecter
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }

        // 2) Calcul habituel via le LayoutManager si présent
        Dimension base;
        if (layout != null) {
            base = layout.preferredLayoutSize(this);
        } else {
            base = new Dimension(400, 80);
        }

        // 3) Appliquer la politique de hauteur FIXED si demandée
        if (heightPolicy == HeightPolicy.FIXED) {
            int h = (fixedHeight > 0) ? fixedHeight : base.height;
            return new Dimension(base.width, h);
        }

        // Sinon, renvoyer la taille calculée
        return base;
    }

    /**
     * Calcule la largeur préférée du ruban. Méthode utilisée si pas de
     * LayoutManager.
     */
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

    /**
 * Synchronise les composants affichés avec le modèle de données.
 * 
 * PRINCIPE FONDAMENTAL - SÉPARATION STRICTE DES ÉTATS :
 * ------------------------------------------------------
 * - Groupe NORMAL    → composants DANS le Ribbon (parent = Ribbon)
 * - Groupe COLLAPSED → composants DANS le OverflowButton (parent ≠ Ribbon)
 * 
 * Un composant n'est JAMAIS présent dans les deux conteneurs simultanément.
 * Le transfert est exclusif et contrôlé par l'état du groupe.
 * 
 * Cette méthode maintient l'invariant : displayedComponents = composants NORMAUX uniquement
 */
private void syncComponentsWithModel() {
    // Vérifications de sécurité
    if (syncingWithModel || model == null || groupRenderer == null || groupModel == null) {
        return;
    }

    syncingWithModel = true;

    try {
        // === CARTE D'IDENTITÉ DES COMPOSANTS ===
        // Associe chaque composant à ses coordonnées (groupe, position, valeur)
        Map<Component, ComponentInfo> newComponentInfoMap = new HashMap<>();
        
        // === ENSEMBLE DES COMPOSANTS EXISTANT DANS LE MODÈLE ===
        // Tous les composants qui DEVRAIENT exister (indépendamment de leur état)
        Set<Component> allExpectedComponents = new HashSet<>();
        
        // === ENSEMBLE DES COMPOSANTS À PLACER DANS LE RIBBON ===
        // UNIQUEMENT ceux des groupes NON collapsed
        Set<Component> componentsToKeepInRibbon = new HashSet<>();

        // ÉTAPE 1 : PARCOURS COMPLET DU MODÈLE
        // -------------------------------------
        for (int groupIndex = 0; groupIndex < model.getGroupCount(); groupIndex++) {
            
            // Déterminer l'état du groupe
            HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
            boolean isCollapsed = (group != null && group.isCollapsed());
            
            int valueCount = model.getValueCount(groupIndex);
            
            for (int position = 0; position < valueCount; position++) {
                Object value = model.getValueAt(position, groupIndex);
                
                // Créer ou récupérer le composant associé à cette valeur
                Component component = createComponentForValue(value, groupIndex, position);

                if (component != null) {
                    // Enregistrer les métadonnées du composant
                    newComponentInfoMap.put(component,
                            new ComponentInfo(groupIndex, position, value));
                    
                    // Ce composant est attendu dans le modèle
                    allExpectedComponents.add(component);

                    // DÉCISION CRUCIALE : Où doit vivre ce composant ?
                    // -------------------------------------------------
                    if (!isCollapsed) {
                        // CAS 1 : Groupe NORMAL → le composant DOIT être dans le Ribbon
                        componentsToKeepInRibbon.add(component);
                    }
                    // CAS 2 : Groupe COLLAPSED → le composant NE DOIT PAS être dans le Ribbon
                    // (il sera dans le OverflowButton, géré par CollapsedGroupRenderer)
                    // → NE PAS l'ajouter à componentsToKeepInRibbon
                }
            }
        }

        // ÉTAPE 2 : NETTOYAGE DES COMPOSANTS ORPHELINS
        // --------------------------------------------
        // Retirer tous les composants qui ne sont plus dans le modèle
        Set<Component> toRemove = new HashSet<>(displayedComponents);
        toRemove.removeAll(allExpectedComponents);
        for (Component comp : toRemove) {
            removeComponentFromContainer(comp);
            // Nettoyer également le cache d'informations
            componentInfoMap.remove(comp);
        }

        // ÉTAPE 3 : AJOUT DES NOUVEAUX COMPOSANTS NORMaux
        // -----------------------------------------------
        // Ajouter uniquement les composants des groupes normaux qui ne sont pas déjà présents
        Set<Component> toAdd = new HashSet<>(componentsToKeepInRibbon);
        toAdd.removeAll(displayedComponents);
        for (Component comp : toAdd) {
            addComponentToContainer(comp);
        }

        // ÉTAPE 4 : EXPULSION DES COMPOSANTS COLLAPSED DU RIBBON
        // -------------------------------------------------------
        // C'est le cœur de la séparation stricte :
        // Tout composant qui est dans le Ribbon mais qui appartient à un groupe collapsed
        // DOIT être immédiatement retiré
        Set<Component> toRemoveFromCollapsed = new HashSet<>(displayedComponents);
        toRemoveFromCollapsed.removeAll(componentsToKeepInRibbon);
        for (Component comp : toRemoveFromCollapsed) {
            removeComponentFromContainer(comp);
            // Important : on garde le composant dans componentInfoMap
            // car il existe toujours dans le modèle, juste déplacé
        }

        // ÉTAPE 5 : MISE À JOUR DES CACHES
        // --------------------------------
        // displayedComponents ne contient PLUS QUE les composants des groupes normaux
        displayedComponents = new HashSet<>(componentsToKeepInRibbon);
        
        // Remplacer la carte d'identité
        componentInfoMap = newComponentInfoMap;

        // Journalisation debug (à désactiver en production)
        if (debugMode) {
            System.out.println("syncComponentsWithModel - " +
                             "Normaux: " + componentsToKeepInRibbon.size() + 
                             ", Total: " + allExpectedComponents.size());
        }

    } finally {
        syncingWithModel = false;
    }
}

/**
 * TRANSFERT EXCLUSIF : Ribbon → OverflowButton
 * --------------------------------------------
 * Vide complètement un groupe du Ribbon et transfère tous ses composants
 * vers un RibbonOverflowButton.
 * 
 * Préconditions :
 * - Le groupe est en cours de collapse
 * - Le bouton est fraîchement créé ou vide
 * 
 * Postconditions :
 * - Tous les composants du groupe n'ont PLUS le Ribbon comme parent
 * - Tous les composants sont dans le bouton (via sa liste interne)
 * - Le cache displayedComponents est mis à jour
 * 
 * @param groupIndex index du groupe à vider
 * @param button le bouton de débordement qui va recevoir les composants
 */
public void transferComponentsToOverflow(int groupIndex, RibbonOverflowButton button) {
    // Validation des paramètres
    if (model == null || button == null) {
        return;
    }
    
    // Vérifier que le groupe existe
    if (groupIndex < 0 || groupIndex >= model.getGroupCount()) {
        return;
    }
    
    // Éviter les transferts redondants pendant les mises à jour
    if (syncingWithModel) {
        return;
    }
    
    if (debugMode) {
        System.out.println(">>> TRANSFERT Ribbon → Overflow - Groupe " + groupIndex);
    }
    
    int valueCount = model.getValueCount(groupIndex);
    int transferredCount = 0;
    
    for (int i = 0; i < valueCount; i++) {
        Object value = model.getValueAt(i, groupIndex);
        
        if (value instanceof JComponent) {
            JComponent comp = (JComponent) value;
            
            // ÉTAPE 1 : RETIRER DU RIBBON
            // ----------------------------
            if (comp.getParent() == this) {
                removeComponentSafely(comp);
                
                // Important : on retire aussi du cache displayedComponents
                displayedComponents.remove(comp);
                
                if (debugMode) {
                    System.out.println("   - Retiré: " + comp.getClass().getSimpleName());
                }
            }
            
            // ÉTAPE 2 : AJOUTER AU BOUTON
            // ----------------------------
            // Le bouton accepte les composants sans parent
            button.addComponent(comp);
            transferredCount++;
        }
    }
    
    if (debugMode) {
        System.out.println("<<< TRANSFERT TERMINÉ: " + transferredCount + 
                         " composants transférés");
    }
}

/**
 * TRANSFERT EXCLUSIF : OverflowButton → Ribbon
 * --------------------------------------------
 * Récupère tous les composants d'un bouton de débordement et les replace
 * dans le Ribbon. Utilisé lors de l'expansion d'un groupe.
 * 
 * Préconditions :
 * - Le groupe repasse en mode normal
 * - Le bouton contient des composants
 * 
 * Postconditions :
 * - Le bouton est vidé (clearComponents)
 * - Les composants sont marqués comme devant être dans le Ribbon
 * - Le prochain syncComponentsWithModel les ajoutera automatiquement
 * 
 * @param groupIndex index du groupe à restaurer
 * @param button le bouton de débordement à vider
 */
public void transferComponentsFromOverflow(int groupIndex, RibbonOverflowButton button) {
    // Validation
    if (button == null) {
        return;
    }
    
    if (syncingWithModel) {
        return;
    }
    
    if (debugMode) {
        System.out.println(">>> TRANSFERT Overflow → Ribbon - Groupe " + groupIndex);
    }
    
    // ÉTAPE 1 : RÉCUPÉRER LA LISTE DES COMPOSANTS CACHÉS
    // ---------------------------------------------------
    List<JComponent> hiddenComponents = button.getHiddenComponents();
    
    if (debugMode) {
        System.out.println("   - Composants dans le bouton: " + hiddenComponents.size());
    }
    
    // ÉTAPE 2 : VIDER LE BOUTON
    // --------------------------
    // Important : on vide AVANT de récupérer les composants
    // pour éviter qu'ils ne soient encore référencés
    button.clearComponents();
    
    // ÉTAPE 3 : NETTOYER LE BOUTON DU RIBBON
    // ---------------------------------------
    // Retirer physiquement le bouton du conteneur
    if (button.getParent() == this) {
        removeComponentSafely(button);
    }
    
    // ÉTAPE 4 : FORCER LA RESYNCHRONISATION
    // --------------------------------------
    // Les composants vont être automatiquement ré-ajoutés au Ribbon
    // lors du prochain syncComponentsWithModel car le groupe n'est plus collapsed
    // On déclenche une mise à jour immédiate
    revalidate();
    repaint();
    
    if (debugMode) {
        System.out.println("<<< TRANSFERT TERMINÉ, resync déclenchée");
    }
}


// Ajouter cette constante en haut de la classe pour le debug
private static final boolean debugMode = false; // Mettre à true pour tracer

    /**
     * Crée un Component pour une valeur en utilisant le GroupRenderer.
     */
    private Component createComponentForValue(Object value, int groupIndex, int position) {
        if (value == null) {
            return null;
        }

        // Si la valeur est déjà un Component, on le retourne tel quel
        if (value instanceof Component) {
            return (Component) value;
        }

        // Sinon, on utilise le renderer
        return groupRenderer.getGroupComponent(
                this, // HRibbon
                value, // Objet à rendre
                groupIndex, // Index du groupe
                position, // Position dans le groupe
                false, // isSelected (à implémenter plus tard)
                false // hasFocus (à implémenter plus tard)
        );
    }

    /**
     * Reçoit les notifications de changement du modèle de données.
     */
    @Override
    public void ribbonChanged(HRibbonModelEvent e) {
        // Synchronise d'abord les composants avec le modèle
        syncComponentsWithModel();

        // Ensuite, gère les changements spécifiques
        if (e.isGlobalChange()) {
            handleGlobalChange();
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
     * Gère un changement global (toutes données).
     */
    private void handleGlobalChange() {
        // La synchronisation a déjà été faite par syncComponentsWithModel()
        // Il ne reste qu'à gérer la création des groupes si nécessaire
        if (autoCreateGroupsFromModel && groupModel != null) {
            createGroupsFromModel();
        }
    }

    /**
     * Gère les changements au niveau groupe (ajout/suppression/déplacement).
     */
    private void handleGroupChange(HRibbonModelEvent e) {
        // La synchronisation a déjà été faite par syncComponentsWithModel()
        // Gestion spécifique des groupes pour l'auto-création
        if (autoCreateGroupsFromModel && groupModel != null) {
            int groupIndex = e.getGroupIndex();

            switch (e.getType()) {
                case HRibbonModelEvent.INSERT:
                    Object groupIdentifier = model.getGroupIdentifier(groupIndex);
                    groupModel.addGroup(groupIdentifier);
                    break;

                case HRibbonModelEvent.DELETE:
                    groupModel.removeGroup(groupIndex);
                    break;

                case HRibbonModelEvent.MOVE:
                    groupModel.moveGroup(e.getPosition(), e.getToPosition());
                    break;
            }
        }
    }

    /**
     * Gère les changements au niveau valeur (composant).
     */
    private void handleValueChange(HRibbonModelEvent e) {
        // La synchronisation a déjà été faite par syncComponentsWithModel()
        // Pas d'action supplémentaire nécessaire
        // Les composants ont déjà été ajoutés/retirés
    }

// =========================================================================
// MÉTHODES PRIVÉES POUR LA GESTION DES COMPOSANTS
// =========================================================================
    /**
     * Ajoute un composant physique au conteneur HRibbon. Méthode
     * package-private pour usage interne.
     */
    void addComponentToContainer(Component component) {
        if (component != null && component.getParent() != this) {
            super.add(component); // Appel à JComponent.add()
            displayedComponents.add(component);
        }
    }

    /**
     * Retire un composant physique du conteneur HRibbon.
     */
    private void removeComponentFromContainer(Component component) {
        if (component != null && component.getParent() == this) {
            super.remove(component); // Appel à JComponent.remove()
            displayedComponents.remove(component);
        }
    }
    
    /**
 * Retire un composant physique du conteneur HRibbon de manière sécurisée.
 * Version publique pour permettre au LayoutManager de nettoyer proprement.
 * 
 * Cette méthode garantit que :
 * - Le composant est retiré de l'arbre Swing
 * - displayedComponents est mis à jour
 * 
 * @param component le composant à retirer
 */
public void removeComponentSafely(Component component) {
    removeComponentFromContainer(component);
}
    

    /**
     * Retire tous les composants du conteneur HRibbon.
     */
    private void removeAllComponents() {
        Component[] components = getComponents();
        for (Component comp : components) {
            super.remove(comp);
        }
        displayedComponents.clear();
    }

    /**
     * Ajoute tous les composants d'un groupe depuis le modèle.
     */
    private void addAllComponentsFromGroup(int groupIndex) {
        if (model == null) {
            return;
        }

        int valueCount = model.getValueCount(groupIndex);
        for (int position = 0; position < valueCount; position++) {
            Object value = model.getValueAt(position, groupIndex);
            if (value instanceof Component) {
                addComponentToContainer((Component) value);
            }
        }
    }

    /**
     * Retire tous les composants d'un groupe.
     */
    private void removeAllComponentsFromGroup(int groupIndex) {
        // Trouve tous les composants de ce groupe et les retire
        // Note: Cette implémentation nécessite de savoir quel composant appartient à quel groupe
        // Pour l'instant, nous allons simplifier et rafraîchir tout
        // Une optimisation serait d'ajouter un mapping groupe->composants
        handleGlobalChange(); // Solution temporaire
    }

    /**
     * Retire un composant spécifique à une position dans un groupe.
     */
    private void removeComponentAt(int position, int groupIndex) {
        // Pour trouver le bon composant, nous devons parcourir tous les composants
        // et vérifier s'ils correspondent à cette position/groupe
        // Solution temporaire : rafraîchir tout
        handleGlobalChange();
    }

    /**
     * Rafraîchit l'affichage d'un composant.
     */
    private void refreshComponentAt(int position, int groupIndex) {
        // Invalide le composant pour forcer un repaint
        Object value = model.getValueAt(position, groupIndex);
        if (value instanceof Component) {
            Component comp = (Component) value;
            comp.revalidate();
            comp.repaint();
        }
    }

    /**
     * Gère le déplacement d'un groupe (réindexation des composants).
     */
    private void handleGroupMove(int fromIndex, int toIndex) {
        // Pour l'instant, rafraîchir tout
        // Une optimisation consisterait à déplacer seulement les composants concernés
        handleGlobalChange();
    }

// =========================================================================
// REDÉFINITION DES MÉTHODES D'AJOUT DE COMPOSANT
// =========================================================================
    /**
     * Empêche l'ajout direct de composants au HRibbon. Les composants doivent
     * passer par le modèle.
     */
    @Override
    public Component add(Component comp) {
        throw new UnsupportedOperationException(
                "Use addComponent() or modify the HRibbonModel to add components"
        );
    }

    /**
     * Empêche l'ajout direct de composants au HRibbon.
     */
    @Override
    public Component add(Component comp, int index) {
        throw new UnsupportedOperationException(
                "Use insertComponent() or modify the HRibbonModel to add components"
        );
    }

    /**
     * Empêche l'ajout direct de composants au HRibbon.
     */
    @Override
    public void add(Component comp, Object constraints) {
        throw new UnsupportedOperationException(
                "Components must be added through the HRibbonModel"
        );
    }

    /**
     * Empêche l'ajout direct de composants au HRibbon.
     */
    @Override
    public void add(Component comp, Object constraints, int index) {
        throw new UnsupportedOperationException(
                "Components must be added through the HRibbonModel"
        );
    }

    // =========================================================================
    // IMPLÉMENTATION DE HRibbonGroupListener
    // =========================================================================
    @Override
    public void groupAdded(HRibbonGroupEvent e) {
        if (isEditing()) {
            stopEditing();
        }

        // CHANGEMENTS DU NOUVEAU GROUPE
        HRibbonGroup group = groupModel.getHRibbonGroup(e.getToIndex());
        if (group != null) {
            group.addPropertyChangeListener(getGroupPropertyChangeListener());
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

        // NETTOYER LE HEADER DE CE GROUPE (si on a un cache)
        if (layout != null) {
            layout.removeHeaderForGroup(e.getFromIndex());
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
            defaultModel.addValue(component, groupIndex); // Component est un Object
            // Le modèle notifiera HRibbon via ribbonChanged()
            // qui appellera syncComponentsWithModel()
        } else {
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
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.addValue(component, groupIdentifier);
            // Le modèle notifiera HRibbon via ribbonChanged()
            // qui appellera syncComponentsWithModel()
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support adding components directly");
        }
    }

    public void addValue(Object value, int groupIndex) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.addValue(value, groupIndex);
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support adding values");
        }
    }

    public void insertValue(Object value, int position, int groupIndex) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (model instanceof DefaultHRibbonModel) {
            DefaultHRibbonModel defaultModel = (DefaultHRibbonModel) model;
            defaultModel.insertValueAt(value, position, groupIndex);
        } else {
            throw new UnsupportedOperationException(
                    "Model does not support inserting values");
        }
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
        throw new IllegalArgumentException("index cannot be null");
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

        if (model instanceof DefaultHRibbonModel) {
            ((DefaultHRibbonModel) model).addGroup(groupIdentifier);
            // Le reste est géré automatiquement par les listeners
            return;
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
            addGroup(group.getGroupIdentifier());
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
     * Retourne le Component correspondant à une valeur spécifique dans le
     * modèle. Utilisé par le LayoutManager pour obtenir les Components à
     * positionner.
     *
     * @param value la valeur dans le modèle
     * @param groupIndex l'index du groupe
     * @param position la position dans le groupe
     * @return le Component associé, ou null si non trouvé
     */
    public Component getComponentForValue(Object value, int groupIndex, int position) {
        // 1. Vérifie les paramètres
        if (value == null) {
            return null;
        }

        // 2. Parcourt le cache componentInfoMap
        for (Map.Entry<Component, ComponentInfo> entry : componentInfoMap.entrySet()) {
            ComponentInfo info = entry.getValue();

            // Compare les indices ET les valeurs (avec equals() pour les objets)
            if (info.groupIndex == groupIndex
                    && info.position == position
                    && Objects.equals(info.value, value)) {
                return entry.getKey();
            }
        }

        // 3. Si non trouvé dans le cache, essaie de le créer via le renderer
        return createComponentForValue(value, groupIndex, position);
    }

    /**
     * Classe interne pour stocker les informations d'un component.
     */
    private class ComponentInfo {

        final int groupIndex;
        final int position;
        final Object value;

        ComponentInfo(int groupIndex, int position, Object value) {
            this.groupIndex = groupIndex;
            this.position = position;
            this.value = value;
        }
    }

    /**
     * Définit la position des headers de groupe.
     *
     * @param alignment HEADER_NORTH, HEADER_SOUTH, HEADER_WEST, HEADER_EAST ou
     * HEADER_HIDDEN
     * @throws IllegalArgumentException si l'alignement n'est pas valide
     */
    public void setHeaderAlignment(int alignment) {
        if (alignment != HEADER_NORTH && alignment != HEADER_SOUTH
                && alignment != HEADER_WEST && alignment != HEADER_EAST && alignment != HEADER_HIDDEN) {
            throw new IllegalArgumentException("Alignment must be HEADER_NORTH, HEADER_SOUTH, HEADER_WEST, HEADER_EAST or HEADER_HIDDEN");
        }
        if (this.headerAlignment != alignment) {
            int old = this.headerAlignment;
            this.headerAlignment = alignment;
            firePropertyChange("headerAlignment", old, alignment);

            // INVALIDER TOUS LES HEADERS
            if (layout != null) {
                layout.invalidateAllHeaders();
                layout.invalidateLayout(this);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * Retourne la position actuelle des headers.
     *
     * @return HEADER_NORTH, HEADER_SOUTH ou HEADER_HIDDEN
     */
    public int getHeaderAlignment() {
        return headerAlignment;
    }

    /**
     * Définit la hauteur des headers en pixels.
     *
     * @param height hauteur en pixels (minimum 0)
     * @throws IllegalArgumentException si la hauteur est négative
     */
    public void setHeaderHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException("Header height cannot be negative");
        }
        if (this.headerHeight != height) {
            int old = this.headerHeight;
            this.headerHeight = height;
            firePropertyChange("headerHeight", old, height);

            // INVALIDER TOUS LES HEADERS (car leur taille change)
            if (layout != null) {
                layout.invalidateAllHeaders();
                layout.invalidateLayout(this);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * Retourne la hauteur actuelle des headers.
     *
     * @return hauteur en pixels
     */
    public int getHeaderHeight() {
        return headerHeight;
    }

    /**
     * Définit la largeur des headers en pixels (pour WEST/EAST).
     *
     * @param width largeur en pixels (minimum 0)
     * @throws IllegalArgumentException si la largeur est négative
     */
    public void setHeaderWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Header width cannot be negative");
        }
        if (this.headerWidth != width) {
            int old = this.headerWidth;
            this.headerWidth = width;
            firePropertyChange("headerWidth", old, width);

            // INVALIDER TOUS LES HEADERS (car leur taille change)
            if (layout != null) {
                layout.invalidateAllHeaders();
                layout.invalidateLayout(this);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * Retourne la largeur actuelle des headers (pour WEST/EAST).
     *
     * @return largeur en pixels
     */
    public int getHeaderWidth() {
        return headerWidth;
    }

    /**
     * Vérifie si les headers sont actuellement visibles.
     *
     * @return true si HEADER_HIDDEN n'est pas défini
     */
    public boolean isHeaderVisible() {
        return headerAlignment != HEADER_HIDDEN;
    }

    /**
     * Méthode interne pour ajouter des composants headers.
     */
    public void addHeaderComponent(Component header) {
        if (header == null) {
            return;
        }

        // Vérifier qu'on n'ajoute pas deux fois le même header
        if (header.getParent() != this) {
            super.add(header);
        }
    }

    /**
     * Méthode interne pour retirer un header.
     */
    public void removeHeaderComponent(Component header) {
        if (header != null && header.getParent() == this) {
            super.remove(header);
        }
    }

    /**
     * Retourne la valeur du header pour un groupe spécifique. Utilisé par le
     * LayoutManager pour créer les composants headers.
     */
    public Object getHeaderValue(int groupIndex) {
        if (groupModel != null) {
            HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
            if (group != null) {
                Object headerValue = group.getHeaderValue();
                if (headerValue != null) {
                    return headerValue;
                }
                // à défaut on retourne l'identifiant du groupe
                return group.getGroupIdentifier();
            }
        }
        return null;
    }

    /**
     * Crée ou retourne le listener pour les changements de propriétés des
     * groupes.
     */
    private PropertyChangeListener getGroupPropertyChangeListener() {
        if (groupPropertyChangeListener == null) {
            groupPropertyChangeListener = evt -> {
                String propertyName = evt.getPropertyName();

                // Si le headerValue a changé
                if ("headerValue".equals(propertyName)) {
                    // Trouver quel groupe a changé
                    HRibbonGroup changedGroup = (HRibbonGroup) evt.getSource();

                    // Trouver l'index du groupe
                    for (int i = 0; i < groupModel.getGroupCount(); i++) {
                        if (groupModel.getHRibbonGroup(i) == changedGroup) {
                            // Invalider le header de ce groupe
                            invalidateHeader(i);
                            break;
                        }
                    }
                }
            };
        }
        return groupPropertyChangeListener;
    }

    /**
     * Invalide le header d'un groupe spécifique. Le header sera recréé au
     * prochain layout.
     */
    private void invalidateHeader(int groupIndex) {
        if (layout != null) {
            // Invalider le layout pour forcer un recalcul
            layout.invalidateLayout(this);

            // Forcer un re-layout et repaint
            revalidate();
            repaint();
        }
    }

    /**
     * Définit si le Ribbon doit forcer le contenu à remplir la hauteur
     * disponible. Par défaut : false (le contenu utilise sa hauteur préférée
     * calculée).
     *
     * @param fills true pour forcer le remplissage vertical, false pour limiter
     * la hauteur au preferredSize calculé
     */
    public void setFillsViewportHeight(boolean fills) {
        if (this.fillsViewportHeight != fills) {
            this.fillsViewportHeight = fills;
            // Invalider le layout / redessiner
            if (layout != null) {
                layout.invalidateLayout(this);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * Indique si le Ribbon remplit la hauteur disponible.
     *
     * @return true si le contenu est étiré pour remplir la hauteur du parent
     */
    public boolean isFillsViewportHeight() {
        return fillsViewportHeight;
    }

    public boolean getFillsViewportHeight() {
        return isFillsViewportHeight();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        if (pref == null) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        switch (heightPolicy) {
            case FILL_PARENT:
                // Autoriser l'étirement vertical si la politique le demande
                return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            case FIXED:
                // Largeur infinie, hauteur limitée à la fixedHeight (ou preferred si non défini)
                int fixedH = fixedHeight > 0 ? fixedHeight : pref.height;
                return new Dimension(Integer.MAX_VALUE, fixedH);
            case PREFERRED_ONLY:
            default:
                // Largeur infinie, hauteur limitée à la preferredHeight
                return new Dimension(Integer.MAX_VALUE, pref.height);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension pref = getPreferredSize();
        if (pref == null) {
            return super.getMinimumSize();
        }
        // On garantit une largeur minimum raisonnable mais la hauteur suit la politique (ou preferredHeight).
        int minWidth = 50;
        int minHeight;
        if (heightPolicy == HeightPolicy.FIXED && fixedHeight > 0) {
            minHeight = fixedHeight;
        } else {
            minHeight = pref.height;
        }
        return new Dimension(minWidth, minHeight);
    }

    public void setHeightPolicy(HeightPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("HeightPolicy cannot be null");
        }
        if (this.heightPolicy != policy) {
            this.heightPolicy = policy;
            // Invalider layout et redessiner pour appliquer la nouvelle politique
            if (layout != null) {
                layout.invalidateLayout(this);
            }
            revalidate();
            repaint();
        }
    }

    public HeightPolicy getHeightPolicy() {
        return this.heightPolicy;
    }

    /**
     * Définit une hauteur fixe utilisée lorsque heightPolicy == FIXED.
     *
     * @param height hauteur en pixels (>= 0)
     */
    public void setFixedHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException("Fixed height must be >= 0");
        }
        if (this.fixedHeight != height) {
            this.fixedHeight = height;
            if (this.heightPolicy == HeightPolicy.FIXED) {
                if (layout != null) {
                    layout.invalidateLayout(this);
                }
                revalidate();
                repaint();
            }
        }
    }

    public int getFixedHeight() {
        return this.fixedHeight;
    }

    /**
     * Retourne la hauteur de contenu préférée calculée par le
     * HRibbonLayoutManager.
     *
     * Cette valeur correspond à la hauteur nécessaire pour afficher la zone
     * "utile" du ruban (les groupes et leurs composants), hors insets (marges
     * du conteneur).
     *
     * à Eviter à appeler en boucle
     *
     * @return hauteur de contenu préférée en pixels (>= 0), excluant les
     * insets.
     */
    public int getPreferredContentHeight() {
        HRibbonLayoutManager lm = (HRibbonLayoutManager) getRubanLayout();
        if (lm != null) {
            Dimension pref = lm.preferredLayoutSize(this);
            if (pref != null) {
                Insets insets = getInsets();
                int insetV = (insets != null) ? (insets.top + insets.bottom) : 0;
                int contentH = pref.height - insetV;
                return Math.max(contentH, 0);
            }
        }

        // Fallback : utiliser la preferredSize générale
        Dimension pref = getPreferredSize();
        if (pref != null) {
            Insets insets = getInsets();
            int insetV = (insets != null) ? (insets.top + insets.bottom) : 0;
            return Math.max(pref.height - insetV, 0);
        }

        return 0;
    }

    /**
     * Retourne la marge verticale/horizontale entre un groupe et son header.
     */
    public int getHeaderMargin() {
        return headerMargin;
    }

    /**
     * Permet de configurer la marge entre groupe et header.
     */
    public void setHeaderMargin(int headerMargin) {
        this.headerMargin = Math.max(0, headerMargin);
        revalidate();
        repaint();
    }

    public Rectangle[] getGroupBounds() {
        LayoutManager lm = getLayout();
        if (lm instanceof rubban.HRibbonLayoutManager) {
            return ((rubban.HRibbonLayoutManager) lm).getGroupBounds();
        }
        return null;
    }

    /**
     * Retourne la couleur de fond par défaut pour tous les en-têtes.
     *
     * @return la couleur de fond par défaut
     */
    public Color getDefaultHeaderBackground() {
        return defaultHeaderBackground;
    }

    /**
     * Définit la couleur de fond par défaut pour tous les en-têtes. Cette
     * couleur sera utilisée pour les groupes qui n'ont pas de couleur
     * spécifique.
     *
     * @param color la nouvelle couleur de fond par défaut
     */
    public void setDefaultHeaderBackground(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("La couleur par défaut ne peut pas être null");
        }
        if (!this.defaultHeaderBackground.equals(color)) {
            this.defaultHeaderBackground = color;
            repaint(); // Redessine le ruban pour appliquer les changements
        }
    }

    /**
     * Retourne la couleur de texte par défaut pour tous les en-têtes.
     *
     * @return la couleur de texte par défaut
     */
    public Color getDefaultHeaderForeground() {
        return defaultHeaderForeground;
    }

    /**
     * Définit la couleur de texte par défaut pour tous les en-têtes. Cette
     * couleur sera utilisée pour les groupes qui n'ont pas de couleur
     * spécifique.
     *
     * @param color la nouvelle couleur de texte par défaut
     */
    public void setDefaultHeaderForeground(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("La couleur par défaut ne peut pas être null");
        }
        if (!this.defaultHeaderForeground.equals(color)) {
            this.defaultHeaderForeground = color;
            repaint();
        }
    }

    /**
     * Retourne la couleur de bordure par défaut pour tous les en-têtes.
     *
     * @return la couleur de bordure par défaut
     */
    public Color getDefaultHeaderBorderColor() {
        return defaultHeaderBorderColor;
    }

    /**
     * Définit la couleur de bordure par défaut pour tous les en-têtes. Cette
     * couleur sera utilisée pour les groupes qui n'ont pas de couleur
     * spécifique.
     *
     * @param color la nouvelle couleur de bordure par défaut
     */
    public void setDefaultHeaderBorderColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("La couleur par défaut ne peut pas être null");
        }
        if (!this.defaultHeaderBorderColor.equals(color)) {
            this.defaultHeaderBorderColor = color;
            repaint();
        }
    }

    /**
     * Retourne la taille de police par défaut pour tous les en-têtes.
     *
     * @return la taille de police par défaut en points
     */
    public int getDefaultHeaderFontSize() {
        return defaultHeaderFontSize;
    }

    /**
     * Définit la taille de police par défaut pour tous les en-têtes. Cette
     * taille sera utilisée pour les groupes qui n'ont pas de taille spécifique.
     *
     * @param size la nouvelle taille de police par défaut en points
     */
    public void setDefaultHeaderFontSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("La taille de police doit être positive");
        }
        if (this.defaultHeaderFontSize != size) {
            this.defaultHeaderFontSize = size;
            revalidate(); // Recalcule les dimensions
            repaint();
        }
    }

    /**
     * Retourne l'indicateur de police en gras par défaut pour tous les
     * en-têtes.
     *
     * @return true si la police est en gras par défaut
     */
    public boolean isDefaultHeaderFontBold() {
        return defaultHeaderFontBold;
    }

    /**
     * Définit si la police doit être en gras par défaut pour tous les en-têtes.
     * Ce paramètre sera utilisé pour les groupes qui n'ont pas de paramètre
     * spécifique.
     *
     * @param bold true pour police en gras, false pour police normale
     */
    public void setDefaultHeaderFontBold(boolean bold) {
        if (this.defaultHeaderFontBold != bold) {
            this.defaultHeaderFontBold = bold;
            repaint();
        }
    }

    /**
     * Retourne la couleur de fond par défaut pour les en-têtes au survol.
     *
     * @return la couleur de fond au survol par défaut
     */
    public Color getDefaultHeaderHoverBackground() {
        return defaultHeaderHoverBackground;
    }

    /**
     * Définit la couleur de fond par défaut pour les en-têtes au survol. Cette
     * couleur sera utilisée pour les groupes qui n'ont pas de couleur
     * spécifique.
     *
     * @param color la nouvelle couleur de fond au survol par défaut
     */
    public void setDefaultHeaderHoverBackground(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("La couleur par défaut ne peut pas être null");
        }
        if (!this.defaultHeaderHoverBackground.equals(color)) {
            this.defaultHeaderHoverBackground = color;
            repaint();
        }
    }

    /**
     * Retourne la couleur de fond par défaut pour les en-têtes sélectionnés.
     *
     * @return la couleur de fond en sélection par défaut
     */
    public Color getDefaultHeaderSelectedBackground() {
        return defaultHeaderSelectedBackground;
    }

    /**
     * Définit la couleur de fond par défaut pour les en-têtes sélectionnés.
     * Cette couleur sera utilisée pour les groupes qui n'ont pas de couleur
     * spécifique.
     *
     * @param color la nouvelle couleur de fond en sélection par défaut
     */
    public void setDefaultHeaderSelectedBackground(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("La couleur par défaut ne peut pas être null");
        }
        if (!this.defaultHeaderSelectedBackground.equals(color)) {
            this.defaultHeaderSelectedBackground = color;
            repaint();
        }
    }

    /**
     * Retourne le rayon des coins arrondis par défaut pour tous les en-têtes.
     *
     * @return le rayon des coins arrondis par défaut en pixels
     */
    public int getDefaultHeaderCornerRadius() {
        return defaultHeaderCornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis par défaut pour tous les en-têtes. Ce
     * rayon sera utilisé pour les groupes qui n'ont pas de rayon spécifique.
     *
     * @param radius le nouveau rayon des coins arrondis par défaut en pixels
     */
    public void setDefaultHeaderCornerRadius(int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Le rayon ne peut pas être négatif");
        }
        if (this.defaultHeaderCornerRadius != radius) {
            this.defaultHeaderCornerRadius = radius;
            revalidate();
            repaint();
        }
    }

    /**
     * Crée une police pour un en-tête en fonction des paramètres fournis. Si
     * certains paramètres sont null, les valeurs par défaut du ruban sont
     * utilisées.
     *
     * @param baseFont la police de base (généralement la police courante du
     * composant)
     * @param fontSize la taille de police souhaitée (ou null pour la valeur par
     * défaut)
     * @param isBold indicateur de police en gras (ou null pour la valeur par
     * défaut)
     * @return une police configurée pour l'en-tête
     */
    public Font createHeaderFont(Font baseFont, Integer fontSize, Boolean isBold) {
        if (baseFont == null) {
            baseFont = getFont();
            if (baseFont == null) {
                baseFont = new Font("Dialog", Font.PLAIN, 12);
            }
        }

        // Utiliser la taille spécifiée ou la taille par défaut
        int size = (fontSize != null) ? fontSize : this.defaultHeaderFontSize;

        // Déterminer le style (gras ou normal)
        int style = Font.PLAIN;
        if (isBold != null) {
            style = isBold ? Font.BOLD : Font.PLAIN;
        } else {
            style = this.defaultHeaderFontBold ? Font.BOLD : Font.PLAIN;
        }

        return baseFont.deriveFont(style, (float) size);
    }

   /**
 * Installe le listener qui détecte les changements de taille du parent.
 * Déclenche un nouveau layout quand le conteneur est redimensionné.
 * 
 * PROTECTION CONTRE LES BOUCLES INFINIES :
 * Utilise le flag isLayoutInProgress pour éviter que le layout déclenche
 * un nouveau resize qui déclenche un nouveau layout, etc.
 */
private void installResizeListener() {
    if (resizeListener == null) {
        resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Éviter les boucles infinies de layout
                if (isLayoutInProgress) {
                    return; // Layout déjà en cours, ne pas déclencher un nouveau
                }
                
                try {
                    isLayoutInProgress = true;
                    
                    // Déclencher un nouveau layout
                    // Le HRibbonLayoutManager va gérer le collapse/expand automatiquement
                    revalidate();
                    repaint();
                    
                } finally {
                    // Toujours réinitialiser le flag, même en cas d'exception
                    isLayoutInProgress = false;
                }
            }
        };
    }
    
    // Ajouter le listener sur le Ribbon lui-même
    addComponentListener(resizeListener);
}
    

/**
 * Ajoute un composant système au Ruban.
 * 
 * Cette méthode est destinée UNIQUEMENT aux composants internes du ruban
 * comme les boutons de débordement, les headers, etc.
 * 
 * Elle contourne la restriction des add() publics qui sont bloqués
 * pour empêcher l'ajout sauvage de composants par l'utilisateur.
 * 
 * @param component le composant système à ajouter
 */
public void addSystemComponent(JComponent component) {
    if (component == null) {
        return;
    }
    
    // Appel direct à l'implémentation de JComponent (public)
    super.add(component);
    
    // IMPORTANT : On doit aussi ajouter ce composant au cache displayedComponents
    // pour que le LayoutManager en tienne compte
    displayedComponents.add(component);
    
    // Marquer ce composant comme système (optionnel, pour debug)
    component.putClientProperty("ribbon.system.component", Boolean.TRUE);
}

    /**
     * Désinstalle le listener de redimensionnement.
     * Appelé lors de la destruction du composant.
     */
    private void uninstallResizeListener() {
        if (resizeListener != null) {
            removeComponentListener(resizeListener);
        }
    }
    
    /**
     * Override de removeNotify pour nettoyer les ressources.
     * Appelé quand le composant est retiré de son conteneur parent.
     */
    @Override
    public void removeNotify() {
        uninstallResizeListener();
        super.removeNotify();
    }

/*
 * NOTES :
 * - Le ComponentListener détecte automatiquement les changements de taille
 * - Quand le parent (JFrame, JPanel, etc.) est redimensionné, componentResized() est appelé
 * - revalidate() déclenche layoutContainer() dans HRibbonLayoutManager
 * - HRibbonLayoutManager appelle ResizeManager qui calcule les actions nécessaires
 * - Les groupes sont collapsed/expanded automatiquement selon l'espace disponible
 */
    
}
