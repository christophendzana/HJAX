package hsplitpane;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;
import hsplitpane.HSplitPane.WrapDirection;


/**
 * Gestionnaire de disposition avec retour à la ligne automatique.
 *
 * Les composants enfants sont alignés dans une direction principale
 * (horizontale par défaut) et s'étirent pour occuper tout l'espace disponible
 * sur l'axe secondaire. Quand l'espace sur l'axe principal est épuisé, les
 * composants suivants passent à la ligne ou colonne suivante. Si même le retour
 * à la ligne ne suffit plus, le JScrollPane parent prend le relais.
 *
 * Ce layout est conçu pour être utilisé à l'intérieur des zones de contenu de
 * HSplitZone.
 */
public class HSplitWrapLayout implements LayoutManager2 {

    // -------------------------------------------------------------------------
    // Paramètres de disposition
    // -------------------------------------------------------------------------
    /**
     * Direction principale d'alignement des composants.
     */
    private WrapDirection direction;

    /**
     * Espace horizontal en pixels entre deux composants adjacents.
     */
    private int hGap;

    /**
     * Espace vertical en pixels entre deux composants adjacents.
     */
    private int vGap;

    /**
 * Si true, les composants s'étirent pour occuper tout l'espace disponible
 * sur l'axe principal. Si false, chaque composant conserve sa preferredSize.
 * False par défaut.
 */
private boolean etirer = false;
    
    // =========================================================================
    // Constructeurs
    // =========================================================================
    /**
     * Crée un layout avec la direction horizontale par défaut et sans
     * espacement.
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
     * @param hGap l'espacement horizontal entre composants
     * @param vGap l'espacement vertical entre composants
     */
    public HSplitWrapLayout(WrapDirection direction, int hGap, int vGap) {
        this.direction = direction;
        this.hGap = hGap;
        this.vGap = vGap;
    }

    // =========================================================================
    // Méthodes principales du LayoutManager2
    // =========================================================================
    /**
     * Calcule et applique les bounds de chaque composant enfant.
     *
     * Le calcul se fait en deux temps : 1. On regroupe les composants en lignes
     * (ou colonnes) selon l'espace dispo. 2. On étire chaque composant pour
     * qu'il remplisse sa portion de la ligne.
     *
     * @param parent le conteneur dont on dispose les enfants
     */
    @Override
    public void layoutContainer(Container parent) {
        
        // synchronized (parent.getTreeLock(): empêches que quelqu’un modifie la
        // hiérarchie des composants en même temps que tu es en train de la 
        // parcourir et de positionner les éléments.
        
        synchronized (parent.getTreeLock()) {

            Insets insets = parent.getInsets();
            int availableW = parent.getWidth() - insets.left - insets.right;
            int availableH = parent.getHeight() - insets.top - insets.bottom;
            int startX = insets.left;
            int startY = insets.top;

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

    
    public boolean isEtirer() {
    return etirer;
}

public void setEtirer(boolean etirer) {
    this.etirer = etirer;
}
    
    /**
     * Positionne les composants en lignes horizontales avec retour à la ligne.
     * Chaque composant est étiré pour remplir la hauteur de sa ligne.
     *
     * @param components les composants visibles à positionner
     * @param startX position X de départ
     * @param startY position Y de départ
     * @param availableW largeur totale disponible
     * @param availableH hauteur totale disponible (non utilisée ici, gérée par
     * scroll)
     */
   /**
 /**
 * Positionne les composants en lignes horizontales avec retour à la ligne.
 * Si etirer est true, chaque composant s'étire en hauteur pour occuper
 * toute la hauteur de sa ligne, et la largeur est distribuée équitablement.
 */
private void layoutHorizontal(List<Component> components,
                               int startX, int startY,
                               int availableW, int availableH) {

    List<List<Component>> lignes = decomposerEnLignes(components, availableW, availableH);

    int y = startY;

    for (List<Component> ligne : lignes) {

        int hauteurLigne = etirer ? distribuerHauteurLigne(lignes, availableH)
                                  : getHauteurMax(ligne);

        int[] largeurs = etirer ? distribuerLargeur(ligne, availableW) : null;

        int x = startX;
        int i = 0;

        for (Component c : ligne) {
            int w = etirer ? largeurs[i] : c.getPreferredSize().width;
            int h = hauteurLigne;
            c.setBounds(x, y, w, h);
            x += w + hGap;
            i++;
        }

        y += hauteurLigne + vGap;
    }
}

    /**
     * Positionne les composants en colonnes verticales avec retour à la
     * colonne. Chaque composant est étiré pour remplir la largeur de sa
     * colonne.
     *
     * @param components les composants visibles à positionner
     * @param startX position X de départ
     * @param startY position Y de départ
     * @param availableW largeur totale disponible
     * @param availableH hauteur totale disponible
     */
/**
 * Positionne les composants en colonnes verticales avec retour à la colonne.
 * Si etirer est true, chaque composant s'étire en hauteur pour occuper
 * sa portion de la colonne ET en largeur pour occuper toute la largeur
 * de sa colonne — particulièrement utile pour les zones WEST et EAST.
 */
private void layoutVertical(List<Component> components,
                             int startX, int startY,
                             int availableW, int availableH) {

    List<List<Component>> colonnes = decomposerEnColonnes(components, availableH, availableW);

    int x = startX;

    for (List<Component> colonne : colonnes) {

        // En mode étirement, la largeur de la colonne est la largeur totale
        // disponible divisée par le nombre de colonnes — les composants
        // occupent ainsi toute la largeur de leur colonne
        int largeurColonne = etirer ? distribuerLargeurColonne(colonnes, availableW)
                                    : getLargeurMax(colonne);

        int[] hauteurs = etirer ? distribuerHauteur(colonne, availableH) : null;

        int y = startY;
        int i = 0;

        for (Component c : colonne) {
            int w = etirer ? largeurColonne : c.getPreferredSize().width;
            int h = etirer ? hauteurs[i]   : c.getPreferredSize().height;
            c.setBounds(x, y, w, h);
            y += h + vGap;
            i++;
        }

        x += largeurColonne + hGap;
        i = 0;
    }
}

/**
 * Calcule la hauteur équitable allouée à chaque ligne quand etirer est true.
 * Les gaps entre lignes sont soustraits avant la distribution.
 *
 * @param lignes       toutes les lignes du layout
 * @param hauteurDispo la hauteur totale disponible
 * @return la hauteur allouée à chaque ligne
 */
private int distribuerHauteurLigne(List<List<Component>> lignes, int hauteurDispo) {
    int n          = lignes.size();
    int gapsTotal  = (n - 1) * vGap;
    int espace     = Math.max(0, hauteurDispo - gapsTotal);
    return n > 0 ? espace / n : 0;
}

/**
 * Calcule la largeur équitable allouée à chaque colonne quand etirer est true.
 * Les gaps entre colonnes sont soustraits avant la distribution.
 *
 * @param colonnes     toutes les colonnes du layout
 * @param largeurDispo la largeur totale disponible
 * @return la largeur allouée à chaque colonne
 */
private int distribuerLargeurColonne(List<List<Component>> colonnes, int largeurDispo) {
    int n         = colonnes.size();
    int gapsTotal = (n - 1) * hGap;
    int espace    = Math.max(0, largeurDispo - gapsTotal);
    return n > 0 ? espace / n : 0;
}

    // =========================================================================
    // Calcul de la taille préférée
    // =========================================================================
    

    /**
 * Calcule la taille préférée réelle du contenu après wrapping.
 *
 * On utilise la largeur du viewport du JScrollPane parent comme référence
 * pour simuler le wrapping. C'est cette valeur que le JScrollPane consulte
 * pour décider si une scrollbar est nécessaire.
 */
@Override
public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {

        Insets insets = parent.getInsets();

        // Largeur de référence pour le wrapping horizontal
        int refW = parent.getWidth() - insets.left - insets.right;
        if (refW <= 0 && parent.getParent() != null) {
            refW = parent.getParent().getWidth() - insets.left - insets.right;
        }
        if (refW <= 0) refW = Integer.MAX_VALUE;

        // Hauteur de référence pour le wrapping vertical
        int refH = parent.getHeight() - insets.top - insets.bottom;
        if (refH <= 0 && parent.getParent() != null) {
            refH = parent.getParent().getHeight() - insets.top - insets.bottom;
        }
        if (refH <= 0) refH = Integer.MAX_VALUE;

        List<Component> visibles = getVisibleComponents(parent);

        if (visibles.isEmpty()) {
            return new Dimension(
                insets.left + insets.right,
                insets.top  + insets.bottom
            );
        }

        int totalW = 0;
        int totalH = insets.top + insets.bottom;

        if (direction == WrapDirection.HORIZONTAL) {
            List<List<Component>> lignes = decomposerEnLignes(visibles, refW, refH);

            for (List<Component> ligne : lignes) {
                totalH += getHauteurMax(ligne) + vGap;
                int largeurLigne = getLargeurTotaleLigne(ligne);
                totalW = Math.max(totalW, largeurLigne + insets.left + insets.right);
            }
            if (!lignes.isEmpty()) totalH -= vGap;

        } else {
            List<List<Component>> colonnes = decomposerEnColonnes(visibles, refH, refW);

            for (List<Component> colonne : colonnes) {
                totalW += getLargeurMax(colonne) + hGap;
                int hauteurColonne = getHauteurTotaleColonne(colonne);
                totalH = Math.max(totalH, hauteurColonne + insets.top + insets.bottom);
            }
            if (!colonnes.isEmpty()) totalW -= hGap;
        }

        return new Dimension(Math.max(totalW, insets.left + insets.right), totalH);
    }
}


    /**
     * Retourne la taille minimale du conteneur. On utilise la taille préférée
     * comme référence minimale.
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
 * Si une nouvelle ligne n'est plus possible faute de hauteur, tous les
 * composants restants sont forcés sur la dernière ligne — le scroll prend
 * le relais.
 *
 * @param components   les composants à répartir
 * @param largeurDispo la largeur maximale par ligne
 * @param hauteurDispo la hauteur totale disponible — sert à vérifier si
 *                     une nouvelle ligne est encore possible
 * @return une liste de lignes, chacune contenant ses composants
 */
private List<List<Component>> decomposerEnLignes(List<Component> components,
                                                  int largeurDispo,
                                                  int hauteurDispo) {
    List<List<Component>> lignes        = new ArrayList<>();
    List<Component>       ligneCourante = new ArrayList<>();
    int                   xCourant     = 0;
    int                   hauteurOccupee = 0;

    for (Component c : components) {
        int largeurComp = c.getPreferredSize().width;
        int hauteurComp = c.getPreferredSize().height;

        boolean rentreEnLargeur = ligneCourante.isEmpty()
                || xCourant + hGap + largeurComp <= largeurDispo;

        if (!rentreEnLargeur) {
            // On vérifie si la hauteur disponible permet une nouvelle ligne
            int hauteurLigneCourante = getHauteurMax(ligneCourante);
            boolean nouvelleLignePossible =
                    hauteurOccupee + hauteurLigneCourante + vGap + hauteurComp <= hauteurDispo;

            if (nouvelleLignePossible) {
                // On valide la ligne courante et on en commence une nouvelle
                lignes.add(ligneCourante);
                hauteurOccupee += hauteurLigneCourante + vGap;
                ligneCourante  = new ArrayList<>();
                xCourant       = 0;
            }
            // Si pas possible → on force le composant sur la ligne courante
            // et le scroll prendra le relais
        }

        ligneCourante.add(c);
        xCourant += (ligneCourante.size() > 1 ? hGap : 0) + largeurComp;
    }

    if (!ligneCourante.isEmpty()) {
        lignes.add(ligneCourante);
    }

    return lignes;
}

    /**
 * Regroupe les composants en colonnes en respectant la hauteur disponible.
 * Si une nouvelle colonne n'est plus possible faute de largeur, tous les
 * composants restants sont forcés dans la dernière colonne — le scroll
 * prend le relais.
 *
 * @param components   les composants à répartir
 * @param hauteurDispo la hauteur maximale par colonne
 * @param largeurDispo la largeur totale disponible — sert à vérifier si
 *                     une nouvelle colonne est encore possible
 * @return une liste de colonnes, chacune contenant ses composants
 */
private List<List<Component>> decomposerEnColonnes(List<Component> components,
                                                    int hauteurDispo,
                                                    int largeurDispo) {
    List<List<Component>> colonnes       = new ArrayList<>();
    List<Component>       colonneCourante = new ArrayList<>();
    int                   yCourant       = 0;
    int                   largeurOccupee = 0;

    for (Component c : components) {
        int largeurComp = c.getPreferredSize().width;
        int hauteurComp = c.getPreferredSize().height;

        boolean rentreEnHauteur = colonneCourante.isEmpty()
                || yCourant + vGap + hauteurComp <= hauteurDispo;

        if (!rentreEnHauteur) {
            // On vérifie si la largeur disponible permet une nouvelle colonne
            int largeurColonneCourante = getLargeurMax(colonneCourante);
            boolean nouvelleColonnePossible =
                    largeurOccupee + largeurColonneCourante + hGap + largeurComp <= largeurDispo;

            if (nouvelleColonnePossible) {
                // On valide la colonne courante et on en commence une nouvelle
                colonnes.add(colonneCourante);
                largeurOccupee += largeurColonneCourante + hGap;
                colonneCourante = new ArrayList<>();
                yCourant        = 0;
            }
            // Si pas possible → on force le composant dans la colonne courante
            // et le scroll prendra le relais
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
     * On commence par attribuer à chaque composant sa largeur préférée, puis on
     * répartit l'espace restant équitablement entre tous les composants.
     *
     * @param ligne les composants de la ligne
     * @param largeurDispo la largeur totale disponible
     * @return un tableau de largeurs, une par composant
     */
    private int[] distribuerLargeur(List<Component> ligne, int largeurDispo) {
        int n = ligne.size();
        int[] result = new int[n];

        // Espace total consommé par les gaps entre composants
        int gapsTotal = (n - 1) * hGap;

        // Largeur disponible pour les composants eux-mêmes
        int espaceComposants = largeurDispo - gapsTotal;

        // Distribution équitable
        int base = espaceComposants / n;
        int reste = espaceComposants % n;

        for (int i = 0; i < n; i++) {
            // Les premiers composants absorbent le pixel restant si nécessaire
            result[i] = base + (i < reste ? 1 : 0);
        }

        return result;
    }

    /**
 * Distribue la hauteur disponible entre les composants d'une colonne.
 * Les gaps entre composants sont soustraits avant la distribution.
 */
private int[] distribuerHauteur(List<Component> colonne, int hauteurDispo) {
    int n        = colonne.size();
    int[] result = new int[n];

    int gapsTotal        = (n - 1) * vGap;
    int espaceComposants = Math.max(0, hauteurDispo - gapsTotal);
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
    /**
     * Retourne la hauteur maximale parmi les composants d'une ligne.
     */
    private int getHauteurMax(List<Component> ligne) {
        int max = 0;
        for (Component c : ligne) {
            max = Math.max(max, c.getPreferredSize().height);
        }
        return max;
    }

    /**
     * Retourne la largeur maximale parmi les composants d'une colonne.
     */
    private int getLargeurMax(List<Component> colonne) {
        int max = 0;
        for (Component c : colonne) {
            max = Math.max(max, c.getPreferredSize().width);
        }
        return max;
    }

    /**
     * Retourne la largeur totale occupée par une ligne (composants + gaps).
     */
    private int getLargeurTotaleLigne(List<Component> ligne) {
        int total = 0;
        for (Component c : ligne) {
            total += c.getPreferredSize().width;
        }
        total += (ligne.size() - 1) * hGap;
        return total;
    }

    /**
     * Retourne la hauteur totale occupée par une colonne (composants + gaps).
     */
    private int getHauteurTotaleColonne(List<Component> colonne) {
        int total = 0;
        for (Component c : colonne) {
            total += c.getPreferredSize().height;
        }
        total += (colonne.size() - 1) * vGap;
        return total;
    }

    /**
     * Retourne uniquement les composants visibles du conteneur parent.
     */
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
