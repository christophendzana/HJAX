package hsupertable.model;

import hsupertable.model.structure.CellNode;
import java.util.Arrays;

/**
 * Adresse immuable d'une sous-cellule, par POSITION dans l'arbre de
 * subdivision — jamais par référence d'objet. ROOT désigne la cellule entière.
 *
 * Pourquoi un chemin plutôt qu'un objet ? Un objet périmé (après une insertion
 * de ligne, un tri, une fusion...) continue d'exister mais ne désigne plus rien
 * de valide ; un chemin, lui, se revalide à chaque usage contre la structure
 * courante (voir {@link #resolve(CellNode)}). Même idée qu'un TreePath en
 * Swing, ou qu'un index plutôt qu'un pointeur.
 *
 * Chaque pas vaut false = FIRST, true = SECOND, dans l'ordre racine → feuille.
 */
public final class SubCellPath {

    public static final SubCellPath ROOT = new SubCellPath(new boolean[0]);

    private final boolean[] steps;

    private SubCellPath(boolean[] steps) {
        this.steps = steps;
    }

    /**
     * Chemin du fils FIRST (second = false) ou SECOND (second = true).
     */
    public SubCellPath child(boolean second) {
        boolean[] next = Arrays.copyOf(steps, steps.length + 1);
        next[steps.length] = second;
        return new SubCellPath(next);
    }

    /**
     * Chemin du parent. Interdit sur ROOT.
     */
    public SubCellPath parent() {
        if (isRoot()) {
            throw new IllegalStateException("ROOT n'a pas de parent");
        }
        return new SubCellPath(Arrays.copyOf(steps, steps.length - 1));
    }

    public boolean isRoot() {
        return steps.length == 0;
    }

    public int depth() {
        return steps.length;
    }

    /**
     * Pas au niveau donné (0 = premier pas depuis la racine).
     */
    public boolean stepAt(int level) {
        return steps[level];
    }

    /**
     * Vrai si le dernier pas mène au fils SECOND. Interdit sur ROOT.
     */
    public boolean lastStepIsSecond() {
        if (isRoot()) {
            throw new IllegalStateException("ROOT n'a pas de dernier pas");
        }
        return steps[steps.length - 1];
    }

    /**
     * Descend depuis root en suivant ce chemin.
     *
     * @return le noeud désigné, ou null si le chemin ne correspond plus à la
     * structure (noeud devenu feuille, par exemple).
     */
    public CellNode resolve(CellNode root) {
        CellNode node = root;
        for (boolean second : steps) {
            if (node == null || node.isLeaf()) {
                return null;
            }
            node = second ? node.getSecond() : node.getFirst();
        }
        return node;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SubCellPath other && Arrays.equals(steps, other.steps);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(steps);
    }

    @Override
    public String toString() {
        if (isRoot()) {
            return "ROOT";
        }
        StringBuilder sb = new StringBuilder();
        for (boolean second : steps) {
            sb.append(second ? "/2" : "/1");
        }
        return sb.toString();
    }
}
