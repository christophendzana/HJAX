package hcomponents;

import hcomponents.vues.HBasicTextAreaUI;
import hcomponents.vues.HTextAreaStyle;
import hcomponents.vues.border.HAbstractBorder;
import javax.swing.JTextArea;

/**
 * Composant HTextArea - Un text area Swing personnalisé avec design moderne.
 * 
 * @author FIDELE
 * @version 1.0
 * @see JTextArea
 */
public class HTextArea extends JTextArea {
    
    /** Rayon des coins arrondis (en pixels) */
    private int cornerRadius = 10;
    
    
    /** Style visuel appliqué au text area */
    private HTextAreaStyle textAreaStyle = HTextAreaStyle.PRIMARY;

    /**
     * Constructeur par défaut.
     */
    public HTextArea() {
        super();
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec texte initial.
     * 
     * @param text le texte initial
     */
    public HTextArea(String text) {
        super(text);
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec nombre de lignes et colonnes.
     * 
     * @param rows le nombre de lignes
     * @param columns le nombre de colonnes
     */
    public HTextArea(int rows, int columns) {
        super(rows, columns);
        updateUI();
        configureDefaults();
    }

    /**
     * Constructeur avec texte, lignes et colonnes.
     * 
     * @param text le texte initial
     * @param rows le nombre de lignes
     * @param columns le nombre de colonnes
     */
    public HTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
        updateUI();
        configureDefaults();
    }

    /**
     * Configure les valeurs par défaut.
     */
    private void configureDefaults() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(false);
        setBorder(null);        
    }

    /**
     * Met à jour l'interface utilisateur.
     */
    @Override
    public void updateUI() {
        setUI(new HBasicTextAreaUI());
    }

    /**
     * Retourne le rayon des coins arrondis.
     * 
     * @return le rayon en pixels
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Définit le rayon des coins arrondis.
     * 
     * @param radius le nouveau rayon en pixels
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    /**
     * Retourne le style visuel actuel.
     * 
     * @return le HTextAreaStyle appliqué
     */
    public HTextAreaStyle getTextAreaStyle() {
        return textAreaStyle;
    }

    /**
     * Définit le style visuel du text area.
     * 
     * @param style le nouveau style à appliquer
     */
    public void setTextAreaStyle(HTextAreaStyle style) {
        this.textAreaStyle = style;
        repaint();
    }

    /**
     * Méthode factory pour créer un HTextArea avec un style prédéfini.
     * 
     * @param text le texte initial
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HTextArea configurée
     */
    public static HTextArea withStyle(String text, HTextAreaStyle style) {
        HTextArea textArea = new HTextArea(text);
        textArea.setTextAreaStyle(style);
        return textArea;
    }

    /**
     * Méthode factory pour créer un HTextArea vide avec un style prédéfini.
     * 
     * @param style le style visuel à appliquer
     * @return une nouvelle instance de HTextArea configurée
     */
    public static HTextArea withStyle(HTextAreaStyle style) {
        HTextArea textArea = new HTextArea();
        textArea.setTextAreaStyle(style);
        return textArea;
    }

  
}