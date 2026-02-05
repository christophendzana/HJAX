package rubban;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LayoutManager pour HRibbon.
 *
 * Positionne les groupes horizontalement et les composants à l'intérieur des groupes.
 * S'inspire du ruban Microsoft Office : groupes côte à côte, composants organisés
 * en lignes à l'intérieur de chaque groupe.
 *
 * Remarques des modifications :
 * - La méthode layoutContainer(...) a été améliorée pour calculer la hauteur
 *   réellement requise par le contenu (en simulant le wrapping des composants
 *   dans chaque groupe) et utiliser cette hauteur lorsque
 *   Ribbon.isFillsViewportHeight() == false.
 * - Une méthode computeRequiredContentHeight(...) a été ajoutée. Elle simule
 *   le wrapping ligne par ligne en utilisant les preferredSize des composants
 *   ou, si la valeur n'est pas un Component, en demandant un composant au renderer
 *   uniquement pour mesurer (sans l'ajouter au conteneur).
 *
 * Ces changements résolvent le problème où le ribbon s'étirait toujours à la
 * hauteur du parent au lieu d'utiliser la hauteur nécessaire au contenu.
 */
public class HRibbonLayoutManager implements LayoutManager2 {

    // =========================================================================
    // CHAMPS
    // =========================================================================

    /** Référence vers le ruban géré. */
    private final Ribbon ribbon;

    /** Mode de répartition : true = tous les groupes ont la même largeur. */
    private boolean equalDistribution = false;

    /** Cache des limites des groupes pour éviter de recalculer à chaque paint. */
    private Rectangle[] groupBoundsCache = null;

    /** Cache des composants par groupe pour optimisation. */
    private Map<Integer, List<Component>> componentsByGroupCache = null;

    private Map<Integer, Component> headerComponents;

    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================

    /**
     * Constructeur.
     *
     * @param ribbon le ruban à gérer
     * @throws IllegalArgumentException si ribbon est null
     */
    public HRibbonLayoutManager(Ribbon ribbon) {
        if (ribbon == null) {
            throw new IllegalArgumentException("Le ruban ne peut pas être null");
        }
        this.ribbon = ribbon;
    }

    // =========================================================================
    // IMPLÉMENTATION DE LayoutManager2
    // =========================================================================

    /**
     * Positionne tous les composants dans le ruban.
     * Méthode principale appelée par Swing lors du layout.
     *
     * @param parent le conteneur parent (doit être un HRibbon)
     */
    @Override
    public void layoutContainer(Container parent) {
        // Vérification de type
        if (!(parent instanceof Ribbon)) {
            return;
        }

        Ribbon hRibbon = (Ribbon) parent;

        // Récupère les modèles nécessaires
        HRibbonModel model = hRibbon.getModel();
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();

        if (model == null || groupModel == null) {
            return; // Pas de données à afficher
        }

        // Invalide les caches (on recalculera et re-remplira)
        groupBoundsCache = null;
        componentsByGroupCache = null;

        // Récupère les marges du ruban
        Insets insets = parent.getInsets();

        // Calcule l'espace disponible TOTAL
        int availableWidth = parent.getWidth() - insets.left - insets.right;
        int availableHeight = parent.getHeight() - insets.top - insets.bottom;

        // Configuration des headers
        int headerAlignment = hRibbon.getHeaderAlignment();
        boolean headersVisible = headerAlignment != Ribbon.HEADER_HIDDEN;
        int headerHeight = headersVisible ? hRibbon.getHeaderHeight() : 0; // 0 parce que si headersVisible->false alors les headers sont hidden 

        // Nombre de groupes
        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return; // Aucun groupe à afficher
        }

        // 1. CALCUL DES LARGEURS DE CHAQUE GROUPE
        int[] groupWidths = calculateGroupWidths(groupModel, availableWidth);

        // 2. ESPACE DISPONIBLE POUR LE CONTENU DES GROUPES (hors headers)
        int contentHeight = availableHeight;
        if (headersVisible) {
            // Pour NORTH/SOUTH : réserver de l'espace en hauteur
            if (headerAlignment == Ribbon.HEADER_NORTH || headerAlignment == Ribbon.HEADER_SOUTH) {
                contentHeight -= headerHeight;
                if (contentHeight < 0) contentHeight = 0;
            }
            // Pour WEST/EAST : pas de réduction de hauteur, l'espace est pris en largeur
            // (déjà comptabilisé dans groupWidths)
        }

        // --- NOUVEAU : calculer la hauteur réellement requise par les groupes (wrapping) ---
        // Si le Ribbon n'est pas configuré pour remplir la hauteur du viewport,
        // on simule le wrapping de chaque groupe pour déterminer la hauteur
        // minimale nécessaire et on limite contentHeight à cette valeur.
        if (!hRibbon.isFillsViewportHeight()) {
            int requiredContentHeight = computeRequiredContentHeight(hRibbon, groupWidths, model, groupModel, headerAlignment);
            if (requiredContentHeight > 0) {
                contentHeight = Math.min(contentHeight, requiredContentHeight);
            }
        }
        // --- FIN DE LA PARTIE NOUVELLE

        // 3. CALCUL DES POSITIONS DES GROUPES (pour le contenu seulement)
        Rectangle[] groupBounds = calculateGroupBounds(groupWidths, groupModel,
                                                       insets, contentHeight, headerAlignment, headerHeight);
        groupBoundsCache = groupBounds; // Mise en cache

        // 4. CRÉER ET POSITIONNER LES HEADERS SI NÉCESSAIRE
        if (headersVisible) {
            createAndPositionHeaders(hRibbon, groupModel, groupBounds,
                                     headerAlignment, headerHeight, insets);
        }

        // 5. ORGANISATION DES COMPOSANTS PAR GROUPE
        Map<Integer, List<Component>> componentsByGroup = organizeComponentsByGroup(hRibbon, model);
        componentsByGroupCache = componentsByGroup; // Mise en cache

        // 6. POSITIONNEMENT DES COMPOSANTS DANS CHAQUE GROUPE
        positionAllComponents(componentsByGroup, groupBounds, groupModel);
    }

    /**
     * Crée et positionne les composants headers pour chaque groupe.
     * Pour NORTH/SOUTH : headers alignés horizontalement sur une même ligne
     * Pour WEST/EAST : header attaché à son groupe (comme un titre de carte)
     */
    private void createAndPositionHeaders(Ribbon ribbon, HRibbonGroupModel groupModel,
                                          Rectangle[] groupBounds, int headerAlignment,
                                          int headerHeight, Insets insets) {
        // Initialiser le cache si nécessaire
        if (headerComponents == null) {
            headerComponents = new HashMap<>();
        }

        // 1. IDENTIFIER LES HEADERS À SUPPRIMER (groupes qui n'existent plus)
        Set<Integer> currentGroups = new HashSet<>();
        for (int i = 0; i < groupBounds.length; i++) {
            currentGroups.add(i);
        }

        // Supprimer les headers des groupes qui n'existent plus
        Iterator<Map.Entry<Integer, Component>> it = headerComponents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Component> entry = it.next();
            if (!currentGroups.contains(entry.getKey())) {
                ribbon.removeHeaderComponent(entry.getValue());
                it.remove();
            }
        }

        // 2. CALCULER LA POSITION POUR TOUS LES HEADERS
        int headerWidth = ribbon.getHeaderWidth();

        // Pour NORTH/SOUTH : tous les headers à la même hauteur
        int commonHeaderY = -1;
        if (headerAlignment == Ribbon.HEADER_NORTH) {
            commonHeaderY = insets.top;
        } else if (headerAlignment == Ribbon.HEADER_SOUTH) {
            commonHeaderY = ribbon.getHeight() - insets.bottom - headerHeight;
        }

        // 3. CRÉER/POSITIONNER CHAQUE HEADER
        for (int i = 0; i < groupBounds.length; i++) {
            Rectangle contentRect = groupBounds[i]; // Rectangle du CONTENU
            HRibbonGroup group = groupModel.getHRibbonGroup(i);

            if (group == null || contentRect == null) {
                continue;
            }

            // Créer ou récupérer le header
            Component header = headerComponents.get(i);
            if (header == null) {
                // Créer un nouveau header via le renderer
                Object headerValue = ribbon.getHeaderValue(i);

                header = ribbon.getGroupRenderer().getHeaderComponent(
                    ribbon, headerValue, i, false
                );

                if (header != null) {
                    ribbon.addHeaderComponent(header);
                    headerComponents.put(i, header);
                }
            }

            // POSITIONNER LE HEADER selon l'alignement
            if (header != null) {
                int headerX, headerY, headerW, headerH;

                switch (headerAlignment) {
                    case Ribbon.HEADER_NORTH:
                        // Header au-dessus du contenu - même largeur que le contenu
                        headerX = contentRect.x;
                        headerY = commonHeaderY;
                        headerW = contentRect.width;
                        headerH = headerHeight;
                        break;

                    case Ribbon.HEADER_SOUTH:
                        // Header en dessous du contenu - même largeur que le contenu
                        headerX = contentRect.x;
                        headerY = commonHeaderY;
                        headerW = contentRect.width;
                        headerH = headerHeight;
                        break;

                    case Ribbon.HEADER_WEST:
                        // Header à gauche du contenu
                        headerX = contentRect.x - headerWidth;
                        headerY = contentRect.y;
                        headerW = headerWidth;
                        headerH = contentRect.height;
                        break;

                    case Ribbon.HEADER_EAST:
                        // Header à droite du contenu
                        headerX = contentRect.x + contentRect.width;
                        headerY = contentRect.y;
                        headerW = headerWidth;
                        headerH = contentRect.height;
                        break;

                    default:
                        continue;
                }

                header.setBounds(headerX, headerY, headerW, headerH);
                header.setVisible(true);
            }
        }
    }

    /**
     * Calcule la largeur de chaque groupe avec un système de distribution.
     * Priorités:
     * 1. Si width est défini (>0) par l'utilisateur : utiliser cette valeur
     * 2. Sinon utiliser preferredWidth
     * 3. Respecter les contraintes min/max
     * 4. Prendre en compte l'espace nécessaire pour les headers (WEST/EAST)
     *
     * @param groupModel le modèle des groupes
     * @param availableWidth largeur disponible totale
     * @return tableau des largeurs pour chaque groupe (incluant l'espace header si WEST/EAST)
     */
    private int[] calculateGroupWidths(HRibbonGroupModel groupModel, int availableWidth) {
        int groupCount = groupModel.getGroupCount();
        int[] widths = new int[groupCount];

        // Marge entre les groupes
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        int headerAlignment = ribbon.getHeaderAlignment();
        int headerWidth = ribbon.getHeaderWidth();

        // Largeur disponible pour les groupes (total - marges entre groupes)
        int totalMargin = groupMargin * (groupCount - 1);
        int widthForGroups = Math.max(availableWidth - totalMargin, 0);

        if (equalDistribution) {
            // distribution égale entre les groupes
            int widthPerGroup = widthForGroups / groupCount;
            for (int i = 0; i < groupCount; i++) {
                widths[i] = Math.max(widthPerGroup, 0);
            }
        } else {
            // distribution par priorité de paramètre: width > preferredWidth > défaut
            int totalRequestedWidth = 0;  // priorité à witdh de HRibbonGroup
            int[] requestedWidths = new int[groupCount]; // largeur demandée par chaque groupe
            boolean[] isFixed = new boolean[groupCount]; // groupes avec width défini

            // 1. PREMIÈRE ETAPE : Collecter les largeurs souhaitées
            for (int i = 0; i < groupCount; i++) {
                HRibbonGroup group = groupModel.getHRibbonGroup(i);
                
                    // Priorité 1 : width défini par l'utilisateur (dans HRibbonGroup)
                    if (group.getWidth() > 0) {
                        requestedWidths[i] = group.getWidth();
                        isFixed[i] = true;
                    }
                    // Priorité 2 : preferredWidth
                    else if (group.getPreferredWidth() > 0) {
                        requestedWidths[i] = group.getPreferredWidth();
                        isFixed[i] = false;
                    }
                    // Priorité 3 : largeur par défaut
                    else {
                        requestedWidths[i] = group.getPreferredWidth(); 
                        isFixed[i] = false;
                    }

                    // Pour WEST/EAST : ajouter l'espace du header
                    if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                        requestedWidths[i] += headerWidth;
                    }
                
                totalRequestedWidth += requestedWidths[i];
            }

            // 2. DEUXIÈME ETAPE : Distribution 
            if (totalRequestedWidth <= widthForGroups) {
                // Assez de place : on utilise les largeurs demandées
                System.arraycopy(requestedWidths, 0, widths, 0, groupCount);

                // Distribuer l'espace restant aux groupes non-fixes
                int remainingSpace = widthForGroups - totalRequestedWidth;
                int flexibleCount = 0;
                for (boolean fixed : isFixed) {
                    if (!fixed) flexibleCount++;
                }

                if (flexibleCount > 0 && remainingSpace > 0) {
                    int extraPerGroup = remainingSpace / flexibleCount;
                    for (int i = 0; i < groupCount; i++) {
                        if (!isFixed[i]) {
                            widths[i] += extraPerGroup;
                        }
                    }
                }
            } else {
                // Pas assez de place : réduction 

                // Calculer l'espace occupé par les groupes fixes
                int fixedSpace = 0;
                int flexibleSpace = 0;
                for (int i = 0; i < groupCount; i++) {
                    if (isFixed[i]) {
                        fixedSpace += requestedWidths[i];
                    } else {
                        flexibleSpace += requestedWidths[i];
                    }
                }

                int availableForFlexible = widthForGroups - fixedSpace;

                for (int i = 0; i < groupCount; i++) {
                    if (isFixed[i]) {
                        // Groupes fixes gardent leur largeur
                        widths[i] = requestedWidths[i];
                    } else {
                        // Groupes flexibles se partagent l'espace restant proportionnellement
                        if (flexibleSpace > 0) {
                            float ratio = (float) requestedWidths[i] / flexibleSpace;
                            widths[i] = Math.max((int) (availableForFlexible * ratio), 50);
                        } else {
                            widths[i] = 50;
                        }
                    }
                }
            }
        }

        // 3. TROISIÈME ETAPE : Appliquer les contraintes min/max
        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                int minWidth = group.getMinWidth();
                int maxWidth = group.getMaxWidth();

                // Pour WEST/EAST : les contraintes incluent déjà l'espace header
                if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                    if (minWidth > 0) {
                        minWidth += headerWidth;
                    }
                    if (maxWidth > 0) {
                        maxWidth += headerWidth;
                    }
                }

                // Appliquer les contraintes
                if (minWidth > 0) {
                    widths[i] = Math.max(widths[i], minWidth);
                }
                if (maxWidth > 0) {
                    widths[i] = Math.min(widths[i], maxWidth);
                }
            }

            // Assure une largeur positive minimale
            widths[i] = Math.max(widths[i], 50);
        }

        return widths;
    }

    /**
     * Calcule les limites des groupes pour le CONTENU seulement.
     * Les headers sont gérés séparément.     
     * Pour WEST/EAST : groupWidths inclut déjà l'espace du header, il faut le
     * supprimer pour le calcul de l'espace des composants.
     */
    private Rectangle[] calculateGroupBounds(int[] groupWidths, HRibbonGroupModel groupModel,
                                            Insets insets, int contentHeight,
                                            int headerAlignment, int headerHeight) {
        int groupCount = groupModel.getGroupCount();
        Rectangle[] bounds = new Rectangle[groupCount];

        // Position X de départ (après la marge gauche)
        int currentX = insets.left;
        int groupMargin = groupModel.getHRibbonGroupMarggin();
        int headerWidth = ribbon.getHeaderWidth();

        // Position Y du contenu selon la position du header
        int contentY = insets.top; // Par défaut en haut

        if (headerAlignment == Ribbon.HEADER_NORTH) {
            // Headers en haut : le contenu commence après les headers
            contentY = insets.top + headerHeight;
        }
        // Pour HEADER_SOUTH/WEST/EAST : le contenu est en haut (headers ailleurs)

        for (int i = 0; i < groupCount; i++) {
            // groupWidths[i] est la largeur TOTALE du groupe (incluant header pour WEST/EAST)
            int totalGroupWidth = groupWidths[i];
            int contentX = currentX;
            int contentWidth = totalGroupWidth;

            // Pour WEST/EAST : déduire l'espace du header de la largeur du contenu
            if (headerAlignment == Ribbon.HEADER_WEST) {
                // Header à gauche : contenu commence après le header
                contentX = currentX + headerWidth;
                contentWidth = totalGroupWidth - headerWidth;
            } else if (headerAlignment == Ribbon.HEADER_EAST) {
                // Header à droite : contenu est à gauche, header prend l'espace à droite
                contentWidth = totalGroupWidth - headerWidth;
                // contentX reste à currentX
            }

            // Assurer une largeur minimale pour le contenu
            contentWidth = Math.max(contentWidth, 20);

            // Crée le rectangle pour le CONTENU de ce groupe
            bounds[i] = new Rectangle(
                contentX,              // x
                contentY,              // y - TOUS LES GROUPES À LA MÊME HAUTEUR
                contentWidth,          // width (espace disponible pour les composants)
                contentHeight          // height - TOUS LES GROUPES ONT LA MÊME HAUTEUR
            );

            // Avance pour le prochain groupe (toujours horizontal)
            // On utilise totalGroupWidth car c'est la largeur réellement occupée
            currentX += totalGroupWidth + groupMargin;
        }

        return bounds;
    }

    /**
     * Organise les composants du HRibbon par groupe.
     * Parcourt le modèle pour associer chaque composant à son groupe.
     *
     * @param hRibbon le ruban
     * @param model le modèle de données
     * @return map groupIndex -> liste des composants dans ce groupe
     */
    private Map<Integer, List<Component>> organizeComponentsByGroup(Ribbon hRibbon, HRibbonModel model) {
        Map<Integer, List<Component>> componentsByGroup = new HashMap<>();

        // Parcourt tous les groupes du modèle
        for (int groupIndex = 0; groupIndex < model.getGroupCount(); groupIndex++) {
            List<Component> groupComponents = new ArrayList<>();

            // Parcourt toutes les valeurs dans ce groupe
            int valueCount = model.getValueCount(groupIndex);
            for (int position = 0; position < valueCount; position++) {
                // 1. Récupère la valeur du modèle
                Object value = model.getValueAt(position, groupIndex);

                // 2. DEMANDE AU HRibbon LE COMPONENT CORRESPONDANT
                Component comp = hRibbon.getComponentForValue(value, groupIndex, position);

                // 3. Vérifie que le composant existe et est bien dans le HRibbon
                if (comp != null && comp.getParent() == hRibbon) {
                    groupComponents.add(comp);
                }
            }

            // Ajoute la liste au map (même si vide)
            componentsByGroup.put(groupIndex, groupComponents);
        }

        return componentsByGroup;
    }

    /**
     * Positionne tous les composants dans leurs groupes respectifs.
     *
     * @param componentsByGroup map des composants par groupe
     * @param groupBounds limites de chaque groupe
     * @param groupModel le modèle des groupes
     */
    private void positionAllComponents(Map<Integer, List<Component>> componentsByGroup,
                                       Rectangle[] groupBounds,
                                       HRibbonGroupModel groupModel) {
        // Pour chaque groupe
        for (int groupIndex = 0; groupIndex < groupBounds.length; groupIndex++) {
            Rectangle groupRect = groupBounds[groupIndex];
            List<Component> components = componentsByGroup.get(groupIndex);

            if (components == null || components.isEmpty() || groupRect == null) {
                continue; // Groupe vide ou inexistant
            }

            // Récupère la configuration du groupe
            HRibbonGroup group = groupModel.getHRibbonGroup(groupIndex);
            if (group == null) {
                continue; // Groupe non trouvé
            }

            // Positionne les composants dans ce groupe
            layoutComponentsInGroup(components, groupRect, group);
        }
    }

    /**
     * Positionne les composants à l'intérieur d'un groupe.
     * Les composants sont organisés en lignes (comme un FlowLayout vertical).
     *
     * @param components liste des composants à positionner
     * @param groupRect limites du groupe
     * @param group configuration du groupe (padding, spacing)
     */
    private void layoutComponentsInGroup(List<Component> components,
                                         Rectangle groupRect,
                                         HRibbonGroup group) {
        if (components.isEmpty()) {
            return;
        }

        // Récupère les paramètres du groupe
        int padding = group.getPadding();
        int spacing = group.getComponentSpacing();

        // Zone intérieure disponible
        int innerX = groupRect.x + padding;
        int innerY = groupRect.y + padding;
        int innerWidth = groupRect.width - (2 * padding);
        int innerHeight = groupRect.height - (2 * padding);

        // 1. ORGANISATION EN LIGNES OPTIMISÉES
        List<List<Component>> lines = organizeComponentsIntoLines(components, innerWidth, spacing);

        // 2. POSITIONNEMENT DE CHAQUE LIGNE
        int currentY = innerY;

        for (List<Component> line : lines) {
            // Calcule la hauteur de la ligne
            int lineHeight = calculateLineHeight(line);

            // Vérifie qu'il reste de la place verticale
            if (currentY + lineHeight > innerY + innerHeight) {
                break;
            }

            // Positionne la ligne
            positionLine(line, innerX, currentY, spacing, lineHeight);

            // Passe à la ligne suivante
            currentY += lineHeight + spacing;
        }
    }

    /**
     * Organise les composants en lignes
     */
    private List<List<Component>> organizeComponentsIntoLines(List<Component> components,
                                                              int maxLineWidth,
                                                              int spacing) {
        List<List<Component>> lines = new ArrayList<>();
        List<Component> currentLine = new ArrayList<>();
        int currentLineWidth = 0;

        for (Component comp : components) {
            int compWidth = comp.getPreferredSize().width;
            int requiredWidth = compWidth;

            // Ajoute l'espacement si ce n'est pas le premier de la ligne
            if (!currentLine.isEmpty()) {
                requiredWidth += spacing;
            }

            // Vérifie si le composant rentre dans la ligne actuelle
            if (currentLineWidth + requiredWidth <= maxLineWidth) {
                // Ajoute à la ligne actuelle
                currentLine.add(comp);
                currentLineWidth += requiredWidth;
            } else {
                // Nouvelle ligne
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = new ArrayList<>();
                currentLine.add(comp);
                currentLineWidth = compWidth;
            }
        }

        // Ajoute la dernière ligne
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        return lines;
    }

    /**
     * Positionne une ligne de composants horizontalement.
     */
    private void positionLine(List<Component> line,
                              int startX,
                              int lineY,
                              int spacing,
                              int lineHeight) {
        int currentX = startX;

        for (Component comp : line) {
            Dimension prefSize = comp.getPreferredSize();
            int compHeight = Math.min(prefSize.height, lineHeight);

            // Centre verticalement dans la ligne
            int compY = lineY + (lineHeight - compHeight) / 2;

            comp.setBounds(currentX, compY, prefSize.width, compHeight);
            currentX += prefSize.width + spacing;
        }
    }

    /**
     * Calcule la hauteur maximale d'une ligne.
     */
    private int calculateLineHeight(List<Component> line) {
        int maxHeight = 0;
        for (Component comp : line) {
            maxHeight = Math.max(maxHeight, comp.getPreferredSize().height);
        }
        return maxHeight;
    }

    // =========================================================================
    // CALCUL DES TAILLES PRÉFÉRÉES, MINIMALES ET MAXIMALES
    // =========================================================================

    /**
     * Calcule la taille préférée du ruban.
     *
     * @param parent le conteneur parent
     * @return la taille préférée
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        if (!(parent instanceof Ribbon)) {
            return new Dimension(0, 0);
        }

        Ribbon hRibbon = (Ribbon) parent;
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();

        if (groupModel == null) {
            return new Dimension(0, 0);
        }

        Insets insets = parent.getInsets();
        int groupCount = groupModel.getGroupCount();

        if (groupCount == 0) {
            return new Dimension(
                insets.left + insets.right,
                insets.top + insets.bottom + 80 // Hauteur par défaut
            );
        }

        // 1. Calcule la largeur totale préférée
        int totalPreferredWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalPreferredWidth += group.getPreferredWidth();
            } else {
                totalPreferredWidth += 100; // Largeur par défaut
            }

            // Ajoute la marge entre groupes (sauf après le dernier)
            if (i < groupCount - 1) {
                totalPreferredWidth += groupMargin;
            }
        }

        // 2. Calcule la hauteur préférée (basée sur le wrapping simulé avec une largeur estimée)
        // On estime la largeur disponible par groupe en partant d'une largeur totale raisonnable.
        // Ici on suppose que preferred width totale = totalPreferredWidth ; on répartit proportionnellement.
        int estimatedAvailableWidth = totalPreferredWidth;
        int[] estimatedGroupWidths = calculateGroupWidths(groupModel, estimatedAvailableWidth);

        int contentHeight = computeRequiredContentHeight(hRibbon, estimatedGroupWidths,
                                                         hRibbon.getModel(), groupModel, hRibbon.getHeaderAlignment());

        int preferredHeight = Math.max(contentHeight, 80); // Minimum 80px

        // 3. Ajoute les marges
        return new Dimension(
            totalPreferredWidth + insets.left + insets.right,
            preferredHeight + insets.top + insets.bottom
        );
    }

    /**
     * Calcule la taille minimale du ruban.
     *
     * @param parent le conteneur parent
     * @return la taille minimale
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        if (!(parent instanceof Ribbon)) {
            return new Dimension(0, 0);
        }

        Ribbon hRibbon = (Ribbon) parent;
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();

        if (groupModel == null) {
            return new Dimension(0, 0);
        }

        Insets insets = parent.getInsets();
        int groupCount = groupModel.getGroupCount();

        if (groupCount == 0) {
            return new Dimension(
                insets.left + insets.right,
                insets.top + insets.bottom + 50 // Hauteur minimale
            );
        }

        // Largeur minimale : somme des largeurs minimales
        int totalMinWidth = 0;
        int groupMargin = groupModel.getHRibbonGroupMarggin();

        for (int i = 0; i < groupCount; i++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(i);
            if (group != null) {
                totalMinWidth += Math.max(group.getMinWidth(), 30); // Minimum 30px
            } else {
                totalMinWidth += 50; // Largeur minimale par défaut
            }

            if (i < groupCount - 1) {
                totalMinWidth += groupMargin;
            }
        }

        return new Dimension(
            totalMinWidth + insets.left + insets.right,
            50 + insets.top + insets.bottom // Hauteur minimale fixe
        );
    }

    /**
     * Calcule la taille maximale du ruban.
     *
     * @param target le conteneur parent
     * @return la taille maximale (presque infinie)
     */
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Calcule la hauteur maximale des composants dans le ruban.
     *
     * @param hRibbon le ruban
     * @return la hauteur maximale trouvée
     */
    private int calculateMaxComponentHeight(Ribbon hRibbon) {
        int maxHeight = 0;
        HRibbonModel model = hRibbon.getModel();

        if (model == null) {
            return 80; // Hauteur par défaut
        }

        // Parcourt tous les composants du modèle
        for (int groupIndex = 0; groupIndex < model.getGroupCount(); groupIndex++) {
            int valueCount = model.getValueCount(groupIndex);
            for (int position = 0; position < valueCount; position++) {
                Object value = model.getValueAt(position, groupIndex);
                if (value instanceof Component) {
                    Component comp = (Component) value;
                    Dimension prefSize = comp.getPreferredSize();
                    maxHeight = Math.max(maxHeight, prefSize.height);
                }
            }
        }

        // Ajoute le padding des groupes
        HRibbonGroupModel groupModel = hRibbon.getGroupModel();
        if (groupModel != null && groupModel.getGroupCount() > 0) {
            HRibbonGroup firstGroup = groupModel.getHRibbonGroup(0);
            if (firstGroup != null) {
                maxHeight += firstGroup.getPadding() * 2; // Padding haut + bas
            }
        }

        return Math.max(maxHeight, 30); // Minimum 30px
    }

    // =========================================================================
    // MÉTHODES D'UTILITÉ POUR L'UI
    // =========================================================================

    /**
     * Retourne les limites des groupes (calculées lors du dernier layout).
     *
     * @return tableau des rectangles des groupes, ou null si non calculé
     */
    public Rectangle[] getGroupBounds() {
        return groupBoundsCache;
    }

    /**
     * Retourne les composants organisés par groupe.
     *
     * @return map des composants par groupe, ou null si non calculé
     */
    public Map<Integer, List<Component>> getComponentsByGroup() {
        return componentsByGroupCache;
    }

    /**
     * Définit le mode de répartition des largeurs de groupe.
     *
     * @param equal true pour largeurs égales, false pour largeurs préférées
     */
    public void setEqualDistribution(boolean equal) {
        if (this.equalDistribution != equal) {
            this.equalDistribution = equal;
            if (ribbon != null) {
                ribbon.revalidate(); // Force un redessin
            }
        }
    }

    /**
     * Retourne le mode de répartition actuel.
     *
     * @return true si les groupes ont des largeurs égales
     */
    public boolean isEqualDistribution() {
        return equalDistribution;
    }

    // =========================================================================
    // MÉTHODES NON UTILISÉES (obligatoires pour LayoutManager2)
    // =========================================================================

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Les composants sont ajoutés via le modèle, pas ici
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        // Les composants sont ajoutés via le modèle, pas ici
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // Les composants sont retirés via le modèle, pas ici
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f; // Centré horizontalement
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f; // Centré verticalement
    }

    @Override
    public void invalidateLayout(Container target) {
        // Invalide les caches
        groupBoundsCache = null;
        componentsByGroupCache = null;
    }

    /**
     * Supprime le header d'un groupe spécifique du cache.
     * À appeler quand le groupe est supprimé ou quand son header change.
     */
    public void removeHeaderForGroup(int groupIndex) {
        if (headerComponents != null) {
            Component oldHeader = headerComponents.remove(groupIndex);
            if (oldHeader != null && ribbon != null) {
                ribbon.removeHeaderComponent(oldHeader);
            }
        }
    }

    /**
     * Invalide tous les headers.
     * À appeler quand la configuration globale des headers change.
     */
    public void invalidateAllHeaders() {
        if (headerComponents != null && ribbon != null) {
            // Retirer tous les headers du ruban
            for (Component header : headerComponents.values()) {
                ribbon.removeHeaderComponent(header);
            }
            headerComponents.clear();
        }
        headerComponents = null; // Force la recréation complète
    }

    // =========================================================================
    // NOUVELLE MÉTHODE : SIMULATION DE WRAPPING POUR CALCUL DE HAUTEUR
    // =========================================================================

    /**
     * Simule le wrapping des composants pour chaque groupe, compte la hauteur
     * nécessaire (padding + lignes) et retourne la hauteur maximale requise
     * parmi tous les groupes. Utilise preferredSize des composants/renderer.
     *
     * @param ribbon le ruban
     * @param groupWidths tableau des largeurs totales des groupes (peut inclure headerWidth pour WEST/EAST)
     * @param model le HRibbonModel (valeurs)
     * @param groupModel le HRibbonGroupModel (configuration des groupes)
     * @param headerAlignment alignement des headers (pour déduire headerWidth si nécessaire)
     * @return hauteur de contenu requise (pixels), excluant insets
     */
    private int computeRequiredContentHeight(Ribbon ribbon,
                                             int[] groupWidths,
                                             HRibbonModel model,
                                             HRibbonGroupModel groupModel,
                                             int headerAlignment) {
        if (groupWidths == null || model == null || groupModel == null) {
            return 0;
        }

        int groupCount = groupModel.getGroupCount();
        if (groupCount == 0) {
            return 0;
        }

        int headerWidth = ribbon.getHeaderWidth();
        int maxRequired = 0;
        GroupRenderer renderer = ribbon.getGroupRenderer();

        for (int gi = 0; gi < groupCount; gi++) {
            HRibbonGroup group = groupModel.getHRibbonGroup(gi);
            if (group == null) {
                continue;
            }

            // largeur disponible pour le contenu (déduire header si WEST/EAST)
            int totalGroupWidth = (gi < groupWidths.length) ? groupWidths[gi] : 0;
            int contentWidth = totalGroupWidth;
            if (headerAlignment == Ribbon.HEADER_WEST || headerAlignment == Ribbon.HEADER_EAST) {
                contentWidth = Math.max(totalGroupWidth - headerWidth, 1);
            }

            int padding = Math.max(0, group.getPadding());
            int innerWidth = Math.max(contentWidth - padding * 2, 1);
            int spacing = Math.max(0, group.getComponentSpacing());

            int valueCount = model.getValueCount(gi);
            if (valueCount <= 0) {
                // Si aucun composant, hauteur minimale = padding * 2
                maxRequired = Math.max(maxRequired, padding * 2);
                continue;
            }

            // Si innerWidth est très petit, on tombe en empilement vertical
            boolean forceVertical = innerWidth <= 1;

            // Simulation du wrapping : on récupère preferredSizes
            java.util.List<java.awt.Dimension> prefs = new java.util.ArrayList<>(valueCount);
            for (int pos = 0; pos < valueCount; pos++) {
                Object value = model.getValueAt(pos, gi);
                java.awt.Dimension pref = null;

                if (value instanceof java.awt.Component) {
                    java.awt.Component comp = (java.awt.Component) value;
                    pref = comp.getPreferredSize();
                } else {
                    try {
                        // Demande au renderer un composant de mesure (sans l'ajouter au conteneur)
                        java.awt.Component comp = renderer.getGroupComponent(ribbon, value, gi, pos, false, false);
                        if (comp != null) {
                            pref = comp.getPreferredSize();
                        }
                    } catch (Throwable t) {
                        // Défensive : si le renderer plante, on ignore et on donnera une taille par défaut
                        pref = null;
                    }
                }

                if (pref == null) {
                    // Valeurs par défaut raisonnables
                    pref = new java.awt.Dimension(50, 24);
                }
                prefs.add(pref);
            }

            // Calcul de la hauteur nécessaire pour ce groupe
            int totalHeight = 0;
            if (forceVertical) {
                // Empilement vertical simple : somme des hauteurs + spacing entre éléments
                for (int i = 0; i < prefs.size(); i++) {
                    if (i > 0) totalHeight += spacing;
                    totalHeight += prefs.get(i).height;
                }
                // Ajoute padding top+bottom
                totalHeight += padding * 2;
            } else {
                int currentLineWidth = 0;
                int currentLineHeight = 0;
                boolean firstInLine = true;
                java.util.Iterator<java.awt.Dimension> it = prefs.iterator();
                while (it.hasNext()) {
                    java.awt.Dimension pd = it.next();
                    int compW = pd.width;
                    int compH = pd.height;

                    int requiredW = compW;
                    if (!firstInLine) {
                        requiredW += spacing;
                    }

                    if (firstInLine || currentLineWidth + requiredW <= innerWidth) {
                        // ajouter dans la ligne courante
                        currentLineWidth = firstInLine ? compW : currentLineWidth + spacing + compW;
                        currentLineHeight = Math.max(currentLineHeight, compH);
                        firstInLine = false;
                    } else {
                        // clore la ligne courante
                        if (totalHeight > 0) {
                            totalHeight += spacing; // espace entre lignes
                        }
                        totalHeight += currentLineHeight;

                        // démarrer nouvelle ligne
                        currentLineWidth = compW;
                        currentLineHeight = compH;
                        firstInLine = false;
                    }
                }

                // ajouter la dernière ligne si existante
                if (!firstInLine || currentLineHeight > 0) {
                    if (totalHeight > 0) {
                        totalHeight += spacing;
                    }
                    totalHeight += currentLineHeight;
                }

                // Ajoute padding top+bottom
                totalHeight += padding * 2;
            }

            maxRequired = Math.max(maxRequired, totalHeight);
        }

        return Math.max(maxRequired, 0);
    }
}