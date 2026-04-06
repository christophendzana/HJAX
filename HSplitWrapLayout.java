package hsplitpane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de disposition avec retour à la ligne automatique.
 *
 * Les composants enfants sont alignés dans une direction principale
 * (horizontale par défaut) et s'étirent pour occuper tout l'espace
 * disponible sur l'axe secondaire. Quand l'espace sur l'axe principal
 * est épuisé, les composants suivants passent à la ligne ou colonne
 * suivante. Si même le retour à la ligne ne suffit plus, le JScrollPane
 * parent prend le relais.
 *
 * Ce layout est conçu pour être utilisé à l'intérieur des zones
 * de contenu de HSplitZone.
 */
public class HSplitWrapLayout implements LayoutManager2 {

    // -------------------------------------------------------------------------
    // Paramètres de disposition
    // -------------------------------------------------------------------------

    /** Direction principale d'alignement des composants. */
    private WrapDirection direction;

    /** Espace horizontal en pixels entre deux composants adjacents. */
    private int hGap;

    /** Espace vertical en pixels entre deux composants adjacents. */
    private int vGap;

    // =========================================================================
    // Constructeurs
    // =========================================================================

    /**
     * Crée un layout avec la direction horizontale par défaut et sans espacement.
     */
    public HSplitWrapLayout() {
        this(WrapDirection.HORIZONTAL, 0, 0);
    }

    /**
     * Crée un layout avec une direction personnalisée et sans espacement.
     *
     * @param direction la direction principale de disposition
     */
    public HSplitWrapLayout(WrapDirection direction) {
        this(direction, 0, 0);
    }

    /**
     * Crée un layout entièrement paramétré.
     *
     * @param direction la direction principale de disposition
     * @param hGap      l'espacement horizontal entre composants
     * @param vGap      l'espacement vertical entre composants
     */
    public HSplitWrapLayout(WrapDirection direction, int hGap, int vGap) {
        this.direction = direction;
        this.hGap      = hGap;
        this.vGap      = vGap;
    }

    // =========================================================================
    // Méthodes principales du LayoutManager2
    // =========================================================================

    /**
     * Calcule et applique les bounds de chaque composant enfant.
     *
     * Le calcul se fait en deux temps :
     * 1. On regroupe les composants en lignes (ou colonnes) selon l'espace dispo.
     * 2. On étire chaque composant pour qu'il remplisse sa portion de la ligne.
     *
     * @param parent le conteneur dont on dispose les enfants
     */
    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {

            Insets insets     = parent.getInsets();
            int availableW    = parent.getWidth()  - insets.left - insets.right;
            int availableH    = parent.getHeight() - insets.top  - insets.bottom;
            int startX        = insets.left;
            int startY        = insets.top;

            // On récupère uniquement les composants visibles
            List<Component> visibles = getVisibleComponents(parent);

            if (visibles.isEmpty()) {
                return;
            }

            if (direction == WrapDirection.HORIZONTAL) {
                layoutHorizontal(visibles, startX, startY, availableW, availableH);
            } else {
                layoutVertical(visibles, startX, startY, availableW, availableH);
            }
        }
    }

    /**
     * Positionne les composants en lignes horizontales avec retour à la ligne.
     * Chaque composant est étiré pour remplir la hauteur de sa ligne.
     *
     * @param components les composants visibles à positionner
     * @param startX     position X de départ
     * @param startY     position Y de départ
     * @param availableW largeur totale disponible
     * @param availableH hauteur totale disponible (non utilisée ici, gérée par scroll)
     */
    private void layoutHorizontal(List<Component> components,
                                   int startX, int startY,
                                   int availableW, int availableH) {

        // Découpe les composants en lignes
        List<List<Component>> lignes = decomposerEnLignes(components, availableW);

        int y = startY;

        for (List<Component> ligne : lignes) {

            // La hauteur de la ligne est dictée par le composant le plus haut
            int hauteurLigne = getHauteurMax(ligne);

            // On distribue la largeur disponible entre les composants de la ligne
            int[] largeurs = distribuerLargeur(ligne, availableW);

            int x = startX;

            for (int i = 0; i < ligne.size(); i++) {
                Component c = ligne.get(i);
                c.setBounds(x, y, largeurs[i], hauteurLigne);
                x += largeurs[i] + hGap;
            }

            y += hauteurLigne + vGap;
        }
    }

    /**
     * Positionne les composants en colonnes verticales avec retour à la colonne.
     * Chaque composant est étiré pour remplir la largeur de sa colonne.
     *
     * @param components les composants visibles à positionner
     * @param startX     position X de départ
     * @param startY     position Y de départ
     * @param availableW largeur totale disponible
     * @param availableH hauteur totale disponible
     */
    private void layoutVertical(List<Component> components,
                                 int startX, int startY,
                                 int availableW, int availableH) {

        // Découpe les composants en colonnes
        List<List<Component>> colonnes = decomposerEnColonnes(components, availableH);

        int x = startX;

        for (List<Component> colonne : colonnes) {

            // La largeur de la colonne est dictée par le composant le plus large
            int largeurColonne = getLargeurMax(colonne);

            // On distribue la hauteur disponible entre les composants de la colonne
            int[] hauteurs = distribuerHauteur(colonne, availableH);

            int y = startY;

            for (int i = 0; i < colonne.size(); i++) {
                Component c = colonne.get(i);
                c.setBounds(x, y, largeurColonne, hauteurs[i]);
                y += hauteurs[i] + vGap;
            }

            x += largeurColonne + hGap;
        }
    }

    // =========================================================================
    // Calcul de la taille préférée
    // =========================================================================

    /**
     * Calcule la taille préférée du conteneur en tenant compte du wrapping.
     * C'est cette valeur que le JScrollPane utilise pour décider
     * si une scrollbar est nécessaire.
     *
     * @param parent le conteneur parent
     * @return la dimension préférée calculée
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {

            Insets insets     = parent.getInsets();
            int availableW    = parent.getWidth()  - insets.left - insets.right;
            int availableH    = parent.getHeight() - insets.top  - insets.bottom;

            List<Component> visibles = getVisibleComponents(parent);

            if (visibles.isEmpty()) {
                return new Dimension(
                    insets.left + insets.right,
                    insets.top  + insets.bottom
                );
            }

            int totalW = insets.left + insets.right;
            int totalH = insets.top  + insets.bottom;

            if (direction == WrapDirection.HORIZONTAL) {
                List<List<Component>> lignes = decomposerEnLignes(visibles,
                    availableW > 0 ? availableW : Integer.MAX_VALUE);

                for (List<Component> ligne : lignes) {
                    totalH += getHauteurMax(ligne) + vGap;
                }
                if (!lignes.isEmpty()) totalH -= vGap;

                // La largeur préférée est la largeur de la ligne la plus large
                for (List<Component> ligne : lignes) {
                    int largeurLigne = getLargeurTotaleLigne(ligne);
                    totalW = Math.max(totalW, largeurLigne + insets.left + insets.right);
                }

            } else {
                List<List<Component>> colonnes = decomposerEnColonnes(visibles,
                    availableH > 0 ? availableH : Integer.MAX_VALUE);

                for (List<Component> colonne : colonnes) {
                    totalW += getLargeurMax(colonne) + hGap;
                }
                if (!colonnes.isEmpty()) totalW -= hGap;

                for (List<Component> colonne : colonnes) {
                    int hauteurColonne = getHauteurTotaleColonne(colonne);
                    totalH = Math.max(totalH, hauteurColonne + insets.top + insets.bottom);
                }
            }

            return new Dimension(totalW, totalH);
        }
    }

    /**
     * Retourne la taille minimale du conteneur.
     * On utilise la taille préférée comme référence minimale.
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    /**
     * Retourne la taille maximale — illimitée, le layout s'adapte.
     */
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // =========================================================================
    // Méthodes utilitaires — décomposition en lignes et colonnes
    // =========================================================================

    /**
     * Regroupe les composants en lignes en respectant la largeur disponible.
     * Chaque composant qui dépasserait la largeur courante passe à la ligne suivante.
     *
     * @param components  la liste des composants à répartir
     * @param largeurDispo la largeur maximale par ligne
     * @return une liste de lignes, chacune contenant ses composants
     */
    private List<List<Component>> decomposerEnLignes(List<Component> components,
                                                      int largeurDispo) {
        List<List<Component>> lignes    = new ArrayList<>();
        List<Component>       ligneCourante = new ArrayList<>();
        int                   xCourant  = 0;

        for (Component c : components) {
            int largeurComp = c.getPreferredSize().width;

            // Si le composant ne rentre pas sur la ligne courante, on passe à la suivante
            // Sauf si la ligne est vide : dans ce cas on force quand même le composant
            if (!ligneCourante.isEmpty() && xCourant + hGap + largeurComp > largeurDispo) {
                lignes.add(ligneCourante);
                ligneCourante = new ArrayList<>();
                xCourant      = 0;
            }

            ligneCourante.add(c);
            xCourant += (ligneCourante.size() > 1 ? hGap : 0) + largeurComp;
        }

        // Ne pas oublier la dernière ligne en cours
        if (!ligneCourante.isEmpty()) {
            lignes.add(ligneCourante);
        }

        return lignes;
    }

    /**
     * Regroupe les composants en colonnes en respectant la hauteur disponible.
     *
     * @param components   la liste des composants à répartir
     * @param hauteurDispo la hauteur maximale par colonne
     * @return une liste de colonnes, chacune contenant ses composants
     */
    private List<List<Component>> decomposerEnColonnes(List<Component> components,
                                                        int hauteurDispo) {
        List<List<Component>> colonnes       = new ArrayList<>();
        List<Component>       colonneCourante = new ArrayList<>();
        int                   yCourant        = 0;

        for (Component c : components) {
            int hauteurComp = c.getPreferredSize().height;

            if (!colonneCourante.isEmpty() && yCourant + vGap + hauteurComp > hauteurDispo) {
                colonnes.add(colonneCourante);
                colonneCourante = new ArrayList<>();
                yCourant        = 0;
            }

            colonneCourante.add(c);
            yCourant += (colonneCourante.size() > 1 ? vGap : 0) + hauteurComp;
        }

        if (!colonneCourante.isEmpty()) {
            colonnes.add(colonneCourante);
        }

        return colonnes;
    }

    // =========================================================================
    // Méthodes utilitaires — distribution de l'espace
    // =========================================================================

    /**
     * Distribue la largeur disponible entre les composants d'une ligne.
     *
     * On commence par attribuer à chaque composant sa largeur préférée,
     * puis on répartit l'espace restant équitablement entre tous les composants.
     *
     * @param ligne      les composants de la ligne
     * @param largeurDispo la largeur totale disponible
     * @return un tableau de largeurs, une par composant
     */
    private int[] distribuerLargeur(List<Component> ligne, int largeurDispo) {
        int n        = ligne.size();
        int[] result = new int[n];

        // Espace total consommé par les gaps entre composants
        int gapsTotal = (n - 1) * hGap;

        // Largeur disponible pour les composants eux-mêmes
        int espaceComposants = largeurDispo - gapsTotal;

        // Distribution équitable
        int base  = espaceComposants / n;
        int reste = espaceComposants % n;

        for (int i = 0; i < n; i++) {
            // Les premiers composants absorbent le pixel restant si nécessaire
            result[i] = base + (i < reste ? 1 : 0);
        }

        return result;
    }

    /**
     * Distribue la hauteur disponible entre les composants d'une colonne.
     *
     * @param colonne      les composants de la colonne
     * @param hauteurDispo la hauteur totale disponible
     * @return un tableau de hauteurs, une par composant
     */
    private int[] distribuerHauteur(List<Component> colonne, int hauteurDispo) {
        int n        = colonne.size();
        int[] result = new int[n];

        int gapsTotal        = (n - 1) * vGap;
        int espaceComposants = hauteurDispo - gapsTotal;
        int base             = espaceComposants / n;
        int reste            = espaceComposants % n;

        for (int i = 0; i < n; i++) {
            result[i] = base + (i < reste ? 1 : 0);
        }

        return result;
    }

    // =========================================================================
    // Méthodes utilitaires — mesures
    // =========================================================================

    /** Retourne la hauteur maximale parmi les composants d'une ligne. */
    private int getHauteurMax(List<Component> ligne) {
        int max = 0;
        for (Component c : ligne) {
            max = Math.max(max, c.getPreferredSize().height);
        }
        return max;
    }

    /** Retourne la largeur maximale parmi les composants d'une colonne. */
    private int getLargeurMax(List<Component> colonne) {
        int max = 0;
        for (Component c : colonne) {
            max = Math.max(max, c.getPreferredSize().width);
        }
        return max;
    }

    /** Retourne la largeur totale occupée par une ligne (composants + gaps). */
    private int getLargeurTotaleLigne(List<Component> ligne) {
        int total = 0;
        for (Component c : ligne) {
            total += c.getPreferredSize().width;
        }
        total += (ligne.size() - 1) * hGap;
        return total;
    }

    /** Retourne la hauteur totale occupée par une colonne (composants + gaps). */
    private int getHauteurTotaleColonne(List<Component> colonne) {
        int total = 0;
        for (Component c : colonne) {
            total += c.getPreferredSize().height;
        }
        total += (colonne.size() - 1) * vGap;
        return total;
    }

    /** Retourne uniquement les composants visibles du conteneur parent. */
    private List<Component> getVisibleComponents(Container parent) {
        List<Component> visibles = new ArrayList<>();
        for (Component c : parent.getComponents()) {
            if (c.isVisible()) {
                visibles.add(c);
            }
        }
        return visibles;
    }

    // =========================================================================
    // Méthodes non utilisées mais requises par l'interface LayoutManager2
    // =========================================================================

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        // Pas de contraintes dans ce layout, l'ordre d'insertion suffit
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Non utilisé
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // Rien à nettoyer, le layout ne maintient pas de référence aux composants
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // Pas d'état interne à invalider
    }

    // =========================================================================
    // Getters et Setters
    // =========================================================================

    public WrapDirection getDirection() {
        return direction;
    }

    /**
     * Modifie la direction de disposition et force un recalcul immédiat.
     *
     * @param direction la nouvelle direction
     */
    public void setDirection(WrapDirection direction) {
        this.direction = direction;
    }

    public int getHGap() {
        return hGap;
    }

    public void setHGap(int hGap) {
        this.hGap = hGap;
    }

    public int getVGap() {
        return vGap;
    }

    public void setVGap(int vGap) {
        this.vGap = vGap;
    }
}
