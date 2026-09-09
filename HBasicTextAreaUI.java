package htextarea;

import htextarea.effect.HEffectPainter;
import htextarea.paragraph.HParagraphPainter;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTextPaneUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.text.StyledDocument;
import IllustrationShape.HShape;
import IllustrationShape.HShapeResizer;
import IllustrationShape.HShapeResizer.HandleType;
import IllustrationShape.HView;
import IllustrationShape.model.HTextContent;
import IllustrationShape.model.ViewEvent;
import IllustrationShape.model.ViewModelSelection;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * UI delegate moderne pour {@link HTextArea}.
 *
 * @author FIDELE
 *
 */
public class HBasicTextAreaUI extends BasicTextPaneUI {

    // États internes pour le rendu
    private boolean hover = false;
    private boolean focus = false;

    // Référence au composant (casté en HTextArea)
    private HTextAreaShape textArea;

    private final ShapeMouseHandler shapeMouseHandler = new ShapeMouseHandler();
    private final Timer hoverTimer = new Timer(400, e -> onHoverTimeout());

    private HandleType activeHandle;
    private Integer activeAdjustmentIndex;
    private boolean movingShape;
    private HShape draggedShape;
    private ViewEvent.Type draggedEventType;
    private int lastMouseX, lastMouseY;
    private HShape hoveredShape;

    // Listeners pour détecter les changements d'état
    private final MouseAdapter mouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            hover = true;
            getComponent().repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            hover = false;
            getComponent().repaint();
        }
    };

    private final FocusAdapter focusListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            focus = true;
            getComponent().repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            focus = false;
            getComponent().repaint();
        }
    };

    // -------------------------------------------------------------------------
    // Installation / désinstallation
    // -------------------------------------------------------------------------
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (!(c instanceof HTextArea)) {
            return;
        }
        this.textArea = (HTextAreaShape) c;
        textArea.setOpaque(false);      // nécessaire pour la transparence des coins

        // Ajout des listeners
        textArea.addMouseListener(mouseListener);
        textArea.addFocusListener(focusListener);
        textArea.addMouseListener(shapeMouseHandler);
        textArea.addMouseMotionListener(shapeMouseHandler);
        hoverTimer.setRepeats(false);
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (textArea != null) {
            textArea.removeMouseListener(mouseListener);
            textArea.removeFocusListener(focusListener);
            textArea.removeMouseListener(shapeMouseHandler);
            textArea.removeMouseMotionListener(shapeMouseHandler);
            hoverTimer.stop();
        }
        super.uninstallUI(c);
    }

    /**
     * Point d'entrée du rendu.
     *
     */
    @Override
    protected void paintSafely(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int w = textArea.getWidth();
            int h = textArea.getHeight();
            int radius = textArea.getCornerRadius();

            // 1. Ombre portée
            if (textArea.isComponentShadowEnabled()) {
                paintComponentShadow(g2, w, h, radius,
                        0.15f, textArea.getComponentShadowOffset());
            }

            // 2. Fond arrondi
            paintBackground(g2, w, h, radius);

            // 3. Bordure arrondie du composant
            paintBorder(g2, w, h, radius);

            // Clip arrondi
            Shape oldClip = g2.getClip();
            g2.clip(new RoundRectangle2D.Float(0, 0, w, h, radius * 2, radius * 2));

            // Translation des insets — à partir d'ici, le g2 est dans
            // l'espace vue, cohérent avec modelToView()
            Insets insets = textArea.getInsets();
            g2.translate(insets.left, insets.top);

            // 4. Effets de caractère (derrière le texte)
            HEffectPainter.peindreEffets(
                    g2,
                    (StyledDocument) textArea.getDocument(),
                    getRootView(textArea)
            );

            // 5. Trame de fond (derrière le texte)
            // g2 est translaté — HParagraphPainter travaille dans l'espace vue
            HParagraphPainter.peindreArrierePlan(
                    g2,
                    (StyledDocument) textArea.getDocument(),
                    getRootView(textArea),
                    textArea
            );

            // Retirer la translation pour super.paintSafely()
            // (Swing applique lui-même les insets en interne)
            g2.translate(-insets.left, -insets.top);

            // 6. Texte Swing
            super.paintSafely(g2);

            // Remettre la translation pour les painters de premier plan
            g2.translate(insets.left, insets.top);

            // 7. Puces, bordures, marques (devant le texte)
            HParagraphPainter.peindrePremierPlan(
                    g2,
                    (StyledDocument) textArea.getDocument(),
                    getRootView(textArea),
                    textArea
            );

            g2.translate(-insets.left, -insets.top);
            g2.setClip(oldClip);

            // 8. Formes IllustrationShape + poignées de la forme sélectionnée
            for (HShape shape : textArea.getViewModel().getShapes()) {
                shape.Paint(g2, 0, 0);

                if (shape instanceof HTextContent content && shape != textArea.getShapeEditor().getEditingShape()) {
                    Graphics2D textG = (Graphics2D) g2.create();
                    textG.translate(shape.getX(), shape.getY());
                    textArea.getShapeRenderer().getRendererComponent(shape, content).paint(textG);
                    textG.dispose();
                }
            }

            for (HShapeResizer resizer : textArea.getShapeResizers()) {
                resizer.Paint(g2, 0, 0);
            }

        } finally {
            g2.dispose();
        }

    }

    /**
     * Peint l'ombre portée du composant. Simule un flou en dessinant plusieurs
     * couches translucides décalées.
     *
     * @param g2 le contexte graphique 2D
     * @param w largeur du composant
     * @param h hauteur du composant
     * @param radius rayon des coins arrondis
     */
    private void paintComponentShadow(Graphics2D g2, int width, int height, int cornerRadius, float shadowOpacity, int shadowOffset) {
        int shadowSize = Math.max(5, (int) (textArea.getComponentShadowBlur() * 2));
        float alphaMax = shadowOpacity * (textArea.getComponentShadowColor().getAlpha() / 255f);
        for (int i = 0; i < shadowSize; i++) {
            float alpha = (shadowSize - i) / (float) shadowSize * alphaMax;
            g2.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
            RoundRectangle2D shadow = new RoundRectangle2D.Float(
                    shadowOffset - i, shadowOffset - i,
                    width - shadowOffset * 2 + i * 2, height - shadowOffset * 2 + i * 2,
                    cornerRadius + i, cornerRadius + i
            );
            g2.draw(shadow);
        }
    }

    /**
     * Peint le fond arrondi en utilisant la couleur correspondant à l'état
     * actuel.
     *
     * @param g2 contexte graphique
     * @param w largeur
     * @param h hauteur
     * @param radius rayon des coins
     */
    private void paintBackground(Graphics2D g2, int w, int h, int radius) {
        Color bg;
        if (focus) {
            bg = textArea.getBackgroundFocus();
        } else if (hover) {
            bg = textArea.getBackgroundHover();
        } else {
            bg = textArea.getBackgroundNormal();
        }
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, radius * 2, radius * 2);
    }

    /**
     * Peint la bordure arrondie avec l'épaisseur et la couleur d'état.
     *
     * @param g2 contexte graphique
     * @param w largeur
     * @param h hauteur
     * @param radius rayon des coins
     */
    private void paintBorder(Graphics2D g2, int w, int h, int radius) {
        int thickness = textArea.getBorderThickness();
        if (thickness <= 0) {
            return;
        }

        Color borderColor;
        if (focus) {
            borderColor = textArea.getBorderFocus();
        } else if (hover) {
            borderColor = textArea.getBorderHover();
        } else {
            borderColor = textArea.getBorderNormal();
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(thickness));
        // On dessine un rectangle arrondi légèrement décalé pour que le trait soit centré sur le bord
        int adjust = thickness / 2;
        g2.drawRoundRect(adjust, adjust,
                w - thickness,
                h - thickness,
                radius * 2, radius * 2);
    }

    // -------------------------------------------------------------------------
    // Méthodes héritées – on ne modifie pas paintBackground(Graphics) d'origine,
    // mais on la surcharge pour qu'elle ne fasse rien (car on peint tout dans paintSafely).
    // -------------------------------------------------------------------------
    /**
     * Surchargée pour ne rien faire : le fond est déjà peint dans
     * {@link #paintSafely(Graphics)}.
     *
     * @param g ignoré
     */
    @Override
    protected void paintBackground(Graphics g) {
        // Rien – on évite le fond rectangulaire par défaut
    }

    private HShape getSelectedShape() {
        return (textArea.getSelectionModel().getViewSelected() instanceof HShape s) ? s : null;
    }

    private void updateHoveredShape(HShape shape) {
        if (shape == hoveredShape) {
            return;
        }
        if (hoveredShape != null) {
            textArea.getViewModel().viewHoverExited(hoveredShape);
        }
        hoveredShape = shape;
        hoverTimer.stop();
        if (hoveredShape != null) {
            textArea.getViewModel().viewHoverEntered(hoveredShape);
            if (textArea.getSelectionModel().isSelectionEmpty()) {
                hoverTimer.start();
            }
        }
    }

    private void onHoverTimeout() {
        if (textArea.getSelectionModel().isSelectionEmpty() && hoveredShape != null) {
            textArea.getSelectionModel().addViewSelected(hoveredShape);
        }
    }

    private class ShapeMouseHandler implements MouseListener, MouseMotionListener {

        @Override
        public void mousePressed(MouseEvent e) {
            for (HShapeResizer resizer : textArea.getShapeResizers()) {
                HandleType handle = resizer.handleAt(e.getX(), e.getY());
                if (handle != null) {
                    activeHandle = handle;
                    draggedShape = resizer.getTargetShape();
                    return;
                }
            }

            HShape primary = getSelectedShape();
            if (primary != null) {
                Integer index = textArea.getViewModel().adjustmentHandleAt(primary, e.getX(), e.getY());
                if (index != null) {
                    activeAdjustmentIndex = index;
                    draggedShape = primary;
                    return;
                }
            }

            HShape clicked = textArea.getViewModel().findShapeAt(e.getX(), e.getY());
            ViewModelSelection selection = textArea.getSelectionModel();

            if (clicked == null) {
                selection.setSelectionMode(ViewModelSelection.SINGLE_SELECTION);
                selection.clearSelection();
            } else if (e.isControlDown()) {
                if (selection.getSelectionMode() == ViewModelSelection.SINGLE_SELECTION) {
                    selection.setSelectionMode(ViewModelSelection.MULTIPLE_SELECTION);
                }
                if (selection.isSelectedView(clicked)) {
                    selection.removeViewSelected(clicked);
                } else {
                    selection.addViewSelected(clicked);
                }
            } else {
                if (!selection.isSelectedView(clicked)) {
                    selection.setSelectionMode(ViewModelSelection.SINGLE_SELECTION);
                    selection.clearSelection();
                    selection.addViewSelected(clicked);
                }
            }

            if (!selection.isSelectionEmpty()) {
                movingShape = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggedShape != null && activeHandle != null) {
                int beforeX = draggedShape.getX();
                int beforeY = draggedShape.getY();
                int beforeWidth = draggedShape.getWidth();
                int beforeHeight = draggedShape.getHeight();
                double beforeRotation = draggedShape.getRotationDegrees();

                activeHandle.drag(draggedShape, e.getX(), e.getY());

                int dx = draggedShape.getX() - beforeX;
                int dy = draggedShape.getY() - beforeY;
                int dWidth = draggedShape.getWidth() - beforeWidth;
                int dHeight = draggedShape.getHeight() - beforeHeight;
                double dRotation = draggedShape.getRotationDegrees() - beforeRotation;

                for (HView view : textArea.getSelectionModel().getListViewSelected()) {
                    if (view instanceof HShape shape && shape != draggedShape) {
                        shape.setX(shape.getX() + dx);
                        shape.setY(shape.getY() + dy);
                        shape.setWidth(shape.getWidth() + dWidth);
                        shape.setHeight(shape.getHeight() + dHeight);
                        shape.setRotationDegrees(shape.getRotationDegrees() + dRotation);
                    }
                }

                draggedEventType = activeHandle.getActionEventType();

            } else if (draggedShape != null && activeAdjustmentIndex != null) {
                draggedShape.applyAdjustmentDrag(activeAdjustmentIndex, e.getX(), e.getY());
                draggedEventType = ViewEvent.Type.VIEW_ADJUSTED;

            } else if (movingShape) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                lastMouseX = e.getX();
                lastMouseY = e.getY();

                for (HView view : textArea.getSelectionModel().getListViewSelected()) {
                    if (view instanceof HShape shape) {
                        shape.setX(shape.getX() + dx);
                        shape.setY(shape.getY() + dy);
                    }
                }

                draggedShape = getSelectedShape();
                draggedEventType = ViewEvent.Type.VIEW_MOVED;
            }

            if (draggedShape != null && draggedEventType != null) {
                textArea.getViewModel().notifyViewEvent(draggedShape, draggedEventType, true);
            }
            textArea.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (draggedShape != null && draggedEventType != null) {
                textArea.getViewModel().notifyViewEvent(draggedShape, draggedEventType, false);
            }
            activeHandle = null;
            activeAdjustmentIndex = null;
            movingShape = false;
            draggedShape = null;
            draggedEventType = null;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateHoveredShape(textArea.getViewModel().findShapeAt(e.getX(), e.getY()));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            updateHoveredShape(null);
            hoverTimer.stop();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() != 2) {
                return;
            }
            HShape shape = textArea.getViewModel().findShapeAt(e.getX(), e.getY());
            if (shape instanceof HTextContent) {
                textArea.getShapeEditor().startEditing(shape, textArea);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }
    }

}
