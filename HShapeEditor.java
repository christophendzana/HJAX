package IllustrationShape;

import IllustrationShape.model.HTextContent;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Éditeur de texte réutilisable pour les HShape implémentant HTextContent.
 * Un seul composant, temporairement ajouté à l'hôte pendant l'édition —
 * une seule édition possible à la fois, comme dans JTable.
 */
public class HShapeEditor {

    private final JTextArea component = new JTextArea();

    private HShape editingShape;
    private HTextContent editingContent;
    private JComponent host;
    private double savedRotationDegrees;

    public HShapeEditor() {
        component.setLineWrap(true);
        component.setWrapStyleWord(true);

        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stopEditing();
            }
        });

        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }
            }
        });
    }

    public boolean isEditing() {
        return editingShape != null;
    }

    public HShape getEditingShape() {
        return editingShape;
    }

    public void startEditing(HShape shape, JComponent host) {
        if (!(shape instanceof HTextContent content)) {
            return;
        }
        if (isEditing()) {
            stopEditing();
        }

        this.editingShape = shape;
        this.editingContent = content;
        this.host = host;
        this.savedRotationDegrees = shape.getRotationDegrees();
        shape.setRotationDegrees(0);

        component.setText(content.getText());
        component.setBounds(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());

        host.add(component);
        host.revalidate();
        host.repaint();
        component.requestFocusInWindow();
    }

    public void stopEditing() {
        if (!isEditing()) {
            return;
        }
        editingContent.setText(component.getText());
        finishEditing();
    }

    public void cancelEditing() {
        if (!isEditing()) {
            return;
        }
        finishEditing();
    }

    private void finishEditing() {
        editingShape.setRotationDegrees(savedRotationDegrees);
        host.remove(component);
        host.revalidate();
        host.repaint();
        editingShape = null;
        editingContent = null;
        host = null;
    }
}