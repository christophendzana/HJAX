/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rubban.layout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * LineWrapper / GroupLayoutEngine
 *
 * Responsabilités : - Organiser une liste de Component en lignes (wrapping) à
 * partir d'une largeur disponible (innerWidth) et d'un espacement horizontal
 * (spacing). - Calculer la hauteur d'une ligne (max des preferred heights). -
 * Positionner (setBounds) les composants d'une ligne dans le rectangle donné. -
 * Calculer la hauteur totale requise pour une liste de preferred sizes (utilisé
 * par le simulateur de preferred height).
 *
 * @author FIDELE
 */
public class LineWrapper {

    public LineWrapper() {
    }

    /**
     * Organise les composants en lignes selon innerWidth et spacing.
     *
     * Doit être appelé sur l'EDT car utilise getPreferredSize().
     *
     * @param components liste des composants (ordonnés)
     * @param innerWidth largeur disponible pour la ligne (px)
     * @param spacing espacement horizontal entre composants (px)
     * @return liste de lignes; chaque ligne est une List<Component>
     */
    public List<List<Component>> wrapIntoLines(List<Component> components, int innerWidth, int spacing) {
        ensureEdt();

        List<List<Component>> lines = new ArrayList<>();
        if (components == null || components.isEmpty()) {
            return lines;
        }

        List<Component> currentLine = new ArrayList<>();
        int currentLineWidth = 0;

        for (Component comp : components) {
            Dimension pref = comp.getPreferredSize();
            int compWidth = (pref != null) ? pref.width : 0;
            int requiredWidth = compWidth;

            if (!currentLine.isEmpty()) {
                requiredWidth += spacing;
            }

            if (currentLine.isEmpty() || currentLineWidth + requiredWidth <= innerWidth) {
                currentLine.add(comp);
                currentLineWidth = currentLine.isEmpty() ? compWidth : currentLineWidth + requiredWidth;

            } else {
                // Fermez la ligne actuelle et commencez-en une nouvelle.
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = new ArrayList<>();
                currentLine.add(comp);
                currentLineWidth = compWidth;
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        return lines;
    }

    /**
     * Calcule la hauteur d'une ligne composée de composants (max preferred
     * height). Doit être appelé sur l'EDT.
     *
     * @param line liste de composants (non null)
     * @return hauteur en px (>= 0)
     */
    public int calculateLineHeight(List<Component> line) {
        ensureEdt();

        int max = 0;
        if (line == null || line.isEmpty()) {
            return max;
        }
        for (Component c : line) {
            Dimension pref = c.getPreferredSize();
            if (pref != null) {
                max = Math.max(max, pref.height);
            }
        }
        return max;
    }

    /**
     * Positionne une ligne de composants horizontalement à partir de startX et
     * lineY, avec un espacement donné et une hauteur de ligne (lineHeight).
     *
     * Doit être appelé sur l'EDT (effectue setBounds()).
     *
     * @param line la ligne de composants
     * @param startX coordonnée X de départ
     * @param lineY coordonnée Y de la ligne
     * @param spacing espacement horizontal entre composants
     * @param lineHeight hauteur de la ligne (pour centrage vertical des
     * composants)
     */
    public void positionLine(List<Component> line, int startX, int lineY, int spacing, int lineHeight) {
        ensureEdt();
        if (line == null || line.isEmpty()) {
            return;
        }

        int currentX = startX;
        for (Component comp : line) {
            Dimension pref = comp.getPreferredSize();
            int compW = (pref != null) ? pref.width : 0;
            int compH = (pref != null) ? pref.height : 0;

            // Centre verticalement dans la ligne
            int compY = lineY + Math.max(0, (lineHeight - compH) / 2);

            comp.setBounds(currentX, compY, compW, Math.min(compH, lineHeight));
            currentX += compW + spacing;
        }
    }

    /**
     * Calcule la hauteur totale nécessaire pour afficher la liste de preferred
     * sizes dans un espace innerWidth, avec spacing entre composants et padding
     * top/bottom.
     *
     * Méthode purement calculatoire (ne touche pas Swing ni Components) : utile
     * pour PreferredSizeCalculator. Elle opère sur une liste de Dimension
     * (pré-mesurées).
     *
     * @param prefs liste des preferred sizes (Dimensions) dans l'ordre
     * @param innerWidth largeur disponible
     * @param spacing espacement horizontal
     * @param padding padding top+bottom (sera ajouté au total)
     * @return hauteur totale requise (padding inclus)
     */
    public int computeHeightForPreferences(List<Dimension> prefs, int innerWidth, int spacing, int padding) {
        if (prefs == null || prefs.isEmpty()) {
            // hauteur minimale = padding top+bottom
            return Math.max(0, padding * 2);
        }

        // Si innerWidth <= 1 on force empilement vertical
        if (innerWidth <= 1) {
            int total = 0;
            for (int i = 0; i < prefs.size(); i++) {
                if (i > 0) {
                    total += spacing;
                }
                total += prefs.get(i).height;
            }
            total += padding * 2;
            return total;
        }

        int totalHeight = 0;
        int currentLineWidth = 0;
        int currentLineHeight = 0;
        boolean firstInLine = true;

        Iterator<Dimension> it = prefs.iterator();
        while (it.hasNext()) {
            Dimension pd = it.next();
            int compW = pd.width;
            int compH = pd.height;

            int requiredW = compW;
            if (!firstInLine) {
                requiredW += spacing;
            }

            if (firstInLine || currentLineWidth + requiredW <= innerWidth) {
                // Ajoute à la ligne actuelle
                currentLineWidth = firstInLine ? compW : currentLineWidth + spacing + compW;
                currentLineHeight = Math.max(currentLineHeight, compH);
                firstInLine = false;
            } else {
                // Ferme la ligne actuelle
                if (totalHeight > 0) {
                    totalHeight += spacing; // spacing between lines
                }
                totalHeight += currentLineHeight;

                // Nouvelle ligne
                currentLineWidth = compW;
                currentLineHeight = compH;
                firstInLine = false;
            }
        }

        // add last line
        if (!firstInLine || currentLineHeight > 0) {
            if (totalHeight > 0) {
                totalHeight += spacing;
            }
            totalHeight += currentLineHeight;
        }

        totalHeight += padding * 2;
        return totalHeight;
    }

    /**
     * Dispose des composants à l'intérieur d'un
     * rectangle de groupe à l'aide du remplissage et de l'espacement.
     *
     * Cette méthode orchestre les opérations wrapIntoLines, calculateLineHeight
     * et positionLine.
     *
     * Must be called on EDT because it uses getPreferredSize() and sets bounds.     
     * @param components list of components for the group (order preserved)
     * @param groupRect rectangle of the group's content area
     * @param padding group padding (px)
     * @param spacing spacing between components (px)
     */
    public void layoutComponentsInGroup(List<Component> components, Rectangle groupRect, int padding, int spacing) {
        ensureEdt();
        if (components == null || components.isEmpty() || groupRect == null) {
            return;
        }

        int innerX = groupRect.x + padding;
        int innerY = groupRect.y + padding;
        int innerWidth = groupRect.width - (2 * padding);
        int innerHeight = groupRect.height - (2 * padding);

        if (innerWidth <= 0 || innerHeight <= 0) {
            return;
        }

        List<List<Component>> lines = wrapIntoLines(components, innerWidth, spacing);

        int currentY = innerY;
        for (List<Component> line : lines) {
            int lineHeight = calculateLineHeight(line);
            if (currentY + lineHeight > innerY + innerHeight) {
                break; // no more vertical space
            }
            positionLine(line, innerX, currentY, spacing, lineHeight);
            currentY += lineHeight + spacing;
        }
    }

    private void ensureEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("LineWrapper: method must be called on the EDT");
        }
    }

}
