package hcomponents;

import hcomponents.vues.HBasicComboBoxUI;
import hcomponents.vues.HComboBoxRenderer;
import hcomponents.vues.HComboBoxStyle;

import javax.swing.*;
import java.util.Vector;

/**
 * ComboBox personnalisée avec design moderne.
 */
public class HComboBox<E> extends JComboBox<E> {

    private static final long serialVersionUID = 1L;

    private HComboBoxStyle comboStyle = HComboBoxStyle.PRIMARY;
    private int cornerRadius = 8;
    private boolean animationsEnabled = true;
    private boolean hoverEnabled = true;
    private int arrowSize = 8;

    public HComboBox() {
        super();
        init();
    }

    public HComboBox(E[] items) {
        super(items);
        init();
    }

    public HComboBox(Vector<E> items) {
        super(items);
        init();
    }

    public HComboBox(ComboBoxModel<E> model) {
        super(model);
        init();
    }

    private void init() {
        // Renderer par défaut (pour valeur courante et popup si pas remplacé)
        setRenderer(new HComboBoxRenderer());
        updateUI();
    }

    @Override
    public void updateUI() {
        // appel à super pour que LookAndFeel se réinstalle proprement
        super.updateUI();
        // installer notre UI personnalisée
        setUI(new HBasicComboBoxUI());
        // s'assurer que le renderer et les couleurs sont appliqués
        setRenderer(new HComboBoxRenderer());
        applyStyle();
        revalidate();
        repaint();
    }

    private void applyStyle() {
        if (comboStyle != null) {
            setForeground(comboStyle.getTextColor());
            setBackground(comboStyle.getBackground());
        }
    }

    public HComboBoxStyle getComboStyle() {
        return comboStyle;
    }

    public void setComboStyle(HComboBoxStyle style) {
        this.comboStyle = style;
        applyStyle();
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }

    public boolean isHoverEnabled() {
        return hoverEnabled;
    }

    public void setHoverEnabled(boolean enabled) {
        this.hoverEnabled = enabled;
    }

    public int getArrowSize() {
        return arrowSize;
    }

    public void setArrowSize(int size) {
        this.arrowSize = size;
        repaint();
    }

    public static <T> HComboBox<T> withStyle(HComboBoxStyle style) {
        HComboBox<T> combo = new HComboBox<>();
        combo.setComboStyle(style);
        return combo;
    }

    public static <T> HComboBox<T> withStyle(T[] items, HComboBoxStyle style) {
        HComboBox<T> combo = new HComboBox<>(items);
        combo.setComboStyle(style);
        return combo;
    }
}