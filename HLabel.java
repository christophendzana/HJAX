package hcomponents;

import hcomponents.vues.HBasicLabelUI;
import hcomponents.vues.HLabelStyle;
import hcomponents.vues.HLabelOrientation;
import java.awt.Font;
import javax.swing.JLabel;

/**
 * Composant HLabel - Un label Swing personnalisé avec design moderne.
 * Supporte l'orientation horizontale et verticale.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JLabel
 */
public class HLabel extends JLabel {
    
    /** Style visuel appliqué au label */
    private HLabelStyle labelStyle = HLabelStyle.PRIMARY;
    
    /** Orientation du texte */
    private HLabelOrientation orientation = HLabelOrientation.HORIZONTAL;
    
    /** Rayon des coins arrondis (pour fond) */
    private int cornerRadius = 8;
    
    /** Afficher un fond arrondi */
    private boolean hasRoundedBackground = false;
    
    /** Padding interne */
    private int padding = 8;

    /**
     * Constructeur par défaut.
     */
    public HLabel() {
        super();
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec texte.
     * 
     * @param text le texte du label
     */
    public HLabel(String text) {
        super(text);
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec texte et orientation.
     * 
     * @param text le texte du label
     * @param orientation l'orientation du texte
     */
    public HLabel(String text, HLabelOrientation orientation) {
        super(text);
        this.orientation = orientation;
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec texte, style et orientation.
     * 
     * @param text le texte du label
     * @param style le style visuel
     * @param orientation l'orientation du texte
     */
    public HLabel(String text, HLabelStyle style, HLabelOrientation orientation) {
        super(text);
        this.labelStyle = style;
        this.orientation = orientation;
        updateUI();
        configureDefaults();
    }

    /**
     * Configure les valeurs par défaut.
     */
    private void configureDefaults() {
        setOpaque(false);
        setBorder(null);
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        applyStyleColors();
    }

    /**
     * Met à jour l'interface utilisateur.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicLabelUI());
    }

    /**
     * Applique les couleurs du style.
     */
    private void applyStyleColors() {
        setForeground(labelStyle.getTextColor());
        repaint();
    }

    // GETTERS ET SETTERS

    public HLabelStyle getLabelStyle() {
        return labelStyle;
    }

    public void setLabelStyle(HLabelStyle style) {
        this.labelStyle = style;
        applyStyleColors();
        repaint();
    }

    public HLabelOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(HLabelOrientation orientation) {
        this.orientation = orientation;
        revalidate();
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public boolean hasRoundedBackground() {
        return hasRoundedBackground;
    }

    public void setHasRoundedBackground(boolean hasRoundedBackground) {
        this.hasRoundedBackground = hasRoundedBackground;
        repaint();
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
        repaint();
    }

    // MÉTHODES FACTORY

    public static HLabel create(String text) {
        return new HLabel(text);
    }

    public static HLabel withStyle(String text, HLabelStyle style) {
        HLabel label = new HLabel(text);
        label.setLabelStyle(style);
        return label;
    }

    public static HLabel withOrientation(String text, HLabelOrientation orientation) {
        HLabel label = new HLabel(text);
        label.setOrientation(orientation);
        return label;
    }

    public static HLabel createStyled(String text, HLabelStyle style, 
                                      HLabelOrientation orientation) {
        HLabel label = new HLabel(text, style, orientation);
        return label;
    }

    public static HLabel createPill(String text, HLabelStyle style) {
        HLabel label = new HLabel(text);
        label.setLabelStyle(style);
        label.setHasRoundedBackground(true);
        label.setCornerRadius(20); // Hauteur/2 pour effet pilule
        label.setPadding(12);
        return label;
    }
}