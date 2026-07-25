package view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTree;

/**
 * Dessine un ou plusieurs blocs de texte avec une trame de fond colorée
 * derrière chaque bloc.
 * 
 * @author FIDELE
 */
public class BackgroundView extends HView {

    private List<String> texts;
    private Color backgroundColor;
    private float padding;

    /**
     * Espace vertical laissé entre deux blocs .
     */
    private static final int SPACE_BETWEEN_BLOCKS = 6;

    /**
     * Couleur de fond par défaut .
     */
    private static final Color DEFAULT_COLOR = new Color(230, 230, 150);

    /**
     * Padding par défaut .
     */
    private static final float DEFAULT_PADDING = 4f;
    
    public BackgroundView() {
        this(new ArrayList<>(), DEFAULT_COLOR, DEFAULT_PADDING);
    }

    
    public BackgroundView(String text) {
        this(add(text), DEFAULT_COLOR, DEFAULT_PADDING);
    }

    
    public BackgroundView(List<String> texts) {
        this(texts, DEFAULT_COLOR, DEFAULT_PADDING);
    }

    
    public BackgroundView(String text, Color backgroundColor) {
        this(add(text), backgroundColor, DEFAULT_PADDING);
    }

    
    public BackgroundView(List<String> texts, Color backgroundColor) {
        this(texts, backgroundColor, DEFAULT_PADDING);
    }

    
    public BackgroundView(String text, float padding) {
        this(add(text), DEFAULT_COLOR, padding);
    }

    
    public BackgroundView(List<String> texts, float padding) {
        this(texts, DEFAULT_COLOR, padding);
    }

    
    public BackgroundView(String text, Color backgroundColor, float padding) {
        this(add(text), backgroundColor, padding);
    }

    
    public BackgroundView(List<String> texts, Color backgroundColor, float padding) {
        this.texts = new ArrayList<>(texts);
        this.backgroundColor = backgroundColor != null ? backgroundColor : DEFAULT_COLOR;
        this.padding = padding >= 0 ? padding : DEFAULT_PADDING;
    }

    
    public BackgroundView(String text, Color backgroundColor, float padding, int spaceBetween) {
        this(add(text), backgroundColor, padding);        
    }


    public List<String> getTexts() {
        return Collections.unmodifiableList(texts);
    }

    public String getText(int index) {
        if (index >= 0 && index < texts.size()) {
            return texts.get(index);
        }
        return null;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public float getPadding() {
        return padding;
    }

    /**
     * Vérifie si la liste de textes est vide.
     */
    public boolean isEmpty() {
        return texts.isEmpty();
    }
    
    public void setTexts(List<String> texts) {
        this.texts = new ArrayList<>(texts);
    }

    public void setText(int index, String text) {
        if (index >= 0 && index < texts.size()) {
            texts.set(index, text);
        }
    }

    public void addText(String text) {
        if (text != null) {
            this.texts.add(text);
        }
    }

    public void removeText(int index) {
        if (index >= 0 && index < texts.size()) {
            texts.remove(index);
        }
    }

    public void removeText(String text) {
        texts.remove(text);
    }

    public void clearTexts() {
        texts.clear();
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("La couleur de fond ne peut pas être null");
        }
        this.backgroundColor = backgroundColor;
    }

    public void setPadding(float padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("Le padding ne peut pas être négatif");
        }
        this.padding = padding;
    }
    
    private static List<String> add(String text) {
        List<String> list = new ArrayList<>();
        if (text != null) {
            list.add(text);
        }
        return list;
    }
    
    @Override
    public void Paint(Graphics g, int x, int y) {
        if (g == null || texts == null || texts.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // On récupère la couleur du texte
            Color originalTextColor = g2.getColor();

            FontMetrics fm = g2.getFontMetrics();
            int currentY = y;

            for (String text : texts) {
                String content = (text != null) ? text : "";

                int textWidth = fm.stringWidth(content);
                int blockWidth = textWidth + Math.round(padding * 2);
                int blockHeight = fm.getHeight() + Math.round(padding * 2);

                g2.setColor(backgroundColor);
                g2.fillRect(x, currentY, blockWidth, blockHeight);

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
    
}
