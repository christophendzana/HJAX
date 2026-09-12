package IllustrationShape;

import IllustrationShape.model.HTextContent;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class HShapeEditor {

    private final JTextArea component = new JTextArea();

    private HShape editingShape;
    private HTextContent editingContent;
    private JLayeredPane layeredPane;
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
        JRootPane rootPane = SwingUtilities.getRootPane(host);
        if (rootPane == null) {
            return; // host pas encore affiché dans une fenêtre réalisée
        }
        if (isEditing()) {
            stopEditing();
        }

        this.editingShape = shape;
        this.editingContent = content;
        this.host = host;
        this.layeredPane = rootPane.getLayeredPane();
        this.savedRotationDegrees = shape.getRotationDegrees();
        shape.setRotationDegrees(0);

        component.setText(content.getText());

        // Bornes calculées en coordonnées de la JLayeredPane, pas de host —
        // c'est ce qui rend le positionnement indépendant du LayoutManager
        // de host : setBounds() ici fait autorité, sans concurrence.
        Point positionInLayeredPane = SwingUtilities.convertPoint(
                host, shape.getX(), shape.getY(), layeredPane);
        component.setBounds(positionInLayeredPane.x, positionInLayeredPane.y,
                shape.getWidth(), shape.getHeight());

        layeredPane.add(component, JLayeredPane.POPUP_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();
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
        layeredPane.remove(component);
        layeredPane.revalidate();
        layeredPane.repaint();
        host.repaint();
        editingShape = null;
        editingContent = null;
        host = null;
        layeredPane = null;
    }
}