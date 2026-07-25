package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dessine un ou plusieurs blocs de texte entourés d'une bordure configurable
 * (chaque côté peut être activé ou non indépendamment).
 *
 * <p>
 * Chaque chaîne de la liste devient un bloc distinct, empilé verticalement sous
 * le précédent, avec la même configuration de bordure appliquée à chacun.</p>
 *
 * @author FIDELE
 */
public class BorderView extends HView {

    private List<String> texts;
    private boolean borderTop;
    private boolean borderBottom;
    private boolean borderLeft;
    private boolean borderRight;
    private float thickness;
    private Color color;
    private float padding;

    /**
     * Espace vertical laissé entre deux blocs empilés.
     */
    private static final int SPACE_BETWEEN_BLOCKS = 6;

    // ===================== CONSTRUCTORS =====================
    /**
     * Constructeur par défaut sans texte ni bordure.
     */
    public BorderView() {
        this(new ArrayList<>(), false, false, false, false, 1f, Color.BLACK, 4f);
    }

    /**
     * Constructeur avec un seul texte et configuration complète.
     */
    public BorderView(String text, boolean top, boolean bottom, boolean left, boolean right,
            float thickness, Color color, float padding) {
        this(add(text), top, bottom, left, right, thickness, color, padding);
    }

    /**
     * Constructeur avec un seul texte et configuration simplifiée.
     */
    public BorderView(String text, boolean top, boolean bottom, boolean left, boolean right) {
        this(text, top, bottom, left, right, 1f, Color.BLACK, 4f);
    }

    /**
     * Constructeur avec un seul texte et une bordure complète.
     */
    public BorderView(String text) {
        this(text, true, true, true, true, 1f, Color.BLACK, 4f);
    }

    /**
     * Constructeur avec un seul texte, une bordure complète et une couleur
     * personnalisée.
     */
    public BorderView(String text, Color color) {
        this(text, true, true, true, true, 1f, color, 4f);
    }

    /**
     * Constructeur avec liste de textes et configuration complète.
     */
    public BorderView(List<String> texts, boolean top, boolean bottom, boolean left, boolean right,
            float thickness, Color color, float padding) {
        this.texts = new ArrayList<>(texts);
        this.borderTop = top;
        this.borderBottom = bottom;
        this.borderLeft = left;
        this.borderRight = right;
        this.thickness = thickness;
        this.color = color;
        this.padding = padding;
    }

    /**
     * Constructeur avec liste de textes et configuration simplifiée.
     */
    public BorderView(List<String> texts, boolean top, boolean bottom, boolean left, boolean right) {
        this(texts, top, bottom, left, right, 1f, Color.BLACK, 4f);
    }

    /**
     * Constructeur avec liste de textes et bordure complète.
     */
    public BorderView(List<String> texts) {
        this(texts, true, true, true, true, 1f, Color.BLACK, 4f);
    }

    /**
     * Constructeur avec liste de textes, bordure complète et couleur
     * personnalisée.
     */
    public BorderView(List<String> texts, Color color) {
        this(texts, true, true, true, true, 1f, color, 4f);
    }

    // ===================== GETTERS =====================
    public List<String> getTexts() {
        return Collections.unmodifiableList(texts);
    }

    public String getText(int index) {
        if (index >= 0 && index < texts.size()) {
            return texts.get(index);
        }
        return null;
    }

    public boolean isBorderTop() {
        return borderTop;
    }

    public boolean isBorderBottom() {
        return borderBottom;
    }

    public boolean isBorderLeft() {
        return borderLeft;
    }

    public boolean isBorderRight() {
        return borderRight;
    }

    public float getThickness() {
        return thickness;
    }

    public Color getColor() {
        return color;
    }

    public float getPadding() {
        return padding;
    }

    // ===================== SETTERS =====================
    public void setTexts(List<String> texts) {
        this.texts = new ArrayList<>(texts);
    }

    public void setText(int index, String text) {
        if (index >= 0 && index < texts.size()) {
            texts.set(index, text);
        }
    }

    public void addText(String text) {
        this.texts.add(text);
    }

    public void removeText(int index) {
        if (index >= 0 && index < texts.size()) {
            texts.remove(index);
        }
    }

    public void clearTexts() {
        texts.clear();
    }

    public void setBorderTop(boolean borderTop) {
        this.borderTop = borderTop;
    }

    public void setBorderBottom(boolean borderBottom) {
        this.borderBottom = borderBottom;
    }

    public void setBorderLeft(boolean borderLeft) {
        this.borderLeft = borderLeft;
    }

    public void setBorderRight(boolean borderRight) {
        this.borderRight = borderRight;
    }

    /**
     * Configure tous les côtés de la bordure en une seule fois.
     */
    public void setBorder(boolean top, boolean bottom, boolean left, boolean right) {
        this.borderTop = top;
        this.borderBottom = bottom;
        this.borderLeft = left;
        this.borderRight = right;
    }

    /**
     * Active ou désactive tous les côtés de la bordure.
     */
    public void setFullBorder(boolean active) {
        this.borderTop = active;
        this.borderBottom = active;
        this.borderLeft = active;
        this.borderRight = active;
    }

    public void setThickness(float thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("L'épaisseur ne peut pas être négative");
        }
        this.thickness = thickness;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("La couleur ne peut pas être null");
        }
        this.color = color;
    }

    public void setPadding(float padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Le padding ne peut pas être négatif");
        }
        this.padding = padding;
    }

    // ===================== UTILITY METHODS =====================
    private static List<String> add(String text) {
        List<String> list = new ArrayList<>();
        if (text != null) {
            list.add(text);
        }
        return list;
    }

    /**
     * Vérifie si la liste de textes est vide.
     */
    public boolean isEmpty() {
        return texts.isEmpty();
    }

    /**
     * Retourne le nombre de blocs de texte.
     */
    public int size() {
        return texts.size();
    }

    // ===================== PAINT =====================
    @Override
    public void Paint(Graphics g, int x, int y) {
        if (g == null || texts == null || texts.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            Color originalTextColor = g2.getColor();
            FontMetrics fm = g2.getFontMetrics();
            int currentY = y;

            for (String text : texts) {
                String content = (text != null) ? text : "";

                int textWidth = fm.stringWidth(content);
                int blockWidth = textWidth + Math.round(padding * 2);
                int blockHeight = fm.getHeight() + Math.round(padding * 2);

                drawBorder(g2, x, currentY, blockWidth, blockHeight);

                g2.setColor(originalTextColor);
                int xText = x + Math.round(padding);
                int yText = currentY + Math.round(padding) + fm.getAscent();
                g2.drawString(content, xText, yText);

                currentY += blockHeight + SPACE_BETWEEN_BLOCKS;
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawBorder(Graphics2D g2, int x, int y, int width, int height) {
        if (!borderTop && !borderBottom && !borderLeft && !borderRight) {
            return;
        }

        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        if (borderTop) {
            g2.drawLine(x, y, x + width, y);
        }
        if (borderBottom) {
            g2.drawLine(x, y + height, x + width, y + height);
        }
        if (borderLeft) {
            g2.drawLine(x, y, x, y + height);
        }
        if (borderRight) {
            g2.drawLine(x + width, y, x + width, y + height);
        }
    }
}
