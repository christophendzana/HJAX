/*
 * HRibbonGroup.java
 * Représente un groupe dans le ruban, décrivant uniquement la présentation d'un groupe de données.
 * Maintenant avec configuration complète des en-têtes (headers).
 * 
 * @see javax.swing.table.TableColumn
 * @see hcomponents.HRibbon.HRibbonModel
 */
package rubban;

import java.awt.Color;
import java.awt.Component;
import java.util.Objects;
import javax.swing.event.SwingPropertyChangeSupport;
import rubban.layout.CollapseLevel;

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
    // PROPRIÉTÉS DE REDIMENSIONNEMENT 
    // =========================================================================
    /**
     * Niveau de collapse actuel du groupe. Détermine si le groupe est affiché
     * normalement ou réduit.
     */
    private CollapseLevel currentLevel = CollapseLevel.NORMAL;

    /**
     * Largeur du groupe lorsqu'il est en mode COLLAPSED (JComboBox). Par défaut
     * 80 pixels.
     */
    private int collapsedWidth = 80;

    /**
     * Composant affiché quand le groupe est collapsed (généralement un
     * JComboBox). Cache pour éviter les recréations inutiles. Transient car ne
     * doit pas être sérialisé.
     */
    private transient RibbonOverflowButton collapsedButton = null;

    // =========================================================================
    // PROPRIÉTÉS DE MISE EN PAGE INTERNE
    // =========================================================================
    /**
     * Espacement horizontal entre les composants Swing à l'intérieur du groupe.
     */
    private int componentMargin = 2;

    /**
     * Marge interne (padding) du groupe, entre la bordure et les composants.
     */
    private int padding = 6;

    // =========================================================================
    // CONFIGURATION DES EN-TÊTES (HEADERS) - NOUVELLES PROPRIÉTÉS
    // =========================================================================
    /**
     * Couleur de fond de l'en-tête du groupe. Si null, la couleur par défaut du
     * ruban sera utilisée.
     */
    private Color headerBackground = null;

    /**
     * Couleur du texte de l'en-tête du groupe. Si null, la couleur par défaut
     * du ruban sera utilisée.
     */
    private Color headerForeground = null;

    /**
     * Couleur de la bordure de l'en-tête du groupe. Si null, la couleur par
     * défaut du ruban sera utilisée.
     */
    private Color headerBorderColor = null;

    /**
     * Taille de police de l'en-tête en points. Si null, la taille par défaut du
     * ruban sera utilisée. Exemples : 11, 12, 14, 16.
     */
    private Integer headerFontSize = null;

    /**
     * Indique si le texte de l'en-tête doit être affiché en gras. Si null, le
     * paramètre par défaut du ruban sera utilisé.
     */
    private Boolean headerFontBold = null;

    /**
     * Couleur de fond de l'en-tête lorsque la souris survole le groupe. Si
     * null, la couleur par défaut du ruban sera utilisée.
     */
    private Color headerHoverBackground = null;

    /**
     * Couleur de fond de l'en-tête lorsque le groupe est sélectionné. Si null,
     * la couleur par défaut du ruban sera utilisée.
     */
    private Color headerSelectedBackground = null;

    /**
     * Rayon des coins arrondis de l'en-tête en pixels. Si null, le rayon par
     * défaut du ruban sera utilisé. 0 = coins carrés, 5 = légèrement arrondi,
     * 15 = très arrondi.
     */
    private Integer headerCornerRadius = null;

    private Color background;

    /**
     * Préfix par défaut auquel sera ajouté le groupe index pour créer les
     * groupIdentifier initial par défaut
     */
    private static String defaultGroupNamePrefix = "Groupe ";

    /**
     * Gestionnaire des écouteurs PropertyChangeListener, notifie les
     * changements de propriétés (largeur, padding, etc.) aux composants
     * intéressés.
     */
    private SwingPropertyChangeSupport changeSupport;

    private boolean collapsible = true;      // peut-on transformer le groupe en menu ?
    private int collapseWidth = 48;          // largeur (px) du placeholder quand collapsed (par défaut)
    private boolean collapsed = false;       // état courant de collapse

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
     * Constructeur avec identifiant de groupe. Si l'identifiant est null, le
     * groupe utilisera un nom par défaut basé sur son futur index dans le
     * modèle.
     *
     * @param groupIdentifier l'identifiant liant ce groupe à une colonne du
     * modèle
     */
    public HRibbonGroup(Object groupIdentifier) {
        this(groupIdentifier, -1);
    }

    /**
     * Constructeur avec index modèle seulement. L'identifiant du groupe sera
     * généré automatiquement sous la forme "Groupe X" où X = modelIndex + 1.
     *
     * @param modelIndex l'index de la colonne dans HRibbonModel
     */
    public HRibbonGroup(int modelIndex) {
        this(null, modelIndex);  // Délègue au constructeur principal
    }

    /**
     * Constructeur complet avec identifiant et index modèle. Si l'identifiant
     * est null, un nom par défaut est généré : - Si modelIndex >= 0 : "Groupe
     * X" (où X = modelIndex + 1) - Sinon : "Groupe ?" (en attendant d'être
     * indexé)
     *
     * @param groupIdentifier l'identifiant du groupe dans le modèle
     * @param modelIndex l'index de la colonne dans HRibbonModel
     */
    public HRibbonGroup(Object groupIdentifier, int modelIndex) {
        // 1. Gestion de l'identifiant
        if (groupIdentifier == null) {
            if (modelIndex >= 0) {
                this.groupIdentifier = defaultGroupNamePrefix + (modelIndex + 1);
            } else {
                this.groupIdentifier = defaultGroupNamePrefix + "?";
            }
        } else {
            this.groupIdentifier = groupIdentifier;
        }

        // 2. Initialisation de l'headerValue avec l'identifiant
        this.headerValue = this.groupIdentifier;

        // 3. Index et dimensions
        this.modelIndex = modelIndex;
        this.preferredWidth = 180;
        this.width = this.preferredWidth;
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
 * Modifie le préfixe utilisé pour générer les noms de groupe par défaut.
 * Par défaut : "Groupe "
 */
public static void setDefaultGroupNamePrefix(String prefix) {
    if (prefix != null) {
        defaultGroupNamePrefix = prefix;
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
     * Définit une couleur de fond personnalisée pour ce groupe. Si null, le
     * Ribbon utilisera la couleur globale par défaut.
     */
    public void setBackground(Color bg) {
        if (!Objects.equals(this.background, bg)) {
            Color old = this.background;
            this.background = bg;
            firePropertyChange("background", old, bg);
        }
    }

    /**
     * Retourne la couleur de fond personnalisée pour ce groupe ou null si
     * aucune.
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Retourne l'espacement entre les composants à l'intérieur du groupe.
     *
     * @return l'espacement en pixels
     */
    public int getComponentMargin() {
        return componentMargin;
    }

    /**
     * Définit l'espacement entre les composants.
     *
     * @param spacing le nouvel espacement en pixels
     */
    public void setComponentMargin(int spacing) {
        if (this.componentMargin != spacing) {
            int old = this.componentMargin;
            this.componentMargin = spacing;
            firePropertyChange("componentMargin", old, spacing);
        }
    }

    // =========================================================================
    // GETTERS ET SETTERS - CONFIGURATION DES EN-TÊTES (NOUVEAU)
    // =========================================================================
    /**
     * Retourne la couleur de fond spécifique de l'en-tête de ce groupe. Si
     * null, la couleur par défaut du ruban doit être utilisée.
     *
     * @return la couleur de fond de l'en-tête, ou null si non définie
     */
    public Color getHeaderBackground() {
        return headerBackground;
    }

    /**
     * Définit une couleur de fond spécifique pour l'en-tête de ce groupe. Si
     * null, la couleur par défaut du ruban sera utilisée.
     *
     * @param headerBackground la nouvelle couleur de fond de l'en-tête
     */
    public void setHeaderBackground(Color headerBackground) {
        if (!Objects.equals(this.headerBackground, headerBackground)) {
            Color old = this.headerBackground;
            this.headerBackground = headerBackground;
            firePropertyChange("headerBackground", old, headerBackground);
        }
    }

    /**
     * Retourne la couleur du texte spécifique de l'en-tête de ce groupe. Si
     * null, la couleur par défaut du ruban doit être utilisée.
     *
     * @return la couleur du texte de l'en-tête, ou null si non définie
     */
    public Color getHeaderForeground() {
        return headerForeground;
    }

    /**
     * Définit une couleur de texte spécifique pour l'en-tête de ce groupe. Si
     * null, la couleur par défaut du ruban sera utilisée.
     *
     * @param headerForeground la nouvelle couleur du texte de l'en-tête
     */
    public void setHeaderForeground(Color headerForeground) {
        if (!Objects.equals(this.headerForeground, headerForeground)) {
            Color old = this.headerForeground;
            this.headerForeground = headerForeground;
            firePropertyChange("headerForeground", old, headerForeground);
        }
    }

    /**
     * Retourne la couleur de bordure spécifique de l'en-tête de ce groupe. Si
     * null, la couleur par défaut du ruban doit être utilisée.
     *
     * @return la couleur de bordure de l'en-tête, ou null si non définie
     */
    public Color getHeaderBorderColor() {
        return headerBorderColor;
    }

    /**
     * Définit une couleur de bordure spécifique pour l'en-tête de ce groupe. Si
     * null, la couleur par défaut du ruban sera utilisée.
     *
     * @param headerBorderColor la nouvelle couleur de bordure de l'en-tête
     */
    public void setHeaderBorderColor(Color headerBorderColor) {
        if (!Objects.equals(this.headerBorderColor, headerBorderColor)) {
            Color old = this.headerBorderColor;
            this.headerBorderColor = headerBorderColor;
            firePropertyChange("headerBorderColor", old, headerBorderColor);
        }
    }

    /**
     * Retourne la taille de police spécifique de l'en-tête de ce groupe. Si
     * null, la taille par défaut du ruban doit être utilisée.
     *
     * @return la taille de police en points, ou null si non définie
     */
    public Integer getHeaderFontSize() {
        return headerFontSize;
    }

    /**
     * Définit une taille de police spécifique pour l'en-tête de ce groupe. Si
     * null, la taille par défaut du ruban sera utilisée.
     *
     * @param headerFontSize la nouvelle taille de police en points
     */
    public void setHeaderFontSize(Integer headerFontSize) {
        if (!Objects.equals(this.headerFontSize, headerFontSize)) {
            Integer old = this.headerFontSize;
            this.headerFontSize = headerFontSize;
            firePropertyChange("headerFontSize", old, headerFontSize);
        }
    }

    /**
     * Retourne l'indicateur de police en gras spécifique de l'en-tête de ce
     * groupe. Si null, le paramètre par défaut du ruban doit être utilisée.
     *
     * @return true si la police doit être en gras, null si non défini
     */
    public Boolean getHeaderFontBold() {
        return headerFontBold;
    }

    /**
     * Définit si la police de l'en-tête doit être en gras. Si null, le
     * paramètre par défaut du ruban sera utilisé.
     *
     * @param headerFontBold true pour police en gras, false pour normal
     */
    public void setHeaderFontBold(Boolean headerFontBold) {
        if (!Objects.equals(this.headerFontBold, headerFontBold)) {
            Boolean old = this.headerFontBold;
            this.headerFontBold = headerFontBold;
            firePropertyChange("headerFontBold", old, headerFontBold);
        }
    }

    /**
     * Retourne la couleur de fond spécifique de l'en-tête au survol. Si null,
     * la couleur par défaut du ruban doit être utilisée.
     *
     * @return la couleur de fond au survol, ou null si non définie
     */
    public Color getHeaderHoverBackground() {
        return headerHoverBackground;
    }

    /**
     * Définit une couleur de fond spécifique pour l'en-tête au survol. Si null,
     * la couleur par défaut du ruban sera utilisée.
     *
     * @param headerHoverBackground la nouvelle couleur de fond au survol
     */
    public void setHeaderHoverBackground(Color headerHoverBackground) {
        if (!Objects.equals(this.headerHoverBackground, headerHoverBackground)) {
            Color old = this.headerHoverBackground;
            this.headerHoverBackground = headerHoverBackground;
            firePropertyChange("headerHoverBackground", old, headerHoverBackground);
        }
    }

    /**
     * Retourne la couleur de fond spécifique de l'en-tête en sélection. Si
     * null, la couleur par défaut du ruban doit être utilisée.
     *
     * @return la couleur de fond en sélection, ou null si non définie
     */
    public Color getHeaderSelectedBackground() {
        return headerSelectedBackground;
    }

    /**
     * Définit une couleur de fond spécifique pour l'en-tête en sélection. Si
     * null, la couleur par défaut du ruban sera utilisée.
     *
     * @param headerSelectedBackground la nouvelle couleur de fond en sélection
     */
    public void setHeaderSelectedBackground(Color headerSelectedBackground) {
        if (!Objects.equals(this.headerSelectedBackground, headerSelectedBackground)) {
            Color old = this.headerSelectedBackground;
            this.headerSelectedBackground = headerSelectedBackground;
            firePropertyChange("headerSelectedBackground", old, headerSelectedBackground);
        }
    }

    /**
     * Retourne le rayon des coins arrondis spécifique de l'en-tête. Si null, le
     * rayon par défaut du ruban doit être utilisée.
     *
     * @return le rayon des coins en pixels, ou null si non défini
     */
    public Integer getHeaderCornerRadius() {
        return headerCornerRadius;
    }

    /**
     * Définit un rayon des coins arrondis spécifique pour l'en-tête. Si null,
     * le rayon par défaut du ruban sera utilisé.
     *
     * @param headerCornerRadius le nouveau rayon des coins en pixels
     */
    public void setHeaderCornerRadius(Integer headerCornerRadius) {
        if (!Objects.equals(this.headerCornerRadius, headerCornerRadius)) {
            Integer old = this.headerCornerRadius;
            this.headerCornerRadius = headerCornerRadius;
            firePropertyChange("headerCornerRadius", old, headerCornerRadius);
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

    /**
     * Indique si le groupe peut être collapsé.
     */
    public boolean isCollapsible() {
        return collapsible;
    }

    /**
     * Configure si le groupe est collapsible.
     */
    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    /**
     * Largeur (px) à utiliser pour le groupe lorsqu'il est collapsé.
     */
    public int getCollapseWidth() {
        return Math.max(0, collapseWidth);
    }

    /**
     * Définit la collapseWidth (px).
     */
    public void setCollapseWidth(int collapseWidth) {
        this.collapseWidth = Math.max(0, collapseWidth);
    }

    /**
     * Modifie l'état collapsed. Ne gère PAS le reparenting ; c'est le rôle de
     * l'OverflowManager.
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    /**
     * Retourne le niveau de collapse actuel du groupe.
     *
     * @return le niveau de collapse (NORMAL ou COLLAPSED)
     */
    public CollapseLevel getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Définit le niveau de collapse du groupe. Notifie les écouteurs du
     * changement.
     *
     * @param level le nouveau niveau de collapse
     */
    public void setCurrentLevel(CollapseLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("CollapseLevel cannot be null");
        }

        if (this.currentLevel != level) {
            CollapseLevel oldLevel = this.currentLevel;
            this.currentLevel = level;

            // Notifier les écouteurs si le support est initialisé
            if (changeSupport != null) {
                changeSupport.firePropertyChange("currentLevel", oldLevel, level);
            }
        }
    }

    /**
     * Retourne la largeur du groupe en mode collapsed.
     *
     * @return largeur en pixels (par défaut 80)
     */
    public int getCollapsedWidth() {
        return collapsedWidth;
    }

    /**
     * Définit la largeur du groupe en mode collapsed.
     *
     * @param width largeur en pixels (doit être > 0)
     */
    public void setCollapsedWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Collapsed width must be positive");
        }

        if (this.collapsedWidth != width) {
            int oldWidth = this.collapsedWidth;
            this.collapsedWidth = width;

            if (changeSupport != null) {
                changeSupport.firePropertyChange("collapsedWidth", oldWidth, width);
            }
        }
    }

    /**
     * Retourne le composant affiché en mode collapsed (généralement un
     * JComboBox).
     *
     * @return le composant collapsed ou null si pas encore créé
     */
    public RibbonOverflowButton getCollapsedButton() {
        return collapsedButton;
    }

    /**
     * Définit le composant affiché en mode collapsed.
     *
     * @param button
     */
    public void setCollapsedButton(RibbonOverflowButton button) {
        this.collapsedButton = button;
    }

    public Component getCollapsedComponent() {
        return collapsedButton;
    }

    public void setCollapsedComponent(Component component) {
        if (component instanceof RibbonOverflowButton) {
            this.collapsedButton = (RibbonOverflowButton) component;
        }
    }

    /**
     * Vérifie si le groupe est actuellement collapsed.
     *
     * @return true si le groupe est en mode COLLAPSED
     */
    public boolean isCollapsed() {
        return currentLevel == CollapseLevel.COLLAPSED;
    }

    /**
     * Vérifie si le groupe est actuellement en mode normal.
     *
     * @return true si le groupe est en mode NORMAL
     */
    public boolean isNormal() {
        return currentLevel == CollapseLevel.NORMAL;
    }

    /**
     * Passe le groupe au niveau collapsed. Raccourci pour
     * setCurrentLevel(CollapseLevel.COLLAPSED).
     */
    public void collapse() {
        setCurrentLevel(CollapseLevel.COLLAPSED);
    }

    /**
     * Passe le groupe au niveau normal. Raccourci pour
     * setCurrentLevel(CollapseLevel.NORMAL).
     */
    public void expand() {
        setCurrentLevel(CollapseLevel.NORMAL);
    }

    /**
     * Invalide le composant collapsed en cache. Force la recréation du
     * JComboBox au prochain affichage.
     */
    public void invalidateCollapsedButton() {
        this.collapsedButton = null;
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
