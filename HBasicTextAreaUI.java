package htextarea;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import javax.swing.text.StyledDocument;

/**
 * UI delegate moderne pour {@link HTextArea}.
 * <p>
 * Gère :
 * <ul>
 * <li>Fond arrondi avec couleurs différentes selon l'état (normal, hover,
 * focus)</li>
 * <li>Bordure arrondie personnalisable (épaisseur, couleur selon état)</li>
 * <li>Ombre portée du composant (drop shadow)</li>
 * <li>Délégation du rendu du texte stylisé au parent
 * {@link BasicTextPaneUI}</li>
 * </ul>
 * <p>
 * L'état "hover" est détecté via un {@link MouseAdapter}, l'état "focus" via un
 * {@link FocusAdapter}.
 * </p>
 *
 * @author FIDELE
 * @version 3.0
 */
public class HBasicTextAreaUI extends BasicTextPaneUI {

    // États internes pour le rendu
    private boolean hover = false;
    private boolean focus = false;

    // Référence au composant (casté en HTextArea)
    private HTextArea textArea;

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
        this.textArea = (HTextArea) c;
        textArea.setOpaque(false);      // nécessaire pour la transparence des coins

        // Ajout des listeners
        textArea.addMouseListener(mouseListener);
        textArea.addFocusListener(focusListener);
    }

    @Override
    public void uninstallUI(JComponent c) {
        if (textArea != null) {
            textArea.removeMouseListener(mouseListener);
            textArea.removeFocusListener(focusListener);
        }
        super.uninstallUI(c);
    }

    @Override
protected void paintSafely(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w      = textArea.getWidth();
        int h      = textArea.getHeight();
        int radius = textArea.getCornerRadius();

        // 1. Ombre du composant
        if (textArea.isComponentShadowEnabled()) {
            paintComponentShadow(g2, w, h, radius, 0.15f,
                    textArea.getComponentShadowOffset());
        }

        // 2. Fond arrondi
        paintBackground(g2, w, h, radius);

        // 3. Bordure arrondie
        paintBorder(g2, w, h, radius);

        // 4. Clip arrondi pour que le texte ne déborde pas sur les coins
        Shape oldClip = g2.getClip();
        g2.clip(new RoundRectangle2D.Float(0, 0, w, h, radius * 2, radius * 2));

        // 5. Correction critique : les coordonnées retournées par modelToView()
        //    sont dans l'espace du composant (0,0 = coin supérieur gauche du JTextPane).
        //    Or, Swing paint le texte en tenant compte des insets du border.
        //    On doit appliquer le même décalage au Graphics2D passé à HEffectPainter
        //    pour que les effets soient dessinés exactement sur le texte.
        Insets insets = textArea.getInsets();
        g2.translate(insets.left, insets.top);

        HEffectPainter.peindreEffets(
                g2,
                (StyledDocument) textArea.getDocument(),
                getRootView(textArea)
        );

        // On retire la translation avant d'appeler super.paintSafely(),
        // car Swing gère lui-même les insets dans sa propre passe de rendu.
        g2.translate(-insets.left, -insets.top);

        super.paintSafely(g2);
        g2.setClip(oldClip);

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
}
