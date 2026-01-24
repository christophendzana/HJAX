/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hcomponents.HRibbon;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.event.SwingPropertyChangeSupport;

/**
 * Représente un groupe dans le ruban, contenant plusieurs composants.
 * Analogie : similaire à TableColumn dans JTable, mais adapté pour un ruban. 
 * 
 * @see javax.swing.table.TableColumn
 */
public class HRibbonGroup {

    // =========================================================================
    // PROPRIÉTÉS D'IDENTIFICATION
    // =========================================================================
    
    /** Identifiant unique du groupe (peut être utilisé comme nom). */
    private Object groupIdentifier;
    
    /** Valeur affichée dans l'en-tête du groupe (si différent de l'identifiant). */
    private Object headerValue;
    
    /** Index de ce groupe dans le modèle de données. */
    private int modelIndex;
    
    /** Index de ce groupe dans la vue (peut différer si les groupes sont réorganisés). */
    private int viewIndex = -1;
    
    // =========================================================================
    // PROPRIÉTÉS DE DIMENSIONNEMENT
    // =========================================================================
    
    /** Largeur actuelle du groupe en pixels. */
    private int width;
    
    /** Largeur préférée du groupe en pixels. */
    private int preferredWidth;
    
    /** Largeur minimale autorisée pour ce groupe. */
    private int minWidth;
    
    /** Largeur maximale autorisée pour ce groupe. */
    private int maxWidth;
    
    /** Largeur par défaut du groupe (utilisée pour l'initialisation). */
    private int defaultWidth = 75;
    
    /** Indique si l'utilisateur peut redimensionner ce groupe. */
    private boolean isResizable = true;
    
    // =========================================================================
    // PROPRIÉTÉS DE MISE EN PAGE
    // =========================================================================
    
    /** Espacement entre les composants à l'intérieur du groupe. */
    private int componentSpacing = 2;
    
    /** Marge interne (padding) du groupe. */
    private int padding = 6;
    
    // =========================================================================
    // DONNÉES
    // =========================================================================
    
    /** Liste des composants contenus dans ce groupe. */
    private ArrayList<Object> components;
    
    // =========================================================================
    // SUPPORT POUR LES NOTIFICATIONS
    // =========================================================================
    
    /** Gestionnaire des écouteurs de changement de propriétés. */
    private SwingPropertyChangeSupport changeSupport;
    
    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    
    /**
     * Constructeur par défaut. Crée un groupe vide sans identifiant.
     */
    public HRibbonGroup() {
        this(null, new ArrayList<>(), 0);
    }
    
    /**
     * Constructeur avec identifiant.
     * 
     * @param groupIdentifier l'identifiant du groupe
     */
    public HRibbonGroup(Object groupIdentifier) {
        this(groupIdentifier, new ArrayList<>(), 0);
    }
    
    /**
     * Constructeur avec liste de composants.
     * 
     * @param components la liste initiale des composants
     */
    public HRibbonGroup(ArrayList<Object> components) {
        this(null, components, 0);
    }
    
    /**
     * Constructeur complet.
     * 
     * @param groupIdentifier l'identifiant du groupe
     * @param components la liste initiale des composants
     * @param modelIndex l'index de ce groupe dans le modèle
     */
    public HRibbonGroup(Object groupIdentifier, ArrayList<Object> components, int modelIndex) {
        this.groupIdentifier = groupIdentifier;
        this.components = (components != null) ? components : new ArrayList<>();
        this.modelIndex = modelIndex;
        this.viewIndex = modelIndex; // Par défaut, vue = modèle
        
        // Initialise les largeurs
        this.width = calculatePreferredWidth();
        this.preferredWidth = this.width;
    }
    
    // =========================================================================
    // MÉTHODES D'ACCÈS AUX PROPRIÉTÉS D'IDENTIFICATION
    // =========================================================================
    
    /**
     * Retourne l'identifiant du groupe.
     * 
     * @return l'identifiant du groupe
     */
    public Object getGroupIdentifier() {
        return groupIdentifier;
    }
    
    /**
     * Définit l'identifiant du groupe.
     * 
     * @param newIdentifier le nouvel identifiant
     */
    public void setGroupIdentifier(Object newIdentifier) {
        if (!Objects.equals(this.groupIdentifier, newIdentifier)) {
            Object old = this.groupIdentifier;
            this.groupIdentifier = newIdentifier;
            firePropertyChange("groupIdentifier", old, newIdentifier);
        }
    }
    
    /**
     * Retourne la valeur d'en-tête du groupe.
     * Si aucune valeur d'en-tête n'est définie, retourne l'identifiant.
     * 
     * @return la valeur à afficher dans l'en-tête
     */
    public Object getHeaderValue() {
        return headerValue != null ? headerValue : groupIdentifier;
    }
    
    /**
     * Définit la valeur d'en-tête du groupe.
     * 
     * @param headerValue la nouvelle valeur d'en-tête
     */
    public void setHeaderValue(Object headerValue) {
        if (!Objects.equals(this.headerValue, headerValue)) {
            Object old = this.headerValue;
            this.headerValue = headerValue;
            firePropertyChange("headerValue", old, headerValue);
        }
    }
    
    /**
     * Retourne l'index de ce groupe dans le modèle de données.
     * 
     * @return l'index modèle
     */
    public int getModelIndex() {
        return modelIndex;
    }
    
    /**
     * Définit l'index de ce groupe dans le modèle de données.
     * 
     * @param newModelIndex le nouvel index modèle
     */
    public void setModelIndex(int newModelIndex) {
        if (this.modelIndex != newModelIndex) {
            int old = this.modelIndex;
            this.modelIndex = newModelIndex;
            firePropertyChange("modelIndex", old, newModelIndex);
        }
    }
    
    /**
     * Retourne l'index de ce groupe dans la vue.
     * L'index vue peut différer de l'index modèle si les groupes sont réorganisés.
     * 
     * @return l'index vue, ou -1 si non défini
     */
    public int getViewIndex() {
        return viewIndex;
    }
    
    /**
     * Définit l'index de ce groupe dans la vue.
     * 
     * @param viewIndex le nouvel index vue
     */
    public void setViewIndex(int viewIndex) {
        if (this.viewIndex != viewIndex) {
            int old = this.viewIndex;
            this.viewIndex = viewIndex;
            firePropertyChange("viewIndex", old, viewIndex);
        }
    }
    
    /**
     * Définit l'identifiant du groupe (alias de setGroupIdentifier).
     * 
     * @param newIdentifier le nouvel identifiant
     * @see #setGroupIdentifier
     */
    public void setIdentifier(Object newIdentifier) {
        setGroupIdentifier(newIdentifier);
    }
    
    /**
     * Retourne l'identifiant du groupe (alias de getGroupIdentifier).
     * 
     * @return l'identifiant du groupe
     * @see #getGroupIdentifier
     */
    public Object getIdentifier() {
        return getGroupIdentifier();
    }
    
    // =========================================================================
    // MÉTHODES DE GESTION DES COMPOSANTS
    // =========================================================================
    
    /**
     * Retourne une liste non modifiable des composants du groupe.
     * 
     * @return la liste des composants
     */
    public List<Object> getComponents() {
        return Collections.unmodifiableList(components);
    }
    
    /**
     * Ajoute un composant au groupe.
     * 
     * @param component le composant à ajouter
     * @throws NullPointerException si le composant est null
     */
    public void addComponent(Object component) {
        if (component == null) {
            throw new NullPointerException("Component cannot be null");
        }
        
        components.add(component);
        preferredWidth = calculatePreferredWidth();
        firePropertyChange("components", null, component);
    }
    
    /**
     * Retire un composant du groupe.
     * 
     * @param component le composant à retirer
     * @return true si le composant était présent et a été retiré
     */
    public boolean removeComponent(Object component) {
        boolean removed = components.remove(component);
        if (removed) {
            preferredWidth = calculatePreferredWidth();
            firePropertyChange("components", component, null);
        }
        return removed;
    }
    
    /**
     * Retourne le nombre de composants dans ce groupe.
     * 
     * @return le nombre de composants
     */
    public int getComponentsCount() {
        return components.size();
    }
    
    // =========================================================================
    // MÉTHODES DE DIMENSIONNEMENT
    // =========================================================================
    
    /**
     * Retourne la largeur actuelle du groupe.
     * 
     * @return la largeur en pixels
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Définit la largeur actuelle du groupe.
     * 
     * @param newWidth la nouvelle largeur en pixels
     */
    public void setWidth(int newWidth) {
        if (this.width != newWidth) {
            int old = this.width;
            this.width = newWidth;
            firePropertyChange("width", old, newWidth);
        }
    }
    
    /**
     * Retourne la largeur préférée du groupe.
     * 
     * @return la largeur préférée en pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /**
     * Définit la largeur préférée du groupe.
     * 
     * @param width la nouvelle largeur préférée en pixels
     */
    public void setPreferredWidth(int width) {
        if (this.preferredWidth != width) {
            int old = this.preferredWidth;
            this.preferredWidth = width;
            firePropertyChange("preferredWidth", old, width);
        }
    }
    
    /**
     * Calcule la largeur préférée du groupe en fonction de ses composants.
     * 
     * <p>La largeur préférée est calculée comme :
     * (somme des largeurs préférées des composants) 
     * + (espacement entre composants * (nombre de composants - 1))
     * + (padding * 2)
     * 
     * <p>Le résultat est contraint par les largeurs minimale et maximale.
     * 
     * @return la largeur préférée calculée
     */
    public int calculatePreferredWidth() {
        if (components.isEmpty()) {
            return Math.max(minWidth, defaultWidth);
        }
        
        int totalWidth = 0;
        int componentCount = 0;
        
        for (Object obj : components) {
            if (obj instanceof JComponent) {
                JComponent comp = (JComponent) obj;
                totalWidth += comp.getPreferredSize().width;
                componentCount++;
            }
        }
        
        // Ajoute les espacements entre composants
        if (componentCount > 1) {
            totalWidth += componentSpacing * (componentCount - 1);
        }
        
        // Ajoute le padding interne
        totalWidth += padding * 2;
        
        // Applique les contraintes min/max
        return applyWidthConstraints(totalWidth);
    }
    
    /**
     * Applique les contraintes de largeur minimale et maximale.
     * 
     * @param calculatedWidth la largeur calculée
     * @return la largeur contrainte
     */
    private int applyWidthConstraints(int calculatedWidth) {
        if (minWidth > 0 && calculatedWidth < minWidth) {
            return minWidth;
        }
        if (maxWidth > 0 && calculatedWidth > maxWidth) {
            return maxWidth;
        }
        return calculatedWidth;
    }
    
    /**
     * Retourne la largeur maximale autorisée pour ce groupe.
     * 
     * @return la largeur maximale en pixels, 0 si aucune limite
     */
    public int getMaxWidth() {
        return maxWidth;
    }
    
    /**
     * Définit la largeur maximale autorisée pour ce groupe.
     * 
     * @param maxWidth la nouvelle largeur maximale en pixels
     */
    public void setMaxWidth(int maxWidth) {
        if (this.maxWidth != maxWidth) {
            int old = this.maxWidth;
            this.maxWidth = maxWidth;
            firePropertyChange("maxWidth", old, maxWidth);
        }
    }
    
    /**
     * Retourne la largeur minimale autorisée pour ce groupe.
     * 
     * @return la largeur minimale en pixels, 0 si aucune limite
     */
    public int getMinWidth() {
        return minWidth;
    }
    
    /**
     * Définit la largeur minimale autorisée pour ce groupe.
     * 
     * @param minWidth la nouvelle largeur minimale en pixels
     */
    public void setMinWidth(int minWidth) {
        if (this.minWidth != minWidth) {
            int old = this.minWidth;
            this.minWidth = minWidth;
            firePropertyChange("minWidth", old, minWidth);
        }
    }
    
    /**
     * Vérifie si ce groupe peut être redimensionné par l'utilisateur.
     * 
     * @return true si le groupe est redimensionnable
     */
    public boolean isResizable() {
        return isResizable;
    }
    
    /**
     * Définit si ce groupe peut être redimensionné par l'utilisateur.
     * 
     * @param isResizable true pour permettre le redimensionnement
     */
    public void setResizable(boolean isResizable) {
        if (this.isResizable != isResizable) {
            boolean old = this.isResizable;
            this.isResizable = isResizable;
            firePropertyChange("resizable", old, isResizable);
        }
    }
    
    /**
     * Retourne le padding interne du groupe.
     * 
     * @return le padding en pixels
     */
    public int getPadding() {
        return padding;
    }
    
    /**
     * Définit le padding interne du groupe.
     * 
     * @param padding le nouveau padding en pixels
     */
    public void setPadding(int padding) {
        if (this.padding != padding) {
            int old = this.padding;
            this.padding = padding;
            firePropertyChange("padding", old, padding);
        }
    }
    
    /**
     * Retourne l'espacement entre les composants dans le groupe.
     * 
     * @return l'espacement en pixels
     */
    public int getComponentSpacing() {
        return componentSpacing;
    }
    
    /**
     * Définit l'espacement entre les composants dans le groupe.
     * 
     * @param spacing le nouvel espacement en pixels
     */
    public void setComponentSpacing(int spacing) {
        if (this.componentSpacing != spacing) {
            int old = this.componentSpacing;
            this.componentSpacing = spacing;
            firePropertyChange("componentSpacing", old, spacing);
        }
    }
    
    /**
     * Retourne la largeur par défaut du groupe.
     * 
     * @return la largeur par défaut en pixels
     */
    public int getDefaultWidth() {
        return defaultWidth;
    }
    
    /**
     * Définit la largeur par défaut du groupe.
     * 
     * @param defaultWidth la nouvelle largeur par défaut en pixels
     */
    public void setDefaultWidth(int defaultWidth) {
        if (this.defaultWidth != defaultWidth) {
            int old = this.defaultWidth;
            this.defaultWidth = defaultWidth;
            firePropertyChange("defaultWidth", old, defaultWidth);
        }
    }
    
    // =========================================================================
    // GESTION DES ÉCOUTEURS DE CHANGEMENT
    // =========================================================================
    
    /**
     * Ajoute un écouteur de changement de propriétés.
     * 
     * @param listener l'écouteur à ajouter
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }
    
    /**
     * Retire un écouteur de changement de propriétés.
     * 
     * @param listener l'écouteur à retirer
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }
    
    /**
     * Notifie tous les écouteurs qu'une propriété a changé.
     * 
     * @param propertyName le nom de la propriété
     * @param oldValue l'ancienne valeur
     * @param newValue la nouvelle valeur
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        SwingPropertyChangeSupport support = changeSupport;
        if (support != null) {
            support.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    /**
     * Retourne le support pour les écouteurs de changement.
     * Crée le support si nécessaire.
     * 
     * @return le support des écouteurs
     */
    private SwingPropertyChangeSupport getChangeSupport() {
        if (changeSupport == null) {
            changeSupport = new SwingPropertyChangeSupport(this);
        }
        return changeSupport;
    }
}