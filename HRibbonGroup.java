/*
 * HRibbonGroup.java
 * Représente un groupe dans le ruban, décrivant uniquement la présentation d'un groupe de données.
 * Analogie stricte avec TableColumn dans JTable : cette classe ne stocke PAS les données, 
 * seulement la configuration d'affichage (largeur, padding, marges, etc.).
 * 
 * @see javax.swing.table.TableColumn
 * @see hcomponents.HRibbon.HRibbonModel
 */
package rubban;

import java.util.Objects;
import javax.swing.event.SwingPropertyChangeSupport;

public class HRibbonGroup {

    // =========================================================================
    // PROPRIÉTÉS D'IDENTIFICATION
    // =========================================================================
    /**
     * Identifiant unique du groupe, utilisé pour le lier à une colonne de
     * données dans le HRibbonModel. Correspond au groupIdentifier du modèle.
     */
    private Object groupIdentifier;

    /**
     * Texte affiché dans l'en-tête du groupe. Peut être différent de
     * groupIdentifier pour l'internationalisation ou l'affichage.
     */
    private Object headerValue;

    /**
     * Index de ce groupe dans le HRibbonModel. Permet de savoir quelle colonne
     * de données ce groupe représente.
     */
    private int modelIndex;

    // =========================================================================
    // PROPRIÉTÉS DE DIMENSIONNEMENT
    // =========================================================================
    /**
     * Largeur actuelle du groupe en pixels, telle affichée à l'écran.
     */
    private int width;

    /**
     * Largeur préférée du groupe en pixels, utilisée pour le calcul du layout.
     */
    private int preferredWidth;

    /**
     * Largeur minimale autorisée pour ce groupe. 0 = pas de minimum imposé.
     */
    private int minWidth = 50;

    /**
     * Largeur maximale autorisée pour ce groupe. 0 = pas de maximum imposé.
     */
    private int maxWidth = 0;

    /**
     * Indique si l'utilisateur peut redimensionner ce groupe via l'interface.
     */
    private boolean isResizable = true;

    // =========================================================================
    // PROPRIÉTÉS DE MISE EN PAGE INTERNE
    // =========================================================================
    /**
     * Espacement horizontal entre les composants Swing à l'intérieur du groupe.
     */
    private int componentSpacing = 2;

    /**
     * Marge interne (padding) du groupe, entre la bordure et les composants.
     */
    private int padding = 6;

    // =========================================================================
    // SUPPORT POUR LES NOTIFICATIONS (PATTERN OBSERVER)
    // =========================================================================
    /**
     * Gestionnaire des écouteurs PropertyChangeListener, notifie les
     * changements de propriétés (largeur, padding, etc.) aux composants
     * intéressés.
     */
    private SwingPropertyChangeSupport changeSupport;

    // =========================================================================
    // CONSTRUCTEURS
    // =========================================================================
    /**
     * Constructeur par défaut. Crée un groupe sans identifiant avec index -1.
     * Utile pour les groupes temporaires ou à initialiser ultérieurement.
     */
    public HRibbonGroup() {
        this(null, -1);
    }

    /**
     * Constructeur avec identifiant de groupe.
     *
     * @param groupIdentifier l'identifiant liant ce groupe à une colonne du
     * modèle
     * @throws IllegalArgumentException si groupIdentifier est null
     */
    public HRibbonGroup(Object groupIdentifier) {
        this(groupIdentifier, -1);
    }

    /**
     * Constructeur complet avec identifiant et index modèle.
     *
     * @param groupIdentifier l'identifiant du groupe dans le modèle
     * @param modelIndex l'index de la colonne dans HRibbonModel
     */
    public HRibbonGroup(Object groupIdentifier, int modelIndex) {
        this.groupIdentifier = groupIdentifier;
        this.modelIndex = modelIndex;
        this.preferredWidth = 180; // Largeur par défaut raisonnable
        this.width = this.preferredWidth; // Initialise la largeur actuelle
        this.minWidth = 80;
    }

    // =========================================================================
    // GETTERS ET SETTERS - PROPRIÉTÉS D'IDENTIFICATION
    // =========================================================================
    /**
     * Retourne l'identifiant du groupe, utilisé pour le lier au HRibbonModel.
     *
     * @return l'identifiant du groupe, peut être null pour les groupes non liés
     */
    public Object getGroupIdentifier() {
        return groupIdentifier;
    }

    /**
     * Définit l'identifiant du groupe et notifie les écouteurs du changement.
     *
     * @param newIdentifier le nouvel identifiant du groupe
     * @throws IllegalArgumentException si newIdentifier est null
     */
    public void setGroupIdentifier(Object newIdentifier) {
        if (newIdentifier == null) {
            throw new IllegalArgumentException("Group identifier cannot be null");
        }
        if (!Objects.equals(this.groupIdentifier, newIdentifier)) {
            Object old = this.groupIdentifier;
            this.groupIdentifier = newIdentifier;
            firePropertyChange("groupIdentifier", old, newIdentifier);
        }
    }

    /**
     * Retourne la valeur affichée dans l'en-tête du groupe. Si headerValue
     * n'est pas défini, retourne groupIdentifier.
     *
     * @return le texte de l'en-tête, jamais null si groupIdentifier n'est pas
     * null
     */
    public Object getHeaderValue() {
        return headerValue != null ? headerValue : groupIdentifier;
    }

    /**
     * Définit la valeur d'en-tête du groupe (affichage uniquement).
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
     * Retourne l'index de ce groupe dans le HRibbonModel.
     *
     * @return l'index modèle, ou -1 si non lié à un modèle
     */
    public int getModelIndex() {
        return modelIndex;
    }

    /**
     * Définit l'index de ce groupe dans le HRibbonModel. Utilisé lors du
     * réarrangement ou de l'ajout/suppression de groupes.
     *
     * @param newModelIndex le nouvel index dans le modèle
     */
    public void setModelIndex(int newModelIndex) {
        if (this.modelIndex != newModelIndex) {
            int old = this.modelIndex;
            this.modelIndex = newModelIndex;
            firePropertyChange("modelIndex", old, newModelIndex);
        }
    }

    /**
     * Alias de getGroupIdentifier() pour compatibilité avec le code existant.
     *
     * @return l'identifiant du groupe
     * @see #getGroupIdentifier()
     */
    public Object getIdentifier() {
        return getGroupIdentifier();
    }

    /**
     * Alias de setGroupIdentifier() pour compatibilité.
     *
     * @param newIdentifier le nouvel identifiant
     * @see #setGroupIdentifier(Object)
     */
    public void setIdentifier(Object newIdentifier) {
        setGroupIdentifier(newIdentifier);
    }

    // =========================================================================
    // GETTERS ET SETTERS - PROPRIÉTÉS DE DIMENSIONNEMENT
    // =========================================================================
    /**
     * Retourne la largeur actuelle d'affichage du groupe.
     *
     * @return la largeur en pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Définit la largeur actuelle du groupe et notifie les écouteurs. La
     * largeur est automatiquement contrainte entre minWidth et maxWidth.
     *
     * @param newWidth la nouvelle largeur en pixels
     */
    public void setWidth(int newWidth) {
        // Applique les contraintes min/max
        if (minWidth > 0 && newWidth < minWidth) {
            newWidth = minWidth;
        }
        if (maxWidth > 0 && newWidth > maxWidth) {
            newWidth = maxWidth;
        }

        if (this.width != newWidth) {
            int old = this.width;
            this.width = newWidth;
            firePropertyChange("width", old, newWidth);
        }
    }

    /**
     * Retourne la largeur préférée du groupe pour le calcul du layout.
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
     * Retourne la largeur minimale autorisée.
     *
     * @return la largeur minimale en pixels, 0 si aucune limite
     */
    public int getMinWidth() {
        return minWidth;
    }

    /**
     * Définit la largeur minimale autorisée.
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
     * Retourne la largeur maximale autorisée.
     *
     * @return la largeur maximale en pixels, 0 si aucune limite
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Définit la largeur maximale autorisée.
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
     * Indique si ce groupe peut être redimensionné par l'utilisateur.
     *
     * @return true si l'utilisateur peut modifier la largeur
     */
    public boolean isResizable() {
        return isResizable;
    }

    /**
     * Active ou désactive le redimensionnement par l'utilisateur.
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

    // =========================================================================
    // GETTERS ET SETTERS - PROPRIÉTÉS DE MISE EN PAGE
    // =========================================================================
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
     * Retourne l'espacement entre les composants à l'intérieur du groupe.
     *
     * @return l'espacement en pixels
     */
    public int getComponentSpacing() {
        return componentSpacing;
    }

    /**
     * Définit l'espacement entre les composants.
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

    // =========================================================================
    // GESTION DES ÉCOUTEURS (PATTERN OBSERVER)
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
     * Notifie tous les écouteurs enregistrés d'un changement de propriété.
     * Méthode protected pour usage interne uniquement.
     *
     * @param propertyName le nom de la propriété modifiée
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
     * Retourne le gestionnaire d'écouteurs, en le créant si nécessaire. Pattern
     * lazy-initialization.
     *
     * @return le SwingPropertyChangeSupport pour ce groupe
     */
    private SwingPropertyChangeSupport getChangeSupport() {
        if (changeSupport == null) {
            changeSupport = new SwingPropertyChangeSupport(this);
        }
        return changeSupport;
    }

    // =========================================================================
    // MÉTHODES D'UTILITÉ
    // =========================================================================
    /**
     * Retourne une représentation textuelle du groupe pour le débogage.
     *
     * @return une chaîne descriptive du groupe
     */
    @Override
    public String toString() {
        return "HRibbonGroup[identifier=" + groupIdentifier
                + ", modelIndex=" + modelIndex
                + ", width=" + width
                + ", preferred=" + preferredWidth + "]";
    }
}
