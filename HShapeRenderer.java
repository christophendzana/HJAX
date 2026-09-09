package IllustrationShape;

import IllustrationShape.model.HTextContent;
import javax.swing.JTextArea;
import java.awt.Component;

/**
 * Rendu réutilisable (flyweight) du texte d'une HShape implémentant
 * HTextContent. Une seule instance, reconfigurée et repeinte pour chaque
 * forme, jamais ajoutée à la hiérarchie de composants — comme
 * TableCellRenderer dans JTable.
 */
public class HShapeRenderer {

    private final JTextArea component = new JTextArea();

    public HShapeRenderer() {
        component.setEditable(false);
        component.setFocusable(false);
        component.setOpaque(false);
        component.setLineWrap(true);
        component.setWrapStyleWord(true);
        component.setBorder(null);
    }

    public Component getRendererComponent(HShape shape, HTextContent content) {
        component.setText(content.getText());
        component.setSize(shape.getWidth(), shape.getHeight());
        return component;
    }
}