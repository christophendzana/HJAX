package hcomponents.vues;

import java.awt.Color;

/**
 * Énumération des styles prédéfinis pour le composant HComboBox.
 */
public enum HComboBoxStyle {

    PRIMARY(
        new Color(255, 255, 255),
        new Color(240, 245, 255),
        new Color(255, 255, 255),
        new Color(13, 110, 253),
        new Color(240, 245, 255),
        new Color(33, 37, 41),
        Color.WHITE,
        new Color(13, 110, 253),
        new Color(13, 110, 253)
    ),
    // autres variantes inchangées...
    SECONDARY(
        new Color(255, 255, 255),
        new Color(240, 242, 245),
        new Color(255, 255, 255),
        new Color(108, 117, 125),
        new Color(240, 242, 245),
        new Color(33, 37, 41),
        Color.WHITE,
        new Color(108, 117, 125),
        new Color(108, 117, 125)
    ),
    SUCCESS(
        new Color(255, 255, 255),
        new Color(240, 250, 245),
        new Color(255, 255, 255),
        new Color(25, 135, 84),
        new Color(240, 250, 245),
        new Color(33, 37, 41),
        Color.WHITE,
        new Color(25, 135, 84),
        new Color(25, 135, 84)
    ),
    DANGER(
        new Color(255, 255, 255),
        new Color(255, 240, 242),
        new Color(255, 255, 255),
        new Color(220, 53, 69),
        new Color(255, 240, 242),
        new Color(33, 37, 41),
        Color.WHITE,
        new Color(220, 53, 69),
        new Color(220, 53, 69)
    ),
    WARNING(
        new Color(255, 255, 255),
        new Color(255, 250, 235),
        new Color(255, 255, 255),
        new Color(255, 193, 7),
        new Color(255, 250, 235),
        new Color(33, 37, 41),
        Color.BLACK,
        new Color(255, 193, 7),
        new Color(193, 142, 0)
    ),
    INFO(
        new Color(255, 255, 255),
        new Color(235, 250, 255),
        new Color(255, 255, 255),
        new Color(13, 202, 240),
        new Color(235, 250, 255),
        new Color(33, 37, 41),
        Color.BLACK,
        new Color(13, 202, 240),
        new Color(13, 202, 240)
    ),
    LIGHT(
        new Color(248, 249, 250),
        new Color(233, 236, 239),
        new Color(255, 255, 255),
        new Color(233, 236, 239),
        new Color(248, 249, 250),
        new Color(33, 37, 41),
        new Color(33, 37, 41),
        new Color(206, 212, 218),
        new Color(108, 117, 125)
    ),
    DARK(
        new Color(52, 58, 64),
        new Color(73, 80, 87),
        new Color(52, 58, 64),
        new Color(108, 117, 125),
        new Color(73, 80, 87),
        Color.WHITE,
        Color.WHITE,
        new Color(73, 80, 87),
        Color.WHITE
    ),
    OCEAN(
        new Color(255, 255, 255),
        new Color(224, 242, 241),
        new Color(255, 255, 255),
        new Color(0, 150, 136),
        new Color(224, 242, 241),
        new Color(33, 37, 41),
        Color.WHITE,
        new Color(0, 150, 136),
        new Color(0, 150, 136)
    ),
    PURPLE(
        new Color(255, 255, 255),
        new Color(243, 229, 245),
        new Color(255, 255, 255),
        new Color(156, 39, 176),
        new Color(243, 229, 245),
        new Color(33, 37, 41),
        Color.WHITE,
        new Color(156, 39, 176),
        new Color(156, 39, 176)
    );

    private final Color background;
    private final Color hoverBackground;
    private final Color popupBackground;
    private final Color selectedBackground;
    private final Color itemHoverBackground;
    private final Color textColor;
    private final Color selectedTextColor;
    private final Color borderColor;
    private final Color arrowColor;

    private HComboBoxStyle(Color bg, Color hoverBg, Color popupBg, Color selectedBg,
                          Color itemHoverBg, Color text, Color selectedText,
                          Color border, Color arrow) {
        this.background = bg;
        this.hoverBackground = hoverBg;
        this.popupBackground = popupBg;
        this.selectedBackground = selectedBg;
        this.itemHoverBackground = itemHoverBg;
        this.textColor = text;
        this.selectedTextColor = selectedText;
        this.borderColor = border;
        this.arrowColor = arrow;
    }

    public Color getBackground() { return background; }
    public Color getHoverBackground() { return hoverBackground; }
    public Color getPopupBackground() { return popupBackground; }
    public Color getSelectedBackground() { return selectedBackground; }
    public Color getItemHoverBackground() { return itemHoverBackground; }
    public Color getTextColor() { return textColor; }
    public Color getSelectedTextColor() { return selectedTextColor; }
    public Color getBorderColor() { return borderColor; }
    public Color getArrowColor() { return arrowColor; }
}