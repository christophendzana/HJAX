package htextarea;


import javax.swing.*;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import java.awt.*;

/**
 * Delegate d'interface utilisateur pour {@link HTextArea}.
 *
 * <p>Hérite de {@link BasicTextPaneUI} puisque {@code HTextArea} étend désormais
 * {@link JTextPane}. Cette classe est responsable du rendu visuel : fond arrondi
 * et délégation du rendu du texte stylisé au pipeline standard de {@code JTextPane}.</p>
 *
 * <p>Deux points importants sur les signatures imposées par {@code BasicTextUI} :</p>
 * <ul>
 *   <li>{@code paintBackground(Graphics)} — prend un seul argument (pas de JComponent).</li>
 *   <li>{@code paint(Graphics, JComponent)} est {@code final} — on passe par
 *       {@code paintSafely(Graphics)} pour injecter notre rendu personnalisé.</li>
 * </ul>
 *
 * @author FIDELE
 * @version 2.0
 * @see BasicTextPaneUI
 */
public class HBasicTextAreaUI extends BasicTextPaneUI {

    // -------------------------------------------------------------------------
    // Rendu du fond
    // -------------------------------------------------------------------------

    /**
     * Peint le fond du composant avec des coins arrondis.
     *
     * <p>Signature imposée par {@link javax.swing.plaf.basic.BasicTextUI} :
     * le composant est récupéré via {@code getComponent()} et non en paramètre.</p>
     *
     * @param g le contexte graphique fourni par Swing
     */
    @Override
    protected void paintBackground(Graphics g) {
        JComponent c = getComponent();

        if (!(c instanceof HTextArea textArea)) {
            super.paintBackground(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int rayon   = textArea.getCornerRadius();
            int largeur = c.getWidth();
            int hauteur = c.getHeight();

            g2.setColor(resoudreCouleurFond(textArea));
            g2.fillRoundRect(0, 0, largeur, hauteur, rayon * 2, rayon * 2);

        } finally {
            g2.dispose();
        }
    }

    /**
     * Résout la couleur de fond selon le {@link HTextAreaStyle} actif.
     *
     * @param textArea le composant dont on lit le style
     * @return la couleur de fond à appliquer
     */
    private Color resoudreCouleurFond(HTextArea textArea) {
        HTextAreaStyle style = textArea.getTextAreaStyle();
        if (style == null) return textArea.getBackground();

        return switch (style) {
            case PRIMARY   -> textArea.getBackground();
            case SECONDARY -> textArea.getBackground().darker();
        };
    }


    // -------------------------------------------------------------------------
    // Rendu complet : effets typographiques + texte standard
    // -------------------------------------------------------------------------

    /**
     * Point d'entrée du rendu personnalisé.
     *
     * <p>{@code paint(Graphics, JComponent)} étant {@code final} dans
     * {@link javax.swing.plaf.basic.BasicTextUI}, on surcharge {@code paintSafely}
     * qui est appelé après les vérifications de sécurité Swing.</p>
     *
     * <p>Ordre des passes de rendu :</p>
     * <ol>
     *   <li>Fond arrondi ({@link #paintBackground(Graphics)}) — déjà géré
     *       en interne par {@code super.paintSafely}.</li>
     *   <li>Effets typographiques ({@link HEffectPainter}) — peints <em>avant</em>
     *       le texte pour apparaître derrière lui (ombre, halo…).</li>
     *   <li>Texte stylisé — délégué au pipeline standard de {@code BasicTextPaneUI}
     *       via {@code super.paintSafely(g)}.</li>
     * </ol>
     *
     * @param g le contexte graphique
     */
    @Override
    protected void paintSafely(Graphics g) {
        JComponent c = getComponent();

        // Passe 1 : effets typographiques (ombre, contour, relief, glow…)
        // On les peint AVANT le texte pour qu'ils apparaissent derrière
        if (c instanceof HTextArea textArea) {
            StyledDocument doc  = textArea.getStyledDocument();
            View           view = getRootView(textArea);
            HEffectPainter.peindreEffets(g, doc, view);
        }

        // Passe 2 : fond arrondi + texte stylisé (pipeline standard)
        super.paintSafely(g);
    }


    // -------------------------------------------------------------------------
    // Installation
    // -------------------------------------------------------------------------

    /**
     * Installe ce delegate sur le composant.
     *
     * <p>On force {@code opaque = false} pour que notre fond personnalisé
     * (avec coins arrondis) soit visible. Sans ça, Swing peindrait un
     * rectangle plein par-dessus nos arrondis.</p>
     *
     * @param c le composant cible
     */
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        c.setOpaque(false);
    }
}
