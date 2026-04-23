package htextarea;

import java.awt.Color;

/**
 * Styles visuels disponibles pour {@link hcomponents.HTextArea}.
 *
 * <p>Chaque constante représente un thème visuel global du composant
 * (couleur de fond, bordure…). Les styles typographiques par caractère
 * (gras, couleur du texte…) sont gérés séparément via les méthodes
 * de {@code HTextArea} et le {@code StyledDocument} interne.</p>
 *
 * <p>Pour ajouter un nouveau style, il suffit :</p>
 * <ol>
 *   <li>D'ajouter une constante ici.</li>
 *   <li>D'ajouter le {@code case} correspondant dans
 *       {@link hcomponents.vues.HBasicTextAreaUI#resoudreCouleurFond}.</li>
 * </ol>
 *
 * @author FIDELE
 * @version 2.0
 */
public enum HTextAreaStyle {

     PRIMARY(
        new Color(255, 255, 255),      // backgroundColor
        new Color(250, 250, 255),      // hoverBackground
        new Color(240, 245, 255),      // focusBackground
        new Color(206, 212, 218),      // borderColor
        new Color(200, 215, 230),      // hoverBorderColor
        new Color(13, 110, 253),       // focusBorderColor
        new Color(33, 37, 41),         // textColor
        new Color(0, 0, 0, 30)         // shadowColor
    ),
    
    SUCCESS(
        new Color(255, 255, 255),
        new Color(240, 250, 245),
        new Color(235, 248, 240),
        new Color(206, 212, 218),
        new Color(180, 210, 190),
        new Color(25, 135, 84),
        new Color(33, 37, 41),
        new Color(0, 0, 0, 30)
    ),
    
    DANGER(
        new Color(255, 255, 255),
        new Color(255, 240, 242),
        new Color(255, 235, 238),
        new Color(206, 212, 218),
        new Color(220, 180, 185),
        new Color(220, 53, 69),
        new Color(33, 37, 41),
        new Color(0, 0, 0, 30)
    ),
    
    WARNING(
        new Color(255, 255, 255),
        new Color(255, 250, 235),
        new Color(255, 248, 225),
        new Color(206, 212, 218),
        new Color(220, 200, 150),
        new Color(255, 193, 7),
        new Color(33, 37, 41),
        new Color(0, 0, 0, 30)
    ),
    
    INFO(
        new Color(255, 255, 255),
        new Color(235, 250, 255),
        new Color(225, 245, 255),
        new Color(206, 212, 218),
        new Color(180, 210, 230),
        new Color(13, 202, 240),
        new Color(33, 37, 41),
        new Color(0, 0, 0, 30)
    ),
    
    DARK(
        new Color(52, 58, 64),
        new Color(73, 80, 87),
        new Color(73, 80, 87),
        new Color(108, 117, 125),
        new Color(173, 181, 189),
        new Color(13, 110, 253),
        new Color(248, 249, 250),
        new Color(0, 0, 0, 80)
    );
    
    private final Color backgroundColor;
    private final Color hoverBackground;
    private final Color focusBackground;
    private final Color borderColor;
    private final Color hoverBorderColor;
    private final Color focusBorderColor;
    private final Color textColor;
    private final Color shadowColor;
    
    private HTextAreaStyle(Color bg, Color hoverBg, Color focusBg, Color border, 
                          Color hoverBorder, Color focusBorder, Color text, Color shadow) {
        this.backgroundColor = bg;
        this.hoverBackground = hoverBg;
        this.focusBackground = focusBg;
        this.borderColor = border;
        this.hoverBorderColor = hoverBorder;
        this.focusBorderColor = focusBorder;
        this.textColor = text;
        this.shadowColor = shadow;
    }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public Color getHoverBackground() { return hoverBackground; }
    public Color getFocusBackground() { return focusBackground; }
    public Color getBorderColor() { return borderColor; }
    public Color getHoverBorderColor() { return hoverBorderColor; }
    public Color getFocusBorderColor() { return focusBorderColor; }
    public Color getTextColor() { return textColor; }
    public Color getShadowColor() { return shadowColor; }
}
